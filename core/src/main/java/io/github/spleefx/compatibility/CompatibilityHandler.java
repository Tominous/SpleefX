/*
 * * Copyright 2020 github.com/ReflxctionDev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.spleefx.compatibility;

import io.github.spleefx.SpleefX;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.arena.bow.BowSpleefArena;
import io.github.spleefx.arena.splegg.SpleggArena;
import io.github.spleefx.compatibility.material.MaterialCompatibility;
import io.github.spleefx.compatibility.worldedit.SchematicProcessor;
import io.github.spleefx.compatibility.worldedit.WGExtraFlagsHook;
import io.github.spleefx.compatibility.worldedit.WorldGuardHook;
import io.github.spleefx.util.plugin.Protocol;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.BlockIterator;

import java.util.function.Supplier;

/**
 * A simple utility for handling compatibility across versions
 */
public class CompatibilityHandler {

    /**
     * The default package domain
     */
    private static final String PACKAGE_DOMAIN = "io.github.spleefx.";

    /**
     * Whether should the plugin auto disable itself
     */
    private static boolean disable = false;

    /**
     * Whether does WorldEdit exist or not
     */
    private static boolean hasWorldEdit = Bukkit.getPluginManager().getPlugin("WorldEdit") != null;

    /**
     * The color factory for materials
     */
    private static MaterialCompatibility materialCompatibility;

    /**
     * The color factory for materials
     */
    private static ProtocolNMS protocolNMS;

    /**
     * WorldGuard hook handler
     */
    private static WorldGuardHook worldGuardHook = WorldGuardHook.FALLBACK;

    /**
     * The schematic processor instance, used for creating instances using {@link SchematicProcessor#newInstance(SpleefX, String)}
     */
    private SchematicProcessor schematicProcessor;

    /**
     * Main plugin instance
     */
    private SpleefX plugin;

    /**
     * Creates a new compatibility handler
     *
     * @param plugin Main plugin instance
     */
    public CompatibilityHandler(SpleefX plugin) {
        this.plugin = plugin;

        if (Protocol.getCurrentProtocol() < 8) {
            disable = true;
            return;
        }

        protocolNMS = create(Protocol.VERSION + ".ProtocolNMSImpl", () -> ProtocolNMS.FALLBACK);
        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuardExtraFlags"))
            worldGuardHook = new WGExtraFlagsHook();

        if (Protocol.isOlderThan(12)) {
            schematicProcessor = create("legacy.WESchematicProcessor", () -> null);
            materialCompatibility = create("legacy.MaterialCompatibilityImpl", () -> null);
            if (worldGuardHook == null && Bukkit.getPluginManager().isPluginEnabled("WorldGuard"))
                worldGuardHook = create("legacy.DefaultWorldGuardHook", () -> WorldGuardHook.FALLBACK);
        } else { // 1.13+
            if (Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit"))
                schematicProcessor = create("modern.FAWESchematicProcessor", () -> null);
            else
                schematicProcessor = create("modern.WESchematicProcessor", () -> null);
            if (worldGuardHook == null && Bukkit.getPluginManager().isPluginEnabled("WorldGuard"))
                worldGuardHook = create("modern.DefaultWorldGuardHook", () -> WorldGuardHook.FALLBACK);
            materialCompatibility = create("modern.MaterialCompatibilityImpl", () -> null);
        }
    }

    /**
     * {@inheritDoc}
     */
    public SchematicProcessor create(String name) {
        return schematicProcessor.newInstance(plugin, name);
    }

    /**
     * Returns the material compatibility controller
     *
     * @return The material compatibility
     */
    public static MaterialCompatibility getMaterialCompatibility() {
        return materialCompatibility;
    }

    /**
     * Returns the title manager
     *
     * @return The title manager
     */
    public static ProtocolNMS getProtocol() {
        return protocolNMS;
    }

    /**
     * Creates a new instance of the specified class, or returns the fallback if no
     * instance can be created.
     *
     * @param className Class to create
     * @param fallback  Fallback in case creation fails
     * @param <T>       The object type
     * @return The created object or the fallback
     */
    @SuppressWarnings("unchecked")
    public static <T> T create(String className, Supplier<T> fallback) {
        try {
            return (T) Class.forName(PACKAGE_DOMAIN + className).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            return fallback.get();
        }
    }

    /**
     * Returns whether should the plugin disable itself
     *
     * @return ^
     */
    public static boolean shouldDisable() {
        return disable;
    }

    /**
     * Returns whether WorldEdit exists or not
     *
     * @return ^
     */
    public static boolean hasWorldEdit() {
        return hasWorldEdit;
    }

    public static WorldGuardHook getWorldGuardHook() {
        return worldGuardHook;
    }

    /**
     * Returns the first object if available, or the second if the first does not exist
     * <p>
     * Used to provide compatibility for newer and legacy methods
     *
     * @param a   First object
     * @param b   Second object in case the first fails
     * @param <R> The object returned
     * @return The appropriate object
     */
    public static <R> R either(Supplier<R> a, Supplier<R> b) {
        try {
            return a.get();
        } catch (NoSuchMethodError | NoSuchFieldError | IllegalArgumentException /* For enums */ e) {
            return b.get();
        }
    }

    /**
     * Returns the hit block from {@link ProjectileHitEvent}
     *
     * @param arena Arena to get from
     * @param event Event to get from
     * @return The hit block, or null if it hit an entity
     */
    public static Block getHitBlock(GameArena arena, ProjectileHitEvent event) {
        if (getHitEntity(arena, event) != null) return null; // No block, an entity was hit
        return either(event::getHitBlock, () -> {
            BlockIterator iterator = new BlockIterator(event.getEntity().getWorld(), event.getEntity().getLocation().toVector(), event.getEntity().getVelocity().normalize(), 0.0D, 4);
            Block hitBlock = null;
            while (iterator.hasNext()) {
                hitBlock = iterator.next();
                if (hitBlock.getType() != Material.AIR) {
                    break;
                }
            }
            return hitBlock;
        });
    }

    /**
     * Returns the hit entity from {@link ProjectileHitEvent}
     *
     * @param arena Arena to get from
     * @param event Event to get from
     * @return The entity hit by the projectile, or null if it did not hit any
     */
    public static Entity getHitEntity(GameArena arena, ProjectileHitEvent event) {
        if (arena == null) return null;
        return either(event::getHitEntity, () -> {
            if (arena instanceof SpleggArena)
                return ((SpleggArena) arena).getDamageMap().get(event.getEntity().getUniqueId());
            else if (arena instanceof BowSpleefArena)
                return ((BowSpleefArena) arena).getDamageMap().get(event.getEntity().getUniqueId());
            else return null;
        });
    }
}