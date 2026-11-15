package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.604, The Ones Already Inside (N2): "Each reader's room has their name on a card in the door's holder. In the bed, asleep under
 * the blanket, is the reader's body as the House has it: their hair, their clothes, their face, too smooth, the eyes moving under the
 * lids." And: "The first body opens its eyes while its reader stands near it. It does not look at the reader. It looks for Vera."
 *
 * <ul>
 * <li>One body to a reader with a room, a LiteraryActor in the LODGING_DOUBLE role, bound to that reader (whose House body it is
 * drawn in: client.LodgingRenderers) and in the room's number as its phase. Everybody in the house sees it; it is that reader's.</li>
 * <li>It is made in the bed only while nobody, spectators included, has the bed in view; until then the bed is only made up.</li>
 * <li>Asleep (gesture ASLEEP) it lies on its back under the blanket, breathing, the eyes going under the lids. Once its own reader
 * has stood beside the bed for three seconds, all told, it opens its eyes (AWAKE) and turns its head on the pillow, away from them,
 * to wherever Vera is in the house. That is kept: it does not sleep again.</li>
 * <li>A hand on it says what it is; with Vera in hearing the reader can ask her, in the panel. A blow does nothing to it, or to
 * anybody ("Unfinished bodies are not yet linked"); with Vera in hearing she says so.</li>
 * </ul>
 */
public final class LodgingDoubles {
    private LodgingDoubles(){}
    public static final int ASLEEP=1,AWAKE=2;
    /** How near the bed its reader stands, and for how long, before it opens its eyes. */
    public static final double BESIDE=3.2;
    public static final int WAKE=60;
    /** How near Vera is to hear a question about it, or see a blow. */
    public static final double HEARING=14;
    /** How far its head turns on the pillow toward where Vera is. */
    public static final float TURN=62;
    /** Each reader's own record (LodgingHouse.own). */
    public static final String SAW="SawDouble0604",SAW_EYES="SawEyes0604",ASKED="AskedDouble0604",NOT_STAYING="NotStaying0604",STRUCK="StruckDouble0604";
    public static final String CARD_TAG="LodgingCard",TALK="lodging_double";

    /** How long each reader has stood beside their own sleeper, by house. */
    private static final Map<BlockPos,Map<UUID,Integer>> BESIDE_TICKS=new HashMap<>();
    static void forget(BlockPos b){BESIDE_TICKS.remove(b.immutable());}
    public static void clearAll(){BESIDE_TICKS.clear();}

