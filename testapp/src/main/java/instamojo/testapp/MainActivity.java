package instamojo.testapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import instamojo.library.Config;
import instamojo.library.Instamojo;

public class MainActivity extends AppCompatActivity {
    Button pay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        pay = (Button) this.findViewById(R.id.pay);

        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = "tester@gmail.com";
                String phone = "7875432991";
                String amount = "10";
                String purpose = "official";
                String buyername = "tester";

                callInstamojo(email, phone, amount, purpose, buyername);
            }
        });
    }

    private void callInstamojo(String email, String phone, String amount, String purpose, String buyername) {
        Intent intent = new Intent(MainActivity.this, Instamojo.class);
        intent.putExtra("email", email);
        intent.putExtra("phone", phone);
        intent.putExtra("purpose", purpose);
        intent.putExtra("amount", amount);
        intent.putExtra("name", buyername);
        startActivityForResult(intent, Config.INSTAMOJO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Config.INSTAMOJO:
                switch (resultCode) {
                    case Config.SUCCESS:
                        Toast.makeText(getApplicationContext(), data.getStringExtra("response"), Toast.LENGTH_LONG)
                                .show();
                        break;
                    case Config.FAILED:
                        Toast.makeText(getApplicationContext(), data.getStringExtra("response"), Toast.LENGTH_LONG)
                                .show();
                        break;
                    default:
                        break;
                }

            default:
                break;
        }
    }
}
