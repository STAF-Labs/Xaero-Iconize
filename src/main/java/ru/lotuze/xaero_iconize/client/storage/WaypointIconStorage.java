package ru.lotuze.xaero_iconize.client.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;
import ru.lotuze.xaero_iconize.XaeroIconize;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class WaypointIconStorage {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final Type STORAGE_TYPE =
            new TypeToken<Map<String, String>>() {}.getType();

    private static final Path FILE =
            FMLPaths.GAMEDIR.get()
                    .resolve("xaero_iconize")
                    .resolve("waypoint_icons.json");

    private static final Map<String, String> ICONS =
            new HashMap<>();

    private static boolean loaded = false;

    private WaypointIconStorage() {
    }

    public static void load() {
        if (loaded) {
            return;
        }

        loaded = true;

        if (!Files.exists(FILE)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(FILE)) {
            Map<String, String> loadedIcons =
                    GSON.fromJson(reader, STORAGE_TYPE);

            if (loadedIcons != null) {
                ICONS.clear();
                ICONS.putAll(loadedIcons);
            }

            XaeroIconize.LOGGER.info(
                    "Loaded {} waypoint icons",
                    ICONS.size()
            );
        } catch (Exception exception) {
            XaeroIconize.LOGGER.error(
                    "Failed to load waypoint icons",
                    exception
            );
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());

            try (Writer writer = Files.newBufferedWriter(FILE)) {
                GSON.toJson(ICONS, STORAGE_TYPE, writer);
            }
        } catch (IOException exception) {
            XaeroIconize.LOGGER.error(
                    "Failed to save waypoint icons",
                    exception
            );
        }
    }

    public static void setIcon(
            WaypointIconKey key,
            ResourceLocation itemId
    ) {
        load();

        ICONS.put(
                key.serialize(),
                itemId.toString()
        );

        save();
    }

    public static ResourceLocation getIcon(
            WaypointIconKey key
    ) {
        load();

        String value = ICONS.get(key.serialize());

        if (value == null) {
            return null;
        }

        return ResourceLocation.tryParse(value);
    }

    public static void removeIcon(
            WaypointIconKey key
    ) {
        load();

        if (ICONS.remove(key.serialize()) != null) {
            save();
        }
    }

    public static void moveIcon(
            WaypointIconKey oldKey,
            WaypointIconKey newKey
    ) {
        load();

        String icon = ICONS.remove(oldKey.serialize());

        if (icon == null) {
            return;
        }

        ICONS.put(
                newKey.serialize(),
                icon
        );

        save();
    }
}