package wfg.native_ui.ui.panels;

import static wfg.native_ui.util.UIConstants.pad;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.PositionAPI;

import wfg.native_ui.internal.util.BorderRenderer;
import wfg.native_ui.internal.util.BorderRenderer.BorderSide;
import wfg.native_ui.ui.Attachments;

public abstract class DockPanel extends CustomPanel<DockPanel> {
    
    public static enum DockDirection {
        LEFT, RIGHT, TOP, BOTTOM
    }

    public boolean isOpen = false;
    public float durIn, durOut = 0.3f;

    protected DockDirection dockDir = DockDirection.LEFT;
    protected float offsetX, offsetY = 0f;
    
    protected Vector2f targetPos;
    protected float progress = 0f;

    protected BorderRenderer border;
    protected String borderPrefix = "ui_border1";

    public DockPanel(final int width, final int height, final DockDirection dir) {
        super(Attachments.getScreenPanel(), width, height);
        Attachments.getScreenPanel().addComponent(m_panel);

        border = new BorderRenderer(borderPrefix, width, height, BorderSide.LEFT);
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
        border.hiddenSides.clear();

        switch (dir) {
        case LEFT: border.hiddenSides.add(BorderSide.LEFT); break;
        case RIGHT: border.hiddenSides.add(BorderSide.RIGHT); break;
        case TOP: border.hiddenSides.add(BorderSide.TOP); break;
        case BOTTOM: border.hiddenSides.add(BorderSide.BOTTOM); break;
        }
    }

    public void setBorder(String prefix) {
        borderPrefix = prefix;
        border = new BorderRenderer(prefix);
        setSize(pos.getWidth(), pos.getHeight());
    }

    public PositionAPI setSize(final float w, final float h) {
        pos.setSize(w, h);
        border.setSize(w + pad*2, h + pad*2);
        return getPos();
    }

    @Override
    public void advance(final float delta) {
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

    @Override
    public void renderBelow(final float alpha) {
        super.renderBelow(alpha);
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