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

import ninja.leaping.configurate.ConfigurationNode;

import java.util.Locale;

public class PackSettings {
    private static PackSettings instance;
    public int githubPort = 3434;
    public boolean githubEnabled = false;
    public int joinDelayMs = 1000;
    public int skinRefreshDelayMs = 5000;
    public boolean joinReplace = true;
    public int joinMaxRetries = 2;
    public BackendJoinLoading backendJoinLoading = BackendJoinLoading.AUTO;

    public enum BackendJoinLoading {
        AUTO,
        ENABLED,
        DISABLED
    }

    public void init(ConfigurationNode root) {
        ConfigurationNode github = root.getNode("github-endpoint");
        this.githubEnabled = github.getNode("enabled").getBoolean();
        this.githubPort = github.getNode("port").getInt();

        ConfigurationNode joinPacks = root.getNode("join-packs");
        this.joinDelayMs = joinPacks.getNode("delay-ms").getInt(joinDelayMs);
        this.skinRefreshDelayMs = joinPacks.getNode("skin-refresh-delay-ms").getInt(skinRefreshDelayMs);
        this.joinReplace = joinPacks.getNode("replace").getBoolean(joinReplace);
        this.joinMaxRetries = joinPacks.getNode("max-retries").getInt(joinMaxRetries);
        String backendLoading = joinPacks.getNode("backend-loading").getString("auto");
        if (backendLoading != null) {
            try {
                this.backendJoinLoading = BackendJoinLoading.valueOf(backendLoading.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                this.backendJoinLoading = BackendJoinLoading.AUTO;
            }
        }
    }
    public static PackSettings get() {
        if (instance == null)
            instance = new PackSettings();
        return instance;
    }
}
