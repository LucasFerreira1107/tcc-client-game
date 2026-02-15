package com.tcc.client.system

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Scaling
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.tcc.client.Main.Companion.UNIT_SCALE
import com.tcc.client.component.AnimationComponent
import com.tcc.client.component.ImageComponent
import com.tcc.client.component.SpawnComponent
import com.tcc.client.config.SpawnCfg
import com.tcc.client.enum.AnimationType
import com.tcc.client.event.MapChangeEvent
import ktx.app.gdxError
import ktx.math.vec2
import ktx.tiled.id
import ktx.tiled.layer
import ktx.tiled.property
import ktx.tiled.type
import ktx.tiled.x
import ktx.tiled.y


class EntitySpawnSystem (
    private val atlas: TextureAtlas
    ): EventListener, IteratingSystem(family {all(SpawnComponent)}){
    private val cachedCfgs = mutableMapOf<String, SpawnCfg>()
    private val cachedSizes = mutableMapOf<String, Vector2>()
    /*private val playerEntities = world.family { all(PlayerComponent) }*/

    override fun onTickEntity(entity: Entity) {
        with(entity[SpawnComponent]){
            val cfg = spawnCfg(type)
           val relativeSize = size(cfg.atlasKey)

            world.entity {
                it += ImageComponent().apply {
                     image = Image().apply {
                         setScaling(Scaling.fill)
                         setPosition(location.x, location.y)
                         setSize(relativeSize.x, relativeSize.y)
                         color = this@with.color
                     }
                }
                it+= AnimationComponent().apply {
                    nextAnimation(cfg.atlasKey, AnimationType.IDLE)
                }
            }
        }
        entity.remove()
    }

    private fun spawnCfg(type:String): SpawnCfg= cachedCfgs.getOrPut(type) {
       when{
           type == "Player" -> SpawnCfg("player")
           type == "Slime" -> SpawnCfg("slime")
           type.isNotBlank() -> SpawnCfg(type.lowercase())
           else -> gdxError("SpawnType must be specified")
       }
    }

    private fun size(atlasKey:String): Vector2 {
        return cachedSizes.getOrPut(atlasKey){
            val regions = atlas.findRegions("$atlasKey/${AnimationType.IDLE.atlasKey}")
            if (regions.isEmpty()){
                gdxError("There are no texture regions for $atlasKey")
            }
            val firstFrame = regions.first()
            vec2(firstFrame.originalWidth * UNIT_SCALE, firstFrame.originalHeight *UNIT_SCALE)
        }
    }

    override fun handle(event: Event?): Boolean{

        if(event is MapChangeEvent){
            val entityLayer = event.map.layer("entities")
            entityLayer.objects.forEach { mapObject ->
                val typeStr = mapObject.type ?: gdxError("MapObject ${mapObject.id} of 'entities' layer does not have a NAME")

                world.entity {
                    it += SpawnComponent(
                        typeStr,
                        vec2(mapObject.x * UNIT_SCALE, mapObject.y * UNIT_SCALE),
                        color = mapObject.property("color", Color.WHITE),
                    )
                }
            }
            return true
        }
        return false
    }

    companion object{
        private const val PLAYER_TYPE = "PLAYER"
    }
}
