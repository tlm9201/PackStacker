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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class JoinPackScheduler {
    private static final Map<UUID, Runnable> pendingCancellers = new ConcurrentHashMap<>();

    private JoinPackScheduler() {
    }

    public static void debounce(PackPlugin plugin, UUID playerId, Runnable action) {
        cancel(playerId);

        PackSettings settings = PackSettings.get();
        if (settings.joinDelayMs <= 0) {
            action.run();
            return;
        }

        Runnable wrapped = () -> {
            pendingCancellers.remove(playerId);
            if (!plugin.isPlayerOnline(playerId))
                return;
            action.run();
        };

        Runnable cancel = plugin.scheduleDelayed(wrapped, settings.joinDelayMs);
        pendingCancellers.put(playerId, cancel);
    }

    public static void cancel(UUID playerId) {
        Runnable cancel = pendingCancellers.remove(playerId);
        if (cancel != null)
            cancel.run();
    }
}