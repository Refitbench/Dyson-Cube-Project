package com.refitbench.dysoncubeproject.world;

import com.refitbench.dysoncubeproject.Config;
import net.minecraft.nbt.NBTTagCompound;

public class DysonSphereStructure {

    private int beams;
    private int solarPanels;
    private int storedPower;
    private int lastConsumedPower;

    public DysonSphereStructure() {
        this(0, 0);
    }

    public DysonSphereStructure(int beams, int solarPanels) {
        this.beams = beams;
        this.solarPanels = solarPanels;
    }

    public int getBeams() {
        return beams;
    }

    public void setBeams(int beams) {
        this.beams = beams;
    }

    public int getSolarPanels() {
        return solarPanels;
    }

    public void setSolarPanels(int solarPanels) {
        this.solarPanels = solarPanels;
    }

    public int getMaxSolarPanels() {
        return beams * Config.BEAM_TO_SOLAR_PANEL_RATIO;
    }

    public double getProgress() {
        return solarPanels / (double) Config.MAX_SOLAR_PANELS;
    }

    public int getMaxBeams() {
        return Config.MAX_SOLAR_PANELS / Config.BEAM_TO_SOLAR_PANEL_RATIO;
    }

    public void increaseBeams(int amount) {
        this.beams += amount;
        if (this.beams > getMaxBeams()) this.beams = getMaxBeams();
    }

    public void increaseSolarPanels(int amount) {
        this.solarPanels += amount;
        if (this.solarPanels > getMaxSolarPanels()) this.solarPanels = getMaxSolarPanels();
    }

    public void generatePower() {
        this.lastConsumedPower = 0;
        this.storedPower = Math.min(this.solarPanels * Config.POWER_PER_SAIL, this.storedPower + this.solarPanels * Config.POWER_PER_SAIL);
    }

    public int extractPower(int amount) {
        int extracted = Math.min(amount, this.storedPower);
        this.storedPower -= extracted;
        this.lastConsumedPower += extracted;
        return extracted;
    }

    public int getStoredPower() {
        return storedPower;
    }

    public int getLastConsumedPower() {
        return lastConsumedPower;
    }

    public NBTTagCompound serializeNBT() {
        var tag = new NBTTagCompound();
        tag.setInteger("beams", beams);
        tag.setInteger("solarPanels", solarPanels);
        tag.setInteger("storedPower", storedPower);
        tag.setInteger("lastConsumedPower", lastConsumedPower);
        return tag;
    }

    public void deserializeNBT(NBTTagCompound tag) {
        this.beams = tag.getInteger("beams");
        this.solarPanels = tag.getInteger("solarPanels");
        this.storedPower = tag.getInteger("storedPower");
        this.lastConsumedPower = tag.getInteger("lastConsumedPower");
    }
}
