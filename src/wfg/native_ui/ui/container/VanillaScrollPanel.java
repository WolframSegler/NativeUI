package wfg.native_ui.ui.container;

import static wfg.native_ui.util.Globals.settings;
import static wfg.native_ui.util.UIConstants.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.fs.graphics.util.Fader;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.ui.O0Oo;

import rolflectionlib.util.RolfLectionUtil;
import wfg.native_ui.ui.panel.CustomPanel;
import wfg.native_ui.ui.visual.GridRenderer;
import wfg.native_ui.util.Arithmetic;
import wfg.native_ui.util.NativeUiUtils;
import wfg.native_ui.util.RenderUtils;
import wfg.native_ui.util.NativeUiUtils.Rect;

/** Not complete yet!!! */
public class VanillaScrollPanel extends CustomPanel {
    private static final Color TRANSPARENT_BLACK = new Color(0, 0, 0, 0);
    private static final float BAR_PAD = 6f;

    public boolean doNotRenderShadow = false;
    public boolean isSimpleShadows = false;
    public boolean isLeftScrollbar = false;
    public boolean showScrollbars = true;
    public boolean useMouseWheel = true;
    public boolean roundOffset = false;

    public float shadowHeight = 50f;
    public float visibilityPad = 150f;

    public Color scrollWidgetColor = new Color(170, 222, 255);
    public Color scrollRailColor = new Color(31, 94, 112, 175);

    private float offsetX = 0f;
    private float offsetY = 0f;
    private float verticalScrollState = 0f;
    
    private FaderUtil topFader = new FaderUtil(0f, 0.1f, 0.1f);
    private FaderUtil bottomFader = new FaderUtil(0f, 0.1f, 0.1f);

    private final ContentContainer contentContainer = new ContentContainer();
    private UIPanelAPI overlayContainer;
    private GridRenderer gridRenderer = null;

    // UNKNOWNS
    private float someFloat1 = 0f;
    private float someFloat2 = 0f;
    private InputEventAPI someEvent1 = null;
    private boolean someBool1 = false;
    private Fader someFader1 = new Fader(0f, 0.25f, 0.25f);
    private SomeEnum1 someData1 = SomeEnum1.RIGHT_MOUSE;
    private boolean someBool3 = false;
    private boolean someBool2 = false;
    private int someInt1 = 0;
    private int someInt2 = 0;
    private FaderUtil someFader2 = new FaderUtil(0f, 0.1f, 0.1f);
    private FaderUtil someFader3 = new FaderUtil(0f, 0.1f, 0.1f);
    private FaderUtil someFader4 = new FaderUtil(0f, 0.1f, 0.1f);
    private FaderUtil someFader5 = new FaderUtil(0f, 0.1f, 0.1f);
    
    public VanillaScrollPanel(UIPanelAPI parent, int width, int height) {
        super(parent, width, height);

        super.add(contentContainer).inTL(0f, 0f);
        overlayContainer = new OverlayPanel().getPanel();
        super.add(overlayContainer);
    }

    public void addToOverlay(UIComponentAPI comp) {
        overlayContainer.addComponent(comp);
    }

    public void removeFromOverlay(UIComponentAPI comp) {
        overlayContainer.removeComponent(comp);
    }

    public ContentContainer getContentContainer() {
        return contentContainer;
    }

    @Override
    public PositionAPI add(UIComponentAPI comp) {
        if (comp == contentContainer) {
            PositionAPI Position2 = super.add(comp);
            return Position2;
        }
        return contentContainer.add(comp);
    }

    @Override
    public void remove(UIComponentAPI comp) {
        super.remove(comp);
        contentContainer.remove(comp);
    }

    public void setContentSize(float w, float h) {
        contentContainer.setSize((int) w, (int) h);
    }

