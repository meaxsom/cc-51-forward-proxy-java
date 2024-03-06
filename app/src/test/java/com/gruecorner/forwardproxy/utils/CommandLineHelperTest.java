package com.gruecorner.forwardproxy.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.junit.Before;
import org.junit.Test;

public class CommandLineHelperTest {
    private final static String kBannedFilePath = "src/test/data/banned-hosts.txt";

    @Before
    public void setUp() throws Exception {
        Configurator.initialize(new DefaultConfiguration());
        Configurator.setRootLevel(Level.DEBUG);
    }

    @Test
    public void testConfigureBannedHosts() {
        List<String> theBannedHosts = CommandLineHelper.configureBannedData(kBannedFilePath);
        assertNotNull(theBannedHosts);
        assertEquals(theBannedHosts.size(), 3);
    }
}
