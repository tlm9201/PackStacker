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

package com.timomcgrath.packstacker.listener;

import com.timomcgrath.packstacker.*;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.PlayerResourcePackStatusEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.player.ResourcePackInfo;

public class PackListener {

    @Subscribe
    public void onPlayerJoin(ServerPostConnectEvent event) {
        Player player = event.getPlayer();
        PackStackerUtil.loadMultiple(player, player.getUniqueId(), PackStackerUtil.getPacksToLoadOnJoin());
    }

    @Subscribe
    public void onPackStatusUpdate(PlayerResourcePackStatusEvent event) {
        Player player = event.getPlayer();
        ResourcePackInfo packInfo = event.getPackInfo();

        if (packInfo == null)
            return;

        byte[] hash = packInfo.getHash();
        if (hash == null)
            return;

        AbstractResourcePack pack = PackCache.getInstance().get(hash);
        PackPlayer packPlayer = PlayerPackCache.getInstance().getPlayer(player.getUniqueId());
        PlayerResourcePackStatusEvent.Status status = event.getStatus();

        switch (status) {
            case SUCCESSFUL:
                packPlayer.addPack(pack);
                Messaging.sendMsg(player, "pack_successfully_loaded", pack.getName());
                break;
            case ACCEPTED:
                Messaging.sendMsg(player, "pack_accepted", pack.getName());
                break;
            case FAILED_DOWNLOAD:
            case DECLINED:
                Messaging.sendMsg(player, "pack_failed_load", pack.getName(), status.name());
                if (pack.isRequired())
                    player.disconnect(Messaging.get("pack_req_kick"));
        }
    }

    @Subscribe
    public void onProxyConnect(PostLoginEvent event) {
        PlayerPackCache.getInstance().initPlayer(event.getPlayer().getUniqueId());
    }

    @Subscribe
    public void onProxyDisconnect(DisconnectEvent event) {
        PlayerPackCache.getInstance().removePlayer(event.getPlayer().getUniqueId());
    }
}
