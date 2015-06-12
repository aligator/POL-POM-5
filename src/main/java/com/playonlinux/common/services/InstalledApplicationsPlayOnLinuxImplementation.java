/*
 * Copyright (C) 2015 PÂRIS Quentin
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

package com.playonlinux.common.services;

import com.playonlinux.app.PlayOnLinuxContext;
import com.playonlinux.common.api.filter.Filter;
import com.playonlinux.common.api.services.BackgroundServiceManager;
import com.playonlinux.common.api.services.InstalledApplications;
import com.playonlinux.common.dto.ui.ShortcutDTO;
import com.playonlinux.domain.PlayOnLinuxException;
import com.playonlinux.domain.Shortcut;
import com.playonlinux.domain.ShortcutSet;
import com.playonlinux.injection.Inject;
import com.playonlinux.injection.Scan;
import com.playonlinux.utils.ObservableDirectoryFiles;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Scan
public class InstalledApplicationsPlayOnLinuxImplementation extends Observable implements InstalledApplications, Observer {
    @Inject
    static PlayOnLinuxContext playOnLinuxContext;

    @Inject
    static BackgroundServiceManager playOnLinuxBackgroundServicesManager;

    ShortcutSet shortcutSet;
    private Iterator<ShortcutDTO> shortcutDtoIterator;
    private List<ShortcutDTO> cache = null;

    InstalledApplicationsPlayOnLinuxImplementation() throws PlayOnLinuxException {
        File shortcutDirectory = playOnLinuxContext.makeShortcutsScriptsPath();
        File iconDirectory = playOnLinuxContext.makeShortcutsIconsPath();
        File configFilesDirectory = playOnLinuxContext.makeShortcutsConfigPath();
        URL defaultIcon = playOnLinuxContext.makeDefaultIconURL();

        ObservableDirectoryFiles shortcutDirectoryObservable = new ObservableDirectoryFiles(shortcutDirectory);
        ObservableDirectoryFiles iconDirectoryObservable = new ObservableDirectoryFiles(iconDirectory);

        playOnLinuxBackgroundServicesManager.register(shortcutDirectoryObservable);
        playOnLinuxBackgroundServicesManager.register(iconDirectoryObservable);

        shortcutSet = new ShortcutSet(shortcutDirectoryObservable, iconDirectoryObservable,
                configFilesDirectory, defaultIcon);

        shortcutSet.addObserver(this);
    }

    protected void finalize() throws Throwable {
        try {
            shortcutSet.deleteObserver(this);
        } finally {
            super.finalize();
        }
    }

    @Override
    public synchronized void update(Observable o, Object arg) {
        shortcutDtoIterator = new Iterator<ShortcutDTO>() {
            volatile int i = 0;

            @Override
            public boolean hasNext() {
                assert(arg instanceof List);
                return ((List<Shortcut>) arg).size() > i;
            }

            @Override
            public ShortcutDTO next() {
                assert(arg instanceof List);
                List<Shortcut> shortcutList = ((List<Shortcut>) arg);
                if(i >= shortcutList.size()) {
                    throw new NoSuchElementException();
                }
                Shortcut shortcut = shortcutList.get(i);
                i++;
                return new ShortcutDTO.Builder()
                        .withName(shortcut.getShortcutName())
                        .withIcon(shortcut.getIconPath())
                        .build();
            }
        };

        this.setChanged();
        this.notifyObservers();
    }

    @Override
    public synchronized Iterator<ShortcutDTO> iterator() {
        return this.shortcutDtoIterator;
    }

    @Override
    public List<ShortcutDTO> getFiltered(Filter<ShortcutDTO> filter) {
        List<ShortcutDTO> filtered = new ArrayList<>();



        List<ShortcutDTO> copy = copyIterator(shortcutDtoIterator);
        System.out.print(shortcutDtoIterator.hasNext());
        System.out.print("Size: " + copy.size());

        for(ShortcutDTO s : copy) {
            System.out.print(s.getName());
        }

        filtered.addAll(copy.stream().filter(filter::apply).collect(Collectors.toList()));
        return filtered;
    }

    @Override
    public int size() {
        updateCache();
        return cache.size();
    }

    @Override
    public ShortcutDTO[] toArray() {
        return cache.toArray(new ShortcutDTO[cache.size()]);
    }

    private void updateCache() {

    }

    public static <T> List<T> copyIterator(Iterator<T> iter) {
        List<T> copy = new ArrayList<T>();
        while (iter.hasNext())
            copy.add(iter.next());
        return copy;
    }
}
