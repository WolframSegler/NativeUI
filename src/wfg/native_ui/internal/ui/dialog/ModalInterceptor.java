package wfg.native_ui.internal.ui.dialog;

import static wfg.native_ui.util.UIConstants.screenH;
import static wfg.native_ui.util.UIConstants.screenW;

import java.util.List;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import wfg.native_ui.ui.panel.CustomPanel;

public class ModalInterceptor extends CustomPanel {
    final ModalDialog dialog;

    public ModalInterceptor(UIPanelAPI parent, ModalDialog dialog) {
        super(parent, screenW, screenH);
        this.dialog = dialog;
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        for (InputEventAPI e : events) {
            if (e.isConsumed()) continue;

            if (e.isLMBDownEvent() && !dialog.getPos().containsEvent(e)) {
                dialog.outsideClickAbsorbed(e);
            }

            e.consume();
        }
    }

    @Override
    public void advance(float delta) {
        super.advance(delta);

        if (dialog.isBeingDismissed() && !dialog.suspendEventInterception &&
            dialog.getFaderBrightness() < 0.5f
        ) { m_parent.removeComponent(m_panel); }
    }
}