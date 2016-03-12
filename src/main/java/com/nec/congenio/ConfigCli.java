package com.nec.congenio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
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
                .hasArg().argName("N")
                .desc("output Nth document").build())
        .addOption(Option.builder("p").longOpt("path")
                .hasArg().argName("DOC_PATH")
                .desc("output a path in document").build())
        .addOption(Option.builder("b").longOpt("base")
                .hasArg().argName("FILE")
                .desc("base document to extend").build())
        .addOption(Option.builder("o").longOpt("outdir")
                .hasArg().argName("DIR_NAME")
                .desc("output directory").build())
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

        if (cline.hasOption('e')) {
            OutputStreamWriter writer = new OutputStreamWriter(System.out);
            cdl.write(writer, true);
        } else {
            generate(cline, cdl);
        }
    }

    private ConfigDescription createDescription(CommandLine cline, String[] args) {
        File file = new File(args[0]);
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

    private ValueHandler handler(CommandLine cline) {
        if (cline.hasOption('o')) {
            File dir = new File(cline.getOptionValue('o'));
            if (cline.hasOption('j')) {
                return new JsonSave(dir);
            } else {
                return new XmlSave(dir);
            }
        }
        if (cline.hasOption('j')) {
           return new JsonPrintout();
        } else {
           return new XmlPrintout();
        }
    }

    private void generate(CommandLine cline, ConfigDescription cdl)
            throws Exception {
        ValueHandler val = handler(cline);
        try {
            val.init(cdl);
            int idx = 0;
            Filter filter = filter(cline);
            Projection proj = projection(cline);
            for (ConfigValue conf : cdl.evaluate()) {
                if (filter.output(idx)) {
                    val.value(idx, proj.project(conf));
                }
                idx++;
                if (idx > filter.maxIndex()) {
                    break;
                }
            }
        } finally {
            val.close();
        }
    }

    private Filter filter(CommandLine cline) {
        return new Filter(cline.getOptionValue("index"));
    }

    private Projection projection(CommandLine cline) {
        return new Projection(cline.getOptionValue("path"));
    }



    /**
     * Execute command line interface of congen.
     * @param args command line arguments
     * @throws Exception when execution fails
     */
    public static void main(String[] args) throws Exception {
        new ConfigCli().execute(args);
    }

    public interface ValueHandler {
        void init(ConfigDescription cdl) throws Exception;

        void value(int idx, ConfigValue value) throws Exception;

        void close() throws Exception;
    }

    static class XmlPrintout implements ValueHandler {
        private OutputStreamWriter writer = new OutputStreamWriter(System.out);

        @Override
        public void init(ConfigDescription cdl) throws Exception {
            // nothing
        }

        @Override
        public void value(int idx, ConfigValue value) throws IOException {
            writeSeparator(idx, writer);
            Values.write(value, writer, true);
        }

        private void writeSeparator(int idx, Writer writer) throws IOException {
            if (idx > 0) {
                writer.write("<!-- " + idx + " -->\n");
            }
        }

        @Override
        public void close() throws Exception {
            writer.flush();
        }
    }

    static class JsonPrintout implements ValueHandler {
        private PrintWriter prn = new PrintWriter(System.out);

        @Override
        public void init(ConfigDescription cdl) throws Exception {
            // nothing
        }

        @Override
        public void value(int idx, ConfigValue value) throws Exception {
            prn.println(JsonValueUtil.toString(value.toJson()));
        }

        @Override
        public void close() throws Exception {
            prn.flush();
        }
    }

    static class XmlSave extends AbstractSave {

        public XmlSave(File dir) {
            super(dir);
        }

        protected String fileSuffix() {
            return ".xml";
        }

        @Override
        protected void write(ConfigValue value, Writer writer)
                throws IOException {
            Values.write(value, writer, true);
        }
    }

    static class JsonSave extends AbstractSave {

        public JsonSave(File dir) {
            super(dir);
        }

        @Override
        protected String fileSuffix() {
            return ".json";
        }

        @Override
        protected void write(ConfigValue value, Writer writer)
                throws IOException {
            writer.write(JsonValueUtil.toString(value.toJson()));
        }
        
    }

    abstract static class AbstractSave implements ValueHandler {
        private File dir;
        private File outDir;

        public AbstractSave(File dir) {
            this.dir = dir;
            this.outDir = new File(dir, "out");
            outDir.mkdirs();
        }

        @Override
        public void init(ConfigDescription cdl) throws Exception {
           File file = new File(dir, "snapshot.xml");
           OutputStreamWriter writer = new OutputStreamWriter(
                   new FileOutputStream(file));
           try {
               cdl.write(writer);
           } finally {
               writer.close();
           }
        }

        private static final int LEN = 8;

        protected File fileFor(int idx) {
            String name = Integer.toString(idx);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < LEN - name.length(); i++) {
                sb.append('0');
            }
            sb.append(name).append(fileSuffix());
            return new File(outDir, sb.toString());
        }

        protected abstract String fileSuffix();

        protected abstract void write(ConfigValue value, Writer writer) throws IOException;

        @Override
        public void value(int idx, ConfigValue value) throws Exception {
            OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(fileFor(idx)));
            try {
                write(value, writer);
            } finally {
                writer.close();
            }
        }

        @Override
        public void close() throws Exception {
        }
    }

    static class Projection {
        private final String[] path;
    
        Projection(String pattern) {
            if (pattern == null) {
                path = new String[0];
            } else {
                // path = pattern.split("\\.");
                path = pattern.split("/");
            }
        }
    
        public ConfigValue project(ConfigValue val) {
            ConfigValue res = val;
            for (String p : path) {
                res = res.getValue(p);
            }
            return res;
        }
    }

    static class Filter {
        private final int idx;

        Filter(String pattern) {
            if (pattern != null) {
                idx = Integer.parseInt(pattern);
            } else {
                idx = -1;
            }
        }
    
        public boolean output(int idx) {
            return this.idx < 0 || this.idx == idx;
        }
    
        public int maxIndex() {
            if (idx < 0) {
                return Integer.MAX_VALUE;
            } else {
                return idx;
            }
        }
    }

}
