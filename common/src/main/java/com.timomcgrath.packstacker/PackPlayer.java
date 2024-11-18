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

public class PackPlayer {
    private final UUID uuid;
    private final Map<String, AbstractResourcePack> activePacks = new HashMap<>();
    private final List<AbstractResourcePack> sortedPacks = new ArrayList<>();
    private boolean passedVerification = true;

    public PackPlayer(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUUID() {
        return uuid;
    }

    public boolean passedVerification() {
        return passedVerification;
    }

    public void setVerified() {
        passedVerification = true;
    }

    public void addPack(AbstractResourcePack pack) {
        activePacks.put(pack.getName().toLowerCase(), pack);
        sortedPacks.add(pack);
    }

    public void removePack(AbstractResourcePack pack) {
      activePacks.remove(pack.getName().toLowerCase());
      sortedPacks.remove(pack);
    }

    public boolean hasPack(AbstractResourcePack pack) {
        return activePacks.containsKey(pack.getName().toLowerCase());
    }

    public AbstractResourcePack getPack(String packName) {
        return activePacks.get(packName);
    }

    public List<AbstractResourcePack> getActivePacks() {
        return sortedPacks;
    }

    public Set<UUID> getActivePackIds() {
        Set<UUID> uuids = new HashSet<>();
        activePacks.values().forEach(pack -> {
            uuids.add(pack.getUuid());
        });
        return uuids;
    }
}
