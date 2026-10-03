/*
 * PackStacker
 * Copyright (C) 2026 Timo McGrath
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
import com.velocitypowered.api.proxy.player.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/** Run with :velocity:regressionCheck; no server or test framework required. */
public class VelocityCompatibilityCheck {
    public static void main(String[] args) throws Exception {
        UUID playerId = UUID.randomUUID();
        List<ResourcePackInfo> applied = new ArrayList<>();
        List<ResourcePackInfo> pending = new ArrayList<>();
        List<ResourcePackRequest> sent = new ArrayList<>();
        List<Component> messages = new ArrayList<>();
        Player player = (Player) Proxy.newProxyInstance(Player.class.getClassLoader(), new Class<?>[]{Player.class},
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "getUniqueId" -> playerId;
                    case "getAppliedResourcePacks" -> applied;
                    case "getPendingResourcePacks" -> pending;
                    case "sendMessage" -> {
                        messages.add((Component) arguments[0]);
                        yield null;
                    }
                    case "sendResourcePacks" -> {
                        ResourcePackRequest request = (ResourcePackRequest) arguments[0];
                        sent.add(request);
                        request.packs().forEach(pack -> pending.add(nativePack(pack.hash())));
                        yield null;
                    }
                    default -> throw new AssertionError("Unexpected Player call: " + method.getName());
                });

        AbstractResourcePack pack = pack("one", "1".repeat(40), (byte) 1);
        applied.add(nativePack(pack.getHash()));
        // Native state must win even when the plugin's cache has no record of this pack.
        PackStackerUtil.loadMultiple(player, playerId, List.of(pack), true);
        check(sent.isEmpty(), "Reload must skip an unchanged applied pack");

        applied.clear();
        PackStackerUtil.loadMultiple(player, playerId, List.of(pack), false);
        PackStackerUtil.loadMultiple(player, playerId, List.of(pack), false);
        check(sent.size() == 1, "Repeated join must skip pending packs");

        pending.clear();
        applied.add(nativePack(pack.getHash()));
        pack.setHash("2".repeat(40));
        pack.reloadPackInfo();
        PackStackerUtil.loadMultiple(player, playerId, List.of(pack), true);
        check(sent.size() == 2, "An updated hash must still be sent");
        check(sent.get(1).packs().get(0).hash().equals(pack.getHash()), "Send the new hash");

        pending.clear();
        applied.clear();
        AbstractResourcePack duplicate = pack("alias", pack.getHash(), (byte) 2);
        AbstractResourcePack priority = pack("priority", "3".repeat(40), (byte) 0);
        PackStackerUtil.loadMultiple(player, playerId, List.of(duplicate, pack, priority), true);
        ResourcePackRequest request = sent.get(2);
        check(request.packs().size() == 2, "Deduplicate hashes within one request");
        check(request.packs().get(0).id().equals(priority.getUuid()), "Preserve priority order");
        check(request.packs().get(1).id().equals(pack.getUuid()), "Keep the highest priority alias");
        check(pack.isApplied(player, playerId), "Single-pack loads must also detect pending packs");
        pack.load(player, playerId);
        check(sent.size() == 3, "Single-pack loads must skip pending packs");

        PackStacker plugin = new PackStacker(null, LoggerFactory.getLogger("compatibility-check"), Path.of("."));
        SkinRefresh.schedule(player);
        var notification = PackStacker.class.getDeclaredMethod("sendPackUpdateNotification", Player.class, AbstractResourcePack.class);
        notification.setAccessible(true);
        notification.invoke(plugin, player, pack);
        check(messages.get(messages.size() - 1).clickEvent() != null, "Update notification must have a command click");
        System.out.println("Velocity compatibility checks passed");
    }

    private static AbstractResourcePack pack(String name, String hash, byte priority) {
        return new ResourcePack(null, name, hash, null, "https://example.com/pack.zip", priority, false, true);
    }

    private static ResourcePackInfo nativePack(String hash) {
        return (ResourcePackInfo) Proxy.newProxyInstance(ResourcePackInfo.class.getClassLoader(),
                new Class<?>[]{ResourcePackInfo.class}, (proxy, method, arguments) -> {
                    if (method.getName().equals("getHash"))
                        return HexFormat.of().parseHex(hash);
                    throw new AssertionError("Unexpected pack call: " + method.getName());
                });
    }

    private static void check(boolean condition, String message) {
        if (!condition)
            throw new AssertionError(message);
    }
}
