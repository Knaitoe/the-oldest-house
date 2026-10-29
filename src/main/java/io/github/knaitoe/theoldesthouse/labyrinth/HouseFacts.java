package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseCorrespondence;
import io.github.knaitoe.theoldesthouse.house.HouseDeathPortraits;
import io.github.knaitoe.theoldesthouse.house.HouseExperience;
import io.github.knaitoe.theoldesthouse.opening.OpeningSequence;
import io.github.knaitoe.theoldesthouse.opening.OpeningStage;
import java.util.*;
import net.minecraft.server.level.ServerPlayer;

/**
 * 0.4.165: what the save already holds for each journey advancement (docs/ADVANCEMENTS_0_4_165.md). Every test reads a
 * record the House keeps for its own reasons; none is written for the menu. The few moments the save never kept (the room
 * between, a stray fed, a companion carried through, something bought back from the Mother) are awarded where they happen,
 * and the advancement itself is then the record. Chalk and the spool are vanilla inventory criteria in their JSON.
 */
public final class HouseFacts {
    private HouseFacts(){}
    private static final Set<FinaleProgress.Phase> AT_THE_CELL=EnumSet.of(FinaleProgress.Phase.FIGHT,FinaleProgress.Phase.COLLAPSE,FinaleProgress.Phase.ESCAPE,
            FinaleProgress.Phase.RELEASE,FinaleProgress.Phase.HOMEWARD,FinaleProgress.Phase.ESCAPED,FinaleProgress.Phase.WITNESSED,FinaleProgress.Phase.LOCKED_OUT,FinaleProgress.Phase.IMPRISONED);

    static final List<HouseAdvancements.Step> JOURNEY=List.of(
            new HouseAdvancements.Step("letter",p->{var s=OpeningSequence.state(p);return s.stage().isAtLeast(OpeningStage.LETTER_DELIVERED)||s.letterDay()>=0||manor(p);}),
            new HouseAdvancements.Step("manor",HouseFacts::manor),
            // The hallway's far door is the labyrinth's first door, so anyone who has reached a place has walked the hallway.
            new HouseAdvancements.Step("hallway",p->!data(p).visited(p.getUUID()).isEmpty()),
            new HouseAdvancements.Step("junction",p->data(p).visited(p.getUUID()).contains("junction")),
            new HouseAdvancements.Step("six_doors",p->deepest(p)>=LabyrinthPacing.STORY_DEPTH),
            new HouseAdvancements.Step("growl",p->Growl.GrowlData.get(p.server).heard(p.getUUID())>0),
            new HouseAdvancements.Step("stone",p->{for(String id:data(p).visited(p.getUUID())){var place=LabyrinthPlace.byId(id);if(place!=null&&StoneHalls.isStone(place))return true;}return false;}),
            new HouseAdvancements.Step("twenty_doors",p->deepest(p)>=20),
            new HouseAdvancements.Step("camp",p->finale(p).getBoolean("Discovered")),
            new HouseAdvancements.Step("bound",p->StaircaseStory.found(data(p).stateEntry(StaircaseStory.STATE,p.getStringUUID()))>=1),
            new HouseAdvancements.Step("first_fire",p->StaircaseFire.flames(finale(p))>=1),
            new HouseAdvancements.Step("five_fires",p->StaircaseFire.flames(finale(p))>=StaircaseFire.REQUIRED),
            new HouseAdvancements.Step("cell",p->finale(p).getBoolean("Warned")||AT_THE_CELL.contains(FinaleProgress.phase(finale(p)))),
            new HouseAdvancements.Step("ending_witness",p->FinaleProgress.phase(finale(p))==FinaleProgress.Phase.WITNESSED),
            new HouseAdvancements.Step("ending_wound",p->FinaleProgress.phase(finale(p))==FinaleProgress.Phase.ESCAPED),
            new HouseAdvancements.Step("ending_locked",p->FinaleProgress.phase(finale(p))==FinaleProgress.Phase.LOCKED_OUT),
            new HouseAdvancements.Step("mother",p->{for(var e:MotherCollection.get(p.server).all())if(p.getUUID().equals(e.owner))return true;return false;}),
            new HouseAdvancements.Step("letters",p->{var own=HouseCorrespondence.record(data(p),p.getUUID());return own.getInt("ReadCount")>=1||!own.getCompound("LegacyUsed").isEmpty();}),
            new HouseAdvancements.Step("likeness",p->{var own=HouseDeathPortraits.record(p.server,p.getUUID());return own.getBoolean("FirstDeath")&&own.hasUUID("Painting");}));

    private static LabyrinthData data(ServerPlayer p){return LabyrinthData.get(p.server);}
    private static net.minecraft.nbt.CompoundTag finale(ServerPlayer p){return FinaleProgress.player(p.server,p.getUUID());}
    private static boolean manor(ServerPlayer p){return HouseExperience.record(data(p),p.getUUID()).getBoolean("ManorSeen")||OpeningSequence.state(p).enteredHouse();}
    /** The deepest this reader has been: the House's saved maximum, or where they stand now. */
    private static int deepest(ServerPlayer p){return Math.max(HouseExperience.record(data(p),p.getUUID()).getInt("Deepest"),data(p).returnDepth(p.getUUID()));}
    /** The reader carries their own account (the Witness book is given with the first story and at the lectern). */
    static boolean holdsAccount(ServerPlayer p){
        var inventory=p.getInventory();
        for(int i=0;i<inventory.getContainerSize();i++)if(WitnessAccount.ownedBook(inventory.getItem(i),p.getUUID()))return true;
        return false;
    }
}
