package com.stocktracker;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class ActionBarCallback implements ActionMode.Callback {
    private ActionBarListener listener;
    
    interface ActionBarListener {
        void onDestroyActionMode(ActionMode mode);
        void onActionItemClicked(MenuItem item);
    }

    public ActionBarCallback(ActionBarListener listener)
    {
        this.listener = listener;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        listener.onActionItemClicked(item);
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        listener.onDestroyActionMode(mode);
    }

}
