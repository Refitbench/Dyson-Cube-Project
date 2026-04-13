package com.refitbench.dysoncubeproject.client;

import com.refitbench.dysoncubeproject.Reference;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;

public class DCPExtraModels {

    public static IBakedModel EM_RAILEJECTOR_GUN;
    public static IBakedModel EM_RAILEJECTOR_BASE;
    public static IBakedModel EM_RAILEJECTOR_PROJECTILE;

    public static IBakedModel RAY_RECEIVER_BASE;
    public static IBakedModel RAY_RECEIVER_PLATE;
    public static IBakedModel RAY_RECEIVER_LENS;
    public static IBakedModel RAY_RECEIVER_LENS_STANDS;

    public static void onModelBake(ModelBakeEvent event) {
        rebakeAll();
    }

    public static void rebakeAll() {
        EM_RAILEJECTOR_BASE = bake("block/em_railejector_base");
        EM_RAILEJECTOR_GUN = bake("block/em_railejector_gun");
        EM_RAILEJECTOR_PROJECTILE = bake("block/em_railejector_projectile");
        RAY_RECEIVER_BASE = bake("block/ray_receiver_base");
        RAY_RECEIVER_PLATE = bake("block/ray_receiver_plate");
        RAY_RECEIVER_LENS = bake("block/ray_receiver_lens");
        RAY_RECEIVER_LENS_STANDS = bake("block/ray_receiver_lens_stands");
    }

    private static IBakedModel bake(String path) {
        try {
            IModel model = ModelLoaderRegistry.getModel(new ResourceLocation(Reference.MOD_ID, path));
            return model.bake(
                model.getDefaultState(),
                net.minecraft.client.renderer.vertex.DefaultVertexFormats.BLOCK,
                ModelLoader.defaultTextureGetter()
            );
        } catch (Exception e) {
            System.err.println("[DysonCubeProject] Failed to bake model: " + path);
            e.printStackTrace();
            return null;
        }
    }
}
