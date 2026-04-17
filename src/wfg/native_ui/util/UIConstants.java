package wfg.native_ui.util;

import static wfg.native_ui.util.Globals.settings;

import java.awt.Color;

import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.Misc;

public class UIConstants {
    private UIConstants() {}

    public static final int pad = 3;
    public static final int hpad = 5;
    public static final int opad = 10; 

    public static final int screenW = (int) settings.getScreenWidth();
    public static final int screenH = (int) settings.getScreenHeight();

    public static final Color text_color = Misc.getTextColor();
    public static final Color highlight = Misc.getHighlightColor();
    public static final Color positive = Misc.getPositiveHighlightColor();
    public static final Color negative = Misc.getNegativeHighlightColor();
    public static final Color base = Misc.getBasePlayerColor();
    public static final Color dark = Misc.getDarkPlayerColor();
    public static final Color grid = settings.getFactionSpec(Factions.PLAYER).getGridUIColor();
    public static final Color gray = Misc.getGrayColor();
    public static final Color glowHighlight = Misc.getTooltipTitleAndLightHighlightColor();

    public static final Color btnTxtColor = Misc.getButtonTextColor();
    public static final Color btnBgColorDark = settings.getColor("buttonBgDark");
    public static final int BUTTON_W = 150;
    public static final int BUTTON_H = 25;

    public static final float bgAlpha = 0.85f;

    public static final String UI_BORDER_1 = "ui_border1";
    public static final String UI_BORDER_2 = "ui_border2";
    public static final String UI_BORDER_3 = "ui_border3";
    public static final String UI_BORDER_4 = "ui_border4";
}