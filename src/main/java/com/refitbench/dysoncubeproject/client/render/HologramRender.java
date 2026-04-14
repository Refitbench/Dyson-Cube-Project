package com.refitbench.dysoncubeproject.client.render;

import com.refitbench.dysoncubeproject.block.DefaultMultiblockControllerBlock;
import com.refitbench.dysoncubeproject.client.DCPShaderHelper;
import com.refitbench.dysoncubeproject.client.DCPShaders;
import com.refitbench.dysoncubeproject.multiblock.MultiblockStructure;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class HologramRender {

    @SubscribeEvent
    public void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
        EntityPlayer player = event.getPlayer();
        if (player == null) return;

        DefaultMultiblockControllerBlock controller = getHeldController(player.getHeldItemMainhand());
        if (controller == null) controller = getHeldController(player.getHeldItemOffhand());
        if (controller == null) return;

        RayTraceResult target = event.getTarget();
        if (target == null || target.typeOfHit != RayTraceResult.Type.BLOCK) return;

        BlockPos anchor = target.getBlockPos().offset(target.sideHit);

        World world = Minecraft.getMinecraft().world;
        if (world == null) return;

        MultiblockStructure structure = controller.getMultiblockStructure();
        int sizeX = structure.getSizeX();
        int sizeY = structure.getSizeY();
        int sizeZ = structure.getSizeZ();
        if (sizeX <= 0 || sizeY <= 0 || sizeZ <= 0) return;

        int halfX = sizeX / 2;
        int halfZ = sizeZ / 2;
        BlockPos min = anchor.add(-halfX, 0, -halfZ);
        BlockPos max = min.add(sizeX, sizeY, sizeZ);

        boolean valid = structure.validateSpace(world, anchor);

        double eps = 0.0025;
        double minX = min.getX() - eps;
        double minY = min.getY() - eps;
        double minZ = min.getZ() - eps;
        double maxX = max.getX() + eps;
        double maxY = max.getY() + eps;
        double maxZ = max.getZ() + eps;

        // Render the preview volume in world space relative to the camera.
        float partialTicks = event.getPartialTicks();
        double camX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double camY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double camZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.translate(-camX, -camY, -camZ);

        // Hologram color
        long time = world.getTotalWorldTime() % 2000;
        float s = (float)(0.5 + 0.5 * Math.sin(time * 0.02));
        float r = valid ? (0.2f + 0.3f * (1.0f - s)) : (0.8f + 0.2f * s);
        float g = valid ? (0.9f * s + 0.4f * (1.0f - s)) : (0.2f + 0.2f * (1.0f - s));
        float b = valid ? 0.9f : (0.1f + 0.2f * (1.0f - s));

        if (DCPShaders.HOLOGRAM != null) {
            DCPShaders.HOLOGRAM.bind();
            DCPShaders.HOLOGRAM.uploadMatrices();
            DCPShaders.HOLOGRAM.setUniform1f("uTime", (world.getTotalWorldTime() % 100000) / 20.0f);
            DCPShaders.HOLOGRAM.setUniform1f("uValid", valid ? 1.0f : 0.0f);

            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableCull();
            GlStateManager.depthMask(false);

            double inset = 0.0025;
            float faceAlpha = 0.85f;

            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buf = tess.getBuffer();
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

            drawBoxFaces(buf, minX + inset, minY + inset + 0.001, minZ + inset,
                    maxX - inset, maxY - inset, maxZ - inset, r, g, b, faceAlpha);

            // Highlight controller position
            int centerX = min.getX() + halfX;
            int centerZ = min.getZ() + halfZ;
            float hr = valid ? 0.35f : 1.0f;
            float hg = valid ? 1.0f : 0.35f;
            float hb = valid ? 1.0f : 0.25f;
            drawBoxFaces(buf,
                    centerX + 0.002, min.getY() + 0.002, centerZ + 0.002,
                    centerX + 1.0 - 0.002, min.getY() + 1.0 - 0.002, centerZ + 1.0 - 0.002,
                    hr, hg, hb, 0.95f);

            tess.draw();
            DCPShaderHelper.unbind();
            GlStateManager.depthMask(true);
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.enableTexture2D();
        } else {
            // Fallback: simple translucent box without shader
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableCull();
            GlStateManager.depthMask(false);

            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buf = tess.getBuffer();
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            drawBoxFaces(buf, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, 0.3f);
            tess.draw();

            GlStateManager.depthMask(true);
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.enableTexture2D();
        }

        GlStateManager.popMatrix();
    }

    private static DefaultMultiblockControllerBlock getHeldController(ItemStack stack) {
        if (stack.isEmpty()) return null;
        if (stack.getItem() instanceof ItemBlock itemBlock
                && itemBlock.getBlock() instanceof DefaultMultiblockControllerBlock controller) {
            return controller;
        }
        return null;
    }

    private static void drawBoxFaces(BufferBuilder buf, double minX, double minY, double minZ,
                                      double maxX, double maxY, double maxZ,
                                      float r, float g, float b, float a) {
        int ri = (int)(r * 255), gi = (int)(g * 255), bi = (int)(b * 255), ai = (int)(a * 255);
        // Bottom
        buf.pos(minX, minY, minZ).color(ri, gi, bi, ai).endVertex();
        buf.pos(maxX, minY, minZ).color(ri, gi, bi, ai).endVertex();
        buf.pos(maxX, minY, maxZ).color(ri, gi, bi, ai).endVertex();
        buf.pos(minX, minY, maxZ).color(ri, gi, bi, ai).endVertex();
        // Top
        buf.pos(minX, maxY, minZ).color(ri, gi, bi, ai).endVertex();
        buf.pos(minX, maxY, maxZ).color(ri, gi, bi, ai).endVertex();
        buf.pos(maxX, maxY, maxZ).color(ri, gi, bi, ai).endVertex();
        buf.pos(maxX, maxY, minZ).color(ri, gi, bi, ai).endVertex();
        // North
        buf.pos(minX, minY, minZ).color(ri, gi, bi, ai).endVertex();
        buf.pos(minX, maxY, minZ).color(ri, gi, bi, ai).endVertex();
        buf.pos(maxX, maxY, minZ).color(ri, gi, bi, ai).endVertex();
        buf.pos(maxX, minY, minZ).color(ri, gi, bi, ai).endVertex();
        // South
        buf.pos(minX, minY, maxZ).color(ri, gi, bi, ai).endVertex();
        buf.pos(maxX, minY, maxZ).color(ri, gi, bi, ai).endVertex();
        buf.pos(maxX, maxY, maxZ).color(ri, gi, bi, ai).endVertex();
        buf.pos(minX, maxY, maxZ).color(ri, gi, bi, ai).endVertex();
        // West
        buf.pos(minX, minY, minZ).color(ri, gi, bi, ai).endVertex();
        buf.pos(minX, minY, maxZ).color(ri, gi, bi, ai).endVertex();
        buf.pos(minX, maxY, maxZ).color(ri, gi, bi, ai).endVertex();
        buf.pos(minX, maxY, minZ).color(ri, gi, bi, ai).endVertex();
        // East
        buf.pos(maxX, minY, minZ).color(ri, gi, bi, ai).endVertex();
        buf.pos(maxX, maxY, minZ).color(ri, gi, bi, ai).endVertex();
        buf.pos(maxX, maxY, maxZ).color(ri, gi, bi, ai).endVertex();
        buf.pos(maxX, minY, maxZ).color(ri, gi, bi, ai).endVertex();
    }
}
