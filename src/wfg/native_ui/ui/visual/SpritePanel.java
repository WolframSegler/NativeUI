package wfg.native_ui.ui.visual;

import static wfg.native_ui.util.Globals.settings;

import java.awt.Color;

import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import wfg.native_ui.ui.component.NativeComponents;
import wfg.native_ui.ui.component.OutlineComp;
import wfg.native_ui.ui.component.OutlineComp.OutlineType;
import wfg.native_ui.ui.core.UIElementFlags.HasOutline;
import wfg.native_ui.ui.panel.CustomPanel;
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
> extends CustomPanel implements HasOutline {
    public final OutlineComp outline = comp().get(NativeComponents.OUTLINE);

    public boolean drawTextureHalo = false;
    public Color fillColor;
    public Color texColor = Color.WHITE;
    public Color texHaloColor = Color.GREEN;
    
    protected SpriteAPI m_sprite;

    public SpritePanel(UIPanelAPI parent, int width, int height, String spriteID,
        Color color, Color fillColor
    ) {
        this(parent, width, height, settings.getSprite(spriteID), color, fillColor);
    }

    public SpritePanel(UIPanelAPI parent, int width, int height, SpriteAPI sprite,
        Color color, Color fillColor
    ) {
        super(parent, width, height);

        outline.enabled = false;
        outline.type = OutlineType.VERY_THIN;

        m_sprite = sprite;
        this.fillColor = fillColor;

        if (color != null) texColor = color;
    }

    @Override
    public void renderBelow(float alpha) {
        super.renderBelow(alpha);
        
        final float x = pos.getX();
        final float y = pos.getY();
        final float w = pos.getWidth();
        final float h = pos.getHeight();

        if (fillColor != null) {
            RenderUtils.drawQuad(x, y, w, h, fillColor, alpha, false);
        }

        if (m_sprite == null) return;

        if (drawTextureHalo && texHaloColor != null) {
            RenderUtils.drawSpriteOutline(
                m_sprite, texHaloColor, x, y, w, h, alpha, 2
            );
        }

        m_sprite.setAlphaMult(alpha);
        m_sprite.setColor(texColor);
        m_sprite.setSize(w, h);
        m_sprite.render(x, y);
    }

    public SpriteAPI getSprite() { return m_sprite; }
    public void setSprite(SpriteAPI sprite) {
        m_sprite = sprite;
    }
    public void setSprite(String spriteID) {
        m_sprite = settings.getSprite(spriteID);
    }

    public static class Base extends SpritePanel<Base> {
        public Base(UIPanelAPI parent, int width, int height, String spriteID, Color color,
            Color fillColor
        ) { super(parent, width, height, spriteID, color, fillColor); }

        public Base(UIPanelAPI parent, int width, int height, SpriteAPI sprite, Color color,
            Color fillColor
        ) { super(parent, width, height, sprite, color, fillColor); }
    }
}