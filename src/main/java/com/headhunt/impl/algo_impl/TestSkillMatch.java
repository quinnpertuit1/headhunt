package com.headhunt.impl.algo_impl;

import com.headhunt.algo.SkillsMatch;
import com.headhunt.utils.commonutils.fileutils.UtilitiesFile;
import com.headhunt.utils.commonutils.mathutils.UtilitiesCollection;

import java.util.*;

/**
 * Created by sagraw200 on 10/09/16.
 */
public class TestSkillMatch {

    private static final String skillsMaster = "src/resources/data/ingest/skill/skill_master.txt";
    private static final String jdSmall = "src/resources/data/ingest/jd/jd.data.parsed.small";
    private static UtilitiesFile utilitiesFile;
    private static SkillsMatch skillsMatch;

    public static void main(String[] args) {
        utilitiesFile = new UtilitiesFile();
        List<String> allSkills = utilitiesFile.readFileInList(skillsMaster);
        List<String> allJds = utilitiesFile.readFileInList(jdSmall);

        skillsMatch = new SkillsMatch(allSkills);
        TreeMap<String, Integer> skillMap = new TreeMap<>();
        int N = 0;
        for (int i = 1000; i < 1800; ++i) {
            N++;
            if (N % 100 == 0) {
                System.out.println(N);
            }
            Set<String> skills_found = skillsMatch.matchSkillForText(allJds.get(i));
            for (String sk : skills_found) {
                if (skillMap.containsKey(sk)) {
                    skillMap.put(sk, skillMap.get(sk)+1 );
                } else {
                    skillMap.put(sk,1);
                }
            }
        }

        LinkedHashMap<String, Integer> sortedMap = (LinkedHashMap) UtilitiesCollection.sortByValue(skillMap);
        System.out.println(sortedMap.size());
        for (String ski : sortedMap.keySet()) {
            System.out.println(ski + " ==> " + sortedMap.get(ski));
        }
    }

}
