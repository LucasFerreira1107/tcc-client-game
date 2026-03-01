package com.tcc.client.component

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.tcc.client.config.SpawnCfg
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType.StaticBody
import com.tcc.client.system.EntitySpawnSystem
import ktx.box2d.body
import ktx.box2d.box
import ktx.math.vec2
import com.badlogic.gdx.physics.box2d.World as PhysicWorld
class PhysicComponent(
    val impulse: Vector2 = vec2(),
    val size: Vector2 = vec2(),
    val offset: Vector2 = vec2()
) : Component<PhysicComponent> {
    lateinit var body: Body
    val prevPos = vec2()


    override fun type() = PhysicComponent


    override fun World.onAdd(entity: Entity) {
        body.userData = entity
    }

    override fun World.onRemove(entity: Entity) {
        body.world.destroyBody(body)
        body.userData = null
    }

    companion object : ComponentType<PhysicComponent>(){
        private val TMP_VEC = vec2()
        private val COLLISION_OFFSET = vec2()
        fun PhysicComponent.bodyFromImageAndCfg( world: PhysicWorld, image : Image, cfg: SpawnCfg): Body {
            val x = image.x
            val y = image.y
            val width = image.width
            val height = image.height
            val bodyType = cfg.bodyType
            val physicScaling = cfg.scalePhysic
            val cmp = this

            return world.body(bodyType) {
                position.set(x+ width *0.5f, y + height*0.5f)
                cmp.prevPos.set(position)
                fixedRotation = true
                allowSleep = false

                val w = width * physicScaling.x
                val h = height * physicScaling.y
                cmp.size.set(w,h)
                cmp.offset.set(cfg.physicOffset)

                //hit box
                box(w,h,cmp.offset){
                    isSensor = bodyType != StaticBody
                    userData = EntitySpawnSystem.HIT_BOX_SENSOR
                }
            }
        }
    }
}
