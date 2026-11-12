package wfg.native_ui.ui.event;

import java.util.ArrayList;

import com.fs.starfarer.api.ui.UIComponentAPI;

public final class UIEventBus {
    private UIEventBus() {}

    private static final ArrayList<UILifecycleListener> sListeners = new ArrayList<>();

    public static final void addListener(UILifecycleListener l) {
        if (!sListeners.contains(l)) sListeners.add(l);
    }

    public static final void removeListener(UILifecycleListener l) {
        sListeners.remove(l);
    }

    public static final void fireAttached(UIComponentAPI panel) {
        final String id = extractId(panel);
        for (UILifecycleListener l : sListeners) {
            l.panelAttached(panel, id);
        }
    }

    public static final void fireDetached(UIComponentAPI panel) {
        final String id = extractId(panel);
        for (UILifecycleListener l : sListeners) {
            l.panelDetached(panel, id);
        }
    }

    public static final void fireRefreshed(UIComponentAPI panel) {
        final String id = extractId(panel);
        for (UILifecycleListener l : sListeners) {
            l.panelRefreshed(panel, id);
        }
    }

    private static final String extractId(UIComponentAPI panel) {
        if (panel instanceof IdentifiedPanel ip) {
            return ip.getPanelId();
        }
        return null;
    }
}