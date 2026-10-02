package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import static io.github.knaitoe.theoldesthouse.house.SceneDetailBlock.Kind.*;
import static io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock.Kind.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;

/** In-place set dressing: no actor spawns, inventory writes, rewards or story-state resets. */
public final class VignetteArchitecture {
    public static final String STATE="vignette_architecture_0427";
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private final ServerLevel l;
    private final BlockPos b;
    private final LabyrinthPlace scene;
    private boolean rugsOnly;
    private VignetteArchitecture(ServerLevel level,BlockPos base,LabyrinthPlace place){l=level;b=base;scene=place;}
    public static boolean applies(LabyrinthPlace p){return p.isVignette()||p==LabyrinthPlace.EXPLORER_CAMP;}
    public static void decorateOnce(ServerLevel l,BlockPos origin,LabyrinthPlace p){
        if(!applies(p))return;
        var d=LabyrinthData.get(l.getServer());CompoundTag done=d.state(STATE);
        String key=origin.asLong()+":"+p.id();
        BlockPos base=LabyrinthPlaces.base(origin,p);if(base==null||l.getBlockState(base.offset(0,-1,-2)).isAir())return;
        if(!done.getBoolean("Rugs:"+key)){var repair=new VignetteArchitecture(l,base,p);repair.rugsOnly=true;repair.dress();done.putBoolean("Rugs:"+key,true);d.setState(STATE,done);}
        if(!done.getBoolean(key)){new VignetteArchitecture(l,base,p).dress();done.putBoolean(key,true);d.setState(STATE,done);}
        // 0.4.28: the shell itself, once, after its furnishings exist so their supports are known.
        var shells=d.state(SceneShells.STATE);
        if(!shells.getBoolean(key)){SceneShells.apply(l,base,p);shells.putBoolean(key,true);d.setState(SceneShells.STATE,shells);}
        SceneExteriors.decorateOnce(l,origin,p);
    }
    /** Explicit structural rebuilds can dress their newly authored room again. */
    static void forget(ServerLevel l,BlockPos origin,LabyrinthPlace p){
        var d=LabyrinthData.get(l.getServer());var t=d.state(STATE);t.remove(origin.asLong()+":"+p.id());d.setState(STATE,t);
        var shells=d.state(SceneShells.STATE);shells.remove(origin.asLong()+":"+p.id());d.setState(SceneShells.STATE,shells);
        SceneExteriors.forget(l,origin,p);
    }
    private void dress(){switch(scene){
        case FLOORBOARDS->floorboards();case HIDE_AND_CLAP->childRoom();case MODEL_HOME->modelHome();
        case HARRIGAN->harrigan();case MOTHER_DEN->mother();case EXPLORER_CAMP->camp();
        case ZAMPANO_COURTYARD->archive();case WHALE->whale();case BARN_WELL->barn();
        case PLAIN->plain();case HOSPITAL->hospital();case KAREN_ROOM->karen();
        case HOLLOWAY_CAMP->holloway();case GOATMAN->trailer();case TED_CAVER->caver();
        case PRESERVED_CAVE->chapel();case DROWNED_TOWN->town();case SHALLOWS->shore(false);
        case PHONE_CANOE->shore(true);
        // This room belongs to the player: its actual copied home is its architecture.
        case RED_ROOM->{ }default->{ }
    }}
    private BlockPos p(int x,int y,int z){return b.offset(x,y,z);}
    private boolean reserved(int x,int y,int z){
        for(var door:scene.doors())if(Math.abs(x-door.rel().getX())<=1&&Math.abs(z-door.rel().getZ())<=2&&y>=door.rel().getY()-1&&y<=door.rel().getY()+2)return true;
        return storyReserved(scene,x,y,z);
    }
    /** Story volumes that set dressing and shell detailing must leave exactly as authored. */
    static boolean storyReserved(LabyrinthPlace scene,int x,int y,int z){
        return switch(scene){
            case FLOORBOARDS->x==2&&z==-6&&y<=1;
            case MODEL_HOME->x<=-10&&x>=-21&&z>=-15&&z<=-11;
            case MOTHER_DEN->(x>=-6&&x<=0&&z<=-9&&z>=-24)||(Math.abs(x)<=2&&z>=-22&&z<=-18);
            case HOSPITAL->Math.abs(x)<=2&&z>=-16&&z<=-12;
            case KAREN_ROOM->Math.abs(x)<=3&&z>=-15&&z<=-6;
            case TED_CAVER->Math.abs(x)<=1&&z<=-8;
            case PHONE_CANOE->Math.abs(x)<=3&&z<=-10;
            default->false;
        };
    }
    private void add(int x,int y,int z,BlockState s){
        if(rugsOnly)return;
        BlockPos at=p(x,y,z);
        if(!reserved(x,y,z)&&l.getBlockState(at).isAir()&&l.getBlockEntity(at)==null
                &&(s.getCollisionShape(l,at).isEmpty()||l.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,new net.minecraft.world.phys.AABB(at)).isEmpty()))l.setBlock(at,s,F);
    }
    private void add(int x,int y,int z,Block block){add(x,y,z,block.defaultBlockState());}
    /** Only authored, ordinary shell material can receive trim; functional blocks are never replaced. */
    private void trim(int x,int y,int z,Block block){
        if(rugsOnly)return;
        BlockPos at=p(x,y,z);BlockState s=l.getBlockState(at);
        if(reserved(x,y,z)||l.getBlockEntity(at)!=null)return;
        if(s.isAir()||s.is(Blocks.SPRUCE_PLANKS)||s.is(Blocks.DARK_OAK_PLANKS)||s.is(Blocks.OAK_PLANKS)||s.is(Blocks.BIRCH_PLANKS)
                ||s.is(Blocks.STONE)||s.is(Blocks.SMOOTH_STONE)||s.is(Blocks.STONE_BRICKS)||s.is(Blocks.MOSSY_STONE_BRICKS)
                ||s.is(Blocks.WHITE_TERRACOTTA)||s.is(Blocks.LIGHT_BLUE_TERRACOTTA)||s.is(Blocks.BROWN_TERRACOTTA)
                ||s.is(Blocks.LIGHT_GRAY_TERRACOTTA)||s.is(Blocks.WHITE_CONCRETE)||s.is(Blocks.LIGHT_GRAY_CONCRETE)
                ||s.is(NovelRegistry.INSTITUTE.get())||s.is(NovelRegistry.PAPER.get())
                ||(scene==LabyrinthPlace.ZAMPANO_COURTYARD&&s.is(Blocks.BARRIER)))l.setBlock(at,block.defaultBlockState(),F);
    }
    private void furniture(int x,int y,int z,HouseholdFurnitureBlock.Kind kind,Direction facing){add(x,y,z,HouseholdFurnitureBlock.state(kind,facing));}
    private void detail(int x,int y,int z,SceneDetailBlock.Kind kind,Direction facing){
        var s=SceneDetailBlock.state(kind,facing);if(SceneDetailBlock.supported(l,p(x,y,z),s))add(x,y,z,s);
    }
    private void detail(int x,int y,int z,SceneDetailBlock.Kind kind){detail(x,y,z,kind,Direction.NORTH);}
    private void table(int x,int y,int z,SceneDetailBlock.Kind kind){furniture(x,y,z,WALNUT_DESK,Direction.SOUTH);detail(x,y+1,z,kind);}
    private void cabinet(int x,int y,int z,SceneDetailBlock.Kind kind){furniture(x,y,z,CHEST_OF_DRAWERS,Direction.SOUTH);detail(x,y+1,z,kind);}
    private void wardBed(int x,int z){
        if(rugsOnly)return;
        var foot=p(x,0,z);var head=foot.north();
        if(l.getBlockState(foot).isAir()&&l.getBlockState(head).isAir()
                &&l.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,new net.minecraft.world.phys.AABB(foot)).isEmpty()
                &&l.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,new net.minecraft.world.phys.AABB(head)).isEmpty())LabyrinthBuilder.bed(l,foot,Direction.NORTH,Blocks.WHITE_BED);
    }
    private void rug(int x0,int x1,int z0,int z1,Block edge,Block centre){
        RugFloorBlock.patch(l,b,x0,x1,z0,z1,edge,centre);
    }
    private void pot(int x,int y,int z,Block plant){add(x,y,z,plant);}
    private void wainscot(int x0,int x1,int z0,int z1,int top,Block base,Block cornice){
        for(int z=z0;z<=z1;z++){trim(x0,0,z,base);trim(x1,0,z,base);trim(x0,top,z,cornice);trim(x1,top,z,cornice);}
        for(int x=x0;x<=x1;x++){trim(x,0,z0,base);trim(x,0,z1,base);trim(x,top,z0,cornice);trim(x,top,z1,cornice);}
    }
    private void pendant(int x,int y,int z,int ceiling){
        if(rugsOnly)return;
        // Repair an authored floating lantern as well as adding new supported fixtures.
        if(!l.getBlockState(p(x,ceiling,z)).isSolid())return;
        for(int yy=y+1;yy<ceiling;yy++)add(x,yy,z,Blocks.CHAIN);
        BlockPos at=p(x,y,z);if(l.getBlockState(at).is(Blocks.LANTERN))l.setBlock(at,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true),F);
        else add(x,y,z,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
    }
    private void panel(int wallX,int z0,int z1,int y0,int y1,Block glass){
        for(int z=z0;z<=z1;z++)for(int y=y0;y<=y1;y++)trim(wallX,y,z,glass);
    }
    private void floorboards(){
        wainscot(-6,6,-10,0,3,Blocks.DARK_OAK_PLANKS,Blocks.STRIPPED_DARK_OAK_WOOD);
        cabinet(4,0,-8,CROCK);table(-4,0,-3,INK_PAPERS);detail(-3,1,-9,TABLE_LAMP);
        furniture(-3,0,-4,CANE_CHAIR,Direction.NORTH);detail(-4,0,-5,SHOES);
        detail(5,2,-5,CLOCK,Direction.WEST);detail(-5,2,-8,FRAME,Direction.EAST);
        rug(-1,1,-4,-2,Blocks.BROWN_CARPET,Blocks.RED_CARPET);detail(4,0,-3,BLANKET);
        pendant(0,3,-6,4);
    }
    private void childRoom(){
        wainscot(-6,6,-13,0,3,Blocks.BIRCH_PLANKS,Blocks.STRIPPED_BIRCH_WOOD);
        cabinet(4,0,-10,TOYS);furniture(-5,0,-8,BEDSIDE_TABLE,Direction.EAST);detail(-5,1,-8,TABLE_LAMP);
        detail(-4,0,-9,SHOES);detail(5,2,-9,FRAME,Direction.WEST);
        table(4,0,-5,INK_PAPERS);detail(4,1,-8,BOOKS);pendant(0,3,-6,4);
        // The open middle remains available to the dynamically placed wardrobe and clap cues.
    }
    private void modelHome(){
        wainscot(-9,9,-19,0,5,Blocks.OAK_PLANKS,Blocks.STRIPPED_OAK_WOOD);
        furniture(6,0,-3,BLUE_SOFA,Direction.WEST);furniture(6,0,-4,BLUE_SOFA,Direction.WEST);
        table(4,0,-4,TEA_SET);cabinet(7,0,-7,VASE);detail(8,2,-5,CLOCK,Direction.WEST);
        furniture(-7,0,-4,CHEST_OF_DRAWERS,Direction.EAST);detail(-7,1,-4,FILE_TRAY);
        detail(-8,2,-3,COAT,Direction.EAST);detail(-7,0,-2,SHOES);
        cabinet(7,0,-17,DISH_RACK);cabinet(6,0,-17,CROCK);detail(5,1,-13,TEA_SET);
        furniture(2,0,-17,RADIATOR,Direction.SOUTH);cabinet(-7,0,-16,TOYS);detail(-6,0,-16,BLANKET);
        rug(-2,1,-6,-3,Blocks.GRAY_CARPET,Blocks.LIGHT_GRAY_CARPET);
        pot(8,0,-9,Blocks.POTTED_FERN);pot(-8,0,-7,Blocks.POTTED_OAK_SAPLING);
        // Nothing is added to the tree's swept yard or the animated chair stack.
    }
    private void harrigan(){
        // Earlier native builds registered this threshold but left its authored timber wall in place.
        if(l.getBlockState(p(0,0,1)).is(Blocks.DARK_OAK_PLANKS)&&l.getBlockState(p(0,1,1)).is(Blocks.DARK_OAK_PLANKS))
            LabyrinthBuilder.placeDoor(l,p(0,0,1),Direction.SOUTH);
        wainscot(-8,8,-25,1,5,Blocks.DARK_OAK_PLANKS,Blocks.STRIPPED_DARK_OAK_WOOD);
        table(6,0,-10,INK_PAPERS);cabinet(6,0,-4,TEA_SET);furniture(5,0,-6,FLORAL_ARMCHAIR,Direction.WEST);
        furniture(2,0,-9,FOOTSTOOL,Direction.NORTH);detail(-6,2,-2,FRAME,Direction.EAST);
        detail(7,2,-8,CLOCK,Direction.WEST);table(-5,0,-2,TABLE_LAMP);detail(5,0,-2,SHOES);
        rug(-2,2,-11,-4,Blocks.BROWN_CARPET,Blocks.RED_CARPET);
        for(int z:new int[]{-17,-23}){table(-5,0,z,VASE);table(5,0,z,VASE);}
        for(int z:new int[]{-16,-18})for(int x:new int[]{-5,5})furniture(x,0,z,CANE_CHAIR,Direction.NORTH);
        rug(-1,1,-18,-15,Blocks.BLACK_CARPET,Blocks.GRAY_CARPET);pendant(0,4,-6,6);pendant(0,4,-17,6);
    }
    private void mother(){
        // Cabinet backs support the original fixed frames; ledges sit between their three rows.
        for(int x:new int[]{-10,10})for(int z=-24;z<=-2;z++)for(int y=0;y<=6;y++)add(x,y,z,Blocks.DARK_OAK_PLANKS);
        for(int x=-8;x<=8;x++)for(int y=0;y<=6;y++)add(x,y,-25,Blocks.DARK_OAK_PLANKS);
        var ledge=Blocks.DARK_OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP);
        for(int y:new int[]{2,4,6}){
            for(int z=-24;z<=-2;z++)for(int x:new int[]{-9,9})add(x,y,z,ledge);
            for(int x=-8;x<=8;x++)add(x,y,-24,ledge);
        }
        for(int z:new int[]{-4,-8,-12,-20})for(int x:new int[]{-9,9})detail(x,3,z,z==-8?CROCK:BOOKS);
        for(int z:new int[]{-3,-9,-16,-24})for(int x:new int[]{-10,10})for(int y=0;y<=12;y++)trim(x,y,z,Blocks.STRIPPED_DARK_OAK_WOOD);
        for(int z:new int[]{-3,-16,-24})for(int x=-9;x<=9;x++)add(x,12,z,Blocks.DARK_OAK_PLANKS);
        cabinet(7,0,-6,BOOKS);cabinet(7,0,-7,CROCK);table(-7,0,-4,TEA_SET);
        detail(6,0,-3,BLANKET);detail(-7,0,-6,CRATE);detail(7,0,-15,SATCHEL);
        table(7,0,-22,TABLE_LAMP);pendant(0,11,-6,14);pendant(6,10,-18,14);
    }
    private void camp(){
        table(-4,0,-12,TOOLS);detail(-4,1,-7,ROPE_COIL);detail(4,1,-9,SATCHEL);
        detail(4,0,-12,CRATE);detail(3,0,-13,BLANKET);detail(-4,0,-10,SHOES);
        for(int x:new int[]{-4,4})add(x,0,-5,Blocks.OAK_LOG);
        for(int z=-12;z<=-10;z++)add(4,2,z,Blocks.BROWN_WOOL);
        for(int y=0;y<=2;y++)add(4,y,-13,Blocks.SPRUCE_FENCE);
        pendant(-4,4,-3,5);
    }
    private void archive(){
        // Continuous apartment facades frame the courtyard, with recessed windows and cornices.
        for(int x:new int[]{-15,15})for(int z=-17;z<=-2;z++)for(int y=0;y<=9;y++){
            if((z+2)%5==0)trim(x,y,z,Blocks.STONE_BRICKS);
            else trim(x,y,z,y==0||y==5||y==9?Blocks.POLISHED_ANDESITE:Blocks.BRICKS);
        }
        for(int x:new int[]{-15,15})for(int z:new int[]{-4,-9,-14})for(int y:new int[]{2,3,7,8})trim(x,y,z,Blocks.LIGHT_GRAY_STAINED_GLASS);
        for(int x:new int[]{-14,14})for(int z=-17;z<=-2;z++)add(x,6,z,Blocks.STONE_BRICK_SLAB);
        for(int x:new int[]{-8,8}){furniture(x,0,-10,CANE_CHAIR,Direction.SOUTH);table(x,0,-12,VASE);}
        table(-8,0,-34,INK_PAPERS);table(-7,0,-34,BOOKS);table(-6,0,-34,TABLE_LAMP);
        furniture(-7,0,-32,CANE_CHAIR,Direction.NORTH);cabinet(8,0,-37,FILE_TRAY);cabinet(7,0,-37,BOOKS);
        cabinet(-10,0,-24,FILE_TRAY);detail(-10,0,-27,CRATE);detail(-9,0,-27,BOOKS);
        for(int z:new int[]{-26,-32,-36})for(int y=0;y<=2;y++)add(-11,y,z,Blocks.BOOKSHELF);
        for(int z:new int[]{-27,-33}){
            table(-3,0,z,INK_PAPERS);table(-2,0,z,BOOKS);table(2,0,z,FILE_TRAY);table(3,0,z,BOOKS);
            furniture(-3,0,z+2,CANE_CHAIR,Direction.NORTH);furniture(3,0,z+2,CANE_CHAIR,Direction.NORTH);
        }
        rug(-4,3,-35,-25,Blocks.BROWN_CARPET,Blocks.GRAY_CARPET);
        detail(11,2,-37,CLOCK,Direction.WEST);detail(-11,2,-21,COAT,Direction.EAST);
        pendant(-7,5,-25,8);pendant(4,5,-28,8);
        for(int z:new int[]{-6,-14}){table(-13,0,z,TABLE_LAMP);table(13,0,z,VASE);}
    }
    private void whale(){
        wainscot(-13,13,-32,0,4,Blocks.POLISHED_ANDESITE,Blocks.STONE_BRICKS);
        for(int z:new int[]{-5,-11,-27})panel(13,z,z+1,2,3,Blocks.LIGHT_BLUE_STAINED_GLASS);
        table(-10,0,-26,INK_PAPERS);table(-9,0,-26,BOOKS);furniture(-10,0,-24,CANE_CHAIR,Direction.NORTH);
        cabinet(9,0,-25,TOWELS);furniture(10,0,-27,CHEST_OF_DRAWERS,Direction.SOUTH);detail(10,1,-27,BLANKET);
        furniture(10,0,-16,RADIATOR,Direction.WEST);furniture(-10,0,-6,FORMICA_TABLE,Direction.SOUTH);detail(-10,1,-6,TEA_SET);
        furniture(-10,0,-4,KITCHEN_STOOL,Direction.NORTH);cabinet(-11,0,-10,DISH_RACK);detail(12,2,-16,CLOCK,Direction.WEST);
        for(int z:new int[]{-4,-8,-12,-16})for(int y=0;y<=2;y++)add(-12,y,z,Blocks.BOOKSHELF);
        for(int x=-7;x<=-4;x++){furniture(x,0,-11,FORMICA_TABLE,Direction.SOUTH);detail(x,1,-11,x%2==0?INK_PAPERS:BOOKS);}
        furniture(-7,0,-9,CANE_CHAIR,Direction.NORTH);furniture(-5,0,-9,CANE_CHAIR,Direction.NORTH);
        table(7,0,-14,TEA_SET);furniture(7,0,-12,GREEN_ARMCHAIR,Direction.NORTH);furniture(9,0,-14,GREEN_ARMCHAIR,Direction.WEST);
        rug(5,10,-17,-11,Blocks.GRAY_CARPET,Blocks.LIGHT_GRAY_CARPET);
        rug(4,8,-25,-21,Blocks.GRAY_CARPET,Blocks.LIGHT_GRAY_CARPET);
        for(int y:new int[]{5,8,11}){
            table(-11,y,-28,BOOKS);detail(-7,y,-27,CRATE);detail(-7,y,-23,SATCHEL);
            for(int z=-28;z<=-21;z++)add(-12,y+1,z,Blocks.DARK_OAK_SLAB);
        }
        for(int z:new int[]{-5,-15,-26})pendant(8,4,z,5);
    }
    private void barn(){
        for(int z:new int[]{-20,-25,-31})for(int x=6;x<=14;x++)add(x,4,z,Blocks.SPRUCE_PLANKS);
        cabinet(6,0,-19,TOOLS);table(7,0,-19,ROPE_COIL);detail(8,0,-20,FEED_SACK);
        detail(14,0,-22,FEED_SACK);detail(14,0,-24,CRATE);detail(6,0,-28,FEED_SACK);
        detail(14,2,-26,COAT,Direction.WEST);pendant(12,3,-25,4);
        for(int z=-14;z<=-5;z+=3){add(11,0,z,Blocks.OAK_FENCE);add(15,0,z,Blocks.OAK_FENCE);}
        for(int x=11;x<=15;x++)add(x,0,-5,Blocks.OAK_FENCE);
        for(int x:new int[]{-12,-9}){trim(x,-1,-10,Blocks.COARSE_DIRT);detail(x,0,-10,CRATE);}
        detail(-5,0,-18,SHOES);table(-10,0,-30,TOOLS);detail(-9,0,-30,ROPE_COIL);
    }
    private void plain(){
        // Continuous distant ground hides the old empty seam beneath the isolated figure.
        for(int x=-30;x<=30;x++)for(int z=-105;z<=-65;z++){
            add(x,-1,z,Blocks.SANDSTONE);
            if(Math.abs(x)>=28)for(int y=0;y<=1+Math.floorMod(x+z,3);y++)add(x,y,z,Blocks.SANDSTONE);
        }
        // A used survey rest at the edge; the figure's open horizon remains the composition.
        add(-7,0,-8,Blocks.SMOOTH_SANDSTONE);add(-7,0,-9,Blocks.SMOOTH_SANDSTONE);
        detail(-7,1,-8,SATCHEL);detail(-7,1,-9,TOOLS);detail(4,0,-8,ROPE_COIL);
        for(int z=-58;z<=-16;z+=11)for(int x:new int[]{-24,24}){
            trim(x,-1,z,Blocks.SANDSTONE);add(x,0,z,Blocks.SANDSTONE_SLAB);add(x+1,0,z-1,Blocks.DEAD_BUSH);
        }
        for(int x=-28;x<=28;x++)if(Math.abs(x)>14)for(int z=-63;z<=-12;z++)if(Math.floorMod(x*17+z*31,37)==0)add(x,0,z,Blocks.SANDSTONE_SLAB);
    }
    private void hospital(){
        wainscot(-9,9,-23,0,4,Blocks.LIGHT_GRAY_CONCRETE,Blocks.WHITE_CONCRETE);
        for(int z:new int[]{-8,-18})panel(-9,z-1,z+1,2,3,Blocks.LIGHT_BLUE_STAINED_GLASS);
        for(int z:new int[]{-7,-19}){
            // Beds occupy side bays, leaving a generous central ward and the original incubator.
            wardBed(-7,z);
            cabinet(-5,0,z,TOWELS);table(-8,0,z-2,MEDICAL_TRAY);
        }
        for(int z:new int[]{-7,-17}){
            wardBed(6,z);
            cabinet(4,0,z,TOWELS);
        }
        for(int z:new int[]{-11,-15})for(int x=-9;x<=-5;x++)add(x,3,z,Blocks.WHITE_WOOL);
        table(7,0,-21,FILE_TRAY);table(6,0,-21,MEDICAL_TRAY);furniture(7,0,-19,KITCHEN_STOOL,Direction.NORTH);
        cabinet(7,0,-7,MEDICAL_TRAY);furniture(8,0,-5,RADIATOR,Direction.WEST);
        detail(8,2,-12,CLOCK,Direction.WEST);detail(-8,0,-3,DUSTPAN);
        for(int z:new int[]{-10,-19}){add(-1,4,z,Blocks.IRON_BLOCK);add(1,4,z,Blocks.IRON_BLOCK);}
    }
    private void karen(){
        wainscot(-9,9,-18,0,4,Blocks.OAK_PLANKS,Blocks.STRIPPED_BIRCH_WOOD);
        table(-6,0,-8,TEA_SET);cabinet(-7,0,-12,BOOKS);table(-7,0,-5,TABLE_LAMP);
        furniture(-4,0,-10,FOOTSTOOL,Direction.NORTH);cabinet(7,0,-14,BLANKET);
        table(7,0,-5,FILE_TRAY);detail(6,0,-6,SATCHEL);detail(7,0,-2,SHOES);
        detail(8,2,-8,FRAME,Direction.WEST);detail(-8,2,-3,COAT,Direction.EAST);
        pot(-8,0,-16,Blocks.POTTED_OAK_SAPLING);pot(8,0,-17,Blocks.POTTED_FERN);
        rug(-8,-4,-12,-5,Blocks.BROWN_CARPET,Blocks.GRAY_CARPET);rug(4,7,-13,-8,Blocks.GRAY_CARPET,Blocks.LIGHT_GRAY_CARPET);
        pendant(-6,4,-8,5);pendant(6,4,-8,5);
    }
    private void holloway(){
        for(var door:scene.doors()){
            var at=b.offset(door.rel());var wall=door.name().equals("entry")?Blocks.DIRT:Blocks.STONE_BRICKS;
            if(l.getBlockState(at).is(wall)&&l.getBlockState(at.above()).is(wall))LabyrinthBuilder.placeDoor(l,at,door.facing());
        }
        table(5,0,-9,TOOLS);detail(-4,1,-7,SATCHEL);detail(-5,0,-5,SHOES);detail(4,0,-3,ROPE_COIL);
        cabinet(-5,0,-10,BLANKET);detail(5,0,-5,CRATE);
        // The hunting chambers stay open; debris collects against their boundaries.
        for(int z:new int[]{-16,-22}){add(-9,0,z,Blocks.MOSSY_STONE_BRICK_SLAB);add(9,0,z,Blocks.CRACKED_STONE_BRICKS);}
        for(int z:new int[]{-27,-35,-44}){
            for(int y=0;y<=4;y++){trim(-8,y,z,Blocks.STRIPPED_SPRUCE_WOOD);trim(8,y,z,Blocks.STRIPPED_SPRUCE_WOOD);}
            detail(-7,0,z,CRATE);
        }
        for(int z:new int[]{-51,-61,-68}){add(9,0,z,Blocks.TUFF_SLAB);add(-9,0,z,Blocks.COBBLESTONE_SLAB);}
        pendant(4,3,-8,5);
    }
    private void trailer(){
        // Peripheral cupboards and external chassis, outside the dynamic family seating/bunks.
        for(int x=-7;x<=7;x++){add(x,6,-78,Blocks.SMOOTH_QUARTZ_SLAB);add(x,6,-54,Blocks.SMOOTH_QUARTZ_SLAB);}
        for(int z:new int[]{-60,-72})for(int x:new int[]{-7,7})add(x,0,z,Blocks.POLISHED_BLACKSTONE);
        for(int x:new int[]{-6,-4,4,6})furniture(x,1,-76,CHEST_OF_DRAWERS,Direction.SOUTH);
        detail(-4,2,-76,DISH_RACK);detail(4,2,-76,CROCK);detail(6,2,-76,TEA_SET);
        detail(7,3,-75,CLOCK,Direction.WEST);detail(-7,3,-56,COAT,Direction.EAST);
        detail(6,0,-51,FEED_SACK);detail(9,0,-50,CRATE);
        for(int z:new int[]{-63,-71}){add(0,5,z,Blocks.SEA_LANTERN);add(-1,5,z,Blocks.IRON_TRAPDOOR);add(1,5,z,Blocks.IRON_TRAPDOOR);}
        detail(7,1,-76,BLANKET);
    }
    private void caver(){
        detail(3,1,-4,TOOLS);detail(-4,0,-3,SATCHEL);detail(-4,0,-5,ROPE_COIL);
        detail(4,0,-5,BLANKET);detail(-3,0,-2,SHOES);
        for(int z:new int[]{-36,-43}){add(-6,0,z,Blocks.CALCITE);add(-6,1,z,Blocks.POINTED_DRIPSTONE);}
        if(l.getBlockState(p(5,5,-41)).isSolid())add(5,4,-41,Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION,Direction.DOWN));
    }
    private void chapel(){
        for(int z:new int[]{-13,-22,-31,-39})for(int x:new int[]{-12,12}){
            add(x,0,z,Blocks.TUFF);add(x,1,z,Blocks.MOSSY_COBBLESTONE);
        }
        table(-10,0,-17,HYMNALS);table(10,0,-32,HYMNALS);detail(-11,0,-40,ROPE_COIL);
        for(int x:new int[]{-7,7})for(int z:new int[]{-14,-26,-38}){
            for(int y=7;y>=4;y--)if(l.getBlockState(p(x,y+1,z)).isSolid())add(x,y,z,Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION,Direction.DOWN));
        }
        // Moisture and rubble stay at the perimeter, away from the pews and canoe alcove.
        for(int z=-35;z<=-15;z+=5)for(int x:new int[]{-11,11})trim(x,-1,z,Blocks.MOSSY_COBBLESTONE);
    }
    private void town(){
        // West-bank rooms were raised by the lake migration; all new furniture uses their dry floor.
        table(-24,0,-31,INK_PAPERS);table(-20,0,-31,BOOKS);table(-24,0,-35,BOOKS);
        furniture(-24,0,-29,CANE_CHAIR,Direction.NORTH);furniture(-20,0,-29,CANE_CHAIR,Direction.NORTH);
        cabinet(-7,0,-47,FILE_TRAY);cabinet(-6,0,-47,CROCK);detail(-9,0,-47,CRATE);
        for(int z:new int[]{-18,-28,-38,-54}){
            trim(-3,-1,z,Blocks.MOSSY_STONE_BRICKS);add(-3,0,z,Blocks.POTTED_FERN);
        }
        table(-6,0,-16,TOOLS);detail(-7,0,-16,ROPE_COIL);
        for(int z:new int[]{-19,-26,-43})for(int x:new int[]{20,24}){
            if(l.getBlockState(p(x,-3,z)).is(Blocks.WATER))add(x,-3,z,Blocks.SEAGRASS);
        }
        // Submerged sanctuary keeps its required swimming channels and hatch entirely clear.
        for(int z:new int[]{-43,-53})trim(9,-12,z,Blocks.MOSSY_STONE_BRICKS);
    }
    private void shore(boolean phone){
        int bank=phone?-7:-10;
        table(phone?-6:-11,0,bank,TOOLS);detail(phone?-7:-12,0,bank,ROPE_COIL);
        detail(phone?7:12,0,bank,CRATE);detail(phone?8:13,0,bank,FEED_SACK);
        for(int x:new int[]{-13,-8,8,13})for(int z:new int[]{-3,-6}){
            if(!l.getBlockState(p(x,-1,z)).is(Blocks.WATER)&&l.getBlockState(p(x,0,z)).isAir())add(x,0,z,Blocks.SHORT_GRASS);
        }
        for(int x:new int[]{-15,15}){add(x,0,-8,Blocks.MOSSY_COBBLESTONE_SLAB);add(x,0,-9,Blocks.MOSS_CARPET);}
        // The pier/canoe lane, thrown shoe route and living grass refuges are untouched.
    }
}
