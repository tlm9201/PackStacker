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
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.resource.ResourcePackStatus;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.UUID;

public abstract class AbstractResourcePack {
    private String hash;
    private UUID uuid;
    private final String name;
    private String url;
    private final Component prompt;
    private final byte priority;
    private final boolean isRequired, loadOnJoin;
    private ResourcePackInfo packInfo;
    private final PackPlugin plugin;

    public AbstractResourcePack(String name, String hash, Component prompt, String url, byte priority, boolean isRequired, boolean loadOnJoin, PackPlugin plugin) {
        this.plugin = plugin;
        UUID uuid = UUID.randomUUID();
        this.name = name;
        this.hash = hash.toLowerCase();
        this.uuid = uuid;
        this.prompt = prompt;
        this.url = url;
        this.priority = priority;
        this.isRequired = isRequired;
        this.loadOnJoin = loadOnJoin;
        reloadPackInfo();
    }

    public void load(@NotNull Audience audience, UUID playerId) {
        PackPlayer packPlayer = PlayerPackCache.getInstance().getPlayer(playerId);

        if (packPlayer.hasPack(this)) {
            Messaging.sendMsg(audience, "pack_already_loaded", name);
            return;
        }

        ResourcePackRequest request = ResourcePackRequest.resourcePackRequest()
                .packs(packInfo)
                .prompt(prompt)
                .build().callback((packId, status, aud) -> packCallback(packId, status, aud, playerId));
        audience.sendResourcePacks(request);
        request.callback();
    }

    public void unload(@NotNull Audience audience, UUID playerId) {
        if (isRequired && !plugin.hasPermission(audience, "pack.bypass")) {
            Messaging.sendMsg(audience, "pack_required", name);
            return;
        }

        audience.removeResourcePacks(uuid);
        PackPlayer packPlayer = PlayerPackCache.getInstance().getPlayer(playerId);
        packPlayer.removePack(this);
    }

    public abstract void packCallback(UUID packId, ResourcePackStatus status, Audience audience, UUID playerId);

    public abstract void reload(UUID uuid);

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public Component getPrompt() {
        return prompt;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public byte getPriority() {
        return priority;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public boolean loadOnJoin() {
        return loadOnJoin;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public ResourcePackInfo getPackInfo() {
        return packInfo;
    }

    public void reloadPackInfo() {
        this.packInfo = ResourcePackInfo.resourcePackInfo(uuid, URI.create(url), hash);
    }
}
