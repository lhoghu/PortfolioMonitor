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

        updateSymbol.overrideHintText(currencyUserInput, positionUserInput);

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                // Get user specified data from the dialog window
                                String currency = currencyUserInput.getText().toString();
                                String positionStr = positionUserInput.getText().toString();

                                int position = 0;
                                if (!positionStr.isEmpty())
                                    position = Long.valueOf(positionStr).intValue();
                                long success = updateSymbol.update(currency, position);

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
