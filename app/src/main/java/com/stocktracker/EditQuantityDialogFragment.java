package com.stocktracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.stocktracker.data.Quote;
import com.stocktracker.util.Utils;

import static com.stocktracker.BuildConfig.DEBUG;

/**
 * Created by dlarson on 9/7/15.
 */
public class EditQuantityDialogFragment extends DialogFragment {
    public final static String TAG = EditQuantityDialogFragment.class.getSimpleName();
    public final static String QUOTE_ARG = "quote";

    private EditStockListener mCallback;

    private TextView mTickerSymbolTextView;
    private EditText mQuantityEditText;
    private Quote mQuote;
    private TextView mStockNameTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (DEBUG) Log.d(TAG, "onCreate");
        final Bundle args = getArguments();
        mQuote = args.getParcelable(QUOTE_ARG);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        final View view = inflater.inflate(R.layout.edit_stock_quantity, null);
        mTickerSymbolTextView = (TextView) view.findViewById(R.id.stockTicker);
        mStockNameTextView = (TextView)view.findViewById(R.id.stockName);
        mQuantityEditText = (EditText) view.findViewById(R.id.stockQuantity);

        mTickerSymbolTextView.setText(mQuote.getSymbol());

        if (mQuote != null) {
            mStockNameTextView.setText(mQuote.getName());
            mTickerSymbolTextView.setText(mQuote.getSymbol());
            mQuantityEditText.setText(String.valueOf(mQuote.getQuantity()));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setTitle(R.string.edit_quantity);
        builder.setPositiveButton(R.string.ok, null);
        builder.setNegativeButton(R.string.cancel, null);

        final AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        editStock();
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

    private void editStock() {
        final String quantityStr = mQuantityEditText.getText().toString();

        if (DEBUG) Log.d(TAG, "New quantity for stock " + mQuote.getSymbol() + " = " + quantityStr);

        if (!Utils.isValidQuantity(quantityStr)) {
            Toast.makeText(this.getActivity(), "Invalid quantity (must be greater than 0)", Toast.LENGTH_SHORT).show();
        } else {
            mCallback.updateStockQuantity(mQuote.getSymbol(), Double.parseDouble(quantityStr), mQuote.getId());
            dismiss();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (EditStockListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement EditStockListener");
        }
    }

    interface EditStockListener {
        void updateStockQuantity(String symbol, double quantity, long id);
    }
}