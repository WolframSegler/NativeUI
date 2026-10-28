package wfg.native_ui.internal.ui.interaction;

import static wfg.native_ui.util.UIConstants.screenH;
import static wfg.native_ui.util.UIConstants.screenW;

import java.util.List;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;

import wfg.native_ui.internal.ui.core.UIEntity;
import wfg.native_ui.ui.Attachments;
import wfg.native_ui.ui.core.UIElementFlags.HasDebugBg;

public class OutsideEventDetector extends UIEntity implements HasDebugBg {
    private final OutsideEventListener listener;

    public boolean consumeMouseMove = true;
    public boolean consumeMouseScroll = true;
    public boolean isRemoveOnAction = true;
    public boolean triggerByModKey = false;
    public boolean triggerOnKeyUp = false;
    
    public OutsideEventDetector(OutsideEventListener owner) {
        super(screenW, screenH);
        listener = owner;
    }

    public final void attach() {
        attachTo(Attachments.getScreenPanel());
        bringToFront();
    }

    @Override
    public void processInputImpl(List<InputEventAPI> events) {
        for (InputEventAPI event : events) {
            if (event.isMouseEvent()) {
                if ((event.isMouseMoveEvent() && consumeMouseMove) ||
                    (event.isMouseScrollEvent() && consumeMouseScroll)
                ) { event.consume(); continue; }

                if ((event.isMouseDownEvent() || event.isMouseUpEvent()) &&
                    !event.isConsumed() && !listener.pos().containsEvent(event)
                ) {
                    listener.outsideClicked(event.isLMBEvent());
                    if (isRemoveOnAction) {
                        detach(); return;
                    }
                }

            } else if (event.isKeyboardEvent()) {
                if (event.isModifierKey()) {
                    if (triggerByModKey) listener.buttonPressed(event.isConsumed() ? Keyboard.KEY_NONE : event.getEventValue());
                    if (isRemoveOnAction) {
                        detach(); return;
                    }
                    event.consume();
                    continue;
                }
                if (event.isKeyUpEvent() && !triggerOnKeyUp) {
                    event.consume(); continue;
                }

                listener.buttonPressed(event.isConsumed() ? Keyboard.KEY_NONE : event.getEventValue());
                if (isRemoveOnAction) {
                    detach(); return;
                }
            }
        }
    }

    public static interface OutsideEventListener {
        void outsideClicked(boolean isLeft);
        void buttonPressed(int lwjgl_key);
        PositionAPI pos();
    }
}