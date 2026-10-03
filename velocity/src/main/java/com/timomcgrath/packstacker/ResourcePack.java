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
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.resource.ResourcePackStatus;
import net.kyori.adventure.text.Component;

import java.util.Optional;
import java.util.HexFormat;
import java.util.UUID;
import java.util.stream.Stream;

public class ResourcePack extends AbstractResourcePack {
    public ResourcePack(PackPlugin plugin, String name, String hash, Component prompt, String url, byte priority, boolean isRequired, boolean loadOnJoin) {
        super(name, hash, prompt, url, priority, isRequired, loadOnJoin, plugin);
    }

    @Override
    public boolean isApplied(Audience audience, UUID playerId) {
        if (!(audience instanceof Player player))
            return super.isApplied(audience, playerId);

        // Velocity rejects duplicate hashes, even when the pack UUID or name differs.
        return Stream.concat(player.getAppliedResourcePacks().stream(), player.getPendingResourcePacks().stream())
                .anyMatch(pack -> pack.getHash() != null
                        && getHash().equalsIgnoreCase(HexFormat.of().formatHex(pack.getHash())));
    }

    @Override
    public void packCallback(UUID packId, ResourcePackStatus status, Audience audience, UUID playerId) {
        Optional<Player> playerOpt = PackStacker.getInstance().getServer().getPlayer(playerId);
        if (playerOpt.isEmpty())
            return;

        Player player = playerOpt.get();
        PackPlayer packPlayer = PlayerPackCache.getInstance().getPlayer(player.getUniqueId());
        AbstractResourcePack pack = PackCache.getInstance().get(packId);
        if (pack == null)
            return;

        switch (status) {
            case SUCCESSFULLY_LOADED:
                if (packPlayer != null)
                    packPlayer.addPack(pack);
                audience.sendMessage(Messaging.get("pack_successfully_loaded", pack.getName()));
                SkinRefresh.schedule(player);
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

                if (!player.hasPermission("pack.bypass") && pack.isRequired()) {
                    player.disconnect(Messaging.get("pack_req_kick"));
                    break;
                }

                SkinRefresh.schedule(player);
        }
    }

    public void packCallbackRemove(UUID packId, ResourcePackStatus status, Audience audience, UUID playerId, ResourcePackRequest toApplyAfter) {
        Optional<Player> playerOpt = PackStacker.getInstance().getServer().getPlayer(playerId);
        if (playerOpt.isEmpty())
            return;

        Player player = playerOpt.get();
        PackPlayer packPlayer = PlayerPackCache.getInstance().getPlayer(player.getUniqueId());
        AbstractResourcePack pack = PackCache.getInstance().get(packId);

        switch (status) {
            case SUCCESSFULLY_LOADED:
                player.sendResourcePacks(toApplyAfter);
                break;
            case ACCEPTED:
                break;
            case DECLINED:
            case DISCARDED:
            case INVALID_URL:
            case FAILED_RELOAD:
            case FAILED_DOWNLOAD:
        }
    }

    @Override
    public void reload(UUID player) {
        PackPlayer packPlayer = PlayerPackCache.getInstance().getPlayer(player);
        Player player1 = PackStacker.getInstance().getServer().getPlayer(player).get();

        ResourcePackRequest.Builder request0Builder = ResourcePackRequest.resourcePackRequest()
                .packs(getPackInfo())
                .prompt(getPrompt())
                .replace(true);
        if (isRequired())
            request0Builder.required(true);

        ResourcePackRequest request0 = request0Builder
                .build().callback((packId, status, aud) -> packCallback(packId, status, aud, player));

        ResourcePackRequest.Builder requestBuilder = ResourcePackRequest.resourcePackRequest()
                .packs(getPackInfo())
                .prompt(getPrompt())
                .replace(true);
        if (isRequired())
            requestBuilder.required(true);

        ResourcePackRequest request = requestBuilder
                .build().callback((packId, status, aud) -> packCallbackRemove(packId, status, aud, player, request0));
        player1.removeResourcePacks(request);
        player1.sendResourcePacks(request0);
    }
}
