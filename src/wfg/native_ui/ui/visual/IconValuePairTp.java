package wfg.native_ui.ui.visual;

import java.awt.Color;

import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import wfg.native_ui.ui.component.NativeComponents;
import wfg.native_ui.ui.component.TooltipComp;
import wfg.native_ui.ui.core.UIElementFlags.HasTooltip;

public class IconValuePairTp extends IconValuePair implements HasTooltip {

    public final TooltipComp tooltip = comp().get(NativeComponents.TOOLTIP);
    
    public IconValuePairTp(UIPanelAPI parent, int w, int h, String iconID, double value, boolean withX, Color color) {
        super(parent, w, h, iconID, value, withX, color);
    }
    
    public IconValuePairTp(UIPanelAPI parent, int w, int h, SpriteAPI icon, double value, boolean withX, Color color) {
        super(parent, w, h, icon, value, withX, color);
    }

    public IconValuePairTp(UIPanelAPI parent, int w, int h, String iconID, double value, boolean withX, Color color,
        String font
    ) {
        super(parent, w, h, iconID, value, withX, color, font);
    }
    
    public IconValuePairTp(UIPanelAPI parent, int w, int h, SpriteAPI icon, double value, boolean withX, Color color,
        String font
    ) {
        super(parent, w, h, icon, value, withX, color, font);
    }
}