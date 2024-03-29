package com.stonedog.gramophone.ui.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialcab.MaterialCab;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.stonedog.gramophone.R;
import com.stonedog.gramophone.adapter.song.SongAdapter;
import com.stonedog.gramophone.helper.MusicPlayerRemote;
import com.stonedog.gramophone.interfaces.CabHolder;
import com.stonedog.gramophone.interfaces.LoaderIds;
import com.stonedog.gramophone.loader.GenreLoader;
import com.stonedog.gramophone.misc.WrappedAsyncTaskLoader;
import com.stonedog.gramophone.model.Genre;
import com.stonedog.gramophone.model.Song;
import com.stonedog.gramophone.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.stonedog.gramophone.util.PhonographColorUtil;
import com.stonedog.gramophone.util.ViewUtil;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.stonedog.gramophone.adapter.song.SongAdapter;
import com.stonedog.gramophone.helper.MusicPlayerRemote;
import com.stonedog.gramophone.interfaces.CabHolder;
import com.stonedog.gramophone.interfaces.LoaderIds;
import com.stonedog.gramophone.loader.GenreLoader;
import com.stonedog.gramophone.model.Genre;
import com.stonedog.gramophone.model.Song;
import com.stonedog.gramophone.ui.activities.base.AbsSlidingMusicPanelActivity;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GenreDetailActivity extends AbsSlidingMusicPanelActivity implements CabHolder, LoaderManager.LoaderCallbacks<ArrayList<Song>> {

    public static final String TAG = GenreDetailActivity.class.getSimpleName();
    private static final int LOADER_ID = LoaderIds.GENRE_DETAIL_ACTIVITY;

    public static final String EXTRA_GENRE = "extra_genre";

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(android.R.id.empty)
    TextView empty;

    private Genre genre;

    private MaterialCab cab;
    private SongAdapter adapter;

    private RecyclerView.Adapter wrappedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDrawUnderStatusbar(true);
        ButterKnife.bind(this);

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        genre = getIntent().getExtras().getParcelable(EXTRA_GENRE);

        setUpRecyclerView();

        setUpToolBar();

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    protected View createContentView() {
        return wrapSlidingMusicPanel(R.layout.activity_genre_detail);
    }

    private void setUpRecyclerView() {
        ViewUtil.setUpFastScrollRecyclerViewColor(this, ((FastScrollRecyclerView) recyclerView), ThemeStore.accentColor(this));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SongAdapter(this, new ArrayList<Song>(), R.layout.item_list, false, this);
        recyclerView.setAdapter(adapter);

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkIsEmpty();
            }
        });
    }

    private void setUpToolBar() {
        toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(genre.name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_genre_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_shuffle_genre:
                MusicPlayerRemote.openAndShuffleQueue(adapter.getDataSet(), true);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    private void checkIsEmpty() {
        empty.setVisibility(
                adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE
        );
    }

    @Override
    protected void onDestroy() {
        if (recyclerView != null) {
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
        return new GenreDetailActivity.AsyncGenreSongLoader(this, genre);
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

    private static class AsyncGenreSongLoader extends WrappedAsyncTaskLoader<ArrayList<Song>> {
        private final Genre genre;

        public AsyncGenreSongLoader(Context context, Genre genre) {
            super(context);
            this.genre = genre;
        }

        @Override
        public ArrayList<Song> loadInBackground() {
            return GenreLoader.getSongs(getContext(), genre.id);
        }
    }
}