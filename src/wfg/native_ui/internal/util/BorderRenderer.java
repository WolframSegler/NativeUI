package wfg.native_ui.internal.util;

import java.util.Arrays;
import java.util.EnumSet;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;

public class BorderRenderer {
    public enum BorderSide {
        LEFT, RIGHT, TOP, BOTTOM
    }

    public boolean renderCenter = true;
    public boolean compensateForHiddenSides = true;
    public final EnumSet<BorderSide> hiddenSides = EnumSet.noneOf(BorderSide.class);

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
    public BorderRenderer(String prefix, float w, float h, BorderSide... hidden) {
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

        final boolean hideLeft = hiddenSides.contains(BorderSide.LEFT);
        final boolean hideRight = hiddenSides.contains(BorderSide.RIGHT);
        final boolean hideTop = hiddenSides.contains(BorderSide.TOP);
        final boolean hideBottom = hiddenSides.contains(BorderSide.BOTTOM);
        
        if (!hideBottom && !hideLeft) bottom_left.render(x, y);
        if (!hideBottom && !hideRight) bottom_right.render(x + width - corner_width, y);
        if (!hideTop && !hideLeft) top_left.render(x, y + height - corner_width);
        if (!hideTop && !hideRight) top_right.render(x + width - corner_width, y + height - corner_width);

        if (!hideLeft) left_mid.renderRegion(x, y + corner_width, 0f, 0f, 1f, tilesHigh);
        if (!hideRight) right_mid.renderRegion(x + width - corner_width, y + corner_width, 0f, 0f, 1f, tilesHigh);

        if (!hideTop) top_mid.renderRegion(x + corner_width, y + height - corner_width, 0f, 0f, tilesWide, 1f);

        if (!hideBottom) bottom_mid.renderRegion(x + corner_width, y, 0f, 0f, tilesWide, 1f);

        if (renderCenter) {
            center.setAlphaMult(alpha);

            float cx = x + corner_width;
            float cy = y + corner_width;
            float cw = tilesWide;
            float ch = tilesHigh;

            if (compensateForHiddenSides) {
                if (hiddenSides.contains(BorderSide.LEFT))   { cx -= corner_width; cw += 1f; }
                if (hiddenSides.contains(BorderSide.RIGHT))  { cw += 1f; }
                if (hiddenSides.contains(BorderSide.BOTTOM)) { cy -= corner_width; ch += 1f; }
                if (hiddenSides.contains(BorderSide.TOP))    { ch += 1f; }
            }

            center.renderRegion(cx, cy, 0f, 0f, cw, ch);
        }
    }
}