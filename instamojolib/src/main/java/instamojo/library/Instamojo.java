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
            amountstr, email, phone, name, description, purpose, accessToken, env;

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

//        Intent intent = new Intent();
//        intent.setAction("ai.devsupport.instamojo");
//        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
//        sendBroadcast(intent);
//        endActivity();

        checkEnvironment(env);

        if (checkValidation()) {
            getOrdernAuth();
        }

        setContentView(R.layout.activity_instamojo);
    }

    private void checkEnvironment(String env) {
        if (TextUtils.equals(env, Config.TEST)) {
            com.instamojo.android.Instamojo.setBaseUrl("https://test.instamojo.com/");
            this.env = env;
        }

        else if (TextUtils.equals(env, Config.PROD)) {
            com.instamojo.android.Instamojo.setBaseUrl("https://api.instamojo.com/");
            this.env = env;
        }

        else {
            fireBroadcast(Config.FAILED, "Invalid Environment");
            endActivity();
        }
    }

    private boolean checkValidation() {
        if (TextUtils.isEmpty(ordernauth_url)) {
            fireBroadcast(Config.FAILED, "Invalid Order URL");
            endActivity();
            return false;
        }

        if (!Config.isValidAmount(amountstr)) {
            fireBroadcast(Config.FAILED, "Invalid Amount - Amount needs to be greater than Rs. 10");
            endActivity();
            return false;
        }

        if (!Config.isValidMail(email)) {
            fireBroadcast(Config.FAILED, "Invalid Email");
            endActivity();
            return false;
        }

        if (!Config.isValidMobile(phone)) {
            fireBroadcast(Config.FAILED, "Invalid Mobile");
            endActivity();
            return false;
        }

        if (TextUtils.isEmpty(name)) {
            fireBroadcast(Config.FAILED, "Invalid Name");
            endActivity();
            return false;
        }

        if (TextUtils.isEmpty(purpose)) {
            fireBroadcast(Config.FAILED, "Invalid Purpose");
            endActivity();
            return false;
        }

        return true;

    }


    private void getOrdernAuth() {
        OrdernAuth ordernAuth = new OrdernAuth();
        ordernAuth.post(ordernauth_url, email, phone, name, amountstr, purpose, new Callback() {
            @Override
            public void onResponse(String response) {
                dismissDialogue();
                JSONObject jsonObject = null;
                String orderId = null, transactionId = null;
                try {
                    jsonObject = new JSONObject(response);
                    accessToken = jsonObject.getString("token");
                    JSONObject orderJson = jsonObject.getJSONObject("order");
                    orderId = orderJson.getString("order_id");
                    transactionId = jsonObject.getString("transaction_id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                createOrder(accessToken, orderId);
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
                        dismissDialogue();
                        if (error != null) {
                            if (error instanceof Errors.ConnectionError) {
                                fireBroadcast(Config.FAILED, "No internet connection");
                                endActivity();
                            } else if (error instanceof Errors.ServerError) {
                                fireBroadcast(Config.FAILED, "Server Error. Try again");
                                endActivity();
                            } else if (error instanceof Errors.AuthenticationError) {
                                fireBroadcast(Config.FAILED, "Check if you are using correct environment/credentials combo");
                                endActivity();
                            } else {
                                fireBroadcast(Config.FAILED, error.toString());
                                endActivity();
                            }
                            return;
                        }
                        startprecreatedUI(order);
                    }
                });

            }
        });
        showDialogue("Creating Payment Request");
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
            final String orderID = data.getStringExtra(Constants.ORDER_ID);
            final String transactionID = data.getStringExtra(Constants.TRANSACTION_ID);
            final String paymentID = data.getStringExtra(Constants.PAYMENT_ID);

            TxnVerify verify = new TxnVerify();

            Callback callback = new Callback() {
                @Override
                public void onResponse(String response) {
                    dismissDialogue();
                    JSONObject jsonresponse = null;
                    try {
                        jsonresponse = new JSONObject(response);
                        if (jsonresponse.getBoolean("status")) {
                            String status = "success";

                            String message = "status=" + status + ":orderId=" + orderID + ":txnId=" + transactionID + ":paymentId=" + paymentID;

                            fireBroadcast(Config.SUCCESS, message);
                        }
                        else {
                            fireBroadcast(Config.FAILED, "Incorrect Payment Details");
                        }
                        endActivity();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };

            if (orderID != null && transactionID != null && paymentID != null) {
                showDialogue("Fetching Payment Status");
                String baseURL;
                if (TextUtils.equals(env, Config.TEST)) {
                    baseURL = "https://test.instamojo.com/";
                }

                else {
                    baseURL = "https://api.instamojo.com/";
                }
                verify.get(baseURL + "v2/payments/", accessToken, paymentID, callback);
            }
            else {
                fireBroadcast(Config.FAILED, "Payment was cancelled");
                endActivity();
            }


        }
    }

    private void fireBroadcast(int code, String message) {
        Intent intent = new Intent();
        intent.setAction("ai.devsupport.instamojo");
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.putExtra("code", code);
        intent.putExtra("response", message);
        sendBroadcast(intent);
    }

    private void endActivity() {
        Instamojo.this.finish();
    }


}
