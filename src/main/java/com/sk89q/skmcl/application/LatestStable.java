/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010, 2011 Albert Pham <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.skmcl.application;

import java.io.IOException;

import static com.sk89q.skmcl.util.SharedLocale._;

/**
 * Indicates the latest stable version.
 */
public class LatestStable extends OnlineVersion {

    @Override
    public String getId() {
        return "stable";
    }

    @Override
    public void setId(String id) {
    }

    @Override
    public String getName() {
        return _("version.latestStable");
    }

    @Override
    public Version resolveOnline(Application application) throws IOException {
        try {
            return application.getLatestStable();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted");
        }
    }

    @Override
    boolean thisEquals(Version other) {
        return super.thisEquals(other) && other instanceof LatestStable;
    }
}
