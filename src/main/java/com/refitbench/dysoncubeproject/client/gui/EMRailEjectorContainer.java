package com.refitbench.dysoncubeproject.client.gui;

import com.refitbench.dysoncubeproject.block.tile.EMRailEjectorTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class EMRailEjectorContainer extends Container {

    private final EMRailEjectorTileEntity tile;
    private int lastEnergy;
    private int lastProgress;
    private int lastMaxProgress;

    public EMRailEjectorContainer(EntityPlayer player, EMRailEjectorTileEntity tile) {
        this.tile = tile;

        // Input slot (the ejectable item slot)
        addSlotToContainer(new SlotItemHandler(tile.getInput(), 0, 5, 38));

        // Player inventory (3 rows of 9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(player.inventory, col + row * 9 + 9, 9 + col * 18, 103 + row * 18));
            }
        }
        // Player hotbar
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(player.inventory, col, 9 + col * 18, 161));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return tile.getWorld().getTileEntity(tile.getPos()) == tile
                && playerIn.getDistanceSq(tile.getPos()) <= 64;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        int energy = tile.getPower().getEnergyStored();
        int progress = tile.getProgress();
        int maxProgress = tile.getMaxProgress();
        for (IContainerListener listener : listeners) {
            if (lastEnergy != energy) {
                listener.sendWindowProperty(this, 0, energy & 0xFFFF);
                listener.sendWindowProperty(this, 1, (energy >> 16) & 0xFFFF);
            }
            if (lastProgress != progress) {
                listener.sendWindowProperty(this, 2, progress);
            }
            if (lastMaxProgress != maxProgress) {
                listener.sendWindowProperty(this, 3, maxProgress);
            }
        }
        lastEnergy = energy;
        lastProgress = progress;
        lastMaxProgress = maxProgress;
    }

    private int clientEnergyLow, clientEnergyHigh;

    @Override
    public void updateProgressBar(int id, int data) {
        switch (id) {
            case 0 -> {
                clientEnergyLow = data & 0xFFFF;
                ((EMRailEjectorTileEntity.WritableEnergyStorage) tile.getPower())
                        .setEnergyStored(clientEnergyLow | (clientEnergyHigh << 16));
            }
            case 1 -> {
                clientEnergyHigh = data & 0xFFFF;
                ((EMRailEjectorTileEntity.WritableEnergyStorage) tile.getPower())
                        .setEnergyStored(clientEnergyLow | (clientEnergyHigh << 16));
            }
            case 2 -> tile.setClientProgress(data);
            case 3 -> tile.setClientMaxProgress(data);
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) return ItemStack.EMPTY;

        ItemStack current = slot.getStack();
        ItemStack original = current.copy();

        if (index == 0) {
            // From machine → player
            if (!mergeItemStack(current, 1, 37, true)) return ItemStack.EMPTY;
        } else {
            // From player → machine input slot
            if (!mergeItemStack(current, 0, 1, false)) return ItemStack.EMPTY;
        }

        if (current.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }
        return original;
    }

    public EMRailEjectorTileEntity getTile() {
        return tile;
    }
}
