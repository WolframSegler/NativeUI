package wfg.native_ui.internal.ui.core;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import wfg.native_ui.ui.core.UIContainerAPI;
import wfg.native_ui.ui.core.UIEntityAPI;
import wfg.native_ui.ui.event.IdentifiedPanel;
import wfg.native_ui.ui.system.BaseSystem;

public class UIContainer extends UIEntity implements UIContainerAPI {
    private final List<UIComponentAPI> children = new ArrayList<>();

    public UIContainer(PositionAPI pos) {
        super(pos);
    }

    public PositionAPI add(UIComponentAPI comp) {
        if (!children.contains(comp)) {
            children.add(comp);
            
            // pos.add(comp.getPosition());
            // comp.getPosition().setParent(pos);
            // comp.setParent(this);

            if (comp instanceof UIEntityAPI element) {
                element.reportAttached();
            }
        }
        return comp.getPosition();
    }

    public void remove(UIComponentAPI comp) {
        if (children.remove(comp)) {
            // pos.remove(comp.getPosition());
            // comp.getPosition().setParent(null);
            // comp.setParent(null);

            if (comp instanceof UIEntityAPI element) {
                element.reportDetached();
            }
        }
    }

    public List<UIComponentAPI> getChildren() {
        return children;
    }

    public List<UIComponentAPI> getChildrenCopy() {
        return new ArrayList<>(children);
    }

    public void clearChildren() {
        for (UIComponentAPI child : getChildrenCopy()) {
            removeComponent(child);
        }
        children.clear();
    }

    public <T extends UIComponentAPI> T getChild(Class<T> type) {
        for (UIComponentAPI child : children) {
            if (type.isInstance(child)) return type.cast(child);
        }
        return null;
    }

    public UIComponentAPI getChild(String panelId) {
        if (panelId == null) return null;
        for (UIComponentAPI child : children) {
            if (child instanceof IdentifiedPanel ip && panelId.equals(ip.getPanelId())) {
                return child;
            }
        }
        return null;
    }

    public PositionAPI addComponent(UIComponentAPI var1) { return add(var1);}
    public void removeComponent(UIComponentAPI var1) { remove(var1); }

    public PositionAPI addPos(UIComponentAPI element) {
        // pos.addChild(element.getPosition());
        return element.getPosition();
    }

    public void removePos(UIComponentAPI element) {
        // pos.removeChild(element.getPosition());
    }

    public void bringComponentToTop(UIComponentAPI element) { bringToTop(element);}
    public void bringToTop(UIComponentAPI element) {
        if (children.remove(element)) {
            children.add(element);
            UIPanelAPI parent = getParent();
            if (parent != null) parent.bringComponentToTop(this);
        }
    }

    public void bringToTopWithinItself(UIComponentAPI element) {
        if (children.remove(element)) {
            children.add(element);
        }
    }

    public void sendToBottom(UIComponentAPI element) {
        if (children.remove(element)) {
            children.add(0, element);
            UIPanelAPI parent = getParent();
            if (parent != null) parent.sendToBottom(this);
        }
    }

    public void sendToBottomWithinItself(UIComponentAPI element) {
        if (children.remove(element)) {
            children.add(0, element);
        }
    }

    @Override
    public final void render(float alpha) {
        for (BaseSystem system : system().getAll()) {
            // system.renderBelow(this, alpha);
        }
        renderBelowImpl(alpha);

        children.forEach(c -> c.render(alpha));

        for (BaseSystem system : system().getAll()) {
            // system.render(this, alpha);
        }
        renderAboveImpl(alpha);
    }

    @Override
    public final void advance(float delta) {
        super.advance(delta);
        advanceImpl(delta);
        children.forEach(c -> c.advance(delta));
    }

    @Override
    public final void processInput(List<InputEventAPI> events) {
        super.processInput(events);
        processInputImpl(events);
        children.forEach(c -> c.processInput(events));
    }
}