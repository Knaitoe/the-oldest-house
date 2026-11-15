package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.608, The Ones Already Inside, its eighth slice (docs/THE_ONES_ALREADY_INSIDE_PLAN.md, N7: "the key and Rae's four turns"). The
 * release key for Daniel's straps.
 *
 * <ul>
 * <li><b>Where it is.</b> On its hook in the workroom until Rae goes up to find Vera; she takes it then (the hook is empty from that
 * tick) and keeps it in her coat. The house keeps where it is: on the hook, with Rae, in the cupboard under the stair, with Vera, in a
 * reader's hand (by their id), or used.</li>
 * <li><b>Seen.</b> In Act II, while Rae stands in her place and nobody is looking at her, she takes it out and looks at it for three
 * seconds. A reader who looks at her then sees it, and she puts it away.</li>
 * <li><b>Asked.</b> A reader who saw it ("You have the key.") or who heard her ask in the hall and watched her go straight up ("You
 * knew the way up.") has her confession, the script's three lines, and then her four turns: keep it and take it to Vera (the reader
 * staying with Dan); give it to the reader; give it to Daniel herself. Or the reader says nothing.</li>
 * <li><b>Hidden.</b> Seen and not asked about for half a minute, Rae walks to the cupboard under the stair, puts it in and shuts the
 * door, and goes back to Dan. A reader who opens that cupboard finds it.</li>
 * <li><b>Used.</b> A reader with it in hand, in Daniel's room, puts it to the straps (or to him): they open, and he walks out through
 * his door, along the back passage and the hall, into the parlor. Rae, who did not do it, goes to the parlor's door and stands in it,
 * between him and Dan. Given to him by Rae herself, she says she is sorry; he says not to; she unlocks him, and he walks the same way.
 * </li>
 * <li><b>Dan told.</b> A reader who heard the confession can tell Dan that Rae knew. He says it to her, and she answers him.</li>
 * </ul>
 *
 * <p>All of it is the house's, for every reader in it; what each reader saw, asked and was told is their own record.
 */
public final class LodgingKey {
    private LodgingKey(){}
    /** The house's record: where the key is, and what Rae is about; the reader's own: what they saw, asked and heard. */
    public static final String WHERE="Key0608",HOOK="hook",RAE="rae",CUPBOARD="cupboard",VERA="vera",USED="used",
        SEEN_AT="KeySeenAt0608",CONFESSED="RaeConfessed0608",FREE="DanielFree0608",IN="DanielIn0608",BY_RAE="FreedByRae0608",TASK="RaeTask0608",TASK_AT="RaeTaskAt0608",
        TOLD_DAN="DanToldRae0608",SAID_DONT="DanielSaidDont0608";
    public static final String SAW_KEY="SawKey0608",HEARD_CONFESSION="HeardConfession0608",ASKED_KEY="AskedKey0608",TOLD_DAN_RAE="ToldDanRae0608",FOUND_KEY="FoundKey0608",
        USED_KEY="UsedKey0608",TURN="RaeTurn0608";
    /** What a reader asked of Rae: keep it and take it to Vera, give it to them, give it to Daniel herself. */
    public static final int TO_VERA=1,GIVE_ME=2,YOURSELF=3;
    /** What Rae is about: nothing; going to the cupboard, at it, back; up to Vera; to Daniel, with him; into the parlor's door. */
    public static final int NONE=0,HIDE_GO=1,HIDE_AT=2,HIDE_BACK=3,UP_TO_VERA=4,TO_DANIEL=5,WITH_DANIEL=6,DOORWAY=7;
    /** How often she takes it out to look at it, for how long, and how long after it is seen she hides it if nobody has asked. */
    public static final int GLANCE_EVERY=400,GLANCE=60,HIDE_AFTER=600;
    /** How far a reader sees her hand from, and how near the middle of their view she must be. */
    public static final double SEE=12,SEE_COS=.94;
    /** Between her words and his at the frame, and from his to the straps. */
    public static final int SORRY_GAP=50,UNLOCK_GAP=40;

