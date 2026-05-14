package wfg.native_ui.ui.panel;

import static wfg.native_ui.util.Globals.settings;

import java.util.List;

import com.fs.graphics.util.Fader;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import rolflectionlib.util.RolfLectionUtil;
import wfg.native_ui.ui.ComponentFactory;
import wfg.native_ui.ui.component.UIComponentContainer;
import wfg.native_ui.ui.core.UIElementFlags;
import wfg.native_ui.ui.core.UIElementFlags.HasBackground;
import wfg.native_ui.ui.system.AudioFeedbackSystem;
import wfg.native_ui.ui.system.BackgroundSystem;
import wfg.native_ui.ui.system.BaseSystem;
import wfg.native_ui.ui.system.DebugBgSystem;
import wfg.native_ui.ui.system.HoverGlowSystem;
import wfg.native_ui.ui.system.InteractionSystem;
import wfg.native_ui.ui.system.NativeSystems;
import wfg.native_ui.ui.system.OutlineSystem;
import wfg.native_ui.ui.system.RawInputSystem;
import wfg.native_ui.ui.system.TooltipSystem;
import wfg.native_ui.ui.system.UISystemContainer;

/**
 * Represents the visual and layout container for a set of components.
 * 
 * <p><strong>Design principles:</strong></p>
 * <ul>
 *   <li>Systems operate primarily on components; direct panel access is secondary and
 *       reserved for panel-defined behavior.</li>
 *   <li>Capability interfaces (such as {@link HasBackground}) register systems that may add their required components.</li>
 * </ul>
 * 
 * <p><strong>Component access policy:</strong></p>
 * <ul>
 *   <li><b>Public components</b> expose supported customization points and may be read or modified
 *       directly by external code.</li>
 *   <li><b>Protected components</b> are internal implementation details and must only be accessed
 *       by this class or subclasses.</li>
 *   <li>If a panel provides a setter for a value that affects component state, that setter
 *       <b>must be used</b> instead of mutating the component directly.</li>
 * </ul>
 * <p>
 * This distinction makes supported extension points explicit while allowing systems to
 * freely read component data.
 * </p>
 */
public abstract class CustomPanel implements CustomUIPanelPlugin {
    public static final Object clearChildrenMethod;
    public static final Object getChildrenCopyMethod;
    public static final Object getChildrenNonCopyMethod;
    public static final Object getFaderMethod;
    public static final Object addToPositionMethod;
    public static final Object removeFromPositionMethod;
    public static final Object positionSetParentMethod;
    public static final Object isSlidOutMethod;

    static {
        final UIPanelAPI panelIns = settings.createCustom(0f, 0f, null);
        final Class<?> panelClazz = panelIns.getClass();
        final Class<?> posClazz = panelIns.getPosition().getClass();

        clearChildrenMethod = RolfLectionUtil.getMethod(
            "clearChildren", panelClazz);
        getChildrenCopyMethod = RolfLectionUtil.getMethod(
            "getChildrenCopy", panelClazz);
        getChildrenNonCopyMethod = RolfLectionUtil.getMethod(
            "getChildrenNonCopy", panelClazz);
        getFaderMethod = RolfLectionUtil.getMethod("getFader", panelClazz);
        isSlidOutMethod = RolfLectionUtil.getMethod("isSlidOut", panelClazz);
        addToPositionMethod = RolfLectionUtil.getMethod("add", posClazz, 1);
        removeFromPositionMethod = RolfLectionUtil.getMethod("remove", posClazz, 1);
        positionSetParentMethod = RolfLectionUtil.getMethod("setParent", posClazz, 1);
    }

    private UIComponentContainer compContainer = null;
    private UISystemContainer systemContainer = null;

    protected final UIPanelAPI m_parent;
    protected final UIPanelAPI m_panel;
    protected PositionAPI pos;

    public CustomPanel(UIPanelAPI parent, int width, int height) {
        m_parent = parent;

        m_panel = settings.createCustom(width, height, this);
        pos = m_panel.getPosition();

        initSystems();
    }

    protected void initSystems() {
        if (this instanceof UIElementFlags.HasInputSnapshot) {
            system().setIfNotPresent(NativeSystems.INPUT_SNAPSHOT, RawInputSystem.get(), this);
        }

        if (this instanceof UIElementFlags.HasBackground) {
            system().setIfNotPresent(NativeSystems.BACKGROUND, BackgroundSystem.get(), this);
        }

        if (this instanceof UIElementFlags.HasDebugBg) {
            system().setIfNotPresent(NativeSystems.DEBUG_BG, DebugBgSystem.get(), this);
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

    public UIPanelAPI getPanel() { return m_panel; }
    public PositionAPI getPos() { return pos; }
    public UIPanelAPI getParent() { return m_parent; }

    public PositionAPI add(LabelAPI a) {
        return add((UIComponentAPI) a);
    }

    public PositionAPI add(TooltipMakerAPI a) {
        return ComponentFactory.addTooltip(a, 0f, false, m_panel);
    }

    public PositionAPI add(UIComponentAPI a) {
        m_panel.addComponent(a);

        return a.getPosition();
    }

    public PositionAPI add(CustomPanel a) {
        m_panel.addComponent(a.getPanel());

        return a.getPos();
    }

    public void remove(LabelAPI a) {
        remove((UIComponentAPI) a);
    }

    public void remove(UIComponentAPI a) {
        m_panel.removeComponent(a);
    }

    public void remove(CustomPanel a) {
        m_panel.removeComponent(a.getPanel());
    }

    public final Fader getPanelFader() {
        return (Fader) RolfLectionUtil.invokeMethodDirectly(getFaderMethod, m_panel);
    }

    public final boolean isSlidOut() {
        return (boolean) RolfLectionUtil.invokeMethodDirectly(isSlidOutMethod, m_panel);
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

    public void clearChildren() {
        clearChildren(m_panel);
    }

    public List<UIComponentAPI> getChildrenNonCopy() {
        return getChildrenNonCopy(m_panel);
    }

    public List<UIComponentAPI> getChildrenCopy() {
        return getChildrenCopy(m_panel);
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

    @SuppressWarnings("unchecked")
    public static final List<UIComponentAPI> clearChildren(UIPanelAPI panel) {
        return (List<UIComponentAPI>) RolfLectionUtil.invokeMethodDirectly(clearChildrenMethod, panel);
    }

    @SuppressWarnings("unchecked")
    public static final List<UIComponentAPI> getChildrenNonCopy(UIPanelAPI panel) {
        return (List<UIComponentAPI>) RolfLectionUtil.invokeMethodDirectly(getChildrenNonCopyMethod, panel);
    }

    @SuppressWarnings("unchecked")
    public static final List<UIComponentAPI> getChildrenCopy(UIPanelAPI panel) {
        return (List<UIComponentAPI>) RolfLectionUtil.invokeMethodDirectly(getChildrenCopyMethod, panel);
    }
}