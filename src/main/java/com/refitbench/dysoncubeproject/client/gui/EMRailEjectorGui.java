package com.refitbench.dysoncubeproject.client.gui;

import com.refitbench.dysoncubeproject.Config;
import com.refitbench.dysoncubeproject.block.tile.EMRailEjectorTileEntity;
import com.refitbench.dysoncubeproject.network.ClientSubscribeSphereMessage;
import com.refitbench.dysoncubeproject.network.DCPNetworkHandler;
import com.refitbench.dysoncubeproject.util.NumberUtils;
import com.refitbench.dysoncubeproject.world.ClientDysonSphere;
import com.refitbench.dysoncubeproject.world.DysonSphereStructure;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.text.DecimalFormat;

public class EMRailEjectorGui extends GuiContainer {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("dysoncubeproject", "textures/gui/dysongui.png");

    private static final int CYAN = 0xFF00FFFF;
    private static final int BG_LIGHT = 0xFFC6C6C6;
    private static final int BORDER_DARK = 0xFF373737;
    private static final int BORDER_LIGHT = 0xFFFFFFFF;

    // GUI element positions
    private static final int ENERGY_X = 26, ENERGY_Y = 24;
    private static final int PROGRESS_X = 45, PROGRESS_Y = 24;
    private static final int INFO_X = 62, INFO_Y = 24;
    private static final int SUB_X = 9, SUB_Y = 78;
    private static final int INPUT_X = 4, INPUT_Y = 37;

    // Titanium texture UV coordinates
    private static final int ENERGY_BG_U = 177, ENERGY_BG_V = 94, ENERGY_BG_W = 18, ENERGY_BG_H = 56;
    private static final int ENERGY_FILL_U = 196, ENERGY_FILL_V = 97, ENERGY_FILL_W = 12, ENERGY_FILL_H = 50;
    private static final int PROGRESS_BORDER_U = 211, PROGRESS_BORDER_V = 1, PROGRESS_BORDER_W = 11, PROGRESS_BORDER_H = 56;
    private static final int PROGRESS_BG_U = 229, PROGRESS_BG_V = 1, PROGRESS_BG_W = 5, PROGRESS_BG_H = 50;
    private static final int PROGRESS_FILL_U = 223, PROGRESS_FILL_V = 1, PROGRESS_FILL_W = 5, PROGRESS_FILL_H = 50;
    private static final int BTN_PULL_U = 196, BTN_PULL_V = 31, BTN_PULL_W = 14, BTN_PULL_H = 14;

    private final EMRailEjectorTileEntity tile;

    public EMRailEjectorGui(EMRailEjectorContainer container) {
        super(container);
        this.tile = container.getTile();
        this.xSize = 176;
        this.ySize = 184;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1f, 1f, 1f, 1f);

        // Titanium-style gray background with border
        drawRect(guiLeft, guiTop, guiLeft + xSize, guiTop + ySize, BG_LIGHT);
        drawHorizontalLine(guiLeft, guiLeft + xSize - 1, guiTop, BORDER_LIGHT);
        drawVerticalLine(guiLeft, guiTop, guiTop + ySize - 1, BORDER_LIGHT);
        drawHorizontalLine(guiLeft, guiLeft + xSize - 1, guiTop + ySize - 1, BORDER_DARK);
        drawVerticalLine(guiLeft + xSize - 1, guiTop, guiTop + ySize - 1, BORDER_DARK);

        // Player inventory slots (background drawn 1px before slot position)
        drawSlotGrid(guiLeft + 8, guiTop + 102, 9, 3);
        drawSlotGrid(guiLeft + 8, guiTop + 160, 9, 1);

        // Input slot
        drawSlotBackground(guiLeft + INPUT_X, guiTop + INPUT_Y);

        // Bind texture for widget assets
        mc.getTextureManager().bindTexture(TEXTURE);
        GlStateManager.color(1f, 1f, 1f, 1f);

        // Energy bar background (from texture)
        drawTexturedModalRect(guiLeft + ENERGY_X, guiTop + ENERGY_Y,
                ENERGY_BG_U, ENERGY_BG_V, ENERGY_BG_W, ENERGY_BG_H);

        // Energy bar fill (red, bottom-up)
        int maxEnergy = tile.getPower().getMaxEnergyStored();
        if (maxEnergy > 0) {
            int stored = tile.getPower().getEnergyStored();
            int fillH = (int) (ENERGY_FILL_H * ((float) stored / maxEnergy));
            if (fillH > 0) {
                drawTexturedModalRect(
                        guiLeft + ENERGY_X + 3, guiTop + ENERGY_Y + 3 + (ENERGY_FILL_H - fillH),
                        ENERGY_FILL_U, ENERGY_FILL_V + (ENERGY_FILL_H - fillH),
                        ENERGY_FILL_W, fillH);
            }
        }

        // Progress bar border (from texture)
        drawTexturedModalRect(guiLeft + PROGRESS_X, guiTop + PROGRESS_Y,
                PROGRESS_BORDER_U, PROGRESS_BORDER_V, PROGRESS_BORDER_W, PROGRESS_BORDER_H);

        // Progress bar background (gray, inside border)
        drawTexturedModalRect(guiLeft + PROGRESS_X + 3, guiTop + PROGRESS_Y + 3,
                PROGRESS_BG_U, PROGRESS_BG_V, PROGRESS_BG_W, PROGRESS_BG_H);