    // TODO fix later
    public void processScrollInputOnly(List<InputEventAPI> events, boolean var2) {
        final float cw = contentContainer.getPos().getWidth();
        final float ch = contentContainer.getPos().getHeight();
        final float pw = pos.getWidth();
        final float ph = pos.getHeight();

        for (InputEventAPI event : events) {
            if (event.isConsumed()) continue;
            
            if (event.isMouseEvent() && pos.containsEvent(event)) someFader1.fadeIn();

            if (useMouseWheel && event.isMouseScrollEvent() && pos.containsEvent(event)) {
                if (ph >= ch && offsetY == 0f) continue;

                float var23 = -100f;
                float var24 = ph * 0.33f;
                if (Math.abs(var23) > var24) {
                    var23 = -var24;
                }

                if (event.isShiftDown()) {
                    var23 *= 5f;
                }

                offsetY += var23 * Math.signum(event.getEventValue());
                if (offsetY < 0f) offsetY = 0f;

                if (ph > ch) {
                    offsetY = 0f;
                } else if (offsetY + ph > ch) {
                    offsetY = ch - ph;
                }

                contentContainer.setOffset(offsetX, offsetY);
                RolfLectionUtil.invokeMethodDirectly(posSetWithSortRecursiveMethod, pos, false);
                posRecompute();
                RolfLectionUtil.invokeMethodDirectly(posSetWithSortRecursiveMethod, pos, true);
                event.consume();
                continue;
            
            }
            if (var2 || O0Oo.\u00d300000() == null || O0Oo.\u00d300000() == this) continue;
            
            boolean mouseDown = someData1 == SomeEnum1.LEFT_MOUSE && event.isLMBDownEvent() || someData1 == SomeEnum1.RIGHT_MOUSE && event.isRMBDownEvent();
            boolean mouseUp = someData1 == SomeEnum1.LEFT_MOUSE && event.isLMBUpEvent() || someData1 == SomeEnum1.RIGHT_MOUSE && event.isRMBUpEvent();
            boolean var11 = event.isLMBDownEvent();
            boolean var12 = event.isLMBUpEvent();
            float var13 = pw * pw / cw;
            float var14 = ph * ph / ch;
            final Rect var15 = getVerticalThumbHitRect();
            boolean var16 = false;
            if (var15 != null && var15.containsEvent(event)) {
                someFader2.fadeIn();
                var16 = true;
            } else {
                someFader2.fadeOut();
            }

            final Rect eff = getVerticalTrackHitRect();
            if (eff != null && eff.containsEvent(event)) {
                someFader3.fadeIn();
                var16 = true;
            } else {
                someFader3.fadeOut();
            }

            boolean someLocalBool = false;

            if (var11 && !someBool1 && !someBool3 && !someBool2 && eff != null && eff.containsEvent(event) && (var15 == null || !var15.containsEvent(event))) {
                final float ngp = (Mouse.getY()/uiScale - pos.getY()) / ph;
                offsetY = Math.max(0f, (1f - ((ngp * ph + var14 / 2f) / ph)) * ch);

                if (ph > ch) {
                    offsetY = 0f;
                } else if (offsetY + ph > ch) {
                    offsetY = ch - ph;
                }

                contentContainer.forceOffset(offsetX, offsetY);
                RolfLectionUtil.invokeMethodDirectly(posSetWithSortRecursiveMethod, pos, false);
                posRecompute();
                RolfLectionUtil.invokeMethodDirectly(posSetWithSortRecursiveMethod, pos, true);
                someLocalBool = true;
            }

            if (var15 != null) {
                if (!var11 || !var15.containsEvent(event) && !someLocalBool) {
                    if (var12 && someBool3) {
                        someBool3 = false;
                        // O0Oo.void(this);
                        consumeAllKeyboardEvents(events);
                        event.consume();
                        break;
                    }

                    if (event.isMouseMoveEvent() && someBool3) {
                        offsetY -= (float)((int)(event.getDY() * (ch / ph)));
                        if (offsetY < 0f) {
                        offsetY = 0f;
                        }

                        if (ph > ch) {
                        offsetY = 0f;
                        } else if (offsetY + ph > ch) {
                        offsetY = ch - ph;
                        }

                        contentContainer.forceOffset(offsetX, offsetY);
                        RolfLectionUtil.invokeMethodDirectly(posSetWithSortRecursiveMethod, pos, false);
                        posRecompute();
                        RolfLectionUtil.invokeMethodDirectly(posSetWithSortRecursiveMethod, pos, true);
                        event.consume();
                    }
                } else {
                    event.consume();
                    someBool3 = true;
                    // O0Oo.Õ00000(this);
                    consumeAllKeyboardEvents(events);
                }
            }

            if (!event.isConsumed()) {
                if (event.isMouseMoveEvent() && var16) {
                    event.consume();
                } else {
                    final Rect hitRect = getHorizontalThumbHitRect();
                    if (hitRect != null && hitRect.containsEvent(event)) {
                        someFader4.fadeIn();
                    } else {
                        someFader4.fadeOut();
                    }

                    final Rect uzn = getHorizontalTrackHitRect();
                    if (uzn != null && uzn.containsEvent(event)) {
                        someFader5.fadeIn();
                    } else {
                        someFader5.fadeOut();
                    }

                    someLocalBool = false;
                    if (var11 && !someBool1 && !someBool3 && !someBool2 && uzn != null && uzn.containsEvent(event) && (hitRect == null || !hitRect.containsEvent(event))) {
                        final float mfc = (Mouse.getX()/uiScale - pos.getX()) / pw;
                        offsetX = ((mfc * pw - var13 / 2f) / pw) * cw;
                        offsetX *= -1f;
                        if (offsetX > 0f) {
                        offsetX = 0f;
                        }

                        if (pw > cw) {
                        offsetX = 0f;
                        } else if (pw - offsetX > cw) {
                        offsetX = pw - cw;
                        }

                        contentContainer.forceOffset(offsetX, offsetY);
                        RolfLectionUtil.invokeMethodDirectly(posSetWithSortRecursiveMethod, pos, false);
                        posRecompute();
                        RolfLectionUtil.invokeMethodDirectly(posSetWithSortRecursiveMethod, pos, true);
                        someLocalBool = true;
                    }

                    if (hitRect != null) {
                        if (!var11 || !hitRect.containsEvent(event) && !someLocalBool) {
                        if (var12 && someBool2) {
                            someBool2 = false;
                            // O0Oo.void(this);
                            consumeAllKeyboardEvents(events);
                            event.consume();
                            break;
                        }

                        if (event.isMouseMoveEvent() && someBool2) {
                            offsetX -= (float)((int)((float)event.getDX() * (cw / pw)));
                            if (offsetX > 0f) {
                                offsetX = 0f;
                            }

                            if (pw > cw) {
                                offsetX = 0f;
                            } else if (pw - offsetX > cw) {
                                offsetX = pw - cw;
                            }

                            contentContainer.forceOffset(offsetX, offsetY);
                            RolfLectionUtil.invokeMethodDirectly(posSetWithSortRecursiveMethod, pos, false);
                            posRecompute();
                            RolfLectionUtil.invokeMethodDirectly(posSetWithSortRecursiveMethod, pos, true);
                            event.consume();
                        }
                        } else {
                        event.consume();
                        someBool2 = true;
                        // O0Oo.Õ00000(this);
                        consumeAllKeyboardEvents(events);
                        }
                    }

                    if (!event.isConsumed()) {
                        if (var14 >= ph && var13 >= pw) {
                        mouseDown = false;
                        }

                        if (mouseDown && pos.containsEvent(event) && O0Oo.Ó00000() == null) {
                        // someEvent1 = event.clone();
                        event.consume();
                        someBool1 = true;
                        someFloat1 = contentContainer.getXOffset();
                        someFloat2 = contentContainer.getYOffset();
                        someInt2 = 0;
                        someInt1 = 0;
                        // O0Oo.Õ00000(this);
                        consumeAllKeyboardEvents(events);
                        } else {
                        if (mouseUp && someBool1) {
                            int var26 = (int)Math.abs(someFloat1 - offsetX);
                            int var27 = (int)Math.abs(someFloat2 - offsetY);
                            if (var26 > someInt2) {
                                someInt2 = var26;
                            }

                            if (var27 > someInt1) {
                                someInt1 = var27;
                            }

                            if (someInt2 + someInt1 > 1) {
                                event.consume();
                            } else {
                                events.add(0, someEvent1);
                            }

                            someEvent1 = null;
                            someBool1 = false;
                            // O0Oo.void(this);
                            consumeAllKeyboardEvents(events);
                            break;
                        }

                        if (event.isMouseMoveEvent() && someBool1) {
                            offsetX += (float)event.getDX();
                            offsetY += (float)event.getDY();
                            if (offsetX > 0f) {
                                offsetX = 0f;
                            }

                            if (offsetY < 0f) {
                                offsetY = 0f;
                            }

                            if (pw > cw) {
                                offsetX = 0f;
                            } else if (pw - offsetX > cw) {
                                offsetX = pw - cw;
                            }

                            if (ph > ch) {
                                offsetY = 0f;
                            } else if (offsetY + ph > ch) {
                                offsetY = ch - ph;
                            }

                            int var28 = (int)Math.abs(someFloat1 - offsetX);
                            int var22 = (int)Math.abs(someFloat2 - offsetY);
                            if (var28 > someInt2) {
                                someInt2 = var28;
                            }

                            if (var22 > someInt1) {
                                someInt1 = var22;
                            }

                            contentContainer.forceOffset(offsetX, offsetY);
                            RolfLectionUtil.invokeMethodDirectly(posSetWithSortRecursiveMethod, pos, false);
                            posRecompute();
                            RolfLectionUtil.invokeMethodDirectly(posSetWithSortRecursiveMethod, pos, true);
                            event.consume();
                        }
                        }
                    }
                }
            }
        }
    }

