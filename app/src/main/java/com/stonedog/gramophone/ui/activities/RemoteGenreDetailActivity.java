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
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
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

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RemoteGenreDetailActivity extends AbsSlidingMusicPanelActivity implements CabHolder, LoaderManager.LoaderCallbacks<ArrayList<Song>> {

    public static final String TAG = RemoteGenreDetailActivity.class.getSimpleName();
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
        return new RemoteGenreDetailActivity.AsyncGenreSongLoader(this, genre);
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
        OkHttpClient client = new OkHttpClient();

        public String get(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();
            return response.body().string();
        }

        public AsyncGenreSongLoader(Context context, Genre genre) {
            super(context);
            this.genre = genre;
        }

        @Override
        public ArrayList<Song> loadInBackground() {
            ArrayList<Song> songs= new ArrayList<Song>();
            try {
                Gson gson = new Gson();
                //String json = get(PreferenceUtil.getInstance(getContext()).getRemoteAPIUrl() + "popularsongs");
                String json = "[{\"id\":213454635,\"title\":\"愛了很久的朋友\",\"data\":\"http://139.162.98.238/data/213454635_000LJm9W4NIrCB.mp3\",\"albumName\":\"愛了很久的朋友\",\"albumId\":3974055,\"artistName\":\"田馥甄\"},{\"id\":213366580,\"title\":\"拿下這一場\",\"data\":\"http://139.162.98.238/data/213366580_002lKDNu0be2I1.mp3\",\"albumName\":\"拿下這一場\",\"albumId\":3960228,\"artistName\":\"宇宙人\"},{\"id\":213432684,\"title\":\"我依然是我\",\"data\":\"http://139.162.98.238/data/213432684_001mmRil2Qmv0g.mp3\",\"albumName\":\"我依然是我\",\"albumId\":3970847,\"artistName\":\"王笠人\"},{\"id\":213392050,\"title\":\"和你在一起\",\"data\":\"http://139.162.98.238/data/213392050_004CavPO1OPRxH.mp3\",\"albumName\":\"和你在一起\",\"albumId\":3964886,\"artistName\":\"汪蘇瀧\"},{\"id\":213446637,\"title\":\"半句再見\",\"data\":\"http://139.162.98.238/data/213446637_0042ih853gboG4.mp3\",\"albumName\":\"半句再見\",\"albumId\":3972762,\"artistName\":\"孫燕姿\"},{\"id\":213493655,\"title\":\"我們\",\"data\":\"http://139.162.98.238/data/213493655_002Ce1kE4crzRK.mp3\",\"albumName\":\"我們\",\"albumId\":3979999,\"artistName\":\"陳奕迅\"},{\"id\":213417001,\"title\":\"自然凋謝\",\"data\":\"http://139.162.98.238/data/213417001_000bWNRf16WN6N.mp3\",\"albumName\":\"自然凋謝\",\"albumId\":3968574,\"artistName\":\"孫盛希\"},{\"id\":213338612,\"title\":\"越界\",\"data\":\"http://139.162.98.238/data/213338612_002bnX7t2LBxFY.mp3\",\"albumName\":\"HIStory2原聲帶 越界\",\"albumId\":3955478,\"artistName\":\"李振源\"},{\"id\":213405700,\"title\":\"說散就散 (官方對唱版)\",\"data\":\"http://139.162.98.238/data/213405700_002iRqbo43fGUk.mp3\",\"albumName\":\"說散就散（官方對唱版）\",\"albumId\":3966719,\"artistName\":\"潘嘉麗\"},{\"id\":213443918,\"title\":\"從來不知道\",\"data\":\"http://139.162.98.238/data/213443918_00145ccY0jXFZX.mp3\",\"albumName\":\"從來不知道\",\"albumId\":3972353,\"artistName\":\"畢書盡\"},{\"id\":213099389,\"title\":\"為了等候你\",\"data\":\"http://139.162.98.238/data/213099389_003VXvJ50pmw5O.mp3\",\"albumName\":\"為了等候你\",\"albumId\":3916040,\"artistName\":\"蔡佩軒\"},{\"id\":213337723,\"title\":\"失重\",\"data\":\"http://139.162.98.238/data/213337723_002rusOC4Rzy5E.mp3\",\"albumName\":\"老男孩 電視劇原聲帶\",\"albumId\":3972768,\"artistName\":\"鬱可唯\"},{\"id\":213385673,\"title\":\"不老男孩\",\"data\":\"http://139.162.98.238/data/213385673_003qlsTj0CFdCM.mp3\",\"albumName\":\"老男孩 電視劇原聲帶\",\"albumId\":3972768,\"artistName\":\"古巨基\"},{\"id\":213341426,\"title\":\"This is how we said goodbye\",\"data\":\"http://139.162.98.238/data/213341426_001lyOi80q1yk5.mp3\",\"albumName\":\"This is how we said goodbye\",\"albumId\":3955872,\"artistName\":\"李玉璽\"},{\"id\":213473240,\"title\":\"兩難\",\"data\":\"http://139.162.98.238/data/213473240_000JYEcO00EHD4.mp3\",\"albumName\":\"南方有喬木 電視劇原聲帶\",\"albumId\":3976784,\"artistName\":\"汪蘇瀧\"},{\"id\":213079190,\"title\":\"不染\",\"data\":\"http://139.162.98.238/data/213079190_001xlfsG2eVHUf.mp3\",\"albumName\":\"香蜜沉沉燼如霜 電視原聲音樂專輯\",\"albumId\":3912338,\"artistName\":\"毛不易\"},{\"id\":213376823,\"title\":\"一樣的月光\",\"data\":\"http://139.162.98.238/data/213376823_003CCCbb0VIJGQ.mp3\",\"albumName\":\"一樣的月光\",\"albumId\":3961985,\"artistName\":\"丁當\"},{\"id\":213340144,\"title\":\"好聚好散\",\"data\":\"http://139.162.98.238/data/213340144_001fLav12Tt0r9.mp3\",\"albumName\":\"好聚好散\",\"albumId\":3955712,\"artistName\":\"王櫟鑫\"},{\"id\":213323697,\"title\":\"易燃易爆炸 (Live)\",\"data\":\"http://139.162.98.238/data/213323697_000t9DlU2m3SSI.mp3\",\"albumName\":\"歌手第二季 第8期\",\"albumId\":3952768,\"artistName\":\"華晨宇\"},{\"id\":213440970,\"title\":\"陽光地中海\",\"data\":\"http://139.162.98.238/data/213440970_0008uXBY1qTx2h.mp3\",\"albumName\":\"陽光地中海\",\"albumId\":3971802,\"artistName\":\"韋禮安\"},{\"id\":203770435,\"title\":\"當我們聊到未來的時候，未來就過去了\",\"data\":\"http://139.162.98.238/data/203770435_004b9PfS4V7ceQ.mp3\",\"albumName\":\"當我們聊到未來的時候，未來就過去了\",\"albumId\":2226771,\"artistName\":\"滿江\"},{\"id\":213389605,\"title\":\"你不會懂\",\"data\":\"http://139.162.98.238/data/213389605_004Ice1i48z6dY.mp3\",\"albumName\":\"你不會懂\",\"albumId\":3964380,\"artistName\":\"鍾欣潼\"},{\"id\":213429092,\"title\":\"百里守約\",\"data\":\"http://139.162.98.238/data/213429092_000tKvfS40Lwjy.mp3\",\"albumName\":\"百里守約\",\"albumId\":3970246,\"artistName\":\"蕭敬騰\"},{\"id\":213701399,\"title\":\"終於愛情\",\"data\":\"http://139.162.98.238/data/213701399_0011JFrB3mdMJv.mp3\",\"albumName\":\"終於愛情\",\"albumId\":4010802,\"artistName\":\"Ella\"},{\"id\":213392425,\"title\":\"就這麼錯過\",\"data\":\"http://139.162.98.238/data/213392425_000JEF8y1Q3fbf.mp3\",\"albumName\":\"翻牆的記憶 電視原聲帶\",\"albumId\":3964946,\"artistName\":\"馬仕釗\"},{\"id\":213331778,\"title\":\"早應該\",\"data\":\"http://139.162.98.238/data/213331778_003M3XDK22HI0T.mp3\",\"albumName\":\"早應該\",\"albumId\":3954414,\"artistName\":\"李毓芬\"},{\"id\":213429169,\"title\":\"SWAG午覺\",\"data\":\"http://139.162.98.238/data/213429169_001EFTva1eThQR.mp3\",\"albumName\":\"SWAG午覺\",\"albumId\":3970272,\"artistName\":\"異鄉人（劉行寬）\"},{\"id\":213366023,\"title\":\"至少還有你\",\"data\":\"http://139.162.98.238/data/213366023_003Lrsou1N6OL0.mp3\",\"albumName\":\"至少還有你\",\"albumId\":3960178,\"artistName\":\"文慧如\"},{\"id\":213346208,\"title\":\"我愛你彭拉昆\",\"data\":\"http://139.162.98.238/data/213346208_002Q5o3O33NAQX.mp3\",\"albumName\":\"我愛你彭拉昆\",\"albumId\":3956599,\"artistName\":\"那對夫妻\"},{\"id\":213144339,\"title\":\"累了就靠著我\",\"data\":\"http://139.162.98.238/data/213144339_001YEZPz1wnDWV.mp3\",\"albumName\":\"彩虹六部曲《第一次》OST\",\"albumId\":3921660,\"artistName\":\"鬱採真\"},{\"id\":213515402,\"title\":\"我懂你的獨特\",\"data\":\"http://139.162.98.238/data/213515402_001JO8lB4UAecy.mp3\",\"albumName\":\"我懂你的獨特\",\"albumId\":3982658,\"artistName\":\"朱俐靜\"},{\"id\":213392430,\"title\":\"枷鎖\",\"data\":\"http://139.162.98.238/data/213392430_001KDoyu0D5W5m.mp3\",\"albumName\":\"翻牆的記憶 電視原聲帶\",\"albumId\":3964946,\"artistName\":\"ØZI\"},{\"id\":212628854,\"title\":\"123我愛你\",\"data\":\"http://139.162.98.238/data/212628854_002WqezQ4dmIeT.mp3\",\"albumName\":\"123我愛你\",\"albumId\":3835228,\"artistName\":\"新樂塵符\"},{\"id\":213354901,\"title\":\"你愛她\",\"data\":\"http://139.162.98.238/data/213354901_004YJ4tH2a4Vt2.mp3\",\"albumName\":\"你愛她\",\"albumId\":3958363,\"artistName\":\"張粹方\"},{\"id\":213383799,\"title\":\"Don't Cry Don't Cry\",\"data\":\"http://139.162.98.238/data/213383799_001FclZh2pjl7n.mp3\",\"albumName\":\"Don't Cry Don't Cry\",\"albumId\":3963256,\"artistName\":\"魏如萱\"},{\"id\":213070964,\"title\":\"沒離開過\",\"data\":\"http://139.162.98.238/data/213070964_0037LsF54XapTO.mp3\",\"albumName\":\"白羊\",\"albumId\":3910922,\"artistName\":\"陳小予\"},{\"id\":213448330,\"title\":\"最想去的地方\",\"data\":\"http://139.162.98.238/data/213448330_003kkA6I3zyxta.mp3\",\"albumName\":\"最想去的地方\",\"albumId\":3973018,\"artistName\":\"炎亞綸\"},{\"id\":203100036,\"title\":\"醉流年\",\"data\":\"http://139.162.98.238/data/203100036_001mIk7G116plU.mp3\",\"albumName\":\"醉流年\",\"albumId\":2148622,\"artistName\":\"新樂塵符\"},{\"id\":213428982,\"title\":\"我的驕傲\",\"data\":\"http://139.162.98.238/data/213428982_003qxKPO46B9sy.mp3\",\"albumName\":\"我的驕傲\",\"albumId\":3981335,\"artistName\":\"林志穎\"},{\"id\":213416979,\"title\":\"我的寶貝\",\"data\":\"http://139.162.98.238/data/213416979_00156J6Y4KqWYp.mp3\",\"albumName\":\"別來無恙\",\"albumId\":3968564,\"artistName\":\"賴慈泓\"},{\"id\":213371268,\"title\":\"奇奇怪怪\",\"data\":\"http://139.162.98.238/data/213371268_003NPRt60XLlBR.mp3\",\"albumName\":\"奇奇怪怪\",\"albumId\":3960876,\"artistName\":\"魏妙如\"},{\"id\":213468338,\"title\":\"平凡之路 (Live)\",\"data\":\"http://139.162.98.238/data/213468338_003suD9L2tvMnZ.mp3\",\"albumName\":\"歌手第二季 第11期\",\"albumId\":3975892,\"artistName\":\"華晨宇\"},{\"id\":213481302,\"title\":\"遠去的飛鳥\",\"data\":\"http://139.162.98.238/data/213481302_001Q1yqo1Wnsgd.mp3\",\"albumName\":\"柔軟\",\"albumId\":3971856,\"artistName\":\"房東的貓\"},{\"id\":213482573,\"title\":\"飄\",\"data\":\"http://139.162.98.238/data/213482573_004FFWcN1Qr4wU.mp3\",\"albumName\":\"金牌投資人 電視原聲帶\",\"albumId\":3978419,\"artistName\":\"蘇妙玲\"},{\"id\":104873731,\"title\":\"Vapor\",\"data\":\"http://139.162.98.238/data/104873731_001Kk2QF0xyF2M.mp3\",\"albumName\":\"Sounds Good Feels Good (Deluxe)\",\"albumId\":1066260,\"artistName\":\"5 Seconds Of Summer\"},{\"id\":213354718,\"title\":\"別廢話\",\"data\":\"http://139.162.98.238/data/213354718_000roUkT2jZ8ky.mp3\",\"albumName\":\"別廢話\",\"albumId\":3958235,\"artistName\":\"袁婭維\"},{\"id\":104879630,\"title\":\"菊次郎的夏天\",\"data\":\"http://139.162.98.238/data/104879630_000UXWb936yYmO.mp3\",\"albumName\":\"綠色花園\",\"albumId\":1191935,\"artistName\":\"群星\"},{\"id\":109746418,\"title\":\"天使也會受傷\",\"data\":\"http://139.162.98.238/data/109746418_003hYMji4VHUcp.mp3\",\"albumName\":\"One Thing\",\"albumId\":1768453,\"artistName\":\"小男孩樂團\"}]";
                Song[] songArray = gson.fromJson(json, Song[].class);
                for(Song song : songArray){
                    songs.add(song);
                }
            } catch (JsonParseException e) {
                e.printStackTrace();
            }
            return songs;
            //return GenreLoader.getSongs(getContext(), genre.id);
        }
    }
}