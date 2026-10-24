package wfg.native_ui.ui.core;

import java.util.List;

import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

public interface UIContainerAPI extends UIEntityAPI, UIPanelAPI {
    PositionAPI add(UIComponentAPI comp);
    PositionAPI add(TooltipMakerAPI tooltip);
    void remove(UIComponentAPI comp);

    List<UIComponentAPI> getChildren();
    List<UIComponentAPI> getChildrenCopy();
    void clearChildren();

    void bringToTop(UIComponentAPI comp);
    void bringToTopWithinItself(UIComponentAPI comp);
    void sendToBottomWithinItself(UIComponentAPI comp);

    /** returns the first result */
    <T extends UIComponentAPI> T getChild(Class<T> type);
    UIComponentAPI getChild(String panelId);

    default void renderBelowImpl(float alpha) {};
    default void renderAboveImpl(float alpha) {};

    @Deprecated
    PositionAPI add(LabelAPI comp); // TODO remove after update as LabelAPI will extend UIComponentAPI
    @Deprecated
    void remove(LabelAPI comp); // TODO remove after update as LabelAPI will extend UIComponentAPI
}