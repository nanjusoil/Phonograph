package com.stonedog.gramophone.ui.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialcab.MaterialCab;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.stonedog.gramophone.R;
import com.stonedog.gramophone.adapter.song.OrderablePlaylistSongAdapter;
import com.stonedog.gramophone.adapter.song.PlaylistSongAdapter;
import com.stonedog.gramophone.adapter.song.SongAdapter;
import com.stonedog.gramophone.helper.MusicPlayerRemote;
import com.stonedog.gramophone.helper.menu.PlaylistMenuHelper;
import com.stonedog.gramophone.interfaces.CabHolder;
import com.stonedog.gramophone.interfaces.LoaderIds;
import com.stonedog.gramophone.loader.PlaylistLoader;
import com.stonedog.gramophone.loader.PlaylistSongLoader;
import com.stonedog.gramophone.misc.WrappedAsyncTaskLoader;
import com.stonedog.gramophone.model.AbsCustomPlaylist;
import com.stonedog.gramophone.model.Playlist;
import com.stonedog.gramophone.model.PlaylistSong;
import com.stonedog.gramophone.model.Song;
import com.stonedog.gramophone.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.stonedog.gramophone.util.PhonographColorUtil;
import com.stonedog.gramophone.util.PlaylistsUtil;
import com.stonedog.gramophone.util.PreferenceUtil;
import com.stonedog.gramophone.util.ViewUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RemotePlaylistDetailActivity extends AbsSlidingMusicPanelActivity implements CabHolder, LoaderManager.LoaderCallbacks<ArrayList<Song>> {

    public static final String TAG = RemotePlaylistDetailActivity.class.getSimpleName();

    private static final int LOADER_ID = LoaderIds.PLAYLIST_DETAIL_ACTIVITY;

    @NonNull
    public static String EXTRA_PLAYLIST = "extra_playlist";

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(android.R.id.empty)
    TextView empty;

    private Playlist playlist;

    private MaterialCab cab;
    private SongAdapter adapter;

    private RecyclerView.Adapter wrappedAdapter;
    private RecyclerViewDragDropManager recyclerViewDragDropManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("QAQ", "inside remote playlist");
        setDrawUnderStatusbar(true);
        ButterKnife.bind(this);

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        playlist = getIntent().getExtras().getParcelable(EXTRA_PLAYLIST);
        Log.v("QAQ", Integer.toString(playlist.id));

        setUpRecyclerView();

        setUpToolbar();

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    protected View createContentView() {
        return wrapSlidingMusicPanel(R.layout.activity_playlist_detail);
    }

    private void setUpRecyclerView() {
        ViewUtil.setUpFastScrollRecyclerViewColor(this, ((FastScrollRecyclerView) recyclerView), ThemeStore.accentColor(this));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (true) {
            adapter = new PlaylistSongAdapter(this, new ArrayList<Song>(), R.layout.item_list, false, this);
            recyclerView.setAdapter(adapter);
        } else {
            recyclerViewDragDropManager = new RecyclerViewDragDropManager();
            final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();
            adapter = new OrderablePlaylistSongAdapter(this, new ArrayList<PlaylistSong>(), R.layout.item_list, false, this, (fromPosition, toPosition) -> {
                if (PlaylistsUtil.moveItem(RemotePlaylistDetailActivity.this, playlist.id, fromPosition, toPosition)) {
                    Song song = adapter.getDataSet().remove(fromPosition);
                    adapter.getDataSet().add(toPosition, song);
                    adapter.notifyItemMoved(fromPosition, toPosition);
                }
            });
            wrappedAdapter = recyclerViewDragDropManager.createWrappedAdapter(adapter);

            recyclerView.setAdapter(wrappedAdapter);
            recyclerView.setItemAnimator(animator);

            recyclerViewDragDropManager.attachRecyclerView(recyclerView);
        }

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkIsEmpty();
            }
        });
    }

    private void setUpToolbar() {
        toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setToolbarTitle(playlist.name);
    }

    private void setToolbarTitle(String title) {
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(playlist instanceof AbsCustomPlaylist ? R.menu.menu_smart_playlist_detail : R.menu.menu_playlist_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_shuffle_playlist:
                MusicPlayerRemote.openAndShuffleQueue(adapter.getDataSet(), true);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return PlaylistMenuHelper.handleMenuClick(this, playlist, item);
    }

    @NonNull
    @Override
    public MaterialCab openCab(final int menu, final MaterialCab.Callback callback) {
        if (cab != null && cab.isActive()) cab.finish();
        cab = new MaterialCab(this, R.id.cab_stub)
                .setMenu(menu)
                .setCloseDrawableRes(R.drawable.ic_close_white_24dp)
                .setBackgroundColor(PhonographColorUtil.shiftBackgroundColorForLightText(ThemeStore.primaryColor(this)))
                .start(callback);
        return cab;
    }

    @Override
    public void onBackPressed() {
        if (cab != null && cab.isActive()) cab.finish();
        else {
            recyclerView.stopScroll();
            super.onBackPressed();
        }
    }

    @Override
    public void onMediaStoreChanged() {
        super.onMediaStoreChanged();

        if (!(playlist instanceof AbsCustomPlaylist)) {
            // Playlist deleted
            if (!PlaylistsUtil.doesPlaylistExist(this, playlist.id)) {
                finish();
                return;
            }

            // Playlist renamed
            final String playlistName = PlaylistsUtil.getNameForPlaylist(this, playlist.id);
            if (!playlistName.equals(playlist.name)) {
                playlist = PlaylistLoader.getPlaylist(this, playlist.id);
                setToolbarTitle(playlist.name);
            }
        }

        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    private void checkIsEmpty() {
        empty.setVisibility(
                adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE
        );
    }

    @Override
    public void onPause() {
        if (recyclerViewDragDropManager != null) {
            recyclerViewDragDropManager.cancelDrag();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (recyclerViewDragDropManager != null) {
            recyclerViewDragDropManager.release();
            recyclerViewDragDropManager = null;
        }

        if (recyclerView != null) {
            recyclerView.setItemAnimator(null);
            recyclerView.setAdapter(null);
            recyclerView = null;
        }

        if (wrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(wrappedAdapter);
            wrappedAdapter = null;
        }
        adapter = null;

        super.onDestroy();
    }

    @Override
    public Loader<ArrayList<Song>> onCreateLoader(int id, Bundle args) {
        return new AsyncPlaylistSongLoader(this, playlist);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Song>> loader, ArrayList<Song> data) {
        if (adapter != null)
            adapter.swapDataSet(data);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Song>> loader) {
        if (adapter != null)
            adapter.swapDataSet(new ArrayList<Song>());
    }

    private static class AsyncPlaylistSongLoader extends WrappedAsyncTaskLoader<ArrayList<Song>> {
        private final Playlist playlist;
        OkHttpClient client = new OkHttpClient();

        public String get(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();
            return response.body().string();
        }


        public AsyncPlaylistSongLoader(Context context, Playlist playlist) {
            super(context);
            this.playlist = playlist;
        }

        @Override
        public ArrayList<Song> loadInBackground() {
            if (playlist instanceof AbsCustomPlaylist) {
                return ((AbsCustomPlaylist) playlist).getSongs(getContext());
            } else {
                ArrayList<Song> SongArrayList = new ArrayList<Song>();
                try {
                    Gson gson = new Gson();
                    String json = get(PreferenceUtil.getInstance(getContext()).getRemoteAPIUrl() + "playlist?id=" + playlist.id);
                    Song[] musicArray = gson.fromJson(json, Song[].class);
                    for(Song music : musicArray){
                        SongArrayList.add(music);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JsonParseException e) {
                    e.printStackTrace();
                }

                return SongArrayList;
            }
        }
    }
}
