package wfg.native_ui.ui.component;

import java.awt.Color;

import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;

public final class HoverGlowComp extends BaseComponent {

    /**
     * Indicates whether this panel controls its own {@link HoverGlowComp#fader} instance.
     * <p>
     * Some panels may instead synchronize their fading behavior with another panel's fader.
     * In such cases, this should return {@code false}.
     */
    public boolean isFaderOwner = true;

    public FaderUtil fader = new FaderUtil(0, 0.1f, 0.2f, false, true);
    public GlowType type = GlowType.OVERLAY;
    public boolean persistent = false;
    public float overlayBrightness = 0.25f;
    public float additiveBrightness = 0.6f;
    public Color color = Color.WHITE;
    public SpriteAPI additiveSprite = null;
    public final LayoutOffset offset = new LayoutOffset();

    /**
     * The polygon vertices of the background shape in CCW order.
     * Value of null defaults to a rectangle.
     */
    public float[] faderMaskVertices = null;

    /**
     * <ul>
     *     <li>{@link Glow#OVERLAY} or {@link Glow#UNDERLAY}: supports polygon-shaped glow.</li>
     *     <li>{@link Glow#ADDITIVE}: works with convex/polygon shapes too; a sprite texture is optional.
     *         If a sprite is provided via {@link #additiveSprite}, it will be used for rendering,
     *         otherwise a colored quad will be drawn.</li>
     * </ul>
     */
    public enum GlowType {
        OVERLAY,
        UNDERLAY,
        ADDITIVE
    }
}