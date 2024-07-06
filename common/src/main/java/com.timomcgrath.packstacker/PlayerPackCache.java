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

public class PlayerPackCache {
    private static PlayerPackCache instance;
    private final Map<UUID, PackPlayer> playerMap;

    private PlayerPackCache() {
        this.playerMap = new HashMap<>();
    }

    public PackPlayer getPlayer(UUID uuid) {
        return playerMap.get(uuid);
    }

    public void removePlayer(UUID uuid) {
        playerMap.remove(uuid);
    }

    public PackPlayer initPlayer(UUID uuid) {
        if (playerMap.containsKey(uuid))
            return getPlayer(uuid);

        PackPlayer packPlayer = new PackPlayer(uuid);
        return playerMap.put(uuid, packPlayer);
    }

    public static PlayerPackCache getInstance() {
        if (instance == null)
            instance = new PlayerPackCache();
        return instance;
    }
}
