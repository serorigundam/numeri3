package tech.ketc.numeri.domain.twitter.client;

import android.support.annotation.NonNull;

import twitter4j.*;

public final class AvoidingAccessErrors {
    private AvoidingAccessErrors() {
        throw new UnsupportedOperationException();
    }

    public static void addStreamListener(@NonNull twitter4j.TwitterStream twitterStream, @NonNull UserStreamListener listener) {
        twitterStream.addListener(listener);
    }
}