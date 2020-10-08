LOCAL_PATH := $(call my-dir)



include $(CLEAR_VARS)

LOCAL_MODULE := xdelta1

LOCAL_C_INCLUDES := $(LOCAL_PATH)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../glib
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../glib/glib
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../glib/android
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../libedsio

LOCAL_SRC_FILES := \
getopt.c \
getopt1.c \
xd_edsio.c \
xdapply.c \
xdelta.c \
xdmain.c \
nativelink.c

LOCAL_CFLAGS := -O3
LOCAL_SHARED_LIBRARIES := libglib-2.0 edsio
LOCAL_LDLIBS := -lz

include $(BUILD_SHARED_LIBRARY)  