package com.refitbench.dysoncubeproject.client.gui;

import com.refitbench.dysoncubeproject.Config;
import com.refitbench.dysoncubeproject.block.tile.RayReceiverTileEntity;
import com.refitbench.dysoncubeproject.network.ClientSubscribeSphereMessage;
import com.refitbench.dysoncubeproject.network.DCPNetworkHandler;
import com.refitbench.dysoncubeproject.util.NumberUtils;
import com.refitbench.dysoncubeproject.world.ClientDysonSphere;
import com.refitbench.dysoncubeproject.world.DysonSphereStructure;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.text.DecimalFormat;

public class RayReceiverGui extends GuiContainer {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("dysoncubeproject", "textures/gui/ray_receiver.png");

    private static final int CYAN = 0xFF00FFFF;
    private static final int BG_LIGHT = 0xFFC6C6C6;
    private static final int BORDER_DARK = 0xFF373737;
    private static final int BORDER_LIGHT = 0xFFFFFFFF;

    // GUI element positions
    private static final int ENERGY_X = 19, ENERGY_Y = 22;
    private static final int INFO_X = 56, INFO_Y = 24;
    private static final int SUB_X = 9, SUB_Y = 80;

    // Titanium texture UV coordinates (same sheet as em_railejector)
    private static final int ENERGY_BG_U = 177, ENERGY_BG_V = 94, ENERGY_BG_W = 18, ENERGY_BG_H = 56;
    private static final int ENERGY_FILL_U = 196, ENERGY_FILL_V = 97, ENERGY_FILL_W = 12, ENERGY_FILL_H = 50;
    private static final int BTN_PULL_U = 196, BTN_PULL_V = 31, BTN_PULL_W = 14, BTN_PULL_H = 14;

    private final RayReceiverTileEntity tile;

    public RayReceiverGui(RayReceiverContainer container) {
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

        // Player inventory slots
        drawSlotGrid(guiLeft + 8, guiTop + 102, 9, 3);
        drawSlotGrid(guiLeft + 8, guiTop + 160, 9, 1);

        // Bind texture for widget assets
        mc.getTextureManager().bindTexture(TEXTURE);
        GlStateManager.color(1f, 1f, 1f, 1f);

        // Energy bar background (from texture)
        drawTexturedModalRect(guiLeft + ENERGY_X, guiTop + ENERGY_Y,
                ENERGY_BG_U, ENERGY_BG_V, ENERGY_BG_W, ENERGY_BG_H);

        // Energy bar fill (red, bottom-up)
        int maxEnergy = tile.getEnergyStorage().getMaxEnergyStored();
        if (maxEnergy > 0) {
            int stored = tile.getEnergyStorage().getEnergyStored();
            int fillH = (int) (ENERGY_FILL_H * ((float) stored / maxEnergy));
            if (fillH > 0) {
                drawTexturedModalRect(
                        guiLeft + ENERGY_X + 3, guiTop + ENERGY_Y + 3 + (ENERGY_FILL_H - fillH),
                        ENERGY_FILL_U, ENERGY_FILL_V + (ENERGY_FILL_H - fillH),
                        ENERGY_FILL_W, fillH);
            }
        }

        // Subscribe button (from texture)
        drawTexturedModalRect(guiLeft + SUB_X, guiTop + SUB_Y,
                BTN_PULL_U, BTN_PULL_V, BTN_PULL_W, BTN_PULL_H);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = "Ray Receiver Controller";
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
                    NumberUtils.getFormattedBigNumber(tile.getEnergyStorage().getEnergyStored()) + " / " +
                            NumberUtils.getFormattedBigNumber(tile.getEnergyStorage().getMaxEnergyStored()) + " FE",
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
