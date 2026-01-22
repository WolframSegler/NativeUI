package wfg.wrap_ui.ui.panels;

import com.fs.starfarer.api.ui.UIPanelAPI;

import wfg.wrap_ui.ui.components.InteractionComp;
import wfg.wrap_ui.ui.components.NativeComponents;
import wfg.wrap_ui.ui.panels.CustomPanel.HasInteraction;

/**
 * Wrapper panel for providing a listener in overlays.
 */
public class ListenerProviderPanel extends CustomPanel<ListenerProviderPanel> implements HasInteraction {
    public final InteractionComp<ListenerProviderPanel> interaction = comp().get(NativeComponents.INTERACTION);

    public ListenerProviderPanel(UIPanelAPI parent, int width, int height) {
        super(parent, width, height);
    }
    public void createPanel() {}
}