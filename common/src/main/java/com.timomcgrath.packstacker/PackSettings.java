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

public class PackSettings {
    private static PackSettings instance;
    public int githubPort = 3434;
    public boolean githubEnabled = false;

    public void init(ConfigurationNode root) {
        ConfigurationNode github = root.getNode("github-endpoint");
        this.githubEnabled = github.getNode("enabled").getBoolean();
        this.githubPort = github.getNode("port").getInt();
    }
    public static PackSettings get() {
        if (instance == null)
            instance = new PackSettings();
        return instance;
    }
}
