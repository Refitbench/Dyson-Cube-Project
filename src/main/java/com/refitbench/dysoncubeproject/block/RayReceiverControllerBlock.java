package com.refitbench.dysoncubeproject.block;

import com.refitbench.dysoncubeproject.DCPContent;
import com.refitbench.dysoncubeproject.block.tile.RayReceiverTileEntity;
import com.refitbench.dysoncubeproject.multiblock.MultiblockStructure;
import com.refitbench.dysoncubeproject.util.RegistryUtil;
import com.refitbench.dysoncubeproject.world.DysonSphereProgressSavedData;
import com.refitbench.dysoncubeproject.world.DysonSphereStructure;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;

import javax.annotation.Nullable;

public class RayReceiverControllerBlock extends DefaultMultiblockControllerBlock implements ITileEntityProvider {

    private static final MultiblockStructure MULTIBLOCK_STRUCTURE = new MultiblockStructure(3, 6, 3);

    public RayReceiverControllerBlock() {
        RegistryUtil.setRegistryName(this, "dysoncubeproject", "ray_receiver_controller");
        setTranslationKey("dysoncubeproject.ray_receiver_controller");
    }

    @Override
    public MultiblockStructure getMultiblockStructure() {
        return MULTIBLOCK_STRUCTURE;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        if (!world.isRemote) {
            if (placer != null) {
                var dyson = DysonSphereProgressSavedData.get(world);
                var subscribedSphere = dyson.getSubscribedFor(placer.getUniqueID().toString());
                dyson.getSpheres().computeIfAbsent(subscribedSphere, s -> new DysonSphereStructure());
                dyson.markDirty();
                var te = world.getTileEntity(pos);
                if (te instanceof RayReceiverTileEntity receiver) {
                    receiver.setDysonSphereId(subscribedSphere);
                }
            }
            // Adjacent structure blocks on same level
            MultiblockStructureBlock.createStructure(world, pos, pos.east());
            MultiblockStructureBlock.createStructure(world, pos, pos.west());
            MultiblockStructureBlock.createStructure(world, pos, pos.north());
            MultiblockStructureBlock.createStructure(world, pos, pos.south());

            // Pillar above controller
            MultiblockStructureBlock.createStructure(world, pos, pos.up());
            MultiblockStructureBlock.createStructure(world, pos, pos.up(2));
            MultiblockStructureBlock.createStructure(world, pos, pos.up(3));
            MultiblockStructureBlock.createStructure(world, pos, pos.up(4));

            // 3x3 ring at y+2
            var lowerCorner = pos.add(-1, 2, -1);
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    var updatedAt = lowerCorner.add(x, 0, z);
                    MultiblockStructureBlock.createStructure(world, pos, updatedAt);
                }
            }

            // Force relight and client re-render across the placed structure volume.
            var min = pos.add(-2, 0, -2);
            var max = pos.add(2, 7, 2);
            for (BlockPos p : BlockPos.getAllInBox(min, max)) {
                world.checkLight(p);
            }
            world.markBlockRangeForRenderUpdate(min, max);
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        super.breakBlock(world, pos, state);
        if (!world.isRemote) {
            world.setBlockToAir(pos.east());
            world.setBlockToAir(pos.west());
            world.setBlockToAir(pos.north());
            world.setBlockToAir(pos.south());

            world.setBlockToAir(pos.up());
            world.setBlockToAir(pos.up(2));
            world.setBlockToAir(pos.up(3));
            world.setBlockToAir(pos.up(4));

            var lowerCorner = pos.add(-1, 2, -1);
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    var updatedAt = lowerCorner.add(x, 0, z);
                    world.setBlockToAir(updatedAt);
                }
            }
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            var te = worldIn.getTileEntity(pos);
            if (te instanceof RayReceiverTileEntity) {
                FMLNetworkHandler.openGui(playerIn, DCPContent.MOD_INSTANCE, DCPContent.GUI_RAY_RECEIVER, worldIn, pos.getX(), pos.getY(), pos.getZ());
            }
        }
        return true;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new RayReceiverTileEntity();
    }
}
