package wfg.wrap_ui.internal.ui.dialogs;

import static wfg.wrap_ui.util.UIConstants.*;

import java.awt.Color;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.FaderUtil;

import wfg.wrap_ui.internal.util.BorderRenderer;
import wfg.wrap_ui.internal.util.NoiseRenderer;
import wfg.wrap_ui.internal.util.PanelFillRenderer;
import wfg.wrap_ui.ui.Attachments;
import wfg.wrap_ui.ui.panels.CustomPanel;

public class FoldingPanel extends CustomPanel<FoldingPanel> {
    public boolean renderBackground = true;
    public boolean transitionEnabled = true;
    public boolean isAlwaysScissor = false;
    public float borderThickness = 7f;
    public float noiseAlpha = 0.7f;

    private UIComponentAPI currentPanel;
    private UIComponentAPI nextPanel;
    private BorderRenderer borderRenderer;
    private PanelFillRenderer backgroundLayer;
    private PanelFillRenderer foregroundLayer;
    private NoiseRenderer noiseRenderer;
    private final FaderUtil fader = new FaderUtil(1f, 0f);
    private final int sizeOffset;
    private int backgroundAlphaMin = 125;
    private int backgroundAlphaMax = 175;

    public FoldingPanel(UIPanelAPI parent, int width, int height, String borderPrefix,
        int borderThickness
    ) {
        super(parent, width + borderThickness * 2 + pad*2, height + borderThickness * 2 + pad*2);

        sizeOffset = borderThickness * 2 + pad*2;
        
        this.borderThickness = borderThickness;
        borderRenderer = new BorderRenderer(borderPrefix, width, height);
        initializeBackground();
        noiseRenderer.fadeOut(0.4f);
    }

    public FoldingPanel(int width, int height, String borderPrefix, int borderThickness) {
        this(Attachments.getScreenPanel(), width, height, borderPrefix, borderThickness);
    }

    public FoldingPanel(int width, int height, String borderPrefix) {
        this(width, height, borderPrefix, 7);
    }

    public FoldingPanel(int width, int height) {
        this(width, height, "ui_border1", 7);
    }

    public FoldingPanel() {
        this(100, 100, "ui_border1", 7);
    }

    public void createPanel() {}

    public void setBorder(String prefix) {
        borderRenderer = new BorderRenderer(prefix, getPos().getWidth(), getPos().getHeight());
    }

    public void setBackgroundAlpha(int min, int max) {
        backgroundAlphaMin = min;
        backgroundAlphaMax = max;
        initializeBackground();
    }

    private void initializeBackground() {
        final PositionAPI pos = getPos();
        final SettingsAPI settings = Global.getSettings();
        backgroundLayer = new PanelFillRenderer(settings.getSprite("ui", "scanline11"),
            pos.getWidth() - pad * 3, pos.getHeight() - pad * 3
        );
        backgroundLayer.setColors(
            new Color(0, 0, 0, 125), new Color(0, 0, 0, backgroundAlphaMin));
        backgroundLayer.setOverlayColors(
            new Color(0, 0, 0, 175), new Color(0, 0, 0, backgroundAlphaMax));

        backgroundLayer.useAdditiveBlend = false;
        backgroundLayer.edgeSize = 7f;

        foregroundLayer = new PanelFillRenderer(settings.getSprite("ui", "scanline11"),
            pos.getWidth() - pad * 3, pos.getHeight() - pad * 3
        );
        foregroundLayer.useAdditiveBlend = true;
        foregroundLayer.setColors(new Color(10, 38, 44, 0), new Color(10, 38, 44, 0));
        noiseRenderer = new NoiseRenderer(settings.getSprite("ui", "noise"),
            pos.getWidth() - pad * 3, pos.getHeight() - pad * 3
        );
    }

    public void foldOut(float dur) {
        fader.setDurationIn(dur);
        fader.fadeIn();
    }

    public void forceFoldIn() {
        fader.forceOut();
    }

    public void forceFoldOut() {
        fader.forceIn();
    }

    public void foldIn(float dur) {
        fader.setDurationOut(dur);
        fader.fadeOut();
    }

