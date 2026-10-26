package wfg.native_ui.ui.interaction;

import com.fs.starfarer.api.Global;

import wfg.native_ui.example.interaction.UIClickableExample;
import wfg.native_ui.internal.ui.core.UIContainer;
import wfg.native_ui.ui.component.InteractionComp;
import wfg.native_ui.ui.component.NativeComponents;
import wfg.native_ui.ui.core.UIElementFlags.HasInteraction;
import wfg.native_ui.ui.functional.CallbackRunnable;

/**
 * Base clickable component with interaction support, sound, shortcut, and checked state.
 *
 * <ul>
 *   <li>Plays mouseover/click sounds and fires an action callback when activated.</li>
 *   <li>If {@link #onClicked} is null the component will toggle its internal {@link #checked} state when activated;
 *   if a callback is provided the callback is invoked instead and the component will NOT toggle automatically.</li>
 *   <li>Supports keyboard shortcut (via {@link #setShortcut}).</li>
 *   <li>Use {@link #setEnabled} to enable/disable the component.</li>
 *   <li>If {@link #disabledWhileInvisible} is false the component can still be activated while fully transparent;
 *   otherwise it is inert.</li>
 * </ul>
 *
 * <p>Interaction details and caveats:
 * <ul>
 *   <li>Click handling is routed through the internal {@link InteractionComp}. Calling {@link #click} with
 *   {@code ignoreState=true} invokes the activation handler regardless of {@code clickable/disabled} state.</li>
 *   <li>Right‑click behavior is opt‑in via {@link #rightClicksOkWhenDisabled} and obeys {@link #clickable}.</li>
 *   <li>Providing an {@link #onClicked} callback means you are responsible for changing {@link #checked} if you want
 *   toggle semantics.</li>
 * </ul>
 * 
 * <p><strong>Example: </strong> {@link UIClickableExample}</p>
 */
public class UIClickable<T extends UIClickable<T>> extends UIContainer implements HasInteraction {

    protected final InteractionComp<T> interaction = comp().get(NativeComponents.INTERACTION);

    public boolean clickable = true;
    public boolean rightClicksOkWhenDisabled = false;
    public boolean performActionWhenDisabled = false;
    public boolean disabledWhileInvisible = true;
    public boolean soundEnabled = true;
    public Object customData = null;
    public String mouseOverSound = "ui_button_mouseover";
    public String disabledSound = "ui_button_disabled_pressed";
    public String pressedSound = "ui_button_pressed";
    public CallbackRunnable<T> onClicked;

    protected boolean disabled = false;
    protected boolean checked = false;
    protected boolean quickMode = false;

    public UIClickable(float width, float height, CallbackRunnable<T> callback) {
        super(width, height);

        onClicked = callback;

        interaction.onClicked = (source, isLeftClick) -> {
            if ((!isLeftClick && !rightClicksOkWhenDisabled) || !clickable) return;
            interaction.onShortcutPressed.run(source, null);
        };
        interaction.onHoverStarted = (source) -> { if (soundEnabled) {
            Global.getSoundPlayer().playUISound(mouseOverSound, 1, 1);
        }};
        interaction.onShortcutPressed = (source, event) -> {
            if (getOpacity() <= 0f && disabledWhileInvisible) return;
            if (event != null) event.consume();

            playPressSound();
            if (disabled && !performActionWhenDisabled) return;

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

    protected final void playPressSound() {
        if (!soundEnabled) return;
        final String audio = disabled && !performActionWhenDisabled ? disabledSound : pressedSound;
        Global.getSoundPlayer().playUISound(audio, 1, 1);
    }
}