package wfg.native_ui.ui.widget;

import java.util.function.Supplier;

import wfg.native_ui.ui.container.DockPanel;
import wfg.native_ui.ui.interaction.UIClickable;

/**
 * <p><strong>Example: </strong> {@link DockButtonExample}</p>
 */
public class DockClickable<T extends DockPanel> extends UIClickable<DockClickable<T>> {
    private T dock;
    private final Supplier<T> dockFactory;

    public DockClickable(int width, int height, Supplier<T> dockFactory) {
        super(width, height, null);
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
        dock.mRemoveWhenClosed = true;
        dock.mOnRemoved = d -> dock = null;
    }
}