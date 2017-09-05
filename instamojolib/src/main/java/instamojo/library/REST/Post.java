package instamojo.library.REST;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by shardullavekar on 03/07/17.
 */

public class Post {
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();

    public String postOrdernAuth(String url, String name, String email, String phone, String purpose, String amount) throws IOException {

        Request request = new Request.Builder()
                .url(url + "?action=new_transaction&name=" + name + "&email=" + email + "&phone=" + phone + "&amount=" + amount
                + "&purpose=" + purpose + "&client_type=Android")
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public String postTxnVerify(String url, String transactionID, String orderID, String paymentID) throws IOException {
        Request request = new Request.Builder()
                        .url(url + "?action=handle_redirect&txnId=" + transactionID + "&orderId=" + orderID + "&paymentId=" + paymentID)
                        .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

}
