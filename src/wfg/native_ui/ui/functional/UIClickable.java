package wfg.native_ui.ui.functional;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.UIPanelAPI;

import wfg.native_ui.ui.component.InteractionComp;
import wfg.native_ui.ui.component.NativeComponents;
import wfg.native_ui.ui.core.UIElementFlags.HasInteraction;
import wfg.native_ui.ui.panel.CustomPanel;
import wfg.native_ui.util.CallbackRunnable;

/**
 * Base clickable component with interaction support, sound, shortcut, and checked state.
 *
 * <ul>
 *   <li>Plays mouseover/click sounds and fires an action callback when activated.</li>
 *   <li>If {@code onClicked} is null the component will toggle its internal {@code checked} state when activated;
 *   if a callback is provided the callback is invoked instead and the component will NOT toggle automatically.</li>
 *   <li>Supports keyboard shortcut (via {@link #setShortcut}).</li>
 *   <li>Use {@link #setEnabled} to enable/disable the component.</li>
 *   <li>If {@code disabledWhileInvisible} is false the component can still be activated while fully transparent;
 *   otherwise it is inert.</li>
 * </ul>
 *
 * <p>Interaction details & caveats:
 * <ul>
 *   <li>Click handling is routed through the internal {@code InteractionComp}. Calling {@link #click(boolean)} with
 *   {@code ignoreState=true} invokes the activation handler regardless of {@code clickable/disabled} state.</li>
 *   <li>Right‑click behavior is opt‑in via {@code rightClicksOkWhenDisabled} and obeys {@code clickable}.</li>
 *   <li>Providing an {@code onClicked} callback means you are responsible for changing {@code checked} if you want
 *   toggle semantics.</li>
 * </ul>
 */
public class UIClickable<T extends UIClickable<T>> extends CustomPanel implements HasInteraction {

    protected final InteractionComp<T> interaction = comp().get(NativeComponents.INTERACTION);

    public boolean clickable = true;
    public boolean rightClicksOkWhenDisabled = false;
    public boolean performActionWhenDisabled = false;
    public boolean disabledWhileInvisible = true;
    public boolean soundEnabled = true;
    public Object customData = null;
    public String mouseOverSound = "ui_button_mouseover";
    public CallbackRunnable<T> onClicked;

    protected boolean disabled = false;
    protected boolean checked = false;
    protected boolean quickMode = false;

    public UIClickable(UIPanelAPI parent, int width, int height, CallbackRunnable<T> callback) {
        super(parent, width, height);

        onClicked = callback;

        interaction.onClicked = (source, isLeftClick) -> {
            if ((!isLeftClick && !rightClicksOkWhenDisabled) || !clickable) return;
            interaction.onShortcutPressed.run(source, null);
        };
        interaction.onHoverStarted = (source) -> { if (soundEnabled) {
            Global.getSoundPlayer().playUISound(mouseOverSound, 1, 1);
        }};
        interaction.onShortcutPressed = (source, event) -> {
            if (m_panel.getOpacity() <= 0f && disabledWhileInvisible) return;
            if (event != null) event.consume();

            if (disabled && !performActionWhenDisabled) {
                if (soundEnabled) Global.getSoundPlayer().playUISound("ui_button_disabled_pressed", 1, 1);
                return;
            } else if (soundEnabled) {
                Global.getSoundPlayer().playUISound("ui_button_pressed", 1, 1);
            }

            if (onClicked != null) {
                onClicked.run(self());
            } else if (!quickMode) {
                checked = !checked;
            }
        };
    }

    public boolean isEnabled() { return !disabled; }
    public void setEnabled(boolean enabled) {
        disabled = !enabled;
    }

    public boolean isChecked() { return checked; }
    public void setChecked(boolean bool) {
        checked = bool;
    }

    public boolean isQuickMode() { return quickMode; }
    public void setQuickMode(boolean mode) {
        quickMode = mode;
    }

    public void click(boolean ignoreState) {
        if (ignoreState) interaction.onShortcutPressed.run(self(), null);
        else interaction.onClicked.handle(self(), true);
    }

    public void setShortcut(int keyCode) {
        interaction.shortcut = keyCode;
    }

    @SuppressWarnings("unchecked")
    protected final T self() {
        return (T) this;
    }
}