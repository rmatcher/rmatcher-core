package com.rmatcher.core.sentiment;

import java.io.IOException;
import java.util.Iterator;

import com.google.common.base.Splitter;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * Created with IntelliJ IDEA.
 * User: Ameen
 * Date: 4/28/13
 * Time: 8:57 PM
 */
public class SentimentScoring {
    private static SWN3 swn3;
    private static MaxentTagger tagger;

    public SentimentScoring() {
        this.swn3 = new SWN3();
        try{
            //this.tagger = new MaxentTagger("target\\classes\\wsj-0-18-left3words.tagger");
            this.tagger = new MaxentTagger("target/classes/wsj-0-18-left3words.tagger");
        } catch(Exception e){
            throw new RuntimeException("Check the Stanford NLP tagger model file. "+e.getMessage());
        }
    }

    public static void main(String [] args) throws IOException {

        String sent1 = "DVAP....\n" +
                "\n" +
                "You have to go at least once in your life. It really is a neat place with alot of history. \n" +
                "\n" +
                "The service is great, it appears to be family run. \n" +
                "\n" +
                "The food is good. Better then Dennys but not as good as Mimi's. \n" +
                "\n" +
                "I had the all u can eat of beef ribs, lasagna, meat loaf, cat fish, chicken, mashed and diced potatoes, stuffing, rice, homemade apple pie, etc and salad bar. I know I am missing a bunch of stuff they had but you get the drift. \n" +
                "\n" +
                "They run specials on Prime rib and stuff so you might want to call to see what they are serving the night you go.";

        String sent2 = "Always reliably good.  Great beer selection as well as fabulous \"bitch fizz\" (ciders, etc.) and cocktails.  Their pizza is outstanding AND they have a healthy menu.  I generally steer clear of chains but this is an exception.";

        SentimentScoring ss = new SentimentScoring();
        double result = ss.scoreSentence(sent1);
        System.out.println(result);

        ss.scoreSentence(sent2);


    }

    public double scoreSentence(String sent){

        String tagged = tagger.tagString(sent);
        Iterator<String> wordsIter = Splitter.on(' ').omitEmptyStrings().trimResults().split(tagged).iterator();

        while(wordsIter.hasNext()){
            String[] word = wordsIter.next().split("_");
            if(word[1].equals("JJ")){

                System.out.println(word[0] + ": " + swn3.extract(word[0],"a"));
            }

        }
        return 0.0;
    }
}
