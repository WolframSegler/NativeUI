package wfg.native_ui.internal.ui.plugins;

import java.util.List;

import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;

import wfg.native_ui.internal.ui.dialogs.ModalDialog;

public class ModalInterceptorPlugin implements CustomUIPanelPlugin{
    final ModalDialog dialog;

    public ModalInterceptorPlugin(ModalDialog d) { dialog = d;}

    public void processInput(List<InputEventAPI> events) {
        for (InputEventAPI e : events) {
            if (e.isConsumed()) continue;

            if (e.isLMBDownEvent() && !dialog.getPos().containsEvent(e)) {
                dialog.outsideClickAbsorbed(e);
            }

            e.consume();
        }
    }

    public void advance(float arg0) {}
    public void positionChanged(PositionAPI arg0) {}
    public void render(float arg0) {}
    public void renderBelow(float arg0) {}
    public void buttonPressed(Object arg0) {}
}