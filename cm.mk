#
# Copyright 2018 The MoKee Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Release name
PRODUCT_RELEASE_NAME := icesky

$(call inherit-product, build/target/product/core_64_bit.mk)
$(call inherit-product, build/target/product/full_base_telephony.mk)

# Inherit from our custom product configuration
$(call inherit-product, vendor/cm/config/common.mk)

## Device identifier. This must come after all inclusions
PRODUCT_DEVICE := icesky
PRODUCT_NAME := cm_icesky
PRODUCT_BRAND := Smartisan
PRODUCT_MODEL := SM801
PRODUCT_MANUFACTURER := smartisan

# Sign bootable images
PRODUCT_SUPPORTS_BOOT_SIGNER := true
PRODUCT_VERITY_SIGNING_KEY := build/target/product/security/verity
