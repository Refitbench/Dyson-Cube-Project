package com.refitbench.dysoncubeproject;

import com.refitbench.dysoncubeproject.client.gui.DCPGuiHandler;
import com.refitbench.dysoncubeproject.network.DCPNetworkHandler;
import com.refitbench.dysoncubeproject.network.DysonSphereSyncMessage;
import com.refitbench.dysoncubeproject.proxy.IProxy;
import com.refitbench.dysoncubeproject.world.DysonSphereProgressSavedData;
import com.refitbench.dysoncubeproject.world.DysonSphereStructure;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION, updateJSON = Reference.UPDATE_JSON)
public class DysonCubeProject {

    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_NAME);

    @Mod.Instance(Reference.MOD_ID)
    public static DysonCubeProject instance;

    @SidedProxy(modId = Reference.MOD_ID, clientSide = "com.refitbench.dysoncubeproject.proxy.ClientProxy", serverSide = "com.refitbench.dysoncubeproject.proxy.CommonProxy")
    public static IProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Starting {} pre-initialization", Reference.MOD_NAME);
        Config.load(event.getSuggestedConfigurationFile());
        DCPContent.MOD_INSTANCE = instance;
        DCPNetworkHandler.init();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        LOGGER.info("Starting {} initialization", Reference.MOD_NAME);
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new DCPGuiHandler());
        MinecraftForge.EVENT_BUS.register(this);
        proxy.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        LOGGER.info("{} initialization complete", Reference.MOD_NAME);
        proxy.postInit();
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new DCPCommand());
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!(event.world instanceof WorldServer worldServer)) return;
        if (worldServer.provider.getDimensionType().getId() != 0) return;

        var data = DysonSphereProgressSavedData.get(worldServer);

        // Sync to all players every 4 ticks
        if (worldServer.getTotalWorldTime() % 4 == 0) {
            NBTTagCompound syncTag = data.writeToNBT(new NBTTagCompound());
            var packet = new DysonSphereSyncMessage(syncTag);
            for (EntityPlayerMP player : worldServer.getMinecraftServer().getPlayerList().getPlayers()) {
                DCPNetworkHandler.INSTANCE.sendTo(packet, player);
            }
        }

        // Generate power for all spheres every tick
        data.getSpheres().values().forEach(DysonSphereStructure::generatePower);
        data.markDirty();
    }

}
