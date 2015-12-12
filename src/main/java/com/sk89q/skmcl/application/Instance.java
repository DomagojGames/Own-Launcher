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

import com.sk89q.skmcl.concurrent.AbstractWorker;
import com.sk89q.skmcl.launch.LaunchContext;
import com.sk89q.skmcl.launch.LaunchedProcess;

import java.io.IOException;

/**
 * An installed application instance.
 */
public interface Instance {

    /**
     * Get the current version of this instance.
     *
     * @return the version
     */
    Version getVersion();

    /**
     * Get a worker to update the instance.
     *
     * @return an update worker
     */
    AbstractWorker<Instance> getUpdater();

    /**
     * Launch this instance.
     *
     * @param context the launch context
     * @return the launched process
     * @throws UpdateRequiredException thrown if an update must be performed first
     * @throws IOException on any sort of I/O error
     */
    LaunchedProcess launch(LaunchContext context)
            throws UpdateRequiredException, IOException;

}
