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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.job.JobParameters;
import android.content.Context;
import android.os.PersistableBundle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.SparseArray;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.runner.AndroidJUnit4;

import com.android.imsserviceentitlement.entitlement.EntitlementResult;
import com.android.imsserviceentitlement.entitlement.VowifiStatus;
import com.android.imsserviceentitlement.job.JobManager;
import com.android.imsserviceentitlement.utils.ImsUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.lang.reflect.Field;

@RunWith(AndroidJUnit4.class)
public class ImsEntitlementPollingServiceTest {
    @Rule public final MockitoRule rule = MockitoJUnit.rule();

    @Spy private Context mContext = ApplicationProvider.getApplicationContext();

    @Mock private ImsUtils mImsUtils;
    @Mock private JobParameters mJobParameters;
    @Mock private SubscriptionManager mSubscriptionManager;
    @Mock private SubscriptionInfo mSubscriptionInfo;
    @Mock private WfcActivationApi mWfcActivationApi;

    private ImsEntitlementPollingService mService;

    private static final int SUB_ID = 1;
    private static final int SLOT_ID = 0;

    @Before
    public void setup() throws Exception {
        mService = new ImsEntitlementPollingService();
        mService.attachBaseContext(mContext);
        mService.onCreate();
        mService.onBind(null);
        mService.injectWfcActivationApi(mWfcActivationApi);
        setActivedSubscription();
        setupImsUtils();
        setJobParameters();
        setWfcEnabledByUser(true);
    }

    @Test
    public void doEntitlementCheck_isWfcEnabledByUserFalse_doNothing() throws Exception {
        setWfcEnabledByUser(false);

        mService.onStartJob(mJobParameters);
        mService.mOngoingTask.get(); // wait for job finish.

        verify(mWfcActivationApi, never()).checkEntitlementStatus();
    }


    @Test
    public void doEntitlementCheck_shouldTurnOffWfc_disableWfc() throws Exception {
        EntitlementResult entitlementResult = getEntitlementResult(sDisableVoWiFi);
        when(mWfcActivationApi.checkEntitlementStatus()).thenReturn(entitlementResult);

        mService.onStartJob(mJobParameters);
        mService.mOngoingTask.get(); // wait for job finish.

        verify(mImsUtils).disableWfc();
    }

    @Test
    public void doEntitlementCheck_shouldNotTurnOffWfc_enableWfc() throws Exception {
        EntitlementResult entitlementResult = getEntitlementResult(sEnableVoWiFi);
        when(mWfcActivationApi.checkEntitlementStatus()).thenReturn(entitlementResult);

        mService.onStartJob(mJobParameters);
        mService.mOngoingTask.get(); // wait for job finish.

        verify(mImsUtils, never()).disableWfc();
    }

    private void setActivedSubscription() {
        when(mSubscriptionInfo.getSimSlotIndex()).thenReturn(SLOT_ID);
        when(mSubscriptionManager.getActiveSubscriptionInfo(SUB_ID)).thenReturn(mSubscriptionInfo);
        when(mContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE))
                .thenReturn(mSubscriptionManager);
    }

    private void setupImsUtils() throws Exception {
        SparseArray<ImsUtils> imsUtilsInstances = new SparseArray<>();
        imsUtilsInstances.put(SUB_ID, mImsUtils);
        Field field = ImsUtils.class.getDeclaredField("instances");
        field.setAccessible(true);
        field.set(null, imsUtilsInstances);
    }

    private void setWfcEnabledByUser(boolean isEnabled) {
        when(mImsUtils.isWfcEnabledByUser()).thenReturn(isEnabled);
    }

    private void setJobParameters() {
        PersistableBundle bundle = new PersistableBundle();
        bundle.putInt(SubscriptionManager.EXTRA_SUBSCRIPTION_INDEX, SUB_ID);
        bundle.putInt(JobManager.EXTRA_SLOT_ID, SLOT_ID);
        when(mJobParameters.getExtras()).thenReturn(bundle);
        when(mJobParameters.getJobId()).thenReturn(JobManager.QUERY_ENTITLEMEN_STATUS_JOB_ID);
    }

    private static EntitlementResult getEntitlementResult(VowifiStatus vowifiStatus) {
        return EntitlementResult.builder()
                .setSuccess(true)
                .setVowifiStatus(vowifiStatus)
                .build();
    }

    private static final VowifiStatus sDisableVoWiFi =
            new VowifiStatus() {
                @Override
                public boolean vowifiEntitled() {
                    return true;
                }

                @Override
                public boolean serverDataMissing() {
                    return true;
                }

                @Override
                public boolean inProgress() {
                    return true;
                }

                @Override
                public boolean incompatible() {
                    return true;
                }
            };

    private static final VowifiStatus sEnableVoWiFi =
            new VowifiStatus() {
                @Override
                public boolean vowifiEntitled() {
                    return true;
                }

                @Override
                public boolean serverDataMissing() {
                    return false;
                }

                @Override
                public boolean inProgress() {
                    return false;
                }

                @Override
                public boolean incompatible() {
                    return false;
                }
            };
}
