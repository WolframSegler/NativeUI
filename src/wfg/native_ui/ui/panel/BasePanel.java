package wfg.native_ui.ui.panel;

import com.fs.starfarer.api.ui.UIPanelAPI;

import wfg.native_ui.ui.component.BackgroundComp;
import wfg.native_ui.ui.component.NativeComponents;
import wfg.native_ui.ui.core.UIElementFlags.HasBackground;

/**
 * An empty implementation of {@link CustomPanel}
 */
public class BasePanel extends CustomPanel implements HasBackground {
    public final BackgroundComp bg = comp().get(NativeComponents.BACKGROUND);

    public BasePanel(UIPanelAPI parent, int width, int height) {
        super(parent, width, height);
    }
}