package wfg.native_ui.ui.functional;

import com.fs.starfarer.api.ui.UIPanelAPI;

import wfg.native_ui.ui.component.InteractionComp;
import wfg.native_ui.ui.component.NativeComponents;
import wfg.native_ui.ui.core.UIElementFlags.HasInteraction;
import wfg.native_ui.ui.panel.CustomPanel;

/**
 * Wrapper panel for providing a listener in overlays.
 */
public class ListenerProviderPanel extends CustomPanel implements HasInteraction {
    public final InteractionComp<ListenerProviderPanel> interaction = comp().get(NativeComponents.INTERACTION);

    public ListenerProviderPanel(UIPanelAPI parent, int width, int height) {
        super(parent, width, height);
    }
}