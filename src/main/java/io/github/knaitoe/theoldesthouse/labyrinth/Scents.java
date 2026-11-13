package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.journal.TornPages;
import io.github.knaitoe.theoldesthouse.opening.CompanionOrders;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.559 (owner, 13 November: "Remember that pets should be able to track readers to specific vignettes too. Specific items or artifacts
 * should lead to specific vignettes"): what a thing held out to a reader's own companion smells of, and where the companion leads with it.
 * Something that is another reader's (a page torn from their journal, a keepsake or a lent thing marked as theirs) smells of that reader;
 * a thing a story gave up smells of that story. A story's scent puts that story behind one of the holder's doors, with the animal heard
 * behind it; a reader's scent does the same for whatever story that reader is in, and takes the holder into that reader's own copy of it
 * when it has room (SceneInstances). The holder's record (STATE) keeps the story it leads to, or the reader it follows and until when.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class Scents {
    private Scents(){}
    public static final String STATE="scents_0559";
    /** How long a companion keeps another reader's scent: five minutes. */
    public static final int READER_TICKS=6000;

    /** What a thing smells of: a story, or a reader (with the name to call them by). */
    public record Scent(@Nullable LabyrinthPlace place,@Nullable UUID reader,String name){}
    /** How taking a scent went. */
    public enum Taken{STORY,ANY_STORY,READER_THERE,READER_WAITING,NOTHING}

    /**
     * What {@code stack} smells of to a companion of {@code holder}: the reader it is another reader's, else the story it came from, else
     * nothing. A thing that is the holder's own smells only of its story.
     */
    public static @Nullable Scent of(ItemStack stack,UUID holder,@Nullable MinecraftServer s){
        if(stack.isEmpty())return null;
        var page=TornPages.ref(stack);
        var data=stack.get(DataComponents.CUSTOM_DATA);var tag=data==null?new CompoundTag():data.copyTag();
        if(page!=null&&!page.owner().equals(holder))return new Scent(null,page.owner(),name(s,page.owner(),tag.getString(TornPages.NAME)));
        if(tag.hasUUID(LiteraryVignettes.OWNER)&&!tag.getUUID(LiteraryVignettes.OWNER).equals(holder))
            return new Scent(null,tag.getUUID(LiteraryVignettes.OWNER),name(s,tag.getUUID(LiteraryVignettes.OWNER),""));
        String from=VignetteYields.of(stack);if(from==null)return null;
        var place=LabyrinthPlace.byId(from);return place==null?null:new Scent(place,null,"");
    }
    private static String name(@Nullable MinecraftServer s,UUID id,String fallback){
        if(s!=null){
            var online=s.getPlayerList().getPlayer(id);if(online!=null)return online.getGameProfile().getName();
            var cache=s.getProfileCache();if(cache!=null){var known=cache.get(id);if(known.isPresent())return known.get().getName();}
        }
        return fallback.isEmpty()?"someone":fallback;
    }

    // ------------------------------------------------------------------------------------------------ the holder's record
    private static CompoundTag record(LabyrinthData d,UUID holder){return d.stateEntry(STATE,holder.toString());}
    private static void record(LabyrinthData d,UUID holder,CompoundTag t){d.setStateEntry(STATE,holder.toString(),t);}
    public static void forget(LabyrinthData d,UUID holder){d.setStateEntry(STATE,holder.toString(),new CompoundTag());}
    /** The story a companion's scent is leading the holder to now, or null. */
    public static @Nullable LabyrinthPlace place(LabyrinthData d,UUID holder){
        var t=record(d,holder);return t.contains("Place")?LabyrinthPlace.byId(t.getString("Place")):null;
    }
    /** The reader whose scent the holder's companion has, or null. */
    public static @Nullable UUID reader(LabyrinthData d,UUID holder){var t=record(d,holder);return t.hasUUID("Reader")?t.getUUID("Reader"):null;}
    /** Whether a story is one the holder can still be dealt. */
    public static boolean open(LabyrinthData d,UUID holder,@Nullable LabyrinthPlace place){
        return place!=null&&LabyrinthDealer.vignettesAvailable(d,holder).contains(place);
    }

    /** A story's scent: that story while it is open to the holder, else any story they have not finished, as Hillary's always was. */
    public static Taken giveStory(LabyrinthData d,UUID holder,LabyrinthPlace place){
        if(open(d,holder,place)){var t=new CompoundTag();t.putString("Place",place.id());record(d,holder,t);d.setHillaryScent(holder,true);return Taken.STORY;}
        forget(d,holder);
        return LabyrinthDealer.giveScent(d,holder)==LabyrinthDealer.Scent.NOTHING?Taken.NOTHING:Taken.ANY_STORY;
    }
    /** A reader's scent, kept for READER_TICKS: their story at once if they are in one that is open to the holder, else when they are. */
    public static Taken giveReader(MinecraftServer s,LabyrinthData d,UUID holder,UUID reader,String name,@Nullable UUID animal){
        if(s.getPlayerList().getPlayer(reader)==null){forget(d,holder);return Taken.NOTHING;}
        var t=new CompoundTag();t.putUUID("Reader",reader);t.putString("Name",name);t.putLong("Until",s.overworld().getGameTime()+READER_TICKS);
        if(animal!=null)t.putUUID("Animal",animal);
        record(d,holder,t);
        var there=readerStory(s,d,holder,reader);
        if(there==null)return Taken.READER_WAITING;
        aim(d,holder,there);return Taken.READER_THERE;
    }
    private static void aim(LabyrinthData d,UUID holder,LabyrinthPlace place){
        var t=record(d,holder);t.putString("Place",place.id());record(d,holder,t);d.setHillaryScent(holder,true);
    }
    /** The story the reader is in now, if it is one the holder can still be dealt. */
    static @Nullable LabyrinthPlace readerStory(MinecraftServer s,LabyrinthData d,UUID holder,UUID reader){
        var q=s.getPlayerList().getPlayer(reader);var origin=HouseSavedData.get(s).houseOrigin();
        if(q==null||!q.isAlive()||q.isSpectator()||origin==null)return null;
        var place=LabyrinthPlaces.placeAt(origin,q.blockPosition());
        if(place==null)place=SceneInstances.placeAt(origin,q.blockPosition());
        if(place==null||!place.isVignette()||!q.level().dimension().equals(NovelRooms.dimension(place)))return null;
        return open(d,holder,place)?place:null;
    }
    /**
     * The dealer has put the scented story behind a door. A story's scent is spent; a reader's keeps its reader until the holder goes
     * through into that story, or it runs out, so the door takes them to that reader's own copy.
     */
    static void dealt(LabyrinthData d,UUID holder){if(reader(d,holder)==null)forget(d,holder);}
    /** The copy of {@code place} the reader whose scent the holder carries is in, or -1. */
    public static int copyToward(MinecraftServer s,LabyrinthData d,UUID holder,LabyrinthPlace place){
        var reader=reader(d,holder);if(reader==null||place!=place(d,holder))return -1;
        var q=s.getPlayerList().getPlayer(reader);if(q==null||!q.isAlive()||q.isSpectator())return -1;
        return SceneInstances.at(s,place,q.level().dimension(),q.position());
    }
    /** The holder has gone through into the story the scent led to: it is spent. */
    public static void arrived(LabyrinthData d,UUID holder,@Nullable LabyrinthPlace place){if(place!=null&&place==place(d,holder))forget(d,holder);}

    // ------------------------------------------------------------------------------------------------ the animal
    /**
     * A reader holds something out to their own companion. Inside the House it takes the scent and leads on; outside, there is no
     * House to find a way through. Returns false when the thing has no scent, so the companion's wheel opens as before.
     */
    public static boolean hold(TamableAnimal pet,ServerPlayer holder,ItemStack stack){
        var scent=of(stack,holder.getUUID(),holder.server);if(scent==null)return false;
        if(!holder.serverLevel().dimension().equals(HouseDimensions.INTERIOR)){
            ExpeditionRhythm.refuse(holder,pet.blockPosition(),"companion_route","Your companion cannot find that route here. Try inside the House.");return true;
        }
        var d=LabyrinthData.get(holder.server);
        var taken=scent.reader()!=null?giveReader(holder.server,d,holder.getUUID(),scent.reader(),scent.name(),pet.getUUID()):giveStory(d,holder.getUUID(),scent.place());
        follow(pet,holder,taken,scent.name());
        return true;
    }
    /** How the companion answers a scent taken: it leads on, waits with it, or lies down. */
    public static void follow(TamableAnimal pet,ServerPlayer holder,Taken taken,String reader){
        String name=pet.hasCustomName()?pet.getCustomName().getString():pet instanceof Cat?"The cat":"The dog";
        switch(taken){
            case NOTHING->{
                pet.playSound(pet instanceof Cat?SoundEvents.CAT_BEG_FOR_FOOD:SoundEvents.WOLF_WHINE,.8F,.8F);
                if(pet.isTame()){pet.setOrderedToSit(true);pet.setInSittingPose(true);}
                holder.displayClientMessage(Component.literal(name+" sniffs at it a long time, then lies down."),true);
            }
            case READER_WAITING->{
                pet.playSound(pet instanceof Cat?SoundEvents.CAT_AMBIENT:SoundEvents.WOLF_PANT,1F,1.2F);
                holder.displayClientMessage(Component.literal(name+" has "+reader+"'s scent, and waits for it to go somewhere."),true);
            }
            default->{
                lead(pet,holder);
                holder.displayClientMessage(Component.literal(taken==Taken.READER_THERE?name+" has "+reader+"'s scent, and goes.":name+" has the scent of it, and goes."),true);
            }
        }
    }
    /** The doors of the place the holder stands in are dealt again with the scent among them, and the companion makes for its door. */
    private static void lead(TamableAnimal pet,ServerPlayer holder){
        var origin=HouseSavedData.get(holder.server).houseOrigin();var d=LabyrinthData.get(holder.server);
        if(origin!=null&&holder.serverLevel().dimension().equals(HouseDimensions.INTERIOR)){
            var here=LabyrinthPlaces.placeAt(origin,holder.blockPosition());
            var random=RandomSource.create(holder.getUUID().getLeastSignificantBits()^holder.serverLevel().getGameTime());
            if(here!=null)LabyrinthDealer.dealPlace(d,holder.getUUID(),here,random);
            else{var door=d.door("hallway_end");if(door!=null)LabyrinthDealer.deal(d,holder.getUUID(),java.util.List.of(door),null,random);}
        }
        pet.playSound(pet instanceof Cat?SoundEvents.CAT_AMBIENT:SoundEvents.WOLF_AMBIENT,1F,1.2F);
        CompanionOrders.issue(pet,holder,CompanionOrders.Order.DEEPER);
    }

    // ------------------------------------------------------------------------------------------------ the trail
    /** Once a second: a reader's scent picks up their trail when they go into a story, and runs out after its time. */
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();if(s.getTickCount()%20!=0)return;var d=LabyrinthData.get(s);long now=s.overworld().getGameTime();
        for(String key:d.stateKeys(STATE)){
            UUID holder;try{holder=UUID.fromString(key);}catch(IllegalArgumentException bad){continue;}
            var t=record(d,holder);if(!t.hasUUID("Reader"))continue;
            if(t.getLong("Until")<now){forget(d,holder);continue;}
            var there=readerStory(s,d,holder,t.getUUID("Reader"));
            if(there==null||there.id().equals(t.getString("Place")))continue;
            aim(d,holder,there);
            var p=s.getPlayerList().getPlayer(holder);if(p==null)continue;
            var animal=t.hasUUID("Animal")?p.serverLevel().getEntity(t.getUUID("Animal")):null;
            if(animal instanceof TamableAnimal pet&&p.getUUID().equals(CompanionOrders.owner(pet))&&pet.isAlive()){
                lead(pet,p);p.displayClientMessage(Component.literal((pet.hasCustomName()?pet.getCustomName().getString():pet instanceof Cat?"The cat":"The dog")+" has found "+t.getString("Name")+"'s trail, and goes."),true);
            }
        }
    }
}
