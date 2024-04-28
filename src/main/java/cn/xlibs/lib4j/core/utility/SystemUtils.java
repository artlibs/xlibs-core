package cn.xlibs.lib4j.core.utility;

import cn.xlibs.lib4j.core.exception.OperationException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * System Utils
 *
 * @author Fury
 * @since 2020-04-08
 *
 * All rights Reserved.
 */
public final class SystemUtils {
    private SystemUtils(){}

    private static final String OS = System.getProperty("os.name").toLowerCase();

    public static boolean isMacOS() {
        return OS.contains("mac os") && !OS.contains("x");
    }

    public static boolean isMacOSX() {
        return "mac os x".equals(OS);
    }

    public static boolean isWindows() {
        return OS.contains("windows");
    }

    public static boolean isWindowsXP() {
        return "windows xp".equals(OS);
    }

    public static boolean isWindows2003() {
        return "windows 2003".equals(OS);
    }

    public static boolean isWindowsVista() {
        return "windows vista".equals(OS);
    }

    public static boolean isLinux() {
        return OS.contains("linux");
    }

    public static boolean isWindows7() {
        return OS.contains("windows 7");
    }

    public static boolean isWindows8() {
        return OS.contains("windows 8");
    }

    public static boolean isWindows10() {
        return OS.contains("windows 10");
    }

    public static boolean isWindows11() {
        return OS.contains("windows 11");
    }

    /**
     * get current time in millis
     * @return current time in millis
     */
    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * get current time in seconds
     * @return current time in seconds
     */
    public static long currentTimeSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * get Machine Code
     *
     * @return Machine Code
     */
    public static String getMachineCode() {
        StringBuilder sb = new StringBuilder();
        boolean isWindows = SystemUtils.isWindows();

        try {
            String[] command = isWindows ? new String[]{"cmd", "/c", "wmic csproduct list full | findstr UUID"}
                    : new String[]{"/bin/sh", "-c", "dmidecode | grep 'System Information' -A 8 | grep -o -E '\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}'"};
            Process proc = Runtime.getRuntime().exec(command);
            try(BufferedReader input = new BufferedReader(new
                    InputStreamReader(proc.getInputStream()
                    , StandardCharsets.UTF_8))) {
                String line = "";
                while ((line = input.readLine()) != null) {
                    sb.append(line);
                }
            }
            proc.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            throw new OperationException(e.getMessage(), e);
        }

        return sb.toString().replace("UUID=", "")
                .replace("UUID:", "")
                .replace("-", "")
                .toUpperCase();
    }
}