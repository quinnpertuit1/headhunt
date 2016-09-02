package com.headhunt.ingest.jd;

import com.headhunt.utils.commonutils.fileutils.UtilitiesFile;
import com.headhunt.utils.commonutils.urlutils.UtilitiesURL;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sagraw200 on 05/08/16.
 */
public class IndeedJdHtml {

    private final String startURL = "http://www.indeed.com/viewjob?jk=";
    private UtilitiesURL uurl;

    private List<String> allJobIds = new ArrayList<>();
    private List<String> readJobIds = new ArrayList<>();

    private String jdDataFile = "src/resources/data/ingest/jd/jd.data.raw";
    private String jdUrlFile = "src/resources/data/ingest/jd/jdurl.data.raw";
    private List<String> allJdUrls = new ArrayList<>();

    private BufferedWriter jdDataBw = null;
    private BufferedWriter jdUrlBw = null;


    public IndeedJdHtml() {
        try {
            uurl = new UtilitiesURL();
            jdDataBw = new BufferedWriter(new FileWriter(jdDataFile, true));
            jdUrlBw = new BufferedWriter(new FileWriter(jdUrlFile, true));

            UtilitiesFile uf = new UtilitiesFile();
            allJdUrls = uf.readFileInList(jdUrlFile);
        } catch (Exception ex) {
            System.err.println("Error in bufferedwriter init: " + ex);
        }

    }

    public static void main(String[] args) {
        IndeedJdHtml indeedJdHtml = new IndeedJdHtml();

        indeedJdHtml.getFirstIdFromSearch();
        indeedJdHtml.impl();
    }

    private void getFirstIdFromSearch() {
        // TODO: RUN FOR THE FIRST 10 SKILLS NOW - FOR TEST
        UtilitiesFile uf = new UtilitiesFile();
        List<String> skills = uf.readFileInList("src/resources/data/ingest/skill/linkedin.skill.tmp");
        List<String> whats = new ArrayList<>();
        for ( String skill : skills ) {
            whats.add(URLEncoder.encode(skill));
        }
        whats = whats.subList(101, 200);
        System.out.println("whats size: " + whats.size());

        String[] wheres = {"Philadelphia%2C+PA",
                "San+Francisco%2C+CA",
                "New+York%2C+NY",
                "Chicago%2C+IL",
                "Pittsburgh%2C+PA",
                "Austin%2C+TX",
                "Raleigh%2C+NC",
                "Cincinnati%2C+OH",
                "Washington%2C+DC"};
        System.out.println("wheres size: " + wheres.length);

        for (String what : whats) {
            for (String where : wheres) {
                List<String> list = new ArrayList<>();
                String jobsearchurl = "http://www.indeed.com/jobs?q="+what+"&l="+where;

                String firstContent = uurl.readStringFromUrl(jobsearchurl);
                String regex = "(<div class=\"row.*data-jk=\").*?(\">)";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(firstContent);
                while (m.find()) {
                    String match = m.group();
                    String id = match.split("data-jk")[1].replaceAll("=","").replaceAll("\"","").replaceAll(">","").trim();
                    list.add(id);
                }
                allJobIds.addAll(list);
            }
        }

        Set<String> tmp = new HashSet<>(allJobIds);
        allJobIds = new ArrayList<>(tmp);
        System.out.println("initial job id urls: " + allJobIds.size());

    }

    private void impl() {
        // store urlText as-is
        // store jdUrl

        String urlText = null;
        String jobId = null;
        List<String> recJobs = new ArrayList<>();

        jobId = allJobIds.get(0);
        urlText = uurl.readStringFromUrl(startURL + jobId);
        writeJdHtml(urlText);
        writeJdUrl((startURL + jobId));
        readJobIds.add(jobId);

        recJobs = getRecommendedJobIds(urlText);
        for (String jid : recJobs) {
            if (!allJobIds.contains(jid)) {
                allJobIds.add(jid);
            }
        }

        int N = 1;
        while (true) {
            while ( true ) {
                jobId = allJobIds.get(N);
                N++;
                if (!readJobIds.contains(jobId)) {
                    break;
                }
            }
            System.out.println("All ids [" + allJobIds.size() + "]\t Read ids [" + readJobIds.size() + "]");

            if (!allJdUrls.contains((startURL + jobId))) {
                try {
                    urlText = uurl.readStringFromUrl(startURL + jobId);
                    writeJdHtml(urlText);
                    writeJdUrl((startURL + jobId));
                    readJobIds.add(jobId);

                    recJobs = getRecommendedJobIds(urlText);
                    for (String jid : recJobs) {
                        if (!allJobIds.contains(jid)) {
                            allJobIds.add(jid);
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("Error in getting job html: " + (startURL + jobId) + "\t" + ex);
                }
            }
        }
    }

    private List<String> getRecommendedJobIds(String urlText) {
        // recommended jobs : <div class="recJobs">
        // jk=e28fa4eeaf95b1f4 inside href
        List<String> result = new ArrayList<>();

        Pattern pattern = null;
        String regex = "<div class=\"recJobs\">(.*)</div>";
        pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher m = pattern.matcher(urlText);
        if (m.find()) {
            String recText = m.group();
            pattern = null;
            regex = "jk=(.*?)&from=recjobs";
            pattern = Pattern.compile(regex, Pattern.DOTALL);
            m = pattern.matcher(recText);
            while (m.find()) {
                String recjobid = m.group();
                result.add(recjobid.replaceAll("jk=", "").replaceAll("&from=recjobs", "").trim());
            }
        }
        return result;
    }

    private void writeJdHtml(String urlText) {
        // urlText
        try {
            urlText = urlText.replaceAll("\n", " ");
            jdDataBw.write(urlText);
            jdDataBw.write("\n");
            jdDataBw.close();
            jdDataBw = new BufferedWriter(new FileWriter(jdDataFile, true));
        } catch (Exception ex) {
            System.err.println("Error in writing jd file: " + ex);
        }
    }

    private void writeJdUrl(String jdUrl) {
        try {
            jdUrlBw.write(jdUrl);
            jdUrlBw.write("\n");
            jdUrlBw.close();
            jdUrlBw = new BufferedWriter(new FileWriter(jdUrlFile, true));
        } catch (Exception ex) {
            System.err.println("Error in writing jd url file: " + ex);
        }
    }

}
