package wfg.native_ui.ui.core;

import com.fs.starfarer.api.ui.UIComponentAPI;

public interface UIBuildableAPI extends UIComponentAPI {

    /**
     * Called when the {@link UIComponentAPI} needs to build or rebuild its child elements.
     * Can be called during initial creation, refresh, or after clearing.
     */
    void buildUI();
}