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

import io.javalin.Javalin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Map;

public class GithubEndpoint {
    private PackPlugin packPlugin;
    private Javalin app;
    private Yaml yaml = new Yaml();

    public GithubEndpoint(PackPlugin packPlugin) {
        this.packPlugin = packPlugin;
    }

    public void init() {
        app = Javalin.create().start(PackSettings.get().githubPort);
        app.post("/", ctx -> post(ctx.body()));
        packPlugin.log("GitHub REST server initialized on port " + app.port());
    }

    private void post(String jsonStr) throws JSONException {
        JSONObject json;
        try {
            json = new JSONObject(jsonStr);
        } catch (JSONException e) {
            packPlugin.log("POST request contains invalid JSON! Exiting...");
            return;
        }

        if (!json.has("action") || !json.has("release")) {
            packPlugin.log("Payload recieved, but not a GitHub publication event! Exiting...");
            return;
        }

        JSONObject release = json.getJSONObject("release");
        packPlugin.log("GitHub publication event received! Processing...");
        if (!release.has("assets"))
            return;

        String body = release.getString("body");
        Map<String, Object> map = yaml.load(body);

        JSONArray assets = release.getJSONArray("assets");
        for (int i = 0; i < assets.length(); i++) {
            JSONObject entry = assets.getJSONObject(i);
            String name = (String) entry.get("name");
            String n = name;
            int j = name.indexOf('.');
            int h = name.indexOf('.', j + 1);
            if (j > 0)
                name = name.substring(0, j);

            String hash = (String) map.get(name);

            if (!isValidSHA1(hash)) {
                packPlugin.log("Invalid SHA-1 hash for pack \"" + name + "\"! Exiting...");
                return;
            }

            PackCache packCache = PackCache.getInstance();
            AbstractResourcePack pack = packCache.get(name);
            if (pack != null) {
                packCache.remove(name);
                pack.setUrl(pack.getUrl());
                pack.setHash(hash);
                pack.reloadPackInfo();
                packCache.add(pack);
                packPlugin.invokeGithubRelease(pack);
                packPlugin.log("Found valid pack in release assets! Applying...");
            } else {
                packPlugin.log("Could not find a PackStacker pack for \"" + name + "\"!");
                packPlugin.log("Processing GitHub publishing event failed for pack \"" + name + "\"");
            }
        }
        if (!assets.isEmpty())
            packPlugin.reloadPlayers();
    }

    public static String checkHashURL(String input) {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            InputStream is = new URL(input).openStream();

            try {
                is = new DigestInputStream(is, sha1);

                int b;

                while (is.read() != 1) {
                    ;
                }
            } finally {
                is.close();
            }
            byte[] digest = sha1.digest();
            return SHAsum(digest);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String SHAsum(byte[] convertme) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        return byteArray2Hex(md.digest(convertme));
    }

    private static String byteArray2Hex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    private boolean isValidSHA1(String string) {
        return string.matches("^[a-fA-F0-9]{40}$");
    }
}
