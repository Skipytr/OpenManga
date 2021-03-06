package org.nv95.openmanga.providers;

import android.content.Context;
import android.text.Html;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.nv95.openmanga.R;

import java.util.ArrayList;

/**
 * Created by nv95 on 06.10.15.
 */
public class MangaTownProvider extends MangaProvider {
    protected static boolean features[] = {true, false, false, true, true};
    protected static final int sorts[] = {R.string.sort_latest,R.string.sort_popular};
    protected static final String sortUrls[] = {"latest","hot"};
    protected static final int genres[] = {R.string.genre_all, R.string.genre_romance, R.string.genre_adventure, R.string.genre_school, R.string.genre_comedy,R.string.genre_vampires, R.string.genre_youkai, R.string.genre_horror,R.string.genre_genderbender,R.string.genre_harem,R.string.genre_ecchi,R.string.genre_shoujo,R.string.genre_seinen,R.string.genre_shounen,R.string.genre_yaoi};
    protected static final String genreUrls[] = {"romance","adventure","school_life","comedy","vampire","youkai","horror","gender_bender","harem","ecchi","shoujo","seinen","shounen","yaoi"};

    @Override
    public MangaList getList(int page, int sort, int genre) throws Exception {
        MangaList list = new MangaList();
        Document document = getPage("http://www.mangatown.com/" + sortUrls[sort] + "/"
                + (genre == 0 ? "" : genreUrls[genre-1] + "/")
                + (page + 1) + ".htm");
        MangaInfo manga;
        Element root = document.body().select("ul.post-list").first();
        for (Element o: root.select("li")) {
            manga = new MangaInfo();
            manga.name = o.select("p.title").first().text();
            manga.subtitle = "";
            try {
                manga.summary = o.select("p").get(1).text();
            } catch (Exception e) {
                manga.summary = "";
            }
            manga.path = o.select("a").first().attr("href");
            try {
                manga.preview = o.select("img").first().attr("src");
            } catch (Exception e) {
                manga.preview = "";
            }
            manga.provider = MangaTownProvider.class;
            list.add(manga);
        }
        return list;
    }

    @Override
    public MangaSummary getDetailedInfo(MangaInfo mangaInfo) {
        MangaSummary summary = new MangaSummary(mangaInfo);
        try {
            Document document = getPage(mangaInfo.getPath());
            Element e = document.body();
            summary.description = Html.fromHtml(e.getElementById("show").html()).toString();
            summary.preview = e.select("img").first().attr("src");
            MangaChapter chapter;
            e = e.select("ul.detail-ch-list").first();
            for (Element o:e.select("li")) {
                chapter = new MangaChapter();
                chapter.name = o.select("a").first().text() + " "  + o.select("span").get(0).text();
                chapter.readLink = o.select("a").first().attr("href");
                chapter.provider = summary.provider;
                summary.chapters.add(0, chapter);
            }
            summary.readLink = summary.chapters.get(0).getReadLink();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //summary.addDefaultChapter();
        return summary;
    }

    @Override
    public ArrayList<MangaPage> getPages(String readLink) {
        ArrayList<MangaPage> pages = new ArrayList<>();
        try {
            Document document = getPage(readLink);
            MangaPage page;
            Element e = document.body().select("select").get(1);
            for (Element o: e.select("option")) {
                page = new MangaPage(o.attr("value"));
                page.provider = MangaTownProvider.class;
                pages.add(page);
            }
            return pages;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getPageImage(MangaPage mangaPage) {
        try {
            Document document = getPage(mangaPage.getPath());
            return document.body().getElementById("image").attr("src");
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getName() {
        return "MangaTown";
    }

    @Override
    public boolean hasFeature(int feature) {
        return features[feature];
    }

    @Override
    public String[] getSortTitles(Context context) {
        return super.getTitles(context, sorts);
    }

    @Override
    public String[] getGenresTitles(Context context) {
        return super.getTitles(context, genres);
    }
}
