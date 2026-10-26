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

import wfg.native_ui.internal.ui.core.UIContainer;
import wfg.native_ui.internal.util.BorderRenderer;
import wfg.native_ui.internal.util.NoiseRenderer;
import wfg.native_ui.internal.util.PanelFillRenderer;
import wfg.native_ui.ui.Attachments;

public class FoldingPanel extends UIContainer {
    private static final SpriteAPI SCANLINE_11 = settings.getSprite("ui", "scanline11");
    private static final SpriteAPI NOISE = settings.getSprite("ui", "noise");

    public boolean renderBackground = true;
    public boolean transitionEnabled = true;
    public boolean isAlwaysScissor = false;
    public float borderThickness = 7f;
    public float noiseAlpha = 0.7f;
    public float borderAlpha = 1f;

    private float mInnerOffset = 0f;
    private UIComponentAPI mCurrentPanel;
    private UIComponentAPI mNextPanel;
    private BorderRenderer mBorderRenderer;
    private PanelFillRenderer mBackgroundLayer;
    private PanelFillRenderer mForegroundLayer;
    private NoiseRenderer mNoiseRenderer;
    private final FaderUtil mFoldFader = new FaderUtil(1f, 0f);
    private int mBackgroundAlphaMin = 125;
    private int mBackgroundAlphaMax = 175;

    public FoldingPanel(UIPanelAPI parent, float width, float height, String borderPrefix,
        float borderThickness
    ) {
        super(width + (borderThickness + opad)*2, height + (borderThickness + opad)*2);
        attachTo(parent);

        this.borderThickness = borderThickness;
        mInnerOffset = borderThickness + opad;

        mBorderRenderer = new BorderRenderer(borderPrefix, false, width + mInnerOffset * 2f, height + mInnerOffset * 2f);
        initializeBackground();
        mNoiseRenderer.fadeOut(0.4f);
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
        mBorderRenderer = new BorderRenderer(prefix, false, getWidth(), getHeight());
    }

    public void setBackgroundAlpha(int min, int max) {
        mBackgroundAlphaMin = min;
        mBackgroundAlphaMax = max;
        initializeBackground();
    }

    private void initializeBackground() {
        final float width = getWidth() - borderThickness * 2f;
        final float height = getHeight() - borderThickness * 2f;

        mBackgroundLayer = new PanelFillRenderer(SCANLINE_11, width, height);
        mBackgroundLayer.setColors(
            new Color(0, 0, 0, 125), new Color(0, 0, 0, mBackgroundAlphaMin));
        mBackgroundLayer.setOverlayColors(
            new Color(0, 0, 0, 175), new Color(0, 0, 0, mBackgroundAlphaMax));

        mBackgroundLayer.useAdditiveBlend = false;

        mForegroundLayer = new PanelFillRenderer(SCANLINE_11, width, height);
        mForegroundLayer.useAdditiveBlend = true;
        mForegroundLayer.setColors(new Color(10, 38, 44, 0), new Color(10, 38, 44, 0));
        mNoiseRenderer = new NoiseRenderer(NOISE, width, height);
    }

    public void foldOut(float dur) {
        mFoldFader.setDurationIn(dur);
        mFoldFader.fadeIn();
    }

    public void forceFoldIn() {
        mFoldFader.forceOut();
    }

    public void forceFoldOut() {
        mFoldFader.forceIn();
    }

    public void foldIn(float dur) {
        mFoldFader.setDurationOut(dur);
        mFoldFader.fadeOut();
    }

    public void setNext(UIComponentAPI comp) {
        if (comp == null) return;
        mNoiseRenderer.fadeIn(0.2f, 0.2f);
        if (!transitionEnabled) mNoiseRenderer.fader.forceIn();

        mNextPanel = comp;
        final float w = getContentWidth();
        final float h = getContentHeight();
        if (mCurrentPanel != null) removePos(mCurrentPanel);
        comp.getPosition().setSize(w, h);
        addPos(comp).inBL(mInnerOffset, mInnerOffset);

        if (!transitionEnabled) {
            mCurrentPanel = mNextPanel;
            mNextPanel = null;
            mNoiseRenderer.fader.forceOut();
        }
    }

    public PositionAPI setSize(float width, float height) {
        setSize(width, height);
        initializeBackground();

        if (mCurrentPanel != null) {
            mCurrentPanel.getPosition().setSize(
                getContentWidth(), getContentHeight()
            );
        }
        return mPos;
    }

    public float getContentWidth() {
        return getWidth() - mInnerOffset * 2f;
    }

    public float getContentHeight() {
        return getHeight() - mInnerOffset * 2f;
    }

    public void flickerNoise(float inDuration, float outDuration) {
        mNoiseRenderer.fadeIn(inDuration, outDuration);
    }

    @Override
    public void advanceImpl(float delta) {
        if (mNextPanel != null && mNoiseRenderer.isMaxBrightness()) {
            mCurrentPanel = mNextPanel;
            mNextPanel = null;
            if (!transitionEnabled) {
                mNoiseRenderer.fader.forceOut();
            }
        }

        if (mFoldFader.getBrightness() != 0f || !mFoldFader.isIdle()) {
            mBackgroundLayer.advance(delta);
            mForegroundLayer.advance(delta);
            mNoiseRenderer.advance(delta);
            mFoldFader.advance(delta);
            if (mCurrentPanel != null) {
                mCurrentPanel.advance(delta);
            }
        }
    }

    public FaderUtil getmFoldFader() {
        return mFoldFader;
    }

    @Override
    public void processInputImpl(List<InputEventAPI> events) {
        if (mCurrentPanel != null && mFoldFader.getBrightness() >= 0.75f && mNextPanel == null) {
            mCurrentPanel.processInput(events);
        }
    }

    @Override
    public void renderBelowImpl(float alpha) {
        if (mFoldFader.getBrightness() == 0f && mFoldFader.isIdle()) return;

        final float brightness = mFoldFader.getBrightness() * alpha;
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

        final int scissorX = (int) ((x + mInnerOffset) * uiScale);
        final int scissorY = (int) ((y + mInnerOffset) * uiScale);
        final int scissorW = (int) ((w - mInnerOffset * 2f) * uiScale);
        final int scissorH = (int) ((h - mInnerOffset * 2f) * uiScale);

        if (renderBackground) {
            mBorderRenderer.setSize(w, h);
            mBorderRenderer.render(x, y, brightness * borderAlphaFactor * borderAlpha);
        }

        if (brightness < 1f || isAlwaysScissor) {
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
        }

        GL11.glScissor((int) x, (int) y, (int) w, (int) h);
        if (renderBackground) {
            mBackgroundLayer.renderVerticalGradient(bx, by,
                brightness * borderAlphaFactor
            );
        }

        GL11.glScissor(scissorX, scissorY, scissorW, scissorH);
        if (mCurrentPanel != null) {
            final float mult = (transitionEnabled && renderBackground) ? 1f - mNoiseRenderer.getBrightness() : 1f;

            mCurrentPanel.render(transitionAlpha * brightness * mult);
        }

        GL11.glScissor((int) x, (int) y, (int) w, (int) h);
        if (renderBackground) {
            mForegroundLayer.renderVerticalGradient(bx, by,
                brightness * borderAlphaFactor
            );
            mNoiseRenderer.render(bx, by,
                brightness * noiseAlpha
            );
        }

        if (brightness < 1f || isAlwaysScissor) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
    }

    public UIComponentAPI getCurr() { return mCurrentPanel; }
    public UIComponentAPI getNext() { return mNextPanel; }
}