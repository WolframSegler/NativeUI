package wfg.native_ui.ui.widget;

// package com.fs.starfarer.api.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fs.starfarer.api.input.InputEventAPI;

// TODO remove
/**
 * Manages input focus for UI elements.
 * <p>
 * The focus stack is a list of {@link Focusable} elements where the last element is the
 * current focus. Focusable elements receive {@link Focusable#focusLost()} when they lose focus.
 * There is no explicit focus-gained callback; an element is considered focused while it is on
 * top of the stack.
 */
public class FocusManager {
    private static List<Focusable> focusStack = new ArrayList<>();

    /**
     * Set to true when a new focus has been granted via {@link #grantFocus(Focusable)}.
     * Reset to false when focus is released or cleared. External code can use this flag
     * to detect focus transitions.
     */
    public static boolean focusJustChanged = false;

    /**
     * Dispatches input events to the currently focused element, if any.
     *
     * @param inputEvents the list of input events to process
     */
    public static void processInput(InputEventList inputEvents) {
        Focusable current = getCurrentFocus();
        if (current != null) {
            current.processInput(inputEvents);
        }
    }

    /**
     * Returns the current focus stack. The last element is the current focus.
     *
     * @return a mutable list (modifications affect the manager directly)
     */
    public static List<Focusable> getFocusStack() {
        return focusStack;
    }

    /**
     * Replaces the entire focus stack.
     * <p>
     * Warning: the previous elements are <b>not</b> notified of focus loss.
     * Typically you should use {@link #grantFocus(Focusable)} instead.
     *
     * @param stack the new stack
     */
    public static void setFocusStack(List<Focusable> stack) {
        focusStack = stack;
    }

    /**
     * Grants exclusive focus to the given element.
     * <p>
     * All previously focused elements receive {@link Focusable#focusLost()},
     * the stack is cleared, and the new element is pushed.
     * Sets {@link #focusJustChanged} to true.
     *
     * @param element the element to receive focus
     */
    public static void grantFocus(Focusable element) {
        focusJustChanged = true;
        for (Focusable f : focusStack) {
            f.focusLost();
        }
        focusStack.clear();
        focusStack.add(element);
    }

    /**
     * Releases focus if the given element is the current focus owner.
     * <p>
     * If the provided element is not the current focus, a RuntimeException is thrown.
     * The current focus receives {@link Focusable#focusLost()} and is removed from the stack.
     * Sets {@link #focusJustChanged} to false.
     *
     * @param owner the element that currently owns focus
     * @throws RuntimeException if {@code owner} is not the current focus
     */
    public static void releaseFocus(Focusable owner) {
        focusJustChanged = false;
        Focusable current = getCurrentFocus();
        if (current != owner && current != null) {
            throw new RuntimeException("Attempt to release focus by non-owner");
        }
        if (current != null) {
            current.focusLost();
        }
        focusStack.remove(current);
    }

    /**
     * Releases focus only if the given element is the current focus.
     * <p>
     * Unlike {@link #releaseFocus(Focusable)}, this method does not throw an
     * exception when the element is not focused.
     * Sets {@link #focusJustChanged} to false.
     *
     * @param element the element that may be focused
     */
    public static void releaseIfFocused(Focusable element) {
        Focusable current = getCurrentFocus();
        if (current == element) {
            focusJustChanged = false;
            if (current != null) {
                current.focusLost();
            }
            focusStack.remove(current);
        }
    }

    /**
     * Adds an element to the top of the focus stack <b>without</b> notifying the
     * previous top of focus loss.
     * <p>
     * Useful for building an initial stack or for pushing overlays that should not
     * disturb the previous focus state. The new element becomes the current focus.
     *
     * @param element the element to push
     */
    public static void pushFocus(Focusable element) {
        focusStack.add(element);
    }

