/*
 * Copyright (C) 2024 The Android Open Source Project
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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.Intent;
import android.telephony.SubscriptionManager;
import android.testing.AndroidTestingRunner;
import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.lang.reflect.Field;

@RunWith(AndroidTestingRunner.class)
public final class WfcActivationActivityTest {
    @Rule public final MockitoRule mockito = MockitoJUnit.rule();

    private static final String TAG = "IMSSE-WfcActivationActivityTest";
    private static final int SUB_ID = 1;
    private static final String EXTRA_LAUNCH_CARRIER_APP = "EXTRA_LAUNCH_CARRIER_APP";
    private static final int LAUNCH_APP_ACTIVATE = 0;
    private static final int LAUNCH_APP_UPDATE = 1;

    @Mock private WfcActivationController mMockWfcActivationController;

    private final Context mAppContext = ApplicationProvider.getApplicationContext();
    private ActivityScenario<WfcActivationActivity> mActivityScenario;

    @Before
    public void setUp() {
        when(mMockWfcActivationController.isSkipWfcActivation()).thenReturn(false);
    }

    @After
    public void tearDown() {
        if (mActivityScenario != null) {
            mActivityScenario.close();
        }
    }

    @Test
    public void launchAppForActivate_isSkipWfcActivationTrue_notDoStartFlow() {
        when(mMockWfcActivationController.isSkipWfcActivation()).thenReturn(true);
        Intent launchIntent =
                new Intent(mAppContext, WfcActivationActivity.class)
                        .putExtra(EXTRA_LAUNCH_CARRIER_APP, LAUNCH_APP_ACTIVATE)
                        .putExtra(SubscriptionManager.EXTRA_SUBSCRIPTION_INDEX, SUB_ID);
        mockWfcActivationController(mMockWfcActivationController);

        mActivityScenario = ActivityScenario.launch(launchIntent);

        verify(mMockWfcActivationController, never()).startFlow();
    }

    @Test
    public void launchAppForActivate_isSkipWfcActivationFalse_doStartFlow() {
        Intent launchIntent =
                new Intent(mAppContext, WfcActivationActivity.class)
                        .putExtra(EXTRA_LAUNCH_CARRIER_APP, LAUNCH_APP_ACTIVATE)
                        .putExtra(SubscriptionManager.EXTRA_SUBSCRIPTION_INDEX, SUB_ID);
        mockWfcActivationController(mMockWfcActivationController);

        mActivityScenario = ActivityScenario.launch(launchIntent);

        verify(mMockWfcActivationController).startFlow();
    }

    @Test
    public void launchAppForUpdate_isSkipWfcActivationTrue_doStartFlow() {
        when(mMockWfcActivationController.isSkipWfcActivation()).thenReturn(true);
        Intent launchIntent =
                new Intent(mAppContext, WfcActivationActivity.class)
                        .putExtra(EXTRA_LAUNCH_CARRIER_APP, LAUNCH_APP_UPDATE)
                        .putExtra(SubscriptionManager.EXTRA_SUBSCRIPTION_INDEX, SUB_ID);
        mockWfcActivationController(mMockWfcActivationController);

        mActivityScenario = ActivityScenario.launch(launchIntent);

        verify(mMockWfcActivationController).startFlow();
    }

    @Test
    public void launchAppForUpdate_isSkipWfcActivationFalse_doStartFlow() {
        Intent launchIntent =
                new Intent(mAppContext, WfcActivationActivity.class)
                        .putExtra(EXTRA_LAUNCH_CARRIER_APP, LAUNCH_APP_UPDATE)
                        .putExtra(SubscriptionManager.EXTRA_SUBSCRIPTION_INDEX, SUB_ID);
        mockWfcActivationController(mMockWfcActivationController);

        mActivityScenario = ActivityScenario.launch(launchIntent);

        verify(mMockWfcActivationController).startFlow();
    }

    private void mockWfcActivationController(WfcActivationController mockWfcActivationController) {
        try {
            Field field = WfcActivationActivity.class.getDeclaredField("sWfcActivationController");
            field.setAccessible(true);
            field.set(null, mockWfcActivationController);
        } catch (Exception e) {
            Log.d(TAG, "Mocking WfcActivationController failed.");
        }
    }
}