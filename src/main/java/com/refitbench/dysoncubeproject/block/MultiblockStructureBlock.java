package com.refitbench.dysoncubeproject.block;

import com.refitbench.dysoncubeproject.DCPContent;
import com.refitbench.dysoncubeproject.block.tile.MultiblockStructureTileEntity;
import com.refitbench.dysoncubeproject.util.RegistryUtil;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

public class MultiblockStructureBlock extends Block implements ITileEntityProvider {

    public MultiblockStructureBlock() {
        super(Material.IRON);
        setHardness(5.0F);
        setResistance(10.0F);
        RegistryUtil.setRegistryName(this, "dysoncubeproject", "multiblock_structure");
        setTranslationKey("dysoncubeproject.multiblock_structure");
    }

    public static void createStructure(World world, BlockPos controllerPos, BlockPos at) {
        world.setBlockState(at, DCPContent.MULTIBLOCK_STRUCTURE.getDefaultState());
        world.checkLight(at); // force immediate light recalculation so TESR doesn't briefly sample 0
        var te = world.getTileEntity(at);
        if (te instanceof MultiblockStructureTileEntity structureTE) {
            structureTE.setControllerPos(controllerPos);
        }
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public int getLightOpacity(IBlockState state) {
        return 0;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        var te = worldIn.getTileEntity(pos);
        if (te instanceof MultiblockStructureTileEntity structureTE) {
            var controllerPos = structureTE.getControllerPos();
            if (controllerPos != null) {
                var controllerState = worldIn.getBlockState(controllerPos);
                var controllerBlock = controllerState.getBlock();
                if (controllerBlock instanceof DefaultMultiblockControllerBlock) {
                    return controllerBlock.onBlockActivated(worldIn, controllerPos, controllerState, playerIn, hand, facing, hitX, hitY, hitZ);
                }
            }
        }
        return false;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        var te = worldIn.getTileEntity(pos);
        if (te instanceof MultiblockStructureTileEntity structureTE) {
            var controllerPos = structureTE.getControllerPos();
            if (controllerPos != null) {
                var controllerState = worldIn.getBlockState(controllerPos);
                if (controllerState.getBlock() instanceof DefaultMultiblockControllerBlock) {
                    worldIn.destroyBlock(controllerPos, true);
                }
            }
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return null; // No drops from multiblock structure blocks
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new MultiblockStructureTileEntity();
    }
}
