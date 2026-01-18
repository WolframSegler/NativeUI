package wfg.wrap_ui.ui.plugins;

import wfg.wrap_ui.ui.panels.SpritePanel;

public class SpritePanelPlugin<
    PanelType extends SpritePanel<PanelType>
> extends CustomPanelPlugin<PanelType, SpritePanelPlugin<PanelType>> {

    @Override
    public void renderBelow(float alphaMult) {
        super.renderBelow(alphaMult);
        getPanel().renderImpl(alphaMult);
    }
}