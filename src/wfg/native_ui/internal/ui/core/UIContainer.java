package wfg.native_ui.internal.ui.core;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;

import rolflectionlib.ui.UiUtil;
import wfg.native_ui.ui.ComponentFactory;
import wfg.native_ui.ui.core.UIContainerAPI;
import wfg.native_ui.ui.core.UIElementAPI;
import wfg.native_ui.ui.event.IdentifiedPanel;
import wfg.native_ui.ui.system.BaseSystem;

public class UIContainer extends UIEntity implements UIContainerAPI {
    private final ArrayList<UIComponentAPI> mChildren = new ArrayList<>(4);
    /** Used for iteration. */
    private UIComponentAPI[] mChildSnapshot = new UIComponentAPI[0];

    public UIContainer(float width, float height) {
        super(width, height);
    }

    public UIContainer() {
        super();
    }

    public UIContainer(PositionAPI pos) {
        super(pos);
    }

    public PositionAPI add(UIComponentAPI comp) {
        if (UiUtil.uiComponentClass.isInstance(comp)) {
            for (UIComponentAPI child : mChildren) {
                if (child instanceof UIVanillaWrapper wrapper && wrapper.isWrapped(comp)) {
                    return wrapper.getPosition();
                }
            }
            comp = new UIVanillaWrapper(comp);
        }

        if (!mChildren.contains(comp)) {
            mChildren.add(comp);
            final PositionAPI compPos = comp.getPosition();
            // TODO mPos.add(compPos);
            // TODO compPos.setParent(mPos);
            // TODO comp.setParent(this);

            if (comp instanceof UIElementAPI element) {
                element.reportAttached();
            }
        }
        return comp.getPosition();
    }

    public PositionAPI add(TooltipMakerAPI tooltip) {
        return ComponentFactory.addTooltip(tooltip, 0f, false, this);
    }

    public void remove(UIComponentAPI comp) {
        if (UiUtil.uiComponentClass.isInstance(comp)) {
            for (UIComponentAPI child : mChildren) {
                if (child instanceof UIVanillaWrapper wrapper && wrapper.isWrapped(comp)) {
                    comp = wrapper;
                    break;
                }
            }
        }

        if (mChildren.remove(comp)) {
            final PositionAPI compPos = comp.getPosition();
            // TODO mPos.remove(compPos);
            // TODO compPos.setParent(null);
            // TODO comp.setParent(null);

            if (comp instanceof UIElementAPI element) {
                element.reportDetached();
            }
        }
    }

    public List<UIComponentAPI> getChildren() {
        return mChildren;
    }

    public List<UIComponentAPI> getChildrenCopy() {
        return new ArrayList<>(mChildren);
    }

    public void clearChildren() {
        for (UIComponentAPI child : getChildrenCopy()) {
            removeComponent(child);
        }
        mChildren.clear();
    }

    public <T extends UIComponentAPI> T getChild(Class<T> type) {
        for (UIComponentAPI child : mChildren) {
            if (type.isInstance(child)) return type.cast(child);

            if (child instanceof UIVanillaWrapper wrapper) {
                final UIComponentAPI candidate = wrapper.getWrapped();
                if (type.isInstance(candidate)) return type.cast(candidate);
            }
        }
        return null;
    }

    public UIComponentAPI getChild(String panelId) {
        if (panelId == null) return null;
        for (UIComponentAPI child : mChildren) {
            if (child instanceof IdentifiedPanel ip && panelId.equals(ip.getPanelId())) {
                return child;
            }
        }
        return null;
    }

    public PositionAPI addComponent(UIComponentAPI comp) { return add(comp); }
    public void removeComponent(UIComponentAPI comp) { remove(comp); }

    public PositionAPI addPos(UIComponentAPI comp) {
        final PositionAPI compPos = comp.getPosition();
        // TODO mPos.addChild(compPos);
        // TODO compPos.setParent(mPos);

        return comp.getPosition();
    }

    public void removePos(UIComponentAPI comp) {
        final PositionAPI compPos = comp.getPosition();
        // TODO mPos.removeChild(compPos);
        // TODO compPos.setParent(null);
    }

    public void bringComponentToTop(UIComponentAPI comp) { bringToTop(comp); }
    public void bringToTop(UIComponentAPI comp) {
        if (mChildren.remove(comp)) {
            mChildren.add(comp);
            bringToFront();
        }
    }

    public void bringToTopWithinItself(UIComponentAPI comp) {
        if (mChildren.remove(comp)) {
            mChildren.add(comp);
        }
    }

    public void sendToBottom(UIComponentAPI comp) {
        if (mChildren.remove(comp)) {
            mChildren.add(0, comp);
            sendToBack();
        }
    }

    public void sendToBottomWithinItself(UIComponentAPI comp) {
        if (mChildren.remove(comp)) {
            mChildren.add(0, comp);
        }
    }

    @Override
    public void renderImpl(float alpha) {
        for (BaseSystem system : system().getAll()) {
            system.renderBelow(this, alpha);
        }
        renderBelowImpl(alpha);

        final int size = mChildren.size();
        final UIComponentAPI[] snap = mChildren.toArray(mChildSnapshot);
        if (snap != mChildSnapshot) mChildSnapshot = snap;
        for (int i = 0; i < size; i++) snap[i].render(alpha);

        for (BaseSystem system : system().getAll()) {
            system.renderAbove(this, alpha);
        }
        renderAboveImpl(alpha);
    }

    @Override
    public void advanceImpl(float delta) {
        super.advanceImpl(delta);

        final int size = mChildren.size();
        final UIComponentAPI[] snap = mChildren.toArray(mChildSnapshot);
        if (snap != mChildSnapshot) mChildSnapshot = snap;
        for (int i = 0; i < size; i++) snap[i].advance(delta);
    }

    @Override
    public void processInputImpl(List<InputEventAPI> events) {
        super.processInputImpl(events);

        final int size = mChildren.size();
        final UIComponentAPI[] snap = mChildren.toArray(mChildSnapshot);
        if (snap != mChildSnapshot) mChildSnapshot = snap;
        for (int i = size - 1; i >= 0; i--) snap[i].processInput(events);
    }


    // TODO remove after update
    @Deprecated
    public PositionAPI add(LabelAPI comp) {
        return add((UIComponentAPI) comp);
    }

    // TODO remove after update
    @Deprecated
    public void remove(LabelAPI comp) {
        remove((UIComponentAPI) comp);
    }
}