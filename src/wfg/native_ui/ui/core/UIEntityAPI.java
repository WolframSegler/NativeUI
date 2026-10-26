package wfg.native_ui.ui.core;

import wfg.native_ui.ui.component.UIComponentContainer;
import wfg.native_ui.ui.core.UIElementFlags.HasBackground;
import wfg.native_ui.ui.system.UISystemContainer;

/**
 * <p><strong>Design principles:</strong></p>
 * <ul>
 *   <li>Systems operate primarily on components; direct panel access is secondary and
 *       reserved for panel-defined behavior.</li>
 *   <li>Capability interfaces (such as {@link HasBackground}) register systems that may add their required components.</li>
 * </ul>
 * 
 * <p><strong>Component access policy:</strong></p>
 * <ul>
 *   <li><b>Public components</b> expose supported customization points and may be read or modified
 *       directly by external code.</li>
 *   <li><b>Protected components</b> are internal implementation details and must only be accessed
 *       by this class or subclasses.</li>
 *   <li>If a panel provides a setter for a value that affects component state, that setter
 *       <b>must be used</b> instead of mutating the component directly.</li>
 * </ul>
 */
public interface UIEntityAPI extends UIElementAPI {

    /**
     * Returns the container holding components attached to this element.
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

    /** Uses {@link UIElementFlags} to attach native systems by doing instanceof checks. */
    void initSystems();
}