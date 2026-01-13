package wfg.wrap_ui.ui.plugins;

import wfg.wrap_ui.ui.panels.DockPanel;

public class DockPanelPlugin extends CustomPanelPlugin<DockPanel, DockPanelPlugin> {
    @Override
    public void advance(float amount) {
        super.advance(amount);
        getPanel().advanceImpl(amount);
    }

    @Override
    public void renderBelow(float alphaMult) {
        getPanel().renderImpl(alphaMult);
        super.render(alphaMult);
    }
}