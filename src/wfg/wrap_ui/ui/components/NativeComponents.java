package wfg.wrap_ui.ui.components;

/**
 * Enum defining the "core" components that NativeUI provides.
 * Used for O(1) access inside {@link ComponentContainer}.
 * Order of elements matter.
 */
public enum NativeComponents {
    LAYOUT_OFFSET,
    UI_CONTEXT,
    AUDIO_FEEDBACK,
    BACKGROUND,
    OUTLINE,
    HOVER_GLOW,
    INTERACTION,
    TOOLTIP
}