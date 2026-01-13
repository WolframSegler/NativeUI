package wfg.wrap_ui.ui.events;

import com.fs.starfarer.api.ui.UIPanelAPI;

public interface UILifecycleListener {
    void panelAttached(UIPanelAPI panel, String panelId);
    void panelDetached(UIPanelAPI panel, String panelId);
    default void panelRefreshed(UIPanelAPI panel, String panelId) {}
}