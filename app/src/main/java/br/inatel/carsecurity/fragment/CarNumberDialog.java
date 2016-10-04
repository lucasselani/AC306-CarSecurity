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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import br.inatel.carsecurity.R;
import br.inatel.carsecurity.provider.NumberManagement;

/**
 * Created by Lucas on 03/10/2016.
 */

public class CarNumberDialog extends DialogFragment {
    private int[] mEditTextsIds = {R.id.nacional_code, R.id.ddd, R.id.number};
    private EditText[] mEditTexts = new EditText[mEditTextsIds.length];

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

        for (int i=0; i<mEditTextsIds.length; i++)
            mEditTexts[i] = (EditText) modifyView.findViewById(mEditTextsIds[i]);

        builder.setPositiveButton("SALVAR", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) { }
        });

        builder.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast.makeText(getContext(), "Alterações descartadas com sucesso",
                        Toast.LENGTH_LONG).show();
                dismiss();
            }
        });

        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog alertDialog = (AlertDialog) getDialog();
        if(alertDialog != null){
            Button positiveButton = alertDialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Boolean wantToCloseDialog = true;
                    String number = "";

                    for (EditText et : mEditTexts) {
                        if (et.getText().toString().equals("")) {
                            et.setHint("Preencha este campo!");
                            et.setHintTextColor(Color.RED);
                            wantToCloseDialog = false;
                        } else {
                            et.setHintTextColor(Color.GRAY);
                            number += et.getText().toString();
                        }
                    }

                    if (wantToCloseDialog) {
                        NumberManagement mNumberManagement = new NumberManagement(getContext());
                        mNumberManagement.setCarNumber(number);
                        Toast.makeText(getContext(), "Número " + number + " salvo com sucesso",
                                Toast.LENGTH_LONG).show();
                        dismiss();
                    }
                }
            });
        }
    }
}
