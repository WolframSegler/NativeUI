package wfg.native_ui.ui.core;

// TODO make this extend UIComponentAPI with the new update
public interface UIBuildableAPI {

    /**
     * Called whenever the panel needs to build or rebuild its child elements.
     * Can be called during initial creation, refresh, or after clearing.
     */
    void buildUI();
}