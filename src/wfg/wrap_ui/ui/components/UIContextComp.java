package wfg.wrap_ui.ui.components;

import wfg.wrap_ui.ui.UIContext;
import wfg.wrap_ui.ui.UIContext.Context;

public final class UIContextComp extends BaseComponent {
    public Context targetContext = Context.NONE;
    public boolean ignoreContext = false;

    public final boolean isValid() {
        return ignoreContext || UIContext.is(targetContext);
    }
}