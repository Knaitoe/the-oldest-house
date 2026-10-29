package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.*;

/** Four ordinary lives behind four existing fragments. The other fragments end where they did. */
public final class DomesticLives {
    public static final String STATE="domestic_lives_0128";
    public record Home(String id,LabyrinthPlace hall,int x0,int x1,int z0,int z1,BlockPos opening,String paper){
        public BoundingBox local(){return new BoundingBox(x0,-1,z0,x1,4,z1);}
        public AABB area(BlockPos b){return new AABB(b.getX()+x0,b.getY()-1,b.getZ()+z0,b.getX()+x1+1,b.getY()+5,b.getZ()+z1+1);}
    }
    public static final List<Home> HOMES=List.of(
            new Home("family",LabyrinthPlace.STRAIGHT_HALL,-20,-7,-26,-14,new BlockPos(-7,0,-20),"family_paper"),
            new Home("students",LabyrinthPlace.BENT_HALL,7,20,-18,-7,new BlockPos(7,0,-14),"student_paper"),
            new Home("drinker",LabyrinthPlace.BENT_HALL,-8,1,-36,-24,new BlockPos(-8,0,-29),"smoked_paper"),
            new Home("grandmother",LabyrinthPlace.CROSS_HALL,-19,-8,-8,3,new BlockPos(-10,0,-8),"rose_paper"));
    /** A fixed discovery for this world, rather than extending most ordinary snippets. */
    public static Set<String> selection(long seed){
        var random=new Random(seed^0x4d3e91b80c92a167L);var rows=new ArrayList<>(HOMES);Collections.shuffle(rows,random);
        var chosen=new LinkedHashSet<String>();var first=rows.getFirst();chosen.add(first.id);
        if(random.nextInt(4)==0)for(var h:rows)if(h.hall!=first.hall){chosen.add(h.id);break;}
        return Set.copyOf(chosen);
    }
    public static boolean selected(ServerLevel l,BlockPos b,Home h){
        // Never seal an extension already made in 0.4.128, including on an old save.
        var state=LabyrinthData.get(l.getServer()).state(STATE);String key=key(b,h);
        return state.getBoolean(key)||state.getBoolean(key+":chosen")||state.contains(key+":cursor")||selection(l.getSeed()).contains(h.id);
    }
    public static BlockPos approach(Home h){return switch(h.id){
        case "family"->new BlockPos(-1,0,-18);case "students"->new BlockPos(1,0,-12);
        case "drinker"->new BlockPos(-14,0,-27);default->new BlockPos(-8,0,-14);
    };}
    public static BoundingBox workBounds(Home h){
        var a=approach(h);return new BoundingBox(Math.min(h.x0,a.getX()-1),-1,Math.min(h.z0,a.getZ()-1),Math.max(h.x1,a.getX()+1),4,Math.max(h.z1,a.getZ()+1));
    }
    public static AABB workArea(BlockPos b,Home h){var a=workBounds(h);return new AABB(b.getX()+a.minX(),b.getY()-1,b.getZ()+a.minZ(),b.getX()+a.maxX()+1,b.getY()+5,b.getZ()+a.maxZ()+1);}
    public record Edit(BlockPos at,BlockState next){}
    private static final Map<String,List<Edit>> PLANS=new HashMap<>();
    private DomesticLives(){}
    public static BoundingBox extend(LabyrinthPlace p,BoundingBox old){
        int x0=old.minX(),x1=old.maxX(),z0=old.minZ(),z1=old.maxZ(),y0=old.minY(),y1=old.maxY();
        for(var h:HOMES)if(h.hall==p){x0=Math.min(x0,h.x0);x1=Math.max(x1,h.x1);z0=Math.min(z0,h.z0);z1=Math.max(z1,h.z1);y0=Math.min(y0,-1);y1=Math.max(y1,4);}
        return new BoundingBox(x0,y0,z0,x1,y1,z1);
    }
    public static String key(BlockPos base,Home h){return h.id+":"+base.asLong();}
    private static void put(Map<BlockPos,BlockState> e,int x,int y,int z,BlockState s){e.put(new BlockPos(x,y,z),s);}
    private static void fit(Map<BlockPos,BlockState> e,int x,int y,int z,UncannyFurnitureBlock.Kind k,Direction f){put(e,x,y,z,UncannyFurnitureBlock.of(k,f));}
    private static void fit(Map<BlockPos,BlockState> e,int x,int y,int z,UncannyFurnitureBlock.Kind k){fit(e,x,y,z,k,Direction.NORTH);}
    private static void handled(Map<BlockPos,BlockState> e,int x,int y,int z,DomesticFittingBlock.Kind k){put(e,x,y,z,DomesticFittingBlock.of(k,Direction.NORTH));}
    private static void handled(Map<BlockPos,BlockState> e,int x,int y,int z,DomesticFittingBlock.Kind k,Direction f){put(e,x,y,z,DomesticFittingBlock.of(k,f));}
    private static void common(Map<BlockPos,BlockState> e,int x,int y,int z,HouseholdFurnitureBlock.Kind k,Direction f){put(e,x,y,z,HouseBlocks.HOUSEHOLD_FURNITURE.get().defaultBlockState().setValue(HouseholdFurnitureBlock.KIND,k).setValue(HouseholdFurnitureBlock.FACING,f));}
    /** 0.4.170: the household's own fridge-freezer, its back to the wall {@code f}: the fridge below with its food, the freezer above. */
    private static void fridge(Map<BlockPos,BlockState> e,int x,int z,DomesticFittingBlock.Kind k,Direction f){handled(e,x,0,z,k,f);handled(e,x,1,z,DomesticFittingBlock.Kind.FRIDGE_TOP,f);}
    private static void closet(Map<BlockPos,BlockState> e,int x,int z,Direction f){handled(e,x,0,z,DomesticFittingBlock.Kind.CLOSET_LOWER,f);handled(e,x,1,z,DomesticFittingBlock.Kind.CLOSET_UPPER,f);}
    private static void interior(Map<BlockPos,BlockState> e,int x,int y,int z,VignetteInteriorBlock.Kind k,Direction f){put(e,x,y,z,VignetteInteriorBlock.of(k,f));}
    private static void wallX(Map<BlockPos,BlockState> e,Home h,int x,int z0,int z1,int... gaps){
        for(int z=z0;z<=z1;z++)for(int y=0;y<4;y++){boolean gap=false;for(int g:gaps)if(g==z)gap=true;put(e,x,y,z,gap?Blocks.AIR.defaultBlockState():DomesticRegistry.material(h.paper).defaultBlockState());}
    }
    private static void wallZ(Map<BlockPos,BlockState> e,Home h,int z,int x0,int x1,int... gaps){
        for(int x=x0;x<=x1;x++)for(int y=0;y<4;y++){boolean gap=false;for(int g:gaps)if(g==x)gap=true;put(e,x,y,z,gap?Blocks.AIR.defaultBlockState():DomesticRegistry.material(h.paper).defaultBlockState());}
    }
    private static void bed(Map<BlockPos,BlockState> e,int x,int z,Block type){
        var s=type.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH);put(e,x,0,z,s.setValue(BedBlock.PART,BedPart.FOOT));put(e,x,0,z-1,s.setValue(BedBlock.PART,BedPart.HEAD));
    }
    private static void lamp(Map<BlockPos,BlockState> e,int x,int z){put(e,x,3,z,Blocks.LANTERN.defaultBlockState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HANGING,true));}
    private static void window(Map<BlockPos,BlockState> e,int x,int z,Direction inward){
        fit(e,x,1,z,UncannyFurnitureBlock.Kind.WINDOW_FRAME,inward);
        var behind=new BlockPos(x,1,z).relative(inward.getOpposite());
        put(e,behind.getX(),behind.getY(),behind.getZ(),Blocks.BLACK_WOOL.defaultBlockState());
    }
    public static List<Edit> plan(Home h){return PLANS.computeIfAbsent(h.id,ignored->{
        var e=new LinkedHashMap<BlockPos,BlockState>();
        for(int y=-1;y<=4;y++)for(int z=h.z0;z<=h.z1;z++)for(int x=h.x0;x<=h.x1;x++){
            boolean edge=x==h.x0||x==h.x1||z==h.z0||z==h.z1;
            var s=y==-1?DomesticPalette.floor(h.id,x,z):y==4?Blocks.BIRCH_PLANKS.defaultBlockState():edge?DomesticRegistry.material(h.paper).defaultBlockState():Blocks.AIR.defaultBlockState();
            put(e,x,y,z,s);
        }
        switch(h.id){
            case "family"->{
                wallX(e,h,-14,-25,-15,-22,-18);wallZ(e,h,-20,-19,-15);
                for(int i=0;i<3;i++)bed(e,-19+i*2,-24,new Block[]{Blocks.ORANGE_BED,Blocks.LIGHT_BLUE_BED,Blocks.PURPLE_BED}[i]);
                fit(e,-18,0,-21,UncannyFurnitureBlock.Kind.WARDROBE);interior(e,-16,0,-21,VignetteInteriorBlock.Kind.TOY_CUPBOARD,Direction.SOUTH);
                bed(e,-18,-18,Blocks.GREEN_BED);bed(e,-17,-18,Blocks.GREEN_BED);
                closet(e,-19,-16,Direction.EAST);handled(e,-18,0,-15,DomesticFittingBlock.Kind.PACKED_CLOTHES);
                fit(e,-19,0,-19,UncannyFurnitureBlock.Kind.BEDSIDE);handled(e,-19,1,-19,DomesticFittingBlock.Kind.PHOTO_FAMILY);fit(e,-16,0,-19,UncannyFurnitureBlock.Kind.BEDSIDE);fit(e,-16,1,-19,UncannyFurnitureBlock.Kind.DESK_LAMP);fit(e,-15,0,-16,UncannyFurnitureBlock.Kind.WARDROBE,Direction.WEST);
                interior(e,-15,1,-21,VignetteInteriorBlock.Kind.HILL_HEIGHT_MARKS,Direction.WEST);
                for(int x=-13;x<=-9;x++)interior(e,x,0,-25,VignetteInteriorBlock.Kind.HOLIDAY_LARDER,Direction.NORTH);
                interior(e,-12,0,-25,VignetteInteriorBlock.Kind.HOLIDAY_SINK,Direction.NORTH);fridge(e,-8,-25,DomesticFittingBlock.Kind.FRIDGE_FAMILY,Direction.NORTH);handled(e,-13,1,-25,DomesticFittingBlock.Kind.BREAKFAST);
                for(int x=-11;x<=-10;x++)common(e,x,0,-22,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.NORTH);
                for(int x:new int[]{-11,-10}){common(e,x,0,-21,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH);common(e,x,0,-23,HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.SOUTH);}
                common(e,-9,0,-22,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.WEST);
                fit(e,-12,0,-15,UncannyFurnitureBlock.Kind.SOFA_LEFT);fit(e,-11,0,-15,UncannyFurnitureBlock.Kind.SOFA_RIGHT);fit(e,-11,0,-17,UncannyFurnitureBlock.Kind.COFFEE_TABLE);
                window(e,-8,-17,Direction.WEST);lamp(e,-11,-19);lamp(e,-17,-22);lamp(e,-17,-17);
            }
            case "students"->{
                wallX(e,h,14,-17,-8,-15,-10);wallZ(e,h,-13,15,19);
                bed(e,16,-16,Blocks.BLUE_BED);bed(e,19,-16,Blocks.RED_BED);bed(e,18,-10,Blocks.BLACK_BED);
                fit(e,15,0,-14,UncannyFurnitureBlock.Kind.DESK);put(e,15,1,-14,DomesticFittingBlock.of(DomesticFittingBlock.Kind.DESK_REMNANTS,Direction.NORTH).setValue(DomesticFittingBlock.STAGE,1));
                fit(e,19,0,-14,UncannyFurnitureBlock.Kind.BOOKCASE);handled(e,17,0,-14,DomesticFittingBlock.Kind.BOOK_BOX);
                fit(e,15,0,-8,UncannyFurnitureBlock.Kind.DESK);fit(e,15,1,-8,UncannyFurnitureBlock.Kind.DESK_LAMP);handled(e,19,0,-8,DomesticFittingBlock.Kind.PACKED_CLOTHES);
                closet(e,19,-12,Direction.WEST);handled(e,12,0,-17,DomesticFittingBlock.Kind.RECORD_PLAYER);fit(e,13,0,-17,UncannyFurnitureBlock.Kind.BOOKCASE);
                for(int x=8;x<=10;x++)interior(e,x,0,-17,VignetteInteriorBlock.Kind.TRAILER_CUPBOARD,Direction.NORTH);
                interior(e,8,0,-17,VignetteInteriorBlock.Kind.TRAILER_SINK,Direction.NORTH);fridge(e,11,-17,DomesticFittingBlock.Kind.FRIDGE_STUDENTS,Direction.NORTH);
                common(e,10,0,-14,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.NORTH);common(e,9,0,-14,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.EAST);common(e,10,0,-13,HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.NORTH);
                fit(e,9,0,-8,UncannyFurnitureBlock.Kind.SOFA_LEFT);fit(e,10,0,-8,UncannyFurnitureBlock.Kind.SOFA_RIGHT);fit(e,10,0,-10,UncannyFurnitureBlock.Kind.COFFEE_TABLE);
                handled(e,8,0,-9,DomesticFittingBlock.Kind.EMPTY_BOTTLES);window(e,19,-9,Direction.WEST);lamp(e,11,-13);lamp(e,17,-14);lamp(e,17,-9);
            }
            case "drinker"->{
                wallZ(e,h,-32,-7,0,-2,-1);
                bed(e,-6,-34,Blocks.WHITE_BED);fit(e,-3,0,-35,UncannyFurnitureBlock.Kind.WARDROBE,Direction.SOUTH);
                fit(e,-7,0,-34,UncannyFurnitureBlock.Kind.BEDSIDE);handled(e,-7,1,-34,DomesticFittingBlock.Kind.FOLDED_SHIRT);
                for(int z=-31;z<=-29;z++)interior(e,0,0,z,VignetteInteriorBlock.Kind.TRAILER_CUPBOARD,Direction.WEST);
                interior(e,0,0,-30,VignetteInteriorBlock.Kind.TRAILER_SINK,Direction.WEST);handled(e,0,1,-29,DomesticFittingBlock.Kind.DIRTY_DISHES);fridge(e,0,-28,DomesticFittingBlock.Kind.FRIDGE_DRINKER,Direction.EAST);
                common(e,-1,0,-26,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.SOUTH);fit(e,-2,0,-28,UncannyFurnitureBlock.Kind.COFFEE_TABLE);
                handled(e,0,0,-26,DomesticFittingBlock.Kind.EMPTY_BOTTLES);handled(e,-7,0,-28,DomesticFittingBlock.Kind.BOTTLE_RETURNS);
                fit(e,-5,0,-25,UncannyFurnitureBlock.Kind.BEDSIDE);window(e,0,-27,Direction.WEST);lamp(e,-4,-29);lamp(e,-3,-34);
            }
            case "grandmother"->{
                wallX(e,h,-14,-7,2,-4,0);wallZ(e,h,-2,-18,-15);
                bed(e,-18,-5,Blocks.PINK_BED);fit(e,-15,0,-7,UncannyFurnitureBlock.Kind.WARDROBE,Direction.SOUTH);fit(e,-17,0,-6,UncannyFurnitureBlock.Kind.BEDSIDE);fit(e,-17,1,-6,UncannyFurnitureBlock.Kind.DESK_LAMP);
                common(e,-18,0,0,HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,Direction.SOUTH);common(e,-16,0,0,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.SOUTH);
                fit(e,-17,0,2,UncannyFurnitureBlock.Kind.COUNTER);handled(e,-17,1,2,DomesticFittingBlock.Kind.RADIO,Direction.SOUTH);
                fit(e,-16,0,2,UncannyFurnitureBlock.Kind.COUNTER);fit(e,-15,0,2,UncannyFurnitureBlock.Kind.SIDEBOARD);fit(e,-15,1,2,UncannyFurnitureBlock.Kind.CUP);interior(e,-15,0,1,VignetteInteriorBlock.Kind.SEWING_BASKET,Direction.SOUTH);window(e,-18,1,Direction.EAST);
                fit(e,-15,0,-3,UncannyFurnitureBlock.Kind.BEDSIDE);put(e,-15,1,-3,DomesticFittingBlock.of(DomesticFittingBlock.Kind.PHOTO_COUPLE,Direction.SOUTH).setValue(DomesticFittingBlock.STAGE,1));
                for(int x=-13;x<=-11;x++)interior(e,x,0,-7,VignetteInteriorBlock.Kind.HOLIDAY_LARDER,Direction.NORTH);
                interior(e,-13,0,-7,VignetteInteriorBlock.Kind.HOLIDAY_SINK,Direction.NORTH);fridge(e,-9,-5,DomesticFittingBlock.Kind.FRIDGE_GRANDMOTHER,Direction.EAST);
                common(e,-11,0,-2,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.NORTH);common(e,-12,0,-2,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.EAST);common(e,-11,0,-1,HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.NORTH);
                lamp(e,-11,-5);lamp(e,-16,-4);lamp(e,-16,0);
            }
            default->throw new IllegalArgumentException(h.id);
        }
        // Extend a free edge of the snippet into a two-wide passage, around its
        // original counter/bed and finite container, rather than through them.
        int x0,x1,z0,z1;boolean sideways=!h.id.equals("grandmother");
        switch(h.id){
            case "family"->{x0=-6;x1=-3;z0=-21;z1=-21;}
            case "students"->{x0=3;x1=6;z0=-15;z1=-15;}
            case "drinker"->{x0=-12;x1=-9;z0=-30;z1=-30;}
            default->{x0=-11;x1=-11;z0=-12;z1=-9;}
        }
        for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++){
            put(e,x,-1,z,DomesticPalette.entry(h.id,x,z));
            for(int y=0;y<4;y++)put(e,x,y,z,Blocks.AIR.defaultBlockState());put(e,x,4,z,Blocks.BIRCH_PLANKS.defaultBlockState());
            for(int y=0;y<4;y++)put(e,sideways?x:x-1,y,sideways?z-1:z,DomesticRegistry.material(h.paper).defaultBlockState());
        }
        for(int n=0;n<2;n++)for(int y=-1;y<4;y++){
            var at=h.opening.relative(sideways?Direction.NORTH:Direction.WEST,n).above(y);e.remove(at);
            e.put(at,y==-1?DomesticPalette.entry(h.id,at.getX(),at.getZ()):Blocks.AIR.defaultBlockState());
        }
        DomesticStories.plan(e,h.id);
        return e.entrySet().stream().map(v->new Edit(v.getKey(),v.getValue())).toList();
    });}
    private static boolean fabric(BlockState s){return s.isAir()||s.is(Blocks.WHITE_TERRACOTTA)||s.is(Blocks.LIGHT_GRAY_TERRACOTTA)||s.is(Blocks.OAK_PLANKS)||s.is(Blocks.STONE)||s.is(Blocks.SMOOTH_STONE);}
    /**
     * 0.4.170, the owner: "Ensure they don't appear until several passes through the hallways, so they feel more impactful
     * when first encountered." A new home is built behind its fragment's wall, but every cell a reader in the hall could see
     * (the shared wall, the passage through the fragment and the opening) waits. The fragment ends where it always did until
     * the halls have been passed through {@link #PASSES} times and this hall has been seen {@link #HALL_SEEN} times without
     * it; then the home bleeds in while nobody is near ({@link #bleed}).
     */
    public static final int PASSES=10,HALL_SEEN=2;
    public static void fresh(ServerLevel l,BlockPos base,LabyrinthPlace place){
        var d=LabyrinthData.get(l.getServer());var tag=d.state(STATE);
        for(var h:HOMES)if(h.hall==place&&selected(l,base,h)){
            // A home that has already bled in (or was made before 0.4.170) is rebuilt whole.
            String key=key(base,h);var join=tag.getBoolean(key)&&!tag.contains(key+":join")?Set.<BlockPos>of():join(l,base,h);
            for(var edit:plan(h))if(!join.contains(edit.at))l.setBlock(base.offset(edit.at),edit.next,LabyrinthBuilder.flags());
            if(join.isEmpty())tag.remove(key+":join");else tag.putLongArray(key+":join",join.stream().mapToLong(BlockPos::asLong).toArray());
            tag.putBoolean(key,true);tag.putBoolean(key+":chosen",true);DomesticHomeRepairs.complete(l,base,h);DomesticContinuity.complete(l,base,h);BuildBlocks.after(l,()->DomesticStories.seed(l,base,h));}
        d.setState(STATE,tag);
    }
    /** The plan cells a reader in the hall could see: everything outside the home's own box, and its walls where the hall's air meets them. */
    static Set<BlockPos> join(ServerLevel l,BlockPos base,Home h){
        var out=new HashSet<BlockPos>();var box=h.local();
        for(var edit:plan(h)){var r=edit.at;
            if(!box.isInside(r)){out.add(r);continue;}
            if(r.getX()!=h.x0&&r.getX()!=h.x1&&r.getZ()!=h.z0&&r.getZ()!=h.z1)continue;
            for(var side:Direction.Plane.HORIZONTAL){var n=r.relative(side);if(!box.isInside(n)&&BuildBlocks.state(l,base.offset(n)).isAir()){out.add(r);break;}}
        }
        // The opening itself always waits, whatever stands beside it in the hall.
        boolean sideways=!h.id.equals("grandmother");
        for(int n=0;n<2;n++)for(int y=-1;y<4;y++)out.add(h.opening.relative(sideways?Direction.NORTH:Direction.WEST,n).above(y));
        return out;
    }
    /** Whether this home is still sealed behind its fragment. */
    public static boolean sealed(LabyrinthData d,BlockPos base,Home h){return d.state(STATE).contains(key(base,h)+":join");}
    static int returns(LabyrinthData d,BlockPos origin,LabyrinthPlace p){return d.stateEntry(HallAtmosphere.STATE,origin.asLong()+":"+p.id()).getInt("Returns");}
    /** How many times the halls have been entered, each occupied stay counted once however many readers share it. */
    public static int passes(LabyrinthData d,BlockPos origin){int n=0;for(var p:LabyrinthPlace.values())if(HallAtmosphere.room(p))n+=returns(d,origin,p);return n;}
    public static boolean due(LabyrinthData d,BlockPos origin,Home h){return passes(d,origin)>=PASSES&&returns(d,origin,h.hall)>=HALL_SEEN;}
    /**
     * Opens a sealed home: the shared wall, the passage and the opening, all at once, only while the area is loaded, no camera
     * is within sixteen blocks of it and nothing living stands in it. Cells holding a block entity are left alone.
     */
    public static boolean bleed(ServerLevel l,BlockPos base,Home h){
        var d=LabyrinthData.get(l.getServer());var tag=d.state(STATE);String key=key(base,h);if(!tag.contains(key+":join"))return true;var area=workArea(base,h);
        for(int x=(int)Math.floor(area.minX)>>4;x<=((int)Math.ceil(area.maxX)-1)>>4;x++)for(int z=(int)Math.floor(area.minZ)>>4;z<=((int)Math.ceil(area.maxZ)-1)>>4;z++)if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(ChunkPos.asLong(x,z)))return false;
        if(l.players().stream().anyMatch(p->area.inflate(16).intersects(p.getCamera().getBoundingBox())))return false;
        if(!l.getEntitiesOfClass(LivingEntity.class,area,e->e.isAlive()).isEmpty())return false;
        var join=new HashSet<BlockPos>();for(long at:tag.getLongArray(key+":join"))join.add(BlockPos.of(at));
        for(var edit:plan(h)){if(!join.contains(edit.at))continue;var at=base.offset(edit.at);if(l.getBlockEntity(at)==null)l.setBlock(at,edit.next,Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);}
        tag.remove(key+":join");tag.putBoolean(key+":bled",true);d.setState(STATE,tag);return true;
    }
    public static boolean step(ServerLevel l,BlockPos base,Home h){
        var d=LabyrinthData.get(l.getServer());var tag=d.state(STATE);String key=key(base,h);if(tag.getBoolean(key))return true;
        if(!selected(l,base,h))return true;var area=workArea(base,h);
        for(int x=(int)Math.floor(area.minX)>>4;x<=((int)Math.ceil(area.maxX)-1)>>4;x++)for(int z=(int)Math.floor(area.minZ)>>4;z<=((int)Math.ceil(area.maxZ)-1)>>4;z++)if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(ChunkPos.asLong(x,z)))return false;
        if(l.players().stream().anyMatch(p->area.inflate(16).intersects(p.getCamera().getBoundingBox())))return false;
        if(!l.getEntitiesOfClass(LivingEntity.class,area,e->e.isAlive()).isEmpty())return false;
        var plan=plan(h);int cursor=tag.getInt(key+":cursor");long deadline=System.nanoTime()+3_000_000;int visits=0;
        while(cursor<plan.size()&&visits++<256&&(visits==1||System.nanoTime()<deadline)){
            var edit=plan.get(cursor++);var at=base.offset(edit.at);var old=l.getBlockState(at);
            if(l.getBlockEntity(at)==null&&fabric(old))l.setBlock(at,edit.next,Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
        }
        tag.putInt(key+":cursor",cursor);if(cursor>=plan.size()){tag.putBoolean(key,true);DomesticHomeRepairs.complete(l,base,h);DomesticStories.seed(l,base,h);}d.setState(STATE,tag);return cursor>=plan.size();
    }
    public static void tick(MinecraftServer s){
        if(s instanceof net.minecraft.gametest.framework.GameTestServer||LabyrinthBuilder.isCarving())return;
        var origin=HouseSavedData.get(s).houseOrigin();if(origin==null||!origin.equals(LabyrinthData.get(s).builtOrigin()))return;
        var l=s.getLevel(HouseDimensions.INTERIOR);if(l==null)return;
        for(var h:HOMES){if(LabyrinthData.get(s).door(h.hall.entryDoorId())==null)continue;var b=LabyrinthPlaces.base(origin,h.hall);if(!selected(l,b,h))continue;
            var state=LabyrinthData.get(s).state(STATE);String key=key(b,h);
            // A sealed home waits for its passes, then bleeds in at once; one change in a server tick.
            if(state.contains(key+":join")){if(due(LabyrinthData.get(s),origin,h)&&bleed(l,b,h))return;continue;}
            if(state.getBoolean(key)){if(DomesticHomeRepairs.step(l,b,h))return;continue;}int before=state.getInt(key+":cursor");
            step(l,b,h);
            // An unloaded earlier home must not starve a different, resident hall.
            // Only one actual slice may run in this server tick.
            if(LabyrinthData.get(s).state(STATE).getInt(key+":cursor")>before)return;
        }
    }
}
