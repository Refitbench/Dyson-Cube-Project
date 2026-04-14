package com.refitbench.dysoncubeproject.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public interface IProxy {
    default void init() {}
    default void postInit() {}
    default Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) { return null; }
}
