package com.timomcgrath.packstacker;

import com.timomcgrath.packstacker.listener.PackListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public final class PackStacker extends JavaPlugin {
    private static PackStacker plugin;
    private final File packDataFolder = new File(getDataFolder(), "packs");
    private File messagesFile = new File(getDataFolder(), "messages.yml");

    public PackStacker() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        reloadPacks();
        reloadMessages();

        getCommand("pack").setExecutor(new PackCommand());
        Bukkit.getPluginManager().registerEvents(new PackListener(), this);
    }

    @Override
    public void onDisable() {
    }

    @NotNull
    public static PackStacker getPlugin() {
        if (plugin == null)
            throw new IllegalStateException("Attempted to obtain plugin instance while plugin is null.");
        return plugin;
    }

    public void reloadPacks() {
        if (!packDataFolder.exists())
            packDataFolder.mkdirs();

        File[] files = packDataFolder.listFiles();
        if (files == null)
            return;

        List<FileConfiguration> packList = getPackConfigurations(files);

        PackCache packCache = PackCache.getInstance();
        packCache.reset();
        Collection<ResourcePack> packs = new LinkedList<>();
        packList.forEach(config -> packs.add(ResourcePack.create(config)));
        packCache.addAll(packs);
    }

    public void reloadMessages() {
        if (!messagesFile.exists()) {
            messagesFile.getParentFile().mkdirs();
            saveResource("messages.yml", false);
        }
        Messaging.init(YamlConfiguration.loadConfiguration(messagesFile));
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
