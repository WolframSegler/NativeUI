package wfg.native_ui.ui.visual;

import static wfg.native_ui.util.Globals.settings;

import java.awt.Color;

import com.fs.starfarer.api.graphics.SpriteAPI;

import wfg.native_ui.internal.ui.core.UIEntity;
import wfg.native_ui.ui.component.NativeComponents;
import wfg.native_ui.ui.component.OutlineComp;
import wfg.native_ui.ui.component.OutlineComp.OutlineType;
import wfg.native_ui.ui.core.UIElementFlags.HasOutline;
import wfg.native_ui.ui.system.BaseSystem;
import wfg.native_ui.util.RenderUtils;


/**
 * {@link AbstractSpriteElement} is a UI panel for displaying a {@link SpriteAPI} with
 * optional coloring and border outline. It implements {@link HasOutline}.
 * 
 * <p><b>Usage:</b>
 * <ul>
 *   <li>To subclass and customize, extend {@link AbstractSpriteElement} with your own {@code PanelType}.</li>
 *   <li>To directly instantiate a generic panel without subclassing, use the inner {@link SpriteElement} class.</li>
 * </ul>
 * 
 * <p><b>Example:</b>
 * <pre>
 * SpriteElement sprite = new SpriteElement(64, 64, "ui/icons/sprite", Color.WHITE, null);
 * sprite.outline.color = Color.RED;
 * 
 * panel.addComponent(sprite.getPanel());
 * </pre>
 */
public class AbstractSpriteElement<
    PanelType extends AbstractSpriteElement<PanelType>
> extends UIEntity implements HasOutline {
    public final OutlineComp outline = comp().get(NativeComponents.OUTLINE);

    public boolean drawTextureHalo = false;
    public Color fillColor;
    public Color texColor = Color.WHITE;
    public Color texHaloColor = Color.GREEN;
    
    protected SpriteAPI mSprite;

    public AbstractSpriteElement(float width, float height, String spriteID,
        Color color, Color fillColor
    ) {
        this(width, height, settings.getSprite(spriteID), color, fillColor);
    }

    public AbstractSpriteElement(float width, float height, SpriteAPI sprite,
        Color color, Color fillColor
    ) {
        super(width, height);

        outline.enabled = false;
        outline.type = OutlineType.VERY_THIN;

        mSprite = sprite;
        this.fillColor = fillColor;

        if (color != null) texColor = color;
    }

    @Override
    public void renderImpl(float alpha) {
        for (BaseSystem system : system().getAll()) {
            system.renderBelow(this, alpha);
        }
        
        renderSprite(alpha);

        for (BaseSystem system : system().getAll()) {
            system.renderAbove(this, alpha);
        }
    }

    private final void renderSprite(float alpha) {
        final float x = getX();
        final float y = getY();
        final float w = getWidth();
        final float h = getHeight();

        if (fillColor != null) {
            RenderUtils.drawQuad(x, y, w, h, fillColor, alpha, false);
        }

        if (mSprite == null) return;

        if (drawTextureHalo && texHaloColor != null) {
            RenderUtils.drawSpriteOutline(
                mSprite, texHaloColor, x, y, w, h, alpha, 2
            );
        }

        mSprite.setAlphaMult(alpha);
        mSprite.setColor(texColor);
        mSprite.setSize(w, h);
        mSprite.render(x, y);
    }

    public SpriteAPI getSprite() { return mSprite; }
    public void setSprite(SpriteAPI sprite) {
        mSprite = sprite;
    }
    public void setSprite(String spriteID) {
        mSprite = settings.getSprite(spriteID);
    }

    public static class SpriteElement extends AbstractSpriteElement<SpriteElement> {
        public SpriteElement(float width, float height, String spriteID, Color color,
            Color fillColor
        ) { super(width, height, spriteID, color, fillColor); }

        public SpriteElement(float width, float height, SpriteAPI sprite, Color color,
            Color fillColor
        ) { super(width, height, sprite, color, fillColor); }
    }
}