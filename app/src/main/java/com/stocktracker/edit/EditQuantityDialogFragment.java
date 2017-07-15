package com.stocktracker.edit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.stocktracker.R;
import com.stocktracker.data.Stock;
import com.stocktracker.util.Utils;

import static com.stocktracker.BuildConfig.DEBUG;

/**
 * Created by dlarson on 9/7/15.
 */
public class EditQuantityDialogFragment extends DialogFragment {
    public final static String TAG = EditQuantityDialogFragment.class.getSimpleName();
    private final static String STOCK_ARG = "stock";
    private final static String NAME_ARG = "stock_name";

    private EditStockListener stockListenerCallback;

    private EditText quantityEditText;
    private Stock stock;
    private String name;

    public static EditQuantityDialogFragment newInstance(Stock stock, String name) {
        EditQuantityDialogFragment fragment = new EditQuantityDialogFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(STOCK_ARG, stock);
        bundle.putString(NAME_ARG, name);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (DEBUG) Log.d(TAG, "onCreate");
        stock = getArguments().getParcelable(STOCK_ARG);
        name = getArguments().getString(NAME_ARG);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        final View view = inflater.inflate(R.layout.edit_stock_quantity, null);
        TextView tickerSymbolTextView = (TextView) view.findViewById(R.id.stockTicker);
        TextView stockNameTextView = (TextView) view.findViewById(R.id.stockName);
        quantityEditText = (EditText) view.findViewById(R.id.stockQuantity);

        tickerSymbolTextView.setText(stock.getSymbol());

        if (stock != null) {
            stockNameTextView.setText(name);
            tickerSymbolTextView.setText(stock.getSymbol());
            quantityEditText.setText(String.valueOf(stock.getQuantity()));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setTitle(R.string.edit_quantity);
        builder.setPositiveButton(R.string.ok, null);
        builder.setNegativeButton(R.string.cancel, null);

        final AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(dialog -> {
            final Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(view1 -> updateQuantity());

            final Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            negativeButton.setOnClickListener(view1 -> alertDialog.dismiss());
        });

        return alertDialog;
    }

    private void updateQuantity() {
        final String quantityStr = quantityEditText.getText().toString();

        if (DEBUG) Log.d(TAG, "New quantity for stock " + stock.getSymbol() + " = " + quantityStr);

        if (!Utils.isValidQuantity(quantityStr)) {
            Toast.makeText(this.getActivity(), getString(R.string.invalid_quantity), Toast.LENGTH_SHORT).show();
        } else {
            stockListenerCallback.updateStockQuantity(
                    stock.getSymbol(),
                    Double.parseDouble(quantityStr),
                    stock.getId());
            dismiss();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            stockListenerCallback = (EditStockListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement EditStockListener");
        }
    }

    public interface EditStockListener {
        void updateStockQuantity(String symbol, double quantity, long id);
    }
}