package wfg.native_ui.internal.ui.core; // originally com.fs.starfarer.ui;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.input.Mouse;

import static wfg.native_ui.util.UIConstants.uiScale; // Global.getSettings().getScreenScaleMult()

/**
 * Manually de-obfuscated to understand the inner-workings.
 */
public class Position implements PositionAPI, Cloneable {
    private float posX;
    private float posY;
    private float posWidth;
    private float posHeight;
    private Position parent;
    private Position basePosition;
    private float widthScaler1;
    private float heightScaler1;
    private float widthScaler2;
    private float heightScaler2;
    private float alignOffsetX;
    private float alignOffsetY;
    private float offsetX;
    private float offsetY;
    private List<Position> children;
    private TransformListener someListener1;
    private boolean isRoundCoordinates;
    private boolean suspendRecompute;
    public static boolean suspendSort = false;
    private boolean withSort;

    public Position(TransformListener var1) {
        children = new ArrayList<>();
        isRoundCoordinates = true;
        suspendRecompute = false;
        withSort = true;
        someListener1 = var1;
    }

    public Position() {
        children = new ArrayList<>();
        isRoundCoordinates = true;
        suspendRecompute = false;
        withSort = true;
    }

    public Position(float var1, float var2) {
        children = new ArrayList<>();
        isRoundCoordinates = true;
        suspendRecompute = false;
        withSort = true;
        posWidth = var1;
        posHeight = var2;
        if (someListener1 != null) {
            someListener1.sizeChanged(var1, var2);
        }

    }

    public Position(float var1, float var2, float var3, float var4) {
        this(var1, var2, var3, var4, null);
    }

    public Position(float var1, float var2, float var3, float var4, Position var5) {
        children = new ArrayList<>();
        isRoundCoordinates = true;
        suspendRecompute = false;
        withSort = true;
        alignOffsetX = var1;
        alignOffsetY = var2;
        posWidth = var3;
        posHeight = var4;
        parent = var5;
        if (someListener1 != null) {
            someListener1.sizeChanged(var3, var4);
        }

        recompute();
    }

    public void setRoundCoordinates(boolean var1) {
        isRoundCoordinates = var1;
        Iterator<Position> var3 = children.iterator();

        while (var3.hasNext()) {
            Position var2 = (Position) var3.next();
            var2.setRoundCoordinates(var1);
        }

    }

    public boolean isSuspendRecompute() {
        return suspendRecompute;
    }

    public void setSuspendRecompute(boolean var1) {
        suspendRecompute = var1;
    }

    public void recompute() {
        if (!suspendRecompute) {
            Position var1 = basePosition;
            if (var1 == null) {
                var1 = parent;
            }

            if (var1 != null) {
                float var2 = posX;
                float var3 = posY;
                posX = var1.posX + widthScaler1 * var1.posWidth + posWidth * widthScaler2
                        + alignOffsetX + offsetX;
                posY = var1.posY + heightScaler1 * var1.posHeight + posHeight * heightScaler2
                        + alignOffsetY + offsetY;
                if (isRoundCoordinates) {
                    posX = (float) Math.round(posX);
                    posY = (float) Math.round(posY);
                }

                if (someListener1 != null && (var2 != posX || var3 != posY)) {
                    someListener1.locationChanged();
                }
            }

            if (children.contains(this)) {
                throw new RuntimeException("Children contains this");
            } else {
                someMethod1();
                Iterator<Position> var5 = children.iterator();

                while (var5.hasNext()) {
                    Position var4 = (Position) var5.next();
                    var4.recompute();
                }

            }
        }
    }

    public void setWithSortRecurvsive(boolean var1) {
        setWithSortHelper(this, var1);
    }

    public void setWithSortHelper(Position var1, boolean var2) {
        var1.setWithSort(var2);
        Iterator<Position> var4 = var1.children.iterator();

        while (var4.hasNext()) {
            Position var3 = (Position) var4.next();
            setWithSortHelper(var3, var2);
        }

    }

