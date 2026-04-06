package wfg.native_ui.ui.system;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import wfg.native_ui.ui.panel.CustomPanel;

/**
 * Base container for systems.
 * Stores native singleton systems and custom systems for a UI element.
 * Handles forwarding calls like advance, render, and processInput.
 */
public class UISystemContainer {
    private final BaseSystem[] nativeSystems = new BaseSystem[NativeSystems.values().length];
    private final List<BaseSystem> customSystems = new ArrayList<>(2);
    private List<BaseSystem> cached = null;

    public final void set(NativeSystems type, BaseSystem system, CustomPanel<?> element) {
        nativeSystems[type.ordinal()] = system;
        system.init(element);
        invalidateCache();
    }

    @SuppressWarnings("unchecked")
    public final <T extends BaseSystem> T get(NativeSystems type) {
        return (T) nativeSystems[type.ordinal()];
    }

    public final boolean has(NativeSystems type) {
        return nativeSystems[type.ordinal()] != null;
    }

    public final void setIfNotPresent(NativeSystems type, BaseSystem system, CustomPanel<?> element) {
        if (!has(type)) set(type, system, element);
    }


    public final void addCustom(BaseSystem system, CustomPanel<?> element) {
        customSystems.add(system);
        system.init(element);
        invalidateCache();
    }

    @SuppressWarnings("unchecked")
    public final <T extends BaseSystem> T getCustom(Class<T> type) {
        for (BaseSystem sys : customSystems) {
            if (type.isInstance(sys)) return (T) sys;
        }
        return null;
    }

    public final boolean hasCustom(Class<? extends BaseSystem> type) {
        for (BaseSystem sys : customSystems) {
            if (type.isInstance(sys)) return true;
        }
        return false;
    }

    public final void addCustomIfNotPresent(BaseSystem system, CustomPanel<?> element) {
        if (!hasCustom(system.getClass())) addCustom(system, element);
    }


    public final void invalidateCache() { cached = null;}
    public final List<BaseSystem> getCustomSystems() { return Collections.unmodifiableList(cached);}
    public final List<BaseSystem> getAll() {
        if (cached == null) {
            cached = new ArrayList<>();
            for (BaseSystem sys : nativeSystems) if (sys != null) cached.add(sys);
            cached.addAll(customSystems);
        }
        return cached;
    }
}