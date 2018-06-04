package com.novoda.noplayer.internal.exoplayer;

import android.content.Context;
import android.os.Handler;

import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.novoda.noplayer.NoPlayer;
import com.novoda.noplayer.internal.Heart;
import com.novoda.noplayer.internal.SystemClock;
import com.novoda.noplayer.internal.exoplayer.forwarder.ExoPlayerForwarder;
import com.novoda.noplayer.internal.exoplayer.mediasource.MediaSourceFactory;
import com.novoda.noplayer.internal.listeners.PlayerListenersHolder;
import com.novoda.noplayer.internal.utils.AndroidDeviceVersion;
import com.novoda.noplayer.model.LoadTimeout;

public class NoPlayerExoPlayerCreator {

    private final InternalCreator internalCreator;

    public static NoPlayerExoPlayerCreator newInstance(Handler handler) {
        InternalCreator internalCreator = new InternalCreator(handler);
        return new NoPlayerExoPlayerCreator(internalCreator);
    }

    NoPlayerExoPlayerCreator(InternalCreator internalCreator) {
        this.internalCreator = internalCreator;
    }

    public NoPlayer createExoPlayer(Context context, boolean downgradeSecureDecoder) {
        ExoPlayerTwoImpl player = internalCreator.create(context, downgradeSecureDecoder);
        player.initialise();
        return player;
    }

    static class InternalCreator {

        private final Handler handler;

        InternalCreator(Handler handler) {
            this.handler = handler;
        }

        ExoPlayerTwoImpl create(Context context, boolean downgradeSecureDecoder) {
            MediaSourceFactory mediaSourceFactory = new MediaSourceFactory(context, handler);

            MediaCodecSelector mediaCodecSelector = downgradeSecureDecoder
                    ? SecurityDowngradingCodecSelector.newInstance()
                    : MediaCodecSelector.DEFAULT;

            CompositeTrackSelectorCreator trackSelectorCreator = new CompositeTrackSelectorCreator();

            ExoPlayerCreator exoPlayerCreator = new ExoPlayerCreator(context);
            RendererTypeRequesterCreator rendererTypeRequesterCreator = new RendererTypeRequesterCreator();
            AndroidDeviceVersion androidDeviceVersion = AndroidDeviceVersion.newInstance();
            ExoPlayerFacade exoPlayerFacade = new ExoPlayerFacade(
                    androidDeviceVersion,
                    mediaSourceFactory,
                    trackSelectorCreator,
                    exoPlayerCreator,
                    rendererTypeRequesterCreator
            );

            PlayerListenersHolder listenersHolder = new PlayerListenersHolder();
            ExoPlayerForwarder exoPlayerForwarder = new ExoPlayerForwarder();
            LoadTimeout loadTimeout = new LoadTimeout(new SystemClock(), handler);
            Heart heart = Heart.newInstance(handler);

            return new ExoPlayerTwoImpl(
                    exoPlayerFacade,
                    listenersHolder,
                    exoPlayerForwarder,
                    loadTimeout,
                    heart,
                    mediaCodecSelector
            );
        }
    }
}
