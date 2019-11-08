#!/bin/bash
#
# Copyright (C) 2018-2019 The MoKee Open Source Project
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

set -e

DEVICE=icesky
VENDOR=smartisan

# Load extract_utils and do some sanity checks
MY_DIR="${BASH_SOURCE%/*}"
if [[ ! -d "$MY_DIR" ]]; then MY_DIR="$PWD"; fi

MK_ROOT="$MY_DIR"/../../..

HELPER="$MK_ROOT"/vendor/mokee/build/tools/extract_utils.sh
if [ ! -f "$HELPER" ]; then
    echo "Unable to find helper script at $HELPER"
    exit 1
fi
. "$HELPER"

# Default to sanitizing the vendor folder before extraction
CLEAN_VENDOR=true

SRC=$1
SRC_QC=$2

if [ -z "$SRC" ]; then
    SRC=adb
fi

# Initialize the helper
setup_vendor "$DEVICE" "$VENDOR" "$MK_ROOT" false "$CLEAN_VENDOR"

extract "$MY_DIR"/proprietary-files.txt "$SRC" "$SECTION"
extract "$MY_DIR"/proprietary-files-qc.txt "$SRC_QC" "$SECTION"

"$MY_DIR"/setup-makefiles.sh

BLOB_ROOT="$MK_ROOT"/vendor/"$VENDOR"/"$DEVICE"/proprietary

# Audio
sed -i 's|/system/etc/|/vendor/etc/|g' $BLOB_ROOT/vendor/lib/hw/audio.primary.msm8992.so
sed -i 's|/system/etc/|/vendor/etc/|g' $BLOB_ROOT/vendor/lib/libacdbloader.so
sed -i 's|/system/etc/|/vendor/etc/|g' $BLOB_ROOT/vendor/lib64/hw/audio.primary.msm8992.so
sed -i 's|/system/etc/|/vendor/etc/|g' $BLOB_ROOT/vendor/lib64/libacdbloader.so
sed -i 's|/system/lib/|/vendor/lib/|g' $BLOB_ROOT/vendor/lib/hw/audio.primary.msm8992.so
sed -i 's|/system/lib/|/vendor/lib/|g' $BLOB_ROOT/vendor/lib64/hw/audio.primary.msm8992.so

# QMI
sed -i 's|qcom_diag|oem_2950|g' $BLOB_ROOT/vendor/etc/permissions/qti_permissions.xml
