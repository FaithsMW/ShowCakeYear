package com.faiths.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class CakeTextureOverrides {
    private static final String MOD_ID = "showcakeyear";
    private static final String TEXTURE_DIRECTORY = "cakes";
    private static final String TEXTURE_PREFIX = "cakes/";
    private static final String TEXTURE_SUFFIX = ".png";
    private static final Set<String> RESERVED_COLORS = Set.of("black", "blue", "default");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type CONFIG_TYPE = new TypeToken<Map<String, String>>() { }.getType();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("showcakeyear.json");
    private static final Map<Integer, ResourceLocation> YEAR_TEXTURE_OVERRIDES = new HashMap<>();
    private static boolean legacyPinkEnabled = false;

    private CakeTextureOverrides() {
    }

    public static ResourceLocation getOverrideForYear(int year) {
        ResourceLocation location = YEAR_TEXTURE_OVERRIDES.get(year);
        if (location == null || !textureExists(location)) {
            return null;
        }
        return location;
    }

    public static void loadFromConfig() {
        YEAR_TEXTURE_OVERRIDES.clear();
        legacyPinkEnabled = false;
        if (!Files.exists(CONFIG_PATH)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (root == null || !root.isJsonObject()) {
                return;
            }

            // Backward compatible: old format was {"69":"purple"}
            if (root.getAsJsonObject().has("overrides")) {
                ConfigData configData = GSON.fromJson(root, ConfigData.class);
                if (configData == null) {
                    return;
                }

                legacyPinkEnabled = configData.legacyPinkEnabled;
                if (configData.overrides != null) {
                    loadOverrides(configData.overrides);
                }
            } else {
                Map<String, String> legacyOverrides = GSON.fromJson(root, CONFIG_TYPE);
                if (legacyOverrides != null) {
                    loadOverrides(legacyOverrides);
                }
            }
        } catch (IOException | JsonParseException ignored) {
        }
    }

    public static OverrideResult setOverride(int year, String color) {
        String normalizedColor = color.toLowerCase(Locale.ROOT);
        if (RESERVED_COLORS.contains(normalizedColor)) {
            return OverrideResult.RESERVED_COLOR;
        }

        ResourceLocation texture = toTextureLocation(normalizedColor);
        if (!textureExists(texture)) {
            return OverrideResult.MISSING_TEXTURE;
        }

        YEAR_TEXTURE_OVERRIDES.put(year, texture);
        saveToConfig();
        return OverrideResult.SUCCESS;
    }

    public static boolean resetOverride(int year) {
        boolean removed = YEAR_TEXTURE_OVERRIDES.remove(year) != null;
        if (removed) {
            saveToConfig();
        }
        return removed;
    }

    public static boolean isLegacyPinkEnabled() {
        return legacyPinkEnabled;
    }

    public static boolean toggleLegacyPinkEnabled() {
        legacyPinkEnabled = !legacyPinkEnabled;
        saveToConfig();
        return legacyPinkEnabled;
    }

    public static List<String> getAvailableCustomColors() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return List.of();
        }

        ResourceManager resourceManager = minecraft.getResourceManager();
        Map<ResourceLocation, Resource> textures = resourceManager.listResources(
                TEXTURE_DIRECTORY,
                location -> location.getNamespace().equals(MOD_ID) && location.getPath().endsWith(TEXTURE_SUFFIX)
        );

        List<String> colors = new ArrayList<>();
        for (ResourceLocation location : textures.keySet()) {
            String color = colorFromLocation(location);
            if (color != null && !RESERVED_COLORS.contains(color)) {
                colors.add(color);
            }
        }

        colors.sort(String::compareTo);
        return colors;
    }

    public static String getReservedColorsDisplay() {
        return "black, blue, default";
    }

    private static void saveToConfig() {
        Map<String, String> overrideEntries = new HashMap<>();
        for (Map.Entry<Integer, ResourceLocation> entry : YEAR_TEXTURE_OVERRIDES.entrySet()) {
            String color = colorFromLocation(entry.getValue());
            if (color != null) {
                overrideEntries.put(String.valueOf(entry.getKey()), color);
            }
        }

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                ConfigData configData = new ConfigData();
                configData.legacyPinkEnabled = legacyPinkEnabled;
                configData.overrides = overrideEntries;
                GSON.toJson(configData, writer);
            }
        } catch (IOException ignored) {
        }
    }

    private static void loadOverrides(Map<String, String> configEntries) {
        for (Map.Entry<String, String> entry : configEntries.entrySet()) {
            int year;
            try {
                year = Integer.parseInt(entry.getKey());
            } catch (NumberFormatException ignored) {
                continue;
            }

            if (year < 1) {
                continue;
            }

            String color = entry.getValue();
            if (color == null) {
                continue;
            }

            String normalizedColor = color.toLowerCase(Locale.ROOT);
            if (RESERVED_COLORS.contains(normalizedColor)) {
                continue;
            }

            YEAR_TEXTURE_OVERRIDES.put(year, toTextureLocation(normalizedColor));
        }
    }

    private static ResourceLocation toTextureLocation(String color) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, TEXTURE_PREFIX + color + TEXTURE_SUFFIX);
    }

    private static boolean textureExists(ResourceLocation location) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return false;
        }

        return minecraft.getResourceManager().getResource(location).isPresent();
    }

    private static String colorFromLocation(ResourceLocation location) {
        String path = location.getPath();
        if (!path.startsWith(TEXTURE_PREFIX) || !path.endsWith(TEXTURE_SUFFIX)) {
            return null;
        }

        return path.substring(TEXTURE_PREFIX.length(), path.length() - TEXTURE_SUFFIX.length()).toLowerCase(Locale.ROOT);
    }

    public enum OverrideResult {
        SUCCESS,
        RESERVED_COLOR,
        MISSING_TEXTURE
    }

    private static class ConfigData {
        boolean legacyPinkEnabled;
        Map<String, String> overrides = new HashMap<>();
    }
}
