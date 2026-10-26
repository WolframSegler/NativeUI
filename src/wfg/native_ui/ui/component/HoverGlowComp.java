package wfg.native_ui.ui.component;

import java.awt.Color;

import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.FaderUtil.State;

public final class HoverGlowComp extends BaseComponent {

    /**
     * Indicates whether this panel controls its own {@link #fader} instance.
     * <p>
     * Some panels may instead synchronize their fading behavior with another panel's fader.
     * In such cases, this should be {@code false}.
     */
    public boolean isFaderOwner = true;

    public FaderUtil fader = new FaderUtil(0f, 0.1f, 0.2f, false, true);
    /** Determines draw call and base brightness */
    public GlowType type = GlowType.OVERLAY;
    /** Forces fader state to {@link State#IN} */
    public boolean persistent = false;
    /** Used when element is hovered over */
    public float glowBrightness = 0.6f;
    /** Used when element is LMB pressed */
    public float flashBrightness = 0.9f;
    /** If > 0f, overrides the brightness directly and bypasses the fader and flash logic. */
    public float overrideBrightness = -1f;
    public Color color = Color.WHITE;
    /** The sprite is binded if glow type is {@link GlowType#ADDITIVE} */
    public SpriteAPI additiveSprite = null;
    public final LayoutOffset offset = new LayoutOffset();

    /**
     * The polygon vertices of the glow shape in CCW order, {@code null} defaults to a rectangle.
     */
    public float[] faderMaskVertices = null;

    /**
     * <ul>
     *     <li>{@link GlowType#OVERLAY} or {@link GlowType#UNDERLAY} supports polygon-shaped glow.</li>
     *     <li>{@link GlowType#ADDITIVE} works with convex/polygon shapes too; a sprite texture is optional.
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