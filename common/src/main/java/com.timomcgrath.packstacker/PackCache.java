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

import java.util.*;

public class PackCache {
    private static PackCache instance;
    private final Map<UUID, AbstractResourcePack> packMap;
    private final Map<String, UUID> packNameMap;
    private final Map<String, UUID> packHashMap;

    protected PackCache() {
        this.packMap = new HashMap<>();
        this.packNameMap = new HashMap<>();
        this.packHashMap = new HashMap<>();
    }

    public static PackCache getInstance() {
        if (instance == null)
            instance = new PackCache();
        return instance;
    }

    public void reset() {
        packMap.clear();
        packNameMap.clear();
        packHashMap.clear();
    }

    public void add(AbstractResourcePack pack) {
        UUID uuid = pack.getUuid();
        packMap.put(uuid, pack);
        packNameMap.put(pack.getName().toLowerCase(), uuid);
        packHashMap.put(pack.getHash(), uuid);
    }

    public void remove(String packName) {
        AbstractResourcePack pack = get(packName);
        packMap.remove(pack.getUuid());
        packNameMap.remove(packName);
        packHashMap.remove(pack.getHash());
    }

    public void addAll(Collection<AbstractResourcePack> packs) {
        packs.forEach(this::add);
    }

    public Collection<AbstractResourcePack> getAll() {
        return packMap.values();
    }

    public List<String> getPackNames() {
        return packNameMap.keySet().stream().toList();
    }

    public AbstractResourcePack get(UUID uuid) {
        return packMap.get(uuid);
    }

    /**
     * Velocity does not offer pack ids
     * @param hash
     * @return
     */
    public AbstractResourcePack get(byte[] hash) {
        return get(packHashMap.get(HexFormat.of().formatHex(hash)));
    }

    public AbstractResourcePack get(String name) {
        return get(packNameMap.get(name));
    }
    public boolean exists(String packname) {
        return packNameMap.containsKey(packname);
    }
}
