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

import static com.android.imsserviceentitlement.ImsServiceEntitlementStatsLog.IMS_SERVICE_ENTITLEMENT_UPDATED;
import static com.android.imsserviceentitlement.ImsServiceEntitlementStatsLog.IMS_SERVICE_ENTITLEMENT_UPDATED__APP_RESULT__CANCELED;
import static com.android.imsserviceentitlement.ImsServiceEntitlementStatsLog.IMS_SERVICE_ENTITLEMENT_UPDATED__APP_RESULT__DISABLED;
import static com.android.imsserviceentitlement.ImsServiceEntitlementStatsLog.IMS_SERVICE_ENTITLEMENT_UPDATED__APP_RESULT__ENABLED;
import static com.android.imsserviceentitlement.ImsServiceEntitlementStatsLog.IMS_SERVICE_ENTITLEMENT_UPDATED__APP_RESULT__FAILED;
import static com.android.imsserviceentitlement.ImsServiceEntitlementStatsLog.IMS_SERVICE_ENTITLEMENT_UPDATED__APP_RESULT__UNKNOWN_RESULT;
import static com.android.imsserviceentitlement.ImsServiceEntitlementStatsLog.IMS_SERVICE_ENTITLEMENT_UPDATED__PURPOSE__POLLING;
import static com.android.imsserviceentitlement.ImsServiceEntitlementStatsLog.IMS_SERVICE_ENTITLEMENT_UPDATED__PURPOSE__UNKNOWN_PURPOSE;
import static com.android.imsserviceentitlement.ImsServiceEntitlementStatsLog.IMS_SERVICE_ENTITLEMENT_UPDATED__SERVICE_TYPE__VOWIFI;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;

import com.android.imsserviceentitlement.entitlement.EntitlementResult;
import com.android.imsserviceentitlement.job.JobManager;
import com.android.imsserviceentitlement.utils.ImsUtils;
import com.android.imsserviceentitlement.utils.TelephonyUtils;

/**
 * The {@link JobService} for querying entitlement status in the background.
 * The jobId is unique for different subId + job combination, so can run the same job for different
 * subIds w/o cancelling each others. See {@link JobManager}.
 */
public class ImsEntitlementPollingService extends JobService {
    private static final String TAG = "IMSSE-ImsEntitlementPollingService";

    public static final ComponentName COMPONENT_NAME =
            ComponentName.unflattenFromString(
                    "com.android.imsserviceentitlement/ImsEntitlementPollingService");

    private ImsEntitlementApi mImsEntitlementApi;

    /**
     * Cache job id associated {@link EntitlementPollingTask} objects for canceling once job be
     * canceled.
     */
    private final SparseArray<EntitlementPollingTask> mTasks = new SparseArray<>();

    @VisibleForTesting EntitlementPollingTask mOngoingTask;

