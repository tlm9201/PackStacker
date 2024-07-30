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

package com.timomcgrath.packstacker.factory;

import com.timomcgrath.packstacker.AbstractResourcePack;
import com.timomcgrath.packstacker.PackStacker;
import com.timomcgrath.packstacker.ResourcePack;
import com.timomcgrath.packstacker.ResourcePackFactory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import ninja.leaping.configurate.ConfigurationNode;

public class VelocityResourcePackFactory implements ResourcePackFactory {
    @Override
    public AbstractResourcePack create(ConfigurationNode root) {
        String name, hash, url, promptStr;
        byte priority;
        boolean isRequired, loadOnJoin;

        name = root.getNode("name").getString();
        hash = root.getNode("hash").getString();
        url = root.getNode("url").getString();
        promptStr = root.getNode("prompt").getString();
        priority = (byte) root.getNode("priority").getInt(0);
        isRequired = root.getNode("required").getBoolean(false);
        loadOnJoin = root.getNode("load_on_join").getBoolean();

        Component prompt = null;
        if (promptStr != null)
            prompt = MiniMessage.miniMessage().deserialize(promptStr);

        return new ResourcePack(PackStacker.getInstance(), name, hash, prompt, url, priority, isRequired, loadOnJoin);
    }
}
