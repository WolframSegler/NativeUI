package wfg.wrap_ui.ui.panels;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Mouse;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import rolflectionlib.util.RolfLectionUtil;
import wfg.wrap_ui.ui.ComponentFactory;
import wfg.wrap_ui.ui.components.ComponentContainer;
import wfg.wrap_ui.ui.components.InputSnapshot;
import wfg.wrap_ui.ui.components.LayoutOffsetComp;
import wfg.wrap_ui.ui.components.NativeComponents;
import wfg.wrap_ui.ui.components.UIContextComp;
import wfg.wrap_ui.ui.plugins.ForwardingPanelPlugin;
import wfg.wrap_ui.ui.systems.AudioFeedbackSystem;
import wfg.wrap_ui.ui.systems.BackgroundSystem;
import wfg.wrap_ui.ui.systems.BaseSystem;
import wfg.wrap_ui.ui.systems.HoverGlowSystem;
import wfg.wrap_ui.ui.systems.InteractionSystem;
import wfg.wrap_ui.ui.systems.OutlineSystem;
import wfg.wrap_ui.ui.systems.TooltipSystem;

/**
 * Represents the visual and layout container for a set of components.
 *
 * <p><strong>Design principles:</strong></p>
 * <ul>
 *   <li>The panel owns all <em>UI-specific</em> state like background color, position,
 *       dimensions, layout offsets, and other visual properties.</li>
 *   <li>Panels are responsible for creating, registering, and wiring their components.</li>
 *   <li>Systems operate primarily on components; direct panel access is secondary and
 *       reserved for panel-defined behavior.</li>
 *   <li>Capability interfaces (such as {@link HasBackground}) register systems that may add their required components.</li>
 *   <li>Panels may expose selected components as <code>public final</code> fields.
 *       Such components are part of the panel’s public contract. Components not exposed
 *       this way are internal and must be accessed only through panel APIs.</li>
 *   <li>The associated plugin is a thin forwarder and does not own state or behavior.</li>
 * </ul>
 */
public abstract class CustomPanel<
    PanelType extends CustomPanel<PanelType>
