package com.dungeon.game;

import android.content.Context;
import android.graphics.*;
import android.view.*;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private Thread gameThread;
    private volatile boolean running;
    private GameLogic logic;
    private Paint paint;
    private Typeface font;

    private static final int VIEWPORT_TILES_X = 21;
    private static final int VIEWPORT_TILES_Y = 15;
    private int tileSize;
    private int viewX, viewY;
    private int screenW, screenH;

    private float touchStartX, touchStartY;
    private static final int SWIPE_THRESHOLD = 50;

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        font = Typeface.MONOSPACE;
        logic = new GameLogic();
        setFocusable(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        screenW = getWidth();
        screenH = getHeight();
        int tileW = screenW / VIEWPORT_TILES_X;
        int tileH = screenH / VIEWPORT_TILES_Y;
        tileSize = Math.min(tileW, tileH);
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        screenW = width;
        screenH = height;
        int tileW = screenW / VIEWPORT_TILES_X;
        int tileH = screenH / VIEWPORT_TILES_Y;
        tileSize = Math.min(tileW, tileH);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        running = false;
        try { gameThread.join(); } catch (InterruptedException e) {}
    }

    @Override
    public void run() {
        while (running) {
            Canvas canvas = getHolder().lockCanvas();
            if (canvas != null) {
                render(canvas);
                getHolder().unlockCanvasAndPost(canvas);
            }
            try { Thread.sleep(50); } catch (InterruptedException e) {}
        }
    }

    private void render(Canvas canvas) {
        centerViewport();
        int mapPixelW = VIEWPORT_TILES_X * tileSize;
        int mapPixelH = VIEWPORT_TILES_Y * tileSize;

        canvas.drawColor(Color.BLACK);

        for (int vx = 0; vx < VIEWPORT_TILES_X; vx++) {
            for (int vy = 0; vy < VIEWPORT_TILES_Y; vy++) {
                int wx = viewX + vx;
                int wy = viewY + vy;
                if (wx < 0 || wx >= GameLogic.WORLD_WIDTH || wy < 0 || wy >= GameLogic.WORLD_HEIGHT)
                    continue;

                int px = vx * tileSize;
                int py = vy * tileSize;

                if (!logic.explored[wx][wy]) {
                    canvas.drawRect(px, py, px + tileSize, py + tileSize, paint);
                    continue;
                }

                Tile tile = logic.tiles[wx][wy];
                int brightness = logic.visible[wx][wy] ? 255 : 80;
                int tr = tile.r * brightness / 255;
                int tg = tile.g * brightness / 255;
                int tb = tile.b * brightness / 255;
                paint.setColor(Color.rgb(tr, tg, tb));
                canvas.drawRect(px, py, px + tileSize, py + tileSize, paint);

                paint.setColor(Color.rgb(
                    Math.min(255, tr + 30),
                    Math.min(255, tg + 30),
                    Math.min(255, tb + 30)));
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(1);
                canvas.drawRect(px, py, px + tileSize, py + tileSize, paint);
                paint.setStyle(Paint.Style.FILL);

                drawChar(canvas, tile.symbol, px, py, tr, tg, tb);
            }
        }

        for (Item it : logic.items) {
            int vx = it.x - viewX;
            int vy = it.y - viewY;
            if (vx < 0 || vx >= VIEWPORT_TILES_X || vy < 0 || vy >= VIEWPORT_TILES_Y) continue;
            if (!logic.visible[it.x][it.y]) continue;
            drawChar(canvas, it.symbol, vx * tileSize, vy * tileSize, it.r, it.g, it.b);
        }

        for (Entity m : logic.monsters) {
            if (!m.isAlive()) continue;
            int vx = m.x - viewX;
            int vy = m.y - viewY;
            if (vx < 0 || vx >= VIEWPORT_TILES_X || vy < 0 || vy >= VIEWPORT_TILES_Y) continue;
            if (!logic.visible[m.x][m.y]) continue;
            drawChar(canvas, m.symbol, vx * tileSize, vy * tileSize, m.r, m.g, m.b);
        }

        drawChar(canvas, logic.player.symbol,
            (logic.player.x - viewX) * tileSize,
            (logic.player.y - viewY) * tileSize,
            255, 255, 255);

        drawUI(canvas, mapPixelH);

        if (logic.gameOver) {
            paint.setColor(Color.argb(180, 0, 0, 0));
            canvas.drawRect(0, 0, screenW, screenH, paint);
            paint.setColor(Color.RED);
            paint.setTextSize(48);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            String msg = "GAME OVER";
            float tw = paint.measureText(msg);
            canvas.drawText(msg, (screenW - tw) / 2, screenH / 2 - 20, paint);
            paint.setTextSize(24);
            paint.setTypeface(Typeface.DEFAULT);
            paint.setColor(Color.WHITE);
            String sub = "Tap to restart";
            tw = paint.measureText(sub);
            canvas.drawText(sub, (screenW - tw) / 2, screenH / 2 + 30, paint);
        }
        if (logic.won) {
            paint.setColor(Color.argb(180, 0, 0, 0));
            canvas.drawRect(0, 0, screenW, screenH, paint);
            paint.setColor(Color.YELLOW);
            paint.setTextSize(48);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            String msg = "VICTORY!";
            float tw = paint.measureText(msg);
            canvas.drawText(msg, (screenW - tw) / 2, screenH / 2 - 20, paint);
            paint.setTextSize(24);
            paint.setTypeface(Typeface.DEFAULT);
            paint.setColor(Color.WHITE);
            String sub = "You escaped the dungeon!";
            tw = paint.measureText(sub);
            canvas.drawText(sub, (screenW - tw) / 2, screenH / 2 + 30, paint);
        }
    }

    private void drawChar(Canvas canvas, char ch, int x, int y, int r, int g, int b) {
        paint.setColor(Color.rgb(r, g, b));
        paint.setTextSize(tileSize * 0.8f);
        paint.setTypeface(font);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(String.valueOf(ch), x + tileSize / 2f, y + tileSize * 0.8f, paint);
    }

    private void drawUI(Canvas canvas, int mapH) {
        int uiY = mapH;
        int uiH = screenH - mapH;
        paint.setColor(Color.rgb(10, 10, 15));
        canvas.drawRect(0, uiY, screenW, screenH, paint);

        paint.setColor(Color.RED);
        paint.setTextSize(18);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("HP:", 10, uiY + 24, paint);

        int hpX = 50;
        int hpW = screenW - hpX - 10;
        paint.setColor(Color.DKGRAY);
        canvas.drawRect(hpX, uiY + 8, hpX + hpW, uiY + 24, paint);
        if (logic.player.hp > 0) {
            float ratio = (float)logic.player.hp / logic.player.maxHp;
            int hpColor = ratio > 0.5f ? 0xFF00FF00 : (ratio > 0.25f ? 0xFFFFA500 : 0xFFFF0000);
            paint.setColor(hpColor);
            canvas.drawRect(hpX, uiY + 8, hpX + hpW * ratio, uiY + 24, paint);
        }
        paint.setColor(Color.WHITE);
        paint.setTextSize(14);
        canvas.drawText(logic.player.hp + "/" + logic.player.maxHp, hpX + 5, uiY + 22, paint);

        paint.setTextSize(14);
        canvas.drawText("ATK:" + logic.player.attack + " DEF:" + logic.player.defense, hpX, uiY + 46, paint);
        canvas.drawText("Depth:" + logic.currentDepth + " Mon:" + logic.monsters.size(), hpX, uiY + 64, paint);

        paint.setTextSize(13);
        paint.setColor(Color.LTGRAY);
        String msg = logic.log.getLatest();
        if (msg.length() > 40) msg = msg.substring(0, 40);
        canvas.drawText(msg, 10, uiY + 86, paint);

        paint.setTextSize(12);
        paint.setColor(Color.GRAY);
        canvas.drawText("Swipe to move | Wait: tap self", 10, uiY + 106, paint);
    }

    private void centerViewport() {
        viewX = logic.player.x - VIEWPORT_TILES_X / 2;
        viewY = logic.player.y - VIEWPORT_TILES_Y / 2;
        viewX = Math.max(0, Math.min(viewX, GameLogic.WORLD_WIDTH - VIEWPORT_TILES_X));
        viewY = Math.max(0, Math.min(viewY, GameLogic.WORLD_HEIGHT - VIEWPORT_TILES_Y));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (logic.gameOver || logic.won) {
            logic = new GameLogic();
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStartX = event.getX();
                touchStartY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
                float dx = event.getX() - touchStartX;
                float dy = event.getY() - touchStartY;
                float dist = (float)Math.sqrt(dx * dx + dy * dy);

                if (dist < SWIPE_THRESHOLD) {
                    logic.endTurn();
                } else if (Math.abs(dx) > Math.abs(dy)) {
                    logic.movePlayer(dx > 0 ? Direction.RIGHT : Direction.LEFT);
                } else {
                    logic.movePlayer(dy > 0 ? Direction.DOWN : Direction.UP);
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    public void resetGame() {
        logic = new GameLogic();
    }
}
