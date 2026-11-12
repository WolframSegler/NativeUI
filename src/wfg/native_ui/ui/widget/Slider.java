package wfg.native_ui.ui.widget;

import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.graphics.util.GLListManager;
import com.fs.graphics.util.GLListManager.GLListToken;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.util.FaderUtil;

import wfg.native_ui.example.widget.SliderExample;
import wfg.native_ui.internal.ui.core.UIContainer;
import wfg.native_ui.ui.component.InputSnapshotComp;
import wfg.native_ui.ui.component.NativeComponents;
import wfg.native_ui.ui.core.UIElementFlags.HasInputSnapshot;
import wfg.native_ui.util.Arithmetic;
import wfg.native_ui.util.RenderUtils;

import static wfg.native_ui.util.Globals.settings;
import static wfg.native_ui.util.UIConstants.text_color;

import java.awt.Color;
import java.util.List;
import java.util.function.Supplier;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

/**
 * <p><strong>Example: </strong> {@link SliderExample}</p>
 */
public class Slider extends UIContainer implements HasInputSnapshot {
    protected final InputSnapshotComp input = comp().get(NativeComponents.INPUT_SNAPSHOT);

    public float minRange = 0f;
    public float maxRange = 1f;
    public float minValue = 0f;
    public float maxValue = Float.MAX_VALUE;
    public LabelAPI label;
    public String labelText = null;
    public Color labelColor = text_color;
    public Color labelValueColor = text_color;
    public float flashOnOverflowFraction = Float.MAX_VALUE;
    public boolean showNoText = false;
    public boolean showValueOnly = false;
    public boolean clampCurrToMax = false;
    public boolean showDecimalForValueOnlyMode = false;
    public int numSubdivisions = 0;
    public boolean showPercent = false;
    public boolean showPercentAndTitle = false;
    public float scrollSpeed = 100f;
    public boolean showLabelOnly = false;
    public boolean roundBarValue = false;
    public int roundingIncrement = 1;
    public Color widgetColor = settings.getColor("widgetBorderColorBright");
    public boolean showAdjustableIndicator = false;

    /**
     * Optional supplier that dynamically provides the label's text. Example:
     * <pre>
     * slider.customText = () -> Misc.getDGSCredits(slider.getProgress());
     * </pre>
     */
    public Supplier<String> customText = null;

    private final int windowWidth = Display.getWidth();
    private final int windowHeight = Display.getWidth();

    private float mCachedMaxValue = 1f;
    private float mProgressValue = 0f;
    private float mCachedProgressValue = 0f;
    private float mCachedMax = Float.MAX_VALUE;
    private float mCachedMin = 0f;
    private float mPotentialDecreaseAmount = 0f;
    private float mCachedPotentialDecreaseAmount = 0f;
    private float mCachedShowNotchOnIfBelowProgress = -3.4028235E38f;
    private float mShowNotchOnIfBelowProgress = -3.4028235E38f;
    private SpriteAPI mLineTexture = settings.getSprite("graphics/hud/line4x4.png");
    private FaderUtil mBarHighlightFader = null;
    private boolean mUserAdjustable = true;
    private Color mBarColor = settings.getColor("progressBarStandardColor");
    private Color mBarColorOverflow = settings.getColor("progressBarOverflowColor");
    private boolean mShouldInterpolateCachedValues = false;
    private FaderUtil mFlashOnOverflowFader = null;
    private GLListToken mGLListToken;
    private boolean mLineUpTextOnCenter = false;
    private float mLineUpTextOnCenterWidth = 0f;
    private boolean mHighlightBrightnessOverride = false;
    private float mHighlightBrightnessOverrideValue = -1f;
    private float mCachedAlphaMult = -1f;
    private float mCachedHighlightBrightness = -1f;

    public Slider(String initialText, float minRange, float maxRange, float width, float height) {
        super(width, height);

        this.labelText = initialText;
        this.minRange = minRange;
        this.maxRange = maxRange;
        mCachedMaxValue = maxRange;

        createLabel(null);
    }

    private void createLabel(String fontInput) {
        final String font = fontInput == null ? Fonts.DEFAULT_SMALL : fontInput;
        label = settings.createLabel("", font);
        label.setColor(labelColor);
        label.setHighlightOnMouseover(true);
        label.setAlignment(Alignment.MID);
        add(label).inMid();
        label.autoSizeToWidth(label.computeTextWidth(label.getText()));
    }

    public void setPotentialDecreaseAmount(float amount) {
        if (mPotentialDecreaseAmount != amount) {
            GLListManager.invalidateList(mGLListToken);
        }

        mPotentialDecreaseAmount = amount;
    }

    public float getShowNotchOnIfBelowProgress() {
        return mShowNotchOnIfBelowProgress;
    }

    public void setShowNotchOnIfBelowProgress(float progress) {
        if (mShowNotchOnIfBelowProgress != progress) {
            GLListManager.invalidateList(mGLListToken);
        }

        mShowNotchOnIfBelowProgress = progress;
    }

