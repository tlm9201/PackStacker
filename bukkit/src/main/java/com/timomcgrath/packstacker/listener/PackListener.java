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

import com.timomcgrath.packstacker.JoinPackScheduler;
import com.timomcgrath.packstacker.PackSettings;
import com.timomcgrath.packstacker.PackStacker;
import com.timomcgrath.packstacker.PackStackerUtil;
import com.timomcgrath.packstacker.PlayerPackCache;
import com.timomcgrath.packstacker.ProxyDetection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PackListener implements Listener {

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    PlayerPackCache.getInstance().initPlayer(player.getUniqueId());

    if (!shouldSendJoinPacks())
      return;

    PackStackerUtil.loadJoinPacks(PackStacker.getPlugin(), player, player.getUniqueId());
  }

  private boolean shouldSendJoinPacks() {
    PackSettings settings = PackSettings.get();
    return switch (settings.backendJoinLoading) {
      case ENABLED -> true;
      case DISABLED -> false;
      case AUTO -> !ProxyDetection.isBehindVelocity();
    };
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    UUID playerId = event.getPlayer().getUniqueId();
    JoinPackScheduler.cancel(playerId);
    PlayerPackCache.getInstance().removePlayer(playerId);
  }
}
