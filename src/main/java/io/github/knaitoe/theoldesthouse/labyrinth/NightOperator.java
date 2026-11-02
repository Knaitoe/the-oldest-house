package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.252, the Night Operator (docs/NIGHT_OPERATOR_0_4_252.md), the owner's third proposal of 2 November: "An operator at an old telephone
 * exchange asks you to help someone find their way out of another wing. You patch into different rooms and identify them through
 * background sounds: running water, a refrigerator, someone walking on a bare floor. Opening the correct connection lets the caller
 * continue. Their footsteps become progressively nearer. When they finally say they're outside, footsteps cross your exchange toward its
 * door. Nobody is visible. The operator removes their headset: 'That's us finished.'"
 * <p>
 * A small switchroom at night: a cord switchboard along one wall, nine jacks under a field of lamps, the incoming line's lamp lit at the
 * operator's end, where the operator sits in a headset. A hand on a jack lets that reader alone hear the room it reaches, thin as a
 * telephone makes it; which jack reaches which room is fixed for each world. The caller says what they can hear behind the next door;
 * a crouching hand on the right jack puts them through, and their footsteps come nearer beyond the entry door. Through the last room,
 * the exchange's own line, they say they are outside; footsteps cross the switchroom to the far door, which opens and shuts with nobody
 * at it. The operator hangs up their headset. A puzzle room by its pacing (family LINE, early only); nothing counts down or hurts; no
 * Witness.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class NightOperator {
    private NightOperator(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.NIGHT_OPERATOR;
    public static final String STATE="night_operator_0252";
    /** The operator's stranger phases: in their headset, and with it hung up. */
    public static final int OPERATOR=55,OPERATOR_BARE=56;
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    /** The rooms the board reaches, what the caller hears in each when put through to it, and the exchange's own line. */
    public enum Line {
        WATER("Water running. A tap left on."),
        FRIDGE("Something humming. A fridge."),
        BOARDS("Someone walking about, on bare boards."),
        CLOCK("Just a clock."),
        RAIN("Rain on a window."),
        RADIO("A radio between stations."),
        KETTLE("A kettle, nearly on the boil."),
        QUIET("Nothing. Nothing at all."),
        EXCHANGE("A switchboard. Someone breathing near it.");
        public final String heard;
        Line(String heard){this.heard=heard;}
        public SoundEvent sound(){return PuzzleRegistry.LINES.get(ordinal()).get();}
    }
    /** The way out, room by room, and what the caller says of the door in front of them before each. */
    public static final Line[] WAY={Line.WATER,Line.FRIDGE,Line.BOARDS,Line.CLOCK,Line.EXCHANGE};
    static final String[] NEXT={
        "Hello? Is somebody there? I can't find the way out. There's a door here, and water running behind it. A tap, I think.",
        "I'm through. There's another door. Something humming behind it, like a fridge.",
        "Through. Someone's walking about behind this one. Bare boards. Put me through anyway.",
        "Only a clock behind this one. Just ticking.",
        "I can see light under a door. There's a switchboard in there, I can hear it. Put me through."};
    static final String[] EXPLAIN={
        "You'll be the relief. Good. I've someone on the line I can't place.",
        "They're lost in the other wing. They'll tell you what they can hear behind the next door.",
        "Hand on a jack and you'll hear that room. Crouch and press it to put them through."};
    static final String[] NOTES={
        "SHIFT NOTES. Nights: one on the board at all times.",
        "Callers from the other wing: put them through a room at a time. They know the way better than we do.",
        "Don't put anyone through to a room you haven't listened to.",
        "A line with nothing on it after midnight is not a fault. Leave it.",
        "If they say they're outside, let them by."};
    /** How far beyond the entry door the caller's footsteps stop after each room they come through. */
    static final int[] FAR={22,15,9,4};
    /** The phases: the caller on the line, footsteps crossing the room, the far door, all done. */
    public static final int CALLING=0,CROSSING=1,DOOR=2,FINISHED=3;

    // ------------------------------------------------------------------ the plan
    /** The board along the west wall: nine jacks at z -3 to -11, the incoming line at the operator's end, the headset hook beyond it. */
    public static final int BOARD_X=-5,FIRST_Z=-3,JACKS=9;
    public static final BlockPos CALLER=new BlockPos(-5,1,-2),STOOL=new BlockPos(-4,0,-2),HOOK=new BlockPos(-5,2,-1);
    public static BlockPos jack(int i){return new BlockPos(BOARD_X,1,FIRST_Z-i);}

    static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){l.setBlock(b.offset(x,y,z),s,FLAGS);}
    static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){l.setBlock(b.offset(at),s,FLAGS);}
    static void fill(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int y=Math.min(y0,y1);y<=Math.max(y0,y1);y++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)set(l,b,x,y,z,s);}
    static CompoundTag state(ServerLevel l){return LabyrinthData.get(l.getServer()).state(STATE);}
    static void save(ServerLevel l,CompoundTag s){LabyrinthData.get(l.getServer()).setState(STATE,s);}
    public static int phase(ServerLevel l){return state(l).getInt("Phase");}
    public static int step(ServerLevel l){return state(l).getInt("Step");}
    /** Whether the caller is at a door, waiting to be put through. */
    public static boolean ready(ServerLevel l){return state(l).getBoolean("Ready");}

    /** Which room each jack reaches: fixed for each world. */
    public static Line lineAt(ServerLevel l,int i){return order(l).get(i);}
    public static int jackOf(ServerLevel l,Line line){return order(l).indexOf(line);}
    static List<Line> order(ServerLevel l){var lines=new ArrayList<>(List.of(Line.values()));Collections.shuffle(lines,new Random(l.getSeed()*31+PLACE.slot()));return lines;}

    // ------------------------------------------------------------------ building
    public static void build(ServerLevel l,BlockPos b){
        boolean solved=PuzzleRooms.solved(l.getServer(),PLACE);
        var plaster=PuzzleRegistry.draftMaterial("plaster").defaultBlockState();var dado=Blocks.DARK_OAK_PLANKS.defaultBlockState();
        // The shell: dark boards, a dark oak dado round the walls, plaster above and overhead.
        for(int x=-6;x<=6;x++)for(int z=-12;z<=0;z++)for(int y=-1;y<=4;y++){
            boolean wall=x==-6||x==6||z==-12||z==0;
            set(l,b,x,y,z,y==-1?dado:y==4?plaster:wall?(y==0?dado:plaster):Blocks.AIR.defaultBlockState());
        }
        for(int x=-5;x<=5;x++)for(int z=-11;z<=-1;z++)set(l,b,x,-1,z,Blocks.DARK_OAK_PLANKS.defaultBlockState());
        fill(l,b,-2,0,-9,2,0,-4,Blocks.BROWN_CARPET.defaultBlockState());
        // The board: a shelf of plugs and keys, the jacks over it, the lamps over them; the incoming line at the operator's end.
        for(int i=0;i<JACKS;i++){var at=jack(i);set(l,b,at.below(),ExchangeBlock.of(ExchangeBlock.Kind.DESK,Direction.EAST));set(l,b,at,ExchangeBlock.of(ExchangeBlock.Kind.JACK,Direction.EAST));set(l,b,at.above(),ExchangeBlock.of(ExchangeBlock.Kind.PANEL,Direction.EAST));}
        set(l,b,CALLER.below(),ExchangeBlock.of(ExchangeBlock.Kind.DESK,Direction.EAST));set(l,b,CALLER,ExchangeBlock.of(ExchangeBlock.Kind.CALLER,Direction.EAST,solved?0:1));set(l,b,CALLER.above(),ExchangeBlock.of(ExchangeBlock.Kind.PANEL,Direction.EAST));
        set(l,b,HOOK,ExchangeBlock.of(ExchangeBlock.Kind.HOOK,Direction.EAST,solved?1:0));
        set(l,b,STOOL,HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.WEST));
        // The rest of the room: the shift notes, a radiator, a chair by the wall, and the one lamp.
        set(l,b,5,1,-4,ExchangeBlock.of(ExchangeBlock.Kind.NOTES,Direction.WEST));set(l,b,5,1,-5,ExchangeBlock.of(ExchangeBlock.Kind.NOTES,Direction.WEST));
        set(l,b,5,0,-8,HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.RADIATOR,Direction.WEST));
        set(l,b,5,0,-2,HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.WEST));
        set(l,b,-1,3,-6,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        LabyrinthBuilder.entrance(l,b,plaster,dado,plaster);
        LabyrinthBuilder.doors(l,b,PLACE);
        farDoor(l,b,solved);
        var old=operator(l);if(old!=null)old.discard();RoomActors.clear(l,RoomActors.box(b,PLACE),NightOperator::isOperator);RoomActors.settlePanes(l,b,PLACE);
        var s=new CompoundTag();if(solved)s.putInt("Phase",FINISHED);save(l,s);
    }
    /** The far door, iron with no handle on this side. */
    static void farDoor(ServerLevel l,BlockPos b,boolean open){
        var lower=b.offset(PuzzleRooms.farDoor(PLACE));
        var door=Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING,Direction.SOUTH).setValue(DoorBlock.HINGE,DoorHingeSide.LEFT).setValue(DoorBlock.OPEN,open);
        l.setBlock(lower,door.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER),FLAGS);l.setBlock(lower.above(),door.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER),FLAGS);
        l.playSound(null,lower,open?SoundEvents.IRON_DOOR_OPEN:SoundEvents.IRON_DOOR_CLOSE,SoundSource.BLOCKS,.8F,1F);
    }
    /** Plugs the cord into jack {@code at} (or none, -1), pulling it from wherever it was. */
    static void plug(ServerLevel l,BlockPos b,int at){
        for(int i=0;i<JACKS;i++){var pos=b.offset(jack(i));var st=l.getBlockState(pos);if(ExchangeBlock.is(st,ExchangeBlock.Kind.JACK)){int want=i==at?2:st.getValue(ExchangeBlock.STAGE)==1?1:0;if(st.getValue(ExchangeBlock.STAGE)!=want)l.setBlock(pos,st.setValue(ExchangeBlock.STAGE,want),FLAGS);}}
    }
    static void callerLamp(ServerLevel l,BlockPos b,boolean lit){var pos=b.offset(CALLER);var st=l.getBlockState(pos);if(ExchangeBlock.is(st,ExchangeBlock.Kind.CALLER))l.setBlock(pos,st.setValue(ExchangeBlock.STAGE,lit?1:0),FLAGS);}

    // ------------------------------------------------------------------ the operator and the caller
    static boolean isOperator(LiteraryActor a){return a.role()==LiteraryActor.STRANGER&&(a.phase()==OPERATOR||a.phase()==OPERATOR_BARE);}
    @Nullable static LiteraryActor operator(ServerLevel l){var s=state(l);if(!s.hasUUID("Operator"))return null;return l.getEntity(s.getUUID("Operator")) instanceof LiteraryActor a&&a.isAlive()?a:null;}
    static LiteraryActor spawn(ServerLevel l,BlockPos b,boolean bare){
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return null;
        a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,bare?OPERATOR_BARE:OPERATOR);a.setNoGravity(true);
        a.moveTo(b.getX()+STOOL.getX()+.5,b.getY()+STOOL.getY(),b.getZ()+STOOL.getZ()+.5,90,0);a.setYBodyRot(90);a.setYHeadRot(90);
        if(!l.addFreshEntity(a))return null;
        var s=state(l);s.putUUID("Operator",a.getUUID());save(l,s);return a;
    }
    static void say(ServerLevel l,LiteraryActor a,String line){a.say(line);var s=state(l);s.putLong("Quiet",l.getGameTime()+100);save(l,s);}
    static void face(LiteraryActor a,float yaw){a.setYRot(yaw);a.setYBodyRot(yaw);a.setYHeadRot(yaw);}
    static void look(LiteraryActor a,Vec3 at){var d=at.subtract(a.getEyePosition());a.setYHeadRot((float)Math.toDegrees(Math.atan2(-d.x,d.z)));}
    @Nullable static ServerPlayer nearest(ServerLevel l,Vec3 from){ServerPlayer best=null;double d=Double.MAX_VALUE;for(var p:l.players()){if(!p.isAlive()||p.isSpectator()||PuzzleRooms.at(p)!=PLACE)continue;double dd=p.position().distanceToSqr(from);if(dd<d){d=dd;best=p;}}return best;}
    /** The caller's voice, thin on the line: on the action bar of everyone in the room, as speech always is. */
    static void caller(ServerLevel l,String line){
        var text=Component.literal("“"+line+"”");
        for(var p:l.players())if(PuzzleRooms.at(p)==PLACE)p.displayClientMessage(text,true);
    }
    /** One footstep, somewhere the caller has got to. */
    static void footstep(ServerLevel l,BlockPos b,double x,double z,float volume){
        l.playSound(null,b.getX()+x,b.getY(),b.getZ()+z,SoundEvents.WOOD_STEP,SoundSource.AMBIENT,volume,.85F+l.random.nextFloat()*.1F);
    }

    /** The room's turn, every tick while anyone is in it. */
    static void tick(ServerLevel l,BlockPos b){
        // 0.4.263 (as the window cleaner's room): wait for the room's entities, then keep the one operator and let any other go.
        var box=RoomActors.box(b,PLACE);if(!RoomActors.ready(l,box))return;
        var s=state(l);long now=l.getGameTime();int phase=s.getInt("Phase");
        if(s.contains("Quiet")&&now>s.getLong("Quiet")){RoomActors.hush(l,box,NightOperator::isOperator);s.remove("Quiet");save(l,s);s=state(l);}
        var a=RoomActors.one(l,box,s,"Operator",NightOperator::isOperator);save(l,s);s=state(l);
        if(a==null){a=spawn(l,b,phase>=DOOR&&s.getInt("Door")>=2||phase==FINISHED);if(a==null)return;s=state(l);}
        // The operator sits at the board, turning to whoever is nearest only to speak to them.
        face(a,90);var r=nearest(l,a.position());if(r!=null&&s.contains("Quiet"))look(a,r.getEyePosition());
        switch(phase){
            case CALLING->{
                if(!s.getBoolean("Lit")){callerLamp(l,b,true);l.playSound(null,b.offset(CALLER),PuzzleRegistry.BUZZ.get(),SoundSource.BLOCKS,.8F,1F);s.putBoolean("Lit",true);s.putLong("Next",now+20);save(l,s);return;}
                int told=s.getInt("Told");
                if(told<EXPLAIN.length){if(now>=s.getLong("Next")){say(l,a,EXPLAIN[told]);s=state(l);s.putInt("Told",told+1);s.putLong("Next",now+(told+1<EXPLAIN.length?80:60));save(l,s);}return;}
                // The caller's footsteps, coming nearer beyond the entry door after each room they get through.
                if(s.getInt("Walk")>0){
                    if(now<s.getLong("Next"))return;
                    int left=s.getInt("Walk");int far=FAR[Math.max(0,Math.min(FAR.length-1,s.getInt("Step")-1))];
                    footstep(l,b,.5+(left%2==0?.3:-.3),1.5+far+left-1,2F);
                    s.putInt("Walk",left-1);s.putLong("Next",now+9);if(left==1)s.putLong("Next",now+25);save(l,s);return;
                }
                if(!s.getBoolean("Ready")&&now>=s.getLong("Next")){int step=s.getInt("Step");caller(l,NEXT[Math.min(step,NEXT.length-1)]);s.putBoolean("Ready",true);save(l,s);}
            }
            case CROSSING->{
                // Footsteps cross the switchroom from the entry door to the far door, past whoever is there. Nobody is.
                if(now<s.getLong("Next"))return;
                int i=s.getInt("Cross");
                if(i>10){s.putInt("Phase",DOOR);s.putInt("Door",0);s.putLong("Next",now+25);save(l,s);farDoor(l,b,true);return;}
                footstep(l,b,.5+(i%2==0?.25:-.25),-.5-i,.9F);
                s.putInt("Cross",i+1);s.putLong("Next",now+8);save(l,s);
            }
            case DOOR->{
                if(now<s.getLong("Next"))return;
                int door=s.getInt("Door");
                if(door==0){farDoor(l,b,false);s.putInt("Door",1);s.putLong("Next",now+40);save(l,s);}
                else if(door==1){
                    // The operator takes off their headset and hangs it beside the spare one.
                    a.appearance(LiteraryActor.STRANGER,OPERATOR_BARE);
                    var hook=b.offset(HOOK);var st=l.getBlockState(hook);if(ExchangeBlock.is(st,ExchangeBlock.Kind.HOOK))l.setBlock(hook,st.setValue(ExchangeBlock.STAGE,1),FLAGS);
                    callerLamp(l,b,false);plug(l,b,-1);
                    say(l,a,"That's us finished.");s=state(l);s.putInt("Door",2);s.putLong("Next",now+40);save(l,s);
                }else{
                    farDoor(l,b,true);
                    PuzzleRooms.solve(l,PLACE,"The far door's latch has been let go. It stands open on the dark.");
                    s=state(l);s.putInt("Phase",FINISHED);save(l,s);
                }
            }
            default->{}
        }
    }

    // ------------------------------------------------------------------ hands on
    private static final Map<UUID,Long> LISTENED=new ConcurrentHashMap<>();
    private static final Map<UUID,Integer> NOTE=new ConcurrentHashMap<>();
    public static boolean use(ServerPlayer p,BlockPos pos,BlockState st){
        if(p.isSpectator())return false;
        var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null)return false;
        var place=LabyrinthPlaces.placeAt(origin,pos);if(place!=PLACE)return false;
        var b=LabyrinthPlaces.base(origin,place);var l=p.serverLevel();var s=state(l);long now=l.getGameTime();int phase=s.getInt("Phase");
        switch(st.getValue(ExchangeBlock.KIND)){
            case JACK->{
                int i=FIRST_Z-(pos.getZ()-b.getZ());if(i<0||i>=JACKS)return true;var line=lineAt(l,i);
                if(!p.isShiftKeyDown()){
                    // Listening: that reader alone hears the room on the line, for a few seconds.
                    Long last=LISTENED.get(p.getUUID());if(last!=null&&now-last>=0&&now-last<50)return true;
                    LISTENED.put(p.getUUID(),now);
                    p.playNotifySound(line.sound(),SoundSource.VOICE,1F,1F);
                    if(st.getValue(ExchangeBlock.STAGE)==0){l.setBlock(pos,st.setValue(ExchangeBlock.STAGE,1),FLAGS);l.scheduleTick(pos,st.getBlock(),70);}
                    PuzzleRooms.say(p,"Line "+(i+1)+".");
                    return true;
                }
                if(phase!=CALLING){PuzzleRooms.say(p,"The board is quiet. Nobody is on the line.");return true;}
                if(!s.getBoolean("Ready")){PuzzleRooms.say(p,s.getInt("Told")<EXPLAIN.length?"The operator hasn't finished explaining.":"They aren't at the next door yet.");return true;}
                plug(l,b,i);l.playSound(null,pos,SoundEvents.TRIPWIRE_CLICK_ON,SoundSource.BLOCKS,.8F,1.3F);
                int step=s.getInt("Step");
                if(line==WAY[step]){
                    step++;s.putInt("Step",step);s.putBoolean("Ready",false);
                    if(step>=WAY.length){caller(l,"Thank you. I'm outside now. I can hear you.");s.putInt("Phase",CROSSING);s.putInt("Cross",0);s.putLong("Next",now+60);}
                    else{caller(l,"Right. Going through.");s.putInt("Walk",4);s.putLong("Next",now+30);}
                    save(l,s);
                }else{
                    boolean been=false;for(int k=0;k<step;k++)if(WAY[k]==line)been=true;
                    caller(l,been?"I've been in there already.":line.heard+" That's not the one.");
                }
            }
            case CALLER->PuzzleRooms.say(p,phase==CALLING?"The incoming line. Its lamp is lit: somebody is on it.":"The incoming line. Its lamp is out.");
            case NOTES->{int k=NOTE.merge(p.getUUID(),1,Integer::sum)-1;PuzzleRooms.say(p,NOTES[Math.floorMod(k,NOTES.length)]);}
            case HOOK->PuzzleRooms.say(p,st.getValue(ExchangeBlock.STAGE)==0?"A spare headset on its hook.":"Two headsets on the hook now.");
            case PANEL->PuzzleRooms.say(p,"The board's lamps, one over each line.");
            case DESK->PuzzleRooms.say(p,"Plugs and keys, worn bright where hands go.");
        }
        return true;
    }

    // ------------------------------------------------------------------ the clock
    @SubscribeEvent public static void serverTick(ServerTickEvent.Post e){
        var server=e.getServer();var origin=HouseSavedData.get(server).houseOrigin();var l=server.getLevel(HouseDimensions.INTERIOR);
        if(origin==null||l==null)return;
        if(LabyrinthData.get(server).door(PLACE.entryDoorId())==null)return;
        for(var p:l.players())if(PuzzleRooms.at(p)==PLACE){var b=LabyrinthPlaces.base(origin,PLACE);if(b!=null&&l.isLoaded(b))tick(l,b);break;}
    }
    /** For tests: the operator, if they are here. */
    @Nullable public static LiteraryActor operatorFor(ServerLevel l){return operator(l);}
    /** For tests' messages: the room's saved state. */
    public static String debug(ServerLevel l){return state(l).toString();}
}
