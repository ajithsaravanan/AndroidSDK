package instamojo.library;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by shardullavekar on 05/09/17.
 */

public class InstamojoPay extends BroadcastReceiver {
    String amountstr, email, phone, name, description, purpose;

    JSONObject payment;
    Activity activity;
    InstapayListener listener;

    public void start(Activity activity, JSONObject payment, InstapayListener listener){
        this.activity = activity;
        this.payment = payment;
        this.listener = listener;
        initInstamojo();
    }

    private void initInstamojo() {

        try {
            amountstr = payment.getString("amount");

            purpose = payment.getString("purpose");

            email = payment.getString("email");

            phone = payment.getString("phone");

            name = payment.getString("name");

            String env = payment.getString("env");

            Intent intent = new Intent(activity, Instamojo.class);
            intent.putExtra("email", email);
            intent.putExtra("phone", phone);
            intent.putExtra("purpose", purpose);
            intent.putExtra("amount", amountstr);
            intent.putExtra("name", name);
            intent.putExtra("env", env);

            activity.startActivity(intent);

        } catch (JSONException e) {
            e.printStackTrace();
            listener.onFailure(Config.FAILED, "One of the params in JSON is missing");
        }

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int code = intent.getIntExtra("code", Config.FAILED);
        if (code == Config.SUCCESS) {
            listener.onSuccess(intent.getStringExtra("response"));
        }
        else {
            listener.onFailure(Config.FAILED, intent.getStringExtra("response"));
        }
    }
}
