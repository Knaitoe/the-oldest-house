package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
import static io.github.knaitoe.theoldesthouse.labyrinth.HouseReadPropBlock.Part.*;
import static io.github.knaitoe.theoldesthouse.labyrinth.HouseReadSites.Room.KITCHEN;

/**
 * The kitchen table (0.4.168), the first quiet room. The owner: "The skin-color selection should be the first vignette the
 * player encounters." Four o'clock, after school: a drawing of the reader with only its pencil lines, and an open tin of
 * crayons, twelve for skin in an order shuffled for each reader and eight for everything else. The crayon the reader puts on
 * the face is the House's only source for {@code TONE}, and the hair's for the hair colour of the House's body; nothing else
 * reads them, they read nothing else, and the House never names one. Putting the picture up on the fridge is what the room is
 * for, so the doors open once it is up.
 */
public final class HouseReadKitchen {
    private HouseReadKitchen(){}
    public static final BlockPos SHEET_AT=new BlockPos(0,1,-5),TIN_LEFT_AT=new BlockPos(-2,1,-5),TIN_RIGHT_AT=new BlockPos(-1,1,-5),PLATE_AT=new BlockPos(2,1,-5),
        FRIDGE_AT=new BlockPos(3,0,-11),RADIO_AT=new BlockPos(0,1,-11),SINK_AT=new BlockPos(-2,0,-11),CLOCK_AT=new BlockPos(-4,2,-11),EXIT=new BlockPos(5,0,-12),PENDANT_AT=new BlockPos(0,3,-5);
    /** The twelve skin crayons, from very light to very deep, with pink, neutral, golden and olive undertones (the design's own). */
    public static final List<String> TONES=List.of("#f4d6c6","#eecba8","#e3b68a","#d4b28c","#cc9a6e","#bf9070","#ad7a4f","#a3825e","#8a5a3a","#7b5140","#5a3726","#3e261c");
    /** The eight others, for hair, clothes and everything else: black, dark brown, light brown, yellow, orange-red, grey, blue, green. */
    public static final List<String> OTHERS=List.of("#262220","#5b3a24","#a87b4f","#e9c64a","#d4592e","#8d8a86","#3e64b0","#4c9648");
    /** A crayon's colour: 0 to 11 the skin crayons, 12 to 19 the others. */
    public static String hex(int colour){return colour<12?TONES.get(Math.floorMod(colour,12)):OTHERS.get(Math.floorMod(colour-12,8));}
    public static int rgb(int colour){return Integer.parseInt(hex(colour).substring(1),16);}
    /** A colour a face could be: any skin crayon, or either brown. A blue or green face is play. */
    public static boolean person(int colour){return colour>=0&&colour<12||colour==13||colour==14;}
    /** A colour hair could be: black, the browns, yellow, orange-red or grey. */
    public static boolean hairColour(int colour){return colour>=12&&colour<=17;}
    /** The values {@code TONE} can take: the colour of the face, as drawn. */
    public static final Set<String> SKIN=java.util.stream.IntStream.range(0,20).filter(HouseReadKitchen::person).mapToObj(HouseReadKitchen::hex).collect(java.util.stream.Collectors.toUnmodifiableSet());

    // ---- The tin: two blocks, six skin crayons along the back of each and four others along the front.
    /** The skin crayons' places along the tin, per reader: no place is a default, and none is first for everyone. */
    public static int[] order(UUID id){var tones=new ArrayList<Integer>();for(int i=0;i<12;i++)tones.add(i);Collections.shuffle(tones,new Random(id.getMostSignificantBits()^Long.rotateLeft(id.getLeastSignificantBits(),23)^168));return tones.stream().mapToInt(Integer::intValue).toArray();}
    /** The ten crayons in one half of the tin, back row then front. */
    public static int[] colours(UUID owner,boolean right){int[] order=order(owner),out=new int[10];for(int k=0;k<6;k++)out[k]=order[(right?6:0)+k];for(int k=0;k<4;k++)out[6+k]=12+(right?4:0)+k;return out;}
    /** Where each crayon lies in its half of the tin, in pixels: the back row of six, then the front row of four. */
    public static double[] slot(int slot){return slot<6?new double[]{1.6+2.2*slot,3.2+2.2*slot,4,9}:new double[]{2.4+3.2*(slot-6),4.0+3.2*(slot-6),9.6,14.6};}
    /** The crayon under a click on the tin, or -1 for the lid. */
    static int slotAt(BlockPos at,Vec3 hit){
        double x=(hit.x-at.getX())*16,y=(hit.y-at.getY())*16,z=(hit.z-at.getZ())*16;if(z<3.6&&y>1.6)return -1;
        if(z<9.3){int k=(int)Math.floor((x-1.2)/2.2);return Mth.clamp(k,0,5);}
        return 6+Mth.clamp((int)Math.floor((x-1.6)/3.2),0,3);
    }

