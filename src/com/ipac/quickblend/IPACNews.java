package com.ipac.quickblend;

import java.io.IOException;
import java.util.ArrayList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class IPACNews
{
    private static Entry[] entries;
    
    public static void init()
    {
        ArrayList<Entry> entryList = new ArrayList<>();
        try
        {
            loadFromWebsite(entryList);
        }
        catch(Exception err)
        {
            err.printStackTrace();
        }
        for(Entry entry : entryList)
        {
            System.out.println("Found Entry: " + entry);
        }
        entries = entryList.toArray(new Entry[0]);
    }
    private static void loadFromWebsite(ArrayList<Entry> entries) throws IOException
    {
        Document doc = Jsoup.connect("http://www.ipac-inc.com/").get();
        String title = null, date = null, link = null;
        for(Element news : doc.getElementsByClass("home-news-wrapper"))
        {
            for(Element elem : news.getAllElements())
            {
                if(elem.tag().toString().equals("a")) link = elem.attr("href");
                else if(elem.tag().toString().equals("div") && elem.className().equals("date")) date = elem.text();
                else if(elem.tag().toString().equals("h4")) title = elem.text();
                if(title != null && date != null && link != null)
                {
                    entries.add(new Entry(title,date,link));
                    title = null;
                    date = null;
                    link = null;
                }
            }
        }
    }
    
    public static final class Entry
    {
        public final String title, date, link;
        
        private Entry(String title, String date, String link)
        {
            this.title = title;
            this.date = date;
            this.link = link;
        }
    }
}