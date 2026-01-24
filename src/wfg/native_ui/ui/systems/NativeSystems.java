package wfg.native_ui.ui.systems;

/**
 * Enum defining the "core" systems that NativeUI provides.
 * Used for O(1) access inside {@link UISystemContainer}.
 * Order of elements matters.
 */
public enum NativeSystems {
    INPUT_SNAPSHOT,
    BACKGROUND,
    AUDIO_FEEDBACK,
    HOVER_GLOW,
    OUTLINE,
    INTERACTION,
    TOOLTIP,
}