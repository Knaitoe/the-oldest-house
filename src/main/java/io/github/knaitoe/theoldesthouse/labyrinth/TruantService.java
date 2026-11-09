package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseInternalTeleport;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import static io.github.knaitoe.theoldesthouse.labyrinth.TruantFittingBlock.Kind.*;

/**
 * 0.4.307, Johnny Truant's service halls (docs/TRUANT_SERVICE_0_4_307.md), the middle-hall pairs from the owner's "Encounter Pairs": three
 * places of his own in the stone and service halls, each where he can be glad help and, later, the same object turned on someone.
 * <ul>
 * <li><b>The chalk arrow.</b> A service junction. Help: he chalks an arrow toward the branch the pipes run down and the floor drains into.
 * Turned (ARROW): on a later pass he rubs it out and draws it toward the branch that narrows and drops into a sump. The pipes still give
 * the way away; a ladder climbs out of the sump.</li>
 * <li><b>The pipe knock.</b> A blind elbow with a steam main that shivers, then vents across the corner, then is quiet, over and over;
 * the shiver shows on the pipe all the way back up the corridor. Help: from round the elbow he knocks three on the pipe once it is
 * quiet, and answers a reader's knock. Turned (KNOCK): the same three just as the shiver starts.</li>
 * <li><b>The lift brake.</b> A service lift down to the way onward. Help: he holds the brake until the last reader is aboard, then lets
 * it down gently. Turned (BRAKE): he lets go as the one he wants is crossing the sill: the car lurches, then drops. Whoever is aboard
 * rides it; the brake on the car stops it; there is a ledge in the shaft wall beside the sill.</li>
 * </ul>
 * Nothing hostile happens with anyone under twenty passes in the place (Truant.gated), and each harm is recorded as his others are.
 */
public final class TruantService {
    private TruantService(){}
    public static final LabyrinthPlace CHALK=LabyrinthPlace.TRUANT_CHALK,KNOCK=LabyrinthPlace.TRUANT_KNOCK,LIFT=LabyrinthPlace.TRUANT_LIFT;
    public static final List<LabyrinthPlace> PLACES=List.of(CHALK,KNOCK,LIFT);
    public static final int HELP_CHALK=16,HELP_KNOCK=32,HELP_BRAKE=64;
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    // ------------------------------------------------------------------ the plans
    /** The chalk junction: the arrow on the north wall, the junction's middle, the sump at the end of the narrow branch, his spot. */
    public static final BlockPos ARROW_AT=new BlockPos(0,1,-11);
    public static final Vec3 JUNCTION=new Vec3(.5,0,-9.5),CHALK_SPOT=new Vec3(1.6,0,-9.4);
    public static final int SUMP_X=7,SUMP_FLOOR=-5;
    /** The elbow: its burst zone round the corner, the steam main's vents, where he waits round the corner; the cycle and its parts. */
    public static final int CYCLE=160,SHIVER=30,VENT=60,HELP_KNOCK_AT=64,TURNED_KNOCK_AT=2;
    public static final Vec3 KNOCK_SPOT=new Vec3(-6.4,0,-14.2);
    /** The lift: the shaft's cells, the platform at the top and bottom, the landing brake, his spot, the ledge in the shaft wall. */
    /** The landing is up a stair, eight above the floor the way on is at; the car's platform stands at TOP up there, BOTTOM down here. */
    public static final int SHAFT_X0=-1,SHAFT_X1=1,SHAFT_Z0=-11,SHAFT_Z1=-13,LANDING=8,TOP=7,BOTTOM=-1;
    public static final BlockPos BRAKE_AT=new BlockPos(1,LANDING,-9),LEDGE=new BlockPos(-2,LANDING,-11);
    public static final Vec3 LIFT_SPOT=new Vec3(.9,LANDING,-9.4);

