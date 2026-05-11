package wfg.native_ui.internal.ui.core;

import java.util.List;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.FaderUtil;

import wfg.native_ui.ui.core.UIElementAPI;
import wfg.native_ui.ui.event.UIEventBus;

public class UIElement implements UIElementAPI {
    protected PositionAPI pos;
    protected UIPanelAPI parent;
    protected FaderUtil fader;

    public UIElement(PositionAPI initialPos) {
        pos = initialPos;
    }

    public PositionAPI getPosition() { return pos; }
    public PositionAPI pos() { return pos; }
    public float getX() { return pos.getX(); }
    public float getY() { return pos.getY(); }
    public float getCenterX() { return pos.getCenterX(); }
    public float getCenterY() { return pos.getCenterY(); }
    public float getWidth() { return pos.getWidth(); }
    public float getHeight() { return pos.getHeight(); }

    public void setPos(PositionAPI pos) { this.pos = pos; }
    public void setWidth(float w) { pos.setSize(w, pos.getHeight()); }
    public void setHeight(float h) { pos.setSize(pos.getWidth(), h); }
    public PositionAPI setSize(float w, float h) {
        return pos.setSize(w, h);
    }

    public void moveBy(float dx, float dy) {
        final float currX = pos.getX();
        final float currY = pos.getY();

        pos.setXAlignOffset(0f);
        pos.setYAlignOffset(0f);

        final float offsetX = pos.getX() - currX;
        final float offsetY = pos.getY() - currY;

        pos.setXAlignOffset(offsetX + dx);
        pos.setYAlignOffset(offsetY + dy);
    }

    public void resizeBy(float dw, float dh) {
        setWidth(pos.getWidth() + dw);
        setHeight(pos.getHeight() + dh);
    }

    public UIPanelAPI getParent() { return parent; }
    public PositionAPI setParent(UIPanelAPI parent) {
        this.parent = parent;
        // pos.setParent(parent.getPosition());
        return pos;
    }

    public void render(float alpha) {}
    public void processInput(List<InputEventAPI> events) {}
    public void advance(float delta) {}

    public void setOpacity(float opacity) { fader.setBrightness(opacity); }
    public float getOpacity() { return fader.getBrightness(); }


    public void bringToFront() {
        if (parent != null) parent.bringComponentToTop(this);
    }
    public void sendToBack() {
        if (parent != null) parent.sendToBottom(this);
    }
    public void detach() {
        parent.removeComponent(this);
        reportDetached();
    }

    public void reportAttached() { UIEventBus.fireAttached(this); }
    public void reportDetached() { UIEventBus.fireDetached(this); }
}