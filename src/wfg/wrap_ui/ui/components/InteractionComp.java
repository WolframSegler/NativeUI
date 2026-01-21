package wfg.wrap_ui.ui.components;

import wfg.wrap_ui.util.CallbackRunnable;

import org.lwjgl.input.Keyboard;
import com.fs.starfarer.api.ui.TooltipMakerAPI.ActionListenerDelegate;

/**
 * Component describing interaction behavior for a panel.
 * <p>This is a strongly-typed, explicit alternative to {@link ActionListenerDelegate}.</p>
 *
 * <p>
 * Behavior is supplied via functional hooks rather than subclassing.
 * All hooks are optional.
 * </p>
 */
public final class InteractionComp<SourceType> extends BaseComponent {

    /** Use {@link Keyboard}. Default of 0 means no shortcut. */
    public int shortcut = 0;

    /** Called once per frame while the cursor is over the panel. */
    public CallbackRunnable<SourceType> onHover;

    /** Called once when the cursor first enters the panel. */
    public CallbackRunnable<SourceType> onHoverStarted;

    /** Called once when the cursor leaves the panel. */
    public CallbackRunnable<SourceType> onHoverEnded;

    /** Called when the shortcut key is pressed. */
    public CallbackRunnable<SourceType> onShortcutPressed;

    /** Called once when the panel is clicked. */
    public ClickHandler<SourceType> onClicked;

    @FunctionalInterface
    public static interface ClickHandler<SourceType> {
        void handle(SourceType source, boolean isLeftClick);
    }
}