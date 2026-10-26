package wfg.native_ui.internal.util;

import static wfg.native_ui.util.Globals.settings;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import com.fs.starfarer.api.graphics.SpriteAPI;

import wfg.native_ui.internal.ui.Side;
import wfg.native_ui.util.RenderUtils;
import wfg.native_ui.util.UIConstants;

/**
 * The texture size should match the actual size of the sprites.
 * <br><br>
 * Available prefixes:
 * <ul>
 *  <li>{@link UIConstants#UI_BORDER_1}</li>
 *  <li>{@link UIConstants#UI_BORDER_2}</li>
 *  <li>{@link UIConstants#UI_BORDER_3}</li>
 *  <li>{@link UIConstants#UI_BORDER_4}</li>
 * </ul>
 */
public final class BorderRenderer {
    private static final int HIDE_LEFT = 1;   // 0001
    private static final int HIDE_RIGHT = 2;  // 0010
    private static final int HIDE_TOP = 4;    // 0100
    private static final int HIDE_BOTTOM = 8; // 1000

    public boolean renderCenter = true;
    public boolean compensateForHiddenSides = true;
    public Color centerColor = Color.white;

    private final SpriteAPI bottom_left;
    private final SpriteAPI bottom_right;
    private final SpriteAPI bottom_mid;
    private final SpriteAPI center;
    private final SpriteAPI left_mid;
    private final SpriteAPI right_mid;
    private final SpriteAPI top_left;
    private final SpriteAPI top_right;
    private final SpriteAPI top_mid;
    private final float corner_size;
    private final float fudge;
    private int hiddenSidesMask = 0;
    private float width;
    private float height;

    public BorderRenderer(String prefix, boolean whiteCenter, float w, float h) {
        this(prefix, whiteCenter);
        this.setSize(w, h);
    }

    // TODO fix UIConstants#UI_BORDER_2 rendering bug
    public BorderRenderer(String prefix, boolean whiteCenter) {
        bottom_left = settings.getSprite("ui", prefix + "_bot_left");
        bottom_right = settings.getSprite("ui", prefix + "_bot_right");
        bottom_mid = settings.getSprite("ui", prefix + "_bot");
        center = settings.getSprite("ui", whiteCenter ? "center_white" : "panel00_center");
        left_mid = settings.getSprite("ui", prefix + "_left");
        right_mid = settings.getSprite("ui", prefix + "_right");
        top_left = settings.getSprite("ui", prefix + "_top_left");
        top_right = settings.getSprite("ui", prefix + "_top_right");
        top_mid = settings.getSprite("ui", prefix + "_top");
        corner_size = bottom_left.getWidth();

        center.setSize(corner_size, corner_size);

        fudge = switch (prefix) {
            case UIConstants.UI_BORDER_1 -> 3f;
            case UIConstants.UI_BORDER_2 -> 4f;
            case UIConstants.UI_BORDER_3 -> 3f;
            case UIConstants.UI_BORDER_4 -> 2f;
            default -> 0f;
        };
    }
 
    public BorderRenderer(String prefix, boolean whiteCenter, float w, float h, Side... hidden) {
        this(prefix, whiteCenter);
        this.setSize(w, h);

        for (Side side : hidden) hideSide(side);
    }

    public final void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public final void hideSide(Side side) {
        hiddenSidesMask |= bitForSide(side);
    }

    public final void showSide(Side side) {
        hiddenSidesMask &= ~bitForSide(side);
    }

    public final void clearSides() {
        hiddenSidesMask = 0;
    }

    public final void render(float x, float y, float alpha) {
        final boolean hideTop = (hiddenSidesMask & HIDE_TOP) != 0;
        final boolean hideLeft = (hiddenSidesMask & HIDE_LEFT) != 0;
        final boolean hideRight = (hiddenSidesMask & HIDE_RIGHT) != 0;
        final boolean hideBottom = (hiddenSidesMask & HIDE_BOTTOM) != 0;

        final float leftOffset = (compensateForHiddenSides && hideLeft) ? -corner_size : 0f;
        final float bottomOffset = (compensateForHiddenSides && hideBottom) ? -corner_size : 0f;
        final float rightOffset = (compensateForHiddenSides && hideRight) ? corner_size : 0f;
        final float topOffset = (compensateForHiddenSides && hideTop) ? corner_size : 0f;

        final float innerX = x + corner_size + leftOffset;
        final float innerY = y + corner_size + bottomOffset;
        final float innerW = width - 2f * corner_size - leftOffset + rightOffset;
        final float innerH = height - 2f * corner_size - bottomOffset + topOffset;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        if (renderCenter) {
            final float cx = innerX - fudge;
            final float cy = innerY - fudge;
            final float cw = innerW + 2f * fudge;
            final float ch = innerH + 2f * fudge;
            RenderUtils.setGlColor(centerColor, alpha);
            center.bindTexture();
            NineSliceUtils.drawStretch(false, false, corner_size, corner_size, cx, cy, cw, ch);
        }

        RenderUtils.setGlColor(Color.white, alpha);
        if (!hideBottom && !hideLeft) {
            bottom_left.bindTexture();
            NineSliceUtils.drawCorner(false, false, x, y, corner_size, corner_size);
        }
        if (!hideBottom && !hideRight) {
            bottom_right.bindTexture();
            NineSliceUtils.drawCorner(false, false, x + width - corner_size, y, corner_size, corner_size);
        }
        if (!hideTop && !hideLeft) {
            top_left.bindTexture();
            NineSliceUtils.drawCorner(false, false, x, y + height - corner_size, corner_size, corner_size);
        }
        if (!hideTop && !hideRight) {
            top_right.bindTexture();
            NineSliceUtils.drawCorner(false, false, x + width - corner_size, y + height - corner_size, corner_size, corner_size);
        }

        if (!hideLeft) {
            left_mid.bindTexture();
            NineSliceUtils.drawStretch(false, false, 1f, 1f, x, innerY, corner_size, innerH);
        }
        if (!hideRight) {
            right_mid.bindTexture();
            NineSliceUtils.drawStretch(false, false, 1f, 1f, x + width - corner_size, innerY, corner_size, innerH);
        }
        if (!hideTop) {
            top_mid.bindTexture();
            NineSliceUtils.drawStretch(false, false, 1f, 1f, innerX, y + height - corner_size, innerW, corner_size);
        }
        if (!hideBottom) {
            bottom_mid.bindTexture();
            NineSliceUtils.drawStretch(false, false, 1f, 1f, innerX, y, innerW, corner_size);
        }

        GL11.glDisable(GL11.GL_BLEND);
    }

    private static final int bitForSide(Side side) {
        return switch (side) {
            case LEFT -> HIDE_LEFT;
            case RIGHT -> HIDE_RIGHT;
            case TOP -> HIDE_TOP;
            case BOTTOM -> HIDE_BOTTOM;
        };
    }
}