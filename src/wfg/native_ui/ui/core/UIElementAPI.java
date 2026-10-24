package wfg.native_ui.ui.core;

import java.util.List;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.FaderUtil;

public interface UIElementAPI extends UIComponentAPI {

    /** Short-hand alias for {@link #getPosition()} */
    PositionAPI pos();
    float getX();
    float getY();
    float getCenterX();
    float getCenterY();
    float getWidth();
    float getHeight();

    void setPos(PositionAPI pos);
    void setWidth(float w);
    void setHeight(float h);
    PositionAPI setSize(float w, float h);

    void moveBy(float dx, float dy);
    void resizeBy(float dw, float dh);

    UIPanelAPI getParent();
    /** Does not set {@link PositionAPI} parent. */
    PositionAPI setParent(final UIPanelAPI parent);

    default void reportAttached() {};
    default void reportDetached() {};

    default void advanceImpl(float delta) {};
    default void renderImpl(float alpha) {};
    default void processInputImpl(List<InputEventAPI> events) {};

    void bringToFront();
    void sendToBack();
    void detach();

    /** adds position only. */
    PositionAPI addPos(UIComponentAPI comp);
    /** removes position only */
    void removePos(UIComponentAPI comp);

    FaderUtil getFader();

    /**
     * Wraps this element with a vanilla {@link CustomPanelAPI} and adds the wrapper panel to the parent.
     * Used to add a custom {@link UIComponentAPI} implementation to a vanilla {@link UIPanelAPI}.
     * Returns the position of this element.
     * <p>Not idempotent: Throws if the element is already wrapped or already attached to a parent.
     * Call {@link #detach} before re-wrapping.
     */
    PositionAPI wrap(UIPanelAPI parent);

    /**
     * The representation of this element in its parent's children list.
     * Returns {@code this} when not wrapped, or the wrapper panel when wrapped.
     */
    public UIComponentAPI getSelfInParent();
}