package wfg.native_ui.ui.table;

import wfg.native_ui.ui.component.InteractionComp;
import wfg.native_ui.ui.core.UIBuildableAPI;

/**
 * <p>{@link #buildUI} may be called at any time by a parent container,
 * implementations must be idempotent and support rebuilding.
 */
public interface WidgetAPI<T> extends UIBuildableAPI {
    /** nullable */
    InteractionComp<T> getInteraction();
}