package com.tcc.client.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.tcc.client.component.MoveComponent
import com.tcc.client.component.PhysicComponent
import ktx.ashley.allOf
import ktx.math.component1
import ktx.math.component2

class MoveSystem : IteratingSystem(
    family { all(MoveComponent, PhysicComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val moveCmp = entity[MoveComponent]
        val physicCmp = entity[PhysicComponent]
        val mass = physicCmp.body.mass
        val (velX, velY) = physicCmp.body.linearVelocity
        val (cos, sin) = moveCmp.cosSin

        if (moveCmp.cosSin.isZero){
            // sem direção para o movimento
            if (!physicCmp.body.linearVelocity.isZero){
                //entidade está se movendo -> está parando
                physicCmp.impulse.set(
                    mass * (0f -velX),
                    mass * (0f -velY)
                )
            }
            return
        }
        physicCmp.impulse.set(
            mass * (moveCmp.speed * cos - velX),
            mass * (moveCmp.speed * sin - velY)
        )
    }
}
