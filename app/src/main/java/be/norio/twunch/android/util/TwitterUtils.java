package be.norio.twunch.android.util;

import be.norio.twunch.android.BuildConfig;
import twitter4j.AsyncTwitterFactory;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterUtils {


    public static void getToken() {

        Thread t = new Thread() {
            @Override
            public void run() {
                OAuth2Token token;
                ConfigurationBuilder cb;
                cb = new ConfigurationBuilder();
                cb.setApplicationOnlyAuthEnabled(true);
                cb.setOAuthConsumerKey(BuildConfig.TWITTER_CONSUMER_KEY);
                cb.setOAuthConsumerSecret(BuildConfig.TWITTER_CONSUMER_SECRET);
                try {
                    token = new TwitterFactory(cb.build()).getInstance().getOAuth2Token();
                    PrefsUtils.setTwitterToken(token.getAccessToken());
                } catch (Exception e) {
                    System.out.println("Can't get OAuth2 token from Twitter");
                    e.printStackTrace();
                }
            }
        };
        t.start();

    }

}
