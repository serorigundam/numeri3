package net.ketc.numeri.domain.service;

import android.support.annotation.NonNull;

import twitter4j.TwitterStream;
import twitter4j.UserStreamListener;

public final class AvoidingAccessErrors {
    private AvoidingAccessErrors() {
        throw new UnsupportedOperationException();
    }

    public static void addStreamListener(@NonNull TwitterStream twitterStream, @NonNull UserStreamListener listener) {
        twitterStream.addListener(listener);
    }
}

