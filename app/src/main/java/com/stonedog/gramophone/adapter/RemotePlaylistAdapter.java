package com.stonedog.gramophone.adapter;

import android.graphics.PorterDuff;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.bumptech.glide.Glide;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.stonedog.gramophone.R;
import com.stonedog.gramophone.adapter.base.AbsMultiSelectAdapter;
import com.stonedog.gramophone.adapter.base.MediaEntryViewHolder;
import com.stonedog.gramophone.dialogs.ClearSmartPlaylistDialog;
import com.stonedog.gramophone.dialogs.DeletePlaylistDialog;
import com.stonedog.gramophone.helper.menu.PlaylistMenuHelper;
import com.stonedog.gramophone.helper.menu.SongsMenuHelper;
import com.stonedog.gramophone.interfaces.CabHolder;
import com.stonedog.gramophone.loader.PlaylistSongLoader;
import com.stonedog.gramophone.model.AbsCustomPlaylist;
import com.stonedog.gramophone.model.Playlist;
import com.stonedog.gramophone.model.Song;
import com.stonedog.gramophone.model.smartplaylist.AbsSmartPlaylist;
import com.stonedog.gramophone.model.smartplaylist.LastAddedPlaylist;
import com.stonedog.gramophone.util.MusicUtil;
import com.stonedog.gramophone.util.NavigationUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class RemotePlaylistAdapter extends AbsMultiSelectAdapter<RemotePlaylistAdapter.ViewHolder, Playlist> {

    public static final String TAG = RemotePlaylistAdapter.class.getSimpleName();

    private static final int SMART_PLAYLIST = 0;
    private static final int DEFAULT_PLAYLIST = 1;

    protected final AppCompatActivity activity;
    protected ArrayList<Playlist> dataSet;
    protected int itemLayoutRes;

    public RemotePlaylistAdapter(AppCompatActivity activity, ArrayList<Playlist> dataSet, @LayoutRes int itemLayoutRes, @Nullable CabHolder cabHolder) {
        super(activity, cabHolder, R.menu.menu_playlists_selection);
        this.activity = activity;
        this.dataSet = dataSet;
        this.itemLayoutRes = itemLayoutRes;
        setHasStableIds(true);
    }

    public ArrayList<Playlist> getDataSet() {
        return dataSet;
    }

    public void swapDataSet(ArrayList<Playlist> dataSet) {
        this.dataSet = dataSet;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return dataSet.get(position).id;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false);
        return createViewHolder(view, viewType);
    }

    protected ViewHolder createViewHolder(View view, int viewType) {
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Playlist playlist = dataSet.get(position);

        holder.itemView.setActivated(isChecked(playlist));

        if (holder.title != null) {
            holder.title.setText(playlist.name);
        }

        if (holder.getAdapterPosition() == getItemCount() - 1) {
            if (holder.shortSeparator != null) {
                holder.shortSeparator.setVisibility(View.GONE);
            }
        } else {
            if (holder.shortSeparator != null && !(dataSet.get(position) instanceof AbsSmartPlaylist)) {
                holder.shortSeparator.setVisibility(View.VISIBLE);
            }
        }

        if (holder.image != null) {
            Glide.with(activity).load("http://www.fnordware.com/superpng/pngtest16rgba.png").into(holder.image);
        }
    }

    private int getIconRes(Playlist playlist) {
        if (playlist instanceof AbsSmartPlaylist) {
            return ((AbsSmartPlaylist) playlist).iconRes;
        }
        return MusicUtil.isFavoritePlaylist(activity, playlist) ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_queue_music_white_24dp;
    }

    @Override
    public int getItemViewType(int position) {
        return dataSet.get(position) instanceof AbsSmartPlaylist ? SMART_PLAYLIST : DEFAULT_PLAYLIST;
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    protected Playlist getIdentifier(int position) {
        return dataSet.get(position);
    }

    @Override
    protected String getName(Playlist playlist) {
        return playlist.name;
    }

    @Override
    protected void onMultipleItemAction(@NonNull MenuItem menuItem, @NonNull ArrayList<Playlist> selection) {
        switch (menuItem.getItemId()) {
            case R.id.action_delete_playlist:
                for (int i = 0; i < selection.size(); i++) {
                    Playlist playlist = selection.get(i);
                    if (playlist instanceof AbsSmartPlaylist) {
                        AbsSmartPlaylist absSmartPlaylist = (AbsSmartPlaylist) playlist;
                        ClearSmartPlaylistDialog.create(absSmartPlaylist).show(activity.getSupportFragmentManager(), "CLEAR_PLAYLIST_" + absSmartPlaylist.name);
                        selection.remove(playlist);
                        i--;
                    }
                }
                if (selection.size() > 0) {
                    DeletePlaylistDialog.create(selection).show(activity.getSupportFragmentManager(), "DELETE_PLAYLIST");
                }
                break;
            default:
                SongsMenuHelper.handleMenuClick(activity, getSongList(selection), menuItem.getItemId());
                break;
        }
    }

    @NonNull
    private ArrayList<Song> getSongList(@NonNull List<Playlist> playlists) {
        final ArrayList<Song> songs = new ArrayList<>();
        for (Playlist playlist : playlists) {
            if (playlist instanceof AbsCustomPlaylist) {
                songs.addAll(((AbsCustomPlaylist) playlist).getSongs(activity));
            } else {
                songs.addAll(PlaylistSongLoader.getPlaylistSongList(activity, playlist.id));
            }
        }
        return songs;
    }

    public class ViewHolder extends MediaEntryViewHolder {

        public ViewHolder(@NonNull View itemView, int itemViewType) {
            super(itemView);

            if (itemViewType == SMART_PLAYLIST) {
                if (shortSeparator != null) {
                    shortSeparator.setVisibility(View.GONE);
                }
                itemView.setBackgroundColor(ATHUtil.resolveColor(activity, R.attr.cardBackgroundColor));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    itemView.setElevation(activity.getResources().getDimensionPixelSize(R.dimen.card_elevation));
                }
            }

            if (image != null) {
                int iconPadding = activity.getResources().getDimensionPixelSize(R.dimen.list_item_image_icon_padding);
                image.setPadding(iconPadding, iconPadding, iconPadding, iconPadding);
                image.setColorFilter(ATHUtil.resolveColor(activity, R.attr.iconColor), PorterDuff.Mode.SRC_IN);
            }

            if (menu != null) {
                menu.setOnClickListener(view -> {
                    final Playlist playlist = dataSet.get(getAdapterPosition());
                    final PopupMenu popupMenu = new PopupMenu(activity, view);
                    popupMenu.inflate(getItemViewType() == SMART_PLAYLIST ? R.menu.menu_item_smart_playlist : R.menu.menu_item_playlist);
                    if (playlist instanceof LastAddedPlaylist) {
                        popupMenu.getMenu().findItem(R.id.action_clear_playlist).setVisible(false);
                    }
                    popupMenu.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == R.id.action_clear_playlist) {
                            if (playlist instanceof AbsSmartPlaylist) {
                                ClearSmartPlaylistDialog.create((AbsSmartPlaylist) playlist).show(activity.getSupportFragmentManager(), "CLEAR_SMART_PLAYLIST_" + playlist.name);
                                return true;
                            }
                        }
                        return PlaylistMenuHelper.handleMenuClick(
                                activity, dataSet.get(getAdapterPosition()), item);
                    });
                    popupMenu.show();
                });
            }
        }

        @Override
        public void onClick(View view) {
            if (isInQuickSelectMode()) {
                toggleChecked(getAdapterPosition());
            } else {
                Playlist playlist = dataSet.get(getAdapterPosition());
                Log.v("QAQ", "Position: " + playlist.id);
                NavigationUtil.goToRemotePlaylist(activity, playlist);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            toggleChecked(getAdapterPosition());
            return true;
        }
    }
}
