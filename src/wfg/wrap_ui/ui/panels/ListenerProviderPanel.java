package wfg.wrap_ui.ui.panels;

import com.fs.starfarer.api.ui.UIPanelAPI;

import wfg.wrap_ui.ui.panels.CustomPanel.AcceptsActionListener;
import wfg.wrap_ui.ui.plugins.BasePanelPlugin;

/**
 * Wrapper panel for providing a listener in overlays.
 */
public class ListenerProviderPanel extends CustomPanel<
    BasePanelPlugin<ListenerProviderPanel>, ListenerProviderPanel
> implements AcceptsActionListener {
    public ListenerProviderPanel(UIPanelAPI parent, int width, int height) {
        super(parent, width, height, new BasePanelPlugin<>());

        getPlugin().init(this);
    }

    public void createPanel() {}
}