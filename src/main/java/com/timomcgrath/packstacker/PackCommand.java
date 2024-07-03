package com.timomcgrath.packstacker;

import com.timomcgrath.packstacker.util.PackUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class PackCommand implements CommandExecutor, TabExecutor {
  private static final List<String> packTabCompletes = Arrays.asList("load");

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
    parsePackCommand(sender, args);
    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
    switch (args[0].toLowerCase()) {
      case "load" -> {
        switch (args.length) {
          case 2 -> {
            if (sender.hasPermission(Permissions.PACK_LOAD_SELF.get()) || sender.hasPermission(Permissions.PACK_LOAD_OTHERS.get()))
              return filterByStart(PackCache.getInstance().getPackNames(), args[1]);
          }
          case 3 -> {
            if (sender.hasPermission(Permissions.PACK_LOAD_OTHERS.get()))
              return null;
          }
        }
      }
      default -> {
        return packTabCompletes;
      }
    }
    return List.of();
  }

  private void parsePackCommand(CommandSender sender, String[] args) {
    switch (args[0]) {
      case "load" -> parseLoadCommand(sender, remFirstArg(args));
    }
  }

  private void parseLoadCommand(CommandSender sender, String[] args) {
    String arg = args[0].toLowerCase();

    switch (args.length) {
      case 1 -> {
        if (sender instanceof Player player) {
          if (!player.hasPermission(Permissions.PACK_LOAD_SELF.get())) {
            Messaging.sendMsg(sender, "cmd_no_perm");
            return;
          }

          if (!PackUtil.loadByName(player, arg))
            Messaging.sendMsg(sender, "invalid_pack", arg);
        } else
          Messaging.sendMsg(sender, "player_not_verbose");
      }
      case 2 -> {
        if (sender instanceof Player player) {
          if (!player.hasPermission(Permissions.PACK_LOAD_OTHERS.get())) {
            Messaging.sendMsg(sender, "cmd_no_perm");
            return;
          }
        }

        Player player = Bukkit.getPlayer(args[1].toLowerCase());
        if (player != null) {
          if (!PackUtil.loadByName(player, arg))
            Messaging.sendMsg(sender, "invalid_pack", arg);
        }
        else
          Messaging.sendMsg(sender, "player_not_online", args[1]);
      }
    }
  }

  private static String[] remFirstArg(String[] arr) {
    return remArgs(arr, 1);
  }

  private static String[] remArgs(String[] arr, int startFromIndex) {

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

  private static List<String> filterByStart(List<String> list, String startingWith) {
    if (list == null || startingWith == null) {
      return Collections.emptyList();
    }
    return list.stream().filter(name -> name.toLowerCase(Locale.ROOT).startsWith(startingWith.toLowerCase(Locale.ROOT))).collect(Collectors.toList());
  }
}
