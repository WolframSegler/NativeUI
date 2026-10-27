package wfg.native_ui.internal.ui.dialog;

import static wfg.native_ui.util.UIConstants.*;
import static wfg.native_ui.util.Globals.settings;

import java.awt.Color;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.FaderUtil;

import wfg.native_ui.internal.ui.core.UIElement;
import wfg.native_ui.internal.util.BorderRenderer;
import wfg.native_ui.internal.util.NoiseRenderer;
import wfg.native_ui.internal.util.PanelFillRenderer;
import wfg.native_ui.ui.Attachments;

public class FoldingPanel extends UIElement {
    private static final SpriteAPI SCANLINE_11 = settings.getSprite("ui", "scanline11");
    private static final SpriteAPI NOISE = settings.getSprite("ui", "noise");

    public boolean renderBackground = true;
    public boolean transitionEnabled = true;
    public boolean isAlwaysScissor = false;
    public float borderThickness = 7f;
    public float noiseAlpha = 0.7f;
    public float borderAlpha = 1f;

    private float innerOffset = 0f;
    private UIComponentAPI currentPanel;
    private UIComponentAPI nextPanel;
    private BorderRenderer borderRenderer;
    private PanelFillRenderer backgroundLayer;
    private PanelFillRenderer foregroundLayer;
    private NoiseRenderer noiseRenderer;
    private final FaderUtil foldFader = new FaderUtil(1f, 0f);
    private int backgroundAlphaMin = 125;
    private int backgroundAlphaMax = 175;

    public FoldingPanel(UIPanelAPI parent, float width, float height, String borderPrefix,
        float borderThickness
    ) {
        super(width + (borderThickness + opad)*2, height + (borderThickness + opad)*2);
        attachTo(parent);

        this.borderThickness = borderThickness;
        innerOffset = borderThickness + opad;

        borderRenderer = new BorderRenderer(borderPrefix, false, width + innerOffset * 2f, height + innerOffset * 2f);
        initializeBackground();
    }

    public FoldingPanel(float width, float height, String borderPrefix, float borderThickness) {
        this(Attachments.getScreenPanel(), width, height, borderPrefix, borderThickness);
    }

    public FoldingPanel(float width, float height, String borderPrefix) {
        this(width, height, borderPrefix, 7);
    }

    public FoldingPanel(float width, float height) {
        this(width, height, UI_BORDER_1, 7);
    }

    public FoldingPanel() {
        this(100, 100, UI_BORDER_1, 7);
    }

    public void setBorder(String prefix) {
        borderRenderer = new BorderRenderer(prefix, false, getWidth(), getHeight());
    }

    public void setBackgroundAlpha(int min, int max) {
        backgroundAlphaMin = min;
        backgroundAlphaMax = max;
        initializeBackground();
    }

    private void initializeBackground() {
        final float width = getWidth() - borderThickness * 2f;
        final float height = getHeight() - borderThickness * 2f;

        backgroundLayer = new PanelFillRenderer(SCANLINE_11, width, height);
        backgroundLayer.setColors(
            new Color(0, 0, 0, 125), new Color(0, 0, 0, backgroundAlphaMin));
        backgroundLayer.setOverlayColors(
            new Color(0, 0, 0, 175), new Color(0, 0, 0, backgroundAlphaMax));

        backgroundLayer.useAdditiveBlend = false;

        foregroundLayer = new PanelFillRenderer(SCANLINE_11, width, height);
        foregroundLayer.useAdditiveBlend = true;
        foregroundLayer.setColors(new Color(10, 38, 44, 0), new Color(10, 38, 44, 0));
        noiseRenderer = new NoiseRenderer(NOISE, width, height);
        noiseRenderer.fadeOut(0.4f);
    }

    public void foldOut(float dur) {
        foldFader.setDurationIn(dur);
        foldFader.fadeIn();
    }

    public void forceFoldIn() {
        foldFader.forceOut();
    }

    public void forceFoldOut() {
        foldFader.forceIn();
    }

    public void foldIn(float dur) {
        foldFader.setDurationOut(dur);
        foldFader.fadeOut();
    }

