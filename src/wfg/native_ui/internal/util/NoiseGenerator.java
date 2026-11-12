package wfg.native_ui.internal.util;

public final class NoiseGenerator {
    public float startTime = 0f;
    public float baseDuration = 0.25f;
    public float amplitude = (float)Math.min(Math.random() * 0.75f + 0.5f, 1f);
    public float cycleDuration = baseDuration * 0.25F + (float)Math.random() * baseDuration * 0.75f;
    public float fadeDuration = cycleDuration / 5f;

    private float mAngle = (float)Math.random() * 360f;
    private float mIntensity = 1f;
    private float mElapsedTime = 0f;
    private float mMinTime = 0f;

    public void reset() {
        cycleDuration = baseDuration * 0.25f + (float)Math.random() * baseDuration * 0.75f;
        amplitude = (float)Math.min(Math.random() * 0.75f + 0.5f, 1f);
        startTime = 0f;
        fadeDuration = cycleDuration / 5f;
        mElapsedTime = 0f;
        mAngle = (float)Math.random() * 360f;
        mIntensity = 1f;
    }

    /** Initialize noise cycle, optionally with a reduced duration */
    public void startCycle(boolean shortenCycle) {
        mMinTime = 0.01f;
        cycleDuration = baseDuration * 0.4f + (float)Math.random() * baseDuration * 0.6f;
        if (shortenCycle) {
            cycleDuration = baseDuration * 0.4f + (float)Math.random() * baseDuration * 0.3f;
        }

        amplitude = 1f;
        fadeDuration = cycleDuration / 5f;
    }

    public float getIntensity() {
        return mIntensity * amplitude;
    }

    public float getAngle() {
        return mAngle;
    }

    public boolean advance(float delta) {
        mElapsedTime += delta;
        if (mElapsedTime > cycleDuration && mMinTime <= 0f) {
            reset();
            return true;
        } else {
            if (mElapsedTime > startTime + fadeDuration) {
                mIntensity -= delta / (cycleDuration - startTime - fadeDuration);
                if (mIntensity < 0f) {
                mIntensity = 0f;
                }
            } else {
                mIntensity = 1f;
            }

            return false;
        }
    }
}