    public void setXOffset(float offset) {
        offsetX = offset;
        contentContainer.forceOffset(offset, offsetY);
    }

    public void setYOffset(float offset) {
        offsetY = offset;
        contentContainer.forceOffset(offsetX, offset);
    }

    // TODO with the new update make this override the base processInput method
    @Override
    public void processInput(List<InputEventAPI> events) {
        someFader3.fadeOut();
        someFader2.fadeOut();
        processScrollInputOnly(events, false);
        if (!someBool1) {
            final List<InputEventAPI> remainingEvents = new ArrayList<>();
            for (InputEventAPI event : events) {
                if (event.isConsumed() || event.isMouseEvent() && !event.isMouseMoveEvent() && !pos.containsEvent(event)) continue;
                remainingEvents.add(event);
            }
            super.processInput(remainingEvents);
        }
    }

    public FaderUtil getTopFader() {
        return topFader;
    }

    public FaderUtil getBottomFader() {
        return bottomFader;
    }

    @Override
    public void advance(float delta) {
        super.advance(delta);
        someFader1.advance(delta);
        if (!someBool2 && !someBool3 && !someBool1) {
            someFader1.fadeOut();
        }

        final float yOff = contentContainer.getYOffset();
        final float topShadow = Math.min(yOff, shadowHeight);
        if (topShadow > 0f) {
            topFader.fadeIn();
        } else {
            topFader.fadeOut();
        }

        final float bottomShadow = Math.min(shadowHeight,
            contentContainer.getPos().getHeight() - pos.getHeight() - yOff
        );
        if (bottomShadow > 0f) {
            bottomFader.fadeIn();
        } else {
            bottomFader.fadeOut();
        }

        topFader.advance(delta);
        bottomFader.advance(delta);
        someFader2.advance(delta);
        someFader3.advance(delta);
        someFader4.advance(delta);
        someFader5.advance(delta);
    }

