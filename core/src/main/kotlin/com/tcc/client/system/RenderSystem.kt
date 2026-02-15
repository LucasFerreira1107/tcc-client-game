package com.tcc.client.system

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.collection.compareEntity
import com.github.quillraven.fleks.collection.compareEntityBy
import com.tcc.client.Main.Companion.UNIT_SCALE
import com.tcc.client.component.ImageComponent
import com.tcc.client.event.MapChangeEvent
import ktx.graphics.use
import ktx.tiled.forEachLayer

/**
 * Sistema de renderização usando Fleks 2.11.
 *
 * No padrão ECS, um System é responsável por processar entidades que possuem
 * um conjunto específico de componentes. O RenderSystem processa todas as
 * entidades que possuem um ImageComponent.
 *
 * Este sistema:
 * 1. Ordena entidades por ImageComponent (usando a lógica de compareTo do componente)
 * 2. A cada frame, atualiza e desenha o Stage do Scene2D
 * 3. Garante que as imagens das entidades estejam na ordem correta de renderização
 *
 * @property stage O Stage do LibGDX Scene2D usado para renderização
 *
 * @see IteratingSystem Classe base do Fleks para sistemas que iteram sobre entidades
 */
class RenderSystem(
    private val stage: Stage,
) : EventListener, IteratingSystem(
    // Define a família de entidades que este sistema processa
    // 'all(ImageComponent)' significa: todas as entidades que possuem ImageComponent
    family = family {
        all(ImageComponent)
    },
    // Define o comparador para ordenar entidades durante o processamento
    // Usa a implementação de Comparable do ImageComponent para ordenar por layer e posição X
    comparator = compareEntityBy(ImageComponent)
) {
    private val mapRenderer = OrthogonalTiledMapRenderer(null, UNIT_SCALE, stage.batch)
    private var bgdLayer = mutableListOf<TiledMapTileLayer>()
    private var fgdLayer = mutableListOf<TiledMapTileLayer>()

    private val orthoCamera = stage.camera as OrthographicCamera
    /**
     * Método chamado a cada frame antes de processar as entidades.
     *
     * Este método:
     * 1. Aplica as transformações do viewport (posição, zoom, etc.)
     * 2. Atualiza todos os actors do Stage (animações, input, etc.)
     * 3. Desenha todos os actors visíveis do Stage na tela
     */
    override fun onTick() {
        // Chama o método pai para garantir que a lógica base do IteratingSystem seja executada
        super.onTick()

        // Com o Stage, executa o ciclo de renderização:
        with(stage) {
            // Aplica as transformações do viewport (câmera, escalas, etc.)
            viewport.apply()
            AnimatedTiledMapTile.updateAnimationBaseTime()

            mapRenderer.setView(orthoCamera)
            if (bgdLayer.isNotEmpty()) {

                stage.batch.use (orthoCamera.combined){
                    bgdLayer.forEach{ mapRenderer.renderTileLayer(it)}  }
            }
            // Atualiza todos os actors do Stage:
            // - Processa eventos de input (toques, cliques, teclado)
            // - Atualiza animações (actions, timers)
            // - Executa a lógica de atores (se houver)
            act(deltaTime)

            // Desenha todos os actors visíveis do Stage na tela
            // Isso renderiza todas as imagens associadas às entidades
            draw()

            if (fgdLayer.isNotEmpty()) {
                stage.batch.use (orthoCamera.combined){
                    fgdLayer.forEach{ mapRenderer.renderTileLayer(it)}  }
            }
        }
    }

    /**
     * Método chamado para cada entidade que possui ImageComponent.
     *
     * Este método é executado após onTick(), para cada entidade na família,
     * na ordem definida pelo comparador.
     *
     * Atualmente garante que a imagem da entidade esteja na frente de outros
     * elementos da mesma camada (útil para ajustes de profundidade em runtime).
     *
     * @param entity A entidade sendo processada (garantidamente possui ImageComponent)
     */
    override fun onTickEntity(entity: Entity) {
        // Acessa o ImageComponent da entidade usando a sintaxe de array
        // Fleks garante que a entidade possui o componente (devido ao family)
        // Move a imagem para a frente da pilha de renderização
        entity[ImageComponent].image.toFront()
    }

    override fun handle(event: Event?): Boolean {
        when(event){
            is MapChangeEvent ->{
                bgdLayer.clear()
                fgdLayer.clear()

                event.map.forEachLayer<TiledMapTileLayer>{ layer ->
                    if (layer.name.startsWith("fgd_")){
                        fgdLayer.add(layer)
                    }else{
                        bgdLayer.add(layer)
                    }
                }
                return true
            }
        }
        return false
    }
}
