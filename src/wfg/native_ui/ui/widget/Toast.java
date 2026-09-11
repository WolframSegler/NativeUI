package wfg.native_ui.ui.widget;

import static wfg.native_ui.util.Globals.settings;
import static wfg.native_ui.util.UIConstants.*;

import java.awt.Color;
import java.util.List;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.FaderUtil.State;

import wfg.native_ui.example.widget.ToastExample;
import wfg.native_ui.internal.ui.Side;
import wfg.native_ui.internal.ui.core.UIContainer;
import wfg.native_ui.internal.util.ToastRenderer;
import wfg.native_ui.ui.Attachments;
import wfg.native_ui.ui.ComponentFactory;
import wfg.native_ui.ui.core.UIContainerAPI;
import wfg.native_ui.util.NativeUiUtils;

// TODO test this
/**
 * A holographic notification that slides on/off screen from the left or right edge.
 * It has three layers: the handle, the hologram and the countdown bar, all managed by {@link ToastRenderer}.
 * The countdown is optional, and can be modified using {@link #setToastProgress()}.
 * 
 * <ul>
 *   <li>The constructor attaches the toast to the given parent immediately - do not
 *   add it again. The open position is calculated via {@link #calculateTargetPos()},
 *   and the closed position is derived from {@link #mDockDir}.</li>
 *   <li>Open/close is driven by {@link #open()} / {@link #close()}. The animated visibility
 *   progress [0, 1] is retrieved via {@link #getOpenProgress()}; the toast detaches itself from
 *   the parent once the close animation finishes.</li>
 *   <li>Content is added to an internal {@link UIContainerAPI} exposed by
 *   {@link #getContentContainer()}. All {@code add}/{@code remove}/{@code addPos}/{@code removePos}
 *   overloads delegate to it, so tooltips and components attach normally.</li>
 *   <li>Call {@link #changeOffset()} to nudge the anchored position, or
 *   {@link #changeDirection()} to swap the dock side at runtime, the handle mirrors
 *   accordingly.</li>
 * </ul>
 *
 * <p>Lifecycle
 * <ul>
 *   <li>A right-click anywhere inside the toast dismisses it via {@link #close()}.</li>
 *   <li>By default a toast counts down from {@code 1f} to {@code 0f} over
 *   {@link #mToastProgressTime} seconds and closes itself. Call
 *   {@link #setToastProgress()} with a non-positive value to make it persistent.</li>
 * </ul>
 *
 * <p><strong>Example:</strong> {@link ToastExample}</p>
 */
public class Toast extends UIContainer {
    private static final float ZERO_TOAST_PROGRESS = 0f;

    public Color mHandleColor = Color.WHITE;
    public Color mHoloColor = settings.getColor("progressBarStandardColor");

    public float mBgAlpha = 0.85f;
    
    protected float mOffsetX = 0f;
    protected float mOffsetY = 0f;
    
    protected float mTargetPosX;
    protected float mTargetPosY;
    
    protected final UIContainerAPI mContentContainer;
    
    private Side mDockDir = Side.LEFT;
    private float mContentPad = hpad;

    private final FaderUtil mHoverFader = new FaderUtil(0f, 0.1f, 0.3f);
    private float mToastProgress = 1f;
    private float mToastProgressTime = 3f;

    public Toast(float width, float height, final Side dir) {
        this(Attachments.getScreenPanel(), width, height, dir);
    }

    public Toast(final UIPanelAPI parent, float width, float height, final Side dir) {
        super();

        parent.addComponent(this);

        mFader.forceOut();
        mDockDir = dir;

        mContentContainer = new UIContainer();
        add(mContentContainer);

        setSize(width, height);

        calculateTargetPos();
        updatePosition();
    }

    public final float getOpenProgress() { return mFader.getBrightness(); }
    public boolean isOpen() { return !mFader.isFadingOut(); }
    public void close() { mFader.fadeOut(); }
    public void open() {
        mFader.fadeIn();
        mParent.bringComponentToTop(this);
    }

    public void changeOffset(final float x, final float y) {
        mOffsetX = x; mOffsetY = y;
        calculateTargetPos();
        updatePosition();
    }

