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

package com.android.imsserviceentitlement.ts43;

/** Constants to be used in GSMA TS.43 protocol. */
public final class Ts43Constants {
    private Ts43Constants() {}

    /** Node types of XML response content. */
    public static final class ResponseXmlNode {
        private ResponseXmlNode() {}

        /** Node name of token. */
        public static final String TOKEN = "TOKEN";
        /** Node name of application. */
        public static final String APPLICATION = "APPLICATION";
        /** Node name of vers. */
        public static final String VERS = "VERS";
    }

    /** Attribute names of XML response content. */
    public static final class ResponseXmlAttributes {
        private ResponseXmlAttributes() {}

        /** XML attribute name of token. */
        public static final String TOKEN = "token";
        /** XML attribute name of application identifier. */
        public static final String APP_ID = "AppID";
        /** XML attribute name of entitlement status. */
        public static final String ENTITLEMENT_STATUS = "EntitlementStatus";
        /** XML attribute name of E911 address status. */
        public static final String ADDR_STATUS = "AddrStatus";
        /** XML attribute name of terms and condition status. */
        public static final String TC_STATUS = "TC_Status";
        /** XML attribute name of provision status. */
        public static final String PROVISION_STATUS = "ProvStatus";
        /** XML attribute name of entitlement server URL. */
        public static final String SERVER_FLOW_URL = "ServiceFlow_URL";
        /** XML attribute name of entitlement server user data. */
        public static final String SERVER_FLOW_USER_DATA = "ServiceFlow_UserData";
        /** XML attribute name of version. */
        public static final String VERSION = "version";
        /** XML attribute name of validity. */
        public static final String VALIDITY = "validity";
        /** XML attribute name of RATVoiceEntitleInfoDetails. */
        public static final String RAT_VOICE_ENTITLE_INFO_DETAILS = "RATVoiceEntitleInfoDetails";
        /** XML attribute name of AccessType in TS43 v8 section 4.1.1. */
        public static final String ACCESS_TYPE = "AccessType";
        /** XML attribute name of HomeRoamingNWType in TS43 v8 section 4.1.1. */
        public static final String HOME_ROAMING_NW_TYPE = "HomeRoamingNWType";
        /** XML attribute name of NetworkVoiceIRatCapability in TS43 v8 section 4.1.1. */
        public static final String NETWORK_VOICE_IRAT_CAPABILITY = "NetworkVoiceIRatCapability";
    }

    /** Value of EntitlementStatus. */
    public static final class EntitlementStatus {
        private EntitlementStatus() {}

        /** The service allowed, but not yet provisioned and activated on the network side. */
        public static final int DISABLED = 0;
        /** The service allowed, provisioned and activated on the network side. */
        public static final int ENABLED = 1;
        /** The service cannot be offered. */
        public static final int INCOMPATIBLE = 2;
        /** The service being provisioned on the network side. */
        public static final int PROVISIONING = 3;
    }

    /** Value of AccessType. */
    public static final class AccessTypeValue {
        private AccessTypeValue() {}

        /** RAT of type LTE (4G). */
        public static final String LTE = "1";

        /** RAT of type NG-RAN (5G). */
        public static final String NGRAN = "2";
    }

    /** Value of HomeRoamingNWTypeValue. */
    public static final class HomeRoamingNwTypeValue {
        private HomeRoamingNwTypeValue() {}

        /** Include both home and roaming networks. */
        public static final String ALL = "1";

        /** Home network type. */
        public static final String HOME = "2";

        /** Roaming network type. */
        public static final String ROAMING = "3";
    }

    /** Value of entitlement_version parameters */
    public static final class EntitlementVersion {
        private EntitlementVersion() {}

        /** Version 2.0. */
        public static final int ENTITLEMENT_VERSION_TWO = 2;

        /** Version 8.0. */
        public static final int ENTITLEMENT_VERSION_EIGHT = 8;
    }
}
