package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;

/**
 * 0.4.276, the owner on the Borrasca renders: "The renders are plain and look messily composed." Composition only: every story
 * position, walk and sightline stays where it was (the station, the store and the Sheriff's office round their square, the child's walk
 * up the middle of the town, the Triple Tree's forks, the depot's aisle and the line from the clearing to the ridge lamp).
 *
 * <p>The town had three buildings at its front and fifty blocks of empty lawn behind them. Now the trail leaves the square as the
 * town's one street, with four houses on their lots either side, a chapel, the trailhead where the street gives out, poles with a lamp
 * on each, and trees in the yards and along the valley's sides. The Triple Tree is a tree: three thick trunks out of one burl, roots,
 * limbs and one crown over the fort. The ridge depot's long steel walls are broken into bays, and its yard has a tailings heap, a water
 * tank on legs, pallets and the ruts the trucks have worn.
 *
 * <p>0.4.277, the owner: "Consider actual regional landscaping and architecture. Drisking Missouri." Drisking is an Ozark town, so the
 * houses are what an Ozark street has: clapboard and an Ozark rock house, brick, a pale board house, tin roofs (green, galvanised and
 * rusted) and one of shingles, chain-link along the street, a redbud and a dogwood in the yards; the church is a white frame Baptist
 * church under a tin roof; the poles are creosoted; the Triple Tree is a great white oak; the depot's heap is grey chat.
 */
final class BorrascaDressing {
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private static final BlockState AIR=Blocks.AIR.defaultBlockState();
    private BorrascaDressing(){}

