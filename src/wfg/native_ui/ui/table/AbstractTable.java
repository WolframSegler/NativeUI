package wfg.native_ui.ui.table;

import static wfg.native_ui.util.Globals.settings;
import static wfg.native_ui.util.UIConstants.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.ScrollPanelAPI;

import wfg.native_ui.internal.ui.core.UIContainer;
import wfg.native_ui.internal.util.BorderRenderer;
import wfg.native_ui.ui.ComponentFactory;
import wfg.native_ui.ui.core.UIBuildableAPI;
import wfg.native_ui.ui.core.UIContainerAPI;
import wfg.native_ui.util.Arithmetic;
import wfg.native_ui.util.UIConstants;

public abstract class AbstractTable<T, W extends WidgetAPI<W>>
    extends UIContainer implements UIBuildableAPI
{
    private static final BorderRenderer sHighlightRenderer = new BorderRenderer(UIConstants.UI_BORDER_3, true, 0f, 0f);
    static { sHighlightRenderer.centerColor = new Color(0, 0, 0, 0); }

    /** Outer margin between widgets and panel edges. */
    protected final int mMargin;
    /** Gap between widgets, both horizontal and vertical. */
    protected final int mGap;
    /** The constant height of the widgets. */
    protected final int mWidgetH;

    /** List of currently displayed widgets. */
    protected final ArrayList<W> mWidgets = new ArrayList<>();

    /** Currently selected widget, if {@link #mIsSelectionEnabled}. */
    protected W mSelectedWidget;
    /** Currently highlighted widget, if {@link #mIsHighlightingEnabled}. */
    protected W mHighlightedWidget;
    /** Determines whether widget selection is enabled. */
    protected boolean mIsSelectionEnabled = false;
    /** Determines whether widget highlighting with {@link #sHighlightRenderer} is enabled. */
    protected boolean mIsHighlightingEnabled = false;

    /** Scrollable container built via {@link ComponentFactory}. */
    protected ScrollPanelAPI mScrollContainer;
    /** The content container that holds the {@link WidgetAPI}'s. */
    protected UIContainerAPI mContent;
    /** Last known scroll offset, restored on rebuild. */
    protected float mScrollOffset = 0f;

    protected AbstractTable(float w, float h, int widgetH, int margin, int gap) {
        super(w, h);
        mWidgetH = widgetH;
        mMargin = margin;
        mGap = gap;
    }

    /** @return the list of data items to display. */
    protected abstract List<T> getDataList();
    /**
     * @param item the data object
     * @param index its position in the list
     * @return fully constructed widget
     */
    protected abstract W createWidget(T item, int index);
    /**
     * @param source the widget that was clicked
     * @param isLeftClick {@code true} when called by a keyboard event
     * @param isKeyboard {@code true} when called by a keyboard event
     */
    protected abstract void onWidgetClicked(W source, boolean isLeftClick, boolean isKeyboard);

    /** @return the message to display when the data list is empty.*/
    protected abstract String getEmptyMessage();
    /** Populate mContent, set its height, and leave mScrollContainer untouched. */
    protected abstract void createUI(List<T> items);

    /** Highlight box width drawn by {@link #sHighlightRenderer}. */
    protected abstract float computeHighlightWidth();
    /** 
     * Handle an arrow key. The event is consumed automatically.
     * @return {@code true} if the event was handled.
     */
    protected abstract boolean handleArrowKey(InputEventAPI event, int key);

    @Override
    public void buildUI() {
        clearChildren();
        mWidgets.clear();
        mSelectedWidget = null;
        mHighlightedWidget = null;

        final List<T> items = getDataList();
        if (items.isEmpty()) {
            final LabelAPI empty = settings.createLabel(getEmptyMessage(), Fonts.DEFAULT_SMALL);
            empty.setColor(gray);
            add(empty).inMid();
            return;
        }
        if (mScrollContainer != null) mScrollOffset = mScrollContainer.getYOffset();

        final float contentW = computeContentContainerWidth();
        mContent = new UIContainer(contentW, 0f);

        createUI(items);
        if (mContent.getHeight() <= 0f) throw new IllegalStateException("createUI() did not set mContent height");

        final float viewHeight = getHeight() - getScrollContainerMarginTop();
        mScrollContainer = ComponentFactory.wrapWithScrollPanel(mContent, contentW, viewHeight);
        add(mScrollContainer).inTL(0f, getScrollContainerMarginTop());
        mScrollContainer.setYOffset(Arithmetic.clamp(mScrollOffset, 0f, getMaxScrollHeight()));
    }

    @Override
    public void processInputImpl(List<InputEventAPI> events) {
        for (final InputEventAPI e : events) {
            if (e.isConsumed() || !e.isKeyDownEvent()) continue;
            final int key = e.getEventValue();
            if (key == Keyboard.KEY_LEFT || key == Keyboard.KEY_RIGHT ||
                key == Keyboard.KEY_DOWN || key == Keyboard.KEY_UP
            ) {
                if (handleArrowKey(e, key)) { e.consume(); continue; }
            }

            if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) {
                if (mHighlightedWidget != null) {
                    onWidgetClicked(mHighlightedWidget, true, true);
                    e.consume();
                }
            }
        }

        super.processInputImpl(events);
    }

    @Override
    public void renderAboveImpl(float alpha) {
        if (mHighlightedWidget == null) return;

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(
            (int) mScrollContainer.getX() - hpad,
            (int) mScrollContainer.getY() - hpad,
            (int) mScrollContainer.getWidth() + opad,
            (int) mScrollContainer.getHeight() + opad
        );

        sHighlightRenderer.setSize(computeHighlightWidth() + opad, mWidgetH + opad);
        sHighlightRenderer.render(mHighlightedWidget.getX() - hpad, mHighlightedWidget.getY() - hpad, 0.4f);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    protected final float computeContentContainerWidth() {
        return computeContentContainerW(getWidth());
    }

    protected final float computeContentWidth() {
        return computeContentContainerWidth() - 2f * mMargin;
    }

    public final void scrollToBottom() {
        if (mScrollContainer == null) return;
        mScrollContainer.setYOffset(getMaxScrollHeight());
    }

    protected final boolean setHighlighted(int index) {
        if (!mIsHighlightingEnabled) return false;
        if (index < 0 || index >= mWidgets.size()) return false;
        mHighlightedWidget = mWidgets.get(index);
        mScrollContainer.ensureVisible(mHighlightedWidget);
        Global.getSoundPlayer().playUISound("ui_number_scrolling", 1f, 1f);
        return true;
    }

    protected float getScrollContainerMarginTop() { return 0f; }

    private final float getMaxScrollHeight() {
        return Math.max(0f, mContent.getHeight() + getScrollContainerMarginTop() - getHeight());
    }

    public static final float computeContentContainerW(float width) {
        return width - ComponentFactory.SCROLL_PANEL_WIDTH_PAD;
    }
}