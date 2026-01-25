package wfg.native_ui.ui.core;

import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import wfg.native_ui.ui.components.UIComponentContainer;
import wfg.native_ui.ui.systems.UISystemContainer;

public interface UIElementAPI extends UIComponentAPI {

    /**
     * Returns the container holding optional components attached to this element.
     * Components may include background renderers, hover glows, audio hooks, or
     * other behavior/decorator objects.
     */
    UIComponentContainer getUIComponentContainer();

    /** Short-hand alias for {@link #getUIComponentContainer()} */
    UIComponentContainer comp();

    /** Returns the container holding systems attached to this element. */
    UISystemContainer getUISystemContainer();

    /** Short-hand alias for {@link #getUISystemContainer()} */
    UISystemContainer system();


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
    PositionAPI setParent(final UIPanelAPI parent);

    default void reportAttached() {};
    default void reportDetached() {};

    void bringToFront();
    void sendToBack();
}