    public void scrollToBottom() {
        final float outerH = pos.getHeight();
        final float contentH = contentContainer.getPos().getHeight();
        offsetX = contentContainer.getXOffset();
        offsetY = contentH - outerH;
        if (outerH > contentH) offsetY = 0f;

        contentContainer.setOffset(offsetX, offsetY);
    }

    public void scrollToY(float y) {
        final float outerH = pos.getHeight();
        final float contentH = contentContainer.getPos().getHeight();
        offsetX = contentContainer.getXOffset();
        offsetY = y;
        if (outerH > contentH) {
            offsetY = 0f;
        } else if (offsetY + outerH > contentH) {
            offsetY = contentH - outerH;
        } else if (offsetY < 0f) {
            offsetY = 0f;
        }
        contentContainer.setOffset(offsetX, offsetY);
    }

    public void setShowGrid(float cellWidth, float cellHeight, boolean showGrid) {
        gridRenderer = !showGrid ? null : new GridRenderer(cellWidth, cellHeight);
    }

    public void updateGrid(float f2, float f3) {
        gridRenderer.setRows(f2);
        gridRenderer.setCols(f3);
    }

    // TODO with the new update make this override the base render method
    @Override
    public void render(float alpha) {
        GL11.glScissor((int)pos.getX(), (int)pos.getY(), (int)pos.getWidth(), (int)pos.getHeight());
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        if (gridRenderer != null) {
            final PositionAPI contPos = contentContainer.getPos();
            gridRenderer.render(contPos.getX(), contPos.getY() - 1f, contPos.getWidth(), contPos.getHeight(), alpha * 0.5f);
        }

        super.render(alpha);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private final void renderEdgeFadeShadows(float alpha) {
        if (alpha <= 0f || doNotRenderShadow) return;

        final float px = pos.getX();
        final float py = pos.getY();
        final float pw = pos.getWidth();
        final float ph = pos.getHeight();

        final float bottomShadowH = Math.min(shadowHeight, contentContainer.getPos().getHeight() - (ph + contentContainer.getYOffset()));
        final float topShadowH = Math.min(shadowHeight, contentContainer.getYOffset());

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);

        topFader.forceIn();
        bottomFader.forceIn();

        if (isSimpleShadows) {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            if (topShadowH > 0f) {
                RenderUtils.drawGradientQuad(px, py + ph - topShadowH, pw, topShadowH, TRANSPARENT_BLACK, Color.BLACK, Color.BLACK, TRANSPARENT_BLACK,
                    alpha * topFader.getBrightness());
            }

            if (bottomShadowH > 0f) {
                RenderUtils.drawGradientQuad(px, py, pw, bottomShadowH, Color.BLACK, TRANSPARENT_BLACK, TRANSPARENT_BLACK, Color.BLACK,
                    alpha * bottomFader.getBrightness());
            }
            return;
        }

        GL11.glBlendFunc(GL11.GL_ZERO, GL11.GL_SRC_ALPHA);
        GL11.glColorMask(false, false, false, true);

        if (topShadowH > 0f) {
            RenderUtils.drawGradientQuad(px, py + ph - topShadowH, pw, topShadowH, TRANSPARENT_BLACK, Color.BLACK, Color.BLACK, TRANSPARENT_BLACK,
                alpha * topFader.getBrightness());
        }
        if (bottomShadowH > 0f) {
            RenderUtils.drawGradientQuad(px, py, pw, bottomShadowH, Color.BLACK, TRANSPARENT_BLACK, TRANSPARENT_BLACK, Color.BLACK,
                alpha * bottomFader.getBrightness());
        }

        GL11.glBlendFunc(GL11.GL_DST_ALPHA, GL11.GL_ONE_MINUS_DST_ALPHA);
        GL11.glColorMask(true, true, true, false);

        if (topShadowH > 0f) {
            RenderUtils.drawGradientQuad(px, py + ph - topShadowH, pw, topShadowH, TRANSPARENT_BLACK, Color.BLACK, Color.BLACK, TRANSPARENT_BLACK,
                alpha * topFader.getBrightness());
        }
        if (bottomShadowH > 0f) {
            RenderUtils.drawGradientQuad(px, py, pw, bottomShadowH, Color.BLACK, TRANSPARENT_BLACK, TRANSPARENT_BLACK, Color.BLACK,
                alpha * bottomFader.getBrightness());
        }

        GL11.glColorMask(true, true, true, true);
    }

