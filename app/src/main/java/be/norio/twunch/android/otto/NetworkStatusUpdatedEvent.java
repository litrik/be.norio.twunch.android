package be.norio.twunch.android.otto;

public class NetworkStatusUpdatedEvent {

    private final int mOustandingNetworkCalls;

    public NetworkStatusUpdatedEvent(int oustandingNetworkCalls) {
        mOustandingNetworkCalls = oustandingNetworkCalls;
    }

    public int getOustandingNetworkCalls() {
        return mOustandingNetworkCalls;
    }
}
