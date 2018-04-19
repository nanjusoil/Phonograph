package com.stonedog.gramophone.ui.fragments.mainactivity.remotehome.pager;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;

import com.stonedog.gramophone.R;
import com.stonedog.gramophone.adapter.GenreAdapter;
import com.stonedog.gramophone.adapter.RemoteGenreAdapter;
import com.stonedog.gramophone.interfaces.LoaderIds;
import com.stonedog.gramophone.loader.GenreLoader;
import com.stonedog.gramophone.misc.WrappedAsyncTaskLoader;
import com.stonedog.gramophone.model.Genre;
import com.stonedog.gramophone.adapter.GenreAdapter;
import com.stonedog.gramophone.interfaces.LoaderIds;
import com.stonedog.gramophone.loader.GenreLoader;
import com.stonedog.gramophone.model.Genre;

import java.util.ArrayList;

public class GenresFragment extends AbsLibraryPagerRecyclerViewFragment<RemoteGenreAdapter, LinearLayoutManager> implements LoaderManager.LoaderCallbacks<ArrayList<Genre>> {

    public static final String TAG = GenresFragment.class.getSimpleName();

    private static final int LOADER_ID = LoaderIds.GENRES_FRAGMENT;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @NonNull
    @Override
    protected LinearLayoutManager createLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    @NonNull
    @Override
    protected RemoteGenreAdapter createAdapter() {
        ArrayList<Genre> dataSet = getAdapter() == null ? new ArrayList<Genre>() : getAdapter().getDataSet();
        return new RemoteGenreAdapter(getLibraryFragment().getMainActivity(), dataSet, R.layout.item_list_no_image);
    }

    @Override
    protected int getEmptyMessage() {
        return R.string.no_genres;
    }

    @Override
    public void onMediaStoreChanged() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<ArrayList<Genre>> onCreateLoader(int id, Bundle args) {
        return new GenresFragment.AsyncGenreLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Genre>> loader, ArrayList<Genre> data) {
        getAdapter().swapDataSet(data);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Genre>> loader) {
        getAdapter().swapDataSet(new ArrayList<Genre>());
    }

    private static class AsyncGenreLoader extends WrappedAsyncTaskLoader<ArrayList<Genre>> {
        public AsyncGenreLoader(Context context) {
            super(context);
        }

        @Override
        public ArrayList<Genre> loadInBackground() {
            Genre genre = new Genre(1, "種類", 30);
            ArrayList<Genre> genres= new ArrayList<Genre>();
            genres.add(genre);
            return genres;
            //return GenreLoader.getAllGenres(getContext());
        }
    }
}
