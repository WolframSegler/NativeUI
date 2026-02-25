package wfg.native_ui.ui.core;

import wfg.native_ui.ui.components.UIComponentContainer;
import wfg.native_ui.ui.systems.UISystemContainer;

public interface UIEntityAPI extends UIElementAPI {

    /**
     * Returns the container holding optional components attached to this element.
     * Components may include background renderers, hover glows, audio hooks, or
     * other behavior/decorator objects.
     */
    UIComponentContainer getUIComponentContainer();

    /** Short-hand alias for {@link #getUIComponentContainer()} */
    UIComponentContainer comp();

    /** Returns the container holding systems attached to this element. */
    UISystemContainer getUISystemContainer();

    /** Short-hand alias for {@link #getUISystemContainer()} */
    UISystemContainer system();
}