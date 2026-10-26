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
     * Attaches this element to {@code parent}, wrapping it in a vanilla {@link CustomPanelAPI} if needed.
     *
     * <ul>
     *   <li>If {@code parent} is a {@link UIContainerAPI}, the element is added directly.</li>
     *   <li>If {@code parent} is a vanilla panel, the element is wrapped and added.</li>
     * </ul>
     *
     * <p>It enforces a single-parent invariant: detach the element before attaching it elsewhere.
     *
     * @return this element's {@link PositionAPI} for chaining calls.
     * @throws IllegalStateException if this element is already wrapped or already attached.
     * @throws IllegalArgumentException if {@code parent} is neither a {@link UIContainerAPI} nor a vanilla panel.
     */
    PositionAPI attachTo(UIPanelAPI parent);

    /**
     * The representation of this element in its parent's children list.
     * Returns {@code this} when not wrapped, or the wrapper panel when wrapped.
     */
    UIComponentAPI getSelfInParent();
}