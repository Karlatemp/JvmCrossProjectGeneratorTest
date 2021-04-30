#include <jni.h>
#include <iostream>
#include "org_example_nativetest_TestNative.h"

JNIEXPORT void JNICALL Java_org_example_nativetest_TestNative_test
  (JNIEnv *, jclass) {
  std::cout << "OK" << std::endl;
}
