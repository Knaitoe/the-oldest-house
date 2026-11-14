package io.github.knaitoe.theoldesthouse.house;

import java.util.ArrayList;
import java.util.List;

/**
 * 0.4.580: prose cut into book pages that the game shows whole. A book's page is 114 pixels wide and fourteen lines high, and the game
 * draws nothing past the fourteenth line: text that runs longer is cut off there without a mark. The Witness account put each room on one
 * page whatever its length, so a room with much to say after it (what happened afterwards, who else was there) lost its end.
 *
 * <p>lines() counts the lines the game will wrap a page's text to, measured with the advances the correspondence carries for each hand,
 * none of which is narrower than its glyph: the game never needs more lines than this counts. split() fills each page with whole
 * sentences, so that a page ends where a sentence does, and breaks a sentence between words only when it is longer than a page by
 * itself. A head (a room's title) stays over the first of its text.
 */
public final class BookPages {
    private BookPages(){}
    /** A page's width in pixels and its height in lines, as the game's book screen wraps and draws it. */
    public static final int WIDTH=114,LINES=14;
    private static final int SPACE=4;
    /** Written so by habit before a name, or as an initial: a full stop that ends no sentence. */
    private static final List<String> TITLES=List.of("Mr","Mrs","Ms","Dr","St","No");

    /** Each hand's widths for the letters below 256, looked up once: an account is paged again whenever its reader's record changes. */
    private static final java.util.Map<HouseWriting.WritingStyle,int[]> WIDTHS=new java.util.concurrent.ConcurrentHashMap<>();
    private static int advance(HouseWriting.WritingStyle style,int cp){
        if(cp>=256)return CorrespondenceTexts.advance(style,cp);
        return WIDTHS.computeIfAbsent(style,s->{int[] table=new int[256];for(int i=0;i<256;i++)table[i]=CorrespondenceTexts.advance(s,i);return table;})[cp];
    }
    private static int width(HouseWriting.WritingStyle style,String word){
        int w=0;for(int at=0;at<word.length();){int cp=word.codePointAt(at);at+=Character.charCount(cp);w+=advance(style,cp);}return w;
    }
    /** The most lines the game wraps this page's text to. A line break in the text ends a line; an empty line is a line. */
    public static int lines(HouseWriting.WritingStyle style,String text){
        int lines=0;
        for(String paragraph:text.split("\n",-1)){
            lines++;int used=0;
            for(String word:paragraph.split(" ")){
                if(word.isEmpty())continue;
                int w=width(style,word);
                if(used>0&&used+SPACE+w>WIDTH){lines++;used=0;}
                // A word wider than the page runs on over as many lines as it needs.
                while(w>WIDTH){lines++;w-=WIDTH;}
                used+=(used>0?SPACE:0)+w;
            }
        }
        return lines;
    }
    /** Whether the game shows a page of this text whole. */
    public static boolean fits(HouseWriting.WritingStyle style,String text){return lines(style,text)<=LINES;}

    /** A sentence, and what parts it from the one before it when both are on a page: a space, a line's end, or an empty line. */
    private record Unit(String text,String gap){}
    /**
     * A paragraph's sentences: each ends at a full stop, a question mark or an exclamation mark, with any closing quotation mark or bracket
     * after it. What follows in lower case carries the sentence on ("Is that all?" she said; I waited... and it came), so no page ends there.
     */
    public static List<String> sentences(String paragraph){
        var out=new ArrayList<String>();int from=0;
        for(int at=0;at<paragraph.length();at++){
            char c=paragraph.charAt(at);if(c!='.'&&c!='?'&&c!='!')continue;
            int end=at+1;while(end<paragraph.length()&&"\"'\u201d\u2019)]".indexOf(paragraph.charAt(end))>=0)end++;
            if(end<paragraph.length()&&paragraph.charAt(end)!=' ')continue;
            int next=end;while(next<paragraph.length()&&paragraph.charAt(next)==' ')next++;
            if(next<paragraph.length()&&Character.isLowerCase(paragraph.charAt(next)))continue;
            if(c=='.'){int word=at;while(word>from&&Character.isLetter(paragraph.charAt(word-1)))word--;String before=paragraph.substring(word,at);
                if(TITLES.contains(before)||before.length()==1&&Character.isUpperCase(before.charAt(0)))continue;}
            out.add(paragraph.substring(from,end).strip());from=end;at=end-1;
        }
        if(from<paragraph.length()&&!paragraph.substring(from).isBlank())out.add(paragraph.substring(from).strip());
        return out;
    }
    private static String render(String head,List<Unit> page){
        var text=new StringBuilder(head.isEmpty()?"":head+"\n\n");
        for(int i=0;i<page.size();i++){if(i>0)text.append(page.get(i).gap());text.append(page.get(i).text());}
        return text.toString();
    }
    /**
     * The text in pages, each of which the game shows whole. The head, if there is one, stands over the first page's text with an empty
     * line under it. A line's end in the text, and an empty line between paragraphs, are kept where both sides of them are on one page.
     */
    public static List<String> split(HouseWriting.WritingStyle style,String head,String text){
        var units=new ArrayList<Unit>();String gap="\n";
        for(String line:text.strip().split("\n",-1)){
            String body=line.strip().replaceAll("[ \\t]+"," ");
            if(body.isEmpty()){gap="\n\n";continue;}
            boolean first=true;
            for(String sentence:sentences(body)){units.add(new Unit(sentence,first?gap:" "));first=false;}
            gap="\n";
        }
        var pages=new ArrayList<List<Unit>>();var page=new ArrayList<Unit>();
        for(int i=0;i<units.size();i++){
            var unit=units.get(i);String over=pages.isEmpty()?head:"";
            page.add(unit);
            if(fits(style,render(over,page)))continue;
            page.removeLast();
            if(!page.isEmpty()){pages.add(page);page=new ArrayList<>();i--;continue;}
            // A sentence longer than a page: as many of its words as the page holds, and the rest goes on as a sentence of its own.
            String[] words=unit.text().split(" ");int take=1;
            while(take<words.length&&fits(style,render(over,List.of(new Unit(String.join(" ",java.util.Arrays.copyOf(words,take+1)),unit.gap())))))take++;
            pages.add(new ArrayList<>(List.of(new Unit(String.join(" ",java.util.Arrays.copyOf(words,take)),unit.gap()))));
            if(take<words.length)units.set(i--,new Unit(String.join(" ",java.util.Arrays.copyOfRange(words,take,words.length))," "));
        }
        if(!page.isEmpty())pages.add(page);
        // A last page of a line or two takes the sentence before it, and the one before that, until it is more than two lines or the page
        // before it is down to one sentence.
        while(pages.size()>1){
            var last=pages.getLast();var before=pages.get(pages.size()-2);
            if(lines(style,render("",last))>2||before.size()<2)break;
            var moved=new ArrayList<Unit>();moved.add(before.getLast());moved.addAll(last);
            if(!fits(style,render("",moved)))break;
            before.removeLast();pages.set(pages.size()-1,moved);
        }
        var out=new ArrayList<String>();
        for(int i=0;i<pages.size();i++)out.add(render(i==0?head:"",pages.get(i)));
        if(out.isEmpty()&&!head.isEmpty())out.add(head);
        return List.copyOf(out);
    }
}
