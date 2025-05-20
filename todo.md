# Desenvolvimento do Mod Barium para Minecraft 1.21.5

## Configuração do Ambiente
- [x] Criar diretório para o projeto
- [x] Configurar ambiente de desenvolvimento Fabric para Minecraft 1.21.5
- [x] Configurar dependências (Fabric API)
- [ ] Verificar compatibilidade com Sodium

## Implementação das Otimizações
- [x] Pathfinding e IA de mobs
  - [x] Implementar caching de rotas inteligentes
  - [x] Simplificar verificação de colisão para mobs em repouso
  - [x] Reduzir frequência de atualizações para mobs distantes
- [x] Ticking de blocos e tile entities
  - [x] Implementar sistema de ticking sob demanda
  - [x] Otimizar hoppers
- [x] Eventos de Redstone
  - [x] Limitar propagação desnecessária de sinais
  - [x] Implementar fila compactada para atualizações
- [x] Sistema de partículas e efeitos
  - [x] Implementar culling de partículas fora do campo de visão
  - [x] Implementar LOD para partículas distantes
- [x] Ticking de entidades distantes
  - [x] Implementar congelamento de entidades fora da zona de interesse
- [x] Inventários e containers
  - [x] Implementar cache de slots vazios
  - [x] Otimizar verificação de inventários
- [x] Mundo e salvamento (chunk saving)
  - [x] Implementar bufferização inteligente para chunks
  - [x] Implementar compressão assíncrona de chunks
- [x] Som e áudio
  - [x] Implementar filtragem de sons não audíveis
- [x] Otimização da HUD e textos
  - [x] Implementar redesenho condicional
  - [x] Implementar cache de fontes pré-rasterizadas
  - [x] Reduzir frequência de atualizações da HUD

## Compilação e Distribuição
- [ ] Compilar mod em arquivo JAR
- [ ] Preparar arquivos para GitHub
- [ ] Validar funcionamento e compatibilidade
- [ ] Enviar arquivos ao usuário
