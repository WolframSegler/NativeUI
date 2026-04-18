package wfg.native_ui.util;

import static wfg.native_ui.util.Globals.settings;
import static wfg.native_ui.util.UIConstants.*;

import java.awt.Color;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.ui.impl.StandardTooltipV2;

import rolflectionlib.util.RolfLectionUtil;

public class NativeUiUtils {
    public static final int APICodexHeight = 28;

    public static final Object expandLabelField = RolfLectionUtil.getFieldByName(
        "expandLabel", StandardTooltipV2.class
    );

    public static final void resetFlowLeft(final TooltipMakerAPI tooltip, float gap) {
        final float prevHeight = tooltip.getHeightSoFar();
        final LabelAPI alignReset = tooltip.addPara("", 0f);
        alignReset.getPosition().inTL(gap, prevHeight);
        tooltip.setHeightSoFar(prevHeight);
    }

    /**
     * This function assumes that the sprite is pointing right.
     * In other words, it's directed towards the positive x-axis in Hyperspace.
     */
    public static final void rotateSprite(Vector2f origin, Vector2f target, SpriteAPI sprite) {
        final float angleDegrees = (float) Math.toDegrees(Math.atan2(
            target.y - origin.y, target.x - origin.x
        ));

        sprite.setAngle(angleDegrees);
    }

    /**
     * values below 1 lower brightness. Values above 1 increase it.
     */
    public static final Color adjustBrightness(Color base, float factor) {
        final int r = Math.min(255, (int) (base.getRed() * factor));
        final int g = Math.min(255, (int) (base.getGreen() * factor));
        final int b = Math.min(255, (int) (base.getBlue() * factor));
        
        return new Color(r, g, b, base.getAlpha());
    }

    /**
     * @param t Must be between 0 and 1
     */
    public static final Color lerpColor(Color c1, Color c2, float t) {
        final int r = (int) (c1.getRed() + t * (c2.getRed() - c1.getRed()));
        final int g = (int) (c1.getGreen() + t * (c2.getGreen() - c1.getGreen()));
        final int b = (int) (c1.getBlue() + t * (c2.getBlue() - c1.getBlue()));
        final int a = (int) (c1.getAlpha() + t * (c2.getAlpha() - c1.getAlpha()));

        return new Color(r, g, b, a);
    }

    /**
     * Anchors a panel relative to another panel via {@link #anchorPanel} and then clamps
     * the result to stay within screen bounds.
     */
    public static final void anchorPanelWithBounds(
        UIComponentAPI panel, UIComponentAPI anchor, AnchorType type, int gap
    ) {
        if (panel == null || anchor == null) return;

        AnchorPanelOffset offsets = anchorPanel(panel, anchor, type, gap);

        final PositionAPI Ppos = panel.getPosition();
        final int panelX = (int) Ppos.getX();
        final int panelY = (int) Ppos.getY();
        final int panelW = (int) Ppos.getWidth();
        final int panelH = (int) Ppos.getHeight();

        float offsetX = 0;
        if (panelX < 0) {
            offsetX = opad + (-panelX);
        }
        else if (panelX + panelW > screenW) {
            offsetX = screenW - (panelX + panelW + opad);
        }

        float offsetY = 0;
        if (panelY < 0) {
            offsetY = opad + (-panelY);
            if (panel instanceof TooltipMakerAPI tp) {
                if (tp.getCodexEntryId() != null) {
                    offsetY += APICodexHeight;
                }
            }
        } else if (panelY + panelH > screenH) {
            offsetY = screenH - (panelY + panelH + opad);
        }

        Ppos.inBL(offsets.x + offsetX, offsets.y + offsetY);
    }

    protected static class AnchorPanelOffset {
        public final float x;
        public final float y;
        public AnchorPanelOffset(float x, float y) {
            this.x = x; this.y = y;
        }
    }

