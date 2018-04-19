package com.stonedog.gramophone.ui.fragments.mainactivity.remotehome.pager;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.AdapterView;

import com.stonedog.gramophone.R;
import com.stonedog.gramophone.adapter.PlaylistAdapter;
import com.stonedog.gramophone.adapter.RemotePlaylistAdapter;
import com.stonedog.gramophone.interfaces.LoaderIds;
import com.stonedog.gramophone.loader.PlaylistLoader;
import com.stonedog.gramophone.loader.RemotePlaylistLoader;
import com.stonedog.gramophone.misc.WrappedAsyncTaskLoader;
import com.stonedog.gramophone.model.Playlist;
import com.stonedog.gramophone.model.smartplaylist.HistoryPlaylist;
import com.stonedog.gramophone.model.smartplaylist.LastAddedPlaylist;
import com.stonedog.gramophone.model.smartplaylist.MyTopTracksPlaylist;
import com.stonedog.gramophone.adapter.PlaylistAdapter;
import com.stonedog.gramophone.loader.PlaylistLoader;
import com.stonedog.gramophone.model.Playlist;

import org.angmarch.views.NiceSpinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlaylistsFragment extends AbsLibraryPagerRecyclerViewFragment<RemotePlaylistAdapter, LinearLayoutManager> implements LoaderManager.LoaderCallbacks<ArrayList<Playlist>> {

    public static final String TAG = PlaylistsFragment.class.getSimpleName();

    private static final int LOADER_ID = LoaderIds.PLAYLISTS_FRAGMENT;

    @BindView(R.id.layout_swipe_refresh)
    SwipeRefreshLayout layoutSwipeRefresh;

    @BindView(R.id.nice_spinner)
    NiceSpinner niceSpinner;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, this);
        layoutSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getLoaderManager().restartLoader(LOADER_ID, null, PlaylistsFragment.this);
            }
        });

    }

    @NonNull
    @Override
    protected LinearLayoutManager createLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    @NonNull
    @Override
    protected RemotePlaylistAdapter createAdapter() {
        List<String> dataset = new LinkedList<>(Arrays.asList("One", "Two", "Three", "Four", "Five"));
        niceSpinner.attachDataSource(dataset);
        ArrayList<Playlist> dataSet = getAdapter() == null ? new ArrayList<Playlist>() : getAdapter().getDataSet();
        niceSpinner.addOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
        return new RemotePlaylistAdapter(getLibraryFragment().getMainActivity(), dataSet, R.layout.item_list_single_row, getLibraryFragment());
    }

    @Override
    protected int getEmptyMessage() {
        return R.string.no_playlists;
    }

    @Override
    public void onMediaStoreChanged() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<ArrayList<Playlist>> onCreateLoader(int id, Bundle args) {
        return new AsyncPlaylistLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Playlist>> loader, ArrayList<Playlist> data) {
        getAdapter().swapDataSet(data);
        layoutSwipeRefresh.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Playlist>> loader) {
        getAdapter().swapDataSet(new ArrayList<Playlist>());
    }

    private static class AsyncPlaylistLoader extends WrappedAsyncTaskLoader<ArrayList<Playlist>> {
        public AsyncPlaylistLoader(Context context) {
            super(context);
        }

        private static ArrayList<Playlist> getAllPlaylists(Context context) {
            ArrayList<Playlist> playlists = new ArrayList<>();

            //playlists.add(new LastAddedPlaylist(context));
            //playlists.add(new HistoryPlaylist(context));
            //playlists.add(new MyTopTracksPlaylist(context));

            playlists.addAll(RemotePlaylistLoader.getAllPlaylists(context));

            return playlists;
        }

        @Override
        public ArrayList<Playlist> loadInBackground() {
            return getAllPlaylists(getContext());
        }
    }
}
