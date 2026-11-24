package wfg.native_ui.ui.table;

import static wfg.native_ui.util.Globals.settings;
import static wfg.native_ui.util.UIConstants.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;

import wfg.native_ui.example.table.SortableTableExample;
import wfg.native_ui.internal.ui.core.UIContainer;
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
import wfg.native_ui.ui.core.UIElementFlags.HasAudioFeedback;
import wfg.native_ui.ui.core.UIElementFlags.HasBackground;
import wfg.native_ui.ui.core.UIElementFlags.HasGlow;
import wfg.native_ui.ui.core.UIElementFlags.HasInteraction;
import wfg.native_ui.ui.core.UIElementFlags.HasOutline;
import wfg.native_ui.ui.core.UIElementFlags.HasTooltip;
import wfg.native_ui.ui.functional.CallbackRunnable;
import wfg.native_ui.ui.functional.ClickHandler;
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
 *   <li>Adding rows with multiple typed cells.</li>
 *   <li>Sorting rows by any sortable column.</li>
 *   <li>Row selection events via a listener callback for interactive behavior.</li>
 * </ul>
 * <p>
 * <b>Accepted row cell types:</b>
 * <ul>
 *   <li>{@link Number} - displayed as a small-font label with optional color.</li>
 *   <li>{@link String} - displayed as a small-font label with optional color.</li>
 *   <li>{@link SpriteAPI} - displayed as a sprite panel UI component.</li>
 *   <li>{@link UIComponentAPI} - displayed directly as a UI panel component.</li>
 *   <li>{@link LabelAPI} - displayed as a label UI component with optional color.</li>
 * </ul>
 * </p>
 * 
 * <p>This component supports tooltips both for headers and rows via {@link TooltipBuilder}.<p>
 * 
 * <p><strong>Example: </strong> {@link SortableTableExample}</p>
 */
public class SortableTable extends ListTable<SortableTable.RowData, SortableTable.TableRow> implements HasOutline {
    private final static SpriteAPI SORT_ICON = settings.getSprite("ui", "sortIcon");

    public final OutlineComp outline = comp().get(NativeComponents.OUTLINE);

    private final ArrayList<ColumnData> mColumns = new ArrayList<>();
    private final ArrayList<RowData> mRowData = new ArrayList<>();

    private final int mHeaderHeight;

    public boolean showSortIcon = true;
    public boolean sortingEnabled = true;
    public int tooltipWidth = 400;
    public int headerGap = 5;

    private ColumnLayout mLayout;
    private RowData mSelectedData;

    private int mSelectedSortColumnIndex = -1;
    private boolean mAscending = true;

    public SortableTable(float width, float height) {
        this(width, height, 20, 28);
    }

    public SortableTable(float width, float height, int headerHeight, int rowHeight) {
        super(width, height, rowHeight, pad, 0);
        mHeaderHeight = headerHeight;

        outline.color = NativeUiUtils.adjustBrightness(new Color(grid.getRed(), grid.getGreen(), grid.getBlue()), 0.3f);
        outline.enabled = false;

        mIsSelectionEnabled = true;
        mIsHighlightingEnabled = true;
    }

    @Override
    public void buildUI() {
        super.buildUI();
        syncSelectionVisuals();
    }

    @Override
    public void createUI(final List<RowData> items) {
        mLayout = new ColumnLayout(mColumns, getWidth(), headerGap, ColumnLayout.LayoutPolicy.STRETCH);
        
        for (int i = 0; i < mColumns.size(); i++) {
            if (!mLayout.isHeaderColumn(i)) continue;
            final ColumnData col = mColumns.get(i);
            final HeaderPanel panel = new HeaderPanel(mLayout.widthOfHeader(i), mHeaderHeight, col, i);
            add(panel).inTL(mLayout.xOfHeader(i), 0f);
        }

        super.createUI(items);
    }

    public RowData getSelectedData() { return mSelectedData; }
    public List<ColumnData> getColumns() { return Collections.unmodifiableList(mColumns); }
    public List<RowData> getRows() { return Collections.unmodifiableList(mRowData); }

    @Override protected List<RowData> getDataList() { return mRowData; }
    @Override protected String getEmptyMessage() { return ""; }
    @Override protected float getScrollContainerMarginTop() { return mHeaderHeight + 1; }
    @Override protected float computeHighlightWidth()  { return computeContentWidth(); }

    @Override
    protected TableRow createWidget(RowData data, int index) {
        return new TableRow(data, mLayout, computeContentWidth(), mWidgetH);
    }

