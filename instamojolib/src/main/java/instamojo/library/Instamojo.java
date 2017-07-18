package instamojo.library;

import android.app.ProgressDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

public class Instamojo extends AppCompatActivity {

    String ordernauth_url, txnid_url, amountstr;

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

        setContentView(R.layout.activity_instamojo);
    }
}