    public void setNext(UIComponentAPI comp) {
        if (comp == null) return;
        noiseRenderer.fadeIn(0.2f, 0.2f);
        if (!transitionEnabled) noiseRenderer.fader.forceIn();

        nextPanel = comp;
        final float w = getContentWidth();
        final float h = getContentHeight();
        if (currentPanel != null) removePos(currentPanel);
        comp.getPosition().setSize(w, h);
        addPos(comp).inBL(innerOffset, innerOffset);
        if (!transitionEnabled) {
            currentPanel = nextPanel;
            nextPanel = null;
            noiseRenderer.fader.forceOut();
        }
    }

    @Override
    public PositionAPI setSize(float width, float height) {
        super.setSize(width, height);
        initializeBackground();

        if (currentPanel != null) {
            currentPanel.getPosition().setSize(
                getContentWidth(), getContentHeight()
            );
        }
        if (nextPanel != null) {
            nextPanel.getPosition().setSize(
                getContentWidth(), getContentHeight()
            );
        }
        return mPos;
    }

    public float getContentWidth() {
        return getWidth() - innerOffset * 2f;
    }

    public float getContentHeight() {
        return getHeight() - innerOffset * 2f;
    }

    public void flickerNoise(float inDuration, float outDuration) {
        noiseRenderer.fadeIn(inDuration, outDuration);
    }

    @Override
    public void advanceImpl(float delta) {
        if (nextPanel != null && noiseRenderer.isMaxBrightness()) {
            currentPanel = nextPanel;
            nextPanel = null;
            if (!transitionEnabled) {
                noiseRenderer.fader.forceOut();
            }
        }

        if (foldFader.getBrightness() != 0f || !foldFader.isIdle()) {
            backgroundLayer.advance(delta);
            foregroundLayer.advance(delta);
            noiseRenderer.advance(delta);
            foldFader.advance(delta);
            if (currentPanel != null) {
                currentPanel.advance(delta);
            }
        }
    }

    public FaderUtil getFoldFader() {
        return foldFader;
    }

    @Override
    public void processInputImpl(List<InputEventAPI> events) {
        if (currentPanel != null && foldFader.getBrightness() >= 0.75f && nextPanel == null) {
            currentPanel.processInput(events);
        }
    }

    @Override
    public void renderImpl(float alpha) {
        if (foldFader.getBrightness() == 0f && foldFader.isIdle()) return;

        final float brightness = foldFader.getBrightness() * alpha;
        final float heightScale = Math.min(1f, brightness / 0.75f);
        final float transitionAlpha = Math.max(0f, (brightness - 0.75f) / 0.25f);
        final float borderAlphaFactor = Math.min(1f, brightness / 0.25f);

        final float maxH = getHeight();
        final float w = getWidth();
        final float h = (maxH * heightScale);

        final float x = getX();
        final float y = (getY() + (maxH - h) / 2f);
        final float bx = x + borderThickness;
        final float by = y + borderThickness;

        final int scissorX = (int) ((x + innerOffset) * uiScale);
        final int scissorY = (int) ((y + innerOffset) * uiScale);
        final int scissorW = (int) ((w - innerOffset * 2f) * uiScale);
        final int scissorH = (int) ((h - innerOffset * 2f) * uiScale);

        if (renderBackground) {
            borderRenderer.setSize(w, h);
            borderRenderer.render(x, y, brightness * borderAlphaFactor * borderAlpha);
        }

        if (brightness < 1f || isAlwaysScissor) {
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
        }

        GL11.glScissor((int) x, (int) y, (int) w, (int) h);
        if (renderBackground) {
            backgroundLayer.renderVerticalGradient(bx, by,
                brightness * borderAlphaFactor
            );
        }

        GL11.glScissor(scissorX, scissorY, scissorW, scissorH);
        if (currentPanel != null) {
            final float mult = (transitionEnabled && renderBackground) ? 1f - noiseRenderer.getBrightness() : 1f;

            currentPanel.render(transitionAlpha * brightness * mult);
        }

        GL11.glScissor((int) x, (int) y, (int) w, (int) h);
        if (renderBackground) {
            foregroundLayer.renderVerticalGradient(bx, by,
                brightness * borderAlphaFactor
            );
            noiseRenderer.render(bx, by,
                brightness * noiseAlpha
            );
        }

        if (brightness < 1f || isAlwaysScissor) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
    }

    public UIComponentAPI getCurr() { return currentPanel; }
    public UIComponentAPI getNext() { return nextPanel; }
}