package com.barium.client.mixin;

import com.barium.client.util.ChunkRenderManager;
import com.barium.config.BariumConfig;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkBuilder.BuiltChunk.class)
public abstract class ChunkRenderMixin {

    @Shadow public abstract BlockPos getOrigin();

    @Inject(method = "shouldBuild()Z", at = @At("HEAD"), cancellable = true)
    private void barium$onShouldBuild(CallbackInfoReturnable<Boolean> cir) {
        // Se a otimização de frustum estiver desligada, não fazemos nada.
        if (!BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING) {
            return;
        }

        final BlockPos origin = this.getOrigin();
        final int chunkX = origin.getX() >> 4;
        final int chunkZ = origin.getZ() >> 4;

        // A única verificação que faremos aqui é a de Frustum Culling, que é segura.
        // Ela checa se o chunk está dentro do campo de visão da câmera, o que é apropriado
        // para o método shouldBuild(). Isso previne que o jogo construa chunks que já saíram da tela.
        if (!ChunkRenderManager.getInstance().isChunkInFrustum(chunkX, chunkZ)) {
            cir.setReturnValue(false);
        }
    }
}