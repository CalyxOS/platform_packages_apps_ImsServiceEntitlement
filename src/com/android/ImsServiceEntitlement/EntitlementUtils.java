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

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.android.imsserviceentitlement.WfcActivationController.EntitlementResultCallback;
import com.android.imsserviceentitlement.entitlement.EntitlementResult;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Handle entitlement check */
public class EntitlementUtils {

    public static final String LOG_TAG = "IMSSE-EntitlementUtils";

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
    private static final ExecutorService DIRECT_EXECUTOR_SERVICE =
            MoreExecutors.newDirectExecutorService();
    private static ListenableFuture<EntitlementResult> checkEntitlementFuture;

    /**
     * Whether to execute entitlementCheck in caller's thread, set to true via reflection for test.
     */
    private static boolean useDirectExecutorForTest = false;

    private EntitlementUtils() {}

    public static void entitlementCheck(
            WfcActivationApi activationApi, EntitlementResultCallback callback) {
        ListeningExecutorService service =
                MoreExecutors.listeningDecorator(
                        useDirectExecutorForTest ? DIRECT_EXECUTOR_SERVICE : EXECUTOR_SERVICE);
        checkEntitlementFuture = service.submit(() -> getEntitlementStatus(activationApi));
        Futures.addCallback(
                checkEntitlementFuture,
                new FutureCallback<EntitlementResult>() {
                    @Override
                    public void onSuccess(EntitlementResult result) {
                        callback.onEntitlementResult(result);
                        checkEntitlementFuture = null;
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.w(LOG_TAG, "get entitlement status failed.", t);
                        checkEntitlementFuture = null;
                    }
                },
                DIRECT_EXECUTOR_SERVICE);
    }

    public static void cancelEntitlementCheck() {
        if (checkEntitlementFuture != null) {
            Log.i(LOG_TAG, "cancel entitlement status check.");
            checkEntitlementFuture.cancel(true);
        }
    }

    /**
     * Gets entitlement status via carrier-specific entitlement API over network; returns null on
     * network falure or other unexpected failure from entitlement API.
     */
    @WorkerThread
    @Nullable
    private static EntitlementResult getEntitlementStatus(WfcActivationApi activationApi) {
        try {
            return activationApi.checkEntitlementStatus();
        } catch (RuntimeException e) {
            Log.e("WfcActivationActivity", "getEntitlementStatus failed.", e);
            return null;
        }
    }
}