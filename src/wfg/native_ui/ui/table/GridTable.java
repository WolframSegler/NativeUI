package wfg.native_ui.ui.table;

import static wfg.native_ui.util.UIConstants.*;

import java.util.List;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.input.InputEventAPI;

import wfg.native_ui.example.table.GridTableExample;

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
public abstract class GridTable<T, W extends WidgetAPI<W>> extends AbstractTable<T, W> {

    protected final int mWidgetW;

    /**
     * Leftover horizontal space is distributed evenly as additional gap
     * between widgets, making the grid to span the full width while keeping
     * the outer margin equal to the base gap. If false, widgets are left‑aligned.
     */
    protected boolean mJustifyGrid = false;

    public GridTable(float width, float height, int widgetW, int widgetH) {
        this(width, height, widgetW, widgetH, hpad, hpad);
    }

    public GridTable(float width, float height, int widgetW, int widgetH, int margin, int gap) {
        super(width, height, widgetH, margin, gap);
        mWidgetW = widgetW;
    }

    @Override
    public void createUI(List<T> items) {
        final int cols = computeColCount();
        final float verGap = computeVerticalGap();

        for (int i = 0; i < items.size(); i++) {
            final T item = items.get(i);
            final W widget = createWidget(item, i);
            mWidgets.add(widget);

            if (mIsSelectionEnabled) {
                widget.getInteraction().onClicked = (w, isLeft) -> onWidgetClicked(w, isLeft, false);
            }

            final int row = i / cols;
            final int col = i % cols;

            final float x = mMargin + col * (mWidgetW + verGap);
            final float y = mMargin + row * (mWidgetH + mGap);

            mContent.add(widget).inTL(x, y);
        }

        final int rows = (items.size() + cols - 1) / cols;
        final float contentHeight = mMargin + rows * (mWidgetH + mGap);
        mContent.setHeight(contentHeight);
    }

    protected final int computeColCount() {
        final float contentW = computeContentWidth();
        return Math.max(1, (int) ((contentW + mGap) / (mWidgetW + mGap)));
    }

    protected final float computeVerticalGap() {
        final int cols = computeColCount();
        if (!mJustifyGrid || cols <= 1) return mGap;

        final float contentW = computeContentWidth();
        final float baseTotalW = cols * mWidgetW + (cols - 1) * mGap;
        final float extraSpace = contentW - baseTotalW;

        return mGap + extraSpace / (cols - 1);
    }

    @Override
    protected float computeHighlightWidth() {
        return mWidgetW;
    }

    @Override
    protected boolean handleArrowKey(final InputEventAPI event, final int key) {
        final boolean forward = key == Keyboard.KEY_DOWN || key == Keyboard.KEY_RIGHT;
        final boolean isHorizontal = key == Keyboard.KEY_LEFT || key == Keyboard.KEY_RIGHT;

        final int size = mWidgets.size();
        if (size == 0) return false;

        if (mHighlightedWidget == null) {
            return setHighlighted(forward ? 0 : size - 1);
        }

        final int index = mWidgets.indexOf(mHighlightedWidget);
        if (index < 0) return false;

        final int moveToIndex;
        if (isHorizontal) {
            int next = forward ? index + 1 : index - 1;
            if (next < 0) next = size - 1;
            else if (next >= size) next = 0;
            moveToIndex = next;
        } else {
            final int colCount = computeColCount();
            final int col = index % colCount;
            if (forward) {
                int next = index + colCount;
                if (next >= size) next = col;
                moveToIndex = next;
            } else {
                int next = index - colCount;
                if (next < 0) {
                    final int lastRowStart = ((size - 1) / colCount) * colCount;
                    next = lastRowStart + col;
                    if (next >= size) next -= colCount; // last row lacks this column
                }
                moveToIndex = next;
            }
        }

        return setHighlighted(moveToIndex);
    }
}