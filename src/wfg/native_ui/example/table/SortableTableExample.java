package wfg.native_ui.example.table;

import static wfg.native_ui.util.Globals.settings;
import static wfg.native_ui.util.UIConstants.*;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.impl.codex.CodexDataV2;
import com.fs.starfarer.api.ui.Fonts;

import wfg.native_ui.internal.ui.core.UIContainer;
import wfg.native_ui.ui.core.UIBuildableAPI;
import wfg.native_ui.ui.dialog.DialogPanel;
import wfg.native_ui.ui.table.SortableTable;
import wfg.native_ui.ui.table.SortableTable.CellAlg;
import wfg.native_ui.ui.table.SortableTable.ColumnData;
import wfg.native_ui.ui.table.SortableTable.RowData;
import wfg.native_ui.ui.visual.AbstractSpriteElement.SpriteElement;
import wfg.native_ui.util.NumFormat;

/**
 * A commodity table that shows various stats about the commodity.
 */
public final class SortableTableExample extends UIContainer implements UIBuildableAPI {
    public static final int PANEL_W = 950;
    public static final int PANEL_H = 650;

    public SortableTableExample(float w, float h) {
        super(w, h);

        buildUI();
    }

    @Override
    public void buildUI() {
        clearChildren();

        final int tableStartY = 160;

        final SortableTable table = new SortableTable(
            PANEL_W - 20, PANEL_H - (tableStartY + 10),
            20, 60
        );

        table.addHeaders(
            new ColumnData("", 55, null, false, true,  false, 1),
            new ColumnData("Commodity", 150, null, true, true, true, 1),
            new ColumnData("Stored", 95, null, true, false, false, -1),
            new ColumnData("Consumed", 110, "Total continuous consumption by colony.", true, false, false, -1),
            new ColumnData("Base Prod", 130, "Theoretical local daily production, assuming no deficits or shortages.", true, false, false, -1),
            new ColumnData("Real Prod", 130, "Actual daily production after accounting for stored deficits.", true, false, false, -1),
            new ColumnData("Base Net",  135, "Net daily change in stockpile, ignoring imports or exports.", true, false, false, -1),
            new ColumnData("Real Net",  125, "This is a tooltip text.", true, false, false, -1)
        );

        for (CommoditySpecAPI com : settings.getAllCommoditySpecs().stream().filter(spec -> !spec.isNonEcon()).toList()) {

            final SpriteElement comIcon = new SpriteElement(
                42, 42, com.getIconName(), null, null
            );
            
            final long stored = Math.round(Math.random() * 8000d);
            final long consumption = Math.round(Math.random() * 500d);
            final long baseProd = Math.round(Math.random() * 700d);
            final long modifiedProd = Math.round(Math.random() * 600d);
            final long baseBalance = baseProd - consumption;
            final long realBalance = Math.round(Math.random() * 200d);

            final Color baseBlcColor = baseBalance < 0 ? 
                negative : baseBalance > 0 ?
                positive : text_color;

            final Color realBlcColor = realBalance < 0 ? 
                negative : realBalance > 0 ?
                positive : text_color;

            boolean failed = false;
            try {
                Global.getSettings().loadFont("graphics/fonts/insignia17LTaa.fnt");
            } catch (Exception e) {
                failed = true;
            }
            final String numFont = failed ? Fonts.DEFAULT_SMALL : "graphics/fonts/insignia17LTaa.fnt";

            final RowData data = new RowData()
                .cell(comIcon, CellAlg.MID, null, null)
                .cell(com.getName(), CellAlg.LEFT, com.getName(), base)
                .cell(settings.createLabel(NumFormat.engNotate(stored), numFont), CellAlg.LEFTOPAD, stored, null)
                .cell(settings.createLabel(NumFormat.engNotate(consumption), numFont), CellAlg.LEFTOPAD, consumption, negative)
                .cell(settings.createLabel(NumFormat.engNotate(baseProd), numFont), CellAlg.LEFTOPAD, baseProd, highlight)
                .cell(settings.createLabel(NumFormat.engNotate(modifiedProd), numFont), CellAlg.LEFTOPAD, modifiedProd, highlight)
                .cell(settings.createLabel(NumFormat.engNotate(baseBalance), numFont), CellAlg.LEFTOPAD, baseBalance, baseBlcColor)
                .cell(settings.createLabel(NumFormat.engNotate(realBalance), numFont), CellAlg.LEFTOPAD, realBalance, realBlcColor)
                .onClicked((row, isLeftClick) -> {
                    new DialogPanel(null, "Options menu", "confirm", "dismiss").show(0.3f, 0.3f);
                })
                .styler((row) -> {
                    row.tooltip.builder = (tp, exp) -> {
                        final String value = NumFormat.engNotate(700);
                        tp.addPara("Set as not exportable" + " - " + value, 0f, highlight, value);
                    };
                    row.tooltip.codexID = CodexDataV2.getCommodityEntryId(com.getId());
                });
                
            table.addRow(data);
        }

        add(table).inTL(10, tableStartY);

        /** sorting the table also calls buildUI() on the table */
        table.sortRows(2);
    }
}