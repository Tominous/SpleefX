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

import io.github.spleefx.SpleefX;
import io.github.spleefx.gui.ArenaSettingsGUI;
import io.github.spleefx.arena.ModeType;
import io.github.spleefx.arena.api.ArenaData;
import io.github.spleefx.arena.api.ArenaType;
import io.github.spleefx.arena.api.FFAManager;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.arena.spleef.SpleefArena;
import io.github.spleefx.command.sub.PluginSubcommand;
import io.github.spleefx.extension.ExtensionsManager;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.listeners.RenameListener;
import io.github.spleefx.message.MessageKey;
import io.github.spleefx.team.GameTeam;
import io.github.spleefx.team.TeamColor;
import io.github.spleefx.util.game.Chat;
import io.github.spleefx.util.game.Metas;
import io.github.spleefx.util.io.CopyStore;
import io.github.spleefx.util.item.Items;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.permissions.Permission;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("unchecked")
public class ArenaSubcommand<T extends GameArena> extends PluginSubcommand {

    private static final List<String> HELP =
            Arrays.asList(
                    "&earena &ccreate &a<arena key> &3[ffa | teams] &b<arena display name> &7- &dCreate a map with a unique key and a display name",
                    "&earena &cremove &a<arena key> &7- &dRemove an arena",
                    "&earena &cspawnpoint &a<arena key> &b<team> &7- &dSet the spawnpoint of a specific team",
                    "&earena &csettings &a<arena key> &7- &dOpen the arena settings GUI",
                    "&earena &clobby &a<arena key> &7- &dSet the arena lobby (waiting area)",
                    "&earena &cremovelobby &a<arena key> &7- &dRemove the arena lobby",
                    "&earena &cregenerate &a<arena key> &7- &dRegenerate the arena"
            );

    private static final List<String> TEAMS = Arrays.stream(TeamColor.values()).filter(TeamColor::isUsable).map(c -> c.name().toLowerCase()).collect(Collectors.toList());

    public static final List<String> ARGS_1 = Arrays.asList("create", "lobby", "remove", "removelobby", "settings", "spawnpoint", "regenerate");

    public static final List<String> TYPES = Arrays.asList("ffa", "teams");

    private static final List<String> SETTINGS = Arrays.asList("deathLevel", "teams", "displayName", "membersPerTeam", "gameTime", "minimum", "maxPlayerCount");

    private ModeType type;

    private ArenaFactory<T> arenaFactory;

    private Permission permission;

    public ArenaSubcommand(ModeType type, ArenaFactory<T> arenaFactory) {
        super("arena", null, "Control arenas", (c) -> "/" + c.getName() + " arena <create | remove | teams | spawnpoint | displayname | settings> <arena> [args...]");
        permission = new Permission("spleefx.arena." + type.name().toLowerCase());
        super.permission = (c) -> permission;
        super.helpMenu = HELP;
        this.type = type;
        this.arenaFactory = arenaFactory;
    }

    /**
     * Returns a list of tabs for this subcommand.
     *
     * @param args Command arguments. Does <i>NOT</i> contain this subcommand.
     * @return A list of all tabs.
     */
    @Override
    public List<String> onTab(CommandSender sender, Command command, String[] args) {
        if (!sender.hasPermission(getPermission(command))) return Collections.emptyList();
        GameExtension e = ExtensionsManager.getFromCommand(command.getName());
        switch (args.length) {
            case 0:
                return Collections.emptyList();
            case 1:
                return ARGS_1.stream().filter(a -> a.startsWith(args[0])).collect(Collectors.toList());
            case 2:
                if (args[0].equalsIgnoreCase("create"))
                    return Collections.emptyList();
                return GameArena.ARENAS.get().values().stream().filter(gameArena -> gameArena.type == type && gameArena.getKey().startsWith(args[1]) &&
                        e.getKey().equals(gameArena.getExtension().getKey())).map(ArenaData::getKey).collect(Collectors.toList());
            case 3:
                GameArena arena = GameArena.getByKey(args[1]);
                if (arena == null && !args[0].equalsIgnoreCase("create"))
                    return Collections.emptyList();
                switch (args[0].toLowerCase()) {
                    case "spawnpoint":
                        if (arena.getArenaType() == ArenaType.TEAMS)
                            return arena.getTeams().stream().map(team -> team.getName().toLowerCase()).collect(Collectors.toList());
                        return IntStream.rangeClosed(1, arena.getMaxPlayerCount()).mapToObj(Integer::toString).collect(Collectors.toCollection(LinkedList::new));
                    case "create":
                        return TYPES.stream().filter(s -> s.startsWith(args[2])).collect(Collectors.toList());
                    case "settings":
                        return SETTINGS.stream().filter(s -> s.startsWith(args[2])).collect(Collectors.toList());
                }
            default:
                if (args[0].toLowerCase().equals("settings")) {
                    GameArena gameArena = GameArena.getByKey(args[1]);
                    if (args[2].equalsIgnoreCase("teams")) {
                        if (gameArena.getArenaType() == ArenaType.FREE_FOR_ALL) return Collections.emptyList();
                        List<String> teams = new ArrayList<>(TEAMS);
                        teams.removeIf(t -> gameArena.getTeams().contains(TeamColor.get(t)));
                        return teams.stream().filter(s -> s.startsWith(args[args.length - 1])).collect(Collectors.toList());
                    }
                    return Collections.emptyList();
                }
        }
        return Collections.emptyList();
    }

