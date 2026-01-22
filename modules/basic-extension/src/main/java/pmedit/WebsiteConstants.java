package pmedit;

public class WebsiteConstants {

    public static String hrefQuery = "?utm_source=pmedit&utm_medium=app";

    public static String websiteUrl = "https://pdf.metadata.care/";
    public static String downloadUrl = "https://pdf.metadata.care/download/";
    public static String contactFormUrl = "https://pdf.metadata.care/contact/";
    public static String batchLicenseUrl = "https://pdf.metadata.care/buy/";

    public static String pdfCompressionHelp = "https://pdf.metadata.care/help/pdf-file-compression/";

    public static String getLink(String url, String text) {
        return
                "<a href=\"" + url + hrefQuery + "\">"
                        + text +
                        "</a>";
    }
    public static String getLink(String url) {
        return getLink(url, url);
    }
}
