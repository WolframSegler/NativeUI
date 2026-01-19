package wfg.wrap_ui.ui.plugins;

import wfg.wrap_ui.ui.panels.CustomPanel;

public class BasePanelPlugin<
    PanelType extends CustomPanel<
        ? extends CustomPanelPlugin<?, ? extends BasePanelPlugin<PanelType>>, 
        PanelType
    >
> extends CustomPanelPlugin<PanelType, BasePanelPlugin<PanelType>> {}