package com.nec.congenio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.junit.Test;

public class ConfigCliTest {

    @Test
    public void testArgs() throws Exception {
        ConfigCli cli = new ConfigCli();
        CommandLine cline =
                cli.createCommandLine("-e", "--json",
                        "-Lconf=~/config", "-Ltmp=/tmp",
                        "-b", "base.xml",
                        "-i", "1", "test.xml");
        assertTrue(cline.hasOption('e'));
        assertTrue(cline.hasOption("extend-only"));
        assertTrue(cline.hasOption('j'));
        assertTrue(cline.hasOption("json"));
        Properties libs = cline.getOptionProperties("L");
        assertNotNull(libs);
        assertEquals("~/config", libs.getProperty("conf"));
        assertTrue(cline.hasOption('b'));
        assertTrue(cline.hasOption("base"));
        String base = cline.getOptionValue('b');
        assertNotNull(base);
        assertEquals("base.xml", base);
        assertTrue(cline.hasOption("index"));
        assertTrue(cline.hasOption('i'));
        assertEquals("1", cline.getOptionValue("index"));
        List<String> args = cline.getArgList();
        assertEquals(1, args.size());
        assertEquals("test.xml", args.get(0));
    }
}
