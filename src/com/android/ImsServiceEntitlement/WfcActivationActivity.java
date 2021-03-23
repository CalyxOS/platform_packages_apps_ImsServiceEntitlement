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

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.StringRes;

import com.google.android.setupdesign.util.ThemeResolver;

/** The UI for WFC activation. */
public class WfcActivationActivity extends FragmentActivity implements WfcActivationUi {
    private static final String TAG = "IMSSE-WfcActivationActivity";

    // Dependencies
    private WfcActivationController wfcActivationController;
    private WfcWebPortalFragment wfcWebPortalFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        createDependeny();
        setSuwTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wfc_activation);

        int subId = ActivityConstants.getSubId(getIntent());
        wfcActivationController.startFlow();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wfcActivationController.finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (wfcWebPortalFragment != null && wfcWebPortalFragment.onKeyDown(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean showActivationUi(
            @StringRes int title,
            @StringRes int text,
            boolean isInProgress,
            @StringRes int primaryButtonText,
            int primaryButtonResult,
            @StringRes int secondaryButtonText) {
        runOnUiThreadIfAlive(
                () -> {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    SuwUiFragment frag =
                            SuwUiFragment.newInstance(
                                    title,
                                    text,
                                    isInProgress,
                                    primaryButtonText,
                                    primaryButtonResult,
                                    secondaryButtonText);
                    ft.replace(R.id.wfc_activation_container, frag);
                    // commit may be executed after activity's state is saved.
                    ft.commitAllowingStateLoss();
                });
        return true;
    }

    @Override
    public boolean showWebview(String url, String postData, String jsControllerName) {
        runOnUiThreadIfAlive(
                () -> {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    wfcWebPortalFragment = WfcWebPortalFragment.newInstance(
                            url,
                            postData,
                            jsControllerName);
                    ft.replace(R.id.wfc_activation_container, wfcWebPortalFragment);
                    // commit may be executed after activity's state is saved.
                    ft.commitAllowingStateLoss();
                });
        return true;
    }

    private void runOnUiThreadIfAlive(Runnable r) {
        if (!isFinishing() && !isDestroyed()) {
            runOnUiThread(r);
        }
    }

    @Override
    public void setResultAndFinish(int resultCode) {
        Log.d(TAG, "setResultAndFinish: result=" + resultCode);
        if (!isFinishing() && !isDestroyed()) {
            setResult(resultCode);
            finish();
        }
    }

    @Override
    public WfcActivationController getController() {
        return wfcActivationController;
    }

    private void setSuwTheme() {
        int theme =
                ThemeResolver.getDefault().resolve(
                        SystemProperties.get("setupwizard.theme"),
                        false);
        setTheme(theme != 0 ? theme : R.style.SudThemeGlif_Light);
    }

    private void createDependeny() {
        Log.d(TAG, "Loading dependencies...");
        // TODO(b/177495634) Use DependencyInjector
        if (wfcActivationController == null) {
            // Default initialization
            Log.d(TAG, "Default WfcActivationController initialization");
            Intent startIntent = this.getIntent();
            int subId = ActivityConstants.getSubId(startIntent);
            wfcActivationController =
                    new WfcActivationController(
                            /* context = */ this,
                            /* wfcActivationUi = */ this,
                            new WfcActivationApi(this, subId),
                            this.getIntent());
        }
    }
}