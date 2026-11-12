package wfg.native_ui.ui.visual;

import static wfg.native_ui.util.Globals.settings;

import java.awt.Color;

import com.fs.starfarer.api.graphics.SpriteAPI;

import wfg.native_ui.ui.component.AudioFeedbackComp;
import wfg.native_ui.ui.component.GlowComp;
import wfg.native_ui.ui.component.NativeComponents;
import wfg.native_ui.ui.component.TooltipComp;
import wfg.native_ui.ui.core.UIElementFlags.HasAudioFeedback;
import wfg.native_ui.ui.core.UIElementFlags.HasGlow;
import wfg.native_ui.ui.core.UIElementFlags.HasTooltip;

/**
 * An {@link AbstractSpriteElement} with tooltip, glow, and audio feedback support.
 */
public class InteractiveSprite extends AbstractSpriteElement<InteractiveSprite>
    implements HasTooltip, HasGlow, HasAudioFeedback
{
    public final TooltipComp tooltip = comp().get(NativeComponents.TOOLTIP);
    public final GlowComp glow = comp().get(NativeComponents.GLOW);
    public final AudioFeedbackComp audio = comp().get(NativeComponents.AUDIO_FEEDBACK);

    public InteractiveSprite(float width, float height, String spriteID,
        Color color, Color fillColor
    ) {
        this(width, height, settings.getSprite(spriteID), color, fillColor);
    }

    public InteractiveSprite(float width, float height, SpriteAPI sprite,
        Color color, Color fillColor
    ) {
        super(width, height, sprite, color, fillColor);

        glow.flashBrightness = 0.3f;
        glow.additiveSprite = mSprite;
    }
}