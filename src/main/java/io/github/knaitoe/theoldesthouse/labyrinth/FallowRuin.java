package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import static io.github.knaitoe.theoldesthouse.labyrinth.FallowSite.*;

/**
 * 0.4.680, Fallow's light (docs/FALLOW_0_4_680.md): what it leaves of the station. At the light the plate glass blows in out of its
 * frames. Then, while anyone is below to hear it, the rest comes down a part at a time: the canopy over the pumps, its sheets on the
 * concrete and its columns lying beside their feet; the store's roof, into the store; its side and back walls, the yard side into the
 * yard; and last the fall of it across the yard onto the cellar doors. With nobody below, all of it comes down at once when the canopy
 * goes, after the readers out in the light have gone. The front of the store stands, toward the light, with its sign; so does the
 * House's door in the back wall.
 *
 * <p>Nothing here is put where a body stands. A fresh afternoon builds the station again from its own plans (FallowSite.mend), which no
 * chance enters, so the station is as it was.
 */
public final class FallowRuin {
    private FallowRuin(){}
    /** Ticks after the light: the plate glass, the canopy, the store's roof, its walls, the cellar doors covered. */
    public static final int GLASS=0,CANOPY=40,ROOF=60,WALLS=90,DOORS=120;
    private static final int[] AT={GLASS,CANOPY,ROOF,WALLS,DOORS};
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    /** At so many ticks after the light, whatever is due comes down (State.ruined keeps which parts have). */
    static void tick(ServerLevel l,Fallow.Site s,List<ServerPlayer> here,int k){
        var st=s.strike;boolean below=false;
        for(var p:here)if(!p.isSpectator()&&FallowSite.inCellar(FallowStrike.rel(s,p)))below=true;
        for(int i=0;i<AT.length;i++){
            if((st.ruined&1<<i)!=0||k<AT[i]&&(below||k<CANOPY))continue;
            st.ruined|=1<<i;
            switch(i){
                case 0->{glass(l,s.base);ledger(l,s.base);}
                case 1->{canopy(l,s.base);heard(l,s,here,FallowRegistry.CANOPY.get(),.45F,FallowTexts.RUIN_CANOPY);}
                case 2->{roof(l,s.base);heard(l,s,here,FallowRegistry.COLLAPSE.get(),.5F,FallowTexts.RUIN_STORE);}
                case 3->{walls(l,s.base);heard(l,s,here,FallowRegistry.COLLAPSE.get(),.35F,null);}
                default->{doors(l,s.base);heard(l,s,here,FallowRegistry.DEBRIS.get(),1F,FallowTexts.RUIN_DOORS);}
            }
        }
    }
    /** Each part heard from below, muffled, and told; the world's own sound for whoever else is about (spectators). */
    private static void heard(ServerLevel l,Fallow.Site s,List<ServerPlayer> here,SoundEvent e,float volume,String cue){
        for(var p:here){
            if(p.isSpectator()||!FallowSite.inCellar(FallowStrike.rel(s,p)))continue;
            FallowStrike.heard(p,e,volume);if(cue!=null)Fallow.cue(p,cue);
        }
    }

