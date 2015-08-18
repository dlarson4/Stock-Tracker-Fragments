package com.stocktracker;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.stocktracker.data.QuoteResponse;
import com.stocktracker.db.StockContentProviderFacade;
import com.stocktracker.log.Logger;
import com.stocktracker.util.Utils;

public class AddStockFragment extends Fragment
{
    private static final Object CLASS_NAME = AddStockFragment.class.getSimpleName();
    
    public static final String TAG = AddStockFragment.class.getCanonicalName();
    
    private AddStockListener mCallback;
    
    private StockContentProviderFacade dao;
    private Handler downloadHandler = null;
    
    static interface AddStockListener
    {
        void saveNewStock(QuoteResponse quoteResponse, double quantity);
        void cancelAddNewStock();
    }
    
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        try
        {
            mCallback = (AddStockListener)activity;
        }
        catch(ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " must implement AddStockListener");
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        if(Logger.isLoggingEnabled())
        {
            Logger.debug("%s.%s:", CLASS_NAME, "onCreate");
        }
        
        downloadHandler = new StockDownloadHandler(this);
        dao = new StockContentProviderFacade(this.getActivity());
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.add_stock_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        Button saveButton = (Button)getView().findViewById(R.id.save);
        Button cancelButton = (Button)getView().findViewById(R.id.cancel);
        
        saveButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                addStock();
            }
        });
        
        cancelButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mCallback.cancelAddNewStock();
            }
        });
    }    
    
    private void addStock()
    {
        EditText symbolEditText = (EditText)getView().findViewById(R.id.edit_message);
        EditText quantityEditText = (EditText)getView().findViewById(R.id.quantity_message);

        String stockSymbol = symbolEditText.getText().toString();
        String quantityStr = quantityEditText.getText().toString();

        if(Logger.isLoggingEnabled())
        {
            Logger.debug("%s.%s: Stock symbol value entered = '%s', quantity = '%s'.", CLASS_NAME, "addStock", stockSymbol, quantityStr);
        }

        if(!(isValidSymbol(stockSymbol)))
        {
            Toast toast = Toast.makeText(this.getActivity(), "No stock symbol entered", Toast.LENGTH_SHORT);
            toast.show();
        }
        else if(!Utils.isValidQuantity(quantityStr))
        {
            Toast toast = Toast.makeText(this.getActivity(), "Invalid quantity (must be greater than 0)", Toast.LENGTH_SHORT);
            toast.show();
        }
        else
        {
            boolean duplicate = dao.isDuplicate(stockSymbol); // TODO should be made asynchronous

            if(Logger.isLoggingEnabled())
            {
                Logger.debug("%s.%s: Stock '%s' is duplicate? '%b'", CLASS_NAME, "saveNewStock", stockSymbol, duplicate);
            }

            if(duplicate)
            {
                Toast toast = Toast.makeText(this.getActivity(), getString(R.string.stock_alredy_exists, stockSymbol), Toast.LENGTH_SHORT);
                toast.show();
            }
            else
            {
                final String url = UrlBuilder.buildQuoteUrl(stockSymbol);

                if(Logger.isLoggingEnabled())
                {
                    Logger.debug("%s.%s: UrlBuilder.buildQuoteUrl() returned '%s'.", CLASS_NAME, "saveNewStock", url);
                }

                Bundle extras = new Bundle();
                extras.putString("TICKER", stockSymbol);
                extras.putDouble("QUANTITY", Double.parseDouble(quantityStr));
                Intent intent = DownloadIntentService.createIntent(this.getActivity(), Uri.parse(url), downloadHandler, extras, 0);
                
                if(Logger.isLoggingEnabled())
                {
                    Logger.debug("%s.%s: Starting download intent service.", CLASS_NAME, "saveNewStock");
                }
                
                this.getActivity().startService(intent);
            }
        }
    }

    private boolean isValidSymbol(String symbol)
    {
        return symbol != null && symbol.trim().length() > 0;
    }
    
    /**
     * A constructor that gets a weak reference to the enclosing class. We do this to avoid memory leaks during Java
     * Garbage Collection.
     * 
     * @see https://groups.google.com/forum/#!msg/android-developers/1aPZXZG6kWk/lIYDavGYn5UJ
     */
    private static class StockDownloadHandler extends Handler
    {
        // Allows Fragment to be garbage collected properly
        private WeakReference<AddStockFragment> mFragment;

        public StockDownloadHandler(AddStockFragment activity)
        {
            mFragment = new WeakReference<AddStockFragment>(activity);
        }

        @Override
        public void handleMessage(Message message)
        {
            AddStockFragment fragment = mFragment.get();
            
            // check if the AddStockFragment is gone
            if(fragment == null)
            {
                return;
            }

            QuoteResponse quoteResponse = DownloadIntentService.getQuoteResponse(message);
            Bundle extras = DownloadIntentService.getExtras(message);
            
            String ticker = extras.getString("TICKER");
            double quantity = extras.getDouble("QUANTITY");

            fragment.addStockDone(quoteResponse, ticker, quantity);
        }
    }

    public void addStockDone(QuoteResponse quoteResponse, String ticker, double quantity)
    {
        if(Utils.isValidStock(quoteResponse))
        {
            if(Logger.isLoggingEnabled())
            {
                Logger.debug("%s.%s: New stock '%s' verified.", CLASS_NAME, "addStockDone", ticker);
            }
            
            mCallback.saveNewStock(quoteResponse, quantity);
        }
        else
        {
            Logger.warn("%s.%s: User attempted to add invalid stock '%s'", CLASS_NAME, "addStockDone", ticker);

            Toast toast = Toast.makeText(getActivity(), getString(R.string.unrecognized_stock, ticker), Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
