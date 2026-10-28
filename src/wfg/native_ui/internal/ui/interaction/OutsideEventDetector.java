package wfg.native_ui.internal.ui.interaction;

import static wfg.native_ui.util.UIConstants.screenH;
import static wfg.native_ui.util.UIConstants.screenW;

import java.util.List;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import wfg.native_ui.internal.ui.core.UIElement;
import wfg.native_ui.ui.Attachments;

public class OutsideEventDetector extends UIElement {
    private final OutsideEventListener mListener;
    private final UIPanelAPI mTargetParent;

    public boolean consumeMouseMove = true;
    public boolean consumeMouseScroll = true;
    public boolean isRemoveOnAction = true;
    public boolean triggerByModKey = false;
    public boolean triggerOnKeyUp = false;
    
    public OutsideEventDetector(OutsideEventListener owner) {
        this(owner, Attachments.getScreenPanel());
    }

    public OutsideEventDetector(OutsideEventListener owner, UIPanelAPI targetParent) {
        super(screenW, screenH);
        mListener = owner;
        mTargetParent = targetParent;
    }

    public final void attach() {
        attachTo(mTargetParent);
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
                    !event.isConsumed() && !mListener.pos().containsEvent(event)
                ) {
                    mListener.outsideClicked(event.isLMBEvent());
                    if (isRemoveOnAction) {
                        detach(); return;
                    }
                }

            } else if (event.isKeyboardEvent()) {
                if (event.isModifierKey()) {
                    if (triggerByModKey) mListener.buttonPressed(event.isConsumed() ? Keyboard.KEY_NONE : event.getEventValue());
                    if (isRemoveOnAction) {
                        detach(); return;
                    }
                    event.consume();
                    continue;
                }
                if (event.isKeyUpEvent() && !triggerOnKeyUp) {
                    event.consume(); continue;
                }

                mListener.buttonPressed(event.isConsumed() ? Keyboard.KEY_NONE : event.getEventValue());
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