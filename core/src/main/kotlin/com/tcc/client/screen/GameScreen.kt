package com.tcc.client.screen

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.configureWorld
import com.tcc.client.component.AnimationComponent
import com.tcc.client.component.ImageComponent
import com.tcc.client.enum.AnimationType
import com.tcc.client.event.MapChangeEvent
import com.tcc.client.event.fire
import com.tcc.client.system.AnimationSystem
import com.tcc.client.system.EntitySpawnSystem
import com.tcc.client.system.RenderSystem
import ktx.app.KtxScreen
import ktx.artemis.entity
import ktx.assets.disposeSafely
import ktx.log.logger

/**
 * Tela principal do jogo onde a gameplay acontece.
 *
 * Esta classe implementa a tela de jogo usando:
 * - LibGDX para renderização e gerenciamento de viewport
 * - Fleks 2.11 como ECS (Entity Component System) para gerenciamento de entidades
 * - Scene2D para renderização de UI e sprites
 *
 * A tela é responsável por:
 * 1. Configurar o World do ECS (sistemas e dependências)
 * 2. Criar entidades iniciais (ex: jogador)
 * 3. Atualizar o World a cada frame
 * 4. Gerenciar o viewport e redimensionamento
 * 5. Liberar recursos quando descartada
 */
class GameScreen : KtxScreen {
    /**
     * SpriteBatch para desenho direto de sprites (atualmente não usado diretamente,
     * pois a renderização é feita através do Stage/Scene2D).
     * Pode ser usado para desenho customizado ou debug.
     */
    private val spriteBatch: Batch = SpriteBatch()

    /**
     * Stage do Scene2D do LibGDX.
     *
     * O Stage gerencia actors (elementos visuais) como imagens, labels, botões, etc.
     * É usado para renderização hierárquica e gerenciamento de UI.
     *
     * ExtendViewport com proporção 16:9:
     * - Mantém a proporção da tela mesmo em diferentes resoluções
     * - Expande/encolhe a view para manter a aspect ratio
     */

    private val stage: Stage = Stage(ExtendViewport(16f, 9f))

    /**
     * TextureAtlas contendo todas as texturas e animações do jogo.
     *
     * Um TextureAtlas é um arquivo que agrupa múltiplas texturas em uma única imagem,
     * melhorando a performance ao reduzir chamadas de desenho.
     *
     * O atlas contém:
     * - Regiões de animação no formato "atlasKey/animationType"
     *   Exemplos: "player/idle", "player/run", "slime/attack", "chest/open"
     * - Cada animação pode ter múltiplos frames (player/idle.1, player/idle.2, etc.)
     *
     * O arquivo .atlas define os bounds (posições) de cada região na imagem.
     * O arquivo .png contém a imagem com todas as texturas.
     *
     * Caminho: "atlas/game-player&slime.atlas" (deve estar em assets/atlas/)
     */
    private val atlasTexture: TextureAtlas = TextureAtlas("graphics/game.atlas")

    /**
     * World do ECS Fleks 2.11.
     *
     * O World é o coração do sistema ECS:
     * - Gerencia todas as entidades do jogo
     * - Mantém os componentes associados às entidades
     * - Executa os sistemas registrados a cada frame
     *
     * Configuração do World:
     * 1. injectables: Registra dependências que podem ser injetadas em sistemas/components
     *    - Stage é registrado para uso nos componentes/sistemas
     * 2. systems: Registra os sistemas que serão executados a cada update
     *    - RenderSystem: Responsável por renderizar entidades com ImageComponent
     */
    private var currentMap: TiledMap? = null
    private val world = configureWorld {
        // Registra dependências que podem ser injetadas em componentes e sistemas
        injectables {
            add(stage)  // Stage disponível para injeção em ImageComponent e RenderSystem
        }

        // Registra os sistemas do jogo (executados na ordem definida)
        systems {
            // Sistema de animação que gerencia animações de entidades com AnimationComponent
            add(AnimationSystem(atlasTexture))

            // Sistema de renderização que desenha entidades com ImageComponent
            add(RenderSystem(stage))

            add(EntitySpawnSystem(atlasTexture))
        }
    }

    /**
     * Método chamado quando a tela é exibida pela primeira vez.
     *
     * Neste método, criamos as entidades iniciais do jogo.
     * Atualmente cria apenas uma entidade representando o jogador.
     */
    override fun show() {
        log.debug { "Tela de jogo esta sendo exibida" }

        world.systems.forEach { system -> if (system is EventListener){
                stage.addListener(system)
            }
        }
        try {
            val tiledMap = TmxMapLoader().load("graphics/map/map-02.tmx")
            // Dispara evento para que o RenderSystem processe o mapa
            stage.fire(MapChangeEvent(tiledMap))
        } catch (e: Exception) {
            log.error(e) { "Erro ao carregar mapa: ${e.message}" }
            // Continua a execução mesmo se o mapa não carregar (para desenvolvimento)
        }

        // Cria uma nova entidade no World
        // A sintaxe 'world.entity { }' é um DSL do Fleks para criação de entidades

    }

        /**
         * Método chamado quando a janela é redimensionada.
         *
         * Atualiza o viewport do Stage para manter a proporção correta
         * e garantir que o jogo seja renderizado corretamente em diferentes resoluções.
         *
         * @param width Nova largura da janela em pixels
         * @param height Nova altura da janela em pixels
         */
        override fun resize(width: Int, height: Int) {
            // Atualiza o viewport com as novas dimensões
            // O terceiro parâmetro (true) indica para centralizar a câmera
            stage.viewport.update(width, height, true)
        }

        /**
         * Método chamado a cada frame para atualizar e renderizar o jogo.
         *
         * Este método é o loop principal do jogo:
         * 1. Atualiza o World do ECS, que por sua vez:
         *    - Atualiza todos os sistemas registrados
         *    - Os sistemas processam as entidades que possuem os componentes correspondentes
         *
         * @param delta Tempo decorrido desde o último frame (em segundos)
         */
        override fun render(delta: Float) {
            // Atualiza o World do ECS
            // Isso executa todos os sistemas registrados (ex: RenderSystem)
            // Os sistemas processam as entidades que possuem os componentes necessários
            world.update(delta)
        }

        /**
         * Método chamado quando a tela é descartada/fechada.
         *
         * Libera todos os recursos alocados para evitar memory leaks:
         * - Stage (e todos os actors dentro dele)
         * - Texturas carregadas
         * - World do ECS (e todas as entidades/componentes/sistemas)
         */
        override fun dispose() {
            // Libera o Stage e todos os actors dentro dele
            stage.disposeSafely()

            // Libera a textura do jogador
            atlasTexture.disposeSafely()

            // Libera o World do ECS e todos os seus recursos
            world.dispose()

            currentMap?.disposeSafely()
        }

        /**
         * Logger para mensagens de debug/informação relacionadas à GameScreen.
         * Usa o sistema de logging do KTX que abstrai o logging do LibGDX.
         */
        companion object {
            private val log = logger<GameScreen>()
        }
}

