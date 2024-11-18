/*
 * PackStacker
 * Copyright (C) 2024 Timo McGrath
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.timomcgrath.packstacker.listener;

import com.timomcgrath.packstacker.PackStackerUtil;
import com.timomcgrath.packstacker.PlayerPackCache;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PackListener implements Listener {

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    PlayerPackCache.getInstance().initPlayer(player.getUniqueId());

    PackStackerUtil.loadMultiple(player, player.getUniqueId(), PackStackerUtil.getPacksToLoadOnJoin(), false);
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    PlayerPackCache.getInstance().removePlayer(event.getPlayer().getUniqueId());
  }
}
