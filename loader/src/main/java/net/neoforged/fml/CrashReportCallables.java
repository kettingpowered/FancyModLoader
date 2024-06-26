/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.fml;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.slf4j.Logger;

public class CrashReportCallables {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<ISystemReportExtender> crashCallables = Collections.synchronizedList(new ArrayList<>());
    private static final List<ICrashReportHeader> HEADERS = Collections.synchronizedList(new ArrayList<>());

    /**
     * Register a custom {@link ISystemReportExtender}
     */
    public static void registerCrashCallable(ISystemReportExtender callable) {
        crashCallables.add(callable);
    }

    /**
     * Register a {@link ISystemReportExtender system report extender} with the given header name and content
     * generator, which will always be appended to the system report
     * 
     * @param headerName      The name of the system report entry
     * @param reportGenerator The report generator to be called when a crash report is built
     */
    public static void registerCrashCallable(String headerName, Supplier<String> reportGenerator) {
        registerCrashCallable(new ISystemReportExtender() {
            @Override
            public String getLabel() {
                return headerName;
            }

            @Override
            public String get() {
                return reportGenerator.get();
            }
        });
    }

    /**
     * Register a {@link ISystemReportExtender system report extender} with the given header name and content
     * generator, which will only be appended to the system report when the given {@link BooleanSupplier} returns true
     * 
     * @param headerName      The name of the system report entry
     * @param reportGenerator The report generator to be called when a crash report is built
     * @param active          The supplier of the flag to be checked when a crash report is built
     */
    public static void registerCrashCallable(String headerName, Supplier<String> reportGenerator, BooleanSupplier active) {
        registerCrashCallable(new ISystemReportExtender() {
            @Override
            public String getLabel() {
                return headerName;
            }

            @Override
            public String get() {
                return reportGenerator.get();
            }

            @Override
            public boolean isActive() {
                try {
                    return active.getAsBoolean();
                } catch (Throwable t) {
                    LOGGER.warn("CrashCallable '{}' threw an exception while checking the active flag, disabling", headerName, t);
                    return false;
                }
            }
        });
    }

    /**
     * Registers a header to be added to the top of crash reports.
     *
     * @param header the header
     */
    public static void registerHeader(ICrashReportHeader header) {
        HEADERS.add(header);
    }

    public static List<ISystemReportExtender> allCrashCallables() {
        return List.copyOf(crashCallables);
    }

    public static Stream<String> getHeaders() {
        return HEADERS.stream().map(ICrashReportHeader::getHeader).filter(Objects::nonNull);
    }
}
