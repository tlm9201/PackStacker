package com.timomcgrath.packstacker;

import java.util.*;

public class PackCache {
  private static PackCache instance;
  private final Map<UUID, ResourcePack> packMap;
  private final Map<String, UUID> packNameMap;

  private PackCache() {
    this.packMap = new HashMap<>();
    this.packNameMap = new HashMap<>();
  }

  public static PackCache getInstance() {
    if (instance == null)
      instance = new PackCache();
    return instance;
  }

  public void reset() {
    packMap.clear();
    packNameMap.clear();
  }

  public void add(ResourcePack pack) {
    UUID uuid = pack.getUuid();
    packMap.put(uuid, pack);
    packNameMap.put(pack.getName().toLowerCase(), uuid);
    PackStacker.getPlugin().getLogger().info(String.format("Loaded pack \"%s\"", pack.getName()));
  }

  public void addAll(Collection<ResourcePack> packs) {
    packs.forEach(this::add);
  }

  public Collection<ResourcePack> getAll() {
    return packMap.values();
  }

  public List<String> getPackNames() {
    return packNameMap.keySet().stream().toList();
  }

  public ResourcePack get(UUID uuid) {
    return packMap.get(uuid);
  }

  public ResourcePack get(String name) {
    return get(packNameMap.get(name));
  }
}
