package wfg.native_ui.ui.component;

import java.awt.Color;

import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;

public final class GlowComp extends BaseComponent {

    /**
     * Indicates whether this entity controls its own faders.
     * <p>
     * Some entities may instead synchronize their fading behavior with another entity's faders.
     * In such cases, this should be {@code false}.
     */
    public boolean isSelfFaderOwner = true;

    /** Used when entity is hovered over. */
    public float glowBrightness = 0.6f;
    /** Used when entity is selected. */
    public float highlightBrightness = 0.4f;
    /** Used when entity is LMB pressed. */
    public float flashBrightness = 0.24f;

    /** If > 0f, overrides the brightness directly and bypasses the fader and flash logic. */
    public float overrideBrightness = -1f;

    public FaderUtil glowFader = new FaderUtil(0f, 0.1f, 0.2f, false, true);
    public FaderUtil highlightFader = new FaderUtil(0f, 0.1f, 0.2f, false, false);
    public FaderUtil flashFader = new FaderUtil(0f, 0.05f, 0.15f, false, true);

    /** Determines draw call and base brightness */
    public GlowType type = GlowType.OVERLAY;
    /** Used by the draw calls. */
    public Color color = Color.WHITE;

    /**
     * The polygon vertices of the glow shape in CCW order, {@code null} defaults to a rectangle.
     */
    public float[] faderMaskVertices = null;
    /** The sprite is binded if {@link #type} is {@link GlowType#ADDITIVE} */
    public SpriteAPI additiveSprite = null;
    public final LayoutOffset offset = new LayoutOffset();

    public final boolean isHighlighted() {
        return highlightFader.isFadedIn() || highlightFader.isFadingIn();
    }

    public final boolean isFlashing() {
        return flashFader.isFadedIn() && flashFader.isFadingIn();
    }

    public final void highlight() {
        highlightFader.fadeIn();
    }

    public final void unhighlight() {
        highlightFader.fadeOut();
    }

    public final void setHighlighted(boolean highlighted) {
        if (highlighted) {
            highlight();
        } else {
            unhighlight();
        }
    }

    public final void flash() {
        flashFader.fadeIn();
    }

    public final void setOwner(final GlowComp owner) {
        final boolean isSelfOwner = owner == null;
        if (isSelfOwner && isSelfFaderOwner) return;

        isSelfFaderOwner = isSelfOwner;
        glowFader = isSelfOwner ? new FaderUtil(0f, 0.1f, 0.2f, false, true) : owner.glowFader;
        highlightFader = isSelfOwner ? new FaderUtil(0f, 0.1f, 0.2f, false, false) : owner.highlightFader;
        flashFader = isSelfOwner ? new FaderUtil(0f, 0.05f, 0.15f, false, true) : owner.flashFader;
    }
    
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