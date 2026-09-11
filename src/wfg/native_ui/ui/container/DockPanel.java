package wfg.native_ui.ui.container;

import static wfg.native_ui.util.UIConstants.*;

import java.util.List;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import wfg.native_ui.example.container.DockPanelExample;
import wfg.native_ui.internal.ui.Side;
import wfg.native_ui.internal.ui.core.UIContainer;
import wfg.native_ui.internal.ui.interaction.OutsideEventDetector;
import wfg.native_ui.internal.ui.interaction.OutsideEventDetector.OutisdeEventListener;
import wfg.native_ui.internal.util.BorderRenderer;
import wfg.native_ui.ui.Attachments;
import wfg.native_ui.ui.ComponentFactory;
import wfg.native_ui.ui.core.UIBuildableAPI;
import wfg.native_ui.ui.core.UIContainerAPI;
import wfg.native_ui.ui.functional.CallbackRunnable;
import wfg.native_ui.util.UIConstants;

/**
 * A reusable dockable panel that slides on/off screen from one of the four sides.
 *
 * <ul>
 *   <li>The constructor attaches the panel to the given parent immediately — you do not need (and
 *   should not) add it again. The panel calculates its open position via {@link #calculateTargetPos()}
 *   and computes the closed (off-screen) position based on the chosen {@link Side}.</li>
 *   <li>Open/close is driven by {@link #open()} / {@link #close()}. The animated visibility progress
 *   value (0..1) is stored in {@link #progress}; animation timing is controlled by {@link #durIn}
 *   and {@link #durOut}.</li>
 *   <li>Call {@link #changeOffset()} to nudge the final anchored position, or
 *   {@link #changeDirection()} to change the dock side at runtime (the border will be updated).</li>
 * </ul>
 *
 * <p>Behavior & lifecycle caveats
 * <ul>
 *   <li>By default the panel attaches an {@link OutsideEventDetector} when opened and closes when
 *   an outside click or the cancel button is pressed. Override {@link #outsideClicked()} / {@link #buttonPressed()}
 *   only if you intend to change that behavior.</li>
 *   <li>If {@link #removeWhenClosed} is true the panel will remove itself from the parent when its
 *   close animation finishes.</li>
 * </ul>
 * 
 * <p><strong>Example: </strong> {@link DockPanelExample}</p>
 */
