package com.timomcgrath.packstacker.listener;

import com.timomcgrath.packstacker.PackCache;
import com.timomcgrath.packstacker.util.PackUtil;
import com.timomcgrath.packstacker.ResourcePack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;

public class PackListener implements Listener {

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    List<ResourcePack> requiredPacks = new ArrayList<>(PackCache.getInstance().getAll().stream().filter(ResourcePack::loadOnJoin).toList()); // Get all required packs
    PackUtil.loadMultiple(event.getPlayer(), requiredPacks);
  }
}
