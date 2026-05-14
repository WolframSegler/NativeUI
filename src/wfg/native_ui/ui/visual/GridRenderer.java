package wfg.native_ui.ui.visual;

import static wfg.native_ui.util.Globals.settings;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import com.fs.graphics.util.GLListManager;
import com.fs.starfarer.api.graphics.SpriteAPI;

import wfg.native_ui.util.NativeUiUtils;
import wfg.native_ui.util.RenderUtils;

public class GridRenderer {
    private Color gridColor = settings.getColor("standardGridColor");
    private float columnCount;
    private float rowCount;
    private float cellWidth;
    private float cellHeight;

    private SpriteAPI solidLineSprite;
    private SpriteAPI dashLineSprite;
    private GLListManager.GLListToken listToken = null;

    public GridRenderer(float columnCount, float rowCount, float cellWidth, float cellHeight) {
        this.columnCount = columnCount;
        this.rowCount = rowCount;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;

        solidLineSprite = settings.getSprite("graphics/hud/line4x4.png");
        dashLineSprite  = settings.getSprite("graphics/fx/beamfringe.png");
    }

    public GridRenderer(float cellWidth, float cellHeight) {
        this(3f, 3f, cellWidth, cellHeight);
    }

    public final float getCols() { return columnCount; }
    public final void setCols(float cols) {
        if (columnCount != cols) GLListManager.invalidateList(listToken);
        columnCount = cols;
    }

    public final float getRows() { return rowCount; }
    public final void setRows(float rows) {
        if (rowCount != rows) GLListManager.invalidateList(listToken);
        rowCount = rows;
    }

    public final float getCellWidth() { return cellWidth; }
    public final void setCellWidth(float w) {
        if (cellWidth != w) GLListManager.invalidateList(listToken);
        cellWidth = w;
    }

    public final float getCellHeight() { return cellHeight; }
    public final void setCellHeight(float h) {
        if (cellHeight != h) GLListManager.invalidateList(listToken);
        cellHeight = h;
    }

    public final Color getGridColor() { return gridColor; }
    public final void setGridColor(Color c) { gridColor = c; }

    public void render(float x, float y, float width, float height, float alphaMult) {
        GLListManager.invalidateList(listToken);
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0f);

        float localX = 0f;
        float localY = 0f;

        final Color color = NativeUiUtils.setAlpha(gridColor, alphaMult);

        if (!GLListManager.callList(listToken)) {
            listToken = GLListManager.beginList();

            final float solidThickness = 1f;
            final float dashThickness = 12f;
            float counter = 0f;

            while (counter <= rowCount) {
                final float x1 = localX;
                final float x2 = localX + columnCount * cellWidth + 1f;
                final float yPos = localY + height - counter * cellHeight;
                RenderUtils.drawGradientSprite(
                    solidLineSprite, x1, yPos, x2, yPos, solidThickness, color,
                    false, 1f, 1f, 1f
                );
                counter += 1f;
            }

            counter = 0f;
            while (counter <= columnCount) {
                final float xPos = localX + counter * cellWidth;
                final float y1 = localY + height - rowCount * cellHeight;
                final float y2 = localY + height;
                RenderUtils.drawGradientSprite(
                    solidLineSprite, xPos, y1, xPos, y2, solidThickness, color,
                    false, 1f, 1f, 1f
                );
                counter += 1f;
            }

            counter = 0f;
            while (counter <= rowCount) {
                final float x1 = localX;
                final float x2 = localX + columnCount * cellWidth + 1f;
                final float yPos = localY + height - counter * cellHeight;
                RenderUtils.drawGradientSprite(
                    dashLineSprite, x1, yPos, x2, yPos, dashThickness, color,
                    true, 0.5f, 0.5f, 0.5f
                );
                counter += 1f;
            }

            counter = 0f;
            while (counter <= columnCount) {
                final float xPos = localX + counter * cellWidth;
                final float y1 = localY + height - rowCount * cellHeight;
                final float y2 = localY + height;
                RenderUtils.drawGradientSprite(
                    dashLineSprite, xPos, y1, xPos, y2, dashThickness, color,
                    true, 0.5f, 0.5f, 0.5f
                );
                counter += 1f;
            }

            GLListManager.endList();
        }

        GL11.glPopMatrix();
    }
}