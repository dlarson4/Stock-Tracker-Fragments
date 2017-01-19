package com.stocktracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.stocktracker.data.QuoteResponse;
import com.stocktracker.db.StockContentProviderFacade;
import com.stocktracker.util.UrlBuilder;
import com.stocktracker.util.Utils;

import java.lang.ref.WeakReference;

import static com.stocktracker.BuildConfig.DEBUG;

/**
 * Created by dlarson on 9/7/15.
 */
public class AddStockDialogFragment extends DialogFragment {
    public final static String TAG = AddStockDialogFragment.class.getSimpleName();

    private AddStockDialogListener mCallback;
    private Handler downloadHandler = null;

    private StockContentProviderFacade mDao;
    private EditText mSymbolEditText;
    private EditText mQuantityEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (DEBUG) Log.d(TAG, "onCreate");

        downloadHandler = new StockDownloadHandler(this);
        mDao = new StockContentProviderFacade(this.getActivity());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        final View view = inflater.inflate(R.layout.add_stock, null);
        mSymbolEditText = (EditText) view.findViewById(R.id.stockTicker);
        mQuantityEditText = (EditText) view.findViewById(R.id.stockQuantity);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setMessage(R.string.add_stock);
        builder.setPositiveButton(R.string.save, null);
        builder.setNegativeButton(R.string.cancel, null);

        final AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        addStock();
                    }
                });

                final Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                negativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });
            }
        });

        return alertDialog;
    }

    private void addStock() {
        String stockSymbol = mSymbolEditText.getText().toString();
        String quantityStr = mQuantityEditText.getText().toString();

        if (DEBUG) Log.d(TAG, "Stock symbol value entered = " + stockSymbol + ", quantity = " + quantityStr);

        if (!(isValidSymbol(stockSymbol))) {
            Toast.makeText(this.getActivity(), "No stock symbol entered", Toast.LENGTH_SHORT).show();
        } else if (!Utils.isValidQuantity(quantityStr)) {
            Toast.makeText(this.getActivity(), "Invalid quantity (must be greater than 0)", Toast.LENGTH_SHORT).show();
        } else {
            final boolean duplicate = mDao.isDuplicate(stockSymbol); // TODO should be made asynchronous

            if (DEBUG) Log.d(TAG, "Stock " + stockSymbol + " is duplicate? " + duplicate);

            if (duplicate) {
                Toast.makeText(this.getActivity(), getString(R.string.stock_alredy_exists, stockSymbol), Toast.LENGTH_SHORT).show();
            } else {
                final String url = UrlBuilder.buildQuoteUrl(stockSymbol);

                if (DEBUG) Log.d(TAG, "UrlBuilder.buildQuoteUrl() returned " + url);

                Bundle extras = new Bundle();
                extras.putString("TICKER", stockSymbol);
                extras.putDouble("QUANTITY", Double.parseDouble(quantityStr));
                Intent intent = DownloadIntentService.createIntent(this.getActivity(), Uri.parse(url), downloadHandler, extras, 0);

                if (DEBUG) Log.d(TAG, "Starting download intent service.");

                getActivity().startService(intent);
            }
        }
    }
    /**
     * A constructor that gets a weak reference to the enclosing class. We do this to avoid memory leaks during Java
     * Garbage Collection.
     * <p/>
     * groups.google.com/forum/#!msg/android-developers/1aPZXZG6kWk/lIYDavGYn5UJ
     */
    private static class StockDownloadHandler extends Handler {
        // Allows Fragment to be garbage collected properly
        private final WeakReference<AddStockDialogFragment> mFragment;

        public StockDownloadHandler(AddStockDialogFragment activity) {
            mFragment = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            AddStockDialogFragment fragment = mFragment.get();

            // check if the AddStockFragment is gone
            if (fragment == null) {
                return;
            }

            QuoteResponse quoteResponse = DownloadIntentService.getQuoteResponse(message);
            Bundle extras = DownloadIntentService.getExtras(message);

            String ticker = extras.getString("TICKER");
            double quantity = extras.getDouble("QUANTITY");

            fragment.addStockDone(quoteResponse, ticker, quantity);
        }
    }

    private void addStockDone(QuoteResponse quoteResponse, String ticker, double quantity) {
        if (Utils.isValidStock(quoteResponse)) {
            if (DEBUG) Log.d(TAG, "New stock " + ticker + " verified.");
            mCallback.saveNewStock(quoteResponse, quantity);
        } else {
            if (DEBUG) Log.d(TAG, "User attempted to add invalid stock " + ticker);
            Toast.makeText(getActivity(), getString(R.string.unrecognized_stock, ticker), Toast.LENGTH_SHORT).show();
        }
        AddStockDialogFragment.this.dismiss();
    }

    public interface AddStockDialogListener {
        void saveNewStock(QuoteResponse quoteResponse, double quantity);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (AddStockDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement AddStockDialogListener");
        }
    }

    private boolean isValidSymbol(String symbol) {
        return symbol != null && symbol.trim().length() > 0;
    }
}