package wfg.native_ui.ui.widget;

import java.util.function.Supplier;

import wfg.native_ui.example.widget.DockButtonExample;
import wfg.native_ui.ui.container.DockPanel;
import wfg.native_ui.ui.interaction.UIClickable;

/**
 * <p><strong>Example: </strong> {@link DockButtonExample}</p>
 */
public class DockClickable<T extends DockPanel> extends UIClickable<DockClickable<T>> {
    private T mDock;
    private final Supplier<T> mDockFactory;

    public DockClickable(int width, int height, Supplier<T> dockFactory) {
        super(width, height, null);
        this.mDockFactory = dockFactory;
        onClicked = btn -> {
            if (mDock == null) createDock();
            if (mDock == null) return;

            if (mDock.isOpen()) mDock.close();
            else mDock.open(true);
        };
        
        setQuickMode(true);
    }

    private void createDock() {
        mDock = mDockFactory.get();
        mDock.mRemoveWhenClosed = true;
        mDock.mOnRemoved = d -> mDock = null;
    }
}