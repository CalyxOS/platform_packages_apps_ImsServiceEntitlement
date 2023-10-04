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

import com.android.imsserviceentitlement.ts43.Ts43Constants.EntitlementStatus;
import com.android.imsserviceentitlement.ts43.Ts43SmsOverIpStatus;
import com.android.imsserviceentitlement.ts43.Ts43VolteStatus;
import com.android.imsserviceentitlement.ts43.Ts43VonrStatus;
import com.android.imsserviceentitlement.ts43.Ts43VowifiStatus;

import com.google.auto.value.AutoValue;

/** The result of the entitlement status check. */
@AutoValue
public abstract class EntitlementResult {
    private static final Ts43VowifiStatus INACTIVE_VOWIFI_STATUS =
            Ts43VowifiStatus.builder()
                    .setEntitlementStatus(EntitlementStatus.INCOMPATIBLE)
                    .setTcStatus(Ts43VowifiStatus.TcStatus.NOT_AVAILABLE)
                    .setAddrStatus(Ts43VowifiStatus.AddrStatus.NOT_AVAILABLE)
                    .setProvStatus(Ts43VowifiStatus.ProvStatus.NOT_PROVISIONED)
                    .build();

    private static final Ts43VowifiStatus ACTIVE_VOWIFI_STATUS =
            Ts43VowifiStatus.builder()
                    .setEntitlementStatus(EntitlementStatus.ENABLED)
                    .setTcStatus(Ts43VowifiStatus.TcStatus.NOT_REQUIRED)
                    .setAddrStatus(Ts43VowifiStatus.AddrStatus.NOT_REQUIRED)
                    .setProvStatus(Ts43VowifiStatus.ProvStatus.NOT_REQUIRED)
                    .build();

    private static final Ts43VolteStatus INACTIVE_VOLTE_STATUS =
            Ts43VolteStatus.builder()
                    .setEntitlementStatus(EntitlementStatus.INCOMPATIBLE)
                    .build();

    private static final Ts43VolteStatus ACTIVE_VOLTE_STATUS =
            Ts43VolteStatus.builder()
                    .setEntitlementStatus(EntitlementStatus.ENABLED)
                    .build();

    private static final Ts43VonrStatus INACTIVE_VONR_STATUS =
            Ts43VonrStatus.builder()
                    .setHomeEntitlementStatus(EntitlementStatus.INCOMPATIBLE)
                    .setRoamingEntitlementStatus(EntitlementStatus.INCOMPATIBLE)
                    .build();

    private static final Ts43VonrStatus ACTIVE_VONR_STATUS =
            Ts43VonrStatus.builder()
                    .setHomeEntitlementStatus(EntitlementStatus.ENABLED)
                    .setRoamingEntitlementStatus(EntitlementStatus.ENABLED)
                    .build();

    private static final Ts43SmsOverIpStatus INACTIVE_SMSOVERIP_STATUS =
            Ts43SmsOverIpStatus.builder()
                    .setEntitlementStatus(EntitlementStatus.INCOMPATIBLE)
                    .build();

    private static final Ts43SmsOverIpStatus ACTIVE_SMSOVERIP_STATUS =
            Ts43SmsOverIpStatus.builder()
                    .setEntitlementStatus(EntitlementStatus.ENABLED)
                    .build();

    /** Returns a new {@link Builder} object. */
    public static Builder builder(boolean isDefaultEnabled) {
        return new AutoValue_EntitlementResult.Builder()
                .setVowifiStatus(isDefaultEnabled ? ACTIVE_VOWIFI_STATUS : INACTIVE_VOWIFI_STATUS)
                .setVolteStatus(isDefaultEnabled ? ACTIVE_VOLTE_STATUS : INACTIVE_VOLTE_STATUS)
                .setSmsoveripStatus(
                        isDefaultEnabled ? ACTIVE_SMSOVERIP_STATUS : INACTIVE_SMSOVERIP_STATUS)
                .setVonrStatus(isDefaultEnabled ? ACTIVE_VONR_STATUS : INACTIVE_VONR_STATUS)
                .setEmergencyAddressWebUrl("")
                .setEmergencyAddressWebData("")
                .setTermsAndConditionsWebUrl("")
                .setRetryAfterSeconds(-1);
    }

    /** The entitlement and service status of VoWiFi. */
    public abstract Ts43VowifiStatus getVowifiStatus();
    /** The entitlement and service status of VoLTE. */
    public abstract Ts43VolteStatus getVolteStatus();
    /** The entitlement and service status of VoNR. */
    public abstract Ts43VonrStatus getVonrStatus();
    /** The entitlement and service status of SMSoIP. */
    public abstract Ts43SmsOverIpStatus getSmsoveripStatus();
    /** The URL to the WFC emergency address web form. */
    public abstract String getEmergencyAddressWebUrl();
    /** The data associated with the POST request to the WFC emergency address web form. */
    public abstract String getEmergencyAddressWebData();
    /** The URL to the WFC T&C web form. */
    public abstract String getTermsAndConditionsWebUrl();
    /** Service temporary unavailable, retry the status check after a delay in seconds. */
    public abstract long getRetryAfterSeconds();

    /** Builder of {@link EntitlementResult}. */
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract EntitlementResult build();
        public abstract Builder setVowifiStatus(Ts43VowifiStatus vowifiStatus);
        public abstract Builder setVolteStatus(Ts43VolteStatus volteStatus);
        public abstract Builder setVonrStatus(Ts43VonrStatus vonrStatus);
        public abstract Builder setSmsoveripStatus(Ts43SmsOverIpStatus smsoveripStatus);
        public abstract Builder setEmergencyAddressWebUrl(String emergencyAddressWebUrl);
        public abstract Builder setEmergencyAddressWebData(String emergencyAddressWebData);
        public abstract Builder setTermsAndConditionsWebUrl(String termsAndConditionsWebUrl);
        public abstract Builder setRetryAfterSeconds(long retryAfter);
    }

    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder("EntitlementResult{");
        builder.append(",getVowifiStatus=").append(getVowifiStatus());
        builder.append(",getVolteStatus=").append(getVolteStatus());
        builder.append(",getVonrStatus=").append(getVonrStatus());
        builder.append(",getSmsoveripStatus=").append(getSmsoveripStatus());
        builder.append(",getEmergencyAddressWebUrl=").append(opaque(getEmergencyAddressWebUrl()));
        builder.append(",getEmergencyAddressWebData=").append(opaque(getEmergencyAddressWebData()));
        builder.append(",getTermsAndConditionsWebUrl=").append(getTermsAndConditionsWebUrl());
        builder.append(",getRetryAfter=").append(getRetryAfterSeconds());
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
