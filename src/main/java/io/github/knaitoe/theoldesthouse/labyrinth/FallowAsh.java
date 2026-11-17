package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import static io.github.knaitoe.theoldesthouse.labyrinth.FallowSite.*;

/**
 * 0.4.682, Fallow 4b (docs/FALLOW_0_4_682.md): what the light leaves outside. Once the readers out in it have gone, the field burns a few
 * tiles a tick, from the station outward in the order the tiles are kept: the wheat to stubble and to bare ash, the soil under it grey with
 * ash. Where things came down hard the ground is opened, a handful of craters burnt black at the bottom. The telephone poles go down
 * across the road away from the light, some broken off at their feet, their wires gone, the last length of each with its crossarm stood on
 * end out of the ground. The farmhouse burns to its stone sill: the chimney stands with its top broken off, and the front door stands in a
 * charred scrap of its frame, still the way on; on the floor, burnt beams and the ash of the rest. The windbreak burns standing, its leaves
 * gone and its trunks broken; the yard round the house is burnt black.
 *
 * <p>Whatever is not loaded waits until it is. Nothing is put where a body stands. A fresh afternoon puts all of it back from its plans,
 * which no chance enters (FallowSite.wheatAt, FallowSite.FARM_SEED), where it is loaded; what was not is put back during the next
 * afternoon, before the light, out of sight of everyone at the station.
 */
