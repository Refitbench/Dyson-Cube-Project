package com.refitbench.dysoncubeproject.client.gui;

import com.refitbench.dysoncubeproject.block.tile.RayReceiverTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class RayReceiverContainer extends Container {

    private final RayReceiverTileEntity tile;
    private int lastEnergy;

    public RayReceiverContainer(EntityPlayer player, RayReceiverTileEntity tile) {
        this.tile = tile;

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
        int energy = tile.getEnergyStorage().getEnergyStored();
        for (IContainerListener listener : listeners) {
            if (lastEnergy != energy) {
                listener.sendWindowProperty(this, 0, energy & 0xFFFF);
                listener.sendWindowProperty(this, 1, (energy >> 16) & 0xFFFF);
            }
        }
        lastEnergy = energy;
    }

    private int clientEnergyLow, clientEnergyHigh;

    @Override
    public void updateProgressBar(int id, int data) {
        switch (id) {
            case 0 -> {
                clientEnergyLow = data & 0xFFFF;
                ((RayReceiverTileEntity.WritableEnergyStorage) tile.getEnergyStorage())
                        .setEnergyStored(clientEnergyLow | (clientEnergyHigh << 16));
            }
            case 1 -> {
                clientEnergyHigh = data & 0xFFFF;
                ((RayReceiverTileEntity.WritableEnergyStorage) tile.getEnergyStorage())
                        .setEnergyStored(clientEnergyLow | (clientEnergyHigh << 16));
            }
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        // No machine slots to transfer to
        return ItemStack.EMPTY;
    }

    public RayReceiverTileEntity getTile() {
        return tile;
    }
}
