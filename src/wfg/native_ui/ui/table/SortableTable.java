package wfg.native_ui.ui.table;

import static wfg.native_ui.util.Globals.settings;
import static wfg.native_ui.util.UIConstants.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import wfg.native_ui.ui.ComponentFactory;
import wfg.native_ui.ui.component.AudioFeedbackComp;
import wfg.native_ui.ui.component.BackgroundComp;
import wfg.native_ui.ui.component.HoverGlowComp;
import wfg.native_ui.ui.component.InteractionComp;
import wfg.native_ui.ui.component.NativeComponents;
import wfg.native_ui.ui.component.OutlineComp;
import wfg.native_ui.ui.component.TooltipComp;
import wfg.native_ui.ui.component.HoverGlowComp.GlowType;
import wfg.native_ui.ui.component.InteractionComp.ClickHandler;
import wfg.native_ui.ui.component.TooltipComp.TooltipBuilder;
import wfg.native_ui.ui.core.UIBuildableAPI;
import wfg.native_ui.ui.core.UIElementFlags.HasAudioFeedback;
import wfg.native_ui.ui.core.UIElementFlags.HasBackground;
import wfg.native_ui.ui.core.UIElementFlags.HasHoverGlow;
import wfg.native_ui.ui.core.UIElementFlags.HasInteraction;
import wfg.native_ui.ui.core.UIElementFlags.HasOutline;
import wfg.native_ui.ui.core.UIElementFlags.HasTooltip;
import wfg.native_ui.ui.panel.CustomPanel;
import wfg.native_ui.ui.visual.SpritePanel;
import wfg.native_ui.ui.visual.SpritePanel.Base;
import wfg.native_ui.util.NativeUiUtils;
import wfg.native_ui.util.NativeUiUtils.AnchorType;

/**
 * SortableTable is a customizable, sortable UI table component designed to display
 * tabular data within a custom panel environment. It extends {@link CustomPanel}
 * to integrate smoothly with the NativeUI framework.
 * <p>
 * <b>This table supports:</b>
 * <ul>
 *   <li>Dynamic column headers with customizable width, tooltip, alignment, and sorting behavior.</li>
 *   <li>Adding rows with multiple typed cells (text, icons, UIComponent, numeric values).</li>
 *   <li>Sorting rows by any sortable column (e.g., numeric or text columns).</li>
 *   <li>Row selection events via a listener callback for interactive behavior.</li>
 * </ul>
 * <p>
 * <b>Accepted row cell types:</b>
 * <ul>
 *   <li>{@link Number} — displayed as a small-font label with optional color.</li>
 *   <li>{@link String} — displayed as a small-font label with optional color.</li>
 *   <li>{@link SpritePanel} — displayed as a sprite panel UI component.</li>
 *   <li>{@link UIComponentAPI} — displayed directly as a UI panel component.</li>
 *   <li>{@link LabelAPI} — displayed as a label UI component with optional color.</li>
 * </ul>
 * </p>
 * <b>Typical usage example:</b>
 * <pre>{
 * SortableTable table = new SortableTable(...);
 *
 * // Setup headers with labels, widths, tooltips, merge flags and merge group
 * table.addHeaders(
 *     "", 40, null, true, false, 1, // Icon header
 *     "Colony", 200, "Colony name", true, true, 1,
 *     "Size", 100, "Colony size", false, false, -1,
 *     "Faction", 150, "Controlling faction", false, false, -1,
 *     // etc.
 * );
 * ...
 * 
 * // Add rows with multiple typed cells and associated tooltips
 * table.addCell(iconPanel, cellAlg.LEFT, null, null);
 * table.addCell(colonyName, cellAlg.LEFT, null, textColor);
 * table.addCell(colonySize, cellAlg.MID, null, textColor);
 * ...
 * 
 * // Register a listener for row selection
 * final CallbackRunnable<RowManager> rowSelectedRunnable = (row) -> {
 *      ...
 * };
 *
 * table.pushRow(customData, tpBuilder, onRowClicked, codexID, textColor, highlight);
 *
 * // Add table to UI panel and initialize it
 * parentPanel.addComponent(table.getPanel()).inTL(0, 0);
 * table.buildUI();
 *
 * // Enable sorting by a particular column index. Calls buildUI() internally
 * table.sortRows(columnIndex);
 * }</pre>
 * <p>
 * This component supports tooltips both for headers and rows via {@link TooltipBuilder}.
 * <p>
 */