    /**
     * Small utility to anchor the panel without actually using PositionAPI anchors.
     * Makes UI lifecycle dependencies easier to manage.
     * Does not handle screen bounds or overflow.
     */
    public static final AnchorPanelOffset anchorPanel(UIComponentAPI panel, UIComponentAPI anchor, AnchorType type, int gap) {

        if (panel == null || anchor == null) return null;

        final PositionAPI Ppos = panel.getPosition();
        final PositionAPI Apos = anchor.getPosition();

        Ppos.inBL(0, 0); // Reset the position. It's still relative
        final float panelX = Ppos.getX();
        final float panelY = Ppos.getY();
        final float panelW = Ppos.getWidth();
        final float panelH = Ppos.getHeight();

        final float anchorX = Apos.getX();
        final float anchorY = Apos.getY();
        final float anchorW = Apos.getWidth();
        final float anchorH = Apos.getHeight();

        int widthCompensation = 0;

        if (panel instanceof TooltipMakerAPI ^ anchor instanceof TooltipMakerAPI) {
            boolean panelIsTooltip = panel instanceof TooltipMakerAPI;
            TooltipMakerAPI tooltip = panelIsTooltip ? 
                (TooltipMakerAPI) panel : (TooltipMakerAPI) anchor;
            int comp = (int) ((tooltip.getPosition().getWidth() - tooltip.getWidthSoFar()) / 2f);
            widthCompensation = panelIsTooltip ? comp : -comp;
        }

        int heightCompensation = 0;

        if (panel instanceof TooltipMakerAPI tp) {
            if (tp.getCodexEntryId() != null) {
                heightCompensation = APICodexHeight / 2;
            }
        } 

        final float offsetX;
        final float offsetY;
        
        switch (type) {
        case LeftTop:
            offsetX = anchorX - panelX - panelW - gap + widthCompensation;
            offsetY = anchorY + anchorH - panelY - panelH;
            break;

        case LeftMid:
            offsetX = anchorX - panelX - panelW - gap + widthCompensation;
            offsetY = anchorY - panelY + (anchorH - panelH) / 2f - heightCompensation;
            break;

        case LeftBottom:
            offsetX = anchorX - panelX - panelW - gap + widthCompensation;
            offsetY = anchorY - panelY - heightCompensation;
            break;

        default: case RightTop:
            offsetX = anchorX + anchorW - panelX + gap - widthCompensation;
            offsetY = anchorY + anchorH - panelY - panelH;
            break;

        case RightMid:
            offsetX = anchorX + anchorW - panelX + gap - widthCompensation;
            offsetY = anchorY - panelY + (anchorH - panelH) / 2f - heightCompensation;
            break;

        case RightBottom:
            offsetX = anchorX + anchorW - panelX + gap - widthCompensation;
            offsetY = anchorY - panelY - heightCompensation;
            break;

        case TopLeft:
            offsetX = anchorX - panelX - widthCompensation;
            offsetY = anchorY + anchorH - panelY + gap;
            break;

        case TopMid:
            offsetX = anchorX - panelX + (anchorW - panelW - widthCompensation*2) / 2f;
            offsetY = anchorY + anchorH - panelY + gap;
            break;

        case TopRight:
            offsetX = anchorX + anchorW - panelX - panelW + widthCompensation;
            offsetY = anchorY + anchorH - panelY + gap;
            break;

        case BottomLeft:
            offsetX = anchorX - panelX - widthCompensation;
            offsetY = anchorY - panelY - panelH - gap;
            break;

        case BottomMid:
            offsetX = anchorX - panelX + (anchorW - panelW - widthCompensation*2) / 2f;
            offsetY = anchorY - panelY - panelH - gap;
            break;

        case BottomRight:
            offsetX = anchorX + anchorW - panelX - panelW + widthCompensation;
            offsetY = anchorY - panelY - panelH - gap;
            break;

        case MidTopLeft:
            offsetX = anchorX + (anchorW / 2f) - panelX + gap - widthCompensation;
            offsetY = anchorY + (anchorH / 2f) - panelY - panelH;
            break;

        case MidTopRight:
            offsetX = anchorX + (anchorW / 2f) - panelX - panelW + gap - widthCompensation;
            offsetY = anchorY + (anchorH / 2f) - panelY - panelH;
            break;

        case MidBottomLeft:
            offsetX = anchorX + (anchorW / 2f) - panelX + gap - widthCompensation;
            offsetY = anchorY + (anchorH / 2f) - panelY;
            break;

        case MidBottomRight:
            offsetX = anchorX + (anchorW / 2f) - panelX - panelW + gap - widthCompensation;
            offsetY = anchorY + (anchorH / 2f) - panelY;
            break;
        }

        Ppos.inBL(offsetX, offsetY);

        return new AnchorPanelOffset(offsetX, offsetY);
    }

