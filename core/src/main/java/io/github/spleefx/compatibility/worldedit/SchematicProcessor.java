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
package io.github.spleefx.compatibility.worldedit;

import com.sk89q.worldedit.EmptyClipboardException;
import io.github.spleefx.SpleefX;
import io.github.spleefx.compatibility.CompatibilityHandler;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

/**
 * An abstract class for processing schematics across different WorldEdit versions
 */
public abstract class SchematicProcessor {

    /**
     * Represents the schematic file
     */
    protected File schematic;

    /**
     * Plugin instance
     */
    protected SpleefX plugin;

    /**
     * Accessed reflectively by {@link CompatibilityHandler}
     */
    protected SchematicProcessor() {
    }

    /**
     * Creates a new schematic processor
     *
     * @param plugin Plugin instance
     * @param name   Name of the schematic
     */
    public SchematicProcessor(SpleefX plugin, String name) {
        this.plugin = plugin;
        File dataDirectory = plugin.getArenasFolder();
        schematic = new File(dataDirectory, name + ".schem");
        try {
            schematic.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes the specified clipboard data to the schematic
     *
     * @param player Player to save clipboard for
     */
    public abstract void write(Player player) throws EmptyClipboardException;

    /**
     * Pastes the specified clipboard at the specified location
     *
     * @param location Location to paste in
     */
    public abstract void paste(Location location);

    /**
     * Creates a new instance of the processor
     *
     * @param plugin Plugin instance
     * @param name   Name of the schematic
     * @return The newly created schematic processor
     */
    public abstract SchematicProcessor newInstance(SpleefX plugin, String name);

}
