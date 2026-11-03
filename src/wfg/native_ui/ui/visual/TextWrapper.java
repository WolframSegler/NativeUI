package wfg.native_ui.ui.visual;

import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.LabelAPI;

import wfg.native_ui.example.visual.TextWrapperExample;
import wfg.native_ui.internal.ui.core.UIContainer;
import wfg.native_ui.ui.component.AudioFeedbackComp;
import wfg.native_ui.ui.component.NativeComponents;
import wfg.native_ui.ui.component.TooltipComp;
import wfg.native_ui.ui.core.UIBuildableAPI;
import wfg.native_ui.ui.core.UIElementFlags.HasAudioFeedback;
import wfg.native_ui.ui.core.UIElementFlags.HasTooltip;

/**
 * A text wrapper with tooltip and audio feedback.
 *
 * <p><strong>Example: </strong> {@link TextWrapperExample}</p>
 */
public abstract class TextWrapper extends UIContainer implements
    HasTooltip, HasAudioFeedback, UIBuildableAPI
{
    public final TooltipComp tooltip = comp().get(NativeComponents.TOOLTIP);
    public final AudioFeedbackComp audio = comp().get(NativeComponents.AUDIO_FEEDBACK);

    // Shared state for anonymous subclasses to modify.
    public ButtonAPI mCheckbox;
    public float textX1, textX2 = 0;
    public float textY1, textY2 = 0;
    public float textW1, textH1 = 0;
    public LabelAPI label1, label2 = null;

    public TextWrapper(int width, int height) {
        super(width, height);

        buildUI();
    }
}