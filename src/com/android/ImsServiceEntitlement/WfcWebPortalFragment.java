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

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

/** A fragment of WebView to render WFC T&C and emergency address web portal */
public class WfcWebPortalFragment extends Fragment {
    private static final String TAG = "IMSSE-WfcWebPortalFragment";

    private static final String KEY_URL_STRING = "url";
    private static final String KEY_POST_DATA_STRING = "post_data";
    private static final String KEY_JS_CALLBACK_OBJECT_STRING = "js_callback_object";

    private static final String URL_WITH_PDF_FILE_EXTENSION = ".pdf";

    private WebView webView;
    private boolean finishFlow = false;

    /** Public static constructor */
    public static WfcWebPortalFragment newInstance(
            String url, String postData, String jsControllerName) {
        WfcWebPortalFragment frag = new WfcWebPortalFragment();

        Bundle args = new Bundle();
        args.putString(KEY_URL_STRING, url);
        args.putString(KEY_POST_DATA_STRING, postData);
        args.putString(KEY_JS_CALLBACK_OBJECT_STRING, jsControllerName);
        frag.setArguments(args);

        return frag;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_webview, container, false);

        Bundle arguments = getArguments();
        Log.d(TAG, "Webview arguments: " + arguments);
        String url = arguments.getString(KEY_URL_STRING, "");
        String postData = arguments.getString(KEY_POST_DATA_STRING, "");
        String jsCallbackObject = arguments.getString(KEY_JS_CALLBACK_OBJECT_STRING, "");

        ProgressBar spinner = v.findViewById(R.id.loadingbar);
        webView = v.findViewById(R.id.webview);
        webView.setWebViewClient(
                new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        return false; // Let WebView handle redirected URL
                    }

                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        spinner.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        spinner.setVisibility(View.GONE);
                        super.onPageFinished(view, url);
                    }
                });
        webView.addOnAttachStateChangeListener(
                new OnAttachStateChangeListener() {
                    @Override
                    public void onViewAttachedToWindow(View v) {
                    }

                    @Override
                    public void onViewDetachedFromWindow(View v) {
                        Log.d(TAG, "#onViewDetachedFromWindow");
                        if (!finishFlow) {
                            ((WfcActivationUi) getActivity()).setResultAndFinish(
                                    Activity.RESULT_CANCELED);
                        }
                    }
                });
        webView.addJavascriptInterface(new JsInterface(getActivity()), jsCallbackObject);
        WebSettings settings = webView.getSettings();
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptEnabled(true);

        if (TextUtils.isEmpty(postData)) {
            webView.loadUrl(url);
        } else {
            webView.postUrl(url, postData.getBytes());
        }
        return v;
    }

    /**
     * To support webview handle back key to go back previous page.
     *
     * @return {@code true} let activity not do anything for this key down.
     *         {@code false} activity should handle key down.
     */
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            if (webView != null
                    && webView.canGoBack()
                    && webView.getUrl().toLowerCase().endsWith(URL_WITH_PDF_FILE_EXTENSION)) {
                webView.goBack();
                return true;
            }
        }
        return false;
    }

    /** Emergency address websheet javascript callback. */
    private class JsInterface {
        private final WfcActivationUi ui;

        JsInterface(Activity activity) {
            ui = (WfcActivationUi) activity;
        }

        /**
         * Callback function when the VoWiFi service flow ends properly between the device and the
         * VoWiFi portal web server.
         */
        @JavascriptInterface
        public void entitlementChanged() {
            Log.d(TAG, "#entitlementChanged");
            finishFlow = true;
            ui.getController().finishFlow();
        }

        /**
         * Callback function when the VoWiFi service flow ends prematurely, either by user
         * action or due to a web sheet or network error.
         */
        @JavascriptInterface
        public void dismissFlow() {
            Log.d(TAG, "#dismissFlow");
            ui.setResultAndFinish(Activity.RESULT_CANCELED);
        }
    }
}