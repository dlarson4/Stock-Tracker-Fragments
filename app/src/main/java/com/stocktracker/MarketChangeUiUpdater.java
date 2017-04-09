package com.stocktracker;

import android.app.Activity;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.TextView;

import com.stocktracker.util.FormatUtils;

import java.math.BigDecimal;

import static com.stocktracker.BuildConfig.DEBUG;

/**
 * Created by dlarson on 11/1/15.
 */
public class MarketChangeUiUpdater {
    private static final String TAG = MarketChangeUiUpdater.class.getSimpleName();

    private Activity activity;
    private BigDecimal todaysValue;
    private BigDecimal previousValue;

    public MarketChangeUiUpdater(Activity activity, BigDecimal todaysValue, BigDecimal previousValue) {
        this.activity = activity;
        this.todaysValue = todaysValue;
        this.previousValue = previousValue;
    }

    public void update() {
        if (todaysValue != null && previousValue != null) {
            final BigDecimal todaysChange = todaysValue.subtract(previousValue);
            final FormatUtils.ChangeType changeType = FormatUtils.getChangeType(todaysChange.doubleValue());

            final TextView marketValueChangeView = (TextView) activity.findViewById(R.id.marketValueChange);
            final TextView totalMarketChangePercentView = (TextView) activity.findViewById(R.id.totalMarketChangePercent);

            int changeColor = getChangeColor(changeType);

            // change amount
            final String todaysChangeFormatted = FormatUtils.formatMarketValue(todaysChange);
            marketValueChangeView.setText(todaysChangeFormatted);
            marketValueChangeView.setTextColor(changeColor);

            // change as a percent
            String changeSymbol = FormatUtils.getChangeSymbol(activity, changeType);

            if (DEBUG) Log.d(TAG, "previousValue = " + previousValue);

            if (previousValue != null && previousValue.intValue() != 0) {
                BigDecimal todaysChangePercent = todaysChange.divide(previousValue, 3);
                String todaysChangePercentFormatted = changeSymbol + FormatUtils.formatPercent(Math.abs(todaysChangePercent.doubleValue()));

                totalMarketChangePercentView.setText(todaysChangePercentFormatted);
                totalMarketChangePercentView.setTextColor(changeColor);
            }
        }
    }

    private int getChangeColor(FormatUtils.ChangeType changeType) {
        int changeColor;
        if (changeType == FormatUtils.ChangeType.Negative) {
            changeColor = ActivityCompat.getColor(activity, R.color.red);
        } else {
            changeColor = ActivityCompat.getColor(activity, R.color.green);
        }
        return changeColor;
    }

}
