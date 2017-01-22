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

package com.playonlinux.containers.wine.parameters;

import com.playonlinux.win32.registry.RegistryWriter;

import static org.junit.Assert.assertEquals;

public abstract class AbstractRegistryParameterTest<T extends RegistryParameter> {
    private final RegistryWriter registryWriter = new RegistryWriter();

    protected void testStringValue(T value, String expected) {
        assertEquals("WINE REGISTRY Version 2\n" +
                ";; Generated by PlayOnLinux\n" +
                "\n" +
                "[HKEY_CURRENT_USER\\Software\\Wine\\Direct3D]\n" +
                "\""+value.getClass().getSimpleName()+"\"=\""+expected+"\"", registryWriter.generateRegFileContent(value.toRegistryPatch()).trim());
    }

    protected void testRemoveValue(T value) {
        assertEquals("WINE REGISTRY Version 2\n" +
                ";; Generated by PlayOnLinux\n" +
                "\n" +
                "[HKEY_CURRENT_USER\\Software\\Wine\\Direct3D]\n" +
                "\""+value.getClass().getSimpleName()+"\"=-", registryWriter.generateRegFileContent(value.toRegistryPatch()).trim());
    }
}
