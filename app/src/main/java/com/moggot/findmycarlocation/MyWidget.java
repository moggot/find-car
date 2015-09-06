package com.moggot.findmycarlocation;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

public class MyWidget extends AppWidgetProvider {

    final static String ACTION_CHANGE = "com.moggot.widgetbutton.change_count";
    final static String LOG_TAG = "myLogs";
    public final static String WIDGET_IS_CAR_PARKED = "is_car_parked";
    public final static String WIDGET_PREF = "widget_pref";

    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        // обновляем все экземпляры
        for (int i : appWidgetIds) {
            updateWidget(context, appWidgetManager, i);
        }
    }

    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        // Удаляем Preferences
        Editor editor = context.getSharedPreferences(
                WIDGET_PREF, Context.MODE_PRIVATE).edit();
        for (int widgetID : appWidgetIds) {
            editor.remove(WIDGET_IS_CAR_PARKED + widgetID);
        }
        editor.commit();
    }

    static void updateWidget(Context ctx, AppWidgetManager appWidgetManager,
                             int widgetID) {
        SharedPreferences sp = ctx.getSharedPreferences(
                WIDGET_PREF, Context.MODE_PRIVATE);

        Log.d(LOG_TAG, "widgetID_update_MyWidget = " + widgetID);
        // Читаем счетчик
        boolean isCarParked = sp.getBoolean(SharedPreference.s_state_location_save, false);

        // Помещаем данные в текстовые поля
        RemoteViews widgetView = new RemoteViews(ctx.getPackageName(),
                R.layout.widget);
        Log.d(LOG_TAG, "isCarParked1_MyWidget = " + isCarParked);
        // Конфигурационный экран (первая зона)
        if (isCarParked)
            widgetView.setImageViewResource(R.id.widget_save_location, R.mipmap.car);
        else
            widgetView.setImageViewResource(R.id.widget_save_location, R.mipmap.man);
        // Счетчик нажатий (третья зона)
        Intent countIntent = new Intent(ctx, MyWidget.class);
        countIntent.setAction(ACTION_CHANGE);
        countIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        PendingIntent pIntent = PendingIntent.getBroadcast(ctx, widgetID, countIntent, 0);
        widgetView.setOnClickPendingIntent(R.id.widget_save_location, pIntent);

        // Обновляем виджет
        appWidgetManager.updateAppWidget(widgetID, widgetView);
    }

    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        // Проверяем, что это intent от нажатия на третью зону
        if (intent.getAction().equalsIgnoreCase(ACTION_CHANGE)) {

            // извлекаем ID экземпляра
            int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
            Bundle extras = intent.getExtras();
            if (extras != null) {
                mAppWidgetId = extras.getInt(
                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID);

            }
            if (mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                // Читаем значение счетчика, увеличиваем на 1 и записываем
                SharedPreferences sp = context.getSharedPreferences(
                        WIDGET_PREF, Context.MODE_PRIVATE);
                boolean isCarParked = sp.getBoolean(SharedPreference.s_state_location_save, false);
                Log.d(LOG_TAG, "isCarParked_onRecieve_MyWidget = " + isCarParked);
                sp.edit().putBoolean(SharedPreference.s_state_location_save,
                        isCarParked).commit();

                // Обновляем виджет
                Log.d(LOG_TAG, "mAppWidgetId_MyWidget = " + mAppWidgetId);
                updateWidget(context, AppWidgetManager.getInstance(context),
                        mAppWidgetId);
            }
        }
    }

}