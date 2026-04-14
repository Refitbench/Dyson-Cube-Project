package com.refitbench.dysoncubeproject;

import com.refitbench.dysoncubeproject.util.RegistryUtil;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.registries.IForgeRegistry;

@SuppressWarnings({"rawtypes", "unchecked"})
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class DCPRecipes {

    @SubscribeEvent
        public static void registerRecipes(RegistryEvent.Register event) {
                if (!new ResourceLocation("minecraft", "recipes").equals(event.getName())) return;
                IForgeRegistry registry = event.getRegistry();

        // EM Rail Ejector Controller: DRB / RCB / SSS (copper if available, else gold)
        register(registry, "em_railejector_controller",
                new ShapedOreRecipe(
                        new ResourceLocation(Reference.MOD_ID, "em_railejector_controller"),
                        new ItemStack(DCPContent.EM_RAILEJECTOR_CONTROLLER),
                        "DRB", "RCB", "SSS",
                        'D', "gemDiamond",
                        'R', "dustRedstone",
                        'B', new ItemStack(DCPContent.BEAM),
                        'C', OreDictionary.getOres("blockCopper").isEmpty() ? "blockGold" : "blockCopper",
                        'S', new ItemStack(Blocks.STONE_SLAB, 1, 0)));

        // Solar Sail: GCG / GCG / LCL (copper if available, else gold)
        register(registry, "solar_sail",
                new ShapedOreRecipe(
                        new ResourceLocation(Reference.MOD_ID, "solar_sail"),
                        new ItemStack(DCPContent.SOLAR_SAIL),
                        "GCG", "GCG", "LCL",
                        'G', "paneGlassColorless",
                        'C', OreDictionary.getOres("ingotCopper").isEmpty() ? "ingotGold" : "ingotCopper",
                        'L', "gemLapis"));

        // Solar Sail Package: GGG / GIG / GGG
        register(registry, "solar_sail_package",
                new ShapedOreRecipe(
                        new ResourceLocation(Reference.MOD_ID, "solar_sail_package"),
                        new ItemStack(DCPContent.SOLAR_SAIL_PACKAGE),
                        "GGG", "GIG", "GGG",
                        'G', new ItemStack(DCPContent.SOLAR_SAIL),
                        'I', "blockIron"));

        // Beam x2: NIN / BIB / NIN
        register(registry, "beam",
                new ShapedOreRecipe(
                        new ResourceLocation(Reference.MOD_ID, "beam"),
                        new ItemStack(DCPContent.BEAM, 2),
                        "NIN", "BIB", "NIN",
                        'N', "nuggetIron",
                        'I', "blockIron",
                        'B', new ItemStack(Blocks.IRON_BARS)));

        // Beam Package: _G_ / GIG / _G_ (copper if available, else gold)
        register(registry, "beam_package",
                new ShapedOreRecipe(
                        new ResourceLocation(Reference.MOD_ID, "beam_package"),
                        new ItemStack(DCPContent.BEAM_PACKAGE),
                        " G ", "GIG", " G ",
                        'G', new ItemStack(DCPContent.BEAM),
                        'I', OreDictionary.getOres("blockCopper").isEmpty() ? "blockGold" : "blockCopper"));

        // Ray Receiver Controller: SSS / NBN / III
        register(registry, "ray_receiver_controller",
                new ShapedOreRecipe(
                        new ResourceLocation(Reference.MOD_ID, "ray_receiver_controller"),
                        new ItemStack(DCPContent.RAY_RECEIVER_CONTROLLER),
                        "SSS", "NBN", "III",
                        'S', new ItemStack(DCPContent.SOLAR_SAIL),
                        'N', new ItemStack(Blocks.STONE_SLAB, 1, 0),
                        'I', "blockIron",
                        'B', new ItemStack(DCPContent.BEAM)));
    }

        private static void register(IForgeRegistry registry, String name, IRecipe recipe) {
        RegistryUtil.setRegistryName(recipe, Reference.MOD_ID, name);
                RegistryUtil.register(registry, recipe);
    }
}
