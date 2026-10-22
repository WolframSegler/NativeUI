package wfg.native_ui.ui.container;

import static wfg.native_ui.util.UIConstants.*;

import java.util.List;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import rolflectionlib.ui.UiUtil;
import wfg.native_ui.example.container.DockPanelExample;
import wfg.native_ui.internal.ui.Side;
import wfg.native_ui.internal.ui.core.UIContainer;
import wfg.native_ui.internal.ui.core.UIElement;
import wfg.native_ui.internal.ui.interaction.OutsideEventDetector;
import wfg.native_ui.internal.ui.interaction.OutsideEventDetector.OutsideEventListener;
import wfg.native_ui.internal.util.BorderRenderer;
import wfg.native_ui.ui.Attachments;
import wfg.native_ui.ui.core.UIBuildableAPI;
import wfg.native_ui.ui.core.UIContainerAPI;
import wfg.native_ui.ui.functional.CallbackRunnable;
import wfg.native_ui.util.UIConstants;

/**
 * A reusable dockable panel that slides on/off screen from one of the four sides.
 *
 * <ul>
 *   <li>The constructor attaches the panel to the given parent immediately - do not
 *   add it again. The panel calculates its open position via {@link #calculateTargetPos()}
 *   and computes the closed (off-screen) position based on the chosen {@link Side}.</li>
 *   <li>Open/close is driven by {@link #open()} / {@link #close()}. The animated visibility progress
 *   value [0, 1] is stored in {@link #mProgress}; animation timing is controlled by {@link #mDurIn}
 *   and {@link #mDurOut}.</li>
 *   <li>Call {@link #changeOffset()} to nudge the final anchored position, or
 *   {@link #changeDirection()} to change the dock side at runtime (the border will be updated).</li>
 * </ul>
 *
 * <p>Lifecycle
 * <ul>
 *   <li>By default the panel attaches an {@link OutsideEventDetector} when opened and closes when
 *   an outside click or the cancel button is pressed. Override {@link #outsideClicked()} / {@link #buttonPressed()}
 *   only if you intend to change that behavior.</li>
 *   <li>If {@link #mRemoveWhenClosed} is true the panel will remove itself from the parent when its
 *   close animation finishes.</li>
 * </ul>
 * 
 * <p><strong>Example: </strong> {@link DockPanelExample}</p>
 */
