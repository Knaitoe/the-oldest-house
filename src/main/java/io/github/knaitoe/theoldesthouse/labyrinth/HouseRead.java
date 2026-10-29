package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import java.util.function.Consumer;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerPlayer;

/**
 * What the House has read of one reader (0.4.159). The quiet rooms hand in structured signals as they happen; this class
 * turns them into one category per trait, with a strength, and keeps the room observations the account and the book recite.
 * Nothing here is sent to another reader's client or written to the playtest log.
 */
public final class HouseRead {
    public static final String STATE="house_read";
    public enum Trait {
        AGE(Set.of("ANALOG","TRANSITIONAL","DIGITAL")),
        HAIR(Set.of("WRAPPED","BRAIDED_AND_OILED","BRUSHED_DRY","WASHED_AND_BARBERED")),
        SELF(Set.of("MASC","FEM","ANDROGYNOUS","UNSEEN")),
        DRAWN_TO(Set.of("MEN","WOMEN","BOTH","NO_ONE")),
        HOME(Set.of("HAVEN","DANGER","RUNAWAY")),
        /** 0.4.168: the colour the reader gave their own face at the kitchen table. Only ever the crayon's colour; never named. */
        TONE(HouseReadKitchen.SKIN);
        private final Set<String> values;
        Trait(Set<String> values){this.values=values;}
        public boolean valid(String value){return values.contains(value);}
    }
    public enum Strength {UNKNOWN,TENTATIVE,SETTLED,UNCERTAIN}
    public record Category(String value,Strength strength){}
    private HouseRead(){}
    private static CompoundTag record(LabyrinthData d,UUID id){return d.stateEntry(STATE,id.toString());}
    public static Optional<Category> category(ServerPlayer reader,Trait trait){return category(LabyrinthData.get(reader.server),reader.getUUID(),trait);}
    /** Only a tentative or settled valid read; unknown, uncertain, malformed and missing records all read as nothing. */
    public static Optional<Category> category(LabyrinthData d,UUID id,Trait trait){
        if(!enabled(trait))return Optional.empty();var t=record(d,id).getCompound(trait.name());String value=t.getString("Value");if(!trait.valid(value))return Optional.empty();
        try{var strength=Strength.valueOf(t.getString("Strength"));return strength==Strength.TENTATIVE||strength==Strength.SETTLED?Optional.of(new Category(value,strength)):Optional.empty();}catch(IllegalArgumentException ignored){return Optional.empty();}
    }
    /** The read as it stands, uncertain included: for tests and the operator's status line only. */
    public static Category raw(LabyrinthData d,UUID id,Trait trait){var t=record(d,id).getCompound(trait.name());try{return new Category(t.getString("Value"),t.contains("Strength")?Strength.valueOf(t.getString("Strength")):Strength.UNKNOWN);}catch(IllegalArgumentException e){return new Category("",Strength.UNKNOWN);}}
    public static String value(LabyrinthData d,UUID id,Trait trait,boolean settled){return category(d,id,trait).filter(c->!settled||c.strength()==Strength.SETTLED).map(Category::value).orElse("");}
    public static Optional<String> hair(ServerPlayer reader,boolean settled){
        return category(reader,Trait.HAIR).filter(c->!settled||c.strength()==Strength.SETTLED).map(Category::value);
    }
    /** Who the reader is drawn to, only when settled (a larger change, per the design); empty otherwise. */
    public static String drawnTo(ServerPlayer reader){
        return category(reader,Trait.DRAWN_TO).filter(c->c.strength()==Strength.SETTLED).map(Category::value).orElse("");
    }
    /** Only a settled spoken read supplies an address; UNSEEN and every unread/uncertain state use the neutral line. */
    public static String address(ServerPlayer reader,String neutral,String masc,String fem,String androgynous){
        return category(reader,Trait.SELF).filter(c->c.strength()==Strength.SETTLED).map(c->switch(c.value()){
            case "MASC"->masc;case "FEM"->fem;case "ANDROGYNOUS"->androgynous;default->neutral;
        }).orElse(neutral);
    }
    /** Older readers' strangers are a man and a woman; everyone else's, and unread readers', a boy and a girl. */
    public static boolean older(LabyrinthData d,UUID id){var age=value(d,id,Trait.AGE,false);return age.equals("ANALOG")||age.equals("TRANSITIONAL");}
    /** The server option: a trait the operator has turned off is never inferred, and its saved read is ignored. */
    public static boolean enabled(Trait trait){return io.github.knaitoe.theoldesthouse.house.HouseConfig.readEnabled(trait==Trait.DRAWN_TO);}

