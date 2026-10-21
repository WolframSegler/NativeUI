package wfg.native_ui.internal.ui.core;

import static wfg.native_ui.util.Globals.settings;

import java.util.List;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.FaderUtil;

import wfg.native_ui.ui.MethodFields;
import wfg.native_ui.ui.core.UIElementAPI;
import wfg.native_ui.ui.event.UIEventBus;
import wfg.native_ui.util.Arithmetic;

public class UIElement implements UIElementAPI {
    static final float FADE_SPEED_MULT = settings.getFloat("uiFadeSpeedMult");

    protected FaderUtil mFader = new FaderUtil(1f, 0.05f, 0.25f);
    protected PositionAPI mPos;
    protected UIPanelAPI mParent;

    protected float mOpacity = 1f;

    public UIElement(float width, float height) {
        this();
        mPos.setSize(width, height);
    }

    public UIElement() {
        this(MethodFields.createPosition());
    }

    public UIElement(PositionAPI pos) {
        mPos = pos;
    }

    public PositionAPI getPosition() { return mPos; }
    public PositionAPI pos() { return mPos; }
    public float getX() { return mPos.getX(); }
    public float getY() { return mPos.getY(); }
    public float getCenterX() { return mPos.getCenterX(); }
    public float getCenterY() { return mPos.getCenterY(); }
    public float getWidth() { return mPos.getWidth(); }
    public float getHeight() { return mPos.getHeight(); }

    public void setPos(PositionAPI pos) { mPos = pos; }
    public void setWidth(float w) { mPos.setSize(w, mPos.getHeight()); }
    public void setHeight(float h) { mPos.setSize(mPos.getWidth(), h); }
    public PositionAPI setSize(float w, float h) {
        return mPos.setSize(w, h);
    }

    public void moveBy(float dx, float dy) {
        final float currX = mPos.getX();
        final float currY = mPos.getY();

        mPos.setXAlignOffset(0f);
        mPos.setYAlignOffset(0f);

        final float oldOffsetX = currX - mPos.getX();
        final float oldOffsetY = currY - mPos.getY();

        mPos.setXAlignOffset(oldOffsetX + dx);
        mPos.setYAlignOffset(oldOffsetY + dy);
    }

    public void resizeBy(float dw, float dh) {
        mPos.setSize(mPos.getWidth() + dw, mPos.getHeight() + dh);
    }

    public UIPanelAPI getParent() { return mParent; }
    public PositionAPI setParent(UIPanelAPI parent) {
        mParent = parent;
        MethodFields.setPositionParent(mPos, parent == null ? null : parent.getPosition());
        return mPos;
    }

    public final void render(float alpha) {
        alpha *= mOpacity;
        if (alpha <= 0f) return;

        renderImpl(alpha * mFader.getBrightness());
    }

    public final void processInput(List<InputEventAPI> events) {
        if (mFader.isFadedOut() || mOpacity <= 0f) return;
        
        processInputImpl(events);
    }

    public final void advance(float delta) {
        mFader.advance(delta * FADE_SPEED_MULT);

        advanceImpl(delta);
    }

    public void setOpacity(float opacity) { mOpacity = Arithmetic.clamp(opacity, 0f, 1f); }
    public float getOpacity() { return mOpacity; }


    public void bringToFront() {
        if (mParent != null) mParent.bringComponentToTop(this);
    }
    public void sendToBack() {
        if (mParent != null) mParent.sendToBottom(this);
    }
    public void detach() {
        if (mParent == null) return; 
        mParent.removeComponent(this);
    }

    public FaderUtil getFader() {
        return mFader;
    }

    public UIComponentAPI wrap() {
        return new UICustomWrapper(this).panel();
    }

    public void reportAttached() { UIEventBus.fireAttached(this); }
    public void reportDetached() { UIEventBus.fireDetached(this); }
}