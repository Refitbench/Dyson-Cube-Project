package com.refitbench.dysoncubeproject.client.gui;

import com.refitbench.dysoncubeproject.DCPContent;
import com.refitbench.dysoncubeproject.DysonCubeProject;
import com.refitbench.dysoncubeproject.block.tile.EMRailEjectorTileEntity;
import com.refitbench.dysoncubeproject.block.tile.RayReceiverTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class DCPGuiHandler implements IGuiHandler {

    @Nullable
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        return switch (id) {
            case DCPContent.GUI_EM_RAILEJECTOR -> te instanceof EMRailEjectorTileEntity ejector
                    ? new EMRailEjectorContainer(player, ejector) : null;
            case DCPContent.GUI_RAY_RECEIVER -> te instanceof RayReceiverTileEntity receiver
                    ? new RayReceiverContainer(player, receiver) : null;
            default -> null;
        };
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return DysonCubeProject.proxy.getClientGuiElement(id, player, world, x, y, z);
    }
}
