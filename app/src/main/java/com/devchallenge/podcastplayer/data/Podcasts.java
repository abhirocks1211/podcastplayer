package com.devchallenge.podcastplayer.data;

import android.content.Context;
import android.util.Log;

import com.devchallenge.podcastplayer.data.model.Podcast;
import com.devchallenge.podcastplayer.data.net.NetworkManager;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by yarolegovich on 13.09.2016.
 */
public class Podcasts {

    private static final String LOG_TAG = Podcast.class.getSimpleName();

    private static Podcasts instance;

    public static Podcasts getInstance() {
        return instance;
    }

    public static void init(Context context) {
        instance = new Podcasts(context);
    }

    private List<Podcast> podcasts;
    private Cache cache;

    public Podcasts(Context context) {
        this.podcasts = Collections.emptyList();
        this.cache = new Cache(context);
    }

    public Observable<List<Podcast>> getPodcasts() {
        return podcasts.isEmpty() ? cache.loadPodcastList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(this::setPodcasts)
                .onErrorResumeNext(
                        new NetworkManager().getRss()
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnNext(this::savePodcasts))
                : Observable.just(podcasts);
    }

    private void savePodcasts(List<Podcast> podcasts) {
        Log.d(LOG_TAG, podcasts.toString());
        setPodcasts(podcasts);
        cache.cachePodcastList(podcasts);
    }

    private void setPodcasts(List<Podcast> podcasts) {
        this.podcasts = podcasts;
    }

    public Podcast next(Podcast currentPodcast) {
        return podcasts.get((findIndex(currentPodcast) + 1) % podcasts.size());
    }

    public Podcast previous(Podcast currentPodcast) {
        int index = findIndex(currentPodcast);
        return podcasts.get(index != 0 ? index - 1 : podcasts.size() - 1);
    }

    private int findIndex(Podcast currentPodcast) {
        String title = currentPodcast.getTitle();
        for (int i = 0; i < podcasts.size(); i++) {
            if (podcasts.get(i).getTitle().equals(title)) {
                return i;
            }
        }
        return -1;
    }

}