# WrapUI
Composition-oriented UI framework with use of panels instead of custom plugins.

# Features
- Custom UIComponentAPI and UIPanelAPI implementations. 
- Global UI attachment points via [`Attachments`](src/wfg/native_ui/ui/Attachments.java).
- Barebones UI context tracking using [`UIContext`](src/wfg/native_ui/ui/UIContext.java).
- [`Systems`](src/wfg/native_ui/ui/system/BaseSystem.java) for tooltips, hoverGlow etc. to reduce boilerplate, with a more composition-oriented approach.

# UI Elements
- [`UIClickable`](src/wfg/native_ui/ui/functional/UIClickable.java) has click handling and click sounds. Can take in a callback.
- [`Button`](src/wfg/native_ui/ui/functional/Button.java) implementation similar to vanilla, without using `ButtonAPI`.
- [`CheckboxButton`](src/wfg/native_ui/ui/functional/CheckboxButton.java) subclassing Button and using the checkbox sprite.
- [`RadioPanel`](src/wfg/native_ui/ui/widget/RadioPanel.java) for having a list of mutually exclusive options to choose from.
- Simple [`PieChart`](src/wfg/native_ui/ui/visual/PieChart.java).
- Carbon copy of the vanilla [`Slider`](src/wfg/native_ui/ui/widget/Slider.java) used in settings (the blue one).
- [`SortableTable`](src/wfg/native_ui/ui/table/SortableTable.java) similar to `UITable` in functionality, avoiding the obfuscated vanilla table.
- [`SpritePanel`](src/wfg/native_ui/ui/visual/SpritePanel.java) with optional tooltip; essentially a `SpriteAPI` wrapper.
- [`ScrollPanel`](src/wfg/native_ui/ui/container/ScrollPanel.java) for vertical and horizontal scroll.
- [`DialogPanel`](src/wfg/native_ui/ui/dialog/DialogPanel.java) is the vanilla Folding Dialog Panel but without the annoying vanilla API.
- [`DockPanel`](src/wfg/native_ui/ui/container/DockPanel.java) docks to the specified side of the screen and can move in and out of the viewport.
- [`DockClickable`](src/wfg/native_ui/ui/functional/DockClickable.java) and [`DockButton`](src/wfg/native_ui/ui/functional/DockButton.java) manage the lifecycle of a [`DockPanel`](src/wfg/native_ui/ui/container/DockPanel.java).
- [`IconValuePair`](src/wfg/native_ui/ui/visual/IconValuePair.java) standardizes the formatting of icon-value pairs. Also includes a variant with tooltip support.

# Utils
- [`CallbackRunnable`](src/wfg/native_ui/util/CallbackRunnable.java) enables access to the panel that called the runnable. Used extensively.
- [`NativeUiUtils`](src/wfg/native_ui/util/NativeUiUtils.java) contains miscellaneous helper methods used throughout the framework.
- [`NumFormat`](src/wfg/native_ui/util/NumFormat.java) formats very large or very small numbers.
- [`RenderUtils`](src/wfg/native_ui/util/RenderUtils.java) provides helper methods for common OpenGL boilerplate.
- [`UIConstants`](src/wfg/native_ui/util/UIConstants.java) houses commonly used values and colors to improve readability. Intended for static import.
- [`ArrayMap`](src/wfg/native_ui/util/ArrayMap.java) is a lightweight ordered map backed by arrays, intended for cases where a HashMap would be unnecessary.

# Usage
- All panels that wish to use Systems must extend [`CustomPanel`](src/wfg/native_ui/ui/panel/CustomPanel.java).
- To access the actual `CustomPanelAPI` instance, the getPanel() method can be used.

# Possible Questions
- Why is it called Wrap UI?
    - Because [`CustomPanel`](src/wfg/native_ui/ui/panel/CustomPanel.java) is a wrapper for `CustomPanelAPI` and is itself not the actual panel.

- How do I see the java documentation for classes?
    - Make sure to include the src file as a dependency. The JAR file does not contain the documentation.