package com.example.ssa_ezra.smsvoice;

// image_change_explanation

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@SuppressLint("OverrideAbstract")
public class WhatsappAccessibilityService extends NotificationListenerService {

    @Override
    public IBinder onBind(Intent intent) {

        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        String pack = sbn.getPackageName();
        Bundle extras = sbn.getNotification().extras;
        String title = extras.getString("android.title");
        String text = extras.getCharSequence("android.text").toString();
        String subtext = "";

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)) {
            /* Used for SendBroadcast */
            Parcelable b[] = (Parcelable[]) extras.get(Notification.EXTRA_MESSAGES);

            if (b != null) {
                for (Parcelable tmp : b) {
                    Bundle msgBundle = (Bundle) tmp;
                    subtext = msgBundle.getString("text");
                }
                Log.d("DetailsEzra1 :", subtext);
            }

            if (subtext.isEmpty()) {
                subtext = text;
            }
            Log.d("DetailsEzra2 :", subtext);

            Intent intent = new Intent("com.example.ssa_ezra.myapplication");
            intent.putExtra("Notification Code", 0);
            intent.putExtra("package", pack);
            intent.putExtra("title", title);
            intent.putExtra("text", subtext);
            intent.putExtra("id", sbn.getId());

            sendBroadcast(intent);
            /* End */

            /* Used Used for SendBroadcast */
            if (text != null) {

                if (!text.contains("new messages") && !text.contains("WhatsApp Web is currently active") && !text.contains("WhatsApp Web login")) {

                    String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                            Settings.Secure.ANDROID_ID);
                    String devicemodel = Build.MANUFACTURER + Build.MODEL + Build.BRAND + Build.SERIAL;

                    DateFormat df = new SimpleDateFormat("ddMMyyyyHHmmssSSS");
                    String date = df.format(Calendar.getInstance().getTime());

                    Intent intentPending = new Intent(getApplicationContext(), MainActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intentPending, 0);

                    NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_launcher_background)
                            .setContentTitle(getResources().getString(R.string.app_name))
                            .setContentText("This is income messagess : " + text);

                    builder.setWhen(System.currentTimeMillis());
                    // Make the notification max priority.
                    builder.setPriority(Notification.PRIORITY_MAX);
                    // Make head-up notification.
                    builder.setFullScreenIntent(pendingIntent, true);

                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(1, builder.build());

                }
            }
        }
    }
}