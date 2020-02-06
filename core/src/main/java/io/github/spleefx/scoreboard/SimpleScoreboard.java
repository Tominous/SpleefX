package io.github.spleefx.scoreboard;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SimpleScoreboard {

    private Scoreboard scoreboard;

    private String title;
    private Map<String, Integer> scores;
    private List<Team> teams;

    public SimpleScoreboard(String title) {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.title = ChatColor.translateAlternateColorCodes('&', title);
        scores = Maps.newLinkedHashMap();
        teams = Lists.newArrayList();
    }

    public SimpleScoreboard blankLine() {
        add(" ");
        return this;
    }

    public SimpleScoreboard add(String text) {
        add(text, null);
        return this;
    }

    public SimpleScoreboard add(String text, Integer score) {
        Preconditions.checkArgument(text.length() < 48, "text cannot be over 48 characters in length");
        text = ChatColor.translateAlternateColorCodes('&', fixDuplicates(text));
        scores.put(text, score);
        return this;
    }

    private String fixDuplicates(String text) {
        while (scores.containsKey(text))
            text += "§r";
        if (text.length() > 48)
            text = text.substring(0, 47);
        return text;
    }

    private Map.Entry<Team, String> createTeam(String text) {
        String result = "";
        if (text.length() <= 16)
            return new AbstractMap.SimpleEntry<>(null, text);
        Team team = scoreboard.registerNewTeam("text-" + scoreboard.getTeams().size());
        Iterator<String> iterator = Splitter.fixedLength(16).split(text).iterator();
        team.setPrefix(iterator.next());
        result = iterator.next();
        if (text.length() > 32)
            team.setSuffix(iterator.next());
        teams.add(team);
        return new AbstractMap.SimpleEntry<>(team, result);
    }

    public void build() {
        Objective obj = scoreboard.registerNewObjective((title.length() > 16 ? title.substring(0, 15) : title), "dummy");
        obj.setDisplayName(title);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        int index = scores.size();

        for (Map.Entry<String, Integer> text : scores.entrySet()) {
            Map.Entry<Team, String> team = createTeam(text.getKey());
            Integer score = text.getValue() != null ? text.getValue() : index;
            if (team.getKey() != null)
                team.getKey().addEntry(team.getValue());
            obj.getScore(team.getValue()).setScore(score);
            index -= 1;
        }
    }

    public void reset() {
        title = null;
        scores.clear();
        for (Team t : teams)
            t.unregister();
        teams.clear();
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public void send(Player... players) {
        for (Player p : players)
            p.setScoreboard(scoreboard);
    }

}