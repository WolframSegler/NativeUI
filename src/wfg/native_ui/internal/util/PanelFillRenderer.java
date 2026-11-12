package wfg.native_ui.internal.util;

import org.lwjgl.opengl.GL11;

import com.fs.starfarer.api.graphics.SpriteAPI;

import wfg.native_ui.util.RenderUtils;

import java.awt.Color;

public final class PanelFillRenderer {
    public boolean useOverlay = false;
    public boolean useAdditiveBlend = true;
    public boolean useGradient = true;
    public float panelW = 0f, panelH = 0f;
    public float edgeSize = 0f;

    private SpriteAPI mSprite;
    private Color mTopColor = new Color(30, 114, 132);
    private Color mBottomColor = new Color(10, 38, 44);
    private Color mTopOverlayColor = new Color(30, 114, 132, 155);
    private Color mBottomOverlayColor = new Color(10, 38, 44, 155);

    public PanelFillRenderer(SpriteAPI sprite) {
        this(sprite, sprite.getWidth(), sprite.getHeight());
    }

    public PanelFillRenderer(SpriteAPI sprite, float width, float height) {
        mSprite = sprite;
        panelW = width;
        panelH = height;
    }

    public void setSize(float width, float height) {
        panelW = width;
        panelH = height;
    }

    public void advance(float delta) {}

    public void renderVerticalGradient(float x, float y, float alpha) {
        renderGradient(mTopColor, mBottomColor, x, y, alpha, 0f);
        if (useOverlay) {
            renderGradient(mTopOverlayColor, mBottomOverlayColor, x, y + 1f, alpha, -1f);
        }
    }

    /**
     * Helper method that renders a two-color gradient with optional top/bottom caps.
     */
    private void renderGradient(Color topColor, Color bottomColor, float x, float y, float alpha,
        float yOffsset
    ) {
        if (useGradient) {
            if (edgeSize > 0f) {
                drawTexturedQuad(topColor, bottomColor, topColor, bottomColor, alpha, x, y, 0f, 0f, edgeSize / mSprite.getWidth(), (panelH + yOffsset) / mSprite.getHeight());
                drawTexturedQuad(bottomColor, topColor, bottomColor, topColor, alpha, x + panelW - edgeSize, y, 0f, 0f, edgeSize / mSprite.getWidth(), (panelH + yOffsset) / mSprite.getHeight());
            }

            drawTexturedQuad(bottomColor, bottomColor, bottomColor, bottomColor, alpha, x + edgeSize, y, 0f, 0f, (panelW - edgeSize * 2f) / mSprite.getWidth(), (panelH + yOffsset) / mSprite.getHeight());
        }
    }

    /**
     * Renders a simple quad with a single color (or gradient).
     */
    public void renderQuad(float x1, float y1, float x2, float y2, float x3, float y3, float x4, 
        float y4, float alpha) {
        renderQuadWithColor(mBottomColor, alpha, x1, y1, x2, y2, x3, y3, x4, y4);
        if (useOverlay) {
            renderQuadWithColor(mBottomOverlayColor, alpha, x1, y1 + 1f, x2, y2 + 1f, x3, y3 + 1f, x4, y4 + 1f);
        }
    }

    /**
     * Renders a textured quad with a subtle top-to-bottom gradient overlay.
     */
    public void renderTexturedQuad(float x, float y, float alpha) {
        Color transparentColor = new Color(mBottomColor.getRed(), mBottomColor.getGreen(), mBottomColor.getBlue(), 0);

        drawTexturedQuad(mBottomColor, mBottomColor, transparentColor, transparentColor, alpha, x + edgeSize, y, 0f, 0f, (panelW - edgeSize * 2f) / 
        mSprite.getWidth(), panelH / 
        mSprite.getHeight());
        if (useOverlay) {
            transparentColor = new Color(mBottomOverlayColor.getRed(), mBottomOverlayColor.getGreen(), mBottomOverlayColor.getBlue(), 0);
            drawTexturedQuad(mBottomColor, mBottomColor, transparentColor, transparentColor, alpha, x + edgeSize, y, 0f, 0f, (panelW - edgeSize * 2f) / 
            mSprite.getWidth(), panelH / 
            mSprite.getHeight());
        }
    }

