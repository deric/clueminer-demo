/*
 * Copyright (C) 2011-2016 clueminer.org
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

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 *
 * @author deric
 */
public class ArgParser {

    @Option(name = "-f", aliases = {"--format"}, usage = "output format (png, jpg)")
    public String format;

    @Option(name = "-p", usage = "JSON params")
    public String params;

    public ArgParser run(String[] args) {
        CmdLineParser p = new CmdLineParser(this);
        try {
            p.parseArgument(args);

        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            p.printUsage(System.err);
        }
        return this;
    }

}
