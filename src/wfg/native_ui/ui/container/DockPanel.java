package wfg.native_ui.ui.container;

import static wfg.native_ui.util.Globals.settings;
import static wfg.native_ui.util.UIConstants.*;

import java.util.List;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import rolflectionlib.util.RolfLectionUtil;
import wfg.native_ui.internal.ui.Side;
import wfg.native_ui.internal.ui.functional.OutsideEventDetector;
import wfg.native_ui.internal.ui.functional.OutsideEventDetector.OutisdeEventListener;
import wfg.native_ui.internal.util.BorderRenderer;
import wfg.native_ui.ui.Attachments;
import wfg.native_ui.ui.ComponentFactory;
import wfg.native_ui.ui.core.UIBuildableAPI;
import wfg.native_ui.ui.panel.CustomPanel;
import wfg.native_ui.util.CallbackRunnable;
import wfg.native_ui.util.UIConstants;

/**
 * A reusable dockable panel that slides on/off screen from one of the four sides.
 *
 * <ul>
 *   <li>The constructor attaches the panel to the given parent immediately — you do not need (and
 *   should not) add it again. The panel calculates its open position via {@link #calculateTargetPos()}
 *   and computes the closed (off-screen) position based on the chosen {@code Side}.</li>
 *   <li>Open/close is driven by {@link #open()} / {@link #close()}. The animated visibility progress
 *   value (0..1) is stored in {@code progress}; animation timing is controlled by {@code durIn}
 *   and {@code durOut}.</li>
 *   <li>Call {@link #changeOffset(float,float)} to nudge the final anchored position, or
 *   {@link #changeDirection(Side)} to change the dock side at runtime (the border will be updated).</li>
 * </ul>
 *
 * <p>Behavior & lifecycle caveats
 * <ul>
 *   <li>By default the panel attaches an {@link OutsideEventDetector} when opened and closes when
 *   an outside click or the cancel button is pressed. Override {@link #outsideClicked} / {@link #buttonPressed}
 *   only if you intend to change that behavior.</li>
 *   <li>If {@code removeWhenClosed} is true the panel will remove itself from the parent when its
 *   close animation finishes.</li>
 * </ul>
 */
public abstract class DockPanel extends CustomPanel implements
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
    protected final UIPanelAPI contentContainer;

    private Side dockDir = Side.LEFT;
    private int innerPad;

    public DockPanel(int width, int height, final Side dir) {
        this(Attachments.getScreenPanel(), width, height, dir, opad);
    }

    public DockPanel(int width, int height, final Side dir, int padding) {
        this(Attachments.getScreenPanel(), width, height, dir, padding);
    }

    public DockPanel(final UIPanelAPI parent, int width, int height, final Side dir, int padding) {
        super(parent, width + padding*2, height + padding*2);
        detector = new OutsideEventDetector(this);
        parent.addComponent(m_panel);

        contentContainer = settings.createCustom(width, height, null);
        m_panel.addComponent(contentContainer).inBL(padding, padding);

        dockDir = dir;
        innerPad = padding;

        border = new BorderRenderer(borderPrefix, false, width + padding*2, height + padding*2, dir);
        calculateTargetPos();
        updatePosition();
    }

    public boolean isOpen() { return isOpen;}
    public void close() { isOpen = false;}
    public void open() { open(false);}
    public void open(boolean guardIfProgressHigh) {
        if (guardIfProgressHigh && progress > 0.6f) return;
        isOpen = true;
        detector.attach();
        m_parent.bringComponentToTop(m_panel);
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
        setSize(pos.getWidth() + innerPad*2, pos.getHeight() + innerPad*2);

        border.clearSides();
        border.hideSide(dockDir);
    }

    public PositionAPI setSize(final float w, final float h) {
        pos.setSize(w + innerPad*2, h + innerPad*2);
        contentContainer.getPosition().setSize(w, h);
        border.setSize(w + innerPad*2, h + innerPad*2);
        return getPos();
    }

    @Override
    public void advance(final float delta) {
        super.advance(delta);
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
            m_parent.removeComponent(m_panel);
            if (onRemoved != null) onRemoved.run(this);
        }
    }

    @Override
    public void renderBelow(final float alpha) {
        super.renderBelow(alpha);

        if (border != null) {
            border.render(pos.getX(), pos.getY(), alpha * bgAlpha);
        }
    }

    @Override
    public final void outsideClicked(boolean isLeft) {
        close();
    }
    @Override
    public final void buttonPressed(int lwjgl_key) {
        close();
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        super.processInput(events);

        for (InputEventAPI event : events) {
            if (!event.isMouseEvent() || !pos.containsEvent(event)) continue;

            event.consume();
        }
    }

    @Override
    public UIPanelAPI getPanel() { return contentContainer; }
    public UIPanelAPI getDockPanel() { return m_panel; }

    @Override
    public PositionAPI add(TooltipMakerAPI a) {
        return ComponentFactory.addTooltip(a, 0f, false, contentContainer);
    }

    @Override
    public PositionAPI add(UIComponentAPI a) {
        contentContainer.addComponent(a);

        return a.getPosition();
    }

    @Override
    public PositionAPI add(CustomPanel a) {
        contentContainer.addComponent(a.getPanel());

        return a.getPos();
    }

    @Override
    public void remove(UIComponentAPI a) {
        contentContainer.removeComponent(a);
    }

    @Override
    public void remove(CustomPanel a) {
        contentContainer.removeComponent(a.getPanel());
    }

    @Override
    public PositionAPI addPositionOnly(UIComponentAPI comp) {
        final PositionAPI position = comp.getPosition();
        RolfLectionUtil.invokeMethodDirectly(positionSetParentMethod, position, pos);
        RolfLectionUtil.invokeMethodDirectly(addToPositionMethod, contentContainer.getPosition(), position);
        return position;
    }

    @Override
    public PositionAPI removePositionOnly(UIComponentAPI comp) {
        final PositionAPI position = comp.getPosition();
        RolfLectionUtil.invokeMethodDirectly(positionSetParentMethod, position, (Object)null);
        RolfLectionUtil.invokeMethodDirectly(removeFromPositionMethod, contentContainer.getPosition(), position);
        return position;
    }

    @Override
    public final void clearChildren() {
        RolfLectionUtil.invokeMethodDirectly(clearChildrenMethod, contentContainer);
    }

    protected void updatePosition() {
        final PositionAPI pos = getPos();
        final float eased = easeOutCubic(progress, 1f);

        final float openX = targetPosX;
        final float openY = targetPosY;

        final float closedX;
        final float closedY;

        switch (dockDir) {
        case LEFT:
            closedX = -pos.getWidth();
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
            closedY = -pos.getHeight();
            break;
        default:
            closedX = openX;
            closedY = openY;
        }

        final float x = closedX + (openX - closedX) * eased;
        final float y = closedY + (openY - closedY) * eased;

        pos.inBL(x, y);
    }

    protected void calculateTargetPos() {
        final float screenWidth = screenW;
        final float screenHeight = screenH;
        final PositionAPI pos = getPos();
        final float panelWidth = pos.getWidth();
        final float panelHeight = pos.getHeight();

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
        return 1f - (float)Math.pow(1f - progress, 3);
    }
}