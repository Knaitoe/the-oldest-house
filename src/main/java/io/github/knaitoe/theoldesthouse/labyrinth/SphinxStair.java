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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import static io.github.knaitoe.theoldesthouse.labyrinth.SphinxBlock.Kind.*;

/**
 * 0.4.335, the Sphinx's second crossing (docs/SPHINX_0_4_335.md; the owner's draft, "Middle stone halls · The stair above you"): "Which
 * stair takes you down to the landing above us?"
 * <p>
 * An approach behind an iron rail, then a stone gallery at the head of a narrow shaft. Three flights go down from it into the dark, with
 * long gaps between them, and over their heads hangs an iron-grated landing, its whole floor in sight from the gallery. A tray of loose
 * stone chips stands at the rail. A chip let go at a stair's mouth rattles down it: two settle somewhere far below; the third comes back
 * out onto the landing overhead, still rolling, and stops against its stone lip. The test can be repeated from the rail as often as anyone
 * likes.
 * <p>
 * Each mouth has a gate with a release catch on its post, a stop pin and a survey socket. Releasing the verified gate swings it back, and
 * whoever goes down that flight comes up onto the landing they were watching; from the landing a short stair goes down to the way onward,
 * open from then on for everyone. A refused gate catches and she says to leave it shut; its pin shows, and pulling it brings dust down out
 * of the dark, an eye passing up through the gap between the flights, and a hand coming up the chosen stair. Anyone still on the gallery in
 * her reach is swept back over the approach rail, which is as far as she reaches; nobody is hurt. Going down under her carries a reader to
 * the way onward and closes that mouth behind them in cracked masonry. The way onward is always open along the corridor past the gallery.
 * <p>
 * The room is shared; each reader's answer and conduct are their own (SphinxCore), and the survey pins work here as in the vestibule.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class SphinxStair {
    private SphinxStair(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.SPHINX_STAIR;
    public static final String KEY="Stair",RIDDLE="stair";
    public static final String QUESTION="Which stair takes you down to the landing above us?";
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    // ------------------------------------------------------------------ the plan
    /** The three flights' middle columns; each flight is three wide. */
    public static final int[] MOUTHS={-7,0,7};
    /** The gate row; the gallery in front of it (z -4..-6); the approach rail behind the gallery; the approach strip behind that. */
    public static final int GATE_Z=-7,RAIL_Z=-3,TOP=9,STEPS=10,LIVE_TURN=5;
    /** The landing: an iron grating over the heads of the flights, its lip along the gallery side. */
    public static final int LANDING_Y=4,LANDING_Z0=-8,LANDING_Z1=-12;
    /** Where she stops: back over the approach rail. How far either side of a mouth she reaches along the gallery. */
    public static final double SWEEP_Z=-1.6;public static final int REACH=6,PEEK=140;
    public static final BlockPos TRAY=new BlockPos(3,0,-6),GRILLE_AT=new BlockPos(-9,1,-2),JIB=new BlockPos(10,5,-10),ARRIVE=new BlockPos(0,5,-10);
    public static final int ONWARD_X=11,DOOR_Z=-23;
    public static BlockPos gate(int i,int dx){return new BlockPos(MOUTHS[i]+dx,0,GATE_Z);}
    public static BlockPos release(int i){return new BlockPos(MOUTHS[i]+2,1,GATE_Z+1);}
    public static BlockPos pin(int i){return new BlockPos(MOUTHS[i]-2,1,GATE_Z+1);}
    public static BlockPos survey(int i){return new BlockPos(MOUTHS[i]+2,0,GATE_Z+1);}
    /** The cell above tread k of flight i (tread 0 at the mouth). */
    public static BlockPos tread(int i,int k){return new BlockPos(MOUTHS[i],-k,GATE_Z-1-k);}
    /** Where a chip comes to rest: at the foot of a false flight; on the landing against its lip, above the live one. */
    public static BlockPos foot(int i){return tread(i,STEPS-1);}
    public static BlockPos onLanding(int i){return new BlockPos(MOUTHS[i],LANDING_Y+1,LANDING_Z0);}
    /** Her eye's gap and depth for each mouth: the west gap for the west and middle flights (the middle one further in), the east for the east. */
    public static int eyeX(int i){return i==2?2:-5;}
    public static int eyeZ(int i){return i==1?-14:-10;}
    static final String[] WHERE={"at the left stair","at the middle stair","at the right stair"};

    static BlockState mat(String name){return Sphinx.mat(name);}
    static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){l.setBlock(b.offset(at),s,FLAGS);}
    static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){l.setBlock(b.offset(x,y,z),s,FLAGS);}
    static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){Sphinx.box(l,b,x0,y0,z0,x1,y1,z1,s);}

    // ------------------------------------------------------------------ shared state
    static CompoundTag room(ServerLevel l){return SphinxCore.room(l,KEY);}
    static void room(ServerLevel l,CompoundTag r){SphinxCore.room(l,KEY,r);}
    public static int live(ServerLevel l){var r=room(l);if(!r.contains("Live")){r.putInt("Live",l.getRandom().nextInt(3));room(l,r);}return r.getInt("Live");}
    public static boolean solved(ServerLevel l){return room(l).getBoolean("Solved");}
    public static boolean released(ServerLevel l){return room(l).getBoolean("Released");}
    public static int provoked(ServerLevel l){var r=room(l);return r.contains("Provoked")?r.getInt("Provoked"):-1;}
    public static boolean sealed(ServerLevel l,int i){return (room(l).getInt("Sealed")&(1<<i))!=0;}
    public static boolean warned(ServerLevel l,int i){return (room(l).getInt("Warned")&(1<<i))!=0;}
    public static CompoundTag record(ServerLevel l,UUID reader){return SphinxCore.record(l,reader,RIDDLE);}

    // ------------------------------------------------------------------ building
    public static void build(ServerLevel l,BlockPos b){
        var stone=mat("ashlar");var flags=mat("flags");var dark=mat("dark");var air=Blocks.AIR.defaultBlockState();
        int live=live(l);var r=room(l);boolean released=r.getBoolean("Released"),solved=r.getBoolean("Solved");
        box(l,b,-12,-14,-24,13,TOP,0,stone);
        // Above head height the shaft's walls go dark and keep going.
        box(l,b,-12,5,-24,13,TOP,0,dark);box(l,b,-12,-14,-24,13,-3,-8,dark);
        // The approach and its rail; the gallery at the head of the shaft.
        box(l,b,-9,-1,-1,9,-1,-6,flags);box(l,b,-9,0,-1,9,3,-2,air);box(l,b,-9,4,-1,9,4,-2,stone);
        box(l,b,-9,0,-3,9,TOP-1,-6,air);
        for(int x=-9;x<=9;x++)if(Math.abs(x)>1)set(l,b,x,0,RAIL_Z,Blocks.IRON_BARS.defaultBlockState());
        set(l,b,GRILLE_AT,SphinxBlock.of(GRILLE,Direction.EAST));
        set(l,b,0,3,-2,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        for(int x:new int[]{-4,4}){set(l,b,x,2,-5,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));for(int y=3;y<TOP;y++)set(l,b,x,y,-5,Blocks.CHAIN.defaultBlockState());}
        // The shaft: open from the gate row north, down into the dark and up past the landing.
        box(l,b,-8,-13,-8,8,TOP-1,-20,air);box(l,b,-9,LANDING_Y,-8,9,TOP-1,-13,air);
        // The front: open over the gates up into the dark; posts, the gates across the three mouths, and rails across the gaps between them.
        box(l,b,-9,0,GATE_Z,9,TOP-1,GATE_Z,air);
        for(int x:new int[]{-9,-5,-2,2,5,9}){set(l,b,x,0,GATE_Z,stone);set(l,b,x,1,GATE_Z,stone);}
        for(int x:new int[]{-4,-3,3,4})set(l,b,x,0,GATE_Z,SphinxBlock.of(RAIL,Direction.NORTH,0));
        for(int i=0;i<3;i++){
            int c=MOUTHS[i];
            // A flight: ten treads down into the dark, a block of stone under each, and on into a turn nobody can see round.
            for(int k=0;k<STEPS+2;k++)for(int dx=-1;dx<=1;dx++){
                set(l,b,c+dx,-1-k,GATE_Z-1-k,Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH));
                set(l,b,c+dx,-2-k,GATE_Z-1-k,Blocks.STONE_BRICKS.defaultBlockState());
            }
            if(sealed(l,i))for(int dx=-1;dx<=1;dx++)for(int y=0;y<=2;y++)set(l,b,c+dx,y,GATE_Z,mat("ashlar_seam"));
            else gateShut(l,b,i,i==live&&released);
            set(l,b,release(i),SphinxBlock.of(LATCH,Direction.SOUTH,i==live&&released?1:0));
            set(l,b,pin(i),SphinxBlock.of(PIN,Direction.SOUTH));
            set(l,b,survey(i),SphinxBlock.of(SURVEY,Direction.SOUTH,SphinxCore.pinCount(l,KEY,i)));
        }
        set(l,b,TRAY,SphinxBlock.of(SphinxBlock.Kind.TRAY,Direction.NORTH));
        // The landing: an iron grating over the flights' heads, a stone lip along its south edge, a rail along the drop to the north.
        for(int x=-9;x<=9;x++)for(int z=LANDING_Z0;z>=LANDING_Z1;z--)set(l,b,x,LANDING_Y,z,SphinxBlock.of(GRATE,Direction.NORTH,z==LANDING_Z0?1:0));
        for(int x=-9;x<=9;x++)set(l,b,x,LANDING_Y+1,LANDING_Z1-1,Blocks.IRON_BARS.defaultBlockState());
        set(l,b,0,LANDING_Y+3,-10,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));for(int y=LANDING_Y+4;y<TOP;y++)set(l,b,0,y,-10,Blocks.CHAIN.defaultBlockState());
        // From the landing a short stair goes down to the way onward; its opening is plain stone until the crossing is made.
        box(l,b,11,LANDING_Y+1,-10,12,LANDING_Y+2,-10,air);
        for(int k=0;k<5;k++){set(l,b,12,LANDING_Y-k,-11-k,Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH));box(l,b,12,LANDING_Y-k+1,-11-k,12,LANDING_Y-k+3,-11-k,air);}
        box(l,b,12,0,-16,12,2,-16,air);
        set(l,b,JIB,solved?air:stone);set(l,b,JIB.above(),solved?air:stone);
        // The way onward: out of the gallery's east end and up the corridor to the far door.
        box(l,b,10,0,-5,10,2,-5,air);box(l,b,ONWARD_X,-1,-4,ONWARD_X,-1,DOOR_Z,flags);box(l,b,ONWARD_X,0,-4,ONWARD_X,2,DOOR_Z,air);
        set(l,b,ONWARD_X,2,-14,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        r.remove("Provoked");r.remove("Emerged");r.putInt("Warned",0);room(l,r);
        LabyrinthBuilder.entrance(l,b,stone,flags,stone);
        LabyrinthBuilder.doors(l,b,PLACE);
    }
    /** A mouth's gate shut across its three cells, or released: swung back along its west post, the rest of the mouth open. */
    static void gateShut(ServerLevel l,BlockPos b,int i,boolean released){
        for(int dx=-1;dx<=1;dx++)set(l,b,gate(i,dx),released?(dx==-1?SphinxBlock.of(RAIL,Direction.NORTH,1):Blocks.AIR.defaultBlockState()):SphinxBlock.of(RAIL,Direction.NORTH,0));
    }
    public static int gateStage(ServerLevel l,BlockPos b,int i){var s=l.getBlockState(b.offset(gate(i,0)));return SphinxBlock.is(s,RAIL)?s.getValue(SphinxBlock.STAGE):s.isAir()?1:-1;}
    public static boolean jibOpen(ServerLevel l,BlockPos b){return l.getBlockState(b.offset(JIB)).isAir();}

    // ------------------------------------------------------------------ where readers are
    static @Nullable BlockPos base(net.minecraft.server.MinecraftServer server){var o=HouseSavedData.get(server).houseOrigin();return o==null?null:LabyrinthPlaces.base(o,PLACE);}
    static boolean here(ServerPlayer p){var o=HouseSavedData.get(p.server).houseOrigin();return o!=null&&p.level().dimension().equals(HouseDimensions.INTERIOR)&&LabyrinthPlaces.placeAt(o,p.blockPosition())==PLACE;}
    static double lx(ServerPlayer p,BlockPos b){return p.getX()-b.getX();}
    static double ly(ServerPlayer p,BlockPos b){return p.getY()-b.getY();}
    static double lz(ServerPlayer p,BlockPos b){return p.getZ()-b.getZ();}
    public static boolean inGallery(ServerPlayer p,BlockPos b){double z=lz(p,b),y=ly(p,b),x=lx(p,b);return here(p)&&z<RAIL_Z&&z>GATE_Z&&y>-.5&&y<3&&x>-9.6&&x<9.6;}
    /** On flight i, past its gate. */
    static boolean onFlight(ServerPlayer p,BlockPos b,int i){double x=lx(p,b)-(MOUTHS[i]+.5);return here(p)&&Math.abs(x)<1.6&&lz(p,b)<GATE_Z&&ly(p,b)<.5&&lz(p,b)>GATE_Z-STEPS-3;}
    static List<ServerPlayer> readers(ServerLevel l){var out=new ArrayList<ServerPlayer>();for(var p:l.players())if(!p.isSpectator()&&here(p))out.add(p);return out;}

    // ------------------------------------------------------------------ her voice, from under the step
    static void ask(ServerLevel l,ServerPlayer p){
        l.playSound(null,p.getX(),p.getY()-2.5,p.getZ(),PuzzleRegistry.SPHINX_VOICE.get(),SoundSource.HOSTILE,1F,.92F);
        p.displayClientMessage(Component.literal("“"+QUESTION+"”").withStyle(net.minecraft.ChatFormatting.GRAY,net.minecraft.ChatFormatting.ITALIC),true);
        SphinxCore.heard(l,p.getUUID(),RIDDLE);ASKED.add(p.getUUID());
    }
    static void say(ServerLevel l,BlockPos b,String line,List<ServerPlayer> to){
        for(var p:to){l.playSound(null,p.getX(),p.getY()-2.5,p.getZ(),PuzzleRegistry.SPHINX_VOICE.get(),SoundSource.HOSTILE,.8F,.92F);
            p.displayClientMessage(Component.literal("“"+line+"”").withStyle(net.minecraft.ChatFormatting.GRAY,net.minecraft.ChatFormatting.ITALIC),true);}
    }

    // ------------------------------------------------------------------ chips
    /** A stone chip from the tray: plain flint by its look, kept only in this room. */
    public static ItemStack chip(){
        var s=new ItemStack(Items.FLINT);s.set(DataComponents.CUSTOM_NAME,Component.literal("Stone Chip").withStyle(net.minecraft.ChatFormatting.RESET));
        var tag=new CompoundTag();tag.putBoolean("SphinxChip",true);s.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));return s;
    }
    public static boolean isChip(ItemStack s){var d=s.get(DataComponents.CUSTOM_DATA);return s.is(Items.FLINT)&&d!=null&&d.copyTag().getBoolean("SphinxChip");}
    /** A chip on its way down a stair: which mouth, how far it has come, and where it is now. */
    static final class Roll{final int mouth;int step;long hold;@Nullable BlockPos at;Roll(int mouth){this.mouth=mouth;}}
    private static final Map<Integer,Roll> ROLLS=new HashMap<>();
    public static boolean rolling(int i){return ROLLS.containsKey(i);}
    static void drop(ServerLevel l,BlockPos b,ServerPlayer p,int i){
        if(sealed(l,i)){PuzzleRooms.say(p,"The mouth is masonry now. There is nowhere to let it go.");return;}
        if(ROLLS.containsKey(i)){PuzzleRooms.say(p,"A chip is still going down that stair.");return;}
        var hand=p.getMainHandItem();if(!isChip(hand)){hand=p.getOffhandItem();if(!isChip(hand)){PuzzleRooms.say(p,"The top tread is worn smooth at the mouth. A chip let go here would go down.");return;}}
        hand.shrink(1);
        // Clear the last chip that came to rest from this stair, wherever it lay.
        for(var at:new BlockPos[]{foot(i),onLanding(i)})if(SphinxBlock.is(l.getBlockState(b.offset(at)),CHIP))set(l,b,at,Blocks.AIR.defaultBlockState());
        ROLLS.put(i,new Roll(i));
        l.playSound(null,b.offset(tread(i,0)),PuzzleRegistry.SPHINX_RATTLE.get(),SoundSource.BLOCKS,.9F,1F);
        PuzzleRooms.say(p,"You let the chip go at the mouth of the stair.");
    }
    /** One step of every rolling chip: down its flight; the live one into the dark, then back out across the landing overhead to its lip. */
    static void roll(ServerLevel l,BlockPos b,int live){
        for(var it=ROLLS.values().iterator();it.hasNext();){
            var r=it.next();if(r.hold>0){r.hold--;continue;}
            if(r.at!=null&&SphinxBlock.is(l.getBlockState(b.offset(r.at)),CHIP))set(l,b,r.at,Blocks.AIR.defaultBlockState());
            BlockPos next;boolean rest=false;
            if(r.mouth!=live){
                if(r.step>=STEPS){it.remove();continue;}
                next=tread(r.mouth,r.step);rest=r.step==STEPS-1;
            }else if(r.step<LIVE_TURN){next=tread(r.mouth,r.step);
            }else if(r.step==LIVE_TURN){r.at=null;r.step++;r.hold=6;continue; // gone round the turn, out of sight
            }else{
                int across=r.step-LIVE_TURN-1;int z=LANDING_Z1+across;if(z>LANDING_Z0){it.remove();continue;}
                next=new BlockPos(MOUTHS[r.mouth],LANDING_Y+1,z);rest=z==LANDING_Z0;
                if(across==0)l.playSound(null,b.offset(next),PuzzleRegistry.SPHINX_RATTLE.get(),SoundSource.BLOCKS,.7F,1.25F);
            }
            set(l,b,next,SphinxBlock.of(CHIP,Direction.NORTH));r.at=next;r.step++;
            var at=b.offset(next);l.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST,Blocks.STONE.defaultBlockState()),at.getX()+.5,at.getY()+.1,at.getZ()+.5,2,.1,.02,.1,0);
            if(rest){it.remove();l.playSound(null,at,PuzzleRegistry.SPHINX_LATCH.get(),SoundSource.BLOCKS,.4F,1.9F);
                l.sendParticles(ParticleTypes.POOF,at.getX()+.5,at.getY()+.1,at.getZ()+.5,3,.15,.02,.15,0);}
        }
    }

    // ------------------------------------------------------------------ hands on
    private static final Set<UUID> ASKED=new HashSet<>();
    private static final Map<UUID,Long> LAST=new HashMap<>();
    public static void clearAll(){ASKED.clear();LAST.clear();STRIKES.clear();ROLLS.clear();EMPTY[0]=0;}
    static int mouthOf(BlockPos rel){
        for(int i=0;i<3;i++){if(rel.equals(release(i))||rel.equals(pin(i))||rel.equals(survey(i)))return i;
            if(rel.getZ()==GATE_Z&&rel.getY()>=0&&rel.getY()<=1&&Math.abs(rel.getX()-MOUTHS[i])<=1)return i;}
        return -1;
    }
    public static boolean use(ServerPlayer p,BlockPos pos,BlockState s){
        if(p.isSpectator())return false;var l=p.serverLevel();var b=base(p.server);if(b==null||!here(p))return false;
        long now=l.getGameTime();if(now-LAST.getOrDefault(p.getUUID(),-100L)<4)return true;LAST.put(p.getUUID(),now);
        int i=mouthOf(pos.subtract(b));
        switch(s.getValue(SphinxBlock.KIND)){
            case SphinxBlock.Kind.TRAY->{if(!p.getInventory().add(chip()))p.drop(chip(),false);l.playSound(null,pos,PuzzleRegistry.SPHINX_RATTLE.get(),SoundSource.BLOCKS,.25F,1.6F);
                PuzzleRooms.say(p,"You take a chip of stone from the tray.");}
            case RAIL->{if(i>=0)drop(l,b,p,i);}
            case LATCH->{if(i>=0)release(l,b,p,i);}
            case PIN->{if(i>=0)pull(l,b,p,i);}
            case SURVEY->{if(i>=0)survey(l,b,p,i);}
            case GRILLE->{if(provoked(l)>=0){PuzzleRooms.say(p,"Only breathing comes up through the grille.");return true;}ask(l,p);}
            case GRATE->PuzzleRooms.say(p,"Iron grating. Through it, a long way down, the stair you came up.");
            case CHIP->PuzzleRooms.say(p,"A chip of stone, come to rest.");
            case LIMB->PuzzleRooms.say(p,"A foreleg laid up the stair, furred, heavier than the flight.");
            case FINGERS->PuzzleRooms.say(p,"A paw on the top tread, the claws hooked over the gate.");
            default->{}
        }
        return true;
    }
    /** A gate's catch released: the answer. The live gate swings back; any other catches, and she says to leave that stair shut. */
    static void release(ServerLevel l,BlockPos b,ServerPlayer p,int i){
        if(sealed(l,i)){PuzzleRooms.say(p,"There is no stair there now. The masonry is cracked in a stair's shape.");return;}
        if(provoked(l)==i){PuzzleRooms.say(p,"The catch is bent hard against the post. Something is leaning on the gate.");return;}
        if(!SphinxCore.begin(l,KEY)){PuzzleRooms.say(p,"Another gate is still moving.");return;}
        int live=live(l);boolean right=i==live;
        var answered=SphinxCore.commit(l,KEY,RIDDLE,p,i,live);
        for(int k=0;k<3;k++)SphinxCore.socket(l,b.offset(survey(k)),Direction.SOUTH,0);
        for(var uuid:answered.keySet()){if(uuid.equals(p.getUUID()))continue;var other=l.getServer().getPlayerList().getPlayer(uuid);
            if(other!=null)PuzzleRooms.say(other,"A catch is released. Your pin "+WHERE[answered.get(uuid)]+" is your answer.");}
        var catchState=l.getBlockState(b.offset(release(i)));l.setBlock(b.offset(release(i)),catchState.setValue(SphinxBlock.STAGE,1),FLAGS);
        if(right){
            var r=room(l);r.putBoolean("Released",true);room(l,r);
            gateShut(l,b,i,true);l.playSound(null,b.offset(gate(i,0)),PuzzleRegistry.SPHINX_LATCH.get(),SoundSource.BLOCKS,.9F,.8F);
            PuzzleRooms.say(p,"The catch gives. The gate swings back against its post, and the stair goes down into the dark.");
        }else{
            l.scheduleTick(b.offset(release(i)),PuzzleRegistry.SPHINX.get(),20);
            l.playSound(null,b.offset(gate(i,0)),PuzzleRegistry.SPHINX_CATCH.get(),SoundSource.BLOCKS,1F,.9F);
            var r=room(l);r.putInt("Warned",r.getInt("Warned")|(1<<i));
            set(l,b,pin(i),SphinxBlock.of(PIN,Direction.SOUTH,1));
            say(l,b,"Not that stair. Leave that one shut.",readers(l));
            if(provoked(l)<0){r.putLong(peek(i),l.getGameTime()+PEEK);room(l,r);eye(l,b,i,-4);}else room(l,r);
        }
    }
    /** The stop pin pulled at a refused gate: the provocation, one stair at a time. */
    static void pull(ServerLevel l,BlockPos b,ServerPlayer p,int i){
        var s=l.getBlockState(b.offset(pin(i)));
        if(SphinxBlock.stage(s)==0){PuzzleRooms.say(p,"The gate's stop. Something small is set into the post, flush.");return;}
        if(SphinxBlock.stage(s)==2||provoked(l)>=0){PuzzleRooms.say(p,"The pin is out.");return;}
        set(l,b,pin(i),s.setValue(SphinxBlock.STAGE,2));
        var r=room(l);r.putInt("Provoked",i);r.putLong("Emerged",l.getGameTime()+50);r.putLong("Pulled",l.getGameTime());
        if(r.contains(peek(i))){r.remove(peek(i));eye(l,b,i,Integer.MIN_VALUE);}room(l,r);
        SphinxCore.forced(l,p.getUUID(),RIDDLE);
        l.playSound(null,b.offset(pin(i)),PuzzleRegistry.SPHINX_PIN.get(),SoundSource.BLOCKS,1F,1F);
        l.playSound(null,b.offset(gate(i,0)).above(6),PuzzleRegistry.SPHINX_BREATH.get(),SoundSource.HOSTILE,1.4F,.9F);
        PuzzleRooms.say(p,"The pin comes free. Dust starts down out of the dark overhead.");
        EMPTY[0]=0;
    }
    static void survey(ServerLevel l,BlockPos b,ServerPlayer p,int i){
        if(sealed(l,i)){PuzzleRooms.say(p,"The socket is cracked through with the masonry.");return;}
        int was=SphinxCore.pinOf(l,KEY,p.getUUID());int now=SphinxCore.pin(l,KEY,p.getUUID(),i);
        if(was>=0)SphinxCore.socket(l,b.offset(survey(was)),Direction.SOUTH,SphinxCore.pinCount(l,KEY,was));
        SphinxCore.socket(l,b.offset(survey(i)),Direction.SOUTH,SphinxCore.pinCount(l,KEY,i));
        SphinxCore.pinned(l,p,b.offset(survey(i)),now,WHERE[i]);
    }
    static void unpin(ServerLevel l,BlockPos b,UUID reader){
        int was=SphinxCore.pinOf(l,KEY,reader);if(was<0)return;SphinxCore.unpin(l,KEY,reader);
        SphinxCore.socket(l,b.offset(survey(was)),Direction.SOUTH,SphinxCore.pinCount(l,KEY,was));
    }

    // ------------------------------------------------------------------ her eye in the gap
    static String peek(int i){return "PeekUntil"+i;}
    /** Her eye, face up in the gap beside flight i, its top at {@code y}; MIN_VALUE clears it. Four across, two deep, looking up. */
    static void eye(ServerLevel l,BlockPos b,int i,int y){
        int x0=eyeX(i),z0=eyeZ(i);
        for(int yy=-13;yy<=-1;yy++)for(int dx=0;dx<4;dx++)for(int dz=0;dz<2;dz++){var at=new BlockPos(x0+dx,yy,z0+dz);if(SphinxEyeBlock.is(l.getBlockState(b.offset(at)))&&yy!=y)set(l,b,at,Blocks.AIR.defaultBlockState());}
        if(y==Integer.MIN_VALUE)return;
        int look=lookAt(l,b,i);
        for(int dx=0;dx<4;dx++)for(int dz=0;dz<2;dz++){var at=new BlockPos(x0+dx,y,z0+dz);var st=l.getBlockState(b.offset(at));
            if(st.isAir()||SphinxEyeBlock.is(st))set(l,b,at,SphinxEyeBlock.of((dz==0?4:0)+dx,look,SphinxEyeBlock.UP));}
    }
    public static int eyeY(ServerLevel l,BlockPos b,int i){for(int y=-1;y>=-13;y--)if(SphinxEyeBlock.is(l.getBlockState(b.offset(eyeX(i),y,eyeZ(i)))))return y;return Integer.MIN_VALUE;}
    public static int lookAt(ServerLevel l,BlockPos b,int i){
        double cx=b.getX()+eyeX(i)+2,cz=b.getZ()+eyeZ(i)+1;ServerPlayer near=null;double best=Double.MAX_VALUE;
        for(var p:readers(l)){double d=Math.pow(p.getX()-cx,2)+Math.pow(p.getZ()-cz,2);if(d<best){best=d;near=p;}}
        if(near==null)return SphinxEyeBlock.AHEAD;
        double dx=near.getX()-cx,dz=near.getZ()-cz;return (dx<-1.2?0:dx>1.2?2:1)+3*(dz<-.4?0:dz>1.0?2:1);
    }
    static void follow(ServerLevel l,BlockPos b,int i){
        int y=eyeY(l,b,i);if(y==Integer.MIN_VALUE)return;int look=lookAt(l,b,i);
        for(int dx=0;dx<4;dx++)for(int dz=0;dz<2;dz++){var at=b.offset(eyeX(i)+dx,y,eyeZ(i)+dz);var st=l.getBlockState(at);if(SphinxEyeBlock.is(st)&&st.getValue(SphinxEyeBlock.LOOK)!=look)l.setBlock(at,st.setValue(SphinxEyeBlock.LOOK,look),FLAGS);}
    }
    /** Her foreleg up flight i from the dark to tread {@code from} (STEPS: none), in the middle column. */
    static void limb(ServerLevel l,BlockPos b,int i,int from){
        for(int k=0;k<STEPS+2;k++){var at=tread(i,k);var st=l.getBlockState(b.offset(at));boolean on=k>=Math.max(1,from);
            if(on&&(st.isAir()||SphinxBlock.is(st,CHIP)))set(l,b,at,SphinxBlock.of(LIMB,Direction.SOUTH));else if(!on&&SphinxBlock.is(st,LIMB))set(l,b,at,Blocks.AIR.defaultBlockState());}
    }

    // ------------------------------------------------------------------ the clock
    private static final Map<UUID,Long> STRIKES=new HashMap<>();
    private static final long[] EMPTY={0};
    static void update(ServerLevel l,BlockPos b){
        var readers=readers(l);var r=room(l);int live=live(l);long now=l.getGameTime();
        roll(l,b,live);
        if(now%20<2)for(var uuid:SphinxCore.pins(l,KEY).keySet()){var q=l.getServer().getPlayerList().getPlayer(uuid);if(q==null||!here(q))unpin(l,b,uuid);}
        for(int i=0;i<3;i++){
            if(!r.contains(peek(i)))continue;long until=r.getLong(peek(i));
            if(until<=now){eye(l,b,i,Integer.MIN_VALUE);r.remove(peek(i));room(l,r);continue;}
            if(now%4<2){follow(l,b,i);var top=b.offset(gate(i,0)).above(TOP-2);
                l.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST,Blocks.SAND.defaultBlockState()),top.getX()+.5,top.getY(),top.getZ()+1.5,3,1.2,.05,.4,0);}
        }
        for(var p:readers){
            if(inGallery(p,b)&&!ASKED.contains(p.getUUID())&&provoked(l)<0)ask(l,p);
            // A fall into the shaft is caught and set back at the rail: testing is safe.
            if(ly(p,b)<-6&&!onFlight(p,b,live)||ly(p,b)<LANDING_Y-1&&lz(p,b)<LANDING_Z1-1&&ly(p,b)>-1){
                p.fallDistance=0;HouseInternalTeleport.translate(p,b.getX()+.5-p.getX(),b.getY()-p.getY(),b.getZ()+SWEEP_Z-p.getZ());
                PuzzleRooms.say(p,"The dark gives you back at the rail.");continue;}
            // Down the released live flight and round the turn: up onto the landing you were watching.
            if(r.getBoolean("Released")&&onFlight(p,b,live)&&lz(p,b)<GATE_Z-1-3){
                HouseInternalTeleport.translate(p,0,b.getY()+ARRIVE.getY()-p.getY(),b.getZ()+ARRIVE.getZ()+.5-p.getZ());p.fallDistance=0;
                if(!r.getBoolean("Solved")){r.putBoolean("Solved",true);room(l,r);set(l,b,JIB,Blocks.AIR.defaultBlockState());set(l,b,JIB.above(),Blocks.AIR.defaultBlockState());
                    l.playSound(null,b.offset(JIB),PuzzleRegistry.SPHINX_SCRAPE.get(),SoundSource.BLOCKS,.8F,.6F);}
                PuzzleRooms.say(p,"The stair goes down and round, and you come up onto the landing. Below you, through the grating, is the gallery.");
                continue;
            }
            int forced=provoked(l);
            if(forced>=0&&r.getLong("Emerged")<=now&&onFlight(p,b,forced)){
                // Down under her: out by the way onward, and the mouth closes behind.
                HouseInternalTeleport.translate(p,b.getX()+ONWARD_X+.5-p.getX(),b.getY()-p.getY(),b.getZ()-19.5-p.getZ());p.fallDistance=0;
                r.putInt("Sealed",r.getInt("Sealed")|(1<<forced));room(l,r);
                for(int dx=-1;dx<=1;dx++)for(int y=0;y<=2;y++)set(l,b,MOUTHS[forced]+dx,y,GATE_Z,mat("ashlar_seam"));
                withdraw(l,b);
                PuzzleRooms.say(p,"You go down under her arm, and the stair turns you out into a corridor. Behind you the mouth closes like a throat.");
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
        long pulled=r.getLong("Pulled"),emerged=r.getLong("Emerged");
        if(now%4<2){var top=b.offset(gate(forced,0)).above(TOP-2);l.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST,Blocks.SAND.defaultBlockState()),top.getX()+.5,top.getY(),top.getZ()+1.5,6,1.6,.05,.6,0);}
        if(emerged>now){
            long e=now-pulled;
            // An eye coming up through the gap between the flights, then her foreleg up the stair.
            if(e>=10)eye(l,b,forced,(int)Math.min(-2,-13+(e-10)/2));
            if(e>=30)limb(l,b,forced,(int)Math.max(1,STEPS+1-(e-30)/2));
            return;
        }
        follow(l,b,forced);
        if(!SphinxBlock.is(l.getBlockState(b.offset(tread(forced,0))),FINGERS)){
            limb(l,b,forced,1);
            for(int dx=-1;dx<=1;dx++){set(l,b,tread(forced,0).offset(dx,0,0),SphinxBlock.of(FINGERS,Direction.SOUTH));if(!sealed(l,forced))set(l,b,gate(forced,dx),SphinxBlock.of(RAIL,Direction.NORTH,2));}
            l.playSound(null,b.offset(gate(forced,0)),PuzzleRegistry.SPHINX_STRIKE.get(),SoundSource.HOSTILE,1F,.6F);
        }
        boolean anyone=false;
        for(var p:readers){
            if(!inGallery(p,b))continue;anyone=true;
            var rec=record(l,p.getUUID());if(!rec.getBoolean("Retreated")){rec.putBoolean("Retreated",true);SphinxCore.record(l,p.getUUID(),RIDDLE,rec);}
            if(Math.abs(lx(p,b)-(MOUTHS[forced]+.5))>REACH)continue;
            long t=STRIKES.getOrDefault(p.getUUID(),0L);
            if(t==0){STRIKES.put(p.getUUID(),now+25);l.playSound(null,p.blockPosition(),PuzzleRegistry.SPHINX_BREATH.get(),SoundSource.HOSTILE,1F,1.3F);
                l.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST,Blocks.SAND.defaultBlockState()),p.getX(),p.getY()+2.6,p.getZ(),12,.4,.1,.4,0);}
            else if(t<=now){STRIKES.remove(p.getUUID());
                // She never hurts anyone: her paw comes up over the gate and sweeps them back over the approach rail.
                l.playSound(null,p.blockPosition(),PuzzleRegistry.SPHINX_STRIKE.get(),SoundSource.HOSTILE,1F,1F);
                double x=Math.max(-8.5,Math.min(8.5,lx(p,b)));
                HouseInternalTeleport.translate(p,b.getX()+x-p.getX(),0,b.getZ()+SWEEP_Z-p.getZ());
                var rec2=record(l,p.getUUID());rec2.putInt("Swept",rec2.getInt("Swept")+1);SphinxCore.record(l,p.getUUID(),RIDDLE,rec2);
                PuzzleRooms.say(p,"Her paw comes up over the gate and sweeps you back over the rail.");}
        }
        for(var p:readers)if(!inGallery(p,b))STRIKES.remove(p.getUUID());
        if(anyone)EMPTY[0]=0;else if(++EMPTY[0]>=50)withdraw(l,b);
    }
    /** She draws back down: the paw and foreleg go, the eye sinks, the gate shuts on its stop, the pin goes back in. */
    static void withdraw(ServerLevel l,BlockPos b){
        var r=room(l);int i=r.contains("Provoked")?r.getInt("Provoked"):-1;if(i<0)return;
        for(int dx=-1;dx<=1;dx++)if(SphinxBlock.is(l.getBlockState(b.offset(tread(i,0).offset(dx,0,0))),FINGERS))set(l,b,tread(i,0).offset(dx,0,0),Blocks.AIR.defaultBlockState());
        limb(l,b,i,STEPS+2);eye(l,b,i,Integer.MIN_VALUE);
        if(!sealed(l,i))gateShut(l,b,i,i==live(l)&&r.getBoolean("Released"));
        set(l,b,pin(i),SphinxBlock.of(PIN,Direction.SOUTH));
        r.remove("Provoked");r.remove("Emerged");r.remove("Pulled");r.putInt("Warned",r.getInt("Warned")&~(1<<i));room(l,r);
        STRIKES.clear();EMPTY[0]=0;
        l.playSound(null,b.offset(gate(i,0)).below(4),PuzzleRegistry.SPHINX_BREATH.get(),SoundSource.HOSTILE,.8F,.7F);
    }
    /** Chips go no further than the room: anyone leaving with one finds their pocket empty. */
    static void pockets(ServerPlayer p){var inv=p.getInventory();for(int k=0;k<inv.getContainerSize();k++)if(isChip(inv.getItem(k)))inv.setItem(k,ItemStack.EMPTY);}
    private static final Set<UUID> INSIDE=new HashSet<>();
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();if(server.getTickCount()%2!=0)return;
        var l=server.getLevel(HouseDimensions.INTERIOR);var b=base(server);if(l==null||b==null||!l.isLoaded(b))return;
        if(LabyrinthData.get(server).door(PLACE.entryDoorId())==null)return;
        for(var uuid:new ArrayList<>(INSIDE)){var p=server.getPlayerList().getPlayer(uuid);if(p==null||!here(p)){INSIDE.remove(uuid);if(p!=null)pockets(p);}}
        boolean any=!ASKED.isEmpty()||provoked(l)>=0||!ROLLS.isEmpty()||room(l).contains("Pins");
        for(int i=0;i<3&&!any;i++)if(room(l).contains(peek(i)))any=true;
        for(var p:l.players())if(here(p)){any=true;INSIDE.add(p.getUUID());}
        if(any)update(l,b);
    }
}
