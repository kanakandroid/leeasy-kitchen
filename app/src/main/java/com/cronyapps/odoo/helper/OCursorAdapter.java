package com.cronyapps.odoo.helper;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.LayoutRes;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class OCursorAdapter extends CursorAdapter {

    private int resourceId = -1;
    private LayoutInflater inflater;
    private Context mContext;
    private OnViewBindListener mOnViewBindListener;

    public OCursorAdapter(Context context, Cursor cursor, @LayoutRes int layout) {
        super(context, cursor, false);
        mContext = context;
        resourceId = layout;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(resourceId, parent, false);
    }

    public int getResourceLayout() {
        return resourceId;
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (mOnViewBindListener != null) {
            mOnViewBindListener.onViewBind(view, cursor);
        }
    }

    public void setOnViewBindListener(OnViewBindListener listener) {
        mOnViewBindListener = listener;
    }

    public interface OnViewBindListener {
        void onViewBind(View view, Cursor cursor);
    }
}
