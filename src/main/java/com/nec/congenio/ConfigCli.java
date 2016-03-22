/*******************************************************************************
 * Copyright 2015, 2016 Junichi Tatemura
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.nec.congenio;

import java.io.File;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.nec.congenio.exec.OutputFormat;
import com.nec.congenio.exec.ValueExecBuilder;
import com.nec.congenio.exec.ValueHandler;

/**
 * A class that implements a Command-Line Interface.
 * @author tatemura
 *
 */
public class ConfigCli {
    public enum Opt {
        /**
         * Option to set a filtering
         * to select generated documents
         * by index.
         */
        INDEX("i", LOPT_INDEX),
        /**
         * Option to set a document path
         * that extracts output from a
         * generated document.
         */
        PATH("p", LOPT_PATH),
        /**
         * Option to specify output
         * format (XML, JSON, properties).
         */
        FORMAT("f", LOPT_FORMAT),
        /**
         * Option to specify a base
         * document to be extended.
         */
        BASE("b", "base"),
        /**
         * Option to set an output
         * directory.
         */
        OUT_DIR("o", "outdir"),
        /**
         * Option to define a
         * library path.
         */
        LIB("L"),
        /**
         * Option to resolve extension
         * only.
         */
        EXTEND_ONLY("e", "extend-only");

        private final String value;
        private final String longOpt;
        Opt(String value) {
            this.value = value;
            this.longOpt = null;
        }

        Opt(String value, String longOpt) {
            this.value = value;
            this.longOpt = longOpt;
        }

        protected String shortOpt() {
            return value;
        }

        protected String longOpt() {
            return longOpt;
        }
    }

    public static final String LOPT_PATH = "path";
    public static final String LOPT_INDEX = "index";
    public static final String LOPT_FORMAT = "format";

    private static final Map<String, Option> OPTS = new HashMap<String, Option>();

    static {
        def(optFor(Opt.INDEX)
                .hasArg().argName("INDEX_PATTERN"),
                "selects documents by its index (e.g., '1', '0,3,4', '2..4')");
        def(optFor(Opt.PATH)
                .hasArg().argName("DOC_PATH"),
                "outputs a path in document");
        def(optFor(Opt.FORMAT)
                .hasArg().argName("FORMAT"),
                "sets output format (xml, json, properties,"
                        + " xml-no-indent, json-no-indent)");
        def(optFor(Opt.BASE)
                .hasArg().argName("FILE"),
                "sets a base document to extend");
        def(optFor(Opt.OUT_DIR)
                .hasArg().argName("DIR_NAME"),
                "sets output directory");
        def(optFor(Opt.LIB)
                .hasArgs().valueSeparator().argName("LIB_NAME=PATH"),
                "defines a lib path");
        def(optFor(Opt.EXTEND_ONLY),
                "resolves extention only");
    }

    private final String name;
    private final Set<String> options =
            new HashSet<String>(OPTS.keySet());

    @Nullable
    private ConfigDescription baseDescription;

    public ConfigCli() {
        this.name = "congen";
    }

    public ConfigCli(String name) {
        this.name = name;
    }

    /**
     * Disable options.
     * @param opts an array of options
     *        to be disabled.
     */
    public void disableOptions(Opt... opts) {
        for (Opt o : opts) {
            options.remove(o.shortOpt());
        }
    }

    /**
     * Enable options.
     * @param opts an array of options
     *        to be enabled.
     */
    public void enableOptions(Opt...opts) {
        for (Opt o : opts) {
            options.add(o.shortOpt());
        }
    }

    public void resetOptions() {
        options.clear();
    }

    public void resetOptionsToDefault() {
        options.clear();
        options.addAll(OPTS.keySet());
    }

    public void setBaseDescription(ConfigDescription baseDescription) {
        this.baseDescription = baseDescription;
    }

    private static void def(Option.Builder builder, String description) {
        Option opt = builder.desc(description).build();
        OPTS.put(opt.getOpt(), opt);
    }

    private static Option.Builder optFor(Opt opt) {
        Option.Builder builder = Option.builder(opt.shortOpt());
        String lopt = opt.longOpt();
        if (lopt != null) {
            builder.longOpt(lopt);
        }
        return builder;
    }

