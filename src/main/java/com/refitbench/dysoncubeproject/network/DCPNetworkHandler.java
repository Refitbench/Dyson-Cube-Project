package com.refitbench.dysoncubeproject.network;

import com.refitbench.dysoncubeproject.Reference;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class DCPNetworkHandler {

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MOD_ID);

    private static int discriminator = 0;

    public static void init() {
        INSTANCE.registerMessage(DysonSphereSyncMessage.Handler.class, DysonSphereSyncMessage.class, discriminator++, Side.CLIENT);
        INSTANCE.registerMessage(ClientSubscribeSphereMessage.Handler.class, ClientSubscribeSphereMessage.class, discriminator++, Side.SERVER);
    }
}
