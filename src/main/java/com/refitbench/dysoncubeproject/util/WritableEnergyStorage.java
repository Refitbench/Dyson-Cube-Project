package com.refitbench.dysoncubeproject.util;

import net.minecraftforge.energy.EnergyStorage;

public class WritableEnergyStorage extends EnergyStorage {

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
