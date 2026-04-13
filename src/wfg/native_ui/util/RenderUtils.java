package wfg.native_ui.util;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;

import java.awt.Color;

public class RenderUtils {
    private static final SettingsAPI settings = Global.getSettings();
    private static final SpriteAPI LINE_TEX = settings.getSprite("graphics/hud/line4x4.png");

    /**
     * @param x = posX
     * @param y = posY
     * @param w = width
     * @param h = height
     * @param t = thickness
     */
    public static final void drawFramedBorder(float x, float y, float w, float h, float t, Color       
        color, float alphaMult) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        setGlColor(color, alphaMult);
        GL11.glBegin(GL11.GL_QUADS);

        // Bottom
        GL11.glVertex2f(x - t, y - t);
        GL11.glVertex2f(x + w, y - t);
        GL11.glVertex2f(x + w, y);
        GL11.glVertex2f(x - t, y);
        
        // Right
        GL11.glVertex2f(x + w, y - t);
        GL11.glVertex2f(x + w + t, y - t);
        GL11.glVertex2f(x + w + t, y + h);
        GL11.glVertex2f(x + w, y + h);

        // Top
        GL11.glVertex2f(x, y + h);
        GL11.glVertex2f(x + w + t, y + h);
        GL11.glVertex2f(x + w + t, y + h + t);
        GL11.glVertex2f(x, y + h + t);

