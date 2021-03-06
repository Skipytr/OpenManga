package org.nv95.openmanga.providers;

import android.os.Bundle;

/**
 * Created by nv95 on 30.09.15.
 * Более подробная информация
 */
public class MangaSummary extends MangaInfo {
    protected String readLink;
    protected String description;
    protected MangaChapters chapters;

    public MangaSummary(MangaInfo mangaInfo) {
        this.name = mangaInfo.name;
        this.summary = mangaInfo.summary;
        this.path = mangaInfo.path;
        this.preview = mangaInfo.preview;
        this.subtitle = mangaInfo.subtitle;
        this.provider = mangaInfo.provider;
        this.description = "";
        this.readLink = "";
        this.chapters = new MangaChapters();
    }

    public MangaSummary(Bundle bundle) {
        super(bundle);
        this.readLink = bundle.getString("readlink");
        this.description = bundle.getString("description");
        chapters = new MangaChapters(bundle);
    }

    @Override
    public Bundle toBundle() {
        Bundle bundle = super.toBundle();
        bundle.putString("readlink", readLink);
        bundle.putString("description", description);
        bundle.putAll(chapters.toBundle());
        return bundle;
    }

    public String getReadLink() {
        return readLink;
    }

    public String getDescription() {
        return description;
    }

    public MangaChapters getChapters() {
        return chapters;
    }

    /**
     * Uses then manga has only one chapter
     * If provider doesn't support table of contents
     */
    public void addDefaultChapter() {
        MangaChapter chapter = new MangaChapter();
        chapter.provider = this.provider;
        chapter.name = this.name;
        chapter.readLink = this.readLink;
        chapters.add(chapter);
    }
}
