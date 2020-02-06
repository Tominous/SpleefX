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
package io.github.spleefx.scoreboard;

import com.google.gson.annotations.Expose;
import io.github.spleefx.arena.ArenaPlayer;
import io.github.spleefx.arena.api.ArenaType;
import io.github.spleefx.arena.api.BaseArenaEngine;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.message.MessageKey;
import io.github.spleefx.team.GameTeam;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class ScoreboardHolder {

    @Expose
    private boolean enabled = true;

    @Expose
    private String title = "";

    @Expose
    private Map<Integer, String> text = new LinkedHashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public String getTitle() {
        return title;
    }

    public Map<Integer, String> getText() {
        return text;
    }

    public Optional<SimpleScoreboard> scoreboard(ArenaPlayer player, Map<String, Supplier<String>> placeholders) {
        if (!isEnabled()) return Optional.empty();
        SimpleScoreboard scoreboard = new SimpleScoreboard(getTitle());

        getText().forEach((score, text) -> {
            text = replacePlaceholders(player, text);
            for (String s : placeholders.keySet()) {
                if (text.contains(s)) text = text.replace(s, String.valueOf(placeholders.get(s).get()));
            }
            scoreboard.add(text.trim().isEmpty() ? String.valueOf(ChatColor.COLOR_CHAR) + score : text, score);
        });
        return Optional.of(scoreboard);
    }

    private String replacePlaceholders(ArenaPlayer player, String message) {
        GameArena arena = player.getCurrentArena();
        BaseArenaEngine<? extends GameArena> engine = (BaseArenaEngine<? extends GameArena>) arena.getEngine();
        Location location = player.getPlayer().getLocation();
        if (arena != null) {
            message = message
                    .replace("{arena}", arena.getKey())
                    .replace("{arena_displayname}", arena.getDisplayName())
                    .replace("{arena_time_left}", Integer.toString(engine.timeLeft))
                    .replace("{arena_playercount}", Integer.toString(arena.getEngine().getPlayerTeams().size()))
                    .replace("{arena_minimum}", Integer.toString(arena.getMinimum()))
                    .replace("{arena_maximum}", Integer.toString(arena.getMaximum()))
                    .replace("{arena_stage}", arena.getEngine().getArenaStage().getState())
                    .replace("{countdown}", String.valueOf(engine.countdown))
                    .replace("{plural}", engine.countdown == 1 ? "" : "s");
            if (arena.getArenaType() == ArenaType.TEAMS) {
                GameTeam team = engine.getPlayerTeams().get(player);
                message = message
                        .replace("{team}", team.getColor().chat())
                        .replace("{team_color}", team.getColor().getChatColor());
            }
        }
        message = message
                .replace("{x}", Double.toString(location.getX()))
                .replace("{y}", Double.toString(location.getY()))
                .replace("{z}", Double.toString(location.getZ()))
                .replace("{player}", player.getPlayer().getName())
                .replace("{extension_key}", arena.getExtension().getKey())
                .replace("{extension_chat_prefix}", arena.getExtension().getChatPrefix())
                .replace("{extension}", arena.getExtension().getDisplayName());
        if (MessageKey.PAPI) {
            message = PlaceholderAPI.setPlaceholders(player.getPlayer(), message);
        }
        return message;
    }

}