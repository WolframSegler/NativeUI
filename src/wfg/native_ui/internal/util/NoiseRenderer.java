package wfg.native_ui.internal.util;

import static wfg.native_ui.util.Globals.settings;

import java.util.Random;
import java.awt.Color;

import org.lwjgl.opengl.GL11;

import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;

import wfg.native_ui.util.RenderUtils;

public final class NoiseRenderer {
    public final FaderUtil fader = new FaderUtil(0f, 1f, 1f, false, true);
    public Color noiseColor = settings.getColor("noiseColor");
    public float renderW;
    public float renderH;

    private final SpriteAPI mSprite;
    private final NoiseGenerator mNoise = new NoiseGenerator();
    private long mRandomNum = 0L;

    public NoiseRenderer(SpriteAPI sprite, float width, float height) {
        mSprite = sprite;
        renderW = width;
        renderH = height;
    }

    public float getNoiseBrightness() {
        return (0.5f + 0.5f * mNoise.getIntensity()) * fader.getBrightness();
    }

    public float getBrightness() {
        return fader.getBrightness();
    }

    public void fadeIn(float inDuration) {
        fader.setDurationIn(inDuration);
        fader.fadeIn();
    }

    public void fadeOut(float inDuration) {
        fader.setDurationOut(inDuration);
        fader.fadeOut();
    }

    public void fadeIn(float inDuration, float outDuration) {
        fader.setDuration(inDuration, outDuration);
        fader.fadeIn();
    }

    public boolean isMaxBrightness() {
        return fader.getBrightness() == 1f;
    }

    public void advance(float delta) {
        fader.advance(delta);

        if (fader.getBrightness() > 0f) {
            mNoise.advance(delta);
            mRandomNum = Misc.random.nextLong();
        }
    }

    public void render(float x, float y, float intensityMultiplier) {
        final float intensity = getNoiseBrightness() * intensityMultiplier;
        if (intensity > 0f) {
            final Random rand = mRandomNum == 0L ? new Random() : new Random(mRandomNum);

            render(mSprite, intensity, x, y, rand.nextFloat(), rand.nextFloat(), 1f * renderW / mSprite.getWidth(), 1f * renderH / mSprite.getHeight());
        }
    }

    private void render(SpriteAPI sprite, float alpha, float x, float y, float uStart,
        float vStart, float uWidth, float vHeight
    ) {
        sprite.bindTexture();
        final float texW = sprite.getTexWidth();
        final float texH = sprite.getTexHeight();

        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0f);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        RenderUtils.setGlColor(noiseColor, alpha);
        final float epsilon = 0.001f;
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(uStart * texW + epsilon, vStart * texH + epsilon);
        GL11.glVertex2f(0f, 0f);
        GL11.glTexCoord2f(uStart * texW + epsilon, (vStart + vHeight) * texH - epsilon);
        GL11.glVertex2f(0f, renderH);
        GL11.glTexCoord2f((uStart + uWidth) * texW - epsilon, (vStart + vHeight) * texH - epsilon);
        GL11.glVertex2f(renderW, renderH);
        GL11.glTexCoord2f((uStart + uWidth) * texW - epsilon, vStart * texH + epsilon);
        GL11.glVertex2f(renderW, 0f);
        GL11.glEnd();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }
}