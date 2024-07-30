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

import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.resource.ResourcePackStatus;
import net.kyori.adventure.text.Component;

import java.util.Optional;
import java.util.UUID;

public class ResourcePack extends AbstractResourcePack {
    public ResourcePack(PackPlugin plugin, String name, String hash, Component prompt, String url, byte priority, boolean isRequired, boolean loadOnJoin) {
        super(name, hash, prompt, url, priority, isRequired, loadOnJoin, plugin);
    }

    @Override
    public void packCallback(UUID packId, ResourcePackStatus status, Audience audience, UUID playerId) {
        Optional<Player> playerOpt = PackStacker.getInstance().getServer().getPlayer(playerId);
        if (playerOpt.isEmpty())
            return;

        Player player = playerOpt.get();
        PackPlayer packPlayer = PlayerPackCache.getInstance().getPlayer(player.getUniqueId());
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
                PackStacker.getInstance().getLogger().info(player.getUniqueId() + " " + player.hasPermission("pack.bypass"));

                if (player.hasPermission("pack.bypass"))
                    break;

                if (pack.isRequired())
                    player.disconnect(Messaging.get("pack_req_kick"));
        }
    }
}
