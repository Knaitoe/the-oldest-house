package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * The Three Attic Whalestoe Institute (0.4.66). A letter posted is answered before it arrives: the reply is already in the hand
 * that let it go, dated earlier, quoting it. The first letters of each reply's middle page spell the room whose number has been
 * taken off its door. After the third letter, that pigeonhole holds what was sent, exactly as it was written; read in her room,
 * it is the end. Nothing in the building says so.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class WhaleInstitute {
    /** Each reader's own record inside the novel state: Sent, Posted, Blanks, Returned. */
    public static final String KEY="Whale0466",CLOCK_TAG="WhaleClock0466";
    /** Her room, the box that answers, the letters the slot takes and the paper her desk gives each reader. */
    public static final int ROOM=7,LETTERS=3,BLANKS=4;
    public static final BlockPos OUTGOING=new BlockPos(5,1,-9),DESK=new BlockPos(7,0,-24),LECTERN=new BlockPos(5,0,-24),
        CALENDAR=new BlockPos(9,2,-24),HER_DOOR=new BlockPos(2,0,-23),ATTIC_DESK=new BlockPos(2,6,-30);
    /** The ground floor inside its outer walls, for the recessed-window check. */
    public static final BoundingBox INTERIOR=new BoundingBox(-12,0,-31,12,4,-1);
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    /** Rooms down the corridor, south to north; the odd numbers are on the east side. */
    private static final int[] DOOR_Z={-11,-15,-19,-23};
    private WhaleInstitute(){}

    /** Twelve boxes on the post room's east wall, numbered in reading order as you face them: 1 to 6 above, 7 to 12 below. */
    public static BlockPos pigeonhole(int n){return new BlockPos(12,n<=6?2:1,-7+(n-1)%6);}
    public static int pigeonholeAt(BlockPos rel){
        if(rel.getX()!=12)return 0;int column=rel.getZ()+7;if(column<0||column>5)return 0;
        return rel.getY()==2?column+1:rel.getY()==1?column+7:0;
    }

    // ------------------------------------------------------------------------------------------------ the building
    static void build(ServerLevel l,BlockPos b){
        clearClocks(l,b);
        BlockState paint=NovelRegistry.INSTITUTE.get().defaultBlockState(),dado=Blocks.POLISHED_ANDESITE.defaultBlockState(),air=Blocks.AIR.defaultBlockState();
        // Linoleum underfoot, a plain ceiling overhead, painted walls over a stone dado.
        for(int x=-13;x<=13;x++)for(int z=-32;z<=0;z++)set(l,b,x,-1,z,((x+z)&1)==0?Blocks.WHITE_CONCRETE:Blocks.LIGHT_GRAY_CONCRETE);
        NovelRooms.box(l,b,-13,5,-32,13,5,0,Blocks.SMOOTH_STONE.defaultBlockState());
        wall(l,b,-13,-32,-13,0,paint,dado);wall(l,b,13,-32,13,0,paint,dado);wall(l,b,-13,0,13,0,paint,dado);wall(l,b,-13,-32,13,-32,paint,dado);
        // Reception's back wall, with the way into the corridor and the outgoing slot set into it.
        wall(l,b,-12,-9,12,-9,paint,dado);NovelRooms.box(l,b,-1,0,-9,1,2,-9,air);
        // The corridor's walls, the rooms either side of it, and the arch into the dayroom.
        wall(l,b,-2,-25,-2,-10,paint,dado);wall(l,b,2,-25,2,-10,paint,dado);
        for(int z:new int[]{-13,-17,-21,-25}){wall(l,b,-12,z,-3,z,paint,dado);wall(l,b,3,z,12,z,paint,dado);}
        NovelRooms.box(l,b,-1,0,-25,1,3,-25,air);
        NovelRooms.door(l,b,Direction.SOUTH,Blocks.BIRCH_DOOR,true);
        reception(l,b);corridor(l,b);rooms(l,b);dayroom(l,b);attics(l,b);
    }
    private static void reception(ServerLevel l,BlockPos b){
        // The slot in the wall, and the old plaque on its post beside it.
        BuildBlocks.set(l,b.offset(OUTGOING),NovelRooms.prop(NovelPropBlock.Kind.MAIL_SLOT,Direction.SOUTH),F);
        set(l,b,4,0,-8,Blocks.OAK_FENCE);BuildBlocks.set(l,b.offset(4,1,-8),HouseBlocks.MAIL_PLAQUE.get().defaultBlockState(),F);
        for(int n=1;n<=12;n++)BuildBlocks.set(l,b.offset(pigeonhole(n)),NovelRegistry.PIGEONHOLE.get().defaultBlockState().setValue(PigeonholeBlock.FACING,Direction.WEST).setValue(PigeonholeBlock.NUMBER,n),F);
        for(int z=-7;z<=-2;z++){BuildBlocks.set(l,b.offset(12,0,z),Blocks.SPRUCE_PLANKS.defaultBlockState(),F);BuildBlocks.set(l,b.offset(12,3,z),Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP),F);}
        sign(l,b.offset(12,3,-8),Direction.WEST,new String[]{"","POST","",""});
        // Benches turned to the door; a board of rules on the west wall.
        for(int x=-11;x<=-7;x++)for(int z:new int[]{-3,-6})BuildBlocks.set(l,b.offset(x,0,z),Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH),F);
        sign(l,b.offset(-12,2,-4),Direction.EAST,new String[]{"VISITING HOURS","","none at present",""});
        sign(l,b.offset(-12,2,-7),Direction.EAST,new String[]{"Patients may","write as often","as they like.",""});
        NovelRooms.furniture(l,b.offset(-4,0,-8),HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.SOUTH);
        BuildBlocks.set(l,b.offset(-4,1,-8),SceneDetailBlock.state(SceneDetailBlock.Kind.VASE,Direction.SOUTH),F);
        clock(l,b,-6,3,-1,Direction.NORTH,2);clock(l,b,8,3,-1,Direction.NORTH,5);
        for(int x:new int[]{-6,0,7})lantern(l,b,x,4,-4);
    }
    private static void corridor(ServerLevel l,BlockPos b){
        for(int z:new int[]{-12,-17,-22})lantern(l,b,0,4,z);
        // Every clock on the corridor keeps its own hour.
        clock(l,b,1,2,-12,Direction.WEST,3);clock(l,b,-1,2,-16,Direction.EAST,1);clock(l,b,1,2,-20,Direction.WEST,6);clock(l,b,-1,2,-24,Direction.EAST,4);
        for(int i=0;i<4;i++){int z=DOOR_Z[i],east=2*i+1,west=2*i+2;
            // Her number has been taken off: two screw holes where the plate was.
            sign(l,b.offset(1,2,z+1),Direction.WEST,east==ROOM?new String[]{"","·      ·","",""}:new String[]{"",Integer.toString(east),"",""});
            sign(l,b.offset(-1,2,z+1),Direction.EAST,new String[]{"",Integer.toString(west),"",""});
            boolean eastLocked=east==5,westLocked=west==2||west==8;
            NovelRooms.door(l,b.offset(2,0,z),Direction.EAST,eastLocked?Blocks.IRON_DOOR:Blocks.BIRCH_DOOR,false);
            NovelRooms.door(l,b.offset(-2,0,z),Direction.WEST,westLocked?Blocks.IRON_DOOR:Blocks.BIRCH_DOOR,false);
            window(l,b,13,z,true);window(l,b,-13,z,true);
        }
    }
    private static void rooms(ServerLevel l,BlockPos b){
        // 1: two beds, both made. 2, 5 and 8 do not open.
        NovelRooms.bed(l,b.offset(10,0,-10),Blocks.WHITE_BED,Direction.EAST);NovelRooms.bed(l,b.offset(10,0,-12),Blocks.WHITE_BED,Direction.EAST);
        NovelRooms.furniture(l,b.offset(12,0,-11),HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.WEST);lantern(l,b,7,4,-11);
        NovelRooms.bed(l,b.offset(-11,0,-11),Blocks.WHITE_BED,Direction.WEST);
        // 3: a chair turned to the wall.
        NovelRooms.bed(l,b.offset(10,0,-14),Blocks.WHITE_BED,Direction.EAST);NovelRooms.furniture(l,b.offset(4,0,-16),HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.SOUTH);
        NovelRooms.furniture(l,b.offset(12,0,-16),HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.WEST);lantern(l,b,7,4,-15);
        // 4: the baths.
        for(int z:new int[]{-14,-16})BuildBlocks.set(l,b.offset(-11,0,z),Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL,3),F);
        NovelRooms.furniture(l,b.offset(-8,0,-15),HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.WEST);lantern(l,b,-7,4,-15);
        NovelRooms.bed(l,b.offset(10,0,-19),Blocks.WHITE_BED,Direction.EAST);
        // 6: chairs, stacked out of the way.
        for(int x=-11;x<=-8;x++){NovelRooms.furniture(l,b.offset(x,0,-20),HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH);set(l,b,x,0,-18,Blocks.BARREL);}
        NovelRooms.furniture(l,b.offset(-10,1,-18),HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.EAST);lantern(l,b,-6,4,-19);
        NovelRooms.bed(l,b.offset(-11,0,-23),Blocks.WHITE_BED,Direction.WEST);
        // 7: hers. A bed, a desk with paper in it, her letter, the calendar.
        NovelRooms.bed(l,b.offset(10,0,-22),Blocks.WHITE_BED,Direction.EAST);
        NovelRooms.furniture(l,b.offset(DESK),HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH);
        BuildBlocks.set(l,b.offset(DESK).above(),SceneDetailBlock.state(SceneDetailBlock.Kind.INK_PAPERS,Direction.SOUTH),F);
        NovelRooms.furniture(l,b.offset(7,0,-23),HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH);
        NovelRooms.lectern(l,b.offset(LECTERN),NovelTexts.whaleOpening());
        NovelRooms.furniture(l,b.offset(12,0,-24),HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.WEST);
        for(int x=8;x<=11;x++)set(l,b,x,0,-23,Blocks.BROWN_CARPET);
        sign(l,b.offset(CALENDAR),Direction.SOUTH,new String[]{"","Thursday","the 14th",""});lantern(l,b,8,4,-23);
    }
    private static void dayroom(ServerLevel l,BlockPos b){
        // Chairs facing windows that were painted shut.
        for(int x:new int[]{0,4,8})window(l,b,x,-32,false);
        for(int x:new int[]{0,4,8})NovelRooms.furniture(l,b.offset(x,0,-30),HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH);
        for(int x:new int[]{2,6})NovelRooms.furniture(l,b.offset(x,0,-28),HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH);
        NovelRooms.furniture(l,b.offset(-3,0,-28),HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.SOUTH);
        BuildBlocks.set(l,b.offset(-3,1,-28),SceneDetailBlock.state(SceneDetailBlock.Kind.TEA_SET,Direction.SOUTH),F);
        // The nurses' counter, with the ward book open on it.
        for(int x=8;x<=11;x++)set(l,b,x,0,-27,Blocks.SPRUCE_PLANKS);
        BuildBlocks.set(l,b.offset(9,1,-27),SceneDetailBlock.state(SceneDetailBlock.Kind.INK_PAPERS,Direction.NORTH),F);
        for(int y=0;y<=2;y++)set(l,b,-12,y,-27,Blocks.BOOKSHELF);
        clock(l,b,12,2,-29,Direction.WEST,7);
        lantern(l,b,-4,4,-28);lantern(l,b,6,4,-29);
        // The stair up to the attics, two wide along the north wall, solid beneath, with its well cut through the ceiling.
        for(int i=0;i<=5;i++){int x=-6-i;
            for(int z:new int[]{-31,-30}){for(int y=0;y<i;y++)set(l,b,x,y,z,Blocks.SPRUCE_PLANKS);
                BuildBlocks.set(l,b.offset(x,i,z),Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.WEST),F);}}
        NovelRooms.box(l,b,-10,5,-31,-9,5,-30,Blocks.AIR.defaultBlockState());
    }
    private static void attics(ServerLevel l,BlockPos b){
        // Three attics under one roof. They insist there have always been three.
        BlockState paint=NovelRegistry.INSTITUTE.get().defaultBlockState(),boards=Blocks.DARK_OAK_PLANKS.defaultBlockState();
        NovelRooms.box(l,b,-13,6,-25,13,9,-25,paint);NovelRooms.box(l,b,-13,6,-32,13,9,-32,paint);
        NovelRooms.box(l,b,-13,6,-32,-13,9,-25,paint);NovelRooms.box(l,b,13,6,-32,13,9,-25,paint);
        NovelRooms.box(l,b,-13,10,-32,13,10,-25,boards);
        NovelRooms.box(l,b,-4,6,-31,-4,9,-26,boards);NovelRooms.box(l,b,4,6,-31,4,9,-26,boards);
        NovelRooms.door(l,b.offset(-4,6,-28),Direction.EAST,Blocks.BIRCH_DOOR,true);
        NovelRooms.door(l,b.offset(4,6,-28),Direction.EAST,Blocks.IRON_DOOR,false);
        for(var at:new int[][]{{-10,-29},{-9,-29},{-8,-29},{-8,-30},{-8,-31}})set(l,b,at[0],6,at[1],Blocks.SPRUCE_FENCE);
        set(l,b,-12,6,-27,Blocks.BARREL);set(l,b,-11,6,-27,Blocks.BARREL);set(l,b,-12,7,-27,Blocks.BARREL);
        NovelRooms.furniture(l,b.offset(-7,6,-27),HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.SOUTH);
        // The middle attic: one chair at a painted-over window, and an undated letter.
        window(l,b,0,-32,false,7);
        NovelRooms.furniture(l,b.offset(0,6,-30),HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH);
        NovelRooms.lectern(l,b.offset(ATTIC_DESK),NovelTexts.whaleLast());
        lantern(l,b,-8,9,-28);lantern(l,b,0,9,-27);
    }

    /** Institute notices are fixed in place: nobody rewrites the room numbers. */
    private static void sign(ServerLevel l,BlockPos at,Direction facing,String[] lines){
        NovelRooms.sign(l,at,facing,lines);BuildBlocks.after(l,()->{if(l.getBlockEntity(at) instanceof SignBlockEntity s)s.setWaxed(true);});
    }
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,Block block){BuildBlocks.set(l,b.offset(x,y,z),block.defaultBlockState(),F);}
    private static void wall(ServerLevel l,BlockPos b,int x0,int z0,int x1,int z1,BlockState paint,BlockState dado){
        NovelRooms.box(l,b,x0,0,z0,x1,0,z1,dado);NovelRooms.box(l,b,x0,1,z0,x1,4,z1,paint);
    }
    private static void lantern(ServerLevel l,BlockPos b,int x,int y,int z){BuildBlocks.set(l,b.offset(x,y,z),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true),F);}
    private static void window(ServerLevel l,BlockPos b,int a,int c,boolean side){window(l,b,a,c,side,2);}
    /** A frosted pane set back behind the wall, with a stone reveal round it; side windows are in the east or west wall. */
    private static void window(ServerLevel l,BlockPos b,int a,int c,boolean side,int y){
        BlockState frame=Blocks.POLISHED_ANDESITE.defaultBlockState(),pane=Blocks.WHITE_STAINED_GLASS_PANE.defaultBlockState(),air=Blocks.AIR.defaultBlockState();
        if(side){int x=a,z=c,out=x+Integer.signum(x);
            for(int dy=0;dy<=1;dy++){BuildBlocks.set(l,b.offset(x,y+dy,z),air,F);BuildBlocks.set(l,b.offset(out,y+dy,z),pane,F);
                BuildBlocks.set(l,b.offset(out,y+dy,z-1),frame,F);BuildBlocks.set(l,b.offset(out,y+dy,z+1),frame,F);}
            BuildBlocks.set(l,b.offset(out,y-1,z),frame,F);BuildBlocks.set(l,b.offset(out,y+2,z),frame,F);
        }else{int x=a,z=c,out=z-1;
            for(int dy=0;dy<=1;dy++){BuildBlocks.set(l,b.offset(x,y+dy,z),air,F);BuildBlocks.set(l,b.offset(x,y+dy,out),pane,F);
                BuildBlocks.set(l,b.offset(x-1,y+dy,out),frame,F);BuildBlocks.set(l,b.offset(x+1,y+dy,out),frame,F);}
            BuildBlocks.set(l,b.offset(x,y-1,out),frame,F);BuildBlocks.set(l,b.offset(x,y+2,out),frame,F);
        }
    }
    /** A clock in a fixed frame, turned so that no two in the building agree. */
    private static void clock(ServerLevel l,BlockPos b,int x,int y,int z,Direction facing,int rotation){
        BuildBlocks.after(l,()->{var frame=new ItemFrame(l,b.offset(x,y,z),facing);frame.setItem(new ItemStack(Items.CLOCK),false);frame.setRotation(rotation);
            var tag=new CompoundTag();frame.saveWithoutId(tag);tag.putBoolean("Fixed",true);tag.putBoolean("Invulnerable",true);frame.load(tag);
            frame.addTag(CLOCK_TAG);l.addFreshEntity(frame);});
    }
    /** Before a carve: a frame whose wall is taken away would drop its clock. */
    public static void clearClocks(ServerLevel l,BlockPos b){
        var r=LabyrinthPlace.WHALE.room();var box=new AABB(b.getX()+r.minX()-1,b.getY()+r.minY()-1,b.getZ()+r.minZ()-1,b.getX()+r.maxX()+2,b.getY()+r.maxY()+2,b.getZ()+r.maxZ()+2);
        for(var frame:l.getEntitiesOfClass(ItemFrame.class,box,f->f.getTags().contains(CLOCK_TAG)))frame.discard();
    }

    // ------------------------------------------------------------------------------------------------ the post
    /** Every verb in the institute. True when it handled the block; the reader's record is updated in place. */
    static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var w=own.getCompound(KEY);int box=pigeonholeAt(rel);boolean handled=true;
        if(rel.equals(OUTGOING))post(p,b,own,w);
        else if(box>0)pigeonhole(p,b,own,w,box);
        else if(rel.equals(DESK))desk(p,own,w);
        else if(rel.equals(LECTERN))NovelVignettes.open(p,LabyrinthPlace.WHALE,NovelTexts.whaleOpening(),"WhaleOpening0466",false,own);
        else if(rel.equals(ATTIC_DESK))NovelVignettes.open(p,LabyrinthPlace.WHALE,NovelTexts.whaleLast(),"WhaleAttic0466",false,own);
        else handled=false;
        own.put(KEY,w);return handled;
    }
    private static void desk(ServerPlayer p,CompoundTag own,CompoundTag w){
        if(p.getInventory().countItem(Items.WRITABLE_BOOK)>0){NovelVignettes.cue(p,own,"There is still paper in your hands.");return;}
        int taken=w.getInt("Blanks");if(taken>=BLANKS){NovelVignettes.cue(p,own,"The drawer is empty.");return;}
        w.putInt("Blanks",taken+1);NovelVignettes.give(p,VignetteYields.mark(new ItemStack(Items.WRITABLE_BOOK),LabyrinthPlace.WHALE.id()));
        p.playNotifySound(SoundEvents.BOOK_PAGE_TURN,SoundSource.BLOCKS,.6F,1);
    }
    private static void post(ServerPlayer p,BlockPos b,CompoundTag own,CompoundTag w){
        var hand=p.getMainHandItem();int sent=w.getInt("Sent");
        if(sent>=LETTERS){NovelVignettes.cue(p,own,"The slot is stopped up with paper.");return;}
        if(hand.is(Items.WRITABLE_BOOK)){NovelVignettes.cue(p,own,"The flap will not take an unsigned letter.");return;}
        WrittenBookContent content=hand.get(DataComponents.WRITTEN_BOOK_CONTENT);
        if(content==null){NovelVignettes.cue(p,own,"Cold air comes up through the slot.");return;}
        if(!content.author().equals(p.getGameProfile().getName())){NovelVignettes.cue(p,own,"The slot takes only letters in your own hand.");return;}
        var posted=w.getList("Posted",Tag.TAG_COMPOUND);posted.add(hand.copyWithCount(1).save(p.registryAccess()));w.put("Posted",posted);
        sent++;w.putInt("Sent",sent);
        ItemStack reply=VignetteYields.mark(NovelTexts.whaleReply(sent,p.getGameProfile().getName(),quote(content)),LabyrinthPlace.WHALE.id());
        hand.shrink(1);
        // The hand that let go of the letter is already holding the answer.
        if(p.getMainHandItem().isEmpty())p.setItemInHand(InteractionHand.MAIN_HAND,reply);else NovelVignettes.give(p,reply);
        p.serverLevel().playSound(null,b.offset(OUTGOING),SoundEvents.BOOK_PAGE_TURN,SoundSource.BLOCKS,.7F,.8F);
        calendar(p,b,sent);
    }
    private static void pigeonhole(ServerPlayer p,BlockPos b,CompoundTag own,CompoundTag w,int n){
        if(n!=ROOM||w.getInt("Sent")<LETTERS||w.getBoolean("Returned")){NovelVignettes.cue(p,own,"Empty.");return;}
        var posted=w.getList("Posted",Tag.TAG_COMPOUND);
        for(int i=0;i<posted.size();i++)NovelVignettes.give(p,ItemStack.parseOptional(p.registryAccess(),posted.getCompound(i)));
        w.putBoolean("Returned",true);
        p.serverLevel().playSound(null,b.offset(pigeonhole(n)),SoundEvents.BOOK_PUT,SoundSource.BLOCKS,.7F,.9F);
        // Down the corridor, privately, the door with no number.
        var door=Vec3.atCenterOf(b.offset(HER_DOOR));
        p.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.WOODEN_DOOR_OPEN),SoundSource.BLOCKS,door.x,door.y,door.z,.6F,.8F,p.getRandom().nextLong()));
    }
    /** Her words back to her: the first sentence, or as much of it as a line of a letter holds. */
    static String quote(WrittenBookContent content){
        var text=new StringBuilder();for(var page:content.pages())text.append(page.raw().getString()).append(' ');
        String clean=ChatFormatting.stripFormatting(text.toString());clean=clean==null?"":clean.replaceAll("\\s+"," ").trim();
        if(clean.isEmpty())return "";
        int end=clean.length();for(char c:new char[]{'.','!','?'}){int i=clean.indexOf(c);if(i>=8&&i+1<end)end=i+1;}
        String said=clean.substring(0,end);
        if(said.length()>48){int cut=said.lastIndexOf(' ',48);said=said.substring(0,cut>20?cut:48).trim()+"...";}
        return said;
    }

    // ------------------------------------------------------------------------------------------------ the calendar
    /** The calendar in her room, crossed out again after each letter. Only its reader sees it change. */
    static void calendar(ServerPlayer p,BlockPos b,int stage){
        if(stage<=0)return;var at=b.offset(CALENDAR);var level=p.serverLevel();var state=level.getBlockState(at);
        if(!(state.getBlock() instanceof WallSignBlock)||!level.isLoaded(at))return;
        var text=new SignText();Component[] lines=calendarLines(stage);for(int i=0;i<4;i++)text=text.setMessage(i,lines[i]);
        // A detached copy only: the real sign, and every other reader's view of it, keep their own words.
        var tag=new CompoundTag();SignText.DIRECT_CODEC.encodeStart(level.registryAccess().createSerializationContext(NbtOps.INSTANCE),text).result().ifPresent(t->tag.put("front_text",t));tag.putBoolean("is_waxed",true);
        var copy=new SignBlockEntity(at,state);copy.setLevel(level);p.connection.send(ClientboundBlockEntityDataPacket.create(copy,(entity,registries)->tag));
    }
    public static Component[] calendarLines(int stage){
        Component thursday=Component.literal("Thursday 14").withStyle(ChatFormatting.STRIKETHROUGH),tuesday=Component.literal("Tuesday 9").withStyle(ChatFormatting.STRIKETHROUGH),
            monday=Component.literal("Monday 9").withStyle(ChatFormatting.STRIKETHROUGH);
        return switch(Math.min(3,stage)){
            case 1->new Component[]{thursday,Component.literal("Tuesday"),Component.literal("the 9th"),Component.empty()};
            case 2->new Component[]{thursday,tuesday,Component.literal("Monday"),Component.literal("the 9th")};
            default->new Component[]{thursday,tuesday,monday,Component.literal("today")};
        };
    }
    static void arrive(ServerPlayer p,CompoundTag own){var b=IndianLakeRooms.base(p.server,LabyrinthPlace.WHALE);if(b!=null)calendar(p,b,own.getCompound(KEY).getInt("Sent"));}
    /** A private sign is forgotten when its chunk is sent again, so it is said again now and then. */
    static void tick(ServerPlayer p,BlockPos b,CompoundTag own){if(p.tickCount%100==0)calendar(p,b,own.getCompound(KEY).getInt("Sent"));}

    // ------------------------------------------------------------------------------------------------ reading what came back
    static boolean herRoom(ServerPlayer p,BlockPos b){
        Vec3 r=p.position().subtract(b.getX(),b.getY(),b.getZ());return r.x>=3&&r.x<13&&r.z>=-24&&r.z<-21&&r.y>-.5&&r.y<5;
    }
    @SubscribeEvent(priority=EventPriority.HIGH) public static void read(PlayerInteractEvent.RightClickItem e){
        if(e.isCanceled()||!(e.getEntity() instanceof ServerPlayer p)||!NovelVignettes.inside(p,LabyrinthPlace.WHALE))return;
        var stack=p.getItemInHand(e.getHand());if(!stack.has(DataComponents.WRITTEN_BOOK_CONTENT))return;
        var b=IndianLakeRooms.base(p.server,LabyrinthPlace.WHALE);if(b==null||!herRoom(p,b))return;
        var data=LabyrinthData.get(p.server);var own=NovelVignettes.personal(data,p.getUUID());var w=own.getCompound(KEY);if(!w.getBoolean("Returned"))return;
        var posted=w.getList("Posted",Tag.TAG_COMPOUND);boolean theirs=false;
        for(int i=0;i<posted.size()&&!theirs;i++)theirs=ItemStack.isSameItemSameComponents(stack,ItemStack.parseOptional(p.registryAccess(),posted.getCompound(i)));
        if(!theirs)return;
        e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);
        NovelVignettes.readReturned(p,LabyrinthPlace.WHALE,stack.copy());
    }
}
