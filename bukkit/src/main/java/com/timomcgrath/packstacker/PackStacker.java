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

import com.timomcgrath.packstacker.listener.PackListener;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public final class PackStacker extends JavaPlugin implements PackPlugin {
    private static PackStacker plugin;
    private final File packDataFolder = new File(getDataFolder(), "packs");
    private File messagesFile = new File(getDataFolder(), "messages.yml");

    public PackStacker() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        reloadAll();

        getCommand("pack").setExecutor(new PackCommand(this));
        Bukkit.getPluginManager().registerEvents(new PackListener(), this);
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void reloadMessages() {
        FileLoader fileLoader = new FileLoader(getDataFolder().toPath());
        fileLoader.loadMessages();
    }

    @Override
    public void reloadPacks() {
        FileLoader fileLoader = new FileLoader(getDataFolder().toPath());
        fileLoader.loadPacks(new BukkitResourcePackFactory());
    }

    @Override
    public void reloadAll() {
        FileLoader fileLoader = new FileLoader(getDataFolder().toPath());
        fileLoader.loadMessages();
        fileLoader.loadPacks(new BukkitResourcePackFactory());
    }

    @Override
    public boolean hasPermission(Audience audience, String permission) {
        if (!(audience instanceof Player player))
            return true;

        return player.hasPermission(permission);
    }

    @Override
    public List<String> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers().parallelStream().map(player -> player.getName().toLowerCase()).collect(Collectors.toList());
    }

    @NotNull
    public static PackStacker getPlugin() {
        if (plugin == null)
            throw new IllegalStateException("Attempted to obtain plugin instance while plugin is null.");
        return plugin;
    }

    @NotNull
    private static List<FileConfiguration> getPackConfigurations(File[] files) {
        List<File> fileList = new ArrayList<>(List.of(files));
        for (Iterator<File> i = fileList.iterator(); i.hasNext();) {
            File file = i.next();
            String fileName = file.getName();
            String extension = "";
            int j = fileName.lastIndexOf('.');
            if (j > 0)
                extension = fileName.substring(j+1);

            if (!extension.equals("pack"))
                i.remove();
        }

        List<FileConfiguration> packList = new LinkedList<>();
        fileList.forEach(pack -> packList.add(YamlConfiguration.loadConfiguration(pack)));
        return packList;
    }
}
