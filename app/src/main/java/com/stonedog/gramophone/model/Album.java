package com.stonedog.gramophone.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class Album implements Parcelable {
    public final ArrayList<Song> songs;
    public final int remoteId;
    public final String remoteName;

    public Album(ArrayList<Song> songs) {
        this.songs = songs;
        this.remoteId = 0;
        this.remoteName = "";

    }

    public Album() {
        this.songs = new ArrayList<>();
        this.remoteId = 0;
        this.remoteName = "";
    }

    public Album(int id, String name) {
        this.songs = new ArrayList<>();
        this.remoteId = id;
        this.remoteName = name;
    }
    public int getId() {
        if(this.remoteId == 0) {
            return safeGetFirstSong().albumId;
        }else{
            return this.remoteId;
        }
    }

    public String getTitle() {
        if(this.remoteName.isEmpty()){
            return safeGetFirstSong().albumName;
        }else {
            return this.remoteName;
        }
    }

    public int getArtistId() {
        return safeGetFirstSong().artistId;
    }

    public String getArtistName() {
        return safeGetFirstSong().artistName;
    }

    public int getYear() {
        return safeGetFirstSong().year;
    }

    public long getDateModified() {
        return safeGetFirstSong().dateModified;
    }

    public int getSongCount() {
        return songs.size();
    }

    @NonNull
    public Song safeGetFirstSong() {
        return songs.isEmpty() ? Song.EMPTY_SONG : songs.get(0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Album that = (Album) o;

        return songs != null ? songs.equals(that.songs) : that.songs == null;

    }

    @Override
    public int hashCode() {
        return songs != null ? songs.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Album{" +
                "songs=" + songs +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(songs);
    }

    protected Album(Parcel in) {
        this.songs = in.createTypedArrayList(Song.CREATOR);
        this.remoteId = in.readInt();
        this.remoteName = in.readString();
    }

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        public Album createFromParcel(Parcel source) {
            return new Album(source);
        }

        public Album[] newArray(int size) {
            return new Album[size];
        }
    };
}
