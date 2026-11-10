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
import com.fs.starfarer.api.ui.UIComponentAPI;

import wfg.native_ui.example.table.SortableTableExample;
import wfg.native_ui.internal.ui.core.UIContainer;
import wfg.native_ui.ui.ComponentFactory;
import wfg.native_ui.ui.component.AudioFeedbackComp;
import wfg.native_ui.ui.component.BackgroundComp;
import wfg.native_ui.ui.component.GlowComp;
import wfg.native_ui.ui.component.InteractionComp;
import wfg.native_ui.ui.component.NativeComponents;
import wfg.native_ui.ui.component.OutlineComp;
import wfg.native_ui.ui.component.TooltipComp;
import wfg.native_ui.ui.component.GlowComp.GlowType;
import wfg.native_ui.ui.component.TooltipComp.TooltipBuilder;
import wfg.native_ui.ui.core.UIBuildableAPI;
import wfg.native_ui.ui.core.UIContainerAPI;
import wfg.native_ui.ui.core.UIElementFlags.HasAudioFeedback;
import wfg.native_ui.ui.core.UIElementFlags.HasBackground;
import wfg.native_ui.ui.core.UIElementFlags.HasGlow;
import wfg.native_ui.ui.core.UIElementFlags.HasInteraction;
import wfg.native_ui.ui.core.UIElementFlags.HasOutline;
import wfg.native_ui.ui.core.UIElementFlags.HasTooltip;
import wfg.native_ui.ui.functional.ClickHandler;
import wfg.native_ui.ui.visual.AbstractSpriteElement;
import wfg.native_ui.ui.visual.AbstractSpriteElement.SpriteElement;
import wfg.native_ui.util.NativeUiUtils;
import wfg.native_ui.util.NativeUiUtils.AnchorType;

/**
 * SortableTable is a customizable, sortable UI table element designed to display
 * tabular data within a custom panel environment.
 * <p>
 * <b>This table supports:</b>
 * <ul>
 *   <li>Column headers with customizable width, tooltip, alignment, and sorting behavior.</li>
 *   <li>Adding rows with multiple typed cells (text, icons, UIComponent, numeric values).</li>
 *   <li>Sorting rows by any sortable column (e.g., numeric or text columns).</li>
 *   <li>Row selection events via a listener callback for interactive behavior.</li>
 * </ul>
 * <p>
 * <b>Accepted row cell types:</b>
 * <ul>
 *   <li>{@link Number} - displayed as a small-font label with optional color.</li>
 *   <li>{@link String} - displayed as a small-font label with optional color.</li>
 *   <li>{@link AbstractSpriteElement} - displayed as a sprite panel UI component.</li>
 *   <li>{@link UIComponentAPI} - displayed directly as a UI panel component.</li>
 *   <li>{@link LabelAPI} - displayed as a label UI component with optional color.</li>
 * </ul>
 * </p>
 * 
 * <p>This component supports tooltips both for headers and rows via {@link TooltipBuilder}.<p>
 * 
 * <p><strong>Example: </strong> {@link SortableTableExample}</p>
 */
public class SortableTable extends UIContainer implements UIBuildableAPI, HasOutline {
    private final static SpriteAPI SORT_ICON = settings.getSprite("ui", "sortIcon");

    private final List<ColumnManager> mColumns = new ArrayList<>();
    private final List<TableRow> mRows = new ArrayList<>();

    private final int HEADER_HEIGHT;
    private final int ROW_HEIGHT;

    public final OutlineComp outline = comp().get(NativeComponents.OUTLINE);

    public boolean showSortIcon = true;
    public boolean sortingEnabled = true;
    public int columnGap = 5;
    public int tooltipWidth = 400;

    private int mSelectedSortColumnIndex = -1;
    private boolean mAscending = true;
    private TableRow mPendingRow = null;
    private TableRow mSelectedRow;

    public TableRow getPendingRow() { return mPendingRow; }
    public TableRow getSelectedRow() { return mSelectedRow; }
    public List<ColumnManager> getColumns() { return mColumns; }
    public List<TableRow> getRows() { return mRows; }

    public SortableTable(float width, float height) {
        this(width, height, 20, 28);
    }

