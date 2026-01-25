package wfg.native_ui.ui.events;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.ui.UIComponentAPI;

public final class UIEventBus {
    private static final UIEventBus INSTANCE = new UIEventBus();

    public static UIEventBus getInstance() {
        return INSTANCE;
    }

    private final List<UILifecycleListener> listeners = new ArrayList<>();

    private UIEventBus() {}

    public final void addListener(UILifecycleListener l) {
        if (!listeners.contains(l)) listeners.add(l);
    }

    public final void removeListener(UILifecycleListener l) {
        listeners.remove(l);
    }

    public final void fireAttached(UIComponentAPI panel) {
        final String id = extractId(panel);
        for (UILifecycleListener l : listeners) {
            l.panelAttached(panel, id);
        }
    }

    public final void fireDetached(UIComponentAPI panel) {
        final String id = extractId(panel);
        for (UILifecycleListener l : listeners) {
            l.panelDetached(panel, id);
        }
    }

    public final void fireRefreshed(UIComponentAPI panel) {
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