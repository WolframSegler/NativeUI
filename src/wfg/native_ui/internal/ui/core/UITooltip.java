package wfg.native_ui.internal.ui.core;

import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.ui.impl.StandardTooltipV2Expandable;

import wfg.native_ui.internal.ui.interaction.OutsideEventDetector;
import wfg.native_ui.internal.ui.interaction.OutsideEventDetector.OutsideEventListener;
import wfg.native_ui.ui.Attachments;
import wfg.native_ui.ui.ComponentFactory;
import wfg.native_ui.ui.component.TooltipComp;

public class UITooltip extends StandardTooltipV2Expandable implements OutsideEventListener {
    private enum TooltipState { SHOWING, DISMISSING, FINISHED }

    private final TooltipComp spec;
    private final OutsideEventDetector detector = new OutsideEventDetector(this);
    private final UIPanelAPI parent = Attachments.getScreenPanel();
    private TooltipState state = TooltipState.SHOWING;

    public static UITooltip create(TooltipComp spec) { return new UITooltip(spec); }

    private UITooltip(TooltipComp spec) {
        super(spec.width - 10f, spec.expandable);
        this.spec = spec;

        setShowBorder(true);
        setShowBackground(true);
        setSelfRemove(false);
        setBgAlpha(spec.bgAlpha);
        setCodexEntryId(spec.codexID);

        detector.consumeMouseMove = false;
        detector.consumeMouseScroll = false;
        detector.triggerByModKey = true;
        detector.triggerOnKeyUp = true;
        detector.isRemoveOnAction = false;

        createImpl(false);
    }

    @Override
    public void createImpl(boolean expanded) {
        if (expanded) {
            expandString = spec.expandTxt == null ? "%s more info" : spec.expandTxt;
        } else {
            unexpandString = spec.unexpandTxt == null ? "%s hide" : spec.unexpandTxt;
        }
        spec.builder.buildTp(this, expanded);
    }
    
    public final void attach() {
        beforeShown();

        detector.attach();
        ComponentFactory.addTooltip(this, 0f, false, parent);
        parent.bringComponentToTop(this);
        getFader().fadeIn();
        spec.positioner.position(this, false);

        notifyShown();
    }

    public final void detach() {
        if (state == TooltipState.FINISHED) return;
        state = TooltipState.FINISHED;
        detector.detach();
        parent.removeComponent(this);

        notiftyHidden();
    }

    public void dismiss() {
        if (state != TooltipState.SHOWING) return;
        state = TooltipState.DISMISSING;
        getFader().fadeOut();
    }

    @Override
    public void advanceImpl(float delta) {
        super.advanceImpl(delta);
        if (state == TooltipState.DISMISSING && getFader().isFadedOut()) {
            detach();
        }
    }

    public boolean isExpanded() { return expanded; }
    public boolean isFinished() { return state == TooltipState.FINISHED; }
    public PositionAPI pos() { return ((UIComponentAPI)this).getPosition(); }
    public void outsideClicked(boolean isLeft) { spec.internal_dismissedThisHover = true; dismiss(); }
    public void buttonPressed(int lwjgl_key) { spec.internal_dismissedThisHover = true; dismiss(); }
}