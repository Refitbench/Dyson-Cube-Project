package com.refitbench.dysoncubeproject.client.tile;

import com.refitbench.dysoncubeproject.block.tile.RayReceiverTileEntity;
import com.refitbench.dysoncubeproject.Reference;
import com.refitbench.dysoncubeproject.client.DCPExtraModels;
import com.refitbench.dysoncubeproject.client.DCPShaderHelper;
import com.refitbench.dysoncubeproject.client.DCPShaders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import java.io.IOException;

public class RayReceiverRender extends TileEntitySpecialRenderer<RayReceiverTileEntity> {

    @Override
    public void render(RayReceiverTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (te.getWorld() == null) return;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        int light = te.getWorld().getCombinedLight(te.getPos().up(6), 0);
        // If lighting is transitioning (freshly placed), scan upward for a valid value
        if (light == 0) {
            for (int dy = 7; dy <= 16 && light == 0; dy++) {
                light = te.getWorld().getCombinedLight(te.getPos().up(dy), 0);
            }
            if (light == 0) light = 0xF000F0; // full-bright fallback
        }

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
            DCPShaders.HOLO_HEX.setUniform1f("uIsSkyPass", 0.0f);

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
        renderDirectTexturedModel(model, packedLight);
    }

    private void renderDirectTexturedModel(IBakedModel model, int packedLight) {
        java.util.List<BakedQuad> generalQuads = model.getQuads(null, null, 0L);
        if (generalQuads.isEmpty()) return;

        TextureAtlasSprite sprite = generalQuads.get(0).getSprite();
        if (sprite == null || sprite.getIconName() == null) return;

        float lightScale = getLightScale(packedLight);
        boolean atlasFallback = shouldUseAtlasFallback(sprite);
        Minecraft mc = Minecraft.getMinecraft();
        prepareDirectTextureDraw(mc, sprite, packedLight, lightScale, atlasFallback);
        renderQuadsImmediate(generalQuads, sprite, lightScale, atlasFallback);
        for (EnumFacing face : EnumFacing.values()) {
            renderQuadsImmediate(model.getQuads(null, face, 0L), sprite, lightScale, atlasFallback);
        }
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
    }

    private void prepareDirectTextureDraw(Minecraft mc, TextureAtlasSprite sprite, int packedLight, float lightScale, boolean atlasFallback) {
        DCPShaderHelper.unbind();
        GL20.glUseProgram(0);
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.setLightmapTextureCoords(
            OpenGlHelper.lightmapTexUnit,
            (float) (packedLight & 0xFFFF),
            (float) ((packedLight >> 16) & 0xFFFF)
        );
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glLoadIdentity();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(lightScale, lightScale, lightScale, 1.0f);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.color(lightScale, lightScale, lightScale, 1.0f);
        ResourceLocation directTexture = atlasFallback ? null : resolveDirectTexture(mc, sprite);
        mc.getTextureManager().bindTexture(directTexture != null ? directTexture : TextureMap.LOCATION_BLOCKS_TEXTURE);
    }

    private boolean shouldUseAtlasFallback(TextureAtlasSprite sprite) {
        String iconName = sprite.getIconName();
        return iconName == null || "missingno".equals(iconName) || iconName.endsWith(":missingno");
    }

    private ResourceLocation resolveDirectTexture(Minecraft mc, TextureAtlasSprite sprite) {
        String iconName = sprite.getIconName();
        int split = iconName.indexOf(':');
        String namespace = split >= 0 ? iconName.substring(0, split) : Reference.MOD_ID;
        String path = split >= 0 ? iconName.substring(split + 1) : iconName;
        ResourceLocation direct = new ResourceLocation(namespace, "textures/" + path + ".png");
        try {
            mc.getResourceManager().getResource(direct);
            return direct;
        } catch (IOException ignored) {
            return null;
        }
    }

    private void renderQuadsImmediate(java.util.List<BakedQuad> quads, TextureAtlasSprite sprite, float lightScale, boolean atlasFallback) {
        float spanU = sprite.getMaxU() - sprite.getMinU();
        float spanV = sprite.getMaxV() - sprite.getMinV();
        for (BakedQuad quad : quads) {
            int[] vertexData = quad.getVertexData();
            int stride = vertexData.length / 4;
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glColor4f(lightScale, lightScale, lightScale, 1.0f);
            for (int vertex = 0; vertex < 4; vertex++) {
                int base = vertex * stride;
                float px = Float.intBitsToFloat(vertexData[base]);
                float py = Float.intBitsToFloat(vertexData[base + 1]);
                float pz = Float.intBitsToFloat(vertexData[base + 2]);
                float atlasU = Float.intBitsToFloat(vertexData[base + 4]);
                float atlasV = Float.intBitsToFloat(vertexData[base + 5]);
                if (atlasFallback) {
                    GL11.glTexCoord2f(atlasU, atlasV);
                } else {
                    float localU = spanU == 0.0f ? 0.0f : (atlasU - sprite.getMinU()) / spanU;
                    float localV = spanV == 0.0f ? 0.0f : (atlasV - sprite.getMinV()) / spanV;
                    GL11.glTexCoord2f(localU, localV);
                }
                GL11.glVertex3f(px, py, pz);
            }
            GL11.glEnd();
        }
    }

    private float getLightScale(int packedLight) {
        int sky = (packedLight >> 20) & 0xF;
        int block = (packedLight >> 4) & 0xF;
        float level = Math.max(sky, block) / 15.0f;
        return Math.max(0.2f, level);
    }

    @Override
    public boolean isGlobalRenderer(RayReceiverTileEntity te) {
        return true;
    }
}
