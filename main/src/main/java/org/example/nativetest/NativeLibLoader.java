package org.example.nativetest;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

class NativeLibLoader {
    private static final String LIB_LOC = "META-INF/native/Test/";
    private static final String LIB_NAME = "testing";
    private static final String LOCAL_LIB_PATH_PREFIX = "native/cmake-build-debug/lib" + LIB_NAME + ".";
    private static final String PROJECT_NAME = "Test";

    private static File findRuningLoc() {
        URL loc = NativeLibLoader.class.getResource("NativeLibLoader.class");
        if (loc == null || !loc.getProtocol().equals("file")) return null;
        File file = Paths.get(URI.create(loc.toString())).toFile();
        long pcount = NativeLibLoader.class.getName().chars().filter(it -> it == '.').count();
        pcount += 5; // build/class/java/main
        pcount++; // Will insert in template
        while (pcount-- > 0) {
            file = file.getParentFile();
        }
        return file;
    }

    private static String trimOsName(String os) {
        String osL = os.toLowerCase(Locale.ROOT);
        if (osL.startsWith("windows ")) return "windows";
        if (osL.startsWith("mac os")) return "macos";
        if (osL.startsWith("linux")) return "linux";
        return os;
    }

    private static String extName(String os) {
        switch (os) {
            case "windows":
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

        File fLocal = findRuningLoc();
        if (fLocal != null) {
            File libLocal = new File(fLocal, LOCAL_LIB_PATH_PREFIX + extName);
            if (libLocal.isFile()) {
                System.load(libLocal.getAbsolutePath());
                return;
            }
        }

        String osArch = System.getProperty("os.arch");
        String path = "/" + LIB_LOC + LIB_NAME + "-" + osName + "-" + osArch + "." + extName;
        try (InputStream libRes = NativeLibLoader.class.getResourceAsStream(path)) {
            if (libRes == null) {
                throw new UnsupportedOperationException("`" + PROJECT_NAME + "` is not supported on " + osName + " " + osArch + " because `" + path + "` not found.");
            }
            File file = Files.createTempFile(LIB_NAME, "." + extName).toFile();
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
