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

import wfg.native_ui.internal.ui.Side;
import wfg.native_ui.internal.ui.core.UIContainer;
import wfg.native_ui.internal.util.ToastRenderer;
import wfg.native_ui.ui.Attachments;
import wfg.native_ui.ui.ComponentFactory;
import wfg.native_ui.ui.core.UIContainerAPI;
import wfg.native_ui.util.NativeUiUtils;

public class Toast extends UIContainer {
    private static final float NO_TOAST_PROGRESS = -1f;

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

    private FaderUtil mHoverFader = new FaderUtil(0f, 0.1f, 0.3f);
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
    }

    public final float getProgress() { return mFader.getBrightness(); }
    public boolean isOpen() { return !mFader.isFadingOut(); }
    public void close() { mFader.fadeOut(); }
    public void open() {
        mFader.fadeIn();
        mParent.bringComponentToTop(this);
    }

    public void changeOffset(final float x, final float y) {
        mOffsetX = x; mOffsetY = y;
        calculateTargetPos();
    }

    /** Valid: {@link Side#RIGHT} and {@link Side#LEFT}. */
    public void changeDirection(final Side dir) {
        if (dir.isVertical()) throw new IllegalArgumentException("Side must be LEFT or RIGHT");
        mDockDir = dir;
        calculateTargetPos();
    }

    @Override 
    public PositionAPI setSize(final float w, final float h) {
        mPos.setSize(w + ToastRenderer.HANDLE_W + mContentPad * 2f, h + mContentPad * 2f);
        mContentContainer.getPosition().setSize(w, h).inBL(ToastRenderer.HANDLE_W + mContentPad, mContentPad);
        return mPos;
    }

    public void setContentPad(float pad) {
        mContentPad = pad;
        setSize(getWidth(), getHeight());
    }

    /**
     * @param progress The current progress of the countdown. Default is {@code 1f}. {@code -1f} for unlimited.
     * @param time The time it takes in seconds for the countdown to reach from {@code 1f} to {@code 0f}.
     */
    public void setToastProgress(float progress, float time) {
        mToastProgress = progress;
        mToastProgressTime = time;
    }

    @Override
    public void renderBelowImpl(float alpha) {
        final float glow = mHoverFader.getBrightness(); // TODO complete it
        ToastRenderer.render(mPos, false, alpha * mBgAlpha, mHandleColor, mHoloColor, glow, mToastProgress);
    }

    @Override
    public void advanceImpl(float delta) {
        super.advanceImpl(delta);

        mHoverFader.setState(NativeUiUtils.containsMouse(mPos) ? State.IN : State.OUT);

        final float target = isOpen() ? 1f : 0f;

        if (getProgress() != target) {
            updatePosition();
        }

        if (!isOpen() && getProgress() < 0.005f) {
            detach();
        }

        if (isOpen() && mToastProgress != NO_TOAST_PROGRESS) {
            final float toastDelta = delta / mToastProgressTime;

            mToastProgress = Math.max(0f, mToastProgress - toastDelta);
            if (mToastProgress == 0f) close();
        }
    }

    @Override
    public void processInputImpl(List<InputEventAPI> events) {
        super.processInputImpl(events);
        for (InputEventAPI event : events) {
            if (!event.isMouseEvent() || !mPos.containsEvent(event)) continue;
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
        final float eased = easeOutCubic(getProgress(), 1f);

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
            y = (screenHeight - panelHeight) / 2f + mOffsetY;
            break;
        case RIGHT:
            x = screenWidth - panelWidth + mOffsetX;
            y = (screenHeight - panelHeight) / 2f + mOffsetY;
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