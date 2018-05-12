/*
 * Copyright (C) 2011-2018 clueminer.org
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

import com.beust.jcommander.Parameter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.clueminer.utils.FileUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author deric
 */
public class ImgParams {

    @Parameter(names = "--dir", description = "directory for results", required = false)
    public String home = System.getProperty("user.home") + File.separatorChar
            + NbBundle.getMessage(FileUtils.class, "FOLDER_Home");

    @Parameter(names = {"--width", "-w"})
    public int width = 800;

    @Parameter(names = {"--height", "-h"})
    public int height = 600;

    @Parameter(names = {"--format", "-f"})
    String format = "png";

    @Parameter(names = {"--data", "-d"})
    public String dataset;

    @Parameter(names = {"--cluster", "-c"}, description = "if false golden clustering will be used")
    public boolean computeClustering = false;

    /**
     * A hack to support passing json containing whitespaces
     */
    @Parameter(names = {"--params", "-p"}, variableArity = true, splitter = NoopSplitter.class)
    public List<String> params = new ArrayList<>();

    private String p;

    public String getParams() {
        if (p == null) {
            StringBuilder sb = new StringBuilder();
            for (String s : params) {
                sb.append(s).append(" ");
            }
            p = sb.toString();

        }
        return p;
    }


}
