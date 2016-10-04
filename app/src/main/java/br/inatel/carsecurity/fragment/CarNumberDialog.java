package br.inatel.carsecurity.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import br.inatel.carsecurity.R;
import br.inatel.carsecurity.provider.NumberManagement;

/**
 * Created by Lucas on 03/10/2016.
 */

public class CarNumberDialog extends DialogFragment {
    private int[] mEditTexts = {R.id.nacional_code, R.id.ddd, R.id.number};

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface MDialogListener {
        public void onMDialogPositiveClick(String newValue);
    }

    // Use this instance of the interface to deliver action events
    MDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View modifyView = inflater.inflate(R.layout.car_number_layout, null);
        builder.setView(modifyView);

        builder.setPositiveButton("SALVAR", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                boolean ok = true;
                String number = "";
                for (int et : mEditTexts) {
                    EditText editText = (EditText) modifyView.findViewById(et);
                    if (editText.getText().toString().equals("")) {
                        editText.setHint("Preencha este campo!");
                        editText.setHintTextColor(Color.RED);
                        ok = false;
                    } else {
                        editText.setHintTextColor(Color.BLACK);
                        number += editText.getText().toString();
                    }
                }

                if (ok) {
                    NumberManagement mNumberManagement = new NumberManagement(getContext());
                    mNumberManagement.setCarNumber(number);
                }
            }
        });

        builder.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dismiss();
            }
        });

        // Create the AlertDialog object and return it
        return builder.create();
    }
}
