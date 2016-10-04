package com.headhunt.ingest.resume;

import com.headhunt.utils.commonutils.fileutils.UtilitiesFile;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Created by sagraw200 on 3/8/16.
 */
public class LinkedinUserDatabase {

    private static String loginurl = "https://www.linkedin.com/uas/login?fromSignIn=true&trk=uno-reg-guest-home";
    private static String uId = "<linkedin_user_id>";
    private static String passW = "<linkedin_password>";

    private static final String outputFile = "src/main/java/com/headhunt/ingest/resume/user.data";
    private static final String outputUrlFile = "src/main/java/com/headhunt/ingest/resume/userurl.data";

    private static List<String> cookies = new ArrayList<>();
    private HttpsURLConnection conn;

    private final String USER_AGENT = "Mozilla/5.0";

    public static void main(String[] args) throws Exception {
        run();
    }

    private static void run() throws Exception {

        LinkedinUserDatabase http = new LinkedinUserDatabase();

        // Login to linked
        login(http);

//        // get user database
//        userDbParser(http);

        // read resume
        readResume(http);

    }

    private static void readResume(LinkedinUserDatabase http) {
        String uurl = "https://www.linkedin.com/in/aditya-prakash-6b234410";

        try {
//            String res1 = http.GetPageContent(uurl);
            String res2 = http.GetPageContentGzipped(uurl);
            System.out.println("");
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    private static void userDbParser(LinkedinUserDatabase http) {
        UtilitiesFile utilitiesFile = new UtilitiesFile();
        List<String> urlsRead = utilitiesFile.readFileInList(outputUrlFile);

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile, true));
            BufferedWriter bwurl = new BufferedWriter(new FileWriter(outputUrlFile, true));

            String commonUrl = "https://www.linkedin.com/directory/people-";

            char[] letters = "abcdefghijklmnopqrstuvwxyz".toCharArray();
            for (char c : letters) {
                for (int i = 1; i <= 10; ++i) {
                    for (int j = 1; j <= 10; ++j) {
                        for (int k = 1; k <= 100; ++k) {
                            String alphabet = c + "";
                            int page1 = i;
                            int page2 = j;
                            int page3 = k;

                            String pattern = alphabet + "-" + page1 + "-" + page2 + "-" + page3;  //"<alphabet>-<page1>-<page2>-<page3>"
                            String userUrl = commonUrl + pattern;
                            if (urlsRead.contains(userUrl)) {
                                continue;
                            }

                            String pageContent = "";
                            try {
                                bwurl.write(userUrl);
                                bwurl.write("\n");

                                pageContent = http.GetPageContentGzipped(userUrl);
                            } catch (Exception ex1) {
                                System.err.println("Can't parse this webpage " + userUrl + "\t" + ex1);
                                continue;
                            }

                            if (!pageContent.trim().isEmpty()) {
                                // get everything in between   a href="/in/   and "
                                String regex = "(a href=\"/in/).*?(\")";
                                Pattern p = Pattern.compile(regex);
                                Matcher m = p.matcher(pageContent);
                                while (m.find()) {
                                    String str = m.group();
                                    str = str.replaceAll("a href=\"", "");
                                    str = str.replaceAll("\"", "").replaceAll("/in/", "");
                                    bw.write(str);
                                    bw.write("\n");
                                }
                                bw.close();
                                bwurl.close();
                                bw = new BufferedWriter(new FileWriter(outputFile, true));
                                bwurl = new BufferedWriter(new FileWriter(outputUrlFile, true));
//                            Thread.sleep(50);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Error in parsing user database: "+ ex);
        }
    }

    private static void login(LinkedinUserDatabase http) {
        try {
            // make sure cookies is turn on
            CookieHandler.setDefault(new CookieManager());

            // 1. Send a "GET" request, so that you can extract the form's data.
            String page = http.GetPageContent(loginurl);
            String postParams = http.getFormParams(page, uId, passW);
            // username=sagraw200&password=07_Sept198310&submit=Sign+in&_csrf=53f2f7dc-401c-4ecd-964f-516da3de68cf
            // 2. Construct above post's content and then send a POST request for
            // authentication
            http.sendPost(loginurl, postParams);
        } catch (Exception ex) {
            System.err.println("Error in loggin to linkedin: "+ ex);
        }
    }



    private void sendPost(String url, String postParams) throws Exception {

        URL obj = new URL(url);
        conn = (HttpsURLConnection) obj.openConnection();

        // Acts like a browser
        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Host", "www.linkedin.com");
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.8,hi;q=0.6");
//        for (String cookie : cookies) {
//            conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
//        }
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Referer", "https://www.linkedin.com/");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", Integer.toString(postParams.length()));

        conn.setDoOutput(true);
        conn.setDoInput(true);

        // Send post request
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(postParams);
        wr.flush();
        wr.close();

        int responseCode = conn.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + postParams);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in =
                new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        // System.out.println(response.toString());

    }

    private String GetPageContent(String url) throws Exception {

        URL obj = new URL(url);
        conn = (HttpsURLConnection) obj.openConnection();

        // default is GET
        conn.setRequestMethod("GET");

        conn.setUseCaches(false);

        // act like a browser
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        if (cookies != null) {
            for (String cookie : this.cookies) {
                conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
            }
        }
        int responseCode = conn.getResponseCode();
//        System.out.println("\nSending 'GET' request to URL : " + url);
//        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // Get the response cookies
        setCookies(conn.getHeaderFields().get("Set-Cookie"));

        return response.toString();
    }

    private String GetPageContentGzipped(String url) throws Exception {

        URL obj = new URL(url);
        conn = (HttpsURLConnection) obj.openConnection();

        // default is GET
        conn.setRequestMethod("GET");

        conn.setUseCaches(false);

        // act like a browser
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        // TODO - LinkedinUserDatabase do not support the default encoding 'identity', so set gzip ; next step is to decompress it
        conn.setRequestProperty( "Accept-Encoding" , "gzip, deflate" );

        if (cookies != null) {
            for (String cookie : this.cookies) {
                conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
            }
        }
        int responseCode = conn.getResponseCode();

        Object content = conn.getContent();
        String contentEncoding = conn.getContentEncoding();
        int contentLen = conn.getContentLength();
        String contentType = conn.getContentType();
        InputStream contentInputStream = conn.getInputStream();
        GZIPInputStream contentInputStreamGzipped = new GZIPInputStream(contentInputStream);

        StringBuilder response = new StringBuilder();

        InputStreamReader reader = new InputStreamReader(contentInputStreamGzipped);
        BufferedReader in = new BufferedReader(reader);
        String readed;
        while ((readed = in.readLine()) != null) {
            response.append(readed).append("\n");
        }


//        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//        String inputLine;
//        StringBuffer response = new StringBuffer();
//
//        while ((inputLine = in.readLine()) != null) {
//            response.append(inputLine);
//        }
//        in.close();

        // Get the response cookies
        setCookies(conn.getHeaderFields().get("Set-Cookie"));

        return response.toString();

    }

    public String getFormParams(String html, String username, String password)
            throws UnsupportedEncodingException {

        System.out.println("Extracting form's data...");

        Document doc = Jsoup.parse(html);

        // Google form id
        Element loginform = doc.getElementById("login");
        Elements inputElements = loginform.getElementsByTag("input");
        List<String> paramList = new ArrayList<>();
        for (Element inputElement : inputElements) {
            String key = inputElement.attr("name");
            String value = inputElement.attr("value");

            if (key.equals("session_key"))
                value = username;
            else if (key.equals("session_password"))
                value = password;
            paramList.add(key + "=" + URLEncoder.encode(value, "UTF-8"));
        }

        // build parameters list
        StringBuilder result = new StringBuilder();
        for (String param : paramList) {
            if (result.length() == 0) {
                result.append(param);
            } else {
                result.append("&" + param);
            }
        }
        return result.toString();
    }

    public List<String> getCookies() {
        return cookies;
    }

    public void setCookies(List<String> cookies) {
        this.cookies = cookies;
    }

}