    public void setHighlightOnMouseover(boolean bool) {
        if (bool) {
            mBarHighlightFader = new FaderUtil(0.05f, 0.25f);
        } else {
            mBarHighlightFader = null;
        }

    }

    public void setUserAdjustable(boolean bool) {
        mUserAdjustable = bool;
        setHighlightOnMouseover(bool);
    }

    public float getProgress() {
        return mProgressValue;
    }

    public float getProgressInterpolated() {
        return mCachedProgressValue;
    }

    public void setLineUpTextOnCenter(boolean bool, float width) {
        mLineUpTextOnCenter = bool;
        mLineUpTextOnCenterWidth = width;
    }

    public FaderUtil getBarHighlightFader() {
        return mBarHighlightFader;
    }

    public void setProgress(float progress) {
        progress = Math.max(minRange, progress);

        if (mProgressValue != progress) {
            GLListManager.invalidateList(mGLListToken);
        }

        mProgressValue = progress;
    }

    public void setBarColor(Color color) {
        if (!mBarColor.equals(color)) {
            GLListManager.invalidateList(mGLListToken);
        }

        mBarColor = color;
    }

    public Color getBarColor() {
        return mBarColor;
    }

    public void setLabelFont(String font) {
        final String text = label.getText();
        createLabel(font);
        label.setText(text);
    }

    public void setBarColorOverflow(Color color) {
        if (!mBarColorOverflow.equals(color)) {
            GLListManager.invalidateList(mGLListToken);
        }

        mBarColorOverflow = color;
    }

    public FaderUtil getHighlight() {
        return mBarHighlightFader;
    }

    public float getXCoordinateForProgressValue(float progress) {
        final float w = getWidth() - 8f;
        return w * (mCachedProgressValue - minRange) / (maxRange - minRange) +
                w * (progress - minRange) / (maxRange - minRange) + 4.5f;
    }

    public void setHighlightBrightnessOverride(float brightness) {
        mHighlightBrightnessOverrideValue = brightness;
        mHighlightBrightnessOverride = true;
    }

    public void forceSync() {
        boolean needsRefresh = false;
        if (mCachedProgressValue != mProgressValue || mCachedMin != minValue ||
                mCachedMaxValue != maxRange || mCachedMax != maxValue
                || mCachedPotentialDecreaseAmount != mPotentialDecreaseAmount
                || mCachedShowNotchOnIfBelowProgress != mShowNotchOnIfBelowProgress)
            needsRefresh = true;

        mCachedProgressValue = mProgressValue;
        mCachedMin = minValue;
        mCachedMaxValue = maxRange;
        mCachedMax = maxValue;
        mCachedPotentialDecreaseAmount = mPotentialDecreaseAmount;
        mCachedShowNotchOnIfBelowProgress = mShowNotchOnIfBelowProgress;
        if (needsRefresh) {
            GLListManager.invalidateList(mGLListToken);
        }
    }

