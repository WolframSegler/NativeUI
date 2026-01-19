package wfg.wrap_ui.ui.panels;

import static wfg.wrap_ui.util.UIConstants.bgAlpha;
import static wfg.wrap_ui.util.UIConstants.dark;

import java.awt.Color;
import java.util.Optional;
import java.util.function.Supplier;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.ActionListenerDelegate;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.FaderUtil;

import rolflectionlib.util.RolfLectionUtil;
import wfg.wrap_ui.ui.plugins.CustomPanelPlugin;
import wfg.wrap_ui.ui.systems.ActionListenerSystem;
import wfg.wrap_ui.ui.systems.FaderSystem.Glow;
import wfg.wrap_ui.ui.systems.OutlineSystem.Outline;
import wfg.wrap_ui.ui.systems.TooltipSystem;

/**
 * Represents the visual and layout container for a set of components managed by a matching {@link CustomPanelPlugin}.
 *
 * <p><strong>Design principles:</strong></p>
 * <ul>
 *   <li>The panel is responsible for all <em>UI-specific</em> state — such as background color, position,
 *       dimensions, and any interface-specific properties (e.g. implementing {@link HasBackground}).</li>
 *   <li>The panel does not store or manage plugin-specific logic or toggles; those belong in the plugin.</li>
 *   <li>By implementing capability interfaces (like {@link HasBackground}), the panel exposes relevant data
 *       to both the plugin and systems in a type-safe way.</li>
 *   <li>Recursive bounds (with selective wildcards) ensure methods like <code>{@link #getPlugin()}</code> / <code>{@link #getPanel()}</code> resolve to the concrete types at compile time, while still allowing a single plugin implementation to be reused by multiple panel subclasses.</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * // Panel implementing {@link HasBackground}
 * public class MyPanel extends CustomPanel< MyPanelPlugin<MyPanel>, MyPanel, CustomPanelAPI> implements HasBackground {
 *     private final Color bgColor;
 *
 *     public Color getBgColor() { return bgColor; }
 * }
 * </pre>
 */
public abstract class CustomPanel<
    PluginType extends CustomPanelPlugin<? extends CustomPanel<?, ?>, PluginType>, 
    PanelType extends CustomPanel<PluginType, ? extends CustomPanel<?, ? extends PanelType>>
