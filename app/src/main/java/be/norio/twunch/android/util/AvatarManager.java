package be.norio.twunch.android.util;

import android.os.Handler;
import android.os.Looper;

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

    private static AvatarManager instance = null;

    public static AvatarManager getInstance() {
        if (instance == null) {
            instance = new AvatarManager();
        }
        return instance;
    }

    private AsyncTwitter mTwitter;
    Map<String, String> mAvatars = new HashMap<String, String>();
    Set mQueue = Collections.synchronizedSet(new LinkedHashSet<String>());
    int mOutstandingNetworkCalls = 0;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    AvatarManager() {
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
                mAvatars.put(user.getScreenName(), user.getBiggerProfileImageURL());
                decrementOutstandingNetworkCalls();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        BusProvider.getInstance().post(new AvatarAvailableEvent());
                    }
                });
            }

            @Override
            public void onException(TwitterException te, TwitterMethod method) {
                super.onException(te, method);
                decrementOutstandingNetworkCalls();
                System.out.println(te.toString());
            }
        });
    }

    private void decrementOutstandingNetworkCalls() {
        mOutstandingNetworkCalls--;
        if (mOutstandingNetworkCalls == 0) {
            mQueue.clear();
        }
    }

    public boolean isAvatarAvailable(String userid) {
        return mAvatars.containsKey(userid);
    }

    public void addToQueue(String userid) {
        if (!mQueue.contains(userid)) {
            mOutstandingNetworkCalls++;
            mQueue.add(userid);
            mTwitter.showUser(userid);
        }
    }

    public String getAvatar(String userid) {
        return mAvatars.get(userid);
    }
}