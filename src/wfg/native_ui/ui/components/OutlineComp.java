package wfg.native_ui.ui.components;

import static wfg.native_ui.util.UIConstants.dark;

import java.awt.Color;

public final class OutlineComp extends BaseComponent {
    public OutlineType type = OutlineType.LINE;
    public Color color = dark;

    public static enum OutlineType {
        LINE,
        VERY_THIN,
        THIN,
        MEDIUM,
        THICK,
        TEX_VERY_THIN,
        TEX_THIN,
        TEX_MEDIUM,
        TEX_THICK
    }
}