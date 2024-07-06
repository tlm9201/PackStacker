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
import net.kyori.adventure.resource.ResourcePackStatus;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ResourcePack extends AbstractResourcePack {
    public ResourcePack(String name, String hash, Component prompt, String url, byte priority, boolean isRequired, boolean loadOnJoin) {
        super(name, hash, prompt, url, priority, isRequired, loadOnJoin);
    }

    @Override
    public void packCallback(UUID packId, ResourcePackStatus status, Audience audience, UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        PackPlayer packPlayer = PlayerPackCache.getInstance().getPlayer(playerId);

        if (player == null)
            return;

        AbstractResourcePack pack = PackCache.getInstance().get(packId);
        switch (status) {
            case SUCCESSFULLY_LOADED:
                packPlayer.addPack(pack);
                audience.sendMessage(Messaging.get("pack_successfully_loaded", pack.getName()));
                break;
            case ACCEPTED:
                Messaging.sendMsg(audience, "pack_accepted", pack.getName());
                break;
            case DECLINED:
            case DISCARDED:
            case INVALID_URL:
            case FAILED_RELOAD:
            case FAILED_DOWNLOAD:
                audience.sendMessage(Messaging.get("pack_failed_load", pack.getName(), status.name()));
                if (pack.isRequired())
                    player.kick(Messaging.get("pack_req_kick"));
        }
    }
}