    /**
     * Handles the command input
     *
     * @param sender Command sender
     * @param args   Extra command arguments
     * @return {@code true} if the command succeed, {@code false} if it is desired to send {@link #getHelpMenu()}.
     */
    @Override
    public boolean handle(Command command, CommandSender sender, String[] args) {
        GameExtension ex = ExtensionsManager.getFromCommand(command.getName());
        switch (args.length) {
            case 0:
            case 1:
                return false;
            case 2:
                switch (args[0]) {
                    case "remove":
                        try {
                            T removed = (T) SpleefX.getPlugin().getArenaManager().removeArena(args[1]);
                            if (removed == null)
                                Chat.prefix(sender, ex, MessageKey.INVALID_ARENA.getText().replace("{arena}", args[1]));
                            else
                                MessageKey.ARENA_DELETED.send(sender, removed, null, null, null, command.getName(),
                                        null, -1, ex);
                        } catch (IllegalStateException e) {
                            Chat.plugin(sender, "&c" + e.getMessage());
                        }
                        return true;
                    case "lobby": {
                        if (checkSender(sender)) {
                            MessageKey.NOT_PLAYER.send(sender, null, null, null, null, command.getName(),
                                    null, -1, ex);
                            return true;
                        }
                        T arena = (T) GameArena.getByKey(args[1]);
                        if (arena == null) {
                            Chat.prefix(sender, ex, MessageKey.INVALID_ARENA.getText().replace("{arena}", args[1]));
                            return true;
                        }
                        Location old = ((Player) sender).getLocation();
                        Location lobby = arena.setLobby(new Location(old.getWorld(), old.getBlockX() + 0.5, old.getBlockY(), old.getBlockZ() + 0.5, old.getYaw(), old.getPitch()));
                        MessageKey.LOBBY_SET.send(sender, arena, null, lobby, null, command.getName(),
                                null, -1, ex);
                        return true;
                    }
                    case "removelobby": {
                        T arena = (T) GameArena.getByKey(args[1]);
                        if (arena == null) {
                            Chat.prefix(sender, ex, MessageKey.INVALID_ARENA.getText().replace("{arena}", args[1]));
                            return true;
                        }
                        arena.setLobby(null);
                        Chat.prefix(sender, ex, "&aLobby for arena &e" + arena.getKey() + " &ahas been removed.");
                    }
                    return true;
                    case "regenerate":
                    case "regen": {
                        T arena = (T) GameArena.getByKey(args[1]);
                        if (arena == null) {
                            Chat.prefix(sender, ex, MessageKey.INVALID_ARENA.getText().replace("{arena}", args[1]));
                            return true;
                        }
                        Chat.prefix(sender, arena, "&eRegenerating...");
                        arena.getEngine().regenerate();
                        Chat.prefix(sender, arena, "&aArena &e" + arena.getKey() + " &ahas been regenerated.");
                    }
                    return true;
                    case "settings":
                        if (checkSender(sender)) {
                            MessageKey.NOT_PLAYER.send(sender, null, null, null, null, command.getName(),
                                    null, -1, ex);
                            return true;
                        }
                        T arena = (T) GameArena.getByKey(args[1]);
                        if (arena == null) {
                            Chat.prefix(sender, ex, MessageKey.INVALID_ARENA.getText().replace("{arena}", args[1]));
                            return true;
                        }
                        Metas.set(((Player) sender), "spleefx.editing", new FixedMetadataValue(SpleefX.getPlugin(), arena));
                        new ArenaSettingsGUI(arena, ((Player) sender));
                        return true;
                    default:
                        return false;
                }
            case 3:
                switch (args[0]) {
                    case "settings":
                        if (args[2].equalsIgnoreCase("toggle")) {
                            T arena = (T) GameArena.getByKey(args[1]);
                            if (arena == null) {
                                Chat.prefix(sender, ex, MessageKey.INVALID_ARENA.getText().replace("{arena}", args[1]));
                                return true;
                            }
                            arena.setEnabled(!arena.isEnabled());
                            Chat.prefix(sender, arena, String.format((arena.isEnabled() ? "&aArena &e%s &ahas been enabled" : "&cArena &e%s &ahas been disabled"), arena.getKey()));
                        }
                        return false;
                    case "create": {
                        if (checkSender(sender)) {
                            MessageKey.NOT_PLAYER.send(sender, null, null, null, null, command.getName(),
                                    null, -1, ex);
                            return true;
                        }
                        try {
                            T arena = (T) GameArena.getByKey(args[1]);
                            if (arena != null) { // An arena with that key already exists
                                MessageKey.ARENA_ALREADY_EXISTS.send(sender, arena, null, null, null, command.getName(),
                                        null, -1, ex);
                                return true;
                            }
                            SpleefX.getPlugin().getArenaManager().add((Player) sender, arenaFactory.create(args[1], args[2], CopyStore.LOCATIONS.get(sender), ArenaType.TEAMS, ex), command.getName());
                        } catch (ClassCastException e) {
                            Chat.plugin(sender, "&cThe specified arena is not a " + type.name().replace("_", " ").toLowerCase() + " arena.");
                        }
                        return true;
                    }
                    case "spawnpoint": {
                        if (checkSender(sender)) {
                            MessageKey.NOT_PLAYER.send(sender, null, null, null, null, command.getName(),
                                    null, -1, ex);
                            return true;
                        }
                        T arena = (T) GameArena.getByKey(args[1]);
                        if (arena == null) { // An arena with that key already exists
                            Chat.prefix(sender, ex, MessageKey.INVALID_ARENA.getText().replace("{arena}", args[1]));
                            return true;
                        }
                        if (arena.getArenaType() == ArenaType.FREE_FOR_ALL) {
                            try {
                                FFAManager m = arena.getFFAManager();
                                int index = Integer.parseInt(args[2]);
                                if (index > arena.getMaxPlayerCount()) {
                                    Chat.plugin(sender, String.format("&cValue &e%s &cis greater than the arena's maximum count (&e%s&c)", index, arena.getMaxPlayerCount()));
                                    return true;
                                }
                                Location old = ((Player) sender).getLocation();
                                Location spawn = new Location(old.getWorld(), old.getBlockX() + 0.5, old.getBlockY(), old.getBlockZ() + 0.5, old.getYaw(), old.getPitch());
                                m.registerSpawnpoint(index, spawn);
                                Chat.prefix(sender, arena, "&aSpawnpoint for index &e" + index + String.format(" &ahas been set to &e%s&a, &e%s&a, &e%s&a.", spawn.getX(), spawn.getY(), spawn.getZ()));
                            } catch (NumberFormatException e) {
                                Chat.prefix(sender, arena, "&cInvalid number: &e" + args[2]);
                            }
                        } else {
                            TeamColor color = TeamColor.get(args[2]);
                            if (color == TeamColor.INVALID) {
                                Chat.plugin(sender, "&cInvalid color: &e" + args[2]);
                                return true;
                            }
                            if (!arena.getTeams().contains(color)) {
                                MessageKey.TEAM_NOT_REGISTERED.send(sender, arena, color, null, null, command.getName(),
                                        null, -1, ex);
                                return true;
                            }
                            Location old = ((Player) sender).getLocation();
                            Location spawn = new Location(old.getWorld(), old.getBlockX() + 0.5, old.getBlockY(), old.getBlockZ() + 0.5, old.getYaw(), old.getPitch());
                            arena.registerSpawnPoint(color, spawn);
                            MessageKey.SPAWNPOINT_SET.send(sender, arena, color, spawn, null, command.getName(),
                                    null, -1, ex);
                        }
                        return true;
                    }
                }
                return false;
            default: // 4+
                if (args[0].equalsIgnoreCase("create")) {
                    if (checkSender(sender)) {
                        MessageKey.NOT_PLAYER.send(sender, null, null, null, null, command.getName(),
                                null, -1, ex);
                        return true;
                    }
                    GameArena arena = GameArena.getByKey(args[1]);
                    if (arena != null) { // An arena with that key already exists
                        MessageKey.ARENA_ALREADY_EXISTS.send(sender, arena, null, null, null, command.getName(),
                                null, -1, ex);
                        return true;
                    }
                    @SuppressWarnings("SuspiciousMethodCalls")
                    T newArena = arenaFactory.create(args[1], combine(args, 3), CopyStore.LOCATIONS.get(sender), ArenaType.lookup(args[2]), ex);
                    if (newArena.getArenaType() == ArenaType.FREE_FOR_ALL)
                        newArena.setMaxPlayerCount(2);
                    SpleefX.getPlugin().getArenaManager().add((Player) sender, newArena, command.getName());
                    return true;
                }

                if (args[0].equalsIgnoreCase("settings")) {
                    T arena = (T) GameArena.getByKey(args[1]);
                    if (arena == null) {
                        Chat.prefix(sender, ex, MessageKey.INVALID_ARENA.getText().replace("{arena}", args[1]));
                        return true;
                    }
                    switch (args[2].toLowerCase()) {
                        case "displayname":
                            arena.setDisplayName(combine(args, 3));
                            Chat.prefix(sender, arena, "&aArena &e" + arena.getKey() + "&a's display name has been set to &d" + arena.getDisplayName());
                            return true;
                        case "teams":
                            if (arena.getArenaType() == ArenaType.FREE_FOR_ALL) {
                                Chat.prefix(sender, arena, "&cYou cannot add teams to FFA arenas!");
                                return true;
                            }
                            List<TeamColor> add = Arrays.stream(Arrays.copyOfRange(args, 3, args.length)).map(TeamColor::get).collect(Collectors.toList());
                            Optional<TeamColor> invalid = add.stream().filter(team -> team == TeamColor.INVALID).findFirst();
                            if (invalid.isPresent()) {
                                Chat.prefix(sender, arena, "&cA team (or more) was invalid.");
                                return true;
                            }
                            add.removeIf(arena.getTeams()::contains);
                            arena.getTeams().addAll(add);
                            add.forEach(team -> arena.gameTeams.add(new GameTeam(team, new ArrayList<>())));
                            Chat.prefix(sender, arena, "&aSuccessfully added teams: " + PluginSubcommand.joinNiceString(add.toArray()));
                            return true;
                        case "membersperteam":
                            parseThen(arena, sender, args[3], 1, (e, v) -> {
                                e.setMembersPerTeam(v);
                                Chat.prefix(sender, arena, "&aArena &e" + arena.getKey() + "&a's members per team count has been set to &e" + v);
                            });
                            return true;
                        case "gametime":
                            parseThen(arena, sender, args[3], 1, (e, v) -> {
                                arena.setGameTime(v);
                                Chat.prefix(sender, arena, "&aArena &e" + arena.getKey() + "&a's game time has been set to &e" + v);
                            });
                            return true;
                        case "deathlevel":
                            parseThen(arena, sender, args[3], 1, (e, v) -> {
                                arena.setDeathLevel(v);
                                Chat.prefix(sender, arena, "&aArena &e" + arena.getKey() + "&a's death level has been set to &e" + v);
                            });
                            return true;
                        case "minimum":
                            parseThen(arena, sender, args[3], 2, (e, v) -> {
                                arena.setMinimum(v);
                                Chat.prefix(sender, arena, "&aArena &e" + arena.getKey() + "&a's minimum players required has been set to &e" + v);
                            });
                            return true;
                        case "maxplayercount":
                            if (arena.getArenaType() != ArenaType.FREE_FOR_ALL)
                                return false;
                            parseThen(arena, sender, args[3], 2, (e, v) -> {
                                arena.setMaxPlayerCount(v);
                                Chat.prefix(sender, arena, "&aArena &e" + arena.getKey() + "&a's max player count has been set to &e" + v);
                            });
                            return true;
                    }
                }
                break;
        }
        return false;
    }

