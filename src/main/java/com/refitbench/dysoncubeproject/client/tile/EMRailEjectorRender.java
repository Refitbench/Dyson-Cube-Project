package com.refitbench.dysoncubeproject.client.tile;

import com.refitbench.dysoncubeproject.Reference;
import com.refitbench.dysoncubeproject.block.tile.EMRailEjectorTileEntity;
import com.refitbench.dysoncubeproject.client.DCPExtraModels;
import com.refitbench.dysoncubeproject.client.DCPShaderHelper;
import com.refitbench.dysoncubeproject.client.DCPShaders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import java.util.Random;
import java.util.List;
import java.io.IOException;

public class EMRailEjectorRender extends TileEntitySpecialRenderer<EMRailEjectorTileEntity> {

    @Override
    public void render(EMRailEjectorTileEntity entity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (entity.getWorld() == null) return;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        int light = entity.getWorld().getCombinedLight(entity.getPos().up(3), 0);
        // If lighting is transitioning (freshly placed), scan upward for a valid value
        if (light == 0) {
            for (int dy = 4; dy <= 16 && light == 0; dy++) {
                light = entity.getWorld().getCombinedLight(entity.getPos().up(dy), 0);
            }
            if (light == 0) light = 0xF000F0; // full-bright fallback
        }

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

        // CHARGING ANIMATION
        if (t >= period - chargeWindow) {
            float chargeT = (t - (period - chargeWindow)) / chargeWindow; // 0..1
            float intensity = (float) Math.pow(chargeT, 3.0); // ramp up

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.12, 0.45, 0.5);

            boolean useShader = DCPShaders.RAIL_ELECTRIC != null;
            if (useShader) {
                DCPShaders.RAIL_ELECTRIC.bind();
                DCPShaders.RAIL_ELECTRIC.uploadMatrices();
                DCPShaders.RAIL_ELECTRIC.setUniform1f("uTime", (gameTime + partialTicks) / 20.0f);
                DCPShaders.RAIL_ELECTRIC.setUniform1f("uIntensity", intensity);
            }

            // PORT NOTE: Upstream RenderType uses TRANSLUCENT_TRANSPARENCY (SRC_ALPHA, ONE_MINUS_SRC_ALPHA).
            // The shader outputs HDR-range colors (glow up to 4.0) which makes additive unnecessary upstream.
            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableCull();
            GlStateManager.depthMask(false);

            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buf = tess.getBuffer();
            // PORT NOTE: Upstream RenderType "railElectricLines" actually uses VertexFormat.Mode.QUADS.
            // Each pair of 2-vertex segments forms one quad in the continuous vertex stream.
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

            int segments = 7;
            float baseRadius = 0.62f + 0.05f * (float) Math.sin((gameTime + partialTicks) * 0.2f);
            float jitter = 0.05f;
            int r = 100;
            int g = 200;
            int b = 255;
            int a = Math.min(255, 60 + (int) (195 * intensity));

            // PORT NOTE: Upstream uses entity.getLevel().getRandom() (world random source).
            // In 1.12, entity.getWorld().rand is the equivalent.
            Random rng = entity.getWorld().rand;

            // Draw multiple short jittery segments forming rough arcs around the barrel (YZ plane circle)
            for (int ring = 0; ring < (int) (38 * chargeT); ring++) {
                float ringOffsetX = 0.05f * ring; // along barrel
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

                    // Slight expansion as it charges
                    float expand = 0.04f * intensity;
                    y1 *= (1.0f + expand);
                    z1 *= (1.0f + expand);
                    y2 *= (1.0f + expand);
                    z2 *= (1.0f + expand);

                    buf.pos(0.0f + ringOffsetX, y1, z1).color(r, g, b, a).endVertex();
                    buf.pos(0.12f + ringOffsetX, y2, z2).color(r, g, b, a).endVertex();
                }
            }

            tess.draw();

            if (useShader) DCPShaderHelper.unbind();
            GlStateManager.depthMask(true);
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.enableTexture2D();

