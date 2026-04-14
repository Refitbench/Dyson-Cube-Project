package com.refitbench.dysoncubeproject;

import com.refitbench.dysoncubeproject.block.EMRailEjectorControllerBlock;
import com.refitbench.dysoncubeproject.block.MultiblockStructureBlock;
import com.refitbench.dysoncubeproject.block.RayReceiverControllerBlock;
import com.refitbench.dysoncubeproject.block.tile.EMRailEjectorTileEntity;
import com.refitbench.dysoncubeproject.block.tile.MultiblockStructureTileEntity;
import com.refitbench.dysoncubeproject.block.tile.RayReceiverTileEntity;
import com.refitbench.dysoncubeproject.item.DysonComponentItem;
import com.refitbench.dysoncubeproject.util.RegistryUtil;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

@SuppressWarnings({"rawtypes", "unchecked"})
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class DCPContent {

    public static final int GUI_EM_RAILEJECTOR = 0;
    public static final int GUI_RAY_RECEIVER = 1;

    public static Object MOD_INSTANCE; // set from main mod class

    // Creative tab
    public static final CreativeTabs TAB = new CreativeTabs(CreativeTabs.CREATIVE_TAB_ARRAY.length, Reference.MOD_ID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(SOLAR_SAIL);
        }
    };

    // Blocks
    public static Block MULTIBLOCK_STRUCTURE;
    public static Block EM_RAILEJECTOR_CONTROLLER;
    public static Block RAY_RECEIVER_CONTROLLER;

    // Items
    public static Item SOLAR_SAIL;
    public static Item SOLAR_SAIL_PACKAGE;
    public static Item BEAM;
    public static Item BEAM_PACKAGE;

    // Sounds
    public static SoundEvent SOUND_RAILGUN;
    public static SoundEvent SOUND_RAY;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register event) {
        if (!new ResourceLocation("minecraft", "blocks").equals(event.getName())) return;
        MULTIBLOCK_STRUCTURE = new MultiblockStructureBlock();
        EM_RAILEJECTOR_CONTROLLER = new EMRailEjectorControllerBlock();
        RAY_RECEIVER_CONTROLLER = new RayReceiverControllerBlock();

        IForgeRegistry registry = event.getRegistry();
        RegistryUtil.register(registry, MULTIBLOCK_STRUCTURE);
        RegistryUtil.register(registry, EM_RAILEJECTOR_CONTROLLER);
        RegistryUtil.register(registry, RAY_RECEIVER_CONTROLLER);

        GameRegistry.registerTileEntity(MultiblockStructureTileEntity.class, new ResourceLocation(Reference.MOD_ID, "multiblock_structure"));
        GameRegistry.registerTileEntity(EMRailEjectorTileEntity.class, new ResourceLocation(Reference.MOD_ID, "em_railejector"));
        GameRegistry.registerTileEntity(RayReceiverTileEntity.class, new ResourceLocation(Reference.MOD_ID, "ray_receiver"));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register event) {
        if (!new ResourceLocation("minecraft", "items").equals(event.getName())) return;
        IForgeRegistry registry = event.getRegistry();

        // Item blocks for placeable blocks
        RegistryUtil.register(registry, createItemBlock(EM_RAILEJECTOR_CONTROLLER));
        RegistryUtil.register(registry, createItemBlock(RAY_RECEIVER_CONTROLLER));

        // Dyson component items
        SOLAR_SAIL = createItem(new DysonComponentItem(1, 0, TAB), "solar_sail");
        SOLAR_SAIL_PACKAGE = createItem(new DysonComponentItem(8, 0, TAB), "solar_sail_package");
        BEAM = createItem(new DysonComponentItem(0, 1, TAB), "beam");
        BEAM_PACKAGE = createItem(new DysonComponentItem(0, 4, TAB), "beam_package");

        RegistryUtil.register(registry, SOLAR_SAIL);
        RegistryUtil.register(registry, SOLAR_SAIL_PACKAGE);
        RegistryUtil.register(registry, BEAM);
        RegistryUtil.register(registry, BEAM_PACKAGE);
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register event) {
        if (!new ResourceLocation("minecraft", "soundevents").equals(event.getName())) return;
        SOUND_RAILGUN = (SoundEvent) RegistryUtil.setRegistryName(
                new SoundEvent(new ResourceLocation(Reference.MOD_ID, "railgun")),
                Reference.MOD_ID, "railgun");
        SOUND_RAY = (SoundEvent) RegistryUtil.setRegistryName(
                new SoundEvent(new ResourceLocation(Reference.MOD_ID, "ray")),
                Reference.MOD_ID, "ray");

        IForgeRegistry registry = event.getRegistry();
        RegistryUtil.register(registry, SOUND_RAILGUN);
        RegistryUtil.register(registry, SOUND_RAY);
    }

    private static Item createItem(Item item, String name) {
        RegistryUtil.setRegistryName(item, Reference.MOD_ID, name);
        item.setTranslationKey(Reference.MOD_ID + "." + name);
        return item;
    }

    private static ItemBlock createItemBlock(Block block) {
        var ib = new ItemBlock(block);
        RegistryUtil.setRegistryName(ib, RegistryUtil.getRegistryName(block));
        ib.setCreativeTab(TAB);
        return ib;
    }
}
