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

package com.android.imsserviceentitlement;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.android.imsserviceentitlement.entitlement.EntitlementResult;
import com.android.imsserviceentitlement.fcm.FcmTokenStore;
import com.android.imsserviceentitlement.fcm.FcmUtils;
import com.android.imsserviceentitlement.ts43.Ts43Constants.ResponseXmlAttributes;
import com.android.imsserviceentitlement.ts43.Ts43Constants.ResponseXmlNode;
import com.android.imsserviceentitlement.ts43.Ts43VowifiStatus;
import com.android.imsserviceentitlement.utils.TelephonyUtils;
import com.android.imsserviceentitlement.utils.XmlDoc;
import com.android.libraries.entitlement.CarrierConfig;
import com.android.libraries.entitlement.ServiceEntitlement;
import com.android.libraries.entitlement.ServiceEntitlementException;
import com.android.libraries.entitlement.ServiceEntitlementRequest;

/** Implementation of the entitlement API. */
public class WfcActivationApi {
    private static final String TAG = "IMSSE-WfcActivationApi";

    private static final String JS_CONTROLLER_NAME = "VoWiFiWebServiceFlow";

    private final Context context;
    private final int subId;
    private final ServiceEntitlement serviceEntitlement;

    private String mCachedAccessToken;

    public WfcActivationApi(Context context, int subId) {
        this.context = context;
        this.subId = subId;
        CarrierConfig carrierConfig = getCarrierConfig(context);
        this.serviceEntitlement = new ServiceEntitlement(context, carrierConfig, subId);
    }

    @VisibleForTesting
    WfcActivationApi(Context context, int subId, ServiceEntitlement serviceEntitlement) {
        this.context = context;
        this.subId = subId;
        this.serviceEntitlement = serviceEntitlement;
    }

    /**
     * Returns WFC entitlement check result from carrier API (over network), or {@code null} on
     * unrecoverable network issue or malformed server response. This is blocking call so should not
     * be called on main thread.
     */
    @Nullable
    public EntitlementResult checkEntitlementStatus() {
        return voWifiEntitlementStatus();
    }

    /** Returns the name of JS controller object used in emergency address webview. */
    public String getWebviewJsControllerName() {
        return JS_CONTROLLER_NAME;
    }

    /** Query for status of {@link AppId#VOWIFI}). */
    @VisibleForTesting
    @Nullable
    EntitlementResult voWifiEntitlementStatus() {
        Log.d(TAG, "voWifiEntitlementStatus subId=" + subId);

        ServiceEntitlementRequest.Builder requestBuilder = ServiceEntitlementRequest.builder();
        if (!TextUtils.isEmpty(mCachedAccessToken)) {
            requestBuilder.setAuthenticationToken(mCachedAccessToken);
        }
        FcmUtils.fetchFcmToken(context, subId);
        requestBuilder.setNotificationToken(FcmTokenStore.getToken(context, subId));
        // Set fake device info to avoid leaking
        requestBuilder.setTerminalVendor("vendorX");
        requestBuilder.setTerminalModel("modelY");
        requestBuilder.setTerminalSoftwareVersion("versionZ");
        ServiceEntitlementRequest request = requestBuilder.build();

        XmlDoc entitlementXmlDoc = null;
        try {
            entitlementXmlDoc =
                    new XmlDoc(
                            serviceEntitlement.queryEntitlementStatus(
                                    ServiceEntitlement.APP_VOWIFI, request));
            // While finishing the initial AuthN, save the token
            // and to be used next time for fast AuthN.
            entitlementXmlDoc.get(
                        ResponseXmlNode.TOKEN,
                        ResponseXmlAttributes.TOKEN,
                        ServiceEntitlement.APP_VOWIFI)
                    .ifPresent(token -> mCachedAccessToken = token);
        } catch (ServiceEntitlementException e) {
            Log.e(TAG, "queryEntitlementStatus failed", e);
        }
        return entitlementXmlDoc == null ? null : toEntitlementResult(entitlementXmlDoc);
    }

    private static EntitlementResult toEntitlementResult(XmlDoc doc) {
        EntitlementResult.Builder builder =
                EntitlementResult.builder()
                        .setSuccess(true)
                        .setVowifiStatus(Ts43VowifiStatus.builder(doc).build());
        doc.get(
                ResponseXmlNode.APPLICATION,
                ResponseXmlAttributes.SERVER_FLOW_URL,
                ServiceEntitlement.APP_VOWIFI)
            .ifPresent(url -> builder.setEmergencyAddressWebUrl(url));
        doc.get(
                ResponseXmlNode.APPLICATION,
                ResponseXmlAttributes.SERVER_FLOW_USER_DATA,
                ServiceEntitlement.APP_VOWIFI)
            .ifPresent(userData -> builder.setEmergencyAddressWebData(userData));
        return builder.build();
    }

    private CarrierConfig getCarrierConfig(Context context) {
        String entitlementServiceUrl = TelephonyUtils.getEntitlementServerUrl(context, subId);
        return CarrierConfig.builder().setServerUrl(entitlementServiceUrl).build();
    }
}