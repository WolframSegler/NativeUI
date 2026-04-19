package wfg.native_ui.ui.table;

import com.fs.starfarer.api.ui.UIComponentAPI;

import wfg.native_ui.ui.component.InteractionComp;
import wfg.native_ui.ui.core.UIBuildableAPI;

public interface WidgetAPI<T> extends UIBuildableAPI {
    InteractionComp<T> getInteraction();
    UIComponentAPI getElement(); // TODO remove after update
}