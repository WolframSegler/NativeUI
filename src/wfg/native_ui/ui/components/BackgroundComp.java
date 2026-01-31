package wfg.native_ui.ui.components;

import static wfg.native_ui.util.UIConstants.bgAlpha;

import java.awt.Color;

public final class BackgroundComp extends BaseComponent {
    public Color color = new Color(0, 0, 0, 255);
    public float alpha = bgAlpha;
    public final LayoutOffset offset = new LayoutOffset();
}