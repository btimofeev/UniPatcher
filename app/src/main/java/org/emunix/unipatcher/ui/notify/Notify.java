/*
Copyright (C) 2013 Boris Timofeev

This file is part of UniPatcher.

UniPatcher is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

UniPatcher is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with UniPatcher.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.emunix.unipatcher.ui.notify;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.emunix.unipatcher.UniPatcher;
import org.emunix.unipatcher.ui.activity.MainActivity;

public abstract class Notify {
    protected static int count = 1;
    protected final int ID = count;
    protected Context context;

    protected NotificationCompat.Builder notifyBuilder;
    protected NotificationManagerCompat notifyMng;

    public Notify(Context c) {
        context = c;
        count++;

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notifyBuilder = new NotificationCompat.Builder(context, UniPatcher.NOTIFICATION_CHANNEL_ID);
        notifyBuilder.setContentIntent(PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT));
        notifyMng = NotificationManagerCompat.from(context);
    }

    public void show() {
        notifyMng.notify(ID, notifyBuilder.build());
    }

    public int getID() {
        return ID;
    }

    public NotificationCompat.Builder getNotifyBuilder() {
        return notifyBuilder;
    }

    public void showResult(String message) {
        if (message == null) {
            setCompleted();
        } else {
            setFailed(message);
        }
        show();
    }

    public abstract void setCompleted();

    public abstract void setFailed(String message);

    public void setProgress(boolean isEnabled) {
        notifyBuilder.setProgress(0, 0, isEnabled);
    }

    public void setSticked(boolean isSticked) {
        notifyBuilder.setAutoCancel(!isSticked);
        notifyBuilder.setOngoing(isSticked);
    }
}
