package wfg.native_ui.internal.util;

import static wfg.native_ui.util.Globals.settings;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.PositionAPI;

import wfg.native_ui.util.NativeUiUtils;
import wfg.native_ui.util.RenderUtils;

public final class ToastRenderer {
    private ToastRenderer() {};
    private static final SpriteAPI HANDLE_CORNER = settings.getSprite("ui", "toast_handle_bottom_left");
    private static final SpriteAPI HANDLE_LEFT = settings.getSprite("ui", "toast_handle_center_left");
    private static final SpriteAPI HOLO_CORNER = settings.getSprite("ui", "toast_holo_corner");
    private static final SpriteAPI HOLO_LEFT = settings.getSprite("ui", "toast_holo_left");
    private static final SpriteAPI HOLO_TOP = settings.getSprite("ui", "toast_holo_top");
    private static final SpriteAPI HOLO_CENTER = settings.getSprite("ui", "toast_holo_center");

    public static final float HANDLE_W = HANDLE_CORNER.getTextureWidth();

    private static final float PROGRESS_BAR_HEIGHT = 2f;
    private static final float PROGRESS_BAR_PADDING = 4f;
    private static final float HANDLE_CORNER_H = HANDLE_CORNER.getTextureHeight();
    private static final float HOLO_CORNER_S = HOLO_CORNER.getWidth();

    public static final void render(PositionAPI pos, boolean mirror, float alpha, Color handleColor, Color holoColor,
        float glowIntensity, float barProgress
    ) {
        render(pos.getX(), pos.getY(), pos.getWidth(), pos.getHeight(), mirror, alpha, handleColor, holoColor, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA,
            glowIntensity, barProgress
        );
    }

    public static final void render(float x, float y, float w, float h, boolean mirror, float alpha, Color handleColor, Color holoColor,
        int blendSrc, int blendDest, float glowIntensity, float barProgress
    ) {
        final float handleX = x + (mirror ? w - HANDLE_W : 0f);
        final float holoX = x + (mirror ? 0f : HANDLE_W);
        final float holoW = w - HANDLE_W;

        final float blY = y;
        final float tlY = y + h - HANDLE_CORNER_H;
        final float centerY = y + HANDLE_CORNER_H;
        final float handleInnerH = h - 2f * HANDLE_CORNER_H;

        final float holoInnerX = holoX + HOLO_CORNER_S;
        final float holoInnerY = y + HOLO_CORNER_S;
        final float holoInnerW = holoW - 2f * HOLO_CORNER_S;
        final float holoInnerH = h - 2f * HOLO_CORNER_S;

        final float holoRight = holoX + holoW - HOLO_CORNER_S;
        final float holoTop = y + h - HOLO_CORNER_S;

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);

        GL11.glBlendFunc(blendSrc, blendDest);
        RenderUtils.setGlColor(holoColor, alpha);
        
        drawHologram(
            holoRight, holoTop, holoInnerX, holoInnerY, holoInnerW,
            holoInnerH, holoX, holoW, y, h
        );

        if (glowIntensity > 0.04f) {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            RenderUtils.setGlColor(holoColor, alpha * glowIntensity);

            drawHologram(
                holoRight, holoTop,
                holoInnerX, holoInnerY, holoInnerW, holoInnerH,
                holoX, holoW, y, h
            );
        }

        if (barProgress > 0f) {
            drawProgressBar(holoX, y, holoW, h, alpha, holoColor, barProgress, mirror);
            GL11.glBlendFunc(blendSrc, blendDest);
        }

        RenderUtils.setGlColor(handleColor, alpha);

        HANDLE_CORNER.bindTexture();
        NineSliceUtils.drawCorner(HANDLE_CORNER, mirror, false, handleX, blY);

        HANDLE_CORNER.bindTexture();
        NineSliceUtils.drawCorner(HANDLE_CORNER, mirror, true, handleX, tlY);

        HANDLE_LEFT.bindTexture();
        NineSliceUtils.drawStretch(HANDLE_LEFT, mirror, false, handleX, centerY, HANDLE_W, handleInnerH);

        GL11.glDisable(GL11.GL_BLEND);
    }

    private static final void drawHologram(
        float holoRight, float holoTop, float holoInnerX, float holoInnerY, float holoInnerW,
        float holoInnerH, float holoX, float holoW, float y, float h
    ) {
        HOLO_CENTER.bindTexture();
        NineSliceUtils.drawStretch(false, false, HOLO_CENTER.getWidth(), HOLO_CENTER.getHeight(), holoInnerX, holoInnerY, holoInnerW, holoInnerH);

        HOLO_CORNER.bindTexture();
        NineSliceUtils.drawCorner(false, false, holoX, y, HOLO_CORNER_S, HOLO_CORNER_S);
        NineSliceUtils.drawCorner(true, false, holoRight, y, HOLO_CORNER_S, HOLO_CORNER_S);
        NineSliceUtils.drawCorner(false, true, holoX, holoTop, HOLO_CORNER_S, HOLO_CORNER_S);
        NineSliceUtils.drawCorner(true, true, holoRight, holoTop, HOLO_CORNER_S, HOLO_CORNER_S);

        HOLO_LEFT.bindTexture();
        NineSliceUtils.drawStretch(false, false, HOLO_CORNER_S, HOLO_LEFT.getHeight(), holoX, holoInnerY, HOLO_CORNER_S, holoInnerH);
        NineSliceUtils.drawStretch(true, false, HOLO_CORNER_S, HOLO_LEFT.getHeight(), holoX + holoW - HOLO_CORNER_S, holoInnerY, HOLO_CORNER_S, holoInnerH);

        HOLO_TOP.bindTexture();
        NineSliceUtils.drawStretch(false, false, HOLO_TOP.getWidth(), HOLO_CORNER_S, holoInnerX, y, holoInnerW, HOLO_CORNER_S);
        NineSliceUtils.drawStretch(false, true, HOLO_TOP.getWidth(), HOLO_CORNER_S, holoInnerX, y + h - HOLO_CORNER_S, holoInnerW, HOLO_CORNER_S);
    }

    private static final void drawProgressBar(
        float holoX, float y, float holoW, float h,
        float alpha, Color color, float progress, boolean mirror
    ) {
        final float barWidth = holoW - 2f * PROGRESS_BAR_PADDING;
        final float barX = holoX + PROGRESS_BAR_PADDING;
        final float barY = y + PROGRESS_BAR_PADDING;
        
        RenderUtils.setGlColor(Color.BLACK, alpha * 0.24f);
        RenderUtils.quadVertex(barX, barY, barWidth, PROGRESS_BAR_HEIGHT);
        
        final float progressWidth = barWidth * progress;
        final float progressX = mirror ? barX + barWidth - progressWidth : barX;
        
        final Color barColor = NativeUiUtils.adjustBrightnessWithAlpha(color, 1.3f);
        RenderUtils.setGlColor(barColor, alpha);
        RenderUtils.quadVertex(progressX, barY, progressWidth, PROGRESS_BAR_HEIGHT);
        
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        final Color glowColor = NativeUiUtils.adjustBrightnessWithAlpha(color, 2.0f);
        RenderUtils.setGlColor(glowColor, alpha * 0.3f);
        RenderUtils.quadVertex(progressX, barY - 1f, progressWidth, PROGRESS_BAR_HEIGHT + 2f);
    }
}