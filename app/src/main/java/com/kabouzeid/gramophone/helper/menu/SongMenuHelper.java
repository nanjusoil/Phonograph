package com.kabouzeid.gramophone.helper.menu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.dialogs.AddToPlaylistDialog;
import com.kabouzeid.gramophone.dialogs.DeleteSongsDialog;
import com.kabouzeid.gramophone.dialogs.SongDetailDialog;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.interfaces.PaletteColorHolder;
import com.kabouzeid.gramophone.misc.UpdateToastMediaScannerCompletionListener;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.tageditor.AbsTagEditorActivity;
import com.kabouzeid.gramophone.ui.activities.tageditor.SongTagEditorActivity;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;

import java.io.File;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongMenuHelper {
    public static final int MENU_RES = R.menu.menu_item_song;

    public static boolean handleMenuClick(@NonNull FragmentActivity activity, @NonNull Song song, int menuItemId) {
        switch (menuItemId) {
            case R.id.action_download_song:
                if(song.data.startsWith("http")) {
                    String url = song.data;

                    String fileName = url.substring( url.lastIndexOf('/')+1, url.length() );
                    FileDownloader.getImpl().create(song.data)
                            .setPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + fileName)
                            .setListener(new FileDownloadListener() {
                                @Override
                                protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                                }

                                @Override
                                protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                                }

                                @Override
                                protected void completed(BaseDownloadTask task) {
                                    Context context = activity.getApplicationContext();
                                    String[] toBeScanned = {Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + fileName};
                                    MediaScannerConnection.scanFile(context, toBeScanned, null, context instanceof Activity ? new UpdateToastMediaScannerCompletionListener((Activity) context, toBeScanned) : null);
                                }

                                @Override
                                protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                                }

                                @Override
                                protected void error(BaseDownloadTask task, Throwable e) {
                                }

                                @Override
                                protected void warn(BaseDownloadTask task) {

                                }
                            })
                            .start();

                }
                return true;
            case R.id.action_set_as_ringtone:
                MusicUtil.setRingtone(activity, song.id);
                return true;
            case R.id.action_share:
                activity.startActivity(Intent.createChooser(MusicUtil.createShareSongFileIntent(song, activity), null));
                return true;
            case R.id.action_delete_from_device:
                DeleteSongsDialog.create(song).show(activity.getSupportFragmentManager(), "DELETE_SONGS");
                return true;
            case R.id.action_add_to_playlist:
                AddToPlaylistDialog.create(song).show(activity.getSupportFragmentManager(), "ADD_PLAYLIST");
                return true;
            case R.id.action_play_next:
                MusicPlayerRemote.playNext(song);
                return true;
            case R.id.action_add_to_current_playing:
                MusicPlayerRemote.enqueue(song);
                return true;
            case R.id.action_tag_editor:
                Intent tagEditorIntent = new Intent(activity, SongTagEditorActivity.class);
                tagEditorIntent.putExtra(AbsTagEditorActivity.EXTRA_ID, song.id);
                if (activity instanceof PaletteColorHolder)
                    tagEditorIntent.putExtra(AbsTagEditorActivity.EXTRA_PALETTE, ((PaletteColorHolder) activity).getPaletteColor());
                activity.startActivity(tagEditorIntent);
                return true;
            case R.id.action_details:
                SongDetailDialog.create(song).show(activity.getSupportFragmentManager(), "SONG_DETAILS");
                return true;
            case R.id.action_go_to_album:
                NavigationUtil.goToAlbum(activity, song.albumId);
                return true;
            case R.id.action_go_to_artist:
                NavigationUtil.goToArtist(activity, song.artistId);
                return true;
        }
        return false;
    }

    public static abstract class OnClickSongMenu implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {
        private AppCompatActivity activity;

        public OnClickSongMenu(@NonNull AppCompatActivity activity) {
            this.activity = activity;
        }

        public int getMenuRes() {
            return MENU_RES;
        }

        @Override
        public void onClick(View v) {
            PopupMenu popupMenu = new PopupMenu(activity, v);
            popupMenu.inflate(getMenuRes());
            popupMenu.setOnMenuItemClickListener(this);
            popupMenu.show();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            return handleMenuClick(activity, getSong(), item.getItemId());
        }

        public abstract Song getSong();
    }
}
