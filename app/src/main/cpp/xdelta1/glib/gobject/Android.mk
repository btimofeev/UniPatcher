LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES	:=			\
	gatomicarray.c			\
	gbinding.c			\
	gboxed.c			\
	gclosure.c			\
	genums.c			\
	gobject.c			\
	gparam.c			\
	gparamspecs.c			\
	gsignal.c			\
	gsourceclosure.c		\
	gtype.c				\
	gtypemodule.c			\
	gtypeplugin.c			\
	gvalue.c			\
	gvaluearray.c			\
	gvaluetransform.c		\
	gvaluetypes.c

LOCAL_MODULE := libgobject-2.0

LOCAL_C_INCLUDES :=			\
	$(LOCAL_PATH)			\
	$(LOCAL_PATH)/android		\
	$(LOCAL_PATH)/android/gobject	\
	$(LOCAL_PATH)/android-internal	\
	$(GLIB_TOP)/android-internal	\
	$(GLIB_C_INCLUDES)

LOCAL_CFLAGS :=				\
	-DHAVE_CONFIG_H			\
	-DG_LOG_DOMAIN=\"GLib-GObject\"	\
	-DGOBJECT_COMPILATION		\
	-DG_DISABLE_CONST_RETURNS	\
	-DG_DISABLE_DEPRECATED 

ifeq ($(GLIB_BUILD_STATIC),true)
LOCAL_STATIC_LIBRARIES := libglib-2.0 libgthread-2.0

include $(BUILD_STATIC_LIBRARY)
else
LOCAL_SHARED_LIBRARIES := libglib-2.0 libgthread-2.0

include $(BUILD_SHARED_LIBRARY)
endif
