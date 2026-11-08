package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import static io.github.knaitoe.theoldesthouse.labyrinth.ClockworkBlock.Kind.*;

/**
 * 0.4.362, the Clockwork Attic (docs/CLOCKWORK_ATTIC_0_4_362.md), item 4 of the owner's "New Puzzle and Hazard Room Designs": stopped clock
 * mechanisms and pendulum weights; starting one clock starts a slow, visible chain that opens or closes a route. Every mechanism is
 * visible and started or stopped by hand, at a fixed and readable speed; a train left running can raise a ladder or drop a weight another
 * train needs.
 * <p>
 * A train is a master clock and a row of gears along a wall, ending at a heavy lever. Set swinging, the pendulum drives the train a tooth
 * every half second: the motion travels down the row a gear at a time, every gear it has reached turning with it, and when it reaches the
 * end the lever seats with a heavy click and does that train's work. A pin pushed through a gear's teeth stops the train there, at once
 * if the motion has already reached it; drawn out, it lets the train go on. Stilled, the clock's key winds the whole train back to its
 * start, undoing what its lever did. A train left running keeps running while nobody is there: the room catches up when someone comes back.
 * <ul>
 * <li><b>The clockwork attic</b> (early): one long train down the attic, from the master clock by the door to a lever that raises the
 * ladder to the hatch into the loft, where the far door is. One gear halfway down starts pinned.</li>
 * <li><b>The clockwork attics</b> (deep): three bays off a corridor, each with its own train, seen into through windows. A's lever sets a
 * catch; B's draws up the bars across the doorway into the last bay and lets go of a weight hanging over C's train, which the catch holds
 * only if A has already run; C's raises the ladder to the loft. Run B before A and the weight comes down into C's gears, and C stops there.</li>
 * </ul>
 * Like every puzzle room nothing hurts or is used up and everything winds back by hand; solved stays solved, for everyone; no Witness.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class Clockwork {
    private Clockwork(){}
    public static final LabyrinthPlace EARLY=LabyrinthPlace.CLOCKWORK_ATTIC,DEEP=LabyrinthPlace.CLOCKWORK_ATTICS;
    public static final String STATE="clockwork_0362";
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    /** Ticks per tooth: the fixed speed every train runs at. */
    public static final int TOOTH=10;
    public static boolean is(@Nullable LabyrinthPlace p){return p==EARLY||p==DEEP;}

    /** A master clock (case and face), its row of gears in the order the motion reaches them, its end lever, and the teeth each gear takes. */
    public record Train(String id,BlockPos clockCase,BlockPos face,List<BlockPos> stations,BlockPos lever,int span){
        public int length(){return stations.size()*span;}
        public int index(BlockPos rel){return stations.indexOf(rel);}
    }
    static List<BlockPos> row(int x,int y,int z0,int z1){var out=new ArrayList<BlockPos>();for(int z=z0;z>=z1;z--)out.add(new BlockPos(x,y,z));return out;}

    // The early attic: the train along the east wall, the ladder up the partition under the loft, the far door in the loft's gable.
    public static final Train MAIN=new Train("main",new BlockPos(4,0,-1),new BlockPos(4,1,-1),row(4,2,-2,-17),new BlockPos(4,1,-17),3);
    public static final int EARLY_PIN=8;
    public static final BlockPos EARLY_LADDER=new BlockPos(0,0,-20);
    // The deep attics: three bays off the corridor, a train along each bay's east wall.
    public static final Train A=new Train("a",new BlockPos(7,0,-4),new BlockPos(7,1,-4),row(7,2,-5,-9),new BlockPos(7,1,-9),4),
        B=new Train("b",new BlockPos(7,0,-12),new BlockPos(7,1,-12),row(7,2,-13,-17),new BlockPos(7,1,-17),4),
        C=new Train("c",new BlockPos(7,0,-20),new BlockPos(7,1,-20),row(7,2,-21,-25),new BlockPos(7,1,-25),4);
    /** Where B's weight hangs over C's train, where it comes down, the gear it jams, the catch that can hold it, the bars into C's bay, C's ladder. */
    public static final BlockPos WEIGHT_UP=new BlockPos(7,4,-23),WEIGHT_DOWN=new BlockPos(7,3,-23),CATCH_AT=new BlockPos(6,4,-23),
        GATE_AT=new BlockPos(-4,0,-23),DEEP_LADDER=new BlockPos(2,0,-26);
    public static final int JAM=2;
    public static List<Train> trains(LabyrinthPlace place){return place==EARLY?List.of(MAIN):List.of(A,B,C);}
    static @Nullable Train train(LabyrinthPlace place,String id){for(var t:trains(place))if(t.id().equals(id))return t;return null;}

    // ------------------------------------------------------------------ saved state
    public static CompoundTag room(MinecraftServer s,LabyrinthPlace place){return LabyrinthData.get(s).stateEntry(STATE,place.id());}
    static void save(MinecraftServer s,LabyrinthPlace place,CompoundTag room){LabyrinthData.get(s).setStateEntry(STATE,place.id(),room);}
    static CompoundTag of(CompoundTag room,Train t){return room.getCompound(t.id());}
    public static int progress(MinecraftServer s,LabyrinthPlace place,Train t){return of(room(s,place),t).getInt("Progress");}
    public static boolean running(MinecraftServer s,LabyrinthPlace place,Train t){return of(room(s,place),t).getBoolean("Running");}
    public static boolean pinned(MinecraftServer s,LabyrinthPlace place,Train t,int station){return (of(room(s,place),t).getInt("Pins")&(1<<station))!=0;}
    public static boolean weightDown(MinecraftServer s){return room(s,DEEP).getBoolean("WeightDown");}
    public static boolean catchSet(MinecraftServer s){return room(s,DEEP).getBoolean("Catch");}
    static boolean solved(MinecraftServer s,LabyrinthPlace place){return PuzzleRooms.solved(s,place);}

    /** Whether the motion can go on: not if a pinned gear (or the weight, for C) stands where it has reached or is about to reach. */
    static boolean blocked(Train t,CompoundTag tr,boolean jammed){
        int p=tr.getInt("Progress"),pins=tr.getInt("Pins");
        for(int i=0;i<t.stations().size();i++)if((pins&(1<<i))!=0&&i*t.span()<=p)return true;
        return jammed&&JAM*t.span()<=p;
    }

    // ------------------------------------------------------------------ building
    static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){l.setBlock(b.offset(at),s,FLAGS);}
    static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){l.setBlock(b.offset(x,y,z),s,FLAGS);}
    static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){
        for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int y=Math.min(y0,y1);y<=Math.max(y0,y1);y++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)set(l,b,x,y,z,s);
    }
    static BlockState fit(ClockworkBlock.Kind k,Direction f,int stage){return ClockworkBlock.of(k,f,stage);}
    static BlockState air(){return Blocks.AIR.defaultBlockState();}
    static BlockState glass(){return Blocks.GLASS.defaultBlockState();}
    static void hang(ServerLevel l,BlockPos b,int x,int y,int z){set(l,b,x,y,z,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));}
    /** A loft over the far end, its floor at y 4 with a hatch for the ladder, a partition under its front edge. */
    static void loft(ServerLevel l,BlockPos b,int x0,int x1,int zFront,int zBack,BlockPos ladder){
        var boards=Blocks.SPRUCE_PLANKS.defaultBlockState();
        box(l,b,x0,4,zFront,x1,4,zBack,boards);box(l,b,x0,0,zFront-1,x1,3,zBack,boards);
        set(l,b,ladder.getX(),4,ladder.getZ(),air());
        for(int x=x0;x<=x1;x+=2)set(l,b,x,5,zFront,Blocks.SPRUCE_FENCE.defaultBlockState());
        set(l,b,ladder.getX(),5,ladder.getZ(),air());
    }
    static void beams(ServerLevel l,BlockPos b,int x0,int x1,int z0,int z1){
        var beam=Blocks.DARK_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X);
        for(int z=z0;z>=z1;z-=4)for(int x=x0;x<=x1;x++)set(l,b,x,6,z,beam);
    }
    static void placeTrain(ServerLevel l,BlockPos b,Train t,int pins){
        set(l,b,t.clockCase(),fit(CASE,Direction.WEST,0));set(l,b,t.face(),fit(FACE,Direction.WEST,0));
        for(int i=0;i<t.stations().size();i++)set(l,b,t.stations().get(i),fit((pins&(1<<i))!=0?PINNED:GEAR,Direction.WEST,0));
        set(l,b,t.lever(),fit(LEVER,Direction.WEST,0));
    }
    static CompoundTag fresh(int progress,int pins){var t=new CompoundTag();t.putInt("Progress",progress);t.putBoolean("Running",false);t.putInt("Pins",pins);t.putLong("Last",0);return t;}

    public static void buildEarly(ServerLevel l,BlockPos b){
        boolean solved=solved(l.getServer(),EARLY);
        var walls=Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();var floor=Blocks.OAK_PLANKS.defaultBlockState();var roof=Blocks.DARK_OAK_PLANKS.defaultBlockState();
        box(l,b,-5,-1,-26,5,8,0,walls);box(l,b,-5,7,-26,5,8,0,roof);
        box(l,b,-4,-1,-24,4,-1,-1,floor);box(l,b,-4,0,-24,4,6,-1,air());
        beams(l,b,-4,4,-4,-16);
        loft(l,b,-4,4,-20,-24,EARLY_LADDER);
        var room=new CompoundTag();room.put("main",fresh(solved?MAIN.length():0,solved?0:1<<EARLY_PIN));save(l.getServer(),EARLY,room);
        placeTrain(l,b,MAIN,solved?0:1<<EARLY_PIN);
        if(solved)ladder(l,b,EARLY_LADDER,true);
        hang(l,b,-2,5,-8);hang(l,b,1,5,-16);hang(l,b,0,6,-22);
        set(l,b,-4,0,-6,Blocks.BARREL.defaultBlockState());set(l,b,-4,0,-12,Blocks.BARREL.defaultBlockState());set(l,b,-4,1,-12,Blocks.BARREL.defaultBlockState());
        LabyrinthBuilder.entrance(l,b,walls,floor,walls);
        LabyrinthBuilder.doors(l,b,EARLY);
        render(l,b,EARLY,false);
    }

    public static void buildDeep(ServerLevel l,BlockPos b){
        boolean solved=solved(l.getServer(),DEEP);
        var walls=Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();var floor=Blocks.OAK_PLANKS.defaultBlockState();var roof=Blocks.DARK_OAK_PLANKS.defaultBlockState();
        var inner=Blocks.SPRUCE_PLANKS.defaultBlockState();
        box(l,b,-9,-1,-32,9,8,0,walls);box(l,b,-9,7,-32,9,8,0,roof);
        box(l,b,-8,-1,-30,8,-1,-1,floor);box(l,b,-8,0,-30,8,6,-1,air());box(l,b,8,0,-30,8,6,-1,walls);
        // The corridor's wall, the wall behind the entry hall and the walls between the bays, each with windows.
        box(l,b,-4,0,-27,-4,6,-3,inner);box(l,b,-3,0,-3,8,6,-3,inner);box(l,b,-3,0,-11,8,6,-11,inner);box(l,b,-3,0,-19,8,6,-19,inner);box(l,b,-8,0,-27,-5,6,-27,inner);
        box(l,b,-8,0,-30,-4,6,-28,walls);
        for(int z:new int[]{-5,-9,-13,-17,-21,-25})box(l,b,-4,1,z,-4,2,z,glass());
        for(int z:new int[]{-3,-11,-19})box(l,b,2,1,z,5,2,z,glass());
        for(int z:new int[]{-7,-15,-23})box(l,b,-4,0,z,-4,1,z,air());
        beams(l,b,-8,8,-4,-28);
        loft(l,b,-3,8,-26,-30,DEEP_LADDER);
        var room=new CompoundTag();
        for(var t:trains(DEEP))room.put(t.id(),fresh(solved?t.length():0,0));
        room.putBoolean("Catch",solved);room.putBoolean("WeightDown",false);room.putBoolean("Gate",solved);
        save(l.getServer(),DEEP,room);
        for(var t:trains(DEEP))placeTrain(l,b,t,0);
        // B's weight over C's train, on its chain from the beam; the catch beside it; the bars across the doorway into C's bay.
        for(int y=5;y<=6;y++)set(l,b,WEIGHT_UP.getX(),y,WEIGHT_UP.getZ(),Blocks.CHAIN.defaultBlockState());
        set(l,b,WEIGHT_UP,fit(WEIGHT,Direction.WEST,0));set(l,b,CATCH_AT,fit(CATCH,Direction.WEST,solved?1:0));
        gate(l,b,solved);
        if(solved)ladder(l,b,DEEP_LADDER,true);
        hang(l,b,-6,5,-8);hang(l,b,-6,5,-20);hang(l,b,2,5,-8);hang(l,b,2,5,-16);hang(l,b,2,5,-24);hang(l,b,2,6,-28);
        LabyrinthBuilder.entrance(l,b,walls,floor,walls);
        LabyrinthBuilder.doors(l,b,DEEP);
        render(l,b,DEEP,false);
    }
    static void gate(ServerLevel l,BlockPos b,boolean up){
        set(l,b,GATE_AT,up?air():fit(GATE,Direction.EAST,0));set(l,b,GATE_AT.above(),fit(GATE,Direction.EAST,up?1:0));
    }

    // ------------------------------------------------------------------ the trains at work
    /** Every half second while someone is in the room, and on their return after time away: each running train catches up. */
    public static void update(ServerLevel l,BlockPos b,LabyrinthPlace place){
        var s=l.getServer();long now=l.getGameTime();var room=room(s,place);boolean any=false;
        for(var t:trains(place)){
            var tr=of(room,t);long last=tr.getLong("Last");
            if(!tr.getBoolean("Running")){tr.putLong("Last",now);room.put(t.id(),tr);continue;}
            int steps=last<=0?1:(int)Math.min(t.length()+1,(now-last)/TOOTH);
            if(steps<=0)continue;
            tr.putLong("Last",last<=0?now:last+(long)steps*TOOTH);room.put(t.id(),tr);
            for(int i=0;i<steps;i++){
                tr=of(room,t);
                if(tr.getInt("Progress")>=t.length()||blocked(t,tr,t==C&&room.getBoolean("WeightDown"))){
                    if(!tr.getBoolean("Strained")){tr.putBoolean("Strained",true);room.put(t.id(),tr);l.playSound(null,b.offset(t.face()),PuzzleRegistry.CLOCKWORK_TEETH.get(),SoundSource.BLOCKS,.5F,.6F);}
                    break;
                }
                int p=tr.getInt("Progress")+1;tr.putInt("Progress",p);tr.putBoolean("Strained",false);room.put(t.id(),tr);any=true;
                if(i==steps-1)l.playSound(null,b.offset(t.clockCase()),PuzzleRegistry.CLOCKWORK_TICK.get(),SoundSource.BLOCKS,.35F,1F);
                if(p%t.span()==1&&p/t.span()<t.stations().size())l.playSound(null,b.offset(t.stations().get(p/t.span())),PuzzleRegistry.CLOCKWORK_TEETH.get(),SoundSource.BLOCKS,.6F,1F);
                if(p>=t.length()){tr.putBoolean("Running",false);room.put(t.id(),tr);save(s,place,room);seat(l,b,place,t);room=room(s,place);break;}
            }
        }
        save(s,place,room);
        render(l,b,place,any);
    }
    /** Shows each train as it stands: the gears the motion has reached turning together, the pendulum, the hands, the lever. */
    static void render(ServerLevel l,BlockPos b,LabyrinthPlace place,boolean moving){
        var room=room(l.getServer(),place);
        for(var t:trains(place)){
            var tr=of(room,t);int p=tr.getInt("Progress"),pins=tr.getInt("Pins");boolean running=tr.getBoolean("Running")&&!tr.getBoolean("Strained");
            for(int i=0;i<t.stations().size();i++){
                var at=b.offset(t.stations().get(i));var st=l.getBlockState(at);if(!st.is(PuzzleRegistry.CLOCKWORK.get()))continue;
                boolean reached=p>i*t.span();int stage=reached?(p+i)%4:0;
                var kind=(pins&(1<<i))!=0?PINNED:GEAR;
                if(!reached)stage=0;else if(!moving&&st.getValue(ClockworkBlock.KIND)==kind)stage=st.getValue(ClockworkBlock.STAGE);
                var want=fit(kind,st.getValue(ClockworkBlock.FACING),stage);if(!want.equals(st))l.setBlock(at,want,FLAGS);
            }
            setStage(l,b,t.clockCase(),running&&moving?1+p%2:0);
            setStage(l,b,t.face(),(p/t.span())%4);
            setStage(l,b,t.lever(),p>=t.length()?1:0);
        }
    }
    static void setStage(ServerLevel l,BlockPos b,BlockPos rel,int stage){var at=b.offset(rel);var st=l.getBlockState(at);if(st.is(PuzzleRegistry.CLOCKWORK.get())&&st.getValue(ClockworkBlock.STAGE)!=stage)l.setBlock(at,st.setValue(ClockworkBlock.STAGE,stage),FLAGS);}

    /** A train's lever seats: what it does. */
    static void seat(ServerLevel l,BlockPos b,LabyrinthPlace place,Train t){
        var s=l.getServer();l.playSound(null,b.offset(t.lever()),PuzzleRegistry.CLOCKWORK_LEVER.get(),SoundSource.BLOCKS,1F,1F);
        if(place==EARLY||t==C){
            ladder(l,b,place==EARLY?EARLY_LADDER:DEEP_LADDER,true);
            PuzzleRooms.solve(l,place,"Somewhere a lever seats with a heavy click, and a ladder comes down from the hatch into the loft.");
            return;
        }
        var room=room(s,DEEP);
        if(t==A){
            room.putBoolean("Catch",true);save(s,DEEP,room);set(l,b,CATCH_AT,fit(CATCH,Direction.WEST,1));
            say(l,"A lever seats in the first bay. Through the windows, in the last bay, a catch swings out under the weight's chain.");
        }else if(t==B){
            if(!room.getBoolean("Gate")){room.putBoolean("Gate",true);gate(l,b,true);l.playSound(null,b.offset(GATE_AT),PuzzleRegistry.CLOCKWORK_GATE.get(),SoundSource.BLOCKS,.9F,1F);}
            if(room.getBoolean("Catch"))say(l,"A lever seats in the second bay. The bars draw up from the last doorway, and the weight drops an inch onto its catch and holds.");
            else{room.putBoolean("WeightDown",true);dropWeight(l,b);say(l,"A lever seats in the second bay. The bars draw up from the last doorway, and in the last bay the weight comes down into the gears.");}
            save(s,DEEP,room);
        }
    }
    /** A train wound back from its end: its lever comes up, and what it did is undone where it can be. Returns false if it cannot be wound back now. */
    static boolean unseat(ServerLevel l,BlockPos b,LabyrinthPlace place,Train t){
        var s=l.getServer();
        if(place==EARLY||t==C){
            if(solved(s,place))return true;
            var col=place==EARLY?EARLY_LADDER:DEEP_LADDER;
            if(!l.getEntitiesOfClass(LivingEntity.class,new AABB(b.offset(col)).expandTowards(0,5,0),e->e.isAlive()&&!e.isSpectator()).isEmpty())return false;
            ladder(l,b,col,false);return true;
        }
        var room=room(s,DEEP);
        if(t==A){
            room.putBoolean("Catch",false);set(l,b,CATCH_AT,fit(CATCH,Direction.WEST,0));
            if(of(room,B).getInt("Progress")>=B.length()&&!room.getBoolean("WeightDown")){room.putBoolean("WeightDown",true);dropWeight(l,b);}
        }else if(t==B&&room.getBoolean("WeightDown")){room.putBoolean("WeightDown",false);raiseWeight(l,b);}
        save(s,DEEP,room);return true;
    }
    static void dropWeight(ServerLevel l,BlockPos b){
        set(l,b,WEIGHT_UP,Blocks.CHAIN.defaultBlockState());set(l,b,WEIGHT_DOWN,fit(WEIGHT,Direction.WEST,0));
        l.playSound(null,b.offset(WEIGHT_DOWN),PuzzleRegistry.CLOCKWORK_WEIGHT.get(),SoundSource.BLOCKS,1F,1F);
    }
    static void raiseWeight(ServerLevel l,BlockPos b){set(l,b,WEIGHT_DOWN,air());set(l,b,WEIGHT_UP,fit(WEIGHT,Direction.WEST,0));}
    static void ladder(ServerLevel l,BlockPos b,BlockPos col,boolean up){
        var rung=Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH);
        for(int y=0;y<=4;y++){var at=b.offset(col.getX(),y,col.getZ());var st=l.getBlockState(at);if(up&&st.isAir())l.setBlock(at,rung,FLAGS);else if(!up&&st.is(Blocks.LADDER))l.setBlock(at,air(),FLAGS);}
    }
    static void say(ServerLevel l,String line){for(var p:l.players())if(PuzzleRooms.at(p)==DEEP)PuzzleRooms.say(p,line);}

    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();if(server.getTickCount()%TOOTH!=5)return;var origin=HouseSavedData.get(server).houseOrigin();var l=server.getLevel(HouseDimensions.INTERIOR);
        if(origin==null||l==null)return;
        for(var place:new LabyrinthPlace[]{EARLY,DEEP}){
            if(LabyrinthData.get(server).door(place.entryDoorId())==null)continue;
            for(var p:l.players())if(PuzzleRooms.at(p)==place){var b=LabyrinthPlaces.base(origin,place);if(b!=null&&l.isLoaded(b))update(l,b,place);break;}
        }
    }

    // ------------------------------------------------------------------ hands on
    record Found(Train train,int station){}
    static @Nullable Found find(LabyrinthPlace place,BlockPos rel){
        for(var t:trains(place)){int i=t.index(rel);if(i>=0)return new Found(t,i);if(rel.equals(t.clockCase())||rel.equals(t.face())||rel.equals(t.lever()))return new Found(t,-1);}
        return null;
    }
    public static boolean use(ServerPlayer p,BlockPos pos,BlockState s){
        if(p.isSpectator())return false;
        var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null)return false;
        var place=LabyrinthPlaces.placeAt(origin,pos);if(!is(place))return false;
        var l=p.serverLevel();var b=LabyrinthPlaces.base(origin,place);var rel=pos.subtract(b);int stage=s.getValue(ClockworkBlock.STAGE);
        // Catch up first, so a hand on a train meets it as it stands now.
        update(l,b,place);
        var found=find(place,rel);var server=p.server;
        switch(s.getValue(ClockworkBlock.KIND)){
            case CASE->{
                if(found==null)return true;var t=found.train();var room=room(server,place);var tr=of(room,t);
                if(tr.getInt("Progress")>=t.length()){PuzzleRooms.say(p,"The pendulum hangs still. The train has run to its end; the key in the face winds it back.");return true;}
                boolean run=!tr.getBoolean("Running");tr.putBoolean("Running",run);tr.putLong("Last",l.getGameTime());tr.putBoolean("Strained",false);room.put(t.id(),tr);save(server,place,room);
                l.playSound(null,pos,PuzzleRegistry.CLOCKWORK_TICK.get(),SoundSource.BLOCKS,.7F,run?1.1F:.8F);
                PuzzleRooms.say(p,run?"You set the pendulum swinging. Along the wall, a gear begins to turn.":"You still the pendulum. The train stops where it is.");
                render(l,b,place,false);
            }
            case FACE->{
                if(found==null)return true;var t=found.train();var room=room(server,place);var tr=of(room,t);
                if(tr.getBoolean("Running")){PuzzleRooms.say(p,"The hands are going. Still the pendulum before you wind it back.");return true;}
                if(tr.getInt("Progress")==0){PuzzleRooms.say(p,"The clock stands wound to its start. The key turns no further.");return true;}
                if(tr.getInt("Progress")>=t.length()&&!unseat(l,b,place,t)){PuzzleRooms.say(p,"The key won't turn while someone is on the ladder.");return true;}
                room=room(server,place);tr=of(room,t);tr.putInt("Progress",0);tr.putBoolean("Strained",false);room.put(t.id(),tr);save(server,place,room);
                l.playSound(null,pos,PuzzleRegistry.CLOCKWORK_RATCHET.get(),SoundSource.BLOCKS,.9F,1F);
                PuzzleRooms.say(p,"You turn the key. The whole train winds back, every gear at once, to its start.");
                render(l,b,place,false);
            }
            case GEAR,PINNED->{
                if(found==null||found.station()<0)return true;var t=found.train();var room=room(server,place);var tr=of(room,t);
                int pins=tr.getInt("Pins")^(1<<found.station());tr.putInt("Pins",pins);tr.putBoolean("Strained",false);room.put(t.id(),tr);save(server,place,room);
                boolean in=(pins&(1<<found.station()))!=0;
                l.playSound(null,pos,PuzzleRegistry.CLOCKWORK_TEETH.get(),SoundSource.BLOCKS,.6F,in?.7F:1.3F);
                PuzzleRooms.say(p,in?"You push an iron pin through the gear's teeth.":"You draw the pin out of the gear's teeth.");
                render(l,b,place,false);
            }
            case LEVER->PuzzleRooms.say(p,stage==1?"The lever has seated, heavy and down.":"A heavy iron lever at the end of the gears, waiting for the motion to reach it.");
            case WEIGHT->PuzzleRooms.say(p,rel.equals(WEIGHT_DOWN)?"The weight has come down into the gears. It is far too heavy to lift.":"An iron weight hangs on its chain over the gears.");
            case CATCH->PuzzleRooms.say(p,stage==1?"An iron catch, swung out under the weight's chain.":"An iron catch on a pivot, folded back against the wall.");
            case GATE->PuzzleRooms.say(p,stage==1?"The bars are drawn up into the lintel.":"Bars across the doorway. A rod runs up from them into the works overhead.");
        }
        return true;
    }
}
