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

package com.timomcgrath.packstacker;

import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PackCommand extends AbstractPackCommand implements CommandExecutor, TabExecutor {

  protected PackCommand(PackPlugin plugin) {
    super(plugin);
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
    parsePackCommand(sender, args);
    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
    return suggest(sender, args);
  }

  @Override
  protected void parseLoadCommand(Audience sender, String[] args) {
    if (args.length == 0) {
      Messaging.sendMsg(sender, "pack_help");
      return;
    }

    String arg = args[0].toLowerCase();
    switch (args.length) {
      case 1 -> {
        if (sender instanceof Player player) {
          if (!player.hasPermission("pack.load.self")) {
            Messaging.sendMsg(sender, "cmd_no_perm");
            return;
          }

          if (!PackStackerUtil.loadByName(player, player.getUniqueId(), arg))
            Messaging.sendMsg(sender, "invalid_pack", arg);
        } else
          Messaging.sendMsg(sender, "player_not_verbose");
      }
      case 2 -> {
        if (sender instanceof Player player) {
          if (!player.hasPermission("pack.load.others")) {
            Messaging.sendMsg(sender, "cmd_no_perm");
            return;
          }
        }

        Player player = Bukkit.getPlayer(args[1].toLowerCase());
        if (player != null) {
          if (!PackStackerUtil.loadByName(player, player.getUniqueId(), arg))
            Messaging.sendMsg(sender, "invalid_pack", arg);
        }
        else
          Messaging.sendMsg(sender, "player_not_online", args[1]);
      }
      default -> Messaging.sendMsg(sender, "pack_help");
    }
  }

  @Override
  protected void parseUnloadCommand(Audience sender, String[] args) {
    switch (args.length) {
      case 1 -> {
        if (!(sender instanceof Player player)) {
          Messaging.sendMsg(sender, "player_not_verbose");
          return;
        }

        PackPlayer packPlayer = PlayerPackCache.getInstance().getPlayer(player.getUniqueId());
        AbstractResourcePack pack = packPlayer.getPack(args[0].toLowerCase());
        if (pack != null) {
          pack.unload(player, player.getUniqueId());
        } else
          Messaging.sendMsg(sender, "invalid_pack", args[0].toLowerCase());
      }
    }
  }
}
