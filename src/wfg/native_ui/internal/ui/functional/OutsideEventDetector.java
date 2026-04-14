package wfg.native_ui.internal.ui.functional;

import static wfg.native_ui.util.UIConstants.screenH;
import static wfg.native_ui.util.UIConstants.screenW;

import java.util.List;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;

import wfg.native_ui.ui.Attachments;
import wfg.native_ui.ui.panel.CustomPanel;

public class OutsideEventDetector extends CustomPanel<OutsideEventDetector> {
    private final OutisdeEventListener listener;

    public boolean consumeMouseMove = true;
    public boolean consumeMouseScroll = true;
    public boolean isRemoveOnAction = true;
    public boolean triggerByModKey = false;
    public boolean triggerOnKeyUp = false;
    
    public OutsideEventDetector(OutisdeEventListener owner) {
        super(Attachments.getScreenPanel(), screenW, screenH);
        listener = owner;
    }

    public final void attach() {
        m_parent.addComponent(m_panel);
        m_parent.bringComponentToTop(m_panel);
    }

    public final void detach() {
        m_parent.removeComponent(m_panel);
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        for (InputEventAPI event : events) {
            if (event.isConsumed()) continue;

            if (event.isMouseEvent()) {
                if ((event.isMouseMoveEvent() && consumeMouseMove) ||
                    (event.isMouseScrollEvent() && consumeMouseScroll)
                ) { event.consume(); continue; }
                if ((event.isMouseDownEvent() || event.isMouseUpEvent()) &&
                    !listener.getPos().containsEvent(event)
                ) {
                    listener.outsideClicked(event.isLMBEvent());
                    if (isRemoveOnAction) {
                        detach(); continue;
                    }
                }

            } else if (event.isKeyboardEvent()) {
                if (event.isModifierKey()) {
                    if (triggerByModKey) listener.buttonPressed(event.getEventValue());
                    event.consume();
                    continue;
                }
                if (event.isKeyUpEvent() && !triggerOnKeyUp) { event.consume(); continue; }

                listener.buttonPressed(event.getEventValue());
                if (isRemoveOnAction) {
                    detach(); continue;
                }
            }
        }
    }

    public static interface OutisdeEventListener {
        void outsideClicked(boolean isLeft);
        void buttonPressed(int lwjgl_key);
        PositionAPI getPos();
    }
}