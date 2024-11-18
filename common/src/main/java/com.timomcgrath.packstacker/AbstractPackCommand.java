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
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public abstract class AbstractPackCommand {
    static final List<String> packTabCompletes = Arrays.asList("load", "unload", "list", "reload", "update");
    static final List<String> packReloadTabCompletes = Arrays.asList("messages", "packs", "all");
    protected final PackPlugin plugin;

    protected AbstractPackCommand(PackPlugin plugin) {
        this.plugin = plugin;
    }

    protected List<String> suggest(Audience sender, String[] args) {
        if (args.length == 0)
            return packTabCompletes;

        switch (args[0].toLowerCase()) {
            case "load" -> {
                switch (args.length) {
                    case 2 -> {
                            return filterByStart(PackCache.getInstance().getPackNames(), args[1]);
                    }
                    case 3 -> {
                            return plugin.getOnlinePlayers();
                    }
                }
            }
            case "unload" -> {
                switch (args.length) {
                    case 2 -> {
                        return filterByStart(PackCache.getInstance().getPackNames(), args[1]);
                    }
                    case 3 -> {
                        return plugin.getOnlinePlayers();
                    }
                }
            }
            case "list", "update" -> {
                if (args.length == 2)
                    return filterByStart(PackCache.getInstance().getPackNames(), args[1]);
            }
            case "reload" -> {
                if (args.length == 2)
                    return filterByStart(packReloadTabCompletes, args[1]);
            }
            default -> {
                if (args.length == 1)
                    return filterByStart(packTabCompletes, args[0]);
            }
        }
        return List.of();
    }

    protected void parsePackCommand(Audience sender, String[] args) {
        if (args.length == 0) {
            Messaging.sendMsg(sender, "pack_help");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "load" -> {
                if (plugin.hasAnyPermission(sender, "pack.load.self", "pack.load.others")) {
                    parseLoadCommand(sender, remFirstArg(args));
                    return;
                }
            }
            case "unload" -> {
                if (plugin.hasAnyPermission(sender, "pack.unload.self", "pack.unload.others")) {
                    parseUnloadCommand(sender, remFirstArg(args));
                    return;
                }
            }
            case "reload" -> {
                if (plugin.hasAnyPermission(sender, "pack.reload.messages", "pack.reload.packs")) {
                    parseReloadCommand(sender, remFirstArg(args));
                    return;
                }
            }
            case "update" -> {
                parseUpdateCommand(sender, remFirstArg(args));
                return;
            }
            case "list" -> {
                parseListCommand(sender, remFirstArg(args));
                return;
            }
            default -> {
                Messaging.sendMsg(sender, "pack_help");
                return;
            }
        }
        Messaging.sendMsg(sender, "cmd_no_perm");
    }

    protected abstract void parseUpdateCommand(Audience sender, String[] args);

    protected abstract void parseLoadCommand(Audience sender, String[] args);

    protected abstract void parseUnloadCommand(Audience sender, String[] args);

    void parseReloadCommand(Audience sender, String[] args) {
        if (args.length == 0) {
            plugin.reloadAll();
            Messaging.sendMsg(sender, "reload_cfg_all");
            return;
        }

        switch(args[0].toLowerCase()) {
            case "messages" -> {
                if (plugin.hasPermission(sender, "pack.reload.messages")) {

                    plugin.reloadMessages();
                    Messaging.sendMsg(sender, "reload_cfg_msgs");
                    return;
                }
            }
            case "packs" -> {
                if (plugin.hasPermission(sender, "pack.reload.packs")) {

                    plugin.reloadPacks();
                    Messaging.sendMsg(sender, "reload_cfg_packs");
                    return;
                }
            }
            case "all" -> {
                if (plugin.hasPermission(sender, "pack.reload.all") ||
                        plugin.hasPermission(sender, "pack.reload.packs") && plugin.hasPermission(sender, "pack.reload.messages")) {

                    plugin.reloadAll();
                    Messaging.sendMsg(sender, "reload_cfg_all");
                    return;
                }
            }
            default -> {
                Messaging.sendMsg(sender, "pack_help");
                return;
            }
        }
        Messaging.sendMsg(sender, "cmd_no_perm");
    }

    void parseListCommand(Audience sender, String[] args) {
        if (args.length != 0) {
            Messaging.sendMsg(sender, "pack_help");
            return;
        }

        Messaging.sendMsg(sender, "available_packs", StringUtils.join(PackCache.getInstance().getPackNames(), '\n'));
    }

    protected static String[] remFirstArg(String[] arr) {
        return remArgs(arr, 1);
    }

    protected static String[] remArgs(String[] arr, int startFromIndex) {

        if (arr.length == 0)
            return arr;
        else if (arr.length < startFromIndex)
            return new String[0];
        else {
            String[] newSplit = new String[arr.length - startFromIndex];
            System.arraycopy(arr, startFromIndex, newSplit, 0, arr.length - startFromIndex);
            return newSplit;
        }
    }

    protected static List<String> filterByStart(List<String> list, String startingWith) {
        if (list == null || startingWith == null) {
            return Collections.emptyList();
        }
        return list.stream().filter(name -> name.toLowerCase(Locale.ROOT).startsWith(startingWith.toLowerCase(Locale.ROOT))).collect(Collectors.toList());
    }
}