        // Left
        GL11.glVertex2f(x - t, y);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x, y + h + t);
        GL11.glVertex2f(x - t, y + h + t);

        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    public static final void drawQuad(float x, float y, float w, float h, Color color, float alphaMult,
        boolean additive) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(
            GL11.GL_SRC_ALPHA,
            additive ? GL11.GL_ONE : GL11.GL_ONE_MINUS_SRC_ALPHA
        );

        setGlColor(color, alphaMult);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x + w, y);
        GL11.glVertex2f(x + w, y + h);
        GL11.glVertex2f(x, y + h);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    public static final void drawAdditiveGlow(SpriteAPI sprite, float x, float y, Color glowColor, float intensity) {
        if (sprite == null || intensity <= 0f) return;

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        final int r = Math.min(255, (int)(glowColor.getRed() * intensity));
        final int g = Math.min(255, (int)(glowColor.getGreen() * intensity));
        final int b = Math.min(255, (int)(glowColor.getBlue() * intensity));
        final int a = Math.min(255, (int)(255 * intensity));
        sprite.setColor(new Color(r, g, b, a));

        sprite.setAdditiveBlend();
        sprite.render(x, y);

        sprite.setNormalBlend();
        sprite.setColor(Color.white);

        GL11.glPopMatrix();
    }

    public static final void setGlColor(Color color, float alphaMult) {
        GL11.glColor4ub(
            (byte) color.getRed(),
            (byte) color.getGreen(),
            (byte) color.getBlue(),
            (byte) (color.getAlpha() * alphaMult)
        );
    }

    public static final void drawSpriteOutline(SpriteAPI sprite, Color color, float x, float y, float w, float h,
        float alpha, float radius) {

        if (color == null || sprite == null) {
            return;
        }

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();

        sprite.setSize(w, h);

        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glColorMask(false, false, false, true);
        quadWithBlend(x - w / 2f, y - h / 2f, w * 2.0F, h * 2.0F, new Color(0, 0, 0, 0), 0.0F);

        sprite.setBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        sprite.setAlphaMult(alpha * 0.75f);


        for(float angle = 0; angle < 360; angle += 30) {
            float dx = (float) Math.cos(Math.toRadians(angle));
            float dy = (float) Math.sin(Math.toRadians(angle));
            sprite.render(x + radius * dx, y + radius * dy);
        }

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_ZERO, GL11.GL_SRC_ALPHA);

        quadNoBlend(x - w / 2f + 1, y - h / 2f + 1, w * 2 - 2, h * 2 - 2, Color.white, alpha);
        quadNoBlend(x - w / 2f + 1, y - h / 2f + 1, w * 2 - 2, h * 2 - 2, Color.white, alpha);
        GL11.glColorMask(true, true, true, true);
        GL11.glBlendFunc(GL11.GL_DST_ALPHA, GL11.GL_ONE_MINUS_DST_ALPHA);
        quadNoBlend(x - w / 2f + 1, y - h / 2f + 1, w * 2 - 2, h * 2 - 2, color, alpha);

        GL11.glPopMatrix();
        GL11.glPopAttrib();

        sprite.setAlphaMult(1f);
        sprite.setNormalBlend();
    }

    public static final void drawGradientSprite(
        float x1, float y1,
        float x2, float y2,
        float gradientWidth,
        Color color, boolean additive,
        float alphaStart, float alphaMiddle, float alphaEnd
    ) {
        drawGradientSprite(
            LINE_TEX, x1, y1, x2, y2, gradientWidth, color, additive, alphaStart, alphaMiddle, alphaEnd
        );
    }

    public static final void drawGradientSprite(
        SpriteAPI sprite,
        float x1, float y1,
        float x2, float y2,
        float gradientWidth,
        Color color, boolean additive,
        float alphaStart, float alphaMiddle, float alphaEnd
    ) {
        GL11.glPushMatrix();

        if (sprite != null) {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            sprite.bindTexture();
        } else {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
        }

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, additive ? GL11.GL_ONE : GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Compute the orthogonal vector for the gradient
        final Vector2f edge = new Vector2f(x2 - x1, y2 - y1);
        normalize(edge);
        edge.set(edge.y, -edge.x);
        edge.scale(gradientWidth * 0.5f);

        // Draw the quad with gradient alpha
        GL11.glBegin(GL11.GL_QUAD_STRIP);

        // Left edge
        GL11.glColor4ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(),
            (byte)Math.min(255, (int)(color.getAlpha() * alphaStart)));
        GL11.glTexCoord2f(0f, 0f);
        GL11.glVertex2f(x1 - edge.x, y1 - edge.y);
        GL11.glTexCoord2f(0f, 1f);
        GL11.glVertex2f(x1 + edge.x, y1 + edge.y);

        // Middle
        GL11.glColor4ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(),
            (byte)Math.min(255, (int)(color.getAlpha() * alphaMiddle)));
        GL11.glTexCoord2f(0.5f, 0f);
        GL11.glVertex2f((x1 + x2) * 0.5f - edge.x, (y1 + y2) * 0.5f - edge.y);
        GL11.glTexCoord2f(0.5f, 1f);
        GL11.glVertex2f((x1 + x2) * 0.5f + edge.x, (y1 + y2) * 0.5f + edge.y);

        // Right edge
        GL11.glColor4ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(),
            (byte)Math.min(255, (int)(color.getAlpha() * alphaEnd)));
        GL11.glTexCoord2f(1f, 0f);
        GL11.glVertex2f(x2 - edge.x, y2 - edge.y);
        GL11.glTexCoord2f(1f, 1f);
        GL11.glVertex2f(x2 + edge.x, y2 + edge.y);

        GL11.glEnd();
        GL11.glPopMatrix();
    }

    public static final void drawHighlightBar(
        float x, float y, float w, float h,
        Color baseColor, float alpha, float highlightIntensity, boolean darkOverlay
    ) {
        if (h <= 1f) return;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Top half gradient shine
        float shineFactor = 0.3f + highlightIntensity * 0.7f;
        Color shineColor = blendColors(baseColor, Color.WHITE, shineFactor * 0.5f);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor4ub((byte) shineColor.getRed(), (byte) shineColor.getGreen(), (byte) shineColor.getBlue(),
            (byte) (alpha * shineColor.getAlpha() * 0.75f));
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x + w, y);
        GL11.glVertex2f(x + w, y + h / 2f);
        GL11.glVertex2f(x, y + h / 2f);
        GL11.glVertex2f(x + w, y + h / 2f);
        GL11.glVertex2f(x, y + h / 2f);
        GL11.glVertex2f(x, y + h);
        GL11.glVertex2f(x + w, y + h);
        GL11.glEnd();

        // Main gradient
        float mainFactor = alpha * 0.75f + 0.25f * highlightIntensity;
        Color mainColor = baseColor;
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        if (darkOverlay) {
            mainColor = new Color(0, 0, 0, 127);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            mainFactor = 1f;
        }

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor4ub((byte) mainColor.getRed(), (byte) mainColor.getGreen(), (byte) mainColor.getBlue(), (byte) 0);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x + w, y);

        GL11.glColor4ub((byte) mainColor.getRed(), (byte) mainColor.getGreen(), (byte) mainColor.getBlue(),
                        (byte) (mainFactor * mainColor.getAlpha()));
        GL11.glVertex2f(x + w, y + h / 2f);
        GL11.glVertex2f(x, y + h / 2f);

        GL11.glColor4ub((byte) mainColor.getRed(), (byte) mainColor.getGreen(), (byte) mainColor.getBlue(),
                        (byte) (mainFactor * mainColor.getAlpha()));
        GL11.glVertex2f(x + w, y + h / 2f);
        GL11.glVertex2f(x, y + h / 2f);

        GL11.glColor4ub((byte) mainColor.getRed(), (byte) mainColor.getGreen(), (byte) mainColor.getBlue(), (byte) 0);
        GL11.glVertex2f(x, y + h);
        GL11.glVertex2f(x + w, y + h);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    public static final Color blendColors(Color c1, Color c2, float t) {
        if (t <= 0f) return c1;
        if (t >= 1f) return c2;

        final int r = Math.min(255, Math.max(0, Math.round(c1.getRed() + (c2.getRed() - c1.getRed()) * t)));
        final int g = Math.min(255, Math.max(0, Math.round(c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t)));
        final int b = Math.min(255, Math.max(0, Math.round(c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t)));
        final int a = Math.min(255, Math.max(0, Math.round(c1.getAlpha() + (c2.getAlpha() - c1.getAlpha()) * t)));

        return new Color(r, g, b, a);
    }

    /**
     * Draws a filled convex polygon using CCW vertices.
     *
     * @param verts  float array of XY pairs: [x0, y0, x1, y1, ...]
     * @param color  fill color
     * @param alpha  transparency multiplier (0..1)
     */
    public static final void drawPolygon(float[] verts, Color color, float alpha) {
        if (verts == null || verts.length < 6) return; // at least 3 points

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        setGlColor(color, alpha);

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        for (int i = 0; i < verts.length; i += 2) {
            GL11.glVertex2f(verts[i], verts[i + 1]);
        }
        GL11.glEnd();
    }

    /**
     * Builds a CCW vertex array for a rectangle with corner cuts.
     *
     * @param x     bottom-left X
     * @param y     bottom-left Y
     * @param w     width
     * @param h     height
     * @param cuts  4-element array [BL, BR, TR, TL] in pixels
     * @return      float array of XY pairs for the convex polygon
     */
    public static final float[] buildCornersVertices(float x, float y, float w, float h, float[] cuts) {
        if (cuts == null || cuts.length != 4) cuts = new float[]{0f, 0f, 0f, 0f};
        final float cutBL = cuts[0], cutBR = cuts[1], cutTR = cuts[2], cutTL = cuts[3];

        return new float[] {
            x + cutBL,        y,             // bottom-left inner
            x + w - cutBR,    y,             // bottom-right inner
            x + w,            y + cutBR,     // bottom-right vertical
            x + w,            y + h - cutTR, // top-right vertical
            x + w - cutTR,    y + h,         // top-right horizontal
            x + cutTL,        y + h,         // top-left horizontal
            x,                y + h - cutTL, // top-left vertical
            x,                y + cutBL      // bottom-left vertical
        };
    }

    /**
     * Available prefixes:
     * <ul>
     *  <li>{@link UIConstants#UI_BORDER_1}</li>
     *  <li>{@link UIConstants#UI_BORDER_2}</li>
     *  <li>{@link UIConstants#UI_BORDER_3}</li>
     *  <li>{@link UIConstants#UI_BORDER_4}</li>
     * </ul>
     */
    public static final void drawRoundedBorder(float x, float y, float width, float height,
        float alpha, String borderPrefix, int textureSize, Color color
    ) {
        final SpriteAPI nw = settings.getSprite("ui", borderPrefix + "_top_left");
        final SpriteAPI ne = settings.getSprite("ui", borderPrefix + "_top_right");
        final SpriteAPI sw = settings.getSprite("ui", borderPrefix + "_bot_left");
        final SpriteAPI se = settings.getSprite("ui", borderPrefix + "_bot_right");

        final SpriteAPI n = settings.getSprite("ui", borderPrefix + "_top");
        final SpriteAPI s = settings.getSprite("ui", borderPrefix + "_bot");
        final SpriteAPI w = settings.getSprite("ui", borderPrefix + "_left");
        final SpriteAPI e = settings.getSprite("ui", borderPrefix + "_right");

        for (SpriteAPI sprite : new SpriteAPI[] { nw, ne, sw, se, n, s, w, e }) {
            sprite.setAlphaMult(alpha);
            sprite.setColor(color);
        }

        // Draw corners
        nw.render(x, y + height - textureSize);
        ne.render(x + width - textureSize, y + height - textureSize);
        sw.render(x, y);
        se.render(x + width - textureSize, y);

        // Resize edges to stretch between corners
        n.setSize(width - 2 * textureSize, textureSize);
        s.setSize(width - 2 * textureSize, textureSize);
        w.setSize(textureSize, height - 2 * textureSize);
        e.setSize(textureSize, height - 2 * textureSize);

        // Draw edges
        n.render(x + textureSize, y + height - textureSize);
        s.render(x + textureSize, y);
        w.render(x, y + textureSize);
        e.render(x + width - textureSize, y + textureSize);
    }

    private static final void quadWithBlend(float x, float y, float w, float h, Color color, float alphaMult) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ZERO);
        setGlColor(color, alphaMult);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x, y + h);
        GL11.glVertex2f(x + w, y + h);
        GL11.glVertex2f(x + w, y);
        GL11.glEnd();
    }

    private static final void quadNoBlend(float x, float y, float w, float h, Color color, float alphaMult) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        setGlColor(color, alphaMult);
        
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x, y + h);
        GL11.glVertex2f(x + w, y + h);
        GL11.glVertex2f(x + w, y);
        GL11.glEnd();
    }

    private static final Vector2f normalize(Vector2f vec) {
        if (vec.lengthSquared() > Float.MIN_VALUE) {
            vec.normalise();
        }
        return vec;
    }
}   