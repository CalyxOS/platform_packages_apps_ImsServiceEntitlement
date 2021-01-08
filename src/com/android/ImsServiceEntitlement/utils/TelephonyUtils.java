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

package com.android.imsserviceentitlement.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/** This class implements Telephony helper methods. */
public class TelephonyUtils {
    public static final String TAG = TelephonyUtils.class.getSimpleName();

    private final ConnectivityManager connectivityManager;
    private final TelephonyManager telephonyManager;

    public TelephonyUtils(Context context) {
        this(context, SubscriptionManager.INVALID_SUBSCRIPTION_ID);
    }

    public TelephonyUtils(Context context, int subId) {
        /* We can also use:
         *
         * telephonyManager = context.getSystemService(TelephonyManager.class);
         *
         * But Context#getSystemService(Class<T> serviceClass) is a final method, which cannot
         * be stubbed in Mockito. Hence it's little more dificult to test.
         */
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            telephonyManager =
                    ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE))
                            .createForSubscriptionId(subId);
        } else {
            telephonyManager = (TelephonyManager) context.getSystemService(
                    Context.TELEPHONY_SERVICE);
        }

        connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    /** Returns device timestamp in milliseconds. */
    public long getTimeStamp() {
        return System.currentTimeMillis();
    }

    /** Returns device uptime in milliseconds. */
    public long getUptimeMillis() {
        return android.os.SystemClock.uptimeMillis();
    }

    /** Returns device model name. */
    public String getDeviceName() {
        return Build.MODEL;
    }

    /** Returns device OS version. */
    public String getDeviceOsVersion() {
        return Build.VERSION.RELEASE;
    }

    /** Returns {@code true} if network is connected (cellular or WiFi). */
    public boolean isNetworkConnected() {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    /**
     * Returns the response of EAP-AKA authetication {@code data} or {@code null} on failure.
     *
     * <p>Requires permission: READ_PRIVILEGED_PHONE_STATE
     */
    public String getEapAkaAuthentication(String data) {
        return telephonyManager.getIccAuthentication(
                TelephonyManager.APPTYPE_USIM, TelephonyManager.AUTHTYPE_EAP_AKA, data);
    }

    /** Returns carrier ID. */
    public int getCarrierId() {
        return telephonyManager.getSimCarrierId();
    }

    /** Returns fine-grained carrier ID. */
    public int getSpecificCarrierId() {
        return telephonyManager.getSimSpecificCarrierId();
    }

    /**
     * Returns {@code true} if the {@code subId} still point to a actived SIM; {@code false}
     * otherwise.
     */
    public static boolean isActivedSubId(Context context, int subId) {
        SubscriptionManager subscriptionManager =
                (SubscriptionManager) context.getSystemService(
                        Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        SubscriptionInfo subInfo = subscriptionManager.getActiveSubscriptionInfo(subId);
        return subInfo != null;
    }

    /**
     * Returns the slot index for the actived {@code subId}; {@link
     * SubscriptionManager#INVALID_SIM_SLOT_INDEX} otherwise.
     */
    public static int getSlotId(Context context, int subId) {
        SubscriptionManager subscriptionManager =
                (SubscriptionManager) context.getSystemService(
                        Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        SubscriptionInfo subInfo = subscriptionManager.getActiveSubscriptionInfo(subId);
        if (subInfo != null) {
            return subInfo.getSimSlotIndex();
        }
        Log.d(TAG, "Can't find actived subscription for " + subId);
        return SubscriptionManager.INVALID_SIM_SLOT_INDEX;
    }
}