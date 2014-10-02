package com.lhoghu.portfoliomonitor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class TradeInputDialog {

    private Context context;
    private UpdateSymbol updateSymbol;

    public TradeInputDialog(Context context, UpdateSymbol updateSymbol) {
        this.context = context;
        this.updateSymbol = updateSymbol;
    }

    public AlertDialog create() {
        LayoutInflater li = LayoutInflater.from(context);
        View dialogView = li.inflate(R.layout.add_symbol_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(dialogView);

        final EditText currencyUserInput = (EditText) dialogView.findViewById(R.id.currency_user_input);
        final EditText positionUserInput = (EditText) dialogView.findViewById(R.id.position_user_input);
        final EditText boughtAtUserInput = (EditText) dialogView.findViewById(R.id.boughtat_user_input);

        updateSymbol.overrideHintText(currencyUserInput, positionUserInput, boughtAtUserInput);

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                // Get user specified data from the dialog window
                                String currency = currencyUserInput.getText().toString();
                                String positionStr = positionUserInput.getText().toString();
                                String boughtAtStr = boughtAtUserInput.getText().toString();

                                int position = 0;
                                if (!positionStr.isEmpty())
                                    position = Long.valueOf(positionStr).intValue();

                                Double boughtAt = null;
                                if (!boughtAtStr.isEmpty())
                                    boughtAt = Double.valueOf(boughtAtStr);

                                long success = updateSymbol.update(currency, position, boughtAt);

                                updateSymbol.onCompleted(success);
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        return alertDialog;
    }
}
