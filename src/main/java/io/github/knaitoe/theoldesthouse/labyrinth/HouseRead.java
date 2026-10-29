package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.server.level.ServerPlayer;

/** Read side for the six authored scene echoes. Observation and category assignment remain separate, deferred work. */
public final class HouseRead {
    public static final String STATE="house_read";
    public enum Trait {
        HAIR(Set.of("WRAPPED","BRAIDED_AND_OILED","BRUSHED_DRY","WASHED_AND_BARBERED")),
        SELF(Set.of("MASC","FEM","ANDROGYNOUS","UNSEEN")),
        /** 0.4.152: read by the shelter when it is built; the lake house partner is its first consumer. */
        DRAWN_TO(Set.of("MEN","WOMEN","BOTH","NO_ONE"));
        private final Set<String> values;
        Trait(Set<String> values){this.values=values;}
    }
    public enum Strength {UNKNOWN,TENTATIVE,SETTLED,UNCERTAIN}
    public record Category(String value,Strength strength){}
    private HouseRead(){}
    public static Optional<Category> category(ServerPlayer reader,Trait trait){
        var record=LabyrinthData.get(reader.server).stateEntry(STATE,reader.getUUID().toString()).getCompound(trait.name());
        String value=record.getString("Value");if(!trait.values.contains(value))return Optional.empty();
        try{
            var strength=Strength.valueOf(record.getString("Strength"));
            return strength==Strength.TENTATIVE||strength==Strength.SETTLED?Optional.of(new Category(value,strength)):Optional.empty();
        }catch(IllegalArgumentException ignored){return Optional.empty();}
    }
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
}
