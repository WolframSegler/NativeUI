package wfg.native_ui.example.table;

import static wfg.native_ui.util.UIConstants.*;

import java.util.Collections;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.ids.Factions;

import wfg.native_ui.example.interaction.UIClickableExample;
import wfg.native_ui.example.interaction.UIClickableExample.PlannedOrder;
import wfg.native_ui.ui.table.GridTable;
import wfg.native_ui.util.NativeUiUtils;

public final class GridTableExample extends GridTable<PlannedOrder, UIClickableExample>  {

    public GridTableExample(int width, int height) {
        super(width, height, UIClickableExample.WIDTH, UIClickableExample.HEIGHT, opad*2);
        mUniformOuterGap = true;
        mJustifyGrid = true;
        mIsSelectionEnabled = true;
        buildUI();
    }

    public final void clearSelection() {
        for (UIClickableExample w : mWidgets) {
            w.selectionState = WidgetSelectionState.NONE;
            w.buildUI();
        }
        mSelectedWidget = null;
    }

    protected List<PlannedOrder> getDataList() {
        List<PlannedOrder> orders = getOrdersCopy();
        if (orders.size() > 500) {
            orders = orders.subList(0, 500);
        }
        return orders;
    }

    protected UIClickableExample createWidget(PlannedOrder item, int index) {
        return new UIClickableExample(item, index, Global.getSettings().getFactionSpec(Factions.PERSEAN));
    }

    protected void onWidgetClicked(UIClickableExample source) {
        switch (source.selectionState) {
        case NONE:
            if (NativeUiUtils.isShiftDown()) { // if shift down, remove immediately
                getOrders().remove(source.index);
                buildUI();
                break;
            }
            source.selectionState = WidgetSelectionState.REMOVE;
            source.buildUI();
            mSelectedWidget = source;
            for (UIClickableExample widget : mWidgets) {
                if (widget == source) continue;

                widget.selectionState = WidgetSelectionState.SWAP;
                widget.buildUI();
            }
            break;

        case REMOVE:
            getOrders().remove(source.index);
            buildUI();
            break;

        case SWAP:
            swapOrders(source.index, mSelectedWidget.index);
            NativeUiUtils.swapPositions(source, mSelectedWidget);

            clearSelection();
            break;
        }
    }

    protected String getEmptyMessage() {
        return "No orders";
    }

    // source of data
    private final List<PlannedOrder> getOrders() {
        return Collections.emptyList();
    }

    // source of data
    private final List<PlannedOrder> getOrdersCopy() {
        return Collections.emptyList();
    }

    private final void swapOrders(int index1, int index2) {
        // swap the the entries at both indexes
    }

    /** Used by the example */
    public enum WidgetSelectionState {
        NONE,
        REMOVE,
        SWAP
    }
}