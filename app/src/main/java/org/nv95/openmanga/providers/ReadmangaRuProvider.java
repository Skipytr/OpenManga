package org.nv95.openmanga.providers;

import android.content.Context;
import android.text.Html;

import org.json.JSONArray;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nv95.openmanga.R;
import org.nv95.openmanga.utils.ErrorReporter;

import java.util.ArrayList;

/**
 * Created by nv95 on 30.09.15.
 * provider for http://readmanga.me/
 */
public class ReadmangaRuProvider extends MangaProvider {
    protected static final boolean features[] = {true, true, false, true, true};
    protected static final int sorts[] = {R.string.sort_popular,R.string.sort_updated,R.string.rating};
    protected static final String sortUrls[] = {"popular","updated","votes"};
    protected static final int genres[] = {R.string.genre_all, R.string.genre_art, R.string.genre_action, R.string.genre_martialarts, R.string.genre_vampires, R.string.genre_harem, R.string.genre_mystery,R.string.genre_shoujo,R.string.genre_shounen,R.string.genre_horror,R.string.genre_school,R.string.genre_ecchi};
    protected static final String genreUrls[] = {"art","action","martial_arts","vampires","harem","mystery","shoujo","shounen","horror","school","ecchi"};

    @Override
    public MangaList getList(int page, int sort, int genre) throws Exception {
        MangaList list = new MangaList();
        Document document = getPage("http://readmanga.me/list" +
                (genre == 0 ? "" : "/genre/" + genreUrls[genre-1])
                + "?sortType=" + sortUrls[sort] + "&offset=" + page*70 + "&max=70");
        MangaInfo manga;
        Element t;
        Elements elements = document.body().select("div.col-sm-6");
        for (Element o: elements) {
            manga = new MangaInfo();
            t = o.select("h3").first();
            if (t == null) {
                continue;
            }
            manga.name = t.text();
            try {
                manga.subtitle = o.select("h4").first().text();
            } catch (Exception e) {
                manga.subtitle = "";
            }
            manga.summary = o.select("a.element-link").text();
            manga.path = "http://readmanga.me" + o.select("a").first().attr("href");
            try {
                manga.preview = o.select("img").first().attr("src");
            } catch (Exception e) {
                manga.preview = "";
            }
            manga.provider = ReadmangaRuProvider.class;
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
            summary.readLink = "http://readmanga.me" + e.select("span.read-first").first().child(0).attr("href") + "?mature=1";;
            String descr = e.select("div.manga-description").first().html();
            int p = descr.indexOf("<a h");
            if (p>0)
                descr = descr.substring(0,p);
            summary.description = Html.fromHtml(descr).toString();
            summary.preview = e.select("div.picture-fotorama").first().child(0).attr("data-full");
            MangaChapter chapter;
            e = e.select("table.table").first();
            for (Element o:e.select("a")) {
                chapter = new MangaChapter();
                chapter.name = o.text();
                chapter.readLink = "http://readmanga.me" + o.attr("href") + "?mature=1";;
                chapter.provider = summary.provider;
                summary.chapters.add(0, chapter);
            }
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
            int start= 0;
            String s;
            Elements es = document.body().select("script");
            for (Element o: es) {
                s = o.html();
                start = s.indexOf("rm_h.init(");
                if (start != -1) {
                    start += 10;
                    int p = s.lastIndexOf("]") + 1;
                    s = s.substring(start, p);
                    JSONArray array = new JSONArray(s);
                    JSONArray o1;
                    for (int i=0;i<array.length();i++) {
                        o1 = array.getJSONArray(i);
                        page = new MangaPage(o1.getString(1) + o1.getString(0) + o1.getString(2));
                        page.provider = MintMangaProvider.class;
                        pages.add(page);
                    }
                    return pages;
                }
            }
        } catch (Exception e) {
            ErrorReporter.getInstance().report(e);
        }
        return null;
    }

    @Override
    public String getPageImage(MangaPage mangaPage) {
        return mangaPage.getPath();
    }

    @Override
    public String getName() {
        return "ReadManga";
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

    @Override
    public MangaList search(String query, int page) throws Exception {
        if (page > 0) {
            return MangaList.Empty();
        }
        MangaList list = new MangaList();
        String data[] = new String[] {
                "q", query
        };
        Document document = postPage("http://readmanga.me/search", data);
        MangaInfo manga;
        Elements elements = document.body().select("div.col-sm-6");
        for (Element o: elements) {
            manga = new MangaInfo();
            manga.name = o.select("h3").first().text();
            manga.summary = o.select("a.element-link").text();
            manga.path = "http://readmanga.me" + o.select("a").first().attr("href");
            manga.preview = o.select("img").first().attr("src");
            manga.provider = ReadmangaRuProvider.class;
            list.add(manga);
        }
        return list;
    }
}
