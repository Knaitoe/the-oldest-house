package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;

/**
 * 0.4.262 (the owner, of Borrasca: "the architecture/mapping of borrasca needs a lot of improvement"). The mountain is a valley now, not
 * a staircase. The old ground rose in flat terraces the full width of the map, one block every five, with sheer risers, stone walls down
 * both sides and a wall at the back; its trees stood on a grid and its rocks were cubes.
 *
 * <p>Here the ground is one height for every column, worked out from where the column is and nothing else (the site is built from a
 * recorded plan, so nothing here reads the world back): the trail's own floor is kept exactly, and the slopes rise gently away from it
 * on both sides with an undulation of their own, up to the valley walls, which are cliffs a reader cannot climb. The gully, the way down
 * the far side and the new ridge road are cut into the slopes with banks of their own; every authored place keeps its level; the ground
 * next to the yard's wire stays low, so the wire is never a step from the hillside. Tall pines with their crowns well above a person's
 * head stand among younger spruce, ferns and fallen trunks; the rocks are rocks.
 */
public final class BorrascaLand {
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private BorrascaLand(){}

    // ---- Where things are -----------------------------------------------------------------------------------------

    /**
     * The ridge road: from the head of the lane above the clearing along the front of the wire, round the yard's corner, up the cutting
     * outside the east fence and round the shoulder to the turning bay where the men leave their truck. The men come down it and go
     * back up it (BorrascaStory.APPROACH and EXIT walk the same line).
     */
    static final BlockPos[] ROAD={new BlockPos(3,12,-162),new BlockPos(21,12,-162),new BlockPos(30,12,-165),new BlockPos(31,12,-173),new BlockPos(31,12,-191),new BlockPos(35,12,-197),new BlockPos(37,12,-204)};
    /** The yard's fence lines (the wire), inclusive. */
    static final int YARD_X0=-30,YARD_X1=27,YARD_Z0=-214,YARD_Z1=-167;
    /** The turning bay at the top of the road, behind the shoulder. */
    static final int BAY_X0=32,BAY_X1=39,BAY_Z0=-207,BAY_Z1=-195;
    private record Route(BlockPos[] points,double half,double bank){}
    private static final Route[] ROUTES={new Route(BorrascaRooms.GULLY,1.7,1.4),new Route(BorrascaRooms.RETURN_ROUTE,1.7,.9),new Route(ROAD,2.3,2.4)};
    /**
     * 0.4.420: the same ways in the world, for the slopes between the authored places. 0.4.422: a step that crosses a slope is drawn out
     * with the hill (a point every two blocks, swaying and rising with the trail), so a way keeps its place beside the trail instead of
     * cutting straight through the woods; and the way down the far side ends at the trailhead, so it never runs through the street.
     */
    private static final Route[] WORLD_ROUTES={worldRoute(ROUTES[0],false),worldRoute(ROUTES[1],true),worldRoute(ROUTES[2],false)};
    private static Route worldRoute(Route r,boolean endAtTrailhead){
        var out=new ArrayList<BlockPos>();var p=r.points();
        for(int i=0;i<p.length;i++){
            var a=p[i];
            if(i+1==p.length){out.add(BorrascaRooms.world(a));break;}
            var c=p[i+1];double wa=BorrascaRooms.worldZ(a.getZ()),wc=BorrascaRooms.worldZ(c.getZ());
            int n=BorrascaRooms.crosses(wa,wc)?Math.max(1,(int)Math.ceil(Math.abs(wc-wa)/2)):1;
            for(int k=0;k<n;k++){
                double w=wa+(wc-wa)*k/n,z=BorrascaRooms.authoredZ(w),t=c.getZ()==a.getZ()?0:Math.max(0,Math.min(1,(z-a.getZ())/(c.getZ()-a.getZ())));
                var v=BorrascaRooms.world(new net.minecraft.world.phys.Vec3(a.getX()+(c.getX()-a.getX())*t,a.getY()+(c.getY()-a.getY())*t,z));
                out.add(new BlockPos((int)Math.round(v.x),(int)Math.round(v.y),(int)Math.round(w)));
            }
        }
        if(endAtTrailhead)out.removeIf(q->q.getZ()>BorrascaRooms.TOWN_END+2);
        return new Route(out.toArray(BlockPos[]::new),r.half(),r.bank());
    }
    /** Places kept at their own level, with the ground easing back to the slope over the margin. */
    private record Flat(int x0,int z0,int x1,int z1,int h,int margin){}  // margin: kept for reading; the easing is a cone
    private static final Flat[] FLATS={
        new Flat(-14,-154,7,-144,9,5),           // the clearing
        new Flat(-12,-166,29,-160,12,4),         // the plateau at the head of the trail, in front of the wire
        new Flat(-10,-106,9,-80,0,4),            // the Triple Tree and the fort
        new Flat(BAY_X0,BAY_Z0,BAY_X1,BAY_Z1,12,1),
        new Flat(3,-78,7,-74,0,3),               // marker one
        new Flat(6,-124,8,-122,4,2)};            // marker two

    // ---- The ground ------------------------------------------------------------------------------------------------

