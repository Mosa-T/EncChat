

package com.example.encchat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingServiceClass extends FirebaseMessagingService {


    Intent resultIntent;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notiTitle = remoteMessage.getNotification().getTitle();
        String notiMessage = remoteMessage.getNotification().getBody();
        String noticlick = remoteMessage.getNotification().getClickAction();
        String userid = remoteMessage.getData().get("from_user_id");
        String username = remoteMessage.getData().get("from_username");
        String mom_by_id = remoteMessage.getData().get("name");


        int notiId = (int) System.currentTimeMillis();

        resultIntent = new Intent(noticlick);
        resultIntent.putExtra("from_user_id",userid);
        resultIntent.putExtra("username",username);
        resultIntent.putExtra("name",mom_by_id);


        PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, resultIntent,
                PendingIntent.FLAG_ONE_SHOT);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, NOTIFICATION_SERVICE)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(notiTitle)
                .setContentText(notiMessage)
                .setSound(alarmSound)
                .setContentIntent(resultPendingIntent);

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        nm.notify(notiId,mBuilder.build());


    }
}
