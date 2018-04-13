package com.stonedog.gramophone.ui.fragments.mainactivity.remotehome.pager;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;

import com.stonedog.gramophone.ui.fragments.AbsMusicServiceFragment;
import com.stonedog.gramophone.ui.fragments.mainactivity.remotehome.RemoteHomeFragment;
import com.stonedog.gramophone.ui.fragments.AbsMusicServiceFragment;
import com.stonedog.gramophone.ui.fragments.mainactivity.remotehome.RemoteHomeFragment;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AbsLibraryPagerFragment extends AbsMusicServiceFragment {

    /* http://stackoverflow.com/a/2888433 */
    @Override
    public LoaderManager getLoaderManager() {
        return getParentFragment().getLoaderManager();
    }

    public RemoteHomeFragment getLibraryFragment() {
        return (RemoteHomeFragment) getParentFragment();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }
}
