package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;

/**
 * 0.4.354, The Last Crossing, Night Two (docs/LAST_CROSSING_0_4_354.md): what a turned reader's speech sounds like after dark. From the
 * owner's design: "Alter the text of speech sent inside the vignette while transformed. Sender's name stays visible. Sender sees the same
 * broken line. Daylight speech normal. Night Two: A rasp, a repeated consonant, and a hiss now and then. Longer instructions can still make
 * it through. Night Three: Sentences break more often. Short calls get through more reliably. Near the plate or while holding a live gate
 * shut, pauses get longer and a line can fall to a few audible words." And: "Preserve names, directions, negations when the line carrying
 * them comes through."
 *
 * <p>The line is broken once, when it is said, and everyone (the speaker too) gets the same broken line: in chat, over the speaker's head
 * and in the speaker's own conversation panel. Whatever survives of a name, a direction or a "not" survives whole.
 */
public final class LastCrossingSpeech {
    private LastCrossingSpeech(){}
    /** How broken: daylight; Night Two's rasp; Night Three's broken sentences; choked by the plate or by a live gate held shut. */
    public static final int CLEAR=0,NIGHT_TWO=1,NIGHT_THREE=2,CHOKED=3;
    /** Directions and negations: never broken, never dropped. */
    static final Set<String> KEPT=Set.of(
        "no","not","don't","dont","never","nothing","nobody","none","nowhere","can't","cant","won't","wont","isn't","aren't","didn't","doesn't","shouldn't","stop","wait",
        "up","down","left","right","north","south","east","west","under","over","below","above","behind","back","front","in","out","here","there","inside","outside",
        "near","far","across","through","off","on","upstairs","downstairs");
    /** Words that can go without losing the sense, first to be lost when a line falls apart. */
    static final Set<String> SMALL=Set.of("the","a","an","to","and","then","just","so","it","is","are","was","of","for","that","this","be","we","you","i'm","i","my","your",
        "put","get","go","come","when","if","at","with","by","all","some","now","again","still","okay","ok","yeah");
    public static final String HISS="[hiss]";