    public CommandLine createCommandLine(String... args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        return parser.parse(getOptions(), args);
    }

    /**
     * Gets options for congen execution.
     * @return Apache CLI options.
     */
    public Options getOptions() {
        Options opts = new Options()
        .addOption("h", "help", false, "shows help message");

        for (String o : options) {
            Option op = OPTS.get(o);
            if (op != null) {
                opts.addOption(op);
            }
        }
        return opts;
    }

    /**
     * Executes a command.
     * @param args command line arguments
     * @throws ParseException when command line parsing fails
     * @throws Exception when execution fails
     */
    public void execute(String... args) throws Exception {
        CommandLine cline = createCommandLine(args);
        if (cline.hasOption('h')) {
            doHelp();
        } else if (cline.hasOption('e')) {
            showExtendOnly(cline);
        } else {
            ValueExecBuilder builder = builder(cline);
            setHandler(builder, cline);
            builder.build().run();
        }
    }

    /**
     * Executes a command with a given handler.
     * @param handler the handler that handles generated values.
     * @param args command arguments.
     * @throws Exception when the handler throws exception or
     *      when document generation is failed.
     */
    public void execute(ValueHandler handler, String... args) throws Exception {
        execute(handler, createCommandLine(args));
    }

    public void execute(ValueHandler handler, CommandLine cline) throws Exception {
        builder(cline).handler(handler).build().run();
    }

    private ValueExecBuilder builder(CommandLine cline) {
        String[] cmdArgs = cline.getArgs();
        ConfigDescription cdl = createDescription(cline, new File(cmdArgs[0]));
        ValueExecBuilder builder = new ValueExecBuilder(cdl)
        .filterIndex(cline.getOptionValue(LOPT_INDEX))
        .path(cline.getOptionValue(LOPT_PATH));
        return builder;
    }

    public void doHelp(String... args) {
        HelpFormatter help = new HelpFormatter();
        help.printHelp(name + " [OPTIONS] DOCUMENT", getOptions());
    }

    private void showExtendOnly(CommandLine cline) {
        String[] args = cline.getArgs();
        if (args.length != 1) {
            doHelp();
        } else {
            ConfigDescription cdl = createDescription(cline, new File(args[0]));
            OutputStreamWriter writer = new OutputStreamWriter(System.out);
            cdl.write(writer, true);
        }
    }

    private ConfigDescription createDescription(CommandLine cline, File file) {
        Map<String, String> libDefs =
                ConfigProperties.getLibDefs(file.getParentFile());
        libDefs.putAll(libPathDefs(cline));

        ConfigDescription base = baseDescription(cline);
        if (base != null) {
            return ConfigDescription.create(file, base, libDefs);
        } else {
            return ConfigDescription.create(file, libDefs);
        }
    }

    private ConfigDescription baseDescription(CommandLine cline) {
        if (cline.hasOption('b')) {
            String base = cline.getOptionValue('b');
            return ConfigDescription.create(new File(base));
        }
        return baseDescription;
    }

    private Map<String, String> libPathDefs(CommandLine cline) {
        Properties props = cline.getOptionProperties("L");
        if (props != null) {
            return ConfigProperties.toAbsolutes(props, new File("."));
        }
        return Collections.emptyMap();
    }

    private void setHandler(ValueExecBuilder builder, CommandLine cline) {
        if (cline.hasOption('o')) {
            File dir = new File(cline.getOptionValue('o'));
            builder.save(dir, format(cline));
        } else {
            builder.print(format(cline));
        }
    }

    private OutputFormat format(CommandLine cline) {
        if (cline.hasOption(LOPT_FORMAT)) {
            OutputFormat format = OutputFormat.find(
                    cline.getOptionValue(LOPT_FORMAT));
            if (format != null) {
                return format;
            } else {
                throw new ConfigException(
                        "unsupported output format: "
                        + cline.getOptionValue(LOPT_FORMAT));
            }
        } else {
            return OutputFormat.XML;
        }
    }


    /**
     * Execute command line interface of congen.
     * @param args command line arguments
     * @throws Exception when execution fails
     */
    public static void main(String[] args) throws Exception {
        new ConfigCli().execute(args);
    }
}
