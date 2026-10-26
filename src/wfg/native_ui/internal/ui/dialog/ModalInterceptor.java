package wfg.native_ui.internal.ui.dialog;

import static wfg.native_ui.util.UIConstants.screenH;
import static wfg.native_ui.util.UIConstants.screenW;

import java.util.List;

import com.fs.starfarer.api.input.InputEventAPI;

import wfg.native_ui.internal.ui.core.UIElement;

public class ModalInterceptor extends UIElement {
    final ModalDialog mDialog;

    public ModalInterceptor(ModalDialog dialog) {
        super(screenW, screenH);
        mDialog = dialog;
    }

    @Override
    public void processInputImpl(List<InputEventAPI> events) {
        for (InputEventAPI e : events) {
            if (e.isConsumed()) continue;

            if (e.isLMBDownEvent() && !mDialog.pos().containsEvent(e)) {
                mDialog.outsideClickAbsorbed(e);
            }

            e.consume();
        }
    }

    @Override
    public void advanceImpl(float delta) {
        if (mDialog.isBeingDismissed() && !mDialog.suspendEventInterception &&
            mDialog.getFaderBrightness() < 0.5f
        ) { detach(); }
    }
}