public abstract class DockPanel extends UIElement implements
    OutsideEventListener, UIBuildableAPI
{
    public boolean mRemoveWhenClosed = false;
    public float mDurIn = 0.3f;
    public float mDurOut = 0.3f;
    public float mBgAlpha = 0.85f;

    public CallbackRunnable<DockPanel> mOnRemoved = null;

    protected boolean mIsOpen = false;

    protected float mOffsetX = 0f;
    protected float mOffsetY = 0f;
    
    protected float mTargetPosX;
    protected float mTargetPosY;
    protected float mProgress = 0f;

    protected BorderRenderer mBorder;
    protected String mBorderPrefix = UIConstants.UI_BORDER_1;

    protected final OutsideEventDetector mDetector;
    protected final UIContainerAPI mContentContainer;

    private Side mDockDir = Side.LEFT;
    private float mInnerPad;

    public DockPanel(float width, float height, final Side dir) {
        this(Attachments.getScreenPanel(), width, height, dir, opad);
    }

    public DockPanel(float width, float height, final Side dir, float padding) {
        this(Attachments.getScreenPanel(), width, height, dir, padding);
    }

    public DockPanel(final UIPanelAPI parent, float width, float height, final Side dir, float padding) {
        super(width + padding*2, height + padding*2);

        if (UiUtil.uiComponentClass.isInstance(parent)) {
            wrap(parent);
        } else {
            parent.addComponent(this);
        }

        mDetector = new OutsideEventDetector(this);

        mContentContainer = new UIContainer(width, height);
        super.addPos(mContentContainer);
        mContentContainer.getPosition().inBL(padding, padding);

        mDockDir = dir;
        mInnerPad = padding;

        mBorder = new BorderRenderer(mBorderPrefix, false, width + padding*2, height + padding*2, dir);
        calculateTargetPos();
        updatePosition();
    }

    public boolean isOpen() { return mIsOpen; }
    public void close() { mIsOpen = false; }
    public void open() { open(false); }
    public void open(boolean guardIfProgressHigh) {
        if (guardIfProgressHigh && mProgress > 0.6f) return;
        mIsOpen = true;
        mDetector.attach();
        bringToFront();
    }

    public void changeOffset(final float x, final float y) {
        mOffsetX = x; mOffsetY = y;
        calculateTargetPos();
        updatePosition();
    }

    public void changeDirection(final Side dir) {
        mDockDir = dir;
        calculateTargetPos();
        updatePosition();
        mBorder.clearSides();
        mBorder.hideSide(dir);
    }

    /**
     * Available prefixes:
     * <ul>
     *  <li>{@link UIConstants#UI_BORDER_1}</li>
     *  <li>{@link UIConstants#UI_BORDER_2}</li>
     *  <li>{@link UIConstants#UI_BORDER_3}</li>
     *  <li>{@link UIConstants#UI_BORDER_4}</li>
     * </ul>
     */
    public void setBorder(String prefix) {
        mBorderPrefix = prefix;
        mBorder = new BorderRenderer(prefix, false);
        setSize(mContentContainer.getWidth(), mContentContainer.getHeight());

        mBorder.clearSides();
        mBorder.hideSide(mDockDir);
    }

    @Override 
    public PositionAPI setSize(final float w, final float h) {
        mPos.setSize(w + mInnerPad*2f, h + mInnerPad*2f);
        mContentContainer.getPosition().setSize(w, h);
        mBorder.setSize(w + mInnerPad*2f, h + mInnerPad*2f);
        calculateTargetPos();
        updatePosition();
        return mPos;
    }

    @Override
    public void advanceImpl(final float delta) {
        mContentContainer.advance(delta);

        final float target = mIsOpen ? 1f : 0f;
        final float speed = mIsOpen ?
            (mDurIn > 0f ? 1f / mDurIn : Float.POSITIVE_INFINITY) :
            (mDurOut > 0f ? 1f / mDurOut : Float.POSITIVE_INFINITY);

        if (mProgress != target) {
            final float step = speed * delta;
            mProgress = mIsOpen ? Math.min(mProgress + step, 1f) : Math.max(mProgress - step, 0f);

            updatePosition();
        }

        if (!mIsOpen && mRemoveWhenClosed && mProgress < 0.005f) {
            detach();
            if (mOnRemoved != null) mOnRemoved.run(this);
        }
    }

    @Override
    public void renderImpl(final float alpha) {
        if (mBorder != null) {
            mBorder.render(getX(), getY(), alpha * mBgAlpha);
        }
        mContentContainer.render(alpha);
    }

    public final void outsideClicked(boolean isLeft) { close(); }
    public final void buttonPressed(int lwjgl_key) { close(); }

    @Override
    public void processInputImpl(List<InputEventAPI> events) {
        mContentContainer.processInput(events);
        for (InputEventAPI event : events) {
            if (!event.isMouseEvent() || !mPos.containsEvent(event)) continue;
            event.consume();
        }
    }

    public UIContainerAPI getContent() { return mContentContainer; }

    public PositionAPI add(TooltipMakerAPI tooltip) {
        return mContentContainer.add(tooltip);
    }

    public PositionAPI add(UIComponentAPI comp) {
        return mContentContainer.addComponent(comp);
    }

    /** TODO remove */
    public PositionAPI add(LabelAPI comp) {
        return mContentContainer.add(comp);
    }

    public void remove(UIComponentAPI comp) {
        mContentContainer.removeComponent(comp);
    }

    public void clearChildren() {
        mContentContainer.clearChildren();
    }

    @Override
    public PositionAPI addPos(UIComponentAPI comp) {
        return mContentContainer.addPos(comp);
    }

    @Override
    public void removePos(UIComponentAPI comp) {
        mContentContainer.removePos(comp);
    }

    protected void updatePosition() {
        final float eased = easeOutCubic(mProgress, 1f);

        final float openX = mTargetPosX;
        final float openY = mTargetPosY;

        final float closedX;
        final float closedY;

        switch (mDockDir) {
        case LEFT:
            closedX = -mPos.getWidth();
            closedY = openY;
            break;
        case RIGHT:
            closedX = screenW;
            closedY = openY;
            break;
        case TOP:
            closedX = openX;
            closedY = screenH;
            break;
        case BOTTOM:
            closedX = openX;
            closedY = -mPos.getHeight();
            break;
        default:
            closedX = openX;
            closedY = openY;
        }

        final float x = closedX + (openX - closedX) * eased;
        final float y = closedY + (openY - closedY) * eased;

        mPos.inBL(x, y);
    }

    protected void calculateTargetPos() {
        final float screenWidth = screenW;
        final float screenHeight = screenH;
        final float panelWidth = getWidth();
        final float panelHeight = getHeight();

        final float x;
        final float y;

        switch (mDockDir) {
        default: case LEFT:
            x = 0f + mOffsetX;
            y = (screenHeight - panelHeight) / 2f + mOffsetY;
            break;
        case RIGHT:
            x = screenWidth - panelWidth + mOffsetX;
            y = (screenHeight - panelHeight) / 2f + mOffsetY;
            break;
        case TOP:
            x = (screenWidth - panelWidth) / 2f + mOffsetX;
            y = screenHeight - panelHeight + mOffsetY;
            break;
        case BOTTOM:
            x = (screenWidth - panelWidth) / 2f + mOffsetX;
            y = 0f + mOffsetY;
            break;
        }
        
        mTargetPosX = x;
        mTargetPosY = y;
    }

    protected static float easeOutCubic(final float t, final float end) {
        final float progress = Math.min(Math.max(t / end, 0f), 1f);
        return 1f - (float)Math.pow(1f - progress, 3d);
    }
}