    private static int hash(int x,int y,int z){int h=x*73856093^y*19349663^z*83492791^0x5f3;h^=h>>>13;h*=0x5bd1e995;return (h^h>>>15)&0x7fffffff;}
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,Block block){set(l,b,x,y,z,block.defaultBlockState());}
    private static void box(ServerLevel l,BlockPos b,int x,int y,int z,int X,int Y,int Z,BlockState s){NovelRooms.box(l,b,Math.min(x,X),Math.min(y,Y),Math.min(z,Z),Math.max(x,X),Math.max(y,Y),Math.max(z,Z),s);}
    private static void box(ServerLevel l,BlockPos b,int x,int y,int z,int X,int Y,int Z,Block block){box(l,b,x,y,z,X,Y,Z,block.defaultBlockState());}
    private static BlockState stair(Block block,Direction facing,Half half){return block.defaultBlockState().setValue(StairBlock.FACING,facing).setValue(StairBlock.HALF,half);}
    private static BlockState slab(Block block,SlabType type){return block.defaultBlockState().setValue(SlabBlock.TYPE,type);}
    private static BlockState log(Block block,Direction.Axis axis){return block.defaultBlockState().setValue(RotatedPillarBlock.AXIS,axis);}
    private static BlockState leaves(Block block){return block.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);}
    private static BlockState hanging(){return Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true);}

    // ================================================================================================ the town

    /** The street runs up the middle from the square (z -14) to the trees (z -70); the child's walk keeps to it. */
    /** 0.4.420: the street runs on to the trailhead at the town's new north end. */
    static final int STREET_Z0=-14,STREET_Z1=BorrascaRooms.TOWN_END+1;

    static void town(ServerLevel l,BlockPos b){
        street(l,b);
        // Two lots either side, their doors on the street (the west houses face east, the east houses west).
        // 0.4.277: white clapboard under green tin; an Ozark rock house under galvanised tin; pale boards under shingles; brick under rust.
        house(l,b,-23,-35,-15,-43,true,Blocks.BIRCH_PLANKS,Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS,Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB,"PRUITT",0);
        house(l,b,-23,-47,-15,-55,true,Blocks.MUD_BRICKS,Blocks.POLISHED_ANDESITE_STAIRS,Blocks.POLISHED_ANDESITE_SLAB,"HOLLIS",1);
        house(l,b,15,-35,23,-43,false,Blocks.STRIPPED_OAK_LOG,Blocks.DEEPSLATE_TILE_STAIRS,Blocks.DEEPSLATE_TILE_SLAB,"WEBB",2);
        house(l,b,15,-47,23,-55,false,Blocks.BRICKS,Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS,Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB,"CORLEY",3);
        chapel(l,b);
        trailhead(l,b);
        poles(l,b);
        trees(l,b);
    }

    /** Gravel up the middle with coarse shoulders, forecourts to the store's porch and the Sheriff's door, the square between. */
    private static void street(ServerLevel l,BlockPos b){
        for(int z=STREET_Z0;z>=STREET_Z1;z--)for(int x=-4;x<=4;x++){
            int ax=Math.abs(x);int k=hash(x,1,z)%10;BlockState s;
            // It narrows to the trail at its top end.
            boolean top=z<STREET_Z1+6;int width=top?2:3;
            if(ax>width+1)continue;
            if(ax<=width)s=top?BorrascaRooms.walked(x,z,ax):(k<9?Blocks.GRAVEL:Blocks.COARSE_DIRT).defaultBlockState();
            else s=(k<3?Blocks.COARSE_DIRT:Blocks.GRASS_BLOCK).defaultBlockState();
            set(l,b,x,-1,z,s);
        }
        // The store's forecourt: worn gravel from its porch steps to the street.
        for(int x=-12;x<=-5;x++)for(int z=-28;z<=-18;z++){int k=hash(x,2,z)%10;if(Math.abs(z+23)>4-(x+12)/4&&k<6)continue;set(l,b,x,-1,z,(k<9?Blocks.GRAVEL:Blocks.COARSE_DIRT).defaultBlockState());}
        // A bench outside the store and a rain barrel at the office.
        BorrascaShops.seats(l,b,new BlockPos(-11,0,-20),new BlockPos(-11,0,-19),Direction.EAST,false); // 0.4.454
        set(l,b,17,0,-28,Blocks.BARREL);
    }

    /**
     * A small house on its lot: walls three high on a stone footing, a gable roof along the street with its eaves out a block, a porch on
     * the street side, a chimney at the back, windows each side, chain-link along the street with an opening and a post with the name, a
     * path to the porch, a tree in the yard. Inside, a room and a lamp (its windows lit). Each lot has its own yard: a swing, a washing
     * line, a flower bed, a kennel. 0.4.277: frame houses have white corner boards; the rock house ({@code MUD_BRICKS}) is laid in the
     * Ozark way, odd brown slabs in their mortar; the porch roof is the house's own tin or shingle.
     */
    private static void house(ServerLevel l,BlockPos b,int xa,int za,int xb,int zb,boolean west,Block wall,Block roof,Block roofSlab,String name,int yard){
        int x0=Math.min(xa,xb),x1=Math.max(xa,xb),z0=Math.max(za,zb),z1=Math.min(za,zb);int zc=(z0+z1)/2;
        int front=west?x1:x0,back=west?x0:x1;Direction out=west?Direction.EAST:Direction.WEST;int o=west?1:-1;
        boolean frame=wall==Blocks.BIRCH_PLANKS||wall==Blocks.STRIPPED_OAK_LOG;
        box(l,b,x0-1,0,z1-1,x1+1,10,z0+1,AIR);
        // Footing and floor.
        for(int x=x0;x<=x1;x++)for(int z=z1;z<=z0;z++)set(l,b,x,-1,z,(x==x0||x==x1||z==z0||z==z1)?Blocks.COBBLESTONE.defaultBlockState():Blocks.OAK_PLANKS.defaultBlockState());
        // Walls, corner posts, and the plate the roof sits on.
        for(int x=x0;x<=x1;x++)for(int z=z1;z<=z0;z++){if(x!=x0&&x!=x1&&z!=z0&&z!=z1)continue;for(int y=0;y<=3;y++)set(l,b,x,y,z,wallCell(wall,x,y,z));}
        if(frame)for(int x:new int[]{x0,x1})for(int z:new int[]{z0,z1})box(l,b,x,0,z,x,3,z,log(Blocks.STRIPPED_BIRCH_LOG,Direction.Axis.Y));
        // Ceiling, so the room has a lamp to hang from and the attic is closed.
        box(l,b,x0,4,z1,x1,4,z0,Blocks.OAK_PLANKS);
        // Roof: stairs up from the eaves on each side to a ridge along the street, gable ends filled.
        for(int k=0;;k++){int left=x0-1+k,right=x1+1-k,y=4+k;if(left>right)break;
            for(int z=z1-1;z<=z0+1;z++){
                if(left==right)set(l,b,left,y,z,slab(roofSlab,SlabType.BOTTOM));
                else{set(l,b,left,y,z,stair(roof,Direction.EAST,Half.BOTTOM));set(l,b,right,y,z,stair(roof,Direction.WEST,Half.BOTTOM));}
            }
            if(k>=1)for(int x=left+1;x<=right-1;x++)for(int z:new int[]{z0,z1})set(l,b,x,y,z,wallCell(wall,x,y,z));
            if(left==right)break;
        }
        // Windows: two on the street, two at the back, one each end; the door in the middle of the street side.
        for(int dz:new int[]{-2,2}){box(l,b,front,1,zc+dz,front,2,zc+dz,Blocks.GLASS_PANE);box(l,b,back,1,zc+dz,back,2,zc+dz,Blocks.GLASS_PANE);}
        for(int z:new int[]{z0,z1})box(l,b,(x0+x1)/2,1,z,(x0+x1)/2,2,z,Blocks.GLASS_PANE);
        NovelRooms.door(l,b.offset(front,0,zc),out,Blocks.OAK_DOOR,false);
        // Porch: boards level with the floor, two posts, a slab roof and its light (one house leaves it burning).
        int p0=front+o,p1=front+2*o;
        for(int x=Math.min(p0,p1);x<=Math.max(p0,p1);x++)for(int z=zc-2;z<=zc+2;z++){set(l,b,x,-1,z,Blocks.OAK_PLANKS);set(l,b,x,3,z,slab(roofSlab,SlabType.BOTTOM));}
        for(int dz:new int[]{-2,2})box(l,b,p1,0,zc+dz,p1,2,zc+dz,Blocks.BIRCH_FENCE);
        set(l,b,p0,2,zc+1,hanging());
        BorrascaShops.seats(l,b,new BlockPos(p0,0,zc-1),new BlockPos(p0,0,zc-1),out,false); // 0.4.454: the porch seat, looking out
        // Chimney at the back, up through the eave: fieldstone, or brick on the brick house.
        boolean brick=wall==Blocks.BRICKS;
        for(int y=0;y<=9;y++)set(l,b,back-o,y,z1+1,brick?Blocks.BRICKS.defaultBlockState():(hash(back,y,z1)%4==0?Blocks.MOSSY_COBBLESTONE:Blocks.COBBLESTONE).defaultBlockState());
        set(l,b,back-o,10,z1+1,brick?Blocks.BRICK_WALL:Blocks.COBBLESTONE_WALL);
        // Inside: a rug, a table and chairs, a chair by the stove, drawers, a bed against the back wall, a lamp.
        int ix=back+o;
        box(l,b,Math.min(ix,front-o*2),0,zc-1,Math.max(ix,front-o*2),0,zc+1,Blocks.BROWN_CARPET);
        NovelRooms.furniture(l,b.offset(front-2*o,0,z0-1),HouseholdFurnitureBlock.Kind.FORMICA_TABLE,out);
        NovelRooms.furniture(l,b.offset(front-3*o,0,z0-1),HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,out);
        NovelRooms.furniture(l,b.offset(ix,0,z0-1),HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,out);
        NovelRooms.furniture(l,b.offset(ix,0,zc),HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,out);
        bed(l,b,ix,z1+1,out);
        set(l,b,(x0+x1)/2,3,zc,hanging());
        // The yard: path, chain-link along the street with an opening for the path, the name on the post beside it, a tree.
        int street=west?-5:5;
        for(int x=Math.min(p1+o,street),X=Math.max(p1+o,street);x<=X;x++)set(l,b,x,-1,zc,BorrascaRooms.walked(x,zc,1));
        for(int z=z1-1;z<=z0+1;z++){if(z==zc)continue;set(l,b,street,0,z,Blocks.IRON_BARS);}
        box(l,b,street,0,zc,street,1,zc,AIR);
        for(int x=Math.min(street,back-o),X=Math.max(street,back-o);x<=X;x++){set(l,b,x,0,z0+2,Blocks.IRON_BARS);set(l,b,x,0,z1-2,Blocks.IRON_BARS);}
        box(l,b,street,0,zc+1,street,1,zc+1,log(Blocks.STRIPPED_OAK_LOG,Direction.Axis.Y));BorrascaSigns.hang(l,b.offset(street+o,1,zc+1),BorrascaSigns.Sign.valueOf("NAME_"+name),out); // 0.4.449: each family's own plate
        int tx=street+(west?-3:3),tz=z0;
        switch(yard){
            case 0->{BorrascaLand.oak(l,b,tx,0,tz,6);}
            case 1->{redbud(l,b,tx,0,z0);swing(l,b,street+(west?-4:4),zc-3,west);}
            case 2->{BorrascaLand.oak(l,b,tx,0,z1,5);line(l,b,back-3*o,z0,z1,west);}
            default->{dogwood(l,b,tx,0,z0);beds(l,b,street+(west?-2:2),zc,west);kennel(l,b,back,z0,west);}
        }
        BorrascaRooms.join(l,b,Math.min(x0-3,street-1),0,z1-3,Math.max(x1+3,street+1),2,z0+3);
    }
    private static void bed(ServerLevel l,BlockPos b,int x,int z,Direction out){
        // Foot toward the room, head at the end wall.
        var foot=b.offset(x,0,z+1);var head=b.offset(x,0,z);
        BuildBlocks.set(l,foot,Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH).setValue(BedBlock.PART,BedPart.FOOT),F);
        BuildBlocks.set(l,head,Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH).setValue(BedBlock.PART,BedPart.HEAD),F);
    }
    /**
     * 0.4.423 (owner: "players should be able to jump from swings"): the yard's swing is a real one, the porch swing's seat and ropes
     * (HouseReadSwing) under a crossbar on two posts. It hangs across the yard, so it swings toward the house's path and a reader who
     * jumps off it lands on the grass, not in the chain-link. 0.4.443: it had been hung facing the yard's side fence, three blocks off,
     * which the push forward now given to every jump would reach. It faces the path, with six clear blocks of yard in front of it.
     */
    private static void swing(ServerLevel l,BlockPos b,int x,int z,boolean west){
        for(int dx:new int[]{-1,2})box(l,b,x+dx,0,z,x+dx,3,z,Blocks.OAK_FENCE);
        box(l,b,x-1,4,z,x+2,4,z,log(Blocks.STRIPPED_OAK_LOG,Direction.Axis.X));
        var rope=HouseReadRegistry.ROPE.get().defaultBlockState().setValue(ChainBlock.AXIS,Direction.Axis.Y);
        var seat=HouseReadRegistry.FITTING.get().defaultBlockState().setValue(HouseReadPropBlock.PART,HouseReadPropBlock.Part.SWING).setValue(HorizontalDirectionalBlock.FACING,Direction.NORTH);
        for(int dx=0;dx<=1;dx++){for(int y=1;y<=3;y++)BuildBlocks.set(l,b.offset(x+dx,y,z),rope,F);BuildBlocks.set(l,b.offset(x+dx,0,z),seat,F);}
    }
    private static void line(ServerLevel l,BlockPos b,int x,int z0,int z1,boolean west){
        for(int z:new int[]{z0,z1}){box(l,b,x,0,z,x,2,z,Blocks.OAK_FENCE);}
        for(int z=z1+1;z<=z0-1;z++)set(l,b,x,2,z,Blocks.TRIPWIRE);
    }
    private static void beds(ServerLevel l,BlockPos b,int x,int zc,boolean west){
        Block[] flowers={Blocks.POPPY,Blocks.DANDELION,Blocks.AZURE_BLUET,Blocks.OXEYE_DAISY,Blocks.CORNFLOWER};
        for(int z=zc-4;z<=zc-2;z++){set(l,b,x,-1,z,Blocks.COARSE_DIRT);set(l,b,x,0,z,flowers[hash(x,5,z)%flowers.length]);}
        for(int z=zc+2;z<=zc+4;z++){set(l,b,x,-1,z,Blocks.COARSE_DIRT);set(l,b,x,0,z,flowers[hash(x,6,z)%flowers.length]);}
    }
    private static void kennel(ServerLevel l,BlockPos b,int x,int z,boolean west){
        int kx=x+(west?-2:2);box(l,b,kx,0,z-1,kx+1,0,z-1,Blocks.OAK_PLANKS);box(l,b,kx,0,z-2,kx+1,1,z-2,Blocks.OAK_PLANKS);
        box(l,b,kx,1,z-1,kx+1,1,z-1,slab(Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB,SlabType.BOTTOM));set(l,b,kx,0,z-1,AIR);
    }
    /** 0.4.277: one cell of a house's wall. The rock house is laid Ozark fashion, odd brown and tan slabs in their mortar. */
    private static BlockState wallCell(Block wall,int x,int y,int z){
        if(wall==Blocks.MUD_BRICKS){int k=hash(x,y+70,z)%10;return (k<4?Blocks.MUD_BRICKS:k<7?Blocks.PACKED_MUD:k<9?Blocks.GRANITE:Blocks.COBBLESTONE).defaultBlockState();}
        return wall==Blocks.STRIPPED_OAK_LOG?log(wall,Direction.Axis.Y):wall.defaultBlockState();
    }
    /** 0.4.277: an eastern redbud: a short crooked trunk and a small round head of pink. */
    private static void redbud(ServerLevel l,BlockPos b,int x,int y0,int z){
        box(l,b,x,y0,z,x,y0+1,z,log(Blocks.DARK_OAK_LOG,Direction.Axis.Y));set(l,b,x+1,y0+2,z,log(Blocks.DARK_OAK_LOG,Direction.Axis.Y));
        for(int dx=-2;dx<=3;dx++)for(int dz=-2;dz<=2;dz++)for(int dy=2;dy<=4;dy++){
            double d=Math.pow((dx-.5)/2.4,2)+Math.pow(dz/2.2,2)+Math.pow((dy-3)/1.4,2);if(d>1||hash(x+dx,y0+dy,z+dz)%6==0)continue;
            if(dx==1&&dz==0&&dy==2)continue;set(l,b,x+dx,y0+dy,z+dz,leaves(Blocks.CHERRY_LEAVES));
        }
    }
    /** 0.4.277: a flowering dogwood: a low, spreading tree in tiers, in bloom. */
    private static void dogwood(ServerLevel l,BlockPos b,int x,int y0,int z){
        box(l,b,x,y0,z,x,y0+2,z,log(Blocks.OAK_LOG,Direction.Axis.Y));
        for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)for(int dy=2;dy<=4;dy++){
            int r=dy==4?1:2;if(Math.abs(dx)+Math.abs(dz)>r+(dy==3?1:0))continue;if(dx==0&&dz==0&&dy<3)continue;if(hash(x+dx,y0+dy,z+dz)%5==0)continue;
            set(l,b,x+dx,y0+dy,z+dz,leaves(hash(x+dx,y0+dy+3,z+dz)%3==0?Blocks.AZALEA_LEAVES:Blocks.FLOWERING_AZALEA_LEAVES));
        }
    }

    /**
     * The church at the top of the east side: a white frame Baptist church (0.4.277: under a galvanised tin roof, as Ozark country
     * churches are), its steeple over the door on the street, pews inside.
     */
    private static void chapel(ServerLevel l,BlockPos b){
        int x0=14,x1=22,z0=-59,z1=-70;
        box(l,b,x0-4,0,z1-1,x1+1,16,z0+1,AIR);
        for(int x=x0;x<=x1;x++)for(int z=z1;z<=z0;z++)set(l,b,x,-1,z,(x==x0||x==x1||z==z0||z==z1)?Blocks.STONE_BRICKS:Blocks.OAK_PLANKS);
        for(int x=x0;x<=x1;x++)for(int z=z1;z<=z0;z++){if(x!=x0&&x!=x1&&z!=z0&&z!=z1)continue;box(l,b,x,0,z,x,4,z,Blocks.WHITE_CONCRETE);}
        for(int x:new int[]{x0,x1})for(int z:new int[]{z0,z1})box(l,b,x,0,z,x,4,z,Blocks.BIRCH_PLANKS);
        // Tall windows down both sides.
        for(int z=z1+2;z<=z0-2;z+=3){box(l,b,x0,1,z,x0,3,z,Blocks.GLASS_PANE);box(l,b,x1,1,z,x1,3,z,Blocks.GLASS_PANE);}
        // Roof: steep, galvanised tin, eaves out a block.
        for(int k=0;;k++){int left=x0-1+k,right=x1+1-k,y=5+k;if(left>right)break;
            for(int z=z1-1;z<=z0+1;z++){if(left==right)set(l,b,left,y,z,slab(Blocks.POLISHED_ANDESITE_SLAB,SlabType.BOTTOM));else{set(l,b,left,y,z,stair(Blocks.POLISHED_ANDESITE_STAIRS,Direction.EAST,Half.BOTTOM));set(l,b,right,y,z,stair(Blocks.POLISHED_ANDESITE_STAIRS,Direction.WEST,Half.BOTTOM));}}
            if(k>=1)for(int x=left+1;x<=right-1;x++)for(int z:new int[]{z0,z1})set(l,b,x,y,z,Blocks.WHITE_CONCRETE.defaultBlockState());
            if(left==right)break;
        }
        // The steeple: a square tower on the street end, the door at its foot facing the street, a spire.
        int tx0=x0-3,tx1=x0-1,tz0=-63,tz1=-65;
        for(int x=tx0;x<=tx1;x++)for(int z=tz1;z<=tz0;z++){set(l,b,x,-1,z,Blocks.STONE_BRICKS);if(x!=tx0&&x!=tx1&&z!=tz0&&z!=tz1)continue;box(l,b,x,0,z,x,10,z,Blocks.WHITE_CONCRETE);}
        box(l,b,tx0+1,0,tz1+1,tx0+1,9,tz1+1,AIR);box(l,b,tx1,0,tz1+1,x0,2,tz1+1,AIR);set(l,b,x0-1,-1,tz1+1,Blocks.OAK_PLANKS);
        set(l,b,tx0+1,10,tz1+1,Blocks.DARK_OAK_PLANKS);
        NovelRooms.door(l,b.offset(tx0,0,tz1+1),Direction.WEST,Blocks.DARK_OAK_DOOR,false);
        for(int z=tz1;z<=tz0;z++)box(l,b,tx0,7,z,tx1,8,z,Blocks.WHITE_CONCRETE);box(l,b,tx0+1,7,tz1+1,tx0+1,8,tz1+1,AIR);
        set(l,b,tx0,8,tz1+1,Blocks.IRON_BARS);set(l,b,tx1,8,tz1+1,Blocks.IRON_BARS);set(l,b,tx0+1,8,tz0,Blocks.IRON_BARS);set(l,b,tx0+1,8,tz1,Blocks.IRON_BARS);
        set(l,b,tx0+1,9,tz1+1,Blocks.BELL.defaultBlockState().setValue(BellBlock.ATTACHMENT,BellAttachType.CEILING));
        box(l,b,tx0,11,tz1,tx1,11,tz0,slab(Blocks.POLISHED_ANDESITE_SLAB,SlabType.BOTTOM));set(l,b,tx0+1,11,tz1+1,Blocks.POLISHED_ANDESITE);set(l,b,tx0+1,12,tz1+1,Blocks.ANDESITE_WALL);set(l,b,tx0+1,13,tz1+1,Blocks.LIGHTNING_ROD);
        // Inside: pews either side of an aisle facing the far end, a table there, a lamp.
        // 0.4.454 (owner: "again, custom textures would be beneficial here."): oak pews, a run either side of the aisle in each row
        // (the last row stands back from the aisle for the door), their ends shaped and hymnals in the racks; they were plank stairs.
        int aisle=(x0+x1)/2;
        for(int z:new int[]{-60,-62,-66,-68}){int gap=z==-68?2:1;
            BorrascaShops.seats(l,b,new BlockPos(x0+1,0,z),new BlockPos(aisle-gap,0,z),Direction.NORTH,true);BorrascaShops.seats(l,b,new BlockPos(aisle+gap,0,z),new BlockPos(x1-1,0,z),Direction.NORTH,true);}
        NovelRooms.furniture(l,b.offset((x0+x1)/2,0,z1+1),HouseholdFurnitureBlock.Kind.READING_DESK,Direction.SOUTH);
        for(int z:new int[]{-62,-67}){box(l,b,x0,5,z,x1,5,z,log(Blocks.STRIPPED_OAK_LOG,Direction.Axis.X));set(l,b,(x0+x1)/2,4,z,hanging());}
        // The path from the street to its door, and its board.
        for(int x=5;x<tx0;x++)set(l,b,x,-1,tz1+1,BorrascaRooms.walked(x,tz1+1,1));
        // 0.4.449: the church's board at the street: white boards between two posts under a cap, lettered in black (one post and a
        // wooden sign stood here).
        for(int z:new int[]{-63,-60})box(l,b,6,0,z,6,2,z,log(Blocks.OAK_LOG,Direction.Axis.Y));
        box(l,b,6,1,-62,6,2,-61,Blocks.BIRCH_PLANKS);box(l,b,6,3,-63,6,3,-60,slab(Blocks.OAK_SLAB,SlabType.BOTTOM));
        BorrascaSigns.span(l,b.offset(5,1,-63),BorrascaSigns.Sign.CHURCH,Direction.WEST);
    }

    /** Where the street gives out: gravel for cars, a log rail, a bin and a bench, and two posts the trail leaves between. */
    private static void trailhead(ServerLevel l,BlockPos b){
        // 0.4.420: where the street ends, now at the town's north end; the woods begin past the posts.
        int d=BorrascaRooms.TOWN_END+1-(-69);var t=b.offset(0,0,d);
        for(int x=-18;x<=-6;x++)for(int z=-58;z>=-68;z--){int k=hash(x,7,z)%10;if(Math.abs(x+12)+Math.abs(z+63)>9&&k<5)continue;set(l,t,x,-1,z,(k<9?Blocks.GRAVEL:Blocks.COARSE_DIRT).defaultBlockState());}
        for(int z=-60;z>=-66;z-=3)box(l,t,-17,0,z,-15,0,z,log(Blocks.OAK_LOG,Direction.Axis.X));
        set(l,t,-7,0,-60,Blocks.BARREL);BorrascaShops.seats(l,t,new BlockPos(-7,0,-66),new BlockPos(-7,0,-67),Direction.WEST,false); // 0.4.454
        // The posts, either side of the trail where the street ends, a board across them high over the path.
        for(int x:new int[]{-4,4})box(l,t,x,0,-69,x,4,-69,log(Blocks.OAK_LOG,Direction.Axis.Y));
        box(l,t,-4,5,-69,4,5,-69,log(Blocks.STRIPPED_OAK_LOG,Direction.Axis.X));
        // 0.4.449 (owner, of the two small wooden signs that were on this beam: "Could benefit from custom signs"): the trail's name
        // burned into a plank seven blocks long on the beam, and the distance on a board that hangs from it on two chains.
        BorrascaSigns.span(l,t.offset(-3,5,-68),BorrascaSigns.Sign.ARCH,Direction.SOUTH);
        BorrascaSigns.hang(l,t.offset(0,4,-69),BorrascaSigns.Sign.ARCH_MILE,Direction.SOUTH);
    }

    /** Creosoted poles down the street's east side, a lamp on an arm over the street from each. */
    private static void poles(ServerLevel l,BlockPos b){
        for(int z:new int[]{-33,-45,-57}){
            box(l,b,5,0,z,5,6,z,log(Blocks.DARK_OAK_LOG,Direction.Axis.Y));
            box(l,b,4,6,z,6,6,z,log(Blocks.STRIPPED_DARK_OAK_LOG,Direction.Axis.X));
            set(l,b,4,5,z,hanging());
        }
    }

    /**
     * Trees along the valley's sides behind the lots and the buildings; the street, the square and the lots themselves stay open.
     * 0.4.277: the town's own trees, not the taiga's: big yard oaks, lower oaks, a red cedar in the fence line.
     */
    private static void trees(ServerLevel l,BlockPos b){
        int[][] at={{-33,-12},{-34,-38},{-31,-46},{-33,-58},{-29,-66},{33,-10},{34,-34},{31,-44},{33,-55},{29,-66},{-26,-62},{26,-73},{-9,-73},{10,-74}};
        for(var t:at){int x=t[0],z=t[1];int k=hash(x,8,z)%3;int y=Math.max(0,BorrascaLand.ground(x,z));
            if(k==0)BorrascaLand.tallOak(l,b,x,y,z,11+hash(x,9,z)%3,hash(x,11,z)%3==0);else if(k==1)BorrascaLand.oak(l,b,x,y,z,5+hash(x,10,z)%2);else BorrascaLand.cedar(l,b,x,y,z,6+hash(x,12,z)%2);
        }
    }

    // ================================================================================================ the Triple Tree

    /**
     * Three trunks out of one burl: each trunk thickened to two by two from where it leaves the burl, roots flaring out behind and to
     * the sides (never into the forks the trail takes round it at x -4 and x 3), limbs reaching out over the roof, and one crown over
     * the fort. 0.4.277: a great white oak, the tree an Ozark hollow keeps for a hundred years.
     */
    static void tree(ServerLevel l,BlockPos b){
        var wood=log(Blocks.OAK_LOG,Direction.Axis.Y);
        box(l,b,-4,3,-101,-3,16,-101,wood);box(l,b,-4,6,-100,-4,16,-100,wood);
        box(l,b,0,5,-101,1,17,-101,wood);box(l,b,1,5,-100,1,17,-100,wood);
        box(l,b,4,0,-100,5,16,-100,wood);box(l,b,5,6,-99,5,16,-99,wood);
        // 0.4.448: names on the thick of the east trunk at its foot, and on the trunks' other halves where they pass through the fort.
        for(int x=4;x<=5;x++)for(int y=0;y<=2;y++)set(l,b,x,y,-100,BorrascaNamesBlock.old(x,y,-100));
        for(int[] t:new int[][]{{-4,-100},{1,-100},{5,-99}})for(int y=6;y<=8;y++)set(l,b,t[0],y,t[1],BorrascaNamesBlock.old(t[0],y,t[1]));
        // Roots over the ground, out to the sides beyond the forks (behind the burl is the trail again, past them).
        int[][] roots={{-6,-99,0},{-7,-99,0},{-7,-100,2},{-8,-98,2},{6,-98,0},{7,-98,0},{7,-99,2},{8,-100,2},{-6,-96,0},{6,-95,0}};
        for(var r:roots)set(l,b,r[0],0,r[1],log(Blocks.OAK_LOG,r[2]==0?Direction.Axis.X:Direction.Axis.Z));
        for(int[] at:new int[][]{{-7,-101},{-8,-99},{7,-97},{8,-99},{-6,-97}})set(l,b,at[0],-1,at[1],Blocks.ROOTED_DIRT);
        // Limbs, stepping up and out over the roof.
        for(int i=0;i<5;i++){set(l,b,-5-i,13+i/2,-101+(i>2?1:0),log(Blocks.OAK_LOG,Direction.Axis.X));}
        for(int i=0;i<5;i++){set(l,b,6+i,14+i/2,-100+(i>2?1:0),log(Blocks.OAK_LOG,Direction.Axis.X));}
        for(int i=0;i<5;i++){set(l,b,0,15+i/2,-102-i,log(Blocks.OAK_LOG,Direction.Axis.Z));}
        for(int i=0;i<5;i++)set(l,b,1,16,-99+i,log(Blocks.OAK_LOG,Direction.Axis.Z));
        // The crown over them, above the roof's ridge: a clump over each trunk and one forward over the fort, broken at the edges, the
        // limbs showing through between.
        double[][] clumps={{-6,17,-101,4.6,2.8,4.4},{1,19.5,-102,4.8,2.6,4.6},{7,17.5,-98,4.4,2.7,4.2},{-1,16.5,-93,5.2,2.2,4.6},{4,20.5,-96,3.4,1.8,3.4}};
        for(var c:clumps)for(int x=(int)(c[0]-c[3]-1);x<=c[0]+c[3]+1;x++)for(int z=(int)(c[2]-c[5]-1);z<=c[2]+c[5]+1;z++)for(int y=(int)(c[1]-c[4]-1);y<=c[1]+c[4]+1;y++){
            if(y<14)continue;double d=Math.pow((x-c[0])/c[3],2)+Math.pow((y-c[1])/c[4],2)+Math.pow((z-c[2])/c[5],2);
            if(d>1+.18*((hash(x,y+50,z)%100)/50.0-1))continue;if(d>.55&&hash(x,y,z)%3==0)continue;
            var at=b.offset(x,y,z);if(!BuildBlocks.state(l,at).isAir())continue;
            BuildBlocks.set(l,at,leaves(Blocks.OAK_LEAVES),F);
        }
        // Under it: ferns and moss over the leaves, clear of the forks and the ladder.
        for(int x=-8;x<=7;x++)for(int z=-104;z<=-82;z++){
            if(Math.abs(x+4)<=1&&z>=-102&&z<=-96||Math.abs(x-3)<=1&&z>=-102&&z<=-84||x>=-3&&x<=2&&z>=-102&&z<=-97||Math.abs(x)<=2)continue;
            int k=hash(x,11,z)%14;if(k==0)set(l,b,x,0,z,Blocks.FERN);else if(k==1)set(l,b,x,0,z,Blocks.MOSS_CARPET);else if(k==2)set(l,b,x,-1,z,Blocks.MOSS_BLOCK);
        }
    }

    // ================================================================================================ the ridge depot's yard

    /** Bays on the depot's long walls, a hood over its front door, the ruts, the tailings heap, the tank on its legs, pallets and pipe. */
    static void yard(ServerLevel l,BlockPos b){
        // Steel columns in the walls' own plane every few blocks, so the long sides read as bays (doors and windows kept).
        var column=Blocks.IRON_BLOCK.defaultBlockState();
        for(int z:new int[]{-177,-184,-191,-197})box(l,b,-14,12,z,-14,16,z,column);
        for(int z:new int[]{-176,-181,-189,-197})box(l,b,-29,12,z,-29,16,z,column);
        for(int x:new int[]{-25,-20})box(l,b,x,12,-204,x,16,-204,column);
        box(l,b,-27,12,-169,-27,16,-169,column);
        // A hood over the front door on two brackets.
        box(l,b,-19,15,-168,-15,15,-167,slab(Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB,SlabType.BOTTOM));
        for(int x:new int[]{-19,-15})set(l,b,x,14,-168,Blocks.IRON_BARS);
        set(l,b,-17,14,-167,hanging());
        // Ruts from the gate to the shed's bay, and patches where the yard stays wet.
        for(int z=-168;z>=-184;z--)for(int x:new int[]{-2,2})set(l,b,x,11,z,(hash(x,12,z)%4==0?Blocks.PACKED_MUD:Blocks.COARSE_DIRT).defaultBlockState());
        for(int x=6;x<=26;x++)for(int z=-212;z<=-169;z++){double m=Math.sin(x*.3+z*.21)+.6*Math.sin(z*.17-x*.11);if(m>1.2&&hash(x,13,z)%3!=0)set(l,b,x,11,z,Blocks.COARSE_DIRT);}
        // The tailings heap, east of the shed, out of every walk and the line to the lamp: grey chat, as the lead belt's heaps are.
        for(int x=11;x<=25;x++)for(int z=-198;z<=-185;z++)for(int y=12;y<=17;y++){
            double d=Math.pow((x-18)/6.5,2)+Math.pow((z+191.5)/5.8,2)+Math.pow((y-11.5)/5.0,2);if(d>1)continue;
            int k=hash(x,y,z)%12;set(l,b,x,y,z,(k<6?Blocks.GRAVEL:k<9?Blocks.ANDESITE:k<11?Blocks.STONE:Blocks.DIORITE).defaultBlockState());
        }
        // The tank on its legs, by the east fence.
        for(int x:new int[]{19,22})for(int z:new int[]{-173,-176})box(l,b,x,12,z,x,18,z,Blocks.SPRUCE_FENCE);
        for(int x=19;x<=22;x++)for(int z=-176;z<=-173;z++)set(l,b,x,19,z,Blocks.SPRUCE_PLANKS);
        for(int y=20;y<=22;y++)for(int x=19;x<=22;x++)for(int z=-176;z<=-173;z++){if(x!=19&&x!=22&&z!=-176&&z!=-173)continue;set(l,b,x,y,z,log(Blocks.STRIPPED_SPRUCE_LOG,Direction.Axis.Y));}
        for(int x=19;x<=22;x++)for(int z=-176;z<=-173;z++)set(l,b,x,23,z,slab(Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB,SlabType.BOTTOM));
        // 0.4.449 (owner, of the ladder that ended under a closed box: "No entry"): the ladder goes on up to the lid, a hatch in the
        // lid stands open over a ladder down the inside, and the tank is dry: red silt on its floor (BorrascaRooms.TANK_SILT), from
        // the two years the town's water ran red. A no-entry disc is nailed up beside the ladder.
        for(int y=12;y<=23;y++)set(l,b,23,y,-174,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.EAST));
        set(l,b,21,23,-174,Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING,Direction.WEST).setValue(TrapDoorBlock.HALF,Half.BOTTOM).setValue(TrapDoorBlock.OPEN,true));
        for(int y=21;y<=22;y++)set(l,b,21,y,-174,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.WEST));
        for(int x=20;x<=21;x++)for(int z=-175;z<=-174;z++)set(l,b,x,20,z,Blocks.RED_SAND);
        BorrascaSigns.hang(l,b.offset(23,21,-175),BorrascaSigns.Sign.ROUNDEL,Direction.EAST);
        // 0.4.449: NO ENTRY on the wire either side of the gate, and the disc on the depot's corner at the mouth of the passage to its
        // front door.
        BorrascaSigns.span(l,b.offset(3,13,-166),BorrascaSigns.Sign.NO_ENTRY,Direction.SOUTH);BorrascaSigns.span(l,b.offset(-4,13,-166),BorrascaSigns.Sign.NO_ENTRY,Direction.SOUTH);
        BorrascaSigns.hang(l,b.offset(-13,13,-169),BorrascaSigns.Sign.ROUNDEL,Direction.EAST);
        // Pallets and a stack of pipe by the depot's east door.
        // 0.4.447 (owner's screenshot of the yard): two of these hung in the air, one a top slab over nothing and one a slab set a whole
        // block up over a half one. The corner is now a stack three boards high and the rest lie on the gravel.
        for(int[] p:new int[][]{{-8,-176},{-9,-175},{-9,-174}}){set(l,b,p[0],12,p[1],slab(Blocks.SPRUCE_SLAB,SlabType.BOTTOM));}
        set(l,b,-9,12,-176,slab(Blocks.SPRUCE_SLAB,SlabType.DOUBLE));set(l,b,-9,13,-176,slab(Blocks.SPRUCE_SLAB,SlabType.BOTTOM));
        // 0.4.440: along the refinery's east wall, outside it.
        for(int z=-194;z>=-197;z--){set(l,b,9,12,z,log(Blocks.STRIPPED_SPRUCE_LOG,Direction.Axis.X));}
        box(l,b,9,12,-199,10,12,-199,Blocks.IRON_BARS);
    }
}
