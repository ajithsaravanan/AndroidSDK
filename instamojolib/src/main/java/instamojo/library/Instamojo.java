package instamojo.library;

import android.app.ProgressDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.instamojo.android.models.Order;

import org.json.JSONException;
import org.json.JSONObject;

import instamojo.library.API.OrdernAuth;

public class Instamojo extends AppCompatActivity {

    String ordernauth_url, txnid_url, webhook,
            amountstr, email, mobile, buyer, description;

    ApplicationInfo app;

    Bundle bundle;

    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        app = null;

        dialog = new ProgressDialog(Instamojo.this);

        try {
            app = getApplicationContext().getPackageManager()
                    .getApplicationInfo(getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        bundle = app.metaData;

        amountstr = getIntent().getStringExtra("amount");

        ordernauth_url = bundle.getString(Config.ORDER_AUTHURL);

        txnid_url = bundle.getString(Config.TXNID_URL);

        webhook = bundle.getString(Config.WEBHOOK);

        if (TextUtils.isEmpty(ordernauth_url)) {
            Toast.makeText(getApplicationContext(), "Invalid Order URL", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        if (TextUtils.isEmpty(txnid_url)) {
            Toast.makeText(getApplicationContext(), "Invalid Transaction URL", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        if (TextUtils.isEmpty(amountstr)) {
            Toast.makeText(getApplicationContext(), "Invalid Amount", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        getOrdernAuth();

        setContentView(R.layout.activity_instamojo);
    }


    private void getOrdernAuth() {
        OrdernAuth ordernAuth = new OrdernAuth();
        ordernAuth.post(ordernauth_url, amountstr, new Callback() {
            @Override
            public void onResponse(String response) {
                dismissDialogue();
                JSONObject jsonObject = null;
                String authToken = null, orderId = null, transactionId = null;
                try {
                    jsonObject = new JSONObject(response);
                    authToken = jsonObject.getString("auth");
                    orderId = jsonObject.getString("orderId");
                    transactionId = jsonObject.getString("txnId");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                createOrder(authToken, orderId, transactionId);
            }
        });
        showDialogue("Fetching order details");
    }

    private void createOrder(String accessToken, String orderId, String transactionId) {
        Order order = new Order(accessToken, transactionId, buyer, email, mobile, amountstr, description);
        validateOrder(order);
    }

    private void validateOrder(Order order) {
        if (!order.isValid()){

            if (!order.isValidName()){
                endActivity(Config.FAILED, "Buyer name is invalid");
            }

            if (!order.isValidEmail()){
                endActivity(Config.FAILED, "Buyer email is invalid");
            }

            if (!order.isValidPhone()){
                endActivity(Config.FAILED, "Buyer phone is invalid");
            }

            if (!order.isValidAmount()){
                endActivity(Config.FAILED, "Amount is invalid");
            }

            if (!order.isValidDescription()){
                endActivity(Config.FAILED, "description is invalid");
            }

            if (!order.isValidTransactionID()){
                endActivity(Config.FAILED, "Transaction ID is invalid");
            }

            if (!order.isValidRedirectURL()){
                endActivity(Config.FAILED, "Redirection URL is invalid");
            }

            if (!order.isValidWebhook()) {
                endActivity(Config.FAILED, "Webhook URL is invalid");
            }

        }

    }

    private void showDialogue(String message) {
        dialog.setMessage(message);
        dialog.show();
    }

    private void dismissDialogue() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }


    private void endActivity(int resultCode, String message) {
        setResult(resultCode);
        Instamojo.this.finish();
        return;
    }

}
