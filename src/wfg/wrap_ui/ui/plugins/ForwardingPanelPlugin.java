package wfg.wrap_ui.ui.plugins;

import java.util.List;

import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;

import wfg.wrap_ui.ui.panels.CustomPanel;

/**
 * Forwards all {@link CustomUIPanelPlugin} calls to its associated {@link CustomPanel}.
 */
public class ForwardingPanelPlugin implements CustomUIPanelPlugin {
    public CustomPanel<?> m_panel;

    public void renderBelow(float alpha) { m_panel.renderBelow(alpha); }
    public void render(float alpha) { m_panel.render(alpha); }
    public void advance(float delta) { m_panel.advance(delta); }
    public void processInput(List<InputEventAPI> events) { m_panel.processInput(events); }
    public void buttonPressed(Object buttonID) { m_panel.buttonPressed(buttonID); }
    public void positionChanged(PositionAPI position) { m_panel.positionChanged(position); }
}