> {
    public static final Object clearChildrenMethod;
    public static final Object getChildrenCopyMethod;
    public static final Object getChildrenNonCopyMethod;
    public static final Object addToPositionMethod;
    public static final Object removeFromPositionMethod;
    public static final Object positionSetParentMethod;

    static {
        final UIPanelAPI panelIns = Global.getSettings().createCustom(0, 0, null);
        final Class<?> panelClazz = panelIns.getClass();
        final Class<?> posClazz = panelIns.getPosition().getClass();

        clearChildrenMethod = RolfLectionUtil.getMethod(
            "clearChildren", panelClazz);
        getChildrenCopyMethod = RolfLectionUtil.getMethod(
            "getChildrenCopy", panelClazz);
        getChildrenNonCopyMethod = RolfLectionUtil.getMethod(
            "getChildrenNonCopy", panelClazz);
        addToPositionMethod = RolfLectionUtil.getMethod("add", posClazz, 1);
        removeFromPositionMethod = RolfLectionUtil.getMethod("remove", posClazz, 1);
        positionSetParentMethod = RolfLectionUtil.getMethod("setParent", posClazz, 1);
    }

    private ComponentContainer compContainer = null;
    private final List<BaseSystem<PanelType>> systems = new ArrayList<>();
    protected final InputSnapshot inputSnapshot = new InputSnapshot();

    protected final UIPanelAPI m_parent;
    protected final UIPanelAPI m_panel;
    protected final PositionAPI pos;

    /**
     * Ownership and lifecycle rules for child panels:
     * <ul>
     *   <li>The child <b>MUST NOT</b> add itself to the parent.
     *       This prevents the child from being responsible for its own positioning,
     *       since each panel handles positioning its children separately.</li>
     *   <li>The parent <b>MUST NOT</b> call <code>{@link #createPanel()}</code>.
     *      This ensures that the child’s members are fully initialized before panel creation.</li>
     * </ul>
     */
    public CustomPanel(UIPanelAPI parent, int width, int height) {
        m_parent = parent;

        final ForwardingPanelPlugin plugin = new ForwardingPanelPlugin();
        m_panel = Global.getSettings().createCustom(width, height, plugin);
        pos = m_panel.getPosition();
        plugin.m_panel = this;

        initSystems();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void initSystems() {
        if (this instanceof HasBackground) {
            addSystem(new BackgroundSystem(this));
        }

        if (this instanceof HasOutline) {
            addSystem(new OutlineSystem(this));
        }

        if (this instanceof HasHoverGlow) {
            addSystem(new HoverGlowSystem(this));
        }

        if (this instanceof HasTooltip) {
            addSystem(new TooltipSystem(this));
        }

        if (this instanceof HasAudioFeedback) {
            addSystem(new AudioFeedbackSystem(this));
        }

        if (this instanceof HasInteraction) {
            addSystem(new InteractionSystem(this));
        }

        if (this instanceof HasLayoutOffset) {
            comp().setIfNotPresent(NativeComponents.LAYOUT_OFFSET, new LayoutOffsetComp());
        }

        if (this instanceof HasUIContext) {
            comp().setIfNotPresent(NativeComponents.UI_CONTEXT, new UIContextComp());
        }
    }

    public final ComponentContainer getComponentContainer() { return comp(); }
    public final ComponentContainer comp() {
        if (compContainer == null) compContainer = new ComponentContainer();
        return compContainer;
    }

    protected final <C extends BaseSystem<PanelType>> void addSystem(C system) {
        systems.add(system);
    }

    public void removeSystem(BaseSystem<PanelType> system) {
        systems.remove(system);
        system.onRemove();
    }

    public final List<BaseSystem<PanelType>> getPanelSystems() { return systems(); }
    public final List<BaseSystem<PanelType>> systems() {
        return systems;
    }

    public void render(float alpha) {
        for (BaseSystem<PanelType> system : systems) {
            system.render(alpha, inputSnapshot);
        }
    }

    public void renderBelow(float alpha) {
        for (BaseSystem<PanelType> system : systems) {
            system.renderBelow(alpha, inputSnapshot);
        }
    }

    public void advance(float delta) {
        for (BaseSystem<PanelType> system : systems) {
            system.advance(delta, inputSnapshot);
        }
    }

    public void processInput(List<InputEventAPI> events) {
        inputSnapshot.resetFrameFlags();

        // General events used by most systems
        for (InputEventAPI event : events) {
            
            if (event.isMouseMoveEvent()) {
                inputSnapshot.mouseEvent = event;

                final float mouseX = event.getX();
                final float mouseY = event.getY();

                final float x = pos.getX();
                final float y = pos.getY();
                final float w = pos.getWidth();
                final float h = pos.getHeight();

                // Check for mouse over panel
                final boolean hoveredBefore = inputSnapshot.hoveredLastFrame;
                inputSnapshot.hoveredLastFrame = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;

                inputSnapshot.hoverStarted = inputSnapshot.hoveredLastFrame && !hoveredBefore;
                inputSnapshot.hoverEnded   = !inputSnapshot.hoveredLastFrame && hoveredBefore;
            }

            if (event.isLMBDownEvent() && inputSnapshot.hoveredLastFrame) {
                inputSnapshot.LMBDownLastFrame = true;
                inputSnapshot.hasLMBClickedBefore = true;
                inputSnapshot.isActive = true;
            }

            if (event.isLMBUpEvent() || !Mouse.isButtonDown(0)) {
                if (inputSnapshot.hasLMBClickedBefore) inputSnapshot.LMBUpLastFrame = true;
                inputSnapshot.isActive = false;
                inputSnapshot.hasLMBClickedBefore = false;
            }

            if (event.isRMBDownEvent() && inputSnapshot.hoveredLastFrame) {
                inputSnapshot.RMBDownLastFrame = true;
                inputSnapshot.hasRMBClickedBefore = true;
            }

            if (event.isRMBUpEvent() || !Mouse.isButtonDown(1)) {
                if (inputSnapshot.hasRMBClickedBefore) inputSnapshot.RMBUpLastFrame = true;
                inputSnapshot.hasRMBClickedBefore = false;
            }
        }

        // System specific
        for (BaseSystem<PanelType> system : systems) {
            system.processInput(events, inputSnapshot);
        }
    }

    public void buttonPressed(Object buttonID) {}
    public void positionChanged(PositionAPI position) {}

    public final UIPanelAPI getPanel() { return m_panel; }
    public final PositionAPI getPos() { return pos; }
    public final UIPanelAPI getParent() { return m_parent; }

    public final PositionAPI add(LabelAPI a) {
        return add((UIComponentAPI) a);
    }

    public final PositionAPI add(TooltipMakerAPI a) {
        return ComponentFactory.addTooltip(a, 0f, false, m_panel);
    }

    public final PositionAPI add(UIComponentAPI a) {
        m_panel.addComponent(a);

        return (a).getPosition();
    }

    public final PositionAPI add(CustomPanel<?> a) {
        m_panel.addComponent(a.getPanel());

        return a.getPos();
    }

    public final void remove(LabelAPI a) {
        remove((UIComponentAPI) a);
    }

    public final void remove(UIComponentAPI a) {
        m_panel.removeComponent(a);
    }

    public PositionAPI addPositionOnly(UIComponentAPI comp) {
        final PositionAPI position = comp.getPosition();
        RolfLectionUtil.invokeMethodDirectly(positionSetParentMethod, position, pos);
        RolfLectionUtil.invokeMethodDirectly(addToPositionMethod, pos, position);
        return position;
    }

    public PositionAPI removePositionOnly(UIComponentAPI comp) {
        final PositionAPI position = comp.getPosition();
        RolfLectionUtil.invokeMethodDirectly(positionSetParentMethod, position, (Object)null);
        RolfLectionUtil.invokeMethodDirectly(removeFromPositionMethod, pos, position);
        return position;
    }

    public final void clearChildren() {
        RolfLectionUtil.invokeMethodDirectly(clearChildrenMethod, m_panel);
    }

    public final void setSize(int width, int height) {
        pos.setSize(width, height);
    }

    public final void setWidth(int width) {
        pos.setSize(width, pos.getHeight());
    }

    public final void setHeight(int height) {
        pos.setSize(pos.getWidth(), height);
    }

    /**
     * The method for populating the panel. Can be left empty.
     */
    public abstract void createPanel();

    public interface HasInteraction {}
    public interface HasHoverGlow {}
    public interface HasOutline {}
    public interface HasAudioFeedback {}
    public interface HasBackground {}
    public interface HasTooltip {}
    public interface HasLayoutOffset {}
    public interface HasUIContext {}
}