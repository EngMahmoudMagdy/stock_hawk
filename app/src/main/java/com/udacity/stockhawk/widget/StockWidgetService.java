package com.udacity.stockhawk.widget;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Set;

/**
 * Created by engma on 5/14/2017.
 */

public class StockWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewFactory();
    }

    public class ListRemoteViewFactory implements RemoteViewsFactory {

        private Cursor data = null;

        //Lifecycle start
        @Override
        public void onCreate() {
            //No action needed
        }

        @Override
        public void onDestroy() {
            if (data != null) {
                data.close();
                data = null;
            }

        }
        //Lifecycle end

        @Override
        public void onDataSetChanged() {
            if (data != null) data.close();

            final long identityToken = Binder.clearCallingIdentity();
            Set<String> stockPref = PrefUtils.getStocks(getApplicationContext());
            String[] s=  Contract.Quote.QUOTE_COLUMNS.toArray(new String[stockPref.size()]);
            data = getContentResolver().query(Contract.Quote.URI,
                    s,
                    null,
                    null,
                    Contract.Quote.COLUMN_SYMBOL);
            Binder.restoreCallingIdentity(identityToken);
        }

        @Override
        public int getCount() {
            return data == null ? 0 : data.getCount();
        }

        @SuppressLint("PrivateResource")
        @Override
        public RemoteViews getViewAt(int position) {
            if (position == AdapterView.INVALID_POSITION || data == null
                    || !data.moveToPosition(position)) {
                return null;
            }

            RemoteViews remoteViews = new RemoteViews(getBaseContext().getPackageName(), R.layout.widget_list_item_quote);


            String stockSymbol = data.getString(Contract.Quote.POSITION_SYMBOL);
            Float stockPrice = data.getFloat(Contract.Quote.POSITION_PRICE);
            Float absoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            Float percentageChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

            int backgroundDrawable;

            DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.getDefault());
            DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.getDefault());
            dollarFormatWithPlus.setPositivePrefix("+$");

            DecimalFormat   percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
            percentageFormat.setMaximumFractionDigits(2);
            percentageFormat.setMinimumFractionDigits(2);
            percentageFormat.setPositivePrefix("+");

            if (absoluteChange > 0) {
                backgroundDrawable = R.drawable.percent_change_pill_green;
            } else {
                backgroundDrawable = R.drawable.percent_change_pill_red;
            }

            remoteViews.setTextViewText(R.id.stock_symbol, stockSymbol);
            remoteViews.setTextViewText(R.id.bid_price, dollarFormat.format(stockPrice));

            String change = dollarFormatWithPlus.format(absoluteChange);
            String percentage = percentageFormat.format(percentageChange / 100);

            //remoteViews.setTextViewText(R.id.stock_change, change);

            if (PrefUtils.getDisplayMode(getApplicationContext())
                    .equals(getApplicationContext().getString(R.string.pref_display_mode_absolute_key))) {
                remoteViews.setTextViewText(R.id.stock_change, change);
                remoteViews.setContentDescription(R.id.stock_change, change);
            } else {
                remoteViews.setTextViewText(R.id.stock_change, percentage);
                remoteViews.setContentDescription(R.id.stock_change, percentage);
            }

            remoteViews.setInt(R.id.stock_change, "setBackgroundResource", backgroundDrawable);

            final Intent i = new Intent();
            i.putExtra(Contract.Quote.COLUMN_SYMBOL, stockSymbol);
            i.putExtra(Contract.Quote.COLUMN_HISTORY, data.getString(Contract.Quote.POSITION_HISTORY));
            i.putExtra(Contract.Quote.COLUMN_NAME, data.getString(Contract.Quote.POSITION_NAME));
            i.putExtra(Contract.Quote.COLUMN_PRICE, stockPrice);
            i.putExtra(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE));
            i.putExtra(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, absoluteChange);
            remoteViews.setOnClickFillInIntent(R.id.list_item, i);


            return remoteViews;
        }

        @Override
        public RemoteViews getLoadingView() {
            return new RemoteViews(getPackageName(), R.layout.widget_list_item_quote);
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int i) {
            return data.moveToPosition(i) ? data.getLong(Contract.Quote.POSITION_ID) : i;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