    private void renderScrollbar(float alpha) {
        if (alpha <= 0f) return;

        final float px = pos.getX();
        final float py = pos.getY();
        final float pw = pos.getWidth();
        final float ph = pos.getHeight();
        final float cw = contentContainer.getPos().getWidth();
        final float ch = contentContainer.getPos().getHeight();
        
        final Color barColor = NativeUiUtils.setAlpha(scrollWidgetColor, 0.66f);
        final float thumbH = ch <= 0f ? ph : ph * ph / ch;

        if (thumbH < ph) {
            final float thumbX = isLeftScrollbar ? getVerticalThumbHitRect().x : px + pw - pad;
            final float thumbY = py + ph - (contentContainer.getYOffset() / ch) * ph - thumbH;

            // Rail (track)
            RenderUtils.drawQuad(thumbX + 1f, py + 1f, pad - 2f, ph,
                scrollRailColor != null ? scrollRailColor : barColor,
                alpha * (scrollRailColor != null ? 1f : 0.5f), false);

            // Thumb
            RenderUtils.drawQuad(thumbX, thumbY, pad, thumbH, barColor, alpha, false);

            // Thumb glow
            final float glowAlpha = someBool3 ? 1f : someFader2.getBrightness();
            if (glowAlpha > 0f) {
                final float glowScale = (float) Math.sqrt(glowAlpha);
                final float glowWidth = BAR_PAD * glowScale * 2f + pad;
                final float glowLeft = thumbX - BAR_PAD * glowScale;
                final float bevelRadius = Math.min(thumbH * 0.4f, 5f * glowScale);

                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                RenderUtils.setGlColor(barColor, alpha * glowAlpha * 0.75f);
                GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
                GL11.glVertex2f(glowLeft + glowWidth - bevelRadius, thumbY);
                GL11.glVertex2f(glowLeft + bevelRadius, thumbY);
                GL11.glVertex2f(glowLeft + glowWidth, thumbY + bevelRadius - 1f);
                GL11.glVertex2f(glowLeft, thumbY + bevelRadius - 1f);
                GL11.glVertex2f(glowLeft + glowWidth, thumbY + thumbH - bevelRadius);
                GL11.glVertex2f(glowLeft, thumbY + thumbH - bevelRadius);
                GL11.glVertex2f(glowLeft + glowWidth - bevelRadius, thumbY + thumbH);
                GL11.glVertex2f(glowLeft + bevelRadius, thumbY + thumbH);
                GL11.glEnd();
            }

            // Hover preview thumb (vertical)
            final float hoverAlpha = someBool3 ? 1f : someFader3.getBrightness();
            if (hoverAlpha > 0f && !someBool2 && !someBool3 && !someBool1) {
                float hoverThumbY = py + ((Mouse.getY()/uiScale - py) / ph) * ph - thumbH / 2f;
                if (hoverThumbY < py) {
                    hoverThumbY = py;
                } else if (hoverThumbY + thumbH > py + ph) {
                    hoverThumbY = py + ph - thumbH;
                }

                RenderUtils.drawQuad(thumbX, hoverThumbY, pad, thumbH, barColor, alpha * hoverAlpha * 0.75f, false);

                final float hoverGlowScale = (float) Math.sqrt(hoverAlpha);
                final float hoverGlowWidth = BAR_PAD * hoverGlowScale * 2f + pad;
                final float hoverGlowLeft = thumbX - BAR_PAD * hoverGlowScale;
                final float hoverBevelRadius = Math.min(thumbH * 0.4f, 5f * hoverGlowScale);

                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                RenderUtils.setGlColor(barColor, alpha * hoverAlpha * 0.5f);
                GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
                GL11.glVertex2f(hoverGlowLeft + hoverGlowWidth - hoverBevelRadius, hoverThumbY);
                GL11.glVertex2f(hoverGlowLeft + hoverBevelRadius, hoverThumbY);
                GL11.glVertex2f(hoverGlowLeft + hoverGlowWidth, hoverThumbY + hoverBevelRadius - 1f);
                GL11.glVertex2f(hoverGlowLeft, hoverThumbY + hoverBevelRadius - 1f);
                GL11.glVertex2f(hoverGlowLeft + hoverGlowWidth, hoverThumbY + thumbH - hoverBevelRadius);
                GL11.glVertex2f(hoverGlowLeft, hoverThumbY + thumbH - hoverBevelRadius);
                GL11.glVertex2f(hoverGlowLeft + hoverGlowWidth - hoverBevelRadius, hoverThumbY + thumbH);
                GL11.glVertex2f(hoverGlowLeft + hoverBevelRadius, hoverThumbY + thumbH);
                GL11.glEnd();
            }
        }

        // Horizontal scrollbar
        final float hThumbWidth = pw * pw / cw;
        if (hThumbWidth < pw) {
            final float hThumbX = px - (contentContainer.getXOffset() * pw / cw);

            // Horizontal rail
            RenderUtils.drawQuad(px, py + 1f, pw - 2f, pad - 2f,
                scrollRailColor != null ? scrollRailColor : barColor,
                alpha * (scrollRailColor != null ? 1f : 0.5f), false);

            // Horizontal thumb
            RenderUtils.drawQuad(hThumbX, py, hThumbWidth, pad, barColor, alpha, false);

            // Horizontal thumb glow
            final float hGlowAlpha = someBool2 ? 1f : someFader4.getBrightness();
            if (hGlowAlpha > 0f) {
                RenderUtils.drawQuad(hThumbX, py, hThumbWidth, pad, barColor, alpha * hGlowAlpha, true);
            }

            // Horizontal hover preview thumb
            final float hHoverAlpha = someBool2 ? 1f : someFader5.getBrightness();
            if (hHoverAlpha > 0f && !someBool2 && !someBool3 && !someBool1) {
                final float hoverThumbX = px + ((Mouse.getX()/uiScale - px) / pw) * pw - hThumbWidth / 2f;
                RenderUtils.drawQuad(hoverThumbX, py, hThumbWidth, pad, barColor, alpha * hHoverAlpha * 0.75f, false);
            }
        }
    }

