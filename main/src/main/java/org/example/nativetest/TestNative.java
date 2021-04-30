package org.example.nativetest;

public class TestNative {
    public static native void test();

    static {
        NativeLibLoader.load();
    }

    public static void main(String[] args) {
    }
}
