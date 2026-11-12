package wfg.native_ui.internal.ui.core;

import static wfg.native_ui.util.Globals.settings;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.FaderUtil;

import wfg.native_ui.ui.ComponentFactory;
import wfg.native_ui.ui.MethodFields;
import wfg.native_ui.ui.core.UIContainerAPI;
import wfg.native_ui.ui.core.UIElementAPI;
import wfg.native_ui.ui.core.UIEntityAPI;
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

    public PositionAPI add(UIComponentAPI comp) {
        comp = resolveComp(comp);
        if (comp instanceof UIVanillaWrapper) return comp.getPosition();
        if (MethodFields.uiInternalCompClass.isInstance(comp)) {
            comp = new UIVanillaWrapper(comp);
        }

        if (!mChildren.contains(comp)) {
            mChildren.add(comp);
            addPos(comp);
            comp.setParent2(this);

            if (comp instanceof UIElementAPI element) {
                element.reportAttached();
            }
        }
        return comp.getPosition();
    }

    /**
     * This call internally calls {@link #addComponent} on the {@link UIContainer}, which treats it as a usual {@link UIComponentAPI}, so the wrapping works.
     */
    public PositionAPI add(TooltipMakerAPI tooltip) {
        return ComponentFactory.addTooltip(tooltip, 0f, false, this);
    }

    public void remove(UIComponentAPI toRemove) {
        final UIComponentAPI comp = resolveComp(toRemove);

        if (mChildren.remove(comp)) {
            removePos(comp);
            comp.setParent2(null);

            if (comp instanceof UIElementAPI element) {
                element.reportDetached();
            }
        }

        if (toRemove instanceof UIEntityAPI entity) {
            entity.notifyRemoved();
        }
    }

    public List<UIComponentAPI> getChildren() {
        return mChildren;
    }

    public List<UIComponentAPI> getChildrenCopy() {
        return new ArrayList<>(mChildren);
    }

    public final List<UIComponentAPI> getChildrenCopy2() {
        return getChildrenCopy();
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
                final UIComponentAPI candidate = wrapper.wrapped();
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

    public void bringComponentToTop(UIComponentAPI comp) { bringToTop(comp); }
    public void bringToTop(UIComponentAPI comp) {
        comp = resolveComp(comp);
        if (mChildren.remove(comp)) {
            mChildren.add(comp);
            bringToFront();
        }
    }

    public void bringToTopWithinItself(UIComponentAPI comp) {
        comp = resolveComp(comp);
        if (mChildren.remove(comp)) {
            mChildren.add(comp);
        }
    }

    public void sendToBottom(UIComponentAPI comp) {
        comp = resolveComp(comp);
        if (mChildren.remove(comp)) {
            mChildren.add(0, comp);
            sendToBack();
        }
    }

    public void sendToBottomWithinItself(UIComponentAPI comp) {
        comp = resolveComp(comp);
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
        final int size = mChildren.size();
        final UIComponentAPI[] snap = mChildren.toArray(mChildSnapshot);
        if (snap != mChildSnapshot) mChildSnapshot = snap;
        for (int i = 0; i < size; i++) snap[i].advance(delta);

        super.advanceImpl(delta);
    }

    @Override
    public void processInputImpl(List<InputEventAPI> events) {
        final int size = mChildren.size();
        final UIComponentAPI[] snap = mChildren.toArray(mChildSnapshot);
        if (snap != mChildSnapshot) mChildSnapshot = snap;
        for (int i = size - 1; i >= 0; i--) snap[i].processInput(events);

        super.processInputImpl(events);
    }

    public final PositionAPI createPosition() {
        return settings.createPosition();
    }

    public final UIComponentAPI wrapAndAdd(UIComponentAPI comp, FaderUtil fader) {
        add(comp);
        return comp;
    }

    /** Returns the wrapper if the component is a vanilla element. */
    private UIComponentAPI resolveComp(UIComponentAPI comp) {
        if (!MethodFields.uiInternalCompClass.isInstance(comp)) return comp;
        for (UIComponentAPI child : mChildren) {
            if (child instanceof UIVanillaWrapper wrapper && wrapper.isWrapped(comp)) return wrapper;
        }
        return comp;
    }
}