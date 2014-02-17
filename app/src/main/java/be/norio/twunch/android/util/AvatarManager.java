package be.norio.twunch.android.util;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import be.norio.twunch.android.BuildConfig;
import be.norio.twunch.android.otto.AvatarAvailableEvent;
import be.norio.twunch.android.otto.BusProvider;
import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

public class AvatarManager {

    private static Context mContext;
    private static AsyncTwitter mTwitter;
    private static LruCache<String,String> mAvatars;
    private static Set mQueue = Collections.synchronizedSet(new LinkedHashSet<String>());
    private static int mOutstandingNetworkCalls = 0;
    private static final Handler mHandler = new Handler(Looper.getMainLooper());


    public static void initialize(Application application) {
        mContext = application;

        mAvatars = PrefsUtils.getAvatars();

        ConfigurationBuilder cb2 = new ConfigurationBuilder();
        cb2.setApplicationOnlyAuthEnabled(true);
        cb2.setOAuthConsumerKey(BuildConfig.TWITTER_CONSUMER_KEY);
        cb2.setOAuthConsumerSecret(BuildConfig.TWITTER_CONSUMER_KEY);
        cb2.setOAuth2TokenType("bearer");
        cb2.setOAuth2AccessToken(PrefsUtils.getTwitterToken());

        mTwitter = new AsyncTwitterFactory(cb2.build()).getInstance();
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
                System.out.println(te.toString());
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
        if (!mQueue.contains(userid)) {
            mOutstandingNetworkCalls++;
            mQueue.add(userid);
            mTwitter.showUser(userid);
        }
    }

    public static String getAvatar(String userid) {
        return mAvatars.get(userid.toLowerCase());
    }
}