    @Override
    protected void onWidgetClicked(TableRow source, boolean isLeftClick, boolean isKeyboard) {
        final RowData data = source.getData();
        if (data.onClicked != null) {
            data.onClicked.handle(source, isLeftClick);
        } else {
            selectRow(data);
        }
    }

    public void selectRow(RowData data) {
        mSelectedData = data;
        syncSelectionVisuals();
    }

    public void addRow(RowData data) {
        if (mColumns.isEmpty()) {
            throw new IllegalStateException("addRow() before addHeaders()");
        }
        if (data.cellCount() != mColumns.size()) {
            throw new IllegalStateException(
                "cell count " + data.cellCount() + " != column count " + mColumns.size());
        }
        mRowData.add(data);
    }

    public void clearRows() { mRowData.clear(); }

    public void sortRows(int index) {
        if (index == mSelectedSortColumnIndex) mAscending = !mAscending;
        sortRows(index, mAscending);
    }

    public void sortRows(int index, boolean ascending) {
        if (mRowData.isEmpty()) { buildUI(); return; }
        mAscending = ascending;
        mSelectedSortColumnIndex = index;

        final Object probe = mRowData.get(0).cellAt(index).getSortValue();
        if (probe instanceof String) Collections.sort(mRowData, stringComparator);
        else if (probe instanceof Number) Collections.sort(mRowData, numberComparator);
        else throw new IllegalArgumentException(
            "Cannot sort rows: unsupported sort value type '"
                + probe.getClass().getSimpleName() + "'. Supported: String, Number.");

        buildUI();
    }

    public void addHeaders(ColumnData... cols) {
        mColumns.clear();
        mRowData.clear();

        for (ColumnData col : cols) {
            sanitizeColumn(col);
            mColumns.add(col);
        }
        validateMergeGroups();
    }

    private void sanitizeColumn(ColumnData col) {
        if (col.width <= 0) throw new IllegalArgumentException("Column width must be positive");

        if (col.tooltip != null &&
            !(col.tooltip instanceof String) &&
            !(col.tooltip instanceof TooltipBuilder)
        ) {
            throw new IllegalArgumentException("Tooltip for '" + col.title + "' must be String, TooltipBuilder, or null");
        }

        if (col.isMerged) {
            if (col.mergeSetID < 0) throw new IllegalArgumentException("Merged column '" + col.title + "' requires a non-negative mergeSetID");
            if (!col.isParent && col.tooltip != null) {
                throw new IllegalArgumentException("Merged non-parent column '" + col.title + "' must not carry a tooltip");
            }
        } else {
            if (col.isParent) throw new IllegalArgumentException("Non-merged column '" + col.title + "' cannot be a merge parent");
            if (col.mergeSetID >= 0) throw new IllegalArgumentException("Non-merged column '" + col.title + "' must not set mergeSetID");
        }
    }

    private void validateMergeGroups() {
        final LinkedHashMap<Integer, ArrayList<ColumnData>> groups = new LinkedHashMap<>();
        for (ColumnData col : mColumns) {
            if (col.isMerged) groups.computeIfAbsent(col.mergeSetID, k -> new ArrayList<>()).add(col);
        }

        for (Map.Entry<Integer, ArrayList<ColumnData>> e : groups.entrySet()) {
            final int id = e.getKey();
            final ArrayList<ColumnData> group = e.getValue();

            final int parents = (int) group.stream().filter(c -> c.isParent).count();
            if (parents != 1) {
                throw new IllegalArgumentException("Merge group " + id + " must have exactly one parent; found " + parents);
            }

            final int firstIdx = mColumns.indexOf(group.get(0));
            final int lastIdx = mColumns.indexOf(group.get(group.size() - 1));
            if (lastIdx - firstIdx + 1 != group.size()) {
                throw new IllegalArgumentException("Merge group " + id + " members must be contiguous in the column list");
            }
        }
    }

    private void syncSelectionVisuals() {
        for (TableRow row : mWidgets) {
            row.glow.setHighlighted(row.getData() == mSelectedData);
        }
    }

    private final Comparator<RowData> stringComparator = (a, b) -> {
        final String x = (String) a.cellAt(mSelectedSortColumnIndex).getSortValue();
        final String y = (String) b.cellAt(mSelectedSortColumnIndex).getSortValue();
        final int cmp = x.compareTo(y);
        return mAscending ? cmp : -cmp;
    };

    private final Comparator<RowData> numberComparator = (a, b) -> {
        final Number x = (Number) a.cellAt(mSelectedSortColumnIndex).getSortValue();
        final Number y = (Number) b.cellAt(mSelectedSortColumnIndex).getSortValue();
        final int cmp = Double.compare(x.doubleValue(), y.doubleValue());
        return mAscending ? cmp : -cmp;
    };

