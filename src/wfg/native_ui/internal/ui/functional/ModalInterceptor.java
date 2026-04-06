package wfg.native_ui.internal.ui.functional;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import wfg.native_ui.internal.ui.dialog.ModalDialog;
import wfg.native_ui.ui.panel.CustomPanel;

public class ModalInterceptor extends CustomPanel<ModalInterceptor> {
    final ModalDialog dialog;

    public ModalInterceptor(UIPanelAPI parent, ModalDialog dialog) {
        super(parent, (int) Global.getSettings().getScreenWidth(), (int) Global.getSettings().getScreenHeight());
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
}