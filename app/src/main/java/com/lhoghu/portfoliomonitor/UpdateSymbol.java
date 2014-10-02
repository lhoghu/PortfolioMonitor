package com.lhoghu.portfoliomonitor;

import android.widget.EditText;

/**
 * Helper class for TradeInputDialog
 *
 * Subclass this so that the dialog can be used to either update or add new symbols to the db
 */
abstract public class UpdateSymbol {

    /**
     * This method should be derived to set the currency and position on a trade in the db
     *
     * @param currency The currency to set on the trade
     * @param position The position to set on the trade
     * @param boughtAt The amount paid for each unit to set on the trade
     * @return -1 if the db update is unsuccessful, otherwise the id of the row updated in the db
     */
    public abstract long update(String currency, int position, Double boughtAt);

    /**
     * Method to override the hint values in the TradeInputDialog if values already exist
     */
    public void overrideHintText(
            EditText currencyView,
            EditText positionView,
            EditText boughtAtView)
    {}

    /**
     * Method child classes can implement to execute actions after db updates
     */
    public void onCompleted(long success) {}
}