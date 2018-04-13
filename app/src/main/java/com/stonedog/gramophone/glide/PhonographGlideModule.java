package com.stonedog.gramophone.glide;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.module.GlideModule;
import com.stonedog.gramophone.glide.artistimage.ArtistImage;
import com.stonedog.gramophone.glide.artistimage.ArtistImageLoader;
import com.stonedog.gramophone.glide.audiocover.AudioFileCover;
import com.stonedog.gramophone.glide.audiocover.AudioFileCoverLoader;
import com.stonedog.gramophone.glide.artistimage.ArtistImage;
import com.stonedog.gramophone.glide.artistimage.ArtistImageLoader;
import com.stonedog.gramophone.glide.audiocover.AudioFileCover;
import com.stonedog.gramophone.glide.audiocover.AudioFileCoverLoader;

import java.io.InputStream;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PhonographGlideModule implements GlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {

    }

    @Override
    public void registerComponents(Context context, Glide glide) {
        glide.register(AudioFileCover.class, InputStream.class, new AudioFileCoverLoader.Factory());
        glide.register(ArtistImage.class, InputStream.class, new ArtistImageLoader.Factory(context));
    }
}
