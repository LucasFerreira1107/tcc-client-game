package com.tcc.client.component
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Component
import com.tcc.client.enum.AnimationType

/**
 * Componente ECS (Fleks 2.11) que gerencia animações de sprites para entidades.
 * 
 * Este componente armazena o estado de animação de uma entidade, incluindo:
 * - A animação atual (sequência de frames)
 * - O tempo decorrido desde o início da animação (stateTime)
 * - O modo de reprodução (loop, normal, reverse, etc.)
 * - A próxima animação a ser reproduzida (se houver mudança de estado)
 * 
 * O componente trabalha em conjunto com o AnimationSystem, que atualiza
 * o stateTime e aplica os frames corretos ao ImageComponent da entidade.
 * 
 * @property atlasKey Chave base no atlas (ex: "player", "slime", "chest")
 *                    Usada para construir o caminho completo da animação
 * @property stateTime Tempo acumulado desde o início da animação atual (em segundos)
 *                    Incrementado pelo AnimationSystem a cada frame
 * @property playerMode Modo de reprodução da animação:
 *                     - LOOP: Repete infinitamente
 *                     - NORMAL: Reproduz uma vez e para
 *                     - REVERSED: Reproduz ao contrário
 *                     - LOOP_REVERSED: Loop ao contrário
 * @property animation A animação atual (sequência de frames)
 *                    Inicializada pelo AnimationSystem quando necessário
 * @property nextAnimation Caminho completo da próxima animação a ser reproduzida
 *                        Formato: "atlasKey/animationType" (ex: "player/idle", "slime/run")
 *                        Quando diferente de NO_ANIMATION, o AnimationSystem troca a animação
 * 
 * @see AnimationSystem Sistema que processa este componente
 * @see AnimationType Enum com os tipos de animação disponíveis
 */
data class AnimationComponent(
    /**
     * Chave base no atlas que identifica o conjunto de animações de uma entidade.
     * Exemplos: "player", "slime", "chest"
     * Esta chave é combinada com o AnimationType para formar o caminho completo.
     */
    var atlasKey: String = "",
    
    /**
     * Tempo decorrido desde o início da animação atual (em segundos).
     * Incrementado pelo AnimationSystem a cada frame usando deltaTime.
     * Usado para calcular qual frame da animação deve ser exibido.
     */
    var stateTime: Float = 0f,
    
    /**
     * Modo de reprodução da animação.
     * Por padrão é LOOP, fazendo a animação repetir infinitamente.
     */
    var playerMode: Animation.PlayMode = Animation.PlayMode.LOOP
) : Component<AnimationComponent> {
    /**
     * A animação atual sendo reproduzida.
     * Contém a sequência de frames (TextureRegionDrawable) e a duração de cada frame.
     * Inicializada pelo AnimationSystem quando uma nova animação é solicitada.
     */
    lateinit var animation: Animation<TextureRegionDrawable>
    
    /**
     * Retorna o tipo do componente (usado internamente pelo Fleks).
     */
    override fun type() = AnimationComponent
    
    /**
     * Caminho completo da próxima animação a ser reproduzida.
     * Formato: "atlasKey/animationType" (ex: "player/idle", "slime/run")
     * 
     * Quando diferente de NO_ANIMATION, o AnimationSystem detecta a mudança
     * e troca para a nova animação, resetando o stateTime.
     * 
     * É private set para garantir que apenas os métodos nextAnimation() possam alterá-lo.
     */
    var nextAnimation: String = NO_ANIMATION
        private set

    /**
     * Define a próxima animação usando apenas o tipo (assume que atlasKey já está definido).
     * 
     * Constrói o caminho completo como "atlasKey/animationType".
     * Exemplo: se atlasKey = "player" e type = IDLE, nextAnimation = "player/idle"
     * 
     * @param type Tipo da animação (IDLE, RUN, ATTACK, etc.)
     */
    fun nextAnimation(type: AnimationType) {
        nextAnimation = "$atlasKey/${type.atlasKey}"
    }

    /**
     * Define a próxima animação especificando tanto a chave do atlas quanto o tipo.
     * 
     * Útil quando o atlasKey ainda não foi definido ou precisa ser alterado.
     * Constrói o caminho completo como "atlasKey/animationType".
     * 
     * @param atlasKey Chave base no atlas (ex: "player", "slime")
     * @param type Tipo da animação (IDLE, RUN, ATTACK, etc.)
     */
    fun nextAnimation(atlasKey: String, type: AnimationType) {
        this.atlasKey = atlasKey
        nextAnimation = "$atlasKey/${type.atlasKey}"
    }

    /**
     * Limpa a próxima animação, indicando que não há mudança de animação pendente.
     * 
     * Após ser chamado, o AnimationSystem continuará reproduzindo a animação atual.
     */
    fun clearAnimation() {
        nextAnimation = NO_ANIMATION
    }

    /**
     * Verifica se a animação atual terminou (útil para animações não-loop).
     * 
     * @return true se a animação terminou, false caso contrário
     */
    fun isAnimationFinished() = animation.isAnimationFinished(stateTime)

    /**
     * Objeto companion que define o tipo único do componente no sistema ECS.
     * Fleks usa isso para identificar e agrupar componentes do mesmo tipo.
     */
    companion object : ComponentType<AnimationComponent>() {
        /**
         * Constante que indica que não há próxima animação pendente.
         * Quando nextAnimation == NO_ANIMATION, o AnimationSystem continua
         * reproduzindo a animação atual sem trocar.
         */
        const val NO_ANIMATION = ""
    }
}