> {
    public static final Object clearChildrenMethod;
    public static final Object getChildrenCopyMethod;
    public static final Object getChildrenNonCopyMethod;
    public static final Object addToPositionMethod;
    public static final Object removeFromPositionMethod;
    public static final Object positionSetParentMethod;

    private static final Object pluginField;

    static {
        final CustomPanelAPI panelIns = Global.getSettings().createCustom(0, 0, null);
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
        pluginField = RolfLectionUtil.getAllFields(panelClazz).stream()
            .filter(f -> CustomUIPanelPlugin.class.isAssignableFrom(
                RolfLectionUtil.getFieldType(f)))
            .findFirst().get();
    }

    protected final UIPanelAPI m_parent;
    protected final CustomPanelAPI m_panel;
    protected final PluginType m_plugin;

    /**
     * Ownership and lifecycle rules for child panels:
     * <ul>
     *   <li>The child <b>MUST NOT</b> add itself to the parent.
     *       This prevents the child from being responsible for its own positioning,
     *       since each panel handles positioning its children separately.</li>
     *   <li>The parent <b>MUST NOT</b> call <code>{@link #createPanel()}</code>.
     *      This ensures that the child’s members are fully initialized before panel creation.</li>
     *   <li>The child is responsible for initializing the Plugin with <code>{@link #getPlugin()}</code>
     *      and calling init(this).</li>
     * </ul>
     */
    public CustomPanel(UIPanelAPI parent, int width, int height, PluginType plugin) {
        m_parent = parent;
        m_plugin = plugin;
        
        m_panel = Global.getSettings().createCustom(width, height, plugin);
    }

    public final CustomPanelAPI getPanel() {
        return m_panel;
    }

    public final PositionAPI getPos() {
        return m_panel.getPosition();
    }

    /**
     * Returns the parent panel cast to the expected type.
     * <p>
     * This cast is unchecked and does not guarantee type safety at compile time.
     * It is provided for convenience based on the assumption that the parent
     * is of the expected type (usually {@code CustomPanelAPI}).
     * <p>
     * Use with caution: if the actual parent type differs, a {@code ClassCastException}
     * may occur at runtime.
     */
    public final UIPanelAPI getParent() {
        return m_parent;
    }

    public final PluginType getPlugin() {
        return m_plugin;
    }

    public final void setPlugin(CustomUIPanelPlugin newPlugin) {
        RolfLectionUtil.setPrivateVariable(pluginField, m_panel, newPlugin);
    }

    public final PositionAPI add(LabelAPI a) {
        return add((UIComponentAPI) a);
    }

    public final PositionAPI add(TooltipMakerAPI a) {
        return m_panel.addUIElement(a);
    }

    public final PositionAPI add(UIComponentAPI a) {
        m_panel.addComponent(a);

        return (a).getPosition();
    }

    public final PositionAPI add(CustomPanel<?, ?> a) {
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
        final PositionAPI pos = comp.getPosition();
        RolfLectionUtil.invokeMethodDirectly(positionSetParentMethod, pos, getPos());
        RolfLectionUtil.invokeMethodDirectly(addToPositionMethod, getPos(), pos);
        return pos;
    }

    public PositionAPI removePositionOnly(UIComponentAPI comp) {
        final PositionAPI pos = comp.getPosition();
        RolfLectionUtil.invokeMethodDirectly(positionSetParentMethod, pos, (Object)null);
        RolfLectionUtil.invokeMethodDirectly(removeFromPositionMethod, getPos(), pos);
        return pos;
    }

    public final void clearChildren() {
        RolfLectionUtil.invokeMethodDirectly(clearChildrenMethod, m_panel);
    }

    public final void setSize(int width, int height) {
        m_panel.getPosition().setSize(width, height);
    }

    public final void setWidth(int width) {
        m_panel.getPosition().setSize(width, getPos().getHeight());
    }

    public final void setHeight(int height) {
        m_panel.getPosition().setSize(getPos().getWidth(), height);
    }

    /**
     * The method for populating the main panel. Can be left empty.
     */
    public abstract void createPanel();

    public static interface HasMarket {
        MarketAPI getMarket();
        default void setMarket(MarketAPI market) {}
    }

    public interface HasFaction {
        default FactionAPI getFaction() {
            return Global.getSector().getPlayerFaction();
        }
    }

    /**
     * Marks a panel as being able to accept and store a {@link HasActionListener}.
     * <p>
     * This interface is primarily intended for panels that work with the explicit
     * interaction methods defined in {@link HasActionListener}, allowing {@code ActionListenerSystem} 
     * to automatically invoke those callbacks.
     * </p>
     *
     * <p>
     * This design keeps {@code AcceptsActionListener} compatible with both {@link HasActionListener}
     * and {@link TooltipMakerAPI.ActionListenerDelegate} while encouraging use of the more explicit,
     * strongly-typed {@link HasActionListener} methods for clarity and composability.
     * </p>
     */
    public static interface AcceptsActionListener {
        default Optional<HasActionListener> getActionListener() {
            return Optional.empty();
        }
        default void setActionListener(HasActionListener listener) {}

        /**
         * Optional support for the vanilla Starsector ActionListenerDelegate.
         * This listener is not invoked automatically by the custom {@link ActionListenerSystem}.
         * If you want to use it, you must call actionPerformed() manually from your plugin.
         */
        default Optional<ActionListenerDelegate> getVanillaActionListener() {
            return Optional.empty();
        }
        default void setVanillaActionListener(ActionListenerDelegate listener) {}
    }

    /**
     * A strongly-typed, explicit alternative to {@link com.fs.starfarer.api.ui.TooltipMakerAPI.ActionListenerDelegate}.
     * <p>
     * While the built-in {@code ActionListenerDelegate} reports a single, catch-all {@code actionPerformed} event,
     * this listener clearly differentiates between interaction types.
     * </p>
     * 
     * <p>
     * Implement this interface in any {@link CustomPanel} (or compatible type) to handle specific user interactions.
     * The {@code source} parameter passed to each method is always the panel where the event originated.
     * </p>
     */
    public static interface HasActionListener {

        default boolean isListenerEnabled() {
            return true;
        }

        /**
         * Called once per frame while the cursor is over the panel.
         */
        default void onHover(CustomPanel<?, ?> source) {}

        /**
         * Called once when the cursor first enters the panel.
         */
        default void onHoverStarted(CustomPanel<?, ?> source) {}

        /**
         * Called once when the cursor leaves the panel.
         */
        default void onHoverEnded(CustomPanel<?, ?> source) {}

        default void onClicked(CustomPanel<?, ?> source, boolean isLeftClick) {}

        default void onShortcutPressed(CustomPanel<?, ?> source) {}

        /**
         * Use org.lwjgl.input.Keyboard. Default of 0 means no shortcut.
         */
        default int getShortcut() {
            return 0;
        } 
    }

    public static interface HasFader {

        /**
         * Indicates whether this panel controls its own {@code faderUtil} instance.
         * <p>
         * Some panels may instead synchronize their fading behavior with another panel's fader.
         * In such cases, this should return {@code false}.
         */
        default boolean isFaderOwner() {
            return true;
        }

        FaderUtil getFader();

        /**
         * Returns the type of glow this panel should use.
         *
         * <ul>
         *     <li>{@link Glow#OVERLAY} or {@link Glow#UNDERLAY}: supports polygon-shaped glow.</li>
         *     <li>{@link Glow#ADDITIVE}: works with convex/polygon shapes too; a sprite texture is optional.
         *         If a sprite is provided via {@link #getAdditiveSprite()}, it will be used for rendering,
         *         otherwise a colored quad/polygon will be drawn.</li>
         *     <li>{@link Glow#NONE}: no glow.</li>
         * </ul>
         */
        default Glow getGlowType() {
            return Glow.OVERLAY;
        }

        default boolean isPersistentGlow() {
            return false;
        }

        default float getOverlayBrightness() {
            return 0.25f;
        }

        default float getAdditiveBrightness() {
            return 0.6f;
        }

        default Color getGlowColor() {
            return Color.WHITE;
        }

        /**
         * Returns the sprite used for additive glow.
         *
         * <p>Providing a sprite will render the glow using the texture, otherwise the system will
         * draw a colored polygon/quad according to {@link #getFaderMaskVertices()} (if applicable). 
         */
        default Optional<SpriteAPI> getAdditiveSprite() {
            return Optional.empty();
        }

        /**
         * Returns the polygon vertices of the background shape in CCW order.
         * Returning null is a rectangle.
         */
        default float[] getFaderMaskVertices() {
            return null;
        }
    }

    public static interface HasOutline {

        default Outline getOutline() {
            return Outline.LINE;
        }

        default Color getOutlineColor() {
            return dark;
        }
    }

    public static interface HasAudioFeedback {
        default boolean isSoundActive() {
            return true;
        }

        default boolean isUseDisableSound() {
            return false;
        }

        default String getButtonPressedSound() {
            return "ui_button_pressed";
        }

        default String getMouseOverSound() {
            return "ui_button_mouseover";
        }

        default String getButtonPressedDisabledSound() {
            return "ui_button_disabled_pressed";
        }
    }

    public static interface HasBackground {
        default Color getBgColor() {
            return new Color(0, 0, 0, 255);
        }

        default boolean isBgEnabled() {
            return true;
        }

        default float getBgAlpha() {
            return bgAlpha;
        }
    }

    public static interface HasTooltip {

        /**
        * Return the parent panel of the tooltip.
        * Must return a non-null UIPanelAPI. Otherwise the tooltip will not be removed.
        * Never attach the tooltip to the codex. It WILL crash.
        */
        UIPanelAPI getTpParent();

        /**
        * Return the parent panel of the codex is, ideally the same as the tooltip.
        * Must return a non-null UIPanelAPI. Otherwise the codex will not be removed.
        */
        default Optional<UIPanelAPI> getCodexParent() {
            return Optional.empty();
        }

        /**
        * The {@link TooltipSystem} will call this.
        * Must create its own tooltip, attach it, position it and return it.
        * A new tooltip will be created instead of an update.
        * Therefore, conditional changes to the tooltip should happen during creation.
        */
        TooltipMakerAPI createAndAttachTp();

        /**
        * The {@link TooltipSystem} will call this.
        * Must create its own codex, attach it, position it and return it.
        */
        default Optional<TooltipMakerAPI> createAndAttachCodex() {
            return Optional.empty();
        }

        /**
         * The system uses this ID to open the codex.
         * Therefore it must be provided independent of {@code createCodex()} for codex behaviour.
         */ 
        default Optional<String> getCodexID() {
            return Optional.empty();
        }

        /**
         * Use this toggle to conditionally disable the tooltip.
         */
        default boolean isTooltipEnabled() {
            return true;
        }

        default float getTooltipDelay() {
            return 0.3f;
        }

        default boolean isExpanded() {
            return false;
        }

        default void setExpanded(boolean a) {}

        /**
         * Represents a tooltip creator. 
         * Used primarily to pass tooltip creation logic around as an object
         *
         * This class should be returned from {@link #createAndAttachTp()} when the real tooltip
         * is not yet available. The {@code factory} Supplier is used to supply the actual
         * TooltipMakerAPI instance later, enabling lazy/dynamic creation.
         *
         * The real power lies in the ability to assign the {@code factory} field a Supplier
         * from any scope, allowing flexible tooltip-building.
         *
         * For example, a table component can accept a factory from its user and call it
         * to create tooltips for headers or rows on demand, vastly improving flexibility.
         *
         * <p><b>Example usage:</b>
         * <pre>
         * public TooltipMakerAPI createAndAttachTp() {
         *     PendingTooltip pending = new PendingTooltip();
         *     pending.factory = () -> {
         *         // Create and return the real tooltip here
         *         TooltipMakerAPI tooltip = ComponentFactory.createTooltip(...);
         *         // Configure tooltip as needed
         *         ComponentFactory.addTooltip(...).inTL(...);
         *         return tooltip;
         *     };
         *     return pending.factory.get();
         * }
         * </pre>
         */
        public static class PendingTooltip<ParentType extends UIPanelAPI> {
            /**
             * Factory method to create the tooltip.
             * Must be set by subclasses or instances.
             */
            public Supplier<TooltipMakerAPI> factory;

            /**
             * Factory method to return the parent panel of the tooltip created by the factory.
             * Must return a non-null UIPanelAPI.
             * Classes that support PendingTooltip will use this factory.
             */
            public  Supplier<ParentType> parentSupplier;
        }
    }
}