package org.nv95.openmanga;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.nv95.openmanga.providers.MangaList;
import org.nv95.openmanga.providers.MangaProvider;

/**
 * Created by nv95 on 01.10.15.
 */
public class SearchActivity extends AppCompatActivity implements MangaListFragment.MangaListListener {
    private MangaListFragment listFragment;
    private String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        listFragment = new MangaListFragment();
        listFragment.setArguments(getIntent().getExtras());
        getFragmentManager().beginTransaction().add(R.id.frame_main, listFragment).commit();
        query = getIntent().getStringExtra("query");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setSubtitle(query);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public MangaList onListNeeded(MangaProvider provider, int page) throws Exception {
        return query == null ? MangaList.Empty() : provider.search(query, page);
    }

    @Override
    public String onEmptyList(MangaProvider provider) {
        return getString(R.string.no_manga_found);
    }
}
