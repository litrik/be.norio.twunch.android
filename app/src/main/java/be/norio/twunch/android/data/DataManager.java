package be.norio.twunch.android.data;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import be.norio.twunch.android.data.model.Twunch;
import be.norio.twunch.android.data.model.Twunches;
import be.norio.twunch.android.otto.BusProvider;
import be.norio.twunch.android.otto.NetworkStatusUpdatedEvent;
import be.norio.twunch.android.otto.TwunchesAvailableEvent;
import be.norio.twunch.android.util.PrefsUtils;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.SimpleXMLConverter;
import retrofit.http.GET;

public class DataManager {

    private static DataManager instance = null;
    private final Context mContext;
    private final TwunchServer mServer;
    private final TwunchData mTwunchData;
    private int mOutstandingNetworkCalls = 0;

    public static DataManager getInstance() {
        if(instance == null) {
            instance = new DataManager();
        }
        return instance;
    }
    protected DataManager() {
        mContext = PrefsUtils.getContext();
        mServer = new RestAdapter.Builder().setServer("http://twunch.be/").setConverter(new SimpleXMLConverter()).build().create(TwunchServer.class);
        if (PrefsUtils.isDataAvailable()) {
            mTwunchData = PrefsUtils.getData();
        } else {
            mTwunchData = new TwunchData();
            loadTwunches();
        }
    }

    public Twunch getTwunch(String id) {
        final List<Twunch> twunches = mTwunchData.getTwunches();
        for (int i = 0; i < twunches.size(); i++) {
            final Twunch twunch =  twunches.get(i);
            if(id.equals(twunch.getId())) {
                return twunch;
            }
        }
        return null;
    }

    public List<Twunch> getTwunches() {
        return new ArrayList<Twunch>(mTwunchData.getTwunches());
    }

    public void loadTwunches() {
        incrementOutstandingNetworkCalls();
        mServer.loadTwunches(new Callback<Twunches>() {
            @Override
            public void success(Twunches twunches, Response response) {
                decrementOutstandingNetworkCalls();
                mTwunchData.setTwunches(twunches.twunches);
                BusProvider.getInstance().post(new TwunchesAvailableEvent(mTwunchData.getTwunches()));
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                decrementOutstandingNetworkCalls();
                retrofitError.printStackTrace();
            }
        });
    }


    private void incrementOutstandingNetworkCalls() {
        mOutstandingNetworkCalls++;
        BusProvider.getInstance().post(new NetworkStatusUpdatedEvent(mOutstandingNetworkCalls));
    }

    private void decrementOutstandingNetworkCalls() {
        mOutstandingNetworkCalls--;
        if (mOutstandingNetworkCalls == 0) {
            PrefsUtils.setData(mTwunchData);
        }
        BusProvider.getInstance().post(new NetworkStatusUpdatedEvent(mOutstandingNetworkCalls));
    }

    interface TwunchServer {
        @GET("/events.xml?when=future")
        void loadTwunches(Callback<Twunches> callback);
    }

}
