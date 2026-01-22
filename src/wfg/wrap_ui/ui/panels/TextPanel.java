package wfg.wrap_ui.ui.panels;

import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import wfg.wrap_ui.ui.components.AudioFeedbackComp;
import wfg.wrap_ui.ui.components.NativeComponents;
import wfg.wrap_ui.ui.components.TooltipComp;
import wfg.wrap_ui.ui.components.UIContextComp;
import wfg.wrap_ui.ui.panels.CustomPanel.HasAudioFeedback;
import wfg.wrap_ui.ui.panels.CustomPanel.HasTooltip;
import wfg.wrap_ui.ui.panels.CustomPanel.HasUIContext;

/**
 * A text-based UI panel with tooltip, audio feedback, and UI context support.
 *
 * <p>This panel is intended to be subclassed anonymously for ad-hoc UI creation. 
 * Subclasses override {@link #createPanel()} to define UI elements and layout. 
 * Internal fields (checkbox, labels, text positions) are exposed publicly so that 
 * external code can read panel state from the anonymous subclass.</p>
 *
 * <p>Usage example:
 * <pre>{@code
 * TextPanel panel = new TextPanel(parent, 300, 50) {
 *     @Override
 *     public void createPanel() {
 *         m_checkbox = addCheckbox("Enable", 10, 10);
 *         label1 = addLabel("Hello", 20, 10);
 *         textX1 = 0; textY1 = 0; textW1 = 100; textH1 = 20;
 *     }
 * };
 *
 * // External code can now inspect the fields:
 * if (panel.m_checkbox.isChecked()) { ... }
 *
 * // Tooltip setup:
 * panel.tooltip.builder = (tooltip, expanded) -> tooltip.addPara("Example text", 3f);
 * panel.tooltip.positioner = (tooltip, expanded) -> WrapUiUtils.anchorPanel(
 *     tooltip, anchorPanel, AnchorType.LeftTop, pad
 * );
 * }</pre>
 */
public class TextPanel extends CustomPanel<TextPanel> implements
    HasTooltip, HasAudioFeedback, HasUIContext
{
    public final TooltipComp tooltip = comp().get(NativeComponents.TOOLTIP);
    public final UIContextComp context = comp().get(NativeComponents.UI_CONTEXT);
    public final AudioFeedbackComp audio = comp().get(NativeComponents.AUDIO_FEEDBACK);

    // Shared state for anonymous subclasses to modify.
    public ButtonAPI m_checkbox;
    public float textX1, textX2 = 0;
    public float textY1, textY2 = 0;
    public float textW1, textH1 = 0;
    public LabelAPI label1, label2 = null;

    public TextPanel(UIPanelAPI parent, int width, int height) {
        super(parent, width, height);

        context.ignore = true;
        createPanel();
    }
    public void createPanel() {}
}