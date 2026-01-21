package wfg.wrap_ui.ui.components;

import static wfg.wrap_ui.util.UIConstants.opad;

import com.fs.starfarer.api.ui.TooltipMakerAPI;

import wfg.wrap_ui.util.WrapUiUtils;
import wfg.wrap_ui.ui.systems.TooltipSystem;

public final class TooltipComp extends BaseComponent {
    public float tpWidth = 400f;
    public float tooltipDelay = 0.3f;
    public boolean expandable = false;
    public boolean useScroller = false;
    public String codexID = null;

    /** Builds or updates the tooltip contents. REQUIRED. */
    public TooltipBuilder builder;

    /** Positions the tooltip after it has been attached. OPTIONAL. */
    public TooltipPositioner positioner = TooltipPositioner.DEFAULT;


    /** Internal: only used by {@link TooltipSystem}, do not access */
    public TooltipMakerAPI tooltip;
    /** Internal: only used by {@link TooltipSystem}, do not access */
    public float hoverTime = 0f;

    
    @FunctionalInterface
    public static interface TooltipBuilder {
        void buildTp(TooltipMakerAPI tp, boolean expanded);
    }

    @FunctionalInterface
    public static interface TooltipPositioner {
        void position(TooltipMakerAPI tp, boolean expanded);
        TooltipPositioner DEFAULT = (tp, expanded) -> WrapUiUtils.mouseCornerPos(tp, opad);
    }
}