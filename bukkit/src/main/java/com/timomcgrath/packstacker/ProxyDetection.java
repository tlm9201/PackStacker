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

public final class ProxyDetection {
    private static Boolean behindVelocity;

    private ProxyDetection() {
    }

    public static boolean isBehindVelocity() {
        if (behindVelocity != null)
            return behindVelocity;

        behindVelocity = detectVelocity();
        return behindVelocity;
    }

    private static boolean detectVelocity() {
        try {
            Class<?> configClass = Class.forName("io.papermc.paper.configuration.GlobalConfiguration");
            Object config = configClass.getMethod("get").invoke(null);
            Object proxies = configClass.getField("proxies").get(config);
            Object velocity = proxies.getClass().getField("velocity").get(proxies);
            return velocity.getClass().getField("enabled").getBoolean(velocity);
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }
}