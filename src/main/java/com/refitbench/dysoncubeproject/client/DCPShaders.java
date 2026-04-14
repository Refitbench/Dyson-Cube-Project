package com.refitbench.dysoncubeproject.client;

import org.lwjgl.opengl.GL11;

/**
 * Holds compiled shader programs. Null if loading failed.
 */
public class DCPShaders {
    public static DCPShaderHelper HOLOGRAM;
    public static DCPShaderHelper HOLO_HEX;
    public static DCPShaderHelper DYSON_SUN;
    public static DCPShaderHelper RAIL_ELECTRIC;
    public static DCPShaderHelper RAIL_BEAM;

    public static boolean ANY_AVAILABLE;
    public static boolean RAIL_EFFECTS_AVAILABLE;
    public static boolean USING_LEGACY_PROFILE;

    public static void loadAll() {
        HOLOGRAM = DCPShaderHelper.load("hologram");
        HOLO_HEX = DCPShaderHelper.load("holo_hex");
        DYSON_SUN = DCPShaderHelper.load("dyson_sun");
        RAIL_ELECTRIC = DCPShaderHelper.load("rail_electric");
        RAIL_BEAM = DCPShaderHelper.load("rail_beam");

        ANY_AVAILABLE = HOLOGRAM != null || HOLO_HEX != null || DYSON_SUN != null || RAIL_ELECTRIC != null || RAIL_BEAM != null;
        RAIL_EFFECTS_AVAILABLE = RAIL_ELECTRIC != null && RAIL_BEAM != null;
        USING_LEGACY_PROFILE = usesLegacy(HOLOGRAM) || usesLegacy(HOLO_HEX) || usesLegacy(DYSON_SUN) || usesLegacy(RAIL_ELECTRIC) || usesLegacy(RAIL_BEAM);

        System.out.println("[DysonCubeProject] OpenGL version: " + GL11.glGetString(GL11.GL_VERSION));
        logShader("hologram", HOLOGRAM);
        logShader("holo_hex", HOLO_HEX);
        logShader("dyson_sun", DYSON_SUN);
        logShader("rail_electric", RAIL_ELECTRIC);
        logShader("rail_beam", RAIL_BEAM);
        System.out.println("[DysonCubeProject] Shader summary: any=" + ANY_AVAILABLE
            + ", railEffects=" + RAIL_EFFECTS_AVAILABLE
            + ", legacyProfileUsed=" + USING_LEGACY_PROFILE);
    }

    private static boolean usesLegacy(DCPShaderHelper shader) {
        return shader != null && shader.isLegacyProfile();
    }

    private static void logShader(String name, DCPShaderHelper shader) {
        if (shader == null) {
            System.out.println("[DysonCubeProject] Shader " + name + ": unavailable");
            return;
        }
        System.out.println("[DysonCubeProject] Shader " + name + ": loaded ("
            + (shader.isLegacyProfile() ? "GLSL120 fallback" : "GLSL150") + ")");
    }
}