    public void setNext(UIComponentAPI comp) {
        if (comp == null) return;
        noiseRenderer.fadeIn(0.2f, 0.2f);
        if (!transitionEnabled) noiseRenderer.fader.forceIn();

        nextPanel = comp;
        final float w = getContentWidth();
        final float h = getContentHeight();
        remove(currentPanel);
        comp.getPosition().setSize(w, h);
        addPositionOnly(comp).inTL(borderThickness, borderThickness);
        if (!transitionEnabled) {
            currentPanel = nextPanel;
            nextPanel = null;
            noiseRenderer.fader.forceOut();
        }
    }

    public PositionAPI setSize(float width, float height) {
        if (currentPanel != null) {
            currentPanel.getPosition().setSize(
                width - sizeOffset, height - sizeOffset
            );
        }

        final PositionAPI pos = getPos().setSize(width, height);
        initializeBackground();
        return pos;
    }

    public float getContentWidth() {
        return getPos().getWidth() - sizeOffset;
    }

    public float getContentHeight() {
        return getPos().getHeight() - sizeOffset;
    }

    public void flickerNoise(float inDuration, float outDuration) {
        noiseRenderer.fadeIn(inDuration, outDuration);
    }

    @Override
    public void advance(float delta) {
        super.advance(delta);
        if (nextPanel != null && noiseRenderer.isMaxBrightness()) {
            currentPanel = nextPanel;
            nextPanel = null;
            if (!transitionEnabled) {
                noiseRenderer.fader.forceOut();
            }
        }

        if (fader.getBrightness() != 0.0F || !fader.isIdle()) {
            backgroundLayer.advance(delta);
            foregroundLayer.advance(delta);
            noiseRenderer.advance(delta);
            fader.advance(delta);
            if (currentPanel != null) {
                currentPanel.advance(delta);
            }
        }
    }

    public FaderUtil getFader() {
        return fader;
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        super.processInput(events);
        if (currentPanel != null && fader.getBrightness() >= 0.75f && nextPanel == null) {
            currentPanel.processInput(events);
        }
    }

    @Override
    public void renderBelow(float alpha) {
        super.renderBelow(alpha);
        if (fader.getBrightness() == 0f && fader.isIdle()) return;

        final float brightness = fader.getBrightness() * alpha;
        final float heightScale = Math.min(1f, brightness / 0.75f);
        final float transitionAlpha = Math.max(0f, (brightness - 0.75f) / 0.25f);
        final float borderAlphaFactor = Math.min(1f, brightness / 0.25f);
        final float panelWidth = pos.getWidth();
        final float panelHeight = pos.getHeight() * heightScale;

        final float x = pos.getX();
        final float y = pos.getY() + pos.getHeight() / 2f - panelHeight / 2f;
        if (renderBackground) {
            borderRenderer.setSize(panelWidth, panelHeight);
            borderRenderer.render(x, y, brightness * borderAlphaFactor);
        }

        if (brightness != 1f || isAlwaysScissor) {
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            
            final float scale = Global.getSettings().getScreenScaleMult();
            final int scissorX = (int) (x + borderThickness * scale);
            final int scissorY = (int) (y + borderThickness * scale);
            final int scissorW = (int) ((panelWidth - sizeOffset) * scale);
            final int scissorH = (int) ((panelHeight - sizeOffset) * scale);
            GL11.glScissor(scissorX, scissorY, scissorW, scissorH);
        }

        if (renderBackground) {
            backgroundLayer.renderVerticalGradient(pos.getX() + pad, pos.getY() + pad,
                brightness * borderAlphaFactor
            );
        }

        if (currentPanel != null) {
            if (transitionEnabled && renderBackground) {
            currentPanel.render(transitionAlpha * brightness * (1f - noiseRenderer.getBrightness()));
            } else {
            currentPanel.render(transitionAlpha * brightness);
            }
        }

        if (renderBackground) {
            foregroundLayer.renderVerticalGradient(pos.getX() + pad, pos.getY() + pad, 
                brightness * borderAlphaFactor
            );
            noiseRenderer.render(pos.getX() + pad, pos.getY() + pad,
                brightness * noiseAlpha
            );
        }

        if (brightness != 1f || isAlwaysScissor) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
    }

    public UIComponentAPI getCurr() { return currentPanel; }
    public UIComponentAPI getNext() { return nextPanel; }
}