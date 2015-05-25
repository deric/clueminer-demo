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
package org.clueminer.demo.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.openide.util.Exceptions;

/**
 * code from
 * @link http://stackoverflow.com/questions/3923129/get-a-list-of-resources-from-classpath-directory
 * @author deric
 */
public class ResourceList {

    private static final String OS = System.getProperty("os.name").toLowerCase();

    /**
     * for all elements of java.class.path get a Collection of resources Pattern
     * pattern = Pattern.compile(".*"); gets all resources
     *
     * @param pattern the pattern to match
     * @return the resources in the order they are found
     */
    public static Collection<String> getResources(final Pattern pattern) {
        final List<String> retval = new LinkedList<>();
        final String classPath = System.getProperty("java.class.path", ".");
        String pathSeparator;
        if (isWindows()) {
            pathSeparator = ";";
        } else {
            pathSeparator = ":";
        }
        final String[] classPathElements = classPath.split(pathSeparator);
        for (final String element : classPathElements) {
            retval.addAll(getResources(element, pattern));
        }
        return retval;
    }

    private static Collection<String> getResources(final String element, final Pattern pattern) {
        final List<String> retval = new LinkedList<>();
        final File file = new File(element);
        if (file.isDirectory()) {
            retval.addAll(getResourcesFromDirectory(file, pattern));
        } else {
            if (file.exists()) {
                retval.addAll(getResourcesFromJarFile(file, pattern));
            } else {
                System.err.println("can't open file: " + file);
            }
        }
        return retval;
    }

    private static Collection<String> getResourcesFromJarFile(final File file,
            final Pattern pattern) {
        final List<String> retval = new LinkedList<>();
        ZipInputStream zip = null;
        try {
            zip = new ZipInputStream(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }

        if (zip != null) {
            try {
                while (zip.available() == 1) {
                    final ZipEntry ze = zip.getNextEntry();
                    final String fileName = ze.getName();
                    final boolean accept = pattern.matcher(fileName).matches();
                    if (accept) {
                        retval.add(fileName);
                    }
                    zip.closeEntry();
                }
                zip.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return retval;
    }

    private static Collection<String> getResourcesFromDirectory(
            final File directory,
            final Pattern pattern) {
        final List<String> retval = new LinkedList<>();
        final File[] fileList = directory.listFiles();
        for (final File file : fileList) {
            if (file.isDirectory()) {
                retval.addAll(getResourcesFromDirectory(file, pattern));
            } else {
                try {
                    final String fileName = file.getCanonicalPath();
                    final boolean accept = pattern.matcher(fileName).matches();
                    if (accept) {
                        retval.add(fileName);
                    }
                } catch (final IOException e) {
                    throw new Error(e);
                }
            }
        }
        return retval;
    }

    public static boolean isWindows() {
        return (OS.contains("win"));
    }

    public static boolean isMac() {
        return (OS.contains("mac"));
    }

    public static boolean isUnix() {
        return (OS.contains("nix") || OS.contains("nux") || OS.contains("aix"));
    }

    public static boolean isSolaris() {
        return (OS.contains("sunos"));
    }

}