    // ---- The drawing: four parts to colour, mapped pixel by pixel on the sheet (row 0 is the top of the page, away from the chair).
    public enum Region {FACE,HANDS,HAIR,CLOTHES}
    /** The figure's parts: f the face, h the hands, r the hair, c the clothes. tools/house_read_kitchen_art.py draws the same map. */
    public static final String[] FIGURE={
        "................",
        "......rrrr......",
        ".....rrrrrr.....",
        ".....rffffr.....",
        ".....ffffff.....",
        ".....ffffff.....",
        "......ffff......",
        ".......ff.......",
        "....cccccccc....",
        "..cccccccccccc..",
        ".hhccccccccccchh",
        ".hh..cccccc..hh.",
        ".....cccccc.....",
        ".....cc..cc.....",
        ".....cc..cc.....",
        "................"};
    /** The part under a click on the sheet: the nearest marked pixel within two, so a near miss still colours what it meant. */
    static Region regionAt(BlockPos at,Vec3 hit){
        int col=Mth.clamp((int)((hit.x-at.getX())*16),0,15),row=Mth.clamp((int)((hit.z-at.getZ())*16),0,15);Region best=null;int bestDistance=99;
        for(int dz=-2;dz<=2;dz++)for(int dx=-2;dx<=2;dx++){int r=row+dz,c=col+dx;if(r<0||r>15||c<0||c>15)continue;Region found=region(FIGURE[r].charAt(c));int distance=dx*dx+dz*dz;if(found!=null&&distance<bestDistance){best=found;bestDistance=distance;}}
        return best;
    }
    public static Region region(char c){return switch(c){case 'f'->Region.FACE;case 'h'->Region.HANDS;case 'r'->Region.HAIR;case 'c'->Region.CLOTHES;default->null;};}
    static String key(Region r){return switch(r){case FACE->"Face";case HANDS->"Hands";case HAIR->"Hair";case CLOTHES->"Clothes";};}

