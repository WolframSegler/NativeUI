package wfg.native_ui.example.table;

import static wfg.native_ui.util.Globals.settings;
import static wfg.native_ui.util.UIConstants.*;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.LabelAPI;

import wfg.native_ui.internal.ui.core.UIContainer;
import wfg.native_ui.ui.component.InteractionComp;
import wfg.native_ui.ui.component.NativeComponents;
import wfg.native_ui.ui.core.UIElementFlags;
import wfg.native_ui.ui.table.ListTable;
import wfg.native_ui.ui.table.WidgetAPI;

/**
 * Example usage of {@link ListTable}.
 * <p>Displays every faction in the current sector in a vertical, scrollable list.
 * Clicking a row selects it; clicking the selected row again deselects it.
 * Only one row can be selected at a time.
 */
public final class ListTableExample extends ListTable<FactionAPI, ListTableExample.FactionRow> {

    private static final int ROW_H = 40;

    public ListTableExample(int width, int height) {
        super(width, height, ROW_H, opad);
        mIsSelectionEnabled = true;
        mAddRowGap = true;
        buildUI();
    }

    @Override
    protected List<FactionAPI> getDataList() {
        return new ArrayList<>(Global.getSector().getAllFactions());
    }

    @Override
    protected FactionRow createWidget(FactionAPI item, int index) {
        return new FactionRow(item);
    }

    @Override
    protected void onWidgetClicked(FactionRow source) {
        if (mSelectedWidget == source) {
            mSelectedWidget = null;
            source.selected = false;
            source.buildUI();
            return;
        }

        final FactionRow prev = mSelectedWidget;
        mSelectedWidget = source;
        if (prev != null) {
            prev.selected = false;
            prev.buildUI();
        }
        source.selected = true;
        source.buildUI();
    }

    @Override
    protected String getEmptyMessage() {
        return "No factions in this sector";
    }

    /**
     * A single row displaying a faction's display name.
     * Highlights via text color when {@link #selected}.
     */
    public static final class FactionRow extends UIContainer
        implements WidgetAPI<FactionRow>, UIElementFlags.HasInteraction
    {
        private final FactionAPI faction;

        /** Selection state; managed by the owning {@link ListTableExample}. */
        public boolean selected = false;

        public FactionRow(FactionAPI faction) {
            this.faction = faction;
        }

        @Override
        public InteractionComp<FactionRow> getInteraction() {
            return comp().get(NativeComponents.INTERACTION);
        }

        @Override
        public void buildUI() {
            clearChildren();

            final LabelAPI lbl = settings.createLabel(
                faction.getDisplayName(), Fonts.ORBITRON_12);
            lbl.setColor(selected ? highlight : base);
            lbl.setAlignment(Alignment.LMID);
            lbl.getPosition()
                .setSize(getWidth() - opad * 2, getHeight())
                .inTL(opad, 0);
            add(lbl);
        }
    }
}