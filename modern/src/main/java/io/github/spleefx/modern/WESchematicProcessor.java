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

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import io.github.spleefx.SpleefX;
import io.github.spleefx.compatibility.worldedit.SchematicProcessor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class WESchematicProcessor extends SchematicProcessor {

    public WESchematicProcessor() {
    }

    /**
     * Creates a new schematic processor
     *
     * @param plugin Plugin instance
     * @param name   Name of the schematic
     */
    public WESchematicProcessor(SpleefX plugin, String name) {
        super(plugin, name);
    }

    /**
     * Writes the specified clipboard data to the schematic
     *
     * @param player Player to save clipboard for
     */
    @Override
    public void write(Player player) throws EmptyClipboardException {
        BukkitPlayer bPlayer = BukkitAdapter.adapt(player);
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(bPlayer);
        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(schematic))) {
            writer.write(session.getClipboard().getClipboard());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Pastes the specified clipboard at the specified location
     *
     * @param location Location to paste in
     */
    @Override
    public void paste(Location location) {
        try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(location.getWorld()), -1)) {
            Operation operation = new ClipboardHolder(load())
                    .createPaste(editSession)
                    .to(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
                    .ignoreAirBlocks(false)
                    .build();
            Operations.complete(operation);
            editSession.flushSession();
        } catch (WorldEditException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the schematic as a clipboard
     *
     * @return The clipboard of the schematic
     */
    private Clipboard load() {
        ClipboardFormat format = ClipboardFormats.findByFile(schematic);
        try (ClipboardReader reader = format.getReader(new FileInputStream(schematic))) {
            return reader.read();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Creates a new instance of the processor
     *
     * @param plugin Plugin instance
     * @param name   Name of the schematic
     * @return The newly created schematic processor
     */
    @Override
    public SchematicProcessor newInstance(SpleefX plugin, String name) {
        return new WESchematicProcessor(plugin, name);
    }
}