    public class HeaderPanel extends UIContainer implements UIBuildableAPI, HasTooltip,
        HasOutline, HasBackground, HasGlow, HasAudioFeedback, HasInteraction
    {
        public final OutlineComp outline = comp().get(NativeComponents.OUTLINE);
        public final BackgroundComp bg = comp().get(NativeComponents.BACKGROUND);
        public final GlowComp glow = comp().get(NativeComponents.GLOW);
        public final AudioFeedbackComp audio = comp().get(NativeComponents.AUDIO_FEEDBACK);
        public final InteractionComp<HeaderPanel> interaction = comp().get(NativeComponents.INTERACTION);
        public final TooltipComp tooltip = comp().get(NativeComponents.TOOLTIP);

        protected final ColumnData column;
        public int listIndex = -1;

        public HeaderPanel(float width, float height, ColumnData column, int listIndex) {
            super(width, height);
            this.column = column;
            this.listIndex = listIndex;

            interaction.onClicked = (source, isLeftClick) -> {
                if (!sortingEnabled || !column.sortable) return;
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

            if (sortingEnabled && showSortIcon && column.sortable) {
                final SpriteElement sortIcon = new SpriteElement(
                    mHeaderHeight - 3, mHeaderHeight - 3,
                    SORT_ICON, base, null
                );
    
                add(sortIcon).inTR(2, 1);
            }
        }
    }

    public class TableRow extends UIContainer implements WidgetAPI<TableRow>,
        HasTooltip, HasGlow, HasOutline, HasAudioFeedback, HasInteraction
    {
        public final TooltipComp tooltip = comp().get(NativeComponents.TOOLTIP);
        public final GlowComp glow = comp().get(NativeComponents.GLOW);
        public final OutlineComp outline = comp().get(NativeComponents.OUTLINE);
        public final AudioFeedbackComp audio = comp().get(NativeComponents.AUDIO_FEEDBACK);
        public final InteractionComp<TableRow> interaction = comp().get(NativeComponents.INTERACTION);
        
        private final RowData mData;
        private final ColumnLayout mLayout;

        public TableRow(RowData data, ColumnLayout layout, float width, float height) {
            super(width, height);
            mData = data;
            mLayout = layout;

            outline.enabled = false;
            glow.color = dark;
            glow.type = GlowType.UNDERLAY;
            glow.glowBrightness = 1.1f;
        }

        @Override
        public void buildUI() {
            clearChildren();
            for (int i = 0; i < mData.cellCount(); i++) {
                final CellData cell = mData.cellAt(i);
                final Color color = cell.textColor != null ? cell.textColor : mData.textColor;

                final UIComponentAPI comp;
                final float compW;

                if (cell.value instanceof Number) {
                    LabelAPI l = settings.createLabel(String.valueOf(cell.value), Fonts.DEFAULT_SMALL);
                    l.setColor(color); comp = (UIComponentAPI) l; compW = l.getPosition().getWidth();
                } else if (cell.value instanceof String txt) {
                    LabelAPI l = settings.createLabel(txt, Fonts.DEFAULT_SMALL);
                    l.setColor(color); comp = (UIComponentAPI) l; compW = l.getPosition().getWidth();
                } else if (cell.value instanceof SpriteAPI sprite) {
                    comp = new SpriteElement(sprite.getWidth(), sprite.getHeight(), sprite, null, null);
                    compW = comp.getPosition().getWidth();
                } else if (cell.value instanceof LabelAPI l) {
                    l.setColor(color); comp = (UIComponentAPI) l; compW = l.getPosition().getWidth();
                } else if (cell.value instanceof UIComponentAPI panel) {
                    comp = panel; compW = panel.getPosition().getWidth();
                } else {
                    throw new IllegalArgumentException("Unsupported cell type: " + cell.value.getClass());
                }

                add(comp).inLMid(calcXOffset(mLayout.xOf(i), mLayout.wOf(i), compW, cell.alignment));
            }

            if (mData.styler != null) mData.styler.run(this);
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

        public RowData getData() { return mData; }
        public InteractionComp<TableRow> getInteraction() { return interaction; }
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

    public static final class ColumnData {
        public final String title;
        public final float width;
        public final Object tooltip;
        public final boolean sortable;
        public final boolean isMerged;
        public final boolean isParent;
        public final int mergeSetID;

        public ColumnData(String title, float width, Object tooltip,
            boolean sortable, boolean isMerged, boolean isParent, int mergeSetID
        ) {
            this.title = title;
            this.width = width;
            this.tooltip = tooltip;
            this.sortable = sortable;
            this.isMerged = isMerged;
            this.isParent = isParent;
            this.mergeSetID = mergeSetID;
        }
    }

    public static final class ColumnLayout {
        public enum LayoutPolicy {
            /** Scale declared widths to fill totalWidth. */
            STRETCH,
            /** First n-1 keeps declared widths, last absorbs slack. */
            LAST_COLUMN,
            /** Keeps declared widths, extands to the right. */
            LEFT_ALIGN
        }

        private final float[] xOf;
        private final float[] wOf;

        private final float mTotalWidth;
        private final ColumnData[] mCols;

        public ColumnLayout(List<ColumnData> cols, float totalWidth, int gap, LayoutPolicy policy) {
            final int n = cols.size();
            xOf = new float[n];
            wOf = new float[n];

            mTotalWidth = totalWidth;
            mCols = cols.toArray(new ColumnData[0]);

            float sum = 0;
            for (int i = 0; i < n; i++) {
                wOf[i] = cols.get(i).width;
                sum += wOf[i];
            }
            final float gapTotal = (n > 1) ? (n - 1) * (float) gap : 0f;
            final float slack = totalWidth - sum - gapTotal;

            switch (policy) {
                case STRETCH: {
                    if (n > 0) {
                        final float per = slack / n;
                        for (int i = 0; i < n; i++) wOf[i] += per;
                    }
                    break;
                }
                case LAST_COLUMN: {
                    if (n > 0) wOf[n - 1] += slack;
                    break;
                }
                default:
            }

            float x = 0f;
            for (int i = 0; i < n; i++) {
                xOf[i] = x;
                x += wOf[i];
                if (i < n - 1) x += gap;
            }
        }

        public float xOf(int col) { return xOf[col]; }
        public float wOf(int col) { return wOf[col]; }
        public float totalWidth() { return mTotalWidth; }

        public boolean isHeaderColumn(int colIndex) {
            final ColumnData c = mCols[colIndex];
            return !c.isMerged || c.isParent;
        }

        public int firstMemberOf(int coldIndex) {
            final ColumnData c = mCols[coldIndex];
            if (!c.isMerged) return coldIndex;
            for (int i = 0; i < mCols.length; i++) {
                if (mCols[i].isMerged && mCols[i].mergeSetID == c.mergeSetID) return i;
            }
            return coldIndex;
        }

        public int lastMemberOf(int colIndex) {
            final ColumnData c = mCols[colIndex];
            if (!c.isMerged) return colIndex;
            int last = colIndex;
            for (int i = 0; i < mCols.length; i++) {
                if (mCols[i].isMerged && mCols[i].mergeSetID == c.mergeSetID) last = i;
            }
            return last;
        }

        public float xOfHeader(int colIndex) {
            return xOf[firstMemberOf(colIndex)];
        }

        public float widthOfHeader(int colIndex) {
            final int first = firstMemberOf(colIndex);
            final int last = lastMemberOf(colIndex);
            return xOf[last] + wOf[last] - xOf[first];
        }
    }

    public static final class CellData {
        /** Number, String, SpriteAPI, LabelAPI, or UIComponentAPI. */
        public final Object value;
        public final CellAlg alignment;
        /** If null, {@link #value} is used as the sort key. */
        public final Object sortValue;
        /** Per-cell override; {@code null} falls back to {@link RowData#textColor}. */
        public final Color textColor;

        public CellData(Object value, CellAlg alignment, Object sortValue, Color textColor) {
            this.value = value;
            this.alignment = alignment;
            this.sortValue = sortValue;
            this.textColor = textColor;
        }

        public Object getSortValue() {
            return sortValue != null ? sortValue : value;
        }
    }

    public static final class RowData {
        private final ArrayList<CellData> cells = new ArrayList<>();

        /** Custom data for this {@link RowData}. */
        public Object customData;
        /** The default selection is used if {@code null}. */
        public ClickHandler<TableRow> onClicked;
        /** The default color if {@link CellData} specify one. */
        public Color textColor = base;

        /** Called when a new row is created for customization. */
        public CallbackRunnable<TableRow> styler;

        public RowData cell(Object value, CellAlg alignment, Object sortValue, Color cellColor) {
            cells.add(new CellData(value, alignment, sortValue, cellColor));
            return this;
        }

        public RowData customData(Object v) { customData = v; return this; }
        public RowData onClicked(ClickHandler<TableRow> h) { onClicked = h; return this; }
        public RowData textColor(Color c) { textColor = c; return this; }
        public RowData styler(CallbackRunnable<TableRow> s) { styler = s; return this; }

        public int cellCount() { return cells.size(); }
        public CellData cellAt(int i) { return cells.get(i); }
        public List<CellData> cells() { return Collections.unmodifiableList(cells); }
    }
}