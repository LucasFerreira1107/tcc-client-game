package com.tcc.client

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.disposeSafely
import ktx.assets.toInternalFile
import ktx.async.KtxAsync
import ktx.graphics.use
import com.tcc.client.screen.GameScreen

/**
 * Classe principal da aplicação cliente do jogo.
 *
 * Esta classe herda de KtxGame, que é uma extensão Kotlin do ApplicationListener do LibGDX.
 * É responsável por inicializar a aplicação, configurar o nível de log e gerenciar as telas do jogo.
 *
 * @see KtxGame Documentação da classe base KtxGame da biblioteca KTX
 */
class Main : KtxGame<KtxScreen>() {

    /**
     * Método chamado quando a aplicação é criada/inicializada.
     *
     * Este método:
     * 1. Inicializa o sistema assíncrono KTX para carregamento de assets
     * 2. Define o nível de log para DEBUG (útil durante desenvolvimento)
     * 3. Adiciona a tela de jogo (GameScreen) ao gerenciador de telas
     * 4. Define a GameScreen como a tela inicial do jogo
     */
    override fun create() {
        // Inicializa o sistema assíncrono KTX para carregamento de recursos em background
        KtxAsync.initiate()

        // Define o nível de log para DEBUG - permite ver mensagens detalhadas durante desenvolvimento
        Gdx.app.logLevel = Application.LOG_DEBUG

        // Tela de exemplo comentada (pode ser usada para testes ou menu inicial)
        // addScreen(FirstScreen())
        // setScreen<FirstScreen>()

        // Adiciona a tela de jogo ao gerenciador de telas
        addScreen(GameScreen())

        // Define a GameScreen como a tela ativa inicial
        setScreen<GameScreen>()
    }

    companion object {
        const val UNIT_SCALE = 1/16F
    }
}

/**
 * Tela de exemplo (atualmente comentada/desabilitada).
 *
 * Esta tela demonstra um uso básico do LibGDX:
 * - Carrega uma textura (logo.png)
 * - Usa SpriteBatch para desenhar
 * - Gerencia o ciclo de vida (dispose)
 *
 * Pode ser usada como referência ou para testes simples.
 * Atualmente não está sendo utilizada no fluxo principal do jogo.
 */
class FirstScreen : KtxScreen {
    // Carrega uma textura com filtro linear (melhor qualidade ao redimensionar)
    private val image = Texture("logo.png".toInternalFile(), true).apply { setFilter(Linear, Linear) }

    // SpriteBatch é usado para desenhar sprites/texturas na tela
    private val batch = SpriteBatch()

    /**
     * Método chamado a cada frame para renderizar a tela.
     *
     * @param delta Tempo decorrido desde o último frame (em segundos)
     */
    override fun render(delta: Float) {
        // Limpa a tela com uma cor cinza claro (RGB: 0.7, 0.7, 0.7)
        clearScreen(red = 0.7f, green = 0.7f, blue = 0.7f)

        // Usa o SpriteBatch para desenhar a imagem na posição (100, 160)
        // O método 'use' garante que o batch seja iniciado e finalizado corretamente
        batch.use {
            it.draw(image, 100f, 160f)
        }
    }

    /**
     * Método chamado quando a tela é descartada.
     *
     * Libera recursos alocados (texturas, batches, etc.) para evitar memory leaks.
     * O método 'disposeSafely' verifica se o objeto não é nulo antes de descartar.
     */
    override fun dispose() {
        image.disposeSafely()
        batch.disposeSafely()
    }
}