    /**
     * One signal from a quiet room. A first signal opens a read; a second never stands alone. Agreeing signals settle it,
     * a disagreeing one makes it uncertain, and the next decides (settled if that value has been seen twice).
     */
    public static void signal(ServerPlayer p,Trait trait,String value,boolean second){
        if(!trait.valid(value)||!enabled(trait))return;var d=LabyrinthData.get(p.server);var own=record(d,p.getUUID());var t=own.getCompound(trait.name());var list=t.getList("Signals",Tag.TAG_COMPOUND);
        if(second&&list.isEmpty())return;if(list.size()>=16)return;
        var s=new CompoundTag();s.putString("Value",value);s.putBoolean("Second",second);s.putString("Room",room(p));s.putLong("Tick",p.serverLevel().getGameTime());list.add(s);t.put("Signals",list);
        settle(t);own.put(trait.name(),t);d.setStateEntry(STATE,p.getUUID().toString(),own);changed(p,trait);
    }
    /** A room revising its own first signal (the coatroom's glass turned after dressing): the trait starts again from it. */
    public static void restart(ServerPlayer p,Trait trait,String value){
        if(!enabled(trait))return;var d=LabyrinthData.get(p.server);var own=record(d,p.getUUID());own.remove(trait.name());d.setStateEntry(STATE,p.getUUID().toString(),own);signal(p,trait,value,false);
    }
    public static int signals(ServerPlayer p,Trait trait){return record(LabyrinthData.get(p.server),p.getUUID()).getCompound(trait.name()).getList("Signals",Tag.TAG_COMPOUND).size();}
    static void settle(CompoundTag t){
        String value="",tie="";var strength=Strength.UNKNOWN;var counts=new HashMap<String,Integer>();
        for(var tag:t.getList("Signals",Tag.TAG_COMPOUND)){var v=((CompoundTag)tag).getString("Value");int n=counts.merge(v,1,Integer::sum);
            switch(strength){
                case UNKNOWN->{value=v;strength=Strength.TENTATIVE;}
                case TENTATIVE,SETTLED->{if(v.equals(value))strength=Strength.SETTLED;else{strength=Strength.UNCERTAIN;tie=v;}}
                case UNCERTAIN->{value=v;tie="";strength=n>=2?Strength.SETTLED:Strength.TENTATIVE;}
            }}
        t.putString("Value",value);t.putString("Strength",strength.name());if(tie.isEmpty())t.remove("Tie");else t.putString("Tie",tie);
    }
    private static String room(ServerPlayer p){var room=HouseReadSites.inside(p);return room==null?"":room.key();}

    /** The room observations the account and the book recite: literal acts, never categories. */
    public static CompoundTag seen(LabyrinthData d,UUID id){return record(d,id).getCompound("Seen");}
    public static void see(ServerPlayer p,HouseReadSites.Room room,Consumer<CompoundTag> edit){
        var d=LabyrinthData.get(p.server);var own=record(d,p.getUUID());var seen=own.getCompound("Seen");var t=seen.getCompound(room.key());edit.accept(t);seen.put(room.key(),t);own.put("Seen",seen);d.setStateEntry(STATE,p.getUUID().toString(),own);
    }
    /** Writes a value only once: the first instinct is what the House keeps. */
    public static void first(ServerPlayer p,HouseReadSites.Room room,String key,String value){see(p,room,t->{if(!t.contains(key))t.putString(key,value);});}
    public static void flag(ServerPlayer p,HouseReadSites.Room room,String key){see(p,room,t->t.putBoolean(key,true));}
    private static void changed(ServerPlayer p,Trait trait){if(trait==Trait.AGE||trait==Trait.HAIR||trait==Trait.SELF)HouseReadLook.send(p);}
}
