package com.zzh.assistant.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import com.wang.avi.AVLoadingIndicatorView;
import com.zzh.assistant.R;


public class DialogProcess extends AlertDialog {
    public DialogProcess(Context context) {
        super(context);
    }

    public DialogProcess(Context context, int theme) {
        super(context, theme);
    }

    protected DialogProcess(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_process);
        AVLoadingIndicatorView loadingIndicatorView=(AVLoadingIndicatorView)findViewById(R.id.loadingIndicatorView);
        loadingIndicatorView.smoothToShow();
        this.setCanceledOnTouchOutside(false);
    }
}
