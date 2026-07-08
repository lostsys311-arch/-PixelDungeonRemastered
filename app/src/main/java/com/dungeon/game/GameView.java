package com.dungeon.game;

import android.content.Context;
import android.graphics.*;
import android.view.*;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable, SoundListener {
    private Thread gameThread;
    private volatile boolean running;
    private GameLogic logic;
    private SoundManager sound;
    private Paint paint;
    private TileAtlas atlas;
    private GraphicsQuality quality;
    private int tileSize;
    private int viewTilesX, viewTilesY;
    private int viewX, viewY;
    private int screenW, screenH;
    private float touchStartX, touchStartY;
    private static final int SWIPE_THRESHOLD = 50;
    private boolean showSettings;
    private Rect settingsBtn;

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setFilterBitmap(true);
        quality = GraphicsQuality.MID;
        logic = new GameLogic();
        logic.setSoundListener(this);
        sound = new SoundManager(context);
        setFocusable(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        recalcLayout();
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        screenW = width; screenH = height;
        recalcLayout();
    }

    private void recalcLayout() {
        screenW = getWidth();
        screenH = getHeight();
        int maxTileW = screenW / quality.viewTilesX;
        int maxTileH = screenH / (quality.viewTilesY(screenW, screenH) + 5);
        tileSize = Math.min(quality.tileSize, Math.min(maxTileW, maxTileH));
        viewTilesX = screenW / tileSize;
        viewTilesY = (screenH - tileSize * 6) / tileSize;
        if (viewTilesX < 8) viewTilesX = 8;
        if (viewTilesY < 6) viewTilesY = 6;
        atlas = new TileAtlas(tileSize, quality.detailedSprites);
        settingsBtn = new Rect(screenW - 55, 10, screenW - 10, 55);
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
            try { Thread.sleep(quality == GraphicsQuality.FANCY ? 33 : 50); } catch (InterruptedException e) {}
        }
    }

    private void render(Canvas canvas) {
        centerViewport();
        int mapH = viewTilesY * tileSize;

        canvas.drawColor(Color.BLACK);

        if (logic.inShop) {
            renderShop(canvas);
            return;
        }

        for (int vx = 0; vx < viewTilesX; vx++) {
            for (int vy = 0; vy < viewTilesY; vy++) {
                int wx = viewX + vx;
                int wy = viewY + vy;
                if (wx < 0 || wx >= GameLogic.WORLD_WIDTH || wy < 0 || wy >= GameLogic.WORLD_HEIGHT)
                    continue;

                int px = vx * tileSize;
                int py = vy * tileSize;

                if (!logic.explored[wx][wy]) {
                    paint.setColor(Color.BLACK);
                    canvas.drawRect(px, py, px + tileSize, py + tileSize, paint);
                    continue;
                }

                Tile tile = logic.tiles[wx][wy];
                boolean vis = logic.visible[wx][wy];
                int bright = vis ? 255 : 70;
                paint.setAlpha(bright);
                canvas.drawBitmap(atlas.getTile(tile), px, py, paint);
                paint.setAlpha(255);

                if (vis && quality.smoothLighting) {
                    paint.setColor(Color.argb(15, 255, 255, 200));
                    canvas.drawRect(px, py, px + tileSize, py + tileSize, paint);
                }
            }
        }

        for (Item it : logic.items) {
            int vx = it.x - viewX;
            int vy = it.y - viewY;
            if (vx < 0 || vx >= viewTilesX || vy < 0 || vy >= viewTilesY) continue;
            if (!logic.visible[it.x][it.y]) continue;
            canvas.drawBitmap(atlas.getItem(it.type), vx * tileSize, vy * tileSize, paint);
        }

        for (Entity m : logic.monsters) {
            if (!m.isAlive()) continue;
            int vx = m.x - viewX;
            int vy = m.y - viewY;
            if (vx < 0 || vx >= viewTilesX || vy < 0 || vy >= viewTilesY) continue;
            if (!logic.visible[m.x][m.y]) continue;
            Bitmap etex = atlas.getEntity(m.name.hashCode());
            canvas.drawBitmap(etex, vx * tileSize, vy * tileSize, paint);
            if (m.isBoss) {
                paint.setColor(Color.RED);
                paint.setTextSize(tileSize * 0.25f);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("BOSS", vx * tileSize + tileSize / 2f,
                    vy * tileSize - 4, paint);
            }
            if (quality != GraphicsQuality.POTATO && !m.isBoss) {
                paint.setColor(Color.WHITE);
                paint.setTextSize(tileSize * 0.25f);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(String.valueOf(m.hp), vx * tileSize + tileSize / 2f,
                    vy * tileSize + tileSize - 3, paint);
            }
        }

        canvas.drawBitmap(atlas.getPlayerSprite(quality.detailedSprites),
            (logic.player.x - viewX) * tileSize,
            (logic.player.y - viewY) * tileSize, paint);

        drawUI(canvas, mapH);

        if (logic.gameOver) {
            paint.setColor(Color.argb(190, 0, 0, 0));
            canvas.drawRect(0, 0, screenW, screenH, paint);
            paint.setColor(Color.RED);
            paint.setTextSize(44);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("GAME OVER", screenW / 2f, screenH / 3f, paint);
            paint.setTextSize(22);
            paint.setTypeface(Typeface.DEFAULT);
            paint.setColor(Color.WHITE);
            canvas.drawText("Depth: " + logic.currentDepth + "  Score: " + logic.score,
                screenW / 2f, screenH / 3f + 50, paint);
            canvas.drawText("Level: " + logic.player.level + "  Kills: " + logic.monstersKilled,
                screenW / 2f, screenH / 3f + 80, paint);
            paint.setTextSize(20);
            paint.setColor(Color.LTGRAY);
            canvas.drawText("Tap to restart", screenW / 2f, screenH / 2f + 50, paint);
        }
        drawSettingsOverlay(canvas);
    }

    private void renderShop(Canvas canvas) {
        paint.setColor(Color.rgb(15, 12, 10));
        canvas.drawRect(0, 0, screenW, screenH, paint);

        paint.setColor(Color.rgb(60, 50, 40));
        canvas.drawRoundRect(screenW * 0.05f, screenH * 0.05f,
            screenW * 0.95f, screenH * 0.95f, 16, 16, paint);
        paint.setColor(Color.rgb(80, 65, 50));
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRoundRect(screenW * 0.05f, screenH * 0.05f,
            screenW * 0.95f, screenH * 0.95f, 16, 16, paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(Color.rgb(255, 215, 0));
        paint.setTextSize(32);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("SHOP", screenW / 2f, screenH * 0.12f, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(18);
        paint.setTypeface(Typeface.DEFAULT);
        canvas.drawText("Gold: " + logic.player.gold, screenW / 2f, screenH * 0.17f, paint);

        if (logic.shopItems != null) {
            for (int i = 0; i < logic.shopItems.size(); i++) {
                Item it = logic.shopItems.get(i);
                if (it == null) continue;
                int y = (int)(screenH * 0.22f + i * screenH * 0.1f);
                paint.setColor(Color.rgb(40, 35, 30));
                canvas.drawRoundRect(screenW * 0.1f, y, screenW * 0.9f, y + screenH * 0.08f, 8, 8, paint);
                canvas.drawBitmap(atlas.getItem(it.type), screenW * 0.12f, y + 8, paint);
                paint.setColor(Color.WHITE);
                paint.setTextSize(16);
                paint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(it.name, screenW * 0.22f, y + 30, paint);
                paint.setColor(Color.rgb(255, 215, 0));
                paint.setTextAlign(Paint.Align.RIGHT);
                String priceStr = it.price + "g" + (logic.player.gold >= it.price ? "" : " (need more)");
                paint.setColor(logic.player.gold >= it.price ? Color.rgb(255, 215, 0) : Color.RED);
                canvas.drawText(priceStr, screenW * 0.85f, y + 30, paint);
            }
        }

        paint.setColor(Color.rgb(80, 180, 80));
        paint.setTextSize(20);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawRoundRect(screenW * 0.3f, screenH * 0.88f,
            screenW * 0.7f, screenH * 0.94f, 8, 8, paint);
        paint.setColor(Color.WHITE);
        canvas.drawText("LEAVE SHOP", screenW / 2f, screenH * 0.92f, paint);

        paint.setTextSize(14);
        paint.setColor(Color.GRAY);
        canvas.drawText("Tap item to buy, or LEAVE to exit", screenW / 2f, screenH * 0.98f, paint);
    }

    private void drawUI(Canvas canvas, int mapH) {
        int uiY = mapH;
        paint.setColor(Color.rgb(10, 10, 15));
        canvas.drawRect(0, uiY, screenW, screenH, paint);

        int hpX = 50;
        int hpW = (int)(screenW * 0.35f);
        paint.setColor(Color.RED);
        paint.setTextSize(16);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("HP", 10, uiY + 20, paint);
        paint.setColor(Color.DKGRAY);
        canvas.drawRect(hpX, uiY + 4, hpX + hpW, uiY + 20, paint);
        if (logic.player.hp > 0) {
            float ratio = (float)logic.player.hp / logic.player.maxHp;
            int hpColor = ratio > 0.5f ? 0xFF00FF00 : (ratio > 0.25f ? 0xFFFFA500 : 0xFFFF0000);
            paint.setColor(hpColor);
            canvas.drawRect(hpX, uiY + 4, hpX + hpW * ratio, uiY + 20, paint);
        }
        paint.setColor(Color.WHITE);
        paint.setTextSize(12);
        canvas.drawText(logic.player.hp + "/" + logic.player.maxHp, hpX + 5, uiY + 17, paint);

        int xpX = (int)(screenW * 0.5f);
        int xpW = (int)(screenW * 0.35f);
        paint.setColor(Color.rgb(100, 50, 150));
        canvas.drawText("XP", (int)(screenW * 0.45f), uiY + 20, paint);
        paint.setColor(Color.DKGRAY);
        canvas.drawRect(xpX, uiY + 4, xpX + xpW, uiY + 20, paint);
        if (logic.player.xpNext > 0) {
            float xpRatio = (float)logic.player.xp / logic.player.xpNext;
            paint.setColor(Color.rgb(150, 80, 200));
            canvas.drawRect(xpX, uiY + 4, xpX + xpW * Math.min(1, xpRatio), uiY + 20, paint);
        }
        paint.setColor(Color.WHITE);
        paint.setTextSize(11);
        canvas.drawText("Lv." + logic.player.level + " " + logic.player.xp + "/" + logic.player.xpNext,
            xpX + 3, uiY + 17, paint);

        int lineY = uiY + 30;
        paint.setTextSize(14);
        paint.setColor(Color.rgb(255, 215, 0));
        canvas.drawText("G:" + logic.player.gold, 10, lineY + 14, paint);
        paint.setColor(Color.LTGRAY);
        canvas.drawText("D:" + logic.currentDepth + " S:" + logic.score, screenW / 2, lineY + 14, paint);
        paint.setColor(Color.rgb(150, 150, 180));
        canvas.drawText("ATK:" + logic.player.attack + " DEF:" + logic.player.defense, 10, lineY + 30, paint);
        canvas.drawText("K:" + logic.monstersKilled, screenW / 2, lineY + 30, paint);
        if (logic.infiniteMode) {
            paint.setColor(Color.rgb(255, 100, 100));
            paint.setTextSize(12);
            canvas.drawText("INFINITE MODE", (int)(screenW * 0.7f), lineY + 14, paint);
        }

        paint.setTextSize(12);
        paint.setColor(Color.LTGRAY);
        String msg = logic.log.getLatest();
        if (msg.length() > 44) msg = msg.substring(0, 44);
        canvas.drawText(msg, 10, lineY + 50, paint);

        paint.setTextSize(10);
        paint.setColor(Color.GRAY);
        canvas.drawText("Swipe:move  Tap:wait  Quality:" + quality.name(), 10, lineY + 68, paint);

        paint.setColor(Color.rgb(60, 60, 80));
        canvas.drawRoundRect(settingsBtn.left, settingsBtn.top,
            settingsBtn.right, settingsBtn.bottom, 8, 8, paint);
        paint.setColor(Color.WHITE);
        paint.setTextSize(26);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("\u2699", settingsBtn.centerX(), settingsBtn.centerY() + 9, paint);
    }

    private void drawSettingsOverlay(Canvas canvas) {
        if (!showSettings) return;
        paint.setColor(Color.argb(200, 0, 0, 0));
        canvas.drawRect(0, 0, screenW, screenH, paint);

        int cx = screenW / 2, cy = screenH / 2;
        paint.setColor(Color.rgb(25, 25, 35));
        canvas.drawRoundRect(cx - 160, cy - 180, cx + 160, cy + 180, 16, 16, paint);
        paint.setColor(Color.WHITE);
        paint.setTextSize(22);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("SETTINGS", cx, cy - 150, paint);

        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextSize(15);
        GraphicsQuality[] qs = GraphicsQuality.values();
        for (int i = 0; i < qs.length; i++) {
            int y = cy - 100 + i * 38;
            boolean sel = quality == qs[i];
            paint.setColor(sel ? Color.rgb(80, 180, 255) : Color.rgb(120, 120, 120));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            canvas.drawRoundRect(cx - 120, y - 12, cx + 120, y + 16, 8, 8, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(sel ? Color.rgb(80, 180, 255) : Color.LTGRAY);
            canvas.drawText(qs[i].name() + " (" + qs[i].tileSize + "px)", cx, y + 6, paint);
        }

        int y = cy + 100;
        paint.setColor(Color.rgb(120, 120, 120));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        canvas.drawRoundRect(cx - 80, y - 12, cx + 80, y + 16, 8, 8, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(sound.isEnabled() ? Color.rgb(80, 200, 80) : Color.rgb(200, 80, 80));
        canvas.drawText(sound.isEnabled() ? "SOUND: ON" : "SOUND: OFF", cx, y + 6, paint);

        paint.setTextSize(13);
        paint.setColor(Color.GRAY);
        canvas.drawText("Tap outside to close", cx, cy + 155, paint);
    }

    private void centerViewport() {
        viewX = logic.player.x - viewTilesX / 2;
        viewY = logic.player.y - viewTilesY / 2;
        viewX = Math.max(0, Math.min(viewX, GameLogic.WORLD_WIDTH - viewTilesX));
        viewY = Math.max(0, Math.min(viewY, GameLogic.WORLD_HEIGHT - viewTilesY));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                touchStartX = event.getX();
                touchStartY = event.getY();
            }
            return true;
        }

        float ex = event.getX(), ey = event.getY();

        if (showSettings) {
            handleSettingsTap(ex, ey);
            return true;
        }

        if (settingsBtn.contains((int)ex, (int)ey)) {
            showSettings = true;
            return true;
        }

        if (logic.gameOver || logic.won) {
            logic = new GameLogic();
            logic.setSoundListener(this);
            return true;
        }

        if (logic.inShop) {
            handleShopTap(ex, ey);
            return true;
        }

        float dx = ex - touchStartX, dy = ey - touchStartY;
        float dist = (float)Math.sqrt(dx * dx + dy * dy);

        if (dist < SWIPE_THRESHOLD) {
            logic.endTurn();
            sound.play("step");
        } else if (Math.abs(dx) > Math.abs(dy)) {
            logic.movePlayer(dx > 0 ? Direction.RIGHT : Direction.LEFT);
            sound.play("step");
        } else {
            logic.movePlayer(dy > 0 ? Direction.DOWN : Direction.UP);
            sound.play("step");
        }
        return true;
    }

    private void handleShopTap(float x, float y) {
        if (x >= screenW * 0.3f && x <= screenW * 0.7f &&
            y >= screenH * 0.88f && y <= screenH * 0.94f) {
            logic.leaveShop();
            return;
        }

        if (logic.shopItems != null) {
            for (int i = 0; i < logic.shopItems.size(); i++) {
                Item it = logic.shopItems.get(i);
                if (it == null) continue;
                int sy = (int)(screenH * 0.22f + i * screenH * 0.1f);
                if (x >= screenW * 0.1f && x <= screenW * 0.9f &&
                    y >= sy && y <= sy + screenH * 0.08f) {
                    logic.buyItem(i);
                    return;
                }
            }
        }
    }

    private void handleSettingsTap(float x, float y) {
        int cx = screenW / 2, cy = screenH / 2;

        if (x < cx - 160 || x > cx + 160 || y < cy - 180 || y > cy + 180) {
            showSettings = false;
            return;
        }

        GraphicsQuality[] qs = GraphicsQuality.values();
        for (int i = 0; i < qs.length; i++) {
            int sy = cy - 100 + i * 38;
            if (y >= sy - 12 && y <= sy + 16 && x >= cx - 120 && x <= cx + 120) {
                if (quality != qs[i]) {
                    quality = qs[i];
                    recalcLayout();
                    logic.updateFov();
                }
                return;
            }
        }

        int sy = cy + 100;
        if (y >= sy - 12 && y <= sy + 16 && x >= cx - 80 && x <= cx + 80) {
            sound.setEnabled(!sound.isEnabled());
        }
    }

    public void onSound(String name) { sound.play(name); }
    public void playSound(String name) { sound.play(name); }
    public SoundManager getSound() { return sound; }
}