    /** Valid: {@link Side#RIGHT} and {@link Side#LEFT}. */
    public void changeDirection(final Side dir) {
        if (dir.isVertical()) throw new IllegalArgumentException("Side must be LEFT or RIGHT");
        mDockDir = dir;
        calculateTargetPos();
        updatePosition();
    }

    @Override 
    public PositionAPI setSize(final float w, final float h) {
        mPos.setSize(w + ToastRenderer.HANDLE_W + mContentPad * 2f, h + mContentPad * 2f);
        mContentContainer.getPosition().setSize(w, h).inBL(ToastRenderer.HANDLE_W + mContentPad, mContentPad);
        return mPos;
    }

    public void setContentPad(float pad) {
        mContentPad = pad;
        setSize(mContentContainer.getWidth(), mContentContainer.getHeight());
    }

    /**
     * @param progress The current progress of the countdown. Default is {@code 1f}. {@code 0f} for unlimited.
     * @param time The time it takes in seconds for the countdown to reach from {@code 1f} to {@code 0f}.
     */
    public void setToastProgress(float progress, float time) {
        mToastProgress = progress;
        mToastProgressTime = time;
    }

    @Override
    public void renderBelowImpl(float alpha) {
        final boolean mirror = mDockDir == Side.RIGHT;
        ToastRenderer.render(mPos, mirror, alpha * mBgAlpha, mHandleColor, mHoloColor, mHoverFader.getBrightness(), mToastProgress);
    }

    @Override
    public void advanceImpl(float delta) {
        super.advanceImpl(delta);

        mHoverFader.setState(NativeUiUtils.containsMouse(mPos) ? State.IN : State.OUT);
        mHoverFader.advance(delta);

        final boolean open = isOpen();
        final float target = open ? 1f : 0f;
        final float progress = getOpenProgress();

        if (progress != target) {
            updatePosition();
        }

        if (!open && progress < 0.005f) {
            detach();
        }

        if (open && mToastProgress > ZERO_TOAST_PROGRESS) {
            final float toastDelta = delta / mToastProgressTime;

            mToastProgress = Math.max(ZERO_TOAST_PROGRESS, mToastProgress - toastDelta);
            if (mToastProgress == ZERO_TOAST_PROGRESS) close();
        }
    }

    @Override
    public void processInputImpl(List<InputEventAPI> events) {
        super.processInputImpl(events);
        for (InputEventAPI event : events) {
            if (event.isConsumed() || !event.isMouseEvent() || !mPos.containsEvent(event)) continue;
            if (event.isRMBDownEvent()) close();
            event.consume();
        }
    }

    public UIContainerAPI getContentContainer() { return mContentContainer; }

    @Override
    public PositionAPI add(TooltipMakerAPI tooltip) {
        return ComponentFactory.addTooltip(tooltip, 0f, false, mContentContainer);
    }

    @Override
    public PositionAPI add(UIComponentAPI comp) {
        return mContentContainer.addComponent(comp);
    }

    @Override
    public void remove(UIComponentAPI comp) {
        mContentContainer.removeComponent(comp);
    }

    @Override
    public PositionAPI addPos(UIComponentAPI comp) {
        return mContentContainer.addPos(comp);
    }

    @Override
    public void removePos(UIComponentAPI comp) {
        mContentContainer.removePos(comp);
    }

    @Override
    public final void clearChildren() {
        mContentContainer.clearChildren();
    }

    protected void updatePosition() {
        final float eased = easeOutCubic(getOpenProgress(), 1f);

        final float openX = mTargetPosX;
        final float openY = mTargetPosY;

        final float closedX;
        final float closedY;

        switch (mDockDir) {
        case LEFT: default:
            closedX = -mPos.getWidth() - opad;
            closedY = openY;
            break;
        case RIGHT:
            closedX = screenW + opad;
            closedY = openY;
            break;
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
        case LEFT: default:
            x = mOffsetX;
            y = screenHeight - panelHeight + mOffsetY;
            break;
        case RIGHT:
            x = screenWidth - panelWidth + mOffsetX;
            y = screenHeight - panelHeight + mOffsetY;
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