    public boolean isWithSort() {
        return withSort;
    }

    public void setWithSort(boolean var1) {
        withSort = var1;
    }

    private void someMethod1() {
        if (!suspendRecompute) {
            if (withSort && !suspendSort) {
                LinkedList<Position> var1 = new LinkedList<>();
                int var2 = 0;

                label47: do {
                    if (var1.size() >= children.size()) {
                        children.clear();
                        children.addAll(var1);
                        return;
                    }

                    Iterator<Position> var4 = children.iterator();

                    while (true) {
                        Position var3;
                        Position var5;
                        do {
                            do {
                                if (!var4.hasNext()) {
                                    ++var2;
                                    continue label47;
                                }

                                var3 = (Position) var4.next();
                            } while (var1.contains(var3));

                            var5 = var3.basePosition;
                            if (var5 != null && !children.contains(var5)) {
                                throw new RuntimeException("May only anchor on siblings");
                            }
                        } while (var5 != null && !var1.contains(var5));

                        var1.add(var3);
                    }
                } while (var2 <= children.size());

                throw new RuntimeException("Circular dependency of sibling positions detected");
            }
        }
    }

    public float getX() {
        return posX;
    }

    public float getY() {
        return posY;
    }

    public float getWidth() {
        return posWidth;
    }

    public float getHeight() {
        return posHeight;
    }

    public Position setSize(float var1, float var2) {
        float var3 = posWidth;
        float var4 = posHeight;
        posWidth = var1;
        posHeight = var2;
        recompute();
        if (someListener1 != null && (var3 != var1 || var4 != var2)) {
            someListener1.sizeChanged(var1, var2);
        }

        return this;
    }

    public Position setLocation(float var1, float var2) {
        posX = var1;
        posY = var2;
        recompute();
        return this;
    }

    public Position setOffset(float var1, float var2) {
        offsetX = var1;
        offsetY = var2;
        boolean var3 = withSort;
        setWithSortRecurvsive(false);
        recompute();
        setWithSortRecurvsive(var3);
        return this;
    }

    public Position getBase() {
        return basePosition;
    }

    public void add(int var1, Position var2) {
        if (!children.contains(var2)) {
            children.add(var1, var2);
            var2.recompute();
        }

    }

    public void add(Position var1) {
        if (!children.contains(var1)) {
            children.add(var1);
            var1.recompute();
        }

    }

    public void remove(Position var1) {
        children.remove(var1);
    }

    public void clearChildren() {
        children.clear();
    }

    public Position inTL(float var1, float var2) {
        return relativeTo((Position) null, 0f, 1f, 0f, -1f, var1, -var2);
    }

    public Position inTMid(float var1) {
        return relativeTo((Position) null, 0.5f, 1f, -0.5f, -1f, 0f, -var1);
    }

    public Position inTR(float var1, float var2) {
        return relativeTo((Position) null, 1f, 1f, -1f, -1f, -var1, -var2);
    }

    public Position inRMid(float var1) {
        return relativeTo((Position) null, 1f, 0.5f, -1f, -0.5f, -var1, 0f);
    }

    public Position inMid() {
        return relativeTo((Position) null, 0.5f, 0.5f, -0.5f, -0.5f, 0f, 0f);
    }

    public Position inBR(float var1, float var2) {
        return relativeTo((Position) null, 1f, 0f, -1f, 0f, -var1, var2);
    }

    public Position inBMid(float var1) {
        return relativeTo((Position) null, 0.5f, 0f, -0.5f, 0f, 0f, var1);
    }

    public Position inBL(float var1, float var2) {
        return relativeTo((Position) null, 0f, 0f, 0f, 0f, var1, var2);
    }

    public Position inLMid(float var1) {
        return relativeTo((Position) null, 0f, 0.5f, 0f, -0.5f, var1, 0f);
    }

    public Position leftOfTop(UIComponentAPI var1, float var2) {
        return relativeTo(var1, 0f, 1f, -1f, -1f, -var2, 0f);
    }

