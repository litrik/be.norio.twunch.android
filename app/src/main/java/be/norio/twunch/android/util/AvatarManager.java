
package be.norio.twunch.android.util;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import be.norio.twunch.android.BuildConfig;
import be.norio.twunch.android.otto.AvatarAvailableEvent;
import be.norio.twunch.android.otto.BusProvider;
import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterMethod;
import twitter4j.User;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;

public class AvatarManager {

    private static Context mContext;
    private static AsyncTwitter mTwitter;
    private static LruCache<String, String> mAvatars;
    private static Set mQueue = Collections.synchronizedSet(new LinkedHashSet<String>());
    private static int mOutstandingNetworkCalls = 0;
    private static final Handler mHandler = new Handler(Looper.getMainLooper());

    public static void initialize(Application application) {
        mContext = application;

        mAvatars = PrefsUtils.getAvatars();

        if (PrefsUtils.getTwitterToken() == null) {
            getToken();
        } else {
            configureClient();
        }

    }

    private static void getToken() {
        Thread t = new Thread() {
            @Override
            public void run() {
                OAuth2Token token;
                ConfigurationBuilder cb;
                cb = new ConfigurationBuilder();
                cb.setApplicationOnlyAuthEnabled(true);
                cb.setUseSSL(true);
                cb.setOAuthConsumerKey(BuildConfig.TWITTER_CONSUMER_KEY);
                cb.setOAuthConsumerSecret(BuildConfig.TWITTER_CONSUMER_SECRET);
                try {
                    token = new TwitterFactory(cb.build()).getInstance().getOAuth2Token();
                    PrefsUtils.setTwitterToken(token.getAccessToken());
                    configureClient();
                } catch (Exception e) {
                    System.out.println("Can't get OAuth2 token from Twitter");
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    private static void configureClient() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setApplicationOnlyAuthEnabled(true);
        cb.setUseSSL(true);
        cb.setOAuthConsumerKey(BuildConfig.TWITTER_CONSUMER_KEY);
        cb.setOAuthConsumerSecret(BuildConfig.TWITTER_CONSUMER_KEY);
        cb.setOAuth2TokenType("bearer");
        cb.setOAuth2AccessToken(PrefsUtils.getTwitterToken());

        mTwitter = new AsyncTwitterFactory(cb.build()).getInstance();
        mTwitter.addListener(new TwitterAdapter() {
            @Override
            public void gotUserDetail(User user) {
                super.gotUserDetail(user);
                mAvatars.put(user.getScreenName().toLowerCase(), user.getOriginalProfileImageURL());
                decrementOutstandingNetworkCalls();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        BusProvider.getInstance().post(new AvatarAvailableEvent());
                    }
                });
                PrefsUtils.setAvatars(mAvatars);
            }

            @Override
            public void onException(TwitterException te, TwitterMethod method) {
                super.onException(te, method);
                decrementOutstandingNetworkCalls();
                if (te.getStatusCode() == 401) {
                    PrefsUtils.setTwitterToken(null);
                } else {
                    te.printStackTrace();
                }
            }
        });
    }

    private static void decrementOutstandingNetworkCalls() {
        mOutstandingNetworkCalls--;
        if (mOutstandingNetworkCalls == 0) {
            mQueue.clear();
        }
    }

    public static boolean isAvatarAvailable(String userid) {
        return mAvatars.get(userid.toLowerCase()) != null;
    }

    public static void addToQueue(String userid) {
        if (!mQueue.contains(userid) && mTwitter != null) {
            mOutstandingNetworkCalls++;
            mQueue.add(userid);
            mTwitter.showUser(userid);
        }
    }

    public static String getAvatar(String userid) {
        return mAvatars.get(userid.toLowerCase());
    }
}