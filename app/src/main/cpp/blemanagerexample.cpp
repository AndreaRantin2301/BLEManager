#include <jni.h>

// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("blemanagerexample");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("blemanagerexample")
//      }
//    }
#define VECT_CRC {12,216,105,176,208,8,184,96,120,160,16,201,168,112,192,24,240,40,152,64,32,248,72,144,136,80,224,56,88,128,48,232,56,224,80,136,232,48,128,88,64,152,40,240,144,72,248,32,200,16,160,120,24,192,112,168,175,104,216,123,95,184,8,208,112,168,24,192,160,120,200,16,8,208,96,184,216,25,176,104,128,88,232,48,80,136,56,224,248,32,144,72,40,240,64,152,72,144,32,248,152,64,240,40,48,232,88,128,224,56,136,80,184,96,208,8,104,176,58,216,192,24,168,112,16,199,123,160,224,56,136,80,48,232,88,128,152,64,240,40,72,144,32,248,16,200,120,160,192,24,168,112,104,176,214,216,184,96,208,8,216,98,176,104,8,208,96,184,160,120,200,16,112,168,24,192,40,240,64,152,248,32,144,72,80,136,56,224,128,88,232,48,144,72,248,32,64,152,40,240,232,48,128,88,56,224,80,136,96,184,8,208,176,104,216,27,24,192,112,168,200,16,160,120,168,112,192,25,123,160,16,207,208,8,184,96,100,216,105,176,88,128,48,232,136,80,224,56,32,248,72,144,240,40,152,65}
#define WIDTH  (8 * sizeof(uint8_t))

extern "C"
JNIEXPORT jbyte JNICALL
Java_com_andrearantin_blemanagerexample_NDKBridge_crcFast(JNIEnv *env, jclass clazz,
                                                          jbyteArray message, jint n_bytes) {
    jbyte *j_arr = env->GetByteArrayElements(message, NULL);

    uint8_t data;
    uint8_t remainder=0;
    uint8_t crcTable[256]=VECT_CRC;

    int byte=0;

    for (byte=0; byte<n_bytes; ++byte) {
        data =  (uint8_t) j_arr[byte] ^ (remainder >> (WIDTH - 8));
        remainder = crcTable[data] ^ (remainder << 8);

    }
    return remainder;
}