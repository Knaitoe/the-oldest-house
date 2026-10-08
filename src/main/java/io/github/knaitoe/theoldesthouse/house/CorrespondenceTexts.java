package io.github.knaitoe.theoldesthouse.house;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Authored prose, with native book pages and stable writer hands. No player state lives here. */
public final class CorrespondenceTexts {
    public record Note(String id, String title, String author, HouseWriting.WritingStyle style,
                       String chain, int installment, int depth, HouseMarginalia.Thread surface, String text) {}
    private record Glyph(int codePoint, boolean erased) {}
    private static final List<Note> NOTES = loadNotes();
    private static final JsonObject ADVANCES = resource("advances").getAsJsonObject();
    private CorrespondenceTexts() {}

    private static JsonElement resource(String name) {
        try (var stream = CorrespondenceTexts.class.getResourceAsStream("/data/the_oldest_house/correspondence/"+name+".json")) {
            if (stream == null) throw new IllegalStateException("Missing correspondence: "+name);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (IOException e) { throw new IllegalStateException("Cannot read correspondence: "+name,e); }
    }
    private static List<Note> loadNotes() {
        var result = new ArrayList<Note>();
        for (var entry : resource("letters").getAsJsonArray()) {
            var n = entry.getAsJsonObject();
            result.add(new Note(n.get("id").getAsString(),n.get("title").getAsString(),n.get("author").getAsString(),
                    HouseWriting.WritingStyle.valueOf(n.get("style").getAsString()),n.get("chain").getAsString(),
                    n.get("installment").getAsInt(),n.get("depth").getAsInt(),
                    HouseMarginalia.Thread.valueOf(n.get("surface").getAsString()),n.get("text").getAsString()));
        }
        if (result.size()!=108 || result.stream().map(Note::id).distinct().count()!=108)
            throw new IllegalStateException("The approved correspondence must contain 108 distinct notes");
        return List.copyOf(result);
    }
    public static List<Note> originals() { return NOTES; }
    public static List<Note> all() {
        var all=new ArrayList<>(NOTES);all.addAll(NovelCorrespondence.all());return List.copyOf(all);
    }
    public static Note find(String id) { return all().stream().filter(n->n.id().equals(id)).findFirst().orElse(null); }
    public static ItemStack book(Note note, Map<String,String> facts) {
        String text=note.text();
        for (var fact:facts.entrySet()) text=text.replace("{"+fact.getKey()+"}",fact.getValue());
        if (text.contains("{reader}") || text.contains("{possession}") || text.contains("{companion}"))
            throw new IllegalArgumentException("Missing actual personal fact for "+note.id());
        return HouseWriting.book(note.title(),note.author(),pages(note.style(),text,NovelCorrespondence.contains(note.id())));
    }
    private static int advance(HouseWriting.WritingStyle style,int cp) {
        var table=ADVANCES.getAsJsonObject(style.name());
        String glyph=new String(Character.toChars(cp));
        // Unknown Unicode and the one plain-font memo use a conservative native glyph bound.
        return table!=null&&table.has(glyph)?table.get(glyph).getAsInt():cp==' '?4:cp<128?8:16;
    }
    private static List<Component> pages(HouseWriting.WritingStyle style,String text,boolean source) {
        var glyphs=new ArrayList<Glyph>();boolean erased=false;
        for(int offset=0;offset<text.length();) {
            if(text.startsWith("~~",offset)){erased=!erased;offset+=2;continue;}
            int cp=text.codePointAt(offset);offset+=Character.charCount(cp);glyphs.add(new Glyph(cp,erased));
        }
        var lines=new ArrayList<List<Glyph>>();var line=new ArrayList<Glyph>();int width=0;
        for(int at=0;at<glyphs.size();) {
            var g=glyphs.get(at);
            if(g.codePoint()=='\n'){lines.add(List.copyOf(line));line.clear();width=0;at++;continue;}
            if(g.codePoint()==' '){
                // A source separator must become either a space or a line break. Dropping it
                // near the width limit can join dots, short words or cipher initials.
                // Retain the old authored pagination path; already saved books are immutable.
                if(source&&!line.isEmpty()&&width+4>100){trim(line);lines.add(List.copyOf(line));line.clear();width=0;}
                if(!line.isEmpty()&&width+4<=100){line.add(g);width+=4;}at++;continue;
            }
            int end=at,wordWidth=0;
            while(end<glyphs.size()&&glyphs.get(end).codePoint()!=' '&&glyphs.get(end).codePoint()!='\n')
                wordWidth+=advance(style,glyphs.get(end++).codePoint());
            if(!line.isEmpty()&&width+wordWidth>100){trim(line);lines.add(List.copyOf(line));line.clear();width=0;}
            while(at<end){g=glyphs.get(at++);int size=advance(style,g.codePoint());
                if(width+size>100&&!line.isEmpty()){lines.add(List.copyOf(line));line.clear();width=0;}
                line.add(g);width+=size;
            }
        }
        trim(line);if(!line.isEmpty())lines.add(List.copyOf(line));
        var pages=new ArrayList<Component>();
        for(int start=0;start<lines.size();) {
            while(start<lines.size()&&lines.get(start).isEmpty())start++;
            if(start==lines.size())break;
            int end=Math.min(start+12,lines.size());
            while(end>start&&lines.get(end-1).isEmpty())end--;
            var page=Component.empty();
            for(int row=start;row<end;row++) {
                if(row>start)page.append(HouseWriting.page(style,"\n"));
                var buffer=new StringBuilder();boolean strike=false;
                for(var glyph:lines.get(row)) {
                    if(glyph.erased()!=strike&&!buffer.isEmpty()){append(page,style,buffer.toString(),strike);buffer.setLength(0);}
                    strike=glyph.erased();buffer.appendCodePoint(glyph.codePoint());
                }
                if(!buffer.isEmpty())append(page,style,buffer.toString(),strike);
            }
            pages.add(page);start=end;
        }
        return List.copyOf(pages);
    }
    private static void trim(ArrayList<Glyph> line){while(!line.isEmpty()&&line.getLast().codePoint()==' ')line.removeLast();}
    private static void append(net.minecraft.network.chat.MutableComponent page,HouseWriting.WritingStyle style,String text,boolean erased){
        page.append(HouseWriting.page(style,text).copy().withStyle(s->s.withStrikethrough(erased)));
    }
    /** Player-chosen names are displayed as literal text; never interpret them as markup. */
    public static String literal(String input) {
        var out=new StringBuilder();input.codePoints().filter(cp->!Character.isISOControl(cp)&&cp!='~'&&cp!='{'&&cp!='}')
                .limit(48).forEach(out::appendCodePoint);
        return out.isEmpty()?"an unnamed belonging":out.toString();
    }
}
