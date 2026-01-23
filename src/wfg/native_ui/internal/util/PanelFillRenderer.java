package wfg.native_ui.internal.util;

import org.lwjgl.opengl.GL11;

import com.fs.starfarer.api.graphics.SpriteAPI;

import wfg.native_ui.util.RenderUtils;

import java.awt.Color;

public class PanelFillRenderer {
    public boolean useOverlay = false;
    public boolean useAdditiveBlend = true;
    public boolean useGradient = true;
    public float panelW = 0, panelH = 0;
    public float edgeSize = 0f;

    private SpriteAPI m_sprite;
    private Color topColor = new Color(30, 114, 132);
    private Color bottomColor = new Color(10, 38, 44);
    private Color topOverlayColor = new Color(30, 114, 132, 155);
    private Color bottomOverlayColor = new Color(10, 38, 44, 155);

    public PanelFillRenderer(SpriteAPI sprite) {
        this(sprite, sprite.getWidth(), sprite.getHeight());
    }

    public PanelFillRenderer(SpriteAPI sprite, float width, float height) {
        m_sprite = sprite;
        panelW = width;
        panelH = height;
    }

    public void setSize(float width, float height) {
        panelW = width;
        panelH = height;
    }

    public void advance(float delta) {}

    public void renderVerticalGradient(float x, float y, float alpha) {
        renderGradient(topColor, bottomColor, x, y, alpha, 0.0F);
        if (useOverlay) {
            renderGradient(topOverlayColor, bottomOverlayColor, x, y + 1.0F, alpha, -1.0F);
        }
    }

    /**
     * Helper method that renders a two-color gradient with optional top/bottom caps.
     */
    private void renderGradient(Color topColor, Color bottomColor, float x, float y, float alpha,
        float yOffsset
    ) {
        if (useGradient) {
            if (edgeSize > 0.0F) {
                drawTexturedQuad(topColor, bottomColor, topColor, bottomColor, alpha, x, y, 0.0F, 0.0F, edgeSize / m_sprite.getWidth(), (panelH + yOffsset) / m_sprite.getHeight());
                drawTexturedQuad(bottomColor, topColor, bottomColor, topColor, alpha, x + panelW - edgeSize, y, 0.0F, 0.0F, edgeSize / m_sprite.getWidth(), (panelH + yOffsset) / m_sprite.getHeight());
            }

            drawTexturedQuad(bottomColor, bottomColor, bottomColor, bottomColor, alpha, x + edgeSize, y, 0.0F, 0.0F, (panelW - edgeSize * 2.0F) / m_sprite.getWidth(), (panelH + yOffsset) / m_sprite.getHeight());
        }
    }

    /**
     * Renders a simple quad with a single color (or gradient).
     */
    public void renderQuad(float x1, float y1, float x2, float y2, float x3, float y3, float x4, 
        float y4, float alpha) {
        renderQuadWithColor(bottomColor, alpha, x1, y1, x2, y2, x3, y3, x4, y4);
        if (useOverlay) {
            renderQuadWithColor(bottomOverlayColor, alpha, x1, y1 + 1.0F, x2, y2 + 1.0F, x3, y3 + 1.0F, x4, y4 + 1.0F);
        }
    }

    /**
     * Renders a textured quad with a subtle top-to-bottom gradient overlay.
     */
    public void renderTexturedQuad(float x, float y, float alpha) {
        Color transparentColor = new Color(bottomColor.getRed(), bottomColor.getGreen(), bottomColor.getBlue(), 0);

        drawTexturedQuad(bottomColor, bottomColor, transparentColor, transparentColor, alpha, x + edgeSize, y, 0f, 0f, (panelW - edgeSize * 2f) / 
        m_sprite.getWidth(), panelH / 
        m_sprite.getHeight());
        if (useOverlay) {
            transparentColor = new Color(bottomOverlayColor.getRed(), bottomOverlayColor.getGreen(), bottomOverlayColor.getBlue(), 0);
            drawTexturedQuad(bottomColor, bottomColor, transparentColor, transparentColor, alpha, x + edgeSize, y, 0f, 0f, (panelW - edgeSize * 2f) / 
            m_sprite.getWidth(), panelH / 
            m_sprite.getHeight());
        }
    }

    /**
     * Convenience method to call the full drawTexturedQuad with UV calculation.
     */
    public void renderTexturedQuad(Color topLeft, Color bottomLeft, Color bottomRight,
        Color topRight, float alpha, float x, float y, float width, float height
    ) {
        float uStart = 0.0F;
        float vStart = 0.0F;
        float uEnd = width / m_sprite.getWidth();
        float vEnd = height / m_sprite.getHeight();
        drawTexturedQuad(topLeft, bottomLeft, bottomRight, topRight, alpha, x, y,
            uStart, vStart, uEnd, vEnd);
    }

    private void drawTexturedQuad(Color topLeft, Color bottomLeft, Color bottomRight, Color topRight,
        float alpha, float x, float y, float uStart, float vStart, float width, float height
    ) {
        m_sprite.bindTexture();
        final float texWidth = m_sprite.getTexWidth();
        final float texHeight = m_sprite.getTexHeight();
        final float spriteWidth = m_sprite.getWidth();
        final float spriteHeight = m_sprite.getHeight();

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
        m_sprite.bindTexture();

        final float spriteWidth = m_sprite.getWidth();
        final float spriteHeight = m_sprite.getHeight();

        final float uOffset = (x3 - x0 + (x2 - x1)) / spriteWidth / 2.0F;
        final float vOffset = (y1 - y0 + (y2 - y3)) / spriteHeight / 2.0F;

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
        topColor = top;
        bottomColor = bottom;
    }

    public void setOverlayColors(Color top, Color bottom) {
        topOverlayColor = top;
        bottomOverlayColor = bottom;
        useOverlay = true;
    }
}