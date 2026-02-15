package com.tcc.client.component

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World

/**
 * Componente do ECS (Entity Component System) usando Fleks 2.11.
 * 
 * Este componente representa uma imagem gráfica associada a uma entidade.
 * No padrão ECS:
 * - Entity: objeto de jogo (ex: jogador, NPC, item)
 * - Component: dados/propriedades (ex: posição, imagem, saúde)
 * - System: lógica/comportamento (ex: renderização, física)
 * 
 * O ImageComponent armazena:
 * - Uma imagem (Image do LibGDX Scene2D) para renderização
 * - Um layer (camada) para controle de profundidade de desenho
 * 
 * @property image A imagem Scene2D que será renderizada (inicializada posteriormente)
 * @property layer Número da camada para ordenação de renderização (menor = mais ao fundo)
 * 
 * @see Component Interface base do Fleks para componentes
 * @see Comparable Implementado para ordenação de entidades durante a renderização
 */
class ImageComponent(): Component<ImageComponent>, Comparable<ImageComponent> {
    /**
     * Imagem Scene2D que será renderizada na tela.
     * lateinit permite inicialização posterior (necessária pois o componente é criado antes da imagem)
     */
    lateinit var image: Image
    
    /**
     * Camada de renderização. Usado para controlar a ordem de desenho:
     * - Camadas menores são desenhadas primeiro (ficam atrás)
     * - Camadas maiores são desenhadas por último (ficam na frente)
     */
    var layer = 0

    /**
     * Retorna o tipo do componente (usado internamente pelo Fleks).
     * 
     * @return O tipo singleton do componente
     */
    override fun type() = ImageComponent
    
    /**
     * Objeto companion que define o tipo único do componente no sistema ECS.
     * Fleks usa isso para identificar e agrupar componentes do mesmo tipo.
     */
    companion object : ComponentType<ImageComponent>()

    /**
     * Callback chamado quando este componente é adicionado a uma entidade no World.
     * 
     * Este método:
     * - Injeta o Stage do LibGDX (usando injeção de dependência do Fleks)
     * - Adiciona a imagem como actor no Stage para que ela seja renderizada
     * 
     * @param entity A entidade à qual o componente foi adicionado
     */
    override fun World.onAdd(entity: Entity) {
        // Injeta o Stage que foi registrado como dependência no GameScreen
        // e adiciona a imagem como um actor, tornando-a visível e gerenciável pelo Scene2D
        this.inject<Stage>().addActor(image)
    }

    /**
     * Callback chamado quando este componente é removido de uma entidade.
     * 
     * Remove a imagem do Stage para evitar que ela continue sendo renderizada
     * após a entidade ser destruída.
     * 
     * @param entity A entidade da qual o componente está sendo removido
     */
    override fun World.onRemove(entity: Entity) {
        // Remove a imagem do Stage para evitar renderização de entidades destruídas
        this.inject<Stage>().root.removeActor(image)
    }
    
    /**
     * Compara dois ImageComponents para ordenação durante a renderização.
     * 
     * A ordenação segue esta lógica:
     * 1. Primeiro compara por layer (camada) - menor layer primeiro
     * 2. Se os layers forem iguais, compara por posição X - maior X primeiro (ordem da direita para esquerda)
     * 
     * Isso garante que entidades em camadas diferentes sejam desenhadas na ordem correta,
     * e que entidades na mesma camada sejam desenhadas da direita para a esquerda
     * (útil para efeitos de profundidade).
     * 
     * @param other O outro ImageComponent a ser comparado
     * @return Int negativo se this < other, zero se iguais, positivo se this > other
     */
    override fun compareTo(other: ImageComponent): Int {
        val layerDiff = layer.compareTo(other.layer)
        return if (layerDiff != 0) {
            // Se as camadas são diferentes, ordena por layer (menor primeiro)
            layerDiff
        } else {
            // Se as camadas são iguais, ordena por posição X (maior primeiro = direita para esquerda)
            other.image.x.compareTo(image.x)
        }
    }
}
