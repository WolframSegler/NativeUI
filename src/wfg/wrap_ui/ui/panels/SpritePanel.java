package wfg.wrap_ui.ui.panels;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import wfg.wrap_ui.ui.panels.CustomPanel.HasOutline;
import wfg.wrap_ui.ui.plugins.SpritePanelPlugin;
import wfg.wrap_ui.ui.systems.OutlineSystem.Outline;
import wfg.wrap_ui.util.RenderUtils;


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
 * SpritePanel.Base sprite = new SpritePanel.Base(parent, 64, 64, plugin, "ui/icons/sprite", Color.WHITE, null, true);
 * sprite.setOutlineColor(Color.RED);
 * 
 * panel.addComponent(sprite.getPanel());
 * </pre>
 */
public class SpritePanel<
    PanelType extends SpritePanel<PanelType>
> extends CustomPanel<SpritePanelPlugin<PanelType>, PanelType> 
    implements HasOutline{

    public static class Base extends SpritePanel<Base> {
        public Base(UIPanelAPI parent, int width, int height, String spriteID, Color color,
            Color fillColor
        ) {
            super(parent, width, height, spriteID, color, fillColor);
        }
    }

    public boolean drawBorder = false;
    public boolean drawTextureHalo = false;
    
    public Color color;
    public Color outlineColor;
    public Color fillColor;
    public Color texHaloColor = Color.GREEN;

    protected SpriteAPI m_sprite;

    @SuppressWarnings("unchecked")
    public SpritePanel(UIPanelAPI parent, int width, int height, String spriteID,
        Color color, Color fillColor
    ) {
        super(parent, width, height, new SpritePanelPlugin<>());

        m_sprite = Global.getSettings().getSprite(spriteID);
        this.color = color;
        this.fillColor = fillColor;

        getPlugin().init((PanelType)this);
    }

    public void renderImpl(float alphaMult) {
        if (m_sprite == null) return;

        if (color != null) m_sprite.setColor(color);

        final PositionAPI pos = getPos();
        final float x = pos.getX();
        final float y = pos.getY();
        final float w = pos.getWidth();
        final float h = pos.getHeight();

        if (fillColor != null) {
            m_sprite.setColor(fillColor);
            RenderUtils.drawQuad(x, y, w, h, fillColor, alphaMult, false);
        }

        if (drawTextureHalo && texHaloColor != null) {
            RenderUtils.drawSpriteOutline(
                m_sprite, texHaloColor, x, y, w, h, alphaMult, 2
            );
        }

        m_sprite.setSize(w, h);
        m_sprite.render(x, y);
    }

    @Override
    public Outline getOutline() {
        return drawBorder ? Outline.VERY_THIN : Outline.NONE;
    }

    @Override
    public Color getOutlineColor() {
        return outlineColor;
    }

    public void setSprite(String spriteID) {
        m_sprite = Global.getSettings().getSprite(spriteID);
    }
    
    public void setSprite(SpriteAPI sprite) {
        m_sprite = sprite;
    }

    public SpriteAPI getSprite() { return m_sprite; }
    public void createPanel() {}
}