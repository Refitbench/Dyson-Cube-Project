package com.refitbench.dysoncubeproject.client.tile;

import com.refitbench.dysoncubeproject.block.EMRailEjectorControllerBlock;
import com.refitbench.dysoncubeproject.block.tile.EMRailEjectorTileEntity;
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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class EMRailEjectorRender extends TileEntitySpecialRenderer<EMRailEjectorTileEntity> {

    @Override
    public void render(EMRailEjectorTileEntity entity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (entity.getWorld() == null) return;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        int light = entity.getWorld().getCombinedLight(entity.getPos().up(3), 0);

        // Render the base model
        renderBakedModel(DCPExtraModels.EM_RAILEJECTOR_BASE, light);

        // Move to gun mount position
        GlStateManager.translate(0, 2.5, 0);
        GlStateManager.rotate(-90, 0, 1, 0);
        GlStateManager.translate(1, 0, 0);
        GlStateManager.rotate(-90, 0, 1, 0);
        GlStateManager.rotate(90, 0, 0, 1);

        // Aim gun by current yaw/pitch (rotate around offset pivot)
        GlStateManager.translate(0, 0.5f, 0.5f);
        GlStateManager.rotate(360 - entity.getCurrentYaw(), 1, 0, 0);
        GlStateManager.translate(0, -0.5f, -0.5f);

        GlStateManager.translate(0, 0.5f, 0.5f);
        GlStateManager.rotate(360 - entity.getCurrentPitch(), 0, 0, 1);
        GlStateManager.translate(0, -0.5f, -0.5f);

        // Render the gun model
        renderBakedModel(DCPExtraModels.EM_RAILEJECTOR_GUN, light);

        // Animation parameters
        long gameTime = entity.getWorld().getTotalWorldTime();
        float period = entity.getMaxProgress();
        float shootWindow = 28f;
        float chargeWindow = 85f;
        float t = entity.getProgress();

        // CHARGING ANIMATION - electric arcs around muzzle
        if (t >= period - chargeWindow && DCPShaders.RAIL_ELECTRIC != null) {
            float chargeT = (t - (period - chargeWindow)) / chargeWindow;
            float intensity = (float) Math.pow(chargeT, 3.0);

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.12, 0.45, 0.5);

            DCPShaders.RAIL_ELECTRIC.bind();
            DCPShaders.RAIL_ELECTRIC.uploadMatrices();
            DCPShaders.RAIL_ELECTRIC.setUniform1f("uTime", (gameTime + partialTicks) / 20.0f);
            DCPShaders.RAIL_ELECTRIC.setUniform1f("uIntensity", intensity);

            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableCull();
            GlStateManager.depthMask(false);

            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buf = tess.getBuffer();
            buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

            int segments = 7;
            float baseRadius = 0.62f + 0.05f * (float) Math.sin((gameTime + partialTicks) * 0.2f);
            float jitter = 0.05f;
            Random rng = new Random(gameTime);

            for (int ring = 0; ring < (int)(38 * chargeT); ring++) {
                float ringOffsetX = 0.05f * ring;
                for (int i = 0; i < segments; i++) {
                    float seed = i * 17.0f + ring * 31.0f + rng.nextFloat() * 6f;
                    float ang = (float) Math.toRadians((i * (360f / segments)) + (float) Math.sin((gameTime + partialTicks + seed) * 0.6f) * 20f);
                    float ang2 = ang + (float) Math.toRadians(10 + (float) Math.sin((gameTime + partialTicks + seed * 1.37f) * 0.9f) * 12f);
                    float rad1 = baseRadius + (float) Math.sin((gameTime + partialTicks + seed) * 0.8f) * jitter;
                    float rad2 = baseRadius + 0.07f + (float) Math.sin((gameTime + partialTicks + seed * 0.77f) * 0.8f) * jitter;

                    float y1 = (float) (Math.cos(ang) * rad1);
                    float z1 = (float) (Math.sin(ang) * rad1);
                    float y2 = (float) (Math.cos(ang2) * rad2);
                    float z2 = (float) (Math.sin(ang2) * rad2);

                    float expand = 0.04f * intensity;
                    y1 *= (1.0f + expand);
                    z1 *= (1.0f + expand);
                    y2 *= (1.0f + expand);
                    z2 *= (1.0f + expand);

                    int a = Math.min(255, 60 + (int)(195 * intensity));
                    buf.pos(0.0f + ringOffsetX, y1, z1).color(100, 200, 255, a).endVertex();
                    buf.pos(0.12f + ringOffsetX, y2, z2).color(100, 200, 255, a).endVertex();
                }
            }

            tess.draw();
            DCPShaderHelper.unbind();
            GlStateManager.depthMask(true);
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.enableTexture2D();

            GlStateManager.popMatrix();
        }

        // AFTER SHOOTING ANIMATION
        float timeSinceShot = gameTime - entity.getLastExecution();
        float progress = timeSinceShot / shootWindow;

        // Rail beam
        if (timeSinceShot > 0 && timeSinceShot < shootWindow && DCPShaders.RAIL_BEAM != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.12, 0.45, 0.5);

            float beamIntensity = 1.2f * (1.0f - progress);

            DCPShaders.RAIL_BEAM.bind();
            DCPShaders.RAIL_BEAM.uploadMatrices();
            DCPShaders.RAIL_BEAM.setUniform1f("uTime", (gameTime + partialTicks) / 20.0f);
            DCPShaders.RAIL_BEAM.setUniform1f("uIntensity", beamIntensity);

            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableCull();
            GlStateManager.depthMask(false);

            float beamLen = 160.0f * (2.0f - progress * 2);
            float halfW = 0.10f + 0.06f * (1.0f - progress);

            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buf = tess.getBuffer();
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

            // Vertical ribbon
            buf.pos(0.0f, -halfW, 0.0f).color(230, 255, 255, 255).endVertex();
            buf.pos(beamLen, -halfW, 0.0f).color(230, 255, 255, 255).endVertex();
            buf.pos(beamLen, halfW, 0.0f).color(230, 255, 255, 255).endVertex();
            buf.pos(0.0f, halfW, 0.0f).color(230, 255, 255, 255).endVertex();

            // Horizontal ribbon
            buf.pos(0.0f, 0.0f, -halfW).color(230, 255, 255, 255).endVertex();
            buf.pos(beamLen, 0.0f, -halfW).color(230, 255, 255, 255).endVertex();
            buf.pos(beamLen, 0.0f, halfW).color(230, 255, 255, 255).endVertex();
            buf.pos(0.0f, 0.0f, halfW).color(230, 255, 255, 255).endVertex();

            tess.draw();
            DCPShaderHelper.unbind();
            GlStateManager.depthMask(true);
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.enableTexture2D();

            GlStateManager.popMatrix();
        }

        // Shockwave ring at muzzle
        float shockDur = 6.0f;
        if (timeSinceShot > 0 && timeSinceShot < shockDur && DCPShaders.RAIL_ELECTRIC != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.12, 0.45, 0.5);

            DCPShaders.RAIL_ELECTRIC.bind();
            DCPShaders.RAIL_ELECTRIC.uploadMatrices();
            DCPShaders.RAIL_ELECTRIC.setUniform1f("uTime", (gameTime + partialTicks) / 20.0f);
            DCPShaders.RAIL_ELECTRIC.setUniform1f("uIntensity", 1.0f);

            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.disableCull();
            GlStateManager.depthMask(false);

            float radius = 0.2f + 0.9f * (timeSinceShot / shockDur);
            int segs = 32;

            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buf = tess.getBuffer();
            buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

            for (int depth = 0; depth < 7; depth++) {
                for (int i = 0; i < segs; i++) {
                    double a0 = (Math.PI * 2 * i) / segs;
                    double a1 = (Math.PI * 2 * (i + 1)) / segs;
                    float py0 = (float) (Math.cos(a0) * radius);
                    float pz0 = (float) (Math.sin(a0) * radius);
                    float py1 = (float) (Math.cos(a1) * radius);
                    float pz1 = (float) (Math.sin(a1) * radius);
                    buf.pos(depth * 0.5f, py0, pz0).color(255, 255, 255, 255).endVertex();
                    buf.pos(depth * 0.5f, py1, pz1).color(255, 255, 255, 255).endVertex();
                }
            }

            tess.draw();
            DCPShaderHelper.unbind();
            GlStateManager.depthMask(true);
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.enableTexture2D();

            GlStateManager.popMatrix();
        }

        // Projectile
        if (timeSinceShot > 0 && timeSinceShot < shootWindow && DCPExtraModels.EM_RAILEJECTOR_PROJECTILE != null) {
            float distance = 0.5f + progress * 1000f;
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.75, -0.1, 0);
            GlStateManager.translate(distance, 0, 0);

            renderBakedModel(DCPExtraModels.EM_RAILEJECTOR_PROJECTILE, light);

            // Additive glow quads
            if (DCPShaders.RAIL_BEAM != null) {
                DCPShaders.RAIL_BEAM.bind();
                DCPShaders.RAIL_BEAM.uploadMatrices();
                DCPShaders.RAIL_BEAM.setUniform1f("uTime", (gameTime + partialTicks) / 20.0f);
                DCPShaders.RAIL_BEAM.setUniform1f("uIntensity", 1.2f);

                GlStateManager.disableTexture2D();
                GlStateManager.enableBlend();
                GlStateManager.disableCull();
                GlStateManager.depthMask(false);

                float s = 0.18f;
                Tessellator tess = Tessellator.getInstance();
                BufferBuilder buf = tess.getBuffer();
                buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

                buf.pos(-s, -s, 0.0f).color(230, 255, 255, 255).endVertex();
                buf.pos(s, -s, 0.0f).color(230, 255, 255, 255).endVertex();
                buf.pos(s, s, 0.0f).color(230, 255, 255, 255).endVertex();
                buf.pos(-s, s, 0.0f).color(230, 255, 255, 255).endVertex();

                buf.pos(-s, 0.0f, -s).color(230, 255, 255, 255).endVertex();
                buf.pos(s, 0.0f, -s).color(230, 255, 255, 255).endVertex();
                buf.pos(s, 0.0f, s).color(230, 255, 255, 255).endVertex();
                buf.pos(-s, 0.0f, s).color(230, 255, 255, 255).endVertex();

                tess.draw();
                DCPShaderHelper.unbind();
                GlStateManager.depthMask(true);
                GlStateManager.enableCull();
                GlStateManager.disableBlend();
                GlStateManager.enableTexture2D();
            }

            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
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
    public boolean isGlobalRenderer(EMRailEjectorTileEntity te) {
        return true;
    }
}
