//
// Created by Chen on 2019/5/28.
//

#include "Hello.h"
#include "example_chen_com_detecthandwriting_CheckActivity.h"
#include <string>
#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <opencv2/opencv.hpp>
using namespace cv;
using namespace std;


JNIEXPORT jstring JNICALL Java_example_chen_com_detecthandwriting_CheckActivity_sayHello
  (JNIEnv *env, jobject){
 return env->NewStringUTF("hello ndk");
}

JNIEXPORT jintArray JNICALL Java_example_chen_com_detecthandwriting_CheckActivity_gray
(JNIEnv *env, jobject instance, jintArray buf, jint w,
                                            jint h) {
    jint *cbuf = env->GetIntArrayElements(buf, JNI_FALSE );
        if (cbuf == NULL) {
            return 0;
        }

        Mat imgData(h, w, CV_8UC4, (unsigned char *) cbuf);

        uchar* ptr = imgData.ptr(0);
        for(int i = 0; i < w*h; i ++){
            //计算公式：Y(亮度) = 0.299*R + 0.587*G + 0.114*B
            //对于一个int四字节，其彩色值存储方式为：BGRA
            int grayScale = (int)(ptr[4*i+2]*0.299 + ptr[4*i+1]*0.587 + ptr[4*i+0]*0.114);
            ptr[4*i+1] = grayScale;
            ptr[4*i+2] = grayScale;
            ptr[4*i+0] = grayScale;
        }

        int size = w * h;
        jintArray result = env->NewIntArray(size);
        env->SetIntArrayRegion(result, 0, size, cbuf);
        env->ReleaseIntArrayElements(buf, cbuf, 0);
        return result;

}