    public static boolean is(@Nullable LabyrinthPlace place){return place!=null&&PLACES.contains(place);}
    static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){l.setBlock(b.offset(x,y,z),s,FLAGS);}
    static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){l.setBlock(b.offset(at),s,FLAGS);}
    static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){
        for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int y=Math.min(y0,y1);y<=Math.max(y0,y1);y++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)set(l,b,x,y,z,s);
    }
    static BlockState air(){return Blocks.AIR.defaultBlockState();}
    static BlockState pipe(Direction f,int stage){return TruantFittingBlock.of(PIPE,f,stage);}
    static BlockState wallPipe(Direction f){return TankBlock.of(TankBlock.Kind.PIPE,f,TankBlock.RUN);}

    // ------------------------------------------------------------------ building
    public static void build(ServerLevel l,BlockPos b,LabyrinthPlace place){
        switch(place){
            case TRUANT_CHALK->buildChalk(l,b);
            case TRUANT_KNOCK->buildKnock(l,b);
            case TRUANT_LIFT->buildLift(l,b);
            default->{}
        }
    }
    static void shell(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1){box(l,b,x0,y0,z0,x1,y1,z1,Blocks.STONE_BRICKS.defaultBlockState());}
    static void floor(ServerLevel l,BlockPos b,int x0,int z0,int x1,int z1,int y){box(l,b,x0,y-1,z0,x1,y-1,z1,Blocks.POLISHED_ANDESITE.defaultBlockState());box(l,b,x0,y,z0,x1,y+2,z1,air());}
    static void lamp(ServerLevel l,BlockPos b,int x,int y,int z){set(l,b,x,y,z,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));}

    /** The junction: the corridor north, the cross passage, the drained west branch with the pipes to the way on, the dry east branch to the drop. */
    public static void buildChalk(ServerLevel l,BlockPos b){
        shell(l,b,-8,-5,-20,8,4,0);
        floor(l,b,-1,-1,1,-8,0);
        floor(l,b,-6,-9,6,-10,0);
        floor(l,b,-6,-11,-4,-18,0);
        // The pipes: down the corridor's west wall, along the junction's north wall, down the west branch's west wall.
        for(int z=-1;z>=-8;z--)set(l,b,-1,2,z,wallPipe(Direction.EAST));
        for(int x=-6;x<=-1;x++)set(l,b,x,2,-10,wallPipe(Direction.SOUTH));
        for(int z=-11;z>=-18;z--)set(l,b,-6,2,z,wallPipe(Direction.EAST));
        // A gutter down the floor that drains west, and the west branch a step lower past its first yard, up again a yard short of the way
        // on. 0.4.403, the owner's playtest: the dip ran right up to the door, and a reader crossing from a block below its sill came out
        // in the next hall half in its floor.
        for(int x=-5;x<=0;x++)set(l,b,x,-1,-9,Blocks.IRON_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF,Half.TOP));
        for(int z=-13;z>=-17;z--)for(int x=-6;x<=-4;x++){set(l,b,x,-2,z,Blocks.POLISHED_ANDESITE.defaultBlockState());set(l,b,x,-1,z,air());}
        for(int x=-6;x<=-4;x++)set(l,b,x,-1,-13,Blocks.POLISHED_ANDESITE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH));
        for(int x=-6;x<=-4;x++)set(l,b,x,-1,-17,Blocks.POLISHED_ANDESITE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));
        // The east branch narrows to one wide and two high, then the floor ends over a sump with a ladder out.
        box(l,b,3,0,-9,6,2,-9,Blocks.STONE_BRICKS.defaultBlockState());box(l,b,3,2,-10,7,2,-10,Blocks.STONE_BRICKS.defaultBlockState());
        set(l,b,7,0,-10,air());set(l,b,7,1,-10,air());
        box(l,b,SUMP_X,SUMP_FLOOR+1,-10,SUMP_X,-1,-12,air());set(l,b,SUMP_X,SUMP_FLOOR,-10,Blocks.COBBLESTONE.defaultBlockState());
        for(int y=SUMP_FLOOR+1;y<=-1;y++)set(l,b,SUMP_X,y,-12,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH));
        set(l,b,SUMP_X,0,-12,air());set(l,b,SUMP_X,1,-12,air());set(l,b,SUMP_X,0,-11,air());set(l,b,SUMP_X,1,-11,air());set(l,b,SUMP_X,-1,-11,Blocks.POLISHED_ANDESITE.defaultBlockState());
        set(l,b,6,0,-11,air());set(l,b,6,1,-11,air());set(l,b,6,-1,-11,Blocks.POLISHED_ANDESITE.defaultBlockState());
        // The chalk wall, bare; the lamps.
        set(l,b,ARROW_AT,TruantFittingBlock.of(ARROW,Direction.SOUTH,0));
        lamp(l,b,0,2,-4);lamp(l,b,0,2,-9);lamp(l,b,-5,2,-16);
        LabyrinthBuilder.entrance(l,b,Blocks.STONE_BRICKS.defaultBlockState(),Blocks.POLISHED_ANDESITE.defaultBlockState(),Blocks.STONE_BRICKS.defaultBlockState());
        LabyrinthBuilder.doors(l,b,CHALK);
    }

    /** The elbow: the corridor north, the passage west round the blind corner, the branch north to the way on; the steam main the whole way. */
    public static void buildKnock(ServerLevel l,BlockPos b){
        shell(l,b,-9,-2,-22,3,4,0);
        floor(l,b,-1,-1,1,-12,0);
        floor(l,b,-8,-10,-2,-12,0);
        floor(l,b,-7,-13,-5,-20,0);
        for(int z=-1;z>=-11;z--)set(l,b,-1,2,z,pipe(Direction.EAST,0));
        for(int x=-7;x<=-2;x++)set(l,b,x,2,-12,pipe(Direction.SOUTH,0));
        lamp(l,b,0,2,-5);lamp(l,b,-6,2,-17);
        set(l,b,-7,0,-15,Blocks.BARREL.defaultBlockState());
        LabyrinthBuilder.entrance(l,b,Blocks.STONE_BRICKS.defaultBlockState(),Blocks.POLISHED_ANDESITE.defaultBlockState(),Blocks.STONE_BRICKS.defaultBlockState());
        LabyrinthBuilder.doors(l,b,KNOCK);
    }

    /** The lift: a stair up from the door to the landing, the shaft with its car at the top, the brake by the sill, a ledge in the shaft wall, the passage on at the foot. */
    public static void buildLift(ServerLevel l,BlockPos b){
        shell(l,b,-6,-1,-17,6,13,0);
        var floor=Blocks.POLISHED_ANDESITE.defaultBlockState();
        for(int i=0;i<LANDING;i++){
            int z=-1-i;
            box(l,b,-1,i,z,1,i+3,z,air());
            for(int x=-1;x<=1;x++)set(l,b,x,i-1<0?-1:i-1,z,floor);
            if(i>0)for(int x=-1;x<=1;x++)set(l,b,x,i-1,z,Blocks.POLISHED_ANDESITE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));
        }
        floor(l,b,-1,-9,1,-10,LANDING);
        box(l,b,SHAFT_X0,BOTTOM,SHAFT_Z1,SHAFT_X1,LANDING+2,SHAFT_Z0,air());
        floor(l,b,-1,-14,1,-15,0);
        set(l,b,LEDGE,air());set(l,b,LEDGE.above(),air());set(l,b,LEDGE.below(),floor);
        for(int y=BOTTOM+1;y<=LANDING+2;y++)set(l,b,2,y,SHAFT_Z1,Blocks.CHAIN.defaultBlockState());
        car(l,b,TOP);
        set(l,b,BRAKE_AT,TruantFittingBlock.of(BRAKE,Direction.WEST,1));
        lamp(l,b,0,LANDING+2,-9);lamp(l,b,0,2,-15);lamp(l,b,0,6,-4);
        LabyrinthBuilder.entrance(l,b,Blocks.STONE_BRICKS.defaultBlockState(),floor,Blocks.STONE_BRICKS.defaultBlockState());
        LabyrinthBuilder.doors(l,b,LIFT);
    }

    // ------------------------------------------------------------------ the lift's car
    /** Where the car stands: the height of its platform. */
    public static int car(ServerLevel l,BlockPos b){for(int y=TOP;y>=BOTTOM;y--)if(TruantFittingBlock.is(l.getBlockState(b.offset(0,y,SHAFT_Z0-1)),PLATFORM))return y;return TOP;}
    static void car(ServerLevel l,BlockPos b,int y){
        for(int yy=BOTTOM;yy<=TOP+1;yy++)for(int x=SHAFT_X0;x<=SHAFT_X1;x++)for(int z=SHAFT_Z1;z<=SHAFT_Z0;z++){
            var s=l.getBlockState(b.offset(x,yy,z));if(TruantFittingBlock.is(s,PLATFORM)||TruantFittingBlock.is(s,CAR_BRAKE))set(l,b,x,yy,z,air());
        }
        for(int x=SHAFT_X0;x<=SHAFT_X1;x++)for(int z=SHAFT_Z1;z<=SHAFT_Z0;z++)set(l,b,x,y,z,TruantFittingBlock.of(PLATFORM,Direction.NORTH,0));
        set(l,b,1,y+1,SHAFT_Z1,TruantFittingBlock.of(CAR_BRAKE,Direction.WEST,0));
    }
    static List<ServerPlayer> aboard(ServerLevel l,BlockPos b,List<ServerPlayer> readers){
        int y=car(l,b);double top=b.getY()+y+1;var box=new AABB(b.getX()+SHAFT_X0,top-.3,b.getZ()+SHAFT_Z1,b.getX()+SHAFT_X1+1,top+1.5,b.getZ()+SHAFT_Z0+1);
        var out=new ArrayList<ServerPlayer>();for(var p:readers)if(box.contains(p.position()))out.add(p);return out;
    }
    /** One step of the car, and whoever is aboard with it. */
    static void step(ServerLevel l,BlockPos b,int dir,List<ServerPlayer> readers){step(l,b,dir,readers,null);}
    /** As above; the one left behind (the one he let go for) is not carried: they fall after it. */
    static void step(ServerLevel l,BlockPos b,int dir,List<ServerPlayer> readers,@Nullable UUID left){
        int y=car(l,b),next=y+dir;if(next<BOTTOM||next>TOP)return;
        var riders=aboard(l,b,readers);car(l,b,next);
        for(var p:riders)if(!p.getUUID().equals(left))HouseInternalTeleport.translate(p,0,dir,0);
    }

    // ------------------------------------------------------------------ hands on
    public static boolean use(ServerPlayer p,BlockPos pos,BlockState s){
        if(p.isSpectator())return false;var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null)return false;
        var place=LabyrinthPlaces.placeAt(origin,pos);if(!is(place))return false;var b=LabyrinthPlaces.base(origin,place);var l=p.serverLevel();
        switch(s.getValue(TruantFittingBlock.KIND)){
            case ARROW->PuzzleRooms.say(p,switch(s.getValue(TruantFittingBlock.STAGE)){case 1->"A chalk arrow, pointing left.";case 2->"A chalk arrow, pointing right. There is a smudge where another was rubbed out.";default->"Bare plaster, chalk dust on the skirting.";});
            case PIPE->{
                l.playSound(null,pos,PuzzleRegistry.TRUANT_KNOCK.get(),SoundSource.BLOCKS,.9F,1.1F);
                PuzzleRooms.say(p,"You knock three times on the pipe.");
                if(KNOCK_PENDING<0)KNOCK_PENDING=l.getGameTime()+12;
            }
            case BRAKE->PuzzleRooms.say(p,s.getValue(TruantFittingBlock.STAGE)==1?"The lift brake, hauled on. It takes someone's whole weight to hold.":"The lift brake, off.");
            case CAR_BRAKE->{
                if(DROPPING){DROPPING=false;l.setBlock(pos,s.setValue(TruantFittingBlock.STAGE,1),FLAGS);l.playSound(null,pos,PuzzleRegistry.TRUANT_BRAKE.get(),SoundSource.BLOCKS,1F,1.2F);
                    PuzzleRooms.say(p,"You throw the car's brake. It screams on the rails and holds.");}
                else{boolean on=s.getValue(TruantFittingBlock.STAGE)==0;l.setBlock(pos,s.setValue(TruantFittingBlock.STAGE,on?1:0),FLAGS);PuzzleRooms.say(p,on?"You set the car's brake.":"You ease the car's brake off.");}
            }
            case PLATFORM->PuzzleRooms.say(p,"The car's floor: boards in an iron frame, worn pale where feet go.");
        }
        return true;
    }

    // ------------------------------------------------------------------ the scenes, from Truant's clock
    static long KNOCK_PENDING=-1;static boolean DROPPING=false;static int LURCH=0,LOWERING=0,AWAY=0;
    static final Set<UUID> SCALDED=new HashSet<>(),DROPPED=new HashSet<>();
    public static void clearAll(){KNOCK_PENDING=-1;DROPPING=false;LURCH=0;LOWERING=0;AWAY=0;SCALDED.clear();DROPPED.clear();}
    static void newScene(){DROPPING=false;LURCH=0;LOWERING=0;AWAY=0;SCALDED.clear();DROPPED.clear();}
    static Vec3 home(LabyrinthPlace place){return switch(place){case TRUANT_CHALK->CHALK_SPOT;case TRUANT_KNOCK->KNOCK_SPOT;default->LIFT_SPOT;};}

    static void tick(ServerLevel l,BlockPos b,LiteraryActor a,List<ServerPlayer> readers,Truant.Scene sc){
        switch(sc.place){
            case TRUANT_CHALK->chalk(l,b,a,readers,sc);
            case TRUANT_KNOCK->knock(l,b,a,readers,sc);
            case TRUANT_LIFT->lift(l,b,a,readers,sc);
            default->{}
        }
    }

    /** The chalk arrow: drawn for help toward the drained branch; for the one he wants, turned toward the drop. */
    static void chalk(ServerLevel l,BlockPos b,LiteraryActor a,List<ServerPlayer> readers,Truant.Scene sc){
        var at=b.offset(ARROW_AT);int arrow=TruantFittingBlock.stage(l.getBlockState(at));
        var target=Truant.target(readers);Vec3 j=Vec3.atLowerCornerOf(b).add(JUNCTION);
        boolean turned=sc.turn==Truant.Turn.ARROW&&target!=null;
        if(turned){
            var r=Truant.rel(b,target.position());
            if(r.y<-2.5&&Math.abs(r.x-(SUMP_X+.5))<1.2&&DROPPED.add(target.getUUID())){
                Truant.harm(target,"arrow","Arrowed");
                Truant.say(a,"I said quick. I didn't say which.");
            }
        }
        ServerPlayer near=null;for(var p:readers)if(p.position().distanceTo(j)<7){near=p;break;}
        if(near==null)return;
        Truant.face(a,Vec3.atCenterOf(at));
        if(turned&&arrow!=2&&target.position().distanceTo(j)<7){
            l.setBlock(at,TruantFittingBlock.of(ARROW,Direction.SOUTH,2),FLAGS);Truant.gesture(a,Truant.REACH);
            l.playSound(null,at,PuzzleRegistry.TRUANT_CHALK.get(),SoundSource.BLOCKS,.8F,1F);
            Truant.say(a,arrow==1?"Not that way, not tonight. This way. Quick.":"This way. Quick.");
        }else if(!turned&&arrow!=1){
            l.setBlock(at,TruantFittingBlock.of(ARROW,Direction.SOUTH,1),FLAGS);Truant.gesture(a,Truant.REACH);
            l.playSound(null,at,PuzzleRegistry.TRUANT_CHALK.get(),SoundSource.BLOCKS,.8F,1F);
            Truant.say(a,"That way. Where the pipes go. The other one drops.");
            for(var p:readers)if(p.position().distanceTo(j)<9)Truant.helped(p,HELP_CHALK);
        }
    }

    /** The cycle of the steam main at the elbow: where in it we are. */
    public static int phase(ServerLevel l){return (int)(l.getGameTime()%CYCLE);}
    static boolean inSteam(BlockPos b,ServerPlayer p){var r=Truant.rel(b,p.position());return r.x>-3.5&&r.x<2&&r.z<-8.6&&r.z>-12.9&&r.y<2;}
    /** The pipe knock: the steam main's cycle always; his knock from round the corner, at the quiet or, turned, at the shiver. */
    static void knock(ServerLevel l,BlockPos b,LiteraryActor a,List<ServerPlayer> readers,Truant.Scene sc){
        int t=phase(l);int stage=t<SHIVER?1:t<VENT?2:0;
        steam(l,b,stage,t,readers);
        var target=Truant.target(readers);boolean turned=sc.turn==Truant.Turn.KNOCK&&target!=null;
        if(turned&&stage==2&&inSteam(b,target)&&SCALDED.add(target.getUUID())){Truant.harm(target,"knock","Scalded");Truant.say(a,"You heard me knock.");}
        boolean waiting=false;for(var p:readers){var r=Truant.rel(b,p.position());if(r.z>-10&&r.x>-1.5)waiting=true;}
        if(!waiting)return;
        if(turned?t==TURNED_KNOCK_AT:t==HELP_KNOCK_AT){
            rap(l,b,a);
            if(!turned)for(var p:readers)Truant.helped(p,HELP_KNOCK);
        }
        if(KNOCK_PENDING>=0&&l.getGameTime()>=KNOCK_PENDING){
            KNOCK_PENDING=-1;
            // An answer: he knocks back when it is clear, or (turned) whenever he is asked.
            if(turned||stage==0&&t<CYCLE-40)rap(l,b,a);
        }
    }
    static void rap(ServerLevel l,BlockPos b,LiteraryActor a){
        var at=b.offset(-5,2,-12);Truant.gesture(a,Truant.REACH);
        l.playSound(null,at,PuzzleRegistry.TRUANT_KNOCK.get(),SoundSource.BLOCKS,1F,1F);
    }
    /** The main: still, shivering all its length (and rattling), or venting across the corner (and scalding). */
    static void steam(ServerLevel l,BlockPos b,int stage,int t,List<ServerPlayer> readers){
        if(t==0||t==SHIVER||t==VENT){
            for(int z=-1;z>=-11;z--)set(l,b,-1,2,z,pipe(Direction.EAST,stage));
            for(int x=-7;x<=-2;x++)set(l,b,x,2,-12,pipe(Direction.SOUTH,stage));
        }
        if(t==0)l.playSound(null,b.offset(-1,2,-6),PuzzleRegistry.TRUANT_RATTLE.get(),SoundSource.BLOCKS,1F,1F);
        if(t==SHIVER)l.playSound(null,b.offset(-1,2,-11),PuzzleRegistry.TRUANT_HISS.get(),SoundSource.BLOCKS,1.2F,1F);
        if(stage==1&&t%4==0)for(int z=-2;z>=-11;z-=3){var at=b.offset(-1,2,z);l.sendParticles(ParticleTypes.SMOKE,at.getX()+.3,at.getY()+.5,at.getZ()+.5,1,.05,.05,.3,0);}
        if(stage==2&&t%2==0){var c=b.offset(-1,1,-11);l.sendParticles(ParticleTypes.CLOUD,c.getX()+.5,c.getY(),c.getZ()+.5,10,1.6,.6,1.3,.02);}
        if(stage==2&&t%10==0)for(var p:readers)if(inSteam(b,p))p.hurt(l.damageSources().hotFloor(),3F);
    }

    /** The lift brake: held until everyone is aboard and let down gently; turned, let go as the one he wants crosses the sill. */
    static void lift(ServerLevel l,BlockPos b,LiteraryActor a,List<ServerPlayer> readers,Truant.Scene sc){
        var brake=b.offset(BRAKE_AT);int y=car(l,b);
        var target=Truant.target(readers);boolean turned=sc.turn==Truant.Turn.BRAKE&&target!=null;
        if(DROPPING){
            if(++LURCH<10)return;
            UUID left=target==null?null:target.getUUID();
            if(LURCH==10){l.playSound(null,b.offset(0,y,SHAFT_Z0-1),PuzzleRegistry.TRUANT_LURCH.get(),SoundSource.BLOCKS,1F,1F);step(l,b,-1,readers,left);return;}
            if(y<=BOTTOM){DROPPING=false;Truant.say(a,turned?"It does that.":"There.");return;}
            step(l,b,-1,readers,left);return;
        }
        if(turned&&y==TOP&&!DROPPED.contains(target.getUUID())){
            var r=Truant.rel(b,target.position());
            if(r.z<SHAFT_Z0+.8&&r.z>SHAFT_Z0-.4&&r.x>SHAFT_X0-.3&&r.x<SHAFT_X1+1.3&&r.y>LANDING-.5){
                DROPPED.add(target.getUUID());DROPPING=true;LURCH=0;
                l.setBlock(brake,l.getBlockState(brake).setValue(TruantFittingBlock.STAGE,0),FLAGS);Truant.gesture(a,0);
                l.playSound(null,brake,PuzzleRegistry.TRUANT_BRAKE.get(),SoundSource.BLOCKS,1F,.7F);
                Truant.say(a,"Mind the gap.");
            }
            return;
        }
        if(target!=null&&DROPPED.contains(target.getUUID())){
            var r=Truant.rel(b,target.position());
            if(r.y<LANDING-2&&target.hurtTime>0&&SCALDED.add(target.getUUID()))Truant.harm(target,"brake","Dropped");
        }
        // Help: hold the brake at the top; once everyone in the place is aboard, let it down a step at a time.
        Truant.face(a,Vec3.atCenterOf(brake));
        if(y==TOP){
            var s=l.getBlockState(brake);if(TruantFittingBlock.stage(s)!=1){l.setBlock(brake,s.setValue(TruantFittingBlock.STAGE,1),FLAGS);Truant.gesture(a,Truant.HOLD);}
            var on=aboard(l,b,readers);
            if(!on.isEmpty()&&on.size()==readers.size()){
                if(++LOWERING==20){Truant.say(a,"Everyone in? Going down.");for(var p:on)Truant.helped(p,HELP_BRAKE);}
                if(LOWERING>20&&LOWERING%6==0)step(l,b,-1,readers);
            }else LOWERING=0;
        }else if(y>BOTTOM&&LOWERING>20){if(++LOWERING%6==0)step(l,b,-1,readers);}
        else if(y==BOTTOM){
            LOWERING=0;
            // Back up for whoever is still on the landing, empty.
            boolean waiting=false;for(var p:readers){var r=Truant.rel(b,p.position());if(r.y>LANDING-.5&&r.z>SHAFT_Z0+.5)waiting=true;}
            if(waiting&&aboard(l,b,readers).isEmpty()){if(++AWAY%4==0)step(l,b,1,readers);}else AWAY=0;
            if(car(l,b)==TOP)LOWERING=0;
        }else if(y<TOP&&LOWERING<=20){
            if(aboard(l,b,readers).isEmpty()&&++AWAY%4==0)step(l,b,1,readers);
        }
    }
}
