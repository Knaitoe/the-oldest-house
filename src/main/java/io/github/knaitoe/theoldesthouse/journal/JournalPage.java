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
 * the screen, which draws it. The same pieces draw Zampanò's pages. Rubbings (Rubbings) are laid on a page by the server
 * when the reader takes one; the reader's own edits never carry or remove them. The other hand's notes (CoAuthor) are
 * likewise the server's: a crossing-out lies over a word of the reader's and goes when they delete that word, and nothing
 * else of it can be erased.
 */
public record JournalPage(String text, List<Placed> placed, String begun, int day, boolean house, List<Rubbing> rubbings, List<Note> notes) {
    public static final int MAX_TEXT=420, MAX_NEWLINES=14, MAX_PLACED=8, MAX_RUN=96, MAX_NOTES=8;
    public static final JournalPage BLANK=new JournalPage("",List.of(),"",0,false,List.of(),List.of());

    /** A charcoal rubbing laid on the page: which object it was taken from, where it sits as a fraction of the page, and its tilt. */
    public record Rubbing(String id, float x, float y, float angle) {
        CompoundTag save(){var t=new CompoundTag();t.putString("Id",id);t.putFloat("X",x);t.putFloat("Y",y);t.putFloat("Angle",angle);return t;}
        static Rubbing read(CompoundTag t){var id=t.getString("Id");return Rubbings.byId(id)==null?null:new Rubbing(id,Mth.clamp(t.getFloat("X"),0,1),Mth.clamp(t.getFloat("Y"),0,1),Mth.wrapDegrees(t.getFloat("Angle")));}
    }

    /** What the other hand did: wrote on the page, struck through one of the reader's words, or answered a line beneath it. */
    public enum Mark {
        WRITTEN, STRUCK, DIRECTION;
        public String id(){return name().toLowerCase(Locale.ROOT);}
        public static Mark of(String id){for(var m:values())if(m.id().equals(id))return m;return null;}
    }
    /**
     * A note in the other hand's ink. A crossing-out and an answer are tied to a word of the reader's writing, the given use
     * of it counted from the start; a written note has no word. The position is where a written note lies, and where an answer
     * stays once the reader has taken its line away.
     */
    public record Note(Mark mark, String text, String word, int use, float x, float y) {
        /** Where the word it is tied to begins in this writing, or -1 when the reader has taken it away. */
        public int anchor(String writing){var at=uses(writing,word,mark==Mark.STRUCK);return use>=0&&use<at.size()?at.get(use):-1;}
        CompoundTag save(){var t=new CompoundTag();t.putString("Mark",mark.id());if(!text.isEmpty())t.putString("Text",text);if(!word.isEmpty()){t.putString("Word",word);t.putInt("Use",use);}t.putFloat("X",x);t.putFloat("Y",y);return t;}
        static Note read(CompoundTag t){
            var mark=Mark.of(t.getString("Mark"));String text=t.getString("Text"),word=t.getString("Word");
            if(mark==null||text.length()>MAX_RUN||word.length()>32||(mark==Mark.WRITTEN?text.isBlank():word.isBlank()))return null;
            float x=t.getFloat("X"),y=t.getFloat("Y");if(!Float.isFinite(x)||!Float.isFinite(y))return null;
            return new Note(mark,text,word,Math.max(0,t.getInt("Use")),Mth.clamp(x,0,1),Mth.clamp(y,0,1));
        }
    }
    /**
     * Where each whole use of a word begins in the writing. Names are matched exactly, other words in any case; a word joined
     * to letters or digits on either side is not a use ("Tess's" is a use of Tess, "Tessa" is not).
     */
    public static List<Integer> uses(String writing,String word,boolean exact){
        var at=new ArrayList<Integer>();if(word.isEmpty())return at;
        // Compared in place, so a position is always a position in the reader's own writing.
        for(int i=0,end=word.length();end<=writing.length();i++,end++)
            if(writing.regionMatches(!exact,i,word,0,word.length())&&(i==0||!Character.isLetterOrDigit(writing.charAt(i-1)))&&(end>=writing.length()||!Character.isLetterOrDigit(writing.charAt(end))))at.add(i);
        return at;
    }

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

    public boolean empty(){return text.isBlank()&&placed.isEmpty()&&rubbings.isEmpty()&&notes.isEmpty();}
    /** The reader's new writing. Rubbings and the other hand's notes stay, except a crossing-out whose word the reader has deleted. */
    public JournalPage withWriting(String text,List<Placed> placed){return new JournalPage(text,List.copyOf(placed),begun,day,house,rubbings,notes.stream().filter(n->n.mark()!=Mark.STRUCK||n.anchor(text)>=0).toList());}
    public JournalPage begunAt(String where,int day,boolean house){return new JournalPage(text,placed,where,day,house,rubbings,notes);}
    public JournalPage withRubbing(Rubbing r){var all=new ArrayList<>(rubbings);all.add(r);return new JournalPage(text,placed,begun,day,house,List.copyOf(all),notes);}
    public JournalPage withNote(Note n){var all=new ArrayList<>(notes);all.add(n);return new JournalPage(text,placed,begun,day,house,rubbings,List.copyOf(all));}

    public CompoundTag save(){
        var t=new CompoundTag();t.putString("Text",text);
        var runs=new ListTag();for(var p:placed)runs.add(p.save());t.put("Placed",runs);
        if(!begun.isEmpty())t.putString("Begun",begun);if(day>0)t.putInt("Day",day);if(house)t.putBoolean("House",true);
        if(!rubbings.isEmpty()){var list=new ListTag();for(var r:rubbings)list.add(r.save());t.put("Rubbings",list);}
        if(!notes.isEmpty()){var list=new ListTag();for(var n:notes)list.add(n.save());t.put("Notes",list);}
        return t;
    }
    /** A saved page, trusted as the server wrote it. */
    public static JournalPage load(CompoundTag t){
        var runs=new ArrayList<Placed>();var list=t.getList("Placed",Tag.TAG_COMPOUND);
        for(int i=0;i<list.size()&&runs.size()<MAX_PLACED;i++){var p=Placed.read(list.getCompound(i));if(p!=null)runs.add(p);}
        var rubbed=new ArrayList<Rubbing>();var marks=t.getList("Rubbings",Tag.TAG_COMPOUND);
        for(int i=0;i<marks.size();i++){var r=Rubbing.read(marks.getCompound(i));if(r!=null)rubbed.add(r);}
        var notes=new ArrayList<Note>();var ink=t.getList("Notes",Tag.TAG_COMPOUND);
        for(int i=0;i<ink.size()&&notes.size()<MAX_NOTES;i++){var n=Note.read(ink.getCompound(i));if(n!=null)notes.add(n);}
        return new JournalPage(t.getString("Text"),List.copyOf(runs),t.getString("Begun"),t.getInt("Day"),t.getBoolean("House"),List.copyOf(rubbed),List.copyOf(notes));
    }
    /**
     * The writing a reader's screen sent, or null when it is more than the screen allows. The server never takes a page's
     * header from the client: it stamps that itself.
     */
    public static JournalPage fromReader(CompoundTag t){
        String text=t.getString("Text").replace("\r","");
        if(text.length()>MAX_TEXT||text.chars().filter(c->c=='\n').count()>MAX_NEWLINES)return null;
        for(int i=0;i<text.length();i++){char c=text.charAt(i);if(c!='\n'&&(c<' '||c==127||c=='§'))return null;}
        // Shaped, turned and mirrored lines are the House's to set on a page, never the reader's (0.4.185): only writing is read.
        return new JournalPage(text,List.of(),"",0,false,List.of(),List.of());
    }
}
