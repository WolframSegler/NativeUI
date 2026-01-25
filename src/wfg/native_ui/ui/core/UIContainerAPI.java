package wfg.native_ui.ui.core;

import java.util.List;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

public interface UIContainerAPI extends UIElementAPI, UIPanelAPI {
    PositionAPI add(UIComponentAPI element);
    void remove(UIComponentAPI element);

    List<UIComponentAPI> getChildren();
    List<UIComponentAPI> getChildrenCopy();
    void clearChildren();

    void bringToTop(UIComponentAPI element);
    void bringToTopWithinItself(UIComponentAPI element);
    void sendToBottomWithinItself(UIComponentAPI element);

    /** returns the first result */
    <T extends UIComponentAPI> T getChild(Class<T> type);
    UIComponentAPI getChild(String panelId);

    default void advanceImpl(float delta) {};
    default void renderBelowImpl(float alpha) {};
    default void renderAboveImpl(float alpha) {};
    default void processInputImpl(List<InputEventAPI> events) {};
}