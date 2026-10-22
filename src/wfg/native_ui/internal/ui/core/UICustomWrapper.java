package wfg.native_ui.internal.ui.core;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import wfg.native_ui.ui.MethodFields;

/**
 * Vanilla {@link UIPanelAPI} implementation cannot have a custom {@link UIComponentAPI} implementation as its child.
 * This wrapper wraps a custom {@link UIComponentAPI} and passes down its traversal calls to the wrapped panel.
 * Any NativeUI panel that gets attached to a vanilla panel gets wrapped by this panel.
 * This panel is intended for this particular implementation of the core classes.
 */
final class UICustomWrapper implements CustomUIPanelPlugin {
    private final CustomPanelAPI mPanel = Global.getSettings().createCustom(0f, 0f, this);
    private final UIComponentAPI mWrapped;

    UICustomWrapper(UIComponentAPI wrapped) {
        mWrapped = wrapped;
        MethodFields.setPos(mPanel, wrapped.getPosition());
        wrapped.getPosition().inBL(0f, 0f);
    }

    public PositionAPI pos() { return mPanel.getPosition(); }
    public CustomPanelAPI panel() { return mPanel; }
    public UIComponentAPI wrapped() { return mWrapped; } 

    public void advance(float delta) {
        mWrapped.advance(delta);
    }

    public void processInput(List<InputEventAPI> events) {
        mWrapped.processInput(events);
    }

    public void render(float alpha) {
        mWrapped.render(alpha);
    }

    public void positionChanged(PositionAPI pos) {}
    public void renderBelow(float alpha) {}
    public void buttonPressed(Object buttonID) {}
}