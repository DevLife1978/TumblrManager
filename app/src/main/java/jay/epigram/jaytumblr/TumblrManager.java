package jay.epigram.jaytumblr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.User;

import java.lang.ref.WeakReference;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import oauth.signpost.signature.HmacSha1MessageSigner;

/**
 * Created by jay on 14. 11. 17..
 */
public class TumblrManager {
    private static final Logger mLogger = new Logger(TumblrManager.class.getSimpleName());

    private String accessToken;
    private String accessSecret;
    private final OAuthConsumer consumer;
    private final OAuthProvider provider;

    private JumblrClient mTumblr;

    private RequestAuthResult mResult;
    private final Context mContext;
    private WeakReference<Context> mActivity;

    private static TumblrManager currentManager;

    public TumblrManager(Context context) {
        super();
        this.mContext = context.getApplicationContext();
        String consumer_token = context.getString(R.string.tumblr_consumer_token);
        String consumer_secret = context.getString(R.string.tumblr_consumer_secret);
        consumer = new CommonsHttpOAuthConsumer(
                consumer_token, consumer_secret
        );
        consumer.setMessageSigner(new HmacSha1MessageSigner());
        provider = new CommonsHttpOAuthProvider(
                "http://www.tumblr.com/oauth/request_token",
                "http://www.tumblr.com/oauth/access_token",
                "http://www.tumblr.com/oauth/authorize"
        );
        currentManager = this;
    }

    public static boolean authorize(Context context) {
        if (isAuthorized(context)) {
            currentManager = new TumblrManager(context);
            String consumer_token = context.getString(R.string.tumblr_consumer_token);
            String consumer_secret = context.getString(R.string.tumblr_consumer_secret);
            JumblrClient tumblr = new JumblrClient(consumer_token, consumer_secret);

            SharedPreferences sp = context.getSharedPreferences("Tumblr", 0);
            consumer_token = sp.getString("token", null);
            consumer_secret = sp.getString("secret", null);
            tumblr.setToken(consumer_token, consumer_secret);
            currentManager.mTumblr = tumblr;
            return true;
        }
        return false;
    }

    public static TumblrManager CurrentManager() {
        return currentManager;
    }

    public void authorize(final Activity context, RequestAuthResult result) {
        this.mResult = result;
        this.mActivity = new WeakReference<Context>(context);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String authUrl = provider.retrieveRequestToken(consumer, "projectg://authorization/");
                    accessToken = consumer.getToken();
                    accessSecret = consumer.getTokenSecret();

                    if (null != authUrl) {
                        Intent i = new Intent(context, TumblrAuthActivity.class);
                        i.setData(Uri.parse(authUrl));
                        context.startActivityForResult(i, TumblrAuthActivity.AuthUriResult);
                    }

                } catch (OAuthMessageSignerException e) {
                    e.printStackTrace();
                } catch (OAuthNotAuthorizedException e) {
                    e.printStackTrace();
                } catch (OAuthExpectationFailedException e) {
                    e.printStackTrace();
                } catch (OAuthCommunicationException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    protected void authorization(final Uri uri, final Result result) {
        final String token = uri.getQueryParameter("oauth_token");
        final String verifier = uri.getQueryParameter("oauth_verifier");
        this.accessToken = token;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    provider.retrieveAccessToken(consumer, verifier);
                    accessToken = consumer.getToken();
                    accessSecret = consumer.getTokenSecret();
                    mLogger.info("\nkey="+consumer.getToken() + "\ntoken="+consumer.getTokenSecret());
                    mTumblr = new JumblrClient(consumer.getConsumerKey(), consumer.getConsumerSecret());
                    mTumblr.setToken(accessToken, accessSecret);

                    SharedPreferences.Editor editor = getSharedPreferences();
                    editor.putString("token", consumer.getToken());
                    editor.putString("secret", consumer.getTokenSecret());
                    editor.apply();

                    if (mActivity.get() != null) {
                        result.onResult();
                        mResult.onReceiveOAuthAccessTokenAndSecret(true);
                    }
                } catch (OAuthMessageSignerException e) {
                    e.printStackTrace();
                } catch (OAuthNotAuthorizedException e) {
                    e.printStackTrace();
                } catch (OAuthExpectationFailedException e) {
                    e.printStackTrace();
                } catch (OAuthCommunicationException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void clear(Context context) {
        synchronized (TumblrManager.class) {
            SharedPreferences.Editor spe = context.getSharedPreferences("Tumblr", 0).edit();
            spe.remove("token");
            spe.remove("secret");
            spe.apply();
        }
    }

    public interface RequestAuthResult {
        public void onReceiveOAuthAccessTokenAndSecret(boolean success);
    }

    public interface Result {
        public void onResult();
    }

    public JumblrClient Tumblr() {
        return this.mTumblr;
    }

    public User user() {
        return this.Tumblr().user();
    }

    private SharedPreferences.Editor getSharedPreferences() {
        return mContext.getSharedPreferences("Tumblr", 0).edit();
    }

    public static boolean isAuthorized(Context context) {
        SharedPreferences sp = context.getSharedPreferences("Tumblr", 0);
        String token = sp.getString("token", null);
        String secret = sp.getString("secret", null);
        return token != null && secret != null;
    }
}
