package org.novalegal.controllers;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SitemapController {

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getRobotsTxt() {
        return """
                User-agent: *
                Allow: /

                Sitemap: https://novalegal.org/sitemap.xml
                """;
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String getSitemap() {
        String baseUrl = "https://novalegal.org";

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");

        // Core pages
        sb.append(url(baseUrl + "/", "weekly", "1.0"));
        sb.append(url(baseUrl + "/document-simplifier", "weekly", "0.9"));
        sb.append(url(baseUrl + "/meeting-summarizer", "weekly", "0.9"));
        sb.append(url(baseUrl + "/meeting-summarizer/on-premiser", "weekly", "0.9"));

        sb.append(url(baseUrl + "/privacy", "yearly", "0.3"));
        sb.append(url(baseUrl + "/terms", "yearly", "0.3"));
        sb.append(url(baseUrl + "/disclaimer", "yearly", "0.3"));
        sb.append(url(baseUrl + "/contact", "monthly", "0.5"));

        // Stripe pages
        sb.append(url(baseUrl + "/success", "monthly", "0.2"));
        sb.append(url(baseUrl + "/cancel", "monthly", "0.2"));

        // Blog
        sb.append(url(baseUrl + "/blog", "weekly", "0.8"));
        sb.append(url(baseUrl + "/blog/how-to-understand-legal-documents", "monthly", "0.6"));
        sb.append(url(baseUrl + "/blog/simplify-lease-agreement-ai", "monthly", "0.6"));
        sb.append(url(baseUrl + "/blog/ai-to-spot-nda-red-flags", "monthly", "0.6"));
        sb.append(url(baseUrl + "/blog/contract-jargon-explained", "monthly", "0.6"));
        sb.append(url(baseUrl + "/blog/freelance-contract-tips", "monthly", "0.6"));
        sb.append(url(baseUrl + "/blog/why-not-upload-meetings-to-chatgpt", "monthly", "0.6"));

        sb.append("</urlset>");
        return sb.toString();
    }

    private String url(String loc, String changefreq, String priority) {
        return "<url>"
                + "<loc>" + loc + "</loc>"
                + "<changefreq>" + changefreq + "</changefreq>"
                + "<priority>" + priority + "</priority>"
                + "</url>";
    }
}