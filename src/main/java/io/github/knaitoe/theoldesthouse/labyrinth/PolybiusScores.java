package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * 0.4.217, Polybius: the arcade's high-score tables, one for each of its three working games, kept for the whole server (every
 * reader who has played them, whoever is online). A reader holds one place on a table, their best; ten places are kept. The black
 * cabinet keeps no table anyone can read.
 */
public final class PolybiusScores {
    private PolybiusScores(){}
    static final String STATE="polybius_scores_0217";
    public static final int KEEP=10;
    public static final String[] NAMES={"MOTHLIGHT","UNDERTOW","SIGNAL",""};
    public record Entry(String name,UUID id,int score){}
    static String key(int game){return "Game"+game;}
    public static List<Entry> table(MinecraftServer s,int game){
        if(game<0||game>=PolybiusSite.BLACK)return new ArrayList<>();
        return read(LabyrinthData.get(s).stateEntry(STATE,key(game)).getList("Rows",Tag.TAG_COMPOUND));
    }
    static void save(MinecraftServer s,int game,List<Entry> rows){var t=new CompoundTag();t.put("Rows",write(rows));LabyrinthData.get(s).setStateEntry(STATE,key(game),t);}
    /** 0.4.413: a table's rows as they are saved, here and in a cabinet of the reader's own (HomeCabinetBlockEntity). */
    public static List<Entry> read(ListTag list){
        var out=new ArrayList<Entry>();
        for(Tag t:list){var c=(CompoundTag)t;if(c.hasUUID("Id"))out.add(new Entry(c.getString("Name"),c.getUUID("Id"),c.getInt("Score")));}
        return out;
    }
    public static ListTag write(List<Entry> rows){var list=new ListTag();for(var e:rows){var c=new CompoundTag();c.putString("Name",e.name());c.putUUID("Id",e.id());c.putInt("Score",e.score());list.add(c);}return list;}
    /** A finished game: the reader's best stands, and their place on the table is returned (1 for the top), or 0 for none. */
    public static int record(MinecraftServer s,int game,ServerPlayer p,int score){
        if(game<0||game>=PolybiusSite.BLACK||score<=0)return 0;
        var rows=table(s,game);int rank=record(rows,p,score);save(s,game,rows);return rank;
    }
    /** The same on any table's rows, which it changes: the reader's best stands, the ten best are kept, and their place is returned. */
    public static int record(List<Entry> rows,ServerPlayer p,int score){
        if(score<=0)return 0;
        var mine=rows.stream().filter(e->e.id().equals(p.getUUID())).findFirst().orElse(null);
        if(mine==null||mine.score()<score){rows.removeIf(e->e.id().equals(p.getUUID()));rows.add(new Entry(p.getGameProfile().getName(),p.getUUID(),score));}
        rows.sort(Comparator.comparingInt(Entry::score).reversed());while(rows.size()>KEEP)rows.remove(rows.size()-1);
        for(int i=0;i<rows.size();i++)if(rows.get(i).id().equals(p.getUUID()))return i+1;
        return 0;
    }
    /** A reader's best on a game, or 0. */
    public static int best(MinecraftServer s,int game,UUID id){for(var e:table(s,game))if(e.id().equals(id))return e.score();return 0;}
    public static int rank(MinecraftServer s,int game,UUID id){var t=table(s,game);for(int i=0;i<t.size();i++)if(t.get(i).id().equals(id))return i+1;return 0;}
    /** The black cabinet's price for a fourth play: every place the reader held on every table, gone. Returns how many. */
    public static int strike(MinecraftServer s,UUID id){int n=0;for(int g=0;g<PolybiusSite.BLACK;g++){var rows=table(s,g);if(rows.removeIf(e->e.id().equals(id))){n++;save(s,g,rows);}}return n;}
    /** A table as the cabinet's screen sends it. */
    public static CompoundTag tag(MinecraftServer s,int game){return tag(table(s,game));}
    public static CompoundTag tag(List<Entry> rows){var list=new ListTag();for(var e:rows){var c=new CompoundTag();c.putString("Name",e.name());c.putInt("Score",e.score());list.add(c);}var t=new CompoundTag();t.put("Rows",list);return t;}
    /** A table as it hangs over its cabinet: the top five. */
    public static Component board(MinecraftServer s,int game){
        MutableComponent c=Component.literal(game==PolybiusSite.BLACK?"":NAMES[game]+"\n").withStyle(ChatFormatting.LIGHT_PURPLE);
        c.append(Component.literal("HIGH SCORES\n").withStyle(ChatFormatting.YELLOW));
        if(game==PolybiusSite.BLACK){for(int i=0;i<5;i++)c.append(Component.literal("???  ------\n").withStyle(ChatFormatting.DARK_GRAY));return c;}
        var rows=table(s,game);
        for(int i=0;i<5;i++){var line=i<rows.size()?String.format("%d. %-12s %6d",i+1,rows.get(i).name(),rows.get(i).score()):String.format("%d. %-12s %6s",i+1,"---","------");c.append(Component.literal(line+"\n").withStyle(i==0?ChatFormatting.AQUA:ChatFormatting.WHITE));}
        return c;
    }
}
