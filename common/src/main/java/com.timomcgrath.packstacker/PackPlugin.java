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

import net.kyori.adventure.audience.Audience;

import java.util.List;

public interface PackPlugin {
    void reloadMessages();
    void reloadPacks();
    void reloadAll();
    void reloadPlayers();
    boolean hasPermission(Audience audience, String permission);

    default boolean hasAnyPermission(Audience audience, String... permissions) {
        for (String permission : permissions) {
            if (hasPermission(audience, permission))
                return true;
        }

        return false;
    }

    List<String> getOnlinePlayers();

    default int getWebhookPort() {
        return 3434;
    }

    /**
     * Called when a pack changes from a github release webhook event
     * @param pack the affected pack
     */
    void invokeGithubRelease(AbstractResourcePack pack);

    void log(String string);
}
