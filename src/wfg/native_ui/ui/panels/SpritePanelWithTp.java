package wfg.native_ui.ui.panels;

import java.awt.Color;

import com.fs.starfarer.api.ui.UIPanelAPI;

import wfg.native_ui.ui.components.AudioFeedbackComp;
import wfg.native_ui.ui.components.HoverGlowComp;
import wfg.native_ui.ui.components.NativeComponents;
import wfg.native_ui.ui.components.TooltipComp;
import wfg.native_ui.ui.panels.CustomPanel.HasAudioFeedback;
import wfg.native_ui.ui.panels.CustomPanel.HasHoverGlow;
import wfg.native_ui.ui.panels.CustomPanel.HasTooltip;

/**
 * A sprite panel with tooltip, hover glow, and audio feedback support.
 *
 * <p>Usage example:
 * <pre>{@code
 * SpritePanelWithTp panel = new SpritePanelWithTp(parent, 64, 64, "iconPath", null, null);
 * panel.tooltip.builder = (tooltip, expanded) -> {
 *     tooltip.addPara("...", pad);
 * };
 * panel.tooltip.positioner = (tooltip, expanded) -> {
 *     // default if not overridden is NativeUiUtils.mouseCornerPos(tooltip)
 *     NativeUiUtils.anchorPanel(tooltip, anchor, AnchorType.LeftTop, pad);
 * };
 * }</pre>
 */
public class SpritePanelWithTp extends SpritePanel<SpritePanelWithTp>
    implements HasTooltip, HasHoverGlow, HasAudioFeedback
{
    public final TooltipComp tooltip = comp().get(NativeComponents.TOOLTIP);
    public final HoverGlowComp glow = comp().get(NativeComponents.HOVER_GLOW);
    public final AudioFeedbackComp audio = comp().get(NativeComponents.AUDIO_FEEDBACK);

    public SpritePanelWithTp(UIPanelAPI parent, int width, int height, String spriteID,
        Color color, Color fillColor
    ) {
        super(parent, width, height, spriteID, color, fillColor);

        glow.additiveSprite = m_sprite;
    }
}