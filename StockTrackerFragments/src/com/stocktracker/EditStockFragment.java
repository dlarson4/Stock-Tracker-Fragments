package com.stocktracker;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.stocktracker.data.Quote;
import com.stocktracker.log.Logger;
import com.stocktracker.util.Utils;

public class EditStockFragment extends Fragment
{
    private static final Object CLASS_NAME = EditStockFragment.class.getSimpleName();
    private static final String QUOTE_KEY = "quote";
    public static final String TAG = EditStockFragment.class.getCanonicalName();
    
    private Quote quote;
    private EditStockListener mCallback;
    
    public EditStockFragment()
    {
    }
    
    public EditStockFragment(Quote quote)
    {
        super();
        this.quote = quote;
    }

    static interface EditStockListener
    {
        void updateStockQuantity(String symbol, double quantity, long id);
        void cancelUpdateStock();
    }
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        
        if(this.quote != null)
        {
            if(Logger.isDebugEnabled())
            {
                Logger.debug("%s.%s: Saving stock to edit", CLASS_NAME, "onSaveInstanceState");
            }
            outState.putParcelable(QUOTE_KEY, this.quote);
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if(savedInstanceState != null)
        {
            this.quote = (Quote)savedInstanceState.getParcelable(QUOTE_KEY);
            if(Logger.isDebugEnabled())
            {
                Logger.debug("%s.%s: Restored quote = '%s'", CLASS_NAME, "onCreateView", quote);
            }
        }
        return inflater.inflate(R.layout.edit_stock_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        
        TextView stockNameTextView = (TextView)getView().findViewById(R.id.edit_fragment_stock_name);
        TextView symbolTextView = (TextView)getView().findViewById(R.id.edit_fragment_stock_symbol);
        EditText quantityEditText = (EditText)getView().findViewById(R.id.edit_fragment_quantity);
        
        if(quote != null)
        {
            stockNameTextView.setText(quote.getName());
            symbolTextView.setText(quote.getSymbol());
            quantityEditText.setText( String.valueOf(quote.getQuantity()) );
        }
        
        Button saveButtom = (Button)getView().findViewById(R.id.save);
        saveButtom.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                editStock();
            }
        });
        
        Button cancelButtom = (Button)getView().findViewById(R.id.cancel);
        cancelButtom.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mCallback.cancelUpdateStock();
            }
        });
    }

    private void editStock()
    {
        EditText quantityEditText = (EditText) getView().findViewById(R.id.edit_fragment_quantity);

        String quantityStr = quantityEditText.getText().toString();

        if(Logger.isDebugEnabled())
        {
            Logger.debug("%s.%s: New quantity for stock '%s' = '%s'.", CLASS_NAME, "addStock", quote.getSymbol(), quantityStr);
        }

        if(!Utils.isValidQuantity(quantityStr))
        {
            Toast toast = Toast.makeText(this.getActivity(), "Invalid quantity (must be greater than 0)", Toast.LENGTH_SHORT);
            toast.show();
        }
        else
        {
            mCallback.updateStockQuantity(quote.getSymbol(), Double.parseDouble(quantityStr), quote.getId());
        }
    }
    
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        try
        {
            mCallback = (EditStockListener)activity;
        }
        catch(ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " must implement EditStockListener");
        }
    }
}
