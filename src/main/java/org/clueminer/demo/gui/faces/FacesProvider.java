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
package org.clueminer.demo.gui.faces;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.clueminer.attributes.BasicAttrType;
import org.clueminer.dataset.api.DataProvider;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import org.clueminer.dataset.impl.ArrayDataset;
import org.clueminer.dataset.row.IntegerDataRow;
import org.clueminer.demo.ResLoader;
import org.openide.util.Exceptions;

/**
 *
 * @author deric
 * @param <E>
 */
public class FacesProvider<E extends Instance> implements DataProvider {

    private final Map<String, Dataset<E>> datasets;
    private static final Logger LOGGER = Logger.getLogger(FacesProvider.class.getName());

    public FacesProvider() {
        datasets = new HashMap<>();
    }

    public FacesProvider(Map<String, Dataset<E>> data) {
        this.datasets = data;
    }

    public void load() {
        Dataset<E> dataset = loadOlivetti(30);
        datasets.put(dataset.getName(), dataset);
        dataset = loadOlivetti(400);
        datasets.put(dataset.getName(), dataset);
    }

    private Dataset<E> loadOlivetti(int cntImages) {
        ResLoader loader = new ResLoader();
        File f = loader.resource("faces.csv");
        //final int[][] images = new int[cntImages][4096];
        int attrCnt = 4096;
        ArrayDataset<E> dataset = new ArrayDataset<>(cntImages, attrCnt);
        dataset.setName("olivetti-" + cntImages);
        for (int j = 0; j < attrCnt; j++) {
            dataset.attributeBuilder().create("attr_" + j, BasicAttrType.NUMERIC);
        }

        LOGGER.info("loading data...");
        long start = System.currentTimeMillis();
        int i = 0;
        IntegerDataRow inst;
        int cls = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            for (String line; (line = br.readLine()) != null;) {
                String[] bytes = line.split(",");
                inst = new IntegerDataRow(dataset.attributeCount());
                for (int j = 0; j < bytes.length; j++) {
                    inst.set(j, Integer.valueOf(bytes[j]));
                }
                inst.setClassValue(cls);
                dataset.add(inst);
                if (i % 10 == 9) {
                    cls++;
                }
                i++;
                if (i >= cntImages) {
                    return dataset;
                }
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        LOGGER.log(Level.INFO, "data loaded in {0}ms", (System.currentTimeMillis() - start));
        return dataset;
    }

    @Override
    public String[] getDatasetNames() {
        return datasets.keySet().toArray(new String[0]);
    }

    @Override
    public Dataset getDataset(String name) {
        if (datasets.containsKey(name)) {
            return datasets.get(name);
        }
        throw new RuntimeException("unknown dataset " + name);
    }

    @Override
    public Dataset first() {
        Iterator<String> it = datasets.keySet().iterator();
        if (!it.hasNext()) {
            throw new RuntimeException("no datasets were loaded");
        }
        return getDataset(it.next());
    }

    @Override
    public int count() {
        if (datasets == null) {
            return 0;
        }
        return datasets.size();
    }

    @Override
    public boolean hasDataset(String name) {
        return datasets.containsKey(name);
    }

    @Override
    public Iterator iterator() {
        return new DataLoaderIterator();
    }

    private class DataLoaderIterator implements Iterator<Dataset<? extends Instance>> {

        private final Iterator<String> it;

        public DataLoaderIterator() {
            it = datasets.keySet().iterator();
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public Dataset<? extends Instance> next() {
            return getDataset(it.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("not supported yet.");
        }

    }

}
