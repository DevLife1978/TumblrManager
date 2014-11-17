package jay.epigram.jaytumblr;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tumblr.jumblr.types.User;


public class LauchActivity extends Activity implements Button.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lauch);

        final Button btn = (Button)findViewById(R.id.tumblr_btn);
        btn.setOnClickListener(this);

        if (TumblrManager.authorize(this)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    User user = TumblrManager.CurrentManager().user();
                    Message msg = new Message();
                    msg.what = 0;
                    msg.obj = user;
                    mHandler.sendMessage(msg);
                }
            }).start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.lauch, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (TumblrManager.isAuthorized(v.getContext())) {
            TumblrManager.clear(v.getContext());

            Button btn = (Button) findViewById(R.id.tumblr_btn);
            btn.setText("Login");
            TextView tv = (TextView) findViewById(R.id.textView);
            tv.setText("Hello Tumblr");
        }
        else {
            final TumblrManager tm = new TumblrManager(this);
            tm.authorize(this, new TumblrManager.RequestAuthResult() {
                @Override
                public void onReceiveOAuthAccessTokenAndSecret(boolean success) {
                    final User user = tm.user();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView textView = (TextView) findViewById(R.id.textView);
                            textView.setText("Hello " + user.getName());
                            Log.i("User", user.getName());
                            Button btn = (Button) findViewById(R.id.tumblr_btn);
                            btn.setText("Logout");
                        }
                    });
                }
            });
        }
    }

    private final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case 0:
                    User user = (User) msg.obj;
                    Button btn = (Button) findViewById(R.id.tumblr_btn);
                    btn.setText("Logout");
                    TextView tv = (TextView) findViewById(R.id.textView);
                    tv.setText("Hello " + user.getName());
                    break;
            }
        }
    };

}
