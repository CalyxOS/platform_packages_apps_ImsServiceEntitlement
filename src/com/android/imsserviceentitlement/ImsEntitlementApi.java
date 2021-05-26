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

package com.android.imsserviceentitlement;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.android.imsserviceentitlement.entitlement.EntitlementConfiguration;
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

import com.google.common.collect.ImmutableList;

/** Implementation of the entitlement API. */
public class ImsEntitlementApi {
    private static final String TAG = "IMSSE-ImsEntitlementApi";

    private static final String JS_CONTROLLER_NAME = "VoWiFiWebServiceFlow";
    private static final int RESPONSE_TOKEN_EXPIRED = 511;
    private static final int AUTHENTICATION_RETRIES = 1;

    private final Context mContext;
    private final int mSubId;
    private final ServiceEntitlement mServiceEntitlement;
    private final EntitlementConfiguration mLastEntitlementConfiguration;

    private int mRetryFullAuthenticationCount = AUTHENTICATION_RETRIES;

    public ImsEntitlementApi(Context context, int subId) {
        this.mContext = context;
        this.mSubId = subId;
        CarrierConfig carrierConfig = getCarrierConfig(context);
        this.mServiceEntitlement = new ServiceEntitlement(context, carrierConfig, subId);
        this.mLastEntitlementConfiguration = new EntitlementConfiguration(context, subId);
    }

    @VisibleForTesting
    ImsEntitlementApi(
            Context context,
            int subId,
            ServiceEntitlement serviceEntitlement,
            EntitlementConfiguration lastEntitlementConfiguration) {
        this.mContext = context;
        this.mSubId = subId;
        this.mServiceEntitlement = serviceEntitlement;
        this.mLastEntitlementConfiguration = lastEntitlementConfiguration;
    }

    /**
     * Returns WFC entitlement check result from carrier API (over network), or {@code null} on
     * unrecoverable network issue or malformed server response. This is blocking call so should not
     * be called on main thread.
     */
    @Nullable
    public EntitlementResult checkEntitlementStatus() {
        Log.d(TAG, "checkEntitlementStatus subId=" + mSubId);
        ServiceEntitlementRequest.Builder requestBuilder = ServiceEntitlementRequest.builder();
        mLastEntitlementConfiguration.getToken().ifPresent(
                token -> requestBuilder.setAuthenticationToken(token));
        FcmUtils.fetchFcmToken(mContext, mSubId);
        requestBuilder.setNotificationToken(FcmTokenStore.getToken(mContext, mSubId));
        // Set fake device info to avoid leaking
        requestBuilder.setTerminalVendor("vendorX");
        requestBuilder.setTerminalModel("modelY");
        requestBuilder.setTerminalSoftwareVersion("versionZ");
        ServiceEntitlementRequest request = requestBuilder.build();

        XmlDoc entitlementXmlDoc = null;

        try {
            String rawXml = mServiceEntitlement.queryEntitlementStatus(
                    ImmutableList.of(ServiceEntitlement.APP_VOWIFI), request);
            entitlementXmlDoc = new XmlDoc(rawXml);
            mLastEntitlementConfiguration.update(rawXml);
            // Reset the retry count if no exception from queryEntitlementStatus()
            mRetryFullAuthenticationCount = AUTHENTICATION_RETRIES;
        } catch (ServiceEntitlementException e) {
            if (e.getErrorCode()
                    == ServiceEntitlementException.ERROR_HTTP_STATUS_NOT_SUCCESS
                    && e.getHttpStatus() == RESPONSE_TOKEN_EXPIRED) {
                if (mRetryFullAuthenticationCount <= 0) {
                    Log.d(TAG, "Ran out of the retry count, stop query status.");
                    return null;
                }
                Log.d(TAG, "Server asking for full authentication, retry the query.");
                // Clean up the cached data and perform full authentication next query.
                mLastEntitlementConfiguration.reset();
                mRetryFullAuthenticationCount--;
                return checkEntitlementStatus();
            }
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
        String entitlementServiceUrl = TelephonyUtils.getEntitlementServerUrl(context, mSubId);
        return CarrierConfig.builder().setServerUrl(entitlementServiceUrl).build();
    }

    /** Returns the name of JS controller object used in emergency address webview. */
    public String getWebviewJsControllerName() {
        return JS_CONTROLLER_NAME;
    }
}
