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
    protected PositionAPI mPos = MethodFields.createPosition();
    protected UIPanelAPI mParent;

    /** 
     * This is the element itself it not wrapped.
     * If this element is added using {@link #wrap}, it is the wrapper.
     */
    protected UIComponentAPI mSelfInParent = this;

    protected float mOpacity = 1f;

    public UIElement(float width, float height) {
        this();
        mPos.setSize(width, height);
    }

    public UIElement() {}

    public PositionAPI getPosition() { return mPos; }
    public PositionAPI pos() { return mPos; }
    public float getX() { return mPos.getX(); }
    public float getY() { return mPos.getY(); }
    public float getCenterX() { return mPos.getCenterX(); }
    public float getCenterY() { return mPos.getCenterY(); }
    public float getWidth() { return mPos.getWidth(); }
    public float getHeight() { return mPos.getHeight(); }

    public void setPos(PositionAPI pos) {
        if (mSelfInParent != this) throw new UnsupportedOperationException("Cannot setPos on a wrapped element");
        mPos = pos;
    }
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
        if (mParent != null) mParent.bringComponentToTop(mSelfInParent);
    }
    public void sendToBack() {
        if (mParent != null) mParent.sendToBottom(mSelfInParent);
    }
    public void detach() {
        if (mParent != null) mParent.removeComponent(mSelfInParent);
        mParent = null;
        MethodFields.setPositionParent(mPos, null);
        mSelfInParent = this;
    }

    public PositionAPI addPos(UIComponentAPI comp) {
        return MethodFields.addPositionOnly(comp, mPos);
    }

    public void removePos(UIComponentAPI comp) {
        MethodFields.removePositionOnly(comp, mPos);
    }

    public FaderUtil getFader() {
        return mFader;
    }

    public PositionAPI wrap(UIPanelAPI parent) {
        if (mSelfInParent != this) throw new IllegalStateException("UIElement already wrapped");
        if (mParent != null) throw new IllegalStateException("UIElement already attached; detach first");
        if (!MethodFields.uiInternalCompClass.isInstance(parent)) {
            throw new IllegalArgumentException("wrap() requires a vanilla parent");
        }

        mSelfInParent = new UICustomWrapper(this).panel();
        parent.addComponent(mSelfInParent);
        return setParent(parent);
    }

    public UIComponentAPI getSelfInParent() {
        return mSelfInParent;
    }

    public void reportAttached() { UIEventBus.fireAttached(this); }
    public void reportDetached() { UIEventBus.fireDetached(this); }
}