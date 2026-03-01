package com.tcc.client.system

import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.World.Companion.inject
import ktx.assets.disposeSafely
import ktx.log.logger

class DebugSystem(
    private val phWorld: World = inject(),
    private val stage: Stage = inject()
): IntervalSystem(enabled = true) {

    private val physicRenderer by lazy { Box2DDebugRenderer() }

    override fun onTick() {
        stage.isDebugAll = true
        physicRenderer.render(phWorld, stage.camera.combined)
    }

    override fun onDispose() {
        if(enabled){
            physicRenderer.disposeSafely()
        }
    }
    companion object {
        private val log = logger<PhysicSystem>()
    }
}