    private static double clamp(double v,double lo,double hi){return v<lo?lo:v>hi?hi:v;}
    private static int hash(int x,int y,int z){int h=x*73856093^y*19349663^z*83492791;h^=h>>>13;h*=0x5bd1e995;return (h^h>>>15)&0x7fffffff;}
    private static double unit(int x,int y,int z){return hash(x,y,z)/(double)Integer.MAX_VALUE*2-1;}
    /** A smooth undulation, a few blocks high, with no repeat a reader would notice. */
    static double swell(int x,int z){return 1.35*Math.sin(x*.17+z*.09)+1.0*Math.sin(x*.07-z*.13+2.1)+.6*Math.sin((x+z)*.21+.7)+.45*Math.sin(z*.31-x*.04);}
    private static double cb(int z){return clamp((-z-100)/5.0,0,12);}
    /** Distance from a column to a route's line, and the route's floor there. */
    private static double[] near(Route r,int x,int z){
        double best=1e9,y=0;var p=r.points();
        for(int i=0;i+1<p.length;i++){
            double ax=p[i].getX(),az=p[i].getZ(),bx=p[i+1].getX(),bz=p[i+1].getZ(),dx=bx-ax,dz=bz-az,len=dx*dx+dz*dz;
            double t=len==0?0:clamp(((x-ax)*dx+(z-az)*dz)/len,0,1);double qx=ax+dx*t-x,qz=az+dz*t-z,d=Math.sqrt(qx*qx+qz*qz);
            if(d<best){best=d;y=p[i].getY()+(p[i+1].getY()-p[i].getY())*t;}
        }
        return new double[]{best,y};
    }
    private static int chebyshev(int x,int z,int x0,int z0,int x1,int z1){return Math.max(Math.max(x0-x,x-x1),Math.max(z0-z,z-z1));}
    /** The ground's height at a column: the y a reader's feet stand at there. */
    public static int ground(int x,int z){
        int base=BorrascaRooms.trailY(z);double d=Math.abs(x-BorrascaRooms.trailX(z));
        double h;
        if(z>-72)h=0;
        else{
            double f=clamp((d-4)/6,0,1),side=clamp((d-4)*.2,0,4);
            // The slopes come up out of the town over a dozen blocks, not as a bank along its back.
            double fade=clamp((-72-z)/12.0,0,1);h=base+(cb(z)-base)*f+(side+swell(x,z)*f)*fade;
            if(z>-100)h=Math.max(h,0);
            h=Math.max(h,base);
        }
        // Authored places keep their level.
        // (The ground eases away from each at most nine blocks in ten, however far: a cone that meets the slope where it meets it.)
        for(var fl:FLATS){int c=chebyshev(x,z,fl.x0(),fl.z0(),fl.x1(),fl.z1());if(c<=0)h=fl.h();else h=clamp(h,fl.h()-c*.9,fl.h()+c*.9);}
        // The cuts: the gully, the way down the far side, the ridge road.
        boolean onRoad=x>=BAY_X0&&x<=BAY_X1&&z>=BAY_Z0&&z<=BAY_Z1;
        for(int i=0;i<ROUTES.length;i++){
            var r=ROUTES[i];var n=near(r,x,z);double dist=n[0],y=n[1];
            if(dist<=r.half()){h=y;if(i==2)onRoad=true;}
            else{double rise=(dist-r.half())*r.bank()+.4;if(h>y)h=Math.min(h,y+rise);else h=Math.max(h,y-rise);}
        }
        // The valley walls and the head of the valley: one cliff all round (so where the side meets the back there is no stair), seven blocks
        // at its foot and three more each block out, which no route's bank wears down (the road is cut through). The slope below never
        // climbs nearer its foot than three blocks of rock: nothing anywhere is a step up onto a ledge of it.
        int ax=Math.abs(x);double floor=z>-100?0:cb(z);int m=edge(x,z);
        if(!onRoad){if(m<=0)h=Math.min(h,floor+4-m*.9);else h=Math.max(h,floor+7+3*(m-1));}
        // The lane up from the clearing to the wire keeps its own steps.
        if(x>=-1&&x<=5&&z<=-150&&z>=-164)h=BorrascaRooms.trailY(z);
        // Nothing next to the wire stands higher than a child can step from to its top.
        int c=chebyshev(x,z,YARD_X0,YARD_Z0,YARD_X1,YARD_Z1);
        if(c<=0)h=12;else if(c<=2)h=Math.min(h,13);
        return (int)Math.round(clamp(h,-1,25));
    }
    /** How far a column is into the valley's walls: one or more on the cliff, zero at its foot, negative within the valley. */
    static int edge(int x,int z){int ax=Math.abs(x);return Math.max(z>-72?ax-37:ax-35,-216-z);}
    /** A column whose neighbours are two blocks or more above or below it shows rock, not soil. */
    private static boolean steep(int x,int z,int h){for(var d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}})if(Math.abs(ground(x+d[0],z+d[1])-h)>=2)return true;return false;}

    // ---- 0.4.420: the ground in the world -------------------------------------------------------------------------

    private static int za(int w){return (int)Math.round(BorrascaRooms.authoredZ(w));}
    private static int lift(int w){return (int)Math.round(BorrascaRooms.lift(BorrascaRooms.authoredZ(w)));}
    /**
     * The ground at a world column. An authored region's own ground, raised by its lift. On a drawn-out slope: the hillside between
     * the ground at its two ends, low swells across it away from the trail, the trail's own level along the trail (easing up and down
     * its sides at most nine in ten), and the gully and the way down cut through it as they are everywhere else.
     */
    /** How far either side of the trail's line a drawn-out slope keeps the trail's level (0.4.420). */
    static final double BAND=5.5;
    public static int groundAt(int x,int w){
        if(!BorrascaRooms.drawnOutWorld(w)){int z=za(w);return ground(x,z)+(int)Math.round(BorrascaRooms.lift(z));}
        // 0.4.422: the street past the chapel is the town's, level from side to side like the rest of it, out to the valley walls.
        if(w>=BorrascaRooms.TOWN_END)return ground(x,-71);
        var s=BorrascaRooms.slope(w);double t=BorrascaRooms.along(w);
        double h=(ground(x,s[0])+s[4])*(1-t)+(ground(x,s[1])+s[5])*t;
        double tx=BorrascaRooms.trailXw(w),ty=BorrascaRooms.trailYw(w),d=Math.abs(x-tx);
        h+=swell(x,w)*.8*Math.sin(Math.PI*t)*clamp((d-BAND-2.5)/8,0,1);
        // The trail's own level runs BAND either side of its line, so whoever walks beside it rather than on it keeps the same
        // half-steps instead of meeting a full block where the hillside begins.
        if(d<=BAND)h=ty;else h=clamp(h,ty-(d-BAND)*.9,ty+(d-BAND)*.9);
        for(int i=0;i<2;i++){var r=WORLD_ROUTES[i];var n=near(r,x,w);double dist=n[0],y=n[1];
            if(dist<=r.half())h=y;else{double rise=(dist-r.half())*r.bank()+.4;if(h>y)h=Math.min(h,y+rise);else h=Math.max(h,y-rise);}}
        return (int)Math.round(clamp(h,-1,46));
    }
    private static boolean steepAt(int x,int w,int h){for(var d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}})if(Math.abs(groundAt(x+d[0],w+d[1])-h)>=2)return true;return false;}
    /** Which way a world column's floor belongs to (as routeFloor, in the world). */
    static int routeFloorAt(int x,int w,int h){
        // An authored region's floor is its authored ground, so its ways are matched there too: the world line of a way that crosses
        // onto a slope bends away from the authored one near the join, and its risers would go missing.
        if(!BorrascaRooms.drawnOutWorld(w)){int z=za(w);return routeFloor(x,z,h-(int)Math.round(BorrascaRooms.lift(z)));}
        for(int i=0;i<2;i++){var n=near(WORLD_ROUTES[i],x,w);if(n[0]<=WORLD_ROUTES[i].half()&&h==(int)Math.round(clamp(n[1],-1,46)))return i+1;}
        return 0;
    }
    private static boolean gullyBankAt(int x,int w){return BorrascaRooms.drawnOutWorld(w)?near(WORLD_ROUTES[0],x,w)[0]<=WORLD_ROUTES[0].half()+4.5:gullyBank(x,za(w));}
    /** Where nothing grows or lies, in the world: an authored region's own rule; on a drawn-out slope the trail, the ways and the lane. */
    static boolean clearAt(int x,int w,int margin){
        if(!BorrascaRooms.drawnOutWorld(w))return clear(x,za(w),margin);
        // The town's own north end (0.4.420): kept clear like the rest of the town.
        if(w>=BorrascaRooms.TOWN_END-2)return Math.abs(x)<35+margin;
        if(Math.abs(x-BorrascaRooms.trailXw(w))<=BAND+1.5+margin)return true;
        for(var r:WORLD_ROUTES)if(near(r,x,w)[0]<=r.half()+margin)return true;
        int z=za(w),o=(int)Math.round(BorrascaRooms.sway(BorrascaRooms.authoredZ(w)));return z<=-150&&z>=-164&&x>=o-1-margin&&x<=o+5+margin;
    }
    private static int edgeAt(int x,int w){return edge(x,za(w));}
    private static BlockState top(int x,int z,int h,boolean steep){
        if(steep||h>=19)return rock(x,h,z);
        if(z>-72&&Math.abs(x)<38)return Blocks.GRASS_BLOCK.defaultBlockState();
        // 0.4.276: broad patches a walker would notice, not a speckle. 0.4.277: an Ozark oak-hickory floor, not taiga needles: grass and
        // sedge where the canopy lets light through, last year's leaves lying brown in broad drifts, moss only in the wettest hollows.
        double m=Math.sin(x*.07+z*.045)+.7*Math.sin(z*.09-x*.05+1.3)+.12*unit(x,0,z);
        if(m>1.45)return Blocks.MOSS_BLOCK.defaultBlockState();
        if(m<-.75)return Blocks.COARSE_DIRT.defaultBlockState();
        if(m>.9&&z<-110)return Blocks.ROOTED_DIRT.defaultBlockState();
        return (hash(x,1,z)%6==0?Blocks.COARSE_DIRT:Blocks.GRASS_BLOCK).defaultBlockState();
    }
    /** 0.4.277: Ozark dolomite, grey and bedded, with white chert in it and moss on its shaded faces. */
    private static BlockState rock(int x,int y,int z){int k=hash(x,y,z)%20;return (k<9?Blocks.STONE:k<13?Blocks.ANDESITE:k<15?Blocks.COBBLESTONE:k<18?Blocks.MOSSY_COBBLESTONE:Blocks.DIORITE).defaultBlockState();}

    /** The ground itself, every column but the front strip the station and the House's door stand on. */
    static void terrain(ServerLevel l,BlockPos b){
        for(int x=-44;x<=44;x++)for(int w=BorrascaRooms.FAR;w<=-1;w++){
            int h=groundAt(x,w);boolean steep=steepAt(x,w,h);int route=routeFloorAt(x,w,h);boolean bank=steep&&gullyBankAt(x,w);
            var top=route==1?bed(x,w):route==2?BorrascaRooms.walked(x,w,near(WORLD_ROUTES[1],x,w)[0]<=.9?0:2):bank?earth(x,h,w):top(x,w<-74&&w>=BorrascaRooms.TOWN_END?-60:za(w),h-lift(w),steep);
            if(h<=0){if(h==0&&!top.is(Blocks.GRASS_BLOCK))BuildBlocks.set(l,b.offset(x,-1,w),top,F);stepAt(l,b,x,w,h,route);continue;}
            if(h>=3)LiteraryRooms.box(l,b,x,0,w,x,h-3,w,Blocks.STONE);
            if(h>=2)BuildBlocks.set(l,b.offset(x,h-2,w),steep&&!bank?rock(x,h-2,w):Blocks.DIRT.defaultBlockState(),F);
            BuildBlocks.set(l,b.offset(x,h-1,w),top,F);
            stepAt(l,b,x,w,h,route);
        }
        gullyEdges(l,b);
    }
    /**
     * 0.4.276: which way a column's own floor belongs to: 1 the gully's bed, 2 the way down the far side, 0 neither. Only where the
     * way's own level set the ground (the wire, the lane and the cliffs keep theirs).
     */
    static int routeFloor(int x,int z,int h){
        if(chebyshev(x,z,YARD_X0,YARD_Z0,YARD_X1,YARD_Z1)<=1)return 0;
        for(int i=0;i<2;i++){var n=near(ROUTES[i],x,z);if(n[0]<=ROUTES[i].half()&&h==(int)Math.round(clamp(n[1],-1,25)))return i+1;}
        return 0;
    }
    /** A step up along a way: a riser where its floor climbs a block from the column before, facing the climb (world columns). */
    private static void stepAt(ServerLevel l,BlockPos b,int x,int w,int h,int route){
        if(route==0||h<0)return;
        for(var d:Direction.Plane.HORIZONTAL){int nx=x-d.getStepX(),nw=w-d.getStepZ();int nh=groundAt(nx,nw);
            if(nh==h-1&&routeFloorAt(nx,nw,nh)==route){
                var stair=route==1?(hash(x,9,w)%3==0?Blocks.MOSSY_COBBLESTONE_STAIRS:Blocks.STONE_STAIRS).defaultBlockState().setValue(StairBlock.FACING,d):BorrascaRooms.riser(d);
                BuildBlocks.set(l,b.offset(x,h-1,w),stair,F);return;}
        }
    }
    /** The gully's bed: the chert gravel an Ozark creek leaves, grey and white, with ledges of its rock showing through. */
    private static BlockState bed(int x,int z){int k=hash(x,8,z)%20;return (k<11?Blocks.GRAVEL:k<14?Blocks.STONE:k<16?Blocks.DIORITE:k<17?Blocks.MOSSY_COBBLESTONE:k<19?Blocks.COARSE_DIRT:Blocks.ANDESITE).defaultBlockState();}
    /** Within a few blocks of the gully a steep face is its cut bank, earth with roots and the odd stone, not bare rock. */
    private static boolean gullyBank(int x,int z){return near(ROUTES[0],x,z)[0]<=ROUTES[0].half()+4.5;}
    /** 0.4.277: the cut bank is the Ozarks' red cherty clay under its roots. */
    private static BlockState earth(int x,int y,int z){int k=hash(x,y,z)%12;return (k<4?Blocks.COARSE_DIRT:k<6?Blocks.ROOTED_DIRT:k<8?Blocks.TERRACOTTA:k<10?Blocks.DIRT:k<11?Blocks.MOSSY_COBBLESTONE:Blocks.PACKED_MUD).defaultBlockState();}
    /** Ferns along the top of the gully's banks, and one trunk fallen across it high enough to walk under. */
    private static void gullyEdges(ServerLevel l,BlockPos b){
        var r=WORLD_ROUTES[0];int from=(int)Math.round(BorrascaRooms.worldZ(-148)),to=(int)Math.round(BorrascaRooms.worldZ(-92));
        for(int x=-26;x<=4;x++)for(int w=from;w<=to;w++){
            double d=near(r,x,w)[0];if(d<=r.half()+.3||d>r.half()+2.6)continue;int h=groundAt(x,w);if(steepAt(x,w,h))continue;
            if(!BorrascaRooms.drawnOutWorld(w)&&chebyshev(x,za(w),-12,-108,11,-78)<=0)continue;
            int k=hash(x,33,w)%10;var at=b.offset(x,h,w);
            if(k<3)BuildBlocks.set(l,at,Blocks.FERN.defaultBlockState(),F);
            else if(k<4){BuildBlocks.set(l,at,Blocks.LARGE_FERN.defaultBlockState().setValue(DoublePlantBlock.HALF,DoubleBlockHalf.LOWER),F);BuildBlocks.set(l,at.above(),Blocks.LARGE_FERN.defaultBlockState().setValue(DoublePlantBlock.HALF,DoubleBlockHalf.UPPER),F);}
        }
        // The fallen trunk (0.4.277: a sycamore from the bank): across the channel at authored z -122, three above its bed, its ends on the banks.
        int z=(int)Math.round(BorrascaRooms.worldZ(-122));var n=near(r,-15,z);int bedY=(int)Math.round(n[1]),y=bedY+3;int x0=Integer.MIN_VALUE,x1=Integer.MIN_VALUE;
        for(int x=-15;x>=-24;x--)if(groundAt(x,z)>=y){x0=x;break;}
        for(int x=-15;x<=-6;x++)if(groundAt(x,z)>=y){x1=x;break;}
        if(x0!=Integer.MIN_VALUE&&x1!=Integer.MIN_VALUE)for(int x=x0;x<=x1;x++){BuildBlocks.set(l,b.offset(x,Math.max(y,groundAt(x,z)),z),Blocks.BIRCH_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X),F);if(hash(x,35,z)%2==0)BuildBlocks.set(l,b.offset(x,Math.max(y,groundAt(x,z))+1,z),Blocks.MOSS_CARPET.defaultBlockState(),F);}
    }

    // ---- Rocks -----------------------------------------------------------------------------------------------------

    /** A rock: an uneven lump of stone, partly sunk in the ground, mossed where it faces up. Cells passing {@code keep} are left alone. */
    static void rock(ServerLevel l,BlockPos b,double cx,double cy,double cz,double rx,double ry,double rz,java.util.function.IntPredicate keepX,Set<Long> into){
        int x0=(int)Math.floor(cx-rx-1),x1=(int)Math.ceil(cx+rx+1),y0=(int)Math.floor(cy-ry-1),y1=(int)Math.ceil(cy+ry+1),z0=(int)Math.floor(cz-rz-1),z1=(int)Math.ceil(cz+rz+1);
        for(int x=x0;x<=x1;x++){if(keepX!=null&&keepX.test(x))continue;for(int z=z0;z<=z1;z++)for(int y=y0;y<=y1;y++){
            if(!inside(x,y,z,cx,cy,cz,rx,ry,rz))continue;
            boolean capped=!inside(x,y+1,z,cx,cy,cz,rx,ry,rz);
            var state=capped&&hash(x,y,z)%3==0?Blocks.MOSSY_COBBLESTONE.defaultBlockState():rock(x,y,z);
            BuildBlocks.set(l,b.offset(x,y,z),state,F);
            if(capped&&hash(x,y+7,z)%4==0)BuildBlocks.set(l,b.offset(x,y+1,z),Blocks.MOSS_CARPET.defaultBlockState(),F);
            if(into!=null)into.add(ChunkPos.asLong(x,z));
        }}
    }
    private static boolean inside(int x,int y,int z,double cx,double cy,double cz,double rx,double ry,double rz){
        double dx=(x+.5-cx)/rx,dy=(y+.5-cy)/ry,dz=(z+.5-cz)/rz;return dx*dx+dy*dy+dz*dz<=1+.32*unit(x,y,z);
    }

    // ---- The clearing ----------------------------------------------------------------------------------------------

    /**
     * Where she is taken: red clay worn into the needles, ragged at its edges; on its west side a crag of the ridge's own rock, and the
     * broken stump of a great spruce with its roots out across the ground. The way between the two (where a reader can stand out of the
     * light) is kept.
     */
    static void clearing(ServerLevel l,BlockPos b){
        // 0.4.276: the needles worn through to earth where people stood, not a smear of red: bare and trodden at the middle, packed
        // where it is wettest, the needles coming back at its edges.
        for(int x=-14;x<=7;x++)for(int z=-154;z<=-144;z++){
            double e=Math.min(Math.min(x+14,7-x),Math.min(z+154,-144-z))+unit(x,3,z)*1.1;double worn=Math.hypot(x-(-1.5),(z+149)*1.2)+unit(x,4,z)*1.2;double wet=Math.sin(x*.35+z*.28);
            var s=e<1.2?Blocks.GRASS_BLOCK:worn<3.2?(wet>.5?Blocks.PACKED_MUD:Blocks.COARSE_DIRT):worn<5.5?(hash(x,5,z)%3==0?Blocks.ROOTED_DIRT:Blocks.COARSE_DIRT):hash(x,6,z)%4==0?Blocks.COARSE_DIRT:Blocks.GRASS_BLOCK;
            BuildBlocks.set(l,b.offset(x,8,z),s.defaultBlockState(),F);
        }
        LiteraryRooms.box(l,b,-14,9,-154,7,13,-144,Blocks.AIR);
        // The crag, west of the gap a reader can stand in.
        rock(l,b,-15,11,-151.5,2.6,4.2,6.5,x->x>=-12,null);
        for(int z=-157;z<=-146;z++)BuildBlocks.set(l,b.offset(-16,8,z),(hash(-16,7,z)%2==0?Blocks.COARSE_DIRT:Blocks.GRASS_BLOCK).defaultBlockState(),F);
        // The woods close round it on the side away from the ridge (the line up to the lamp stays open), ferns at its rim. 0.4.277: red
        // cedars, which take any Ozark opening first.
        for(int[] t:new int[][]{{-17,-142,6},{-13,-139,7},{-19,-147,5},{-9,-139,5}})cedar(l,b,t[0],ground(t[0],t[1]),t[1],t[2]);
        for(int x=-14;x<=7;x++)for(int z=-154;z<=-144;z++){double e=Math.min(Math.min(x+14,7-x),Math.min(z+154,-144-z));if(e>=1.5||hash(x,7,z)%4!=0||x<-11&&z<-145)continue;BuildBlocks.set(l,b.offset(x,9,z),Blocks.FERN.defaultBlockState(),F);}
        // Broken brush at its edges: dead stems and two saplings snapped off a block up.
        for(int[] at:new int[][]{{-12,-145},{5,-153},{6,-146},{-13,-153},{3,-144}})BuildBlocks.set(l,b.offset(at[0],9,at[1]),Blocks.DEAD_BUSH.defaultBlockState(),F);
        for(int[] at:new int[][]{{-11,-146},{6,-151}}){BuildBlocks.set(l,b.offset(at[0],9,at[1]),Blocks.STRIPPED_OAK_LOG.defaultBlockState(),F);}
        // The stump: three trunks' width of grey wood, broken off at uneven heights, roots splayed over the clay.
        for(int x=-9;x<=-7;x++)for(int z=-153;z<=-151;z++){int top=10+hash(x,9,z)%4;for(int y=9;y<=top;y++)BuildBlocks.set(l,b.offset(x,y,z),Blocks.OAK_WOOD.defaultBlockState(),F);if(hash(x,2,z)%2==0)BuildBlocks.set(l,b.offset(x,top+1,z),Blocks.MOSS_CARPET.defaultBlockState(),F);}
        BuildBlocks.set(l,b.offset(-8,13,-152),Blocks.STRIPPED_OAK_LOG.defaultBlockState(),F);
        for(int[] root:new int[][]{{-10,-151,0},{-10,-153,0},{-6,-152,0},{-8,-150,2},{-8,-154,2},{-10,-150,2}}){
            var axis=root[2]==0?Direction.Axis.X:Direction.Axis.Z;
            BuildBlocks.set(l,b.offset(root[0],9,root[1]),Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,axis),F);
        }
        for(int[] at:new int[][]{{-11,-149},{-6,-154},{-11,-154}})BuildBlocks.set(l,b.offset(at[0],9,at[1]),Blocks.MOSSY_COBBLESTONE.defaultBlockState(),F);
    }

    // ---- The ridge road --------------------------------------------------------------------------------------------

    /** The road's surface: two ruts of gravel either side of a grassy crown, coarse dirt at the verges; a sign where it leaves the wire. */
    static void road(ServerLevel l,BlockPos b){
        var road=ROUTES[2];
        for(int x=-2;x<=40;x++)for(int z=-208;z<=-158;z++){
            var n=near(road,x,z);boolean bay=x>=BAY_X0&&x<=BAY_X1&&z>=BAY_Z0&&z<=BAY_Z1;if(n[0]>road.half()+.2&&!bay)continue;
            if(chebyshev(x,z,YARD_X0,YARD_Z0,YARD_X1,YARD_Z1)<=0)continue;if(x<=4&&z>=-163)continue; // the lane keeps its trail
            double off=n[0];var s=bay?(hash(x,5,z)%3==0?Blocks.COARSE_DIRT:Blocks.GRAVEL):off>1.7?Blocks.COARSE_DIRT:off>.6?Blocks.GRAVEL:hash(x,6,z)%3==0?Blocks.GRASS_BLOCK:Blocks.COARSE_DIRT;
            BuildBlocks.set(l,b.offset(x,11,z),s.defaultBlockState(),F);
        }
        // A low post, no step from its top to the wire's.
        BuildBlocks.set(l,b.offset(28,13,-171),Blocks.OAK_LOG.defaultBlockState(),F);
        NovelRooms.sign(l,b.offset(28,13,-170),Direction.SOUTH,new String[]{"PRESCOTT","REFINING","Service road","No admittance"});
    }

    // ---- The woods -------------------------------------------------------------------------------------------------

    /** Where nothing grows or lies: the trail and every cut, every building, the clearing and the sightlines the taking needs. */
    static boolean clear(int x,int z,int margin){
        if(z>-72)return Math.abs(x)<38+margin-3;
        if(z>=-170&&Math.abs(x-BorrascaRooms.trailX(z))<=4+margin)return true;
        for(var r:ROUTES)if(near(r,x,z)[0]<=r.half()+margin)return true;
        // Nothing that stands or lies within six blocks of the wire: no crown over it, no rock a step from its top.
        if(chebyshev(x,z,YARD_X0,YARD_Z0,YARD_X1,YARD_Z1)<=(margin>=2?6:margin))return true;
        if(chebyshev(x,z,-17,-160,9,-140)<=margin)return true;             // the clearing and its crag
        if(chebyshev(x,z,-1,-166,6,-148)<=margin)return true;              // the lane up to the wire
        if(chebyshev(x,z,-12,-108,11,-78)<=margin)return true;             // the fort and the Triple Tree
        if(chebyshev(x,z,BAY_X0,BAY_Z0,BAY_X1,BAY_Z1)<=margin)return true;
        // The line the men are seen along from the clearing, and the clearing's view of the ridge lamp.
        if(z<=-140&&z>=-170&&x>=-4&&x<=32&&(z+148)*(z+148)<=Math.pow((x+2)*.8+6,2))return true;
        return false;
    }
    /**
     * The woods, last of all: rocks on the slopes and beside the trail where the old cover stood, tall trees whose crowns start well above
     * a person's head (so the slopes past the second marker can still be seen into), younger trees lower down, ferns, and the odd fallen
     * trunk. 0.4.277: the Ozarks' own woods, not taiga: white and black oak and hickory, shortleaf pine (the trail is named for it),
     * sycamores along the gully, red cedar coming up in the open places.
     */
    static void forest(ServerLevel l,BlockPos b){
        var rocky=new HashSet<Long>();
        // Cover beside the trail, where the old blocks stood (a reader still has somewhere to keep out of the Sheriff's light).
        // 0.4.276: not a pair at every eleventh block: one side then the other, at different sizes and distances from the path.
        // 0.4.420: each kept beside the trail wherever the drawn-out trail now runs.
        double[][] cover={{-106,-6.4,1.4,2.3,1.9,2.5},{-108,10.4,-1.2,1.6,1.3,1.7},{-117,9.8,1.0,2.5,1.9,2.4},{-120,-7.6,-1.4,1.5,1.2,1.6},{-128,-6.8,1.5,2.6,2.0,2.8},{-139,9.2,1.6,2.3,1.8,2.4},{-141,-6.2,.2,1.3,1.0,1.4}};
        for(var c:cover){int z=(int)c[0];int w=(int)Math.round(BorrascaRooms.worldZ(z));double y=BorrascaRooms.trailYw(w);double x=c[1]+BorrascaRooms.trailXw(w)-BorrascaRooms.trailX(z);rock(l,b,x,y+.9-(c[4]<1.5?.3:0),w+c[2],c[3],c[4],c[5],null,rocky);}
        // Boulders on the slopes, half sunk.
        int span=-6-BorrascaRooms.FAR;
        for(int i=0,placed=0;i<900&&placed<52;i++){
            int x=-34+hash(i,11,7)%69,w=BorrascaRooms.FAR+8+hash(i,13,5)%(span-90);if(w>BorrascaRooms.TOWN_END||clearAt(x,w,3)||edgeAt(x,w)>=-4)continue;int h=groundAt(x,w);if(h-lift(w)>18)continue;
            double r=1.1+(hash(i,17,3)%10)/9.0;rock(l,b,x+.5,h+.15,w+.5,r*1.15,r*.8,r,null,rocky);placed++;
        }
        var trunks=new HashSet<Long>();
        for(int gx=-42;gx<=42;gx+=6)for(int gz=BorrascaRooms.FAR+4;gz<=-6;gz+=6){
            int x=gx+hash(gx,1,gz)%5-2,w=gz+hash(gx,2,gz)%5-2;int z=za(w);
            if(clearAt(x,w,2)||rocky.contains(ChunkPos.asLong(x,w)))continue;int h=groundAt(x,w);if(h-lift(w)>16&&Math.abs(x)<37&&z>-217)continue;
            // A young cedar can be climbed like a stair, so near the cliffs only the tall trees grow, their crowns well out of reach.
            boolean cliff=edgeAt(x,w)>=-6;int kind=hash(x,3,w)%10;int tall=12+hash(x,4,w)%6;
            boolean creek=near(WORLD_ROUTES[0],x,w)[0]<=WORLD_ROUTES[0].half()+9;
            if(creek&&kind<5)sycamore(l,b,x,h,w,tall);
            else if(kind<3)pine(l,b,x,h,w,13+hash(x,4,w)%7);
            else if(kind<7||cliff)tallOak(l,b,x,h,w,tall,kind==6||hash(x,12,w)%4==0);
            else if(z>-135)oak(l,b,x,h,w,5+hash(x,6,w)%3);
            else cedar(l,b,x,h,w,5+hash(x,5,w)%4);
            trunks.add(ChunkPos.asLong(x,w));
        }
        // Fallen trunks, only where the ground lies level along their whole length.
        for(int i=0,placed=0;i<700&&placed<14;i++){
            int x=-36+hash(i,21,1)%73,w=BorrascaRooms.FAR+15+hash(i,23,2)%(span-80);if(w>BorrascaRooms.TOWN_END)continue;boolean along=hash(i,25,3)%2==0;int len=4+hash(i,27,4)%3;
            int h=groundAt(x,w);boolean ok=h-lift(w)<=18;for(int k=0;k<len&&ok;k++){int ax=x+(along?k:0),aw=w+(along?0:k);ok=!clearAt(ax,aw,2)&&groundAt(ax,aw)==h&&!rocky.contains(ChunkPos.asLong(ax,aw))&&!trunks.contains(ChunkPos.asLong(ax,aw));}
            if(!ok)continue;placed++;
            for(int k=0;k<len;k++){int ax=x+(along?k:0),aw=w+(along?0:k);BuildBlocks.set(l,b.offset(ax,h,aw),(i%2==0?Blocks.OAK_LOG:Blocks.DARK_OAK_LOG).defaultBlockState().setValue(RotatedPillarBlock.AXIS,along?Direction.Axis.X:Direction.Axis.Z),F);if(hash(ax,29,aw)%2==0)BuildBlocks.set(l,b.offset(ax,h+1,aw),Blocks.MOSS_CARPET.defaultBlockState(),F);trunks.add(ChunkPos.asLong(ax,aw));}
        }
        // Undergrowth.
        int under=(int)Math.round(BorrascaRooms.worldZ(-212));
        for(int x=-40;x<=40;x++)for(int w=under;w<=BorrascaRooms.TOWN_END;w++){
            int k=hash(x,31,w)%100;if(k>=9||clearAt(x,w,1)||rocky.contains(ChunkPos.asLong(x,w))||trunks.contains(ChunkPos.asLong(x,w)))continue;
            int h=groundAt(x,w);if(h-lift(w)>18||steepAt(x,w,h))continue;var at=b.offset(x,h,w);
            if(k<3)BuildBlocks.set(l,at,Blocks.FERN.defaultBlockState(),F);
            else if(k<4){BuildBlocks.set(l,at,Blocks.LARGE_FERN.defaultBlockState().setValue(DoublePlantBlock.HALF,DoubleBlockHalf.LOWER),F);BuildBlocks.set(l,at.above(),Blocks.LARGE_FERN.defaultBlockState().setValue(DoublePlantBlock.HALF,DoubleBlockHalf.UPPER),F);}
            else if(k<7)BuildBlocks.set(l,at,Blocks.DEAD_BUSH.defaultBlockState(),F);
            else if(k<8)BuildBlocks.set(l,at,Blocks.BROWN_MUSHROOM.defaultBlockState(),F);
            else BuildBlocks.set(l,at,Blocks.SHORT_GRASS.defaultBlockState(),F);
        }
    }
    private static BlockState leaves(){return Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);}
    /**
     * A tall shortleaf pine, the pine the trail is named for: a long bare trunk and, from about seven blocks up, an open, irregular crown
     * in two or three loose clumps with a rounded top (0.4.277: not a spruce's spire).
     */
    static void pine(ServerLevel l,BlockPos b,int x,int y,int z,int height){
        LiteraryRooms.box(l,b,x,y,z,x,y+height-1,z,Blocks.SPRUCE_LOG);
        int crown=Math.max(7,height-6);int[] radii={1,2,3,2,1,2,3,2,2,1};
        for(int k=0;k<radii.length&&crown+k<=height+1;k++){
            int r=radii[k],yy=y+crown+k;int ox=k>=5?hash(x,20,z)%3-1:0,oz=k>=5?hash(x,21,z)%3-1:0;
            for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){if(dx*dx+dz*dz>r*r+(r>1?1:0))continue;if(dx==0&&dz==0&&crown+k<height)continue;if(r>=2&&hash(x+dx,yy,z+dz)%4==0)continue;BuildBlocks.set(l,b.offset(x+dx+ox,yy,z+dz+oz),leaves(),F);}
        }
    }
    /** 0.4.276: an oak: a short trunk and a rounded, broken crown. */
    static void oak(ServerLevel l,BlockPos b,int x,int y,int z,int height){
        LiteraryRooms.box(l,b,x,y,z,x,y+height-1,z,Blocks.OAK_LOG);
        double cy=y+height-.5,r=2.6;
        for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++)for(int dy=-2;dy<=2;dy++){
            double d=dx*dx/(r*r)+dz*dz/(r*r)+dy*dy/(2.0*2.0);if(d>1+.25*unit(x+dx,y+dy,z+dz))continue;if(dx==0&&dz==0&&dy<1)continue;if(hash(x+dx,y+dy+40,z+dz)%7==0)continue;
            BuildBlocks.set(l,b.offset(x+dx,(int)Math.floor(cy)+dy,z+dz),Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true),F);
        }
    }
    /**
     * 0.4.277: an eastern red cedar, the Ozarks' own small conifer: a reddish trunk and a narrow, dense column of dark green nearly to
     * the ground, swelling here and there.
     */
    static void cedar(ServerLevel l,BlockPos b,int x,int y,int z,int height){
        LiteraryRooms.box(l,b,x,y,z,x,y+height-2,z,Blocks.MANGROVE_LOG);
        for(int k=1;k<=height;k++){int r=k>=height-1?0:k>=height-3?1:(k%3==1&&k>1)?2:1;int yy=y+k;
            for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){if(dx*dx+dz*dz>r*r+(r==2?0:1))continue;if(dx==0&&dz==0&&k<=height-2)continue;if(r==2&&hash(x+dx,yy,z+dz)%3==0)continue;BuildBlocks.set(l,b.offset(x+dx,yy,z+dz),leaves(),F);}}
        BuildBlocks.set(l,b.offset(x,y+height+1,z),leaves(),F);
    }
    /**
     * 0.4.277: a woods-grown oak or hickory: a long clean trunk (nothing to climb, nothing at eye level) and a broad, uneven crown from
     * seven blocks up, a bough or two out under it. Hickory is the darker bark and leaf.
     */
    static void tallOak(ServerLevel l,BlockPos b,int x,int y,int z,int height,boolean hickory){
        var wood=hickory?Blocks.DARK_OAK_LOG:Blocks.OAK_LOG;var leaf=(hickory?Blocks.DARK_OAK_LEAVES:Blocks.OAK_LEAVES).defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);
        LiteraryRooms.box(l,b,x,y,z,x,y+height-1,z,wood);
        int crown=Math.max(7,height-5);double r=3.0+(hash(x,7,z)%3)*.35;double cy=y+crown+2.0;
        for(int k=0;k<2;k++){int dir=hash(x,8+k,z)%4;int sx=dir==0?1:dir==1?-1:0,sz=dir==2?1:dir==3?-1:0;int by=y+crown+k;
            for(int s=1;s<=2;s++)BuildBlocks.set(l,b.offset(x+sx*s,by+(s-1),z+sz*s),wood.defaultBlockState().setValue(RotatedPillarBlock.AXIS,sx!=0?Direction.Axis.X:Direction.Axis.Z),F);}
        for(int dx=-4;dx<=4;dx++)for(int dz=-4;dz<=4;dz++)for(int dy=-2;dy<=3;dy++){
            int yy=(int)Math.floor(cy)+dy;if(yy<y+crown)continue;
            double d=dx*dx/(r*r)+dz*dz/(r*r)+(dy-.3)*(dy-.3)/(2.4*2.4);if(d>1+.22*unit(x+dx,yy,z+dz))continue;
            if(dx==0&&dz==0&&yy<y+height)continue;if(d>.5&&hash(x+dx,yy+40,z+dz)%6==0)continue;
            var at=b.offset(x+dx,yy,z+dz);if(!BuildBlocks.state(l,at).isAir())continue;BuildBlocks.set(l,at,leaf,F);
        }
    }
    /**
     * 0.4.277: a sycamore by the gully: the white, mottled trunk an Ozark creek is lined with, leaning a little at the top, its crown broad
     * and high, white limbs showing through it.
     */
    static void sycamore(ServerLevel l,BlockPos b,int x,int y,int z,int height){
        int lx=hash(x,14,z)%3-1,lz=hash(x,15,z)%3-1;int top=y+height-1;
        for(int yy=y;yy<=top;yy++){boolean lean=yy-y>height*2/3;BuildBlocks.set(l,b.offset(x+(lean?lx:0),yy,z+(lean?lz:0)),Blocks.BIRCH_LOG.defaultBlockState(),F);}
        int cx=x+lx,cz=z+lz;int crown=Math.max(7,height-5);double r=3.4;double cy=y+crown+2.2;
        var leaf=Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);
        for(int s=1;s<=2;s++){BuildBlocks.set(l,b.offset(cx+s,y+crown+1,cz),Blocks.BIRCH_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X),F);BuildBlocks.set(l,b.offset(cx,y+crown+2,cz-s),Blocks.BIRCH_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.Z),F);}
        for(int dx=-4;dx<=4;dx++)for(int dz=-4;dz<=4;dz++)for(int dy=-2;dy<=3;dy++){
            int yy=(int)Math.floor(cy)+dy;if(yy<y+crown)continue;
            double d=dx*dx/(r*r)+dz*dz/(r*r)+(dy-.4)*(dy-.4)/(2.2*2.2);if(d>1+.25*unit(cx+dx,yy,cz+dz))continue;if(d>.45&&hash(cx+dx,yy+60,cz+dz)%5==0)continue;
            var at=b.offset(cx+dx,yy,cz+dz);if(!BuildBlocks.state(l,at).isAir())continue;BuildBlocks.set(l,at,leaf,F);
        }
    }
}
