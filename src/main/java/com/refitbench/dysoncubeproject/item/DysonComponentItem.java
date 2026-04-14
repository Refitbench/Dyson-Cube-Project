package com.refitbench.dysoncubeproject.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

public class DysonComponentItem extends Item {

    private final int defaultSolarSail;
    private final int defaultBeam;

    public DysonComponentItem(int solarSail, int beam, CreativeTabs tab) {
        this.defaultSolarSail = solarSail;
        this.defaultBeam = beam;
        setCreativeTab(tab);
    }

    public static int getSolarSailCount(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("solar_sail")) {
            return stack.getTagCompound().getInteger("solar_sail");
        }
        if (stack.getItem() instanceof DysonComponentItem) {
            return ((DysonComponentItem) stack.getItem()).defaultSolarSail;
        }
        return 0;
    }

    public static int getBeamCount(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("beam")) {
            return stack.getTagCompound().getInteger("beam");
        }
        if (stack.getItem() instanceof DysonComponentItem) {
            return ((DysonComponentItem) stack.getItem()).defaultBeam;
        }
        return 0;
    }

    public static void setSolarSailCount(ItemStack stack, int count) {
        ensureTag(stack).setInteger("solar_sail", count);
    }

    public static void setBeamCount(ItemStack stack, int count) {
        ensureTag(stack).setInteger("beam", count);
    }

    private static NBTTagCompound ensureTag(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        return stack.getTagCompound();
    }

    public int getDefaultSolarSail() {
        return defaultSolarSail;
    }

    public int getDefaultBeam() {
        return defaultBeam;
    }

    @Override
    public void onCreated(ItemStack stack, World worldIn, EntityPlayer playerIn) {
        initializeDefaults(stack);
    }

    public void initializeDefaults(ItemStack stack) {
        if (defaultSolarSail > 0) setSolarSailCount(stack, defaultSolarSail);
        if (defaultBeam > 0) setBeamCount(stack, defaultBeam);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!isInCreativeTab(tab)) return;
        ItemStack stack = new ItemStack(this);
        initializeDefaults(stack);
        items.add(stack);
    }
}
