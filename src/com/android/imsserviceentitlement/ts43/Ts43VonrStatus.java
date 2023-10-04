/*
 * Copyright (C) 2023 The Android Open Source Project
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

package com.android.imsserviceentitlement.ts43;

import static com.android.imsserviceentitlement.ts43.Ts43Constants.ResponseXmlAttributes.ENTITLEMENT_STATUS;
import static com.android.imsserviceentitlement.ts43.Ts43Constants.ResponseXmlAttributes.NETWORK_VOICE_IRAT_CAPABILITY;

import com.android.imsserviceentitlement.utils.XmlDoc;
import com.android.imsserviceentitlement.ts43.Ts43Constants.EntitlementStatus;

import com.google.auto.value.AutoValue;

/**
 * Implementation of Vonr entitlement status and server data availability for TS.43 entitlement
 * solution. This class is only used to report the entitlement status of Vonr.
 */
@AutoValue
public abstract class Ts43VonrStatus {
    /** The entitlement status of Vonr service in Home network. */
    public abstract int homeEntitlementStatus();

    /** The entitlement status of Vonr service in Roaming network.. */
    public abstract int roamingEntitlementStatus();

    /** The voice rat capability status of vonr service in home network. */
    public abstract String homeNetworkVoiceIRatCapability();

    /** The voice rat capability status of vonr service in roaming network. */
    public abstract String roamingNetworkVoiceIRatCapability();

    public static Ts43VonrStatus.Builder builder() {
        return new AutoValue_Ts43VonrStatus.Builder()
                .setHomeEntitlementStatus(EntitlementStatus.INCOMPATIBLE)
                .setRoamingNetworkVoiceIRatCapability("")
                .setRoamingEntitlementStatus(EntitlementStatus.INCOMPATIBLE)
                .setHomeNetworkVoiceIRatCapability("");
    }

    public static Ts43VonrStatus.Builder builder(XmlDoc doc) {
        return builder()
                .setHomeEntitlementStatus(
                        doc.getFromVonrHome(ENTITLEMENT_STATUS)
                                .map(status -> Integer.parseInt(status))
                                .orElse(EntitlementStatus.INCOMPATIBLE))
                .setHomeNetworkVoiceIRatCapability(
                        doc.getFromVonrHome(NETWORK_VOICE_IRAT_CAPABILITY).orElse(""))
                .setRoamingEntitlementStatus(
                        doc.getFromVonrRoaming(ENTITLEMENT_STATUS)
                                .map(status -> Integer.parseInt(status))
                                .orElse(EntitlementStatus.INCOMPATIBLE))
                .setRoamingNetworkVoiceIRatCapability(
                        doc.getFromVonrRoaming(NETWORK_VOICE_IRAT_CAPABILITY).orElse(""));
    }

    /** Builder of {@link Ts43VonrStatus}. */
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Ts43VonrStatus build();

        public abstract Builder setHomeEntitlementStatus(int entitlementStatus);

        public abstract Builder setRoamingEntitlementStatus(int entitlementStatus);

        public abstract Builder setHomeNetworkVoiceIRatCapability(
                String networkVoiceIRatCapability);

        public abstract Builder setRoamingNetworkVoiceIRatCapability(
                String networkVoiceIRatCapability);
    }

    public boolean isHomeActive() {
        return homeEntitlementStatus() == EntitlementStatus.ENABLED;
    }

    public boolean isRoamingActive() {
        return roamingEntitlementStatus() == EntitlementStatus.ENABLED;
    }

    public final String toString() {
        return "Ts43VonrStatus {"
                + "HomeEntitlementStatus="
                + homeEntitlementStatus()
                + ", HomeNetworkVoiceIRatCapability="
                + homeNetworkVoiceIRatCapability()
                + ", RomaingEntitlementStatus="
                + roamingEntitlementStatus()
                + ", RoamingNetworkVoiceIRatCapability="
                + roamingNetworkVoiceIRatCapability()
                + "}";
    }
}