    public SortableTable(float width, float height, int headerHeight, int rowHeight) {
        super(width, height);
        HEADER_HEIGHT = headerHeight;
        ROW_HEIGHT = rowHeight;

        outline.color = NativeUiUtils.adjustBrightness(new Color(grid.getRed(), grid.getGreen(), grid.getBlue()), 0.3f);
        outline.enabled = false;
    }

    public void buildUI() {
        clearChildren();

        // create columns
        int cumulativeXOffset = 0;
        for (int i = 0; i < mColumns.size(); i++) {
            final ColumnManager column = mColumns.get(i);
            if (column.isMerged && !column.isParent) continue;

            final int width;
            final int padOffset;
            if (column.isMerged) {
                int mergedW = 0;
                int lastIndex = -1;

                for (int j = 0; j < mColumns.size(); j++) {
                    final ColumnManager col = mColumns.get(j);
                    if (col.isMerged && col.mergeSetID == column.mergeSetID) {
                        mergedW += col.width; lastIndex = j;
                    }
                }

                width = mergedW;
                padOffset = (lastIndex == mColumns.size() - 1) ? 0 : columnGap;

            } else {
                width = column.width;
                padOffset = (i == mColumns.size() - 1) ? 0 : columnGap;
            }

            final HeaderPanel panel = new HeaderPanel(width - padOffset,
                HEADER_HEIGHT, column, i
            );
            column.headerPanel = panel;

            add(panel).inTL(cumulativeXOffset, 0f);
            cumulativeXOffset += width;
        }


        if (!mRows.isEmpty()) {
            final float contentW = getWidth() - ComponentFactory.SCROLL_PANEL_WIDTH_PAD;
            final UIContainerAPI content = new UIContainer(contentW, 0f);
    
            int cumulativeYOffset = 0;
            for (TableRow row : mRows) {
                content.add(row).inTL(pad, cumulativeYOffset);
    
                cumulativeYOffset += ROW_HEIGHT;
            }
    
            content.setHeight(cumulativeYOffset);
            add(ComponentFactory.wrapWithScrollPanel(content, contentW, getHeight() - (HEADER_HEIGHT + pad)))
                .inTL(0f, HEADER_HEIGHT + pad);
        }
    }

    public class HeaderPanel extends UIContainer implements UIBuildableAPI, HasTooltip,
        HasOutline, HasBackground, HasGlow, HasAudioFeedback, HasInteraction
    {
        public final OutlineComp outline = comp().get(NativeComponents.OUTLINE);
        public final BackgroundComp bg = comp().get(NativeComponents.BACKGROUND);
        public final GlowComp glow = comp().get(NativeComponents.GLOW);
        public final AudioFeedbackComp audio = comp().get(NativeComponents.AUDIO_FEEDBACK);
        public final InteractionComp<HeaderPanel> interaction = comp().get(NativeComponents.INTERACTION);
        public final TooltipComp tooltip = comp().get(NativeComponents.TOOLTIP);

        protected final ColumnManager column;
        public int listIndex = -1;

        public HeaderPanel(int width, int height, ColumnManager column, int listIndex) {
            super(width, height);
            this.column = column;
            this.listIndex = listIndex;

            interaction.onClicked = (source, isLeftClick) -> {
                if (!sortingEnabled) return;
                SortableTable.this.sortRows(listIndex);
            };

            glow.color = glowHighlight;
            glow.offset.setOffset(1, 1, -2, -2);

            outline.color = NativeUiUtils.adjustBrightness(new Color(grid.getRed(), grid.getGreen(), grid.getBlue()), 0.3f);

            if (column.tooltip == null) {
                tooltip.enabled = false;
            } else {
                tooltip.width = tooltipWidth;
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
                    NativeUiUtils.anchorPanelWithBounds(tooltip, this, AnchorType.TopLeft, 0);
                };
            }

            buildUI();
        }

