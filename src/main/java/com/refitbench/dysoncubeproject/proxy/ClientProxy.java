package com.refitbench.dysoncubeproject.proxy;

import com.refitbench.dysoncubeproject.Config;
import com.refitbench.dysoncubeproject.DCPContent;
import com.refitbench.dysoncubeproject.Reference;
import com.refitbench.dysoncubeproject.block.tile.EMRailEjectorTileEntity;
import com.refitbench.dysoncubeproject.block.tile.RayReceiverTileEntity;
import com.refitbench.dysoncubeproject.client.gui.EMRailEjectorContainer;
import com.refitbench.dysoncubeproject.client.gui.EMRailEjectorGui;
import com.refitbench.dysoncubeproject.client.gui.RayReceiverContainer;
import com.refitbench.dysoncubeproject.client.gui.RayReceiverGui;
import com.refitbench.dysoncubeproject.client.DCPExtraModels;
import com.refitbench.dysoncubeproject.client.DCPShaders;
import com.refitbench.dysoncubeproject.client.render.HologramRender;
import com.refitbench.dysoncubeproject.client.render.SkyRender;
import com.refitbench.dysoncubeproject.client.tile.EMRailEjectorRender;
import com.refitbench.dysoncubeproject.client.tile.RayReceiverRender;
import com.refitbench.dysoncubeproject.item.DysonComponentItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        return switch (id) {
            case DCPContent.GUI_EM_RAILEJECTOR -> te instanceof EMRailEjectorTileEntity ejector
                    ? new EMRailEjectorGui(new EMRailEjectorContainer(player, ejector)) : null;
            case DCPContent.GUI_RAY_RECEIVER -> te instanceof RayReceiverTileEntity receiver
                    ? new RayReceiverGui(new RayReceiverContainer(player, receiver)) : null;
            default -> null;
        };
    }

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        // Item models
        registerItemModel(DCPContent.SOLAR_SAIL, "solar_sail");
        registerItemModel(DCPContent.SOLAR_SAIL_PACKAGE, "solar_sail_package");
        registerItemModel(DCPContent.BEAM, "beam");
        registerItemModel(DCPContent.BEAM_PACKAGE, "beam_package");

        // Block item models
        registerItemModel(DCPContent.EM_RAILEJECTOR_CONTROLLER_ITEM, "em_railejector_controller");
        registerItemModel(DCPContent.RAY_RECEIVER_CONTROLLER_ITEM, "ray_receiver_controller");
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
            event.getToolTip().add("\u00a7b" + I18n.format("tooltip.dysoncubeproject.contains_solar_sails", sails));
        }
        if (beams > 0) {
            event.getToolTip().add("\u00a7b" + I18n.format("tooltip.dysoncubeproject.contains_beams", beams));
        }
        if (stack.getItem() == DCPContent.EM_RAILEJECTOR_CONTROLLER_ITEM) {
            String key = Config.RAIL_EJECTOR_REQUIRE_POWER
                    ? "tooltip.dysoncubeproject.power_required"
                    : "tooltip.dysoncubeproject.power_optional";
            event.getToolTip().add("\u00a7b" + I18n.format(key));
        }
    }

    private static void registerItemModel(Item item, String name) {
        if (item != null) {
            ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(new ResourceLocation(Reference.MOD_ID, name), "inventory"));
        }
    }
}
