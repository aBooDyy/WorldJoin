/*
    WorldJoin - A plugin to run commands and send messages on world join.
    Copyright (C) 2019-2020 aBooDyy

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.aboodyy.worldjoin.listeners;

import net.aboodyy.worldjoin.actions.ActionsManager;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.aboodyy.worldjoin.WorldJoin.plugin;

public class WorldJoinListener implements Listener {


    @EventHandler
    public void joinWorld(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        World world = p.getLocation().getWorld();

        FileConfiguration config = plugin.getConfig();
        ActionsManager actionsManager = plugin.getActionsManager();
        List<String> actions = new ArrayList<>();

        if (plugin.getPlayersData().hasSpecialActions(world.getName(), p.getUniqueId())) {
            actions = plugin.getPlayersData().getSpecialActions(world.getName(), p.getUniqueId());
            plugin.getPlayersData().clearSpecialActions(world.getName(), p.getUniqueId());
        }

        if (config.get("worlds." + world.getName()) != null) {
            if (config.getBoolean("worlds." + world.getName() + ".run_on_join")) {
                List<String> joinActions = config.getStringList("worlds." + world.getName() + ".actions");

                if (plugin.getWorldsData().isFirstJoin(world.getName(), p.getUniqueId()) &&
                        config.contains("worlds." + world.getName() + ".first_join_actions"))
                    joinActions = config.getStringList("worlds." + world.getName() + ".first_join_actions");

                actions.addAll(joinActions);
            }
        }

        for (String action : actions) {
            Matcher actionType = Pattern.compile("\\[([^)]+)]").matcher(action);
            String type = actionType.find() ? actionType.group(1).toLowerCase() : "";

            switch (type) {
                case "console":
                    actionsManager.getConsoleAction().console(p, world, world, action);
                    break;
                case "player":
                    actionsManager.getPlayerAction().player(p, world, world, action);
                    break;
                case "message":
                    actionsManager.getMessageAction().message(p, world, world, action);
                    break;
                case "broadcast":
                    actionsManager.getMessageAction().broadcast(p, world, world, action);
                    break;
            }
        }

        if (plugin.getWorldsData().isFirstJoin(world.getName(), p.getUniqueId()))
            plugin.getWorldsData().addPlayer(world.getName(), p.getUniqueId());
    }
}
