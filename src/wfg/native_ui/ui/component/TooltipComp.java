package wfg.native_ui.ui.component;

import static wfg.native_ui.util.UIConstants.opad;

import com.fs.starfarer.api.ui.TooltipMakerAPI;

import wfg.native_ui.internal.ui.core.UITooltip;
import wfg.native_ui.ui.system.TooltipSystem;
import wfg.native_ui.util.NativeUiUtils;

public final class TooltipComp extends BaseComponent {
    public float width = 400f;
    public float bgAlpha = 1f;
    public boolean expandable = false;
    public String codexID = null;
    public String expandTxt = null;
    public String unexpandTxt = null;

    /** Builds or updates the tooltip content. */
    public TooltipBuilder builder;

    /** Positions the tooltip after attachment. Can be called multiple times. */
    public TooltipPositioner positioner = TooltipPositioner.DEFAULT;

    /** Internal - only used by {@link TooltipSystem}. */
    public UITooltip internal_tp;
    /** Internal - only used by {@link TooltipSystem}. */
    public float internal_hoverTime = 0f;
    /**
     * Frames since this element last rendered with positive alpha.
     * Internal - only used by {@link TooltipSystem}.
     */
    public int internal_lastVisibleFrameCounter;

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