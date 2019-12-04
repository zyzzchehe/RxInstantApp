#include <jni.h>
#include <stdio.h>
#include <unistd.h>
#include <fcntl.h>
#include <string.h>
#include <stdlib.h>

static const char *TAG="gpio";
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

JNIEXPORT jint JNICALL Java_gpio_horsent_lib_GPIOController_setGpioState(JNIEnv * env, jclass cls, jint index, jint value){
    int fd = 0;
    switch(index){
        case 0:
            fd = open("/sys/devices/rk_gpio/door_ctrl",O_RDWR);
            if(fd < 0){
                LOGE("open failed for door ctrl");
                return -1;
            }
            if(value == 0){
                write(fd, "10", strlen("10"));
            }else if(value == 1){
                write(fd, "11", strlen("11"));
            }else{
                LOGE("invalid params");
            }
            close(fd);
            break;
         case 2://继电器
             fd = open("/sys/devices/rk_gpio/relay_ctrl",O_RDWR);
             if(fd < 0){
                 LOGE("open failes 2");
                 return -1;
              }
              if(value == 0){
                  write(fd, "10", strlen("10"));
              }else if(value == 1){
                  write(fd, "11", strlen("11"));
              }else{
                  LOGE("invalid params");
              }
              close(fd);
              break;
          case 5:
              fd = open("/sys/devices/backlight.23/backlight/rk28_bl/brightness",O_RDWR);
              if(fd < 0){
                  LOGE("open failes for brightness");
                  return -1;
              }
              if(value == 0){
                  write(fd, "0", strlen("0"));
              }else if(value == 1){
                  write(fd, "255", strlen("255"));
              }else{
                  LOGE("invalid params for brightness");
              }
              close(fd);
              break;
          default:
              LOGE("index error");
              break;
}

JNIEXPORT jint JNICALL Java_gpio_horsent_lib_GPIOController_getGpioState(JNIEnv * env, jclass cls, jint index){
   int fd = 0;
   char buf[10]={'0'};
   switch(index)
   {
       case 0:
           fd = open("/sys/devices/rk_gpio/door_ctrl",O_RDONLY);
           if(fd == -1) {
               printf("error is %s\n", strerror(errno));
               return -1;
           }
           read(fd, buf, sizeof(buf)-1);
           close(fd);
           return (*env)->NewStringUTF(env, buf);
       case 2:
           fd = open("/sys/devices/rk_gpio/relay_ctrl",O_RDONLY);
           if(fd == -1) {
               printf("error is %s\n", strerror(errno));
               return -1;
           }
           read(fd, buf, sizeof(buf)-1);
           close(fd);
           return (*env)->NewStringUTF(env, buf);
       case 5:
           fd = open("/sys/devices/backlight.23/backlight/rk28_bl/brightness",O_RDONLY);
           if(fd == -1) {
                          printf("error is %s\n", strerror(errno));
                          return -1;
                      }
            read(fd, buf, sizeof(buf)-1);
            close(fd);
            return (*env)->NewStringUTF(env, buf);
       default:
            memcpy(buf,"error",strlen("error"));
             return (*env)->NewStringUTF(env, buf);
   }
}