package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.opening.CompanionOrders;
import io.github.knaitoe.theoldesthouse.network.CaverCrawlPayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.*;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Inspired by Ted the Caver: the work earns a passage, and the passage earns a return. */
public final class CaverVignette {
    public static final String ID="ted_caver",BOOK_OWNER="CaverJournalOwner";
    public static final int STROKES=24,STROKE_INTERVAL=25,CRAWL_TICKS=40,DEEP_TICKS=80;
    private record Crawl(ServerPlayer player,@Nullable Pose forced,Pose displayed){}
    private static final Map<UUID,Crawl> CRAWLING=new HashMap<>();
    private CaverVignette(){}
    public static CompoundTag personal(LabyrinthData d,UUID id){return d.state(ID).getCompound("Players").getCompound(id.toString()).copy();}
    private static void save(LabyrinthData d,UUID id,CompoundTag own){var all=d.state(ID);var people=all.getCompound("Players");people.put(id.toString(),own);all.put("Players",people);d.setState(ID,all);}
    public static boolean canDeal(LabyrinthData d,UUID id){return !WitnessAccount.has(d,id,WitnessAccount.Story.TED_CAVER)&&!personal(d,id).getBoolean("Escaped");}
    public static @Nullable BlockPos base(MinecraftServer s){return IndianLakeRooms.base(s,LabyrinthPlace.TED_CAVER);}
    public static boolean inside(ServerPlayer p){return p.gameMode.getGameModeForPlayer()!=GameType.SPECTATOR&&IndianLakeRooms.inside(p,LabyrinthPlace.TED_CAVER);}
    private static Vec3 rel(ServerPlayer p,BlockPos b){return p.position().subtract(b.getX(),b.getY(),b.getZ());}
    private static boolean reach(ServerPlayer p,BlockPos at){return p.distanceToSqr(at.getCenter())<=25;}
    public static void onArrive(ServerPlayer p,LabyrinthPlace place){if(place==LabyrinthPlace.TED_CAVER)enter(p);}
    public static void enter(ServerPlayer p){
        if(!inside(p))return;var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());
        if(!own.getBoolean("Active")){
            if(rel(p,base(p.server)).z> -8.5&&!own.getBoolean("Escaped")){own.putBoolean("Pursuit",false);own.putInt("ReturnCrawl",0);own.putInt("DeepTicks",0);}
            own.putBoolean("Active",true);own.putBoolean("Arrived",true);own.putLong("LastTick",-1);save(d,p.getUUID(),own);
            p.displayClientMessage(Component.literal("A field notebook lies by the supplies. The rope leads down."),false);
        }
        IndianLakeRooms.keepLoaded(p.serverLevel(),base(p.server),LabyrinthPlace.TED_CAVER);
    }
    /** A stroke is a reachable interaction with a real pickaxe, separated by working time. */
    public static boolean chip(ServerPlayer p,BlockPos at){
        if(!inside(p)||!reach(p,at)||!at.equals(base(p.server).offset(CaverCave.APERTURE)))return false;
        var d=LabyrinthData.get(p.server);var state=d.state(ID);int work=state.getInt("Work");
        if(work>=STROKES)return true;
        if(!p.getMainHandItem().is(ItemTags.PICKAXES)){
            p.displayClientMessage(Component.literal("The crack needs a pickaxe. There is one in the camp barrel."),true);return true;
        }
        long now=p.serverLevel().getGameTime();if(state.contains("NextStroke")&&now<state.getLong("NextStroke"))return true;
        state.putInt("Work",++work);state.putLong("NextStroke",now+STROKE_INTERVAL);d.setState(ID,state);
        var own=personal(d,p.getUUID());own.putBoolean("Worked",true);save(d,p.getUUID(),own);updateJournal(p);
        p.serverLevel().sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,Blocks.STONE.defaultBlockState()),at.getX()+.5,at.getY()+.4,at.getZ()+1,8,.15,.18,.05,.02);
        p.serverLevel().playSound(null,at,SoundEvents.STONE_BREAK,SoundSource.BLOCKS,.55F,.7F);
        if(work==8||work==16)privateSound(p,SoundEvents.AMBIENT_CAVE.value(),Vec3.atCenterOf(at.north(9)),.45F,.65F);
        if(work==STROKES){CaverCave.aperture(p.serverLevel(),base(p.server),true);
            p.displayClientMessage(Component.literal("The opening will take your shoulders. Crouch at its mouth to crawl through."),false);
        }else if(work%6==0)p.displayClientMessage(Component.literal("A little more stone gives way. The draught is colder."),true);
        return true;
    }
    public static boolean examine(ServerPlayer p,BlockPos at){
        if(!inside(p)||!reach(p,at))return false;var b=base(p.server);var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());
        if(at.equals(b.offset(CaverCave.MARK))){
            if(!own.getBoolean("Squeezed")){p.displayClientMessage(Component.literal("You haven't followed the passage here."),true);return true;}
            if(!own.getBoolean("MarkRead")){
                own.putBoolean("MarkRead",true);save(d,p.getUUID(),own);updateJournal(p);
                p.displayClientMessage(Component.literal("The cuts go beneath the mineral crust. One looks almost like a shoulder."),false);
                privateSound(p,SoundEvents.STONE_HIT,Vec3.atCenterOf(b.offset(5,-3,-48)),.65F,.55F);
            }return true;
        }
        var local=at.subtract(b);
        if(local.getZ()==-43&&local.getX()>=4&&local.getX()<=6&&local.getY()>=-3&&local.getY()<=-2){
            if(!own.getBoolean("MarkRead")){p.displayClientMessage(Component.literal("Marks on the opposite wall catch what little light there is."),true);return true;}
            if(!own.getBoolean("StoneSeen")){
                own.putBoolean("StoneSeen",true);save(d,p.getUUID(),own);var state=d.state(ID);
                if(!state.getBoolean("StoneMoved")){state.putBoolean("StoneMoved",true);d.setState(ID,state);CaverCave.stone(p.serverLevel(),b,true);}
                updateJournal(p);privateSound(p,SoundEvents.PISTON_CONTRACT,Vec3.atCenterOf(at),.9F,.45F);
                p.displayClientMessage(Component.literal("The stone rolls inward. There is another passage behind it."),false);
            }return true;
        }return false;
    }
    private static boolean squeeze(Vec3 r){return r.x>-.15&&r.x<1.15&&r.z< -21&&r.z> -35.9&&r.y>=-3.2&&r.y< -2.1;}
    private static void crawl(ServerPlayer p,boolean active){
        var old=CRAWLING.get(p.getUUID());
        if(active){
            if(old!=null&&old.player()!=p){restore(old);CRAWLING.remove(p.getUUID());old=null;}
            if(old==null){CRAWLING.put(p.getUUID(),new Crawl(p,p.getForcedPose(),p.getPose()));p.setForcedPose(Pose.SWIMMING);p.setPose(Pose.SWIMMING);p.refreshDimensions();sendCrawl(p,true);}
        }else if(old!=null){CRAWLING.remove(p.getUUID());restore(old);}
    }
    private static void restore(Crawl old){
        var p=old.player();sendCrawl(p,false);if(p.getForcedPose()!=Pose.SWIMMING)return;
        p.setForcedPose(old.forced());p.setPose(old.forced()==null?old.displayed():old.forced());p.refreshDimensions();
    }
    public static boolean crawling(ServerPlayer p){var own=CRAWLING.get(p.getUUID());return own!=null&&own.player()==p;}
    public static void depart(ServerPlayer p){crawl(p,false);var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());
        if(own.getBoolean("Active")){own.putBoolean("Active",false);save(d,p.getUUID(),own);}}
    public static void clearAll(){for(var old:CRAWLING.values())restore(old);CRAWLING.clear();}
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p)depart(p);}
    public static void onDeath(LivingDeathEvent e){if(e.getEntity() instanceof ServerPlayer p){
        var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());
        if(own.getBoolean("Active")&&!own.getBoolean("Escaped")){own.putBoolean("Pursuit",false);own.putInt("ReturnCrawl",0);own.putInt("DeepTicks",0);save(d,p.getUUID(),own);}
        depart(p);
    }}
    private static void sendCrawl(ServerPlayer p,boolean active){var b=base(p.server);if(b!=null)HousePackets.send(p,new CaverCrawlPayload(active,b.getX(),b.getY(),b.getZ()));}
    public static void onServerTick(ServerTickEvent.Post e){
        for(var l:e.getServer().getAllLevels())for(var p:List.copyOf(l.players()))playerTick(p);
    }
    public static void playerTick(ServerPlayer p){
        if(!inside(p)){if(crawling(p)||personal(LabyrinthData.get(p.server),p.getUUID()).getBoolean("Active"))depart(p);echo(p);return;}
        enter(p);var b=base(p.server);var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());var r=rel(p,b);
        long now=p.serverLevel().getGameTime();if(own.getLong("LastTick")==now)return;own.putLong("LastTick",now);
        boolean opened=d.state(ID).getInt("Work")>=STROKES;
        boolean tight=opened&&squeeze(r)&&(p.isShiftKeyDown()||crawling(p)||r.z<=-23&&r.z>=-34);
        crawl(p,tight);
        if(tight&&now%20==0)sendCrawl(p,true);
        if(tight&&r.z<=-23&&r.z>=-34){
            String counter=own.getBoolean("Pursuit")?"ReturnCrawl":"IngressCrawl";
            own.putInt(counter,Math.min(CRAWL_TICKS,own.getInt(counter)+1));
        }
        boolean changed=false;
        if(own.getInt("IngressCrawl")>=CRAWL_TICKS&&r.z< -34.1&&!own.getBoolean("Squeezed")){
            own.putBoolean("Squeezed",true);changed=true;p.displayClientMessage(Component.literal("The chamber is larger than the draught suggested. There are cuts in the left wall."),false);
        }
        if(own.getBoolean("StoneSeen")&&!own.getBoolean("Pursuit")&&r.z< -50&&r.z> -58&&r.x>1&&r.x<8&&r.y>=-3.2&&r.y<0){
            own.putInt("DeepTicks",own.getInt("DeepTicks")+1);
            if(own.getInt("DeepTicks")>=DEEP_TICKS){own.putBoolean("Pursuit",true);own.putInt("ReturnCrawl",0);changed=true;
                privateSound(p,SoundEvents.AMBIENT_CAVE.value(),p.position().add(0,1,-5),1F,.5F);
                p.displayClientMessage(Component.literal("Air moves past your face. Then the rope draws tight behind you."),false);
            }
        }
        if(own.getBoolean("Pursuit")&&!own.getBoolean("Escaped")){
            int clock=own.getInt("PursuitTicks")+1;own.putInt("PursuitTicks",clock);
            if(r.z< -9&&clock%160==0){privateSound(p,SoundEvents.STONE_BREAK,p.position().add(0,.5,-6),.9F,.5F);
                p.push(0,0,-.09);p.connection.send(new ClientboundSetEntityMotionPacket(p));}
            if(own.getInt("ReturnCrawl")>=CRAWL_TICKS&&r.z> -8.5&&r.y>=-.2&&r.y<3&&Math.abs(r.x-.5)<3){
                own.putBoolean("Escaped",true);changed=true;crawl(p,false);
                save(d,p.getUUID(),own);WitnessAccount.resolve(p,WitnessAccount.Story.TED_CAVER,"retraced_the_squeeze");
                p.displayClientMessage(Component.literal("You are above the rope. The mouth of the cave is still where you left it."),false);
            }
        }
        save(d,p.getUUID(),own);if(changed)updateJournal(p);
    }
    /** A few later echoes, confined to play in the House. Never reopens or changes ending credit. */
    private static void echo(ServerPlayer p){
        if(!p.isAlive()||p.gameMode.getGameModeForPlayer()==GameType.SPECTATOR||!p.level().dimension().equals(HouseDimensions.INTERIOR)
                ||FinaleProgress.terminal(FinaleProgress.phase(p.server,p.getUUID())))return;
        var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());if(!own.getBoolean("Escaped")||own.getInt("EchoTicks")>=480)return;
        long now=p.serverLevel().getGameTime();if(own.getLong("LastEcho")==now)return;own.putLong("LastEcho",now);
        int clock=own.getInt("EchoTicks")+1;own.putInt("EchoTicks",clock);save(d,p.getUUID(),own);
        if(clock==240)privateSound(p,SoundEvents.STONE_HIT,p.position().add(0,-1,-4),.25F,.6F);
        if(clock==480){privateSound(p,SoundEvents.STONE_HIT,p.position().add(0,-1,3),.3F,.6F);updateJournal(p);}
    }
    private static void privateSound(ServerPlayer p,SoundEvent sound,Vec3 at,float volume,float pitch){
        p.connection.send(new ClientboundSoundPacket(Holder.direct(sound),SoundSource.AMBIENT,at.x,at.y,at.z,volume,pitch,p.getRandom().nextLong()));
    }
    public static boolean companionScene(ServerPlayer p){return inside(p)&&rel(p,base(p.server)).z< -8;}
    /** Refuse the shaft without changing native identity, owner, health, sit state, or saved order. */
    public static boolean companion(TamableAnimal pet,ServerPlayer p){
        if(!companionScene(p)||pet.level()!=p.level()||pet.isLeashed()||pet.isPassenger()||pet.isOrderedToSit()||CompanionOrders.order(pet)==CompanionOrders.Order.STAY
                ||!p.getUUID().equals(CompanionOrders.owner(pet)))return false;
        var b=base(p.server);pet.getNavigation().stop();var at=new Vec3(b.getX()+.5,b.getY(),b.getZ()-7.5);
        if(pet.getZ()<b.getZ()-8.2||pet.distanceToSqr(at)>25){pet.teleportTo(at.x,at.y,at.z);pet.setDeltaMovement(Vec3.ZERO);pet.resetFallDistance();}
        else if(pet.distanceToSqr(at)>2)pet.getNavigation().moveTo(at.x,at.y,at.z,.65);
        pet.getLookControl().setLookAt(b.getX()+.5,b.getY()-2,b.getZ()-11.5,30,30);return true;
    }
    public static boolean ownedJournal(ItemStack stack,UUID id){var custom=stack.get(DataComponents.CUSTOM_DATA);
        return custom!=null&&custom.copyTag().hasUUID(BOOK_OWNER)&&id.equals(custom.copyTag().getUUID(BOOK_OWNER));}
    public static ItemStack journal(LabyrinthData d,UUID id){
        var own=personal(d,id);var pages=new ArrayList<Component>();
        String[] entries={
            "FIELD NOTEBOOK\n\nR. / Miles\n\nCheck lamps. Leave the line tied. Three sandwiches, two gloves. Miles says a spare glove is a strange thing to count.",
            "FIRST SURVEY\n\nDown the ladder, follow the draught. Work the cracked block with a pickaxe, one blow at a time. Crouch at the opening to crawl. Keep the line tied.",
            "WORKING DAY\n\nI wait for the grit after each blow. A hand would fit behind the crack. The air comes out in bursts.\n\nMiles has stopped counting.",
            "THROUGH\n\nThe squeeze goes farther than the light. I kept my arms in front of me. There was space at the end to stand. I heard my own clothing stop scraping before I stopped moving.",
            "THE CUTS\n\nA shape lies under the crust. I tried drawing it. Each version looks like a different part of a person.\n\nA smooth stone sits opposite. Air comes from behind it.",
            "ANOTHER PASSAGE\n\nThe smooth stone moved away from my hand. Behind it, the line can go on. I have already made the hole large enough to get back.\n\nThat seemed like a good reason to keep going.",
            "LOW CHAMBER\n\nThe rope tightened. Nothing was tied to its end. Stone scraped behind me.\n\nBack through the squeeze. Up the ladder beside the line. Get above the drop.",
            "OUT\n\nI crawled back. The pulls came from behind me. Above the ladder I could stand again.\n\nI did not see Miles at the landing. I cannot remember when I stopped expecting him to answer.",
            "LATER\n\nA chisel sounds behind a shut door. Grit lies in this book. I have packed twice. It is wrong to leave the line there.\n\n[The next date. Nothing follows it.]"};
        int last=Math.max(own.getInt("JournalStage"),journalStage(own));
        for(int i=0;i<=last;i++)pages.add(HouseWriting.page(HouseWriting.WritingStyle.WILL,entries[i]));
        var book=VignetteYields.mark(HouseWriting.book("Field notebook","R.",pages),ID);
        CustomData.update(DataComponents.CUSTOM_DATA,book,tag->tag.putUUID(BOOK_OWNER,id));return book;
    }
    private static int journalStage(CompoundTag own){return own.getInt("EchoTicks")>=480?8:own.getBoolean("Escaped")?7:own.getBoolean("Pursuit")?6:own.getBoolean("StoneSeen")?5:own.getBoolean("MarkRead")?4:own.getBoolean("Squeezed")?3:own.getBoolean("Worked")?2:1;}
    private static void updateJournal(ServerPlayer p){var d=LabyrinthData.get(p.server);var own=personal(d,p.getUUID());own.putInt("JournalStage",Math.max(own.getInt("JournalStage"),journalStage(own)));save(d,p.getUUID(),own);var book=journal(d,p.getUUID());
        for(int i=0;i<p.getInventory().getContainerSize();i++)if(ownedJournal(p.getInventory().getItem(i),p.getUUID()))p.getInventory().setItem(i,book.copy());
        if(ownedJournal(p.containerMenu.getCarried(),p.getUUID()))p.containerMenu.setCarried(book.copy());p.inventoryMenu.broadcastChanges();}
    public static final class JournalMenu extends LecternMenu {
        private final ServerPlayer reader;private final BlockPos at;private final SimpleContainer pages;
        public JournalMenu(int id,ServerPlayer reader,BlockPos at){this(id,reader,at,new SimpleContainer(1));}
        private JournalMenu(int id,ServerPlayer reader,BlockPos at,SimpleContainer pages){super(id,pages,new SimpleContainerData(1));this.reader=reader;this.at=at.immutable();this.pages=pages;pages.setItem(0,journal(LabyrinthData.get(reader.server),reader.getUUID()));}
        public ItemStack book(){return pages.getItem(0).copy();}
        @Override public boolean stillValid(net.minecraft.world.entity.player.Player p){return p==reader&&inside(reader)&&reach(reader,at)&&reader.level().getBlockState(at).is(Blocks.LECTERN);}
        @Override public boolean clickMenuButton(net.minecraft.world.entity.player.Player p,int button){
            if(!stillValid(p))return false;int count=book().get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size();
            if(button==3){var d=LabyrinthData.get(reader.server);var own=personal(d,reader.getUUID());if(own.getBoolean("JournalTaken"))return false;
                own.putBoolean("JournalTaken",true);save(d,reader.getUUID(),own);var book=journal(d,reader.getUUID());
                if(!reader.getInventory().add(book))reader.drop(book,false);reader.inventoryMenu.broadcastChanges();return true;}
            if(button>=100){if(button-100>=count)return false;}else if(button==1){if(getPage()<=0)return false;}else if(button==2){if(getPage()>=count-1)return false;}else return false;
            return super.clickMenuButton(p,button);
        }
    }
    public static void onRightClick(PlayerInteractEvent.RightClickBlock e){
        if(e.getEntity() instanceof ServerPlayer observer&&observer.gameMode.getGameModeForPlayer()==GameType.SPECTATOR
                &&IndianLakeRooms.inside(observer,LabyrinthPlace.TED_CAVER)&&e.getPos().equals(base(observer.server).offset(CaverCave.JOURNAL))){e.setCanceled(true);return;}
        if(!(e.getEntity() instanceof ServerPlayer p)||!inside(p)||!reach(p,e.getPos()))return;
        var b=base(p.server);boolean journal=e.getPos().equals(b.offset(CaverCave.JOURNAL));
        if(e.getHand()!=InteractionHand.MAIN_HAND)return;
        if(journal){e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);
            p.openMenu(new SimpleMenuProvider((id,inventory,reader)->new JournalMenu(id,p,e.getPos()),Component.literal("Field notebook")));}
        else if(chip(p,e.getPos())||examine(p,e.getPos())){e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);}
    }
    public static void onLeftClick(PlayerInteractEvent.LeftClickBlock e){
        if(e.getAction()==PlayerInteractEvent.LeftClickBlock.Action.START&&e.getEntity() instanceof ServerPlayer p&&chip(p,e.getPos()))e.setCanceled(true);
    }
}
