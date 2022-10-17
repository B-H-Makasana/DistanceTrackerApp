package com.example.distanceTractorApp.di

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.distanceTractorApp.MainActivity
import com.example.distanceTractorApp.R
import com.example.distanceTractorApp.util.Constant
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
 class NotificationModule {

    @ServiceScoped
    @Provides
    fun pendongIntent(@ApplicationContext context:Context):PendingIntent{
        return PendingIntent.getActivity(context,Constant.PENDING_INTENT_REQUESTCODE, Intent(context,MainActivity::class.java).apply {
            this.action=Constant.ACTION_NAVIGATE_TO_MAPS_FRAGMENT
        },PendingIntent.FLAG_UPDATE_CURRENT)
    }


     @ServiceScoped
     @Provides
    fun provideNotificationBuilder(@ApplicationContext context: Context,pendingIntent: PendingIntent):NotificationCompat.Builder{
        return NotificationCompat.Builder(context,Constant.NOTOFICATION_CHANNEL_ID).
                setAutoCancel(false).setOngoing(true).setSmallIcon(R.drawable.ic_run).
                setContentIntent(pendingIntent)
    }

    @ServiceScoped
    @Provides
    fun provideNotificationManager(@ApplicationContext context: Context):NotificationManager{
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}