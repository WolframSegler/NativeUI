package wfg.native_ui.ui.panels;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import rolflectionlib.util.RolfLectionUtil;
import wfg.native_ui.ui.ComponentFactory;
import wfg.native_ui.ui.components.UIComponentContainer;
import wfg.native_ui.ui.components.NativeComponents;
import wfg.native_ui.ui.components.UIContextComp;
import wfg.native_ui.ui.core.UIElementFlags;
import wfg.native_ui.ui.core.UIElementFlags.HasBackground;
import wfg.native_ui.ui.plugins.ForwardingPanelPlugin;
import wfg.native_ui.ui.systems.AudioFeedbackSystem;
import wfg.native_ui.ui.systems.BackgroundSystem;
import wfg.native_ui.ui.systems.BaseSystem;
import wfg.native_ui.ui.systems.HoverGlowSystem;
import wfg.native_ui.ui.systems.InteractionSystem;
import wfg.native_ui.ui.systems.NativeSystems;
import wfg.native_ui.ui.systems.OutlineSystem;
import wfg.native_ui.ui.systems.RawInputSystem;
import wfg.native_ui.ui.systems.TooltipSystem;
import wfg.native_ui.ui.systems.UISystemContainer;

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

    private UIComponentContainer compContainer = null;
    private UISystemContainer systemContainer = null;

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

    protected void initSystems() {
        if (this instanceof UIElementFlags.HasUIContext) {
            comp().setIfNotPresent(NativeComponents.UI_CONTEXT, new UIContextComp());
        }

        if (this instanceof UIElementFlags.HasInputSnapshot) {
            system().setIfNotPresent(NativeSystems.INPUT_SNAPSHOT, RawInputSystem.get(), this);
        }

        if (this instanceof UIElementFlags.HasBackground) {
            system().setIfNotPresent(NativeSystems.BACKGROUND, BackgroundSystem.get(), this);
        }

        if (this instanceof UIElementFlags.HasOutline) {
            system().setIfNotPresent(NativeSystems.OUTLINE, OutlineSystem.get(), this);
        }

        if (this instanceof UIElementFlags.HasHoverGlow) {
            system().setIfNotPresent(NativeSystems.HOVER_GLOW, HoverGlowSystem.get(), this);
        }

        if (this instanceof UIElementFlags.HasTooltip) {
            system().setIfNotPresent(NativeSystems.TOOLTIP, TooltipSystem.get(), this);
        }

        if (this instanceof UIElementFlags.HasAudioFeedback) {
            system().setIfNotPresent(NativeSystems.AUDIO_FEEDBACK, AudioFeedbackSystem.get(), this);
        }

        if (this instanceof UIElementFlags.HasInteraction) {
            system().setIfNotPresent(NativeSystems.INTERACTION, InteractionSystem.get(), this);
        }
    }

    public final UIComponentContainer getComponentContainer() { return comp(); }
    public final UIComponentContainer comp() {
        if (compContainer == null) compContainer = new UIComponentContainer();
        return compContainer;
    }

    public final UISystemContainer getPanelSystems() { return system(); }
    public final UISystemContainer system() {
        if (systemContainer == null) systemContainer = new UISystemContainer();
        return systemContainer;
    }

    public void render(float alpha) {
        for (BaseSystem system : system().getAll()) {
            system.render(this, alpha);
        }
    }

    public void renderBelow(float alpha) {
        for (BaseSystem system : system().getAll()) {
            system.renderBelow(this, alpha);
        }
    }

    public void advance(float delta) {
        for (BaseSystem system : system().getAll()) {
            system.advance(this, delta);
        }
    }

    public void processInput(List<InputEventAPI> events) {
        for (BaseSystem system : system().getAll()) {
            system.processInput(this, events);
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
}