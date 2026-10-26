package wfg.native_ui.internal.util;

import org.lwjgl.opengl.GL11;

import com.fs.starfarer.api.graphics.SpriteAPI;

public class NineSliceUtils {
    private static final float EPSILON = 0.001f;

    /**
     * Draws a quad sampling UV region {@code [0, uMax] × [0, vMax]} of the currently bound texture.
     * Assumes no texture offset (standalone sprite; {@code texX == texY == 0}).
     */
    public static final void drawCorner(
        boolean flipU, boolean flipV, float uMax, float vMax,
        float x, float y, float w, float h
    ) {
        final float uLeft = flipU ? uMax : 0f;
        final float uRight = flipU ? 0f : uMax;
        final float vBottom = flipV ? vMax : 0f;
        final float vTop = flipV ? 0f : vMax;

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(uLeft, vBottom);
        GL11.glVertex2f(x, y);
        GL11.glTexCoord2f(uLeft, vTop);
        GL11.glVertex2f(x, y + h);
        GL11.glTexCoord2f(uRight, vTop);
        GL11.glVertex2f(x + w, y + h);
        GL11.glTexCoord2f(uRight, vBottom);
        GL11.glVertex2f(x + w, y);
        GL11.glEnd();
    }

    /**
     * Draws a quad sampling UV region {@code [EPSILON, uMax - EPSILON] × [EPSILON, vMax - EPSILON]}
     * of the currently bound texture. Epsilon-inset to avoid linear-filter bleed on stretched quads.
     * Assumes no texture offset (standalone sprite; {@code texX == texY == 0}).
     */
    public static final void drawStretch(
        boolean flipU, boolean flipV, float uMax, float vMax,
        float x, float y, float w, float h
    ) {
        final float uLeft = flipU ? uMax - EPSILON : EPSILON;
        final float uRight = flipU ? EPSILON : uMax - EPSILON;
        final float vBottom = flipV ? vMax - EPSILON : EPSILON;
        final float vTop = flipV ? EPSILON : vMax - EPSILON;

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(uLeft, vBottom);
        GL11.glVertex2f(x, y);
        GL11.glTexCoord2f(uLeft, vTop);
        GL11.glVertex2f(x, y + h);
        GL11.glTexCoord2f(uRight, vTop);
        GL11.glVertex2f(x + w, y + h);
        GL11.glTexCoord2f(uRight, vBottom);
        GL11.glVertex2f(x + w, y);
        GL11.glEnd();
    }

    /**
     * Draws a 1:1 corner from the currently bound texture (full texture region, no offset).
     */
    public static final void drawCorner(
        boolean flipU, boolean flipV,
        float x, float y, float w, float h
    ) {
        drawCorner(flipU, flipV, 1f, 1f, x, y, w, h);
    }

    /**
     * Draws a 1:1 corner from a SpriteAPI, sized to the sprite's natural dimensions.
     * Assumes no texture offset (standalone sprite; {@code texX == texY == 0}).
     */
    public static final void drawCorner(
        SpriteAPI sprite, boolean flipU, boolean flipV, float x, float y
    ) {
        drawCorner(
            flipU, flipV,
            sprite.getTexWidth(), sprite.getTexHeight(),
            x, y,
            sprite.getWidth(), sprite.getHeight()
        );
    }

    /**
     * Draws a corner from a SpriteAPI with a custom render size.
     * Assumes no texture offset (standalone sprite; {@code texX == texY == 0}).
     */
    public static final void drawCorner(
        SpriteAPI sprite, boolean flipU, boolean flipV,
        float x, float y, float w, float h
    ) {
        drawCorner(
            flipU, flipV,
            sprite.getTexWidth(), sprite.getTexHeight(),
            x, y, w, h
        );
    }

    /**
     * Draws a stretched quad from the currently bound texture (full texture region, no offset).
     */
    public static final void drawStretch(
        boolean flipU, boolean flipV,
        float x, float y, float w, float h
    ) {
        drawStretch(flipU, flipV, 1f, 1f, x, y, w, h);  // ← fixed
    }

    /**
     * Draws a stretched quad from a SpriteAPI, sized to the sprite's natural dimensions.
     * Assumes no texture offset (standalone sprite; {@code texX == texY == 0}).
     */
    public static final void drawStretch(
        SpriteAPI sprite, boolean flipU, boolean flipV, float x, float y
    ) {
        drawStretch(
            flipU, flipV,
            sprite.getTexWidth(), sprite.getTexHeight(),
            x, y,
            sprite.getWidth(), sprite.getHeight()
        );
    }

    /**
     * Draws a stretched quad from a SpriteAPI with a custom render size.
     * Assumes no texture offset (standalone sprite; {@code texX == texY == 0}).
     */
    public static final void drawStretch(
        SpriteAPI sprite, boolean flipU, boolean flipV,
        float x, float y, float w, float h
    ) {
        drawStretch(
            flipU, flipV,
            sprite.getTexWidth(), sprite.getTexHeight(),
            x, y, w, h
        );
    }
}