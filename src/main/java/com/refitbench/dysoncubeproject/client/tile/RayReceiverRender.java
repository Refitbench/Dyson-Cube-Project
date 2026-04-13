package com.refitbench.dysoncubeproject.client.tile;

import com.refitbench.dysoncubeproject.block.tile.RayReceiverTileEntity;
import com.refitbench.dysoncubeproject.client.DCPExtraModels;
import com.refitbench.dysoncubeproject.client.DCPShaderHelper;
import com.refitbench.dysoncubeproject.client.DCPShaders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;

public class RayReceiverRender extends TileEntitySpecialRenderer<RayReceiverTileEntity> {

    @Override
    public void render(RayReceiverTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (te.getWorld() == null) return;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        int light = te.getWorld().getCombinedLight(te.getPos().up(6), 0);

        // Base
        renderBakedModel(DCPExtraModels.RAY_RECEIVER_BASE, light);

        // Plate (elevated)
        GlStateManager.translate(0, 2, 0);
        renderBakedModel(DCPExtraModels.RAY_RECEIVER_PLATE, light);

        // Lens stands
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 2, 1);
        GlStateManager.rotate(90, 0, 1, 0);
        renderBakedModel(DCPExtraModels.RAY_RECEIVER_LENS_STANDS, light);

        // Lens with pitch rotation (rotate around pivot)
        GlStateManager.translate(0, 0.55f, 0.5f);
        GlStateManager.rotate(-90, 1, 0, 0);
        GlStateManager.translate(0, -0.55f, -0.5f);

        GlStateManager.translate(0, 0.55f, 0.5f);
        GlStateManager.rotate(360 - te.getCurrentPitch() - 180, 1, 0, 0);
        GlStateManager.translate(0, -0.55f, -0.5f);

        renderBakedModel(DCPExtraModels.RAY_RECEIVER_LENS, light);

        GlStateManager.popMatrix();

        // Holo hex overlay on top face and side column
        if (DCPShaders.HOLO_HEX != null) {
            Minecraft mc = Minecraft.getMinecraft();
            long gameTime = mc.world.getTotalWorldTime();

            DCPShaders.HOLO_HEX.bind();
            DCPShaders.HOLO_HEX.uploadMatrices();
            DCPShaders.HOLO_HEX.setUniform1f("uTime", (gameTime % 100000) / 20.0f);
            DCPShaders.HOLO_HEX.setUniform1f("uValid", 1.0f);
            DCPShaders.HOLO_HEX.setUniform1f("uSize", 0.75f);

            Entity rv = mc.getRenderViewEntity();
            if (rv != null) {
                DCPShaders.HOLO_HEX.setUniform3f("uCamPos",
                    (float) rv.posX, (float) rv.posY, (float) rv.posZ);
            }

            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableCull();
            GlStateManager.depthMask(false);

            float r = 0.5f, g = 0.9f, b = 0.9f;

            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buf = tess.getBuffer();
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

            // Top face overlay
            drawBoxTopFace(buf, -1, 0, -1, 2, 0.3, 2, r, g, b, 0.85f);

            // Side column overlay
            drawBoxSideFace(buf, 0.2499, 0.5, 0.2499, 0.751, 1.75, 0.751, r, g, b, 0.25f);

            tess.draw();
            DCPShaderHelper.unbind();
            GlStateManager.depthMask(true);
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.enableTexture2D();
        }

        GlStateManager.popMatrix();
    }

    private void drawBoxTopFace(BufferBuilder buf, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float r, float g, float b, float a) {
        int ri = (int)(r * 255), gi = (int)(g * 255), bi = (int)(b * 255), ai = (int)(a * 255);
        buf.pos(minX, maxY, minZ).color(ri, gi, bi, ai).endVertex();
        buf.pos(minX, maxY, maxZ).color(ri, gi, bi, ai).endVertex();
        buf.pos(maxX, maxY, maxZ).color(ri, gi, bi, ai).endVertex();
        buf.pos(maxX, maxY, minZ).color(ri, gi, bi, ai).endVertex();
    }

    private void drawBoxSideFace(BufferBuilder buf, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float r, float g, float b, float a) {
        int ri = (int)(r * 255), gi = (int)(g * 255), bi = (int)(b * 255), ai = (int)(a * 255);
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

    private void renderBakedModel(IBakedModel model, int packedLight) {
        if (model == null) return;
        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.disableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        mc.getTextureManager().bindTexture(net.minecraft.client.renderer.texture.TextureMap.LOCATION_BLOCKS_TEXTURE);
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        for (net.minecraft.client.renderer.block.model.BakedQuad quad : model.getQuads(null, null, 0L)) {
            buf.addVertexData(quad.getVertexData());
            buf.putBrightness4(packedLight, packedLight, packedLight, packedLight);
        }
        for (EnumFacing face : EnumFacing.values()) {
            for (net.minecraft.client.renderer.block.model.BakedQuad quad : model.getQuads(null, face, 0L)) {
                buf.addVertexData(quad.getVertexData());
                buf.putBrightness4(packedLight, packedLight, packedLight, packedLight);
            }
        }
        tess.draw();
        GlStateManager.enableLighting();
    }

    @Override
    public boolean isGlobalRenderer(RayReceiverTileEntity te) {
        return true;
    }
}