    private final Rect getVerticalThumbHitRect() {
        final float ch = contentContainer.getPos().getHeight();
        final float ph = pos.getHeight();

        final float barThickness = 4f;
        final float thumbH = ph * ph / ch;

        if (thumbH < ph) {
            final float rectX = isLeftScrollbar ? 5f : pos.getWidth() - barThickness;
            final float rectY = ph - (contentContainer.getYOffset() / ch) * ph - thumbH;

            return new Rect(pos.getX() + rectX - BAR_PAD, pos.getY() + rectY, barThickness + BAR_PAD * 2f, thumbH);
        }
        return null;
    }

    private final Rect getVerticalTrackHitRect() {
        final float ch = contentContainer.getPos().getHeight();
        final float ph = pos.getHeight();

        final float barThickness = 4f;
        final float thumbH = ph * ph / ch;

        if (thumbH < ph) {
            final float rectX = isLeftScrollbar ? 5f : pos.getWidth() - barThickness;

            return new Rect(pos.getX() + rectX - BAR_PAD, pos.getY(), barThickness + BAR_PAD * 2f, ph);
        }
        return null;
    }

    private final Rect getHorizontalThumbHitRect() {
        final float panelW = pos.getWidth();
        final float contentW = contentContainer.getPos().getWidth();

        final float barThickness = 4f;
        final float thumbW = panelW * panelW / contentW;

        if (thumbW < panelW) {
            final float localThumbX = -contentContainer.getXOffset() * panelW / contentW;
            return new Rect(pos.getX() + localThumbX, pos.getY(), thumbW, barThickness);
        }
        return null;
    }