    /**
     * Convenience method to call the full drawTexturedQuad with UV calculation.
     */
    public void renderTexturedQuad(Color topLeft, Color bottomLeft, Color bottomRight,
        Color topRight, float alpha, float x, float y, float width, float height
    ) {
        float uStart = 0f;
        float vStart = 0f;
        float uEnd = width / mSprite.getWidth();
        float vEnd = height / mSprite.getHeight();
        drawTexturedQuad(topLeft, bottomLeft, bottomRight, topRight, alpha, x, y,
            uStart, vStart, uEnd, vEnd);
    }

    private void drawTexturedQuad(Color topLeft, Color bottomLeft, Color bottomRight, Color topRight,
        float alpha, float x, float y, float uStart, float vStart, float width, float height
    ) {
        mSprite.bindTexture();
        final float texWidth = mSprite.getTexWidth();
        final float texHeight = mSprite.getTexHeight();
        final float spriteWidth = mSprite.getWidth();
        final float spriteHeight = mSprite.getHeight();

        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0f);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);

        if (useAdditiveBlend) {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        } else {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }

        final float epsilon = 0.001f;
        GL11.glBegin(GL11.GL_QUADS);

        GL11.glTexCoord2f(uStart * texWidth + epsilon, vStart * texHeight + epsilon);
        RenderUtils.setGlColor(topRight, alpha);
        GL11.glVertex2f(uStart * spriteWidth, vStart * spriteHeight);

        GL11.glTexCoord2f(uStart * texWidth + epsilon, (vStart + height) * texHeight - epsilon);
        RenderUtils.setGlColor(topLeft, alpha);
        GL11.glVertex2f(uStart * spriteWidth, (vStart + height) * spriteHeight);

        GL11.glTexCoord2f((uStart + width) * texWidth - epsilon, (vStart + height) * texHeight - epsilon);
        RenderUtils.setGlColor(bottomLeft, alpha);
        GL11.glVertex2f((uStart + width) * spriteWidth, (vStart + height) * spriteHeight);

        GL11.glTexCoord2f((uStart + width) * texWidth - epsilon, vStart * texHeight + epsilon);
        RenderUtils.setGlColor(bottomRight, alpha);
        GL11.glVertex2f((uStart + width) * spriteWidth, vStart * spriteHeight);

        GL11.glEnd();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    private void renderQuadWithColor(Color color, float alpha, float x0, float y0, float x1,
        float y1, float x2, float y2, float x3, float y3
    ) {
        mSprite.bindTexture();

        final float spriteWidth = mSprite.getWidth();
        final float spriteHeight = mSprite.getHeight();

        final float uOffset = (x3 - x0 + (x2 - x1)) / spriteWidth / 2f;
        final float vOffset = (y1 - y0 + (y2 - y3)) / spriteHeight / 2f;

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);

        if (useAdditiveBlend) {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        } else {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }

        RenderUtils.setGlColor(color, alpha);

        final float epsilon = 0.001f;

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glTexCoord2f(uOffset / 2f, vOffset / 2f);
        GL11.glVertex2f((x0 + x1 + x2 + x3) / 4f, (y0 + y1 + y2 + y3) / 4f); // center vertex

        GL11.glTexCoord2f(0f + epsilon, 0f + epsilon);
        GL11.glVertex2f(x0, y0);

        GL11.glTexCoord2f(0f + epsilon, vOffset - epsilon);
        GL11.glVertex2f(x1, y1);

        GL11.glTexCoord2f(uOffset - epsilon, vOffset - epsilon);
        GL11.glVertex2f(x2, y2);

        GL11.glTexCoord2f(uOffset - epsilon, 0f + epsilon);
        GL11.glVertex2f(x3, y3);

        GL11.glTexCoord2f(0f + epsilon, 0f + epsilon);
        GL11.glVertex2f(x0, y0);
        GL11.glEnd();

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }
 
    public void setColors(Color top, Color bottom) {
        mTopColor = top;
        mBottomColor = bottom;
    }

    public void setOverlayColors(Color top, Color bottom) {
        mTopOverlayColor = top;
        mBottomOverlayColor = bottom;
        useOverlay = true;
    }
}