package wfg.native_ui.internal.util;

import java.util.Arrays;
import java.util.EnumSet;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;

import wfg.native_ui.internal.ui.Side;

public class BorderRenderer {
    public boolean renderCenter = true;
    public boolean compensateForHiddenSides = true;
    public final EnumSet<Side> hiddenSides = EnumSet.noneOf(Side.class);

    private final SpriteAPI bottom_left;
    private final SpriteAPI bottom_right;
    private final SpriteAPI bottom_mid;
    private final SpriteAPI center;
    private final SpriteAPI left_mid;
    private final SpriteAPI right_mid;
    private final SpriteAPI top_left;
    private final SpriteAPI top_right;
    private final SpriteAPI top_mid;
    private final float corner_width;
    private float width;
    private float height;
    private float tilesWide;
    private float tilesHigh;

    public BorderRenderer(String prefix, float w, float h) {
        this(prefix);
        this.setSize(w, h);
    }

    /**
     * The texture size should match the actual size of the sprites.
     * <pre>
     * Available prefixes:
     * "ui_border1"
     * "ui_border2"
     * "ui_border3"
     * "ui_border4"
     * </pre>
     */
    public BorderRenderer(String prefix) {
        final SettingsAPI settings = Global.getSettings();

        bottom_left = settings.getSprite("ui", prefix + "_bot_left");
        bottom_right = settings.getSprite("ui", prefix + "_bot_right");
        bottom_mid = settings.getSprite("ui", prefix + "_bot");
        center = settings.getSprite("ui", "panel00_center");
        left_mid = settings.getSprite("ui", prefix + "_left");
        right_mid = settings.getSprite("ui", prefix + "_right");
        top_left = settings.getSprite("ui", prefix + "_top_left");
        top_right = settings.getSprite("ui", prefix + "_top_right");
        top_mid = settings.getSprite("ui", prefix + "_top");
        corner_width = bottom_left.getWidth();

        center.setSize(corner_width, corner_width);
    }

    /**
     * The texture size should match the actual size of the sprites.
     * <pre>
     * Available prefixes:
     * "ui_border1"
     * "ui_border2"
     * "ui_border3"
     * "ui_border4"
     * </pre>
     */
    public BorderRenderer(String prefix, float w, float h, Side... hidden) {
        this(prefix);
        this.setSize(w, h);
        if (hidden != null) {
            hiddenSides.addAll(Arrays.asList(hidden));
        }
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
        this.tilesWide = (width - this.corner_width * 2.0F) / this.corner_width;
        this.tilesHigh = (height - this.corner_width * 2.0F) / this.corner_width;
    }

    public void render(float x, float y, float alpha) {
        {
        bottom_left.setAlphaMult(alpha);
        bottom_right.setAlphaMult(alpha);
        top_left.setAlphaMult(alpha);
        top_right.setAlphaMult(alpha);
        left_mid.setAlphaMult(alpha);
        right_mid.setAlphaMult(alpha);
        top_mid.setAlphaMult(alpha);
        bottom_mid.setAlphaMult(alpha);
        }

        final boolean hideLeft = hiddenSides.contains(Side.LEFT);
        final boolean hideRight = hiddenSides.contains(Side.RIGHT);
        final boolean hideTop = hiddenSides.contains(Side.TOP);
        final boolean hideBottom = hiddenSides.contains(Side.BOTTOM);

        final float leftOffset  = (compensateForHiddenSides && hideLeft)  ? -corner_width : 0f;
        final float bottomOffset= (compensateForHiddenSides && hideBottom)? -corner_width : 0f;

        if (renderCenter) {
            center.setAlphaMult(alpha);
            final float fudge = 1f;
            final float cx = x + corner_width + leftOffset;
            final float cy = y + corner_width + bottomOffset;
            final float cw = tilesWide + ((compensateForHiddenSides) ? ((hideLeft?1f:0f) +
                (hideRight?1f:0f)) : 0f);
            final float ch = tilesHigh + ((compensateForHiddenSides) ? ((hideBottom?1f:0f) +
                (hideTop?1f:0f)) : 0f);
            center.renderRegion(cx - fudge*5, cy - fudge*5, 0f, 0f, cw + fudge, ch + fudge);
        }
        
        if (!hideBottom && !hideLeft) bottom_left.render(x, y);
        if (!hideBottom && !hideRight) bottom_right.render(x + width - corner_width, y);
        if (!hideTop && !hideLeft) top_left.render(x, y + height - corner_width);
        if (!hideTop && !hideRight) top_right.render(x + width - corner_width, y + height - corner_width);

        final float verticalY  = y + corner_width + bottomOffset;
        final float verticalH  = tilesHigh + ((compensateForHiddenSides) ? ((hideBottom?1f:0f) + (hideTop?1f:0f)) : 0f);
        final float horizontalX = x + corner_width + leftOffset;
        final float horizontalW = tilesWide + ((compensateForHiddenSides) ? ((hideLeft?1f:0f) + (hideRight?1f:0f)) : 0f);

        if (!hideLeft) left_mid.renderRegion(x, verticalY, 0f, 0f, 1f, verticalH);
        if (!hideRight) right_mid.renderRegion(x + width - corner_width, verticalY, 0f, 0f, 1f, verticalH);

        if (!hideTop) top_mid.renderRegion(horizontalX, y + height - corner_width, 0f, 0f, horizontalW, 1f);
        if (!hideBottom) bottom_mid.renderRegion(horizontalX, y, 0f, 0f, horizontalW, 1f);
    }
}