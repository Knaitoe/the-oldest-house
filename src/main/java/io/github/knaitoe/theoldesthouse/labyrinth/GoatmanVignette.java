package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.opening.CompanionOrders;
import io.github.knaitoe.theoldesthouse.network.*;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.core.particles.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Anansi's Goatman: notice a stranger; the only decisive interaction is the trailer door. */
public final class GoatmanVignette {
    public static final String ID="goatman",ACTOR="TrailerChild",ROUND="TrailerRound",INDEX="TrailerIndex";
    public static final int PATH=1,GATHERING=2,VIGIL=3,DAWN=4,GATHER_TICKS=1200,VIGIL_TICKS=1200,MAX_PLAYERS=16;
    public static final int FACE=1,SILENT=2,LATE_LAUGH=4,STILL=8,FIRELIGHT=16,HEAD=32,PET=64;
    public static final String[] DEMANDS={"Let me in.","It's cold out here.","You know me. Open the door.","Everybody's in there. Let me in.","Please. Just open it."};
    private static final ResourceLocation CHILD_SCALE=ResourceLocation.fromNamespaceAndPath(io.github.knaitoe.theoldesthouse.TheOldestHouse.MOD_ID,"trailer_child_scale");
    private static final Vec3[] ACTIVITIES={new Vec3(-9.5,0,-49.5),new Vec3(-2.5,0,-48.5),new Vec3(.5,1,-57.5),new Vec3(-4.5,1,-71.5),new Vec3(4.5,1,-73.5)};
    private static final Vec3[] SUPPER={new Vec3(-1.5,1,-61.5),new Vec3(2.5,1,-63.5),new Vec3(-1.5,1,-65.5),new Vec3(2.5,1,-67.5),new Vec3(-1.5,1,-69.5)};
    private GoatmanVignette(){}
    public static CompoundTag run(LabyrinthData d){return d.state(ID).getCompound("Run").copy();}
    public static CompoundTag personal(LabyrinthData d,UUID id){return d.state(ID).getCompound("Players").getCompound(id.toString()).copy();}
    private static void saveRun(LabyrinthData d,CompoundTag r){CompoundTag all=d.state(ID);all.put("Run",r);d.setState(ID,all);}
    private static void savePersonal(LabyrinthData d,UUID id,CompoundTag p){CompoundTag all=d.state(ID),players=all.getCompound("Players");players.put(id.toString(),p);all.put("Players",players);d.setState(ID,all);}
    public static boolean canDeal(LabyrinthData d,UUID id){return !WitnessAccount.has(d,id,WitnessAccount.Story.GOATMAN)&&!personal(d,id).getBoolean("Respawn");}
    public static @Nullable BlockPos base(MinecraftServer s){return IndianLakeRooms.base(s,LabyrinthPlace.GOATMAN);}
    private static boolean inBounds(ServerPlayer p){BlockPos b=base(p.server);return b!=null&&p.gameMode.getGameModeForPlayer()!=GameType.SPECTATOR&&p.level().dimension().equals(HouseDimensions.INTERIOR)&&IndianLakeRooms.bounds(b,LabyrinthPlace.GOATMAN).contains(p.position());}
    public static boolean inside(ServerPlayer p){return p.isAlive()&&inBounds(p);}
    private static Vec3 rel(ServerPlayer p,BlockPos b){return p.position().subtract(b.getX(),b.getY(),b.getZ());}
    private static List<ServerPlayer> visitors(ServerLevel l,BlockPos b){return IndianLakeRooms.visitors(l,b,LabyrinthPlace.GOATMAN).stream().filter(p->p.gameMode.getGameModeForPlayer()!=GameType.SPECTATOR).toList();}
    public static void build(MinecraftServer s,ServerLevel l,BlockPos b){GoatmanWoods.build(l,b);}
    public static boolean canEnter(ServerPlayer p){
        if(!canDeal(LabyrinthData.get(p.server),p.getUUID()))return false;
        CompoundTag r=run(LabyrinthData.get(p.server));BlockPos b=base(p.server);ServerLevel l=p.server.getLevel(HouseDimensions.INTERIOR);
        boolean empty=b==null||l==null||visitors(l,b).isEmpty();
        if(!empty&&!r.getCompound("Cohort").contains(p.getUUID().toString())&&r.getCompound("Cohort").getAllKeys().size()>=MAX_PLAYERS)return false;
        CompoundTag own=r.getCompound("Cohort").getCompound(p.getUUID().toString());
        if(r.getInt("Phase")==DAWN)return empty;
        return r.getInt("Phase")!=VIGIL||empty||own.getBoolean("Active")&&!own.getBoolean("Failed");
    }
    private static CompoundTag newRun(ServerPlayer p){
        CompoundTag r=new CompoundTag();r.putUUID("Id",UUID.randomUUID());r.putInt("Phase",PATH);r.putInt("Wrong",p.getRandom().nextInt(5));r.putBoolean("ExtraInside",p.getRandom().nextBoolean());
        List<Integer> tells=new ArrayList<>(List.of(FACE,SILENT,LATE_LAUGH,STILL,FIRELIGHT,HEAD,PET));
        Collections.shuffle(tells,new Random(p.getRandom().nextLong()));int mask=0;for(int i=0,n=2+p.getRandom().nextInt(2);i<n;i++)mask|=tells.get(i);r.putInt("Tells",mask);
        List<Integer> skins=new ArrayList<>(List.of(0,1,2,3,4));Collections.shuffle(skins,new Random(p.getRandom().nextLong()));for(int i=0;i<5;i++)r.putInt("Skin"+i,skins.get(i));
        return r;
    }
    public static void onArrive(ServerPlayer p,LabyrinthPlace place){if(place==LabyrinthPlace.GOATMAN)enter(p);}
    /** Direct arrivals and reconnects use the same saved enrollment. No credit is awarded here. */
    public static boolean enter(ServerPlayer p){
        if(!inside(p)||!canDeal(LabyrinthData.get(p.server),p.getUUID()))return false;
        LabyrinthData d=LabyrinthData.get(p.server);BlockPos b=base(p.server);CompoundTag r=run(d),cohort=r.getCompound("Cohort");
        boolean member=cohort.contains(p.getUUID().toString());
        final CompoundTag initialCohort=cohort;
        boolean other=visitors(p.serverLevel(),b).stream().anyMatch(peer->peer!=p&&initialCohort.contains(peer.getUUID().toString())&&!initialCohort.getCompound(peer.getUUID().toString()).getBoolean("Failed"));
        if(r.getInt("Phase")==0||r.getInt("Phase")==DAWN&&!other||!other&&(!member||cohort.getCompound(p.getUUID().toString()).getBoolean("Failed")||!cohort.getCompound(p.getUUID().toString()).getBoolean("Active"))){
            r=newRun(p);cohort=new CompoundTag();member=false;removeActors(p.serverLevel(),b);
            GoatmanWoods.atmosphere(p.serverLevel(),b,false);GoatmanWoods.door(p.serverLevel(),b,true);
        }
        if(!member&&(r.getInt("Phase")==VIGIL||r.getInt("Phase")==DAWN||cohort.getAllKeys().size()>=MAX_PLAYERS))return false;
        CompoundTag own=cohort.getCompound(p.getUUID().toString());
        if(!member){own.putBoolean("Active",true);own.putDouble("Progress",0);own.putInt("Vigil",0);own.putBoolean("Arrived",false);}
        own.putBoolean("Active",true);cohort.put(p.getUUID().toString(),own);r.put("Cohort",cohort);saveRun(d,r);
        if(!member)p.displayClientMessage(Component.literal("You're late. Your cousins are already at the trailer."),false);
        scale(p,true);IndianLakeRooms.keepLoaded(p.serverLevel(),b,LabyrinthPlace.GOATMAN);stage(p.serverLevel(),b,r);
        GoatmanWoods.supplies(p.serverLevel(),b,4+cohort.getAllKeys().size());return true;
    }
    private static void scale(ServerPlayer p,boolean active){
        var attribute=p.getAttribute(Attributes.SCALE);if(attribute==null)return;
        if(active){if(!attribute.hasModifier(CHILD_SCALE))attribute.addTransientModifier(new AttributeModifier(CHILD_SCALE,-.3,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));}
        else attribute.removeModifier(CHILD_SCALE);
    }
    public static boolean childScale(ServerPlayer p){return p.getAttribute(Attributes.SCALE)!=null&&p.getAttribute(Attributes.SCALE).hasModifier(CHILD_SCALE);}
    public static void depart(ServerPlayer p){
        scale(p,false);HousePackets.send(p,new GoatmanScenePayload(0,0,0));
        LabyrinthData d=LabyrinthData.get(p.server);CompoundTag r=run(d),cohort=r.getCompound("Cohort");String key=p.getUUID().toString();
        if(cohort.contains(key)){CompoundTag own=cohort.getCompound(key);own.putBoolean("Active",false);own.putBoolean("Interrupted",true);cohort.put(key,own);r.put("Cohort",cohort);saveRun(d,r);}
        BlockPos b=base(p.server);if(b!=null)for(var girl:p.serverLevel().getEntitiesOfClass(GoatmanChild.class,IndianLakeRooms.bounds(b,LabyrinthPlace.GOATMAN),c->c.viewer().filter(p.getUUID()::equals).isPresent()))girl.discard();
    }
    private static List<GoatmanChild> actors(ServerLevel l,BlockPos b){return l.getEntitiesOfClass(GoatmanChild.class,IndianLakeRooms.bounds(b,LabyrinthPlace.GOATMAN),c->c.getTags().contains(ACTOR));}
    private static void removeActors(ServerLevel l,BlockPos b){for(var child:actors(l,b))child.discard();}
    public static void stage(ServerLevel l,BlockPos b,CompoundTag r){
        if(!r.hasUUID("Id"))return;List<GoatmanChild> children=actors(l,b);
        for(var c:children)if(!c.getPersistentData().hasUUID(ROUND)||!c.getPersistentData().getUUID(ROUND).equals(r.getUUID("Id")))c.discard();
        for(int i=0;i<5;i++){
            boolean missing=i==r.getInt("Wrong")&&r.getInt("Phase")>=VIGIL&&!r.getBoolean("ExtraInside")&&r.getInt("Clock")>=15;
            final int index=i;var matches=children.stream().filter(c->!c.isRemoved()&&!c.girl()&&c.getPersistentData().getInt(INDEX)==index).toList();
            if(missing){matches.forEach(Entity::discard);continue;}
            for(int n=1;n<matches.size();n++)matches.get(n).discard();
            if(!matches.isEmpty())continue;
            GoatmanChild c=GoatmanRegistry.CHILD.get().create(l);if(c==null)continue;
            c.addTag(ACTOR);c.getPersistentData().putUUID(ROUND,r.getUUID("Id"));c.getPersistentData().putInt(INDEX,i);
            c.appearance(r.getInt("Skin"+i),i==r.getInt("Wrong")?r.getInt("Tells"):0,null);
            Vec3 pos=r.getInt("Phase")>=VIGIL?SUPPER[i]:ACTIVITIES[i];c.moveTo(pos.add(b.getX(),b.getY(),b.getZ()));l.addFreshEntity(c);
        }
    }
    private static GoatmanChild girl(ServerPlayer p,BlockPos b,CompoundTag r){
        var girls=actors(p.serverLevel(),b).stream().filter(c->c.viewer().filter(p.getUUID()::equals).isPresent()).toList();
        for(int i=1;i<girls.size();i++)girls.get(i).discard();if(!girls.isEmpty())return girls.getFirst();
        GoatmanChild c=GoatmanRegistry.CHILD.get().create(p.serverLevel());if(c==null)return null;
        c.appearance(5,0,p.getUUID());c.addTag(ACTOR);c.getPersistentData().putUUID(ROUND,r.getUUID("Id"));c.getPersistentData().putInt(INDEX,-1);
        c.moveTo(GoatmanWoods.path(7).add(b.getX(),b.getY(),b.getZ()));p.serverLevel().addFreshEntity(c);return c;
    }
    private static void move(GoatmanChild c,Vec3 at,boolean backwards,boolean seated,ServerLevel l){
        double distance=c.position().distanceTo(at);c.moveTo(at);c.animate(distance,backwards,seated);
        if(distance>.005&&!c.tell(SILENT)&&!c.girl()){
            double step=c.getPersistentData().getDouble("Step")+distance;
            if(step>.7){l.playSound(null,c.blockPosition(),(c.getY()>=GoatmanVignette.base(l.getServer()).getY()+.7?SoundEvents.WOOD_STEP:SoundEvents.GRASS_STEP),SoundSource.NEUTRAL,.3F,1.2F);step=0;}c.getPersistentData().putDouble("Step",step);
        }
    }
    private static float facing(Vec3 from,Vec3 at){Vec3 v=at.subtract(from);return (float)(Math.atan2(v.z,v.x)*180/Math.PI)-90;}
    private static void face(GoatmanChild c,Vec3 at){float yaw=facing(c.position(),at);c.setYRot(yaw);c.setYHeadRot(yaw);c.yBodyRot=yaw;}
    private static void path(ServerPlayer p,BlockPos b,CompoundTag r,CompoundTag own){
        Vec3 relative=rel(p,b);var projected=GoatmanWoods.project(relative);double progress=own.getDouble("Progress");
        if(!own.getBoolean("Arrived")&&relative.z<-.1){
            // Dense edges cannot be flanked with jumping or a pearl. Normal walking remains free.
            if((!GoatmanWoods.clearing(relative)&&projected.distance()>1.65)||relative.y>2.2||relative.y<-.5||projected.progress()>progress+2){
                Vec3 at=GoatmanWoods.path(progress).add(b.getX(),b.getY(),b.getZ());p.connection.teleport(at.x,at.y,at.z,p.getYRot(),p.getXRot());p.fallDistance=0;
            }else {progress=projected.progress();own.putDouble("Progress",progress);}
        }
        if(!own.getBoolean("GirlGone")&&!own.getBoolean("Arrived")){
            GoatmanChild girl=girl(p,b,r);if(girl!=null){Vec3 at=GoatmanWoods.path(progress+7).add(b.getX(),b.getY(),b.getZ());move(girl,at,true,false,p.serverLevel());face(girl,p.position());
                if(progress+7>=54&&(!p.hasLineOfSight(girl)||p.getLookAngle().dot(girl.position().subtract(p.position()).normalize())<.25)){own.putBoolean("GirlGone",true);girl.discard();}
            }
        }else for(var girl:actors(p.serverLevel(),b))if(girl.viewer().filter(p.getUUID()::equals).isPresent())girl.discard();
        if(GoatmanWoods.clearing(rel(p,b))&&progress>=GoatmanWoods.CLEARING){
            own.putBoolean("Arrived",true);own.putBoolean("GirlGone",true);
            if(!own.getBoolean("Greeted")){own.putBoolean("Greeted",true);p.displayClientMessage(Component.literal("A cousin: There you are. Everyone's here."),false);}
        }
    }
    private static void cousins(ServerLevel l,BlockPos b,CompoundTag r,List<ServerPlayer> present){
        int phase=r.getInt("Phase"),clock=r.getInt("Clock");
        for(var c:actors(l,b)){
            if(c.girl())continue;int i=c.getPersistentData().getInt(INDEX);if(i<0||i>=5)continue;
            boolean supper=phase>=VIGIL||phase==GATHERING&&clock>=600;Vec3 target=supper?SUPPER[i]:ACTIVITIES[i];
            if(c.tell(FIRELIGHT))target=supper?new Vec3(6.5,1,-72.5):new Vec3(6.5,0,-50.5);
            boolean drift=phase==VIGIL&&i==(r.getInt("Wrong")+1)%5&&clock>=700&&clock<980;
            if(drift)target=new Vec3(.5,1,-56.5);
            Vec3 relative=c.position().subtract(b.getX(),b.getY(),b.getZ());
            if(supper&&relative.z>=-55){target=Math.abs(relative.x-.5)>.2?new Vec3(.5,relative.y,Math.max(-54.4,relative.z)):new Vec3(.5,1,-57.5);}
            Vec3 at=target.add(b.getX(),b.getY(),b.getZ());
            if(!supper&&!c.tell(STILL))at=at.add(Math.sin(clock*.018+i)*.35,0,Math.cos(clock*.013+i)*.35);
            Vec3 delta=at.subtract(c.position());double length=delta.length();if(length>.055)at=c.position().add(delta.scale(.055/length));
            move(c,at,false,supper&&!drift&&length<.1,l);
            ServerPlayer nearest=present.stream().min(Comparator.comparingDouble(c::distanceToSqr)).orElse(null);
            if(c.tell(FACE)&&nearest!=null)face(c,nearest.position());
            else if(!c.tell(STILL)){if(length>.02)face(c,at.add(delta));else {float yaw=supper?(c.getX()<b.getX()?270:90):180+(float)Math.sin(clock*.02+i)*20;c.setYRot(yaw);c.yBodyRot=yaw;c.setYHeadRot(yaw);}}
            if(phase==GATHERING&&clock>=80){int beat=clock%240;if(beat==(c.tell(LATE_LAUGH)?112:100))l.playSound(null,c.blockPosition(),GoatmanRegistry.LAUGH.get(),SoundSource.NEUTRAL,.28F,.95F+i*.025F);}
        }
    }
    /** Includes every enrolled real child, rather than treating multiplayer visitors as spectators. */
    public static int expectedCount(CompoundTag r){return r.contains("Expected")?r.getInt("Expected"):4+r.getCompound("Cohort").getAllKeys().size();}
    public static int presentCount(ServerLevel l,BlockPos b){return (int)actors(l,b).stream().filter(c->!c.girl()).count()+(int)visitors(l,b).stream().filter(p->GoatmanWoods.clearing(rel(p,b))).count();}
    public static void onServerTick(ServerTickEvent.Post event){tick(event.getServer());}
    public static void tick(MinecraftServer server){
        // A transient modifier is also removed after commands, death, cloning or dimension changes.
        for(ServerLevel world:server.getAllLevels())for(ServerPlayer p:List.copyOf(world.players()))if(childScale(p)&&!inside(p))depart(p);
        ServerLevel l=server.getLevel(HouseDimensions.INTERIOR);BlockPos b=base(server);if(l==null||b==null)return;
        List<ServerPlayer> present=visitors(l,b);if(present.isEmpty())return;IndianLakeRooms.keepLoaded(l,b,LabyrinthPlace.GOATMAN);
        LabyrinthData d=LabyrinthData.get(server);CompoundTag r=run(d);
        for(ServerPlayer p:present)if(!r.getCompound("Cohort").contains(p.getUUID().toString())&&canDeal(d,p.getUUID())){enter(p);r=run(d);}
        if(r.getInt("Phase")==0)return;CompoundTag cohort=r.getCompound("Cohort");int phase=r.getInt("Phase");
        List<ServerPlayer> enrolled=present.stream().filter(p->cohort.contains(p.getUUID().toString())&&!cohort.getCompound(p.getUUID().toString()).getBoolean("Failed")).toList();
        if(enrolled.isEmpty())return;
        if(phase<VIGIL&&r.getInt("Expected")!=4+enrolled.size()){r.putInt("Expected",4+enrolled.size());GoatmanWoods.supplies(l,b,4+enrolled.size());}
        boolean arrived=true,allInside=true;
        for(ServerPlayer p:enrolled){
            String key=p.getUUID().toString();CompoundTag own=cohort.getCompound(key);scale(p,true);
            if(phase<VIGIL)path(p,b,r,own);
            arrived&=own.getBoolean("Arrived");allInside&=GoatmanWoods.indoors(rel(p,b));
            if(phase==VIGIL){
                if(!GoatmanWoods.indoors(rel(p,b))){
                    // A closed wall or a teleport cannot substitute for opening the real door.
                    Vec3 at=new Vec3(b.getX()+.5,b.getY()+1,b.getZ()-58.5);p.connection.teleport(at.x,at.y,at.z,p.getYRot(),p.getXRot());
                }else own.putInt("Vigil",own.getInt("Vigil")+1);
            }
            cohort.put(key,own);
            if(server.getTickCount()%20==0){int clock=r.getInt("Clock"),index=demandAt(clock);HousePackets.send(p,new GoatmanScenePayload(phase,index,index==0?0:110-clock%200));}
        }
        r.put("Cohort",cohort);
        if(phase==PATH&&arrived){r.putInt("Phase",GATHERING);r.putInt("Clock",0);phase=GATHERING;}
        int clock=r.getInt("Clock");
        if(phase==PATH)clock++;
        if(phase==GATHERING){
            if(arrived)clock++;
            if(clock==600)GoatmanWoods.door(l,b,true);
            if(clock==610)say(enrolled,"A cousin: Food's on the table. Come inside before it gets cold.");
            if(clock>=GATHER_TICKS&&allInside&&actors(l,b).stream().filter(c->!c.girl()).allMatch(c->GoatmanWoods.indoors(c.position().subtract(b.getX(),b.getY(),b.getZ())))){
                phase=VIGIL;clock=0;r.putInt("Phase",phase);
                for(var p:enrolled)HousePackets.send(p,new HouseFadePayload(8,8,14));
                GoatmanWoods.atmosphere(l,b,true);GoatmanWoods.door(l,b,false);
            }
        }else if(phase==VIGIL){
            clock++;GoatmanWoods.door(l,b,false);
            if((clock>=30&&clock<VIGIL_TICKS-16&&(clock%80==30||clock%80==37||clock%80==45)||clock==VIGIL_TICKS-16))hammer(l,b,enrolled);
            if(clock==720)say(enrolled,"A cousin: Maybe we ought to check.");
            if(clock>=VIGIL_TICKS){
                phase=DAWN;r.putInt("Phase",phase);GoatmanWoods.atmosphere(l,b,false);
                for(var p:enrolled){CompoundTag own=cohort.getCompound(p.getUUID().toString());
                    if(own.getInt("Vigil")>=VIGIL_TICKS&&!own.getBoolean("Interrupted")){
                        CompoundTag personal=personal(d,p.getUUID());personal.putBoolean("Finished",true);savePersonal(d,p.getUUID(),personal);
                        WitnessAccount.resolve(p,WitnessAccount.Story.GOATMAN,"kept_door_closed");own.putBoolean("Resolved",true);cohort.put(p.getUUID().toString(),own);
                    }
                }
                say(enrolled,"A cousin: I think it stopped.");GoatmanWoods.door(l,b,true);
            }
        }
        r.putInt("Clock",clock);r.put("Cohort",cohort);saveRun(d,r);
        // Restore saved actors, never reroll a tell or duplicate a cousin after a restart.
        if(server.getTickCount()%20==0||clock==15)stage(l,b,r);cousins(l,b,r,enrolled);
        if(phase<DAWN&&server.getTickCount()%180==0&&phase!=VIGIL)l.playSound(null,b.offset(5,1,-40),GoatmanRegistry.WOODS.get(),SoundSource.AMBIENT,.35F,1);
    }
    public static int demandAt(int clock){return clock>=50&&clock<1080&&clock%200>=50&&clock%200<110?1+Math.min(4,clock/200):0;}
    private static void say(List<ServerPlayer> players,String line){for(var p:players)p.displayClientMessage(Component.literal(line),false);}
    private static void hammer(ServerLevel l,BlockPos b,List<ServerPlayer> players){
        Vec3 at=Vec3.atCenterOf(b.offset(GoatmanWoods.DOOR));
        for(var p:players)p.connection.send(new ClientboundSoundPacket(Holder.direct(GoatmanRegistry.HAMMER.get()),SoundSource.BLOCKS,at.x,at.y,at.z,1.35F,.91F+p.getRandom().nextFloat()*.15F,p.getRandom().nextLong()));
        l.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,Blocks.OAK_PLANKS.defaultBlockState()),at.x,at.y+.4,at.z+.05,10,.38,.55,.08,.012);
    }
    public static boolean companionScene(ServerPlayer p){
        if(!inside(p))return false;CompoundTag r=run(LabyrinthData.get(p.server));int phase=r.getInt("Phase");
        return phase>0&&(phase<VIGIL&&!r.getCompound("Cohort").getCompound(p.getUUID().toString()).getBoolean("Arrived")||phase==VIGIL||phase==GATHERING&&(r.getInt("Tells")&PET)!=0);
    }
    /** A refusal is bodily: even a DEEPER order cannot send a pet in front on the path. */
    public static boolean companion(TamableAnimal pet,ServerPlayer p){
        if(!inside(p)||pet.level()!=p.level()||pet.isLeashed()||pet.isPassenger()||pet.isOrderedToSit()||!p.getUUID().equals(CompanionOrders.owner(pet)))return false;
        BlockPos b=base(p.server);CompoundTag r=run(LabyrinthData.get(p.server)),own=r.getCompound("Cohort").getCompound(p.getUUID().toString());int phase=r.getInt("Phase");
        if(phase<VIGIL&&!own.getBoolean("Arrived")){
            double playerProgress=GoatmanWoods.project(rel(p,b)).progress(),petProgress=GoatmanWoods.project(pet.position().subtract(b.getX(),b.getY(),b.getZ())).progress();
            pet.getNavigation().stop();Vec3 behind=GoatmanWoods.path(Math.max(0,playerProgress-1.8)).add(b.getX(),b.getY(),b.getZ());
            if(petProgress>playerProgress-.4||pet.distanceToSqr(p)>36){pet.teleportTo(behind.x,behind.y,behind.z);pet.setDeltaMovement(Vec3.ZERO);}
            else if(pet.distanceToSqr(p)>4)pet.getNavigation().moveTo(behind.x,behind.y,behind.z,.85);
            pet.getLookControl().setLookAt(p,30,30);return true;
        }
        if(phase==VIGIL||(r.getInt("Tells")&PET)!=0&&phase==GATHERING){
            Entity target=phase==VIGIL?null:actors(p.serverLevel(),b).stream().filter(c->!c.girl()&&c.getPersistentData().getInt(INDEX)==r.getInt("Wrong")).findFirst().orElse(null);
            Vec3 at=target==null?Vec3.atCenterOf(b.offset(GoatmanWoods.DOOR)):target.position();
            Vec3 between=p.position().add(at.subtract(p.position()).normalize().scale(1.25));pet.getNavigation().moveTo(between.x,between.y,between.z,.7);pet.getLookControl().setLookAt(at.x,at.y+.6,at.z,60,45);
            if(phase==VIGIL&&p.serverLevel().getGameTime()%160==0)pet.playSound(pet instanceof Cat?SoundEvents.CAT_HISS:SoundEvents.WOLF_GROWL,.6F,.8F);
            return true;
        }
        return false;
    }
    /** Called only by a reachable, native trailer-door interaction. */
    public static boolean openDoor(ServerPlayer p,BlockPos at){
        if(!inside(p)||p.gameMode.getGameModeForPlayer()==GameType.SPECTATOR)return false;BlockPos b=base(p.server);
        if(!at.equals(b.offset(GoatmanWoods.DOOR))&&!at.equals(b.offset(GoatmanWoods.DOOR).above())||p.position().distanceToSqr(Vec3.atCenterOf(at))>25)return false;
        CompoundTag r=run(LabyrinthData.get(p.server));
        if(r.getInt("Phase")==VIGIL){
            CompoundTag own=r.getCompound("Cohort").getCompound(p.getUUID().toString());if(!own.getBoolean("Active")||own.getBoolean("Failed"))return true;
            p.hurt(p.damageSources().genericKill(),Float.MAX_VALUE);return true;
        }
        var state=p.serverLevel().getBlockState(b.offset(GoatmanWoods.DOOR));boolean open=state.getBlock() instanceof DoorBlock&&!state.getValue(DoorBlock.OPEN);GoatmanWoods.door(p.serverLevel(),b,open);return true;
    }
    public static void onBlock(PlayerInteractEvent.RightClickBlock e){
        if(!(e.getEntity() instanceof ServerPlayer p)||e.getHand()!=InteractionHand.MAIN_HAND||!inside(p))return;
        if(openDoor(p,e.getPos())){e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);return;}
        // Sleeping would skip the night; containers and props provide no alternative resolution.
        var state=e.getLevel().getBlockState(e.getPos());if(state.getBlock() instanceof BedBlock||state.is(Blocks.CAMPFIRE)||state.is(Blocks.SMOKER)||state.is(Blocks.BARREL)||state.getBlock() instanceof TrapDoorBlock){e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);}
    }
    public static void onAttack(AttackEntityEvent e){if(e.getEntity() instanceof ServerPlayer p&&e.getTarget() instanceof GoatmanChild&&inside(p))e.setCanceled(true);}
    /** A failed native death, including keepInventory worlds, puts the original objects on her shelves. */
    public static void onDeath(LivingDeathEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!inBounds(p))return;
        LabyrinthData d=LabyrinthData.get(p.server);CompoundTag r=run(d),cohort=r.getCompound("Cohort"),own=cohort.getCompound(p.getUUID().toString());if(!own.getBoolean("Active"))return;
        own.putBoolean("Failed",true);own.putBoolean("Active",false);cohort.put(p.getUUID().toString(),own);r.put("Cohort",cohort);saveRun(d,r);
        CompoundTag personal=personal(d,p.getUUID());personal.putBoolean("Respawn",true);personal.putInt("Failures",personal.getInt("Failures")+1);savePersonal(d,p.getUUID(),personal);
        MotherCollection collection=MotherCollection.get(p.server);
        for(int i=0;i<p.getInventory().getContainerSize();i++)take(p,collection,p.getInventory().removeItemNoUpdate(i));
        ItemStack cursor=p.containerMenu.getCarried();p.containerMenu.setCarried(ItemStack.EMPTY);take(p,collection,cursor);
        p.inventoryMenu.broadcastChanges();scale(p,false);d.clearReturns(p.getUUID());HousePackets.send(p,new GoatmanScenePayload(0,0,0));
    }
    private static void take(ServerPlayer p,MotherCollection c,ItemStack stack){
        if(stack.isEmpty())return;
        if(c.banished()||c.keepVignetteItem(stack,p.registryAccess(),p.getUUID(),p.serverLevel().getGameTime())==null){ItemEntity item=p.drop(stack,true,false);if(item!=null)MotherOfStrays.recordDropOwner(item,p.getUUID());}
    }
    public static void onRespawnPosition(PlayerRespawnPositionEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p)||e.isFromEndFight()||!personal(LabyrinthData.get(p.server),p.getUUID()).getBoolean("Respawn"))return;
        BlockPos origin=HouseSavedData.get(p.server).houseOrigin();ServerLevel l=p.server.getLevel(HouseDimensions.INTERIOR);if(origin==null||l==null)return;
        Vec3 at=HideAndClap.manorRespawn(origin);l.getChunkAt(BlockPos.containing(at));e.setDimensionTransition(new DimensionTransition(l,at,Vec3.ZERO,0,0,DimensionTransition.DO_NOTHING));
    }
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;scale(p,false);LabyrinthData d=LabyrinthData.get(p.server);CompoundTag own=personal(d,p.getUUID());
        if(own.getBoolean("Respawn")){own.putBoolean("Respawn",false);savePersonal(d,p.getUUID(),own);p.displayClientMessage(Component.literal("You wake in the manor. Your hands are empty."),false);}
    }
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p){scale(p,false);if(inside(p))enter(p);}}
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p){scale(p,false);HousePackets.send(p,new GoatmanScenePayload(0,0,0));}}
}
