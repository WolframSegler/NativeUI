package wfg.native_ui.internal.ui.core;

import java.util.List;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

/**
 * Vanilla {@link UIComponentAPI} implementation cannot have a custom {@link UIPanelAPI} implementation as its parent.
 * This wrapper wraps a vanilla {@link UIComponentAPI} and passes down its traversal calls to the wrapped panel.
 * Any vanilla panel that gets attached to a NativeUI panel gets wrapped by this panel.
 * This panel is intended for this particular implementation of the core classes.
 */
final class UIVanillaWrapper extends UIElement {
    private final UIComponentAPI mWrapped;

    UIVanillaWrapper(UIComponentAPI wrapped) {
        super();

        mWrapped = wrapped;
        mPos = wrapped.getPosition();
        mPos.inBL(0f, 0f);
    };

    UIComponentAPI wrapped() { return mWrapped; }
    boolean isWrapped(UIComponentAPI comp) { return comp == mWrapped; }

    @Override @Deprecated
    public void setPos(PositionAPI pos) {
        throw new UnsupportedOperationException("UIVanillaWrapper manages its own position");
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
    }
}