    private final Rect getHorizontalTrackHitRect() {
        final float pw = pos.getWidth();
        final float cw = contentContainer.getPos().getWidth();
        final float barThickness = 4f;

        final float thumbW = pw * pw / cw;
        if (thumbW < pw) {
            return new Rect(pos.getX(), pos.getY(), pw, barThickness);
        }
        return null;
    }

    public void resetOffset() {
        offsetY = 0f;
        offsetX = 0f;
        contentContainer.forceOffset(offsetX, offsetY);
        posRecompute();
    }

    public void setOffset(float f2, float f3) {
        offsetX = f2;
        offsetY = f3;
        contentContainer.forceOffset(offsetX, offsetY);
        posRecompute();
    }

    public void clampOffset() {
        final PositionAPI contPos = contentContainer.getPos();
        final float pw = pos.getWidth();
        final float ph = pos.getHeight();
        
        if (pw * pw / contPos.getWidth() >= pos.getWidth()) {
            offsetX = 0f;
        }
        if (ph * ph / contPos.getHeight() >= pos.getHeight()) {
            offsetY = 0f;
        }
        setOffset(offsetX, offsetY);
    }

    public float getXOffset() {
        return offsetX;
    }

    public float getYOffset() {
        return offsetY;
    }

    public boolean isScrolling() {
        return contentContainer.isScrolling();
    }

    public GridRenderer getGrid() {
        return gridRenderer;
    }

    public final void ensureVisible(UIComponentAPI comp) {
        final PositionAPI compPos = comp.getPosition();
        final float pad = Math.min(visibilityPad, (pos.getHeight() - compPos.getHeight() - 20f) / 2f);
        final float pTop = pos.getY() + pos.getHeight();
        final float pBot = pos.getY();
        final float cTop = compPos.getY() + compPos.getHeight() + pad;
        final float cBot = compPos.getY() - pad;

        boolean need = true;
        final float newY;
        if (cBot > pTop) {
            newY = contentContainer.getYOffset() - (cTop - pTop);
        } else if (cTop < pBot) {
            newY = contentContainer.getYOffset() + (pBot - cBot);
        } else if (cTop > pTop) {
            newY = contentContainer.getYOffset() - (cTop - pTop);
        } else if (cBot < pBot) {
            newY = contentContainer.getYOffset() + (pBot - cBot);
        } else {
            newY = 0f;
            need = false;
        }
        if (need) scrollToY(newY);
    }

    public final void saveVerticalScrollState() {
        verticalScrollState = getYOffset();
    }

    public final void restoreVerticalScrollState() {
        final float diff = contentContainer.getPos().getHeight() - pos.getHeight();
        if (diff > 0) {
            setOffset(0f, Arithmetic.clamp(verticalScrollState, 0f, diff));
        }
    }

    public class ContentContainer extends CustomPanel {

        private PositionAPI visibleArea = null;
        private float offsetX = 0f;
        private float offsetY = 0f;

        public ContentContainer() {
            super(null, 0, 0);
        }

        @Override
        public void render(float alpha) {
            if (alpha <= 0f) return;

            if (showScrollbars && !doNotRenderShadow && !isSimpleShadows) {
                GL11.glColorMask(false, false, false, true);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ZERO);
                RenderUtils.quadNoBlend(pos.getX(), pos.getY(), pos.getWidth(), pos.getHeight(), Color.BLACK, 0f);
                GL11.glColorMask(true, true, true, true);
            }

            for (UIComponentAPI child : getChildrenCopy()) {
                if (NativeUiUtils.intersects(child.getPosition(), VanillaScrollPanel.this.getPos())
                    && (visibleArea == null || NativeUiUtils.intersects(child.getPosition(), visibleArea))
                ) {
                    child.render(alpha);
                    
                } else if (child instanceof RenderNotifier notifiable) {
                    notifiable.notifyDidNotRender();
                }
            }
        }

        public PositionAPI getVisibleArea() {
            return visibleArea;
        }

        public void setVisibleArea(PositionAPI area) {
            visibleArea = area;
        }