    // ------------------------------------------------------------------------------------------------ where everybody goes
    /** Rae at the cupboard under the stair, facing its door (every spot and leg in the middle of its cell, a body clear of the walls); beside Vera in the corridor; in front of Daniel's frame; in the parlor's door. */
    public static final LodgingPeople.Spot RAE_CUPBOARD=new LodgingPeople.Spot(1.0,LodgingSite.GROUND,-9.6,-90,LodgingPeople.WAIT),
        RAE_BY_VERA=new LodgingPeople.Spot(1.6,LodgingSite.UPPER,-20.9,122,LodgingPeople.WAIT),
        RAE_AT_FRAME=new LodgingPeople.Spot(-6.4,LodgingSite.GROUND,-27.0,140,LodgingPeople.WAIT),
        RAE_DOORWAY=new LodgingPeople.Spot(-2.5,LodgingSite.GROUND,-8.5,90,LodgingPeople.WAIT),
        /** Daniel in the parlor just inside its door, clear of the armchair, turned to the door. */
        DANIEL_PARLOR=new LodgingPeople.Spot(-5.5,LodgingSite.GROUND,-6.5,-135,LodgingPeople.WAIT);
    /** From her place in the hall to the cupboard; up the stair to Vera; to Daniel's frame (his door on leg 3); to the parlor's door. */
    private static final List<Vec3> TO_CUPBOARD=List.of(new Vec3(.5,0,-5),new Vec3(.5,0,-9.6)),
        UP=List.of(new Vec3(3.5,0,-5.5),new Vec3(3.5,5,-12.5),new Vec3(.5,5,-15),new Vec3(.5,5,-19.5)),
        TO_FRAME=List.of(new Vec3(0,0,-5),new Vec3(0,0,-14),new Vec3(-7.5,0,-14),new Vec3(-7.5,0,-18.5),new Vec3(-5.6,0,-20),new Vec3(-5.6,0,-27)),
        TO_DOORWAY=List.of(new Vec3(0,0,-5),new Vec3(0,0,-8.5));
    /** From the cupboard back to her place. */
    private static final List<Vec3> FROM_CUPBOARD=List.of(new Vec3(.5,0,-9.6),new Vec3(.5,0,-5));
    /** Daniel out of the frame, through his door (leg 4), along the back passage, through the hall and the parlor's door (leg 8). */
    private static final List<Vec3> DANIEL_OUT=List.of(new Vec3(-7.5,0,-27.3),new Vec3(-9.4,0,-26.2),new Vec3(-9.4,0,-20),new Vec3(-7.5,0,-18.5),new Vec3(-7.5,0,-14),
        new Vec3(0,0,-14),new Vec3(0,0,-8.5),new Vec3(-2.6,0,-8.5),new Vec3(-5.6,0,-8.5));
    public static final int DANIEL_DOOR_LEG=4,PARLOR_DOOR_LEG=8;

    /** Where one of the four stands now, the key's turns over the chapter's (LodgingStory.spot). */
    public static LodgingPeople.Spot spot(MinecraftServer s,BlockPos b,int stage,int who){
        var h=LodgingStory.house(s,b);
        // 0.4.611: while the two men are put in one room, the meeting has them.
        var meeting=LodgingMeeting.spot(s,b,who);if(meeting!=null)return meeting;
        if(who==LodgingPeople.DANIEL&&h.getBoolean(FREE))return DANIEL_PARLOR;
        if(who==LodgingPeople.RAE)switch(h.getInt(TASK)){
            case HIDE_GO,HIDE_AT->{return RAE_CUPBOARD;}
            case UP_TO_VERA->{return RAE_BY_VERA;}
            case TO_DANIEL,WITH_DANIEL->{return RAE_AT_FRAME;}
            case DOORWAY->{return RAE_DOORWAY;}
            default->{}
        }
        return LodgingStory.spot(stage,who);
    }
    public static boolean free(MinecraftServer s,BlockPos b){return LodgingStory.house(s,b).getBoolean(FREE);}
    public static String where(MinecraftServer s,BlockPos b){var w=LodgingStory.house(s,b).getString(WHERE);return w.isEmpty()?HOOK:w;}
    private static void put(MinecraftServer s,BlockPos b,String where){var h=LodgingStory.house(s,b);h.putString(WHERE,where);LodgingStory.save(s,b,h);}

