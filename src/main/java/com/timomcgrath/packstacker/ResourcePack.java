package com.timomcgrath.packstacker;

import com.timomcgrath.packstacker.util.PackUtil;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

public class ResourcePack {
  private final byte[] hash;
  private final UUID uuid;
  private final String name, url;
  private final Component prompt;
  private final byte priority;
  private final boolean isRequired, loadOnJoin;
  private final ResourcePackInfo packInfo;

  public ResourcePack(String name, String hash, Component prompt, String url, byte priority, boolean isRequired, boolean loadOnJoin) {
    this.name = name;
    this.hash = PackUtil.hexStringToByteArray(hash);
    this.uuid = UUID.randomUUID();
    this.prompt = prompt;
    this.url = url;
    this.priority = priority;
    this.isRequired = isRequired;
    this.loadOnJoin = loadOnJoin;
    packInfo = ResourcePackInfo.resourcePackInfo(uuid, URI.create(url), hash);
  }

  public void load(Player player) {
    ResourcePackRequest request = ResourcePackRequest.resourcePackRequest()
        .packs(packInfo)
        .prompt(prompt)
        .required(isRequired)
        .build().callback((uuid1, status, audience) -> PackUtil.packCallback(uuid1, status, audience, this));
    player.sendResourcePacks(request);
    request.callback();
  }

  public static ResourcePack create(Configuration input) {
    try {
      String name = Objects.requireNonNull(input.getString("name")),
          hash = Objects.requireNonNull(input.getString("hash")),
          promptStr = input.getString("prompt"),
          url = Objects.requireNonNull(input.getString("url"));
      byte priority = (byte) input.getInt("priority");
      boolean isRequired = input.getBoolean("required", false),
          loadOnJoin = input.getBoolean("load_on_join", false);
      Component prompt = null;
      if (promptStr != null)
        prompt = MiniMessage.miniMessage().deserialize(promptStr);

      return new ResourcePack(name, hash, prompt, url, priority, isRequired, loadOnJoin);
    } catch (NullPointerException e) {
      PackStacker.getPlugin().getLogger().severe("An exception occurred while attempting to load a ResourcePack.");
      e.printStackTrace();
    }
    return null;
  }

  public byte[] getHash() {
    return hash;
  }

  public UUID getUuid() {
    return uuid;
  }

  public String getName() {
    return name;
  }

  public Component getPrompt() {
    return prompt;
  }

  public String getPromptLegacy() {
    return LegacyComponentSerializer.legacySection().serialize(prompt);
  }

  public String getUrl() {
    return url;
  }

  public byte getPriority() {
    return priority;
  }

  public boolean isRequired() {
    return isRequired;
  }

  public boolean loadOnJoin() {
    return loadOnJoin;
  }

  public ResourcePackInfo getPackInfo() {
    return packInfo;
  }
}
