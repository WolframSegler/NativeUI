package wfg.native_ui.ui;

import static wfg.native_ui.util.Globals.settings;

import java.util.List;

import com.fs.graphics.util.Fader;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import rolflectionlib.ui.UiUtil;
import rolflectionlib.util.RolfLectionUtil;

/**
 * Method fields for obfuscated vanilla UI classes.
 */
public final class MethodFields {
    private MethodFields() {};
    private static final Object clearChildrenMethod;
    private static final Object getChildrenCopyMethod;
    private static final Object getChildrenNonCopyMethod;
    private static final Object getFaderMethod;
    private static final Object addToPositionMethod;
    private static final Object removeFromPositionMethod;
    private static final Object positionSetParentMethod;
    private static final Object isSlidOutMethod;
    private static final Object setCompParentMethod;
    private static final Object getCompParentMethod;
    private static final Object getPosParentMethod;
    public static final Class<?> uiInternalCompClass = findInternalComponentInterface(UiUtil.uiComponentClass);
    private static final Object uiCompPosField;

    private static final Object positionConstructor;

    static {
        final UIPanelAPI panelIns = settings.createCustom(0f, 0f, null);
        final Class<?> panelClazz = panelIns.getClass();
        final Class<?> posClazz = panelIns.getPosition().getClass();

        uiCompPosField = findPosField(panelClazz);

        clearChildrenMethod = RolfLectionUtil.getMethod(
            "clearChildren", panelClazz);
        getChildrenCopyMethod = RolfLectionUtil.getMethod(
            "getChildrenCopy", panelClazz);
        getChildrenNonCopyMethod = RolfLectionUtil.getMethod(
            "getChildrenNonCopy", panelClazz);
        getFaderMethod = RolfLectionUtil.getMethod("getFader", uiInternalCompClass);
        isSlidOutMethod = RolfLectionUtil.getMethod("isSlidOut", panelClazz);
        addToPositionMethod = RolfLectionUtil.getMethod("add", posClazz, 1);
        removeFromPositionMethod = RolfLectionUtil.getMethod("remove", posClazz, 1);
        positionSetParentMethod = RolfLectionUtil.getMethod("setParent", posClazz, 1);
        setCompParentMethod = RolfLectionUtil.getMethod("setParent", uiInternalCompClass, 1);
        getCompParentMethod = RolfLectionUtil.getMethod("getParent", uiInternalCompClass);
        getPosParentMethod = RolfLectionUtil.getMethod("getParent", posClazz);

        positionConstructor = RolfLectionUtil.getConstructor(posClazz, new Class<?>[] {});
    }

    @SuppressWarnings("unchecked")
    public static final List<UIComponentAPI> clearChildren(UIPanelAPI panel) {
        return (List<UIComponentAPI>) RolfLectionUtil.invokeMethodDirectly(clearChildrenMethod, panel);
    }

    @SuppressWarnings("unchecked")
    public static final List<UIComponentAPI> getChildrenNonCopy(UIPanelAPI panel) {
        return (List<UIComponentAPI>) RolfLectionUtil.invokeMethodDirectly(getChildrenNonCopyMethod, panel);
    }

    @SuppressWarnings("unchecked")
    public static final List<UIComponentAPI> getChildrenCopy(UIPanelAPI panel) {
        return (List<UIComponentAPI>) RolfLectionUtil.invokeMethodDirectly(getChildrenCopyMethod, panel);
    }

    public static final void recomputePos(final PositionAPI position) {
        position.setSize(position.getWidth(), position.getHeight());
    }

    public static final Fader getPanelFader(UIComponentAPI panel) {
        return (Fader) RolfLectionUtil.invokeMethodDirectly(getFaderMethod, panel);
    }

    public static final boolean isSlidOut(UIComponentAPI panel) {
        return (boolean) RolfLectionUtil.invokeMethodDirectly(isSlidOutMethod, panel);
    }

    public static final void setPositionParent(PositionAPI pos, PositionAPI parent) {
        RolfLectionUtil.invokeMethodDirectly(positionSetParentMethod, pos, parent);
    }

    public static final PositionAPI addPositionOnly(UIComponentAPI comp, PositionAPI parentPos) {
        final PositionAPI position = comp.getPosition();
        setPositionParent(position, parentPos);
        RolfLectionUtil.invokeMethodDirectly(addToPositionMethod, parentPos, position);
        return position;
    }

    public static final PositionAPI removePositionOnly(UIComponentAPI comp, PositionAPI parentPos) {
        final PositionAPI position = comp.getPosition();
        setPositionParent(position, null);
        RolfLectionUtil.invokeMethodDirectly(removeFromPositionMethod, parentPos, position);
        return position;
    }

    public static final void setParentOnly(UIComponentAPI comp, UIPanelAPI parent) {
        RolfLectionUtil.invokeMethodDirectly(setCompParentMethod, comp, parent);
    }

    public static final UIPanelAPI getParent(UIComponentAPI comp) {
        return (UIPanelAPI) RolfLectionUtil.invokeMethodDirectly(getCompParentMethod, comp);
    }

    public static final void setPos(UIPanelAPI panel, PositionAPI pos) {
        RolfLectionUtil.setPrivateVariable(uiCompPosField, panel, pos);
    }

    public static final PositionAPI getPosParent(PositionAPI pos) {
        return (PositionAPI) RolfLectionUtil.invokeMethodDirectly(getPosParentMethod, pos);
    }

    public static final PositionAPI createPosition() {
        return (PositionAPI) RolfLectionUtil.instantiateClass(positionConstructor);
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