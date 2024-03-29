package com.stonedog.gramophone.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.stonedog.gramophone.model.Playlist;
import com.stonedog.gramophone.util.PreferenceUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.LongFunction;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RemotePlaylistLoader {

    static OkHttpClient client = new OkHttpClient();

    public static String get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    @NonNull
    public static ArrayList<Playlist> getAllPlaylists(@NonNull final Context context) {
        ArrayList<Playlist> playlists = new ArrayList<Playlist>();
        try {
            Gson gson = new Gson();
            String json = get(PreferenceUtil.getInstance(context).getRemoteAPIUrl() + "popularplaylists");
            Playlist[] playlistArray = gson.fromJson(json, Playlist[].class);
            for (Playlist playlist : playlistArray) {
                playlists.add(playlist);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (JsonParseException e) {
            e.printStackTrace();
            return null;
        }
        return playlists;
    }

    @NonNull
    public static Playlist getPlaylist(@NonNull final Context context, final int playlistId) {
        return getPlaylist(makePlaylistCursor(
                context,
                BaseColumns._ID + "=?",
                new String[]{
                        String.valueOf(playlistId)
                }
        ));
    }

    @NonNull
    public static Playlist getPlaylist(@NonNull final Context context, final String playlistName) {
        return getPlaylist(makePlaylistCursor(
                context,
                MediaStore.Audio.PlaylistsColumns.NAME + "=?",
                new String[]{
                        playlistName
                }
        ));
    }

    @NonNull
    public static Playlist getPlaylist(@Nullable final Cursor cursor) {
        Playlist playlist = new Playlist();

        if (cursor != null && cursor.moveToFirst()) {
            playlist = getPlaylistFromCursorImpl(cursor);
        }
        if (cursor != null)
            cursor.close();
        return playlist;
    }

    @NonNull
    public static ArrayList<Playlist> getAllPlaylists(@Nullable final Cursor cursor) {
        ArrayList<Playlist> playlists = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                playlists.add(getPlaylistFromCursorImpl(cursor));
            } while (cursor.moveToNext());
        }
        if (cursor != null)
            cursor.close();
        return playlists;
    }

    @NonNull
    private static Playlist getPlaylistFromCursorImpl(@NonNull final Cursor cursor) {
        final int id = cursor.getInt(0);
        final String name = cursor.getString(1);
        return new Playlist(id, name);
    }

    @Nullable
    public static Cursor makePlaylistCursor(@NonNull final Context context, final String selection, final String[] values) {
        try {
            return context.getContentResolver().query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                    new String[]{
                            /* 0 */
                            BaseColumns._ID,
                            /* 1 */
                            MediaStore.Audio.PlaylistsColumns.NAME
                    }, selection, values, MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER);
        } catch (SecurityException e) {
            return null;
        }
    }
}
