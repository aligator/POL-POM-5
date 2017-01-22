/*
 * Copyright (C) 2015-2017 PÂRIS Quentin
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.playonlinux.containers.wine;

import com.playonlinux.containers.ContainersManager;
import com.playonlinux.containers.dto.ContainerDTO;
import com.playonlinux.containers.dto.WinePrefixDTO;
import com.playonlinux.containers.wine.configurations.WinePrefixDisplayConfiguration;
import com.playonlinux.containers.wine.configurations.WinePrefixInputConfiguration;
import com.playonlinux.tools.config.CompatibleConfigFileFormatFactory;
import com.playonlinux.tools.config.ConfigFile;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class WinePrefixesManager implements ContainersManager {
    @Value("${application.user.wineprefix}")
    private String winePrefixPath;

    private final CompatibleConfigFileFormatFactory compatibleConfigFileFormatFactory;
    private final WinePrefixDisplayConfiguration winePrefixDisplayConfiguration;
    private final WinePrefixInputConfiguration winePrefixInputConfiguration;

    public WinePrefixesManager(CompatibleConfigFileFormatFactory compatibleConfigFileFormatFactory,
                               WinePrefixDisplayConfiguration winePrefixDisplayConfiguration,
                               WinePrefixInputConfiguration winePrefixInputConfiguration) {
        this.compatibleConfigFileFormatFactory = compatibleConfigFileFormatFactory;
        this.winePrefixDisplayConfiguration = winePrefixDisplayConfiguration;
        this.winePrefixInputConfiguration = winePrefixInputConfiguration;
    }

    @Override
    public void fetchContainers(Consumer<List<ContainerDTO>> callback, Consumer<Exception> errorCallback) {
        final File winePrefixesFile = new File(winePrefixPath);
        winePrefixesFile.mkdirs();

        final File[] winePrefixes = winePrefixesFile.listFiles();

        if(winePrefixes == null) {
            callback.accept(Collections.emptyList());
        } else {
            final List<ContainerDTO> containers = new ArrayList<>();
            for (File winePrefix : winePrefixes) {
                final ConfigFile configFile = compatibleConfigFileFormatFactory.open(new File(winePrefix, "playonlinux.cfg"));
                final File userRegistryFile = new File(winePrefix, "user.reg");

                containers.add(
                        new WinePrefixDTO.Builder()
                            .withName(winePrefix.getName())
                            .withPath(winePrefix.getAbsolutePath())
                            .withArchitecture(configFile.readValue("wineArchitecture", ""))
                            .withDistribution(configFile.readValue("wineDistribution", ""))
                            .withVersion(configFile.readValue("wineVersion", ""))
                            .withGlslValue(winePrefixDisplayConfiguration.getGLSL(userRegistryFile))
                            .withDirectDrawRenderer(winePrefixDisplayConfiguration.getDirectDrawRenderer(userRegistryFile))
                            .withVideoMemorySize(winePrefixDisplayConfiguration.getVideoMemorySize(userRegistryFile))
                            .withOffscreenRenderingMode(winePrefixDisplayConfiguration.getOffscreenRenderingMode(userRegistryFile))
                            .withMultisampling(winePrefixDisplayConfiguration.getMultisampling(userRegistryFile))
                            .withAlwaysOffscreen(winePrefixDisplayConfiguration.getAlwaysOffscreen(userRegistryFile))
                            .withStrictDrawOrdering(winePrefixDisplayConfiguration.getStrictDrawOrdering(userRegistryFile))
                            .withRenderTargetModeLock(winePrefixDisplayConfiguration.getRenderTargetModeLock(userRegistryFile))
                            .withMouseWarpOverride(winePrefixInputConfiguration.getMouseWarpOverride(userRegistryFile))
                            .build()
                );
            }

            containers.sort(ContainerDTO.nameComparator());

            callback.accept(containers);
        }
    }

}