public class SortableTable extends CustomPanel implements
    UIBuildableAPI, HasOutline
{
    private final static SpriteAPI sortIconPath = settings.getSprite("ui", "sortIcon");

    public final OutlineComp outline = comp().get(NativeComponents.OUTLINE);

    private final List<ColumnManager> m_columns = new ArrayList<>();
    private final List<RowPanel> m_rows = new ArrayList<>();

    private final int HEADER_HEIGHT;
    private final int ROW_HEIGHT;

    public boolean showSortIcon = true;
    public boolean sortingEnabled = true;
    public int columnGap = 5;

    private int selectedSortColumnIndex = -1;
    private boolean ascending = true;
    private RowPanel pendingRow = null;
    private RowPanel m_selectedRow;

    public RowPanel getPendingRow() { return pendingRow;}
    public RowPanel getSelectedRow() { return m_selectedRow;}
    public List<ColumnManager> getColumns() { return m_columns;}
    public List<RowPanel> getRows() { return m_rows;}

    public SortableTable(UIPanelAPI parent, int width, int height) {
        this(parent, width, height, 20, 28);
    }

    public SortableTable(UIPanelAPI parent, int width, int height, int headerHeight, int rowHeight) {
        super(parent, width, height);
        HEADER_HEIGHT = headerHeight;
        ROW_HEIGHT = rowHeight;

        outline.color = NativeUiUtils.adjustBrightness(new Color(grid.getRed(), grid.getGreen(), grid.getBlue()), 0.3f);
        outline.enabled = false;
    }

    public void buildUI() {
        clearChildren();

        // create columns
        int cumulativeXOffset = 0;
        for (int i = 0; i < m_columns.size(); i++) {
            final ColumnManager column = m_columns.get(i);
            if (column.isMerged && !column.isParent) continue;

            final int width;
            final int padOffset;
            if (column.isMerged) {
                int mergedW = 0;
                int lastIndex = -1;

                for (int j = 0; j < m_columns.size(); j++) {
                    final ColumnManager col = m_columns.get(j);
                    if (col.isMerged && col.mergeSetID == column.mergeSetID) {
                        mergedW += col.width; lastIndex = j;
                    }
                }

                width = mergedW;
                padOffset = (lastIndex == m_columns.size() - 1) ? 0 : columnGap;

            } else {
                width = column.width;
                padOffset = (i == m_columns.size() - 1) ? 0 : columnGap;
            }

            final HeaderPanel panel;
            if (column.tooltip == null) {
                panel = new HeaderPanel(m_panel, width - padOffset,
                    HEADER_HEIGHT, column, i
                );
            } else {
                panel = new HeaderPanelWithTooltip(m_panel, width - padOffset,
                    HEADER_HEIGHT, column, i
                );
            }
            column.headerPanel = panel;

            add(panel).inTL(cumulativeXOffset, 0f);
            cumulativeXOffset += width;
        }


        // create rows
        final TooltipMakerAPI tp = ComponentFactory.createTooltip(
            pos.getWidth(), true
        );

        int cumulativeYOffset = 0;
        for (RowPanel row : m_rows) {
            tp.addComponent(row.getPanel()).inTL(pad, cumulativeYOffset);

            cumulativeYOffset += ROW_HEIGHT;
        }

        tp.setHeightSoFar(cumulativeYOffset);
        ComponentFactory.addTooltip(tp, pos.getHeight() - (HEADER_HEIGHT + pad),
            true, m_panel
        ).inTL(0f, HEADER_HEIGHT + pad);
    }

    private class HeaderPanel extends CustomPanel implements UIBuildableAPI,
        HasOutline, HasBackground, HasHoverGlow, HasAudioFeedback, HasInteraction
    {
        public final OutlineComp outline = comp().get(NativeComponents.OUTLINE);
        public final BackgroundComp bg = comp().get(NativeComponents.BACKGROUND);
        public final HoverGlowComp glow = comp().get(NativeComponents.HOVER_GLOW);
        public final AudioFeedbackComp audio = comp().get(NativeComponents.AUDIO_FEEDBACK);
        public final InteractionComp<HeaderPanel> interaction = comp().get(NativeComponents.INTERACTION);

        protected final ColumnManager column;
        public int listIndex = -1;


        public HeaderPanel(UIPanelAPI parent, int width, int height, ColumnManager column, int listIndex) {
            super(parent, width, height);
            this.column = column;
            this.listIndex = listIndex;

            interaction.onClicked = (source, isLeftClick) -> {
                if (!sortingEnabled) return;
                SortableTable.this.sortRows(listIndex);
            };

            glow.color = glowHighlight;
            glow.offset.setOffset(1, 1, -2, -2);

            outline.color = NativeUiUtils.adjustBrightness(new Color(grid.getRed(), grid.getGreen(), grid.getBlue()), 0.3f);

            buildUI();
        }

        @Override
        public void buildUI() {
            final LabelAPI lbl = settings.createLabel(column.title, Fonts.ORBITRON_12);
            lbl.autoSizeToWidth(pos.getWidth());
            lbl.setColor(base);
            lbl.setAlignment(Alignment.MID);
            final float lblHeight = lbl.computeTextHeight(lbl.getText());

            add(lbl).inBL(0f, (pos.getHeight() - lblHeight) / 2f );

            if (showSortIcon) {
                final Base sortIcon = new Base(
                    m_panel, HEADER_HEIGHT - 3, HEADER_HEIGHT - 3,
                    sortIconPath, base, null
                );
    
                add(sortIcon).inTR(2, 1);
            }
        }
    }

    public class HeaderPanelWithTooltip extends HeaderPanel implements HasTooltip {
        public final TooltipComp tooltip = comp().get(NativeComponents.TOOLTIP);

        public HeaderPanelWithTooltip(UIPanelAPI parent, int width, int height,
            ColumnManager column, int listIndex) {
            super(parent, width, height, column, listIndex);

            tooltip.width = 300;
            if (column.tooltip instanceof TooltipBuilder builder) {
                tooltip.builder = builder;
            } else if (column.tooltip instanceof String text) {
                tooltip.builder = (tooltip, expanded) -> {
                    tooltip.addPara(text, pad);
                };
            } else {
                throw new IllegalArgumentException(
                    "Tooltip for header '" + column.title + "' has an illegal type: " +
                    column.tooltip.getClass()
                );
            }
            tooltip.positioner = (tooltip, expanded) -> {
                NativeUiUtils.anchorPanelWithBounds(tooltip, m_panel, AnchorType.TopLeft, 0);
            };
        }
    }

    public class ColumnManager {
        public String title;
        public int width;
        public Object tooltip;
        public HeaderPanel headerPanel = null;
        
        public final boolean isMerged;
        public final boolean isParent;
        public final int mergeSetID;

        public ColumnManager(String title, int width, Object tooltip,
            boolean isMerged, boolean isParent, int mergeSetID) {
            this.title = title;
            this.width = width;
            this.tooltip = tooltip;

            this.isMerged = isMerged;
            this.isParent = isParent;
            this.mergeSetID = mergeSetID;
        }
    }

    public class RowPanel extends CustomPanel implements UIBuildableAPI,
        HasTooltip, HasHoverGlow, HasOutline, HasAudioFeedback, HasInteraction
    {
        public final TooltipComp tooltip = comp().get(NativeComponents.TOOLTIP);
        public final HoverGlowComp glow = comp().get(NativeComponents.HOVER_GLOW);
        public final OutlineComp outline = comp().get(NativeComponents.OUTLINE);
        public final AudioFeedbackComp audio = comp().get(NativeComponents.AUDIO_FEEDBACK);
        public final InteractionComp<RowPanel> interaction = comp().get(NativeComponents.INTERACTION);

        public Color textColor = base;
        public Object customData = null;
        
        protected final List<Object> m_cellData = new ArrayList<>();
        protected final List<cellAlg> m_cellAlignment = new ArrayList<>();
        protected final List<Object> m_sortValues = new ArrayList<>();
        protected final List<Color> m_useColor = new ArrayList<>();

        public RowPanel(UIPanelAPI parent, int width, int height) {
            super(parent, width, height);
            outline.enabled = false;

            glow.color = dark;
            glow.type = GlowType.UNDERLAY;
            glow.overlayBrightness = 0.5f;

            interaction.onClicked = (source, isLeftClick) -> {
                SortableTable.this.selectRow(this);
                m_selectedRow = this;
            };
        }

        public void buildUI() {
            int cumulativeXOffset = 0;
            for (int i = 0; i < m_cellData.size(); i++) {
                final Object cell = m_cellData.get(i);
                final cellAlg alignment = m_cellAlignment.get(i);
                final Color useColor = m_useColor.get(i);
                final float logicalW = getColumns().get(i).width;
                final float visualW = logicalW - (i == m_cellData.size() - 1 ? 0 : columnGap);

                UIComponentAPI comp;
                float compWidth;
                float compHeight;

                if (cell instanceof Number) {
                    final LabelAPI label = settings.createLabel(String.valueOf(cell), Fonts.DEFAULT_SMALL);
                    comp = (UIComponentAPI) label;
                    compWidth = label.computeTextWidth(label.getText());
                    compHeight = label.computeTextHeight(label.getText());

                    label.setColor(useColor != null ? useColor : textColor);

                } else if (cell instanceof String txt) {
                    final LabelAPI label = settings.createLabel(txt, Fonts.DEFAULT_SMALL);
                    comp = (UIComponentAPI) label;
                    compWidth = label.computeTextWidth(label.getText());
                    compHeight = label.computeTextHeight(label.getText());

                    label.setColor(useColor != null ? useColor : textColor);

                } else if (cell instanceof SpritePanel sprite) {
                    comp = (UIComponentAPI) sprite.getPanel();
                    compWidth = sprite.getPos().getWidth();
                    compHeight = sprite.getPos().getHeight();

                } else if (cell instanceof UIComponentAPI panel) {
                    comp = panel;
                    compWidth = panel.getPosition().getWidth();
                    compHeight = panel.getPosition().getHeight();

                } else if (cell instanceof LabelAPI label) {
                    comp = (UIComponentAPI) label;
                    compWidth = label.computeTextWidth(label.getText());
                    compHeight = label.computeTextHeight(label.getText());

                    label.setColor(useColor != null ? useColor : textColor);

                } else {
                    throw new IllegalArgumentException("Unsupported cell type: " + cell.getClass());
                }

                final float xOffset = calcXOffset(cumulativeXOffset, visualW, compWidth, alignment);
                final float yOffset = (ROW_HEIGHT - compHeight) / 2f;
                add(comp).inBL(xOffset, yOffset);

                cumulativeXOffset += logicalW;
            }
        }

        private float calcXOffset(float baseX, float colWidth, float compWidth, cellAlg alignment) {
            switch (alignment) {
                case LEFT:
                    return baseX;
                case RIGHT:
                    return baseX + colWidth - compWidth;
                case LEFTPAD:
                    return baseX + pad;
                case LEFTOPAD:
                    return baseX + opad;
                case RIGHTPAD:
                    return baseX + colWidth - compWidth - pad;
                case RIGHTOPAD:
                    return baseX + colWidth - compWidth - opad;
                case MID:
                default:
                    return baseX + (colWidth / 2f) - (compWidth / 2f);
            }
        }

        public Object getSortValue(int columnIndex) {
            if (m_sortValues.get(columnIndex) == null) {
                return m_cellData.get(columnIndex);
            }

            return m_sortValues.get(columnIndex);
        }

        public void setTextColor(Color color) {
            if (color != null) textColor = color;
        }
        
        public void addCell(Object cell, cellAlg alg, Object sort, Color textColor) {
            m_cellData.add(cell);
            m_cellAlignment.add(alg);
            m_sortValues.add(sort);
            m_useColor.add(textColor);
        }
        public List<Object> getCellData() { return m_cellData; }
    }

    /**
     * Each set must contain the title of the header, its width, the text of the
     * tooltip or a {@link TooltipBuilder}, whether if it is merged, if it is the parent and the ID of the mergeSet.
     * A merged non-parent header will not display a tooltip.
     * <br></br> The expected input is {String, int, String, bool, bool, int}.
     * Or alternatively {String, int, {@link TooltipBuilder}, bool, bool, int}.
     * The tooltip and mergeSetID can be left empty:
     * {String, int, null, bool, bool, null}.
     */
    public void addHeaders(Object... headerDatas) {
        m_columns.clear();
        if (headerDatas.length % 6 != 0) {
            throw new IllegalArgumentException(
                "headerDatas must be sextuplets of {String, int, String, Bool, Bool, int}"
            );
        }
        for (int i = 0; i < headerDatas.length; i += 6) {
            Object titleObj = headerDatas[i];
            Object widthObj = headerDatas[i + 1];
            Object tooltipObj = headerDatas[i + 2];
            Object isMergedObj = headerDatas[i + 3];
            Object isParentObj = headerDatas[i + 4];
            Object mergeSetIdObj = headerDatas[i + 5];

            if (!(titleObj instanceof String)) {
                throw new IllegalArgumentException("Header title must be String.");
            }
            if (!(widthObj instanceof Number)) {
                throw new IllegalArgumentException("Header width must be int.");
            }
            if (tooltipObj != null && !(tooltipObj instanceof String || tooltipObj instanceof TooltipBuilder)) {
                throw new IllegalArgumentException("Tooltip text must be String, TooltipBuilder or null.");
            }
            if (!(isMergedObj instanceof Boolean)) {
                throw new IllegalArgumentException("isMerged must be Boolean.");
            }
            if (!(isParentObj instanceof Boolean)) {
                throw new IllegalArgumentException("isParent must be Boolean.");
            }
            if (mergeSetIdObj != null && !(mergeSetIdObj instanceof Number)) {
                throw new IllegalArgumentException("mergeSetID must be int or null.");
            }
            final int mergeSetID = mergeSetIdObj != null ? ((Number) mergeSetIdObj).intValue() : -1;

            m_columns.add(new ColumnManager(
                (String) titleObj,
                ((Number) widthObj).intValue(),
                (Object) tooltipObj,
                (Boolean) isMergedObj,
                (Boolean) isParentObj,
                mergeSetID
            ));
        }
    }

    /**
     * The call order of addCell must match the order of Columns.
     * Supports the following types:
     * String, LabelAPI, {@link SpritePanel}, {@link UIComponentAPI}
     */
    public void addCell(Object cell, cellAlg alg, Object sortValue, Color textColor) {
        if (pendingRow == null) {
            pendingRow = new RowPanel(
                m_panel,
                (int) pos.getWidth() - pad*2,
                ROW_HEIGHT
            );
        }

        pendingRow.addCell(cell, alg, sortValue, textColor);
    }

    /**
     * Uses the added cells to create a row and clears the {@link SortableTable#pendingRow}.
     * The amount of cells must match the column amount.
     * @param customData stored by RowManager instance.
     * @param tp optional TooltipBuilder.
     * @param onRowClicked optional. overrides the default select behavior.
     * @param codexID optional.
     * @param textColor optional. Sets all the cells to that color.
     * @param highlight optional.
     */
    public void pushRow(Object customData, TooltipBuilder tp, ClickHandler<RowPanel> onRowClicked,
        String codexID, Color textColor, Color highlight
    ) {
        if (pendingRow == null || pendingRow.m_cellData.isEmpty()) {
            throw new IllegalStateException("Cannot push row: no cells have been added yet. "
                + "Call addCell() before pushRow().");

        } else if (pendingRow.m_cellData.size() != m_columns.size()) {
            throw new IllegalStateException("Cannot push row: cell count mismatch. "
                + "The number of cells must match the number of columns.");

        }
        pendingRow.setTextColor(textColor);
        pendingRow.customData = customData;
        if (codexID != null) pendingRow.tooltip.codexID = codexID;
        if (highlight != null) pendingRow.glow.color = highlight;
        if (onRowClicked != null) pendingRow.interaction.onClicked = onRowClicked;
        if (tp != null) pendingRow.tooltip.builder = tp;
        
        pendingRow.buildUI();
        m_rows.add(pendingRow);

        pendingRow = null;
    }

    public void sortRows(int index) {
        if (index == selectedSortColumnIndex) {
            ascending = !ascending;
        }

        sortRows(index, ascending);
    }

    public void sortRows(int index, boolean ascending) {
        if (m_rows.isEmpty()) return;

        this.ascending = ascending;
        selectedSortColumnIndex = index;

        final Object value = m_rows.get(0).getSortValue(index);

        if (value instanceof String) {
            Collections.sort(m_rows, stringComparator);

        } else if (value instanceof Integer ||
                value instanceof Long ||
                value instanceof Float ||
                value instanceof Double) {
            Collections.sort(m_rows, numberComparator);

        } else {
            throw new IllegalArgumentException(
                "Cannot sort rows: unsupported sort value type '" +
                value.getClass().getSimpleName() +
                "'. Supported types are String and Number."
            );
        }

        buildUI();
    }

    private final Comparator<RowPanel> stringComparator = (a, b) -> {
        String valA = (String) a.getSortValue(selectedSortColumnIndex);
        String valB = (String) b.getSortValue(selectedSortColumnIndex);

        int cmp = valA.compareTo(valB);
        return ascending ? cmp : -cmp;
    };

    private final Comparator<RowPanel> numberComparator = (a, b) -> {
        Number valA = (Number) a.getSortValue(selectedSortColumnIndex);
        Number valB = (Number) b.getSortValue(selectedSortColumnIndex);

        int cmp = Double.compare(valA.doubleValue(), valB.doubleValue());
        return ascending ? cmp : -cmp;
    };

    public RowPanel selectLastRow() {
        RowPanel target = null;
        for (RowPanel row : m_rows) {
            boolean result = row == m_rows.get(m_rows.size() - 1);
            row.glow.persistent = result;

            if (result) {
                target = row;
            }
        }

        return target;
    }

    public void selectRow(RowPanel selectedRow) {
        for (RowPanel row : m_rows) {
            row.glow.persistent = row == selectedRow;
        }
    }

    /**
     * Determines how the panels inside a cell are positioned
     */
    public enum cellAlg {
        LEFT,
        MID,
        RIGHT,
        LEFTPAD,
        LEFTOPAD,
        RIGHTPAD,
        RIGHTOPAD
    }
}