package com.example.runneroyunkotlin

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceView
import com.example.runneroyunkotlin.game_engine.GameLoop
import com.example.runneroyunkotlin.game_engine.InputController
import com.example.runneroyunkotlin.game_engine.ObstacleManager
import com.example.runneroyunkotlin.game_engine.Player

class GameView(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val loop = GameLoop(holder, ::update, ::render)

    // Assets
    private var bmpBg: Bitmap? = null
    private var bmpGround: Bitmap? = null
    private var bmpPlayer: Bitmap? = null
    private var bmpPlayerRun: Bitmap? = null
    private var bmpPlayerRoll: Bitmap? = null

    // World
    private var groundY = 0f
    private val gravity = 2200f
    private val jumpVelocity = -900f
    private var speed = 520f
    private val speedIncrease = 8f

    private var bgOffset = 0f
    private var groundOffset = 0f

    // State
    private var gameOver = false
    private var score = 0
    private var scoreTimer = 0f

    // Systems
    private val player = Player()
    private val obstacles = ObstacleManager()

    private val input = InputController(
        screenHProvider = { height },
        onTap = { player.jump(jumpVelocity) },
        onSwipeDown = { player.startRoll() },
        onRestartTap = { resetGame() },
        isGameOver = { gameOver }
    )

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        groundY = h * 0.78f

        val playerW = w * 0.10f
        val playerH = h * 0.14f

        val rawBg = BitmapFactory.decodeResource(resources, R.drawable.bg)
        val rawGround = BitmapFactory.decodeResource(resources, R.drawable.ground_tile)
        val rawPlayer = BitmapFactory.decodeResource(resources, R.drawable.player)
        bmpPlayerRun = BitmapFactory.decodeResource(resources, R.drawable.player_run)
        bmpPlayerRoll = BitmapFactory.decodeResource(resources, R.drawable.player_roll)

        bmpBg = Bitmap.createScaledBitmap(rawBg, w, h, true)

        val groundH = (h - groundY + h * 0.05f).toInt()
        var groundW = (groundH * (rawGround.width / rawGround.height.toFloat())).toInt()
        if (groundW < 1) groundW = w / 3
        bmpGround = Bitmap.createScaledBitmap(rawGround, groundW, groundH, true)

        bmpPlayer = Bitmap.createScaledBitmap(rawPlayer, playerW.toInt(), playerH.toInt(), true)

        val px = w * 0.18f
        player.configure(px, groundY, playerW, playerH)

        resetGame()
        super.onSizeChanged(w, h, oldw, oldh)
    }

    fun resume() = loop.start()
    fun pause() = loop.stop()

    private fun resetGame() {
        gameOver = false
        score = 0
        scoreTimer = 0f

        speed = width * 0.65f
        bgOffset = 0f
        groundOffset = 0f

        obstacles.reset()

        val px = width * 0.18f
        val playerW = width * 0.10f
        val playerH = height * 0.14f
        player.configure(px, groundY, playerW, playerH)
    }

    private fun update(dt: Float) {
        if (gameOver) return

        speed += speedIncrease * dt

        scoreTimer += dt
        if (scoreTimer >= 0.2f) {
            score++
            scoreTimer = 0f
        }

        // parallax
        bgOffset -= (speed * 0.15f) * dt
        groundOffset -= speed * dt

        bmpBg?.let { if (bgOffset <= -width) bgOffset += width }
        bmpGround?.let { if (groundOffset <= -it.width) groundOffset += it.width }

        player.update(dt, gravity, groundY)

        // obstacles + collision
        val hit = obstacles.update(
            dt = dt,
            speed = speed,
            w = width,
            h = height,
            groundY = groundY,
            playerH = height * 0.14f,
            playerRect = player.rect
        )
        if (hit) gameOver = true
    }

    private fun render() {
        val c = holder.lockCanvas() ?: return
        drawScene(c)
        holder.unlockCanvasAndPost(c)
    }

    private fun drawScene(c: Canvas) {
        // BG
        bmpBg?.let { bg ->
            c.drawBitmap(bg, bgOffset, 0f, null)
            c.drawBitmap(bg, bgOffset + width, 0f, null)
        } ?: c.drawColor(Color.rgb(10, 10, 16))

        // Ground
        bmpGround?.let { g ->
            var x = groundOffset
            while (x < width) {
                c.drawBitmap(g, x, groundY, null)
                x += g.width
            }
        }

        // Obstacles
        for (ob in obstacles.list) {
            val col = if (ob.high) Color.rgb(255, 80, 190) else Color.rgb(255, 210, 80)
            drawGlowRoundRect(c, ob.rect, 14f, 14f, col)
        }

        // Player
        player.draw(
            c = c,
            bmpRun = bmpPlayerRun,
            bmpRoll = bmpPlayerRoll,
            bmpIdleJump = bmpPlayer,
            glow = { r, col -> drawGlowOnly(c, r, col) }
        )

        // Score + GameOver
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        paint.textSize = 48f
        c.drawText("Score: $score", 30f, 70f, paint)

        if (gameOver) {
            paint.textSize = 72f
            c.drawText("GAME OVER", width * 0.18f, height * 0.45f, paint)
            paint.textSize = 42f
            c.drawText("Tap to restart", width * 0.32f, height * 0.52f, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> { input.onDown(event.x, event.y); true }
            MotionEvent.ACTION_UP -> input.onUp(event.x, event.y)
            else -> super.onTouchEvent(event)
        }
    }

    // --- Glow helpers (aynı seninki) ---
    private fun drawGlowRoundRect(c: Canvas, r: RectF, rx: Float, ry: Float, color: Int) {
        paint.style = Paint.Style.FILL

        paint.color = color
        paint.alpha = 40
        c.drawRoundRect(expand(r, 18f), rx, ry, paint)

        paint.alpha = 70
        c.drawRoundRect(expand(r, 10f), rx, ry, paint)

        paint.alpha = 255
        c.drawRoundRect(r, rx, ry, paint)

        paint.alpha = 255
    }

    private fun drawGlowOnly(c: Canvas, r: RectF, color: Int) {
        paint.style = Paint.Style.STROKE
        paint.color = color

        paint.strokeWidth = 14f
        paint.alpha = 35
        c.drawRoundRect(expand(r, 16f), 22f, 22f, paint)

        paint.strokeWidth = 8f
        paint.alpha = 60
        c.drawRoundRect(expand(r, 10f), 22f, 22f, paint)

        paint.alpha = 255
        paint.strokeWidth = 1f
        paint.style = Paint.Style.FILL
    }

    private fun expand(r: RectF, pad: Float): RectF =
        RectF(r.left - pad, r.top - pad, r.right + pad, r.bottom + pad)
}
