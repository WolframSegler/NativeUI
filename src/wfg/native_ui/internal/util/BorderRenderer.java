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

    private final SpriteAPI mBottomLeft;
    private final SpriteAPI mBottomRight;
    private final SpriteAPI mBottomMid;
    private final SpriteAPI mCenter;
    private final SpriteAPI mLeftMid;
    private final SpriteAPI mRightMid;
    private final SpriteAPI mTopLeft;
    private final SpriteAPI mTopRight;
    private final SpriteAPI mTopMid;
    private final float mCornerSize;
    private final float mFudge;
    private int mHiddenSidesMask = 0;
    private float mWidth;
    private float mHeight;

    public BorderRenderer(String prefix, boolean whiteCenter, float w, float h) {
        this(prefix, whiteCenter);
        this.setSize(w, h);
    }

    // TODO fix UIConstants#UI_BORDER_2 rendering bug
    public BorderRenderer(String prefix, boolean whiteCenter) {
        mBottomLeft = settings.getSprite("ui", prefix + "_bot_left");
        mBottomRight = settings.getSprite("ui", prefix + "_bot_right");
        mBottomMid = settings.getSprite("ui", prefix + "_bot");
        mCenter = settings.getSprite("ui", whiteCenter ? "center_white" : "panel00_center");
        mLeftMid = settings.getSprite("ui", prefix + "_left");
        mRightMid = settings.getSprite("ui", prefix + "_right");
        mTopLeft = settings.getSprite("ui", prefix + "_top_left");
        mTopRight = settings.getSprite("ui", prefix + "_top_right");
        mTopMid = settings.getSprite("ui", prefix + "_top");
        mCornerSize = mBottomLeft.getWidth();

        mCenter.setSize(mCornerSize, mCornerSize);

        mFudge = switch (prefix) {
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
        this.mWidth = width;
        this.mHeight = height;
    }

    public final void hideSide(Side side) {
        mHiddenSidesMask |= bitForSide(side);
    }

    public final void showSide(Side side) {
        mHiddenSidesMask &= ~bitForSide(side);
    }

    public final void clearSides() {
        mHiddenSidesMask = 0;
    }

    public final void render(float x, float y, float alpha) {
        final boolean hideTop = (mHiddenSidesMask & HIDE_TOP) != 0;
        final boolean hideLeft = (mHiddenSidesMask & HIDE_LEFT) != 0;
        final boolean hideRight = (mHiddenSidesMask & HIDE_RIGHT) != 0;
        final boolean hideBottom = (mHiddenSidesMask & HIDE_BOTTOM) != 0;

        final float leftOffset = (compensateForHiddenSides && hideLeft) ? -mCornerSize : 0f;
        final float bottomOffset = (compensateForHiddenSides && hideBottom) ? -mCornerSize : 0f;
        final float rightOffset = (compensateForHiddenSides && hideRight) ? mCornerSize : 0f;
        final float topOffset = (compensateForHiddenSides && hideTop) ? mCornerSize : 0f;

        final float innerX = x + mCornerSize + leftOffset;
        final float innerY = y + mCornerSize + bottomOffset;
        final float innerW = mWidth - 2f * mCornerSize - leftOffset + rightOffset;
        final float innerH = mHeight - 2f * mCornerSize - bottomOffset + topOffset;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        if (renderCenter) {
            final float cx = innerX - mFudge;
            final float cy = innerY - mFudge;
            final float cw = innerW + 2f * mFudge;
            final float ch = innerH + 2f * mFudge;
            RenderUtils.setGlColor(centerColor, alpha);
            mCenter.bindTexture();
            NineSliceUtils.drawStretch(false, false, mCornerSize, mCornerSize, cx, cy, cw, ch);
        }

        RenderUtils.setGlColor(Color.white, alpha);
        if (!hideBottom && !hideLeft) {
            mBottomLeft.bindTexture();
            NineSliceUtils.drawCorner(false, false, x, y, mCornerSize, mCornerSize);
        }
        if (!hideBottom && !hideRight) {
            mBottomRight.bindTexture();
            NineSliceUtils.drawCorner(false, false, x + mWidth - mCornerSize, y, mCornerSize, mCornerSize);
        }
        if (!hideTop && !hideLeft) {
            mTopLeft.bindTexture();
            NineSliceUtils.drawCorner(false, false, x, y + mHeight - mCornerSize, mCornerSize, mCornerSize);
        }
        if (!hideTop && !hideRight) {
            mTopRight.bindTexture();
            NineSliceUtils.drawCorner(false, false, x + mWidth - mCornerSize, y + mHeight - mCornerSize, mCornerSize, mCornerSize);
        }

        if (!hideLeft) {
            mLeftMid.bindTexture();
            NineSliceUtils.drawStretch(false, false, 1f, 1f, x, innerY, mCornerSize, innerH);
        }
        if (!hideRight) {
            mRightMid.bindTexture();
            NineSliceUtils.drawStretch(false, false, 1f, 1f, x + mWidth - mCornerSize, innerY, mCornerSize, innerH);
        }
        if (!hideTop) {
            mTopMid.bindTexture();
            NineSliceUtils.drawStretch(false, false, 1f, 1f, innerX, y + mHeight - mCornerSize, innerW, mCornerSize);
        }
        if (!hideBottom) {
            mBottomMid.bindTexture();
            NineSliceUtils.drawStretch(false, false, 1f, 1f, innerX, y, innerW, mCornerSize);
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