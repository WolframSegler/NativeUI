package wfg.wrap_ui.ui.systems;

import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.ui.impl.StandardTooltipV2Expandable;

import wfg.wrap_ui.ui.Attachments;
import wfg.wrap_ui.ui.ComponentFactory;
import wfg.wrap_ui.ui.components.InputSnapshot;
import wfg.wrap_ui.ui.components.NativeComponents;
import wfg.wrap_ui.ui.components.TooltipComp;
import wfg.wrap_ui.ui.components.UIContextComp;
import wfg.wrap_ui.ui.panels.CustomPanel;

public final class TooltipSystem<
    PanelType extends CustomPanel<PanelType>
> extends BaseSystem<PanelType> {

    private final TooltipComp spec;
    private final UIContextComp context;
    private final UIPanelAPI parent = Attachments.getScreenPanel();

    public TooltipSystem(PanelType panel) {
        super(panel);

        final var comp = panel.comp();
        comp.setIfNotPresent(NativeComponents.TOOLTIP, new TooltipComp());
        comp.setIfNotPresent(NativeComponents.UI_CONTEXT, new UIContextComp());

        spec = comp.getComp(NativeComponents.TOOLTIP);
        context = comp.getComp(NativeComponents.UI_CONTEXT);
    }

    @Override
    public final void advance(float amount, InputSnapshot input) {
        if (!spec.enabled || !context.isValid()) {
            spec.hoverTime = 0f;
            hideTooltip();
            return;
        }

        if (input.hoveredLastFrame && !input.hasLMBClickedBefore) {
            spec.hoverTime += amount;
            if (spec.hoverTime >= spec.tooltipDelay) {
                showTooltip();
            }
        } else {
            spec.hoverTime = 0f;
            hideTooltip();
        }
    }

    private final void showTooltip() {
        if (spec.tooltip != null) return;

        spec.tooltip = ComponentFactory.createTooltip(spec.tpWidth, spec.useScroller);
        updateTooltip(false);

        if (spec.codexID != null) spec.tooltip.setCodexEntryId(spec.codexID);

        if (spec.tooltip instanceof StandardTooltipV2Expandable expandable) {
            expandable.setShowBorder(true);
            expandable.setShowBackground(true);
            expandable.setBgAlpha(1f);

            if (spec.expandable) expandable.makeExpandable();
            else expandable.makeNonExpandable();

            expandable.setBeforeShowing(() -> updateTooltip(false));
            expandable.setBeforeExpanding(() -> updateTooltip(true));
        }

        ComponentFactory.addTooltip(spec.tooltip, 0f, spec.useScroller, parent);
        updateTooltip(false);
        parent.bringComponentToTop(spec.tooltip);
    }

    private final void hideTooltip() {
        if (spec.tooltip != null) {
            parent.removeComponent(spec.tooltip);
            spec.tooltip = null;
        }
    }

    private final void updateTooltip(boolean expanded) {
        if (spec.tooltip == null) return;
        spec.builder.buildTp(spec.tooltip, expanded);
        spec.positioner.position(spec.tooltip, expanded);
        parent.bringComponentToTop(spec.tooltip);
    }
}