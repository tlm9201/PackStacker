package com.timomcgrath.packstacker.util;

import com.timomcgrath.packstacker.Messaging;
import com.timomcgrath.packstacker.PackCache;
import com.timomcgrath.packstacker.ResourcePack;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.resource.ResourcePackStatus;
import org.bukkit.entity.Player;

import java.util.*;

public class PackUtil {

  /**
   * attempts to load a pack by name onto a player.
   * @param player
   * @param name
   * @return if the pack was found and requested to load to client
   */
  public static boolean loadByName(Player player, String name) {
    PackCache packCache = PackCache.getInstance();
    ResourcePack resourcePack = packCache.get(name);

    if (resourcePack != null) {
      resourcePack.load(player);
      return true;
    }
    return false;
  }

  /**
   * Used to convert string representation of sha-1 hashes into a byte[]
   * @param s
   * @return
   */
  public static byte[] hexStringToByteArray(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
          + Character.digit(s.charAt(i+1), 16));
    }
    return data;
  }

  /**
   * Loads multiple ResourcePacks at once. Prompt information is based on the pack with the most priority.
   * @param player
   * @param packs
   */
  public static void loadMultiple(Player player, List<ResourcePack> packs) {
    if (packs.isEmpty())
      return;

    packs.sort(new PackComparator());
    ArrayList<ResourcePackInfo> packInfos = new ArrayList<>();
    packs.forEach(pack-> packInfos.add(pack.getPackInfo()));
    ResourcePack first = packs.getFirst();

    ResourcePackRequest request = ResourcePackRequest.resourcePackRequest()
        .packs(packInfos).prompt(first.getPrompt())
        .build().callback((uuid, status, audience) -> packCallback(uuid, status, audience, first));
    player.sendResourcePacks(request);
  }

  public static void packCallback(UUID uuid, ResourcePackStatus status, Audience audience, ResourcePack pack) {
    Player player = (Player) audience;
    switch (status) {
      case SUCCESSFULLY_LOADED:
        audience.sendMessage(Messaging.get("pack_successfully_loaded", pack.getName()));
        break;
      case ACCEPTED:
        audience.sendMessage(Messaging.get("pack_accepted"));
        break;
      case DECLINED:
      case DISCARDED:
      case INVALID_URL:
      case FAILED_RELOAD:
      case FAILED_DOWNLOAD:
        audience.sendMessage(Messaging.get("pack_failed_load", pack.getName(), status.name()));
        if (pack.isRequired())
          player.kick(Messaging.get("pack_req_kick"));
    }
  }
  static class PackComparator implements Comparator<ResourcePack> {

    /**
     * Pack priority comparison
     * @param p1 the first object to be compared.
     * @param p2 the second object to be compared.
     * @return
     */
    @Override
    public int compare(ResourcePack p1, ResourcePack p2) {
      return Byte.compare(p1.getPriority(), p2.getPriority());
    }
  }
}
