LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := android.hardware.light@2.0-service.icesky
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_RELATIVE_PATH := hw

LOCAL_VENDOR_MODULE := true

LOCAL_INIT_RC := android.hardware.light@2.0-service.icesky.rc

LOCAL_SRC_FILES := \
    service.cpp \
    Light.cpp

LOCAL_SHARED_LIBRARIES := \
    libbase \
    libcutils \
    libhardware \
    libhidlbase \
    libutils \
    android.hardware.light@2.0 \

include $(BUILD_EXECUTABLE)
