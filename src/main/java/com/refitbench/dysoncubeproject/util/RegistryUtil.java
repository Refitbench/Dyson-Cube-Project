package com.refitbench.dysoncubeproject.util;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Helper for Cleanroom compile-time compatibility.
 * Block/Item/SoundEvent implement IForgeRegistryEntry via ASM at runtime,
 * but the compile-time jar doesn't reflect this.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class RegistryUtil {

    public static <T> T setRegistryName(T entry, String modId, String name) {
        ((IForgeRegistryEntry) entry).setRegistryName(new ResourceLocation(modId, name));
        return entry;
    }

    public static <T> T setRegistryName(T entry, ResourceLocation name) {
        ((IForgeRegistryEntry) entry).setRegistryName(name);
        return entry;
    }

    public static ResourceLocation getRegistryName(Object entry) {
        return ((IForgeRegistryEntry) entry).getRegistryName();
    }

    public static void register(IForgeRegistry registry, Object entry) {
        registry.register((IForgeRegistryEntry) entry);
    }
}
