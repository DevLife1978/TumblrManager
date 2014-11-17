package jay.epigram.jaytumblr;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class TumblrAuthActivity extends Activity {

    public static final int AuthUriResult = 101;
    public static final String AuthUriKey = "AuthUriKey";
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tumblr);

        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith({url for auth})) {
                    mWebView.setVisibility(View.INVISIBLE);
                    TumblrManager.CurrentManager().authorization(Uri.parse(url), new TumblrManager.Result() {
                        @Override
                        public void onResult() {
                            setResult(AuthUriResult);
                            finish();
                        }
                    });
                    return false;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(getIntent().getData().toString());
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tumblr, menu);
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
}
