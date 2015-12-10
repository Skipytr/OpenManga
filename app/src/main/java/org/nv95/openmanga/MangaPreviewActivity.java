package org.nv95.openmanga;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.nv95.openmanga.components.AdvancedScrollView;
import org.nv95.openmanga.providers.FavouritesProvider;
import org.nv95.openmanga.providers.HistoryProvider;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.MangaInfo;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.MangaSummary;
import org.nv95.openmanga.providers.SaveService;

/**
 * Created by nv95 on 30.09.15.
 */
public class MangaPreviewActivity extends AppCompatActivity implements View.OnClickListener,
        DialogInterface.OnClickListener, AdvancedScrollView.OnScrollListener {

    protected MangaSummary mangaSummary;
    private ImageView imageView;
    private ImageView readButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        mangaSummary = new MangaSummary(new MangaInfo(getIntent().getExtras()));
        imageView = (ImageView) findViewById(R.id.imageView);
        readButton = (ImageView) findViewById(R.id.ab_read);
        ((TextView) findViewById(R.id.textView_title)).setText(mangaSummary.getName());
        ((TextView)findViewById(R.id.textView_summary)).setText(mangaSummary.getSummary());
        readButton.setOnClickListener(this);
        AdvancedScrollView scrollView = (AdvancedScrollView) findViewById(R.id.scrollView);
        if (scrollView != null) {
            scrollView.setOnScrollListener(this);
        }
        new ImageLoadTask(imageView ,mangaSummary.getPreview(), false, new ColorDrawable(Color.TRANSPARENT)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new LoadInfoTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ab_read:
                //startActivity(new Intent(this,ReadActivity.class).putExtras(mangaSummary.toBundle()));
                showChaptersList();
                break;
        }
    }

    private void showChaptersList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mangaSummary.getChapters().getNames()), this);
        builder.setTitle(R.string.chapters_list);
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton(R.string.continue_reading, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(MangaPreviewActivity.this, ReadActivity.class);
                intent.putExtras(mangaSummary.toBundle());
                HistoryProvider.HistorySummary hs = HistoryProvider.get(MangaPreviewActivity.this, mangaSummary);
                intent.putExtra("chapter", hs.getChapter());
                intent.putExtra("page", hs.getPage());
                startActivity(intent);
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        HistoryProvider.addToHistory(this, mangaSummary, which, 0);
        startActivity(new Intent(this, ReadActivity.class).putExtra("chapter", which).putExtras(mangaSummary.toBundle()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_preview, menu);
        if (FavouritesProvider.Has(this, mangaSummary)) {
            menu.findItem(R.id.action_favourite).setIcon(R.drawable.ic_action_action_favorite);
            menu.findItem(R.id.action_favourite).setTitle(R.string.action_unfavourite);
        }
        menu.findItem(R.id.action_save).setVisible(!LocalMangaProvider.class.equals(mangaSummary.getProvider()));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favourite:
                FavouritesProvider favouritesProvider = new FavouritesProvider(this);
                if (favouritesProvider.has(mangaSummary)) {
                    if (favouritesProvider.remove(mangaSummary)) {
                        item.setIcon(R.drawable.ic_action_action_favorite_outline);
                        item.setTitle(R.string.action_favourite);
                    }
                } else {
                    if (favouritesProvider.add(mangaSummary)) {
                        item.setIcon(R.drawable.ic_action_action_favorite);
                        item.setTitle(R.string.action_unfavourite);
                    }
                }
                return true;
            case R.id.action_save:
                SaveService.Save(this, mangaSummary);
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void OnScroll(AdvancedScrollView scrollView, int x, int y, int oldx, int oldy) {
        float scrollFactor = (float)y / imageView.getHeight();
        if (scrollFactor > 1) {
            scrollFactor = 1f;
        }
        imageView.setPadding(0, y / 2, 0, 0);
        imageView.setColorFilter(Color.argb((int) (180 * scrollFactor), 0, 0, 0));
        float scaleFactor = 3f * (1 - scrollFactor) - 0.9f;
        if (scaleFactor > 1) {
            scaleFactor = 1;
        }
        if (scaleFactor < 0) {
            scaleFactor = 0;
        }
        readButton.setScaleX(scaleFactor);
        readButton.setScaleY(scaleFactor);
    }

    private class LoadInfoTask extends AsyncTask<Void,Void,MangaSummary> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(MangaSummary mangaSummary) {
            super.onPostExecute(mangaSummary);
            MangaPreviewActivity.this.mangaSummary = mangaSummary;
            findViewById(R.id.progressBar).setVisibility(View.GONE);
            findViewById(R.id.ab_read).setEnabled(true);
            ((TextView)findViewById(R.id.textView_description)).setText(mangaSummary.getDescription());
            new ImageLoadTask((ImageView) findViewById(R.id.imageView),mangaSummary.getPreview(), false, null).execute();
            if (mangaSummary.getChapters().size() == 0) {
                findViewById(R.id.ab_read).setEnabled(false);
                Toast.makeText(MangaPreviewActivity.this, R.string.loading_error, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected MangaSummary doInBackground(Void... params) {
            try {
                MangaProvider provider;
                if (mangaSummary.getProvider().equals(LocalMangaProvider.class)) {
                    provider = new LocalMangaProvider(MangaPreviewActivity.this);
                } else {
                    provider = (MangaProvider) mangaSummary.getProvider().newInstance();
                }
                return provider.getDetailedInfo(mangaSummary);
            } catch (Exception e) {
                return new MangaSummary(mangaSummary);
            }
        }
    }
}
