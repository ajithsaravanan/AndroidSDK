package instamojo.library.API;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import instamojo.library.Callback;
import instamojo.library.REST.Post;

/**
 * Created by shardullavekar on 18/07/17.
 */

public class OrdernAuth {
    public OrdernAuth() {

    }

    public void post(String url, String amount, Callback callback) {
        OrdernAuthasynch authasynch = new OrdernAuthasynch(url, amount, callback);
        authasynch.execute();
    }

    private class OrdernAuthasynch extends AsyncTask<Void, Void, String> {
        Callback callback;

        String url, amount;

        public OrdernAuthasynch(String url, String amount, Callback callback) {
            this.callback = callback;
            this.amount = amount;
            this.url = url;
        }

        @Override
        protected String doInBackground(Void... params) {
            Post post = new Post();
            JSONObject jsonObject = new JSONObject();
            String response = null;
            try {
                jsonObject.put("txnId", amount);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                response = post.postdata(url, jsonObject.toString());
            } catch (IOException e) {
                e.printStackTrace();
                callback.onResponse("Internet unavailable or server down");
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            callback.onResponse(s);
        }
    }

}

