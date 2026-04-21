package com.refitbench.dysoncubeproject.client.compat;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SideOnly(Side.CLIENT)
public final class AdvancedRocketryCompat {

    private static final String MOD_ID = "advancedrocketry";
    private static boolean lookupAttempted;
    private static Method getCurrentConfigMethod;
    private static Field skyOverrideField;

    private AdvancedRocketryCompat() {
    }

    public static boolean shouldUseRoundSkySunHolo() {
        if (!Loader.isModLoaded(MOD_ID)) {
            return false;
        }

        if (!resolveHooks()) {
            return false;
        }

        try {
            Object config = getCurrentConfigMethod.invoke(null);
            return config != null && skyOverrideField.getBoolean(config);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }

    private static boolean resolveHooks() {
        if (lookupAttempted) {
            return getCurrentConfigMethod != null && skyOverrideField != null;
        }

        lookupAttempted = true;
        try {
            Class<?> configClass = Class.forName("zmaster587.advancedRocketry.api.ARConfiguration");
            getCurrentConfigMethod = configClass.getMethod("getCurrentConfig");
            skyOverrideField = configClass.getField("skyOverride");
            return true;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            getCurrentConfigMethod = null;
            skyOverrideField = null;
            return false;
        }
    }
}