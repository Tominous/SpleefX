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
package io.github.spleefx.command.sub.base;

import com.google.gson.annotations.Expose;
import io.github.spleefx.arena.ArenaPlayer;
import io.github.spleefx.arena.ArenaStage;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.command.sub.PluginSubcommand;
import io.github.spleefx.extension.ExtensionsManager;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.extension.ItemHolder;
import io.github.spleefx.message.MessageKey;
import io.github.spleefx.util.game.Chat;
import io.github.spleefx.util.item.ItemFactory;
import io.github.spleefx.util.menu.MenuBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.moltenjson.configuration.select.SelectKey;
import org.moltenjson.configuration.select.SelectionHolder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JoinGUISubcommand extends PluginSubcommand {

    public JoinGUISubcommand() {
        super("joingui", (c) -> new Permission("spleefx." + c.getName() + ".joingui", PermissionDefault.TRUE),
                "Display the join GUI", (c) -> "/" + c.getName() + " joingui");
    }

    @Override
    public boolean handle(Command command, CommandSender sender, String[] args) {
        GameExtension extension = ExtensionsManager.getFromCommand(command.getName());
        if (!(sender instanceof Player)) {
            Chat.prefix(sender, extension, MessageKey.NOT_PLAYER.getText());
            return true;
        }
        JoinMenu menu = JoinMenu.MENU.get();
        menu.display((Player) sender, extension);
        return false;
    }

    public static class JoinMenu {

        @SelectKey("menu")
        public static final SelectionHolder<JoinMenu> MENU = new SelectionHolder<>(null);

        /**
         * The menu title
         */
        @Expose
        private String title;

        /**
         * The rows
         */
        @Expose
        private int rows;

        /**
         * The stages that are displayed in the gui
         */
        @Expose
        private List<ArenaStage> stagesToDisplay;

        /**
         * The menu items
         */
        @Expose
        private Map<ArenaStage, ItemHolder> items;

        public void display(Player player, GameExtension mode) {
            MenuBuilder builder = new MenuBuilder(Chat.colorize(title).replace("{player}", player.getName()).replace("{extension}", mode != null ? mode.getDisplayName() : ""), rows);
            int i = 0;
            for (GameArena a : GameArena.ARENAS.get().values()) {
                if (a.getExtension().getKey().equals(mode.getKey())) {
                    if (stagesToDisplay.contains(a.getEngine().getArenaStage())) {
                        builder.item(applyPlaceholders(items.get(a.getEngine().getArenaStage()).factory(), a),
                                i++,
                                (event) -> a.getEngine().join(ArenaPlayer.adapt((Player) event.getWhoClicked())));
                    }
                }
            }
            builder.cancelClickEvents().closeOnClick().build().display(player);
        }

        private static ItemStack applyPlaceholders(ItemFactory item, GameArena arena) {
            ItemMeta current = item.create().getItemMeta();
            item.setName(placeholders(current.getDisplayName(), arena));
            if (current.hasLore())
                item.setLore(current.getLore().stream().map(s -> placeholders(s, arena)).collect(Collectors.toList()));
            return item.create();
        }

        private static String placeholders(String string, GameArena arena) {
            return string.replace("{arena}", arena.getKey())
                    .replace("{arena_displayname}", arena.getDisplayName())
                    .replace("{arena_minimum}", Integer.toString(arena.getMinimum()))
                    .replace("{arena_playercount}", Integer.toString(arena.getEngine().getPlayerTeams().size()))
                    .replace("{arena_maximum}", Integer.toString(arena.getMaximum()))
                    .replace("{arena_stage}", arena.getEngine().getArenaStage().getState())
                    .replace("{extension}", arena.getExtension().getDisplayName());
        }

    }

}