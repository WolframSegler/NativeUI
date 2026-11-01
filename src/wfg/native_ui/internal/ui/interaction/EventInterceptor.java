package wfg.native_ui.internal.ui.interaction;

import static wfg.native_ui.util.UIConstants.screenH;
import static wfg.native_ui.util.UIConstants.screenW;

import java.util.List;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.util.FaderUtil;

import wfg.native_ui.internal.ui.core.UIElement;

public class EventInterceptor extends UIElement {
    final EventInterceptorListener mOwner;

    public EventInterceptor(EventInterceptorListener owner) {
        super(screenW, screenH);
        mOwner = owner;
    }

    @Override
    public void processInputImpl(List<InputEventAPI> events) {
        for (InputEventAPI e : events) {
            if (e.isConsumed()) continue;

            if (e.isLMBDownEvent() && !mOwner.pos().containsEvent(e)) {
                mOwner.absorbed(e);
            }

            e.consume();
        }
    }

    @Override
    public void advanceImpl(float delta) {
        if (mOwner.dismissInterceptor() && !mOwner.suspendInterceptor() &&
            mOwner.getFader().getBrightness() < 0.5f
        ) { detach(); }
    }

    public static interface EventInterceptorListener {
        default void absorbed(InputEventAPI event) {}
        PositionAPI pos();
        boolean dismissInterceptor();
        boolean suspendInterceptor();
        FaderUtil getFader();
    }
}