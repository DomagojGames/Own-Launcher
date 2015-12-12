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

package com.sk89q.skmcl.minecraft.model;

import com.sk89q.skmcl.application.Version;
import lombok.Data;
import lombok.NonNull;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReleaseList {

    private LatestReleases latest;
    private List<Version> versions;

    /**
     * Get a release with the given ID.
     *
     * @param id the ID
     * @return the release
     */
    public Version find(@NonNull String id) {
        for (Version version : getVersions()) {
            if (version.getId().equals(id)) {
                return version;
            }
        }
        return null;
    }

    @Data
    public static class LatestReleases {
        private String snapshot;
        private String release;
    }

}
