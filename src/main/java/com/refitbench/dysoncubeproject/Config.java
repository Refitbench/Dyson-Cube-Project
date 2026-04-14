package com.refitbench.dysoncubeproject;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class Config {

    public static int MAX_SOLAR_PANELS = 50_000_000;
    public static int BEAM_TO_SOLAR_PANEL_RATIO = 6;
    public static int POWER_PER_SAIL = 20;
    public static boolean SHOW_AT_MAX_PROGRESS = false;
    public static int RAY_RECEIVER_EXTRACT_POWER = 50_000_000;
    public static int RAY_RECEIVER_POWER_BUFFER = 100_000_000;
    public static int RAIL_EJECTOR_POWER_BUFFER = 400_000;
    public static int RAIL_EJECTOR_CONSUME = 40;
        public static boolean RAIL_EJECTOR_REQUIRE_POWER = true;

    public static void load(File configFile) {
        var config = new Configuration(configFile);
        config.load();

        var category = config.getCategory("general");

        MAX_SOLAR_PANELS = config.getInt("MAX_SOLAR_PANELS", "general", 50_000_000, 1, Integer.MAX_VALUE,
                "The maximum number of solar panels the Dyson Sphere can have");
        BEAM_TO_SOLAR_PANEL_RATIO = config.getInt("BEAM_TO_SOLAR_PANEL_RATIO", "general", 6, 1, Integer.MAX_VALUE,
                "How many solar panels each beam can support");
        POWER_PER_SAIL = config.getInt("POWER_PER_SAIL", "general", 20, 0, Integer.MAX_VALUE,
                "The amount of power generated per sail");
        SHOW_AT_MAX_PROGRESS = config.getBoolean("SHOW_AT_MAX_PROGRESS", "general", false,
                "Always show sphere at max progress");
        RAY_RECEIVER_EXTRACT_POWER = config.getInt("RAY_RECEIVER_EXTRACT_POWER", "general", 50_000_000, 1, Integer.MAX_VALUE,
                "The power that the ray receiver can extract from the sphere every tick");
        RAY_RECEIVER_POWER_BUFFER = config.getInt("RAY_RECEIVER_POWER_BUFFER", "general", 100_000_000, 1, Integer.MAX_VALUE,
                "The power that the ray receiver buffer has");
        RAIL_EJECTOR_POWER_BUFFER = config.getInt("RAIL_EJECTOR_POWER_BUFFER", "general", 400_000, 1, Integer.MAX_VALUE,
                "The power that the em railejector buffer has");
        RAIL_EJECTOR_CONSUME = config.getInt("RAIL_EJECTOR_CONSUME", "general", 40, 1, Integer.MAX_VALUE,
                "The power that the em railejector consumes each tick per sent item");
        RAIL_EJECTOR_REQUIRE_POWER = config.getBoolean("RAIL_EJECTOR_REQUIRE_POWER", "general", true,
                "Require FE power for the EM Rail Ejector to operate. If false, base speed can run without power.");

        if (config.hasChanged()) {
            config.save();
        }
    }
}
