package wfg.native_ui.ui.table;

import static wfg.native_ui.util.Globals.settings;
import static wfg.native_ui.util.UIConstants.*;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.ScrollPanelAPI;

import wfg.native_ui.example.table.ListTableExample;
import wfg.native_ui.internal.ui.core.UIContainer;
import wfg.native_ui.ui.ComponentFactory;
import wfg.native_ui.ui.core.UIBuildableAPI;
import wfg.native_ui.ui.core.UIContainerAPI;
import wfg.native_ui.util.Arithmetic;

/**
 * Arranges uniform-height widgets in a vertical list with scroll support.
 * Handles scroll position restoration, empty state, and optional widget selection.
 * Each widget is sized to fill the content width minus the outer margins.
 *
 * <p><strong>Example: </strong> {@link ListTableExample}</p>
 *
 * @param <T> the data type for each list item.
 * @param <W> the widget type that displays a {@link T}.
 */
public abstract class ListTable<T, W extends WidgetAPI<W>> extends UIContainer implements UIBuildableAPI {

    /** Outer margin between widgets and panel edges; also used as inter-widget spacing. */
    protected final int mGap;
    protected final int mWidgetH;

    /** List of currently displayed widgets. */
    protected final ArrayList<W> mWidgets = new ArrayList<>();
    /** Currently selected widget, if selection is enabled. */
    protected W mSelectedWidget = null;

    /** Scrollable container built via {@link ComponentFactory}. */
    protected ScrollPanelAPI mScrollContainer;
    /** The content container that holds the widgets. */
    protected UIContainerAPI mContent;
    /** Last known scroll offset, restored on rebuild. */
    protected float mScrollOffset = 0f;
    /** Determines whether widget selection is enabled. */
    protected boolean mIsSelectionEnabled = false;
    /** Determines the usage of {@link #mGap} between rows. */
    protected boolean mAddRowGap = false;

    public ListTable(float width, float height, int widgetH) {
        this(width, height, widgetH, hpad);
    }

    public ListTable(float width, float height, int widgetH, int gap) {
        super(width, height);
        mWidgetH = widgetH;
        mGap = gap;
    }

    /** @return the list of data items to display. */
    protected abstract List<T> getDataList();

    /**
     * Create a widget for the given data item.
     * <p>The returned widget will have its size set via
     * {@link PositionAPI#setSize} and its {@link UIBuildableAPI#buildUI}
     * called by {@link ListTable} after creation.
     *
     * @param item the data object
     * @param index its position in the list
     * @return a fully constructed widget
     */
    protected abstract W createWidget(T item, int index);

    protected abstract void onWidgetClicked(W source);

    /** @return the message to display when the data list is empty. */
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

        final float contentWidth = computeContentContainerWidth();
        final int widgetW = (int) computeContentWidth();
        mContent = new UIContainer(contentWidth, 0f);

        final float margin = mGap;
        final float rowPad = mAddRowGap ? mGap : 0f;

        for (int i = 0; i < items.size(); i++) {
            final T item = items.get(i);
            final W widget = createWidget(item, i);
            widget.getPosition().setSize(widgetW, mWidgetH);
            widget.buildUI();
            mWidgets.add(widget);

            if (mIsSelectionEnabled) {
                widget.getInteraction().onClicked = (w, isLeft) -> onWidgetClicked(w);
            }

            final float y = margin + i * (mWidgetH + rowPad);
            mContent.add(widget).inTL(margin, y);
        }

        final float contentHeight = 2f * margin + items.size() * mWidgetH + (items.size() - 1) * rowPad;
        mContent.setHeight(contentHeight);

        final float visibleHeight = getHeight();
        mScrollContainer = ComponentFactory.wrapWithScrollPanel(mContent, contentWidth, visibleHeight);
        add(mScrollContainer).inTL(0f, 0f);

        final float maxScroll = Math.max(0f, contentHeight - visibleHeight);
        mScrollContainer.setYOffset(Arithmetic.clamp(mScrollOffset, 0f, maxScroll));
    }

    public final void scrollToBottom() {
        if (mScrollContainer == null) return;
        final float maxScroll = Math.max(0f, mContent.getHeight() - getHeight());
        mScrollContainer.setYOffset(maxScroll);
    }

    protected final float computeContentContainerWidth() {
        return getWidth() - ComponentFactory.SCROLL_PANEL_WIDTH_PAD;
    }

    protected final float computeContentWidth() {
        return computeContentContainerWidth() - 2 * mGap;
    }
}