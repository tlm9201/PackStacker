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

import com.google.inject.Inject;
import com.timomcgrath.packstacker.command.PackCommand;
import com.timomcgrath.packstacker.factory.VelocityResourcePackFactory;
import com.timomcgrath.packstacker.listener.PackListener;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.audience.Audience;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Plugin(
        id = "packstacker",
        name = "PackStacker",
        version = BuildConstants.VERSION,
        authors = {"tlm920"}
)
public class PackStacker implements PackPlugin {
    private static PackStacker instance;
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    @Inject
    public PackStacker(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        instance = this;

        logger.info("PackStacker-velocity enabled!");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        FileLoader fileLoader = new FileLoader(dataDirectory);
        fileLoader.loadMessages();
        fileLoader.loadPacks(new VelocityResourcePackFactory());

        server.getEventManager().register(this, new PackListener());

        CommandManager commandManager = server.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("pack")
                .aliases("packstacker", "resourcepack")
                .plugin(this)
                .build();

        SimpleCommand packCommand = new PackCommand(this);
        commandManager.register(commandMeta, packCommand);
    }

    public ProxyServer getServer() {
        return server;
    }

    public static PackStacker getInstance() {
        return instance;
    }

    @Override
    public void reloadMessages() {
        FileLoader fileLoader = new FileLoader(dataDirectory);
        fileLoader.loadMessages();
    }

    @Override
    public void reloadPacks() {
        FileLoader fileLoader = new FileLoader(dataDirectory);
        fileLoader.loadPacks(new VelocityResourcePackFactory());
    }

    @Override
    public void reloadAll() {
        FileLoader fileLoader = new FileLoader(dataDirectory);
        fileLoader.loadMessages();
        fileLoader.loadPacks(new VelocityResourcePackFactory());
    }

    @Override
    public boolean hasPermission(Audience audience, String permission) {
        if (!(audience instanceof Player player))
            return false;

        return player.hasPermission(permission);
    }

    @Override
    public List<String> getOnlinePlayers() {
        return server.getAllPlayers().parallelStream().map(player -> player.getUsername().toLowerCase()).collect(Collectors.toList());
    }
}
