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

package com.sk89q.skmcl.install;

import lombok.Getter;
import lombok.ToString;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;

@ToString
public class HttpResource implements Resource {

    @Getter
    private final URL url;
    private FileResource resource;
    private String versionId;

    public HttpResource(URL url) {
        this.url = url;
    }

    public HttpResource withId(String versionId) {
        this.versionId = versionId;
        return this;
    }

    @Override
    public void setInstaller(InstallerRuntime installer) {
        resource = new FileResource(installer.fetch(url, versionId)).deleteOnCleanup();
    }

    @Override
    public InputStream getInputStream() throws FileNotFoundException {
        return resource.getInputStream();
    }

    @Override
    public void cleanup() {
        resource.cleanup();
    }

}
