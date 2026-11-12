package wfg.native_ui.ui;

import static wfg.native_ui.util.Globals.settings;

import com.fs.graphics.util.Fader;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.ui.UIComponentAPIWrapper;

import rolflectionlib.ui.UiUtil;
import rolflectionlib.util.RolfLectionUtil;

/**
 * Method fields for obfuscated vanilla UI classes.
 */
public final class MethodFields {
    private MethodFields() {};
    private static final Object getFaderMethod;
    public static final Class<?> uiInternalCompClass = findInternalComponentInterface(UiUtil.uiComponentClass);
    private static final Object uiCompPosField;
    private static final Object wrapperWrappedField = RolfLectionUtil.getFieldByName("wrapped", UIComponentAPIWrapper.class);

    static {
        final UIPanelAPI panelIns = settings.createCustom(0f, 0f, null);
        final Class<?> panelClazz = panelIns.getClass();

        uiCompPosField = findPosField(panelClazz);

        getFaderMethod = RolfLectionUtil.getMethod("getFader", uiInternalCompClass);
    }

    public static final Fader getPanelFader(UIComponentAPI panel) {
        return (Fader) RolfLectionUtil.invokeMethodDirectly(getFaderMethod, panel);
    }

    public static final void setPos(UIPanelAPI panel, PositionAPI pos) {
        RolfLectionUtil.setPrivateVariable(uiCompPosField, panel, pos);
    }

    public static final UIComponentAPI unwrapOrSelf(UIComponentAPI comp) {
        if (comp == null) return null;
        if (!(comp instanceof UIComponentAPIWrapper)) return comp;
        return (UIComponentAPI) RolfLectionUtil.getPrivateVariable(wrapperWrappedField, comp);
    }

    private static final Class<?> findInternalComponentInterface(Class<?> baseClass) {
        for (Class<?> iface : baseClass.getInterfaces()) {
            final Object setParentMethod = RolfLectionUtil.getMethodDeclared("setParent", iface, 1);
            if (setParentMethod == null) continue;
            if (RolfLectionUtil.getReturnType(setParentMethod) == void.class) return iface;
        }
        throw new IllegalStateException("InternalUIComponentInterface not found on " + baseClass.getName());
    }

    private static final Object findPosField(Class<?> cls) {
        final UIPanelAPI probe = settings.createCustom(0f, 0f, null);
        final PositionAPI target = probe.getPosition();
        for (Object field : RolfLectionUtil.getAllFields(cls)) {
            try {
                final int mods = RolfLectionUtil.getFieldModifiers(field);
                if (RolfLectionUtil.isStatic(mods)) continue;
                if (RolfLectionUtil.getPrivateVariable(field, probe) == target) {
                    return field;
                }
            } catch (Throwable ignored) {}
        }
        throw new IllegalStateException("Position field not found on " + cls.getName());
    }
}