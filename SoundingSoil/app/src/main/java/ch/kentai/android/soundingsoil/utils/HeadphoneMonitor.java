package ch.kentai.android.soundingsoil.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.os.Message;
import android.widget.Toast;

public class HeadphoneMonitor extends BroadcastReceiver {
    public boolean headphonesActive=false;

    @Override
    public void onReceive(final Context context, final Intent intent)
    {
        if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG))
        {
            int state = intent.getIntExtra("state", -1);
            switch (state) {
                case 0:
                    Log.d("HeadphoneMonitor", "Headset is unplugged");
                    Toast.makeText(context, "Headset is unplugged", Toast.LENGTH_LONG).show();
                    headphonesActive=false;
                    break;
                case 1:
                    Log.d("HeadphoneMonitor", "Headset is plugged in");
                    Toast.makeText(context, "Headset is plugged in", Toast.LENGTH_LONG).show();
                    headphonesActive=true;
                    break;
                default:
                    Log.d("HeadphoneMonitor", "I have no idea what the headset state is");
                    break;
            }

            // push this event onto the queue to be processed by the Handler
            //Message msg = uiHandler.obtainMessage(HEADPHONE_EVENT);
            //MyApp.uiHandler.sendMessage(msg);
        }
    }
}
