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
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.nec.congenio.exec.OutputFormat;
import com.nec.congenio.exec.ValueExecBuilder;

/**
 * A class that implements a Command-Line Interface.
 * @author tatemura
 *
 */
public class ConfigCli {
    public static final String OPT_PATH = "path";
    public static final String OPT_INDEX = "index";
    public static final String OPT_FORMAT = "format";
    private final String name;

    public ConfigCli() {
        this.name = "congen";
    }

    public ConfigCli(String name) {
        this.name = name;
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
        .addOption("e", "extend-only", false, "resolves extention only")
        .addOption("h", "help", false, "shows help message")
        .addOption(Option.builder("i").longOpt(OPT_INDEX)
                .hasArg().argName("INDEX_PATTERN")
                .desc("selects documents by its index (e.g., '1', '0,3,4', '2..4')").build())
        .addOption(Option.builder("p").longOpt(OPT_PATH)
                .hasArg().argName("DOC_PATH")
                .desc("outputs a path in document").build())
        .addOption(Option.builder("f").longOpt(OPT_FORMAT)
                .hasArg().argName("FORMAT")
                .desc("sets output format (xml, json, properties,"
                        + " xml-no-indent, json-no-indent)").build())
        .addOption(Option.builder("b").longOpt("base")
                .hasArg().argName("FILE")
                .desc("sets a base document to extend").build())
        .addOption(Option.builder("o").longOpt("outdir")
                .hasArg().argName("DIR_NAME")
                .desc("sets output directory").build())
        .addOption(Option.builder("L").hasArgs()
                .valueSeparator()
                .argName("LIB_NAME=PATH")
                .desc("defines a lib path").build());
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
            String[] cmdArgs = cline.getArgs();
            ConfigDescription cdl = createDescription(cline, new File(cmdArgs[0]));
            ValueExecBuilder builder = new ValueExecBuilder(cdl)
            .filterIndex(cline.getOptionValue(OPT_INDEX))
            .path(cline.getOptionValue(OPT_PATH));
            setHandler(builder, cline);
            builder.build().run();
        }
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

        if (cline.hasOption('b')) {
            String base = cline.getOptionValue('b');
            return ConfigDescription.create(file,
                    ConfigDescription.create(new File(base)),
                   libDefs);
        } else {
            return ConfigDescription.create(file, libDefs);
        }
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
        if (cline.hasOption(OPT_FORMAT)) {
            OutputFormat format = OutputFormat.find(
                    cline.getOptionValue(OPT_FORMAT));
            if (format != null) {
                return format;
            } else {
                throw new ConfigException(
                        "unsupported output format: "
                        + cline.getOptionValue(OPT_FORMAT));
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
