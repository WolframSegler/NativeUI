package wfg.native_ui.util;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.Misc;

public class UIConstants {
    public static final int pad = 3;
    public static final int hpad = 5;
    public static final int opad = 10; 

    public static final Color text_color = Misc.getTextColor();
    public static final Color highlight = Misc.getHighlightColor();
    public static final Color positive = Misc.getPositiveHighlightColor();
    public static final Color negative = Misc.getNegativeHighlightColor();
    public static final Color base = Misc.getBasePlayerColor();
    public static final Color dark = Misc.getDarkPlayerColor();
    public static final Color grid = Global.getSettings().getFactionSpec(Factions.PLAYER).getGridUIColor();
    public static final Color gray = Misc.getGrayColor();
    public static final Color glowHighlight = Misc.getTooltipTitleAndLightHighlightColor();

    public static final Color btnTxtColor = Misc.getButtonTextColor();
    public static final Color btnBgColorDark = Global.getSettings().getColor("buttonBgDark");
    public static final int BUTTON_W = 150;
    public static final int BUTTON_H = 25;

    public static final float bgAlpha = 0.85f;
}