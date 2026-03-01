package com.tcc.client.system

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Manifold
import com.badlogic.gdx.physics.box2d.World
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.tcc.client.component.ImageComponent
import com.tcc.client.component.PhysicComponent
import ktx.log.logger
import ktx.math.component1
import ktx.math.component2

class PhysicSystem (
    private val physicWorld: World = inject()
) : IteratingSystem(
    family = family{all (PhysicComponent, ImageComponent)},
    interval = Fixed(1/60f)
), ContactListener {
    init {
        physicWorld.setContactListener(this)
    }

    override fun onUpdate() {
        if (physicWorld.autoClearForces){
            log.error {"AutoClearForces precisa estar como falsa para garantir a simulação da fisica corretamente" }
            physicWorld.autoClearForces = false

        }
        super.onUpdate()
        physicWorld.clearForces()

    }

    override fun onTick() {
        super.onTick()
        physicWorld.step(deltaTime, 6,2)
    }

    override fun onTickEntity(entity: Entity) {
        val physicCmp = entity[PhysicComponent]
        val imageCmp =  entity[ImageComponent]

        physicCmp.prevPos.set(physicCmp.body.position)

        if (!physicCmp.impulse.isZero){
            physicCmp.body.applyLinearImpulse(physicCmp.impulse, physicCmp.body.worldCenter, true)
            physicCmp.impulse.setZero()
        }

        imageCmp.image.run {
            val (prevX, prevY) = physicCmp.prevPos
            val (bodyX, bodyY) = physicCmp.body.position

            setPosition(
                MathUtils.lerp(prevX, bodyX, 1f) - width * 0.5f,
                MathUtils.lerp(prevY, bodyY, 1f) - height * 0.5f
            )
        }

    }

    override fun beginContact(p0: Contact?) {
        TODO("Not yet implemented")
    }

    override fun endContact(p0: Contact?) {
        TODO("Not yet implemented")
    }

    override fun preSolve(p0: Contact?, p1: Manifold?) {
        TODO("Not yet implemented")
    }

    override fun postSolve(
        p0: Contact?,
        p1: ContactImpulse?
    ) {
        TODO("Not yet implemented")
    }

    companion object {
        private val log = logger<PhysicSystem>()
    }
}
