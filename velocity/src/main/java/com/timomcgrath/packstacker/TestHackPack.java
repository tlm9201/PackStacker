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

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.player.ResourcePackInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.HexFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO. Incomplete.
 */
public class TestHackPack {
    public static final String HASH_LINK_REGEX = "([^/]+)(?=\\.\\w+$)";
    public static final String HACK_PACK_LINK = "https://github.com/tlm9201/PackStacker/raw/master/verification/e33fe2931c268399e41f829245219a02997d7662.zip";
    public static final Component PROMPT = Component.text("Anti-Pack Hack Verification!").color(NamedTextColor.RED);
    private static TestHackPack instance;
    private final ResourcePackInfo testPack;

    public TestHackPack(ProxyServer server) {
        Pattern pattern = Pattern.compile(HASH_LINK_REGEX);
        Matcher matcher = pattern.matcher(HACK_PACK_LINK);
        ResourcePackInfo pack = server.createResourcePackBuilder(HACK_PACK_LINK)
                .setHash(HexFormat.of().parseHex(matcher.group()))
                .setShouldForce(true)
                .setPrompt(PROMPT)
                .build();

        testPack = pack;
    }

    public static ResourcePackInfo get() {
        if (instance == null)
            instance = new TestHackPack(PackStacker.getInstance().getServer());

        return instance.testPack;
    }
}
