LOCAL_PATH := $(call my-dir)



include $(CLEAR_VARS)

LOCAL_MODULE := edsio

LOCAL_C_INCLUDES := $(LOCAL_PATH)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../glib
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../glib/glib
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../glib/gmodule
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../glib/android

LOCAL_SRC_FILES := \
base64.c \
default.c \
edsio.c \
edsio_edsio.c \
fh.c \
generic.c \
library.c \
maketime.c \
md5c.c \
partime.c \
sha.c \
simple.c

LOCAL_CFLAGS := -O3
LOCAL_SHARED_LIBRARIES := libglib-2.0 libgmodule-2.0
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)  