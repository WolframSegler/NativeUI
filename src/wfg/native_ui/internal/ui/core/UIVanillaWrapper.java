package wfg.native_ui.internal.ui.core;

import java.util.List;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import wfg.native_ui.ui.MethodFields;

/**
 * Vanilla {@link UIComponentAPI} implementation cannot have a custom {@link UIPanelAPI} implementation as its parent.
 * This wrapper wraps a vanilla {@link UIComponentAPI} and passes down its traversal calls to the wrapped panel.
 * Any vanilla panel that gets attached to a NativeUI panel gets wrapped by this panel.
 * This panel is intended for this particular implementation of the core classes.
 */
final class UIVanillaWrapper extends UIElement {
    private final UIComponentAPI mWrapped;

    private float mSyncedW, mSyncedH;

    UIVanillaWrapper(UIComponentAPI wrapped) {
        super();

        mWrapped = wrapped;
        final PositionAPI compPos = wrapped.getPosition();
        MethodFields.addPositionOnly(wrapped, mPos);
        compPos.inBL(0f, 0f);

        mPos.setSize(compPos.getWidth(), compPos.getHeight());
        mSyncedW = compPos.getWidth();
        mSyncedH = compPos.getHeight();
    };

    UIComponentAPI wrapped() { return mWrapped; }
    boolean isWrapped(UIComponentAPI comp) { return comp == mWrapped; }

    /** Do not use for {@link UIVanillaWrapper} */
    @Override @Deprecated
    public void setPos(PositionAPI pos) {}
    @Override
    public void setWidth(float w) { 
        mSyncedW = w;
        mPos.setSize(w, mPos.getHeight());
        mWrapped.getPosition().setSize(w, mPos.getHeight());
    }
    @Override
    public void setHeight(float h) {
        mSyncedH = h;
        mPos.setSize(mPos.getWidth(), h);
        mWrapped.getPosition().setSize(mPos.getWidth(), h);
    }
    @Override
    public PositionAPI setSize(float w, float h) {
        mSyncedW = w;
        mSyncedH = h;
        mWrapped.getPosition().setSize(w, h);
        return mPos.setSize(w, h);
    }

    @Override
    public void resizeBy(float dw, float dh) {
        mSyncedW += dw;
        mSyncedH += dh;
        mPos.setSize(mPos.getWidth() + dw, mPos.getHeight() + dh);
        mWrapped.getPosition().setSize(mPos.getWidth() + dw, mPos.getHeight() + dh);
    }

    @Override
    public void renderImpl(float alpha) {
        mWrapped.render(alpha);
    }

    @Override
    public void processInputImpl(List<InputEventAPI> events) {
        mWrapped.processInput(events);
    }

    @Override
    public void advanceImpl(float delta) {
        mWrapped.advance(delta);
        reconcileSize();
    }

    @Override 
    public void reportDetached() {
        MethodFields.removePositionOnly(mWrapped, mPos);
        super.reportDetached();
    }

    private final void reconcileSize() {
        final float ww = mPos.getWidth();
        final float wh = mPos.getHeight();
        final float vw = mWrapped.getPosition().getWidth();
        final float vh = mWrapped.getPosition().getHeight();

        final boolean wChanged = ww != mSyncedW || wh != mSyncedH;
        final boolean vChanged = vw != mSyncedW || vh != mSyncedH;

        if (vChanged) {
            mPos.setSize(vw, vh);
            mSyncedW = vw;
            mSyncedH = vh;
        } else if (wChanged) {
            mWrapped.getPosition().setSize(ww, wh);
            mSyncedW = ww;
            mSyncedH = wh;
        }
    }
}