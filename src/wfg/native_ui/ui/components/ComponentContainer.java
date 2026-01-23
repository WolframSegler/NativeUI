package wfg.native_ui.ui.components;

import java.util.HashMap;
import java.util.Map;

/**
 * Base container for components.
 * Acts as the "entity" in an ECS-like pattern.
 */
public class ComponentContainer {
    private final BaseComponent[] nativeComponents = new BaseComponent[NativeComponents.values().length];
    private final Map<Class<?>, BaseComponent> customComponents = new HashMap<>();

    public final void set(NativeComponents type, BaseComponent component) {
        nativeComponents[type.ordinal()] = component;
    }

    @SuppressWarnings("unchecked")
    public final <T extends BaseComponent> T get(NativeComponents type) {
        return (T) nativeComponents[type.ordinal()];
    }

    public final boolean has(NativeComponents type) {
        return nativeComponents[type.ordinal()] != null;
    }

    public final void setIfNotPresent(NativeComponents type, BaseComponent component) {
        if (!has(type)) set(type, component);
    }


    public final void addCustom(BaseComponent component) {
        customComponents.put(component.getClass(), component);
    }

    @SuppressWarnings("unchecked")
    public final <T extends BaseComponent> T getCustom(Class<T> type) {
        return (T) customComponents.get(type);
    }

    public final boolean hasCustom(Class<? extends BaseComponent> type) {
        return customComponents.containsKey(type);
    }

    public final void addCustomIfNotPresent(BaseComponent component) {
        if (!hasCustom(component.getClass())) addCustom(component);
    }
}