    @Override
    public void renderBelowImpl(float alpha) {
        super.renderBelowImpl(alpha);
        float roundedProgress = mCachedProgressValue;
        if (roundBarValue) {
            roundedProgress = Math.round(roundedProgress / roundingIncrement) * roundingIncrement;
            if (roundingIncrement != 1) {
                mCachedProgressValue = Math.round(roundedProgress / roundingIncrement) * roundingIncrement;
            }
        }

        float highlightBrightness = (mBarHighlightFader != null)
            ? mBarHighlightFader.getBrightness() : -1f;

        if (mHighlightBrightnessOverride) {
            highlightBrightness = mHighlightBrightnessOverrideValue;
        }

        if (alpha != mCachedAlphaMult || highlightBrightness != mCachedHighlightBrightness) {
            GLListManager.invalidateList(mGLListToken);
        }

        mCachedAlphaMult = alpha;
        mCachedHighlightBrightness = highlightBrightness;
        mShouldInterpolateCachedValues = true;
        final float x = 0;
        final float y = 0;
        final float w = getWidth();
        final float h = getHeight();
        GL11.glPushMatrix();
        GL11.glTranslatef(getX(), getY(), 0f);

        if (!GLListManager.callList(mGLListToken)) {
            mGLListToken = GLListManager.beginList();

            // Background
            RenderUtils.drawQuad(x, y, w, h, Color.BLACK, alpha, false);

            // Layout offsets
            float leftMargin = x + 3f;
            float usableWidth = w - 8f;

            // Adjust for very thin sliders
            if (h <= 5f) {
                leftMargin -= 1f;
                usableWidth += 2f;
            }

            // Precompute ratios
            float minBarWidth = usableWidth * mCachedMin / (mCachedMaxValue - minRange);
            float progressBarWidth = usableWidth * (roundedProgress - minRange) / (mCachedMaxValue - minRange);
            float underfillWidth = usableWidth * (mCachedMaxValue - roundedProgress) / (mCachedMaxValue - minRange);
            float overflowWidth = 0f;
            float overAmount;

            if (roundedProgress > mCachedMaxValue) {
                minBarWidth = usableWidth * mCachedMin / (roundedProgress - minRange);
                progressBarWidth = usableWidth * mCachedMaxValue / (roundedProgress - minRange);
                overflowWidth = usableWidth * (roundedProgress - mCachedMaxValue) / (roundedProgress - minRange);
                underfillWidth = 0f;
                overAmount = roundedProgress - mCachedMaxValue;
            }

            final boolean thin = h <= 5f;

            { // Gradient Border Glow
                
                // Left edge
                RenderUtils.drawGradientSprite(x, y, x, y + h, 2f, widgetColor, false, 0.5f * alpha, 0.5f * alpha, 0.5f * alpha);
                RenderUtils.drawGradientSprite(x + 1f, y, x + 1f, y + h, 2f, widgetColor, true, 1f * alpha, 1f * alpha, alpha);
    
                // Right edge
                RenderUtils.drawGradientSprite(x + w, y, x + w, y + h, 2f, widgetColor, false, 0.5f * alpha, 0.5f * alpha, 0.5f * alpha);
                RenderUtils.drawGradientSprite(x + w - 1f, y, x + w - 1f, y + h, 2f, widgetColor, true, 1f * alpha, 1f * alpha, alpha);
    
                if (!thin) {
                    // Left edge horizontal glow at top/bottom
                    RenderUtils.drawGradientSprite(x + 1f, y, x + 15f, y, 1f, widgetColor, false, 1f * alpha, 0.5f * alpha, 0f);
                    RenderUtils.drawGradientSprite(x + 1f, y + h - 1f, x + 15f, y + h, 1f, widgetColor, false, 1f * alpha, 0.5f * alpha, 0f);
    
                    // Right edge horizontal glow at top/bottom
                    RenderUtils.drawGradientSprite(x + w - 1f, y, x + w - 15f, y, 1f, widgetColor, false, 1f * alpha, 0.5f * alpha, 0f);
                    RenderUtils.drawGradientSprite(x + w - 1f, y + h - 1f, x + w - 15f, y + h, 1f, widgetColor, false, 1f * alpha, 0.5f * alpha, 0f);
                }
            }

            if (minBarWidth > 0f) {  // Progress bar underfill Segment
                overAmount = Math.min(10f, minBarWidth);

                // Top underfill segment
                RenderUtils.drawGradientSprite(
                    mLineTexture, leftMargin - 2f, y + 1f,
                    leftMargin - 2f + overAmount, y + 1f,
                    1f,
                    widgetColor, false,
                    0f, 0.5f * alpha, alpha
                );

                // Top main bar segment
                RenderUtils.drawGradientSprite(
                    mLineTexture, leftMargin - 2f + overAmount, y + 1f,
                    leftMargin + minBarWidth - 1.5f, y + 1f,
                    1f,
                    widgetColor, false,
                    alpha, alpha, alpha
                );

                // Bottom underfill segment
                RenderUtils.drawGradientSprite(
                    mLineTexture, leftMargin - 2f, y + h - 2f,
                    leftMargin - 2f + overAmount, y + h - 2f,
                    1f,
                    widgetColor, false,
                    0f, 0.5f * alpha, alpha
                );

                // Bottom main bar segment
                RenderUtils.drawGradientSprite(
                    mLineTexture, leftMargin - 2f + overAmount, y + h - 2f,
                    leftMargin + minBarWidth - 1.5f, y + h - 2f,
                    1f,
                    widgetColor, false,
                    alpha, alpha, alpha
                );

                // Right vertical bar
                RenderUtils.drawGradientSprite(
                    mLineTexture, leftMargin + minBarWidth - 1.5f, y + 1f,
                    leftMargin + minBarWidth - 1.5f, y + h - 1f,
                    1f,
                    widgetColor, false,
                    alpha, alpha, alpha
                );

                // Right white edge
                RenderUtils.drawGradientSprite(
                    mLineTexture, leftMargin + minBarWidth - 1.5f, y + 1f,
                    leftMargin + minBarWidth - 1.5f, y + h - 1f,
                    1f,
                    Color.WHITE, true,
                    0f, alpha, 0f
                );

                leftMargin += minBarWidth;
            }

            if (underfillWidth > 0f) {  // Underfill / Right-side Bar Segment
                overAmount = Math.min(10f, underfillWidth);
                float rightEdgeX = x + 6f + usableWidth;

                if (thin) {
                    // Small slider: single horizontal lines
                    RenderUtils.drawGradientSprite(rightEdgeX, y + 1f, rightEdgeX - underfillWidth - 1f, y + 1f,
                            1f, widgetColor, false, alpha, alpha, alpha);
                    RenderUtils.drawGradientSprite(rightEdgeX, y + h - 2f, rightEdgeX - underfillWidth - 1f, y + h - 2f,
                            1f, widgetColor, false, alpha, alpha, alpha);
                } else if (numSubdivisions <= 0) {
                    // Regular slider: split horizontal + vertical segments
                    RenderUtils.drawGradientSprite(rightEdgeX + 2f, y + 1f, rightEdgeX + 2f - overAmount, y + 1f,
                        1f, widgetColor, false, 0f, 0.5f * alpha, alpha);
                    RenderUtils.drawGradientSprite(rightEdgeX + 2f - overAmount, y + 1f, rightEdgeX - underfillWidth + 1.5f, y + 1f,
                        1f, widgetColor, false, alpha, alpha, alpha);
                    RenderUtils.drawGradientSprite(rightEdgeX + 2f, y + h - 2f, rightEdgeX + 2f - overAmount, y + h - 2f,
                        1f, widgetColor, false, 0f, 0.5f * alpha, alpha);
                    RenderUtils.drawGradientSprite(rightEdgeX + 2f - overAmount, y + h - 2f, rightEdgeX - underfillWidth + 1.5f, y + h - 2f,
                        1f, widgetColor, false, alpha, alpha, alpha);
                    
                    // Vertical edge lines
                    RenderUtils.drawGradientSprite(rightEdgeX - underfillWidth + 1.5f, y + 1f, rightEdgeX - underfillWidth + 1.5f, y + h - 1f,
                        1f, widgetColor, false, alpha, alpha, alpha);
                    RenderUtils.drawGradientSprite(rightEdgeX - underfillWidth + 1.5f, y + 1f, rightEdgeX - underfillWidth + 1.5f, y + h - 1f,
                        1f, Color.WHITE, false, 0f, alpha, 0f);
                }
            }

            overAmount = progressBarWidth - 2f - (leftMargin - x - 6f);
            float highlightIntensity = 0f;
            if (mBarHighlightFader != null || mHighlightBrightnessOverride) {
                highlightIntensity += highlightBrightness;
                mHighlightBrightnessOverride = false;
            }

            float maxNotchPos;
            float maxNotchOffset;
            if (mCachedMax < mCachedMaxValue && mCachedMax > roundedProgress) { // Highlight Notch Rendering
                if (overAmount > 0f) {
                    if (mCachedShowNotchOnIfBelowProgress < roundedProgress
                            && mCachedShowNotchOnIfBelowProgress >= minRange) {

                        // Compute notch position
                        maxNotchPos = usableWidth * (mCachedShowNotchOnIfBelowProgress - minRange) / (mCachedMaxValue - minRange);
                        maxNotchOffset = getXCoordinateForProgressValue(mCachedShowNotchOnIfBelowProgress) - leftMargin - overAmount;

                        if (-maxNotchOffset <= 2f) {
                            // Simple notch rectangle
                            RenderUtils.drawHighlightBar(leftMargin, y + 1f, overAmount, h - 2f, mBarColor,
                                alpha, highlightIntensity, false
                            );
                        } else {
                            // Split the highlight bar for the notch
                            RenderUtils.drawHighlightBar(leftMargin, y + 1f, overAmount + maxNotchOffset,
                                h - 2f, mBarColor, alpha, highlightIntensity, false
                            );

                            if (-maxNotchOffset > 0f) {
                                RenderUtils.drawHighlightBar(
                                    leftMargin + overAmount + maxNotchOffset, y + 1f, -maxNotchOffset, h - 2f,
                                    mBarColor, alpha * 0.7f, highlightIntensity, false
                                );
                            }
                        }
                    } else {
                        // mCachedShowNotchOnIfBelowProgress is outside range
                        RenderUtils.drawHighlightBar(
                            leftMargin, y + 1f, overAmount, h - 2f,
                            mBarColor, alpha, highlightIntensity, false
                        );
                    }
                }

                // Highlight remaining section of the bar
                maxNotchPos = usableWidth * (mCachedMax - minRange) / (mCachedMaxValue - minRange);
                maxNotchOffset = maxNotchPos - overAmount;
                if (maxNotchOffset > 0f) {
                    RenderUtils.drawHighlightBar(
                        leftMargin + overAmount + 4f,
                        y + 3f,
                        maxNotchOffset,
                        h - 6f,
                        mBarColor,
                        alpha * 0.65f,
                        highlightIntensity,
                        true
                    );
                }

                // Draw the vertical end line
                RenderUtils.drawGradientSprite(
                    mLineTexture,
                    leftMargin + overAmount + 4f + maxNotchOffset + 1.5f, y + 2f,
                    leftMargin + overAmount + 4f + maxNotchOffset + 1.5f, y + h - 2f,
                    1f, widgetColor, false,
                    alpha, alpha, alpha
                );
            }
            else { // drawOverflowAndNotches
                if (overAmount > 0f) {

                    // Draw max progress notch if current progress is near the cached max
                    if (mCachedMax <= roundedProgress && roundedProgress < maxRange - 1f
                            && mCachedMax >= roundedProgress - 1f) {
                        maxNotchPos = usableWidth * (mCachedMax - minRange) / (mCachedMaxValue - minRange);
                        maxNotchOffset = maxNotchPos - overAmount;
                        RenderUtils.drawGradientSprite(mLineTexture, 
                            leftMargin + overAmount + 4f + maxNotchOffset + 1.5f, y + 2f, 
                            leftMargin + overAmount + 4f + maxNotchOffset + 1.5f, y + h - 2f, 
                            1f, widgetColor, false, alpha, alpha, alpha
                        );
                    }

                    // Draw CachedShowNotch highlight
                    if (mCachedShowNotchOnIfBelowProgress < roundedProgress &&
                        mCachedShowNotchOnIfBelowProgress >= minRange
                    ) {
                        float notchRelativeOffset = getXCoordinateForProgressValue(mCachedShowNotchOnIfBelowProgress) - leftMargin - overAmount;

                        if (-notchRelativeOffset <= 2f) {
                            RenderUtils.drawHighlightBar(leftMargin, y + 1f, overAmount, h - 2f, mBarColor, alpha, highlightIntensity, false);
                        } else {
                            RenderUtils.drawHighlightBar(leftMargin, y + 1f, overAmount + notchRelativeOffset,
                                h - 2f, mBarColor, alpha, highlightIntensity, false
                            );

                            if (-notchRelativeOffset > 0f) {
                                RenderUtils.drawHighlightBar(leftMargin + overAmount + notchRelativeOffset,
                                    y + 1f, -notchRelativeOffset, h - 2f, mBarColor, alpha * 0.7f, 
                                    highlightIntensity, false
                                );
                            }
                        }
                    } 
                    // Draw mCachedMax notch if CachedShowNotch is not in range
                    else if (mCachedMax < mCachedMaxValue) {
                        maxNotchPos = usableWidth * (mCachedMax - minRange) / (mCachedMaxValue - minRange);
                        maxNotchOffset = getXCoordinateForProgressValue(mCachedMax) - leftMargin - overAmount;

                        if (-maxNotchOffset <= 2f) {
                            RenderUtils.drawHighlightBar(leftMargin, y + 1f, overAmount, h - 2f, mBarColor, alpha, highlightIntensity, false);
                        } else {
                            RenderUtils.drawHighlightBar(leftMargin, y + 1f, overAmount + maxNotchOffset + 2f, h - 2f, mBarColor, alpha, highlightIntensity, false);

                            if (-maxNotchOffset > 0f) {
                                RenderUtils.drawHighlightBar(leftMargin + overAmount + maxNotchOffset + 2f, y + 1f,
                                    -maxNotchOffset - 2f, h - 2f, mBarColor, alpha * 0.7f, highlightIntensity, false
                                );
                            }
                        }
                    } 
                    // Default highlight
                    else {
                        RenderUtils.drawHighlightBar(leftMargin, y + 1f, overAmount, h - 2f, mBarColor, alpha, highlightIntensity, false);
                    }
                }

                // Draw overflow highlight if progress exceeds cached max
                if (!(mCachedMax < mCachedMaxValue)) {
                    float overflowNotchPos = usableWidth * (mCachedMaxValue - minRange) / (mCachedMaxValue - minRange);
                    float overflowNotchOffset = overflowNotchPos - overAmount - 4f - overflowWidth;

                    if (overflowNotchOffset > 0f) {
                        RenderUtils.drawHighlightBar(
                            leftMargin + overAmount + 5f, y + 3f, overflowNotchOffset, h - 6f,
                            mBarColor, alpha * 0.65f, highlightIntensity * 1f, true
                        );
                    }
                }
            }
            
            if (mCachedPotentialDecreaseAmount > 0f) { // Decrease Notch Indicator
                maxNotchPos = Math.max(0f, roundedProgress - mCachedPotentialDecreaseAmount);
                maxNotchOffset = usableWidth * (roundedProgress - mCachedPotentialDecreaseAmount - minRange)
                    / (mCachedMaxValue - minRange);
                final float notchX = usableWidth * (maxNotchPos - minRange) / (mCachedMaxValue - minRange);

                // Draw the gradient bar for the notch
                RenderUtils.drawGradientSprite(
                    mLineTexture,
                    x + notchX + 5.5f, y,
                    x + notchX + 5.5f, y + h,
                    1f, widgetColor, false,
                    0.5f * alpha, alpha, 0.5f * alpha
                );

                // Draw the additive white highlight on top
                RenderUtils.drawGradientSprite(
                    mLineTexture,
                    x + notchX + 5.5f, y,
                    x + notchX + 5.5f, y + h,
                    1f, Color.white, true,
                    0f, alpha, 0f
                );
            }

            if (overflowWidth > 2f) { // Overflow Highlight Bar
                overAmount = overflowWidth - 1f;
                highlightIntensity = 0f;
                if (mFlashOnOverflowFader != null) {
                    highlightIntensity += mFlashOnOverflowFader.getBrightness();
                }

                RenderUtils.drawHighlightBar(
                    leftMargin + progressBarWidth - (leftMargin - x - 6f),
                    y + 1f,
                    overAmount,
                    h - 2f,
                    mBarColorOverflow,
                    alpha,
                    highlightIntensity,
                    false
                );
            }

            RenderUtils.drawGradientSprite(mLineTexture, x + progressBarWidth + 5.5f, y,
                x + progressBarWidth + 5.5f, y + h, 1f, widgetColor, false,
                0.5f * alpha, alpha, 0.5f * alpha
            );

            if (mUserAdjustable && showAdjustableIndicator) { // User Adjustable Indicator
                RenderUtils.drawGradientSprite(mLineTexture, x + progressBarWidth + 5.5f, y,
                    x + progressBarWidth + 5.5f, y + h, 2f, Color.white, false, 
                    0f, alpha, 0f
                );
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                for (overAmount = -3f; overAmount <= 3f; ++overAmount) {
                    highlightIntensity = Math.min(Math.max(h * 0.5f, 6f), 12f);
                    maxNotchPos = 0.61728394f;
                    maxNotchOffset = x + progressBarWidth + 5.5f + overAmount * 0.25f;
                    float bottomY = y + h;
                    float alphaMult = alpha * 0.5f;

                    GL11.glBegin(GL11.GL_TRIANGLES);
                    RenderUtils.setGlColor(widgetColor, alphaMult);
                    GL11.glVertex2f(maxNotchOffset, y);
                    RenderUtils.setGlColor(widgetColor, alphaMult * 0f);
                    GL11.glVertex2f(maxNotchOffset - highlightIntensity, y - highlightIntensity * maxNotchPos);
                    GL11.glVertex2f(maxNotchOffset + highlightIntensity, y - highlightIntensity * maxNotchPos);

                    RenderUtils.setGlColor(widgetColor, alphaMult);
                    GL11.glVertex2f(maxNotchOffset, bottomY);
                    RenderUtils.setGlColor(widgetColor, alphaMult * 0f);
                    GL11.glVertex2f(maxNotchOffset - highlightIntensity, bottomY + highlightIntensity * maxNotchPos);
                    GL11.glVertex2f(maxNotchOffset + highlightIntensity, bottomY + highlightIntensity * maxNotchPos);
                    GL11.glEnd();
                }
            }

            if (numSubdivisions > 0) { // Subdivision Notches & Underfill Highlight
                for (int i = 0; i < numSubdivisions - 1; ++i) {
                    final float subdivisionX = (int)(usableWidth / (float) numSubdivisions) * (i + 1) + 2;
                    final float notchPadding = 3f;
                    final float topOffset = getXCoordinateForProgressValue(mProgressValue) >
                        leftMargin + subdivisionX ? 1f : 3f;

                    RenderUtils.drawGradientSprite(mLineTexture, leftMargin + subdivisionX - 1f, y + topOffset,
                        leftMargin + subdivisionX - 1f, y + h - topOffset,
                        1f, Color.black, false, alpha, alpha, alpha
                    );

                    RenderUtils.drawGradientSprite(mLineTexture, leftMargin + subdivisionX, y + topOffset,
                        leftMargin + subdivisionX, y + h - topOffset,
                        1f, widgetColor, false, alpha * 0.5f, alpha, alpha * 0.5f
                    );

                    RenderUtils.drawGradientSprite(mLineTexture, leftMargin + subdivisionX + 1f, y + topOffset,
                        leftMargin + subdivisionX + 1f, y + h - topOffset,
                        1f, Color.black, false, alpha, alpha, alpha
                    );

                    RenderUtils.drawGradientSprite(mLineTexture, leftMargin + subdivisionX - notchPadding,
                        y - 1f, leftMargin + subdivisionX + notchPadding + 1f, y - 1f,
                        1f, widgetColor, false, alpha, alpha, alpha
                    );

                    RenderUtils.drawGradientSprite(mLineTexture, leftMargin + subdivisionX - notchPadding, y,
                        leftMargin + subdivisionX + notchPadding + 1f, y,
                        1f, Color.black, false, alpha, alpha, alpha
                    );

                    RenderUtils.drawGradientSprite(mLineTexture, leftMargin + subdivisionX - notchPadding, y + h,
                        leftMargin + subdivisionX + notchPadding + 1f, y + h,
                        1f, widgetColor, false, alpha, alpha, alpha
                    );

                    RenderUtils.drawGradientSprite(mLineTexture, leftMargin + subdivisionX - notchPadding,
                        y + h - 1f, leftMargin + subdivisionX + notchPadding + 1f, y + h - 1f,
                        1f, Color.black, false, alpha, alpha, alpha
                    );
                }

                overAmount = Math.min(10f, underfillWidth);
                final float baseX = x + 6f + usableWidth;

                RenderUtils.drawGradientSprite(mLineTexture, baseX + 2f, y + 1f, baseX + 2f - overAmount,
                    y + 1f, 1f, widgetColor, false, 0f,
                    0.5f * alpha, alpha
                );

                RenderUtils.drawGradientSprite(mLineTexture, baseX + 2f - overAmount, y + 1f,
                    baseX - underfillWidth + 1.5f, y + 1f, 1f, widgetColor, false,
                    alpha, alpha, alpha
                );

                RenderUtils.drawGradientSprite(mLineTexture, baseX + 2f, y + h - 2f,
                    baseX + 2f - overAmount, y + h - 2f, 1f, widgetColor,
                    false, 0f, 0.5f * alpha, alpha
                );

                RenderUtils.drawGradientSprite(mLineTexture, baseX + 2f - overAmount, y + h - 2f,
                    baseX - underfillWidth + 1.5f, y + h - 2f, 1f,
                    widgetColor, false, alpha, alpha, alpha
                );

                RenderUtils.drawGradientSprite(mLineTexture, baseX - underfillWidth + 1.5f, y + 1f,
                    baseX - underfillWidth + 1.5f, y + h - 1f, 1f, widgetColor, 
                    false, alpha, alpha, alpha
                );

                RenderUtils.drawGradientSprite(mLineTexture, baseX - underfillWidth + 1.5f, y + 1f,
                    baseX - underfillWidth + 1.5f, y + h - 1f, 1f, Color.white, 
                    false, 0f, alpha, 0f
                );
            }

            GLListManager.endList();
        }

        GL11.glPopMatrix();
    }

