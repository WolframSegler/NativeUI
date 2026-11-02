package wfg.native_ui.ui.table;

import static wfg.native_ui.util.Globals.settings;
import static wfg.native_ui.util.UIConstants.*;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.ScrollPanelAPI;

import wfg.native_ui.example.table.GridTableExample;
import wfg.native_ui.internal.ui.core.UIContainer;
import wfg.native_ui.ui.ComponentFactory;
import wfg.native_ui.ui.core.UIBuildableAPI;
import wfg.native_ui.ui.core.UIContainerAPI;
import wfg.native_ui.util.Arithmetic;

/**
 * Arranges uniform-sized widgets in a responsive grid with scroll support.
 * Handles scroll position restoration, empty state, and optional widget selection.
 * 
 * <p><strong>Example: </strong> {@link GridTableExample}</p>
 *
 * @param <T> the data type for each grid cell.
 * @param <W> the widget type that displays a {@link T}.
 * 
 */
public abstract class GridTable<T, W extends WidgetAPI<W>> extends UIContainer implements UIBuildableAPI {
    
    /** Gap between widgets, both horizontally and vertically. */
    protected final int mGap;
    protected final int mWidgetW;
    protected final int mWidgetH;

    /** List of currently displayed widgets. */
    protected final ArrayList<W> mWidgets = new ArrayList<>();
    /** Currently selected widget, if selection is enabled. */
    protected W mSelectedWidget = null;

    /** Scrollable container built via {@link ComponentFactory}. */
    protected ScrollPanelAPI mScrollContainer;
    /** The content container that holds the {@link WidgetAPI}'s. */
    protected UIContainerAPI mContent;
    /** Last known scroll offset, restored on rebuild. */
    protected float mScrollOffset = 0f;
    /** Determines whether widget selection is enabled. */
    protected boolean mIsSelectionEnabled = false;
    /** The gap value is used for outer margins (between widgets and panel edges). */
    protected boolean mUniformOuterGap = true;
    /**
     * Leftover horizontal space is distributed evenly as additional gap
     * between widgets, making the grid to span the full width while keeping
     * the outer margin equal to the base gap. If false, widgets are left‑aligned.
     */
    protected boolean mJustifyGrid = false;

    public GridTable(float width, float height, int widgetW, int widgetH) {
        this(width, height, widgetW, widgetH, hpad);
    }

    public GridTable(float width, float height, int widgetW, int widgetH, int gap) {
        super(width, height);
        mWidgetW = widgetW;
        mWidgetH = widgetH;
        mGap = gap;
    }

    /** @return the list of data items to display. */
    protected abstract List<T> getDataList();

    /**
     * Create a widget for the given data item.
     * @param item the data object
     * @param index its position in the list
     * @return a fully constructed widget
     */
    protected abstract W createWidget(T item, int index);

    protected abstract void onWidgetClicked(W source);

    /**
     * @return the message to display when the data list is empty.
     */
    protected abstract String getEmptyMessage();

    @Override
    public void buildUI() {
        clearChildren();
        mWidgets.clear();
        mSelectedWidget = null;

        final List<T> items = getDataList();
        if (items.isEmpty()) {
            final LabelAPI empty = settings.createLabel(getEmptyMessage(), Fonts.DEFAULT_SMALL);
            empty.setColor(gray);
            add(empty).inMid();
            return;
        }

        if (mScrollContainer != null) mScrollOffset = mScrollContainer.getYOffset();
        mContent = new UIContainer(computeContentContainerWidth(), 0f);

        final float margin = computeMargin();
        final int cols = computeColCount();
        final float verGap = computeVerticalGap();

        for (int i = 0; i < items.size(); i++) {
            final T item = items.get(i);
            final W widget = createWidget(item, i);
            mWidgets.add(widget);

            if (mIsSelectionEnabled) {
                widget.getInteraction().onClicked = (w, isLeft) -> onWidgetClicked(w);
            }

            final int row = i / cols;
            final int col = i % cols;

            final float x = margin + col * (mWidgetW + verGap);
            final float y = margin + row * (mWidgetH + mGap);

            mContent.add(widget).inTL(x, y);
        }

        final int rows = (items.size() + cols - 1) / cols;
        final float contentHeight = margin + rows * (mWidgetH + mGap);
        mContent.setHeight(contentHeight);

        final float visibleHeight = getHeight();
        mScrollContainer = ComponentFactory.wrapWithScrollPanel(mContent, computeContentContainerWidth(), visibleHeight);
        add(mScrollContainer).inTL(0f, 0f);

        final float maxScroll = Math.max(0f, contentHeight - visibleHeight);
        mScrollContainer.setYOffset(Arithmetic.clamp(mScrollOffset, 0f, maxScroll));
    }

    public final void scrollToBottom() {
        if (mScrollContainer == null) return;
        final float maxScroll = Math.max(0f, mContent.getHeight() - getHeight());
        mScrollContainer.setYOffset(maxScroll);
    }

    protected  final int computeMargin() {
        return mUniformOuterGap ? mGap : 0;
    }

    protected  final float computeContentContainerWidth() {
        return getWidth() - ComponentFactory.SCROLL_PANEL_WIDTH_PAD;
    }

    protected  final float computeContentWidth() {
        return computeContentContainerWidth() - (2 * computeMargin());
    }

    protected  final int computeColCount() {
        final float contentW = computeContentWidth();
        return Math.max(1, (int) ((contentW + mGap) / (mWidgetW + mGap)));
    }

    protected  final float computeVerticalGap() {
        final int cols = computeColCount();
        if (!mJustifyGrid || cols <= 1) return mGap;


        final float contentW = computeContentWidth();
        final float baseTotalW = cols * mWidgetW + (cols - 1) * mGap;
        final float extraSpace = contentW - baseTotalW;

        return mGap + extraSpace / (cols - 1);
    }
}