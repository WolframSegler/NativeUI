package wfg.native_ui.internal.util;

import static wfg.native_ui.util.Globals.settings;

import java.awt.Color;
import com.fs.starfarer.api.graphics.SpriteAPI;

import wfg.native_ui.internal.ui.Side;
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
public class BorderRenderer {
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
    private float prevAlpha = 0f;

    public BorderRenderer(String prefix, boolean whiteCenter, float w, float h) {
        this(prefix, whiteCenter);
        this.setSize(w, h);
    }

    // TODO fix border2 rendering bug
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
        if (prevAlpha != alpha)  {
            prevAlpha = alpha;

            bottom_left.setAlphaMult(alpha);
            bottom_right.setAlphaMult(alpha);
            top_left.setAlphaMult(alpha);
            top_right.setAlphaMult(alpha);
            left_mid.setAlphaMult(alpha);
            right_mid.setAlphaMult(alpha);
            top_mid.setAlphaMult(alpha);
            bottom_mid.setAlphaMult(alpha);
            center.setAlphaMult(alpha);
        }
        
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

        if (renderCenter) {
            center.setColor(centerColor);
            center.renderRegion(innerX - fudge, innerY - fudge, 0f, 0f, (innerW+fudge*2) / corner_size, (innerH+fudge*2) / corner_size);
        }

        if (!hideBottom && !hideLeft) bottom_left.render(x, y);
        if (!hideBottom && !hideRight) bottom_right.render(x + width - corner_size, y);
        if (!hideTop && !hideLeft) top_left.render(x, y + height - corner_size);
        if (!hideTop && !hideRight) top_right.render(x + width - corner_size, y + height - corner_size);

        final float tileW = innerW / corner_size;
        final float tileH = innerH / corner_size;

        if (!hideLeft) left_mid.renderRegion(x, innerY, 0f, 0f, 1f, tileH);
        if (!hideRight) right_mid.renderRegion(x + width - corner_size, innerY, 0f, 0f, 1f, tileH);
        if (!hideTop) top_mid.renderRegion(innerX, y + height - corner_size, 0f, 0f, tileW, 1f);
        if (!hideBottom) bottom_mid.renderRegion(innerX, y, 0f, 0f, tileW, 1f);
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