    @Override
    @VisibleForTesting
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @VisibleForTesting
    void injectImsEntitlementApi(ImsEntitlementApi imsEntitlementApi) {
        this.mImsEntitlementApi = imsEntitlementApi;
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        PersistableBundle bundle = params.getExtras();
        int subId =
                bundle.getInt(
                        SubscriptionManager.EXTRA_SUBSCRIPTION_INDEX,
                        SubscriptionManager.INVALID_SUBSCRIPTION_ID);

        int jobId = params.getJobId();
        Log.d(TAG, "onStartJob: " + jobId);

        // Ignore the job if the SIM be removed or swapped
        if (!JobManager.isValidJob(this, params)) {
            Log.d(TAG, "Stop for invalid job! " + jobId);
            return false;
        }

        // if the same job ID is scheduled again, the current one will be cancelled by platform and
        // #onStopJob will be called to removed the job.
        mOngoingTask = new EntitlementPollingTask(params, subId);
        mTasks.put(jobId, mOngoingTask);
        mOngoingTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(final JobParameters params) {
        int jobId = params.getJobId();
        Log.d(TAG, "onStopJob: " + jobId);
        EntitlementPollingTask task = mTasks.get(jobId);
        if (task != null) {
            task.cancel(true);
            mTasks.remove(jobId);
        }

        return true;
    }

    @VisibleForTesting
    class EntitlementPollingTask extends AsyncTask<Void, Void, Void> {
        private final JobParameters mParams;
        private final ImsEntitlementApi mImsEntitlementApi;
        private final ImsUtils mImsUtils;
        private final TelephonyUtils mTelephonyUtils;

        // States for metrics
        private long mStartTime;
        private long mDurationMillis;
        private int mPurpose = IMS_SERVICE_ENTITLEMENT_UPDATED__PURPOSE__UNKNOWN_PURPOSE;
        private int mAppResult = IMS_SERVICE_ENTITLEMENT_UPDATED__APP_RESULT__UNKNOWN_RESULT;

        EntitlementPollingTask(final JobParameters params, int subId) {
            this.mParams = params;
            this.mImsUtils = ImsUtils.getInstance(ImsEntitlementPollingService.this, subId);
            this.mTelephonyUtils = new TelephonyUtils(ImsEntitlementPollingService.this, subId);
            this.mImsEntitlementApi = ImsEntitlementPollingService.this.mImsEntitlementApi != null
                    ? ImsEntitlementPollingService.this.mImsEntitlementApi
                    : new ImsEntitlementApi(ImsEntitlementPollingService.this, subId);
        }

        @Override
        protected Void doInBackground(Void... unused) {
            mStartTime = mTelephonyUtils.getUptimeMillis();
            int jobId = JobManager.getPureJobId(mParams.getJobId());
            switch (jobId) {
                case JobManager.QUERY_ENTITLEMEN_STATUS_JOB_ID:
                    mPurpose = IMS_SERVICE_ENTITLEMENT_UPDATED__PURPOSE__POLLING;
                    doWfcEntitlementCheck();
                    break;
                default:
                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            Log.d(TAG, "JobId:" + mParams.getJobId() + "- Task done.");
            sendStatsLogToMetrics();
            ImsEntitlementPollingService.this.jobFinished(mParams, false);
        }

        @Override
        protected void onCancelled(Void unused) {
            sendStatsLogToMetrics();
        }

        @WorkerThread
        private void doWfcEntitlementCheck() {
            if (!mImsUtils.isWfcEnabledByUser()) {
                Log.d(TAG, "WFC not turned on; checkEntitlementStatus not needed this time.");
                return;
            }
            try {
                EntitlementResult result = mImsEntitlementApi.checkEntitlementStatus();
                Log.d(TAG, "Entitlement result: " + result);
                if (shouldTurnOffWfc(result)) {
                    mAppResult = IMS_SERVICE_ENTITLEMENT_UPDATED__APP_RESULT__DISABLED;
                    mImsUtils.disableWfc();
                } else {
                    mAppResult = IMS_SERVICE_ENTITLEMENT_UPDATED__APP_RESULT__ENABLED;
                }
            } catch (RuntimeException e) {
                mAppResult = IMS_SERVICE_ENTITLEMENT_UPDATED__APP_RESULT__FAILED;
                Log.d(TAG, "checkEntitlementStatus failed.", e);
            }
        }

        /**
         * Returns {@code true} when {@code EntitlementResult} says WFC is not activated;
         * Otherwise {@code false} if {@code EntitlementResult} is not of any known pattern.
         */
        private boolean shouldTurnOffWfc(@Nullable EntitlementResult result) {
            if (result == null) {
                Log.d(TAG, "Entitlement API failed to return a result; don't turn off WFC.");
                return false;
            }

            // Only turn off WFC for known patterns indicating WFC not activated.
            return result.getVowifiStatus().serverDataMissing()
                    || result.getVowifiStatus().inProgress()
                    || result.getVowifiStatus().incompatible();
        }

        private void sendStatsLogToMetrics() {
            mDurationMillis = mTelephonyUtils.getUptimeMillis() - mStartTime;

            // If no result set, it was cancelled for reasons.
            if (mAppResult == IMS_SERVICE_ENTITLEMENT_UPDATED__APP_RESULT__UNKNOWN_RESULT) {
                mAppResult = IMS_SERVICE_ENTITLEMENT_UPDATED__APP_RESULT__CANCELED;
            }
            ImsServiceEntitlementStatsLog.write(
                    IMS_SERVICE_ENTITLEMENT_UPDATED,
                    mTelephonyUtils.getCarrierId(),
                    mTelephonyUtils.getSpecificCarrierId(),
                    mPurpose,
                    IMS_SERVICE_ENTITLEMENT_UPDATED__SERVICE_TYPE__VOWIFI,
                    mAppResult,
                    mDurationMillis);
        }
    }
}
