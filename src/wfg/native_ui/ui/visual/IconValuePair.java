package wfg.native_ui.ui.visual;

import static wfg.native_ui.util.Globals.settings;
import static wfg.native_ui.util.UIConstants.*;

import java.awt.Color;

import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.LabelAPI;

import wfg.native_ui.example.visual.IconValuePairExample;
import wfg.native_ui.internal.ui.core.UIContainer;
import wfg.native_ui.ui.visual.AbstractSpriteElement.SpriteElement;
import wfg.native_ui.util.NumFormat;

/**
 * <p><strong>Example: </strong> {@link IconValuePairExample}</p>
 */
public class IconValuePair extends UIContainer {
    private final SpriteElement mIconBase;
    private final LabelAPI mValueLbl;

    public IconValuePair(float w, float h, String iconID, double value, boolean withX, Color color) {
        this(w, h, settings.getSprite(iconID), value, withX, color);
    }

    public IconValuePair(float w, float h, SpriteAPI icon, double value, boolean withX, Color color) {
        this(w, h, icon, value, withX, color, null);
    }

    public IconValuePair(float w, float h, String iconID, double value, boolean withX, Color color,
        String font
    ) {
        this(w, h, settings.getSprite(iconID), value, withX, color, font);
    }
    
    public IconValuePair(float w, float h, SpriteAPI icon, double value, boolean withX, Color color,
        String font
    ) {
        super(w, h);

        mIconBase = new SpriteElement(h, h, icon, null, null);
        final String valueStr = (withX ? Strings.X : "") + NumFormat.engNotate(value);
        mValueLbl = settings.createLabel(valueStr, font == null ? Fonts.DEFAULT_SMALL : font);
        mValueLbl.setColor(color == null ? highlight : color);
        mValueLbl.getPosition().setSize(w - h - pad, h);
        mValueLbl.setAlignment(Alignment.LMID);

        add(mIconBase).inLMid(0f);
        add(mValueLbl).inRMid(0f);
    }

    public final SpriteElement icon() {
        return mIconBase;
    }

    public final LabelAPI label() {
        return mValueLbl;
    }
}