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

package com.sk89q.skmcl.minecraft;

import com.sk89q.skmcl.application.Instance;
import com.sk89q.skmcl.application.UpdateRequiredException;
import com.sk89q.skmcl.application.Version;
import com.sk89q.skmcl.concurrent.AbstractWorker;
import com.sk89q.skmcl.install.FileResource;
import com.sk89q.skmcl.install.ZipExtract;
import com.sk89q.skmcl.launch.JavaProcessBuilder;
import com.sk89q.skmcl.launch.LaunchContext;
import com.sk89q.skmcl.launch.LaunchedProcess;
import com.sk89q.skmcl.minecraft.model.Library;
import com.sk89q.skmcl.minecraft.model.ReleaseManifest;
import com.sk89q.skmcl.profile.Profile;
import com.sk89q.skmcl.session.Identity;
import com.sk89q.skmcl.session.Session;
import com.sk89q.skmcl.util.Environment;
import com.sk89q.skmcl.util.Persistence;
import com.sk89q.skmcl.util.Platform;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;

import static com.sk89q.skmcl.minecraft.model.Library.Extract;

/**
 * An installed version of Minecraft.
 */
@ToString
public class MinecraftInstall implements Instance {

    @Getter
    private final Profile profile;
    @Getter
    private final Version version;
    @Getter
    private final String versionPath;
    @Getter
    private final Environment environment;
    @Getter
    private JarBuilder jarPatcher;

    /**
     * Create a new instance.
     *
     * @param profile the profile containing this installation
     * @param version the version
     * @param environment the environment
     */
    public MinecraftInstall(@NonNull final Profile profile,
                            @NonNull final Version version,
                            @NonNull final Environment environment) {
        this.profile = profile;
        this.version = version;
        this.environment = environment;
        versionPath = String.format("versions/%1$s/%1$s", version.getId());

        // JAR patcher configuration
        File file = new File(getProfile().getContentDir(), versionPath + "-patches.json");
        File dir = new File(getProfile().getContentDir(), versionPath + "-jar-patches");
        File patchedJar = new File(getProfile().getContentDir(), versionPath + "-patched.jar");

        jarPatcher = Persistence.load(file, JarBuilder.class);
        jarPatcher.setPaths(getJarPath(), patchedJar, dir);
        Persistence.bind(jarPatcher, file);
    }

    /**
     * Get the path to the manifest .json file for this version.
     *
     * @return the path
     */
    public File getManifestPath() {
        return new File(getProfile().getContentDir(), versionPath + ".json");
    }

    /**
     * Get the path to the .jar file for this version.
     *
     * @return the path
     */
    public File getJarPath() {
        return new File(getProfile().getContentDir(), versionPath + ".jar");
    }

    /**
     * Get the path to shared assets directory.
     *
     * @return the path
     */
    public File getAssetsDir() {
        return new File(getProfile().getSharedDir(), "assets");
    }

    /**
     * Get the path to shared libraries directory.
     *
     * @return the path
     */
    public File getLibrariesDir() {
        return new File(getProfile().getSharedDir(), "libraries");
    }

    /**
     * Get the path to a new temporary directory to store extracted libraries.
     *
     * <p>The returned directory can be deleted at any point in time.</p>
     *
     * @return the path
     */
    protected File createExtractDir() {
        String id = "-natives-" + System.currentTimeMillis();
        return new File(getProfile().getContentDir(), versionPath + id);
    }

    @Override
    public AbstractWorker getUpdater() {
        return new MinecraftUpdater(this);
    }

    @Override
    public LaunchedProcess launch(final LaunchContext context)
            throws IOException, UpdateRequiredException {
        if (!getJarPath().exists()) {
            throw new UpdateRequiredException("Not yet installed");
        }

        Identity identity = context.getIdentity();
        ObjectMapper mapper = new ObjectMapper();
        final File extractDir = createExtractDir();
        JavaProcessBuilder builder = new JavaProcessBuilder();
        ReleaseManifest manifest = mapper.readValue(
                getManifestPath(), ReleaseManifest.class);
        String clientToken = identity.getClientToken();

        // Add libraries to classpath or extract the libraries as necessary
        for (Library library : manifest.getLibraries()) {
            File path = new File(getLibrariesDir(), library.getPath(context.getEnvironment()));

            if (path.exists()) {
                Extract extract = library.getExtract();
                if (extract != null) {
                    ZipExtract zipExtract = new ZipExtract(
                            new FileResource(path), extractDir);
                    zipExtract.setExclude(extract.getExclude());
                    zipExtract.run();
                } else {
                    builder.classPath(path);
                }
            }
        }

        // Add arguments for the .jar
        String[] args = manifest.getMinecraftArguments().split(" +");
        for (String arg : args) {
            arg = arg.replace("${version_name}", manifest.getId());
            arg = arg.replace("${game_directory}", getProfile().getContentDir().getAbsolutePath());
            arg = arg.replace("${game_assets}", getAssetsDir().getAbsolutePath());
            arg = arg.replace("${auth_player_name}", identity.getName());
            arg = arg.replace("${auth_username}", identity.getName());
            arg = arg.replace("${auth_access_token}", identity.getAccessToken());
            arg = arg.replace("${auth_session}", clientToken);
            builder.getArgs().add(arg);
        }

        // Mac OS X arguments
        if (getEnvironment().getPlatform() == Platform.MAC_OS_X) {
            File icnsPath = new File(getAssetsDir(), "icons/minecraft.icns");
            builder.getFlags().add("-Xdock:icon=" + icnsPath.getAbsolutePath());
            builder.getFlags().add("-Xdock:name=Minecraft");
        }

        builder.getFlags().add("-Djava.library.path=" + extractDir.getAbsoluteFile());
        builder.classPath(getJarPatcher().getExecutedPath());
        builder.setMainClass(manifest.getMainClass());

        ProcessBuilder processBuilder = new ProcessBuilder(builder.buildCommand());
        processBuilder.directory(getProfile().getContentDir());
        Process process = processBuilder.start();

        // Return the process
        return new LaunchedProcess(process) {
            @Override
            public void close() throws IOException {
                FileUtils.deleteDirectory(extractDir);
            }
        };
    }

}
