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

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;



// TODO(b/176127289) add tests
@RunWith(AndroidJUnit4.class)
public class WfcActivationControllerTest {
    @Mock private TelephonyManager mTelephonyManager;
    @Mock private WfcActivationApi mActivationApi;
    @Mock private WfcActivationUi mActivationUi;
    @Mock private ConnectivityManager mConnectivityManager;
    @Mock private NetworkInfo mNetworkInfo;

    private static final String WEBVIEW_JS_CONTROLLER_NAME = "webviewJsControllerName";
    private static final int SUB_ID = 1;

    private WfcActivationController mWfcActivationController;
    private Context mContext;
    private Instrumentation mInstrumentation;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mContext = spy(ApplicationProvider.getApplicationContext());

        when(mActivationApi.getWebviewJsControllerName()).thenReturn(WEBVIEW_JS_CONTROLLER_NAME);
        when(mContext.getSystemService(TelephonyManager.class)).thenReturn(mTelephonyManager);
        when(mTelephonyManager.createForSubscriptionId(SUB_ID)).thenReturn(mTelephonyManager);
        setNetworkConnected(true);

        // now do not need to try/catch
        Field field = EntitlementUtils.class.getDeclaredField("useDirectExecutorForTest");
        field.setAccessible(true);
        field.set(null, true);
    }

    @Test
    public void startFlow_launchAppForActivation_setPurposeActivation() {
        InOrder mOrderVerifier = inOrder(mActivationUi);
        setNetworkConnected(false);
        buildActivity(ActivityConstants.LAUNCH_APP_ACTIVATE);

        mWfcActivationController.startFlow();

        verifyGeneralWaitingUi(mOrderVerifier, R.string.activate_title);
        verifyErrorUi(mOrderVerifier, R.string.activate_title, R.string.wfc_activation_error);
    }

    @Test
    public void startFlow_launchAppForUpdate_setPurposeUpdate() {
        InOrder mOrderVerifier = inOrder(mActivationUi);
        setNetworkConnected(false);
        buildActivity(ActivityConstants.LAUNCH_APP_UPDATE);

        mWfcActivationController.startFlow();

        verifyGeneralWaitingUi(mOrderVerifier, R.string.e911_title);
        verifyErrorUi(mOrderVerifier, R.string.e911_title, R.string.address_update_error);
    }

    @Test
    public void startFlow_launchAppForShowTc_setPurposeUpdate() {
        InOrder mOrderVerifier = inOrder(mActivationUi);
        setNetworkConnected(false);
        buildActivity(ActivityConstants.LAUNCH_APP_SHOW_TC);

        mWfcActivationController.startFlow();

        verifyGeneralWaitingUi(mOrderVerifier, R.string.tos_title);
        verifyErrorUi(mOrderVerifier, R.string.tos_title, R.string.show_terms_and_condition_error);
    }

    @Test
    public void finishFlow_isFinishing_showGeneralWaitingUi() {
        InOrder mOrderVerifier = inOrder(mActivationUi);
        when(mActivationApi.checkEntitlementStatus()).thenReturn(null);
        buildActivity(ActivityConstants.LAUNCH_APP_ACTIVATE);

        mWfcActivationController.finishFlow();

        mOrderVerifier
                .verify(mActivationUi)
                .showActivationUi(
                        R.string.activate_title,
                        R.string.progress_text,
                        true,
                        0,
                        Activity.RESULT_CANCELED,
                        0);
        mOrderVerifier
                .verify(mActivationUi)
                .showActivationUi(
                        R.string.activate_title,
                        R.string.wfc_activation_error,
                        false,
                        R.string.ok,
                        WfcActivationUi.RESULT_FAILURE,
                        0);
    }

    private void buildActivity(int extraLaunchCarrierApp) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.putExtra(SubscriptionManager.EXTRA_SUBSCRIPTION_INDEX, SUB_ID);
        intent.putExtra(ActivityConstants.EXTRA_LAUNCH_CARRIER_APP, extraLaunchCarrierApp);
        mWfcActivationController =
                new WfcActivationController(mContext, mActivationUi, mActivationApi, intent);
    }

    private void setNetworkConnected(boolean isConnected) {
        when(mNetworkInfo.isConnected()).thenReturn(isConnected);
        when(mContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(
                mConnectivityManager);
        when(mConnectivityManager.getActiveNetworkInfo()).thenReturn(mNetworkInfo);
        when(mNetworkInfo.isConnected()).thenReturn(isConnected);
    }

    private void verifyErrorUi(InOrder inOrder, int title, int errorMesssage) {
        inOrder.verify(mActivationUi)
                .showActivationUi(
                        title,
                        errorMesssage,
                        false, R.string.ok,
                        WfcActivationUi.RESULT_FAILURE,
                        0);
    }

    private void verifyGeneralWaitingUi(InOrder inOrder, int title) {
        inOrder.verify(mActivationUi)
                .showActivationUi(title, R.string.progress_text, true, 0, 0, 0);
    }
}
