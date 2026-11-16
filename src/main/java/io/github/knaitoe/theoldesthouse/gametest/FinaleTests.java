package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FinaleTests {
    @GameTest(template="empty",batch="finale") public static void actualCreatureRejectsCopiesAndOneOriginalWoundStartsCollapse(GameTestHelper helper){
        var player=helper.makeMockServerPlayerInLevel();var server=player.server;var data=LabyrinthData.get(server);
        var previous=data.state(FinaleProgress.STATE);MinotaurEntity creature=FinaleRegistry.MINOTAUR.get().create(helper.getLevel());
        try{
            ItemStack sword=new ItemStack(Items.IRON_SWORD);UUID original=WeaponHistory.stamp(sword);CompoundTag record=new CompoundTag();
            record.putUUID("Weapon",original);record.putString("Phase",FinaleProgress.Phase.FIGHT.name());FinaleProgress.save(server,player.getUUID(),record);
            creature.owner(player.getUUID());creature.moveTo(player.position().add(0,0,4));
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,sword);
            helper.assertTrue(!creature.hurt(player.damageSources().playerAttack(player),8),"even the original cannot wound outside the shield opening");
            creature.stagger();ItemStack copy=sword.copy();WeaponHistory.markCopy(copy);player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,copy);
            helper.assertTrue(!creature.hurt(player.damageSources().playerAttack(player),8)&&creature.motion()==MinotaurEntity.STUNNED,"a House copy passes through a genuinely stunned creature");
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,sword);
            helper.assertTrue(creature.hurt(player.damageSources().playerAttack(player),8)&&creature.motion()==MinotaurEntity.WOUNDED,"one original hit starts the wounded crawl");
            helper.assertTrue(creature.getHealth()==creature.getMaxHealth()&&FinaleProgress.phase(server,player.getUUID())==FinaleProgress.Phase.COLLAPSE,"the encounter changes saved phase rather than draining conventional health");
            for(var display:helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.Display.TextDisplay.class,creature.getBoundingBox().inflate(12))){
                if(!display.getTags().contains("HouseFinaleWords"))continue;
                CompoundTag text=new CompoundTag();display.saveWithoutId(text);
                helper.assertTrue(text.getString("text").contains("edge passes through")||text.getString("text").contains("walls begin"),"the native dialogue display retains authored text");display.discard();
            }
        }finally{data.setState(FinaleProgress.STATE,previous);if(creature!=null)creature.discard();if(server.getPlayerList().getPlayers().contains(player))server.getPlayerList().remove(player);}
        helper.succeed();
    }
    @GameTest(template="empty") public static void loadingAChargeNeverResumesAnUnwarnedDash(GameTestHelper helper){
        var creature=FinaleRegistry.MINOTAUR.get().create(helper.getLevel());UUID owner=UUID.randomUUID();creature.owner(owner);
        CompoundTag saved=new CompoundTag();creature.saveWithoutId(saved);saved.putInt("FinaleMotion",MinotaurEntity.CHARGING);saved.putInt("FinaleRemaining",12);
        var restored=FinaleRegistry.MINOTAUR.get().create(helper.getLevel());restored.load(saved);
        helper.assertTrue(owner.equals(restored.owner())&&restored.motion()==MinotaurEntity.WATCHING,"reconnect preserves the encounter owner and restores the charge's warning");creature.discard();restored.discard();helper.succeed();
    }
    @GameTest(template="empty") public static void weaponIdentitySurvivesArchiveAndCopiesCannotWound(GameTestHelper helper){
        var registries=helper.getLevel().registryAccess();ItemStack sword=new ItemStack(Items.IRON_SWORD);sword.setDamageValue(73);
        UUID original=WeaponHistory.stamp(sword);MotherCollection mother=new MotherCollection();UUID owner=UUID.randomUUID();
        var entry=mother.keepItem(sword,registries,owner,0);var loaded=MotherCollection.load(mother.save(new CompoundTag(),registries),registries);
        ItemStack recovered=loaded.claimItem(owner,entry.id,registries);
        helper.assertTrue(WeaponHistory.wounds(recovered,original)&&recovered.getDamageValue()==73,"the real shelf object retains its identity and wear");
        ItemStack duplicate=recovered.copy();WeaponHistory.markCopy(duplicate);
        helper.assertTrue(!WeaponHistory.wounds(duplicate,original)&&WeaponHistory.stamp(duplicate)==null,"a copied edge cannot become original by being used");
        ItemStack other=new ItemStack(Items.IRON_SWORD);WeaponHistory.stamp(other);
        helper.assertTrue(!WeaponHistory.wounds(other,original),"an identical ordinary sword is still another physical object");helper.succeed();
    }
    @GameTest(template="empty") public static void defeatSealsEvenOrdinaryResourcesAcrossRestart(GameTestHelper helper){
        var registries=helper.getLevel().registryAccess();MotherCollection mother=new MotherCollection();UUID player=UUID.randomUUID();
        mother.keepFinaleItem(new ItemStack(Items.COBBLESTONE,48),registries,player,0);
        mother.keepFinaleItem(new ItemStack(Items.DIAMOND_SWORD),registries,player,0);
        MotherCollection loaded=MotherCollection.load(mother.save(new CompoundTag(),registries),registries);
        helper.assertTrue(loaded.all().size()==2,"failure retains resources as well as keepsakes, without world drops");
        for(var entry:loaded.all())helper.assertTrue(entry.sealed&&loaded.claimItem(player,entry.id,registries).isEmpty(),"terminal losses are permanently kept");
        helper.assertTrue(loaded.all().stream().anyMatch(e->e.item(registries).is(Items.COBBLESTONE)&&e.item(registries).getCount()==48),"ordinary inventory counts survive");helper.succeed();
    }
    @GameTest(template="empty") public static void guidanceRemembersTakingShelvesAndAcceptsKindness(GameTestHelper helper){
        var registries=helper.getLevel().registryAccess();MotherCollection mother=new MotherCollection();UUID player=UUID.randomUUID();
        helper.assertTrue(mother.canGuide(player),"leaving the shelves alone qualifies for the rescue");
        var entry=mother.keepItem(new ItemStack(Items.IRON_SWORD),registries,player,0);mother.claimItem(player,entry.id,registries);
        MotherCollection loaded=MotherCollection.load(mother.save(new CompoundTag(),registries),registries);
        helper.assertTrue(!loaded.canGuide(player),"a restart cannot erase taking from her shelves");
        ItemStack gift=new ItemStack(Items.COMPASS);gift.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("My way home"));loaded.offer(player,gift,registries,0);
        helper.assertTrue(loaded.canGuide(player),"a wanted, loved offering restores the possibility of help");helper.succeed();
    }
    @GameTest(template="empty") public static void optionalDiscoveryRequiresDepthAndPersonalExploration(GameTestHelper helper){
        LabyrinthData data=new LabyrinthData();UUID player=UUID.randomUUID(),other=UUID.randomUUID();
        for(int i=0;i<12;i++)data.pushReturn(player,new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,Vec3.ZERO,0));
        helper.assertTrue(!FinaleProgress.eligible(data,player),"depth alone never forces the climax");
        data.visit(player,LabyrinthPlace.HARRIGAN);data.visit(player,LabyrinthPlace.HIDE_AND_CLAP);
        helper.assertTrue(!FinaleProgress.eligible(data,player),"rooms only visited no longer open the way (0.4.384)");
        seal(data,player);
        helper.assertTrue(FinaleProgress.eligible(data,player)&&!FinaleProgress.eligible(data,other),"discovery belongs to each explorer");helper.succeed();
    }
    /** Three trial marks and four stories heard to their end: the staircase's seal (0.4.384). */
    static void seal(LabyrinthData data,UUID player){
        for(var place:List.of(LabyrinthPlace.FLOODED_PASSAGE,LabyrinthPlace.COLD_STORE,LabyrinthPlace.MIRROR_HALL))TrialMarks.mark(data,player,place);
        for(var story:List.of(WitnessAccount.Story.HARRIGAN,WitnessAccount.Story.MODEL_HOME,WitnessAccount.Story.DROWNED_TOWN,WitnessAccount.Story.CLAP))WitnessAccount.resolve(data,player,story,"resolved");
    }
    @GameTest(template="empty") public static void threeTrialMarksAndFourStoriesSealTheStaircase(GameTestHelper h){
        LabyrinthData data=new LabyrinthData();UUID reader=UUID.randomUUID(),every=UUID.randomUUID();int kinds=0;
        for(var place:LabyrinthPlace.values()){var kind=TrialMarks.kind(place);
            h.assertTrue((kind!=null)==LabyrinthPacing.physicalTrial(place),place.id()+" is a kind of trial exactly when it is a physical trial");
            if(kind==null)continue;
            h.assertTrue(!HazardVariety.sibling(kind)&&!TrialMarks.name(kind).equals("a room the House has not named"),place.id()+" counts as a named original");
            if(TrialMarks.mark(data,every,place))kinds++;}
        h.assertTrue(kinds==TrialMarks.count(data,every)&&kinds>=TrialMarks.SEALS,"every kind is its own mark ("+kinds+")");
        for(String page:TrialMarks.pages(data,every))h.assertTrue(page.length()<=180,"every page of the marks fits a book page: "+page);
        h.assertTrue(!TrialMarks.mark(data,reader,LabyrinthPlace.STRAIGHT_HALL)&&!TrialMarks.mark(data,reader,LabyrinthPlace.HARRIGAN)&&!TrialMarks.mark(data,reader,null)
                &&TrialMarks.count(data,reader)==0,"halls and stories earn no mark");
        h.assertTrue(TrialMarks.pages(data,reader).isEmpty(),"the account says nothing of marks before the first");
        h.assertTrue(TrialMarks.mark(data,reader,LabyrinthPlace.BENT_CISTERN)&&!TrialMarks.mark(data,reader,LabyrinthPlace.FLOODED_PASSAGE)
                &&TrialMarks.marks(data,reader).equals(List.of("flooded_passage")),"a sibling earns the mark of the room it varies, once");
        h.assertTrue(String.join(" ",TrialMarks.pages(data,reader)).contains("fewer than three marks"),"one mark: the account says how many the stair wants");
        TrialMarks.mark(data,reader,LabyrinthPlace.COLD_STORE);TrialMarks.mark(data,reader,LabyrinthPlace.YARD_STORM_FIELD);
        h.assertTrue(TrialMarks.marks(data,reader).equals(List.of("flooded_passage","cold_store","yard_storm_field"))&&!TrialMarks.sealed(data,reader),"three marks in the order earned, and not yet a seal");
        h.assertTrue(String.join(" ",TrialMarks.pages(data,reader)).contains("still waits on the rooms"),"the account says the stories are what is missing");
        for(var story:List.of(WitnessAccount.Story.HARRIGAN,WitnessAccount.Story.MODEL_HOME,WitnessAccount.Story.DROWNED_TOWN))WitnessAccount.resolve(data,reader,story,"resolved");
        data.visit(reader,LabyrinthPlace.FLOORBOARDS);
        h.assertTrue(!TrialMarks.sealed(data,reader),"three stories and a fourth only visited are not a seal");
        WitnessAccount.resolve(data,reader,WitnessAccount.Story.CLAP,"won");
        var pages=TrialMarks.pages(data,reader);
        h.assertTrue(TrialMarks.sealed(data,reader)&&!TrialMarks.told(data,reader)&&pages.getFirst().startsWith("THE MARKS")&&!String.join(" ",pages).contains("wait"),"the fourth story makes the seal, and the account stops asking");
        for(int i=0;i<30;i++)for(UUID id:List.of(reader,every))data.pushReturn(id,new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,Vec3.ZERO,0));
        h.assertTrue(FinaleController.canOffer(data,reader)&&LabyrinthDealer.finaleOffered(data,reader)&&!LabyrinthDealer.finaleOffered(data,every),"sealed and deep, the stair is offered; every mark without stories is not enough");
        h.succeed();
    }
    @GameTest(template="empty") public static void endingSurvivesWorldSaveAndDoesNotFinishVignettes(GameTestHelper helper){
        LabyrinthData data=new LabyrinthData();UUID player=UUID.randomUUID();CompoundTag record=new CompoundTag(),world=new CompoundTag();
        record.putString("Phase",FinaleProgress.Phase.LOCKED_OUT.name());world.put(player.toString(),record);data.setState(FinaleProgress.STATE,world);
        var registry=helper.getLevel().registryAccess();LabyrinthData loaded=LabyrinthData.FACTORY.deserializer().apply(data.save(new CompoundTag(),registry),registry);
        helper.assertTrue(FinaleProgress.terminal(FinaleProgress.phase(loaded.state(FinaleProgress.STATE).getCompound(player.toString()))),"player cloning cannot erase a world-owned ending");
        helper.assertTrue(loaded.completed().isEmpty(),"defeat does not complete or erase unfinished vignettes");helper.succeed();
    }
    @GameTest(template="empty") public static void collapseDisablesOriginButPreservesItsActualSite(GameTestHelper helper){
        var registry=helper.getLevel().registryAccess();HouseSavedData house=new HouseSavedData();BlockPos site=new BlockPos(40,70,-30);house.markSpawned(site);house.collapse();
        HouseSavedData loaded=HouseSavedData.load(house.save(new CompoundTag(),registry),registry);
        helper.assertTrue(loaded.isCollapsed()&&loaded.houseOrigin()==null&&loaded.housePosition().orElseThrow().equals(site),"all House controllers stand down while demolition can resume at the real manor");helper.succeed();
    }
    @GameTest(template="empty") public static void greatStaircaseHasPhysicalDepthAndNoRoomSlotOverlap(GameTestHelper helper){
        BlockPos origin=new BlockPos(100,70,100);var plan=FinaleArchitecture.plan(origin);Map<BlockPos,net.minecraft.world.level.block.state.BlockState> blocks=new HashMap<>();
        for(var p:plan){blocks.put(p.pos(),p.block());helper.assertTrue(p.pos().getY()>=-2032&&p.pos().getY()<2032,"geometry stays inside the dimension's real height");
            helper.assertTrue(!LabyrinthPlaces.isInStack(origin,p.pos()),"the staircase does not overwrite saved vignette slots");}
        var route=FinaleArchitecture.staircaseRoute(origin);helper.assertTrue(route.get(0).getY()-route.get(route.size()-1).getY()>=1280,"this is an actual long descent");
        for(int x=0;x>=-13;x--){var at=FinaleArchitecture.base(origin).offset(x,FinaleArchitecture.TOP,24);
            helper.assertTrue(blocks.get(at.below())!=null&&!blocks.get(at.below()).isAir()&&(blocks.get(at)==null||blocks.get(at).isAir())&&(blocks.get(at.above())==null||blocks.get(at.above()).isAir()),"the full authored plan joins Tom's camp to the actual first tread at "+x);}
        for(var at:StaircaseFire.braziers(origin))helper.assertTrue(blocks.get(at.below())!=null&&!blocks.get(at.below()).isAir(),"each of the five hearths stands on the actual authored landing");
        helper.assertTrue(blocks.get(StaircaseFire.shelf(origin).below()).is(Blocks.DARK_OAK_PLANKS),"the supply shelf has native camp support outside the arrival copy");
        for(int i=0;i<route.size();i++){
            helper.assertTrue(blocks.containsKey(route.get(i).below())&&!blocks.get(route.get(i).below()).isAir(),"every tread has a real block at "+i);
            var feet=blocks.get(route.get(i));var head=blocks.get(route.get(i).above());
            helper.assertTrue((feet==null||feet.isAir())&&(head==null||head.isAir()),"a landing cannot obstruct the route at tread "+i);
        }
        var continuation=FinaleArchitecture.continuationRoute(origin);helper.assertTrue(continuation.getFirst().getY()-continuation.getLast().getY()>=1280,"the staircase physically continues equally far below the cell");
        for(var at:continuation){
            var floor=blocks.get(at.below());var feet=blocks.get(at);var head=blocks.get(at.above());
            helper.assertTrue(floor!=null&&!floor.isAir()&&(feet==null||feet.isAir())&&(head==null||head.isAir()),"the lower continuation retains a connected unobstructed tread at "+at);
        }
        for(var mark:StaircaseWear.marks(origin)){
            for(var at:mark.cells())helper.assertTrue(blocks.get(at)!=null&&blocks.get(at).isAir(),"sparse wear is real edge damage, with an actual rail break");
            var at=mark.route();var after=FinaleArchitecture.fullRoute(origin).get(FinaleArchitecture.fullRoute(origin).indexOf(at)+1);boolean alongX=after.getX()!=at.getX();
            for(int side=-3;side<=3;side++)helper.assertTrue(!blocks.get(at.offset(alongX?0:side,-1,alongX?side:0)).isAir(),"wear retains seven continuous central walking columns for shared exploration");
        }
        for(int z=26;z<=32;z++)for(int y=FinaleArchitecture.ARENA;y<FinaleArchitecture.ARENA+3;y++){
            var opening=blocks.get(FinaleArchitecture.base(origin).offset(0,y,z));
            helper.assertTrue(opening==null||opening.isAir(),"the passage through the shaft wall really reaches the cell chamber");
        }
        // 0.4.653: the cell's gate, the passage's two doorways and the causeway are five wide, the gate and doorways seven high
        // (over the horns, which MinotaurVisualProof measures), for a creature half again its old size; the cell's walls stand
        // a block higher, and the body it walks out in clears the way from the middle of its cell to the foot of the stair.
        var b=FinaleArchitecture.base(origin);int half=FinaleArchitecture.GATE_HALF,high=FinaleArchitecture.GATE_HIGH,arena=FinaleArchitecture.ARENA;
        var last=route.get(route.size()-1);int lx=last.getX()-b.getX(),lz=last.getZ()-b.getZ();
        for(int x=-half;x<=half;x++){
            for(int z:new int[]{30,34}){
                for(int y=arena;y<arena+high;y++){var at=blocks.get(b.offset(x,y,z));helper.assertTrue(at==null||at.isAir(),"each doorway of the passage is five wide and seven high: "+x+","+(y-arena)+","+z);}
                helper.assertTrue(blocks.get(b.offset(x,arena+high,z))!=null&&!blocks.get(b.offset(x,arena+high,z)).isAir(),"a lintel stands over each doorway: "+x+","+z);
            }
            for(int y=arena;y<arena+high;y++)helper.assertTrue(blocks.get(b.offset(x,y,59)).is(Blocks.IRON_BARS),"the cell's gate is bars five wide and seven high: "+x+","+(y-arena));
            helper.assertTrue(blocks.get(b.offset(x,arena+high,59)).is(Blocks.CRACKED_DEEPSLATE_BRICKS),"the cell's wall closes over its gate");
            for(int z=lz;z<=32;z++)helper.assertTrue(blocks.get(b.offset(x,arena-1,z))!=null&&!blocks.get(b.offset(x,arena-1,z)).isAir(),"the causeway to the chamber is five wide at "+x+","+z);
        }
        for(int x=-half;x<=lx;x++)for(int z=lz-1;z<=lz-1+2*half;z++)helper.assertTrue(blocks.get(b.offset(x,arena-1,z))!=null&&!blocks.get(b.offset(x,arena-1,z)).isAir(),"the causeway from the stair is five wide at "+x+","+z);
        for(int x:new int[]{-half-1,half+1})helper.assertTrue(blocks.get(b.offset(x,arena,59)).is(Blocks.CRACKED_DEEPSLATE_BRICKS),"the cell's wall stands either side of its gate");
        int cellHigh=FinaleArchitecture.CELL_HIGH;helper.assertTrue(cellHigh>high,"the cell's walls stand higher than its gate");
        for(int x:new int[]{-6,6})for(int z=59;z<=69;z++){var top=blocks.get(b.offset(x,arena+cellHigh-1,z));
            helper.assertTrue(top!=null&&!top.isAir(),"the cell's walls stand "+cellHigh+" high at "+x+","+z);}
        for(int x=-6;x<=6;x++)helper.assertTrue(blocks.get(b.offset(x,arena+cellHigh-1,59))!=null&&!blocks.get(b.offset(x,arena+cellHigh-1,59)).isAir(),"the cell's front stands "+cellHigh+" high at "+x);
        int rail=lx-FinaleArchitecture.STAIR_HALF_WIDTH-1;
        for(int z=lz-1;z<=lz;z++)for(int y=arena;y<=arena+1;y++){var gap=blocks.get(b.offset(rail,y,z));
            helper.assertTrue(gap==null||gap.isAir(),"the inner rail opens where the causeway leaves the last treads: "+z+" / "+(y-arena));}
        net.minecraft.world.level.block.state.BlockState overRiser=blocks.get(b.offset(rail,arena+1,lz+1)),riserString=blocks.get(b.offset(rail,arena,lz+1)),railEnd=blocks.get(b.offset(rail,arena+1,lz+2));
        helper.assertTrue(overRiser==null||overRiser.isAir(),"the inner rail opens over the first riser too: "+overRiser);
        helper.assertTrue(riserString!=null&&!riserString.isAir(),"the string still stands under the first riser, as its step");
        helper.assertTrue(railEnd!=null&&railEnd.is(StairWell.BALUSTRADE.get())&&!railEnd.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.NORTH)
                &&railEnd.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.SOUTH),"the rail above the riser ends at its post: "+railEnd);
        // From the middle of its cell down the causeway on the causeway's floor; then, broader than the two treads level with the
        // causeway, it turns onto the stair up the first riser beside them, to where its climb begins.
        double wide=1.8*MinotaurEntity.SCALE,tall=3.7*MinotaurEntity.SCALE,riser=rail-wide/2;
        var way=new ArrayList<Vec3>();for(double z=64.5;z>=lz+.5;z-=.5)way.add(new Vec3(.5,arena,z));
        for(double x=.5;x<riser;x+=.5)way.add(new Vec3(x,arena,lz+.5));
        for(double x=riser;x<=lx+.5+.6*MinotaurEntity.SCALE;x+=.5)way.add(new Vec3(x,arena+1,lz+.5));
        for(var at:way)for(int x=(int)Math.floor(at.x-wide/2);x<=(int)Math.floor(at.x+wide/2);x++)for(int z=(int)Math.floor(at.z-wide/2);z<=(int)Math.floor(at.z+wide/2);z++)
            for(int y=(int)at.y;y<=(int)Math.floor(at.y+tall);y++){
                var state=blocks.get(b.offset(x,y,z));
                helper.assertTrue(state==null||state.isAir()||z==59&&state.is(Blocks.IRON_BARS),"the creature's body clears its way out at "+at+" (block "+x+","+(y-arena)+","+z+": "+state+")");
            }
        for(int n:new int[]{12,48,100,180}){
            var at=route.get(n);var next=route.get(n+1);boolean alongX=next.getX()!=at.getX();
            for(int lateral=-4;lateral<=4;lateral++){
                var walking=at.offset(alongX?0:lateral,0,alongX?lateral:0);
                helper.assertTrue(!blocks.get(walking.below()).isAir()&&(blocks.get(walking)==null||blocks.get(walking).isAir())&&(blocks.get(walking.above())==null||blocks.get(walking.above()).isAir()),"nine unobstructed blocks of actual walking width at tread "+n);
            }
        }
        for(int x=-FinaleArchitecture.SHAFT_RADIUS;x<=FinaleArchitecture.SHAFT_RADIUS;x++)for(int z=-FinaleArchitecture.SHAFT_RADIUS;z<=FinaleArchitecture.SHAFT_RADIUS;z++)
            helper.assertTrue(!blocks.get(FinaleArchitecture.base(origin).offset(x,FinaleArchitecture.LOOP_BOTTOM-1,z)).isAir(),"a complete shaft floor encloses the view below the great staircase");
        helper.assertTrue(blocks.get(FinaleArchitecture.cell(origin)).is(Blocks.IRON_BARS),"the cell is physically closed before commitment");helper.succeed();
    }
    @GameTest(template="empty") public static void escapeHasConnectedFloorAndRealWrongTurns(GameTestHelper helper){
        BlockPos origin=BlockPos.ZERO;Map<BlockPos,net.minecraft.world.level.block.state.BlockState> blocks=new HashMap<>();for(var p:FinaleArchitecture.plan(origin))blocks.put(p.pos(),p.block());
        var route=FinaleArchitecture.escapeRoute(origin);
        helper.assertTrue(route.get(0).equals(FinaleArchitecture.bottomStart(origin))&&route.get(route.size()-1).equals(FinaleArchitecture.exit(origin)),"the real route joins the arrival and exit");
        for(int i=0;i<route.size();i++){helper.assertTrue(blocks.containsKey(route.get(i).below())&&!blocks.get(route.get(i).below()).isAir(),"each route point has floor");
            if(i>0)helper.assertTrue(route.get(i).distManhattan(route.get(i-1))==1,"no teleport or gap replaces a walkable turn");}
        helper.assertTrue(blocks.containsKey(FinaleArchitecture.base(origin).offset(26,3,95)),"a wrong branch has real geometry");
        helper.assertTrue(blocks.get(FinaleArchitecture.base(origin).offset(0,3,76)).isAir(),"the bottom has real cleared dropoffs rather than generated stone");
        helper.assertTrue(blocks.get(FinaleArchitecture.cell(origin).south(4).above(2)).isAir(),"the cell's interior is carved out of terrain");
        helper.assertTrue(blocks.get(FinaleArchitecture.base(origin).offset(0,150,0)).isAir(),"the shaft has an actual open central void");helper.succeed();
    }
}
