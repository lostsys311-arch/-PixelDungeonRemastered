package com.dungeon.game;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.SoundPool;
import java.io.*;
import java.util.HashMap;

public class SoundManager {
    private SoundPool soundPool;
    private HashMap<String, Integer> soundIds;
    private Context context;
    private float volume;
    private boolean enabled;

    private static final int SAMPLE_RATE = 22050;

    public SoundManager(Context context) {
        this.context = context;
        this.enabled = true;
        this.volume = 0.5f;
        soundIds = new HashMap<>();
        AudioAttributes attrs = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();
        soundPool = new SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attrs)
            .build();
        generateSounds();
    }

    public void setEnabled(boolean e) { this.enabled = e; }
    public boolean isEnabled() { return enabled; }
    public void setVolume(float v) { this.volume = Math.max(0, Math.min(1, v)); }

    private void generateSounds() {
        try {
            soundIds.put("player_hit", loadSound(genHit(0.15f)));
            soundIds.put("monster_hit", loadSound(genHit(0.12f)));
            soundIds.put("player_attack", loadSound(genSwing(0.1f)));
            soundIds.put("item_pickup", loadSound(genPickup(0.08f)));
            soundIds.put("stairs", loadSound(genStairs(0.2f)));
            soundIds.put("death", loadSound(genDeath(0.3f)));
            soundIds.put("victory", loadSound(genVictory(0.4f)));
            soundIds.put("monster_die", loadSound(genMonsterDie(0.15f)));
            soundIds.put("heal", loadSound(genHeal(0.2f)));
            soundIds.put("step", loadSound(genStep(0.06f)));
        } catch (IOException e) {
            enabled = false;
        }
    }

    private int loadSound(byte[] wavData) throws IOException {
        File dir = new File(context.getCacheDir(), "sounds");
        dir.mkdirs();
        File tmp = File.createTempFile("snd", ".wav", dir);
        FileOutputStream fos = new FileOutputStream(tmp);
        fos.write(wavData);
        fos.close();
        int id = soundPool.load(tmp.getPath(), 1);
        tmp.delete();
        return id;
    }

    public void play(String name) {
        if (!enabled) return;
        Integer id = soundIds.get(name);
        if (id != null && id > 0)
            soundPool.play(id, volume, volume, 1, 0, 1.0f);
    }

    public void release() {
        soundPool.release();
    }

    private byte[] genHit(float dur) {
        int len = (int)(SAMPLE_RATE * dur);
        byte[] data = new byte[len];
        for (int i = 0; i < len; i++) {
            float t = (float)i / SAMPLE_RATE;
            float env = 1f - (float)i / len;
            double val = Math.sin(2 * Math.PI * 150 * t) * env * 0.5
                       + (Math.random() - 0.5) * env * 0.3;
            data[i] = (byte)(val * 127);
        }
        return createWav(data);
    }

    private byte[] genSwing(float dur) {
        int len = (int)(SAMPLE_RATE * dur);
        byte[] data = new byte[len];
        for (int i = 0; i < len; i++) {
            float t = (float)i / SAMPLE_RATE;
            float freq = 200 + 400 * t / dur;
            float env = 1f - (float)i / len;
            double val = Math.sin(2 * Math.PI * freq * t) * env * 0.4;
            data[i] = (byte)(val * 127);
        }
        return createWav(data);
    }

    private byte[] genPickup(float dur) {
        int len = (int)(SAMPLE_RATE * dur);
        byte[] data = new byte[len];
        for (int i = 0; i < len; i++) {
            float t = (float)i / SAMPLE_RATE;
            float freq = 800 + 600 * t / dur;
            float env = 1f - (float)i / len;
            double val = Math.sin(2 * Math.PI * freq * t) * env * 0.4;
            data[i] = (byte)(val * 127);
        }
        return createWav(data);
    }

    private byte[] genStairs(float dur) {
        int len = (int)(SAMPLE_RATE * dur);
        byte[] data = new byte[len];
        int half = len / 4;
        for (int i = 0; i < len; i++) {
            float t = (float)i / SAMPLE_RATE;
            float note = (i < half) ? 300 : (i < half * 2 ? 400 : (i < half * 3 ? 500 : 600));
            float env = 1f - (float)i / len;
            double val = Math.sin(2 * Math.PI * note * t) * env * 0.3;
            data[i] = (byte)(val * 127);
        }
        return createWav(data);
    }

    private byte[] genDeath(float dur) {
        int len = (int)(SAMPLE_RATE * dur);
        byte[] data = new byte[len];
        for (int i = 0; i < len; i++) {
            float t = (float)i / SAMPLE_RATE;
            float freq = 300 - 200 * t / dur;
            float env = 1f - (float)i / len;
            double val = Math.sin(2 * Math.PI * freq * t) * env * 0.4
                       + (Math.random() - 0.5) * env * 0.2;
            data[i] = (byte)(val * 127);
        }
        return createWav(data);
    }

    private byte[] genVictory(float dur) {
        int len = (int)(SAMPLE_RATE * dur);
        byte[] data = new byte[len];
        int half = len / 3;
        for (int i = 0; i < len; i++) {
            float t = (float)i / SAMPLE_RATE;
            float note = (i < half) ? 400 : (i < half * 2 ? 500 : 600);
            float env = 1f - (float)i / len;
            double val = Math.sin(2 * Math.PI * note * t) * env * 0.35;
            data[i] = (byte)(val * 127);
        }
        return createWav(data);
    }

    private byte[] genMonsterDie(float dur) {
        int len = (int)(SAMPLE_RATE * dur);
        byte[] data = new byte[len];
        for (int i = 0; i < len; i++) {
            float t = (float)i / SAMPLE_RATE;
            float freq = 400 - 300 * t / dur;
            float env = 1f - (float)i / len;
            double val = (Math.random() - 0.5) * env * 0.5
                       + Math.sin(2 * Math.PI * freq * t) * env * 0.2;
            data[i] = (byte)(val * 127);
        }
        return createWav(data);
    }

    private byte[] genHeal(float dur) {
        int len = (int)(SAMPLE_RATE * dur);
        byte[] data = new byte[len];
        for (int i = 0; i < len; i++) {
            float t = (float)i / SAMPLE_RATE;
            float freq = 500 + 300 * t / dur;
            float env = 1f - (float)i / len;
            double val = Math.sin(2 * Math.PI * freq * t) * env * 0.3;
            data[i] = (byte)(val * 127);
        }
        return createWav(data);
    }

    private byte[] genStep(float dur) {
        int len = (int)(SAMPLE_RATE * dur);
        byte[] data = new byte[len];
        for (int i = 0; i < len; i++) {
            float env = 1f - (float)i / len;
            double val = (Math.random() - 0.5) * env * 0.4;
            data[i] = (byte)(val * 127);
        }
        return createWav(data);
    }

    private byte[] createWav(byte[] pcm) {
        int dataLen = pcm.length;
        int totalLen = 44 + dataLen;
        ByteArrayOutputStream bos = new ByteArrayOutputStream(totalLen);
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            dos.writeBytes("RIFF");
            dos.writeInt(Integer.reverseBytes(totalLen - 8));
            dos.writeBytes("WAVE");
            dos.writeBytes("fmt ");
            dos.writeInt(Integer.reverseBytes(16));
            dos.writeShort(Short.reverseBytes((short)1));
            dos.writeShort(Short.reverseBytes((short)1));
            dos.writeInt(Integer.reverseBytes(SAMPLE_RATE));
            dos.writeInt(Integer.reverseBytes(SAMPLE_RATE));
            dos.writeShort(Short.reverseBytes((short)2));
            dos.writeShort(Short.reverseBytes((short)8));
            dos.writeBytes("data");
            dos.writeInt(Integer.reverseBytes(dataLen));
            dos.write(pcm);
        } catch (IOException e) {}
        return bos.toByteArray();
    }
}
