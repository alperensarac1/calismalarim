package com.example.runneroyunkotlin.game_engine

import android.graphics.RectF
import java.util.Random

class ObstacleManager {
    data class Obstacle(var rect: RectF, val high: Boolean)

    private val rnd = Random()
    val list = ArrayList<Obstacle>()

    private var spawnTimer = 0f
    private var spawnEvery = 1.2f

    fun reset() {
        list.clear()
        spawnTimer = 0f
        spawnEvery = 1.2f
    }

    fun update(dt: Float, speed: Float, w: Int, h: Int, groundY: Float, playerH: Float, playerRect: RectF): Boolean {
        // spawn
        spawnTimer += dt
        if (spawnTimer >= spawnEvery) {
            spawnTimer = 0f
            spawnEvery = 0.95f + rnd.nextFloat() * 0.6f
            spawnObstacle(w.toFloat(), h.toFloat(), groundY, playerH)
        }

        // move + collision
        val it = list.iterator()
        while (it.hasNext()) {
            val ob = it.next()
            ob.rect.offset(-speed * dt, 0f)

            if (RectF.intersects(playerRect, ob.rect)) return true
            if (ob.rect.right < 0f) it.remove()
        }

        return false
    }

    private fun spawnObstacle(w: Float, h: Float, groundY: Float, playerH: Float) {
        val high = rnd.nextBoolean()
        val obW = w * (0.05f + rnd.nextFloat() * 0.05f)

        if (!high) {
            val obH = h * (0.07f + rnd.nextFloat() * 0.05f)
            val r = RectF(w + obW, groundY - obH, w + obW + obW, groundY)
            list.add(Obstacle(r, false))
        } else {
            val gapTop = groundY - playerH - h * 0.08f
            val obH = h * (0.12f + rnd.nextFloat() * 0.06f)
            val r = RectF(w + obW, gapTop - obH, w + obW + obW, gapTop)
            list.add(Obstacle(r, true))
        }
    }
}
