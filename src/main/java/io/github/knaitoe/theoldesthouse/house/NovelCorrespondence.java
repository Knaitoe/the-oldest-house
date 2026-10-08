package io.github.knaitoe.theoldesthouse.house;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Source transcriptions from the owner's scanned edition. These are historical papers, not replies to the player. */
public final class NovelCorrespondence {
    public static final String PELAFINA_CHAIN="HOL_PELAFINA",PANTHER="HOL_Z04",ROOTS="HOL_Z03";
    private static final List<CorrespondenceTexts.Note> NOTES=load();
    private NovelCorrespondence(){}
    private static List<CorrespondenceTexts.Note> load(){
        var all=new ArrayList<CorrespondenceTexts.Note>();
        try(var stream=NovelCorrespondence.class.getResourceAsStream("/data/the_oldest_house/correspondence/novel_sources.json")){
            if(stream==null)throw new IllegalStateException("Missing novel source papers");
            for(var entry:JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonArray()){
                var n=entry.getAsJsonObject();all.add(new CorrespondenceTexts.Note(n.get("id").getAsString(),n.get("title").getAsString(),n.get("author").getAsString(),
                        HouseWriting.WritingStyle.valueOf(n.get("style").getAsString()),n.get("chain").getAsString(),n.get("installment").getAsInt(),n.get("depth").getAsInt(),
                        HouseMarginalia.Thread.valueOf(n.get("surface").getAsString()),n.get("text").getAsString()));
            }
        }catch(IOException e){throw new IllegalStateException("Cannot read novel source papers",e);}
        if(all.size()!=46||all.stream().map(CorrespondenceTexts.Note::id).distinct().count()!=46)throw new IllegalStateException("Incomplete source selection");
        return List.copyOf(all);
    }
    public static List<CorrespondenceTexts.Note> all(){return NOTES;}
    public static List<CorrespondenceTexts.Note> halls(){return NOTES.stream().filter(n->!n.id().equals(PANTHER)).toList();}
    public static CorrespondenceTexts.Note panther(){return NOTES.stream().filter(n->n.id().equals(PANTHER)).findFirst().orElseThrow();}
    public static boolean contains(String id){return id.startsWith("HOL_");}
}
