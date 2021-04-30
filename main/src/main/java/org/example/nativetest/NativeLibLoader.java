package org.example.nativetest;

import java.io.*;
import java.nio.file.Files;
import java.util.Locale;

class NativeLibLoader {
    private static final String LIB_LOC = "META-INF/native/project/";
    private static final String LIB_NAME = "testing";
    private static final String LOCAL_LIB_PATH_PREFIX = "native/cmake-build-debug/" + LIB_NAME + ".";
    private static final String PROJECT_NAME = "Test";

    private static String trimOsName(String os) {
        String osL = os.toLowerCase(Locale.ROOT);
        if (osL.startsWith("windows ")) return "win";
        if (osL.startsWith("mac os")) return "macos";
        if (osL.startsWith("linux")) return "linux";
        return os;
    }

    private static String extName(String os) {
        switch (os) {
            case "win":
                return "dll";
            case "macos":
                return "dylib";
            case "linux":
                return "so";
        }
        return null;
    }

    private static void transfer(InputStream from, OutputStream to) throws IOException {
        byte[] buffer = new byte[10240];
        while (true) {
            int len = from.read(buffer);
            if (len == -1) break;
            to.write(buffer, 0, len);
        }
    }

    private static void load0() {
        String osName = trimOsName(System.getProperty("os.name"));
        String extName = extName(osName);
        File libLocal = new File(LOCAL_LIB_PATH_PREFIX + extName);
        if (libLocal.isFile()) {
            System.load(libLocal.getAbsolutePath());
            return;
        }

        String osArch = System.getProperty("os.arch");
        try (InputStream libRes = NativeLibLoader.class.getResourceAsStream("/" + LIB_LOC + LIB_NAME + "-" + osName + "-" + osArch + "." + extName)) {
            if (libRes == null) {
                throw new UnsupportedOperationException("`" + PROJECT_NAME + "` is not support on " + osName + " " + osArch);
            }
            File file = Files.createTempFile(LIB_NAME, extName).toFile();
            file.deleteOnExit();
            try (OutputStream extract = new BufferedOutputStream(new FileOutputStream(file))) {
                transfer(libRes, extract);
            }
            System.load(file.getAbsolutePath());
        } catch (Exception e) {
            throw new LinkageError(e.toString(), e);
        }
    }

    static void load() {
    }

    static {
        load0();
    }
}