    /**
     * Removes an element from the focus stack.
     * <p>
     * If the element is non-null, its {@link Focusable#focusLost()} is called.
     * If the removed element was the current focus, or if after removal the stack
     * size would be 1 or empty, {@link #focusJustChanged} is set to false.
     *
     * @param element the element to remove (null is ignored)
     */
    public static void removeFocus(Focusable element) {
        if (element != null) {
            element.focusLost();
        }
        if (element != null && (element == getCurrentFocus() || focusStack.size() <= 1)) {
            focusJustChanged = false;
        }
        focusStack.remove(element);
    }

    /**
     * Clears all focus.
     * <p>
     * Every element in the stack receives {@link Focusable#focusLost()},
     * the stack is emptied, and {@link #focusJustChanged} is set to false.
     */
    public static void clearAllFocus() {
        focusJustChanged = false;
        for (Focusable f : focusStack) {
            f.focusLost();
        }
        focusStack.clear();
    }

    /**
     * Returns the currently focused element, or null if the stack is empty.
     *
     * @return the top element of the stack
     */
    public static Focusable getCurrentFocus() {
        return focusStack.isEmpty() ? null : focusStack.get(focusStack.size() - 1);
    }

    /**
     * A UI component that can receive input focus.
     */
    public interface Focusable {

        /**
         * Called every frame to update animations, timers, etc.
         *
         * @param delta time passed since last frame, in seconds
         */
        void advance(float delta);

        /**
         * Called when this element is the current focus and input events should be processed.
         * The element should consume events it handles to prevent further propagation.
         *
         * @param inputEvents mutable list of input events; consumed events can be removed
         */
        void processInput(InputEventList inputEvents);

        /**
         * Called when this element loses focus, either because another element gained it
         * or the focus was cleared. The element should clean up any input-related state.
         */
        void focusLost();
    }

    /**
     * A list of input events with utility methods for event processing.
     * <p>
     * This class is based on the vanilla {@code B} that extends {@code ArrayList<InputEvent>}
     * and provides {@code removeConsumed()} and {@code consumeKeyboardEvents()}.
     */
    public class InputEventList extends ArrayList<InputEventAPI> {

        /**
         * Removes all events that have been marked as consumed.
         * Typically called after input processing to clean up the list.
         */
        public void removeConsumed() {
            Iterator<InputEventAPI> it = iterator();
            while (it.hasNext()) {
                if (it.next().isConsumed()) {
                    it.remove();
                }
            }
        }

        /**
         * Consumes all keyboard events that are not yet consumed and are not mouse events.
         * This prevents keyboard input from being handled by other systems after the
         * focus manager has processed it.
         */
        public void consumeKeyboardEvents() {
            for (InputEventAPI event : this) {
                if (!event.isConsumed() && !event.isMouseEvent() && event.isKeyboardEvent()) {
                    event.consume();
                }
            }
        }
    }

    public interface FocusManagerAPI {

        void processInput(List<InputEventAPI> inputEvents);

        /** Last element is the current focus. */
        List<Focusable> getFocusStack();

        /** Replaced elements are <b>not</b> notified via {@link Focusable#focusLost()}. */
        void setFocusStack(List<Focusable> stack);

        /** Previously focused elements receive {@link Focusable#focusLost()}. */
        void grantFocus(Focusable element);

        /**
         * @param owner the element that currently owns focus
         * @throws RuntimeException if owner is not the current focus
         */
        void releaseFocus(Focusable owner);

        /** Safe version of {@link #releaseFocus(Focusable)}. */
        void releaseIfFocused(Focusable element);

        /** Pushes an element onto the focus stack without notifying the previous focus. */
        void pushFocus(Focusable element);

        /** The element is notified via {@link Focusable#focusLost()} if non-null. */
        void removeFocus(Focusable element);

        /** Notifies every element in the stack. */
        void clearAllFocus();

        /** @return the top element of the focus stack or null if empty */
        Focusable getCurrentFocus();

        /** Indicates whether a focus grant has just occurred this frame. */
        boolean hasFocusJustChanged();
        void setFocusJustChanged(boolean changed);

        public interface Focusable {

            void advance(float delta);

            /** Called when this element is the current focus. */
            void processInput(InputEventList inputEvents);

            void focusLost();
        }
    }
}