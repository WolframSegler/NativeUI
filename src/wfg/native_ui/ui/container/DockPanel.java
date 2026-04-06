package wfg.native_ui.ui.container;

import static wfg.native_ui.util.UIConstants.pad;

import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import wfg.native_ui.internal.ui.Side;
import wfg.native_ui.internal.ui.functional.OutsideEventDetector;
import wfg.native_ui.internal.ui.functional.OutsideEventDetector.OutisdeEventListener;
import wfg.native_ui.internal.util.BorderRenderer;
import wfg.native_ui.ui.Attachments;
import wfg.native_ui.ui.core.UIBuildableAPI;
import wfg.native_ui.ui.panel.CustomPanel;
import wfg.native_ui.util.CallbackRunnable;

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
public abstract class DockPanel extends CustomPanel<DockPanel> implements
    OutisdeEventListener, UIBuildableAPI
{
    public boolean removeWhenClosed = false;
    public boolean loseAttention = true;
    public float durIn = 0.3f;
    public float durOut = 0.3f;
    public float bgAlpha = 0.85f;

    public CallbackRunnable<DockPanel> onRemoved = null;

    protected boolean isOpen = false;

    protected Side dockDir = Side.LEFT;
    protected float offsetX = 0f;
    protected float offsetY = 0f;
    
    protected Vector2f targetPos;
    protected float progress = 0f;

    protected BorderRenderer border;
    protected String borderPrefix = "ui_border1";

    protected final OutsideEventDetector detector;

    public DockPanel(int width, int height, final Side dir) {
        this(Attachments.getScreenPanel(), width, height, dir);
    }

    public DockPanel(final UIPanelAPI parent, int width, int height, final Side dir) {
        super(parent, width, height);
        detector = new OutsideEventDetector(this);
        parent.addComponent(m_panel);

        dockDir = dir;

        border = new BorderRenderer(borderPrefix, width, height, dir);
        targetPos = calculateTargetPos();
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
        targetPos = calculateTargetPos();
    }

    public void changeDirection(final Side dir) {
        dockDir = dir;
        targetPos = calculateTargetPos();
        border.hiddenSides.clear();
        border.hiddenSides.add(dir);
    }

    /**
     * The texture size should match the actual size of the sprites.
     * <pre>
     * Available prefixes:
     * "ui_border1"
     * "ui_border2"
     * "ui_border3"
     * "ui_border4"
     * </pre>
     */
    public void setBorder(String prefix) {
        borderPrefix = prefix;
        border = new BorderRenderer(prefix);
        setSize(pos.getWidth(), pos.getHeight());

        border.hiddenSides.clear();
        border.hiddenSides.add(dockDir);
    }

    public PositionAPI setSize(final float w, final float h) {
        pos.setSize(w, h);
        border.setSize(w + pad*2, h + pad*2);
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
            border.render(pos.getX() - pad, pos.getY() - pad, alpha * bgAlpha);
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

    protected void updatePosition() {
        final PositionAPI pos = getPos();
        final float eased = easeOutCubic(progress, 1f);

        final float openX = targetPos.x;
        final float openY = targetPos.y;

        final float closedX;
        final float closedY;

        switch (dockDir) {
        case LEFT:
            closedX = -pos.getWidth();
            closedY = openY;
            break;
        case RIGHT:
            closedX = Global.getSettings().getScreenWidth();
            closedY = openY;
            break;
        case TOP:
            closedX = openX;
            closedY = Global.getSettings().getScreenHeight();
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

    protected Vector2f calculateTargetPos() {
        final float screenWidth = Global.getSettings().getScreenWidth();
        final float screenHeight = Global.getSettings().getScreenHeight();
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
        
        return new Vector2f(x, y);
    }

    protected static float easeOutCubic(final float t, final float end) {
        final float progress = Math.min(Math.max(t / end, 0f), 1f);
        return 1f - (float)Math.pow(1f - progress, 3);
    }
}