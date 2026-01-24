package wfg.native_ui.ui.core;

import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;

import wfg.native_ui.ui.components.UIComponentContainer;

public interface UIElementAPI extends UIComponentAPI {

    /**
     * Returns the container holding optional components attached to this element.
     * Components may include background renderers, hover glows, audio hooks, or
     * other behavior/decorator objects.
     *
     * @return the UIComponentContainer for this element
     */
    UIComponentContainer getUIComponentContainer();

    /** Short-hand alias for {@link #getUIComponentContainer()} */
    UIComponentContainer comp();
    

    /** Short-hand alias for {@link #getPosition()} */
    PositionAPI pos();
}