/*
Copyright (C) 2014 Boris Timofeev

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

import android.content.Context;
import android.support.v4.app.NotificationCompat;

import org.emunix.unipatcher.R;

public class SmdFixChecksumNotify extends Notify {

    public SmdFixChecksumNotify(Context c, String text) {
        super(c);
        notifyBuilder.setSmallIcon(R.drawable.ic_stat_patching);
        notifyBuilder.setContentTitle(context.getString(R.string.notify_smd_fix_checksum_in_progress));
        notifyBuilder.setContentText(text);
        notifyBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
        setSticked(true);
        setProgress(true);
    }

    @Override
    public void setCompleted() {
        setProgress(false);
        setSticked(false);
        notifyBuilder.setTicker(context.getText(R.string.notify_smd_fix_checksum_complete));
        notifyBuilder.setContentTitle(context.getText(R.string.notify_smd_fix_checksum_complete));
    }

    @Override
    public void setFailed(String message) {
        setProgress(false);
        setSticked(false);
        notifyBuilder.setTicker(context.getText(R.string.notify_error));
        notifyBuilder.setContentTitle(context.getString(R.string.notify_error));
        notifyBuilder.setContentText(message);
        notifyBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
    }
}
