Version 0.4.2-beta
- Tooltip is now hidden when scroll input is detected.
- Added a multi-select tool.
- Renamed SortableTable.RowPanel to SortableTable.TableRow

Version 0.4.1-beta
- Changed getEnabled to isEnabled inside UIClickable.
- Fixed issues related to uiScale.
- Added uiScale as a constant.
- Modified DockPanel to have an inner panel.
- Added a GridRenderer

Version v0.4.0-beta
- package changes.
- Added DockButton/DockClickable for internal dockpanel instance handling.
- Separated click handler from Button as UIClickable.
- Added proxy for XStream serialization to ArrayMap.
- Fixed a bug inside RadioPanel.
- Added to UIConstants "hpad" with a value of 5 and "text_color" for the standard text color used by vanilla.
- Added a white center bg to the BorderRenderer, which allows for custom dialog background colors.
- Changed the rotateSprite method to directly rotate the sprite instead of return the degrees.
- Fixed border renderer not working for other all borders.
- Renamed engNotation to engNotate and reverseEngNotation to reverseEngNotate.
- DialogPanel no longer has an inner panel, and is itself the inner panel.
- Added to AudioFeedbackComp a flag for hover only sounds.
- Added to NativeUiUtils isCtrlDown, isShiftDown and isAltDown.
- Removed CustomPanel generics as it was unused.
- Added IconValuePair for standardized icon-value pairs in UI.
- Removed UIContext as it was no longer needed.
- Added swapPosition to NativeUiUtils.
- Added a grid class.
- Added Arithmetic.java to utils for common math utilities.

Version v0.3.2-beta
- Fixed height compensation for tooltips inside the AnchorPanel method of NativeUiUtils.
- Replaced createPanel() with buildUI().
- Optimize some code by making the sprites a private static member.
- Added CheckboxButton Panel.
- Fixed DockPanel side not being applied at the constructor.
- Added some more documentation.
- Added an array map to utilities.
- Added a RadioPanel.
- The slider is now user adjustable by default.
- Added onRemoved runnable to DockPanel.
- Tooltip system hide/show tooltip methods now public.
- Added tests for ArrayMap

Version v0.3.1-beta
- Fixed issues with Dock Panel.
- Fixed alignment issues with SortableTable.
- Each component now has its own offset.
- Interaction shortcuts are now consumed.
- Fixed table column tooltips.
- Added outside click detector.
- Fixed lack of MouseMoveEvent not updating the mouse position.
- Fixed text padding inside DialogPanel.
- Fixed invisible panels getting a tooltip.

Version v0.3.0-beta
- Modified HasTooltip to reduce boilerplate.
- Turned all the interfaces of CustomPanel to components.
- CustomPanel now owns a ComponentContainer member.
- CustomPanel now owns the systems.
- Replaced all plugins for CustomPanel in favor of a forwarder plugin.
- Renamed the packaging from wrap_ui to native_ui.

Version v0.2.1-beta
- Misc. bug fixes.
- Made SpritePanel and SpritePanelWithTp a lot leaner in terms of composition.
- Added createTooltip and addTooltip to ComponentFactory for CustomPanelAPI independent Tooltips.
- Added addCaptionValueBlock to ComponentFactory to have values with titles.
- Removed ParentType from CustomPanel, as it does not need to be a CustomPanelAPI instance anymore.
- Removed CustomPanelAPI dependency fully. Ready to switch to a custom UIPanelAPI Impl. for the future.


Version v0.2.0-beta
- Created a carbon copy of DialogPanel because the API for vanilla version was too restrictive.
- Created new Attachments for UI injection.
- Switched to RolflectionLib.jar to not depend on kotlin.
- Removed old dialog wrappers.
- Misc. bug fixes.

Version v0.1.0-beta
- Initial release of WrapUI.