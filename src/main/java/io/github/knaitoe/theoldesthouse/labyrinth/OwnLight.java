package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.213, your own light (docs/OWN_LIGHT_0_4_213.md), the last of the dark halls' horrors.
 *
 * <p>Twin branches side by side with gaps in the wall between. Someone walks the other branch level with the reader: their own body,
 * their own walk, a torch in hand. It sets a torch across from every one the reader sets, stops when they stop and turns its head when
 * they turn theirs: a beat late, then a beat early. Halfway, it stops matching. It steps through a gap behind the reader and walks
 * back down their branch the way they came, taking their torches down one at a time with the House's low rumble, and goes out
 * through the way in.
 *
 * <p>Going on without following it, the reader finds their torches set in the far room's wall in the order they were placed, burning
 * ({@code let_it_take_them}). Following it back, every torch is gone at once, the House rumbles, and they come to at the start of the
 * fork with no light but what is in their hands; nothing placed comes back. Either way it goes home first: the next time the reader
 * is at their bed, it has been slept in, a chest beside it is turned round, the lights left burning are out, and a note on the
 * pillow in their own handwriting says "Thank you for the light", or, if they did not follow, only "Not yet."
 *
 * <p>Each reader's double is theirs alone (a private actor in the reader's own skin): a peer sees an empty branch.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class OwnLight {
    private OwnLight(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.OWN_LIGHT;
    /** How many ticks behind, then ahead, it keeps; how far back it reads the reader's walk. */
    public static final int BEAT=10,HISTORY=40;
    /** Where it stops matching a beat late and starts matching a beat early. */
    public static final int EARLY_Z=-12;
    /** How fast it walks back; how near a torch it takes it down; how far back the reader must come to be following. */
    public static final double BACK=.13,TAKE=1.6,FOLLOWING=6;
    private static final Map<UUID,ArrayDeque<double[]>> TRAIL=new HashMap<>();

    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    @Nullable static BlockPos base(ServerPlayer p){return LiteraryVignettes.base(p,PLACE);}
    static boolean inside(ServerPlayer p){return LiteraryVignettes.inside(p,PLACE);}
    static Vec3 rel(ServerPlayer p,BlockPos b){return p.position().subtract(b.getX(),b.getY(),b.getZ());}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    static void heard(ServerPlayer p,Vec3 at,SoundEvent s,float volume,float pitch){p.connection.send(new ClientboundSoundPacket(Holder.direct(s),SoundSource.AMBIENT,at.x,at.y,at.z,volume,pitch,p.getRandom().nextLong()));}
    static boolean lights(BlockState s){return s.is(Blocks.TORCH)||s.is(Blocks.WALL_TORCH)||s.is(Blocks.SOUL_TORCH)||s.is(Blocks.SOUL_WALL_TORCH)||s.is(Blocks.LANTERN)||s.is(Blocks.SOUL_LANTERN);}
    public static List<BlockPos> torches(CompoundTag own){var out=new ArrayList<BlockPos>();for(var e:own.getList("Torches213",Tag.TAG_LONG))out.add(BlockPos.of(((LongTag)e).getAsLong()));return out;}
    static @Nullable LiteraryActor dbl(ServerPlayer p){return LiteraryVignettes.actor(p,PLACE,"Double",LiteraryActor.LOOKOUT_DOUBLE,new BlockPos(0,0,-2),true);}
    static void drop(ServerPlayer p){var d=LabyrinthData.get(p.server);var s=LiteraryVignettes.shared(d,PLACE);String id="Double_"+p.getUUID();
        if(s.hasUUID(id)){if(p.serverLevel().getEntity(s.getUUID(id)) instanceof LiteraryActor a)a.discard();s.remove(id);LiteraryVignettes.shared(d,PLACE,s);}}
    static boolean hasDouble(ServerPlayer p){return LiteraryVignettes.shared(LabyrinthData.get(p.server),PLACE).hasUUID("Double_"+p.getUUID());}

    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        TRAIL.remove(p.getUUID());drop(p);
        if(own.getBoolean("Ready")&&!own.getBoolean("Completed")){cue(p,"At the end, past the fork, the stub of a torch burnt down to nothing.");return;}
        own.remove("Run213");own.remove("Torches213");own.remove("Turned213");own.remove("Followed213");own.remove("TurnZ213");own.putInt("Placed213",0);own.remove("Taken213");
        cue(p,"The hall forks into two, side by side, with a wall between. It is very dark down both.");
    }

    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        if(rel.equals(OwnLightSite.STUB)&&!own.getBoolean("Ready")){cue(p,"The stub of a torch, burnt down to nothing. Somebody's light.");return true;}
        return false;
    }

    // ------------------------------------------------------------------------------------------------ the reader's torches
    @SubscribeEvent public static void placed(BlockEvent.EntityPlaceEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p)||e.isCanceled()||!inside(p)||!lights(e.getPlacedBlock()))return;var b=base(p);if(b==null)return;
        var own=own(p);var rel=e.getPos().subtract(b);if(!OwnLightSite.inside(Vec3.atCenterOf(rel))||own.getBoolean("Turned213"))return;
        var list=own.getList("Torches213",Tag.TAG_LONG);list.add(LongTag.valueOf(rel.asLong()));own.put("Torches213",list);own.putInt("Placed213",own.getInt("Placed213")+1);save(p,own);
        // across the wall, a beat later, it sets one too
        if(own.getBoolean("Run213")){var across=b.offset(OwnLightSite.across(rel));var l=p.serverLevel();
            if(l.getBlockState(across).isAir()){var s=e.getPlacedBlock();if(s.hasProperty(WallTorchBlock.FACING)){var f=s.getValue(WallTorchBlock.FACING);if(f.getAxis()==Direction.Axis.X)s=s.setValue(WallTorchBlock.FACING,f.getOpposite());}
                if(s.canSurvive(l,across)){l.setBlock(across,s,Block.UPDATE_ALL);var mine=own(p);var m=mine.getList("Mirrored213",Tag.TAG_LONG);m.add(LongTag.valueOf(OwnLightSite.across(rel).asLong()));mine.put("Mirrored213",m);save(p,mine);}}}
    }

    // ------------------------------------------------------------------------------------------------ the double
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();
        for(var p:s.getPlayerList().getPlayers()){
            if(!LiteraryVignettes.participant(p)||!inside(p)||!p.isAlive()){if(TRAIL.containsKey(p.getUUID())&&!inside(p)){TRAIL.remove(p.getUUID());drop(p);}continue;}
            var b=base(p);if(b==null)continue;var own=own(p);if(own.getBoolean("Ready"))continue;
            var r=rel(p,b);var l=p.serverLevel();
            var trail=TRAIL.computeIfAbsent(p.getUUID(),k->new ArrayDeque<>());trail.addLast(new double[]{r.x,r.z,p.getYRot(),p.getYHeadRot(),p.isCrouching()?1:0});while(trail.size()>HISTORY)trail.removeFirst();
            if(!own.getBoolean("Run213")){
                int side=OwnLightSite.branch(r);
                if(side!=0){own.putBoolean("Run213",true);own.putInt("Branch213",side);save(p,own);
                    cue(p,"Through a gap in the wall, someone in the other branch. A torch. Walking level with you.");}
                continue;
            }
            if(!own.getBoolean("Turned213")){
                if(r.z<OwnLightSite.HALFWAY){turn(p,b,own,r);continue;}
                match(p,b,own,r,trail);
                continue;
            }
            walkBack(l,p,b,own,r);
            if(!own.getBoolean("Followed213")&&r.z>own.getDouble("TurnZ213")+FOLLOWING&&!OwnLightSite.inLobby(r)){followed(l,p,b,own);continue;}
            if(!own.getBoolean("Followed213")&&OwnLightSite.inEnd(r))reach(l,p,b,own);
        }
    }
    /** Level with the reader across the wall: a beat late, then, after a while, a beat early. */
    static void match(ServerPlayer p,BlockPos b,CompoundTag own,Vec3 r,ArrayDeque<double[]> trail){
        var a=dbl(p);if(a==null)return;var list=new ArrayList<>(trail);int n=list.size();if(n<2)return;
        boolean early=r.z<EARLY_Z;double[] use;
        if(!early)use=list.get(Math.max(0,n-1-BEAT));
        else{double[] now=list.get(n-1),then=list.get(Math.max(0,n-1-BEAT));use=new double[]{now[0]+(now[0]-then[0]),now[1]+(now[1]-then[1]),now[2]+(now[2]-then[2]),now[3]+(now[3]-then[3]),now[4]};}
        double x=-use[0],z=Math.max(OwnLightSite.BRANCH_FAR+.5,Math.min(OwnLightSite.BRANCH_NEAR-.5,use[1]));
        x=Math.max(1.6+.5,Math.min(4.9,Math.abs(x)))*Math.signum(-own.getInt("Branch213"));
        a.noPhysics=true;a.setNoGravity(true);a.appearance(LiteraryActor.LOOKOUT_DOUBLE,use[4]>.5?1:0);a.gesture(2);
        if(!a.getMainHandItem().is(Items.TORCH))a.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(Items.TORCH));
        a.moveTo(b.getX()+x,b.getY(),b.getZ()+z,(float)-use[2],0);a.setYHeadRot((float)-use[3]);a.setYBodyRot((float)-use[2]);
        if(early&&!own.getBoolean("Early213")){own.putBoolean("Early213",true);save(p,own);cue(p,"It moves before you do.");}
    }
    /** Halfway, it stops matching, and comes through a gap behind them into their branch. */
    static void turn(ServerPlayer p,BlockPos b,CompoundTag own,Vec3 r){
        own.putBoolean("Turned213",true);own.putDouble("TurnZ213",r.z);int behind=OwnLightSite.GAPS[0];for(int g:OwnLightSite.GAPS)if(g>r.z+1)behind=g;
        var a=dbl(p);if(a!=null){a.moveTo(b.getX()+.5,b.getY(),b.getZ()+behind+.5,0,0);a.gesture(2);}
        own.putInt("Gap213",behind);save(p,own);
        cue(p,"It stops. It is not level with you any more. It is behind you, in your branch, walking back the way you came.");
        heard(p,p.position(),DarkHallRegistry.STEPS.get(),.8F,.9F);
    }
    /** Back down the reader's branch, taking their torches down one at a time, then out through the way in. */
    static void walkBack(ServerLevel l,ServerPlayer p,BlockPos b,CompoundTag own,Vec3 r){
        if(!hasDouble(p))return;var a=dbl(p);if(a==null)return;
        var at=a.position().subtract(b.getX(),b.getY(),b.getZ());int side=own.getInt("Branch213");
        // its way: across to the reader's side, then down it to the lobby, then to the way in
        Vec3 to=Math.abs(at.x-(side*3+.5))>.4&&at.z<OwnLightSite.BRANCH_NEAR?new Vec3(side*3+.5,0,at.z):at.z<OwnLightSite.LOBBY_Z?new Vec3(side*3+.5,0,OwnLightSite.LOBBY_Z+.5):OwnLightSite.ENTRY;
        var step=to.subtract(at);double d=Math.hypot(step.x,step.z);
        if(d<.3&&to==OwnLightSite.ENTRY){drop(p);heard(p,Vec3.atCenterOf(b),DarkHallRegistry.SLAM.get(),.9F,.8F);cue(p,"Far back, the way in shuts.");own.putBoolean("Gone213",true);save(p,own);return;}
        double m=Math.min(BACK,d);var next=at.add(step.x/d*m,0,step.z/d*m);float yaw=(float)(Math.atan2(-step.x,step.z)*180/Math.PI);
        a.moveTo(b.getX()+next.x,b.getY(),b.getZ()+next.z,yaw,0);a.setYHeadRot(yaw);a.setYBodyRot(yaw);
        if(l.getGameTime()%8==0)heard(p,a.position(),DarkHallRegistry.STEPS.get(),.6F,1F);
        // its own torches across the wall go out with it, and the reader's come down as it passes them
        for(String key:new String[]{"Torches213","Mirrored213"}){var list=own.getList(key,Tag.TAG_LONG);boolean changed=false;
            for(int i=list.size()-1;i>=0;i--){var t=BlockPos.of(((LongTag)list.get(i)).getAsLong());
                if(key.equals("Mirrored213")||Math.hypot(t.getX()+.5-next.x,t.getZ()+.5-next.z)<TAKE){var cell=b.offset(t);
                    if(lights(l.getBlockState(cell))){l.setBlock(cell,Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL);heard(p,Vec3.atCenterOf(cell),DarkHallRegistry.RUMBLE.get(),1F,.7F);if(key.equals("Torches213"))own.putInt("Taken213",own.getInt("Taken213")+1);}
                    list.remove(i);changed=true;}}
            if(changed){own.put(key,list);save(p,own);}}
    }
    /** Following it back: every torch gone at once, the House rumbles, and the reader comes to at the fork. */
    static void followed(ServerLevel l,ServerPlayer p,BlockPos b,CompoundTag own){
        own.putBoolean("Followed213",true);
        for(String key:new String[]{"Torches213","Mirrored213"}){for(var t:own.getList(key,Tag.TAG_LONG)){var cell=b.offset(BlockPos.of(((LongTag)t).getAsLong()));if(lights(l.getBlockState(cell))){l.setBlock(cell,Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL);if(key.equals("Torches213"))own.putInt("Taken213",own.getInt("Taken213")+1);}}own.remove(key);}
        drop(p);heard(p,p.position(),DarkHallRegistry.RUMBLE.get(),1.4F,.6F);heard(p,Vec3.atCenterOf(b),DarkHallRegistry.SLAM.get(),1F,.7F);
        HousePackets.send(p,new HouseFadePayload(2,30,30));p.addEffect(new MobEffectInstance(MobEffects.DARKNESS,120,0,false,false,false));
        p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-2.5,180,0);p.connection.resetPosition();
        home(own,"Thank you for the light.");save(p,own);
        cue(p,"Every torch is gone. The House rumbles. You come to at the fork with no light but what is in your hands.");
    }
    /** Going on without looking back: the torches, set in the far wall in the order they were placed, burning. */
    static void reach(ServerLevel l,ServerPlayer p,BlockPos b,CompoundTag own){
        // whatever it had not yet taken down is taken now, and set back here
        for(String key:new String[]{"Torches213","Mirrored213"}){for(var t:own.getList(key,Tag.TAG_LONG)){var cell=b.offset(BlockPos.of(((LongTag)t).getAsLong()));if(lights(l.getBlockState(cell)))l.setBlock(cell,Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL);}own.remove(key);}
        drop(p);
        int n=OwnLightSite.setBack(l,b,own.getInt("Placed213"));own.putInt("SetBack213",n);
        home(own,"Not yet.");LiteraryVignettes.ready(p,PLACE,own,"let_it_take_them");save(p,own);
        cue(p,n==0?"In the far room, nothing. You brought no light of your own, and nothing has been set back.":"In the far wall, your torches, every one, burning in the order you set them down.");
    }
    static void home(CompoundTag own,String note){own.putString("Note213",note);own.putBoolean("Home213",true);OwnLightLate.begin(own);}

    // ------------------------------------------------------------------------------------------------ it goes home first
    /** At their bed, after: slept in, a chest turned round, the lights out, a note on the pillow. */
    @SubscribeEvent public static void homeTick(ServerTickEvent.Post e){
        var s=e.getServer();if(s.getTickCount()%40!=0)return;
        for(var p:s.getPlayerList().getPlayers()){var own=own(p);if(!own.getBoolean("Home213"))continue;
            var bed=p.getRespawnPosition();if(bed==null||!p.level().dimension().equals(p.getRespawnDimension())||!p.blockPosition().closerThan(bed,12))continue;
            visit(p.serverLevel(),p,bed,own.getString("Note213"));own.remove("Home213");own.putString("Left213",own.getString("Note213"));save(p,own);}
    }
    public static void visit(ServerLevel l,ServerPlayer p,BlockPos bed,String note){
        // a chest nearby, every slot turned the other way round: nothing taken
        for(var at:BlockPos.betweenClosed(bed.offset(-8,-2,-8),bed.offset(8,3,8)))if(l.getBlockEntity(at) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity c){
            int size=c.getContainerSize();var items=new ArrayList<ItemStack>();for(int i=0;i<size;i++)items.add(c.getItem(i));for(int i=0;i<size;i++)c.setItem(i,items.get(size-1-i));c.setChanged();break;}
        // the lights left burning, put out
        for(var at:BlockPos.betweenClosed(bed.offset(-10,-3,-10),bed.offset(10,4,10))){var st=l.getBlockState(at);
            if((st.getBlock() instanceof CandleBlock||st.getBlock() instanceof CampfireBlock||st.getBlock() instanceof AbstractCandleBlock)&&st.hasProperty(BlockStateProperties.LIT)&&st.getValue(BlockStateProperties.LIT))l.setBlock(at.immutable(),st.setValue(BlockStateProperties.LIT,false),Block.UPDATE_ALL);}
        var page=OwnLightTexts.note(p.getGameProfile().getName(),note);var item=new ItemEntity(l,bed.getX()+.5,bed.getY()+.7,bed.getZ()+.5,page);item.setDeltaMovement(Vec3.ZERO);item.setPickUpDelay(10);l.addFreshEntity(item);
        cue(p,"Your bed has been slept in. There is a note on the pillow.");
    }
    /** The House lets lights be set down only in its ordinary halls; here, a reader may set their own. */
    public static boolean allowsPlacing(ServerPlayer p,BlockPos pos,BlockState placed){if(!inside(p)||!lights(placed))return false;var b=base(p);return b!=null&&OwnLightSite.inside(Vec3.atCenterOf(pos.subtract(b)));}
    public static boolean locked(ServerPlayer p){return false;}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){TRAIL.remove(e.getEntity().getUUID());}
    public static void clearAll(){TRAIL.clear();}
}
