package com.refitbench.dysoncubeproject.client.render;

import com.refitbench.dysoncubeproject.Config;
import com.refitbench.dysoncubeproject.client.DCPShaderHelper;
import com.refitbench.dysoncubeproject.client.DCPShaders;
import com.refitbench.dysoncubeproject.world.ClientDysonSphere;
import com.refitbench.dysoncubeproject.world.DysonSphereStructure;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class SkyRender {

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null || mc.player == null) return;
        if (mc.world.isRainingAt(mc.player.getPosition())) return;

        var subscribedTo = ClientDysonSphere.DYSON_SPHERE_PROGRESS.getSubscribedPlayers()
                .getOrDefault(mc.player.getUniqueID().toString(), mc.player.getUniqueID().toString());
        var sphere = ClientDysonSphere.DYSON_SPHERE_PROGRESS.getSpheres().getOrDefault(subscribedTo, null);

        if (sphere == null) return;
        float progress = (float) sphere.getProgress();
        if (Config.SHOW_AT_MAX_PROGRESS) progress = 1f;
        if (progress <= 0) return;

        if (DCPShaders.HOLO_HEX == null) return;

        float partialTicks = event.getPartialTicks();
        float skyAngle = mc.world.getCelestialAngle(partialTicks) * 360.0f;
        long gameTime = mc.world.getTotalWorldTime();

        DCPShaders.HOLO_HEX.bind();
        DCPShaders.HOLO_HEX.setUniform1f("uTime", (gameTime % 100000) / 20.0f);
        DCPShaders.HOLO_HEX.setUniform1f("uValid", 1.0f);
        DCPShaders.HOLO_HEX.setUniform1f("uSize", 25f);
        DCPShaders.HOLO_HEX.setUniform1f("uIsSkyPass", 1.0f);
        DCPShaders.HOLO_HEX.setUniform3f("uCamPos", 0f, 0f, 0f);

        GlStateManager.pushMatrix();

        GlStateManager.rotate(-90.0f, 0, 1, 0);
        GlStateManager.rotate(90.0f, 1, 0, 0);
        GlStateManager.rotate(skyAngle, 1, 0, 0);

        GlStateManager.translate(-30.0f, 0.0f, -310.0f);

        // Upload matrices after all transforms are applied
        DCPShaders.HOLO_HEX.uploadMatrices();

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableCull();
        // Keep depth test enabled — sky-trick z=0.9999w means we only pass where
        // existing depth is 1.0 (empty sky), so we are naturally occluded by terrain.
        GlStateManager.depthMask(false);

        float s = 30.0f;
        float rainLevel = 1.0f - mc.world.getRainStrength(partialTicks);
        int r = (int)(0.5f * 255);
        int g = (int)(0.9f * 255);
        int b = (int)(0.9f * 255);
        int a = (int)(0.7f * rainLevel * 255);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        buf.pos(0, s, 0.0f).color(r, g, b, a).endVertex();
        buf.pos(s * 2 * progress, s, 0.0f).color(r, g, b, a).endVertex();
        buf.pos(s * 2 * progress, -s, 0.0f).color(r, g, b, a).endVertex();
        buf.pos(0, -s, 0.0f).color(r, g, b, a).endVertex();

        tess.draw();

        DCPShaderHelper.unbind();
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();

        GlStateManager.popMatrix();
    }
}
