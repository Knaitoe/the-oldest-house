package io.github.knaitoe.theoldesthouse.journal;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;

/**
 * One page of a reader's journal: their flowing writing, the lines they have lifted out and set somewhere on the page in a
 * shape of their choosing, and, faintly at the foot, where and when it was begun. Shared by the server, which keeps it, and
 * the screen, which draws it. The same pieces draw Zampanò's pages.
 */
public record JournalPage(String text, List<Placed> placed, String begun, int day, boolean house) {
    public static final int MAX_TEXT=420, MAX_NEWLINES=14, MAX_PLACED=8, MAX_RUN=96;
    public static final JournalPage BLANK=new JournalPage("",List.of(),"",0,false);

    /** How a lifted line is laid on the page. Mirroring and the angle apply to any of them. */
    public enum Shape {
        LINE, SPIRAL, CASCADE, COLUMN, BOX;
        public String id(){return name().toLowerCase(Locale.ROOT);}
        public Shape next(){return values()[(ordinal()+1)%values().length];}
        public static Shape of(String id){for(var s:values())if(s.id().equals(id))return s;return null;}
    }

    /** A lifted line: its words, its shape, where it sits as a fraction of the page, its turn in degrees, its size and whether it is mirrored. */
    public record Placed(String text, Shape shape, float x, float y, float angle, float size, boolean mirrored) {
        public static final float MIN_SIZE=.5F, MAX_SIZE=2.5F;
        public Placed with(float x,float y,float angle,float size,boolean mirrored,Shape shape){return new Placed(text,shape,Mth.clamp(x,0,1),Mth.clamp(y,0,1),Mth.wrapDegrees(angle),Mth.clamp(size,MIN_SIZE,MAX_SIZE),mirrored);}
        CompoundTag save(){var t=new CompoundTag();t.putString("Text",text);t.putString("Shape",shape.id());t.putFloat("X",x);t.putFloat("Y",y);t.putFloat("Angle",angle);t.putFloat("Size",size);if(mirrored)t.putBoolean("Mirrored",true);return t;}
        /** Null when the run is not one the screen could have made. */
        static Placed read(CompoundTag t){
            var shape=Shape.of(t.getString("Shape"));String text=t.getString("Text");
            if(shape==null||text.isBlank()||text.length()>MAX_RUN||text.indexOf('\n')>=0)return null;
            float x=t.getFloat("X"),y=t.getFloat("Y"),angle=t.getFloat("Angle"),size=t.contains("Size")?t.getFloat("Size"):1;
            if(!Float.isFinite(x)||!Float.isFinite(y)||!Float.isFinite(angle)||!Float.isFinite(size))return null;
            return new Placed(text,shape,Mth.clamp(x,0,1),Mth.clamp(y,0,1),Mth.wrapDegrees(angle),Mth.clamp(size,MIN_SIZE,MAX_SIZE),t.getBoolean("Mirrored"));
        }
    }

    public boolean empty(){return text.isBlank()&&placed.isEmpty();}
    public JournalPage withWriting(String text,List<Placed> placed){return new JournalPage(text,List.copyOf(placed),begun,day,house);}
    public JournalPage begunAt(String where,int day,boolean house){return new JournalPage(text,placed,where,day,house);}

    public CompoundTag save(){
        var t=new CompoundTag();t.putString("Text",text);
        var runs=new ListTag();for(var p:placed)runs.add(p.save());t.put("Placed",runs);
        if(!begun.isEmpty())t.putString("Begun",begun);if(day>0)t.putInt("Day",day);if(house)t.putBoolean("House",true);
        return t;
    }
    /** A saved page, trusted as the server wrote it. */
    public static JournalPage load(CompoundTag t){
        var runs=new ArrayList<Placed>();var list=t.getList("Placed",Tag.TAG_COMPOUND);
        for(int i=0;i<list.size()&&runs.size()<MAX_PLACED;i++){var p=Placed.read(list.getCompound(i));if(p!=null)runs.add(p);}
        return new JournalPage(t.getString("Text"),List.copyOf(runs),t.getString("Begun"),t.getInt("Day"),t.getBoolean("House"));
    }
    /**
     * The writing a reader's screen sent, or null when it is more than the screen allows. The server never takes a page's
     * header from the client: it stamps that itself.
     */
    public static JournalPage fromReader(CompoundTag t){
        String text=t.getString("Text").replace("\r","");
        if(text.length()>MAX_TEXT||text.chars().filter(c->c=='\n').count()>MAX_NEWLINES)return null;
        for(int i=0;i<text.length();i++){char c=text.charAt(i);if(c!='\n'&&(c<' '||c==127||c=='§'))return null;}
        var list=t.getList("Placed",Tag.TAG_COMPOUND);if(list.size()>MAX_PLACED)return null;
        var runs=new ArrayList<Placed>();
        for(int i=0;i<list.size();i++){var p=Placed.read(list.getCompound(i));if(p==null||p.text().chars().anyMatch(c->c<' '||c==127||c=='§'))return null;runs.add(p);}
        return new JournalPage(text,List.copyOf(runs),"",0,false);
    }
}
