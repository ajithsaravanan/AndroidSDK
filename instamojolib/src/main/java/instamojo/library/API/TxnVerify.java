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

public class TxnVerify {

    public void post(String url, String txnid) {

    }

    private class TxnVerifyasync extends AsyncTask<Void, Void, String> {
        Callback callback;

        String url, txnId;

        public TxnVerifyasync(String url, String txnId, Callback callback) {
            this.callback = callback;
            this.txnId = txnId;
            this.url = url;
        }

        @Override
        protected String doInBackground(Void... params) {
            Post post = new Post();
            JSONObject jsonObject = new JSONObject();
            String response = null;
            try {
                jsonObject.put("txnId", txnId);
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
