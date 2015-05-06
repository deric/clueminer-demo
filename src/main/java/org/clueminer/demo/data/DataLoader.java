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

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.clueminer.dataset.api.DataProvider;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import org.openide.util.Exceptions;

/**
 *
 * @author deric
 */
public class DataLoader implements DataProvider {

    private final Collection<String> datasets;
    private final Map<String, Dataset<? extends Instance>> cache;

    public DataLoader(Collection<String> datasets) {
        this.datasets = datasets;
        this.cache = new HashMap<>(datasets.size());
    }

    @Override
    public String[] getDatasetNames() {
        return datasets.toArray(new String[0]);
    }

    @Override
    public Dataset<? extends Instance> getDataset(String name) {
        if (cache.containsKey(name)) {
            return cache.get(name);
        }
        Dataset<? extends Instance> dataset = loadDataset(name);
        cache.put(name, dataset);
        return dataset;
    }

    @Override
    public Dataset<? extends Instance> first() {
        return getDataset(datasets.iterator().next());
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
    private Dataset<? extends Instance> loadDataset(String name) {
        java.lang.reflect.Method method = null;
        Dataset<? extends Instance> dataset = null;
        try {
            method = this.getClass().getMethod(name);
            dataset = (Dataset<? extends Instance>) method.invoke(name);
        } catch (SecurityException e) {
            System.err.println(e.getMessage());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("dataset " + name + " is not supported");
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
        return dataset;
    }

}
