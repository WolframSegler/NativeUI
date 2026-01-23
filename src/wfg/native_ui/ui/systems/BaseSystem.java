package wfg.native_ui.ui.systems;

import java.util.List;

import com.fs.starfarer.api.input.InputEventAPI;

import wfg.native_ui.ui.components.InputSnapshot;
import wfg.native_ui.ui.panels.CustomPanel;

/**
 * Base class for all systems operating on a panel.
 * 
 * <p>Design note:</p>
 * Systems are intended to remain stateless. Any mutable state or configuration
 * variables must be stored inside the associated component,
 * not in the system itself. Systems should access this state via the panel's components.
 * 
 * <p>Component contract:</p>
 * Each system is responsible for creating its corresponding component and registering it with the panel.
 * For example, in the constructor of the system, you should call:
 * <pre>
 * if (!panel.comp().hasCustomComp(CustomComponent.class)) {
 *      panel.comp().addCustomComp(new CustomComponent());
 * }
 * </pre>
 * This makes the system the authority for component creation, and ensures the panel always has the
 * correct component instance. Panel code and other systems may then safely access this component.
 */
public abstract class BaseSystem<PanelType extends CustomPanel<PanelType>> {
    protected PanelType panel;

    public BaseSystem(PanelType a) {
        panel = a;
    }

    /**
     * Runs before the game itself handles the inputs.
     */
    public void processInput(List<InputEventAPI> events, InputSnapshot input) {}
    public void advance(float amount, InputSnapshot input) {}
    public void renderBelow(float alphaMult, InputSnapshot input) {}
    public void render(float alphaMult, InputSnapshot input) {}
    public void onRemove() {}
}