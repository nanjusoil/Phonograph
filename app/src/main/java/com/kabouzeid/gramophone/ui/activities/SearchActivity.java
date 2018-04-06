package com.kabouzeid.gramophone.ui.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.SearchAdapter;
import com.kabouzeid.gramophone.interfaces.LoaderIds;
import com.kabouzeid.gramophone.loader.AlbumLoader;
import com.kabouzeid.gramophone.loader.ArtistLoader;
import com.kabouzeid.gramophone.loader.SongLoader;
import com.kabouzeid.gramophone.misc.WrappedAsyncTaskLoader;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.base.AbsMusicServiceActivity;
import com.kabouzeid.gramophone.util.PreferenceUtil;
import com.kabouzeid.gramophone.util.Util;
import com.liulishuo.filedownloader.FileDownloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchActivity extends AbsMusicServiceActivity implements SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<List<Object>> {
    public static final String TAG = SearchActivity.class.getSimpleName();
    public static final String QUERY = "query";
    private static final int LOADER_ID = LoaderIds.SEARCH_ACTIVITY;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(android.R.id.empty)
    TextView empty;

    SearchView searchView;

    private SearchAdapter adapter;
    private String query;
    ProgressDialog progress;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setDrawUnderStatusbar(true);
        ButterKnife.bind(this);

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchAdapter(this, Collections.emptyList());
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                empty.setVisibility(adapter.getItemCount() < 1 ? View.VISIBLE : View.GONE);
            }
        });
        recyclerView.setAdapter(adapter);

        recyclerView.setOnTouchListener((v, event) -> {
            hideSoftKeyboard();
            return false;
        });

        setUpToolBar();

        if (savedInstanceState != null) {
            query = savedInstanceState.getString(QUERY);
        }

        if( PreferenceUtil.getInstance(this).getRemoteAPIUrl() == ""){
            Toast.makeText(this , R.string.empty_remote_api_url , Toast.LENGTH_LONG);
            onBackPressed();
        }

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        progress = new ProgressDialog(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(QUERY, query);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void setUpToolBar() {
        toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        final MenuItem searchItem = menu.findItem(R.id.search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setSubmitButtonEnabled(true);

        MenuItemCompat.expandActionView(searchItem);
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                onBackPressed();
                return false;
            }
        });

        searchView.setQuery(query, false);
        searchView.post(() -> searchView.setOnQueryTextListener(SearchActivity.this));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void search(@NonNull String query) {
        this.query = query;
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public void onMediaStoreChanged() {
        super.onMediaStoreChanged();
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        search(query);
        hideSoftKeyboard();
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    private void hideSoftKeyboard() {
        Util.hideSoftKeyboard(SearchActivity.this);
        if (searchView != null) {
            searchView.clearFocus();
        }
    }

    @Override
    public Loader<List<Object>> onCreateLoader(int id, Bundle args) {
        return new AsyncSearchResultLoader(this, query);
    }

    @Override
    public void onLoadFinished(Loader<List<Object>> loader, List<Object> data) {
        progress.dismiss();
        adapter.swapDataSet(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Object>> loader) {
        adapter.swapDataSet(Collections.emptyList());
    }

    private static class AsyncSearchResultLoader extends WrappedAsyncTaskLoader<List<Object>> {
        private final String query;
        OkHttpClient client = new OkHttpClient();


        public AsyncSearchResultLoader(Context context, String query) {
            super(context);
            this.query = query;
        }


        public String get(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();
            return response.body().string();
        }

        @Override
        public List<Object> loadInBackground() {

            List<Object> results = new ArrayList<>();
            if (!TextUtils.isEmpty(query)) {
                List songs = SongLoader.getSongs(getContext(), query);
                FileDownloader.setup(getContext());

                try {
                    Gson gson = new Gson();
                    String json = get(PreferenceUtil.getInstance(getContext()).getRemoteAPIUrl() + "search?song=" +query);
                    Song[] musicArray = gson.fromJson(json, Song[].class);
                    for(Song music : musicArray){
                        songs.add(music);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (!songs.isEmpty()) {
                    results.add(getContext().getResources().getString(R.string.songs));
                    results.addAll(songs);
                }
                List artists = ArtistLoader.getArtists(getContext(), query);
                if (!artists.isEmpty()) {
                    results.add(getContext().getResources().getString(R.string.artists));
                    results.addAll(artists);
                }

                List albums = AlbumLoader.getAlbums(getContext(), query);
                if (!albums.isEmpty()) {
                    results.add(getContext().getResources().getString(R.string.albums));
                    results.addAll(albums);
                }
            }
            return results;
        }
    }

}
