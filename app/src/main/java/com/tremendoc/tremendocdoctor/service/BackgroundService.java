package com.tremendoc.tremendocdoctor.service;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.tremendoc.tremendocdoctor.activity.BaseActivity;

public class BackgroundService extends Worker  {
    public BackgroundService(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        return Result.success();
    }
}
