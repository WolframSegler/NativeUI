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

    private final TooltipComp mSpec;
    private final OutsideEventDetector mDetector = new OutsideEventDetector(this);
    private final UIPanelAPI mParent = Attachments.getScreenPanel();
    private TooltipState mState = TooltipState.SHOWING;

    public static UITooltip create(TooltipComp spec) { return new UITooltip(spec); }

    private UITooltip(TooltipComp spec) {
        super(spec.width - 10f, spec.expandable);
        mSpec = spec;

        setShowBorder(true);
        setShowBackground(true);
        setSelfRemove(false);
        setBgAlpha(spec.bgAlpha);
        setCodexEntryId(spec.codexID);

        mDetector.consumeMouseMove = false;
        mDetector.consumeMouseScroll = false;
        mDetector.triggerByModKey = true;
        mDetector.triggerOnKeyUp = true;
        mDetector.isRemoveOnAction = false;

        createImpl(false);
    }

    @Override
    public void createImpl(boolean expanded) {
        if (expanded) {
            expandString = mSpec.expandTxt == null ? "%s more info" : mSpec.expandTxt;
        } else {
            unexpandString = mSpec.unexpandTxt == null ? "%s hide" : mSpec.unexpandTxt;
        }
        mSpec.builder.buildTp(this, expanded);
    }
    
    public final void attach() {
        beforeShown();

        mDetector.attach();
        ComponentFactory.addTooltip(this, 0f, false, mParent);
        mParent.bringComponentToTop(this);
        getFader().fadeIn();
        mSpec.positioner.position(this, false);

        notifyShown();
    }

    public final void detach() {
        if (mState == TooltipState.FINISHED) return;
        mState = TooltipState.FINISHED;
        mDetector.detach();
        mParent.removeComponent(this);

        notiftyHidden();
    }

    public void dismiss() {
        if (mState != TooltipState.SHOWING) return;
        mState = TooltipState.DISMISSING;
        getFader().fadeOut();
    }

    @Override
    public void advanceImpl(float delta) {
        super.advanceImpl(delta);
        if (mState == TooltipState.DISMISSING && getFader().isFadedOut()) {
            detach();
        }
    }

    public boolean isExpanded() { return expanded; }
    public boolean isFinished() { return mState == TooltipState.FINISHED; }
    public PositionAPI pos() { return ((UIComponentAPI)this).getPosition(); }
    public void outsideClicked(boolean isLeft) {
        mSpec.internal_dismissedThisHover = true; dismiss();
    }
    public void buttonPressed(int lwjgl_key) {
        mSpec.internal_dismissedThisHover = true; dismiss();
    }
}