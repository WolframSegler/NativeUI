package wfg.native_ui.internal.util;

public class NoiseGenerator {
    public float startTime = 0f;
    public float baseDuration = 0.25f;
    public float amplitude = (float)Math.min(Math.random() * 0.75f + 0.5f, 1f);
    public float cycleDuration = baseDuration * 0.25F + (float)Math.random() * baseDuration * 0.75f;
    public float fadeDuration = cycleDuration / 5f;

    private float angle = (float)Math.random() * 360f;
    private float intensity = 1f;
    private float elapsedTime = 0f;
    private float minTime = 0f;

    public void reset() {
        cycleDuration = baseDuration * 0.25f + (float)Math.random() * baseDuration * 0.75f;
        amplitude = (float)Math.min(Math.random() * 0.75f + 0.5f, 1f);
        startTime = 0f;
        fadeDuration = cycleDuration / 5f;
        elapsedTime = 0f;
        angle = (float)Math.random() * 360f;
        intensity = 1f;
    }

    /** Initialize noise cycle, optionally with a reduced duration */
    public void startCycle(boolean shortenCycle) {
        minTime = 0.01f;
        cycleDuration = baseDuration * 0.4f + (float)Math.random() * baseDuration * 0.6f;
        if (shortenCycle) {
            cycleDuration = baseDuration * 0.4f + (float)Math.random() * baseDuration * 0.3f;
        }

        amplitude = 1f;
        fadeDuration = cycleDuration / 5f;
    }

    public float getIntensity() {
        return intensity * amplitude;
    }

    public float getAngle() {
        return angle;
    }

    public boolean advance(float delta) {
        elapsedTime += delta;
        if (elapsedTime > cycleDuration && minTime <= 0f) {
            reset();
            return true;
        } else {
            if (elapsedTime > startTime + fadeDuration) {
                intensity -= delta / (cycleDuration - startTime - fadeDuration);
                if (intensity < 0f) {
                intensity = 0f;
                }
            } else {
                intensity = 1f;
            }

            return false;
        }
    }
}