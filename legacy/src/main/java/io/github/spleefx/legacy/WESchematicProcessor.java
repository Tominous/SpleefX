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
package io.github.spleefx.legacy;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.io.Closer;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.World;
import io.github.spleefx.SpleefX;
import io.github.spleefx.compatibility.worldedit.SchematicProcessor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.BufferedOutputStream;
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
    private WESchematicProcessor(SpleefX plugin, String name) {
        super(plugin, name);
    }

    /**
     * Writes the specified clipboard data to the schematic
     *
     * @param player Player to save clipboard for
     */
    @Override
    public void write(Player player) throws EmptyClipboardException {
        try (Closer closer = Closer.create()) {
            com.sk89q.worldedit.entity.Player localPlayer = plugin.getWorldEdit().wrapPlayer(player);
            LocalSession localSession = plugin.getWorldEdit().getWorldEdit().getSessionManager().get(localPlayer);
            ClipboardHolder selection = localSession.getClipboard();
            FileOutputStream fos = closer.register(new FileOutputStream(schematic));
            BufferedOutputStream bos = closer.register(new BufferedOutputStream(fos));
            ClipboardWriter writer = closer.register(ClipboardFormat.SCHEMATIC.getWriter(bos));
            writer.write(selection.getClipboard(), selection.getWorldData());
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
        try {
            EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession((World) new BukkitWorld(location.getWorld()), -1);
            editSession.enableQueue();

            SchematicFormat schematic = SchematicFormat.getFormat(this.schematic);
            CuboidClipboard cuboidClipboard = schematic.load(this.schematic);

            cuboidClipboard.paste(editSession, BukkitUtil.toVector(location), false);
            editSession.flushQueue();
        } catch (DataException | MaxChangedBlocksException | IOException e) {
            e.printStackTrace();
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