    // ---- Building.
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private static void box(ServerLevel l,BlockPos b,int x,int y,int z,int X,int Y,int Z,BlockState s){BuildBlocks.box(l,b.offset(x,y,z),b.offset(X,Y,Z),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x,int y,int z,int X,int Y,int Z,Block s){box(l,b,x,y,z,X,Y,Z,s.defaultBlockState());}
    private static void put(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    private static void fit(ServerLevel l,BlockPos b,BlockPos at,HouseReadPropBlock.Part part,Direction facing){put(l,b,at,HouseReadRooms.part(part).setValue(HorizontalDirectionalBlock.FACING,facing));}
    private static void fit(ServerLevel l,BlockPos b,int x,int y,int z,HouseReadPropBlock.Part part,Direction facing){fit(l,b,new BlockPos(x,y,z),part,facing);}
    public static AABB bounds(BlockPos b){return new AABB(b.getX()-7,b.getY()-3,b.getZ()-17,b.getX()+9,b.getY()+7,b.getZ()+6);}
    /**
     * A family kitchen at four in the afternoon: low gold light through the window over the sink, the radio turned down, the
     * fridge humming, the clock. The table in the middle with four chairs; the drawing in front of the chair nearest the door,
     * the tin to its left and a plate to its right. The way on is the hall door past the fridge.
     */
    static void build(ServerLevel l,BlockPos b,UUID owner){
        HouseReadRemainingRooms.shell(l,b,6,-12,HouseReadRegistry.WALLPAPER.get().defaultBlockState(),HouseReadRegistry.LINOLEUM.get().defaultBlockState(),HouseReadRegistry.CEILING.get().defaultBlockState(),HouseReadRegistry.PANEL_DOOR.get());
        box(l,b,-6,0,-12,6,0,3,HouseReadRegistry.WAINSCOT.get());box(l,b,-5,0,-11,5,0,1,Blocks.AIR.defaultBlockState());box(l,b,0,0,2,0,1,3,Blocks.AIR.defaultBlockState());
        HouseReadRooms.door(l,b,HouseReadRooms.COAT_ENTRY,HouseReadRegistry.PANEL_DOOR.get(),Direction.SOUTH);
        // The counter along the back wall, tiled above, with the sink under the window and the radio beside it.
        for(int x=-5;x<=1;x++)if(x!=-2)put(l,b,new BlockPos(x,0,-11),HouseReadRegistry.COUNTER.get().defaultBlockState());
        box(l,b,-6,1,-12,2,1,-12,HouseReadRegistry.TILE.get());fit(l,b,SINK_AT,SINK,Direction.SOUTH);fit(l,b,RADIO_AT,RADIO,Direction.SOUTH);fit(l,b,CLOCK_AT,WALL_CLOCK,Direction.SOUTH);
        box(l,b,-3,2,-12,-1,3,-12,HouseReadRegistry.WINDOW.get());
        // Through the window: a yard in the afternoon, a fence and the sky going gold.
        box(l,b,-6,-1,-16,1,-1,-13,Blocks.GRASS_BLOCK.defaultBlockState());box(l,b,-6,-2,-16,1,-2,-13,Blocks.DIRT.defaultBlockState());
        box(l,b,-6,0,-16,1,6,-16,HouseReadRegistry.AFTERNOON.get());box(l,b,-6,0,-15,-6,6,-13,HouseReadRegistry.AFTERNOON.get());box(l,b,1,0,-15,1,6,-13,HouseReadRegistry.AFTERNOON.get());box(l,b,-6,6,-15,1,6,-13,HouseReadRegistry.AFTERNOON.get());
        box(l,b,-5,0,-15,0,0,-15,HouseReadRegistry.PICKET.get());box(l,b,-5,1,-14,0,5,-13,Blocks.AIR.defaultBlockState());
        put(l,b,new BlockPos(-3,4,-14),Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,13));put(l,b,new BlockPos(-2,2,-10),Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,9));
        // The fridge, its drawings and magnets, and the hall door past it.
        fit(l,b,FRIDGE_AT,FRIDGE,Direction.SOUTH);fit(l,b,FRIDGE_AT.above(),FRIDGE_TOP,Direction.SOUTH);
        HouseReadRooms.door(l,b,EXIT,HouseReadRegistry.PANEL_DOOR.get(),Direction.SOUTH);
        box(l,b,3,-1,-16,7,4,-13,HouseReadRegistry.WALLPAPER.get());box(l,b,4,0,-15,6,3,-13,Blocks.AIR.defaultBlockState());box(l,b,4,-1,-15,6,-1,-13,HouseReadRegistry.FLOORBOARDS.get());box(l,b,4,0,-15,6,0,-15,HouseReadRegistry.WAINSCOT.get());
        // The table and its four chairs; the drawing, the tin and the plate on it, and the light over it.
        fit(l,b,-2,0,-5,TABLE_END,Direction.EAST);for(int x=-1;x<=1;x++)fit(l,b,x,0,-5,TABLE_MID,Direction.SOUTH);fit(l,b,2,0,-5,TABLE_END,Direction.WEST);
        fit(l,b,0,0,-4,CHAIR,Direction.SOUTH);fit(l,b,0,0,-6,CHAIR,Direction.NORTH);fit(l,b,-3,0,-5,CHAIR,Direction.WEST);fit(l,b,3,0,-5,CHAIR,Direction.EAST);
        fit(l,b,SHEET_AT,SHEET,Direction.SOUTH);fit(l,b,TIN_LEFT_AT,TIN_LEFT,Direction.SOUTH);fit(l,b,TIN_RIGHT_AT,TIN_RIGHT,Direction.SOUTH);fit(l,b,PLATE_AT,PLATE,Direction.SOUTH);
        fit(l,b,PENDANT_AT,PENDANT,Direction.SOUTH);
        // A rug under the table, a window onto the front walk by the door.
        for(int x=-3;x<=3;x++)for(int z=-7;z<=-3;z++)if(x!=0||z!=-4)if(Math.abs(x)==3&&z!=-5||Math.abs(x)<3&&(z==-7||z==-3||z==-6||z==-4)&&!(x==0&&z==-6))fit(l,b,x,0,z,RUNNER,Direction.EAST);
        box(l,b,6,1,-3,6,2,-2,HouseReadRegistry.WINDOW.get());box(l,b,7,0,-4,8,4,-1,HouseReadRegistry.AFTERNOON.get());box(l,b,7,1,-3,7,2,-2,Blocks.AIR.defaultBlockState());
        // Each half of the tin knows its crayons, which differ for every reader.
        BuildBlocks.after(l,()->{for(var half:List.of(TIN_LEFT_AT,TIN_RIGHT_AT))if(l.getBlockEntity(b.offset(half)) instanceof HouseReadPropBlock.Fitting fitting){var t=new CompoundTag();t.putIntArray("Colours",colours(owner,half.equals(TIN_RIGHT_AT)));t.putInt("Taken",-1);fitting.setRead(t);}
            if(l.getBlockEntity(b.offset(SHEET_AT)) instanceof HouseReadPropBlock.Fitting sheet)sheet.setRead(blank());});
    }
    static CompoundTag blank(){var t=new CompoundTag();for(var r:Region.values())t.putInt(key(r),-1);return t;}

