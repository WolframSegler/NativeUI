package wfg.wrap_ui.ui.panels;

import com.fs.starfarer.api.ui.UIPanelAPI;

import wfg.wrap_ui.ui.components.BackgroundComp;
import wfg.wrap_ui.ui.components.NativeComponents;
import wfg.wrap_ui.ui.panels.CustomPanel.HasBackground;

/**
 * An empty implementation of {@link CustomPanel}
 */
public class BasePanel extends CustomPanel<BasePanel> implements HasBackground {
    public final BackgroundComp bg = comp().get(NativeComponents.BACKGROUND);

    public BasePanel(UIPanelAPI parent, int width, int height) {
        super(parent, width, height);
    }
    public void createPanel() {}
}