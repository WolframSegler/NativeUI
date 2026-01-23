package wfg.native_ui.internal.ui.plugins;

import java.awt.Color;
import java.util.List;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;

import wfg.native_ui.util.RenderUtils;

import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;

/**
 * Used for testing purposes only to visually see the position of a panel.
 */
public class BgPlugin implements CustomUIPanelPlugin {
    public UIComponentAPI comp;

    public void processInput(List<InputEventAPI> events) {}
    public void advance(float arg0) {}
    public void positionChanged(PositionAPI arg0) {}
    public void render(float arg0) {}
    public void renderBelow(float arg0) {
        final PositionAPI pos = comp.getPosition();

        RenderUtils.drawQuad(pos.getX(), pos.getY(), pos.getWidth(), pos.getHeight(),
        Color.red, 1, false);
    }
    public void buttonPressed(Object arg0) {}
}