    public Position leftOfMid(UIComponentAPI var1, float var2) {
        return relativeTo(var1, 0f, 0.5f, -1f, -0.5f, -var2, 0f);
    }

    public Position leftOfBottom(UIComponentAPI var1, float var2) {
        return relativeTo(var1, 0f, 0f, -1f, 0f, -var2, 0f);
    }

    public Position rightOfTop(UIComponentAPI var1, float var2) {
        return relativeTo(var1, 1f, 1f, 0f, -1f, var2, 0f);
    }

    public Position rightOfMid(UIComponentAPI var1, float var2) {
        return relativeTo(var1, 1f, 0.5f, 0f, -0.5f, var2, 0f);
    }

    public Position rightOfBottom(UIComponentAPI var1, float var2) {
        return relativeTo(var1, 1f, 0f, 0f, 0f, var2, 0f);
    }

    public Position aboveLeft(UIComponentAPI var1, float var2) {
        return relativeTo(var1, 0f, 1f, 0f, 0f, 0f, var2);
    }

    public Position aboveMid(UIComponentAPI var1, float var2) {
        return relativeTo(var1, 0.5f, 1f, -0.5f, 0f, 0f, var2);
    }

    public Position aboveRight(UIComponentAPI var1, float var2) {
        return relativeTo(var1, 1f, 1f, -1f, 0f, 0f, var2);
    }

    public Position belowLeft(UIComponentAPI var1, float var2) {
        return relativeTo(var1, 0f, 0f, 0f, -1f, 0f, -var2);
    }

    public Position belowMid(UIComponentAPI var1, float var2) {
        return relativeTo(var1, 0.5f, 0f, -0.5f, -1f, 0f, -var2);
    }

    public Position belowRight(UIComponentAPI var1, float var2) {
        return relativeTo(var1, 1f, 0f, -1f, -1f, 0f, -var2);
    }

    public Position relativeTo(UIComponentAPI var1, float var2, float var3, float var4, float var5, float var6,
            float var7) {
        return relativeTo((Position) var1.getPosition(), var2, var3, var4, var5, var6, var7);
    }

    public Position relativeTo(Position var1, float var2, float var3, float var4, float var5, float var6, float var7) {
        basePosition = var1;
        widthScaler1 = var2;
        heightScaler1 = var3;
        widthScaler2 = var4;
        heightScaler2 = var5;
        alignOffsetX = var6;
        alignOffsetY = var7;
        recompute();
        return this;
    }

    public Position setXAlignOffset(float var1) {
        alignOffsetX = var1;
        recompute();
        return this;
    }

    public float getXAlignOffset() {
        return alignOffsetX;
    }

    public float getYAlignOffset() {
        return alignOffsetY;
    }

    public Position setYAlignOffset(float var1) {
        alignOffsetY = var1;
        recompute();
        return this;
    }

    public Position setAlignOffset(float var1, float var2) {
        alignOffsetX = var1;
        alignOffsetY = var2;
        recompute();
        return this;
    }

    public Position getParent() {
        return parent;
    }

    public Position setParent(Position var1) {
        parent = var1;
        recompute();
        return this;
    }

    public Position parent(UIComponentAPI var1) {
        return setParent((Position) var1.getPosition());
    }

    public float getCenterX() {
        return posX + posWidth / 2f;
    }

    public float getCenterY() {
        return posY + posHeight / 2f;
    }

    public static void main(String[] var0) {
    }

    public Position clone() {
        try {
            Position var1 = (Position) super.clone();
            return var1;
        } catch (CloneNotSupportedException var2) {
            return null;
        }
    }

    public String toString() {
        return String.format("[%f,%f: %fx%f]", getX(), getY(), getWidth(), getHeight());
    }

    public Position setAlignThis(float var1, float var2) {
        widthScaler2 = var1;
        heightScaler2 = var2;
        recompute();
        return this;
    }

