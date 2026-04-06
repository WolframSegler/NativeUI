package wfg.native_ui.ui.component;

import wfg.native_ui.ui.system.AudioFeedbackSystem;

public final class AudioFeedbackComp extends BaseComponent {
    public boolean useDisabledSound = false;
    public String buttonPressedSound = "ui_button_pressed";
    public String mouseOverSound = "ui_button_mouseover";
    public String buttonPressedDisabledSound = "ui_button_disabled_pressed";

    /** Internal: only used by {@link AudioFeedbackSystem} */
    public int accumulatedGameTicks = 0;
}