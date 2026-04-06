package wfg.native_ui.ui.component;

import java.util.ArrayList;

/**
 * Base container for components.
 * Acts as the "entity" in an ECS-like pattern.
 */
public class UIComponentContainer {
    private final BaseComponent[] nativeComponents = new BaseComponent[NativeComponents.values().length];
    private final ArrayList<BaseComponent> customComponents = new ArrayList<>(2);

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
        customComponents.add(component);
    }

    @SuppressWarnings("unchecked")
    public final <T extends BaseComponent> T getCustom(Class<T> type) {
        for (BaseComponent comp : customComponents) {
            if (type.isInstance(comp)) return (T) comp;
        }
        return null;
    }

    public final boolean hasCustom(Class<? extends BaseComponent> type) {
        for (BaseComponent comp : customComponents) {
            if (type.isInstance(comp)) return true;
        }
        return false;
    }

    public final void addCustomIfNotPresent(BaseComponent component) {
        if (!hasCustom(component.getClass())) addCustom(component);
    }
}