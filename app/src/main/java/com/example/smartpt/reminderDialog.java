package com.example.smartpt;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatDialogFragment;

public class reminderDialog extends AppCompatDialogFragment {
    private RadioGroup eReminder;
    private DialogListener listener;

    @Override
    public Dialog onCreateDialog( Bundle savedInstanceState) {


        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        LayoutInflater inflater=getActivity().getLayoutInflater();
        View view= inflater.inflate(R.layout.reminder_dialog, null);
        eReminder=view.findViewById(R.id.eReminderRadioGroup);

        builder.setView(view)
                .setTitle("Reminder")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {

                    }
                }).setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                final String reminder = ((RadioButton)view.findViewById(eReminder.getCheckedRadioButtonId()))
                        .getText().toString();
                listener.applyReminderText(reminder);


            }
        });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener=(DialogListener) context;
        } catch (Exception e){
            throw new ClassCastException(context.toString());
        }
    }

    public interface DialogListener{
        void  applyReminderText(String gender);
    }
}

