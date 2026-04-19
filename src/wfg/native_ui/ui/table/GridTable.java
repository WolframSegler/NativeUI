package wfg.native_ui.ui.table;

import static wfg.native_ui.util.Globals.settings;
import static wfg.native_ui.util.UIConstants.*;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import wfg.native_ui.ui.ComponentFactory;
import wfg.native_ui.ui.core.UIBuildableAPI;
import wfg.native_ui.ui.panel.CustomPanel;
import wfg.native_ui.util.Arithmetic;

/**
 * Arranges uniform-sized widgets in a responsive grid with scroll support.
 * Handles scroll position restoration, empty state, and optional widget selection.
 *
 * @param <T> the data type for each grid cell
 * @param <W> the widget type that displays a {@code T}
 */
public abstract class GridTable<T, W extends WidgetAPI<W>> extends CustomPanel implements UIBuildableAPI {
    
    /** Gap between widgets, both horizontally and vertically. */
    protected final int gap;
    protected final int widgetW;
    protected final int widgetH;

    /** List of currently displayed widgets. */
    protected final List<W> widgets = new ArrayList<>();
    /** Currently selected widget, if selection is enabled. */
    protected W selectedWidget = null;

    /** Scrollable container built via {@code ComponentFactory}. */
    protected TooltipMakerAPI container;
    /** Last known scroll offset, restored on rebuild. */
    protected float scrollOffset = 0f;
    /** Determines whether widget selection is enabled. */
    protected boolean isSelectionEnabled = false;
    /** The gap value is used for outer margins (between widgets and panel edges). */
    protected boolean uniformOuterGap = true;
    /**
     * Leftover horizontal space is distributed evenly as additional gap
     * between widgets, making the grid to span the full width while keeping
     * the outer margin equal to the base gap. If false, widgets are left‑aligned.
     */
    protected boolean justifyGrid = false;

    public GridTable(UIPanelAPI parent, int width, int height, int widgetW, int widgetH) {
        this(parent, width, height, widgetW, widgetH, hpad);
    }

    public GridTable(UIPanelAPI parent, int width, int height, int widgetW, int widgetH, int gap) {
        super(parent, width, height);
        this.widgetW = widgetW;
        this.widgetH = widgetH;
        this.gap = gap;
    }

    /** @return the list of data items to display. */
    protected abstract List<T> getDataList();

    /**
     * Create a widget for the given data item.
     * @param item  the data object
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
        if (container != null) scrollOffset = container.getExternalScroller().getYOffset();

        clearChildren();
        widgets.clear();
        selectedWidget = null;

        final List<T> items = getDataList();
        if (items.isEmpty()) {
            final LabelAPI empty = settings.createLabel(getEmptyMessage(), Fonts.DEFAULT_SMALL);
            empty.setColor(gray);
            add(empty).inMid();
            return;
        }

        container = ComponentFactory.createTooltip(pos.getWidth(), true);

        final float margin = uniformOuterGap ? gap : 0f;
        final float availableW = pos.getWidth() - 2 * margin;
        final int cols = Math.max(1, (int) ((availableW + gap) / (widgetW + gap)));

        final float effGap;
        if (justifyGrid && cols > 1) {
            final float baseTotalW = cols * widgetW + (cols - 1) * gap;
            final float extraSpace = availableW - baseTotalW;

            effGap = gap + extraSpace / (cols - 1);
        } else {
            effGap = gap;
        }

        float yOffset = margin;
        for (int i = 0; i < items.size(); i++) {
            final T item = items.get(i);
            final W widget = createWidget(item, i);
            widgets.add(widget);

            if (isSelectionEnabled) {
                widget.getInteraction().onClicked = (w, left) -> onWidgetClicked(w);
            }

            final int row = i / cols;
            final int col = i % cols;

            final float x = margin + col * (widgetW + effGap);
            final float y = yOffset + row * (widgetH + gap);

            container.addCustom(widget.getElement(), 0f).getPosition().inTL(x, y);
        }

        final int rows = (items.size() + cols - 1) / cols;
        final float contentHeight = yOffset + rows * (widgetH + gap);
        container.setHeightSoFar(contentHeight);

        final float visibleHeight = pos.getHeight() - margin;
        ComponentFactory.addTooltip(container, visibleHeight, true, m_panel).inTL(0f, margin);

        final float maxScroll = Math.max(0f, contentHeight - visibleHeight);
        container.getExternalScroller().setYOffset(Arithmetic.clamp(scrollOffset, 0f, maxScroll));
    }

    public final void scrollToBottom() {
        if (container == null) return;
        final float visibleHeight = pos.getHeight() - gap;
        final float maxScroll = Math.max(0f, container.getHeightSoFar() - visibleHeight);
        container.getExternalScroller().setYOffset(maxScroll);
    }
}