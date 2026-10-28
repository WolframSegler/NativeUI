package wfg.native_ui.internal.ui.core;

import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.ui.impl.StandardTooltipV2Expandable;

import wfg.native_ui.internal.ui.interaction.OutsideEventDetector;
import wfg.native_ui.internal.ui.interaction.OutsideEventDetector.OutsideEventListener;
import wfg.native_ui.ui.Attachments;
import wfg.native_ui.ui.ComponentFactory;

public class UITooltip extends StandardTooltipV2Expandable implements OutsideEventListener {
    private final OutsideEventDetector detector = new OutsideEventDetector(this);
    private final UIPanelAPI parent = Attachments.getScreenPanel();

    private boolean pendingDetach = false;
    private boolean detached = false;

    public UITooltip(float width, boolean expandable) {
        super(width, expandable);

        detector.consumeMouseMove = false;
        detector.consumeMouseScroll = false;
        detector.triggerByModKey = true;
        detector.triggerOnKeyUp = true;
        detector.isRemoveOnAction = false;

        setShowBorder(true);
        setShowBackground(true);
        setSelfRemove(true);
    }

    public void attachAndFadeIn() {
        attach();
        getFader().fadeIn();
        pendingDetach = false;
        detached = false;
    }

    public void fadeOutAndHide() {
        getFader().fadeOut();
        pendingDetach = true;
    }

    public void updateFader(float delta) {
        if (pendingDetach && getFader().isFadedOut()) {
            detach();
        }
    }

    public final void detach() {
        detector.detach();
        parent.removeComponent(this);

        pendingDetach = false;
        detached = true;
        notiftyHidden();
    }

    public final void attach() {
        detector.attach();
        ComponentFactory.addTooltip(this, 0f, false, parent);
        parent.bringComponentToTop(this);
    }

    public boolean isExpanded() { return expanded; }
    public boolean isDetached() { return detached; }
    public PositionAPI pos() { return ((UIComponentAPI)this).getPosition(); }
    public void outsideClicked(boolean isLeft) { detach(); }
    public void buttonPressed(int lwjgl_key) { detach(); }
    public void createImpl(boolean expanded) {}
}