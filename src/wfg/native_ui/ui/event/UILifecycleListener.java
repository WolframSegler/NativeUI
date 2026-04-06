package wfg.native_ui.ui.event;

import com.fs.starfarer.api.ui.UIComponentAPI;

public interface UILifecycleListener {
    void panelAttached(UIComponentAPI comp, String compID);
    void panelDetached(UIComponentAPI comp, String compID);
    default void panelRefreshed(UIComponentAPI comp, String compID) {}
}