package com.refitbench.dysoncubeproject.client;

import com.refitbench.dysoncubeproject.DysonCubeProject;
import com.refitbench.dysoncubeproject.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;

/**
 * Manages OpenGL shader programs for custom rendering effects.
 * In 1.12.2 there's no ShaderInstance; we compile and link raw GLSL.
 */
public class DCPShaderHelper {

    private final int programId;
    private final boolean legacyProfile;

    private DCPShaderHelper(int programId, boolean legacyProfile) {
        this.programId = programId;
        this.legacyProfile = legacyProfile;
    }

    public static DCPShaderHelper load(String name) {
        try {
            String basePath = "assets/" + Reference.MOD_ID + "/shaders/core/";
            String vshSource = readResource(basePath + name + ".vsh");
            String fshSource = readResource(basePath + name + ".fsh");

            int vsh;
            int fsh;
            boolean legacyProfile = false;

            try {
                vsh = compileShader(GL20.GL_VERTEX_SHADER, vshSource, name + ".vsh");
                fsh = compileShader(GL20.GL_FRAGMENT_SHADER, fshSource, name + ".fsh");
            } catch (RuntimeException primaryError) {
                String vLegacy = toLegacyVertexSource(vshSource);
                String fLegacy = toLegacyFragmentSource(fshSource);
                if (vLegacy.equals(vshSource) || fLegacy.equals(fshSource)) {
                    throw primaryError;
                }

                DysonCubeProject.LOGGER.warn("Falling back to GLSL 120 profile for shader '{}'.", name);
                DysonCubeProject.LOGGER.warn("Primary compile error: {}", primaryError.getMessage());
                vsh = compileShader(GL20.GL_VERTEX_SHADER, vLegacy, name + ".vsh [legacy120]");
                fsh = compileShader(GL20.GL_FRAGMENT_SHADER, fLegacy, name + ".fsh [legacy120]");
                legacyProfile = true;
            }

            int program = GL20.glCreateProgram();
            GL20.glAttachShader(program, vsh);
            GL20.glAttachShader(program, fsh);

            // No glBindAttribLocation needed -- shaders use gl_Vertex/gl_Color
            // built-ins which receive data from MC 1.12's fixed-function
            // glVertexPointer/glColorPointer calls via the compatibility profile.

            GL20.glLinkProgram(program);
            if (getProgramStatus(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
                String log = GL20.glGetProgramInfoLog(program, 8192);
                GL20.glDeleteProgram(program);
                GL20.glDeleteShader(vsh);
                GL20.glDeleteShader(fsh);
                DysonCubeProject.LOGGER.error("Shader link failed for {}: {}", name, log);
                return null;
            }

            // Shaders can be detached after linking
            GL20.glDetachShader(program, vsh);
            GL20.glDetachShader(program, fsh);
            GL20.glDeleteShader(vsh);
            GL20.glDeleteShader(fsh);

            return new DCPShaderHelper(program, legacyProfile);
        } catch (Exception e) {
            DysonCubeProject.LOGGER.error("Failed to load shader: {}", name, e);
            return null;
        }
    }

    private static String toLegacyVertexSource(String source) {
        String legacy = source.replace("#version 150 compatibility", "#version 120");
        legacy = legacy.replace("#version 150", "#version 120");
        // gl_Vertex and gl_Color are built-in in GLSL 120, no changes needed for those
        legacy = legacy.replaceAll("(?m)^\\s*out\\s+", "varying ");
        return legacy;
    }

    private static String toLegacyFragmentSource(String source) {
        String legacy = source.replace("#version 150 compatibility", "#version 120");
        legacy = legacy.replace("#version 150", "#version 120");
        legacy = legacy.replaceAll("(?m)^\\s*in\\s+", "varying ");
        legacy = legacy.replaceAll("(?m)^\\s*out\\s+vec4\\s+fragColor\\s*;\\s*\\n?", "");
        legacy = legacy.replace("fragColor", "gl_FragColor");
        return legacy;
    }

    private static int compileShader(int type, String source, String name) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        if (getShaderStatus(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            String log = GL20.glGetShaderInfoLog(shader, 8192);
            GL20.glDeleteShader(shader);
            throw new RuntimeException("Shader compile error in " + name + ": " + log);
        }
        return shader;
    }

    private static int getProgramStatus(int program, int pname) {
        IntBuffer buf = BufferUtils.createIntBuffer(1);
        GL20.glGetProgram(program, pname, buf);
        return buf.get(0);
    }

    private static int getShaderStatus(int shader, int pname) {
        IntBuffer buf = BufferUtils.createIntBuffer(1);
        GL20.glGetShader(shader, pname, buf);
        return buf.get(0);
    }

    private static String readResource(String path) throws Exception {
        try (InputStream is = Minecraft.getMinecraft().getResourceManager()
                .getResource(new ResourceLocation(Reference.MOD_ID,
                        "shaders/core/" + path.substring(path.lastIndexOf('/') + 1)))
                .getInputStream()) {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        }
    }

    public void bind() {
        GL20.glUseProgram(programId);
    }

    public static void unbind() {
        GL20.glUseProgram(0);
    }

    public int getUniformLocation(String name) {
        return GL20.glGetUniformLocation(programId, name);
    }

    public void setUniform1f(String name, float value) {
        int loc = getUniformLocation(name);
        if (loc >= 0) GL20.glUniform1f(loc, value);
    }

    public void setUniform3f(String name, float x, float y, float z) {
        int loc = getUniformLocation(name);
        if (loc >= 0) GL20.glUniform3f(loc, x, y, z);
    }

    public void setUniformMatrix4(String name, FloatBuffer matrix) {
        int loc = getUniformLocation(name);
        if (loc >= 0) GL20.glUniformMatrix4(loc, false, matrix);
    }

    /**
     * Uploads the current GL ModelView and Projection matrices as uniforms.
     */
    public void uploadMatrices() {
        FloatBuffer buf = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buf);
        setUniformMatrix4("ModelViewMat", buf);
        buf.clear();
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, buf);
        setUniformMatrix4("ProjMat", buf);
    }

    public int getProgramId() {
        return programId;
    }

    public boolean isLegacyProfile() {
        return legacyProfile;
    }

    public void delete() {
        GL20.glDeleteProgram(programId);
    }
}
