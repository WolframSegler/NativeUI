package wfg.native_ui.ui.component;

/**
 * Enum defining the "core" components that NativeUI provides.
 * Used for O(1) access inside {@link UIComponentContainer}.
 * Order of elements matter.
 */
public enum NativeComponents {
    INPUT_SNAPSHOT,
    UI_CONTEXT,
    AUDIO_FEEDBACK,
    BACKGROUND,
    OUTLINE,
    HOVER_GLOW,
    INTERACTION,
    TOOLTIP
}