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
package io.github.spleefx.modern;

import io.github.spleefx.SpleefX;
import io.github.spleefx.compatibility.worldedit.SchematicProcessor;
import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * Schematic processor for FastAsyncWorldEdit
 */
public class FAWESchematicProcessor extends WESchematicProcessor {

    public FAWESchematicProcessor() {
    }

    /**
     * Creates a new schematic processor
     *
     * @param plugin Plugin instance
     * @param name   Name of the schematic
     */
    public FAWESchematicProcessor(SpleefX plugin, String name) {
        super(plugin, name);
    }

    /**
     * Pastes the specified clipboard at the specified location
     *
     * @param location Location to paste in
     */
    @Override
    public void paste(Location location) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> super.paste(location));
    }

    /**
     * Creates a new instance of the processor
     *
     * @param plugin Plugin instance
     * @param name   Name of the schematic
     * @return The newly created schematic processor
     */
    @Override public SchematicProcessor newInstance(SpleefX plugin, String name) {
        return new FAWESchematicProcessor(plugin, name);
    }
}