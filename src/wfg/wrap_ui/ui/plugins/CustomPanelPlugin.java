package wfg.wrap_ui.ui.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.lwjgl.input.Mouse;

import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;

import wfg.wrap_ui.ui.UIState;
import wfg.wrap_ui.ui.UIState.State;
import wfg.wrap_ui.ui.panels.CustomPanel;
import wfg.wrap_ui.ui.panels.CustomPanel.AcceptsActionListener;
import wfg.wrap_ui.ui.panels.CustomPanel.HasAudioFeedback;
import wfg.wrap_ui.ui.panels.CustomPanel.HasBackground;
import wfg.wrap_ui.ui.panels.CustomPanel.HasFader;
import wfg.wrap_ui.ui.panels.CustomPanel.HasOutline;
import wfg.wrap_ui.ui.panels.CustomPanel.HasTooltip;
import wfg.wrap_ui.ui.systems.ActionListenerSystem;
import wfg.wrap_ui.ui.systems.AudioFeedbackSystem;
import wfg.wrap_ui.ui.systems.BackgroundSystem;
import wfg.wrap_ui.ui.systems.BaseSystem;
import wfg.wrap_ui.ui.systems.FaderSystem;
import wfg.wrap_ui.ui.systems.OutlineSystem;
import wfg.wrap_ui.ui.systems.TooltipSystem;

/**
 * The plugin serves as the central coordinator for its associated {@link CustomPanel} and systems.
 *
 * <p><strong>Design principles:</strong></p>
 * <ul>
 *   <li>The plugin manages and owns <em>plugin-specific</em> state — flags, toggles, configuration values —
 *       that control system behavior but are not part of the panel's intrinsic UI data.</li>
 *   <li>All <em>panel-specific</em> state (such as background color, dimensions, position) is stored exclusively
 *       in the panel itself. The plugin may query this data via {@link #getPanel()}.</li>
 *   <li>Systems never store their own global state; they query the plugin, which may in turn query the panel.
 *       This keeps systems stateless or minimally stateful, focused only on behavior.</li>
 *   <li>Recursive generics bind a plugin to a compatible panel type while permitting that plugin to be reused 
 *       across panel subclasses, preserving compile-time type safety without casts.</li>
 * </ul>
 *
 * <p>Example data flow:</p>
 * <pre>
 * // Plugin accessing panel data:
 * float width = m_panel.getPos().getWidth();
 *
 * // System accessing panel data via plugin:
 * var panel = getPlugin().getPanel();
 * Color bg = panel.getBgColor();
 * </pre>
 */
public abstract class CustomPanelPlugin<
    PanelType extends CustomPanel<? extends CustomPanelPlugin<?, ? extends PluginType>, PanelType>,
    PluginType extends CustomPanelPlugin<? extends CustomPanel<?, ?>, ? extends CustomPanelPlugin<?, ? extends PluginType>>
> implements CustomUIPanelPlugin {

    public static class InputSnapshot {
        public boolean LMBDownLastFrame = false;
        public boolean LMBUpLastFrame = false;
        public boolean hasLMBClickedBefore = false;

        public boolean RMBDownLastFrame = false;
        public boolean RMBUpLastFrame = false;
        public boolean hasRMBClickedBefore = false;

        public boolean hoveredLastFrame = false;
        public boolean hoverStarted = false;
        public boolean hoverEnded = false;
        public boolean isActive = false;

        public InputEventAPI mouseEvent = null;

        public void resetFrameFlags() {
            LMBDownLastFrame = false;
            LMBUpLastFrame = false;

            RMBDownLastFrame = false;
            RMBUpLastFrame = false;
        }
    }

    protected PanelType m_panel;

    public PanelType getPanel() {
        return m_panel;
    }

    public final List<BaseSystem<?, PanelType>> systems = new ArrayList<>();
    protected final InputSnapshot inputSnapshot = new InputSnapshot();
    
    protected boolean initialized = false;
    protected State targetUIState = State.NONE;
    protected boolean ignoreUIState = false;

    public int offsetX = 0;
    public int offsetY = 0;
    public int offsetW = 0;
    public int offsetH = 0;

    public final UUID UniqueID = UUID.randomUUID();

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void init(PanelType panel) {
        if (initialized) return;
        initialized = true;

        m_panel = panel;

        if (panel instanceof HasTooltip provider) {
            addSystem(new TooltipSystem(this, provider));
        }

        if (panel instanceof HasBackground) {
            addSystem(new BackgroundSystem(this));
        }

        if (panel instanceof HasAudioFeedback) {
            addSystem(new AudioFeedbackSystem(this));
        }

        if (panel instanceof HasOutline) {
            addSystem(new OutlineSystem(this));
        }

        if (panel instanceof HasFader) {
            addSystem(new FaderSystem(this));
        }

        if (panel instanceof AcceptsActionListener) {
            addSystem(new ActionListenerSystem(this));
        }
    }

    protected final <C extends BaseSystem<?, PanelType>> void addSystem(C system) {
        systems.add(system);
    }

    public void removeSystem(BaseSystem<?, PanelType> system) {
        systems.remove(system);
        system.onRemove(inputSnapshot);
    }

    public void setTargetUIState(State a) {
        targetUIState = a;
    }

    public void setIgnoreUIState(boolean a) {
        ignoreUIState = a;
    }

    public boolean isValidUIContext() {
        return UIState.is(targetUIState) || ignoreUIState; 
    }

    /**
     * Effects the background, the fader and the outline position
     */
    public void setOffsets(int x, int y, int width, int height) {
        offsetX = x;
        offsetY = y;
        offsetW = width;
        offsetH = height;
    }

    public void renderBelow(float alphaMult) {
        for (BaseSystem<?, PanelType> system : systems) {
            system.renderBelow(alphaMult, inputSnapshot);
        }
    }

    public void render(float alphaMult) {
        for (BaseSystem<?, PanelType> system : systems) {
            system.render(alphaMult, inputSnapshot);
        }
    }

    public void advance(float amount) {
        for (BaseSystem<?, PanelType> system : systems) {
            system.advance(amount, inputSnapshot);
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

                final PositionAPI pos = m_panel.getPos();
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
        for (BaseSystem<?, PanelType> system : systems) {
            system.processInput(events, inputSnapshot);
        }
    }

    public void buttonPressed(Object buttonId) {}

    public void positionChanged(PositionAPI position) {}
}