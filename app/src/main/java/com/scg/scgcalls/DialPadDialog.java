package com.scg.scgcalls;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.twilio.voice.Call;

public class DialPadDialog extends DialogFragment {
    private Call activeCall;

    public DialPadDialog(Call call) {
        this.activeCall = call;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_dial_pad, null);

        builder.setView(view);

        int[] buttons = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
                R.id.btnStar, R.id.btnHash
        };

        for (int id : buttons) {
            Button b = view.findViewById(id);
            b.setOnClickListener(v -> {
                String digit = ((Button)v).getText().toString();
                activeCall.sendDigits(digit);
            });
        }

        return builder.create();
    }
}