        // TODO replace with the UIComponentAPI processInput version so that the children don't get the events.
        @Override
        public void processInput(List<InputEventAPI> events) {
            if (eventSetXMethod == null || eventSetYMethod == null) {
                final Class<?> eventClazz = events.get(0).getClass();
                eventSetXMethod = RolfLectionUtil.getMethod("setX", eventClazz, 1);
                eventSetYMethod = RolfLectionUtil.getMethod("setY", eventClazz, 1);
            }
            
            for (UIComponentAPI child : getChildrenCopy()) {
                if (!NativeUiUtils.intersects(child.getPosition(), VanillaScrollPanel.this.getPos())) continue;

                int mouseX = -1;
                int mouseY = -1;
                for (InputEventAPI event : events) {
                    if (event.isConsumed() || !event.isMouseEvent() || VanillaScrollPanel.this.getPos().containsEvent(event)) continue;

                    if (mouseX < 0) mouseX = event.getX();
                    if (mouseY < 0) mouseY = event.getY();

                    RolfLectionUtil.invokeMethodDirectly(eventSetXMethod, event, Integer.MAX_VALUE);
                    RolfLectionUtil.invokeMethodDirectly(eventSetYMethod, event, Integer.MAX_VALUE);
                }

                child.processInput(events);

                for (InputEventAPI event : events) {
                    if (event.isConsumed() || !event.isMouseEvent() || VanillaScrollPanel.this.getPos().containsEvent(event) || !event.isMouseEvent()) continue;

                    RolfLectionUtil.invokeMethodDirectly(eventSetXMethod, event, mouseX);
                    RolfLectionUtil.invokeMethodDirectly(eventSetYMethod, event, mouseY);
                }
            }
        }

        public void setOffset(float x, float y) {
            offsetX = x;
            offsetY = y;
        }

        @Override
        public void advance(float delta) {
            super.advance(delta);

            float currentXOffset = getXOffset();
            float currentYOffset = getYOffset();

            final float dx = offsetX - currentXOffset;
            final float dy = offsetY - currentYOffset;

            boolean offsetChanged = false;
            if (dx != 0f) {
                float stepX = Math.signum(dx) * delta * (Math.abs(dx * 3f) + 200f);
                if (Math.abs(stepX) > Math.abs(dx)) stepX = dx;

                currentXOffset += stepX;
                offsetChanged = true;
            }

            if (dy != 0f) {
                float stepY = Math.signum(dy) * delta * (Math.abs(dy * 3f) + 200f);
                if (Math.abs(stepY) > Math.abs(dy)) stepY = dy;

                currentYOffset += stepY;
                offsetChanged = true;
            }

            if (offsetChanged) {
                updateOffset(currentXOffset, currentYOffset);
            }
        }

        public boolean isScrolling() {
            return offsetX - getXOffset() != 0f || offsetY - getYOffset() != 0f;
        }

        public void updateOffset(float x, float y) {
            RolfLectionUtil.invokeMethodDirectly(posSetWithSortRecursiveMethod, pos, false);

            RolfLectionUtil.invokeMethodDirectly(posSetOffset, pos,
                (roundOffset ? (int) x : x),
                (roundOffset ? (int) y : y)
            );

            RolfLectionUtil.invokeMethodDirectly(posSetWithSortRecursiveMethod, pos, true);
        }

        public void forceOffset(float x, float y) {
            updateOffset(x, y);
            setOffset(x, y);
        }

        public float getXOffset() {
            return (float) RolfLectionUtil.invokeMethodDirectly(posGetOffsetX, pos);
        }

        public float getYOffset() {
            return (float) RolfLectionUtil.invokeMethodDirectly(posGetOffsetY, pos);
        }
    }

    public class OverlayPanel extends CustomPanel {
        public OverlayPanel() {
            super(null, 0, 0);
        }

        @Override
        public void render(float alpha) {
            super.render(alpha);

            if (VanillaScrollPanel.this.showScrollbars) {
                VanillaScrollPanel.this.renderEdgeFadeShadows(alpha);

                GL11.glDisable(GL11.GL_SCISSOR_TEST);
                VanillaScrollPanel.this.renderScrollbar(alpha);
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
            }
        }
    }

    public static enum SomeEnum1 {
        LEFT_MOUSE,
        RIGHT_MOUSE
    }

    private static final void consumeAllKeyboardEvents(List<InputEventAPI> events) {
        for (InputEventAPI event : events) {
            if (event.isConsumed() || event.isMouseEvent() || !event.isKeyboardEvent()) {
                continue;
            }
            event.consume();
        }
    }

    public static interface RenderNotifier {
        public void notifyDidNotRender();
    }

    private static Object eventSetXMethod;
    private static Object eventSetYMethod;
    private static final Object posSetWithSortRecursiveMethod = RolfLectionUtil.getMethod(
        "setWithSortRecurvsive", settings.createCustom(0f, 0f, null).getPosition().getClass(), 1
    );
    private static final Object posSetOffset = RolfLectionUtil.getMethod(
        "setOffset", settings.createCustom(0f, 0f, null).getPosition().getClass(), 2
    );
    private static final Object posGetOffsetX = RolfLectionUtil.getMethod(
        "getXOffset", settings.createCustom(0f, 0f, null).getPosition().getClass()
    );
    private static final Object posGetOffsetY = RolfLectionUtil.getMethod(
        "getYOffset", settings.createCustom(0f, 0f, null).getPosition().getClass()
    );
}