    @Override
    public void processInputImpl(List<InputEventAPI> events) {
        super.processInputImpl(events);
        if (!mUserAdjustable && mBarHighlightFader == null) return;

        if (mBarHighlightFader != null) {
            if (input.isActive || input.hoveredLastFrame) {
                mBarHighlightFader.fadeIn();
            } else {
                mBarHighlightFader.fadeOut();
            }
        }

        final InputEventAPI event = input.mouseMoveEvent;
        if (event == null || event.isConsumed()) return;

        if (input.isActive && mUserAdjustable) {
            mapInputToProgress(event);
            event.consume();
        }
    }

    @Override
    public void advanceImpl(float delta) {
        super.advanceImpl(delta);
        if (roundBarValue && roundingIncrement > 0) {
            mCachedProgressValue = Math.round(mCachedProgressValue / roundingIncrement) * roundingIncrement;
            mProgressValue = Math.round(mProgressValue / roundingIncrement) * roundingIncrement;
        }

        if (mCachedProgressValue != mProgressValue || mCachedMin != minValue || mCachedMax != maxValue || 
            mCachedMaxValue != maxRange || mShowNotchOnIfBelowProgress != mCachedShowNotchOnIfBelowProgress
        ) {
            GLListManager.invalidateList(mGLListToken);
        }

        if (mBarHighlightFader != null) mBarHighlightFader.advance(delta);
        if (mFlashOnOverflowFader != null) mFlashOnOverflowFader.advance(delta);

        if (clampCurrToMax && mProgressValue > maxValue) mProgressValue = maxValue;

        // Overflow flash: detect when progress exceeds the configured range and toggle the flash fader
        float overflowRatio;
        if (maxRange <= 0f) {
            overflowRatio = 10f;
        } else {
            overflowRatio = (mProgressValue - minRange) / maxRange - 1f;
            if (overflowRatio < 0f) {
                overflowRatio = 0f;
            }
        }

        if (overflowRatio > flashOnOverflowFraction) {
            if (mFlashOnOverflowFader == null) {
                mFlashOnOverflowFader = new FaderUtil(0.25f, 0.25f);
                mFlashOnOverflowFader.setBounce(true, true);
                mFlashOnOverflowFader.fadeIn();
            }
        } else if (mFlashOnOverflowFader != null) {
            mFlashOnOverflowFader.setBounceUp(false);
            mFlashOnOverflowFader.fadeOut();
            if (mFlashOnOverflowFader.isFadedOut()) {
                mFlashOnOverflowFader = null;
            }
        }

        float interpolationScale;
        if (mShouldInterpolateCachedValues) {
            scrollSpeed = 100f;

            // Compute interpolation scale based on the visible width vs the effective progress range
            interpolationScale = getWidth() / Math.max(mProgressValue - minRange, maxRange - minRange);

            float effectiveRange = mCachedMaxValue - minRange;
            if (mCachedProgressValue - minRange > effectiveRange) {
                effectiveRange = mCachedProgressValue - minRange;
            }
            if (effectiveRange < 1f) {
                effectiveRange = 1f;
            }

            interpolationScale = getWidth() / effectiveRange;

            mCachedProgressValue = Arithmetic.smoothApproach(
                mCachedProgressValue, mProgressValue,
                scrollSpeed / interpolationScale,
                0.02f * Math.abs(mCachedProgressValue - mProgressValue) * interpolationScale,
                delta
            );

            mCachedMin = Arithmetic.smoothApproach(
                mCachedMin, minValue,
                scrollSpeed / interpolationScale,
                0.02f * Math.abs(mCachedMin - minValue) * interpolationScale,
                delta
            );

            mCachedMax = Arithmetic.smoothApproach(
                mCachedMax, maxValue,
                scrollSpeed / interpolationScale,
                0.05f * Math.abs(mCachedMax - maxValue) * interpolationScale,
                delta
            );

            mCachedPotentialDecreaseAmount = Arithmetic.smoothApproach(
                mCachedPotentialDecreaseAmount, mPotentialDecreaseAmount,
                scrollSpeed / interpolationScale,
                0.05f * Math.abs(mCachedPotentialDecreaseAmount - mPotentialDecreaseAmount) * interpolationScale,
                delta
            );

            mCachedMaxValue = Arithmetic.smoothApproach(
                mCachedMaxValue, maxRange,
                scrollSpeed / interpolationScale,
                0.02f * Math.abs(mCachedMaxValue - maxRange) * interpolationScale,
                delta
            );

            mCachedShowNotchOnIfBelowProgress = Arithmetic.smoothApproach(
                mCachedShowNotchOnIfBelowProgress, mShowNotchOnIfBelowProgress,
                scrollSpeed / interpolationScale,
                0.05f * Math.abs(mCachedShowNotchOnIfBelowProgress - mShowNotchOnIfBelowProgress) * interpolationScale,
                delta
            );
        } else {
            mCachedProgressValue = mProgressValue;
            mCachedMin = minValue;
            mCachedMax = maxValue;
            mCachedMaxValue = maxRange;
            mCachedPotentialDecreaseAmount = mPotentialDecreaseAmount;
            mCachedShowNotchOnIfBelowProgress = mShowNotchOnIfBelowProgress;
        }

        mShouldInterpolateCachedValues = false;
        if (customText != null) {
            label.setText(customText.get());

        } else if (showLabelOnly) {
            label.setText(labelText);

        } else if (showPercent) {
            final String displayText = String.format("%d%%", Math.round(mCachedProgressValue));
            label.setText(displayText);

        } else if (showPercentAndTitle) {
            final String displayText = String.format("%d%%", Math.round(mCachedProgressValue));
            label.setText(String.format("%s: %s", labelText, displayText));
        } else if (showValueOnly) {
            final String displayText = showDecimalForValueOnlyMode ? 
                String.format("%.2f", mCachedProgressValue) :
                String.format("%d", Math.round(mCachedProgressValue));

            label.setText(displayText);

        } else if (labelText != null) {
            final String displayText = String.format(
                "%d / %d", Math.round(mCachedProgressValue), Math.round(mCachedMaxValue)
            );
            label.setText(String.format("%s: %s", labelText, displayText));

        } else {
            final String displayText = String.format(
                "%d / %d", Math.round(mCachedProgressValue), Math.round(mCachedMaxValue)
            );
            label.setText(displayText);
        }

        if (showNoText) label.setText("");

        float labelWidth = label.computeTextWidth(label.getText());
        label.autoSizeToWidth(labelWidth);
        label.setColor(labelColor);
        label.setHighlightColor(labelValueColor);
        if (labelWidth != 0f && mLineUpTextOnCenter) {
            final float offset = (label.getPosition().getWidth() / 2f) - labelWidth + mLineUpTextOnCenterWidth;
            label.getPosition().setXAlignOffset(offset);
        }

        if (mCachedProgressValue < mCachedMin) {
            mCachedProgressValue = mCachedMin;
        }
    }

    protected float mapInputToProgress(InputEventAPI mouseEvent) {
        final float mouseX = mouseEvent.getX();
        final float mouseY = mouseEvent.getY();
        if (mouseX < 0 || mouseX > windowWidth || mouseY < 0 || mouseY > windowHeight) {
            return mProgressValue;
        }

        final float offset = 6f;
        final float maxBarWidth = getWidth() - offset;
        final float minBarWidth = maxBarWidth * mCachedMin / (mCachedMaxValue - minRange);
        final float relativeX = Arithmetic.clamp(mouseX - getX() - offset, minBarWidth, maxBarWidth);

        float mProgressValue = relativeX / maxBarWidth * (maxRange - minRange) + minRange;
        if (clampCurrToMax && mProgressValue > maxValue) mProgressValue = maxValue;

        setProgress(mProgressValue);
        forceSync();

        return mProgressValue;
    }
}