    /** The line as it comes out at this level; names are kept whole. The same line, level, names and seed always break the same way. */
    public static String garble(String text,int level,Collection<String> names,long seed){
        if(level<=CLEAR||text==null||text.isBlank())return text;
        var rnd=new Random(seed*31+text.hashCode());
        var lower=new HashSet<String>();for(var n:names)if(n!=null&&!n.isBlank())lower.add(n.toLowerCase(Locale.ROOT));
        var words=new ArrayList<>(List.of(text.trim().split("\\s+")));
        // Choked: a line falls to a few audible words; the small ones go first, and where words went there's a long pause.
        if(level>=CHOKED&&words.size()>2){
            var kept=new ArrayList<String>();boolean gap=false;
            for(var w:words){
                var core=core(w).toLowerCase(Locale.ROOT);
                boolean keep=kept(core,lower)||!SMALL.contains(core)&&rnd.nextInt(3)>0;
                if(keep){if(gap&&!kept.isEmpty())kept.add("…");kept.add(w);gap=false;}else gap=true;
            }
            if(kept.isEmpty())kept.add(words.getFirst());
            words=kept;
        }
        // Night Three: a long sentence breaks in two where a small word starts its second half.
        if(level>=NIGHT_THREE&&words.size()>=5){
            for(int i=2;i<words.size()-1;i++){var core=core(words.get(i)).toLowerCase(Locale.ROOT);
                if(SMALL.contains(core)&&!words.get(i-1).matches(".*[.!?…]$")){words.set(i-1,words.get(i-1).replaceAll("[,;:]$","")+".");words.set(i,capital(words.get(i)));break;}}
        }
        // The rasp: one longer word broken in its throat; the repeated consonant: another word caught on its first sound.
        var rasp=candidates(words,lower,4,false);int r=-1;
        if(!rasp.isEmpty()){r=rasp.get(rnd.nextInt(rasp.size()));words.set(r,rasp(words.get(r)));}
        var repeat=candidates(words,lower,2,true);repeat.remove((Integer)r);
        // A line of nothing but kept words (a name, "Here.") still catches on its first sound: the word stays whole after it.
        if(repeat.isEmpty())for(int i=0;i<words.size();i++){var core=core(words.get(i));if(i!=r&&core.length()>=2&&consonant(core.charAt(0)))repeat.add(i);}
        if(!repeat.isEmpty()){int i=repeat.get(rnd.nextInt(repeat.size()));words.set(i,repeat(words.get(i)));}
        // The hiss, now and then: between sentences if there are two, else partway through a longer line. More of them choked.
        int hisses=level>=CHOKED?2:level>=NIGHT_THREE?1+(words.size()>=8?1:0):words.size()>=6?1:0;
        for(int k=0;k<hisses;k++){
            int at=-1;
            for(int i=0;i<words.size()-1;i++)if(words.get(i).matches(".*[.!?]$")&&!words.get(i+1).equals(HISS)&&!words.get(i).equals(HISS)){at=i+1;if(rnd.nextBoolean())break;}
            if(at<0&&words.size()>=4)at=words.size()/2+rnd.nextInt(2)-(words.size()>4?1:0);
            if(at<=0||at>=words.size())continue;
            if(words.get(at-1).equals(HISS)||words.get(at).equals(HISS))continue;
            words.add(at,HISS);
        }
        return String.join(" ",words);
    }
    /** Positions of words long enough to break that are not names, directions or negations. */
    static List<Integer> candidates(List<String> words,Set<String> names,int length,boolean startConsonant){
        var out=new ArrayList<Integer>();
        for(int i=0;i<words.size();i++){var w=words.get(i);if(w.equals(HISS)||w.equals("…"))continue;var core=core(w);
            if(core.length()<length||kept(core.toLowerCase(Locale.ROOT),names)||core.contains("—")||core.contains("-"))continue;
            if(startConsonant&&!consonant(core.charAt(0)))continue;
            out.add(i);}
        return out;
    }
    static boolean kept(String lowerCore,Set<String> names){return lowerCore.isEmpty()||KEPT.contains(lowerCore)||names.contains(lowerCore)||lowerCore.chars().anyMatch(Character::isDigit)||lowerCore.length()<=1;}
    /** The letters of a word, without what stands before and after it. */
    static String core(String w){int a=0,b=w.length();while(a<b&&!Character.isLetterOrDigit(w.charAt(a)))a++;while(b>a&&!Character.isLetterOrDigit(w.charAt(b-1)))b--;return w.substring(a,b);}
    static boolean consonant(char c){return Character.isLetter(c)&&"aeiouAEIOU".indexOf(c)<0;}
    /** "ramp" to "r—amp": the breath catching after the word's first sounds. */
    static String rasp(String w){
        int a=0;while(a<w.length()&&!Character.isLetter(w.charAt(a)))a++;
        int v=a;while(v<w.length()&&Character.isLetter(w.charAt(v))&&"aeiouAEIOU".indexOf(w.charAt(v))<0)v++;
        if(v==a)v=a+1;
        if(v>=w.length()||!Character.isLetter(w.charAt(v)))return w;
        return w.substring(0,v)+"—"+w.substring(v);
    }
    /** "pin" to "p-pin": the first sound said twice. */
    static String repeat(String w){
        int a=0;while(a<w.length()&&!Character.isLetter(w.charAt(a)))a++;
        if(a>=w.length())return w;
        return w.substring(0,a)+w.charAt(a)+"-"+Character.toLowerCase(w.charAt(a))+w.substring(a+1);
    }
    static String capital(String w){int a=0;while(a<w.length()&&!Character.isLetter(w.charAt(a)))a++;if(a>=w.length())return w;return w.substring(0,a)+Character.toUpperCase(w.charAt(a))+w.substring(a+1);}
}