    // ---- Using it.
    private static CompoundTag visit(ServerPlayer p){return HouseReadSites.state(p.server,p.getUUID()).getCompound(KITCHEN.key());}
    private static void save(ServerPlayer p,CompoundTag visit){var own=HouseReadSites.state(p.server,p.getUUID());own.put(KITCHEN.key(),visit);HouseReadSites.save(p.server,p.getUUID(),own);}
    private static CompoundTag seen(ServerPlayer p){return HouseRead.seen(LabyrinthData.get(p.server),p.getUUID()).getCompound(KITCHEN.key());}
    private static HouseReadPropBlock.Fitting fitting(ServerPlayer p,BlockPos at){return p.serverLevel().getBlockEntity(at) instanceof HouseReadPropBlock.Fitting f?f:null;}
    static boolean kitchenPart(HouseReadPropBlock.Part part){return part==TIN_LEFT||part==TIN_RIGHT||part==SHEET||part==FRIDGE||part==FRIDGE_TOP||part==RADIO;}
    /** The crayon this reader is holding, or -1. */
    public static int held(ServerPlayer p){var hand=p.getMainHandItem();if(!hand.is(HouseReadRegistry.CRAYON.get())||!HouseReadSites.loan(hand,p.getUUID()))return -1;var data=hand.get(DataComponents.CUSTOM_DATA);return data==null?-1:data.copyTag().getInt("Crayon");}
    /** The kitchen's fittings, and its sink, plate and clock. False for anything that is not the kitchen's to answer. */
    public static boolean use(ServerPlayer p,BlockPos at,BlockState state,BlockHitResult hit){
        var part=state.getValue(HouseReadPropBlock.PART);boolean own=HouseReadSites.inside(p)==KITCHEN;
        if(!own)return kitchenPart(part);
        var b=HouseReadSites.base(p.server,p.getUUID(),KITCHEN);if(b==null||!bounds(b).contains(at.getCenter()))return kitchenPart(part);
        switch(part){
            case TIN_LEFT,TIN_RIGHT->tin(p,b,at,hit);
            case SHEET->sheet(p,b,at,hit);
            case FRIDGE,FRIDGE_TOP->fridge(p,b);
            case RADIO->{boolean off=!state.getValue(HouseReadPropBlock.TURNED);p.serverLevel().setBlock(at,state.setValue(HouseReadPropBlock.TURNED,off),Block.UPDATE_CLIENTS);HouseReadSites.sound(p,SoundEvents.LEVER_CLICK,at,.25f);HouseReadSites.act(p,KITCHEN,off?"turned the radio off":"turned the radio back on, low");}
            case SINK,TAP->{HouseReadSites.sound(p,HouseReadRegistry.TAP.get(),at,.4f);HouseReadSites.act(p,KITCHEN,"ran the cold tap at the kitchen sink");}
            case PLATE->{p.displayClientMessage(Component.literal("A sandwich cut into triangles, left out for after school."),true);HouseReadSites.act(p,KITCHEN,"looked at the plate left out for after school");}
            case WALL_CLOCK->p.displayClientMessage(Component.literal("The kitchen clock says a little after four."),true);
            default->{return false;}
        }
        return true;
    }
    private static void tin(ServerPlayer p,BlockPos b,BlockPos at,BlockHitResult hit){
        var fitting=fitting(p,at);if(fitting==null)return;int slot=slotAt(at,hit.getLocation());
        if(slot<0){if(p.serverLevel().getBlockState(b.offset(TIN_LEFT_AT)).getValue(HouseReadPropBlock.WRITING))HouseReadTexts.open(p,KITCHEN);else p.displayClientMessage(Component.literal("A sun with a face, printed inside the lid."),true);return;}
        var t=visit(p);if(t.getBoolean("Hung")){p.displayClientMessage(Component.literal("The picture's done."),true);return;}
        int colour=fitting.read().getIntArray("Colours").length>slot?fitting.read().getIntArray("Colours")[slot]:-1;if(colour<0)return;
        // What the reader holds goes back first; picking the same crayon again just puts it back.
        boolean same=t.contains("HeldAt")&&t.getLong("HeldAt")==at.asLong()&&t.getInt("HeldSlot")==slot;
        putBack(p,b);if(same){HouseReadSites.act(p,KITCHEN,"put the crayon back in the tin");return;}
        var crayon=new ItemStack(HouseReadRegistry.CRAYON.get());crayon.set(DataComponents.DYED_COLOR,new DyedItemColor(rgb(colour),false));CustomData.update(DataComponents.CUSTOM_DATA,crayon,c->c.putInt("Crayon",colour));
        HouseReadMoments.borrow(p,KITCHEN,crayon,false);
        t=visit(p);t.putLong("HeldAt",at.asLong());t.putInt("HeldSlot",slot);save(p,t);
        var read=fitting.read();read.putInt("Taken",slot);fitting.setRead(read);HouseReadSites.sound(p,SoundEvents.ITEM_PICKUP,at,.25f);
        if(!seen(p).contains("FirstCrayon"))HouseRead.see(p,KITCHEN,s->{s.putInt("FirstCrayon",colour);s.putInt("FirstSlot",slot+(at.equals(b.offset(TIN_RIGHT_AT))?10:0));});
        HouseReadSites.act(p,KITCHEN,"took a crayon from the tin");
    }
    /** The crayon in hand back into its place in the tin. */
    static void putBack(ServerPlayer p,BlockPos b){
        var t=visit(p);if(!t.contains("HeldAt"))return;var at=BlockPos.of(t.getLong("HeldAt"));
        if(bounds(b).contains(at.getCenter())){var fitting=fitting(p,at);if(fitting!=null){var read=fitting.read();read.putInt("Taken",-1);fitting.setRead(read);}}
        t.remove("HeldAt");t.remove("HeldSlot");save(p,t);HouseReadMoments.giveBack(p,KITCHEN,false);
    }
    private static void sheet(ServerPlayer p,BlockPos b,BlockPos at,BlockHitResult hit){
        var state=p.serverLevel().getBlockState(at);if(state.getValue(HouseReadPropBlock.TURNED))return;var fitting=fitting(p,at);if(fitting==null)return;
        int colour=held(p);
        if(colour<0){p.displayClientMessage(Component.literal(coloured(fitting.read())?"Your picture. It isn't up on the fridge yet.":"A drawing of you, in pencil, waiting to be coloured in."),true);HouseReadSites.act(p,KITCHEN,"looked at the drawing");return;}
        var region=regionAt(at,hit.getLocation());if(region==null){p.displayClientMessage(Component.literal("You draw a line in the margin."),true);return;}
        var read=fitting.read();int before=read.getInt(key(region));read.putInt(key(region),colour);fitting.setRead(read);
        HouseReadSites.sound(p,SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT,at,.45f);
        var t=visit(p);t.putBoolean("NoteArmed",true);if(!t.contains("ColouredAt"))t.putInt("ColouredAt",t.getInt("Age"));save(p,t);
        HouseReadSites.act(p,KITCHEN,switch(region){case FACE->"coloured the face";case HANDS->"coloured the hands";case HAIR->"coloured the hair";case CLOTHES->"coloured the clothes";});
        signal(p,region,colour,before,read);
    }
    /**
     * The read. The first face in a colour a person could be sets {@code TONE} tentatively; hands in the same crayon settle it;
     * a face coloured again in another tone, or hands in another, disagree, and the next decides. A face in blue or green is
     * play and reads nothing. The hair's last colour, if it is a hair colour, is kept for the House's body.
     */
    static void signal(ServerPlayer p,Region region,int colour,int before,CompoundTag sheet){
        var seen=seen(p);if(colour==before)return;
        switch(region){
            case FACE->{
                HouseRead.see(p,KITCHEN,s->{s.putInt("Face",colour);if(!s.contains("FirstFace"))s.putInt("FirstFace",colour);});
                if(!person(colour)){HouseRead.flag(p,KITCHEN,"Play");return;}
                if(!seen.contains("Tone")){HouseRead.first(p,KITCHEN,"Tone",hex(colour));HouseRead.signal(p,HouseRead.Trait.TONE,hex(colour),false);}
                else HouseRead.signal(p,HouseRead.Trait.TONE,hex(colour),true);
            }
            case HANDS->{
                HouseRead.see(p,KITCHEN,s->s.putInt("Hands",colour));
                if(person(colour)&&seen.contains("Tone"))HouseRead.signal(p,HouseRead.Trait.TONE,hex(colour),true);
            }
            case HAIR->HouseRead.see(p,KITCHEN,s->s.putInt("Hair",colour));
            case CLOTHES->HouseRead.see(p,KITCHEN,s->s.putInt("Clothes",colour));
        }
    }
    static boolean coloured(CompoundTag sheet){for(var r:Region.values())if(sheet.contains(key(r))&&sheet.getInt(key(r))>=0)return true;return false;}
    private static void fridge(ServerPlayer p,BlockPos b){
        var t=visit(p);if(t.getBoolean("Hung")){p.displayClientMessage(Component.literal("Your picture, under a magnet."),true);return;}
        var sheetAt=b.offset(SHEET_AT);var sheet=fitting(p,sheetAt);var read=sheet==null?new CompoundTag():sheet.read();
        if(!coloured(read)){p.displayClientMessage(Component.literal(t.getBoolean("FridgeSeen")?"There's room on the door for one more.":"Magnets, and old drawings: a house, a dog, a sun with a face, a rainbow."),true);t.putBoolean("FridgeSeen",true);save(p,t);HouseReadSites.act(p,KITCHEN,"looked at the drawings on the fridge");return;}
        putBack(p,b);
        var top=b.offset(FRIDGE_AT.above());var door=fitting(p,top);if(door!=null)door.setRead(read);var topState=p.serverLevel().getBlockState(top);if(topState.is(HouseReadRegistry.FITTING.get()))p.serverLevel().setBlock(top,topState.setValue(HouseReadPropBlock.TURNED,true),Block.UPDATE_CLIENTS);
        var sheetState=p.serverLevel().getBlockState(sheetAt);if(sheetState.is(HouseReadRegistry.FITTING.get()))p.serverLevel().setBlock(sheetAt,sheetState.setValue(HouseReadPropBlock.TURNED,true),Block.UPDATE_CLIENTS);
        t=visit(p);t.putBoolean("Hung",true);t.putInt("HungAt",t.getInt("Age"));t.putBoolean("NoteArmed",true);save(p,t);
        boolean alike=read.getInt("Face")>=0&&read.getInt("Face")==read.getInt("Hands");
        HouseRead.see(p,KITCHEN,s->{s.putBoolean("Hung",true);if(alike)s.putBoolean("Alike",true);for(var r:Region.values())if(read.getInt(key(r))>=0)s.putBoolean("Drew"+key(r),true);});
        HouseReadSites.sound(p,SoundEvents.ITEM_FRAME_ADD_ITEM,top,.5f);HouseReadSites.act(p,KITCHEN,"put the picture up on the fridge");
        unlock(p,b);
    }
    private static void unlock(ServerPlayer p,BlockPos b){for(var at:List.of(b.offset(EXIT),b.offset(EXIT).above())){var s=p.serverLevel().getBlockState(at);if(s.getBlock() instanceof DoorBlock)p.serverLevel().setBlock(at,s.setValue(DoorBlock.OPEN,true),Block.UPDATE_CLIENTS);}HouseReadSites.sound(p,SoundEvents.WOODEN_DOOR_OPEN,b.offset(EXIT),.4f);}
    private static void voice(ServerPlayer p,String words){p.displayClientMessage(Component.literal("“"+words+"”").withStyle(net.minecraft.ChatFormatting.GRAY,net.minecraft.ChatFormatting.ITALIC),false);}
    /** The room's clock: its sounds, the voice from the next room for a reader who has not drawn or not put it up, and the way on. */
    static void tick(ServerPlayer p,BlockPos b,CompoundTag t,int age){
        if(age%160==1)HouseReadSites.sound(p,HouseReadRegistry.RADIATOR.get(),b.offset(FRIDGE_AT),.22f);
        if(age%20==1)HouseReadSites.sound(p,HouseReadRegistry.CLOCK.get(),b.offset(CLOCK_AT),.18f);
        var radio=p.serverLevel().getBlockState(b.offset(RADIO_AT));if(age%240==40&&radio.is(HouseReadRegistry.FITTING.get())&&!radio.getValue(HouseReadPropBlock.TURNED))HouseReadSites.sound(p,HouseReadRegistry.NEWS.get(),b.offset(RADIO_AT),.25f);
        if(age>=200)t.putBoolean("NoteArmed",true);
        boolean hung=t.getBoolean("Hung");
        if(!hung&&!t.contains("ColouredAt")&&age==600)voice(p,"Go on, finish your picture. You can put it up on the fridge when you're done.");
        if(!hung&&t.contains("ColouredAt")&&age==Math.max(600,t.getInt("ColouredAt")+600))voice(p,"Put it up on the fridge when you're done, so everybody can see.");
        if(hung&&p.getZ()<b.getZ()-12.4&&p.getX()>b.getX()+4&&p.getX()<b.getX()+6)HouseReadSites.leave(p,true);
    }
    /** Leaving: the crayon in hand goes back into the tin. */
    static void left(ServerPlayer p,CompoundTag t){
        var b=HouseReadSites.base(p.server,p.getUUID(),KITCHEN);var level=p.server.getLevel(KITCHEN.dimension());
        if(b!=null&&level!=null&&t.contains("HeldAt")){var at=BlockPos.of(t.getLong("HeldAt"));if(bounds(b).contains(at.getCenter())&&level.getBlockEntity(at) instanceof HouseReadPropBlock.Fitting f){var read=f.read();read.putInt("Taken",-1);f.setRead(read);}}
        t.remove("HeldAt");t.remove("HeldSlot");
    }
    // ---- What it gives the House's body.
    /** The tone the House's body wears: the face's colour, as read, or -1 for the account's default skin's. */
    public static int tone(LabyrinthData d,UUID reader){String hex=HouseRead.value(d,reader,HouseRead.Trait.TONE,false);return hex.length()==7?Integer.parseInt(hex.substring(1),16):-1;}
    /** The hair colour the House's body wears: the drawing's hair, if it was drawn in a colour hair can be, or -1. */
    public static int hair(LabyrinthData d,UUID reader){if(!HouseRead.enabled(HouseRead.Trait.TONE))return -1;var s=HouseRead.seen(d,reader).getCompound(KITCHEN.key());return s.contains("Hair")&&hairColour(s.getInt("Hair"))?rgb(s.getInt("Hair")):-1;}
}
