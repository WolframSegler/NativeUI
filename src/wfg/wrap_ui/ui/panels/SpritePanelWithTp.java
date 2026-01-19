package wfg.wrap_ui.ui.panels;

import java.awt.Color;
import java.util.Optional;

import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.FaderUtil;

import wfg.wrap_ui.ui.panels.CustomPanel.HasAudioFeedback;
import wfg.wrap_ui.ui.panels.CustomPanel.HasFader;
import wfg.wrap_ui.ui.panels.CustomPanel.HasTooltip;

/**
 * A generic sprite panel with tooltip and fading support.
 *
 * <p>This class is designed to be subclassed or instantiated anonymously. The default implementation
 * provides a template, but the actual functionality should be defined by the subclass or anonymous
 * implementation. Users must override {@link #getTpParent()}, and {@link #createAndAttachTp()} to
 * have a working tooltip.</p>
 *
 * <p>Intended usage example:
 * <pre>{@code
 * SpritePanelWithTp panel = new SpritePanelWithTp(parent, width, height, plugin,
 *                                               "iconPath", color, fillColor, drawBorder) {
 *      @Override
 *      public CustomPanelAPI getTpParent() {
 *          return getPanel();
 *      }
 * }</pre>
 * <pre>{@code
 *      
 *      @Override
 *      public TooltipMakerAPI createAndAttachTp() {
 *          TooltipMakerAPI tp = ComponentFactory.createTooltip(300, false);
 *          tp.addPara("Example text", 3);
 *          
 *          ComponentFactory.addTooltip(tp, 20, false, m_panel).inBR(...);
 *          return tp;
 *      }
 * };
 * }</pre></p>
 *
 * <p>By default, the glow color is white and tooltip methods return null</p>
 */
public class SpritePanelWithTp extends SpritePanel<SpritePanelWithTp>
    implements HasTooltip, HasFader, HasAudioFeedback
{
    public final FaderUtil fader = new FaderUtil(0, 0, 0.2f, true, true);

    public SpritePanelWithTp(UIPanelAPI parent, int width, int height, String spriteID,
        Color color, Color fillColor
    ) {
        super(parent, width, height, spriteID, color, fillColor);
    }

    @Override
    public Optional<SpriteAPI> getAdditiveSprite() {
        return Optional.of(m_sprite);
    }

    public UIPanelAPI getTpParent() { return null; }
    public TooltipMakerAPI createAndAttachTp() { return null; }
    public FaderUtil getFader() { return fader; }
}