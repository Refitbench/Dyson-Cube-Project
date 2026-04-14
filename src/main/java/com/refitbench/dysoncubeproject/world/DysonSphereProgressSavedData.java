package com.refitbench.dysoncubeproject.world;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

import java.util.HashMap;

public class DysonSphereProgressSavedData extends WorldSavedData {

    public static final String ID = "dyson_sphere_progress";

    private final HashMap<String, DysonSphereStructure> spheres = new HashMap<>();
    private final HashMap<String, String> subscribedPlayers = new HashMap<>();

    public DysonSphereProgressSavedData() {
        super(ID);
    }

    public DysonSphereProgressSavedData(String name) {
        super(name);
    }

    public static DysonSphereProgressSavedData get(World world) {
        var storage = world.getMinecraftServer().getWorld(0).getMapStorage();
        var data = (DysonSphereProgressSavedData) storage.getOrLoadData(DysonSphereProgressSavedData.class, ID);
        if (data == null) {
            data = new DysonSphereProgressSavedData();
            storage.setData(ID, data);
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        spheres.clear();
        subscribedPlayers.clear();

        var spheresTag = compound.getCompoundTag("spheres");
        for (var key : spheresTag.getKeySet()) {
            var structure = new DysonSphereStructure();
            structure.deserializeNBT(spheresTag.getCompoundTag(key));
            spheres.put(key, structure);
        }

        var playersTag = compound.getCompoundTag("subscribedPlayers");
        for (var key : playersTag.getKeySet()) {
            subscribedPlayers.put(key, playersTag.getString(key));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        var spheresTag = new NBTTagCompound();
        for (var entry : spheres.entrySet()) {
            spheresTag.setTag(entry.getKey(), entry.getValue().serializeNBT());
        }

        var playersTag = new NBTTagCompound();
        for (var entry : subscribedPlayers.entrySet()) {
            playersTag.setString(entry.getKey(), entry.getValue());
        }

        compound.setTag("spheres", spheresTag);
        compound.setTag("subscribedPlayers", playersTag);
        return compound;
    }

    public HashMap<String, DysonSphereStructure> getSpheres() {
        return spheres;
    }

    public HashMap<String, String> getSubscribedPlayers() {
        return subscribedPlayers;
    }

    public String getSubscribedFor(String playerUUID) {
        return subscribedPlayers.getOrDefault(playerUUID, playerUUID);
    }
}
