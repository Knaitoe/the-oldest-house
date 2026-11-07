package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseInternalTeleport;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import static io.github.knaitoe.theoldesthouse.labyrinth.SphinxBlock.Kind.*;

/**
 * 0.4.339, the Sphinx's fourth crossing (docs/SPHINX_0_4_339.md; the owner's draft, "Outside · The road under the corn"): "Which path was
 * here before the field?"
 * <p>
 * Outside, at dusk. Through the House's back door, down into a roadside ditch, and up into a clearing at the edge of the corn. Three gaps
 * lead out of it, each as worn as the others, each with a field gate between two posts. Her question comes first from the ditch; on her
 * next breath the tassels part well above the reader's head. At each gap the loose soil can be brushed away: two show footprints in the
 * dirt, going in; the third shows cracked asphalt and the edge of a painted line, running on under the standing corn on both sides.
 * <p>
 * Opening the gate on the old road is the answer: the old road runs on through the corn to a strip of asphalt and round to the way onward,
 * which it opens for everyone. A gate on a newer path catches on its stop, she says so, its stop pin shows, and her eye comes up over the
 * corn behind it. Pulling the pin brings her out of the stalks in stages: tassels shaking toward the gap, the sweep of a shoulder rising
 * above the corn, the rows flattening ahead of her forearm, then her hand lying across the track and the gate bowed open. Whoever stays in
 * the clearing in her reach is swept back into the ditch, unhurt. Whoever walks on past her hand is set down past her, the asphalt
 * cracking under her weight, and that gap closes behind them in standing corn. The way onward is always open along the ditch to a turnout
 * and up a lane at the east edge of the field. At the turnout, a surveyor's tin box holds a folded field survey for each reader who has
 * heard her question, whatever they did after.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class SphinxField {
    private SphinxField(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.SPHINX_FIELD;
    public static final String KEY="Field",RIDDLE="field";
    public static final String QUESTION="Which path was here before the field?";
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    // ------------------------------------------------------------------ the plan
    public static final int[] GAPS={-8,0,8};
    /** The ditch (z -3..-6, its floor a block down), the clearing (z -7..-15), the gate row in the corn (z -16), the old road's strip (z -34..-36). */
    public static final int DITCH_Z0=-3,DITCH_Z1=-6,CLEAR_Z1=-15,GATE_Z=-16,PATH_END=-26,ROAD_Z0=-34,ROAD_Z1=-36,FIELD_Z1=-45,LANE_X=15,DOOR_Z=-46;
    public static final int REACH=6,PEEK=140,EMERGE=60;
    public static final double SWEEP_Z=-4.5;
    public static final BlockPos CULVERT=new BlockPos(-2,-1,DITCH_Z1-1),BOX=new BlockPos(14,0,-4);
    public static BlockPos gate(int i){return new BlockPos(GAPS[i],0,GATE_Z);}
    public static BlockPos soil(int i){return new BlockPos(GAPS[i],-1,GATE_Z+1);}
    public static BlockPos pin(int i){return new BlockPos(GAPS[i]-1,1,GATE_Z+1);}
    public static BlockPos survey(int i){return new BlockPos(GAPS[i]+1,1,GATE_Z+1);}
    /** Where the old road's strip meets the lane: corn until someone has walked the old road. */
    public static BlockPos jib(){return new BlockPos(LANE_X-1,0,ROAD_Z0-1);}
    static final String[] WHERE={"at the left gate","at the middle gate","at the right gate"};

    static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){l.setBlock(b.offset(at),s,FLAGS);}
    static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){l.setBlock(b.offset(x,y,z),s,FLAGS);}
    static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){Sphinx.box(l,b,x0,y0,z0,x1,y1,z1,s);}
    static void corn(ServerLevel l,BlockPos b,int x,int z){
        var f=Direction.from2DDataValue(Math.floorMod(x*7+z*13,4));
        set(l,b,x,0,z,StateRouteBlock.of(StateRouteBlock.Kind.CORN,f));set(l,b,x,1,z,StateRouteBlock.of(StateRouteBlock.Kind.CORN,f,1));
    }
    static boolean isCorn(ServerLevel l,BlockPos b,int x,int z){return StateRouteBlock.is(l.getBlockState(b.offset(x,0,z)),StateRouteBlock.Kind.CORN);}
    static BlockState gateState(boolean open){return Blocks.SPRUCE_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING,Direction.NORTH).setValue(FenceGateBlock.OPEN,open);}
    static BlockState asphalt(){return StateRouteRegistry.material("asphalt").defaultBlockState();}

    // ------------------------------------------------------------------ shared state
    static CompoundTag room(ServerLevel l){return SphinxCore.room(l,KEY);}
    static void room(ServerLevel l,CompoundTag r){SphinxCore.room(l,KEY,r);}
    public static int live(ServerLevel l){var r=room(l);if(!r.contains("Live")){r.putInt("Live",l.getRandom().nextInt(3));room(l,r);}return r.getInt("Live");}
    public static boolean solved(ServerLevel l){return room(l).getBoolean("Solved");}
    public static boolean opened(ServerLevel l){return room(l).getBoolean("Open");}
    public static int provoked(ServerLevel l){var r=room(l);return r.contains("Provoked")?r.getInt("Provoked"):-1;}
    public static boolean sealed(ServerLevel l,int i){return (room(l).getInt("Sealed")&(1<<i))!=0;}
    public static boolean warned(ServerLevel l,int i){return (room(l).getInt("Warned")&(1<<i))!=0;}
    public static boolean brushed(ServerLevel l,int i){return (room(l).getInt("Brushed")&(1<<i))!=0;}
    public static CompoundTag record(ServerLevel l,UUID reader){return SphinxCore.record(l,reader,RIDDLE);}

    // ------------------------------------------------------------------ dealing
    /** A rare detour, and only to a reader who has already been out in the House's yard: the field is theirs to come back to. */
    public static int weight(LabyrinthData data,UUID player,int depth){
        if(depth<LabyrinthPacing.STORY_DEPTH)return 0;
        var seen=data.visited(player);for(var p:YardCrossings.ORDINARY)if(seen.contains(p.id()))return 2;return 0;
    }

    // ------------------------------------------------------------------ building
    public static void build(ServerLevel l,BlockPos b){
        var grass=Blocks.GRASS_BLOCK.defaultBlockState();var earth=Blocks.DIRT.defaultBlockState();var air=Blocks.AIR.defaultBlockState();
        int live=live(l);var r=room(l);boolean open=r.getBoolean("Open"),solved=r.getBoolean("Solved");
        // Earth and grass, a hedge round the whole of it, and the field.
        box(l,b,-17,-4,DOOR_Z-1,17,-2,0,earth);box(l,b,-17,-1,DOOR_Z-1,17,-1,0,grass);
        box(l,b,-17,0,DOOR_Z-1,17,7,0,YardRooms.leaves());box(l,b,-16,0,DOOR_Z,16,10,-1,air);
        // The House's back wall and door behind the reader, its bulb lit.
        var clapboard=PuzzleRegistry.yardMaterial("clapboard").defaultBlockState();
        box(l,b,-4,0,0,4,4,0,clapboard);box(l,b,-1,0,0,1,3,0,air);box(l,b,-16,8,0,16,10,0,air);set(l,b,2,2,-1,YardBlock.of(YardBlock.Kind.BULB,Direction.NORTH,0));
        // The roadside ditch, a block down, slabs at either bank where the reader steps in and out; the culvert's grate in its far bank.
        box(l,b,-14,-2,DITCH_Z1,12,-1,DITCH_Z0,air);box(l,b,-14,-3,DITCH_Z1,12,-3,DITCH_Z0,Blocks.MUD.defaultBlockState());box(l,b,-14,-2,DITCH_Z1,12,-2,DITCH_Z0,Blocks.COARSE_DIRT.defaultBlockState());
        var step=Blocks.MUD_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.BOTTOM);
        for(int x=-1;x<=1;x++){set(l,b,x,-1,DITCH_Z0,step);set(l,b,x,-1,DITCH_Z1,step);}
        for(int z=DITCH_Z1;z<=DITCH_Z0;z++)set(l,b,12,-1,z,step);
        // The clearing: stubble and trodden grass; the corn round it, standing, the rows running north.
        for(int x=-12;x<=12;x++)for(int z=DITCH_Z1-1;z>=CLEAR_Z1;z--)set(l,b,x,-1,z,(Math.floorMod(x*3+z*5,7)==0?Blocks.COARSE_DIRT:Blocks.GRASS_BLOCK).defaultBlockState());
        for(int x=-16;x<=13;x++)for(int z=GATE_Z;z>=FIELD_Z1;z--){set(l,b,x,-1,z,Blocks.FARMLAND.defaultBlockState());corn(l,b,x,z);}
        for(int x:new int[]{-16,-15,-14,-13})for(int z=DITCH_Z1-1;z>CLEAR_Z1-1;z--){set(l,b,x,-1,z,Blocks.FARMLAND.defaultBlockState());corn(l,b,x,z);}
        set(l,b,CULVERT,SphinxBlock.of(GRILLE,Direction.SOUTH));
        // Three gaps, each with a worn path going in: two newer paths that end in a trodden circle, and the old road.
        for(int i=0;i<3;i++){
            int c=GAPS[i];boolean road=i==live;int end=road?ROAD_Z0:PATH_END;
            for(int z=GATE_Z;z>=end;z--){set(l,b,c,0,z,air);set(l,b,c,1,z,air);set(l,b,c,-1,z,Blocks.DIRT_PATH.defaultBlockState());}
            if(road)for(int x=c-2;x<=c+2;x++)for(int z=GATE_Z-1;z>=ROAD_Z0;z--)if(x!=c)set(l,b,x,-1,z,asphalt());
            if(!road)for(int x=c-1;x<=c+1;x++)for(int z=PATH_END;z>=PATH_END-2;z--){set(l,b,x,0,z,air);set(l,b,x,1,z,air);set(l,b,x,-1,z,Blocks.COARSE_DIRT.defaultBlockState());}
            for(int dx:new int[]{-1,1}){set(l,b,c+dx,-1,GATE_Z,earth);set(l,b,c+dx,0,GATE_Z,Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());set(l,b,c+dx,1,GATE_Z,Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());}
            if(sealed(l,i)){for(int dx=-1;dx<=1;dx++)corn(l,b,c+dx,GATE_Z);set(l,b,soil(i),asphalt());continue;}
            set(l,b,gate(i),gateState(road&&open));
            set(l,b,soil(i),SphinxBlock.of(SOIL,Direction.NORTH,brushed(l,i)?(road?2:1):0));
            set(l,b,pin(i),SphinxBlock.of(PIN,Direction.SOUTH));
            set(l,b,survey(i),SphinxBlock.of(SURVEY,Direction.SOUTH,SphinxCore.pinCount(l,KEY,i)));
        }
        // The old road's strip across the far side of the field, its painted edge, and where it meets the lane.
        for(int x=GAPS[live]-2;x<=LANE_X-1;x++)for(int z=ROAD_Z0;z>=ROAD_Z1;z--){set(l,b,x,0,z,air);set(l,b,x,1,z,air);set(l,b,x,-1,z,z==ROAD_Z1?StateRouteRegistry.material("edge_yellow_n").defaultBlockState():asphalt());}
        for(int z=ROAD_Z0;z>=ROAD_Z1;z--){if(solved){set(l,b,LANE_X-1,0,z,air);set(l,b,LANE_X-1,1,z,air);}else corn(l,b,LANE_X-1,z);}
        // The way onward, always open: the ditch's east end, a gravel turnout with the surveyor's box, the lane up the east edge.
        for(int x=13;x<=LANE_X;x++)for(int z=DITCH_Z0;z>=DITCH_Z1-1;z--)set(l,b,x,-1,z,Blocks.GRAVEL.defaultBlockState());
        for(int z=DITCH_Z1-1;z>=DOOR_Z;z--){set(l,b,LANE_X,-1,z,Blocks.GRAVEL.defaultBlockState());set(l,b,LANE_X,0,z,air);set(l,b,LANE_X,1,z,air);set(l,b,16,0,z,YardRooms.leaves());}
        set(l,b,BOX,SphinxBlock.of(SphinxBlock.Kind.BOX,Direction.WEST));
        set(l,b,LANE_X,2,-24,YardBlock.of(YardBlock.Kind.BULB,Direction.WEST,0));
        // At the lane's end, the side of a shed with a door in it, its eave over the door.
        box(l,b,13,0,DOOR_Z,16,3,DOOR_Z,clapboard);box(l,b,13,4,DOOR_Z+1,16,4,DOOR_Z+1,Blocks.DARK_OAK_SLAB.defaultBlockState());
        set(l,b,LANE_X+1,2,DOOR_Z+1,YardBlock.of(YardBlock.Kind.BULB,Direction.SOUTH,0));
        r.remove("Provoked");r.remove("Emerged");r.remove("Pulled");r.putInt("Warned",0);for(int i=0;i<3;i++)r.remove(peek(i));room(l,r);
        LabyrinthBuilder.entrance(l,b,clapboard,grass,Blocks.DARK_OAK_SLAB.defaultBlockState());
        LabyrinthBuilder.doors(l,b,PLACE);
    }
    public static boolean gateOpen(ServerLevel l,BlockPos b,int i){var s=l.getBlockState(b.offset(gate(i)));return s.getBlock() instanceof FenceGateBlock&&s.getValue(FenceGateBlock.OPEN);}
    public static int soilStage(ServerLevel l,BlockPos b,int i){var s=l.getBlockState(b.offset(soil(i)));return SphinxBlock.is(s,SOIL)?s.getValue(SphinxBlock.STAGE):-1;}
    public static boolean jibOpen(ServerLevel l,BlockPos b){return l.getBlockState(b.offset(jib())).isAir();}

    // ------------------------------------------------------------------ where readers are
    static @Nullable BlockPos base(net.minecraft.server.MinecraftServer server){var o=HouseSavedData.get(server).houseOrigin();return o==null?null:LabyrinthPlaces.base(o,PLACE);}
    static boolean here(ServerPlayer p){var o=HouseSavedData.get(p.server).houseOrigin();return o!=null&&p.level().dimension().equals(HouseDimensions.OUTSIDE)&&LabyrinthPlaces.placeAt(o,p.blockPosition())==PLACE;}
    static double lx(ServerPlayer p,BlockPos b){return p.getX()-b.getX();}
    static double ly(ServerPlayer p,BlockPos b){return p.getY()-b.getY();}
    static double lz(ServerPlayer p,BlockPos b){return p.getZ()-b.getZ();}
    /** Up out of the ditch and in the clearing, short of the gates. */
    public static boolean inClearing(ServerPlayer p,BlockPos b){double z=lz(p,b),x=lx(p,b);return here(p)&&z<DITCH_Z1&&z>GATE_Z+.1&&x>-12.6&&x<12.6&&ly(p,b)>-.5;}
    static List<ServerPlayer> readers(ServerLevel l){var out=new ArrayList<ServerPlayer>();for(var p:l.players())if(!p.isSpectator()&&here(p))out.add(p);return out;}

    // ------------------------------------------------------------------ her voice: first from the ditch, then from high over the corn
    private static final Map<UUID,Long> ECHO=new HashMap<>();
    static void ask(ServerLevel l,BlockPos b,ServerPlayer p){
        l.playSound(null,p.getX(),b.getY()-1,b.getZ()+SWEEP_Z,PuzzleRegistry.SPHINX_VOICE.get(),SoundSource.HOSTILE,1F,.95F);
        p.displayClientMessage(Component.literal("“"+QUESTION+"”").withStyle(net.minecraft.ChatFormatting.GRAY,net.minecraft.ChatFormatting.ITALIC),true);
        ECHO.put(p.getUUID(),l.getGameTime()+30);SphinxCore.heard(l,p.getUUID(),RIDDLE);ASKED.add(p.getUUID());
    }
    static void say(ServerLevel l,BlockPos b,String line,List<ServerPlayer> to){
        for(var p:to){l.playSound(null,p.getX(),p.getY()+7,b.getZ()+GATE_Z-4,PuzzleRegistry.SPHINX_VOICE.get(),SoundSource.HOSTILE,.85F,.9F);
            p.displayClientMessage(Component.literal("“"+line+"”").withStyle(net.minecraft.ChatFormatting.GRAY,net.minecraft.ChatFormatting.ITALIC),true);}
    }
    /** Tassels parting high over the corn: a rustle and chaff coming down, well above the reader's head. */
    static void tassels(ServerLevel l,double x,double y,double z,int n){
        l.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST,Blocks.HAY_BLOCK.defaultBlockState()),x,y,z,n,1.4,.2,1.4,0);
        l.playSound(null,x,y,z,PuzzleRegistry.SPHINX_RUSTLE.get(),SoundSource.HOSTILE,.9F,.9F);
    }

    // ------------------------------------------------------------------ hands on
    private static final Set<UUID> ASKED=new HashSet<>();
    private static final Map<UUID,Long> LAST=new HashMap<>();
    public static void clearAll(){ASKED.clear();LAST.clear();STRIKES.clear();ECHO.clear();EMPTY[0]=0;}
    static int gapOf(BlockPos rel){for(int i=0;i<3;i++)if(rel.equals(gate(i))||rel.equals(soil(i))||rel.equals(pin(i))||rel.equals(survey(i)))return i;return -1;}
    public static boolean use(ServerPlayer p,BlockPos pos,BlockState s){
        if(p.isSpectator()||!here(p))return false;var l=p.serverLevel();var b=base(p.server);if(b==null)return false;
        long now=l.getGameTime();if(now-LAST.getOrDefault(p.getUUID(),-100L)<4)return true;LAST.put(p.getUUID(),now);
        int i=gapOf(pos.subtract(b));
        switch(s.getValue(SphinxBlock.KIND)){
            case SOIL->{if(i>=0)brush(l,b,p,i);}
            case PIN->{if(i>=0)pull(l,b,p,i);}
            case SURVEY->{if(i>=0)survey(l,b,p,i);}
            case GRILLE->{if(provoked(l)>=0){PuzzleRooms.say(p,"Only breathing comes back up the culvert.");return true;}ask(l,b,p);}
            case BOX->box(l,p);
            case FINGERS->PuzzleRooms.say(p,"A hand laid across the track, each finger as long as you are tall, the soil pushed up in front of it.");
            case LIMB->PuzzleRooms.say(p,"Fur, the colour of the stubble. It rises and falls.");
            case FLAT->PuzzleRooms.say(p,"Corn pressed flat, the stalks broken all one way.");
            default->{}
        }
        return true;
    }
    /** Brushing the loose soil at a gap's mouth: what is under it stays bare for everyone, and the reader remembers what they found. */
    static void brush(ServerLevel l,BlockPos b,ServerPlayer p,int i){
        boolean road=i==live(l);var r=room(l);
        if(!brushed(l,i)){r.putInt("Brushed",r.getInt("Brushed")|(1<<i));room(l,r);set(l,b,soil(i),SphinxBlock.of(SOIL,Direction.NORTH,road?2:1));}
        var rec=record(l,p.getUUID());rec.putInt("Brushed",rec.getInt("Brushed")|(1<<i));SphinxCore.record(l,p.getUUID(),RIDDLE,rec);
        var at=b.offset(soil(i)).above();
        l.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,Blocks.DIRT.defaultBlockState()),at.getX()+.5,at.getY()+.1,at.getZ()+.5,10,.3,.05,.3,0);
        l.playSound(null,at,PuzzleRegistry.SPHINX_SAND.get(),SoundSource.BLOCKS,.7F,1.1F);
        PuzzleRooms.say(p,road?"Under the loose soil, asphalt, cracked, with the edge of a painted line. It runs on under the corn on both sides."
            :"Under the loose soil the dirt is packed hard, with footprints in it, going in.");
    }
    /** A gate: opened on the old road (the answer), or caught on its stop. */
    static void gate(ServerLevel l,BlockPos b,ServerPlayer p,int i){
        if(sealed(l,i)){PuzzleRooms.say(p,"There is no gap there now. The corn stands thick where it was.");return;}
        if(provoked(l)==i){PuzzleRooms.say(p,"The gate is bowed in. Something is lying against it.");return;}
        if(i==live(l)&&opened(l)){PuzzleRooms.say(p,"The gate stands open on the old road.");return;}
        if(provoked(l)>=0){PuzzleRooms.say(p,"Something heavy is lying in the corn. The gates do not move.");return;}
        if(!SphinxCore.begin(l,KEY)){PuzzleRooms.say(p,"Another gate is still moving.");return;}
        int live=live(l);boolean right=i==live;
        var answered=SphinxCore.commit(l,KEY,RIDDLE,p,i,live);
        for(int k=0;k<3;k++)SphinxCore.socket(l,b.offset(survey(k)),Direction.SOUTH,0);
        for(var uuid:answered.keySet()){if(uuid.equals(p.getUUID()))continue;var other=l.getServer().getPlayerList().getPlayer(uuid);
            if(other!=null)PuzzleRooms.say(other,"A gate is lifted. Your pin "+WHERE[answered.get(uuid)]+" is your answer.");}
        if(right){
            var r=room(l);r.putBoolean("Open",true);room(l,r);set(l,b,gate(i),gateState(true));
            l.playSound(null,b.offset(gate(i)),PuzzleRegistry.SPHINX_LATCH.get(),SoundSource.BLOCKS,.9F,.8F);
            PuzzleRooms.say(p,"The gate swings open on the old road.");
        }else{
            l.playSound(null,b.offset(gate(i)),PuzzleRegistry.SPHINX_CATCH.get(),SoundSource.BLOCKS,1F,.8F);
            var r=room(l);r.putInt("Warned",r.getInt("Warned")|(1<<i));
            set(l,b,pin(i),SphinxBlock.of(PIN,Direction.SOUTH,1));
            PuzzleRooms.say(p,"The gate lifts an inch and catches on its stop.");
            say(l,b,"Not that one. That path came after the field.",readers(l));
            r.putLong(peek(i),l.getGameTime()+PEEK);room(l,r);eye(l,b,i,true);
        }
    }
    /** The stop pin pulled: the provocation. */
    static void pull(ServerLevel l,BlockPos b,ServerPlayer p,int i){
        var s=l.getBlockState(b.offset(pin(i)));
        if(SphinxBlock.stage(s)==0){PuzzleRooms.say(p,"The gate's stop. Something small is set into the post, flush.");return;}
        if(SphinxBlock.stage(s)==2||provoked(l)>=0){PuzzleRooms.say(p,"The pin is out.");return;}
        set(l,b,pin(i),s.setValue(SphinxBlock.STAGE,2));
        var r=room(l);r.putInt("Provoked",i);r.putLong("Pulled",l.getGameTime());r.putLong("Emerged",l.getGameTime()+EMERGE);r.remove(peek(i));room(l,r);
        SphinxCore.forced(l,p.getUUID(),RIDDLE);
        l.playSound(null,b.offset(pin(i)),PuzzleRegistry.SPHINX_PIN.get(),SoundSource.BLOCKS,1F,1F);
        PuzzleRooms.say(p,"The pin comes free. Out in the corn, something the size of a barn shifts its weight.");
        EMPTY[0]=0;
    }
    static void survey(ServerLevel l,BlockPos b,ServerPlayer p,int i){
        if(sealed(l,i)){PuzzleRooms.say(p,"The socket is lost in the corn.");return;}
        int was=SphinxCore.pinOf(l,KEY,p.getUUID());int now=SphinxCore.pin(l,KEY,p.getUUID(),i);
        if(was>=0)SphinxCore.socket(l,b.offset(survey(was)),Direction.SOUTH,SphinxCore.pinCount(l,KEY,was));
        SphinxCore.socket(l,b.offset(survey(i)),Direction.SOUTH,SphinxCore.pinCount(l,KEY,i));
        SphinxCore.pinned(l,p,b.offset(survey(i)),now,WHERE[i]);
    }
    static void unpin(ServerLevel l,BlockPos b,UUID reader){
        int was=SphinxCore.pinOf(l,KEY,reader);if(was<0)return;SphinxCore.unpin(l,KEY,reader);
        SphinxCore.socket(l,b.offset(survey(was)),Direction.SOUTH,SphinxCore.pinCount(l,KEY,was));
    }

    // ------------------------------------------------------------------ the folded survey
    /**
     * The surveyor's tin at the turnout: one folded field survey to each reader who has heard her question, whatever they did after, and
     * another only if they no longer carry theirs. Two sketches of the culvert from the same day: a road over it in one, corn planted
     * straight across it in the other. A reader who brushed the old road bare has something to add to either.
     */
    static void box(ServerLevel l,ServerPlayer p){
        if(!record(l,p.getUUID()).getBoolean("Heard")){PuzzleRooms.say(p,"A surveyor's tin on a post, its lid rusted down.");return;}
        if(carries(p)){PuzzleRooms.say(p,"The tin is empty. You have the survey that was in it.");return;}
        p.getInventory().placeItemBackInInventory(survey(p,(record(l,p.getUUID()).getInt("Brushed")&(1<<live(l)))!=0));
        var rec=record(l,p.getUUID());rec.putBoolean("Survey",true);SphinxCore.record(l,p.getUUID(),RIDDLE,rec);
        l.playSound(null,p.blockPosition(),PuzzleRegistry.SPHINX_LATCH.get(),SoundSource.BLOCKS,.5F,1.5F);
        PuzzleRooms.say(p,"Inside the tin, a field survey, folded small.");
    }
    public static ItemStack survey(ServerPlayer owner,boolean brushedRoad){
        var s=new ItemStack(Items.PAPER);s.set(DataComponents.CUSTOM_NAME,Component.literal("Folded Field Survey").withStyle(net.minecraft.ChatFormatting.RESET));
        var tag=new CompoundTag();tag.putBoolean("SphinxSurvey",true);tag.putUUID("Owner",owner.getUUID());s.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
        var lines=new ArrayList<Component>();
        lines.add(Component.literal("Two sketches of the culvert, dated the same day.").withStyle(net.minecraft.ChatFormatting.GRAY));
        lines.add(Component.literal("In one, a road runs over it.").withStyle(net.minecraft.ChatFormatting.GRAY));
        lines.add(Component.literal("In the other, corn is planted straight across it.").withStyle(net.minecraft.ChatFormatting.GRAY));
        if(brushedRoad)lines.add(Component.literal("You know where the road paint ran under the soil.").withStyle(net.minecraft.ChatFormatting.DARK_GRAY,net.minecraft.ChatFormatting.ITALIC));
        s.set(DataComponents.LORE,new ItemLore(lines));return s;
    }
    public static boolean isSurvey(ItemStack s){var d=s.get(DataComponents.CUSTOM_DATA);return s.is(Items.PAPER)&&d!=null&&d.copyTag().getBoolean("SphinxSurvey");}
    public static boolean carries(ServerPlayer p){for(var s:p.getInventory().items)if(isSurvey(s))return true;return isSurvey(p.getOffhandItem());}

    /** The gates open only one way: an answer, at the gate itself. */
    @SubscribeEvent public static void gateHand(PlayerInteractEvent.RightClickBlock e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!(e.getLevel() instanceof ServerLevel l))return;
        if(!(l.getBlockState(e.getPos()).getBlock() instanceof FenceGateBlock)||!here(p))return;
        var b=base(p.server);if(b==null)return;var rel=e.getPos().subtract(b);int i=-1;for(int k=0;k<3;k++)if(rel.equals(gate(k)))i=k;if(i<0)return;
        e.setCanceled(true);e.setCancellationResult(InteractionResult.CONSUME);
        long now=l.getGameTime();if(now-LAST.getOrDefault(p.getUUID(),-100L)<4)return;LAST.put(p.getUUID(),now);
        gate(l,b,p,i);
    }

    // ------------------------------------------------------------------ her eye, her shoulder, her forearm and her hand
    static String peek(int i){return "PeekUntil"+i;}
    /** Her eye over the corn behind gap i, upright and facing the clearing; or gone. */
    static void eye(ServerLevel l,BlockPos b,int i,boolean on){
        int x0=GAPS[i]-1,z=GATE_Z-4;int look=on?lookAt(l,b,i):4;
        for(int dx=0;dx<4;dx++)for(int dy=0;dy<2;dy++){var at=new BlockPos(x0+dx,3+dy,z);var st=l.getBlockState(b.offset(at));
            if(on&&(st.isAir()||SphinxEyeBlock.is(st)))set(l,b,at,SphinxEyeBlock.of((dy==1?0:4)+dx,look,SphinxEyeBlock.SOUTH));
            else if(!on&&SphinxEyeBlock.is(st))set(l,b,at,Blocks.AIR.defaultBlockState());}
    }
    public static boolean eyeUp(ServerLevel l,BlockPos b,int i){return SphinxEyeBlock.is(l.getBlockState(b.offset(GAPS[i]-1,3,GATE_Z-4)));}
    public static int lookAt(ServerLevel l,BlockPos b,int i){
        double cx=b.getX()+GAPS[i]+1;ServerPlayer near=null;double best=Double.MAX_VALUE;
        for(var p:readers(l)){double d=Math.pow(p.getX()-cx,2)+Math.pow(p.getZ()-(b.getZ()+GATE_Z),2);if(d<best){best=d;near=p;}}
        if(near==null)return 4;double dx=near.getX()-cx,dy=near.getEyeY()-(b.getY()+4);
        return (dx<-1.5?0:dx>1.5?2:1)+3*(dy<-2?0:dy>2?2:1);
    }
    static void follow(ServerLevel l,BlockPos b,int i){
        if(!eyeUp(l,b,i))return;int look=lookAt(l,b,i);
        for(int dx=0;dx<4;dx++)for(int dy=0;dy<2;dy++){var at=b.offset(GAPS[i]-1+dx,3+dy,GATE_Z-4);var st=l.getBlockState(at);if(SphinxEyeBlock.is(st)&&st.getValue(SphinxEyeBlock.LOOK)!=look)l.setBlock(at,st.setValue(SphinxEyeBlock.LOOK,look),FLAGS);}
    }
    /** The sweep of her shoulder above the corn, risen {@code h} blocks (0 gone): fur, well back in the field to the east of the gap. */
    static void shoulder(ServerLevel l,BlockPos b,int i,int h){
        int c=GAPS[i]+(i==2?-4:4);
        for(int x=c-1;x<=c+1;x++)for(int z=GATE_Z-10;z>=GATE_Z-12;z--)for(int y=2;y<=5;y++){var at=new BlockPos(x,y,z);var st=l.getBlockState(b.offset(at));
            boolean on=h>0&&y<2+h-(x==c?0:1);
            if(on&&st.isAir())set(l,b,at,SphinxBlock.of(LIMB,Direction.SOUTH));else if(!on&&SphinxBlock.is(st,LIMB))set(l,b,at,Blocks.AIR.defaultBlockState());}
    }
    public static boolean shoulderUp(ServerLevel l,BlockPos b,int i){return SphinxBlock.is(l.getBlockState(b.offset(GAPS[i]+(i==2?-4:4),2,GATE_Z-11)),LIMB);}
    /** The corn flattened either side of the path, from back in the field toward the gate, {@code rows} rows of it (0 standing again). */
    static void flatten(ServerLevel l,BlockPos b,int i,int rows){
        int c=GAPS[i];
        for(int z=PATH_END;z<=GATE_Z-1;z++){
            boolean flat=z<PATH_END+rows;
            for(int x:new int[]{c-2,c-1,c+1,c+2}){var st=l.getBlockState(b.offset(x,0,z));
                if(flat&&StateRouteBlock.is(st,StateRouteBlock.Kind.CORN)){set(l,b,x,1,z,Blocks.AIR.defaultBlockState());set(l,b,x,0,z,SphinxBlock.of(FLAT,Direction.SOUTH));}
                else if(!flat&&SphinxBlock.is(st,FLAT))corn(l,b,x,z);}
        }
    }
    public static boolean flattened(ServerLevel l,BlockPos b,int i){return SphinxBlock.is(l.getBlockState(b.offset(GAPS[i]-1,0,GATE_Z-1)),FLAT);}
    /** Her forearm along the path and her hand across the track just inside the gate. */
    static void hand(ServerLevel l,BlockPos b,int i,boolean on){
        int c=GAPS[i];
        for(int z=GATE_Z-2;z>=GATE_Z-8;z--){var at=new BlockPos(c,0,z);var st=l.getBlockState(b.offset(at));
            if(on&&st.isAir())set(l,b,at,SphinxBlock.of(LIMB,Direction.SOUTH));else if(!on&&SphinxBlock.is(st,LIMB))set(l,b,at,Blocks.AIR.defaultBlockState());}
        for(int dx=-1;dx<=1;dx++){var at=new BlockPos(c+dx,0,GATE_Z-1);var st=l.getBlockState(b.offset(at));
            if(on&&(st.isAir()||SphinxBlock.is(st,FLAT)))set(l,b,at,SphinxBlock.of(FINGERS,Direction.NORTH));else if(!on&&SphinxBlock.is(st,FINGERS))set(l,b,at,dx==0?Blocks.AIR.defaultBlockState():SphinxBlock.of(FLAT,Direction.SOUTH));}
    }
    public static boolean handDown(ServerLevel l,BlockPos b,int i){return SphinxBlock.is(l.getBlockState(b.offset(GAPS[i],0,GATE_Z-1)),FINGERS);}

    // ------------------------------------------------------------------ the clock
    private static final Map<UUID,Long> STRIKES=new HashMap<>();
    private static final long[] EMPTY={0};
    static void update(ServerLevel l,BlockPos b){
        var readers=readers(l);var r=room(l);int live=live(l);long now=l.getGameTime();
        if(now%20<2)for(var uuid:SphinxCore.pins(l,KEY).keySet()){var q=l.getServer().getPlayerList().getPlayer(uuid);if(q==null||!here(q))unpin(l,b,uuid);}
        for(var it=ECHO.entrySet().iterator();it.hasNext();){var e=it.next();if(e.getValue()>now)continue;it.remove();var p=l.getServer().getPlayerList().getPlayer(e.getKey());
            if(p!=null&&here(p)){tassels(l,p.getX(),b.getY()+6,p.getZ()-4,14);l.playSound(null,p.getX(),b.getY()+8,p.getZ()-5,PuzzleRegistry.SPHINX_VOICE.get(),SoundSource.HOSTILE,.7F,.85F);}}
        for(int i=0;i<3;i++){
            r=room(l);if(!r.contains(peek(i)))continue;long until=r.getLong(peek(i));
            if(until<=now){eye(l,b,i,false);r.remove(peek(i));room(l,r);continue;}
            if(now%4<2)follow(l,b,i);
            if(now%10<2){var g=b.offset(gate(i));tassels(l,g.getX()+.5,g.getY()+3,g.getZ()-3.5,4);}
        }
        r=room(l);
        for(var p:readers){
            if((inClearing(p,b)||lz(p,b)<DITCH_Z0+.5&&lz(p,b)>DITCH_Z1-.5&&ly(p,b)<-.4)&&!ASKED.contains(p.getUUID())&&provoked(l)<0)ask(l,b,p);
            // Up the old road and onto the strip of asphalt: round to the way onward, open for everyone from now on.
            if(lz(p,b)<ROAD_Z0+.5&&lz(p,b)>ROAD_Z1-.5&&lx(p,b)<LANE_X-1&&!r.getBoolean("Solved")){
                r.putBoolean("Solved",true);room(l,r);for(int z=ROAD_Z0;z>=ROAD_Z1;z--){set(l,b,LANE_X-1,0,z,Blocks.AIR.defaultBlockState());set(l,b,LANE_X-1,1,z,Blocks.AIR.defaultBlockState());}
                tassels(l,b.getX()+LANE_X-1,b.getY()+1,b.getZ()+ROAD_Z0-1,8);
                PuzzleRooms.say(p,"The old road runs on east under the corn, and at its end the stalks fall aside on a lane.");
            }
            int forced=provoked(l);
            if(forced>=0&&r.getLong("Emerged")<=now&&lz(p,b)<GATE_Z-.2&&lz(p,b)>GATE_Z-9&&Math.abs(lx(p,b)-(GAPS[forced]+.5))<2.6){
                // On past her hand: the asphalt shifts under her weight, the reader is set down past her, and the gap closes in corn.
                l.playSound(null,p.blockPosition(),PuzzleRegistry.SPHINX_CRACK.get(),SoundSource.BLOCKS,1.2F,.8F);
                HouseInternalTeleport.translate(p,b.getX()+LANE_X+.5-p.getX(),b.getY()-p.getY(),b.getZ()-30.5-p.getZ());p.fallDistance=0;
                r=room(l);r.putInt("Sealed",r.getInt("Sealed")|(1<<forced));room(l,r);
                withdraw(l,b);
                int c=GAPS[forced];for(int dx=-1;dx<=1;dx++)corn(l,b,c+dx,GATE_Z);set(l,b,soil(forced),asphalt());set(l,b,pin(forced),Blocks.AIR.defaultBlockState());set(l,b,survey(forced),Blocks.AIR.defaultBlockState());
                r=room(l);
                PuzzleRooms.say(p,"The track cracks and tilts under you, and you are put down somewhere past her, on a lane. Behind you the corn closes.");
            }
        }
        for(var uuid:new ArrayList<>(ASKED)){
            var p=l.getServer().getPlayerList().getPlayer(uuid);
            if(p==null){ASKED.remove(uuid);unpin(l,b,uuid);continue;}
            if(!here(p)){ASKED.remove(uuid);unpin(l,b,uuid);var rec=record(l,uuid);
                if(rec.getBoolean("Retreated")){rec.remove("Retreated");SphinxCore.record(l,uuid,RIDDLE,rec);}
                else if(!rec.contains("First")&&provoked(l)<0)SphinxCore.walked(l,uuid,RIDDLE);}
        }
        int forced=provoked(l);
        if(forced<0)return;
        long e=now-r.getLong("Pulled"),emerged=r.getLong("Emerged");var g=b.offset(gate(forced));
        // She rises out of the stalks in stages.
        if(e<40&&now%6<2)tassels(l,g.getX()+.5,g.getY()+3,g.getZ()-9+e/5.0,6);
        if(e>=8)shoulder(l,b,forced,(int)Math.min(4,1+(e-8)/8));
        if(e>=16)flatten(l,b,forced,(int)Math.min(10,(e-16)/2+1));
        if(e>=40&&!handDown(l,b,forced)){hand(l,b,forced,true);set(l,b,gate(forced),gateState(true));eye(l,b,forced,true);
            l.playSound(null,g,PuzzleRegistry.SPHINX_STRIKE.get(),SoundSource.HOSTILE,1F,.6F);}
        if(now%4<2)follow(l,b,forced);
        if(emerged>now)return;
        boolean anyone=false;
        for(var p:readers){
            if(!inClearing(p,b))continue;anyone=true;
            var rec=record(l,p.getUUID());if(!rec.getBoolean("Retreated")){rec.putBoolean("Retreated",true);SphinxCore.record(l,p.getUUID(),RIDDLE,rec);}
            if(Math.abs(lx(p,b)-(GAPS[forced]+.5))>REACH)continue;
            long t=STRIKES.getOrDefault(p.getUUID(),0L);
            if(t==0){STRIKES.put(p.getUUID(),now+40);l.playSound(null,p.blockPosition(),PuzzleRegistry.SPHINX_BREATH.get(),SoundSource.HOSTILE,1F,1.3F);
                l.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST,Blocks.HAY_BLOCK.defaultBlockState()),p.getX(),p.getY()+2.4,p.getZ(),12,.4,.1,.4,0);}
            else if(t<=now){STRIKES.remove(p.getUUID());
                // She never hurts anyone: her hand sweeps them back into the ditch, the line she stops at.
                l.playSound(null,p.blockPosition(),PuzzleRegistry.SPHINX_STRIKE.get(),SoundSource.HOSTILE,1F,1F);
                double x=Math.max(-12.5,Math.min(11.5,lx(p,b)));
                HouseInternalTeleport.translate(p,b.getX()+x-p.getX(),b.getY()-1-p.getY(),b.getZ()+SWEEP_Z-p.getZ());p.fallDistance=0;
                var rec2=record(l,p.getUUID());rec2.putInt("Swept",rec2.getInt("Swept")+1);SphinxCore.record(l,p.getUUID(),RIDDLE,rec2);
                PuzzleRooms.say(p,"Her hand comes across the clearing and sweeps you back into the ditch.");}
        }
        for(var p:readers)if(!inClearing(p,b))STRIKES.remove(p.getUUID());
        if(anyone)EMPTY[0]=0;else if(++EMPTY[0]>=50)withdraw(l,b);
    }
    /** She sinks back into the corn: the hand and forearm go, the rows stand again, her shoulder and eye sink, the gate drops shut. */
    static void withdraw(ServerLevel l,BlockPos b){
        var r=room(l);int i=r.contains("Provoked")?r.getInt("Provoked"):-1;if(i<0)return;
        hand(l,b,i,false);flatten(l,b,i,0);shoulder(l,b,i,0);eye(l,b,i,false);
        if(!sealed(l,i)){set(l,b,gate(i),gateState(false));set(l,b,pin(i),SphinxBlock.of(PIN,Direction.SOUTH));}
        r=room(l);r.remove("Provoked");r.remove("Emerged");r.remove("Pulled");r.putInt("Warned",r.getInt("Warned")&~(1<<i));room(l,r);
        STRIKES.clear();EMPTY[0]=0;
        l.playSound(null,b.offset(gate(i)).north(8),PuzzleRegistry.SPHINX_SETTLE.get(),SoundSource.HOSTILE,.9F,.8F);
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();if(server.getTickCount()%2!=0)return;
        var l=server.getLevel(HouseDimensions.OUTSIDE);var b=base(server);if(l==null||b==null||!l.isLoaded(b))return;
        if(LabyrinthData.get(server).door(PLACE.entryDoorId())==null)return;
        var r=room(l);boolean any=!ASKED.isEmpty()||provoked(l)>=0||r.contains("Pins");
        for(int i=0;i<3&&!any;i++)if(r.contains(peek(i)))any=true;
        if(!any)for(var p:l.players())if(here(p)){any=true;break;}
        if(any)update(l,b);
    }
}