    // ------------------------------------------------------------------------------------------------ the key itself
    /** The key, for this house. */
    public static ItemStack stack(BlockPos b){
        var out=new ItemStack(LodgingRegistry.KEY.get());var tag=new CompoundTag();tag.putLong("LodgingHouse",b.asLong());
        out.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));return out;
    }
    /** Whether this is the key to this house's straps. */
    public static boolean is(ItemStack s,BlockPos b){
        if(!s.is(LodgingRegistry.KEY.get()))return false;var data=s.get(DataComponents.CUSTOM_DATA);
        return data!=null&&data.copyTag().getLong("LodgingHouse")==b.asLong();
    }
    /** The hand this reader holds the key in, or null. */
    private static @Nullable net.minecraft.world.InteractionHand hand(ServerPlayer p,BlockPos b){
        if(is(p.getMainHandItem(),b))return net.minecraft.world.InteractionHand.MAIN_HAND;
        if(is(p.getOffhandItem(),b))return net.minecraft.world.InteractionHand.OFF_HAND;
        return null;
    }
    /** Rae takes it off its hook on her way into the workroom (LodgingStory's FETCHING). */
    static void taken(ServerLevel l,BlockPos b,CompoundTag h){
        if(!h.getString(WHERE).isEmpty())return;h.putString(WHERE,RAE);
        LodgingSite.place(l,b,LodgingSite.KEY_HOOK,LodgingSite.thing(LodgingThing.Kind.KEY_HOOK,Direction.WEST,1));
    }

    // ------------------------------------------------------------------------------------------------ every tick
    /** Rae's glance at it, now: until when, at this house. */
    private static final Map<BlockPos,Long> GLANCING=new HashMap<>();
    public static void clearAll(){GLANCING.clear();}
    public static void reset(BlockPos b){GLANCING.remove(b);}
    /** Once a tick for the house (LodgingPeople.tick). */
    static void tick(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        var s=l.getServer();var h=LodgingStory.house(s,b);int stage=h.getInt("Stage");long now=l.getGameTime();boolean dirty=false;
        var rae=LodgingPeople.find(l,b,LodgingPeople.RAE);
        // Her glance at it, in Act II, standing in her place with it in her coat.
        if(rae!=null&&stage>=LodgingStory.AFTER&&RAE.equals(h.getString(WHERE))&&h.getInt(TASK)==NONE){
            Long until=GLANCING.get(b);
            if(until==null){
                boolean placed=rae.position().distanceToSqr(LodgingPeople.world(b,spot(s,b,stage,LodgingPeople.RAE)))<.36;
                if(placed&&now%GLANCE_EVERY==0&&!rae.saying()&&here.stream().noneMatch(p->sees(p,rae))){
                    GLANCING.put(b.immutable(),now+GLANCE);rae.setItemSlot(EquipmentSlot.MAINHAND,stack(b));rae.setDropChance(EquipmentSlot.MAINHAND,0);rae.gesture(LodgingPeople.KEY);
                }
            }else{
                boolean seen=false;
                for(var p:here){
                    if(!sees(p,rae))continue;seen=true;var own=LodgingHouse.own(p);
                    if(!own.getBoolean(SAW_KEY)){own.putBoolean(SAW_KEY,true);LodgingHouse.save(p,own);LodgingHouse.cue(p,LodgingTexts.KEY_GLANCE);}
                }
                if(seen&&!h.contains(SEEN_AT)){h.putLong(SEEN_AT,now);dirty=true;}
                if(seen||now>=until)endGlance(b,rae);
            }
        }else if(GLANCING.containsKey(b)&&rae!=null)endGlance(b,rae);
        // Seen, and nobody has asked her: she hides it.
        if(rae!=null&&RAE.equals(h.getString(WHERE))&&h.getInt(TASK)==NONE&&h.contains(SEEN_AT)&&!h.getBoolean(CONFESSED)&&now-h.getLong(SEEN_AT)>=HIDE_AFTER&&!GLANCING.containsKey(b)){
            task(h,HIDE_GO);dirty=true;
        }
        if(rae!=null)dirty|=errand(l,b,h,rae,here);
        // Daniel, let out, walks to the parlor; once he is there Rae (if it was not her) stands in its door.
        if(h.getBoolean(FREE)&&!h.getBoolean(IN)){
            if(h.getInt("KeyLeg"+LodgingPeople.DANIEL)==DANIEL_DOOR_LEG&&!LodgingSite.open(l,b,LodgingSite.DANIEL_DOOR))openDaniels(l,b);
            if(LodgingStory.walkVia(l,b,h,"KeyLeg"+LodgingPeople.DANIEL,LodgingPeople.DANIEL,DANIEL_OUT,DANIEL_PARLOR,LodgingSite.PARLOR_DOOR,PARLOR_DOOR_LEG)){
                h.putBoolean(IN,true);dirty=true;
                if(!h.getBoolean(BY_RAE)&&h.getInt(TASK)==NONE&&rae!=null)task(h,DOORWAY);
            }else dirty=true;
        }
        if(dirty)LodgingStory.save(s,b,h);
    }
    private static void endGlance(BlockPos b,LiteraryActor rae){GLANCING.remove(b);rae.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);if(rae.gesture()==LodgingPeople.KEY)rae.gesture(LodgingPeople.WAIT);}
    /** Whether this reader, alive and in the house, is looking at her: near, with nothing between, she near the middle of their view. */
    static boolean sees(ServerPlayer p,LiteraryActor a){
        if(p.isSpectator()||!p.isAlive()||p.distanceToSqr(a)>SEE*SEE||!p.hasLineOfSight(a))return false;
        var to=a.position().add(0,1.2,0).subtract(p.getEyePosition()).normalize();
        return p.getViewVector(1).dot(to)>=SEE_COS;
    }
    private static void task(CompoundTag h,int task){h.putInt(TASK,task);h.remove(TASK_AT);for(var k:List.copyOf(h.getAllKeys()))if(k.startsWith("KeyLeg"+LodgingPeople.RAE))h.remove(k);}

    /** Rae on an errand: a step of it. True if the house's record changed. */
    private static boolean errand(ServerLevel l,BlockPos b,CompoundTag h,LiteraryActor rae,List<ServerPlayer> here){
        var s=l.getServer();int task=h.getInt(TASK);long now=l.getGameTime();String leg="KeyLeg"+LodgingPeople.RAE;
        switch(task){
            case HIDE_GO->{if(LodgingStory.walkVia(l,b,h,leg,LodgingPeople.RAE,TO_CUPBOARD,RAE_CUPBOARD,LodgingSite.WORK_DOOR,-1)){task(h,HIDE_AT);h.putLong(TASK_AT,now);
                if(!LodgingSite.open(l,b,LodgingSite.CUPBOARD))LodgingHouse.toggle(l,b,LodgingSite.CUPBOARD);}return true;}
            case HIDE_AT->{
                if(now-h.getLong(TASK_AT)<25)return false;
                h.putString(WHERE,CUPBOARD);if(LodgingSite.open(l,b,LodgingSite.CUPBOARD))LodgingHouse.toggle(l,b,LodgingSite.CUPBOARD);
                task(h,HIDE_BACK);return true;
            }
            case HIDE_BACK->{if(LodgingStory.walkVia(l,b,h,leg,LodgingPeople.RAE,FROM_CUPBOARD,LodgingPeople.SPOTS[LodgingPeople.RAE],LodgingSite.WORK_DOOR,-1))task(h,NONE);return true;}
            case UP_TO_VERA->{
                if(h.contains(TASK_AT))return false;
                if(LodgingStory.walkVia(l,b,h,leg,LodgingPeople.RAE,UP,RAE_BY_VERA,LodgingSite.WORK_DOOR,-1)){h.putLong(TASK_AT,now);h.putString(WHERE,VERA);
                    // 0.4.609: she gives it to Vera, who keeps it in her hand.
                    var vera=LodgingPeople.find(l,b,LodgingPeople.VERA);if(vera!=null){vera.setItemSlot(EquipmentSlot.MAINHAND,stack(b));vera.setDropChance(EquipmentSlot.MAINHAND,0);}}
                return true;
            }
            case TO_DANIEL->{
                if(LodgingStory.walkVia(l,b,h,leg,LodgingPeople.RAE,TO_FRAME,RAE_AT_FRAME,LodgingSite.DANIEL_DOOR,3)){h.putInt(TASK,WITH_DANIEL);h.putLong(TASK_AT,now);
                    LodgingStory.speak(l,b,rae,"Rae",LodgingTexts.RAE_SORRY);}
                return true;
            }
            case WITH_DANIEL->{
                long at=now-h.getLong(TASK_AT);var daniel=LodgingPeople.find(l,b,LodgingPeople.DANIEL);
                boolean changed=false;
                if(at>=SORRY_GAP&&!h.getBoolean(SAID_DONT)){h.putBoolean(SAID_DONT,true);changed=true;
                    if(daniel!=null)LodgingStory.speak(l,b,daniel,LodgingPeople.name(LodgingPeople.DANIEL,new CompoundTag()),LodgingTexts.DANIEL_DONT);}
                // She stays at the frame once he is out (her place for the rest of it, until a later chapter moves her).
                if(at>=SORRY_GAP+UNLOCK_GAP&&!h.getBoolean(FREE)){
                    for(var p:here)if(p.distanceToSqr(rae)<=LodgingDaniel.HEARD*LodgingDaniel.HEARD)LodgingHouse.cue(p,LodgingTexts.RAE_UNLOCKS);
                    release(l,b,h,true);changed=true;
                }
                return changed;
            }
            case DOORWAY->{
                if(h.contains(TASK_AT))return false;
                if(LodgingStory.walkVia(l,b,h,leg,LodgingPeople.RAE,TO_DOORWAY,RAE_DOORWAY,LodgingSite.WORK_DOOR,-1)){h.putLong(TASK_AT,now);LodgingStory.speak(l,b,rae,"Rae",LodgingTexts.RAE_NOT_YET);}
                return true;
            }
            default->{return false;}
        }
    }
    /** Daniel's straps open: the frame as it is with them hanging, the key spent, and he is on his way. */
    static void release(ServerLevel l,BlockPos b,CompoundTag h,boolean byRae){
        var at=b.offset(LodgingSite.DANIEL_FRAME);var st=l.getBlockState(at);
        if(LodgingBlock.is(st,LodgingBlock.Kind.FRAME_BACK))l.setBlock(at,st.setValue(LodgingBlock.STAGE,st.getValue(LodgingBlock.STAGE)|4),3);
        l.playSound(null,at,SoundEvents.IRON_TRAPDOOR_OPEN,SoundSource.BLOCKS,.6F,1.5F);l.playSound(null,at,SoundEvents.CHAIN_BREAK,SoundSource.BLOCKS,.4F,.8F);
        h.putString(WHERE,USED);h.putBoolean(FREE,true);if(byRae)h.putBoolean(BY_RAE,true);
        openDaniels(l,b);
        var daniel=LodgingPeople.find(l,b,LodgingPeople.DANIEL);if(daniel!=null)daniel.gesture(LodgingPeople.WAIT);
    }
    private static void openDaniels(ServerLevel l,BlockPos b){
        var lower=l.getBlockState(b.offset(LodgingSite.DANIEL_DOOR));
        if(LodgingBlock.bolted(lower))LodgingSite.leaf(l,b,LodgingSite.DANIEL_DOOR,LodgingBlock.Kind.BOLT_DOOR,lower.getValue(LodgingBlock.FACING),false,0);
        if(!LodgingBlock.open(l.getBlockState(b.offset(LodgingSite.DANIEL_DOOR))))LodgingHouse.toggle(l,b,LodgingSite.DANIEL_DOOR);
    }

    // ------------------------------------------------------------------------------------------------ hands
    /** A hand on the straps, or on him, from his room. True if the key did something. */
    public static boolean straps(ServerPlayer p,BlockPos b){
        var s=p.server;var h=LodgingStory.house(s,b);
        if(h.getBoolean(FREE)){LodgingHouse.cue(p,LodgingTexts.STRAPS_OPEN);return true;}
        var hand=hand(p,b);if(hand==null)return false;
        var daniel=LodgingPeople.find(p.serverLevel(),b,LodgingPeople.DANIEL);if(daniel==null)return false;
        p.getItemInHand(hand).shrink(1);p.swing(hand,true);
        var own=LodgingHouse.own(p);own.putBoolean(USED_KEY,true);LodgingHouse.save(p,own);
        LodgingHouse.cue(p,LodgingTexts.KEY_TURNS);release(p.serverLevel(),b,h,false);LodgingStory.save(s,b,h);
        return true;
    }
    /** A reader opening the cupboard under the stair: what Rae left there. */
    public static void cupboard(ServerPlayer p,BlockPos b){
        var s=p.server;if(!CUPBOARD.equals(where(s,b))||!LodgingSite.open(p.serverLevel(),b,LodgingSite.CUPBOARD))return;
        put(s,b,p.getUUID().toString());var own=LodgingHouse.own(p);own.putBoolean(FOUND_KEY,true);LodgingHouse.save(p,own);
        if(!p.getInventory().add(stack(b)))p.drop(stack(b),false);
        LodgingHouse.cue(p,LodgingTexts.KEY_FOUND);
    }

    // ------------------------------------------------------------------------------------------------ Rae asked
    /** Whether Rae, spoken to in Act II, can be asked about the key by this reader. */
    static boolean askable(ServerPlayer p,BlockPos b,CompoundTag own){
        var h=LodgingStory.house(p.server,b);
        if(!RAE.equals(h.getString(WHERE))||h.getInt(TASK)!=NONE||own.getBoolean(HEARD_CONFESSION))return false;
        return own.getBoolean(SAW_KEY)||own.getBoolean(LodgingPeople.RAE_ASKED);
    }
    /** Rae spoken to by a reader who can ask her about it. */
    static void rae(ServerPlayer p,LiteraryActor rae,CompoundTag own){
        Talk.begin(p,LodgingPeople.TALK+LodgingPeople.RAE,"Rae");LodgingHouse.cue(p,LodgingTexts.RAE_WARY);
        var replies=new java.util.ArrayList<Talk.Reply>();
        if(own.getBoolean(SAW_KEY))replies.add(Talk.reply(LodgingTexts.R_KEY,LodgingKey::confess));
        if(own.getBoolean(LodgingPeople.RAE_ASKED))replies.add(Talk.reply(LodgingTexts.R_WAY_UP,LodgingKey::confess));
        replies.add(Talk.act(LodgingTexts.R_NOTHING,Talk::close));
        Talk.ask(p,replies);
    }
    private static void confess(ServerPlayer p){
        var b=LiteraryVignettes.base(p,LodgingPeople.PLACE);var rae=LodgingPeople.near(p,LodgingPeople.RAE);if(b==null||rae==null){Talk.close(p);return;}
        var own=LodgingHouse.own(p);own.putBoolean(ASKED_KEY,true);own.putBoolean(HEARD_CONFESSION,true);LodgingHouse.save(p,own);
        for(var line:LodgingTexts.RAE_CONFESSION)LodgingPeople.says(p,rae,"Rae",line);
        var h=LodgingStory.house(p.server,b);h.putBoolean(CONFESSED,true);LodgingStory.save(p.server,b,h);
        turns(p);
    }
    private static void turns(ServerPlayer p){
        Talk.ask(p,List.of(Talk.reply(LodgingTexts.R_TO_VERA,r->turned(r,TO_VERA)),Talk.reply(LodgingTexts.R_GIVE_ME,r->turned(r,GIVE_ME)),
            Talk.reply(LodgingTexts.R_YOURSELF,r->turned(r,YOURSELF)),Talk.act(LodgingTexts.R_NOTHING,Talk::close)));
    }
    private static void turned(ServerPlayer p,int turn){
        var b=LiteraryVignettes.base(p,LodgingPeople.PLACE);var rae=LodgingPeople.near(p,LodgingPeople.RAE);if(b==null||rae==null){Talk.close(p);return;}
        var s=p.server;var h=LodgingStory.house(s,b);
        // Somebody else's turn got there first: the key has gone where they sent it.
        if(!RAE.equals(h.getString(WHERE))||h.getInt(TASK)!=NONE){Talk.close(p);return;}
        var own=LodgingHouse.own(p);own.putInt(TURN,turn);LodgingHouse.save(p,own);
        switch(turn){
            case TO_VERA->{LodgingPeople.says(p,rae,"Rae",LodgingTexts.RAE_TO_VERA);task(h,UP_TO_VERA);}
            case GIVE_ME->{
                LodgingPeople.says(p,rae,"Rae",LodgingTexts.RAE_GIVES);LodgingHouse.cue(p,LodgingTexts.KEY_GIVEN);
                h.putString(WHERE,p.getUUID().toString());if(!p.getInventory().add(stack(b)))p.drop(stack(b),false);
            }
            default->{LodgingPeople.says(p,rae,"Rae",LodgingTexts.RAE_ALL_RIGHT);task(h,TO_DANIEL);}
        }
        LodgingStory.save(s,b,h);Talk.end(p);
    }

    // ------------------------------------------------------------------------------------------------ Dan told
    /** Whether this reader can tell Dan that Rae knew. */
    static boolean canTellDan(ServerPlayer p,BlockPos b,CompoundTag own){return own.getBoolean(HEARD_CONFESSION)&&!own.getBoolean(TOLD_DAN_RAE);}
    /** Told: he asks it back, and says it to her if she is near enough to hear; she answers him. */
    static void toldDan(ServerPlayer p){
        var own=LodgingHouse.own(p);own.putBoolean(TOLD_DAN_RAE,true);LodgingHouse.save(p,own);
        var dan=LodgingPeople.near(p,LodgingPeople.DAN);var b=LiteraryVignettes.base(p,LodgingPeople.PLACE);if(dan==null||b==null){Talk.close(p);return;}
        LodgingPeople.says(p,dan,"Dan",LodgingTexts.DAN_SHE_KNEW);
        var rae=LodgingPeople.find(p.serverLevel(),b,LodgingPeople.RAE);
        if(rae!=null&&rae.distanceToSqr(dan)<=LodgingDaniel.HEARD*LodgingDaniel.HEARD){
            LodgingPeople.says(p,dan,"Dan",LodgingTexts.DAN_YOU_KNEW);LodgingPeople.says(p,rae,"Rae",LodgingTexts.RAE_SO_YOU_CAME);
            var h=LodgingStory.house(p.server,b);h.putBoolean(TOLD_DAN,true);LodgingStory.save(p.server,b,h);
        }
        Talk.end(p);
    }
    /** For a case: whose hand it is in, if a reader's. */
    public static @Nullable UUID holder(MinecraftServer s,BlockPos b){try{return UUID.fromString(where(s,b));}catch(IllegalArgumentException e){return null;}}
}
