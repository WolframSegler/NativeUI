package wfg.native_ui.ui.core;

import java.util.List;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;

import wfg.native_ui.ui.components.UIComponentContainer;
import wfg.native_ui.ui.systems.BaseSystem;
import wfg.native_ui.ui.systems.UISystemContainer;

public abstract class UIEntity extends UIElement implements UIEntityAPI {

    private UIComponentContainer compContainer = null;
    private UISystemContainer systemContainer = null;

    public UIEntity(PositionAPI initialPos) {
        super(initialPos);
    }

    public final UIComponentContainer getUIComponentContainer() { return comp(); }
    public final UIComponentContainer comp() {
        if (compContainer == null) compContainer = new UIComponentContainer();
        return compContainer;
    }

    public final UISystemContainer getUISystemContainer() { return system(); }
    public final UISystemContainer system() {
        if (systemContainer == null) systemContainer = new UISystemContainer();
        return systemContainer;
    }

    @Override
    public void render(float alpha) {
        for (BaseSystem system : system().getAll()) {
            // system.renderBelow(this, alpha);
        }
        for (BaseSystem system : system().getAll()) {
            // system.render(this, alpha);
        }
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        for (BaseSystem system : system().getAll()) {
            // system.processInput(this, events);
        }
    }

    @Override
    public void advance(float delta) {
        for (BaseSystem system : system().getAll()) {
            // system.advance(this, delta);
        }
    }
}