    /**
     * Defines anchor positions for UI panel alignment relative to a reference component.
     * <p>
     * The enum names consist of two parts:
     * </p>
     * <ol>
     *   <li><b>Direction</b> - The first word indicates the direction from the anchor component where the panel will be placed:
     *     <ul>
     *       <li><code>Left</code>: Panel is positioned to the left side of the anchor.</li>
     *       <li><code>Right</code>: Panel is positioned to the right side of the anchor.</li>
     *       <li><code>Top</code>: Panel is positioned above the anchor.</li>
     *       <li><code>Bottom</code>: Panel is positioned below the anchor.</li>
     *       <li><code>Mid</code>: Panel is positioned to the vertical center line of the anchor.</li>
     *     </ul>
     *   </li>
     *   <li><b>Alignment</b> - The second word indicates the alignment along the axis perpendicular to the direction:
     *     <ul>
     *       <li>For <code>Left</code> and <code>Right</code> directions, alignment is vertical:
     *         <ul>
     *           <li><code>Top</code>: Align panel's top edge with anchor's top edge.</li>
     *           <li><code>Mid</code>: Align panel's vertical center with anchor's vertical center.</li>
     *           <li><code>Bottom</code>: Align panel's bottom edge with anchor's bottom edge.</li>
     *         </ul>
     *       </li>
     *       <li>For <code>Top</code> and <code>Bottom</code> directions, alignment is horizontal:
     *         <ul>
     *           <li><code>Left</code>: Align panel's left edge with anchor's left edge.</li>
     *           <li><code>Mid</code>: Align panel's horizontal center with anchor's horizontal center.</li>
     *           <li><code>Right</code>: Align panel's right edge with anchor's right edge.</li>
     *         </ul>
     *       </li>
     *       <li>For <code>Mid</code> direction, alignment is Omnidirectional:
     *         <ul>
     *           <li><code>TopLeft</code>: Align panel's top left corner with anchor's center.</li>
     *           <li><code>TopRight</code>: Align panel's top right corner with anchor's center.</li>
     *           <li><code>BottomLeft</code>: Align panel's bottom left corner with anchor's center.</li>
     *           <li><code>BottomRight</code>: Align panel's bottom right corner with anchor's center.</li>
     *         </ul>
     *       </li>
     *     </ul>
     *   </li>
     * </ol>
     */
    public enum AnchorType {
        LeftTop,
        LeftMid,
        LeftBottom,
        RightTop,
        RightMid,
        RightBottom,
        TopLeft,
        TopMid,
        TopRight,
        BottomLeft,
        BottomMid,
        BottomRight,
        MidTopLeft,
        MidTopRight,
        MidBottomLeft,
        MidBottomRight
    }

    /**
     * Swaps the visual positions of two UI components.
     */
    public static final void swapPositions(UIComponentAPI comp1, UIComponentAPI comp2) {
        final PositionAPI pos1 = comp1.getPosition();
        final PositionAPI pos2 = comp2.getPosition();

        final float absX1 = pos1.getX();
        final float absY1 = pos1.getY();
        final float absX2 = pos2.getX();
        final float absY2 = pos2.getY();

        pos1.inBL(0f, 0f);
        pos2.inBL(0f, 0f);

        final float parentX1 = pos1.getX();
        final float parentY1 = pos1.getY();
        final float parentX2 = pos2.getX();
        final float parentY2 = pos2.getY();

        pos1.setXAlignOffset(absX2 - parentX1);
        pos1.setYAlignOffset(absY2 - parentY1);
        pos2.setXAlignOffset(absX1 - parentX2);
        pos2.setYAlignOffset(absY1 - parentY2);
    }

    /**
     * Positions the tooltip at a corner of the mouse.
     */
    public static final void mouseCornerPos(TooltipMakerAPI tooltip, int opad) {
        final int mouseSize = 40;
        final float correction = 8f;

        final PositionAPI pos = tooltip.getPosition();

        final float tooltipW = pos.getWidth();
        final float tooltipH = pos.getHeight();
        final float mouseX = settings.getMouseX();
        final float mouseY = settings.getMouseY();

        pos.inBL(0, 0);

        final float tooltipX = pos.getX();
        final float tooltipY = pos.getY();

        // Bottom-left of mouse
        float offsetX = (mouseX - tooltipX) + mouseSize / 2f;
        float offsetY = (mouseY - tooltipY) - tooltipH - mouseSize;

        // If right-side overflow
        if (tooltipX + offsetX + tooltipW > screenW - opad) {
            offsetX -= tooltipW + mouseSize - correction;
        }

        // If bottom overflow
        if (tooltipY + offsetY < opad) {
            offsetY += tooltipH + mouseSize + correction;
        }

        // If top overflow
        if (tooltipY + offsetY + tooltipH > screenH - opad) {
            offsetY = screenH - tooltipY - tooltipH - opad*2;
        }

        pos.setXAlignOffset(offsetX);
        pos.setYAlignOffset(offsetY);
    }

    public static final boolean containsMouse(final PositionAPI pos) {
        final float x = pos.getX();
        final float y = pos.getY();
        final float w = pos.getWidth();
        final float h = pos.getHeight();
        final float mx = Mouse.getX();
        final float my = Mouse.getY();

        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    public static final boolean isMouseDown() {
        return Mouse.isButtonDown(0) || Mouse.isButtonDown(1);
    }

    public static final boolean isCtrlDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
    }

    public static final boolean isShiftDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
    }

    public static final boolean isAltDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU);
    }
}