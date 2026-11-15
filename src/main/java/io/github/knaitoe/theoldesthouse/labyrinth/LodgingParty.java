package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;

/**
 * 0.4.617, The Ones Already Inside, its seventeenth slice (docs/THE_ONES_ALREADY_INSIDE_PLAN.md, "Party changes: late arrival,
 * withdrawal, disconnects, reconnects, the key's custody, everyone gone; solo").
 *
 * <ul>
 * <li><b>The key.</b> A reader holding the key who leaves the house, logs off, dies or simply has not got it any more puts it back
 * where it lives: in Rae's coat if she gave it, in the cupboard under the stair if it was found there. A key lying loose in the house
 * goes back the same way.</li>
 * <li><b>Late.</b> A reader whose first arrival comes after Rae has begun asking: before the crisis they come in at the hall and Dan
 * tells them in two lines what is going on (Rae asks them in her turn, as she asked the others); once the crisis is under way they come
 * in by the scullery door, with nothing invented for them.</li>
 * <li><b>Everybody gone.</b> The house is not reset: it keeps its chapter, the four where they were, the link and the taken, and its
 * clock waits for a reader (the people tick only while somebody is in it).</li>
 * <li><b>Solo.</b> Every mechanism one reader needs can be worked alone, slower and louder; nothing waits for a second reader.</li>
 * </ul>
 */
public final class LodgingParty {
    private LodgingParty(){}
    /** Where the key lives when its holder is gone: "rae" or "cupboard" (LodgingKey's own names). */
    public static final String LIVES="KeyLives0617",READERS="Readers0617";
    /** Each reader's own: their first arrival recorded, and how they came in late (1 by the hall, 2 by the scullery). */
    public static final String ARRIVED="Arrived0617",LATE="LateIn0617";
    public static final int HALL=1,SCULLERY=2,EVERY=20;
    /** In by the scullery's side door, facing into the house. */
    static final LodgingPeople.Spot SCULLERY_IN=new LodgingPeople.Spot(12.5,LodgingSite.GROUND,-23.5,90,0);

    // ------------------------------------------------------------------------------------------------ the key
    static void tick(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        if(l.getGameTime()%EVERY!=0)return;
        var s=l.getServer();var h=LodgingStory.house(s,b);var holder=LodgingKey.holder(s,b);
        if(holder!=null){
            var p=s.getPlayerList().getPlayer(holder);
            boolean gone=p==null||!p.isAlive()||p.serverLevel()!=l||!LodgingPeople.area(b).contains(p.position())||!carries(p,b);
            if(gone){
                if(p!=null)take(p,b);
                h.putString(LodgingKey.WHERE,h.contains(LIVES)?h.getString(LIVES):LodgingKey.RAE);
                loose(l,b);LodgingStory.save(s,b,h);
            }
        }else loose(l,b);
        // A reader who logged off with it and came back has not got it any more: it went back where it lives.
        holder=LodgingKey.holder(s,b);
        for(var p:here)if(!p.getUUID().equals(holder)&&carries(p,b))take(p,b);
    }
    /** Whether this reader has the key on them anywhere. */
    public static boolean carries(ServerPlayer p,BlockPos b){
        var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(LodgingKey.is(inv.getItem(i),b))return true;
        return LodgingKey.is(p.containerMenu.getCarried(),b);
    }
    private static void take(ServerPlayer p,BlockPos b){
        var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(LodgingKey.is(inv.getItem(i),b))inv.setItem(i,net.minecraft.world.item.ItemStack.EMPTY);
        if(LodgingKey.is(p.containerMenu.getCarried(),b))p.containerMenu.setCarried(net.minecraft.world.item.ItemStack.EMPTY);
    }
    /** A key lying on a floor of the house, dropped or left by a reader who died: gone back where it lives. */
    private static void loose(ServerLevel l,BlockPos b){for(var e:l.getEntitiesOfClass(ItemEntity.class,LodgingPeople.area(b),e->LodgingKey.is(e.getItem(),b)))e.discard();}
    /** Where it was handed over, so it can go back there. */
    public static void lives(CompoundTag house,String where){house.putString(LIVES,where);}

    // ------------------------------------------------------------------------------------------------ arriving late
    /** A reader's arrival (LodgingHouse.arrive). */
    static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        if(own.getBoolean(ARRIVED))return;
        var s=p.server;var h=LodgingStory.house(s,b);var list=h.getList(READERS,Tag.TAG_STRING);
        boolean first=list.isEmpty();
        list.add(StringTag.valueOf(p.getUUID().toString()));h.put(READERS,list);
        own.putBoolean(ARRIVED,true);
        if(!first&&LodgingStory.stage(s,b)>LodgingStory.ASKING){
            var l=p.serverLevel();
            if(crisis(l,b)){
                own.putInt(LATE,SCULLERY);
                p.teleportTo(l,b.getX()+SCULLERY_IN.x(),b.getY()+SCULLERY_IN.y(),b.getZ()+SCULLERY_IN.z(),SCULLERY_IN.look(),0);
                LodgingHouse.cue(p,LodgingTexts.IN_BY_SCULLERY);
            }else{
                own.putInt(LATE,HALL);
                var dan=LodgingPeople.find(l,b,LodgingPeople.DAN);
                for(var line:LodgingTexts.DAN_LATE)Talk.line(p,"Dan",line);
                if(dan!=null)LodgingStory.speak(l,b,dan,"Dan",LodgingTexts.DAN_LATE[0]);
            }
        }
        LodgingStory.save(s,b,h);
    }
    /** The crisis is under way: the first attempt, or any of the four ends. */
    public static boolean crisis(ServerLevel l,BlockPos b){
        var s=l.getServer();
        return LodgingMeeting.stage(l,b)>=LodgingMeeting.ATTEMPTING||LodgingReturn.phase(s,b)!=LodgingReturn.NONE||LodgingComplete.begun(s,b)||LodgingSever.severed(s,b)||LodgingNeither.dead(s,b);
    }
    /** For a case: the readers who have come in, in order. */
    public static List<String> readers(MinecraftServer s,BlockPos b){var list=LodgingStory.house(s,b).getList(READERS,Tag.TAG_STRING);var out=new java.util.ArrayList<String>();for(int i=0;i<list.size();i++)out.add(list.getString(i));return out;}
}
