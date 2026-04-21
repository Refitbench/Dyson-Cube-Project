package com.refitbench.dysoncubeproject.block.tile;

import com.refitbench.dysoncubeproject.Config;
import com.refitbench.dysoncubeproject.DCPContent;
import com.refitbench.dysoncubeproject.item.DysonComponentItem;
import com.refitbench.dysoncubeproject.world.DysonSphereProgressSavedData;
import com.refitbench.dysoncubeproject.world.DysonSphereStructure;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class EMRailEjectorTileEntity extends TileEntity implements ITickable {

    private boolean needsClientRenderRefresh = true;
    private float currentYaw = 180;
    private float currentPitch = 90;
    private float targetYaw = 180;
    private float targetPitch = 90;
    private long lastExecution = 0;
    private int progress = 0;
    private int maxProgress = 120;
    private String dysonSphereId = "default";
    private int rampupAmount = 1;
    private int cooldown = 0;

    private final ItemStackHandler input = new ItemStackHandler(1) {
        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (!(stack.getItem() instanceof DysonComponentItem)) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }
    };

    private final WritableEnergyStorage power = new WritableEnergyStorage(Config.RAIL_EJECTOR_POWER_BUFFER, Config.RAIL_EJECTOR_POWER_BUFFER, 0);

    private void refreshClientRender() {
        if (world == null || !world.isRemote) return;
        world.markBlockRangeForRenderUpdate(pos.add(-2, 0, -2), pos.add(2, 4, 2));
        needsClientRenderRefresh = false;
    }

    private void syncRenderStateToClient() {
        if (world == null || world.isRemote) return;
        IBlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 3);
    }

    @Override
    public void update() {
        // Sun tracking runs on both sides — client needs it for rendering
        targetPitch = world.getCelestialAngle(1f) * 360f;

        if (targetPitch <= 10) targetPitch = 10;
        if (targetPitch >= 350) targetPitch = 10;

        if (targetPitch <= 90) {
            targetYaw = 0;
        } else {
            targetYaw = 180;
        }

        if (targetPitch >= 90 && targetPitch <= 270) {
            targetPitch = 90;
        }

        if (targetPitch >= 270) {
            targetPitch = 360 - targetPitch;
        }

        if (world.isRaining()) {
            targetPitch = 90;
        }

        // Smoothly move toward target
        if (currentPitch < targetPitch) {
            currentPitch = Math.min(currentPitch + 1, targetPitch);
        } else if (currentPitch > targetPitch) {
            currentPitch = Math.max(currentPitch - 1, targetPitch);
        }
        if (currentYaw < targetYaw) {
            currentYaw = Math.min(currentYaw + 1, targetYaw);
        } else if (currentYaw > targetYaw) {
            currentYaw = Math.max(currentYaw - 1, targetYaw);
        }

        if (world.isRemote) {
            if (needsClientRenderRefresh) {
                refreshClientRender();
            }
            clientTick();
            return;
        }

        boolean syncRenderState = false;

        // Progress bar logic
        if (canIncrease()) {
            onTickWork();
            progress++;
            syncRenderState = true;
            if (progress >= maxProgress) {
                progress = 0;
                onFinishWork();
                syncRenderState = true;
            }
            markDirty();
        } else {
            if (progress > 0) {
                progress = 0;
                syncRenderState = true;
                markDirty();
            }
        }

        if (cooldown > 0) cooldown--;

        if (syncRenderState) {
            syncRenderStateToClient();
        }
    }

    private void clientTick() {
        if (progress == 7) {
            world.playSound(pos.getX(), pos.getY(), pos.getZ(), DCPContent.SOUND_RAILGUN, SoundCategory.BLOCKS, 1, 1, false);
        }
    }

    private boolean canIncrease() {
        if (cooldown > 0) return false;
        if (input.getStackInSlot(0).isEmpty()) return false;
        if (world.isRaining() || !world.isDaytime() || !world.canSeeSky(pos.up())) return false;

        var time = world.getCelestialAngle(1f) * 360f;
        if (time <= 10 || time >= 350) return false;

        var dyson = DysonSphereProgressSavedData.get(world).getSpheres()
                .computeIfAbsent(dysonSphereId, s -> new DysonSphereStructure());
        if (dyson.getProgress() >= 1) return false;

        var stack = input.getStackInSlot(0);
        var solarPanels = DysonComponentItem.getSolarSailCount(stack);
        var beams = DysonComponentItem.getBeamCount(stack);
        if (solarPanels > 0 && (dyson.getSolarPanels() + solarPanels) >= dyson.getMaxSolarPanels()) return false;
        if (beams > 0 && dyson.getBeams() >= dyson.getMaxBeams()) return false;

        int requiredPower = (int) (Math.pow(rampupAmount, 2) * Config.RAIL_EJECTOR_CONSUME);
        if (Config.RAIL_EJECTOR_REQUIRE_POWER && power.getEnergyStored() < requiredPower) {
            return false;
        }

        if (rampupAmount > 1 && power.getEnergyStored() < requiredPower) {
            rampupAmount = 1;
            return false;
        }

        return true;
    }

    private void onTickWork() {
        int requiredPower = (int) (Math.pow(rampupAmount, 2) * Config.RAIL_EJECTOR_CONSUME);
        int drain = Config.RAIL_EJECTOR_REQUIRE_POWER
                ? requiredPower
                : (int) Math.min(power.getEnergyStored(), requiredPower);
        power.drainInternal(drain);
    }

    private void onFinishWork() {
        var data = DysonSphereProgressSavedData.get(world);
        var dyson = data.getSpheres().computeIfAbsent(dysonSphereId, s -> new DysonSphereStructure());
        boolean reset = false;
        for (int i = 0; i < rampupAmount; i++) {
            var stack = input.getStackInSlot(0);
            if (!stack.isEmpty()) {
                var solarPanels = DysonComponentItem.getSolarSailCount(stack);
                var beams = DysonComponentItem.getBeamCount(stack);
                stack.shrink(1);
                dyson.increaseBeams(beams);
                dyson.increaseSolarPanels(solarPanels);
            } else {
                reset = true;
            }
        }
        lastExecution = world.getTotalWorldTime();
        cooldown = 30;
        data.markDirty();
        if (reset) {
            rampupAmount = 1;
        } else {
            rampupAmount = Math.min(rampupAmount + 1, 64);
        }
    }

    // --- Getters/Setters ---

    public float getCurrentPitch() { return currentPitch; }
    public float getCurrentYaw() { return currentYaw; }
    public long getLastExecution() { return lastExecution; }
    public int getProgress() { return progress; }
    public int getMaxProgress() { return maxProgress; }
    public String getDysonSphereId() { return dysonSphereId; }
    public ItemStackHandler getInput() { return input; }
    public EnergyStorage getPower() { return power; }

    public void setDysonSphereId(String dysonSphereId) {
        this.dysonSphereId = dysonSphereId;
        markDirty();
    }

    // Client-side setters for container sync
    public void setClientProgress(int progress) { this.progress = progress; }
    public void setClientMaxProgress(int maxProgress) { this.maxProgress = maxProgress; }

    // --- WritableEnergyStorage ---

    public static class WritableEnergyStorage extends EnergyStorage {
        public WritableEnergyStorage(int capacity, int maxReceive, int maxExtract) {
            super(capacity, maxReceive, maxExtract);
        }

        public void setEnergyStored(int energy) {
            this.energy = Math.clamp(energy, 0, capacity);
        }

        public void drainInternal(int amount) {
            this.energy = Math.max(0, this.energy - amount);
        }
    }

    // --- NBT ---

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setFloat("currentYaw", currentYaw);
        compound.setFloat("currentPitch", currentPitch);
        compound.setFloat("targetYaw", targetYaw);
        compound.setFloat("targetPitch", targetPitch);
        compound.setLong("lastExecution", lastExecution);
        compound.setInteger("progress", progress);
        compound.setString("dysonSphereId", dysonSphereId);
        compound.setInteger("rampupAmount", rampupAmount);
        compound.setTag("input", input.serializeNBT());
        compound.setInteger("energy", power.getEnergyStored());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        currentYaw = compound.getFloat("currentYaw");
        currentPitch = compound.getFloat("currentPitch");
        targetYaw = compound.getFloat("targetYaw");
        targetPitch = compound.getFloat("targetPitch");
        lastExecution = compound.getLong("lastExecution");
        progress = compound.getInteger("progress");
        dysonSphereId = compound.getString("dysonSphereId");
        rampupAmount = compound.getInteger("rampupAmount");
        input.deserializeNBT(compound.getCompoundTag("input"));
        power.setEnergyStored(compound.getInteger("energy"));
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

    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(
            pos.getX() - 1, pos.getY(), pos.getZ() - 1,
            pos.getX() + 2, pos.getY() + 5, pos.getZ() + 2
        );
    }

    // --- Capabilities (hasCapability/getCapability added to TileEntity at runtime via Forge ASM) ---

    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) return true;
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return true;
        return false;
    }

    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) return (T) power;
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return (T) input;
        return null;
    }
}
