package top.way2free.testdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UpgradeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        if (null != intent) {
            if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {
                Intent bootIntent = new Intent(context, MainActivity.class);
                bootIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(bootIntent);
            }
        }
    }
}