package wfg.native_ui.ui.component;

/**
 * Base class for all components that can be attached to a {@link UIComponentContainer}.
 *
 * <p>
 * Components in NativeUI are <b>data and behavior holders</b>, not inheritance
 * extension points. They are intended to be <b>configured</b>, not subclassed.
 * </p>
 *
 * <p>
 * Custom behavior should be introduced via functional interfaces rather than by creating subclasses.
 * </p>
 *
 * <p>
 * Components may provide <b>derived or convenience queries</b> that operate solely on their own
 * state. Such queries are encouraged when they simplify system logic, but components should
 * not observe other components or perform side effects.
 * Example: <code>isValid()</code> on a UIContext component.
 * </p>
 *
 * <p>
 * <b>Read/Write Contract:</b>
 * <ul>
 *   <li>Systems may freely read component fields.</li>
 *   <li>If a component value has a corresponding setter on the panel (e.g., updating a shortcut, enabling a tooltip),
 *       the setter <b>must</b> be used to mutate it. Direct field writes may leave dependent state inconsistent.</li>
 *   <li>Components themselves should not perform side effects beyond their own internal derived state.</li>
 * </ul>
 * </p>
 *
 * <p>
 * This design avoids inheritance hierarchies in favor of composition and delegation.
 * <ul>
 *   <li>Keeps component access fast and predictable</li>
 *   <li>Avoids type explosion and casting</li>
 *   <li>Allows components to be freely mixed and reused</li>
 *   <li>Keeps systems decoupled from concrete component implementations</li>
 * </ul>
 * </p>
 *
 * <p>
 * If you feel the urge to subclass a component, it is usually a sign that the
 * component should instead expose an additional callback or functional hook.
 * </p>
 */
public abstract class BaseComponent {

    /**
     * Whether this component is active.
     * Systems consuming this component are expected to respect this flag.
     */
    public boolean enabled = true;
}