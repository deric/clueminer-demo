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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;
import org.clueminer.dataset.api.DataProvider;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import org.clueminer.dataset.plugin.ArrayDataset;
import org.clueminer.io.ARFFHandler;
import org.openide.util.Exceptions;

/**
 *
 * @author deric
 */
public class DataLoader implements DataProvider {

    private final Map<String, String> datasets;
    private final Map<String, Dataset<? extends Instance>> cache;
    private String prefix = "datasets/artificial";

    public DataLoader(Map<String, String> datasets, String prefix) {
        this.datasets = datasets;
        this.cache = new HashMap<>(datasets.size());
        this.prefix = prefix;
    }

    @Override
    public String[] getDatasetNames() {
        return datasets.keySet().toArray(new String[0]);
    }

    @Override
    public Dataset<? extends Instance> getDataset(String name) {
        if (cache.containsKey(name)) {
            return cache.get(name);
        }
        if (!datasets.containsKey(name)) {
            throw new RuntimeException("unknown dataset " + name);
        }

        Dataset<? extends Instance> dataset = loadDataset(name, datasets.get(name));
        cache.put(name, dataset);
        return dataset;
    }

    @Override
    public Dataset<? extends Instance> first() {
        Iterator<String> it = datasets.keySet().iterator();
        if (!it.hasNext()) {
            throw new RuntimeException("no datasets were loaded");
        }
        return getDataset(it.next());
    }

    @Override
    public int count() {
        return datasets.size();
    }

    /**
     * Tries to load dataset by its name. There must be a method for loading the
     * dataset in this class.
     *
     * @param name
     * @return
     */
    private Dataset<? extends Instance> loadDataset(String name, String type) {
        Dataset<? extends Instance> dataset = null;
        switch (type) {
            case "arff":
                //TODO: multi dimensions support
                dataset = new ArrayDataset(10, 2);
                dataset.setName(name);
                ARFFHandler arff = new ARFFHandler();
                try {
                    arff.load(resource(name + "." + type), dataset);
                } catch (FileNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }

                break;
            default:
                throw new RuntimeException("unsupported format " + type);
        }

        return dataset;
    }

    /**
     * Resource packed in jar is not possible to open directly, this method uses
     * a .tmp file which should be on exit deleted
     *
     * @param path
     * @return
     */
    public File resource(String path) {
        String resource = "/" + prefix + "/" + path;
        File file = null;
        URL url = DataLoader.class.getResource(resource);
        if (url == null) {
            throw new RuntimeException("resource not found: " + path);
        }

        if (url.toString().startsWith("jar:")) {
            try {
                InputStream input = getClass().getResourceAsStream(resource);
                file = File.createTempFile("nodesfile", ".tmp");
                OutputStream out = new FileOutputStream(file);
                int read;
                byte[] bytes = new byte[1024];

                while ((read = input.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                file.deleteOnExit();
            } catch (IOException ex) {
                System.err.println(ex.toString());
            }
        } else {
            file = new File(url.getFile());
        }
        return file;
    }

    public static DataProvider createLoader(String path) {
        Map<String, String> datasets = new HashMap<>();

        Pattern pattern = Pattern.compile("(.*)" + path + "(.*)");

        final Collection<String> list = ResourceList.getResources(pattern);
        int idx, dot;
        String dataset;
        String ext;
        for (final String name : list) {
            idx = name.lastIndexOf("/");
            dot = name.lastIndexOf(".");
            dataset = name.substring(idx + 1, dot);
            ext = name.substring(dot + 1);
            datasets.put(dataset, ext);
        }

        return new DataLoader(datasets, path);
    }

}
