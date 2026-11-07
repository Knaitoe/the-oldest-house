package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseInternalTeleport;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import static io.github.knaitoe.theoldesthouse.labyrinth.SphinxBlock.Kind.*;

/**
 * 0.4.338, the Sphinx's third crossing (docs/SPHINX_0_4_338.md; the owner's draft, "Deep dark halls · What holds still"): "Which ledge can
 * hold still when I breathe?"
 * <p>
 * A low passage, a sill, and a near ledge of rock under a low overhang, at the edge of a cavern. Across the gap three stone shelves stand
 * out from the far wall, and beside each an iron survey pin is set into that wall, its head level with a settled shelf. Her voice comes
 * back first from below and then from high above. While anyone is on the ledge she breathes, every nine seconds or so, and sooner when
 * someone asks the question again at the grille: two of the shelves lift a little past their pins, grit running off their edges, and
 * settle again. The third never moves.
 * <p>
 * Each shelf has a short ladder hinged at the near edge, with a stop pin and a survey socket on the posts beside it. Lowering the ladder
 * to the fixed shelf lays it across the gap; crossing it, a passage behind that shelf leads round to the way onward and opens it for
 * everyone. A ladder toward a moving shelf tips and catches on its rope, she says to leave it, and its stop pin shows. Pulling the pin
 * drops the ladder onto that shelf, and the shelf slides sideways off what it was resting on, which was her; a paw settles against the far
 * wall; one eye lowers out of the dark until it is level with the ledge, her face turned on its side to see in under the overhang; and
 * she asks for the ladder back. Putting it back, or everyone stepping back over the sill, lets her withdraw. Whoever stays on the ledge in
 * her reach is swept back over the sill, unhurt. Whoever goes out along the dropped ladder is set down past her by her hand and the
 * ladder is gone under it. The way onward is always open along the corridor off the east end of the ledge.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class SphinxShelf {
    private SphinxShelf(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.SPHINX_SHELF;
    public static final String KEY="Shelf",RIDDLE="shelf";
    public static final String QUESTION="Which ledge can hold still when I breathe?";
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    // ------------------------------------------------------------------ the plan
    public static final int[] SHELVES={-7,0,7};
    /** The sill; the near ledge (z -6..-8) under its overhang; the gap; the far shelves (z -14..-16) against the far wall. */
    public static final int SILL_Z=-5,EDGE_Z=-8,GAP_Z0=-9,GAP_Z1=-13,SHELF_Z0=-14,SHELF_Z1=-16,WALL_Z=-17,TUNNEL_Z=-18,TOP=9,OVERHANG=3;
    public static final int BREATH=180,LIFT=40,REACH=6,PEEK=140,ONWARD_X=11,DOOR_Z=-23;
    public static final double SWEEP_Z=-3.5;
    public static final BlockPos GRILLE_AT=new BlockPos(-1,1,-2);
    public static BlockPos ladder(int i){return new BlockPos(SHELVES[i],0,EDGE_Z);}
    public static BlockPos pin(int i){return new BlockPos(SHELVES[i]-1,0,EDGE_Z+1);}
    public static BlockPos survey(int i){return new BlockPos(SHELVES[i]+1,0,EDGE_Z+1);}
    /** The iron survey pin in the far wall beside shelf i, its head level with the shelf's settled top. */
    public static BlockPos peg(int i){return new BlockPos(SHELVES[i]+2,0,SHELF_Z1);}
    public static BlockPos jib(){return new BlockPos(ONWARD_X-1,0,TUNNEL_Z);}
    /** Which way a forced shelf slides off her: toward the nearer gap. */
    static int slide(int i){return i==2?-1:1;}
    static final String[] WHERE={"at the left ladder","at the middle ladder","at the right ladder"};

    static BlockState mat(String name){return Sphinx.mat(name);}
    static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){l.setBlock(b.offset(at),s,FLAGS);}
    static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){l.setBlock(b.offset(x,y,z),s,FLAGS);}
    static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){Sphinx.box(l,b,x0,y0,z0,x1,y1,z1,s);}

    // ------------------------------------------------------------------ shared state
    static CompoundTag room(ServerLevel l){return SphinxCore.room(l,KEY);}
    static void room(ServerLevel l,CompoundTag r){SphinxCore.room(l,KEY,r);}
    public static int live(ServerLevel l){var r=room(l);if(!r.contains("Live")){r.putInt("Live",l.getRandom().nextInt(3));room(l,r);}return r.getInt("Live");}
    public static boolean solved(ServerLevel l){return room(l).getBoolean("Solved");}
    public static boolean lowered(ServerLevel l){return room(l).getBoolean("Lowered");}
    public static int provoked(ServerLevel l){var r=room(l);return r.contains("Provoked")?r.getInt("Provoked"):-1;}
    public static boolean sealed(ServerLevel l,int i){return (room(l).getInt("Sealed")&(1<<i))!=0;}
    public static boolean warned(ServerLevel l,int i){return (room(l).getInt("Warned")&(1<<i))!=0;}
    public static CompoundTag record(ServerLevel l,UUID reader){return SphinxCore.record(l,reader,RIDDLE);}

    // ------------------------------------------------------------------ building
    public static void build(ServerLevel l,BlockPos b){
        var rock=mat("rock");var dark=mat("dark");var air=Blocks.AIR.defaultBlockState();
        int live=live(l);var r=room(l);boolean lowered=r.getBoolean("Lowered"),solved=r.getBoolean("Solved");
        box(l,b,-12,-14,-24,12,TOP,0,rock);box(l,b,-12,OVERHANG+2,-24,12,TOP,-9,dark);
        // The low passage and its sill.
        box(l,b,-1,0,-1,1,1,-4,air);box(l,b,-1,0,SILL_Z,1,2,SILL_Z,air);
        for(int x=-1;x<=1;x++)set(l,b,x,0,SILL_Z,Blocks.COBBLED_DEEPSLATE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.BOTTOM));
        set(l,b,GRILLE_AT,SphinxBlock.of(GRILLE,Direction.EAST));
        // The near ledge under its overhang.
        box(l,b,-9,0,SILL_Z-1,9,OVERHANG-1,EDGE_Z,air);
        set(l,b,0,OVERHANG-1,-6,Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        // The cavern: the gap, down into the dark and up into it; the far shelves against the far wall, and the void between them.
        box(l,b,-9,-13,GAP_Z0,9,TOP-1,GAP_Z1,air);box(l,b,-9,-13,SHELF_Z0,9,TOP-1,SHELF_Z1,air);
        for(int i=0;i<3;i++){
            int c=SHELVES[i];
            shelf(l,b,i,0,0);
            set(l,b,peg(i),SphinxBlock.of(PEG,Direction.SOUTH));
            set(l,b,peg(i).above(2),Blocks.SOUL_WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING,Direction.SOUTH));
            // The posts at the near edge and their fittings; the ladder.
            set(l,b,c-1,0,EDGE_Z,rock);set(l,b,c+1,0,EDGE_Z,rock);
            set(l,b,pin(i),SphinxBlock.of(PIN,Direction.SOUTH));
            set(l,b,survey(i),SphinxBlock.of(SURVEY,Direction.SOUTH,SphinxCore.pinCount(l,KEY,i)));
            if(sealed(l,i))set(l,b,ladder(i),mat("rock_seam"));
            else{boolean down=i==live&&lowered;set(l,b,ladder(i),SphinxBlock.of(LADDER,Direction.NORTH,down?1:0));if(down)span(l,b,i,true);}
        }
        // Behind the fixed shelf, a passage round to the way onward; its far end is rock until someone has come through it.
        int c=SHELVES[live];box(l,b,c,0,WALL_Z,c,1,TUNNEL_Z,air);box(l,b,Math.min(c,ONWARD_X-2),0,TUNNEL_Z,ONWARD_X-2,1,TUNNEL_Z,air);
        set(l,b,jib(),solved?air:rock);set(l,b,jib().above(),solved?air:rock);
        // The way onward: off the east end of the ledge and up the corridor.
        box(l,b,10,0,-7,10,1,-7,air);box(l,b,ONWARD_X,0,-7,ONWARD_X,1,DOOR_Z,air);
        set(l,b,ONWARD_X,1,-15,Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        r.remove("Provoked");r.remove("Emerged");r.remove("Pulled");r.remove("LiftUntil");r.putInt("Warned",0);room(l,r);
        LabyrinthBuilder.entrance(l,b,rock,rock,rock);
        LabyrinthBuilder.doors(l,b,PLACE);
    }
    /** Shelf i: three by three of stone at the ledge's height, shifted {@code dx} sideways, settled (0) or lifted (1). */
    static void shelf(ServerLevel l,BlockPos b,int i,int dx,int stage){
        int c=SHELVES[i];
        for(int x=c-2;x<=c+2;x++)for(int z=SHELF_Z0;z>=SHELF_Z1;z--){var at=new BlockPos(x,-1,z);var st=l.getBlockState(b.offset(at));
            boolean on=x>=c-1+dx&&x<=c+1+dx;
            if(on)set(l,b,at,SphinxBlock.of(SHELF,Direction.NORTH,stage));else if(SphinxBlock.is(st,SHELF))set(l,b,at,Blocks.AIR.defaultBlockState());}
    }
    public static int shelfStage(ServerLevel l,BlockPos b,int i){var s=l.getBlockState(b.offset(SHELVES[i],-1,SHELF_Z0-1));return SphinxBlock.is(s,SHELF)?s.getValue(SphinxBlock.STAGE):-1;}
    public static boolean shelfAt(ServerLevel l,BlockPos b,int i,int dx){return SphinxBlock.is(l.getBlockState(b.offset(SHELVES[i]+dx,-1,SHELF_Z0-1)),SHELF);}
    /** The ladder laid across the gap toward shelf i, or taken up. */
    static void span(ServerLevel l,BlockPos b,int i,boolean on){
        for(int z=GAP_Z0;z>=GAP_Z1;z--){var at=new BlockPos(SHELVES[i],-1,z);var st=l.getBlockState(b.offset(at));
            if(on&&st.isAir())set(l,b,at,SphinxBlock.of(SPAN,Direction.NORTH));else if(!on&&SphinxBlock.is(st,SPAN))set(l,b,at,Blocks.AIR.defaultBlockState());}
    }
    public static boolean spanned(ServerLevel l,BlockPos b,int i){return SphinxBlock.is(l.getBlockState(b.offset(SHELVES[i],-1,GAP_Z1)),SPAN);}
    public static int ladderStage(ServerLevel l,BlockPos b,int i){var s=l.getBlockState(b.offset(ladder(i)));return SphinxBlock.is(s,LADDER)?s.getValue(SphinxBlock.STAGE):-1;}
    public static boolean jibOpen(ServerLevel l,BlockPos b){return l.getBlockState(b.offset(jib())).isAir();}

    // ------------------------------------------------------------------ where readers are
    static @Nullable BlockPos base(net.minecraft.server.MinecraftServer server){var o=HouseSavedData.get(server).houseOrigin();return o==null?null:LabyrinthPlaces.base(o,PLACE);}
    static boolean here(ServerPlayer p){var o=HouseSavedData.get(p.server).houseOrigin();return o!=null&&p.level().dimension().equals(HouseDimensions.INTERIOR)&&LabyrinthPlaces.placeAt(o,p.blockPosition())==PLACE;}
    static double lx(ServerPlayer p,BlockPos b){return p.getX()-b.getX();}
    static double ly(ServerPlayer p,BlockPos b){return p.getY()-b.getY();}
    static double lz(ServerPlayer p,BlockPos b){return p.getZ()-b.getZ();}
    /** On the near ledge, past the sill. */
    public static boolean onLedge(ServerPlayer p,BlockPos b){double z=lz(p,b),y=ly(p,b),x=lx(p,b);return here(p)&&z<SILL_Z&&z>EDGE_Z-.2&&y>-.5&&y<OVERHANG&&x>-9.6&&x<9.6;}
    /** Out along a laid ladder toward shelf i. */
    static boolean onSpan(ServerPlayer p,BlockPos b,int i){return here(p)&&Math.abs(lx(p,b)-(SHELVES[i]+.5))<.9&&lz(p,b)<EDGE_Z&&lz(p,b)>GAP_Z1&&ly(p,b)>-.5&&ly(p,b)<2;}
    static List<ServerPlayer> readers(ServerLevel l){var out=new ArrayList<ServerPlayer>();for(var p:l.players())if(!p.isSpectator()&&here(p))out.add(p);return out;}

    // ------------------------------------------------------------------ her voice, from below and then from above
    private static final Map<UUID,Long> ECHO=new HashMap<>();
    static void ask(ServerLevel l,ServerPlayer p){
        l.playSound(null,p.getX(),p.getY()-7,p.getZ()-4,PuzzleRegistry.SPHINX_VOICE.get(),SoundSource.HOSTILE,1F,.95F);
        p.displayClientMessage(Component.literal("“"+QUESTION+"”").withStyle(net.minecraft.ChatFormatting.GRAY,net.minecraft.ChatFormatting.ITALIC),true);SphinxCore.speaks(p,QUESTION);
        ECHO.put(p.getUUID(),l.getGameTime()+30);SphinxCore.heard(l,p.getUUID(),RIDDLE);ASKED.add(p.getUUID());
    }
    static void say(ServerLevel l,String line,List<ServerPlayer> to){
        for(var p:to){l.playSound(null,p.getX(),p.getY()+6,p.getZ()-5,PuzzleRegistry.SPHINX_VOICE.get(),SoundSource.HOSTILE,.85F,.9F);
            p.displayClientMessage(Component.literal("“"+line+"”").withStyle(net.minecraft.ChatFormatting.GRAY,net.minecraft.ChatFormatting.ITALIC),true);SphinxCore.speaks(p,line);}
    }
    /** Her breath: two shelves lift past their pins, grit running off their edges, and settle again. */
    static void breathe(ServerLevel l,BlockPos b){
        var r=room(l);long now=l.getGameTime();int live=live(l),forced=provoked(l);
        r.putLong("LiftUntil",now+LIFT);r.putLong("NextBreath",now+BREATH);r.putInt("Breaths",r.getInt("Breaths")+1);room(l,r);
        l.playSound(null,b.offset(0,-8,-11),PuzzleRegistry.SPHINX_BREATH.get(),SoundSource.HOSTILE,1.3F,.85F);
        for(int i=0;i<3;i++)if(i!=live&&i!=forced){shelf(l,b,i,0,1);l.playSound(null,b.offset(SHELVES[i],0,SHELF_Z0-1),PuzzleRegistry.SPHINX_GRIND.get(),SoundSource.BLOCKS,.6F,1.2F);}
    }
    static void settle(ServerLevel l,BlockPos b){
        var r=room(l);int live=live(l),forced=provoked(l);r.remove("LiftUntil");room(l,r);
        for(int i=0;i<3;i++)if(i!=live&&i!=forced)shelf(l,b,i,0,0);
    }

    // ------------------------------------------------------------------ hands on
    private static final Set<UUID> ASKED=new HashSet<>();
    private static final Map<UUID,Long> LAST=new HashMap<>();
    public static void clearAll(){ASKED.clear();LAST.clear();STRIKES.clear();ECHO.clear();EMPTY[0]=0;}
    static int ladderOf(BlockPos rel){for(int i=0;i<3;i++)if(rel.equals(ladder(i))||rel.equals(pin(i))||rel.equals(survey(i)))return i;return -1;}
    public static boolean use(ServerPlayer p,BlockPos pos,BlockState s){
        if(p.isSpectator())return false;var l=p.serverLevel();var b=base(p.server);if(b==null||!here(p))return false;
        long now=l.getGameTime();if(now-LAST.getOrDefault(p.getUUID(),-100L)<4)return true;LAST.put(p.getUUID(),now);
        int i=ladderOf(pos.subtract(b));
        switch(s.getValue(SphinxBlock.KIND)){
            case LADDER->{if(i>=0)ladder(l,b,p,i,s.getValue(SphinxBlock.STAGE));}
            case PIN->{if(i>=0)pull(l,b,p,i);}
            case SURVEY->{if(i>=0)survey(l,b,p,i);}
            case GRILLE->{
                if(provoked(l)>=0){PuzzleRooms.say(p,"Only breathing comes back through the grille.");return true;}
                ask(l,p);var r=room(l);if(r.getLong("NextBreath")>now+20&&!r.contains("LiftUntil")){r.putLong("NextBreath",now+20);room(l,r);}
            }
            case PEG->PuzzleRooms.say(p,"An iron survey pin driven into the rock, a level line scored through its head.");
            case SHELF->PuzzleRooms.say(p,"A shelf of pale stone. Grit lies along its edge.");
            case SPAN->PuzzleRooms.say(p,"The ladder, laid across the gap.");
            case FINGERS->PuzzleRooms.say(p,"A paw against the rock, the claws hooked into a crack.");
            case REACH->PuzzleRooms.say(p,"A foreleg, up the rock into the dark.");
            case LIMB->PuzzleRooms.say(p,"Fur, under where the shelf was. It rises and falls.");
            default->{}
        }
        return true;
    }
    /** A ladder: lowered toward its shelf (the answer), or, dropped by her, put back. */
    static void ladder(ServerLevel l,BlockPos b,ServerPlayer p,int i,int stage){
        if(stage==2){putBack(l,b,p,i);return;}
        if(stage==1){PuzzleRooms.say(p,"The ladder lies across the gap to the shelf.");return;}
        if(provoked(l)>=0){PuzzleRooms.say(p,"Something is breathing under the shelves. The ladders do not move.");return;}
        if(!SphinxCore.begin(l,KEY)){PuzzleRooms.say(p,"Another ladder is still moving.");return;}
        int live=live(l);boolean right=i==live;
        var answered=SphinxCore.commit(l,KEY,RIDDLE,p,i,live);
        for(int k=0;k<3;k++)SphinxCore.socket(l,b.offset(survey(k)),Direction.SOUTH,0);
        for(var uuid:answered.keySet()){if(uuid.equals(p.getUUID()))continue;var other=l.getServer().getPlayerList().getPlayer(uuid);
            if(other!=null)PuzzleRooms.say(other,"A ladder is let down. Your pin "+WHERE[answered.get(uuid)]+" is your answer.");}
        if(right){
            var r=room(l);r.putBoolean("Lowered",true);room(l,r);
            set(l,b,ladder(i),SphinxBlock.of(LADDER,Direction.NORTH,1));span(l,b,i,true);
            l.playSound(null,b.offset(ladder(i)),PuzzleRegistry.SPHINX_LATCH.get(),SoundSource.BLOCKS,.9F,.7F);
            PuzzleRooms.say(p,"You lower the ladder. Its far end comes down on the shelf and lies still.");
        }else{
            l.playSound(null,b.offset(ladder(i)),PuzzleRegistry.SPHINX_CATCH.get(),SoundSource.BLOCKS,1F,.8F);
            var r=room(l);r.putInt("Warned",r.getInt("Warned")|(1<<i));
            set(l,b,pin(i),SphinxBlock.of(PIN,Direction.SOUTH,1));
            PuzzleRooms.say(p,"The ladder tips forward and catches on its rope.");
            say(l,"Not that one. That ledge does not hold still.",readers(l));
            r.putLong(peek(i),l.getGameTime()+PEEK);room(l,r);eye(l,b,i,3);
        }
    }
    /** The stop pin pulled: the ladder drops onto a moving shelf, and she is under it. */
    static void pull(ServerLevel l,BlockPos b,ServerPlayer p,int i){
        var s=l.getBlockState(b.offset(pin(i)));
        if(SphinxBlock.stage(s)==0){PuzzleRooms.say(p,"The ladder's stop. Something small is set into the post, flush.");return;}
        if(SphinxBlock.stage(s)==2||provoked(l)>=0){PuzzleRooms.say(p,"The pin is out.");return;}
        if(lowered(l)&&i==live(l)){PuzzleRooms.say(p,"That ladder is already down.");return;}
        set(l,b,pin(i),s.setValue(SphinxBlock.STAGE,2));
        if(room(l).contains("LiftUntil"))settle(l,b);
        var r=room(l);r.putInt("Provoked",i);r.putLong("Emerged",l.getGameTime()+60);r.putLong("Pulled",l.getGameTime());
        if(r.contains(peek(i))){r.remove(peek(i));}room(l,r);eye(l,b,i,Integer.MIN_VALUE);
        SphinxCore.forced(l,p.getUUID(),RIDDLE);
        set(l,b,ladder(i),SphinxBlock.of(LADDER,Direction.NORTH,2));span(l,b,i,true);
        l.playSound(null,b.offset(pin(i)),PuzzleRegistry.SPHINX_PIN.get(),SoundSource.BLOCKS,1F,1F);
        l.playSound(null,b.offset(ladder(i)),PuzzleRegistry.SPHINX_CATCH.get(),SoundSource.BLOCKS,1F,.6F);
        PuzzleRooms.say(p,"The pin comes free and the ladder drops across the gap onto the shelf.");
        EMPTY[0]=0;
    }
    /** The dropped ladder taken back up: what she asked for. She goes. */
    static void putBack(ServerLevel l,BlockPos b,ServerPlayer p,int i){
        if(provoked(l)!=i){set(l,b,ladder(i),SphinxBlock.of(LADDER,Direction.NORTH,0));span(l,b,i,false);return;}
        var rec=record(l,p.getUUID());rec.putBoolean("PutBack",true);SphinxCore.record(l,p.getUUID(),RIDDLE,rec);
        PuzzleRooms.say(p,"You haul the ladder back up and stand it at the edge.");
        withdraw(l,b);
    }
    static void survey(ServerLevel l,BlockPos b,ServerPlayer p,int i){
        if(sealed(l,i)){PuzzleRooms.say(p,"The socket is cracked through with the rock.");return;}
        int was=SphinxCore.pinOf(l,KEY,p.getUUID());int now=SphinxCore.pin(l,KEY,p.getUUID(),i);
        if(was>=0)SphinxCore.socket(l,b.offset(survey(was)),Direction.SOUTH,SphinxCore.pinCount(l,KEY,was));
        SphinxCore.socket(l,b.offset(survey(i)),Direction.SOUTH,SphinxCore.pinCount(l,KEY,i));
        SphinxCore.pinned(l,p,b.offset(survey(i)),now,WHERE[i]);
    }
    static void unpin(ServerLevel l,BlockPos b,UUID reader){
        int was=SphinxCore.pinOf(l,KEY,reader);if(was<0)return;SphinxCore.unpin(l,KEY,reader);
        SphinxCore.socket(l,b.offset(survey(was)),Direction.SOUTH,SphinxCore.pinCount(l,KEY,was));
    }

    // ------------------------------------------------------------------ her eye, her paw
    static String peek(int i){return "PeekUntil"+i;}
    /** Her eye upright in the gap before shelf i, its lower row at {@code y}, her face turned on its side; MIN_VALUE clears it. */
    static void eye(ServerLevel l,BlockPos b,int i,int y){
        int x0=SHELVES[i]-1;
        for(int yy=-12;yy<=TOP-1;yy++)for(int dx=0;dx<4;dx++){var at=new BlockPos(x0+dx,yy,GAP_Z0-1);if(SphinxEyeBlock.is(l.getBlockState(b.offset(at)))&&(yy<y||yy>y+1))set(l,b,at,Blocks.AIR.defaultBlockState());}
        if(y==Integer.MIN_VALUE)return;
        int look=lookAt(l,b,i,y);
        for(int dx=0;dx<4;dx++)for(int dy=0;dy<2;dy++){var at=new BlockPos(x0+dx,y+dy,GAP_Z0-1);var st=l.getBlockState(b.offset(at));
            if(st.isAir()||SphinxEyeBlock.is(st))set(l,b,at,SphinxEyeBlock.of((dy==1?0:4)+dx,look,SphinxEyeBlock.SOUTH));}
    }
    public static int eyeY(ServerLevel l,BlockPos b,int i){for(int y=-12;y<=TOP-1;y++)if(SphinxEyeBlock.is(l.getBlockState(b.offset(SHELVES[i]-1,y,GAP_Z0-1))))return y;return Integer.MIN_VALUE;}
    /** Upright, her pupil turns left/right toward the nearest reader and up/down toward their height. */
    public static int lookAt(ServerLevel l,BlockPos b,int i,int y){
        double cx=b.getX()+SHELVES[i]+1,cy=b.getY()+y+1;ServerPlayer near=null;double best=Double.MAX_VALUE;
        for(var p:readers(l)){double d=Math.pow(p.getX()-cx,2)+Math.pow(p.getZ()-(b.getZ()+GAP_Z0),2);if(d<best){best=d;near=p;}}
        if(near==null)return 4;
        double dx=near.getX()-cx,dy=near.getEyeY()-cy;return (dx<-1.2?0:dx>1.2?2:1)+3*(dy<-1.2?0:dy>1.2?2:1);
    }
    static void follow(ServerLevel l,BlockPos b,int i){
        int y=eyeY(l,b,i);if(y==Integer.MIN_VALUE)return;int look=lookAt(l,b,i,y);
        for(int dx=0;dx<4;dx++)for(int dy=0;dy<2;dy++){var at=b.offset(SHELVES[i]-1+dx,y+dy,GAP_Z0-1);var st=l.getBlockState(at);if(SphinxEyeBlock.is(st)&&st.getValue(SphinxEyeBlock.LOOK)!=look)l.setBlock(at,st.setValue(SphinxEyeBlock.LOOK,look),FLAGS);}
    }
    /** The gap beside shelf i, where her paw settles against the far wall. */
    static int gapX(int i){return i==2?3:i==1?3:-4;}
    static void paw(ServerLevel l,BlockPos b,int i,boolean on){
        int gx=gapX(i);
        for(int dx=-1;dx<=1;dx++){var at=new BlockPos(gx+dx,1,SHELF_Z1);var st=l.getBlockState(b.offset(at));
            if(on&&st.isAir())set(l,b,at,SphinxBlock.of(FINGERS,Direction.SOUTH));else if(!on&&SphinxBlock.is(st,FINGERS))set(l,b,at,Blocks.AIR.defaultBlockState());}
        for(int y=2;y<=TOP-2;y++){var at=new BlockPos(gx,y,SHELF_Z1);var st=l.getBlockState(b.offset(at));
            if(on&&st.isAir())set(l,b,at,SphinxBlock.of(SphinxBlock.Kind.REACH,Direction.SOUTH));else if(!on&&SphinxBlock.is(st,SphinxBlock.Kind.REACH))set(l,b,at,Blocks.AIR.defaultBlockState());}
    }
    /** Under where a slid shelf stood, her back: fur that rises and falls. */
    static void back(ServerLevel l,BlockPos b,int i,boolean on){
        int c=SHELVES[i],x=slide(i)>0?c-1:c+1;
        for(int z=SHELF_Z0;z>=SHELF_Z1;z--){var at=new BlockPos(x,-1,z);var st=l.getBlockState(b.offset(at));
            if(on&&st.isAir())set(l,b,at,SphinxBlock.of(LIMB,Direction.SOUTH));else if(!on&&SphinxBlock.is(st,LIMB))set(l,b,at,Blocks.AIR.defaultBlockState());}
    }

    // ------------------------------------------------------------------ the clock
    private static final Map<UUID,Long> STRIKES=new HashMap<>();
    private static final long[] EMPTY={0};
    static void update(ServerLevel l,BlockPos b){
        var readers=readers(l);var r=room(l);int live=live(l);long now=l.getGameTime();
        if(now%20<2)for(var uuid:SphinxCore.pins(l,KEY).keySet()){var q=l.getServer().getPlayerList().getPlayer(uuid);if(q==null||!here(q))unpin(l,b,uuid);}
        for(var it=ECHO.entrySet().iterator();it.hasNext();){var e=it.next();if(e.getValue()>now)continue;it.remove();var p=l.getServer().getPlayerList().getPlayer(e.getKey());
            if(p!=null&&here(p))l.playSound(null,p.getX(),p.getY()+8,p.getZ()-5,PuzzleRegistry.SPHINX_VOICE.get(),SoundSource.HOSTILE,.7F,.85F);}
        // Her breath, while anyone is on the ledge or out over the gap.
        boolean present=false;for(var p:readers)if(onLedge(p,b)||lz(p,b)<EDGE_Z)present=true;
        if(r.contains("LiftUntil")){
            if(r.getLong("LiftUntil")<=now)settle(l,b);
            else if(now%4<2)for(int i=0;i<3;i++)if(i!=live&&i!=provoked(l)){var e=b.offset(SHELVES[i],0,SHELF_Z0);
                l.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST,Blocks.GRAVEL.defaultBlockState()),e.getX()+.5,e.getY()+.2,e.getZ()+.9,4,1.3,.05,.1,0);}
        }else if(present&&provoked(l)<0){if(!r.contains("NextBreath")){r.putLong("NextBreath",now+60);room(l,r);}else if(r.getLong("NextBreath")<=now)breathe(l,b);}
        for(int i=0;i<3;i++){
            r=room(l);if(!r.contains(peek(i)))continue;long until=r.getLong(peek(i));
            if(until<=now){eye(l,b,i,Integer.MIN_VALUE);r.remove(peek(i));room(l,r);continue;}
            if(now%4<2)follow(l,b,i);
        }
        r=room(l);
        for(var p:readers){
            if(onLedge(p,b)&&!ASKED.contains(p.getUUID())&&provoked(l)<0)ask(l,p);
            // A fall into the cavern is caught and given back in the passage: testing is safe.
            if(ly(p,b)<-3){p.fallDistance=0;HouseInternalTeleport.translate(p,b.getX()+.5-p.getX(),b.getY()-p.getY(),b.getZ()+SWEEP_Z-p.getZ());PuzzleRooms.say(p,"The dark gives you back in the passage.");continue;}
            // Across the fixed shelf and into the passage behind it: round to the way onward, open for everyone from now on.
            if(lz(p,b)<WALL_Z+.2&&lz(p,b)>TUNNEL_Z-1&&Math.abs(lx(p,b)-(SHELVES[live]+.5))<.8&&!r.getBoolean("Solved")){
                r.putBoolean("Solved",true);room(l,r);set(l,b,jib(),Blocks.AIR.defaultBlockState());set(l,b,jib().above(),Blocks.AIR.defaultBlockState());
                l.playSound(null,b.offset(jib()),PuzzleRegistry.SPHINX_GRIND.get(),SoundSource.BLOCKS,.6F,.8F);
                PuzzleRooms.say(p,"Behind the shelf the rock opens on a passage, and somewhere ahead stone grinds aside.");
            }
            int forced=provoked(l);
            if(forced>=0&&r.getLong("Emerged")<=now&&onSpan(p,b,forced)&&lz(p,b)<EDGE_Z-.6){
                // Out along the ladder she asked for: her hand comes down over the crossing and sets them past her; the ladder is gone under it.
                HouseInternalTeleport.translate(p,b.getX()+ONWARD_X+.5-p.getX(),b.getY()-p.getY(),b.getZ()-15.5-p.getZ());p.fallDistance=0;
                r=room(l);r.putInt("Sealed",r.getInt("Sealed")|(1<<forced));room(l,r);
                span(l,b,forced,false);set(l,b,ladder(forced),mat("rock_seam"));
                l.playSound(null,b.offset(SHELVES[forced],2,GAP_Z0-2),PuzzleRegistry.SPHINX_STRIKE.get(),SoundSource.HOSTILE,1F,.7F);
                withdraw(l,b);r=room(l);
                PuzzleRooms.say(p,"Her hand comes down over the crossing, and you are set down somewhere past her. The ladder is gone under it.");
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
        long pulled=r.getLong("Pulled"),emerged=r.getLong("Emerged"),e=now-pulled;
        if(e>=10&&!shelfAt(l,b,forced,slide(forced))){shelf(l,b,forced,slide(forced),0);back(l,b,forced,true);
            l.playSound(null,b.offset(SHELVES[forced],0,SHELF_Z0-1),PuzzleRegistry.SPHINX_GRIND.get(),SoundSource.BLOCKS,1F,.7F);}
        if(e>=20&&!SphinxBlock.is(l.getBlockState(b.offset(gapX(forced),1,SHELF_Z1)),FINGERS)){paw(l,b,forced,true);
            l.playSound(null,b.offset(gapX(forced),1,SHELF_Z1),PuzzleRegistry.SPHINX_STRIKE.get(),SoundSource.HOSTILE,.8F,.5F);}
        if(e>=30)eye(l,b,forced,(int)Math.max(1,7-(e-30)/5));
        if(now%4<2)follow(l,b,forced);
        if(emerged>now)return;
        if(!r.getBoolean("Asked")){r.putBoolean("Asked",true);room(l,r);say(l,"Put the ladder back.",readers);}
        boolean anyone=false;
        for(var p:readers){
            if(!onLedge(p,b))continue;anyone=true;
            var rec=record(l,p.getUUID());if(!rec.getBoolean("Retreated")){rec.putBoolean("Retreated",true);SphinxCore.record(l,p.getUUID(),RIDDLE,rec);}
            if(Math.abs(lx(p,b)-(SHELVES[forced]+.5))>REACH)continue;
            long t=STRIKES.getOrDefault(p.getUUID(),0L);
            if(t==0){STRIKES.put(p.getUUID(),now+60);l.playSound(null,p.blockPosition(),PuzzleRegistry.SPHINX_BREATH.get(),SoundSource.HOSTILE,1F,1.3F);
                l.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST,Blocks.GRAVEL.defaultBlockState()),p.getX(),p.getY()+2.4,p.getZ(),12,.4,.1,.4,0);}
            else if(t<=now){STRIKES.remove(p.getUUID());
                // She never hurts anyone: her other paw reaches in under the overhang and sweeps them back over the sill.
                l.playSound(null,p.blockPosition(),PuzzleRegistry.SPHINX_STRIKE.get(),SoundSource.HOSTILE,1F,1F);
                HouseInternalTeleport.translate(p,b.getX()+.5-p.getX(),0,b.getZ()+SWEEP_Z-p.getZ());
                var rec2=record(l,p.getUUID());rec2.putInt("Swept",rec2.getInt("Swept")+1);SphinxCore.record(l,p.getUUID(),RIDDLE,rec2);
                PuzzleRooms.say(p,"A paw comes in under the overhang and sweeps you back over the sill.");}
        }
        for(var p:readers)if(!onLedge(p,b))STRIKES.remove(p.getUUID());
        if(anyone)EMPTY[0]=0;else if(++EMPTY[0]>=50)withdraw(l,b);
    }
    /** She goes back under: the shelf slides home, the paw lets go of the wall, the eye rises away, the dropped ladder comes up. */
    static void withdraw(ServerLevel l,BlockPos b){
        var r=room(l);int i=r.contains("Provoked")?r.getInt("Provoked"):-1;if(i<0)return;
        back(l,b,i,false);shelf(l,b,i,0,0);paw(l,b,i,false);eye(l,b,i,Integer.MIN_VALUE);
        if(!sealed(l,i)){set(l,b,ladder(i),SphinxBlock.of(LADDER,Direction.NORTH,0));span(l,b,i,false);}
        set(l,b,pin(i),SphinxBlock.of(PIN,Direction.SOUTH));
        r=room(l);r.remove("Provoked");r.remove("Emerged");r.remove("Pulled");r.remove("Asked");r.putInt("Warned",r.getInt("Warned")&~(1<<i));r.putLong("NextBreath",l.getGameTime()+BREATH);room(l,r);
        STRIKES.clear();EMPTY[0]=0;
        l.playSound(null,b.offset(SHELVES[i],-6,GAP_Z0-2),PuzzleRegistry.SPHINX_BREATH.get(),SoundSource.HOSTILE,.8F,.7F);
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();if(server.getTickCount()%2!=0)return;
        var l=server.getLevel(HouseDimensions.INTERIOR);var b=base(server);if(l==null||b==null||!l.isLoaded(b))return;
        if(LabyrinthData.get(server).door(PLACE.entryDoorId())==null)return;
        var r=room(l);boolean any=!ASKED.isEmpty()||provoked(l)>=0||r.contains("LiftUntil")||r.contains("Pins");
        for(int i=0;i<3&&!any;i++)if(r.contains(peek(i)))any=true;
        if(!any)for(var p:l.players())if(here(p)){any=true;break;}
        if(any)update(l,b);
    }
}
