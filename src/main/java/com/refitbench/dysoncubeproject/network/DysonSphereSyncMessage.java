package com.refitbench.dysoncubeproject.network;

import com.refitbench.dysoncubeproject.world.ClientDysonSphere;
import com.refitbench.dysoncubeproject.world.DysonSphereProgressSavedData;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class DysonSphereSyncMessage implements IMessage {

    private NBTTagCompound tag;

    public DysonSphereSyncMessage() {
    }

    public DysonSphereSyncMessage(NBTTagCompound tag) {
        this.tag = tag;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        tag = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, tag);
    }

    public static class Handler implements IMessageHandler<DysonSphereSyncMessage, IMessage> {
        @Override
        public IMessage onMessage(DysonSphereSyncMessage message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                var data = new DysonSphereProgressSavedData();
                data.readFromNBT(message.tag);
                ClientDysonSphere.DYSON_SPHERE_PROGRESS = data;
            });
            return null;
        }
    }
}
