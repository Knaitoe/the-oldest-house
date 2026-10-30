package io.github.knaitoe.theoldesthouse.journal;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseLayout;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPacing;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlace;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlaces;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/**
 * 0.4.199, journal step 7 (JOURNAL_DESIGN.md, "The post-mortem artifact"; the owner's idea 5, solo). The journal is never the
 * Mother's. When its reader dies in the House it comes back a stage more worn, water-stained and dog-eared, and the pages written
 * since the last death take the worst of it:
 * - dying in an ordinary hall, or the manor, it stays where they fell, lying open, and never despawns;
 * - dying in a story room (rooms that reset, private copies, the scenes outside), it is not there: it waits, and lies by the door
 *   of the third hall they come into after.
 * Picking it up gives it back, and nothing in it is lost. Outside the House it drops like anything else, and the loss rule holds.
 * With keepInventory it stays in their hands, untouched.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class PostMortem {
    public static final int HALLS=3;
    public static final String OPEN="JournalOpen";
    private PostMortem(){}

    /** Its own reader's journal among what they dropped, taken out of the drops before anything else can claim them. */
    @SubscribeEvent(priority=EventPriority.HIGH) public static void drops(LivingDropsEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        var data=Journals.get(p.server);ItemEntity journal=null;
        for(var d:e.getDrops())if(data.check(p,d.getItem())==Journals.Opening.OPEN){journal=d;break;}
        if(journal==null||!HouseDimensions.isHouseDimension(p.level().dimension()))return;
        e.getDrops().remove(journal);
        // Kept in hand, it is untouched.
        if(p.serverLevel().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)){if(!p.getInventory().add(journal.getItem()))e.getDrops().add(journal);return;}
        var b=data.book(p.getUUID());worsen(b);data.setDirty();
        if(fellInHall(p)){
            var lying=new ItemEntity(p.serverLevel(),p.getX(),p.getY()+.1,p.getZ(),open(Journals.stack(p.getUUID(),b)));
            lying.setUnlimitedLifetime();lying.setPickUpDelay(40);lying.setDeltaMovement(Vec3.ZERO);p.serverLevel().addFreshEntity(lying);
        }else b.waiting=HALLS;
    }
    /** A stage more worn; the pages written since the last death are the ones soaked. */
    static void worsen(Journals.Book b){b.wear=Math.min(Journals.WORN,b.wear+1);b.soaked.clear();b.soaked.addAll(b.since);b.since.clear();}
    /** An ordinary hall of the House, or the manor: the journal stays where they fell. Anywhere else it is a story's, and waits. */
    public static boolean fellInHall(ServerPlayer p){
        if(!p.level().dimension().equals(HouseDimensions.INTERIOR))return false;
        var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null)return false;
        if(HouseLayout.isInsideDomesticVolume(p.getX()-origin.getX(),p.getY()-origin.getY(),p.getZ()-origin.getZ()))return true;
        var place=LabyrinthPlaces.placeAt(origin,p.blockPosition());
        return place!=null&&LabyrinthPacing.ordinary(place)&&!place.isVignette();
    }
    /** Coming into a hall: after a death in a story room, the third hall is where it lies, by the door. */
    public static void arrived(ServerPlayer p,LabyrinthPlace place){
        if(p.isSpectator()||place==null||!LabyrinthPacing.ordinary(place))return;
        var data=Journals.get(p.server);var b=data.books().get(p.getUUID());if(b==null||b.waiting<=0)return;
        b.waiting--;data.setDirty();if(b.waiting>0)return;
        var ahead=Vec3.directionFromRotation(0,p.getYRot()).scale(1.5);
        var lying=new ItemEntity(p.serverLevel(),p.getX()+ahead.x,p.getY()+.1,p.getZ()+ahead.z,open(Journals.stack(p.getUUID(),b)));
        lying.setUnlimitedLifetime();lying.setPickUpDelay(20);lying.setDeltaMovement(Vec3.ZERO);p.serverLevel().addFreshEntity(lying);
    }
    public static int waiting(net.minecraft.server.MinecraftServer server,java.util.UUID id){var b=Journals.get(server).books().get(id);return b==null?0:b.waiting;}
    /** Lying open where it fell; the flag goes the moment it is picked up (JournalItem). */
    static ItemStack open(ItemStack s){CustomData.update(DataComponents.CUSTOM_DATA,s,t->t.putBoolean(OPEN,true));return s;}
    public static boolean isOpen(ItemStack s){var d=s.get(DataComponents.CUSTOM_DATA);return d!=null&&d.copyTag().getBoolean(OPEN);}
}
