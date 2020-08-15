package com.bencullivan.blizzard;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * Runs all Blizzard tests.
 * @author Ben Cullivan
 */
public class BlizzardTestRunner {
    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(BlizzardTestSuite.class);
        if (result.getFailureCount() == 0) {
            System.out.println("✅ Yay! The code passed all the tests! ✅");
        } else {
            for (Failure failure : result.getFailures()) {
                System.out.println("❌ " + failure);
            }
        }
    }
}
