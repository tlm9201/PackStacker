package com.timomcgrath.packstacker;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;

import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Objects;

public class Messaging {
  private static final HashMap<String, String> messages = new HashMap<>();
  private static final String MESSAGE_NOT_FOUND = "Message does not exist or is not configured.";

  public static void init(Configuration config) {
    reset();
    config.getKeys(false).parallelStream().forEach(key -> messages.put(key, config.getString(key)));
  }

  public static void sendMsg(CommandSender sender, String key) {
    sender.sendMessage(get(key));
  }

  public static void sendMsg(CommandSender sender, String key, Object... args) {
    sender.sendMessage(get(key, args));
  }

  public static Component get(String key) {
    return LegacyComponentSerializer.legacySection().deserialize(getStr(key));
  }

  public static TextComponent get(String key, Object... args) {
    return LegacyComponentSerializer.legacySection().deserialize(getStr(key, args));
  }

  public static String getStr(String key) {
    String msg = messages.get(key);
    return Objects.isNull(msg) ? MESSAGE_NOT_FOUND : msg;
  }

  public static String getStr(String key, Object... args) {
    String str = getStr(key);

    try {
      return String.format(str, args);
    } catch (IllegalFormatException e) {
      PackStacker.getPlugin().getLogger().warning("An exception occurred when formatting messaging string " + str + " with key: " + key + " and args: " + args);
      return str;
    }
  }

  public static void reset() {
    messages.clear();
  }
}
