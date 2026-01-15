package com.example.runneroyunjava;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class GameView extends SurfaceView implements Runnable {

    // --- Thread / Loop ---
    private Thread gameThread;
    private volatile boolean running = false;
    private final SurfaceHolder holder;
    private Bitmap bmpPlayerRoll;

    // Roll sprite sheet
    private int rollFrameCount = 2;
    private int rollFrameIndex = 0;
    private float rollAnimTimer = 0f;
    private float rollFrameDuration = 0.07f; // daha hızlı güzel durur

    private final Rect rollSrcRect = new Rect();
    private final RectF rollDstRect = new RectF();

    // --- Render ---
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // --- Bitmaps ---
    private Bitmap bmpBg;
    private Bitmap bmpGround;
    private Bitmap bmpPlayer;     // jump/roll için tek frame
    private Bitmap bmpPlayerRun;  // 4 frame sprite sheet

    // --- Sprite sheet (run) ---
    private int runFrameCount = 4;
    private int runFrameIndex = 0;
    private float runAnimTimer = 0f;
    private float runFrameDuration = 0.09f;

    private final Rect srcRect = new Rect();
    private final RectF dstRect = new RectF();

    // --- Parallax/scroll offsets ---
    private float bgOffset = 0f;
    private float groundOffset = 0f;

    // --- World ---
    private float groundY;
    private float gravity = 2200f;       // px/s^2
    private float jumpVelocity = -900f;  // px/s
    private float speed = 520f;          // px/s
    private float speedIncrease = 8f;    // hız artışı

    // --- Player ---
    private final RectF player = new RectF();
    private float playerVX = 0f;
    private float playerVY = 0f;
    private boolean onGround = true;

    // Roll / slide
    private boolean rolling = false;
    private float rollDuration = 0.45f;
    private float rollTimer = 0f;

    private float playerW, playerH;
    private float rollH;

    // --- Obstacles ---
    private static class Obstacle {
        RectF rect;
        boolean high;
        Obstacle(RectF r, boolean high) { rect = r; this.high = high; }
    }

    private final ArrayList<Obstacle> obstacles = new ArrayList<>();
    private final Random rnd = new Random();

    private float spawnTimer = 0f;
    private float spawnEvery = 1.2f;

    // --- Input ---
    private float touchDownX, touchDownY;
    private long touchDownTime;

    // --- Game State ---
    private boolean gameOver = false;
    private int score = 0;
    private float scoreTimer = 0f;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        holder = getHolder();
        setFocusable(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        groundY = h * 0.78f;

        playerW = w * 0.10f;
        playerH = h * 0.14f;
        rollH   = playerH * 0.60f;

        // DRAWABLE'DAN YÜKLE
        Bitmap rawBg     = BitmapFactory.decodeResource(getResources(), R.drawable.bg);
        Bitmap rawGround = BitmapFactory.decodeResource(getResources(), R.drawable.ground_tile);
        Bitmap rawPlayer = BitmapFactory.decodeResource(getResources(), R.drawable.player);
        bmpPlayerRun  = BitmapFactory.decodeResource(getResources(), R.drawable.player_run);
        bmpPlayerRoll = BitmapFactory.decodeResource(getResources(), R.drawable.player_roll);


        // SCALE (bg ekran boyu)
        bmpBg = Bitmap.createScaledBitmap(rawBg, w, h, true);

        // ground tile yükseklik: groundY'den aşağısı kadar
        int groundH = (int) (h - groundY + h * 0.05f);
        int groundW = (int) (groundH * (rawGround.getWidth() / (float) rawGround.getHeight()));
        if (groundW < 1) groundW = w / 3;

        bmpGround = Bitmap.createScaledBitmap(rawGround, groundW, groundH, true);

        // jump/roll tek frame
        bmpPlayer = Bitmap.createScaledBitmap(rawPlayer, (int) playerW, (int) playerH, true);

        resetGame();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void resetGame() {
        gameOver = false;
        score = 0;
        scoreTimer = 0f;

        speed = getWidth() * 0.65f;
        bgOffset = 0f;
        groundOffset = 0f;

        obstacles.clear();
        spawnTimer = 0f;
        spawnEvery = 1.2f;

        runFrameIndex = 0;
        runAnimTimer = 0f;

        float px = getWidth() * 0.18f;
        float pyBottom = groundY;
        player.set(px, pyBottom - playerH, px + playerW, pyBottom);

        playerVY = 0f;
        onGround = true;

        rolling = false;
        rollTimer = 0f;
        setPlayerHeight(playerH);
    }

    private void setPlayerHeight(float height) {
        float bottom = player.bottom;
        player.top = bottom - height;
    }

    // --- Thread control ---
    public void resume() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void pause() {
        running = false;
        try {
            if (gameThread != null) gameThread.join();
        } catch (InterruptedException ignored) {}
    }

    @Override
    public void run() {
        long lastNs = System.nanoTime();
        while (running) {
            if (!holder.getSurface().isValid()) continue;

            long now = System.nanoTime();
            float dt = (now - lastNs) / 1_000_000_000f;
            if (dt > 0.033f) dt = 0.033f;
            lastNs = now;

            update(dt);
            drawGame();
        }
    }

    private void update(float dt) {
        if (gameOver) return;

        // hız artışı
        speed += speedIncrease * dt;

        // skor
        scoreTimer += dt;
        if (scoreTimer >= 0.2f) {
            score++;
            scoreTimer = 0f;
        }

        // parallax scroll
        bgOffset -= (speed * 0.15f) * dt;
        groundOffset -= speed * dt;

        if (bmpBg != null) {
            if (bgOffset <= -getWidth()) bgOffset += getWidth();
        }
        if (bmpGround != null) {
            if (groundOffset <= -bmpGround.getWidth()) groundOffset += bmpGround.getWidth();
        }

        // run anim (sadece yerde + roll değilken)
        if (bmpPlayerRun != null && onGround && !rolling) {
            runAnimTimer += dt;
            if (runAnimTimer >= runFrameDuration) {
                runAnimTimer -= runFrameDuration;
                runFrameIndex = (runFrameIndex + 1) % runFrameCount;
            }
        }

        // roll timer
        if (rolling) {
            rollTimer += dt;
            if (rollTimer >= rollDuration) {
                rolling = false;
                rollTimer = 0f;
                setPlayerHeight(playerH);
            }
        }
// roll anim (sadece rolling iken)
        if (bmpPlayerRoll != null && rolling) {
            rollAnimTimer += dt;
            if (rollAnimTimer >= rollFrameDuration) {
                rollAnimTimer -= rollFrameDuration;
                rollFrameIndex = (rollFrameIndex + 1) % rollFrameCount;
            }
        }

        // player physics
        if (!onGround) {
            playerVY += gravity * dt;
            player.offset(playerVX * dt, playerVY * dt);

            if (player.bottom >= groundY) {
                player.offsetTo(player.left, groundY - player.height());
                playerVY = 0f;
                onGround = true;
            }
        }

        // obstacle spawn
        spawnTimer += dt;
        if (spawnTimer >= spawnEvery) {
            spawnTimer = 0f;
            spawnEvery = 0.95f + rnd.nextFloat() * 0.6f;
            spawnObstacle();
        }

        // obstacles move + collision
        Iterator<Obstacle> it = obstacles.iterator();
        while (it.hasNext()) {
            Obstacle ob = it.next();
            ob.rect.offset(-speed * dt, 0);

            if (RectF.intersects(player, ob.rect)) {
                gameOver = true;
            }

            if (ob.rect.right < 0) it.remove();
        }
    }

    private void spawnObstacle() {
        float w = getWidth();
        float h = getHeight();

        boolean high = rnd.nextBoolean();
        float obW = w * (0.05f + rnd.nextFloat() * 0.05f);

        if (!high) {
            float obH = h * (0.07f + rnd.nextFloat() * 0.05f);
            RectF r = new RectF(
                    w + obW,
                    groundY - obH,
                    w + obW + obW,
                    groundY
            );
            obstacles.add(new Obstacle(r, false));
        } else {
            float gapTop = groundY - playerH - h * 0.08f;
            float obH = h * (0.12f + rnd.nextFloat() * 0.06f);
            RectF r = new RectF(
                    w + obW,
                    gapTop - obH,
                    w + obW + obW,
                    gapTop
            );
            obstacles.add(new Obstacle(r, true));
        }
    }

    private void drawGame() {
        Canvas c = holder.lockCanvas();
        if (c == null) return;

        // 1) BG
        if (bmpBg != null) {
            c.drawBitmap(bmpBg, bgOffset, 0, null);
            c.drawBitmap(bmpBg, bgOffset + getWidth(), 0, null);
        } else {
            c.drawColor(Color.rgb(10, 10, 16));
        }

        // 2) Ground tiles
        if (bmpGround != null) {
            float top = groundY;
            float x = groundOffset;
            while (x < getWidth()) {
                c.drawBitmap(bmpGround, x, top, null);
                x += bmpGround.getWidth();
            }
        } else {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(6f);
            paint.setColor(Color.DKGRAY);
            c.drawLine(0, groundY, getWidth(), groundY, paint);
        }

        // 3) Obstacles (neon basit)
        for (Obstacle ob : obstacles) {
            int col = ob.high ? Color.rgb(255, 80, 190) : Color.rgb(255, 210, 80);
            drawGlowRoundRect(c, ob.rect, 14f, 14f, col);
        }

        // 4) PLAYER DRAW (state: RUN / ROLL / JUMP)

// RUN (yerde + roll değil)
        if (bmpPlayerRun != null && onGround && !rolling) {

            int frameW = bmpPlayerRun.getWidth() / runFrameCount;
            int frameH = bmpPlayerRun.getHeight();

            int left = runFrameIndex * frameW;
            srcRect.set(left, 0, left + frameW, frameH);

            dstRect.set(player.left, player.top, player.right, player.bottom);
            c.drawBitmap(bmpPlayerRun, srcRect, dstRect, null);

            drawGlowOnly(c, player, Color.rgb(0, 255, 140));

        }
// ROLL (rolling = true)
        else if (bmpPlayerRoll != null && rolling) {

            int frameW = bmpPlayerRoll.getWidth() / rollFrameCount;
            int frameH = bmpPlayerRoll.getHeight();

            int left = rollFrameIndex * frameW;
            rollSrcRect.set(left, 0, left + frameW, frameH);

            // Roll'da daha alçak çiz (hitbox zaten rollH)
            float drawH = rollH;

            // İstersen roll çizimini biraz sağa kaydır (daha doğal durur)
            float rollShiftX = playerW * 0.08f;

            rollDstRect.set(
                    player.left + rollShiftX,
                    player.bottom - drawH,
                    player.left + rollShiftX + playerW,
                    player.bottom
            );

            c.drawBitmap(bmpPlayerRoll, rollSrcRect, rollDstRect, null);
            drawGlowOnly(c, player, Color.rgb(80, 180, 255));

        }
// JUMP / fallback (havada veya roll sheet yoksa)
        else if (bmpPlayer != null) {

            float drawH = rolling ? rollH : playerH;

            dstRect.set(
                    player.left,
                    player.bottom - drawH,
                    player.left + playerW,
                    player.bottom
            );

            if (!onGround) {
                c.save();
                c.rotate(-8f, player.centerX(), player.centerY());
                c.drawBitmap(bmpPlayer, null, dstRect, null);
                c.restore();
            } else {
                c.drawBitmap(bmpPlayer, null, dstRect, null);
            }

            drawGlowOnly(c, player, Color.rgb(0, 255, 140));

        }
        else {
            paint.setColor(Color.BLACK);
            c.drawRoundRect(player, 18f, 18f, paint);
        }


        // 5) Scanlines (hafif)
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(Color.WHITE);
        paint.setAlpha(10);
        for (int y = 0; y < getHeight(); y += 10) {
            c.drawLine(0, y, getWidth(), y, paint);
        }
        paint.setAlpha(255);

        // 6) Score
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(48f);
        c.drawText("Score: " + score, 30f, 70f, paint);

        if (gameOver) {
            paint.setTextSize(72f);
            c.drawText("GAME OVER", getWidth() * 0.18f, getHeight() * 0.45f, paint);
            paint.setTextSize(42f);
            c.drawText("Tap to restart", getWidth() * 0.32f, getHeight() * 0.52f, paint);
        }

        holder.unlockCanvasAndPost(c);
    }

    // --- Input ---
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            touchDownX = event.getX();
            touchDownY = event.getY();
            touchDownTime = System.currentTimeMillis();
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            float upX = event.getX();
            float upY = event.getY();

            float dx = upX - touchDownX;
            float dy = upY - touchDownY;

            if (gameOver) {
                resetGame();
                return true;
            }

            float swipeDownThreshold = getHeight() * 0.10f;
            if (dy > swipeDownThreshold && Math.abs(dy) > Math.abs(dx)) {
                startRoll();
                return true;
            }

            long dtMs = System.currentTimeMillis() - touchDownTime;
            if (dtMs < 220) {
                jump();
            }

            return true;
        }

        return super.onTouchEvent(event);
    }

    private void jump() {
        if (!onGround) return;
        if (rolling) return;
        onGround = false;
        playerVY = jumpVelocity;
    }

    private void startRoll() {
        if (!onGround) return;
        if (rolling) return;

        rolling = true;
        rollTimer = 0f;

        // roll anim reset
        rollFrameIndex = 0;
        rollAnimTimer = 0f;

        setPlayerHeight(rollH);
    }


    // --- Glow helpers ---
    private void drawGlowRoundRect(Canvas c, RectF r, float rx, float ry, int color) {
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(color);
        paint.setAlpha(40);
        c.drawRoundRect(expand(r, 18f), rx, ry, paint);

        paint.setAlpha(70);
        c.drawRoundRect(expand(r, 10f), rx, ry, paint);

        paint.setAlpha(255);
        c.drawRoundRect(r, rx, ry, paint);

        paint.setAlpha(255);
    }

    // Bitmap üstüne sadece “aura” efekti (oyuncuyu tekrar doldurmasın)
    private void drawGlowOnly(Canvas c, RectF r, int color) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);

        paint.setStrokeWidth(14f);
        paint.setAlpha(35);
        c.drawRoundRect(expand(r, 16f), 22f, 22f, paint);

        paint.setStrokeWidth(8f);
        paint.setAlpha(60);
        c.drawRoundRect(expand(r, 10f), 22f, 22f, paint);

        paint.setAlpha(255);
        paint.setStrokeWidth(1f);
        paint.setStyle(Paint.Style.FILL);
    }

    private RectF expand(RectF r, float pad) {
        return new RectF(r.left - pad, r.top - pad, r.right + pad, r.bottom + pad);
    }
}
