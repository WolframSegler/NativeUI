package wfg.native_ui.ui.panels;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import wfg.native_ui.ui.components.LayoutOffsetComp;
import wfg.native_ui.ui.components.NativeComponents;
import wfg.native_ui.ui.components.OutlineComp;
import wfg.native_ui.ui.components.UIContextComp;
import wfg.native_ui.ui.components.OutlineComp.OutlineType;
import wfg.native_ui.ui.core.UIElementFlags.HasOutline;
import wfg.native_ui.ui.core.UIElementFlags.HasUIContext;
import wfg.native_ui.util.RenderUtils;


/**
 * {@link SpritePanel} is a UI panel specialized for displaying a single sprite with optional coloring
 * and border outline. It extends {@link CustomPanel} and implements {@link HasOutline} to
 * provide configurable visual appearance with minimal setup.
 * 
 * <p><b>Key features:</b>
 * <ul>
 *   <li>Holds and displays a sprite loaded from a sprite ID or directly assigned {@link SpriteAPI}.</li>
 *   <li>Supports setting primary color, fill color, and toggle for drawing a very thin border outline.</li>
 *   <li>Allows dynamic sprite and color updates via setter methods.</li>
 * </ul>
 * 
 * <p><b>Usage:</b>
 * <ul>
 *   <li>To subclass and customize, extend {@link SpritePanel} with your own {@code PanelType}.</li>
 *   <li>To directly instantiate a generic panel without subclassing, use the inner static {@link Base} class.</li>
 * </ul>
 * 
 * <p><b>Example:</b>
 * <pre>
 * SpritePanel.Base sprite = new SpritePanel.Base(parent, 64, 64, "ui/icons/sprite", Color.WHITE, null);
 * sprite.outline.color = Color.RED;
 * 
 * panel.addComponent(sprite.getPanel());
 * </pre>
 */
public class SpritePanel<
    PanelType extends SpritePanel<PanelType>
> extends CustomPanel<PanelType> implements HasOutline, HasUIContext {

    public final OutlineComp outline = comp().get(NativeComponents.OUTLINE);
    public final LayoutOffsetComp offset = comp().get(NativeComponents.LAYOUT_OFFSET);
    public final UIContextComp context = comp().get(NativeComponents.UI_CONTEXT);

    public boolean drawTextureHalo = false;
    public Color fillColor;
    public Color texColor = Color.WHITE;
    public Color texHaloColor = Color.GREEN;
    
    protected SpriteAPI m_sprite;

    public SpritePanel(UIPanelAPI parent, int width, int height, String spriteID,
        Color color, Color fillColor
    ) {
        super(parent, width, height);

        outline.enabled = false;
        outline.type = OutlineType.VERY_THIN;

        m_sprite = Global.getSettings().getSprite(spriteID);
        this.fillColor = fillColor;

        if (color != null) texColor = color;

    }
    public void createPanel() {}

    @Override
    public void positionChanged(PositionAPI position) {
        super.positionChanged(position);

        m_sprite.setSize(pos.getWidth(), pos.getHeight());
    }

    @Override
    public void renderBelow(float alpha) {
        super.renderBelow(alpha);
        if (m_sprite == null) return;

        final float x = pos.getX();
        final float y = pos.getY();
        final float w = pos.getWidth();
        final float h = pos.getHeight();

        if (fillColor != null) {
            RenderUtils.drawQuad(x, y, w, h, fillColor, alpha, false);
        }

        if (drawTextureHalo && texHaloColor != null) {
            RenderUtils.drawSpriteOutline(
                m_sprite, texHaloColor, x, y, w, h, alpha, 2
            );
        }

        m_sprite.setAlphaMult(alpha);
        m_sprite.setColor(texColor);
        m_sprite.render(x, y);
    }

    public SpriteAPI getSprite() { return m_sprite; }
    public void setSprite(SpriteAPI sprite) {
        m_sprite = sprite;
    }
    public void setSprite(String spriteID) {
        m_sprite = Global.getSettings().getSprite(spriteID);
    }

    public static class Base extends SpritePanel<Base> {
        public Base(UIPanelAPI parent, int width, int height, String spriteID, Color color,
            Color fillColor
        ) { super(parent, width, height, spriteID, color, fillColor); }
    }
}