package com.refitbench.dysoncubeproject.network;

import com.refitbench.dysoncubeproject.world.DysonSphereProgressSavedData;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ClientSubscribeSphereMessage implements IMessage {

    private String sphereId;

    public ClientSubscribeSphereMessage() {
    }

    public ClientSubscribeSphereMessage(String sphereId) {
        this.sphereId = sphereId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        sphereId = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, sphereId);
    }

    public static class Handler implements IMessageHandler<ClientSubscribeSphereMessage, IMessage> {
        @Override
        public IMessage onMessage(ClientSubscribeSphereMessage message, MessageContext ctx) {
            var player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                var data = DysonSphereProgressSavedData.get(player.getServerWorld());
                if (data != null && message.sphereId != null) {
                    data.getSubscribedPlayers().put(player.getUniqueID().toString(), message.sphereId);
                    data.markDirty();
                }
            });
            return null;
        }
    }
}
