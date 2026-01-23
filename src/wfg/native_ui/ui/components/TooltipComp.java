package wfg.native_ui.ui.components;

import static wfg.native_ui.util.UIConstants.opad;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import wfg.native_ui.ui.Attachments;
import wfg.native_ui.ui.systems.TooltipSystem;
import wfg.native_ui.util.NativeUiUtils;

public final class TooltipComp extends BaseComponent {
    public float width = 400f;
    public float delay = 0.3f;
    public float bgAlpha = 1f;
    public boolean expandable = false;
    public boolean useScroller = false;
    public UIPanelAPI parent = Attachments.getScreenPanel();
    public String codexID = null;
    public String expandTxt = null;
    public String unexpandTxt = null;

    /** Builds or updates the tooltip contents. REQUIRED. */
    public TooltipBuilder builder;

    /** Positions the tooltip after it has been attached. OPTIONAL. */
    public TooltipPositioner positioner = TooltipPositioner.DEFAULT;


    /** Internal: only used by {@link TooltipSystem}, do not access */
    public TooltipMakerAPI tp_internal;
    /** Internal: only used by {@link TooltipSystem}, do not access */
    public float hoverTime_internal = 0f;

    
    @FunctionalInterface
    public static interface TooltipBuilder {
        void buildTp(TooltipMakerAPI tp, boolean expanded);
    }

    @FunctionalInterface
    public static interface TooltipPositioner {
        void position(TooltipMakerAPI tp, boolean expanded);
        TooltipPositioner DEFAULT = (tp, expanded) -> NativeUiUtils.mouseCornerPos(tp, opad);
    }
}