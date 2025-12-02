package com.nametag;

import jakarta.annotation.PostConstruct;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class GetVersion {

//    @Scheduled
    @PostConstruct
    public void getVersion() {
        String url = "https://cnb1.github.io/latestversion/";
        String version = getVersionFromWebpage(url);

        if (version != null) {
            System.out.println("The extracted version is: " + version);
        } else {
            System.out.println("Could not extract the version.");
        }
    }

    public static String getVersionFromWebpage(String url) {
        try {
            // 1. Connect and fetch the HTML document
            // It's good practice to set a user agent
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .get();

            // 2. Use a CSS selector to find the element containing the version.
            // Selector: h1#version + p
            // - h1#version: Finds the <h1> tag with the ID 'version' (the h1 element has id='version' in the HTML you provided: <h1 id="version">Version</h1>)
            // - + p: Selects the <p> element that immediately follows the h1#version element (The p element contains 1.0.2: <p>1.0.2</p>)
            Element versionElement = doc.selectFirst("h1#version + p");

            if (versionElement != null) {
                // 3. Get the text from the selected <p> element
                // .text() is used to get the inner text, trimming whitespace.
                return versionElement.text().trim();
            } else {
                System.err.println("Error: Could not find the specific version element.");
                return null;
            }

        } catch (Exception e) {
            return null;
        }
    }
}
