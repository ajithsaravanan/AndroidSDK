package instamojo.library;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.instamojo.android.activities.PaymentDetailsActivity;
import com.instamojo.android.callbacks.OrderRequestCallBack;
import com.instamojo.android.helpers.Constants;
import com.instamojo.android.models.Order;
import com.instamojo.android.network.Request;

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

        if (!TextUtils.isEmpty(webhook)) {
            order.setWebhook(webhook);
        }

        Request request = new Request(order, new OrderRequestCallBack(){

            @Override
            public void onFinish(Order order, Exception e) {
                if (e!= null) {
                endActivity(Config.FAILED, "Error Occured");
                }
                else {
                    startprecreatedUI(order);
                }


            }
        });
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

    private void startprecreatedUI(Order order) {
        Intent intent = new Intent(getBaseContext(), PaymentDetailsActivity.class);
        intent.putExtra(Constants.ORDER, order);
        startActivityForResult(intent, Constants.REQUEST_CODE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE && data != null) {
            String orderID = data.getStringExtra(Constants.ORDER_ID);
            String transactionID = data.getStringExtra(Constants.TRANSACTION_ID);
            String paymentID = data.getStringExtra(Constants.PAYMENT_ID);

            // Check transactionID, orderID, and orderID for null before using them to check the Payment status.
            if (orderID != null && transactionID != null && paymentID != null) {
                String message = "orderId=" + orderID + ":txnId=" + transactionID + ":paymentId=" + paymentID;
                endActivity(Config.SUCCESS, message);
            } else {
                endActivity(Config.FAILED, "Payment was cancelled");
            }
        }
    }

    private void endActivity(int resultCode, String message) {
        Intent data = new Intent();
        data.putExtra("response", message);
        setResult(resultCode, data);
        Instamojo.this.finish();
        return;
    }

}
