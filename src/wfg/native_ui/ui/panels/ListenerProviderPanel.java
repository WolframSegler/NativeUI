package wfg.native_ui.ui.panels;

import com.fs.starfarer.api.ui.UIPanelAPI;

import wfg.native_ui.ui.components.InteractionComp;
import wfg.native_ui.ui.components.NativeComponents;
import wfg.native_ui.ui.core.UIElementFlags.HasInteraction;

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