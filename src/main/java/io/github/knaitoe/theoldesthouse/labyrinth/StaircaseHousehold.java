package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.214: the household on the great staircase (docs/STAIRCASE_HOUSEHOLD_0_4_214.md, docs/STAIRCASE_HOUSEHOLD_PROPOSAL.md).
 *
 * <p>One rhythm for each reader's descent, shared by the five leaks and the household's doors: the descent above the cell is eight
 * stretches of 160 blocks; at most one household thing happens in a stretch, none straight after a stretch that had one, none in the
 * first, and the eighth holds only the last landing. Nothing on the homeward climb.
 *
 * <p>A door's source sheet, read to its last line where the rhythm allows, leaves a painted house door in the shaft wall beside that
 * landing, with a doorstep of household boards out to it. Every block it changes is saved and put back when the door goes: once it
 * has been used and its reader has stepped off the boards, or once nobody it belongs to is still on that descent.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class StaircaseHousehold {
    public static final String STATE="staircase_household_0414",DOORS="staircase_household_doors_0414";
    public static final int STRETCH=160,STRETCHES=8;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private StaircaseHousehold(){}

    /** The household room a source sheet opens, or null. Only rooms already built are listed. */
    public static StaircaseLeakRooms.Kind room(int index){
        return switch(index){
            case 8,11,17,23->StaircaseLeakRooms.Kind.DINING;
            case 15,27,33,45->StaircaseLeakRooms.Kind.BEDROOM;
            case 4,18,30,42,47->StaircaseLeakRooms.Kind.MENDING;
            case 3,7,14,29,38->StaircaseLeakRooms.Kind.DAWN;
            default->null;
        };
    }
    public static CompoundTag own(ServerPlayer p){return LabyrinthData.get(p.server).stateEntry(STATE,p.getUUID().toString());}
    static void save(ServerPlayer p,CompoundTag own){LabyrinthData.get(p.server).setStateEntry(STATE,p.getUUID().toString(),own);}
    /** Fixtures and operators: forget this reader's rhythm for the current descent (their results are kept). */
    public static void forget(ServerPlayer p){var own=own(p);own.remove("Marks");save(p,own);}

    /** Which 160-block stretch of the descent a height is in: 0 at the top, 7 at the bottom above the cell. */
    public static int stretch(double y){return Math.max(0,Math.min(STRETCHES-1,(int)Math.floor((FinaleArchitecture.TOP-y)/STRETCH)));}
    public static int stretch(ServerPlayer p){return stretch(p.getY());}
    /** The rhythm: this reader may have a leak or a door here, now. */
    public static boolean allows(ServerPlayer p){
        if(FinaleProgress.phase(p.server,p.getUUID())!=FinaleProgress.Phase.STAIRCASE||p.getY()<FinaleArchitecture.ARENA)return false;
        int s=stretch(p);if(s<=0||s>=STRETCHES-1)return false;var marks=own(p).getCompound("Marks");
        return !marks.contains(Integer.toString(s))&&!marks.contains(Integer.toString(s-1));
    }
    /** Something happened in this stretch: a leak opened, or a door was left. */
    public static void mark(ServerPlayer p,String what){var own=own(p);var marks=own.getCompound("Marks");marks.putString(Integer.toString(stretch(p)),what);own.put("Marks",marks);save(p,own);}

    // ------------------------------------------------------------------------------------------------ doors
    /** Where a sheet's door stands: in the shaft wall level with its landing, on the wall's outer face. */
    public static BlockPos doorAt(BlockPos origin,BlockPos paper){
        var b=FinaleArchitecture.base(origin);int sx=Integer.signum(paper.getX()-b.getX()),sz=Integer.signum(paper.getZ()-b.getZ());
        return b.offset(sx*FinaleArchitecture.SHAFT_RADIUS,paper.getY()-b.getY(),sz*FinaleArchitecture.STAIR_RADIUS);
    }
    /** The planned blocks of a door, its doorstep and the gap in the landing's rail, in placing order. */
    static LinkedHashMap<BlockPos,BlockState> doorPlan(BlockPos origin,BlockPos door){
        var b=FinaleArchitecture.base(origin);int sx=Integer.signum(door.getX()-b.getX()),sz=Integer.signum(door.getZ()-b.getZ());int y=door.getY(),z=door.getZ();
        var plan=new LinkedHashMap<BlockPos,BlockState>();var boards=Blocks.OAK_PLANKS.defaultBlockState();var air=Blocks.AIR.defaultBlockState();var frame=Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();
        Direction out=sx>0?Direction.EAST:Direction.WEST;
        for(int d=FinaleArchitecture.STAIR_RADIUS+7;d<FinaleArchitecture.SHAFT_RADIUS;d++){int x=b.getX()+sx*d;
            for(int w=-2;w<=2;w++){plan.put(new BlockPos(x,y-1,z+w),boards);
                for(int h=0;h<3;h++)plan.put(new BlockPos(x,y+h,z+w),Math.abs(w)==2&&h==0?Blocks.OAK_FENCE.defaultBlockState().setValue(CrossCollisionBlock.EAST,true).setValue(CrossCollisionBlock.WEST,true):air);}
        }
        for(int w=-1;w<=1;w++)plan.put(new BlockPos(b.getX()+sx*(FinaleArchitecture.STAIR_RADIUS+6),y,z+w),air);
        int wx=b.getX()+sx*FinaleArchitecture.SHAFT_RADIUS;
        for(int h=0;h<3;h++){plan.put(new BlockPos(wx,y+h,z-1),frame);plan.put(new BlockPos(wx,y+h,z+1),frame);}plan.put(new BlockPos(wx,y+2,z),frame.setValue(RotatedPillarBlock.AXIS,Direction.Axis.Z));
        plan.put(new BlockPos(wx+sx,y,z),Blocks.DEEPSLATE_TILES.defaultBlockState());plan.put(new BlockPos(wx+sx,y+1,z),Blocks.DEEPSLATE_TILES.defaultBlockState());
        var leaf=StaircaseLeakRegistry.HOUSEHOLD_DOOR.get().defaultBlockState().setValue(DoorBlock.FACING,out).setValue(DoorBlock.HINGE,DoorHingeSide.LEFT);
        plan.put(new BlockPos(wx,y,z),leaf);plan.put(new BlockPos(wx,y+1,z),leaf.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
        plan.put(new BlockPos(wx-sx,y,z),Blocks.BROWN_CARPET.defaultBlockState());plan.put(new BlockPos(wx-2*sx,y+2,z),Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,9));
        return plan;
    }
    private static AABB step(BlockPos origin,BlockPos door){var b=FinaleArchitecture.base(origin);int sx=Integer.signum(door.getX()-b.getX());
        int x0=b.getX()+sx*(FinaleArchitecture.STAIR_RADIUS+6),x1=b.getX()+sx*FinaleArchitecture.SHAFT_RADIUS;
        return new AABB(Math.min(x0,x1),door.getY()-1,door.getZ()-2,Math.max(x0,x1)+1,door.getY()+3,door.getZ()+3);}
    private static String key(BlockPos at){return Long.toString(at.asLong());}

    /** A household sheet read to its last line on the stairs (StaircaseLeaks.offer). */
    static void read(ServerPlayer p,BlockPos paper,int index){
        var room=room(index);var origin=HouseSavedData.get(p.server).houseOrigin();if(room==null||origin==null||!allows(p))return;
        if(StaircaseLeaks.done(p,index))return;
        var data=LabyrinthData.get(p.server);var door=doorAt(origin,paper);var record=data.stateEntry(DOORS,key(door));var level=p.serverLevel();
        if(record.isEmpty()){
            var plan=doorPlan(origin,door);
            if(plan.keySet().stream().anyMatch(at->!level.hasChunkAt(at)))return;
            if(!level.getEntitiesOfClass(LivingEntity.class,step(origin,door).deflate(.01),e->e.isAlive()&&!e.isSpectator()).isEmpty())return;
            var changes=new ListTag();
            for(var e:plan.entrySet()){var old=level.getBlockState(e.getKey());if(level.getBlockEntity(e.getKey())!=null)continue;var c=new CompoundTag();c.putLong("Pos",e.getKey().asLong());c.put("State",NbtUtils.writeBlockState(old));changes.add(c);}
            for(var e:plan.entrySet())if(level.getBlockEntity(e.getKey())==null)level.setBlock(e.getKey(),e.getValue(),F);
            record.put("Changes",changes);record.putInt("Index",index);record.putInt("Room",room.ordinal());record.putLong("Paper",paper.asLong());
            level.playSound(null,door,SoundEvents.WOODEN_DOOR_CLOSE,SoundSource.BLOCKS,.5F,.9F);
        }
        // A reader who reads another of the door's sheets has it again, even if they have been through it before.
        var owners=record.getCompound("Owners");owners.putInt(p.getUUID().toString(),own(p).getInt("Descent"));record.put("Owners",owners);
        var used=record.getCompound("Used");used.remove(p.getUUID().toString());record.put("Used",used);
        var papers=record.getCompound("Papers");papers.putLong(p.getUUID().toString(),paper.asLong());papers.putInt(p.getUUID()+"Index",index);record.put("Papers",papers);
        data.setStateEntry(DOORS,key(door),record);mark(p,"door:"+index);
        StaircaseLeaks.caption(p,StaircaseHouseholdTexts.doorLeft(room));
        io.github.knaitoe.theoldesthouse.house.PlaytestLog.event(p,"household_door_left","index",index,"room",room.name());
    }
    /** The record of the door at, or above, this block, or null. */
    private static Map.Entry<String,CompoundTag> doorRecord(MinecraftServer server,BlockPos at){
        var data=LabyrinthData.get(server);for(var k:data.stateKeys(DOORS)){var lower=BlockPos.of(Long.parseLong(k));if(lower.equals(at)||lower.above().equals(at))return Map.entry(k,data.stateEntry(DOORS,k));}return null;
    }
    private static boolean owns(ServerPlayer p,CompoundTag record){var id=p.getUUID().toString();return record.getCompound("Owners").contains(id)&&record.getCompound("Owners").getInt(id)==own(p).getInt("Descent")&&!record.getCompound("Used").getBoolean(id);}
    /** The door was tried by hand (HouseholdDoorBlock). */
    public static boolean useDoor(ServerPlayer p,BlockPos at){
        var entry=doorRecord(p.server,at);if(entry==null)return false;var record=entry.getValue();var lower=BlockPos.of(Long.parseLong(entry.getKey()));
        if(p.isSpectator())return true;
        if(!owns(p,record)){StaircaseLeaks.caption(p,"It isn't yours.");p.playNotifySound(SoundEvents.WOODEN_DOOR_CLOSE,SoundSource.BLOCKS,.3F,.6F);return true;}
        var papers=record.getCompound("Papers");String id=p.getUUID().toString();int index=papers.contains(id+"Index")?papers.getInt(id+"Index"):record.getInt("Index");
        var paper=papers.contains(id)?BlockPos.of(papers.getLong(id)):BlockPos.of(record.getLong("Paper"));
        // One door in the wall can belong to several readers, each through their own sheet: the room is the one that reader's sheet opens.
        var kind=room(index)!=null?room(index):StaircaseLeakRooms.Kind.values()[record.getInt("Room")];
        if(!StaircaseLeaks.enterDoor(p,index,kind,paper,lower))StaircaseLeaks.caption(p,"Not now.");
        else p.playNotifySound(SoundEvents.WOODEN_DOOR_OPEN,SoundSource.BLOCKS,.5F,1);
        return true;
    }
    /** Back from a household room through its door (StaircaseLeaks.returnNow). A forced return records nothing and keeps the door. */
    static void returned(ServerPlayer p,CompoundTag session,boolean forced){
        var kind=StaircaseLeakRooms.Kind.values()[session.getInt("Kind")];var chore=session.getCompound("Chore");
        if(forced&&!chore.contains("Result"))return;
        String result=chore.contains("Result")?chore.getString("Result"):"failure";
        var own=own(p);var rooms=own.getCompound("Rooms");var room=rooms.getCompound(kind.name());room.putInt("Tries",room.getInt("Tries")+1);room.putString("Last",result);if(result.equals("success"))room.putBoolean("Success",true);
        for(var k:chore.getAllKeys())if(k.startsWith("Tally"))room.putBoolean(k,true);
        rooms.put(kind.name(),room);own.put("Rooms",rooms);
        var accounts=own.getCompound("Accounts");accounts.putString(Long.toString(session.getLong("Paper")),StaircaseHouseholdTexts.account(kind,result,p.getGameProfile().getName(),chore));own.put("Accounts",accounts);save(p,own);
        if(session.contains("Door")){var data=LabyrinthData.get(p.server);String k=Long.toString(session.getLong("Door"));var record=data.stateEntry(DOORS,k);if(!record.isEmpty()){var used=record.getCompound("Used");used.putBoolean(p.getUUID().toString(),true);record.put("Used",used);data.setStateEntry(DOORS,k,record);}}
        io.github.knaitoe.theoldesthouse.house.PlaytestLog.event(p,"household_room","room",kind.name(),"result",result);
    }
    /** A leak's room closed: it counts in the household's rooms, as a door's vignette does. */
    static void leakReturned(ServerPlayer p,int index,boolean finished){var own=own(p);var leaks=own.getCompound("Leaks");if(!leaks.getBoolean(Integer.toString(index)+"Finished"))leaks.putBoolean(Integer.toString(index)+"Finished",finished);leaks.putBoolean(Integer.toString(index),true);own.put("Leaks",leaks);save(p,own);}
    /** How many of the household's rooms this reader has been in: leaks and doors' vignettes, whatever came of them. */
    public static int rooms(ServerPlayer p){var own=own(p);int n=0;for(var k:own.getCompound("Leaks").getAllKeys())if(!k.endsWith("Finished"))n++;return n+own.getCompound("Rooms").size();}
    /** The household's note about the reader, for the sheet that opened its room: shown after the sheet's own words. */
    public static ItemStack withAccount(ServerPlayer p,BlockPos paper,ItemStack book){
        String text=own(p).getCompound("Accounts").getString(Long.toString(paper.asLong()));if(text.isEmpty())return book;
        var content=book.get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT);if(content==null)return book;
        var pages=new ArrayList<Component>();for(var page:content.pages())pages.add(page.raw());pages.add(HouseWriting.page(HouseWriting.WritingStyle.KAREN,text));
        return HouseWriting.book(content.title().raw(),content.author(),pages);
    }

    @SubscribeEvent public static void tick(ServerTickEvent.Post event){
        var server=event.getServer();if(server.getTickCount()%10!=0)return;var origin=HouseSavedData.get(server).houseOrigin();if(origin==null)return;
        // A descent begins each time a reader comes into the staircase from outside it.
        for(var p:server.getPlayerList().getPlayers()){
            if(p.isSpectator()||StaircaseLeaks.active(p))continue;
            boolean inside=p.serverLevel().dimension().equals(HouseDimensions.INTERIOR)&&FinaleArchitecture.contains(origin,p.blockPosition());
            var own=own(p);if(inside==own.getBoolean("Inside"))continue;
            own.putBoolean("Inside",inside);if(inside){own.putInt("Descent",own.getInt("Descent")+1);own.remove("Marks");}save(p,own);
        }
        if(server.getTickCount()%40!=0)return;
        var level=server.getLevel(HouseDimensions.INTERIOR);if(level==null)return;var data=LabyrinthData.get(server);
        for(var k:new ArrayList<>(data.stateKeys(DOORS))){
            var record=data.stateEntry(DOORS,k);var owners=record.getCompound("Owners");var used=record.getCompound("Used");boolean changed=false;
            for(var id:new ArrayList<>(owners.getAllKeys())){
                var p=server.getPlayerList().getPlayer(UUID.fromString(id));
                boolean away=p==null||used.getBoolean(id)&&!StaircaseLeaks.active(p)||!StaircaseLeaks.active(p)&&own(p).getInt("Descent")!=owners.getInt(id);
                if(away&&!(p!=null&&StaircaseLeaks.active(p))){owners.remove(id);changed=true;}
            }
            if(changed){record.put("Owners",owners);data.setStateEntry(DOORS,k,record);}
            if(!owners.isEmpty())continue;
            var door=BlockPos.of(Long.parseLong(k));var changes=record.getList("Changes",Tag.TAG_COMPOUND);
            boolean loaded=changes.stream().allMatch(t->level.hasChunkAt(BlockPos.of(((CompoundTag)t).getLong("Pos"))));
            if(!loaded||!level.getEntitiesOfClass(LivingEntity.class,step(origin,door).inflate(.5),e->e.isAlive()).isEmpty())continue;
            restore(level,record);data.setStateEntry(DOORS,k,new CompoundTag());
        }
    }
    static void restore(ServerLevel level,CompoundTag record){
        var blocks=level.registryAccess().lookupOrThrow(Registries.BLOCK);var changes=record.getList("Changes",Tag.TAG_COMPOUND);
        // The door's leaves go first, so neither half breaks the other as it is put back.
        for(int pass=0;pass<2;pass++)for(int i=changes.size()-1;i>=0;i--){var c=changes.getCompound(i);var at=BlockPos.of(c.getLong("Pos"));boolean leaf=level.getBlockState(at).getBlock() instanceof HouseholdDoorBlock;
            if(pass==0&&leaf)level.setBlock(at,Blocks.AIR.defaultBlockState(),F|Block.UPDATE_SUPPRESS_DROPS);
            if(pass==1)level.setBlock(at,NbtUtils.readBlockState(blocks,c.getCompound("State")),F|Block.UPDATE_SUPPRESS_DROPS);}
    }
}
