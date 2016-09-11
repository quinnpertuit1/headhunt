package com.headhunt.algo;

import com.headhunt.utils.commonutils.tokenizerutils.TokenizerActionSelector;
import com.headhunt.utils.commonutils.tokenizerutils.UtilitiesTokenizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by sagraw200 on 3/19/16.
 */
public class SkillsMatch {

    private UtilitiesTokenizer utilitiesTokenizer;
    private List<String> all_skills;

    public SkillsMatch(List<String> _all_skills) {
        utilitiesTokenizer = new UtilitiesTokenizer();
        all_skills = new ArrayList<>();
        for (String s : _all_skills) {
            all_skills.add(s.trim().toLowerCase());
        }
    }

    public Set<String> matchSkillForText(String text) {
        Set<String> found_skills = new HashSet<>();

        text = text.replaceAll("\n"," ").replaceAll(","," ").replaceAll("\\\\", " ").replaceAll("/", " ").replaceAll("\\(", " ").replaceAll("\\)", " ").toLowerCase().trim();//.replaceAll("\\."," ")
        for (int i = 4; i > 0; --i) {
            List<String> tokens = getNgram(text, i);
            text = matchAndClean(tokens, text, found_skills);
        }
        return found_skills;
    }

    private String matchAndClean(List<String> tokens, String text, Set<String> found_skills) {

        for (int i = 0; i < tokens.size(); ++i) {
            String token = tokens.get(i).toLowerCase().trim();
            if (all_skills.contains(token)) {
                found_skills.add(token);
                text = text.replaceAll(token," ");
            }
        }
        return text;
    }

    private List<String> getNgram(String text, int gram) {
        int[] grams = {gram};
        TokenizerActionSelector tokenizerActionSelector = new TokenizerActionSelector();
        tokenizerActionSelector.setWhichGrams(grams);
        List<String> jdtokens = utilitiesTokenizer.Tokenize(text, tokenizerActionSelector);
        return jdtokens;
    }

}
