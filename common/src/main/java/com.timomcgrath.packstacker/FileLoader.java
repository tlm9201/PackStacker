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

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FileLoader {
    private final Path dataDirectory, packsDirectory;

    public FileLoader(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.packsDirectory = dataDirectory.resolve("packs");

        if (Files.notExists(dataDirectory)) {
            try {
                Files.createDirectory(dataDirectory);
                Files.createDirectory(packsDirectory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public void loadMessages() {
        Path messages = dataDirectory.resolve("messages.yml");
        try {
            if (Files.notExists(messages)) {
                Files.copy(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("messages.yml")), messages);
            }
            YAMLConfigurationLoader loader = YAMLConfigurationLoader.builder().setPath(messages).build();
            ConfigurationNode root = loader.load();
            Messaging.init(root);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadPacks(ResourcePackFactory factory) {
        PackCache packCache = PackCache.getInstance();
        packCache.reset();

        List<ConfigurationNode> roots = getPackConfigurations(packsDirectory.toFile().listFiles());
        Collection<AbstractResourcePack> packs = new LinkedList<>();
        roots.forEach(root -> packs.add(factory.create(root)));

        packCache.addAll(packs);
    }

    @NotNull
    private static List<ConfigurationNode> getPackConfigurations(File[] files) {
        List<File> fileList = new ArrayList<>(List.of(files));
        for (Iterator<File> i = fileList.iterator(); i.hasNext(); ) {
            File file = i.next();
            String fileName = file.getName();
            String extension = "";
            int j = fileName.lastIndexOf('.');
            if (j > 0)
                extension = fileName.substring(j + 1);

            if (!extension.equals("pack"))
                i.remove();
        }

        List<ConfigurationNode> packList = new LinkedList<>();
        fileList.forEach(pack -> {
            try {
                packList.add(YAMLConfigurationLoader.builder().setFile(pack).build().load());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return packList;
    }
}