    protected static void controlTeam(TeamColor team, int slot, ItemStack clicked, Inventory inventory, GameArena arena) {
        if (arena.getArenaType() != ArenaType.TEAMS) return;
        if (clicked.getType() == Items.ADD_TEAM.getType()) {
            inventory.setItem(slot, Items.REMOVE_TEAM);
            arena.addTeam(team);
            arena.gameTeams.add(new GameTeam(team, new ArrayList<>()));
        } else {
            inventory.setItem(slot, Items.ADD_TEAM);
            arena.removeTeam(team);
            arena.gameTeams.removeIf(gameTeam -> gameTeam.getColor() == team);
        }
    }

    protected static boolean toggle(int slot, ItemStack clicked, Inventory inventory) {
        if (clicked.getType() == Items.ENABLE.getType()) {
            inventory.setItem(slot, Items.DISABLE);
            return true;
        } else {
            inventory.setItem(slot, Items.ENABLE);
            return false;
        }
    }

    protected static int increase(int original, InventoryClickEvent event) {
        int add = event.getClick() == ClickType.MIDDLE ? 5 : 1;
        int c = original + add;
        if (event.getCurrentItem() != null) {
            event.getCurrentItem().setAmount(c);
            event.getInventory().getItem(event.getRawSlot() + 18).setAmount(c);
        }
        return c;
    }