    // ------------------------------------------------------------------------------------------------ the parts
    /** The plate glass, front and side, blown in to the frames' edges. */
    static void glass(ServerLevel l,BlockPos b){
        for(int x=-6;x<=6;x++){if(Math.abs(x)<2)continue;for(int y=1;y<=2;y++)blow(l,b,new BlockPos(x,y,STORE_N));}
        for(int z:new int[]{-9,-10})for(int y=1;y<=2;y++)blow(l,b,new BlockPos(STORE_W,y,z));
        sound(l,b,new BlockPos(0,2,STORE_N),SoundEvents.GLASS_BREAK,2F,.8F);sound(l,b,new BlockPos(STORE_W,2,-9),SoundEvents.GLASS_BREAK,1.5F,.9F);
    }
    /** 0.4.684: the till end of the counter, its top scorched through the glass, and on the shelf beneath it the keeper's ledger, its
     * edges singed (counter stage 4), where the account is. The afternoon's plans put the till end back (FallowSite.store). */
    static void ledger(ServerLevel l,BlockPos b){
        var p=b.offset(ENDING);if(!l.isLoaded(p))return;var s=l.getBlockState(p);
        if(!FallowBlock.is(s,FallowBlock.Kind.COUNTER)||s.getValue(FallowBlock.STAGE)==FallowBlock.LEDGER)return;
        BuildBlocks.set(l,p,s.setValue(FallowBlock.STAGE,FallowBlock.LEDGER),F);
    }
    private static void blow(ServerLevel l,BlockPos b,BlockPos at){
        var p=b.offset(at);if(!l.isLoaded(p))return;var s=l.getBlockState(p);
        if(!FallowBlock.is(s,FallowBlock.Kind.WINDOW)||s.getValue(FallowBlock.STAGE)>=FallowBlock.BLOWN_LOW)return;
        BuildBlocks.set(l,p,s.setValue(FallowBlock.STAGE,s.getValue(FallowBlock.STAGE)+2),F);
        l.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,Blocks.GLASS.defaultBlockState()),p.getX()+.5,p.getY()+.5,p.getZ()+.5,10,.4,.4,.2,.08);
    }
    /** The canopy: its deck gone off its columns onto the concrete in sheets, buckled, a sheet leaning on the island where it caught,
     * its columns broken off at their feet and lying beside them. */
    static void canopy(ServerLevel l,BlockPos b){
        for(int x=CANOPY_W;x<=CANOPY_E;x++)for(int z=CANOPY_N;z<=CANOPY_S;z++)clear(l,b,x,CANOPY_Y,z);
        int[][] feet={{CANOPY_W+1,CANOPY_N+1},{CANOPY_E-1,CANOPY_N+1},{CANOPY_W+1,CANOPY_S-1},{CANOPY_E-1,CANOPY_S-1}};
        for(var c:feet){
            for(int y=1;y<CANOPY_Y;y++)clear(l,b,c[0],y,c[1]);
            // Each lies away from the pumps, the west pair westward and the east pair eastward, its collar at its far end.
            boolean west=c[0]<0;put(l,b,c[0]+(west?-1:1),0,c[1],FallowBlock.of(FallowBlock.Kind.RUBBLE,west?Direction.SOUTH:Direction.NORTH,FallowBlock.COLUMN));
        }
        for(int x=CANOPY_W;x<=CANOPY_E;x++)for(int z=CANOPY_N;z<=CANOPY_S;z++){
            double h=hash(x,z,1);
            if(Math.abs(z-ISLAND_Z)==1&&x>=-3&&x<=2&&h<.7){put(l,b,x,0,z,FallowBlock.of(FallowBlock.Kind.RUBBLE,z<ISLAND_Z?Direction.SOUTH:Direction.NORTH,FallowBlock.LEANING));continue;}
            if(h<.62)put(l,b,x,0,z,FallowBlock.of(FallowBlock.Kind.RUBBLE,turn(x,z),FallowBlock.SHEET));
            else if(h<.74)put(l,b,x,0,z,FallowBlock.of(FallowBlock.Kind.RUBBLE,turn(x,z),FallowBlock.SPREAD));
        }
        dust(l,b,new Vec3(-.5,1,-19),6,4);
        sound(l,b,new BlockPos(0,3,ISLAND_Z),FallowRegistry.CANOPY.get(),2.5F,1F);
    }
    /** The store's roof gone into the store, a lip of the ceiling left hanging here and there at the walls, its lights down with it;
     * the floor deep in it, all but the way from the House's door; the garage's roof the same, over the car on its lift. */
    static void roof(ServerLevel l,BlockPos b){
        for(int x=STORE_W+1;x<=STORE_E-1;x++)for(int z=STORE_N+1;z<=STORE_S-1;z++){
            boolean edge=x==STORE_W+1||x==STORE_E-1||z==STORE_N+1||z==STORE_S-1;
            if(edge&&hash(x,z,2)<.4)continue;
            clear(l,b,x,CEILING,z);clear(l,b,x,CEILING+1,z);
        }
        for(int[] at:new int[][]{{-4,-5},{4,-5},{-4,-9},{4,-9}})clear(l,b,at[0],CEILING-1,at[1]);
        for(int x=STORE_W+1;x<=STORE_E-1;x++)for(int z=STORE_N+1;z<=STORE_S-1;z++){
            if(Math.abs(x)<=1&&z>=-2)continue;
            double h=hash(x,z,3);
            int stage=h<.3?FallowBlock.HEAP:h<.72?FallowBlock.SPREAD:h<.84?FallowBlock.JOIST:-1;
            if(stage>=0)put(l,b,x,0,z,FallowBlock.of(FallowBlock.Kind.RUBBLE,turn(x,z),stage));
        }
        for(int x=STORE_E+1;x<=GARAGE_E-1;x++)for(int z=STORE_N+1;z<=STORE_S-1;z++){
            boolean edge=x==STORE_E+1||x==GARAGE_E-1||z==STORE_N+1||z==STORE_S-1;
            if(!(edge&&hash(x,z,4)<.4))clear(l,b,x,GARAGE_TOP,z);
            double h=hash(x,z,5);
            if(h<.45)put(l,b,x,0,z,FallowBlock.of(FallowBlock.Kind.RUBBLE,turn(x,z),FallowBlock.SPREAD));
            else if(h<.55)put(l,b,x,0,z,FallowBlock.of(FallowBlock.Kind.RUBBLE,turn(x,z),FallowBlock.JOIST));
        }
        for(int[] at:new int[][]{{12,-3},{12,-9},{16,-3},{16,-9}})clear(l,b,at[0],GARAGE_TOP-1,at[1]);
        dust(l,b,new Vec3(.5,3,-6),8,6);
        sound(l,b,new BlockPos(0,3,-6),FallowRegistry.COLLAPSE.get(),2.5F,1F);
    }
    /** The side and back walls broken off raggedly a course or two above the plinth, the yard side lying out in the yard where it
     * fell, its boards still lapped; the House's door and the wall either side of it stand, as does the front. */
    static void walls(ServerLevel l,BlockPos b){
        for(int z=STORE_N+1;z<=STORE_S;z++){
            int keep=2+(int)(hash(STORE_W,z,6)*3);
            for(int y=keep;y<=CEILING+1;y++)clear(l,b,STORE_W,y,z);
        }
        for(int x=STORE_W+1;x<=STORE_E-1;x++){
            if(Math.abs(x)<=2)continue;
            int keep=2+(int)(hash(x,STORE_S,7)*3);
            for(int y=keep;y<=CEILING+1;y++)clear(l,b,x,y,STORE_S);
        }
        for(int z=STORE_N+1;z<=STORE_S-1;z++){
            double h=hash(STORE_W-1,z,8);
            if(h<.55)put(l,b,STORE_W-1,0,z,FallowBlock.of(FallowBlock.Kind.RUBBLE,Direction.WEST,FallowBlock.WALL));
            else if(h<.8)put(l,b,STORE_W-1,0,z,FallowBlock.of(FallowBlock.Kind.RUBBLE,turn(STORE_W-1,z),FallowBlock.HEAP));
        }
        dust(l,b,new Vec3(STORE_W-.5,2,-6),6,5);
        sound(l,b,new BlockPos(STORE_W,2,-6),FallowRegistry.COLLAPSE.get(),2F,.85F);
    }
    /** The fall of it across the yard and onto the cellar doors: the leaves under it, and the yard round them heaped. */
    static void doors(ServerLevel l,BlockPos b){
        for(int z=WELL_S;z>=WELL_N;z--){
            cover(l,b,WELL_W,z,Direction.NORTH);cover(l,b,WELL_E,z,Direction.SOUTH);
        }
        for(int x=WELL_W-2;x<=WELL_E+2;x++)for(int z=WELL_N-1;z<=WELL_S+1;z++){
            if(x>=WELL_W&&x<=WELL_E&&z<=WELL_S&&z>=WELL_N)continue;
            double h=hash(x,z,9);
            if(h<.45)put(l,b,x,0,z,FallowBlock.of(FallowBlock.Kind.RUBBLE,turn(x,z),FallowBlock.HEAP));
            else if(h<.8)put(l,b,x,0,z,FallowBlock.of(FallowBlock.Kind.RUBBLE,turn(x,z),FallowBlock.SPREAD));
        }
        dust(l,b,new Vec3(-13,1,-9.5),3,4);
        sound(l,b,new BlockPos(-13,1,-9),FallowRegistry.DEBRIS.get(),1.5F,1F);
    }
    /** 0.4.681: in the buried days, a length of the doors a head on the steps kept clear when the fall came down is covered once nobody
     * is under it (FallowBuried). */
    static void recover(ServerLevel l,BlockPos b){
        for(int z=WELL_S;z>=WELL_N;z--){cover(l,b,WELL_W,z,Direction.NORTH);cover(l,b,WELL_E,z,Direction.SOUTH);}
    }
    /** A leaf of the cellar doors, shut or thrown back, with the fall of the store on it; its curb on its own side. */
    private static void cover(ServerLevel l,BlockPos b,int x,int z,Direction facing){
        var p=b.offset(x,0,z);if(!l.isLoaded(p))return;var s=l.getBlockState(p);
        if(!(s.isAir()||FallowBlock.is(s,FallowBlock.Kind.CELLAR)||FallowBlock.is(s,FallowBlock.Kind.CELLAR_OPEN)))return;
        // Left open, the doors' well has nothing over it; a head come up the steps into it keeps it clear.
        if(!l.getEntitiesOfClass(LivingEntity.class,new AABB(p),e->e.isAlive()&&!e.isSpectator()).isEmpty())return;
        BuildBlocks.set(l,p,FallowBlock.of(FallowBlock.Kind.RUBBLE,facing,FallowBlock.ON_DOORS),F);
    }

    // ------------------------------------------------------------------------------------------------ the shadows
    /** The front's solid columns, store and garage, where a shadow can lie whole: either side of the front door, the corners, the
     * jambs of the bay. The plate glass between them is gone, and nothing is thrown onto an empty frame. */
    public static final int[] WALL={-8,-7,-1,1,7,8,9,17,18};
    /** Nobody's shadow on the front. */
    public static final int NO_SHADOW=Integer.MIN_VALUE;
    /** A reader out in front of the station when the light comes: the light, from up the road, throws their shadow straight back onto
     * the front, and it stays there. It lies on the solid column nearest where they stood that nobody else's shadow has taken, in the
     * body they wore and the stance it caught them in. A reader to either side of the front, or behind it, throws theirs onto nothing.
     * Returns the column, or {@link #NO_SHADOW}. */
    static int shadow(ServerLevel l,BlockPos b,ServerPlayer p,Vec3 r){
        if(r.z>=STORE_N||r.x<STORE_W-.5||r.x>GARAGE_E+1.5)return NO_SHADOW;
        int best=NO_SHADOW;double near=Double.MAX_VALUE;
        for(int x:WALL){
            var at=b.offset(x,0,STORE_N-1);
            if(!l.isLoaded(at)||!l.getBlockState(at).isAir())continue;
            double d=Math.abs(x+.5-r.x);if(d<near){near=d;best=x;}
        }
        if(best==NO_SHADOW)return NO_SHADOW;
        var at=b.offset(best,0,STORE_N-1);
        BuildBlocks.set(l,at,FallowShadowBlock.facing(Direction.NORTH),Block.UPDATE_CLIENTS);
        if(!(l.getBlockEntity(at) instanceof FallowShadowBlock.Shadow sh))return NO_SHADOW;
        var body=io.github.knaitoe.theoldesthouse.house.HouseConfig.houseBodies()?HouseBodies.body(LabyrinthData.get(l.getServer()),p.getUUID()):null;
        // How far into a step: a walk is about a fifth of a block a tick, a run a little over a quarter.
        float stride=(float)Math.min(1,Math.hypot(p.getX()-p.xo,p.getZ()-p.zo)/.28);
        float phase=(float)(l.getGameTime()*.45%(Math.PI*2));
        sh.cast(p.getUUID(),p.getGameProfile().getName(),body,p.yBodyRot,stride,phase,p.isCrouching());
        return best;
    }

    /** 0.4.681: one of the station's people out in front of it at the light (Boyd at the rig's door): their shadow on the nearest solid
     * column nobody's has taken, as a reader's is, in a plain body, an arm up if they were reaching. */
    static int shadow(ServerLevel l,BlockPos b,LiteraryActor a,Vec3 r,String name,boolean reach){
        if(r.z>=STORE_N||r.x<STORE_W-.5||r.x>GARAGE_E+1.5)return NO_SHADOW;
        int best=NO_SHADOW;double near=Double.MAX_VALUE;
        for(int x:WALL){
            var at=b.offset(x,0,STORE_N-1);
            if(!l.isLoaded(at)||!l.getBlockState(at).isAir())continue;
            double d=Math.abs(x+.5-r.x);if(d<near){near=d;best=x;}
        }
        if(best==NO_SHADOW)return NO_SHADOW;
        var at=b.offset(best,0,STORE_N-1);
        BuildBlocks.set(l,at,FallowShadowBlock.facing(Direction.NORTH),Block.UPDATE_CLIENTS);
        if(!(l.getBlockEntity(at) instanceof FallowShadowBlock.Shadow sh))return NO_SHADOW;
        sh.castOf(a.getUUID(),name,a.yBodyRot,reach);
        return best;
    }

    // ------------------------------------------------------------------------------------------------ helpers
    /** Whether the light has had the station: the canopy off its columns, or the plate glass gone. */
    public static boolean ruined(ServerLevel l,BlockPos b){
        var deck=b.offset(0,CANOPY_Y,ISLAND_Z);var pane=b.offset(-6,1,STORE_N);
        if(!l.isLoaded(deck)||!l.isLoaded(pane))return false;
        var w=l.getBlockState(pane);
        return !FallowBlock.is(l.getBlockState(deck),FallowBlock.Kind.CANOPY)||FallowBlock.is(w,FallowBlock.Kind.WINDOW)&&w.getValue(FallowBlock.STAGE)>=FallowBlock.BLOWN_LOW;
    }
    /** A fresh afternoon: a station the light has had is built again from its plans, once all of it is loaded. */
    static void mend(ServerLevel l,BlockPos b){
        // 0.4.682: the outside first, wherever it is loaded (FallowAsh).
        FallowAsh.mend(l,b);
        if(!ruined(l,b))return;
        for(int x:new int[]{YARD_W,APRON_E})for(int z:new int[]{0,APRON_N})if(!l.isLoaded(b.offset(x,0,z)))return;
        FallowSite.mend(l,b);
    }
    /** A station still fallen when an afternoon has begun (its ground was not all loaded when the afternoon was made fresh) is built
     * again as soon as it can be, before the light and only while nobody stands on any of it. */
    static void leftover(ServerLevel l,Fallow.Site s,List<ServerPlayer> here){
        if(s.strike.ruined!=0||s.strike.toneAt>=0||!ruined(l,s.base))return;
        for(var p:here){
            var r=FallowStrike.rel(s,p);
            if(!p.isSpectator()&&r.x>YARD_W-1&&r.x<APRON_E+2&&r.z>APRON_N-1&&r.z<.5&&r.y>CELLAR_FLOOR-1&&r.y<CANOPY_Y+4)return;
        }
        mend(l,s.base);
    }
    private static void clear(ServerLevel l,BlockPos b,int x,int y,int z){
        var p=b.offset(x,y,z);if(!l.isLoaded(p))return;var s=l.getBlockState(p);
        if(s.isAir())return;
        if(l.random.nextInt(3)==0)l.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,s),p.getX()+.5,p.getY()+.5,p.getZ()+.5,4,.3,.3,.3,.05);
        BuildBlocks.set(l,p,Blocks.AIR.defaultBlockState(),F);
    }
    /** Rubble put only where there is nothing, and never where a body stands. */
    private static void put(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){
        var p=b.offset(x,y,z);if(!l.isLoaded(p)||!l.getBlockState(p).isAir())return;
        if(!l.getEntitiesOfClass(LivingEntity.class,new AABB(p),e->e.isAlive()&&!e.isSpectator()).isEmpty())return;
        BuildBlocks.set(l,p,s,F);
    }
    private static void dust(ServerLevel l,BlockPos b,Vec3 at,double wide,double high){
        var c=Vec3.atLowerCornerOf(b).add(at);
        l.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,c.x,c.y,c.z,18,wide/2,high/2,wide/2,.01);
        l.sendParticles(ParticleTypes.WHITE_ASH,c.x,c.y,c.z,40,wide/2,high/2,wide/2,.02);
    }
    private static void sound(ServerLevel l,BlockPos b,BlockPos at,SoundEvent e,float volume,float pitch){
        var p=b.offset(at);l.playSound(null,p,e,SoundSource.BLOCKS,volume,pitch);
    }
    /** Which way a piece lies, by its place, never by chance, so the fall is the same each time. */
    private static Direction turn(int x,int z){return Direction.from2DDataValue((int)(hash(x,z,11)*4));}
    static double hash(int a,int b,int salt){
        long h=a*73856093L^b*19349663L^salt*83492791L;h^=h>>>13;h*=0x5bd1e995L;h^=h>>>15;h*=0x2545F4914F6CDD1DL;h^=h>>>29;
        return (h&0xffffff)/(double)0x1000000;
    }
}
