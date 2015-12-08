/*
 * Copyright (C) 2011-2015 clueminer.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.clueminer.demo;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import org.clueminer.utils.exec.ResourceLoader;

/**
 *
 * @author deric
 */
public class ResLoader extends ResourceLoader {

    protected static final String prefix = "/org/clueminer/demo";
    protected static final String hintPackage = "demo";


    /**
     * Resource packed in jar is not possible to open directly, this method uses
     * a .tmp file which should be on exit deleted
     *
     * @param path
     * @return
     */
    @Override
    public File resource(String path) {
        return resource(path, prefix, hintPackage);
    }

    @Override
    public Enumeration<URL> searchURL(String path) throws IOException {
        return ResLoader.class.getClassLoader().getResources(path);
    }

}
