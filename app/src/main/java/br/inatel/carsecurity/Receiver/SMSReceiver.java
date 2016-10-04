package br.inatel.carsecurity.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telephony.SmsMessage;
import android.util.Log;

import br.inatel.carsecurity.provider.NumberManagement;

/**
 * Created by lucas on 23/09/2016.
 */
public class SMSReceiver extends BroadcastReceiver {
    private final String DEBUG_TAG = getClass().getSimpleName().toString();
    private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private Context mContext;
    private Intent mIntent;
    private NumberManagement mNumberManagement;

    // Retrieve SMS
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        mIntent = intent;
        mNumberManagement = new NumberManagement(mContext);

        String action = intent.getAction();

        if(action.equals(ACTION_SMS_RECEIVED)){
            String address, content = "";
            SmsMessage[] msgs = getMessagesFromIntent(mIntent);

            if (msgs != null) {
                for (int i = 0; i < msgs.length; i++) {
                    address = msgs[i].getOriginatingAddress();
                    content += msgs[i].getMessageBody();

                    if(address.equals(mNumberManagement.getCarNumber()) && !content.isEmpty()){
                        notifyActivity(content);
                        Log.v(DEBUG_TAG, address + ':' + content);
                    }
                }
            }
        }
    }

    public static SmsMessage[] getMessagesFromIntent(Intent intent) {
        Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
        byte[][] pduObjs = new byte[messages.length][];

        for (int i = 0; i < messages.length; i++) {
            pduObjs[i] = (byte[]) messages[i];
        }
        byte[][] pdus = new byte[pduObjs.length][];
        int pduCount = pdus.length;
        SmsMessage[] msgs = new SmsMessage[pduCount];
        for (int i = 0; i < pduCount; i++) {
            pdus[i] = pduObjs[i];
            msgs[i] = SmsMessage.createFromPdu(pdus[i]);
        }
        return msgs;
    }

    private void notifyActivity(String message) {
        PackageManager pm = mContext.getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage("br.inatel.carsecurity");
        //launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        launchIntent.putExtra("latlgn", message);
        mContext.startActivity(launchIntent);
    }

    private boolean validateMessage(String message){
        String[] content = message.split(",");
        return content.length > 2;
    }
}