public final class FallowAsh {
    private FallowAsh(){}
    /** Ticks after the light: the field begins to burn once the readers out in it have gone. */
    public static final int START=FallowStrike.OUT_FAINT+5,CRATERS_AT=START+10,POLES_AT=START+20,FARM_AT=START+30;
    /** The field in tiles of sixteen by sixteen, counted from its west edge along and from the road's side of the scene northward. */
    static final int TILE=16,ACROSS=(EAST-WEST)/TILE+1,DOWN=-NORTH/TILE+1,TILES=ACROSS*DOWN,PER_TICK=2;
    /** Where things came down hard enough to open the ground: the middle, and how far out it is opened. Clear of the road, the station, the
     * pads and the farmhouse's yard. */
    public static final int[][] CRATERS={{34,-52,3},{-44,-104,3},{6,-74,2},{-8,-122,3},{44,-90,2}};
    /** The telephone poles along the road (FallowSite.poles). */
    public static final int[] POLE_X={-48,-32,-16,0,16,32,48};
    /** The farmhouse's yard, cleared to grass when it was built (FallowSite.farm), and the windbreak along its road side. */
    static final int LOT_W=-42,LOT_E=-14,LOT_S=WINDBREAK_Z-1,LOT_N=FARM_N-4;
    /** How near anyone at the station may be to a thing for it not to be put back before their eyes: the haze is whole by then. */
    static final double UNSEEN=48;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    // ------------------------------------------------------------------------------------------------ the burning
    /** So many ticks after the light: whatever of the outside is due and loaded goes. */
    static void tick(ServerLevel l,Fallow.Site s,int k){
        if(k<START)return;
        var st=s.strike;int burnt=0;
        for(int i=st.ash.nextClearBit(0);i<TILES&&burnt<PER_TICK;i=st.ash.nextClearBit(i+1)){
            if(!loaded(l,s.base,i))continue;
            field(l,s.base,i);st.ash.set(i);burnt++;
        }
        if(!st.cratered&&k>=CRATERS_AT&&craterLoaded(l,s.base)){craters(l,s.base);st.cratered=true;}
        if(!st.polesDown&&k>=POLES_AT&&polesLoaded(l,s.base)){poles(l,s.base);st.polesDown=true;}
        if(!st.farmGone&&k>=FARM_AT&&farmLoaded(l,s.base)){farm(l,s.base);st.farmGone=true;}
    }
    /** One tile of the field: the standing wheat burnt to stubble, or to nothing, and the soil under it grey with ash. */
    static void field(ServerLevel l,BlockPos b,int tile){
        int x0=tileX(tile),z0=tileZ(tile);
        for(int x=x0;x<Math.min(EAST+1,x0+TILE);x++)for(int z=z0;z>Math.max(NORTH-1,z0-TILE);z--){
            var top=b.offset(x,0,z);var st=l.getBlockState(top);
            if(!FallowBlock.is(st,FallowBlock.Kind.WHEAT)||st.getValue(FallowBlock.STAGE)>=FallowBlock.STUBBLE)continue;
            var under=top.below();if(!l.getBlockState(under).is(FallowRegistry.material("soil")))continue;
            BuildBlocks.set(l,under,mat("ash_ground"),F);
            BuildBlocks.set(l,top,FallowRuin.hash(x,z,41)<.55?st.setValue(FallowBlock.STAGE,FallowBlock.ASH):AIR,F);
        }
        var c=Vec3.atLowerCornerOf(b).add(x0+TILE/2.0,1,z0-TILE/2.0);
        l.sendParticles(ParticleTypes.LARGE_SMOKE,c.x,c.y,c.z,6,TILE/3.0,.5,TILE/3.0,.01);
        l.sendParticles(ParticleTypes.WHITE_ASH,c.x,c.y+1,c.z,24,TILE/2.5,1,TILE/2.5,.02);
    }
    /** The ground opened where things came down: a bowl three deep at the middle, burnt black at the bottom and round its lip. */
    static void craters(ServerLevel l,BlockPos b){
        for(var c:CRATERS){
            int cx=c[0],cz=c[1],r=c[2];
            for(int x=cx-r-2;x<=cx+r+2;x++)for(int z=cz-r-2;z<=cz+r+2;z++){
                double d=Math.hypot(x-cx,z-cz);
                if(d>r+1.8||occupied(l,b,x,-4,z,2))continue;
                set(l,b,x,0,z,AIR);
                if(d<=r+.5){
                    int depth=depth(d,r);
                    for(int y=-1;y>=-depth;y--)set(l,b,x,y,z,AIR);
                    set(l,b,x,-depth-1,z,mat("scorched"));
                }else set(l,b,x,-1,z,mat("scorched"));
            }
            var at=Vec3.atLowerCornerOf(b).add(cx+.5,0,cz+.5);
            l.sendParticles(ParticleTypes.LARGE_SMOKE,at.x,at.y,at.z,10,r,.6,r,.01);
        }
    }
    static int depth(double d,int r){return d<r*.45?3:d<r*.8?2:1;}
    /** The poles down away from the light, across the road, a few broken off at their feet; their wires gone. */
    static void poles(ServerLevel l,BlockPos b){
        for(int x=WEST;x<=EAST;x++){
            var at=b.offset(x,POLE_TOP,POLES_Z);var st=l.getBlockState(at);
            if(FallowBlock.is(st,FallowBlock.Kind.POLE)&&st.getValue(FallowBlock.STAGE)>=2&&st.getValue(FallowBlock.STAGE)<=4)BuildBlocks.set(l,at,AIR,F);
        }
        for(int x:POLE_X){
            boolean snapped=snapped(x);
            for(int y=snapped?1:0;y<=POLE_TOP;y++)clear(l,b,x,y,POLES_Z);
            for(int z=snapped?POLES_Z+1:POLES_Z;z<POLES_Z+6;z++)put(l,b,x,0,z,FallowBlock.of(FallowBlock.Kind.POLE,Direction.SOUTH,FallowBlock.POLE_DOWN));
            put(l,b,x,0,POLES_Z+6,FallowBlock.of(FallowBlock.Kind.POLE,Direction.SOUTH,FallowBlock.POLE_DOWN_TOP));
            var at=Vec3.atLowerCornerOf(b).add(x+.5,1,POLES_Z+3.5);
            l.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,at.x,at.y,at.z,4,.3,.3,3,.01);
        }
    }
    /** Which poles broke off at their feet and left a stump standing. */
    public static boolean snapped(int x){return FallowRuin.hash(x,0,43)<.4;}
    /** The farmhouse burnt to its sill: the chimney standing, its top broken off; the front door in a charred scrap of its frame; the
     * floor burnt where it lay, with beams and the ash of the rest on it, clear of the way to the door. The windbreak burnt standing and
     * broken; the yard burnt black. */
    static void farm(ServerLevel l,BlockPos b){
        int mid=(FARM_S+FARM_N)/2;
        for(int x=FARM_W-1;x<=FARM_E+1;x++)for(int z=FARM_N-1;z<=FARM_S+2;z++)for(int y=0;y<=14;y++){
            if(door(x,y,z))continue;
            boolean sill=y==0&&x>=FARM_W&&x<=FARM_E&&z>=FARM_N&&z<=FARM_S&&(x==FARM_W||x==FARM_E||z==FARM_N||z==FARM_S);
            if(!sill)clear(l,b,x,y,z);
        }
        for(int y=0;y<=CHIMNEY_LEFT;y++)set(l,b,FARM_E-3,y,mid,mat("brick"));
        // The door's frame: a charred jamb on either side above the sill, each on the door's side of its block, and the lintel across
        // the top of the door, its ends on the jambs.
        // In the door's own plane, along the north edge of the blocks (where a closed door facing south lies). The farm stages face
        // north unturned: a jamb faced north stands in its block's north-east corner, one faced west in its north-west corner.
        set(l,b,FARM_DRIVE_X-1,1,FARM_S,FallowBlock.of(FallowBlock.Kind.FARM,Direction.NORTH,FallowBlock.JAMB));
        set(l,b,FARM_DRIVE_X+1,1,FARM_S,FallowBlock.of(FallowBlock.Kind.FARM,Direction.WEST,FallowBlock.JAMB));
        set(l,b,FARM_DRIVE_X,2,FARM_S,FallowBlock.of(FallowBlock.Kind.FARM,Direction.NORTH,FallowBlock.LINTEL));
        for(int x=FARM_W;x<=FARM_E;x++)for(int z=FARM_N;z<=FARM_S+2;z++){
            var at=b.offset(x,-1,z);if(l.isLoaded(at)&&l.getBlockState(at).is(FallowRegistry.material("porch")))BuildBlocks.set(l,at,mat("charred"),F);
        }
        for(int x=FARM_W+1;x<=FARM_E-1;x++)for(int z=FARM_N+1;z<=FARM_S-1;z++){
            if(Math.abs(x-FARM_DRIVE_X)<=1&&z>=FARM_S-2||x==FARM_E-3&&z==mid)continue;
            double h=FallowRuin.hash(x,z,44);var turn=Direction.from2DDataValue((int)(FallowRuin.hash(x,z,45)*4));
            if(h<.16)put(l,b,x,0,z,FallowBlock.of(FallowBlock.Kind.FARM,turn,FallowBlock.BEAM));
            else if(h<.34)put(l,b,x,0,z,FallowBlock.of(FallowBlock.Kind.FARM,turn,FallowBlock.BURNT));
        }
        // The windbreak: every leaf gone, each trunk charred and broken off at its own height.
        for(int x=LOT_W;x<=LOT_E;x++)for(int z=WINDBREAK_Z-2;z<=WINDBREAK_Z+2;z++)for(int y=0;y<=9;y++){
            var at=b.offset(x,y,z);if(!l.isLoaded(at))continue;var st=l.getBlockState(at);
            if(st.is(Blocks.OAK_LEAVES))BuildBlocks.set(l,at,AIR,F);
            else if(st.is(Blocks.OAK_LOG))BuildBlocks.set(l,at,y>=2+(int)(FallowRuin.hash(x,0,46)*4)?AIR:mat("charred_log"),F);
        }
        for(int x=LOT_W;x<=LOT_E;x++)for(int z=LOT_N;z<=LOT_S;z++){
            var at=b.offset(x,-1,z);if(l.isLoaded(at)&&l.getBlockState(at).is(Blocks.GRASS_BLOCK))BuildBlocks.set(l,at,mat("scorched"),F);
        }
        var c=Vec3.atLowerCornerOf(b).add((FARM_W+FARM_E)/2.0,2,mid);
        l.sendParticles(ParticleTypes.LARGE_SMOKE,c.x,c.y,c.z,30,6,2,4,.02);
        l.sendParticles(ParticleTypes.WHITE_ASH,c.x,c.y+2,c.z,60,8,3,6,.02);
    }
    /** How high the chimney stands once the house round it is gone: its top broken off. */
    public static final int CHIMNEY_LEFT=10;
    /** The front door's two halves, which the fire leaves alone: it is the way on. */
    static boolean door(int x,int y,int z){return x==FARM_DRIVE_X&&z==FARM_S&&(y==0||y==1);}

    // ------------------------------------------------------------------------------------------------ put back
    /** A fresh afternoon: whatever of the outside the light had is put back from its plans, wherever it is loaded. */
    static void mend(ServerLevel l,BlockPos b){
        for(int i=0;i<TILES;i++)if(loaded(l,b,i))mendField(l,b,i);
        if(craterLoaded(l,b))mendCraters(l,b);
        if(polesLoaded(l,b))mendPoles(l,b);
        if(farmLoaded(l,b))mendFarm(l,b);
    }
    /** An outside left burnt when its afternoon was made fresh (some of it was not loaded) is put back during the next one, before the
     * light, a little at a time, and only where nobody at the station is near enough to see it change. */
    static void leftover(ServerLevel l,Fallow.Site s,List<ServerPlayer> here){
        var st=s.strike;if(st.toneAt>=0)return;
        int looked=0;
        for(int i=st.clean.nextClearBit(0);i<TILES&&looked<4;i=st.clean.nextClearBit(i+1)){
            if(!loaded(l,s.base,i))continue;
            var c=s.base.offset(tileX(i)+TILE/2,0,tileZ(i)-TILE/2);
            if(seen(here,c,UNSEEN+TILE*.71))continue;
            mendField(l,s.base,i);st.clean.set(i);looked++;
        }
        if(st.clean.cardinality()<TILES)return;
        if(!st.cleanOutside){
            boolean craters=craterLoaded(l,s.base),poles=polesLoaded(l,s.base),farm=farmLoaded(l,s.base);
            if(craters&&!seenAny(here,s.base,CRATERS))mendCraters(l,s.base);
            if(poles&&!seen(here,s.base.offset(0,0,POLES_Z),UNSEEN+EAST))mendPoles(l,s.base);
            if(farm&&!seen(here,s.base.offset((FARM_W+FARM_E)/2,0,(FARM_S+FARM_N)/2),UNSEEN+16))mendFarm(l,s.base);
            st.cleanOutside=craters&&poles&&farm&&!burnt(l,s.base);
        }
    }
    /** The field's tile as it was sown: the soil under the ash, the wheat as it stood. */
    static void mendField(ServerLevel l,BlockPos b,int tile){
        int x0=tileX(tile),z0=tileZ(tile);
        for(int x=x0;x<Math.min(EAST+1,x0+TILE);x++)for(int z=z0;z>Math.max(NORTH-1,z0-TILE);z--){
            var under=b.offset(x,-1,z);var top=under.above();
            var ground=l.getBlockState(under);var st=l.getBlockState(top);
            boolean burnt=FallowBlock.is(st,FallowBlock.Kind.WHEAT)&&st.getValue(FallowBlock.STAGE)>=FallowBlock.STUBBLE;
            if(ground.is(FallowRegistry.material("ash_ground"))){
                BuildBlocks.set(l,under,mat("soil"),F);
                if(st.isAir()||burnt)BuildBlocks.set(l,top,wheatAt(x,z),F);
            }else if(burnt)BuildBlocks.set(l,top,ground.is(FallowRegistry.material("yard"))?AIR:wheatAt(x,z),F);
            // 0.4.683: what came up round Opal's bean in the yard (FallowRoad.green) is not the field's: the yard is bare again.
        }
    }
    /** The opened ground filled again as it was laid: earth, the field's soil, the wheat on it. */
    static void mendCraters(ServerLevel l,BlockPos b){
        for(var c:CRATERS){
            int cx=c[0],cz=c[1],r=c[2];
            if(l.getBlockState(b.offset(cx,-1,cz)).is(FallowRegistry.material("soil")))continue;
            for(int x=cx-r-2;x<=cx+r+2;x++)for(int z=cz-r-2;z<=cz+r+2;z++){
                if(Math.hypot(x-cx,z-cz)>r+1.8)continue;
                for(int y=-4;y<=-2;y++)set(l,b,x,y,z,Blocks.DIRT.defaultBlockState());
                set(l,b,x,-1,z,mat("soil"));set(l,b,x,0,z,wheatAt(x,z));
            }
        }
    }
    /** The poles stood up again along the road and strung, and what lay across the road taken up. */
    static void mendPoles(ServerLevel l,BlockPos b){
        boolean down=false;
        for(int x:POLE_X)for(int z=POLES_Z;z<=POLES_Z+6;z++){
            var at=b.offset(x,0,z);var st=l.getBlockState(at);
            if(FallowBlock.is(st,FallowBlock.Kind.POLE)&&st.getValue(FallowBlock.STAGE)>=FallowBlock.POLE_DOWN){BuildBlocks.set(l,at,AIR,F);down=true;}
        }
        for(int x:POLE_X)if(!FallowBlock.is(l.getBlockState(b.offset(x,1,POLES_Z)),FallowBlock.Kind.POLE))down=true;
        if(down)FallowSite.poles(l,b);
    }
    /** The farmhouse built again from its plans, its windbreak grown again from the same seed, and its front door, which the fire left,
     * set back in its wall as it stood. */
    static void mendFarm(ServerLevel l,BlockPos b){
        if(!farmBurnt(l,b))return;
        var lower=b.offset(FARM_DRIVE_X,0,FARM_S);var below=l.getBlockState(lower);var above=l.getBlockState(lower.above());
        FallowSite.farm(l,b,new Random(FARM_SEED));
        if(below.getBlock() instanceof DoorBlock&&above.getBlock() instanceof DoorBlock){BuildBlocks.set(l,lower,below,F);BuildBlocks.set(l,lower.above(),above,F);}
    }
    /** The chimney standing to the ground is the farmhouse burnt: in the house as built, its foot is the floor upstairs. */
    static boolean farmBurnt(ServerLevel l,BlockPos b){return l.getBlockState(b.offset(FARM_E-3,0,(FARM_S+FARM_N)/2)).is(FallowRegistry.material("brick"));}
    /** Whether any of the outside is still as the light left it, among what is loaded: for tests and for the leftover pass. */
    public static boolean burnt(ServerLevel l,BlockPos b){
        if(farmLoaded(l,b)&&farmBurnt(l,b))return true;
        for(int x:POLE_X){var at=b.offset(x,0,POLES_Z+6);if(l.isLoaded(at)&&FallowBlock.is(l.getBlockState(at),FallowBlock.Kind.POLE))return true;}
        for(var c:CRATERS){var at=b.offset(c[0],-1,c[1]);if(l.isLoaded(at)&&!l.getBlockState(at).is(FallowRegistry.material("soil")))return true;}
        return false;
    }

    // ------------------------------------------------------------------------------------------------ where
    static int tileX(int tile){return WEST+tile%ACROSS*TILE;}
    /** The tile's southmost row; it runs north from there. */
    static int tileZ(int tile){return -tile/ACROSS*TILE;}
    static boolean loaded(ServerLevel l,BlockPos b,int tile){
        int x0=tileX(tile),z0=tileZ(tile),x1=Math.min(EAST,x0+TILE-1),z1=Math.max(NORTH,z0-TILE+1);
        return l.isLoaded(b.offset(x0,0,z0))&&l.isLoaded(b.offset(x1,0,z0))&&l.isLoaded(b.offset(x0,0,z1))&&l.isLoaded(b.offset(x1,0,z1));
    }
    static boolean craterLoaded(ServerLevel l,BlockPos b){
        for(var c:CRATERS)for(int dx:new int[]{-c[2]-2,c[2]+2})for(int dz:new int[]{-c[2]-2,c[2]+2})if(!l.isLoaded(b.offset(c[0]+dx,0,c[1]+dz)))return false;
        return true;
    }
    static boolean polesLoaded(ServerLevel l,BlockPos b){
        for(int x=WEST;x<=EAST;x+=8)if(!l.isLoaded(b.offset(x,0,POLES_Z))||!l.isLoaded(b.offset(x,0,POLES_Z+6)))return false;
        return l.isLoaded(b.offset(EAST,0,POLES_Z));
    }
    static boolean farmLoaded(ServerLevel l,BlockPos b){
        for(int x:new int[]{LOT_W,LOT_E})for(int z:new int[]{WINDBREAK_Z+2,LOT_N})if(!l.isLoaded(b.offset(x,0,z)))return false;
        return true;
    }
    /** Whether anyone at the station, spectators too, is within so far of a place. */
    static boolean seen(List<ServerPlayer> here,BlockPos at,double within){
        for(var p:here)if(p.distanceToSqr(Vec3.atCenterOf(at))<within*within)return true;
        return false;
    }
    static boolean seenAny(List<ServerPlayer> here,BlockPos b,int[][] craters){
        for(var c:craters)if(seen(here,b.offset(c[0],0,c[1]),UNSEEN+c[2]))return true;
        return false;
    }
    /** A living body anywhere in a column from so deep to a little over the ground. */
    private static boolean occupied(ServerLevel l,BlockPos b,int x,int y0,int z,int y1){
        var p=b.offset(x,0,z);
        return !l.getEntitiesOfClass(LivingEntity.class,new AABB(p.getX(),p.getY()+y0,p.getZ(),p.getX()+1,p.getY()+y1+1,p.getZ()+1),e->e.isAlive()&&!e.isSpectator()).isEmpty();
    }
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){var p=b.offset(x,y,z);if(l.isLoaded(p))BuildBlocks.set(l,p,s,F);}
    private static void clear(ServerLevel l,BlockPos b,int x,int y,int z){
        var p=b.offset(x,y,z);if(!l.isLoaded(p)||l.getBlockState(p).isAir())return;
        BuildBlocks.set(l,p,AIR,F);
    }
    /** Put only where there is nothing, and never where a body stands. */
    private static void put(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){
        var p=b.offset(x,y,z);if(!l.isLoaded(p)||!l.getBlockState(p).isAir())return;
        if(!l.getEntitiesOfClass(LivingEntity.class,new AABB(p),e->e.isAlive()&&!e.isSpectator()).isEmpty())return;
        BuildBlocks.set(l,p,s,F);
    }

    // ------------------------------------------------------------------------------------------------ for tests
    /** Whether all of the outside the light reaches has gone this afternoon: every tile of the field, the craters, the poles, the farmhouse. */
    public static boolean done(BlockPos b){
        var s=Fallow.afternoon(b);if(s==null)return false;var st=s.strike;
        return st.ash.cardinality()==TILES&&st.cratered&&st.polesDown&&st.farmGone;
    }
}
