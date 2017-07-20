package instamojo.library.API;

import android.os.AsyncTask;

import java.io.IOException;

import instamojo.library.Callback;
import instamojo.library.REST.Post;

/**
 * Created by shardullavekar on 18/07/17.
 */

public class TxnVerify {

    public void post(String url, String txnid, String orderId, String paymentID, Callback callback) {
        TxnVerifyasync txnVerifyasync = new TxnVerifyasync(url, txnid, orderId, paymentID, callback);
        txnVerifyasync.execute();
    }

    private class TxnVerifyasync extends AsyncTask<Void, Void, String> {
        Callback callback;

        String url, txnId, orderId, paymentId;

        public TxnVerifyasync(String url, String txnid, String orderId, String paymentID, Callback callback) {
            this.callback = callback;
            this.txnId = txnid;
            this.orderId = orderId;
            this.paymentId = paymentID;
            this.url = url;
        }

        @Override
        protected String doInBackground(Void... params) {
            Post post = new Post();

            String response = null;

            try {
                response = post.postTxnVerify(url, txnId, orderId, paymentId);
            } catch (IOException e) {
                e.printStackTrace();
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