    public static boolean is(@Nullable net.minecraft.world.entity.Entity e){return e instanceof LiteraryActor a&&a.role()==LiteraryActor.LODGING_DOUBLE;}
    /** The body asleep (or awake) in this room's bed at this house, if it is there. */
    public static @Nullable LiteraryActor sleeper(ServerLevel l,BlockPos b,int room){
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,LodgingPeople.area(b),LodgingDoubles::is))if(a.isAlive()&&a.phase()==room)return a;
        return null;
    }
    /** Where a body lies in a room's bed: the head cell's middle, on the floor of the room. */
    public static Vec3 lying(BlockPos b,int room){var c=b.offset(LodgingSite.bed(room));return new Vec3(c.getX()+.5,c.getY(),c.getZ()+.5);}

    // ------------------------------------------------------------------------------------------------ every tick
    /** Once a tick for the house (LodgingPeople.tick): sleepers made where they are owed, woken by their readers, heads turned. */
    static void tick(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        var s=l.getServer();
        if(l.getGameTime()%10==0)for(var reader:LodgingStory.guests(s,b)){
            int room=LodgingStory.room(s,b,reader);if(room<0||sleeper(l,b,room)!=null)continue;
            var at=lying(b,room);var cell=BlockPos.containing(at);
            if(!l.isLoaded(cell)||!l.areEntitiesLoaded(ChunkPos.asLong(cell))||!RoomActors.ready(l,LodgingPeople.area(b)))continue;
            // Never made in front of anybody: the bed is only made up until nobody has it in view.
            if(LiteraryVignettes.watched(l,at.add(0,.7,0)))continue;
            make(l,b,room,reader);
        }
        var ticks=BESIDE_TICKS.computeIfAbsent(b.immutable(),k->new HashMap<>());
        var vera=LodgingPeople.find(l,b,LodgingPeople.VERA);
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,LodgingPeople.area(b),LodgingDoubles::is)){
            if(!a.isAlive()||a.owner().isEmpty())continue;var owner=a.owner().get();
            // 0.4.612: up out of the bed, Vera has it (LodgingTaken).
            if(LodgingTaken.up(a))continue;
            hold(a,b);
            if(a.gesture()==ASLEEP){
                ServerPlayer reader=null;for(var p:here)if(p.getUUID().equals(owner))reader=p;
                if(reader!=null&&!reader.isSpectator()&&reader.position().distanceToSqr(a.position())<=BESIDE*BESIDE){
                    int t=ticks.merge(owner,1,Integer::sum);
                    if(t>=WAKE){a.gesture(AWAKE);ticks.remove(owner);
                        var own=LodgingHouse.own(reader);if(!own.getBoolean(SAW_EYES)){own.putBoolean(SAW_EYES,true);LodgingHouse.save(reader,own);LodgingHouse.cue(reader,LodgingTexts.DOUBLE_WAKES);}}
                }
                a.setYHeadRot(a.yBodyRot);
            }else{
                // Awake: the head turned on the pillow toward wherever Vera is, to the side of the bed she is on.
                var to=vera!=null?vera.position():Vec3.atCenterOf(b.offset(LodgingSite.ROOM_DOORS[Math.max(0,Math.min(5,a.phase()))]));
                a.setYHeadRot(a.yBodyRot+side(b,a.phase(),a.position(),to)*TURN);
            }
        }
    }
    /** Which way, on its pillow, the body's head turns toward a point: +1 to its right hand, -1 to its left. */
    static int side(BlockPos b,int room,Vec3 body,Vec3 toward){
        // Lying on its back with its head toward the pillow, its right hand is the head's direction turned a quarter clockwise seen from
        // below, which is (dz, -dx) of the direction its head points.
        var head=LodgingSite.bedFacing(Math.max(0,Math.min(5,room))).getOpposite();
        double rx=head.getStepZ(),rz=-head.getStepX();
        var d=toward.subtract(body);return d.x*rx+d.z*rz>=0?1:-1;
    }
    /** A body made, lying in the room's bed, bound to its reader. */
    private static void make(ServerLevel l,BlockPos b,int room,UUID reader){
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return;
        a.bind(LodgingPeople.PLACE,reader);a.appearance(LiteraryActor.LODGING_DOUBLE,room);a.gesture(ASLEEP);
        a.noPhysics=true;a.setNoGravity(true);a.setSilent(true);
        var at=lying(b,room);float yaw=LodgingSite.bedFacing(room).toYRot();
        a.moveTo(at.x,at.y,at.z,yaw,0);a.setYBodyRot(yaw);a.setYHeadRot(yaw);
        l.addFreshEntity(a);
    }
    /** Kept in its bed: nothing moves it. */
    private static void hold(LiteraryActor a,BlockPos b){
        var at=lying(b,a.phase());
        if(a.position().distanceToSqr(at)>.0004)a.setPos(at.x,at.y,at.z);
        a.setDeltaMovement(Vec3.ZERO);float yaw=LodgingSite.bedFacing(Math.max(0,Math.min(5,a.phase()))).toYRot();a.setYRot(yaw);a.setYBodyRot(yaw);
    }

    // ------------------------------------------------------------------------------------------------ the cards
    /** A reader's name on the card in their room's holder at the door: the holder shows its card, the name written on it. */
    static void card(ServerLevel l,BlockPos b,int room,String name){
        var holder=b.offset(LodgingSite.cardHolder(room));var st=l.getBlockState(holder);
        if(LodgingThing.is(st,LodgingThing.Kind.CARD_HOLDER))l.setBlock(holder,st.setValue(LodgingThing.STAGE,1),3);
        for(var old:l.getEntitiesOfClass(Display.TextDisplay.class,new AABB(holder).inflate(.5),d->d.getTags().contains(CARD_TAG)))old.discard();
        var d=EntityType.TEXT_DISPLAY.create(l);if(d==null)return;
        var tag=new CompoundTag();d.saveWithoutId(tag);
        tag.putString("text",Component.Serializer.toJson(Component.literal(name).withColor(0x2b2518),l.registryAccess()));
        tag.putString("billboard","fixed");tag.putInt("background",0);tag.putInt("line_width",400);
        // Small enough that the whole name sits inside the card's ten pixels.
        float scale=(float)Math.min(.5,.62/Math.max(1,name.length()*6*.025));
        var t=new CompoundTag();t.put("left_rotation",floats(0,0,0,1));t.put("right_rotation",floats(0,0,0,1));t.put("translation",floats(0,-.11F*scale,0));t.put("scale",floats(scale,scale,scale));
        tag.put("transformation",t);d.load(tag);
        boolean west=room<3;var face=west?Direction.EAST:Direction.WEST;
        // The card's face is four-fifths of a pixel off the wall; the name a hair in front of it.
        double x=holder.getX()+(west?.056:1-.056),y=holder.getY()+7.5/16,z=holder.getZ()+.5;
        d.moveTo(x,y,z,face.toYRot(),0);d.addTag(CARD_TAG);d.addTag(CARD_TAG+room);
        l.addFreshEntity(d);
    }
    private static net.minecraft.nbt.ListTag floats(float... v){var list=new net.minecraft.nbt.ListTag();for(float f:v)list.add(net.minecraft.nbt.FloatTag.valueOf(f));return list;}

    /** No bodies and no names: for a house built again from nothing, and for tests. */
    public static void reset(ServerLevel l,BlockPos b){
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,LodgingPeople.area(b),LodgingDoubles::is))a.discard();
        for(var d:l.getEntitiesOfClass(Display.TextDisplay.class,LodgingPeople.area(b),d->d.getTags().contains(CARD_TAG)))d.discard();
        for(int i=0;i<6;i++){var holder=b.offset(LodgingSite.cardHolder(i));var st=l.getBlockState(holder);if(LodgingThing.is(st,LodgingThing.Kind.CARD_HOLDER))l.setBlock(holder,st.setValue(LodgingThing.STAGE,0),3);}
        forget(b);
    }

    // ------------------------------------------------------------------------------------------------ a hand on it
    private static boolean mine(ServerPlayer p,LiteraryActor a){return a.owner().isPresent()&&a.owner().get().equals(p.getUUID());}
    private static @Nullable LiteraryActor veraNear(ServerPlayer p){
        var b=LiteraryVignettes.base(p,LodgingPeople.PLACE);if(b==null)return null;var v=LodgingPeople.find(p.serverLevel(),b,LodgingPeople.VERA);
        return v!=null&&v.distanceToSqr(p)<=HEARING*HEARING?v:null;
    }
    private static String whose(ServerPlayer p,LiteraryActor a){
        var b=LiteraryVignettes.base(p,LodgingPeople.PLACE);if(b==null||a.owner().isEmpty())return "";
        return LodgingStory.guestName(p.server,b,a.owner().get());
    }
    /** A hand on a body in a bed: what it is, and Vera asked about it if she is in hearing. */
    public static void touched(ServerPlayer p,LiteraryActor a){
        if(LiteraryVignettes.current(p)!=LodgingPeople.PLACE)return;var own=LodgingHouse.own(p);boolean mine=mine(p,a);
        if(mine&&!own.getBoolean(SAW)){own.putBoolean(SAW,true);LodgingHouse.save(p,own);}
        LodgingHouse.cue(p,mine?(a.gesture()==AWAKE?LodgingTexts.DOUBLE_OWN_AWAKE:LodgingTexts.DOUBLE_OWN):LodgingTexts.doubleOf(whose(p,a),a.gesture()==AWAKE));
        if(mine)ask(p);
    }
    /** The questions at the room or to Vera; Vera answers only if she is near enough to hear them. */
    static boolean ask(ServerPlayer p){
        if(veraNear(p)==null||!left(p))return false;
        Talk.begin(p,TALK,"Vera");offer(p);return true;
    }
    /** Whether anything is left to ask her. */
    private static boolean left(ServerPlayer p){var own=LodgingHouse.own(p);return !own.getBoolean(ASKED)||!own.getBoolean(NOT_STAYING);}
    /** What is left to ask, in the panel already open. */
    private static boolean offer(ServerPlayer p){
        var own=LodgingHouse.own(p);
        var replies=new java.util.ArrayList<Talk.Reply>();
        if(!own.getBoolean(ASKED)){replies.add(Talk.reply(LodgingTexts.R_WHO_THAT,r->answered(r,false)));replies.add(Talk.reply(LodgingTexts.R_WHAT_THIS,r->answered(r,false)));}
        if(!own.getBoolean(NOT_STAYING))replies.add(Talk.reply(LodgingTexts.R_NOT_STAYING,r->answered(r,true)));
        if(replies.isEmpty())return false;
        replies.add(Talk.act(LodgingTexts.R_LEAVE_IT,Talk::close));
        Talk.ask(p,replies);return true;
    }
    private static void answered(ServerPlayer p,boolean leaving){
        var vera=veraNear(p);if(vera==null){Talk.close(p);return;}var own=LodgingHouse.own(p);
        if(leaving){own.putBoolean(NOT_STAYING,true);LodgingHouse.save(p,own);Talk.line(p,"Vera",LodgingTexts.VERA_EITHER_WAY);vera.say("Vera",LodgingTexts.VERA_EITHER_WAY);vera.speaks(LodgingTexts.VERA_EITHER_WAY);}
        else{own.putBoolean(ASKED,true);LodgingHouse.save(p,own);for(var line:LodgingTexts.VERA_YOURS){Talk.line(p,"Vera",line);}vera.say("Vera",LodgingTexts.VERA_YOURS[0]);vera.speaks(LodgingTexts.VERA_YOURS[0]);}
        if(!offer(p))Talk.end(p);
    }
    /** A blow lands on it and does nothing, to it or to anybody. Vera, if she sees, says why. */
    public static void struck(ServerPlayer p,LiteraryActor a){
        if(LiteraryVignettes.current(p)!=LodgingPeople.PLACE)return;var own=LodgingHouse.own(p);
        if(!own.getBoolean(STRUCK)){own.putBoolean(STRUCK,true);LodgingHouse.save(p,own);}
        var vera=veraNear(p);
        if(vera!=null&&vera.hasLineOfSight(p)){Talk.begin(p,TALK,"Vera");Talk.line(p,"Vera",LodgingTexts.VERA_DONT);Talk.end(p);vera.say("Vera",LodgingTexts.VERA_DONT);vera.speaks(LodgingTexts.VERA_DONT);}
        else LodgingHouse.cue(p,LodgingTexts.DOUBLE_STRUCK);
    }
}
