package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import static io.github.knaitoe.theoldesthouse.labyrinth.ResonanceBlock.Kind.*;

/**
 * 0.4.360, the Resonant Pipes (docs/RESONANT_PIPES_0_4_360.md), the owner's "New Puzzle and Hazard Room Designs", item 2: "Certain pipes
 * in the walls carry a faint continuous tone. Blocking or opening sections of pipe changes which tone is heard in each room. The early
 * room teaches that the sound is continuous and travels. The deep version requires the reader to create a specific combination of tones
 * that unlocks a sympathetic latch."
 * <p>
 * Three iron drums hum three notes, low, middle and high, into runs of pipe. A tone goes wherever the pipe is open: valves shut it off,
 * a short section lifted out onto its bracket breaks a run, and a cross pipe left open lets one run's note into another. At the far
 * door a box of tuned brass reeds listens: a reed whose own note reaches it alone sings and the dust lifts off it; anything else
 * there makes it buzz. When every reed sings the catch on the door lifts.
 * <ul>
 * <li><b>The resonant pipe</b> (early): one long service corridor, the three runs along it at head height, a valve at each end of each
 * run, short sections in the middle and high runs. The reed box wants the middle note only. At first the middle run's section is out
 * and every valve is open, so the low and high notes reach the reeds together and they buzz.</li>
 * <li><b>The resonant pipes</b> (deep): three rooms in a row with windows between, sharing the network. A cross pipe in the first room
 * lets the middle and high notes mix; one in the second lets the high note into the low run, another into the middle run. The high run
 * is bricked up at the last wall: it reaches the last room only by a loop over the ceiling, opened there. Three reeds, low, middle and
 * high, each want their own note alone.</li>
 * </ul>
 * Like every puzzle room nothing counts down, hurts or is used up; every valve can be turned back; solved stays solved, for everyone;
 * no Witness. The valves are left as the solver left them.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class ResonantPipes {
    private ResonantPipes(){}
    public static final LabyrinthPlace EARLY=LabyrinthPlace.RESONANT_PIPE,DEEP=LabyrinthPlace.RESONANT_PIPES;
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    public static boolean is(@Nullable LabyrinthPlace p){return p==EARLY||p==DEEP;}

    // ------------------------------------------------------------------ the network
    /** A length of pipe between two joints: always open, or opened and shut by the fitting at {@code control}; the cells it shows in. */
    public record Edge(String a,String b,@Nullable BlockPos control,List<BlockPos> cells){}
    /** A reed box: where it is, the joint it listens at, and the one note it sings for. */
    public record Reed(BlockPos at,String node,char wants){}
    public record Net(Map<Character,String> sources,List<Edge> edges,List<Reed> reeds,BlockPos latch){}

    static List<BlockPos> run(int x,int y,int z0,int z1){var out=new ArrayList<BlockPos>();for(int z=Math.max(z0,z1);z>=Math.min(z0,z1);z--)out.add(new BlockPos(x,y,z));return out;}
    @SafeVarargs static List<BlockPos> cells(List<BlockPos>... parts){var out=new ArrayList<BlockPos>();for(var p:parts)out.addAll(p);return out;}

    // The early corridor: drums over the entry, valves at z -2 and -18, sections at z -11, the manifold at z -19, the reed box at z -20.
    public static final BlockPos E_REED=new BlockPos(0,1,-20),E_LATCH=new BlockPos(1,1,-20);
    public static final BlockPos A_L=new BlockPos(-1,2,-2),A_M=new BlockPos(0,2,-2),A_H=new BlockPos(1,2,-2),F_L=new BlockPos(-1,2,-18),F_M=new BlockPos(0,2,-18),F_H=new BlockPos(1,2,-18),
        S_M=new BlockPos(0,2,-11),S_H=new BlockPos(1,2,-11);
    public static final Net EARLY_NET=new Net(Map.of('L',"sL",'M',"sM",'H',"sH"),List.of(
        new Edge("sL","aL",A_L,List.of()),new Edge("aL","bL",null,run(-1,2,-3,-17)),new Edge("bL","fL",F_L,List.of()),new Edge("fL","man",null,List.of(new BlockPos(-1,2,-19))),
        new Edge("sM","aM",A_M,List.of()),new Edge("aM","pM",null,run(0,2,-3,-10)),new Edge("pM","qM",S_M,List.of()),new Edge("qM","bM",null,run(0,2,-12,-17)),new Edge("bM","fM",F_M,List.of()),new Edge("fM","man",null,List.of(new BlockPos(0,2,-19))),
        new Edge("sH","aH",A_H,List.of()),new Edge("aH","pH",null,run(1,2,-3,-10)),new Edge("pH","qH",S_H,List.of()),new Edge("qH","bH",null,run(1,2,-12,-17)),new Edge("bH","fH",F_H,List.of()),new Edge("fH","man",null,List.of(new BlockPos(1,2,-19))),
        new Edge("man","reed",null,List.of(new BlockPos(0,2,-20)))),
        List.of(new Reed(E_REED,"reed",'M')),E_LATCH);

    // The deep rooms: A z -1..-10, B z -12..-21, C z -23..-32; the runs at x -2, 0, 2; the cross pipes and the loop.
    public static final BlockPos X_MH=new BlockPos(1,2,-6),X_HL=new BlockPos(0,3,-15),X_HM=new BlockPos(1,2,-18),V_LOOP=new BlockPos(2,2,-24),
        R_L=new BlockPos(-2,1,-32),R_M=new BlockPos(0,1,-32),R_H=new BlockPos(2,1,-32),D_LATCH=new BlockPos(4,1,-32);
    public static final Net DEEP_NET=new Net(Map.of('L',"sL",'M',"sM",'H',"sH"),List.of(
        // The low run, straight through, its middle joint where the high cross pipe comes down.
        new Edge("sL","l15",null,run(-2,2,-2,-14)),new Edge("l15","cL",null,run(-2,2,-15,-31)),new Edge("cL","rL",null,List.of(new BlockPos(-2,2,-32))),
        // The middle run, with joints at the first room's cross pipe and the second's.
        new Edge("sM","m6",null,run(0,2,-2,-5)),new Edge("m6","m18",null,run(0,2,-6,-17)),new Edge("m18","cM",null,run(0,2,-18,-31)),new Edge("cM","rM",null,List.of(new BlockPos(0,2,-32))),
        // The high run, bricked up at the last wall; the loop over the ceiling, and its valve in the last room.
        new Edge("sH","h6",null,run(2,2,-2,-5)),new Edge("h6","h15",null,run(2,2,-6,-14)),new Edge("h15","h18",null,run(2,2,-15,-17)),new Edge("h18","h21",null,run(2,2,-18,-21)),
        new Edge("h21","loop",null,cells(run(2,3,-21,-21),run(2,4,-21,-21),run(2,5,-21,-24),run(2,4,-24,-24),run(2,3,-24,-24))),
        new Edge("loop","cH",V_LOOP,List.of()),new Edge("cH","rH",null,cells(run(2,2,-25,-31),List.of(new BlockPos(2,2,-32)))),
        // The cross pipes.
        new Edge("m6","h6",X_MH,List.of()),
        new Edge("h15","l15",X_HL,List.of(new BlockPos(2,3,-15),new BlockPos(1,3,-15),new BlockPos(-1,3,-15),new BlockPos(-2,3,-15))),
        new Edge("m18","h18",X_HM,List.of())),
        List.of(new Reed(R_L,"rL",'L'),new Reed(R_M,"rM",'M'),new Reed(R_H,"rH",'H')),D_LATCH);

    public static Net net(LabyrinthPlace place){return place==EARLY?EARLY_NET:DEEP_NET;}
    /** Whether a length of pipe is open: always, or while its valve is open or its section fitted. */
    static boolean open(ServerLevel l,BlockPos b,Edge e){return e.control()==null||ResonanceBlock.stage(l.getBlockState(b.offset(e.control())))==1;}
    /** Which notes reach each joint: each drum's note spreads along every open length of pipe. */
    public static Map<String,Set<Character>> tones(ServerLevel l,BlockPos b,Net n){
        var adj=new HashMap<String,List<String>>();
        for(var e:n.edges())if(open(l,b,e)){adj.computeIfAbsent(e.a(),k->new ArrayList<>()).add(e.b());adj.computeIfAbsent(e.b(),k->new ArrayList<>()).add(e.a());}
        var out=new HashMap<String,Set<Character>>();
        for(var src:n.sources().entrySet()){
            var seen=new HashSet<String>();var queue=new ArrayDeque<String>();queue.add(src.getValue());seen.add(src.getValue());
            while(!queue.isEmpty()){var at=queue.poll();out.computeIfAbsent(at,k->new TreeSet<>()).add(src.getKey());for(var next:adj.getOrDefault(at,List.of()))if(seen.add(next))queue.add(next);}
        }
        return out;
    }
    /** What a reed hears: nothing, its own note alone, or anything else. */
    public static int reedState(Set<Character> heard,char wants){return heard.isEmpty()?0:heard.size()==1&&heard.contains(wants)?1:2;}
    public static Set<Character> heard(ServerLevel l,BlockPos b,LabyrinthPlace place,Reed r){return tones(l,b,net(place)).getOrDefault(r.node(),Set.of());}
    /** The notes running along the pipe in a cell (relative to the room), empty where it is still, or null where no length of pipe runs. */
    public static @Nullable Set<Character> notesAt(ServerLevel l,BlockPos b,LabyrinthPlace place,BlockPos rel){
        var n=net(place);var tones=tones(l,b,n);
        for(var e:n.edges())if(e.cells().contains(rel))return open(l,b,e)?tones.getOrDefault(e.a(),Set.of()):Set.of();
        return null;
    }

    // ------------------------------------------------------------------ building
    static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){l.setBlock(b.offset(at),s,FLAGS);}
    static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){l.setBlock(b.offset(x,y,z),s,FLAGS);}
    static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){
        for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int y=Math.min(y0,y1);y<=Math.max(y0,y1);y++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)set(l,b,x,y,z,s);
    }
    static BlockState air(){return Blocks.AIR.defaultBlockState();}
    static BlockState fit(ResonanceBlock.Kind k,Direction f,int stage){return ResonanceBlock.of(k,f,stage);}
    /** Lays every length's pipe: along z as runs, across as cross pipes, upright where it climbs; the controls are placed by the rooms. */
    static void lay(ServerLevel l,BlockPos b,Net n){
        for(var e:n.edges()){
            var cs=e.cells();
            for(int i=0;i<cs.size();i++){
                var c=cs.get(i);var prev=i>0?cs.get(i-1):null;var next=i+1<cs.size()?cs.get(i+1):null;
                boolean last=i==cs.size()-1,toReed=e.b().equals("reed")||e.b().startsWith("r");
                boolean vertical=toReed&&last||c.getY()!=2&&(i==0||last)||prev!=null&&prev.getY()!=c.getY()||next!=null&&next.getY()!=c.getY();
                boolean across=!vertical&&(prev!=null&&prev.getX()!=c.getX()||next!=null&&next.getX()!=c.getX()||e.a().equals("fL")||e.a().equals("fH"));
                set(l,b,c,vertical?fit(RISER,Direction.NORTH,0):fit(PIPE,across?Direction.EAST:Direction.NORTH,0));
            }
        }
    }
    static void lantern(ServerLevel l,BlockPos b,int x,int y,int z){set(l,b,x,y,z,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));}
    static boolean solved(ServerLevel l,LabyrinthPlace place){return PuzzleRooms.solved(l.getServer(),place);}

    public static void buildEarly(ServerLevel l,BlockPos b){
        boolean solved=solved(l,EARLY);
        var brick=Blocks.STONE_BRICKS.defaultBlockState();var floor=Blocks.SPRUCE_PLANKS.defaultBlockState();
        box(l,b,-4,-1,-22,4,5,0,brick);
        box(l,b,-3,-1,-20,3,-1,-1,floor);box(l,b,-3,0,-20,3,3,-1,air());
        // The drums on brackets over the entry, one to a run.
        set(l,b,-1,2,-1,fit(DRUM,Direction.SOUTH,0));set(l,b,0,2,-1,fit(DRUM,Direction.SOUTH,1));set(l,b,1,2,-1,fit(DRUM,Direction.SOUTH,2));
        lay(l,b,EARLY_NET);
        for(var v:List.of(A_L,A_M,A_H,F_L,F_M,F_H))set(l,b,v,fit(VALVE,Direction.NORTH,1));
        if(solved){set(l,b,A_L,fit(VALVE,Direction.NORTH,0));set(l,b,A_H,fit(VALVE,Direction.NORTH,0));}
        set(l,b,S_M,fit(SECTION,Direction.NORTH,solved?1:0));set(l,b,S_H,fit(SECTION,Direction.NORTH,1));
        set(l,b,E_REED,fit(REED,Direction.SOUTH,0));set(l,b,E_LATCH,fit(LATCH,Direction.SOUTH,solved?1:0));
        lantern(l,b,-2,3,-6);lantern(l,b,2,3,-14);
        set(l,b,-3,0,-8,Blocks.BARREL.defaultBlockState());set(l,b,3,0,-15,Blocks.CHAIN.defaultBlockState());
        LabyrinthBuilder.entrance(l,b,brick,floor,brick);
        LabyrinthBuilder.doors(l,b,EARLY);
    }

    public static void buildDeep(ServerLevel l,BlockPos b){
        boolean solved=solved(l,DEEP);
        var brick=Blocks.STONE_BRICKS.defaultBlockState();var floor=Blocks.SPRUCE_PLANKS.defaultBlockState();
        box(l,b,-7,-1,-34,7,6,0,brick);
        for(int[] room:new int[][]{{-1,-10},{-12,-21},{-23,-32}}){box(l,b,-6,-1,room[1],6,-1,room[0],floor);box(l,b,-6,0,room[1],6,3,room[0],air());}
        // The doorways between the rooms, and a window in each wall to see the next room's pipes and reeds.
        for(int z:new int[]{-11,-22}){box(l,b,-5,0,z,-4,1,z,air());for(int x=-1;x<=1;x++)set(l,b,x,1,z,Blocks.GLASS_PANE.defaultBlockState());}
        set(l,b,-2,2,-1,fit(DRUM,Direction.SOUTH,0));set(l,b,0,2,-1,fit(DRUM,Direction.SOUTH,1));set(l,b,2,2,-1,fit(DRUM,Direction.SOUTH,2));
        lay(l,b,DEEP_NET);
        set(l,b,X_MH,fit(VALVE,Direction.EAST,solved?0:1));set(l,b,X_HL,fit(VALVE,Direction.EAST,solved?0:1));set(l,b,X_HM,fit(VALVE,Direction.EAST,0));set(l,b,V_LOOP,fit(VALVE,Direction.NORTH,solved?1:0));
        for(var r:DEEP_NET.reeds())set(l,b,r.at(),fit(REED,Direction.SOUTH,0));
        set(l,b,D_LATCH,fit(LATCH,Direction.SOUTH,solved?1:0));
        lantern(l,b,-3,3,-5);lantern(l,b,3,3,-16);lantern(l,b,-3,3,-27);
        set(l,b,-6,0,-3,Blocks.BARREL.defaultBlockState());set(l,b,6,0,-14,Blocks.BARREL.defaultBlockState());
        LabyrinthBuilder.entrance(l,b,brick,floor,brick);
        LabyrinthBuilder.doors(l,b,DEEP);
    }

    // ------------------------------------------------------------------ the room at work
    static SoundEvent note(char pitch){return (pitch=='L'?PuzzleRegistry.RESONANCE_LOW:pitch=='M'?PuzzleRegistry.RESONANCE_MIDDLE:PuzzleRegistry.RESONANCE_HIGH).get();}
    /** Every half second while someone is in the room: which pipes carry a note, what each reed hears, and the catch. */
    public static void update(ServerLevel l,BlockPos b,LabyrinthPlace place,int step){
        var n=net(place);var tones=tones(l,b,n);
        for(var e:n.edges()){
            boolean carrying=open(l,b,e)&&!tones.getOrDefault(e.a(),Set.of()).isEmpty();
            for(var c:e.cells()){var at=b.offset(c);var s=l.getBlockState(at);if(s.is(PuzzleRegistry.RESONANCE.get())&&s.getValue(ResonanceBlock.STAGE)!=(carrying?1:0))l.setBlock(at,s.setValue(ResonanceBlock.STAGE,carrying?1:0),FLAGS);}
        }
        boolean all=true;
        for(var r:n.reeds()){
            var heard=tones.getOrDefault(r.node(),Set.of());int state=reedState(heard,r.wants());if(state!=1)all=false;
            var at=b.offset(r.at());var s=l.getBlockState(at);if(ResonanceBlock.is(s,REED)&&s.getValue(ResonanceBlock.STAGE)!=state)l.setBlock(at,s.setValue(ResonanceBlock.STAGE,state),FLAGS);
            if(step%4==0){for(char pitch:heard)l.playSound(null,at,note(pitch),SoundSource.BLOCKS,.6F,1F);if(state==2)l.playSound(null,at,PuzzleRegistry.RESONANCE_BUZZ.get(),SoundSource.BLOCKS,.5F,1F);}
            if(state==1)l.sendParticles(ParticleTypes.WHITE_ASH,at.getX()+.5,at.getY()+.7,at.getZ()+.5,3,.2,.15,.05,0);
        }
        if(step%4==0)for(var src:n.sources().entrySet())for(var e:n.edges())if(e.a().equals(src.getValue())){var c=e.control()!=null?e.control():e.cells().isEmpty()?null:e.cells().get(0);if(c!=null)l.playSound(null,b.offset(c.getX(),c.getY(),c.getZ()+1),note(src.getKey()),SoundSource.BLOCKS,.2F,1F);break;}
        var latch=b.offset(n.latch());var ls=l.getBlockState(latch);
        if(all&&!solved(l,place)){
            PuzzleRooms.solve(l,place,"The reeds sing together, and the catch on the door lifts.");
            l.playSound(null,latch,PuzzleRegistry.RESONANCE_LATCH.get(),SoundSource.BLOCKS,1F,1F);
        }
        if(solved(l,place)&&ResonanceBlock.is(ls,LATCH)&&ls.getValue(ResonanceBlock.STAGE)!=1)l.setBlock(latch,ls.setValue(ResonanceBlock.STAGE,1),FLAGS);
    }

    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();int t=server.getTickCount();if(t%10!=7)return;var origin=HouseSavedData.get(server).houseOrigin();var l=server.getLevel(HouseDimensions.INTERIOR);
        if(origin==null||l==null)return;
        for(var place:new LabyrinthPlace[]{EARLY,DEEP}){
            if(LabyrinthData.get(server).door(place.entryDoorId())==null)continue;
            for(var p:l.players())if(PuzzleRooms.at(p)==place){var b=LabyrinthPlaces.base(origin,place);if(b!=null&&l.isLoaded(b))update(l,b,place,t/10);break;}
        }
    }

    // ------------------------------------------------------------------ hands on
    static String pitchName(char p){return p=='L'?"low":p=='M'?"middle":"high";}
    /** What a hand on a pipe feels running along it. */
    static String running(Set<Character> notes){
        var names=new ArrayList<String>();for(char c:new char[]{'L','M','H'})if(notes.contains(c))names.add(pitchName(c));
        return switch(names.size()){
            case 1->"A "+names.get(0)+" note runs along it.";
            case 2->"A "+names.get(0)+" note and a "+names.get(1)+" note run along it together.";
            default->"All three notes run along it together.";
        };
    }
    public static boolean use(ServerPlayer p,BlockPos pos,BlockState s){
        if(p.isSpectator())return false;
        var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null)return false;
        var place=LabyrinthPlaces.placeAt(origin,pos);if(!is(place))return false;
        var l=p.serverLevel();var b=LabyrinthPlaces.base(origin,place);int stage=s.getValue(ResonanceBlock.STAGE);
        switch(s.getValue(ResonanceBlock.KIND)){
            case VALVE->{l.setBlock(pos,s.setValue(ResonanceBlock.STAGE,stage==1?0:1),FLAGS);l.playSound(null,pos,PuzzleRegistry.VALVE.get(),SoundSource.BLOCKS,.7F,stage==1?.85F:1.1F);
                PuzzleRooms.say(p,stage==1?"You turn the valve shut. Nothing passes it now.":"You turn the valve open.");}
            case SECTION->{l.setBlock(pos,s.setValue(ResonanceBlock.STAGE,stage==1?0:1),FLAGS);l.playSound(null,pos,PuzzleRegistry.VALVE.get(),SoundSource.BLOCKS,.6F,1.3F);
                PuzzleRooms.say(p,stage==1?"You lift the short length of pipe out and hang it on its bracket.":"You fit the short length of pipe back into the run.");}
            case DRUM->PuzzleRooms.say(p,"An iron drum on a bracket, humming a "+pitchName(stage==0?'L':stage==1?'M':'H')+" note, steady as breathing.");
            case PIPE,RISER->{
                var notes=b==null?null:notesAt(l,b,place,pos.subtract(b));
                PuzzleRooms.say(p,notes==null||notes.isEmpty()?"The pipe is cold and still.":"The pipe trembles under your hand. "+running(notes));
            }
            case REED->{
                Reed reed=null;for(var r:net(place).reeds())if(b.offset(r.at()).equals(pos))reed=r;
                String which=reed==null||place==EARLY?"The reeds":"The "+pitchName(reed.wants())+" reeds";
                PuzzleRooms.say(p,switch(stage){case 1->which+" sing one clear note. The dust lifts off them.";case 2->which+" buzz and rattle against each other.";default->which+" are still behind their grille.";});
            }
            case LATCH->PuzzleRooms.say(p,stage==1?"The catch stands up off the door.":"A catch across the door, held down. A wire runs from it to the reeds.");
        }
        return true;
    }
}