            GlStateManager.popMatrix();
        }

        // AFTER SHOOTING ANIMATION
        float timeSinceShot = gameTime - entity.getLastExecution();
        float progress = timeSinceShot / shootWindow;

        // Rail beam
        if (timeSinceShot > 0 && timeSinceShot < shootWindow) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.12, 0.45, 0.5);

            float beamIntensity = 1.2f * (1.0f - progress);
            boolean useShader = DCPShaders.RAIL_BEAM != null;

            if (useShader) {
                DCPShaders.RAIL_BEAM.bind();
                DCPShaders.RAIL_BEAM.uploadMatrices();
                DCPShaders.RAIL_BEAM.setUniform1f("uTime", (gameTime + partialTicks) / 20.0f);
                DCPShaders.RAIL_BEAM.setUniform1f("uIntensity", beamIntensity);
            }

            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            if (!useShader) {
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            }
            GlStateManager.disableCull();
            GlStateManager.depthMask(false);

            float beamLen = 160.0f;
            float halfW = 0.10f + 0.06f * (1.0f - progress);
            int beamAlpha = Math.max(0, (int) (255 * (1.0f - progress)));

            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buf = tess.getBuffer();
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

            // Vertical ribbon
            buf.pos(0.0f, -halfW, 0.0f).color(230, 255, 255, beamAlpha).endVertex();
            buf.pos(beamLen, -halfW, 0.0f).color(230, 255, 255, beamAlpha).endVertex();
            buf.pos(beamLen, halfW, 0.0f).color(230, 255, 255, beamAlpha).endVertex();
            buf.pos(0.0f, halfW, 0.0f).color(230, 255, 255, beamAlpha).endVertex();

            // Horizontal ribbon
            buf.pos(0.0f, 0.0f, -halfW).color(230, 255, 255, beamAlpha).endVertex();
            buf.pos(beamLen, 0.0f, -halfW).color(230, 255, 255, beamAlpha).endVertex();
            buf.pos(beamLen, 0.0f, halfW).color(230, 255, 255, beamAlpha).endVertex();
            buf.pos(0.0f, 0.0f, halfW).color(230, 255, 255, beamAlpha).endVertex();

            tess.draw();
            if (useShader) DCPShaderHelper.unbind();
            GlStateManager.depthMask(true);
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.enableTexture2D();

            GlStateManager.popMatrix();
        }

        // Shockwave ring at muzzle
        float shockDur = 6.0f;
        if (timeSinceShot > 0 && timeSinceShot < shockDur) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.12, 0.45, 0.5);
            boolean useShader = DCPShaders.RAIL_ELECTRIC != null;

            if (useShader) {
                DCPShaders.RAIL_ELECTRIC.bind();
                DCPShaders.RAIL_ELECTRIC.uploadMatrices();
                DCPShaders.RAIL_ELECTRIC.setUniform1f("uTime", (gameTime + partialTicks) / 20.0f);
                DCPShaders.RAIL_ELECTRIC.setUniform1f("uIntensity", 1.0f);
            }

            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            if (!useShader) {
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
                GL11.glLineWidth(2.0f);
            }
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
            if (useShader) DCPShaderHelper.unbind();
            if (!useShader) GL11.glLineWidth(1.0f);
            GlStateManager.depthMask(true);
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
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
            {
                boolean useShader = DCPShaders.RAIL_BEAM != null;
                if (useShader) {
                    DCPShaders.RAIL_BEAM.bind();
                    DCPShaders.RAIL_BEAM.uploadMatrices();
                    DCPShaders.RAIL_BEAM.setUniform1f("uTime", (gameTime + partialTicks) / 20.0f);
                    DCPShaders.RAIL_BEAM.setUniform1f("uIntensity", 1.2f);
                }

                GlStateManager.disableTexture2D();
                GlStateManager.disableLighting();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
                if (!useShader) {
                    GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
                }
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
                if (useShader) DCPShaderHelper.unbind();
                GlStateManager.depthMask(true);
                GlStateManager.enableCull();
                GlStateManager.disableBlend();
                GlStateManager.enableLighting();
                GlStateManager.enableTexture2D();
            }

            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
    }

    private void renderBakedModel(IBakedModel model, int packedLight) {
        if (model == null) return;
        renderDirectTexturedModel(model, packedLight);
    }

    private void renderDirectTexturedModel(IBakedModel model, int packedLight) {
        List<BakedQuad> generalQuads = model.getQuads(null, null, 0L);
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
        GlStateManager.color(1f, 1f, 1f, 1f);
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
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
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

    private void renderQuadsImmediate(List<BakedQuad> quads, TextureAtlasSprite sprite, float lightScale, boolean atlasFallback) {
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
    public boolean isGlobalRenderer(EMRailEjectorTileEntity te) {
        return true;
    }
}
