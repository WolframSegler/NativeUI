# WrapUI
A composition-oriented UI framework I created while working with Starsector's UI. Not feature-complete.  
Feel free to create forks or contribute. Bug reports are also appreciated.

# Features
- [`CustomPanel`](src/wfg/wrap_ui/ui/panels/CustomPanel.java) for clean injection into vanilla UI hierarchies
- Extensive use of generics for early error prevention
- Global UI attachment points via [`Attachments`](src/wfg/wrap_ui/ui/Attachments.java)
- Barebones UI context tracking using [`UIContext`](src/wfg/wrap_ui/ui/UIContext.java)
- [Systems](src/wfg/wrap_ui/ui/systems/BaseSystem.java) for tooltips, hoverGlow etc. to reduce boilerplate, with a more composition-oriented approach
- Basic [number formatter](src/wfg/wrap_ui/util/NumFormat.java) for large numbers

# UI Elements
- [`Button`](src/wfg/wrap_ui/ui/panels/Button.java) implementation similar to vanilla, without using `ButtonAPI`
- Simple [`PieChart`](src/wfg/wrap_ui/ui/panels/PieChart.java)
- Carbon copy of the vanilla [`Slider`](src/wfg/wrap_ui/ui/panels/Slider.java) used in settings. The blue one.
- [`SortableTable`](src/wfg/wrap_ui/ui/panels/SortableTable.java) similar to `UITable` in functionality, avoiding the obfuscated vanilla table
- [`SpritePanel`](src/wfg/wrap_ui/ui/panels/SpritePanel.java) with optional tooltip; essentially a `SpriteAPI` wrapper.
- [`ScrollPanel`](src/wfg/wrap_ui/ui/panels/ScrollPanel.java) for vertical and horizontal scroll.
- [`DialogPanel`](src/wfg/wrap_ui/ui/dialogs/DialogPanel.java) is the vanilla Folding Dialog Panel but without the annoying vanilla API.
- [`DockPanel`](src/wfg/wrap_ui/ui/panels/DockPanel.java) docks to the specified side of the screen and can move in and out of the viewport.

# Usage
- All panels that wish to use Systems must extend [`CustomPanel`](src/wfg/wrap_ui/ui/panels/CustomPanel.java).
- To access the actual `CustomPanelAPI` instance, the getPanel() method can be used.

# Possible Questions
- Why is it called Wrap UI?
    - Because [`CustomPanel`](src/wfg/wrap_ui/ui/panels/CustomPanel.java) is a wrapper for `CustomPanelAPI` and is itself not the actual panel.

- How do I see the java documentation for classes?
    - Make sure to include the src file as a depenency. The JAR file does not contain the documentation.