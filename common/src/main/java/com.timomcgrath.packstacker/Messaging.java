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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Objects;

public class Messaging {
    private static final HashMap<String, String> messages = new HashMap<>();
    private static final String MESSAGE_NOT_FOUND = "Message does not exist or is not configured.";

    public static void init(ConfigurationNode root) {
        reset();
        root.getChildrenMap().forEach((object, configurationNode) -> {
            String name = object.toString();
            String message = configurationNode.getString();
            messages.put(name, message);
        });
    }

    public static void sendMsg(Audience audience, String key) {
        audience.sendMessage(get(key));
    }

    public static void sendMsg(Audience audience, String key, Object... args) {
        audience.sendMessage(get(key, args));
    }

    public static Component get(String key) {
        return LegacyComponentSerializer.legacySection().deserialize(getStr(key));
    }

    public static TextComponent get(String key, Object... args) {
        return LegacyComponentSerializer.legacySection().deserialize(getStr(key, args));
    }

    public static String getStr(String key) {
        String msg = messages.get(key);
        return Objects.isNull(msg) ? MESSAGE_NOT_FOUND : msg;
    }

    public static String getStr(String key, Object... args) {
        String str = getStr(key);

        try {
            return String.format(str, args);
        } catch (IllegalFormatException e) {
            // TODO: Log exception
            return str;
        }
    }

    public static void reset() {
        messages.clear();
    }
}
