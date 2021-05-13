/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.imsserviceentitlement.entitlement;

import com.google.auto.value.AutoValue;

/** The result of the entitlement status check. */
@AutoValue
public abstract class EntitlementResult {
    private static final VowifiStatus INACTIVE_VOWIFI_STATUS =
            new VowifiStatus() {
                @Override
                public boolean vowifiEntitled() {
                    return false;
                }

                @Override
                public boolean serverDataMissing() {
                    return false;
                }

                @Override
                public boolean inProgress() {
                    return true;
                }

                @Override
                public boolean incompatible() {
                    return false;
                }
            };

    /** Returns a new {@link Builder} object. */
    public static Builder builder() {
        return new AutoValue_EntitlementResult.Builder()
                .setSuccess(false)
                .setVowifiStatus(INACTIVE_VOWIFI_STATUS)
                .setPollInterval(0)
                .setEmergencyAddressWebUrl("")
                .setEmergencyAddressWebData("")
                .setTermsAndConditionsWebUrl("");
    }

    /** Indicates this entitlement query succeeded or failed. */
    public abstract boolean isSuccess();
    /** The entitlement and service status of Vowifi. */
    public abstract VowifiStatus getVowifiStatus();
    /** The interval for scheduling polling job. */
    public abstract int getPollInterval();
    /** The URL to the WFC emergency address web form. */
    public abstract String getEmergencyAddressWebUrl();
    /** The data associated with the POST request to the WFC emergency address web form. */
    public abstract String getEmergencyAddressWebData();
    /** The URL to the WFC T&C web form. */
    public abstract String getTermsAndConditionsWebUrl();

    /** Builder of {@link EntitlementResult}. */
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract EntitlementResult build();
        public abstract Builder setSuccess(boolean success);
        public abstract Builder setVowifiStatus(VowifiStatus vowifiStatus);
        public abstract Builder setPollInterval(int pollInterval);
        public abstract Builder setEmergencyAddressWebUrl(String emergencyAddressWebUrl);
        public abstract Builder setEmergencyAddressWebData(String emergencyAddressWebData);
        public abstract Builder setTermsAndConditionsWebUrl(String termsAndConditionsWebUrl);
    }

    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder("EntitlementResult{");
        builder.append("isSuccess=").append(isSuccess());
        builder.append(",getVowifiStatus=").append(getVowifiStatus());
        builder.append(",getEmergencyAddressWebUrl=").append(opaque(getEmergencyAddressWebUrl()));
        builder.append(",getEmergencyAddressWebData=").append(opaque(getEmergencyAddressWebData()));
        builder.append(",getPollInterval=").append(getPollInterval());
        builder.append(",getTermsAndConditionsWebUrl=").append(getTermsAndConditionsWebUrl());
        builder.append("}");
        return builder.toString();
    }

    private static String opaque(String string) {
        if (string == null) {
            return "null";
        }
        return "string_of_length_" + string.length();
    }
}
