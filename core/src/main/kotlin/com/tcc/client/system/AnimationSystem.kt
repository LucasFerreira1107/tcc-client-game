package com.tcc.client.system

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.tcc.client.component.AnimationComponent
import com.tcc.client.component.AnimationComponent.Companion.NO_ANIMATION
import com.tcc.client.component.ImageComponent
import ktx.app.gdxError
import ktx.collections.map
import ktx.log.logger

/**
 * Sistema ECS (Fleks 2.11) responsável por gerenciar e atualizar animações de entidades.
 * 
 * Este sistema processa todas as entidades que possuem tanto AnimationComponent quanto ImageComponent.
 * Ele é responsável por:
 * 1. Detectar mudanças de animação (quando nextAnimation != NO_ANIMATION)
 * 2. Carregar e cachear animações do TextureAtlas
 * 3. Atualizar o stateTime a cada frame
 * 4. Aplicar o frame correto da animação ao ImageComponent da entidade
 * 
 * O sistema usa cache para evitar recriar animações já carregadas, melhorando performance.
 * 
 * @property atlas TextureAtlas contendo todas as texturas e regiões de animação
 * 
 * @see AnimationComponent Componente que armazena o estado da animação
 * @see ImageComponent Componente que recebe os frames da animação para renderização
 */
class AnimationSystem(
    /**
     * Atlas de texturas contendo todas as regiões de animação.
     * As chaves no atlas devem seguir o formato "atlasKey/animationType"
     * (ex: "player/idle", "slime/run", "chest/open")
     */
    private val atlas: TextureAtlas
) : IteratingSystem(
    // Define a família de entidades que este sistema processa
    // Requer que a entidade tenha AMBOS os componentes: AnimationComponent E ImageComponent
    family {
        all(AnimationComponent, ImageComponent)
    }
) {
    /**
     * Cache de animações já carregadas.
     * 
     * Chave: caminho completo da animação (ex: "player/idle")
     * Valor: objeto Animation com os frames já processados
     * 
     * Evita recriar animações a cada mudança, melhorando significativamente a performance.
     */
    private val cachedAnimations = mutableMapOf<String, Animation<TextureRegionDrawable>>()

    /**
     * Método chamado para cada entidade que possui AnimationComponent e ImageComponent.
     * 
     * Este método:
     * 1. Verifica se há uma nova animação pendente (nextAnimation != NO_ANIMATION)
     * 2. Se sim, carrega a nova animação, reseta o stateTime e aplica o primeiro frame
     * 3. Se não, incrementa o stateTime e aplica o frame correspondente da animação atual
     * 
     * @param entity A entidade sendo processada (garantidamente possui ambos os componentes)
     */
    override fun onTickEntity(entity: Entity) {
        val aniCmp = entity[AnimationComponent]
        
        // Acessa o ImageComponent para atualizar o drawable (frame atual da animação)
        with(entity[ImageComponent]) {
            // Verifica se há uma nova animação pendente
            image.drawable = if (aniCmp.nextAnimation != NO_ANIMATION) {
                // NOVA ANIMAÇÃO: Carrega a animação, reseta o tempo e aplica o primeiro frame
                aniCmp.run {
                    // Carrega a animação do cache (ou cria se não existir)
                    animation = animation(aniCmp.nextAnimation)
                    
                    // Limpa a flag de próxima animação (já foi processada)
                    clearAnimation()
                    
                    // Reseta o tempo para começar do início
                    stateTime = 0f
                    
                    // Aplica o modo de reprodução configurado
                    animation.playMode = playerMode
                    
                    // Retorna o primeiro frame da nova animação
                    animation.getKeyFrame(0f)
                }
            } else {
                // ANIMAÇÃO ATUAL: Incrementa o tempo e aplica o frame correspondente
                aniCmp.run {
                    // Incrementa o tempo decorrido desde o último frame
                    // deltaTime é fornecido pelo IteratingSystem (tempo entre frames)
                    stateTime += deltaTime
                    
                    // Garante que o modo de reprodução está correto (pode ter mudado)
                    animation.playMode = playerMode
                    
                    // Retorna o frame correspondente ao tempo atual
                    animation.getKeyFrame(aniCmp.stateTime)
                }
            }
        }
    }

    /**
     * Carrega uma animação do atlas e a retorna (usando cache quando possível).
     * 
     * Este método:
     * 1. Verifica se a animação já está em cache
     * 2. Se sim, retorna a animação cached
     * 3. Se não, busca todas as regiões do atlas com a chave fornecida,
     *    cria a animação e a armazena no cache
     * 
     * O atlas deve conter múltiplas regiões com o mesmo nome base seguido de números
     * (ex: "player/idle", "player/idle.1", "player/idle.2", etc.) ou usar findRegions()
     * que busca todas as variações automaticamente.
     * 
     * @param atlasKey Caminho completo da animação no atlas (ex: "player/idle", "slime/run")
     * @return Animation com os frames da animação
     * @throws GdxRuntimeException se a chave não for encontrada no atlas
     */
    private fun animation(atlasKey: String): Animation<TextureRegionDrawable> {
        return cachedAnimations.getOrPut(atlasKey) {
            // Log para debug (apenas quando cria nova animação, não quando usa cache)
            LOG.debug { "Criando animação para $atlasKey" }
            
            // Busca todas as regiões no atlas que correspondem à chave
            // findRegions() retorna uma lista de TextureRegion (um para cada frame)
            val regions = atlas.findRegions(atlasKey)
            
            // Validação: garante que pelo menos uma região foi encontrada
            if (regions.isEmpty) {
                gdxError("Não foi possível encontrar regiões para a chave da atlas: $atlasKey")
            }
            
            // Cria a animação com:
            // - Duração de cada frame: DEFAULT_FRAME_DURATION (1/8 segundos = 8 FPS)
            // - Frames: lista de TextureRegionDrawable (compatível com Scene2D)
            Animation(DEFAULT_FRAME_DURATION, regions.map { TextureRegionDrawable(it) })
        }
    }

    companion object {
        /**
         * Logger para mensagens de debug relacionadas ao AnimationSystem.
         */
        private val LOG = logger<AnimationSystem>()
        
        /**
         * Duração padrão de cada frame da animação (em segundos).
         * 
         * Valor: 1/8F = 0.125 segundos = 8 frames por segundo
         * 
         * Este valor pode ser ajustado para animações mais rápidas ou lentas.
         * Para 60 FPS de animação, use 1/60F.
         * Para 12 FPS, use 1/12F.
         */
        private const val DEFAULT_FRAME_DURATION = 1 / 8F
    }
}
