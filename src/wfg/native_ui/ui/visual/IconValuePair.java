package wfg.native_ui.ui.visual;

import static wfg.native_ui.util.Globals.settings;
import static wfg.native_ui.util.UIConstants.*;

import java.awt.Color;

import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import wfg.native_ui.ui.panel.CustomPanel;
import wfg.native_ui.ui.visual.SpritePanel.Base;
import wfg.native_ui.util.NumFormat;

public class IconValuePair extends CustomPanel {
    private final Base iconBase;
    private final LabelAPI valueLbl;

    public IconValuePair(UIPanelAPI parent, int w, int h, String iconID, double value, boolean withX, Color color) {
        this(parent, w, h, settings.getSprite(iconID), value, withX, color);
    }

    public IconValuePair(UIPanelAPI parent, int w, int h, SpriteAPI icon, double value, boolean withX, Color color) {
        this(parent, w, h, icon, value, withX, color, null);
    }

    public IconValuePair(UIPanelAPI parent, int w, int h, String iconID, double value, boolean withX, Color color,
        String font
    ) {
        this(parent, w, h, settings.getSprite(iconID), value, withX, color, font);
    }
    
    public IconValuePair(UIPanelAPI parent, int w, int h, SpriteAPI icon, double value, boolean withX, Color color,
        String font
    ) {
        super(parent, w, h);

        iconBase = new Base(m_panel, h, h, icon, null, null);
        final String valueStr = (withX ? Strings.X : "") + NumFormat.engNotate(value);
        valueLbl = settings.createLabel(valueStr, font == null ? Fonts.DEFAULT_SMALL : font);
        valueLbl.setColor(color == null ? highlight : color);
        valueLbl.getPosition().setSize(w - h - pad, h);
        valueLbl.setAlignment(Alignment.LMID);

        add(iconBase).inBL(0f, 0f);
        add(valueLbl).inBR(0f, 0f);
    }

    public final Base icon() {
        return iconBase;
    }

    public final LabelAPI label() {
        return valueLbl;
    }
}