    protected static int decrease(int original, InventoryClickEvent event, int minimum) {
        int v = event.getClick() == ClickType.MIDDLE ? 5 : 1;
        int c = original;
        if (c - v <= minimum) {
            event.getCurrentItem().setAmount(minimum);
            event.getClickedInventory().getItem(event.getRawSlot() - 18).setAmount(minimum);
            return minimum;
        }
        c -= v;
        if (event.getCurrentItem() != null) {
            event.getCurrentItem().setAmount(c);
            event.getClickedInventory().getItem(event.getRawSlot() - 18).setAmount(c);
        }
        return c;
    }

    protected static void value(GameArena arena, HumanEntity entity, int e) {
        Chat.prefix(entity, arena, "&eCurrent value: &a" + e);
    }

    protected void parseThen(GameArena arena, CommandSender sender, String toParse, int minimum, BiConsumer<GameArena, Integer> then) {
        try {
            int v = Integer.parseInt(toParse);
            if (v < minimum) {
                Chat.prefix(sender, arena, "&cInvalid value, must be at least " + minimum + " (found &e" + v + "&c)");
                return;
            }
            then.accept(arena, v);
        } catch (NumberFormatException e) {
            Chat.prefix(sender, arena, "&cInvalid number: &e" + toParse);
        }
    }

