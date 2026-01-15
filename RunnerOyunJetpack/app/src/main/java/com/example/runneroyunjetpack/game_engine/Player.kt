package com.example.runneroyunjetpack.game_engine

import android.graphics.*

class Player(
    private val runFrameCount: Int = 4,
    private val rollFrameCount: Int = 2
) {
    val rect = RectF()

    private var vx = 0f
    private var vy = 0f
    var onGround = true
        private set

    private var rolling = false
    private var rollTimer = 0f
    private val rollDuration = 0.45f

    private var playerW = 0f
    private var playerH = 0f
    private var rollH = 0f

    private var runFrameIndex = 0
    private var runAnimTimer = 0f
    private val runFrameDuration = 0.09f

    private var rollFrameIndex = 0
    private var rollAnimTimer = 0f
    private val rollFrameDuration = 0.07f

    private val srcRect = Rect()
    private val dstRect = RectF()
    private val rollSrcRect = Rect()
    private val rollDstRect = RectF()

    fun configure(x: Float, groundY: Float, w: Float, h: Float) {
        playerW = w
        playerH = h
        rollH = playerH * 0.60f

        rect.set(x, groundY - playerH, x + playerW, groundY)

        vy = 0f
        onGround = true
        rolling = false
        rollTimer = 0f
        setHeight(playerH)

        runFrameIndex = 0
        runAnimTimer = 0f
        rollFrameIndex = 0
        rollAnimTimer = 0f
    }

    fun jump(jumpVelocity: Float) {
        if (!onGround) return
        if (rolling) return
        onGround = false
        vy = jumpVelocity
    }

    fun startRoll() {
        if (!onGround) return
        if (rolling) return

        rolling = true
        rollTimer = 0f

        rollFrameIndex = 0
        rollAnimTimer = 0f
        setHeight(rollH)
    }

    fun isRolling() = rolling

    fun update(dt: Float, gravity: Float, groundY: Float) {
        // run anim (sadece yerde + roll değilken)
        if (onGround && !rolling) {
            runAnimTimer += dt
            if (runAnimTimer >= runFrameDuration) {
                runAnimTimer -= runFrameDuration
                runFrameIndex = (runFrameIndex + 1) % runFrameCount
            }
        }

        // roll timer + anim
        if (rolling) {
            rollTimer += dt
            if (rollTimer >= rollDuration) {
                rolling = false
                rollTimer = 0f
                setHeight(playerH)
            }

            rollAnimTimer += dt
            if (rollAnimTimer >= rollFrameDuration) {
                rollAnimTimer -= rollFrameDuration
                rollFrameIndex = (rollFrameIndex + 1) % rollFrameCount
            }
        }

        // physics
        if (!onGround) {
            vy += gravity * dt
            rect.offset(vx * dt, vy * dt)

            if (rect.bottom >= groundY) {
                rect.offsetTo(rect.left, groundY - rect.height())
                vy = 0f
                onGround = true
            }
        }
    }

    fun draw(
        c: Canvas,
        bmpRun: Bitmap?,
        bmpRoll: Bitmap?,
        bmpIdleJump: Bitmap?,
        glow: (RectF, Int) -> Unit
    ) {
        // RUN
        if (bmpRun != null && onGround && !rolling) {
            val frameW = bmpRun.width / runFrameCount
            val frameH = bmpRun.height
            val left = runFrameIndex * frameW

            srcRect.set(left, 0, left + frameW, frameH)
            dstRect.set(rect.left, rect.top, rect.right, rect.bottom)
            c.drawBitmap(bmpRun, srcRect, dstRect, null)
            glow(rect, Color.rgb(0, 255, 140))
            return
        }

        // ROLL
        if (bmpRoll != null && rolling) {
            val frameW = bmpRoll.width / rollFrameCount
            val frameH = bmpRoll.height
            val left = rollFrameIndex * frameW

            rollSrcRect.set(left, 0, left + frameW, frameH)

            val drawH = rollH
            val shiftX = playerW * 0.08f
            rollDstRect.set(
                rect.left + shiftX,
                rect.bottom - drawH,
                rect.left + shiftX + playerW,
                rect.bottom
            )

            c.drawBitmap(bmpRoll, rollSrcRect, rollDstRect, null)
            glow(rect, Color.rgb(80, 180, 255))
            return
        }

        // JUMP / FALLBACK
        if (bmpIdleJump != null) {
            val drawH = if (rolling) rollH else playerH
            dstRect.set(rect.left, rect.bottom - drawH, rect.left + playerW, rect.bottom)
            c.drawBitmap(bmpIdleJump, null, dstRect, null)
            glow(rect, Color.rgb(0, 255, 140))
        }
    }

    private fun setHeight(height: Float) {
        val bottom = rect.bottom
        rect.top = bottom - height
    }
}
