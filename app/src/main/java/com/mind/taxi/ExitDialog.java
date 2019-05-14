package com.mind.taxi;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.mind.taxi.R;


public class ExitDialog extends Dialog {
    Context con;
    public ExitDialog(@NonNull Context context) {
        super(context);
        con=context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_exit);
//        setCustomActionbar();


    }
}
