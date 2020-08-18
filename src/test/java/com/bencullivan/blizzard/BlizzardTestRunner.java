package com.bencullivan.blizzard;

import com.bencullivan.blizzard.http.BlizzardMessageBodyTest;
import com.bencullivan.blizzard.http.BlizzardMessageHeaderTest;
import com.bencullivan.blizzard.http.BlizzardMessageTest;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Runs all Blizzard tests.
 * @author Ben Cullivan
 */
public class BlizzardTestRunner {
    public static void main(String[] args) {
        List<DiscoverySelector> tests = new ArrayList<>(Arrays.asList(
                DiscoverySelectors.selectClass(BlizzardMessageHeaderTest.class),
                DiscoverySelectors.selectClass(BlizzardMessageBodyTest.class),
                DiscoverySelectors.selectClass(BlizzardMessageTest.class)
        ));
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request().selectors(tests).build();
        Launcher launcher = LauncherFactory.create();
        launcher.discover(request);
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);
        listener.getSummary().printTo(new PrintWriter(System.out));
        listener.getSummary().printFailuresTo(new PrintWriter(System.out), 0);
    }
}
