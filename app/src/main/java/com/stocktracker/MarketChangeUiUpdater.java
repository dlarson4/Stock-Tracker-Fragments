package com.stocktracker;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.stocktracker.util.FormatUtils;

import java.math.BigDecimal;

import static com.stocktracker.BuildConfig.DEBUG;

/**
 * Created by dlarson on 11/1/15.
 */
public class MarketChangeUiUpdater {
    private static final String TAG = MarketChangeUiUpdater.class.getSimpleName();

    private Activity mActivity;
    private BigDecimal mTodaysValue;
    private BigDecimal mPreviousValue;

    public MarketChangeUiUpdater(Activity activity, BigDecimal todaysValue, BigDecimal previousValue) {
        mActivity = activity;
        this.mTodaysValue = todaysValue;
        this.mPreviousValue = previousValue;
    }

    public void update() {
        if (mTodaysValue != null && mPreviousValue != null) {
            final BigDecimal todaysChange = mTodaysValue.subtract(mPreviousValue);
            final FormatUtils.ChangeType changeType = FormatUtils.getChangeType(todaysChange.doubleValue());

            final TextView marketValueChangeView = (TextView) mActivity.findViewById(R.id.marketValueChange);
            final TextView totalMarketChangePercentView = (TextView)mActivity.findViewById(R.id.totalMarketChangePercent);

            LayoutInflater layoutInflater = LayoutInflater.from(mActivity);
            int changeColor = layoutInflater.getContext().getResources().getColor(R.color.green);
            if (changeType == FormatUtils.ChangeType.Negative) {
                changeColor = layoutInflater.getContext().getResources().getColor(R.color.red);
            }

            // change amount
            final String todaysChangeFormatted = FormatUtils.formatMarketValue(todaysChange);
            marketValueChangeView.setText(todaysChangeFormatted);
            marketValueChangeView.setTextColor(changeColor);

            // change as a percent
            String changeSymbol = FormatUtils.getChangeSymbol(layoutInflater.getContext(), changeType);

            if (DEBUG) Log.d(TAG, "mPreviousValue = " + mPreviousValue);

            if (mPreviousValue != null && mPreviousValue.intValue() != 0) {
                BigDecimal todaysChangePercent = todaysChange.divide(mPreviousValue, 3);
                String todaysChangePercentFormatted = changeSymbol + FormatUtils.formatPercent(Math.abs(todaysChangePercent.doubleValue()));

                totalMarketChangePercentView.setText(todaysChangePercentFormatted);
                totalMarketChangePercentView.setTextColor(changeColor);
            }
        }
    }

}
