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

import java.util.*;

public class PackStackerUtil {

    /**
     * attempts to load a pack by name onto a player.
     *
     * @param audience
     * @param playerId
     * @param name
     * @return if the pack was found and requested to load to client
     */
    public static boolean loadByName(Audience audience, UUID playerId, String name) {
        PackCache packCache = PackCache.getInstance();
        AbstractResourcePack resourcePack = packCache.get(name);

        if (resourcePack != null) {
            resourcePack.load(audience, playerId);
            return true;
        }
        return false;
    }

    /**
     * Loads multiple ResourcePacks at once. Prompt information is based on the pack with the most priority.
     *
     * @param audience
     * @param playerId
     * @param packs
     */
    public static void loadMultiple(Audience audience, UUID playerId, List<AbstractResourcePack> packs, boolean replace) {
        if (packs.isEmpty())
            return;

        PackPlayer packPlayer = PlayerPackCache.getInstance().getPlayer(playerId);
        packs.sort(new PackStackerUtil.PackComparator());
        ArrayList<ResourcePackInfo> packInfos = new ArrayList<>();
        List<AbstractResourcePack> packsToSend = packs.stream().filter(pack -> !packPlayer.hasPack(pack)).toList();

        if (packsToSend.isEmpty())
            return;

        packsToSend.forEach(pack -> packInfos.add(pack.getPackInfo()));
        AbstractResourcePack first = packsToSend.get(0);
        boolean required = packsToSend.stream().anyMatch(AbstractResourcePack::isRequired);

        ResourcePackRequest.Builder requestBuilder = ResourcePackRequest.resourcePackRequest()
                .packs(packInfos)
                .prompt(resolvePrompt(first))
                .replace(replace);
        if (required)
            requestBuilder.required(true);

        ResourcePackRequest request = requestBuilder
                .callback((uuid, status, aud) -> handlePackStatus(first, uuid, status, aud, playerId, packsToSend, replace))
                .build();
        audience.sendResourcePacks(request);
    }

    private static void handlePackStatus(AbstractResourcePack callbackPack, UUID packId, ResourcePackStatus status,
                                         Audience audience, UUID playerId, List<AbstractResourcePack> packs, boolean replace) {
        AbstractResourcePack pack = PackCache.getInstance().get(packId);
        if (pack == null)
            pack = callbackPack;

        if (status == ResourcePackStatus.DISCARDED && shouldRetryJoinPack(playerId, pack)) {
            AbstractResourcePack retrySource = pack != null ? pack : callbackPack;
            if (retrySource != null && !canBypassPacks(retrySource.getPackPlugin(), audience))
                retryJoinPack(audience, playerId, packs, replace);
            return;
        }

        pack.packCallback(packId, status, audience, playerId);
    }

    public static boolean shouldRetryJoinPackPublic(UUID playerId, AbstractResourcePack pack) {
        return shouldRetryJoinPack(playerId, pack);
    }

    private static boolean shouldRetryJoinPack(UUID playerId, AbstractResourcePack pack) {
        if (pack == null)
            return false;

        PackPlayer packPlayer = PlayerPackCache.getInstance().getPlayer(playerId);
        if (packPlayer == null || packPlayer.hasPack(pack))
            return false;

        return packPlayer.getJoinPackRetries() < PackSettings.get().joinMaxRetries;
    }

    public static void retryJoinPack(Audience audience, UUID playerId, List<AbstractResourcePack> packs, boolean replace) {
        PackPlayer packPlayer = PlayerPackCache.getInstance().getPlayer(playerId);
        if (packPlayer == null || packs.isEmpty())
            return;

        if (canBypassPacks(packs.get(0).getPackPlugin(), audience))
            return;

        packPlayer.incrementJoinPackRetries();
        loadMultiple(audience, playerId, packs, true);
    }

    private static Component resolvePrompt(AbstractResourcePack pack) {
        Component prompt = pack.getPrompt();
        if (prompt != null)
            return prompt;

        if (pack.isRequired())
            return Component.text("This server requires a resource pack.");

        return Component.text("This server has a resource pack available.");
    }

    /**
     * Filters both required and load_on_join packs from a given list of packs.
     */
    public static void loadJoinPacks(PackPlugin plugin, Audience audience, UUID playerId) {
        if (canBypassPacks(plugin, audience))
            return;

        List<AbstractResourcePack> packs = getPacksToLoadOnJoin();
        if (packs.isEmpty())
            return;

        PackSettings settings = PackSettings.get();
        PackPlayer packPlayer = PlayerPackCache.getInstance().getPlayer(playerId);
        if (packPlayer != null)
            packPlayer.resetJoinPackRetries();

        JoinPackScheduler.debounce(plugin, playerId, () -> {
            if (!plugin.isPlayerOnline(playerId))
                return;
            if (canBypassPacks(plugin, audience))
                return;
            loadMultiple(audience, playerId, packs, settings.joinReplace);
        });
    }

    public static boolean canBypassPacks(PackPlugin plugin, Audience audience) {
        return plugin.hasPermission(audience, "pack.bypass");
    }

    public static List<AbstractResourcePack> getPacksToLoadOnJoin() {
        Collection<AbstractResourcePack> cached = PackCache.getInstance().getAll();

        if (cached.isEmpty())
            return List.of();

        return new ArrayList<>(PackCache.getInstance().getAll().stream().filter(pack -> pack.isRequired() || pack.loadOnJoin()).toList());
    }

    /**
     * Used to get the most priority pack in a list of packs.
     *
     * @param packs a list of packs to compare.
     * @return the pack with most priority (lowest priority byte value).
     */
    public static AbstractResourcePack mostPriority(List<AbstractResourcePack> packs) {
        packs.sort(new PackComparator());
        return packs.get(0);
    }

    public static class PackComparator implements Comparator<AbstractResourcePack> {

        /**
         * Pack priority comparison
         *
         * @param p1 the first object to be compared.
         * @param p2 the second object to be compared.
         * @return
         */
        @Override
        public int compare(AbstractResourcePack p1, AbstractResourcePack p2) {
            return Byte.compare(p1.getPriority(), p2.getPriority());
        }
    }
}
