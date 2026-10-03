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

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.util.GameProfile;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Clients drop player skins while applying a resource pack. Reapply the skin
 * already on the proxy profile through SkinsRestorer. The backend has to be
 * running SkinsRestorer too, or the refresh never reaches the client.
 */
public final class SkinRefresh {
    private static final String SKINS_RESTORER_ID = "skinsrestorer";
    private static final ConcurrentHashMap<UUID, Long> PENDING = new ConcurrentHashMap<>();
    private static final AtomicLong TOKENS = new AtomicLong();
    private static final AtomicBoolean MISSING_LOGGED = new AtomicBoolean();
    private static final AtomicBoolean FAILURE_LOGGED = new AtomicBoolean();

    private SkinRefresh() {
    }

    public static void schedule(Player player) {
        PackStacker plugin = PackStacker.getInstance();
        if (plugin == null || plugin.getServer() == null)
            return;

        int delayMs = PackSettings.get().skinRefreshDelayMs;
        if (delayMs <= 0)
            return;

        UUID playerId = player.getUniqueId();
        long token = TOKENS.incrementAndGet();
        PENDING.put(playerId, token);
        plugin.scheduleDelayed(() -> {
            if (!PENDING.remove(playerId, token))
                return;
            refresh(playerId);
        }, delayMs);
    }

    public static void cancel(UUID playerId) {
        PENDING.remove(playerId);
    }

    private static void refresh(UUID playerId) {
        PackStacker plugin = PackStacker.getInstance();
        if (plugin == null || plugin.getServer() == null)
            return;

        Optional<Player> online = plugin.getServer().getPlayer(playerId);
        if (online.isEmpty())
            return;

        Player player = online.get();
        String[] textures = currentTextures(player);
        if (textures == null)
            return;

        Optional<PluginContainer> container = plugin.getServer().getPluginManager().getPlugin(SKINS_RESTORER_ID);
        if (container.isEmpty() || container.get().getInstance().isEmpty()) {
            logMissing(plugin);
            return;
        }

        Object skinsRestorer = container.get().getInstance().get();
        ClassLoader loader = skinsRestorer.getClass().getClassLoader();
        ClassLoader previous = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
        try {
            // Velocity isolates plugin classloaders, so SkinsRestorer's API has to
            // be loaded from its own plugin. No-arg applySkin() skips Mojang skins
            // while alwaysApplyPremium is false, so resend the current textures.
            Class<?> provider = Class.forName("net.skinsrestorer.api.SkinsRestorerProvider", true, loader);
            Object api = provider.getMethod("get").invoke(null);
            Class<?> playerClass = velocityPlayerClass(player);
            Object applier = api.getClass().getMethod("getSkinApplier", Class.class).invoke(api, playerClass);
            Class<?> propertyType = Class.forName("net.skinsrestorer.api.property.SkinProperty", true, loader);
            Object property = propertyType.getMethod("of", String.class, String.class)
                    .invoke(null, textures[0], textures[1]);
            findApply(applier.getClass(), propertyType).invoke(applier, player, property);
        } catch (Exception | LinkageError thrown) {
            Throwable cause = thrown instanceof InvocationTargetException && thrown.getCause() != null
                    ? thrown.getCause()
                    : thrown;
            logFailure(plugin, player, cause);
        } finally {
            Thread.currentThread().setContextClassLoader(previous);
        }
    }

    private static String[] currentTextures(Player player) {
        for (GameProfile.Property property : player.getGameProfileProperties()) {
            if (!"textures".equals(property.getName()))
                continue;

            String value = property.getValue();
            String signature = property.getSignature();
            if (value == null || value.isEmpty() || signature == null || signature.isEmpty())
                continue;

            return new String[]{value, signature};
        }
        return null;
    }

    private static Class<?> velocityPlayerClass(Player player) {
        Class<?> found = findInterface(player.getClass(), "com.velocitypowered.api.proxy.Player");
        return found != null ? found : Player.class;
    }

    private static Class<?> findInterface(Class<?> type, String name) {
        while (type != null) {
            for (Class<?> iface : type.getInterfaces()) {
                Class<?> found = findInInterface(iface, name);
                if (found != null)
                    return found;
            }
            type = type.getSuperclass();
        }
        return null;
    }

    private static Class<?> findInInterface(Class<?> iface, String name) {
        if (iface.getName().equals(name))
            return iface;
        for (Class<?> parent : iface.getInterfaces()) {
            Class<?> found = findInInterface(parent, name);
            if (found != null)
                return found;
        }
        return null;
    }

    private static Method findApply(Class<?> applierType, Class<?> propertyType) throws NoSuchMethodException {
        Method bridge = null;
        for (Method method : applierType.getMethods()) {
            if (!"applySkin".equals(method.getName()) || method.getParameterCount() != 2)
                continue;
            if (!method.getParameterTypes()[1].getName().equals(propertyType.getName()))
                continue;

            String playerType = method.getParameterTypes()[0].getName();
            if (playerType.equals("com.velocitypowered.api.proxy.Player")) {
                method.setAccessible(true);
                return method;
            }
            if (playerType.equals("java.lang.Object"))
                bridge = method;
        }
        if (bridge != null) {
            bridge.setAccessible(true);
            return bridge;
        }
        throw new NoSuchMethodException("SkinsRestorer applySkin(Player, SkinProperty)");
    }

    private static void logMissing(PackStacker plugin) {
        if (MISSING_LOGGED.compareAndSet(false, true))
            plugin.getLogger().warn("SkinsRestorer is not loaded, so skins cleared by a resource pack cannot be restored.");
    }

    private static void logFailure(PackStacker plugin, Player player, Throwable cause) {
        if (FAILURE_LOGGED.compareAndSet(false, true))
            plugin.getLogger().warn("Could not reapply skin for " + player.getUsername(), cause);
        else
            plugin.getLogger().warn("Could not reapply skin for {}: {}", player.getUsername(), cause.toString());
    }
}
