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
 * limitations under the License
 */

package com.android.imsserviceentitlement.ts43;

import com.android.imsserviceentitlement.entitlement.VowifiStatus;
import com.android.imsserviceentitlement.ts43.Ts43Constants.ResponseXmlAttributes;
import com.android.imsserviceentitlement.ts43.Ts43Constants.ResponseXmlNode;
import com.android.imsserviceentitlement.utils.XmlDoc;

import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;

/**
 * Implementation of WFC entitlement status and server data availability for TS.43 entitlement
 * solution.
 */
@AutoValue
@VisibleForTesting
public abstract class Ts43VowifiStatus implements VowifiStatus {
    /** The entitlement status of vowifi service. */
    @VisibleForTesting
    public static class EntitlementStatus {
        public EntitlementStatus() {}

        public static final int DISABLED = 0;
        public static final int ENABLED = 1;
        public static final int INCOMPATIBLE = 2;
        public static final int PROVISIONING = 3;
    }

    /** The emergency address status of vowifi service. */
    @VisibleForTesting
    public static class AddrStatus {
        public AddrStatus() {}

        public static final int NOT_AVAILABLE = 0;
        public static final int AVAILABLE = 1;
        public static final int NOT_REQUIRED = 2;
        public static final int IN_PROGRESS = 3;
    }

    /** The terms and condition status of vowifi service. */
    @VisibleForTesting
    public static class TcStatus {
        public TcStatus() {}

        public static final int NOT_AVAILABLE = 0;
        public static final int AVAILABLE = 1;
        public static final int NOT_REQUIRED = 2;
        public static final int IN_PROGRESS = 3;
    }

    /** The provision status of vowifi service. */
    @VisibleForTesting
    public static class ProvStatus {
        public ProvStatus() {}

        public static final int NOT_PROVISIONED = 0;
        public static final int PROVISIONED = 1;
        public static final int NOT_REQUIRED = 2;
        public static final int IN_PROGRESS = 3;
    }

    /** The entitlement status of vowifi service. */
    public abstract int entitlementStatus();
    /** The terms and condition status of vowifi service. */
    public abstract int tcStatus();
    /** The emergency address status of vowifi service. */
    public abstract int addrStatus();
    /** The provision status of vowifi service. */
    public abstract int provStatus();

    public static Ts43VowifiStatus.Builder builder() {
        return new AutoValue_Ts43VowifiStatus.Builder()
                .setEntitlementStatus(EntitlementStatus.DISABLED)
                .setTcStatus(TcStatus.NOT_AVAILABLE)
                .setAddrStatus(AddrStatus.NOT_AVAILABLE)
                .setProvStatus(ProvStatus.NOT_PROVISIONED);
    }

    public static Ts43VowifiStatus.Builder builder(XmlDoc doc) {
        return builder()
                .setEntitlementStatus(
                        Integer.parseInt(
                                doc.get(ResponseXmlNode.APPLICATION,
                                        ResponseXmlAttributes.ENTITLEMENT_STATUS)))
                .setTcStatus(
                        Integer.parseInt(
                                doc.get(ResponseXmlNode.APPLICATION,
                                        ResponseXmlAttributes.TC_STATUS)))
                .setAddrStatus(
                        Integer.parseInt(
                                doc.get(ResponseXmlNode.APPLICATION,
                                        ResponseXmlAttributes.ADDR_STATUS)))
                .setProvStatus(
                        Integer.parseInt(
                                doc.get(ResponseXmlNode.APPLICATION,
                                        ResponseXmlAttributes.PROVISION_STATUS)));
    }

    /** Builder of {@link Ts43VowifiStatus}. */
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Ts43VowifiStatus build();

        public abstract Builder setEntitlementStatus(int entitlementStatus);

        public abstract Builder setTcStatus(int tcStatus);

        public abstract Builder setAddrStatus(int addrStatus);

        public abstract Builder setProvStatus(int provStatus);
    }

    @Override
    public boolean vowifiEntitled() {
        return entitlementStatus() == EntitlementStatus.ENABLED
                && (provStatus() == ProvStatus.PROVISIONED
                || provStatus() == ProvStatus.NOT_REQUIRED)
                && (tcStatus() == TcStatus.AVAILABLE || tcStatus() == TcStatus.NOT_REQUIRED)
                && (addrStatus() == AddrStatus.AVAILABLE
                || addrStatus() == AddrStatus.NOT_REQUIRED);
    }

    @Override
    public boolean serverDataMissing() {
        return entitlementStatus() == EntitlementStatus.DISABLED
                && (tcStatus() == TcStatus.NOT_AVAILABLE
                || addrStatus() == AddrStatus.NOT_AVAILABLE);
    }

    @Override
    public boolean inProgress() {
        return entitlementStatus() == EntitlementStatus.PROVISIONING
                || (entitlementStatus() == EntitlementStatus.DISABLED
                && (tcStatus() == TcStatus.IN_PROGRESS || addrStatus() == AddrStatus.IN_PROGRESS))
                || (entitlementStatus() == EntitlementStatus.DISABLED
                && (provStatus() == ProvStatus.NOT_PROVISIONED
                || provStatus() == ProvStatus.IN_PROGRESS)
                && (tcStatus() == TcStatus.AVAILABLE || tcStatus() == TcStatus.NOT_REQUIRED)
                && (addrStatus() == AddrStatus.AVAILABLE
                || addrStatus() == AddrStatus.NOT_REQUIRED));
    }

    @Override
    public boolean incompatible() {
        return entitlementStatus() == EntitlementStatus.INCOMPATIBLE;
    }

    @Override
    public final String toString() {
        return "Ts43VowifiStatus {"
                + "entitlementStatus="
                + entitlementStatus()
                + ",tcStatus="
                + tcStatus()
                + ",addrStatus="
                + addrStatus()
                + ",provStatus="
                + provStatus()
                + "}";
    }
}