    public static class MenuListener implements Listener {

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (event.getView().getTitle().endsWith(" - Settings"))
                if (event.getWhoClicked().hasMetadata("spleefx.editing")) {
                    event.setCancelled(true);
                    if (event.getCurrentItem() == null) return;
                    GameArena arena = Metas.get(event.getWhoClicked(), "spleefx.editing");
                    if (arena == null) return;
                    switch (event.getRawSlot()) {
                        case 9:
                            controlTeam(TeamColor.RED, 9, event.getCurrentItem(), event.getClickedInventory(), arena);
                            break;
                        case 10:
                            controlTeam(TeamColor.GREEN, 10, event.getCurrentItem(), event.getClickedInventory(), arena);
                            break;
                        case 11:
                            controlTeam(TeamColor.BLUE, 11, event.getCurrentItem(), event.getClickedInventory(), arena);
                            break;
                        case 12:
                            controlTeam(TeamColor.YELLOW, 12, event.getCurrentItem(), event.getClickedInventory(), arena);
                            break;
                        case 13:
                            controlTeam(TeamColor.PINK, 13, event.getCurrentItem(), event.getClickedInventory(), arena);
                            break;
                        case 14:
                            controlTeam(TeamColor.GRAY, 14, event.getCurrentItem(), event.getClickedInventory(), arena);
                            break;
                        case 16:
                            event.getWhoClicked().closeInventory();
                            Metas.set(event.getWhoClicked(), "spleefx.renaming", new FixedMetadataValue(SpleefX.getPlugin(), arena));
                            Chat.plugin(event.getWhoClicked(), "&eType the new &ddisplay name &eof arena &d" + arena.getKey());
                            Chat.plugin(event.getWhoClicked(), "&eTo cancel, type &d" + RenameListener.CANCEL + "&e.");
                            break;

                        case 27:
                            arena.setDeathLevel(increase(arena.getDeathLevel(), event));
                            break;
                        case 28:
                            if (arena.getArenaType() == ArenaType.TEAMS)
                                arena.setMembersPerTeam(increase(arena.getMembersPerTeam(), event));
                            else
                                arena.setMaxPlayerCount(increase(arena.getMaxPlayerCount(), event));
                            break;
                        case 29:
                            arena.setGameTime(increase(arena.getGameTime(), event));
                            break;
                        case 30:
                            arena.setMinimum(increase(arena.getMinimum(), event));
                            break;
                        case 36:
                            value(arena, event.getWhoClicked(), arena.getDeathLevel());
                            break;
                        case 37:
                            value(arena, event.getWhoClicked(), arena.getArenaType() == ArenaType.TEAMS ? arena.getMembersPerTeam() : arena.getMaxPlayerCount());
                            break;
                        case 38:
                            value(arena, event.getWhoClicked(), arena.getGameTime());
                            break;
                        case 39:
                            value(arena, event.getWhoClicked(), arena.getMinimum());
                            break;
/*

                        case 42:
                            arena.setPowerups(toggle(event.getRawSlot(), event.getCurrentItem(), event.getClickedInventory()));
                            break;
*/

                        case 42:
                            arena.setDropMinedBlocks(toggle(event.getRawSlot(), event.getCurrentItem(), event.getClickedInventory()));
                            break;

                        case 43: {
                            if (arena.type == ModeType.SPLEEF)
                                ((SpleefArena) arena).setMelt(toggle(event.getRawSlot(), event.getCurrentItem(), event.getClickedInventory()));
                        }
                        break;

                        case 45:
                            arena.setDeathLevel(decrease(arena.getDeathLevel(), event, 1));
                            break;
                        case 46:
                            if (arena.getArenaType() == ArenaType.TEAMS)
                                arena.setMembersPerTeam(decrease(arena.getMembersPerTeam(), event, 1));
                            else
                                arena.setMaxPlayerCount(decrease(arena.getMaxPlayerCount(), event, 2));
                            break;
                        case 47:
                            arena.setGameTime(decrease(arena.getGameTime(), event, 1));
                            break;
                        case 48:
                            arena.setMinimum(decrease(arena.getMinimum(), event, 2));
                            break;

                        case 53:
                            event.getWhoClicked().closeInventory();
                            MessageKey.ARENA_DELETING.send(event.getWhoClicked(), arena, null, null, null, null,
                                    null, -1, arena.getExtension());
                            try {
                                SpleefX.getPlugin().getArenaManager().removeArena(arena.getKey());
                                MessageKey.ARENA_DELETED.send(event.getWhoClicked(), arena, null, null, null, null,
                                        null, -1, arena.getExtension());
                            } catch (IllegalStateException e) {
                                Chat.plugin(event.getWhoClicked(), "&c" + e.getMessage());
                            }
                            break;
                    }
                }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {
            event.getPlayer().removeMetadata("spleefx.editing", SpleefX.getPlugin());
        }
    }

    /**
     * A simple interface for creating arena instances
     */
    @FunctionalInterface
    public interface ArenaFactory<R extends GameArena> {

        /**
         * Creates a new arena from the specified data
         *
         * @param key               Arena key
         * @param displayName       Arena display name
         * @param regenerationPoint Regeneration point for the arena
         * @param arenaType         The arena's type
         * @param extension         Arena's extension mode. Can be null.
         * @return The arena
         */
        R create(String key, String displayName, Location regenerationPoint, ArenaType arenaType, GameExtension extension);

    }
}