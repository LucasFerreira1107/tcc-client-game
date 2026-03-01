package com.tcc.client.config

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import ktx.math.vec2


data class SpawnCfg(
    val atlasKey: String,
    val bodyType: BodyType = BodyType.DynamicBody,
    val scalePhysic: Vector2 = vec2(),
    val physicOffset: Vector2 = vec2(),
    val scaleSpeed: Float = 1f

){
    companion object{
        const val DEFAULT_SPEED =2F
    }
}
