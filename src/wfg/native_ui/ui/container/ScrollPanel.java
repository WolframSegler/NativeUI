package wfg.native_ui.ui.container;

import java.util.List;

import java.awt.Color;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.input.InputEventType;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import rolflectionlib.util.RolfLectionUtil;
import wfg.native_ui.ui.component.BackgroundComp;
import wfg.native_ui.ui.component.InputSnapshotComp;
import wfg.native_ui.ui.component.NativeComponents;
import wfg.native_ui.ui.core.UIElementFlags.HasBackground;
import wfg.native_ui.ui.core.UIElementFlags.HasInputSnapshot;
import wfg.native_ui.ui.panel.CustomPanel;
/**
 * Do not add children directly to ScrollPanel, but to the contentPanel
 */
public class ScrollPanel extends CustomPanel implements HasBackground, HasInputSnapshot {
    public final BackgroundComp bg = comp().get(NativeComponents.BACKGROUND);
    protected final InputSnapshotComp input = comp().get(NativeComponents.INPUT_SNAPSHOT);

    public enum ScrollType { HORIZONTAL, VERTICAL, BOTH }
    public ScrollType scrollType = ScrollType.VERTICAL;
    public float scrollSpeed = 0.5f;

    protected final UIPanelAPI contentPanel;

    protected int contentWidth, contentHeight;
    protected float scrollOffsetX, scrollOffsetY;

    public ScrollPanel(UIPanelAPI parent, int viewportWidth, int viewportHeight) {
        super(parent, viewportWidth, viewportHeight);

        bg.color = new Color(100, 100, 100);
        bg.enabled = false;

        RolfLectionUtil.getMethodAndInvokeDirectly(
            "setClipping", m_panel, true);

        contentWidth = viewportWidth;
        contentHeight = viewportHeight;

        contentPanel = Global.getSettings().createCustom(viewportWidth, viewportHeight, null);
        add(contentPanel).inBL(0, 0);
    }

    public UIPanelAPI getContentPanel() { return contentPanel; }
    public void addToContent(UIComponentAPI comp) {
        contentPanel.addComponent(comp);
    }

    public void setContentWidth(int width) {
        contentWidth = width;
        contentPanel.getPosition().setSize(width, contentPanel.getPosition().getHeight());
    }

    public void setContentHeight(int height) {
        contentHeight = height;
        contentPanel.getPosition().setSize(contentPanel.getPosition().getWidth(), height);
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        super.processInput(events);
        if (!input.hoveredLastFrame) return;
        
        int scrollValue = 0;
        boolean shiftDown = false;

        for (InputEventAPI e : events) {
            if (e.isConsumed()) { continue; }

            if (e.getEventType() == InputEventType.MOUSE_SCROLL) {
                scrollValue = e.getEventValue();
                e.consume();
                continue;
            }

            if ((e.getEventType() == InputEventType.KEY_DOWN || e.getEventType() == InputEventType.KEY_REPEAT) &&
                (e.getEventValue() == Keyboard.KEY_LSHIFT || e.getEventValue() == Keyboard.KEY_RSHIFT)) {
                shiftDown = true;
            }
        }

        if (scrollValue == 0) return;

        final float delta = -scrollValue * scrollSpeed;

        switch (scrollType) {
        case VERTICAL:
            scrollOffsetY = clampScrollY(scrollOffsetY + delta);
            break;
        case HORIZONTAL:
            scrollOffsetX = clampScrollX(scrollOffsetX + delta);
            break;
        case BOTH:
            if (shiftDown) {
                scrollOffsetX = clampScrollX(scrollOffsetX + delta);
            } else {
                scrollOffsetY = clampScrollY(scrollOffsetY + delta);
            }
            break;
        }
        contentPanel.getPosition().inTL(-scrollOffsetX, scrollOffsetY);
    }

    private float clampScrollX(float x) {
        float max = Math.max(0, contentWidth - getPos().getWidth());
        return x < 0 ? 0 : (x > max ? max : x);
    }

    private float clampScrollY(float y) {
        float max = Math.max(0, contentHeight - getPos().getHeight());
        return y < 0 ? 0 : (y > max ? max : y);
    }
}