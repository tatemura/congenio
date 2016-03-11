package com.nec.congenio;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Properties;

import javax.json.JsonValue;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.nec.congenio.ConfigDescription.Filter;
import com.nec.congenio.ConfigDescription.Projection;
import com.nec.congenio.json.JsonValueUtil;

public class ConfigCli {
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
        .addOption("e", "extend-only", false, "resolve extention only")
        .addOption("j", "json", false, "output json")
        .addOption("h", "help", false, "show help message")
        .addOption(Option.builder("i").longOpt("index")
                .hasArg().argName("N").type(Number.class)
                .desc("output Nth document").build())
        .addOption(Option.builder("p").longOpt("path")
                .hasArg().argName("DOC_PATH")
                .desc("output a path in document").build())
        .addOption(Option.builder("b").longOpt("base")
                .hasArg().argName("FILE")
                .desc("base document to extend").build())
        .addOption(Option.builder("L").hasArgs()
                .valueSeparator()
                .argName("LIB_NAME=PATH")
                .desc("define a lib path").build());
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
        String[] cmdArgs = cline.getArgs();
        if (cline.hasOption('h') || cmdArgs.length == 0) {
            HelpFormatter help = new HelpFormatter();
            help.printHelp(name, getOptions());
            return;
        }

        ConfigDescription cdl = createDescription(cline, cmdArgs);

        /*
         * TODO FIXME set up library paths in a different way
         */
        processLibPaths(cline);

        if (cline.hasOption('e')) {
            OutputStreamWriter writer = new OutputStreamWriter(System.out);
            cdl.write(writer, true);
        } else if (cline.hasOption('j')) {
            generateJson(cline, cdl);
        } else {
            generateXml(cline, cdl);
        }
    }

    private ConfigDescription createDescription(CommandLine cline, String[] args) {
        File file = new File(args[0]);
        if (cline.hasOption('b')) {
            String base = cline.getOptionValue('b');
            return ConfigDescription.create(file,
                    ConfigDescription.create(new File(base)));
        } else {
            return ConfigDescription.create(file);
        }
    }

    private void processLibPaths(CommandLine cline) {
        Properties props = cline.getOptionProperties("L");
        if (props != null) {
            StringBuilder sb = new StringBuilder();
            boolean contd = false;
            for (String libname : props.stringPropertyNames()) {
                String path = props.getProperty(libname);
                if (contd) {
                    sb.append(';');
                } else {
                    contd = true;
                }
                sb.append(libname).append('=').append(path);
            }
            if (contd) {
                System.setProperty(
                        ConfigProperties.PROP_LIBS, sb.toString());
            }
        }
    }

    private void generateJson(CommandLine cline, ConfigDescription cdl) {
        Filter filter = new Filter(cline.getOptionValue("index"));
        Projection proj = new Projection(cline.getOptionValue("path"));
        int idx = 0;
        for (ConfigValue conf : cdl.evaluate()) {
            if (filter.output(idx)) {
                JsonValue value = proj.project(conf).toJson();
                System.out.println(JsonValueUtil.toString(value));
            }
            idx++;
            if (idx > filter.maxIndex()) {
                break;
            }
        }    
    }

    private void generateXml(CommandLine cline, ConfigDescription cdl)
            throws IOException {
        Filter filter = new Filter(cline.getOptionValue("index"));
        Projection proj = new Projection(cline.getOptionValue("path"));
        OutputStreamWriter writer = new OutputStreamWriter(System.out);
        int idx = 0;
        for (ConfigValue conf : cdl.evaluate()) {
            if (filter.output(idx)) {
                writeSeparator(idx, writer);
                Values.write(proj.project(conf), writer, true);
            }
            idx++;
            if (idx > filter.maxIndex()) {
                break;
            }
        }
    }

    void writeSeparator(int idx, Writer writer) throws IOException {
        if (idx > 0) {
            writer.write("<!-- " + idx + " -->\n");
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
