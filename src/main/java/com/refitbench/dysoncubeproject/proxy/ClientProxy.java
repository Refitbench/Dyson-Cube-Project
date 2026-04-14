package com.refitbench.dysoncubeproject.proxy;

import com.refitbench.dysoncubeproject.DCPContent;
import com.refitbench.dysoncubeproject.Reference;
import com.refitbench.dysoncubeproject.block.tile.EMRailEjectorTileEntity;
import com.refitbench.dysoncubeproject.block.tile.RayReceiverTileEntity;
import com.refitbench.dysoncubeproject.client.DCPExtraModels;
import com.refitbench.dysoncubeproject.client.DCPShaders;
import com.refitbench.dysoncubeproject.client.render.HologramRender;
import com.refitbench.dysoncubeproject.client.render.SkyRender;
import com.refitbench.dysoncubeproject.client.tile.EMRailEjectorRender;
import com.refitbench.dysoncubeproject.client.tile.RayReceiverRender;
import com.refitbench.dysoncubeproject.item.DysonComponentItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Side.CLIENT)
public class ClientProxy implements IProxy {

    private static void doInit() {
        // Register TESRs
        ClientRegistry.bindTileEntitySpecialRenderer(EMRailEjectorTileEntity.class, new EMRailEjectorRender());
        ClientRegistry.bindTileEntitySpecialRenderer(RayReceiverTileEntity.class, new RayReceiverRender());

        // Register event-based renderers
        MinecraftForge.EVENT_BUS.register(new SkyRender());
        MinecraftForge.EVENT_BUS.register(new HologramRender());
    }

    private static void doPostInit() {
        DCPShaders.loadAll();
    }

    @Override
    public void init() {
        doInit();
    }

    @Override
    public void postInit() {
        doPostInit();
    }

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        // Item models
        registerItemModel(DCPContent.SOLAR_SAIL, "solar_sail");
        registerItemModel(DCPContent.SOLAR_SAIL_PACKAGE, "solar_sail_package");
        registerItemModel(DCPContent.BEAM, "beam");
        registerItemModel(DCPContent.BEAM_PACKAGE, "beam_package");

        // Block item models
        registerItemModel(Item.getItemFromBlock(DCPContent.EM_RAILEJECTOR_CONTROLLER), "em_railejector_controller");
        registerItemModel(Item.getItemFromBlock(DCPContent.RAY_RECEIVER_CONTROLLER), "ray_receiver_controller");
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        DCPExtraModels.onModelBake(event);
    }

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        // These textures are only referenced by TESR-baked submodels, so register them
        // explicitly to guarantee they are present in the atlas on initial client load.
        event.getMap().registerSprite(new ResourceLocation(Reference.MOD_ID, "block/em_railejector_controller"));
        event.getMap().registerSprite(new ResourceLocation(Reference.MOD_ID, "block/em_railejector_gun"));
        event.getMap().registerSprite(new ResourceLocation(Reference.MOD_ID, "block/ray_receiver_base"));
        event.getMap().registerSprite(new ResourceLocation(Reference.MOD_ID, "block/ray_receiver_plate"));
        event.getMap().registerSprite(new ResourceLocation(Reference.MOD_ID, "block/ray_receiver_lens"));
        event.getMap().registerSprite(new ResourceLocation(Reference.MOD_ID, "block/particle"));
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        var stack = event.getItemStack();
        int sails = DysonComponentItem.getSolarSailCount(stack);
        int beams = DysonComponentItem.getBeamCount(stack);
        if (sails > 0) {
            event.getToolTip().add("\u00a7b" + net.minecraft.client.resources.I18n.format("tooltip.dysoncubeproject.contains_solar_sails", sails));
        }
        if (beams > 0) {
            event.getToolTip().add("\u00a7b" + net.minecraft.client.resources.I18n.format("tooltip.dysoncubeproject.contains_beams", beams));
        }
        if (stack.getItem() == Item.getItemFromBlock(DCPContent.EM_RAILEJECTOR_CONTROLLER)) {
            event.getToolTip().add("\u00a7b" + net.minecraft.client.resources.I18n.format("tooltip.dysoncubeproject.power_optional"));
        }
    }

    private static void registerItemModel(Item item, String name) {
        if (item != null) {
            ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(new ResourceLocation(Reference.MOD_ID, name), "inventory"));
        }
    }
}
