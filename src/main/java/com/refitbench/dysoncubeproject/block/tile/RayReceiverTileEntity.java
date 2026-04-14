package com.refitbench.dysoncubeproject.block.tile;

import com.refitbench.dysoncubeproject.Config;
import com.refitbench.dysoncubeproject.DCPContent;
import com.refitbench.dysoncubeproject.world.DysonSphereProgressSavedData;
import com.refitbench.dysoncubeproject.world.DysonSphereStructure;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;

import javax.annotation.Nullable;

public class RayReceiverTileEntity extends TileEntity implements ITickable {

    private boolean needsClientRenderRefresh = true;
    private String dysonSphereId = "default";
    private float currentPitch = 270;

    private final WritableEnergyStorage energyStorage = new WritableEnergyStorage(Config.RAY_RECEIVER_POWER_BUFFER, 0, Integer.MAX_VALUE);

    private void refreshClientRender() {
        if (world == null || !world.isRemote) return;
        world.markBlockRangeForRenderUpdate(pos.add(-2, 0, -2), pos.add(2, 7, 2));
        needsClientRenderRefresh = false;
    }

    @Override
    public void update() {
        // Lens tracking runs on both sides — client needs it for rendering
        float targetPitch = world.getCelestialAngle(1f) * 360f;
        if (targetPitch >= 90 && targetPitch <= 270) {
            targetPitch = 270;
        }

        if (currentPitch % 360 <= targetPitch) {
            currentPitch = Math.min((currentPitch + 1) % 360, targetPitch);
        } else if (currentPitch > targetPitch) {
            currentPitch = Math.max(currentPitch - 1, targetPitch);
        }

        if (world.isRemote) {
            if (needsClientRenderRefresh) {
                refreshClientRender();
            }
            clientTick();
            return;
        }

        // Extract power from Dyson sphere during daytime
        if (world.isDaytime() && !world.isRaining() && world.canSeeSky(pos.up())) {
            var dyson = DysonSphereProgressSavedData.get(world);
            var extractingAmount = Math.min(Config.RAY_RECEIVER_EXTRACT_POWER,
                    energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored());
            var extracted = dyson.getSpheres()
                    .computeIfAbsent(dysonSphereId, s -> new DysonSphereStructure())
                    .extractPower(extractingAmount);
            energyStorage.setEnergyStored(energyStorage.getEnergyStored() + extracted);
        }

        // Push energy to block below (hasCapability/getCapability added via Forge ASM)
        var below = world.getTileEntity(pos.down());
        if (below instanceof ICapabilityProvider provider
                && provider.hasCapability(CapabilityEnergy.ENERGY, EnumFacing.UP)) {
            var cap = provider.getCapability(CapabilityEnergy.ENERGY, EnumFacing.UP);
            if (cap != null && cap.canReceive()) {
                int toSend = Math.min(Config.RAY_RECEIVER_EXTRACT_POWER, energyStorage.getEnergyStored());
                int received = cap.receiveEnergy(toSend, false);
                energyStorage.setEnergyStored(energyStorage.getEnergyStored() - received);
            }
        }
    }

    private void clientTick() {
        if ((world.getTotalWorldTime() + pos.toLong()) % (17 * 20) == 0
                && world.getWorldTime() % 24000 < 12000
                && !world.isRaining()) {
            world.playSound(pos.getX(), pos.getY(), pos.getZ(),
                    DCPContent.SOUND_RAY, SoundCategory.BLOCKS, 0.5f, 1f, false);
        }
    }

    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(
            pos.getX() - 1, pos.getY(), pos.getZ() - 1,
            pos.getX() + 2, pos.getY() + 7, pos.getZ() + 2
        );
    }

    // --- Getters/Setters ---

    public String getDysonSphereId() { return dysonSphereId; }

    public void setDysonSphereId(String dysonSphereId) {
        this.dysonSphereId = dysonSphereId;
        markDirty();
    }

    public float getCurrentPitch() { return currentPitch; }
    public EnergyStorage getEnergyStorage() { return energyStorage; }

    // --- WritableEnergyStorage ---

    public static class WritableEnergyStorage extends EnergyStorage {
        public WritableEnergyStorage(int capacity, int maxReceive, int maxExtract) {
            super(capacity, maxReceive, maxExtract);
        }

        public void setEnergyStored(int energy) {
            this.energy = Math.max(0, Math.min(capacity, energy));
        }
    }

    // --- NBT ---

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setString("dysonSphereId", dysonSphereId);
        compound.setFloat("currentPitch", currentPitch);
        compound.setInteger("energy", energyStorage.getEnergyStored());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        dysonSphereId = compound.getString("dysonSphereId");
        currentPitch = compound.getFloat("currentPitch");
        energyStorage.setEnergyStored(compound.getInteger("energy"));
    }

    // --- Sync ---

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    // onDataPacket is added to TileEntity at runtime via Forge ASM
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
        needsClientRenderRefresh = true;
        refreshClientRender();
    }

    // --- Capabilities (hasCapability/getCapability added to TileEntity at runtime via Forge ASM) ---

    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) return true;
        return false;
    }

    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) return (T) energyStorage;
        return null;
    }
}
