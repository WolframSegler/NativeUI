package wfg.native_ui.ui.systems;

import java.util.List;

import com.fs.starfarer.api.input.InputEventAPI;

import wfg.native_ui.ui.panels.CustomPanel;

/**
 * Base class for all systems operating on a UI element.
 * 
 * <p>Design note:</p>
 * Systems should not hold per-element state. All element-specific state should live in components.
 * Systems may have static utility members.
 * 
 * <p>Component contract:</p>
 * Each system is responsible for registering its corresponding component and system (for example {@link RawInputSystem}).
 * In the constructor of the system, you should call:
 * <pre>
 * element.comp().addCustomIfNotPresent(new CustomComp());
 * element.system().addCustomIfNotPresent(new CustomSystem()); // needed by the initializing system.
 * </pre>
 * This makes the system the authority for component creation, and ensures the element always has the
 * correct component instance. Element code and other systems may then safely access this component.
 */
public abstract class BaseSystem {

    /**Adds components or systems to the element that will be needed */
    public abstract void init(final CustomPanel<?> element);
    public void onRemove(final CustomPanel<?> element) {}

    public void processInput(final CustomPanel<?> element, final List<InputEventAPI> events) {}
    public void advance(final CustomPanel<?> element, float amount) {}
    public void renderBelow(final CustomPanel<?> element, float alpha) {}
    public void render(final CustomPanel<?> element, float alpha) {}
}