package com.tcc.client.enum

/**
 * Enum que define os tipos de animação disponíveis no jogo.
 * 
 * Cada tipo de animação corresponde a uma sequência de frames no TextureAtlas.
 * O nome do enum é convertido para lowercase para formar a chave no atlas.
 * 
 * Exemplos de uso:
 * - AnimationType.IDLE → chave no atlas: "idle"
 * - AnimationType.RUN → chave no atlas: "run"
 * - AnimationType.ATTACK → chave no atlas: "attack"
 * 
 * Quando combinado com uma atlasKey (ex: "player"), forma o caminho completo:
 * - "player" + IDLE → "player/idle"
 * - "slime" + RUN → "slime/run"
 * 
 * O atlas deve conter regiões com esses nomes para cada entidade.
 * Por exemplo, para um player, o atlas deve ter:
 * - player/idle (ou player/idle.1, player/idle.2, etc. para múltiplos frames)
 * - player/run
 * - player/attack
 * - player/death
 * 
 * @property atlasKey Chave correspondente no atlas (nome do enum em lowercase)
 */
enum class AnimationType {
    /**
     * Animação de ocioso/parado.
     * Reproduzida quando a entidade não está se movendo ou realizando ações.
     */
    IDLE,
    
    /**
     * Animação de corrida/movimento.
     * Reproduzida quando a entidade está se movendo.
     */
    RUN,
    
    /**
     * Animação de ataque.
     * Reproduzida quando a entidade está atacando.
     */
    ATTACK,
    
    /**
     * Animação de morte.
     * Geralmente reproduzida uma vez (NORMAL mode) quando a entidade morre.
     */
    DEATH,
    
    /**
     * Animação de abertura.
     * Usada para objetos como baús, portas, etc.
     */
    OPEN;

    /**
     * Chave correspondente no TextureAtlas.
     * 
     * Converte o nome do enum para lowercase para formar a chave.
     * Exemplos:
     * - IDLE → "idle"
     * - RUN → "run"
     * - ATTACK → "attack"
     * 
     * Esta chave é combinada com a atlasKey do AnimationComponent
     * para formar o caminho completo: "atlasKey/animationType"
     */
    val atlasKey: String = this.toString().lowercase()
}
