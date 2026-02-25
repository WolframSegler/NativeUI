package wfg.native_ui.ui.core;

import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.ui.impl.StandardTooltipV2Expandable;

import wfg.native_ui.internal.ui.panel.OutsideEventDetector;
import wfg.native_ui.internal.ui.panel.OutsideEventDetector.OutisdeEventListener;
import wfg.native_ui.ui.Attachments;
import wfg.native_ui.ui.ComponentFactory;

public class UITooltip extends StandardTooltipV2Expandable implements OutisdeEventListener {
    private final OutsideEventDetector detector;
    private final UIPanelAPI parent = Attachments.getScreenPanel();
    private final boolean useScroller;

    public UITooltip(float width, boolean expandable, boolean useScroller) {
        super(width - (useScroller ? 5f : 0f), expandable);
        this.useScroller = useScroller;

        detector = new OutsideEventDetector(this);
        detector.consumeMouseMove = false;
        detector.consumeMouseScroll = false;
        detector.triggerByModKey = true;
    }

    public final void detach() {
        detector.detach();
        parent.removeComponent(this);
    }

    public final void attach() {
        detector.attach();
        ComponentFactory.addTooltip(this, 0f, useScroller, parent);
        parent.bringComponentToTop(this);
    }

    public PositionAPI getPos() { return ((UIComponentAPI)this).getPosition(); }
    public void buttonPressed(int lwjgl_key) {
        detach();
    }
    public void outsideClicked(boolean isLeft) {
        detach();
    }
    public void createImpl(boolean expanded) {}
}