        @Override
        public void buildUI() {
            final LabelAPI lbl = settings.createLabel(column.title, Fonts.ORBITRON_12);
            lbl.autoSizeToWidth(getWidth());
            lbl.setColor(base);
            lbl.setAlignment(Alignment.MID);

            add(lbl).inLMid(0f);

            if (showSortIcon) {
                final SpriteElement sortIcon = new SpriteElement(
                    HEADER_HEIGHT - 3, HEADER_HEIGHT - 3,
                    SORT_ICON, base, null
                );
    
                add(sortIcon).inTR(2, 1);
            }
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

    public class TableRow extends UIContainer implements UIBuildableAPI,
        HasTooltip, HasGlow, HasOutline, HasAudioFeedback, HasInteraction
    {
        public final TooltipComp tooltip = comp().get(NativeComponents.TOOLTIP);
        public final GlowComp glow = comp().get(NativeComponents.GLOW);
        public final OutlineComp outline = comp().get(NativeComponents.OUTLINE);
        public final AudioFeedbackComp audio = comp().get(NativeComponents.AUDIO_FEEDBACK);
        public final InteractionComp<TableRow> interaction = comp().get(NativeComponents.INTERACTION);

        public Color textColor = base;
        public Object customData = null;
        
        protected final List<Object> mCellData = new ArrayList<>();
        protected final List<CellAlg> mCellAlignment = new ArrayList<>();
        protected final List<Object> mSortValues = new ArrayList<>();
        protected final List<Color> mUseColor = new ArrayList<>();

        public TableRow(float width, float height) {
            super(width, height);
            outline.enabled = false;

            glow.color = dark;
            glow.type = GlowType.UNDERLAY;
            glow.glowBrightness = 1.1f;

            interaction.onClicked = (source, isLeftClick) -> {
                SortableTable.this.selectRow(this);
                mSelectedRow = this;
            };
        }

        public void buildUI() {
            int cumulativeXOffset = 0;
            for (int i = 0; i < mCellData.size(); i++) {
                final Object cell = mCellData.get(i);
                final CellAlg alignment = mCellAlignment.get(i);
                final Color useColor = mUseColor.get(i);
                final float logicalW = getColumns().get(i).width;
                final float visualW = logicalW - (i == mCellData.size() - 1 ? 0 : columnGap);

                final UIComponentAPI comp;
                final float compWidth;

                if (cell instanceof Number) {
                    final LabelAPI label = settings.createLabel(String.valueOf(cell), Fonts.DEFAULT_SMALL);
                    comp = (UIComponentAPI) label;
                    compWidth = label.getPosition().getWidth();

                    label.setColor(useColor != null ? useColor : textColor);

                } else if (cell instanceof String txt) {
                    final LabelAPI label = settings.createLabel(txt, Fonts.DEFAULT_SMALL);
                    comp = (UIComponentAPI) label;
                    compWidth = label.getPosition().getWidth();

                    label.setColor(useColor != null ? useColor : textColor);

                } else if (cell instanceof SpriteAPI sprite) {
                    comp = new SpriteElement(sprite.getWidth(), sprite.getHeight(), sprite, null, null);
                    compWidth = comp.getPosition().getWidth();

                } else if (cell instanceof LabelAPI label) {
                    comp = (UIComponentAPI) label;
                    compWidth = label.getPosition().getWidth();

                    label.setColor(useColor != null ? useColor : textColor);

                } else if (cell instanceof UIComponentAPI panel) {
                    comp = panel;
                    compWidth = panel.getPosition().getWidth();

                } else {
                    throw new IllegalArgumentException("Unsupported cell type: " + cell.getClass());
                }

                final float xOffset = calcXOffset(cumulativeXOffset, visualW, compWidth, alignment);
                add(comp).inLMid(xOffset);

                cumulativeXOffset += logicalW;
            }
        }

        private float calcXOffset(float baseX, float colWidth, float compWidth, CellAlg alignment) {
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
            if (mSortValues.get(columnIndex) == null) {
                return mCellData.get(columnIndex);
            }

            return mSortValues.get(columnIndex);
        }

        public void setTextColor(Color color) {
            if (color != null) textColor = color;
        }
        
        public void addCell(Object cell, CellAlg alg, Object sort, Color textColor) {
            mCellData.add(cell);
            mCellAlignment.add(alg);
            mSortValues.add(sort);
            mUseColor.add(textColor);
        }
        public List<Object> getCellData() { return mCellData; }
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
        mColumns.clear();
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

            mColumns.add(new ColumnManager(
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
     * String, LabelAPI, {@link AbstractSpriteElement}, {@link UIComponentAPI}
     */
    public void addCell(Object cell, CellAlg alg, Object sortValue, Color textColor) {
        if (mPendingRow == null) {
            mPendingRow = new TableRow(getWidth() - pad*2, ROW_HEIGHT);
        }

        mPendingRow.addCell(cell, alg, sortValue, textColor);
    }

    /**
     * Uses the added cells to create a row and clears the {@link SortableTable#mPendingRow}.
     * The amount of cells must match the column amount.
     * @param customData stored by RowManager instance.
     * @param tp optional TooltipBuilder.
     * @param onRowClicked optional. overrides the default select behavior.
     * @param codexID optional.
     * @param textColor optional. Sets all the cells to that color.
     * @param highlight optional.
     */
    public void pushRow(Object customData, TooltipBuilder tp, ClickHandler<TableRow> onRowClicked,
        String codexID, Color textColor, Color highlight
    ) {
        if (mPendingRow == null || mPendingRow.mCellData.isEmpty()) {
            throw new IllegalStateException("Cannot push row: no cells have been added yet. "
                + "Call addCell() before pushRow().");

        } else if (mPendingRow.mCellData.size() != mColumns.size()) {
            throw new IllegalStateException("Cannot push row: cell count mismatch. "
                + "The number of cells must match the number of columns.");

        }
        mPendingRow.setTextColor(textColor);
        mPendingRow.customData = customData;
        if (codexID != null) mPendingRow.tooltip.codexID = codexID;
        if (highlight != null) mPendingRow.glow.color = highlight;
        if (onRowClicked != null) mPendingRow.interaction.onClicked = onRowClicked;
        if (tp != null) mPendingRow.tooltip.builder = tp;
        
        mPendingRow.buildUI();
        mRows.add(mPendingRow);

        mPendingRow = null;
    }

    public void sortRows(int index) {
        if (index == mSelectedSortColumnIndex) {
            mAscending = !mAscending;
        }

        sortRows(index, mAscending);
    }

    public void sortRows(int index, boolean ascending) {
        if (mRows.isEmpty()) return;

        this.mAscending = ascending;
        mSelectedSortColumnIndex = index;

        final Object value = mRows.get(0).getSortValue(index);

        if (value instanceof String) {
            Collections.sort(mRows, stringComparator);

        } else if (value instanceof Integer ||
                value instanceof Long ||
                value instanceof Float ||
                value instanceof Double) {
            Collections.sort(mRows, numberComparator);

        } else {
            throw new IllegalArgumentException(
                "Cannot sort rows: unsupported sort value type '" +
                value.getClass().getSimpleName() +
                "'. Supported types are String and Number."
            );
        }

        buildUI();
    }

    private final Comparator<TableRow> stringComparator = (a, b) -> {
        String valA = (String) a.getSortValue(mSelectedSortColumnIndex);
        String valB = (String) b.getSortValue(mSelectedSortColumnIndex);

        int cmp = valA.compareTo(valB);
        return mAscending ? cmp : -cmp;
    };

    private final Comparator<TableRow> numberComparator = (a, b) -> {
        Number valA = (Number) a.getSortValue(mSelectedSortColumnIndex);
        Number valB = (Number) b.getSortValue(mSelectedSortColumnIndex);

        int cmp = Double.compare(valA.doubleValue(), valB.doubleValue());
        return mAscending ? cmp : -cmp;
    };

    public TableRow selectLastRow() {
        TableRow target = null;
        for (TableRow row : mRows) {
            boolean result = row == mRows.get(mRows.size() - 1);
            if (result) row.glow.flash();
            row.glow.setHighlighted(result);

            if (result) {
                target = row;
            }
        }

        return target;
    }

    public void selectRow(TableRow selectedRow) {
        for (TableRow row : mRows) {
            row.glow.setHighlighted(row == selectedRow);
        }
    }

    /**
     * Determines how the panels inside a cell are positioned
     */
    public enum CellAlg {
        LEFT,
        MID,
        RIGHT,
        LEFTPAD,
        LEFTOPAD,
        RIGHTPAD,
        RIGHTOPAD
    }
}