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

package com.timomcgrath.packstacker.command;

import com.timomcgrath.packstacker.*;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.audience.Audience;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class PackCommand extends AbstractPackCommand implements SimpleCommand {
    public PackCommand(PackPlugin plugin) {
        super(plugin);
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        parsePackCommand(source, args);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return suggest(invocation.source(), invocation.arguments());
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return SimpleCommand.super.suggestAsync(invocation);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return SimpleCommand.super.hasPermission(invocation);
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

                    if (!PackStackerUtil.loadByName(sender, player.getUniqueId(), arg))
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

                Optional<Player> player = PackStacker.getInstance().getServer().getPlayer(args[1].toLowerCase());
                if (player.isPresent()) {
                    if (!PackStackerUtil.loadByName(player.get(), player.get().getUniqueId(), arg))
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
