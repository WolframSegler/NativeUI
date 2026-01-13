package wfg.wrap_ui.ui.panels;

import static wfg.wrap_ui.util.UIConstants.pad;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import wfg.wrap_ui.internal.util.BorderRenderer;
import wfg.wrap_ui.ui.Attachments;
import wfg.wrap_ui.ui.plugins.DockPanelPlugin;

public abstract class DockPanel extends CustomPanel<DockPanelPlugin, DockPanel, UIPanelAPI>{
    
    public static enum DockDirection {
        LEFT, RIGHT, TOP, BOTTOM
    }

    public boolean isOpen = false;
    public float durIn = 0.3f;
    public float durOut = 0.3f;

    protected DockDirection dockDir = DockDirection.LEFT;
    protected float offsetX = 0f;
    protected float offsetY = 0f;
    
    protected Vector2f targetPos;
    protected float progress = 0f;

    protected BorderRenderer border;
    protected String borderPrefix = "ui_border1";

    public DockPanel(final int width, final int height, final DockDirection dir) {
        super(Attachments.getScreenPanel(), width, height, new DockPanelPlugin());
        getPlugin().init(this);

        border = new BorderRenderer(borderPrefix, width, height);
        targetPos = calculateTargetPos(); 
    }
    public void createPanel() {}

    public void changeOffset(final float x, final float y) {
        offsetX = x; offsetY = y;
        targetPos = calculateTargetPos();
    }

    public void changeDirection(final DockDirection dir) {
        dockDir = dir;
        targetPos = calculateTargetPos();
    }

    public void setBorder(String prefix) {
        borderPrefix = prefix;
        setSize(getPos().getWidth(), getPos().getHeight());
    }

    public PositionAPI setSize(final float w, final float h) {
        getPos().setSize(w, h);
        border = new BorderRenderer(borderPrefix, w + pad*2, h + pad*2);
        return getPos();
    }

    public void advanceImpl(final float delta) {
        final float target = isOpen ? 1f : 0f;
        final float speed = isOpen ?
            (durIn > 0f ? 1f / durIn : Float.POSITIVE_INFINITY) :
            (durOut > 0f ? 1f / durOut : Float.POSITIVE_INFINITY);

        if (progress != target) {
            final float step = speed * delta;
            progress = isOpen ? Math.min(progress + step, 1f) : Math.max(progress - step, 0f);

            updatePosition();
        }
    }

    public void renderImpl(final float alpha) {
        final PositionAPI pos = getPos();
        
        if (border != null) {
            border.render(pos.getX() - pad, pos.getY() - pad, alpha);
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