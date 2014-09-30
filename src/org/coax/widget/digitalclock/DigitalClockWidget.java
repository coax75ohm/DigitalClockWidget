package org.coax.widget.digitalclock;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.text.format.DateFormat;
import android.widget.RemoteViews;
import java.util.Calendar;

public class DigitalClockWidget extends AppWidgetProvider {
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

        context.startService(new Intent(UpdateTimeService.UPDATE_TIME));
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);

        context.stopService(new Intent(context, UpdateTimeService.class));
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        context.startService(new Intent(UpdateTimeService.UPDATE_TIME));
    }

    public static final class UpdateTimeService extends Service {
        static final String UPDATE_TIME = "org.coax.widget.digitalclock.action.UPDATE_TIME";

        private Calendar mCalendar;
        private final static IntentFilter mIntentFilter = new IntentFilter();

        static {
            mIntentFilter.addAction(Intent.ACTION_TIME_TICK);
            mIntentFilter.addAction(Intent.ACTION_TIME_CHANGED);
            mIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        }

        @Override
        public void onCreate() {
            super.onCreate();

            mCalendar = Calendar.getInstance();
            registerReceiver(mTimeChangedReceiver, mIntentFilter);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();

            unregisterReceiver(mTimeChangedReceiver);
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            super.onStartCommand(intent, flags, startId);

            if (UPDATE_TIME.equals(intent.getAction())) {
                updateTime();
            }

            return START_REDELIVER_INTENT;
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        private final BroadcastReceiver mTimeChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateTime();
            }
        };

        private void updateTime() {
            mCalendar.setTimeInMillis(System.currentTimeMillis());

            RemoteViews mRemoteViews = new RemoteViews(getPackageName(), R.layout.widget);
            mRemoteViews.setTextViewText(R.id.Time,
                                         DateFormat.format(getString(R.string.time_format), mCalendar));
            mRemoteViews.setTextViewText(R.id.Date,
                                         DateFormat.format(getString(R.string.date_format), mCalendar));

            ComponentName mComponentName = new ComponentName(this, DigitalClockWidget.class);
            AppWidgetManager mAppWidgetManager = AppWidgetManager.getInstance(this);
            mAppWidgetManager.updateAppWidget(mComponentName, mRemoteViews);
        }
    }
}
