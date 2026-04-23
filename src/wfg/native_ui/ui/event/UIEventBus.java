package wfg.native_ui.ui.event;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.ui.UIComponentAPI;

public final class UIEventBus {
    private UIEventBus() {}

    private static final List<UILifecycleListener> listeners = new ArrayList<>();

    public static final void addListener(UILifecycleListener l) {
        if (!listeners.contains(l)) listeners.add(l);
    }

    public static final void removeListener(UILifecycleListener l) {
        listeners.remove(l);
    }

    public static final void fireAttached(UIComponentAPI panel) {
        final String id = extractId(panel);
        for (UILifecycleListener l : listeners) {
            l.panelAttached(panel, id);
        }
    }

    public static final void fireDetached(UIComponentAPI panel) {
        final String id = extractId(panel);
        for (UILifecycleListener l : listeners) {
            l.panelDetached(panel, id);
        }
    }

    public static final void fireRefreshed(UIComponentAPI panel) {
        final String id = extractId(panel);
        for (UILifecycleListener l : listeners) {
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