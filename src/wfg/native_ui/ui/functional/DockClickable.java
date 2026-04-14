package wfg.native_ui.ui.functional;

import java.util.function.Supplier;

import com.fs.starfarer.api.ui.UIPanelAPI;

import wfg.native_ui.ui.container.DockPanel;

public class DockClickable<T extends DockPanel> extends UIClickable<DockClickable<T>> {
    private T dock;
    private final Supplier<T> dockFactory;

    public DockClickable(UIPanelAPI parent, int width, int height, Supplier<T> dockFactory) {
        super(parent, width, height, null);
        this.dockFactory = dockFactory;
        onClicked = btn -> {
            if (dock == null) createDock();
            if (dock == null) return;

            if (dock.isOpen()) dock.close();
            else dock.open(true);
        };
        
        setQuickMode(true);
    }

    private void createDock() {
        dock = dockFactory.get();
        dock.removeWhenClosed = true;
        dock.onRemoved = d -> dock = null;
    }
}