        // Progress bar fill (cyan tinted, bottom-up)
        int maxProg = tile.getMaxProgress();
        if (maxProg > 0) {
            int prog = tile.getProgress();
            int fillH = (int) (PROGRESS_FILL_H * ((float) prog / maxProg));
            if (fillH > 0) {
                GlStateManager.color(0f, 1f, 1f, 1f);
                drawTexturedModalRect(
                        guiLeft + PROGRESS_X + 3, guiTop + PROGRESS_Y + 3 + (PROGRESS_FILL_H - fillH),
                        PROGRESS_FILL_U, PROGRESS_FILL_V + (PROGRESS_FILL_H - fillH),
                        PROGRESS_FILL_W, fillH);
                GlStateManager.color(1f, 1f, 1f, 1f);
            }
        }

        // Subscribe button (from texture)
        drawTexturedModalRect(guiLeft + SUB_X, guiTop + SUB_Y,
                BTN_PULL_U, BTN_PULL_V, BTN_PULL_W, BTN_PULL_H);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // Title
        String title = "EM Rail Ejector Controller";
        fontRenderer.drawString(title, (xSize - fontRenderer.getStringWidth(title)) / 2, 6, 0x404040);

        var sphere = ClientDysonSphere.DYSON_SPHERE_PROGRESS.getSpheres()
                .computeIfAbsent(tile.getDysonSphereId(), s -> new DysonSphereStructure());
        int x = INFO_X;
        int y = INFO_Y;
        int lineH = fontRenderer.FONT_HEIGHT + 1;
        int color = 0x5555FF;

        fontRenderer.drawString("Dyson Information", x, y, color);
        y += lineH;
        fontRenderer.drawString("Progress: " + new DecimalFormat().format(sphere.getProgress() * 100) + "%", x, y, color);
        y += lineH;
        fontRenderer.drawString("Power Gen: " + NumberUtils.getFormattedBigNumber((double) sphere.getSolarPanels() * Config.POWER_PER_SAIL) + " FE", x, y, color);
        y += lineH;
        fontRenderer.drawString("Power Con: " + NumberUtils.getFormattedBigNumber(sphere.getLastConsumedPower()) + " FE", x, y, color);
        y += lineH;
        fontRenderer.drawString("Beams: " + NumberUtils.getFormattedBigNumber(sphere.getBeams()), x, y, color);
        y += lineH;
        fontRenderer.drawString("Sails: " + NumberUtils.getFormattedBigNumber(sphere.getSolarPanels()) + "/" + NumberUtils.getFormattedBigNumber(sphere.getMaxSolarPanels()), x, y, color);
        y += lineH;
        if (sphere.getSolarPanels() >= sphere.getMaxSolarPanels()) {
            fontRenderer.drawString("Needs more beams", x, y, 0xFF5555);
            y += lineH;
        }

        // Cyan border around info area
        int infoX = x - 4;
        int infoY = INFO_Y - 4;
        int infoW = 112;
        int infoH = y - infoY + 2;
        drawHorizontalLine(infoX, infoX + infoW, infoY, CYAN);
        drawHorizontalLine(infoX, infoX + infoW, infoY + infoH, CYAN);
        drawVerticalLine(infoX, infoY, infoY + infoH, CYAN);
        drawVerticalLine(infoX + infoW, infoY, infoY + infoH, CYAN);

        // Energy tooltip
        if (mouseX >= guiLeft + ENERGY_X && mouseX <= guiLeft + ENERGY_X + ENERGY_BG_W
                && mouseY >= guiTop + ENERGY_Y && mouseY <= guiTop + ENERGY_Y + ENERGY_BG_H) {
            drawHoveringText(
                    NumberUtils.getFormattedBigNumber(tile.getPower().getEnergyStored()) + " / " +
                            NumberUtils.getFormattedBigNumber(tile.getPower().getMaxEnergyStored()) + " FE",
                    mouseX - guiLeft, mouseY - guiTop);
        }

        // Subscribe button tooltip
        if (mouseX >= guiLeft + SUB_X && mouseX <= guiLeft + SUB_X + BTN_PULL_W
                && mouseY >= guiTop + SUB_Y && mouseY <= guiTop + SUB_Y + BTN_PULL_H) {
            drawHoveringText("Subscribe to this sphere", mouseX - guiLeft, mouseY - guiTop);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseX >= guiLeft + SUB_X && mouseX <= guiLeft + SUB_X + BTN_PULL_W
                && mouseY >= guiTop + SUB_Y && mouseY <= guiTop + SUB_Y + BTN_PULL_H) {
            DCPNetworkHandler.INSTANCE.sendToServer(new ClientSubscribeSphereMessage(tile.getDysonSphereId()));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
    }

    private void drawSlotBackground(int x, int y) {
        // 3D inset slot: dark top-left edge, light bottom-right, dark fill
        drawRect(x, y, x + 18, y + 18, 0xFF636363);
        drawHorizontalLine(x, x + 17, y, BORDER_DARK);
        drawVerticalLine(x, y, y + 17, BORDER_DARK);
        drawHorizontalLine(x + 1, x + 17, y + 17, BORDER_LIGHT);
        drawVerticalLine(x + 17, y + 1, y + 17, BORDER_LIGHT);
        drawRect(x + 1, y + 1, x + 17, y + 17, 0xFF8B8B8B);
    }

    private void drawSlotGrid(int startX, int startY, int cols, int rows) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                drawSlotBackground(startX + col * 18, startY + row * 18);
            }
        }
    }
}
