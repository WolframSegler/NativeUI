package wfg.native_ui.ui.table;

import static wfg.native_ui.util.UIConstants.*;

import java.util.List;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.input.InputEventAPI;

import wfg.native_ui.example.table.ListTableExample;

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
public abstract class ListTable<T, W extends WidgetAPI<W>> extends AbstractTable<T, W> {

    public ListTable(float width, float height, int widgetH) {
        this(width, height, widgetH, hpad, 0);
    }

    public ListTable(float width, float height, int widgetH, int margin, int gap) {
        super(width, height, widgetH, margin, gap);
    }

    @Override
    public void createUI(final List<T> items) {
        for (int i = 0; i < items.size(); i++) {
            final T item = items.get(i);
            final W widget = createWidget(item, i);
            widget.getPosition().setSize(computeContentWidth(), mWidgetH);
            widget.buildUI();
            mWidgets.add(widget);

            if (mIsSelectionEnabled) {
                widget.getInteraction().onClicked = (w, isLeft) -> onWidgetClicked(w, isLeft, false);
            }

            final float y = mMargin + i * (mWidgetH + mGap);
            mContent.add(widget).inTL(mMargin, y);
        }

        final float contentHeight = 2f * mMargin + items.size() * mWidgetH + (items.size() - 1) * mGap;
        mContent.setHeight(contentHeight);
    }

    @Override
    protected boolean handleArrowKey(InputEventAPI event, int key) {
        final boolean forward = key == Keyboard.KEY_DOWN || key == Keyboard.KEY_RIGHT;
        final int size = mWidgets.size();
        if (size == 0) return false;

        if (mHighlightedWidget == null) {
            setHighlighted(forward ? 0 : size - 1);
            return true;
        }
        final int index = mWidgets.indexOf(mHighlightedWidget);
        if (index < 0) return false;
        int moveToIndex = forward ? index + 1 : index - 1;
        if (moveToIndex < 0) moveToIndex = size - 1;
        if (moveToIndex >= size) moveToIndex = 0;
        return setHighlighted(moveToIndex);
    }

    @Override
    protected float computeHighlightWidth() {
        return computeContentWidth();
    }
}