    public void set(Position var1) {
        if (var1 != null) {
            if (posX != var1.posX || posY != var1.posY || posWidth != var1.posWidth
                    || posHeight != var1.posHeight || widthScaler1 != var1.widthScaler1
                    || heightScaler1 != var1.heightScaler1 || widthScaler2 != var1.widthScaler2
                    || heightScaler2 != var1.heightScaler2 || alignOffsetX != var1.alignOffsetX
                    || alignOffsetY != var1.alignOffsetY || offsetX != var1.offsetX
                    || offsetY != var1.offsetY || basePosition != var1.basePosition
                    || parent != var1.parent) {
                posX = var1.posX;
                posY = var1.posY;
                float var2 = posWidth;
                float var3 = posHeight;
                posWidth = var1.posWidth;
                posHeight = var1.posHeight;
                if (someListener1 != null && (var2 != posWidth || var3 != posHeight)) {
                    someListener1.sizeChanged(posWidth, posHeight);
                }

                widthScaler1 = var1.widthScaler1;
                heightScaler1 = var1.heightScaler1;
                widthScaler2 = var1.widthScaler2;
                heightScaler2 = var1.heightScaler2;
                alignOffsetX = var1.alignOffsetX;
                alignOffsetY = var1.alignOffsetY;
                offsetX = var1.offsetX;
                offsetY = var1.offsetY;
                basePosition = var1.basePosition;
                if (var1.parent != null) {
                    parent = var1.parent;
                }

                recompute();
            }
        }
    }

    public boolean intersects(Position var1) {
        if (posX + posWidth < var1.posX) {
            return false;
        } else if (posX > var1.posX + var1.posWidth) {
            return false;
        } else if (posY + posHeight < var1.posY) {
            return false;
        } else {
            return !(posY > var1.posY + var1.posHeight);
        }
    }

    public void overlap(Position var1) {
        float var2 = Math.min(posX + posWidth, var1.posX + var1.posWidth);
        float var3 = Math.min(posY + posHeight, var1.posY + var1.posHeight);
        posX = Math.max(posX, var1.posX);
        posY = Math.max(posY, var1.posY);
        posWidth = Math.max(0f, var2 - posX);
        posHeight = Math.max(0f, var3 - posY);
    }

    // public boolean containsEvent(InputEventAPI var1) {
    // return containsEvent((InputEvent) var1);
    // }

    /**
     * Originally the parameter var1 was the InputEventAPI implementation. I removed
     * it because the API has all the methods used by this function.
     */
    public boolean containsEvent(InputEventAPI var1) {
        int var2 = var1.getX();
        int var3 = var1.getY();
        return containsLocation(var2, var3, 0f, 0f, 0f, 0f);
    }

    public boolean containsMouse() {
        return containsLocation(Mouse.getX() / uiScale, Mouse.getY() / uiScale, 0f, 0f, 0f, 0f);
    }

    public boolean containsMouse(float var1, float var2, float var3, float var4) {
        return containsLocation(Mouse.getX() / uiScale, Mouse.getY() / uiScale, var1, var2, var3, var4);
    }

    public boolean containsLocation(float var1, float var2, float var3, float var4, float var5, float var6) {
        if (var1 < getX() - var3) {
            return false;
        } else if (var1 >= getX() + getWidth() + var4) {
            return false;
        } else if (var2 < getY() - var6) {
            return false;
        } else {
            return !(var2 >= getY() + getHeight() + var5);
        }
    }

    public static Position atCenter(float var0, float var1, float var2, float var3, Position var4) {
        Position var5 = new Position();
        var5.setParent(var4);
        var5.setSize(var2, var3);
        var5.setAlignOffset(var0 + -var2 / 2f, var1 - var3 / 2f);
        return var5;
    }

    public float getXOffset() {
        return offsetX;
    }

    public float getYOffset() {
        return offsetY;
    }

    public static interface TransformListener {
        public void locationChanged();

        public void sizeChanged(float var1, float var2);
    }
}