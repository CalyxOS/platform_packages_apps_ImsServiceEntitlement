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

import static com.android.imsserviceentitlement.entitlement.EntitlementConfiguration.ClientBehavior.NEEDS_TO_RESET;
import static com.android.imsserviceentitlement.entitlement.EntitlementConfiguration.ClientBehavior.VALID_DURING_VALIDITY;
import static com.google.common.truth.Truth.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.runner.AndroidJUnit4;

import com.android.imsserviceentitlement.entitlement.EntitlementConfiguration;
import com.android.imsserviceentitlement.entitlement.EntitlementResult;
import com.android.imsserviceentitlement.fcm.FcmTokenStore;
import com.android.libraries.entitlement.ServiceEntitlement;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(AndroidJUnit4.class)
public class ImsEntitlementApiTest {
    @Rule public final MockitoRule rule = MockitoJUnit.rule();
    @Mock private ServiceEntitlement mMockServiceEntitlement;

    private static final int SUB_ID = 1;
    private static final String FCM_TOKEN = "FCM_TOKEN";
    private static final String RAW_XML =
            "<wap-provisioningdoc version=\"1.1\">\n"
                    + "    <characteristic type=\"VERS\">\n"
                    + "        <parm name=\"version\" value=\"1\"/>\n"
                    + "        <parm name=\"validity\" value=\"1728000\"/>\n"
                    + "    </characteristic>\n"
                    + "    <characteristic type=\"TOKEN\">\n"
                    + "        <parm name=\"token\" value=\"kZYfCEpSsMr88KZVmab5UsZVzl+nWSsX\"/>\n"
                    + "        <parm name=\"validity\" value=\"3600\"/>\n"
                    + "    </characteristic>\n"
                    + "    <characteristic type=\"APPLICATION\">\n"
                    + "        <parm name=\"AppID\" value=\"ap2004\"/>\n"
                    + "        <parm name=\"EntitlementStatus\" value=\"1\"/>\n"
                    + "    </characteristic>\n"
                    + "</wap-provisioningdoc>\n";

    private final Context mContext = ApplicationProvider.getApplicationContext();
    private final EntitlementConfiguration mEntitlementConfiguration =
            new EntitlementConfiguration(ApplicationProvider.getApplicationContext(), SUB_ID);

    private ImsEntitlementApi mImsEntitlementApi;

    @Before
    public void setUp() {
        mImsEntitlementApi =
                new ImsEntitlementApi(
                        mContext,
                        SUB_ID,
                        mMockServiceEntitlement,
                        mEntitlementConfiguration);
        FcmTokenStore.setToken(mContext, SUB_ID, FCM_TOKEN);
    }

    @Test
    public void checkEntitlementStatus_verifyVowifiStatus() throws Exception {
        when(mMockServiceEntitlement.queryEntitlementStatus(
                eq(ImmutableList.of(ServiceEntitlement.APP_VOWIFI)), any())).thenReturn(RAW_XML);

        EntitlementResult result = mImsEntitlementApi.checkEntitlementStatus();

        assertThat(result.getVowifiStatus().vowifiEntitled()).isTrue();
    }

    @Test
    public void checkEntitlementStatus_verifyConfigs() throws Exception {
        when(mMockServiceEntitlement.queryEntitlementStatus(
                eq(ImmutableList.of(ServiceEntitlement.APP_VOWIFI)), any())).thenReturn(RAW_XML);

        EntitlementResult result = mImsEntitlementApi.checkEntitlementStatus();

        assertThat(mEntitlementConfiguration.getVoWifiStatus()).isEqualTo(1);
        assertThat(mEntitlementConfiguration.getVolteStatus()).isEqualTo(2);
        assertThat(mEntitlementConfiguration.getSmsOverIpStatus()).isEqualTo(2);
        assertThat(mEntitlementConfiguration.getToken().get()).isEqualTo(
                "kZYfCEpSsMr88KZVmab5UsZVzl+nWSsX");
        assertThat(mEntitlementConfiguration.getTokenValidity()).isEqualTo(3600);
        assertThat(mEntitlementConfiguration.entitlementValidation()).isEqualTo(
                VALID_DURING_VALIDITY);
    }

    @Test
    public void checkEntitlementStatus_resultNull_verifyVowifiStatusAndConfigs() throws Exception {
        when(mMockServiceEntitlement.queryEntitlementStatus(
                eq(ImmutableList.of(ServiceEntitlement.APP_VOWIFI)), any())).thenReturn(null);

        EntitlementResult result = mImsEntitlementApi.checkEntitlementStatus();

        assertThat(result.getVowifiStatus().vowifiEntitled()).isFalse();
        assertThat(mEntitlementConfiguration.getVoWifiStatus()).isEqualTo(2);
        assertThat(mEntitlementConfiguration.getVolteStatus()).isEqualTo(2);
        assertThat(mEntitlementConfiguration.getSmsOverIpStatus()).isEqualTo(2);
        assertThat(mEntitlementConfiguration.getToken().isPresent()).isFalse();
        assertThat(mEntitlementConfiguration.getTokenValidity()).isEqualTo(0);
        assertThat(mEntitlementConfiguration.entitlementValidation()).isEqualTo(NEEDS_TO_RESET);
    }
}
