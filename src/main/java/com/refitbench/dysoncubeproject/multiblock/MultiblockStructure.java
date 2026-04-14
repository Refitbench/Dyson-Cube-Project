package com.refitbench.dysoncubeproject.multiblock;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MultiblockStructure {

    private final int sizeX, sizeY, sizeZ;

    public MultiblockStructure(int sizeX, int sizeY, int sizeZ) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public int getSizeZ() {
        return sizeZ;
    }

    public boolean validateSpace(World world, BlockPos anchor) {
        if (sizeX <= 0 || sizeY <= 0 || sizeZ <= 0) return false;

        int halfX = sizeX / 2;
        int halfZ = sizeZ / 2;
        var min = anchor.add(-halfX, 0, -halfZ);
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    if (!world.isAirBlock(min.add(x, y, z))) return false;
                }
            }
        }
        return true;
    }

    public AxisAlignedBB getAABB(BlockPos anchor) {
        return new AxisAlignedBB(
                anchor.getX() - sizeX, anchor.getY(), anchor.getZ() - sizeZ,
                anchor.getX() + sizeX, anchor.getY() + sizeY, anchor.getZ() + sizeZ
        );
    }
}
