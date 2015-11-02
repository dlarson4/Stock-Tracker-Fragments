package com.stocktracker;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

import com.stocktracker.util.FormatUtils;

import java.math.BigDecimal;

import static com.stocktracker.BuildConfig.DEBUG;

/**
 * Created by dlarson on 11/1/15.
 */
public class MarketTotalUiUpdater {
    private static final String TAG = MarketTotalUiUpdater.class.getSimpleName();

    private Activity mActivity;
    private BigDecimal mMarketValue;

    public MarketTotalUiUpdater(Activity activity, BigDecimal marketValue) {
        mActivity = activity;
        mMarketValue = marketValue;
    }

    public void update() {
        final TextView totalMarketValueView = (TextView) mActivity.findViewById(R.id.totalMarketValue);
        if (totalMarketValueView != null) {
            totalMarketValueView.setText(FormatUtils.formatMarketValue(mMarketValue));
        }
    }
}
