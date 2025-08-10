package com.barium.client.render;

import com.barium.BariumMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class BariumRenderManager {
    private static final BariumRenderManager INSTANCE = new BariumRenderManager();
    public static BariumRenderManager getInstance() { return INSTANCE; }

    // Usando os getters corretos e estáveis para os RenderLayers
    private static final List<RenderLayer> CHUNK_LAYERS = List.of(
        RenderLayer.getSolid(), 
        RenderLayer.getCutoutMipped(), 
        RenderLayer.getCutout(), 
        RenderLayer.getTranslucentMovingBlock() // Substituto para getTranslucent()
    );

    private final Map<Long, RenderableChunk> chunks = new ConcurrentHashMap<>();
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private ExecutorService mesherExecutor;
    private StreamingBuffer streamingBuffer;
    private ChunkMesher chunkMesher;
    private World world;

    private void init() {
        this.mesherExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.streamingBuffer = new StreamingBuffer(256 * 1024 * 1024);
        this.chunkMesher = new ChunkMesher();
        BariumMod.LOGGER.info("Barium Render Manager inicializado (on-demand).");
    }

    private void ensureInitialized() {
        if (this.initialized.compareAndSet(false, true)) {
            this.init();
        }
    }

    /**
     * NOVO MÉTODO: Permite que outros Mixins (como o BufferBuilderMixin) saibam
     * se nosso renderizador está ativo e se devem silenciar o vanilla.
     * @return true se o renderizador customizado estiver inicializado.
     */
    public boolean isActive() {
        return this.initialized.get();
    }

    public void onWorldChange(@Nullable ClientWorld newWorld) {
        this.ensureInitialized();
        this.world = newWorld;
        if (!this.chunks.isEmpty()) {
            this.chunks.values().forEach(RenderableChunk::delete);
            this.chunks.clear();
        }
    }

    public void scheduleRebuild(int sectionX, int sectionY, int sectionZ, boolean isPriority) {
        this.ensureInitialized();
        if (this.world == null) return;
        
        BlockPos origin = new BlockPos(sectionX << 4, sectionY << 4, sectionZ << 4);
        RenderableChunk chunk = this.chunks.computeIfAbsent(origin.asLong(), k -> new RenderableChunk(origin));
        
        if (chunk.needsRebuild()) {
            this.mesherExecutor.submit(new ChunkRebuildTask(chunk, this.world, this.chunkMesher));
        }
    }

    public void uploadMeshedChunk(RenderableChunk chunk) {
        if (!this.initialized.get()) return;
        ChunkMesher.Result result = chunk.getMeshResult();
        if (result == null || result.isEmpty()) return;
        
        for (Map.Entry<RenderLayer, ByteBuffer> entry : result.layerBuffers().entrySet()) {
            // A upload agora é tratada pelo RenderableChunk, que usa o buffer global
            chunk.upload(entry.getKey(), entry.getValue());
        }
        result.free();
        chunk.setMeshResult(null);
    }

    public void render(MatrixStack matrices, Camera camera, Frustum frustum) {
        if (!this.isActive()) return;

        double camX = camera.getPos().getX();
        double camY = camera.getPos().getY();
        double camZ = camera.getPos().getZ();

        matrices.push();
        matrices.translate(-camX, -camY, -camZ);
        
        for (RenderLayer layer : CHUNK_LAYERS) {
            layer.startDrawing();
            
            for (RenderableChunk chunk : this.chunks.values()) {
                if (frustum != null && !frustum.isVisible(chunk.getBoundingBox())) {
                    continue;
                }
                matrices.push();
                matrices.translate(chunk.origin.getX(), chunk.origin.getY(), chunk.origin.getZ());
                // TODO: Passar a matriz para o shader antes de desenhar
                chunk.draw(layer);
                matrices.pop();
            }
            
            layer.endDrawing();
        }
        
        matrices.pop();
    }

    private static class ChunkRebuildTask implements Runnable {
        private final RenderableChunk chunk;
        private final World world;
        private final ChunkMesher mesher;

        public ChunkRebuildTask(RenderableChunk chunk, World world, ChunkMesher mesher) {
            this.chunk = chunk;
            this.world = world;
            this.mesher = mesher;
        }

        @Override
        public void run() {
            ChunkMesher.Result result = this.mesher.mesh(this.world, chunk.origin);
            MinecraftClient.getInstance().execute(() -> {
                chunk.setMeshResult(result);
                BariumRenderManager.getInstance().uploadMeshedChunk(chunk);
            });
        }
    }

    // O método de shutdown não foi adicionado antes, é importante
    public void shutdown() {
        if (this.initialized.get()) {
            if (this.mesherExecutor != null) this.mesherExecutor.shutdown();
            if (this.streamingBuffer != null) this.streamingBuffer.delete();
        }
    }
}