package wfg.native_ui.ui.system;

import java.util.List;

import com.fs.starfarer.api.input.InputEventAPI;

import wfg.native_ui.ui.core.UIEntityAPI;

/**
 * Base class for all systems operating on a UI element.
 * 
 * <br><br>
 * 
 * Systems should not hold per-element state. All element-specific state should live in components.
 * Systems may have static utility members.
 * 
 * Each system is responsible for registering its corresponding component and system (for example {@link RawInputSystem}).
 * In the {@link #init} method of the system, you should call:
 * <pre>
 * element.comp().addCustomIfNotPresent(new CustomComp());
 * element.system().addCustomIfNotPresent(new CustomSystem()); // if the system needs other systems.
 * </pre>
 * This makes the system the authority for component creation, and ensures the element always has the
 * correct component instance. {@link UIEntityAPI} code and other systems may then safely access this component.
 */
public abstract class BaseSystem {

    /**Adds components or systems to the element that will be needed */
    public abstract void init(final UIEntityAPI element);
    public void onRemove(final UIEntityAPI element) {}

    public void processInput(final UIEntityAPI element, final List<InputEventAPI> events) {}
    public void advance(final UIEntityAPI element, float delta) {}
    public void renderBelow(final UIEntityAPI element, float alpha) {}
    public void renderAbove(final UIEntityAPI element, float alpha) {}
}