public abstract class DockPanel extends UIContainer implements
    OutisdeEventListener, UIBuildableAPI
{
    public boolean removeWhenClosed = false;
    public float durIn = 0.3f;
    public float durOut = 0.3f;
    public float bgAlpha = 0.85f;

    public CallbackRunnable<DockPanel> onRemoved = null;

    protected boolean isOpen = false;

    protected float offsetX = 0f;
    protected float offsetY = 0f;
    
    protected float targetPosX;
    protected float targetPosY;
    protected float progress = 0f;

    protected BorderRenderer border;
    protected String borderPrefix = UIConstants.UI_BORDER_1;

    protected final OutsideEventDetector detector;
    protected final UIContainerAPI contentContainer;

    private Side dockDir = Side.LEFT;
    private float innerPad;

    public DockPanel(float width, float height, final Side dir) {
        this(Attachments.getScreenPanel(), width, height, dir, opad);
    }

    public DockPanel(float width, float height, final Side dir, float padding) {
        this(Attachments.getScreenPanel(), width, height, dir, padding);
    }

    public DockPanel(final UIPanelAPI parent, float width, float height, final Side dir, float padding) {
        super(width + padding*2, height + padding*2);
        setParent(parent);
        detector = new OutsideEventDetector(this);
        parent.addComponent(this);

        contentContainer = new UIContainer(width, height);
        add(contentContainer).inBL(padding, padding);

        dockDir = dir;
        innerPad = padding;

        border = new BorderRenderer(borderPrefix, false, width + padding*2, height + padding*2, dir);
        calculateTargetPos();
        updatePosition();
    }

    public boolean isOpen() { return isOpen; }
    public void close() { isOpen = false; }
    public void open() { open(false); }
    public void open(boolean guardIfProgressHigh) {
        if (guardIfProgressHigh && progress > 0.6f) return;
        isOpen = true;
        detector.attach();
        mParent.bringComponentToTop(this);
    }

    public void changeOffset(final float x, final float y) {
        offsetX = x; offsetY = y;
        calculateTargetPos();
    }

    public void changeDirection(final Side dir) {
        dockDir = dir;
        calculateTargetPos();
        border.clearSides();
        border.hideSide(dir);
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
        borderPrefix = prefix;
        border = new BorderRenderer(prefix, false);
        setSize(getWidth() + innerPad*2f, getHeight() + innerPad*2f);

        border.clearSides();
        border.hideSide(dockDir);
    }

    @Override 
    public PositionAPI setSize(final float w, final float h) {
        mPos.setSize(w + innerPad*2f, h + innerPad*2f);
        contentContainer.getPosition().setSize(w, h);
        border.setSize(w + innerPad*2f, h + innerPad*2f);
        return mPos;
    }

    @Override
    public void advanceImpl(final float delta) {
        super.advanceImpl(delta);
        final float target = isOpen ? 1f : 0f;
        final float speed = isOpen ?
            (durIn > 0f ? 1f / durIn : Float.POSITIVE_INFINITY) :
            (durOut > 0f ? 1f / durOut : Float.POSITIVE_INFINITY);

        if (progress != target) {
            final float step = speed * delta;
            progress = isOpen ? Math.min(progress + step, 1f) : Math.max(progress - step, 0f);

            updatePosition();
        }

        if (!isOpen && removeWhenClosed && progress < 0.005f) {
            detach();
            if (onRemoved != null) onRemoved.run(this);
        }
    }

    @Override
    public void renderBelowImpl(final float alpha) {
        if (border != null) {
            border.render(getX(), getY(), alpha * bgAlpha);
        }
    }

    public final void outsideClicked(boolean isLeft) { close(); }
    public final void buttonPressed(int lwjgl_key) { close(); }

    @Override
    public void processInputImpl(List<InputEventAPI> events) {
        super.processInputImpl(events);
        for (InputEventAPI event : events) {
            if (!event.isMouseEvent() || !mPos.containsEvent(event)) continue;
            event.consume();
        }
    }

    public UIContainerAPI getContentContainer() { return contentContainer; }

    @Override
    public PositionAPI add(TooltipMakerAPI tooltip) {
        return ComponentFactory.addTooltip(tooltip, 0f, false, contentContainer);
    }

    @Override
    public PositionAPI add(UIComponentAPI comp) {
        return contentContainer.addComponent(comp);
    }

    @Override
    public void remove(UIComponentAPI comp) {
        contentContainer.removeComponent(comp);
    }

    @Override
    public PositionAPI addPos(UIComponentAPI comp) {
        return contentContainer.addPos(comp);
    }

    @Override
    public void removePos(UIComponentAPI comp) {
        contentContainer.removePos(comp);
    }

    @Override
    public final void clearChildren() {
        contentContainer.clearChildren();
    }

    protected void updatePosition() {
        final float eased = easeOutCubic(progress, 1f);

        final float openX = targetPosX;
        final float openY = targetPosY;

        final float closedX;
        final float closedY;

        switch (dockDir) {
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

        switch (dockDir) {
        default: case LEFT:
            x = 0f + offsetX;
            y = (screenHeight - panelHeight) / 2f + offsetY;
            break;
        case RIGHT:
            x = screenWidth - panelWidth + offsetX;
            y = (screenHeight - panelHeight) / 2f + offsetY;
            break;
        case TOP:
            x = (screenWidth - panelWidth) / 2f + offsetX;
            y = screenHeight - panelHeight + offsetY;
            break;
        case BOTTOM:
            x = (screenWidth - panelWidth) / 2f + offsetX;
            y = 0f + offsetY;
            break;
        }
        
        targetPosX = x;
        targetPosY = y;
    }

    protected static float easeOutCubic(final float t, final float end) {
        final float progress = Math.min(Math.max(t / end, 0f), 1f);
        return 1f - (float)Math.pow(1f - progress, 3d);
    }
}