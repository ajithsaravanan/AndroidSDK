package instamojo.library;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.instamojo.android.activities.PaymentDetailsActivity;
import com.instamojo.android.callbacks.OrderRequestCallBack;
import com.instamojo.android.helpers.Constants;
import com.instamojo.android.models.Errors;
import com.instamojo.android.models.Order;
import com.instamojo.android.network.Request;

import org.json.JSONException;
import org.json.JSONObject;

import instamojo.library.API.OrdernAuth;
import instamojo.library.API.TxnVerify;

public class Instamojo extends AppCompatActivity {

    String ordernauth_url,
            amountstr, email, phone, name, description, purpose;

    ApplicationInfo app;

    Bundle bundle;

    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.instamojo.android.Instamojo.initialize(this);

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

        purpose = getIntent().getStringExtra("purpose");

        email = getIntent().getStringExtra("email");

        phone = getIntent().getStringExtra("phone");

        name = getIntent().getStringExtra("name");

        purpose = getIntent().getStringExtra("purpose");

        description = getIntent().getStringExtra("description");

        String env = getIntent().getStringExtra("env");

        ordernauth_url = bundle.getString(Config.ORDER_AUTHURL);

        checkEnvironment(env);

        checkValidation();

        getOrdernAuth();

        setContentView(R.layout.activity_instamojo);
    }

    private void checkEnvironment(String env) {
        if (TextUtils.equals(env, Config.TEST)) {
            com.instamojo.android.Instamojo.setBaseUrl("https://test.instamojo.com/");
        }

        else if (TextUtils.equals(env, Config.PROD)) {
            com.instamojo.android.Instamojo.setBaseUrl("https://api.instamojo.com/");
        }

        else {
            endActivity(Config.FAILED, "Invalid Environment");
        }
    }

    private void checkValidation() {
        if (TextUtils.isEmpty(ordernauth_url)) {
            endActivity(Config.FAILED, "Invalid Order URL");
        }

        if (TextUtils.isEmpty(amountstr)) {
            endActivity(Config.FAILED, "Invalid Amount");
        }

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(phone)) {
            endActivity(Config.FAILED, "Invalid Email or Phone");
        }
    }


    private void getOrdernAuth() {
        OrdernAuth ordernAuth = new OrdernAuth();
        ordernAuth.post(ordernauth_url, email, phone, name, amountstr, purpose, new Callback() {
            @Override
            public void onResponse(String response) {
                dismissDialogue();
                JSONObject jsonObject = null;
                String authToken = null, orderId = null, transactionId = null;
                try {
                    jsonObject = new JSONObject(response);
                    authToken = jsonObject.getString("token");
                    JSONObject orderJson = jsonObject.getJSONObject("order");
                    orderId = orderJson.getString("order_id");
                    transactionId = jsonObject.getString("transaction_id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                createOrder(authToken, orderId);
            }
        });
        showDialogue("Fetching order details");
    }

    private void createOrder(String accessToken, String orderId) {

        Request request = new Request(accessToken, orderId, new OrderRequestCallBack() {
            @Override
            public void onFinish(final Order order, final Exception error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (error != null) {
                            if (error instanceof Errors.ConnectionError) {
                                endActivity(Config.FAILED, "No internet connection");
                            } else if (error instanceof Errors.ServerError) {
                                endActivity(Config.FAILED, "Server Error. Try again");
                            } else if (error instanceof Errors.AuthenticationError) {
                                endActivity(Config.FAILED, "Access token is invalid or expired. Please Update the token!!");
                            } else {
                                endActivity(Config.FAILED, error.toString());
                            }
                            return;
                        }
                        startprecreatedUI(order);
                    }
                });

            }
        });
        request.execute();
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

            if (orderID != null && transactionID != null && paymentID != null) {
                postTxnverify(transactionID, orderID, paymentID);
            } else {
                endActivity(Config.FAILED, "Payment was cancelled");
            }
        }
    }

    private void postTxnverify(final String txnid, final String orderId, final String paymentId) {
        TxnVerify txnVerify = new TxnVerify();
        txnVerify.post(ordernauth_url, txnid, orderId, paymentId, new Callback() {
            @Override
            public void onResponse(String response) {
                JSONObject jsonObject = null;
                String status = null;
                try {
                    jsonObject = new JSONObject(response);
                    status = jsonObject.getString("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String message = "status=" + status + ":orderId=" + orderId + ":txnId=" + txnid + ":paymentId=" + paymentId;
                if (TextUtils.equals(status, "success")) {
                    endActivity(Config.SUCCESS, message);
                }
                else {
                    endActivity(Config.FAILED, message);
                }
            }
        });
    }

    private void endActivity(int resultCode, String message) {
        Intent data = new Intent();
        data.putExtra("response", message);
        setResult(resultCode, data);
        Instamojo.this.finish();
        return;
    }

}
