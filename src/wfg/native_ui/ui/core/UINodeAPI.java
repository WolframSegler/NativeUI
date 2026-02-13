package wfg.native_ui.ui.core;

import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

public interface UINodeAPI extends UIComponentAPI {

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