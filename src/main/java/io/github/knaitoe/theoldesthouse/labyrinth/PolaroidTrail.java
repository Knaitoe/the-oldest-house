package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.PolaroidFlashPayload;
import io.github.knaitoe.theoldesthouse.network.PolaroidViewPayload;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3f;

/**
 * 0.4.204, the Polaroid Trail (docs/POLAROID_TRAIL_0_4_204.md): the owner's fourth proposal of 31 October, after Penpal's
 * footsteps, balloons, boxes, screens and friends. Every place the reader walked as a child was being photographed from ten
 * feet away.
 *
 * <p>The watcher is never seen, only his camera: an amber glint from a hiding place while the flash charges, then the flash.
 * Half a minute later the developed photograph is left nearby under a yellow balloon: the reader, from where he stood, taken
 * by the House's own camera through the real blocks ({@link HouseLens}). To go on, the reader stands where the photograph was
 * taken of them and looks back at where he was: a beam of dust runs down the sightline with the shutter's click, and what was
 * shut opens. Josh, the reader's friend, is only a sepia shape and a flashlight beam pointing the way, and his voice on the
 * walkie-talkie.
 *
 * <p>Four visits, one place each: the yard (the creek seen from the culvert opens its side grate); the crawlspace under the
 * house next door (the cat bowl, then the bed of stolen clothes, opens the hatch in the clay); the cinema (four flashes from the
 * booth, the one sitting behind the reader a few rows nearer in each, until turning round to face him cuts the power and opens
 * the fire exit); the pines (the last photograph, taken from straight above the trench on the left, and the reader looking
 * straight up from it: fifty balloons light in the branches, and the album is ready in the deer blind). Understanding.
 *
 * <p>Failure: looking at the camera while the flash goes off. The visit goes white and the reader is back on the porch with
 * that visit to do again; and until they finish, now and then, in the House's ordinary halls, a photograph of them from behind
 * is in their pocket that they did not put there.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class PolaroidTrail {
    private PolaroidTrail(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.POLAROID_TRAIL;
    /** The flash charges for this long (the glint), and the photograph is left this long after it. */
    public static final int CHARGE=30,DEVELOP=600;
    /** How close to the photographed spot, and how straight the look back must be. */
    public static final double SPOT=1.4,LOOK=.94;
    public static final int FW=64,FH=48;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private static final DustParticleOptions AMBER=new DustParticleOptions(new Vector3f(1F,.75F,0F),1F),SEPIA=new DustParticleOptions(new Vector3f(.85F,.47F,.02F),.8F),
        MOTE=new DustParticleOptions(new Vector3f(1F,.98F,.86F),.6F),FIREFLY=new DustParticleOptions(new Vector3f(.85F,1F,.4F),.5F),BEAM=new DustParticleOptions(new Vector3f(.95F,.95F,.88F),.7F);

    /** One photograph the watcher takes: which visit, where he stands, where it is left. */
    public record Shot(String id,int visit,Vec3 node,BlockPos drop){}
    public static final List<Shot> SHOTS=List.of(
        new Shot("creek",1,new Vec3(-13.5,.9,-24.5),new BlockPos(8,1,-12)),
        new Shot("bowl",2,new Vec3(-19.5,3.4,-43.5),new BlockPos(-19,3,-51)),
        new Shot("clothes",2,new Vec3(-4.5,3.4,-44.5),new BlockPos(-7,3,-56)),
        new Shot("c1",3,new Vec3(.5,9.5,-70.5),new BlockPos(-4,3,-98)),
        new Shot("c2",3,new Vec3(.5,9.5,-70.5),new BlockPos(-2,3,-98)),
        new Shot("c3",3,new Vec3(.5,9.5,-70.5),new BlockPos(0,3,-98)),
        new Shot("c4",3,new Vec3(.5,9.5,-70.5),new BlockPos(2,3,-98)),
        new Shot("p1",4,new Vec3(-12.5,14.5,-114.5),new BlockPos(-8,4,-121)),
        new Shot("p2",4,new Vec3(14.5,15.5,-130.5),new BlockPos(7,4,-124)),
        new Shot("p3",4,new Vec3(-14.5,14.5,-138.5),new BlockPos(-8,4,-126)),
        new Shot("p4",4,new Vec3(-5.0,17.5,-123.0),new BlockPos(-7,3,-123)));
    public static @Nullable Shot shot(String id){for(var s:SHOTS)if(s.id().equals(id))return s;return null;}
    /** The rows the one behind the reader sits in after each flash in the cinema, and the last, right behind them. */
    public static final int[] SITTER_ROWS={0,3,6,9,10};

    // ------------------------------------------------------------------------------------------------ records
    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    static ListTag shots(CompoundTag own){return own.getList("Shots204",Tag.TAG_COMPOUND);}
    public static @Nullable CompoundTag find(CompoundTag own,String id){var list=shots(own);for(int i=0;i<list.size();i++)if(list.getCompound(i).getString("Id").equals(id))return list.getCompound(i);return null;}
    public static int state(CompoundTag own,String id){var s=find(own,id);return s==null?-1:s.getInt("State");}
    static void put(CompoundTag own,CompoundTag shot){var list=shots(own);for(int i=0;i<list.size();i++)if(list.getCompound(i).getString("Id").equals(shot.getString("Id"))){list.set(i,shot);own.put("Shots204",list);return;}list.add(shot);own.put("Shots204",list);}
    static void arm(ServerPlayer p,CompoundTag own,String id,int delay){if(find(own,id)!=null)return;var t=new CompoundTag();t.putString("Id",id);t.putInt("State",0);t.putLong("At",p.serverLevel().getGameTime()+delay);put(own,t);}
    @Nullable static BlockPos base(ServerPlayer p){return LiteraryVignettes.base(p,PLACE);}
    static boolean inside(ServerPlayer p){return LiteraryVignettes.inside(p,PLACE);}
    static Vec3 rel(ServerPlayer p,BlockPos b){return p.position().subtract(b.getX(),b.getY(),b.getZ());}
    public static Vec3 at(BlockPos b,Vec3 r){return new Vec3(b.getX()+r.x,b.getY()+r.y,b.getZ()+r.z);}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    static void sound(ServerLevel l,Vec3 at,SoundEvent s,float volume,float pitch){l.playSound(null,at.x,at.y,at.z,s,SoundSource.AMBIENT,volume,pitch);}
    /** Everyone in this copy of the trail with the reader. */
    static List<ServerPlayer> scene(MinecraftServer s,BlockPos b){var out=new ArrayList<ServerPlayer>();for(var q:s.getPlayerList().getPlayers())if(inside(q)&&b.equals(base(q)))out.add(q);return out;}
    /** Whether the reader's eyes are on a point: in front of them, nearly straight, and nothing solid between. */
    static boolean looking(ServerPlayer p,Vec3 point,double dot){
        var eye=p.getEyePosition();var d=point.subtract(eye);double len=d.length();if(len<.2||len>48)return false;
        if(p.getLookAngle().dot(d.scale(1/len))<dot)return false;
        var hit=p.level().clip(new ClipContext(eye,point,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,CollisionContext.empty()));
        return hit.getType()==HitResult.Type.MISS||hit.getLocation().distanceTo(eye)>=len-.7;
    }

    /** Whether the camera at a node has a clear line to the reader's eyes (the same line the reader looks back down). */
    static boolean sees(ServerLevel l,Vec3 node,ServerPlayer p){
        var eye=p.getEyePosition();double len=eye.distanceTo(node);if(len<.2)return true;
        var hit=l.clip(new ClipContext(node,eye,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,CollisionContext.empty()));
        return hit.getType()==HitResult.Type.MISS||hit.getLocation().distanceTo(node)>=len-.7;
    }

    // ------------------------------------------------------------------------------------------------ the ways on
    public static final int GRATE=1,HATCH=2,FIRE_EXIT=4;
    static void gates(ServerLevel l,BlockPos b,int mask){
        for(int x=PolaroidSite.CULVERT_X0;x<=PolaroidSite.CULVERT_X1;x++)for(int y=0;y<=1;y++)stage(l,b.offset(x,y,PolaroidSite.GRATE_Z),(mask&GRATE)!=0?1:0);
        stage(l,b.offset(PolaroidSite.HATCH),(mask&HATCH)!=0?1:0);
        var door=b.offset(PolaroidSite.FIRE_EXIT);var s=l.getBlockState(door);
        if(s.getBlock() instanceof DoorBlock d&&s.getValue(DoorBlock.OPEN)!=((mask&FIRE_EXIT)!=0))d.setOpen(null,l,s,door,(mask&FIRE_EXIT)!=0);
    }
    static void stage(ServerLevel l,BlockPos at,int stage){var s=l.getBlockState(at);if(s.is(PolaroidRegistry.FITTING.get())&&s.getValue(PolaroidBlock.STAGE)!=stage)l.setBlock(at,s.setValue(PolaroidBlock.STAGE,stage),F);}
    static int openFor(MinecraftServer s,BlockPos b){int mask=0;for(var q:scene(s,b))mask|=own(q).getInt("Gates204");return mask;}

    // ------------------------------------------------------------------------------------------------ arrival
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        int visit=own.getInt("Visit");
        // An unfinished visit starts again from its beginning; what an earlier visit opened stays open for this reader.
        if(own.getInt("Beat")<visit)clearVisit(p,b,own,visit);
        own.putInt("CrawlTicks204",0);
        var l=p.serverLevel();int mask=own.getInt("Gates204");for(var q:scene(p.server,b))if(q!=p)mask|=own(q).getInt("Gates204");gates(l,b,mask);
        if(visit==1&&own.getInt("Beat")<1)josh(p,own,"start",null,null);
    }
    /** Takes away what a visit left (its photographs, its balloons, the cinema's state) so it can be walked again. */
    static void clearVisit(ServerPlayer p,BlockPos b,CompoundTag own,int visit){
        var l=p.serverLevel();var list=shots(own);var keep=new ListTag();
        for(int i=0;i<list.size();i++){var t=list.getCompound(i);var s=shot(t.getString("Id"));if(s!=null&&s.visit()==visit){clearDrop(l,b,s);continue;}keep.add(t);}
        own.put("Shots204",keep);
        if(visit==3){cinema(l,b,false);own.putBoolean("Projector204",false);own.putInt("Sil204",0);}
        own.putLong("LastFlash204",0);
    }
    static void clearDrop(ServerLevel l,BlockPos b,Shot s){
        var at=b.offset(s.drop());if(PolaroidBlock.is(l.getBlockState(at),PolaroidBlock.Kind.PHOTO))l.setBlock(at,Blocks.AIR.defaultBlockState(),F);
        var up=at.above(2);if(PolaroidBlock.is(l.getBlockState(up),PolaroidBlock.Kind.BALLOON))l.setBlock(up,Blocks.AIR.defaultBlockState(),F);
    }

    // ------------------------------------------------------------------------------------------------ hands
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var s=l.getBlockState(b.offset(rel));var kind=PolaroidBlock.kind(s);if(kind==null)return false;
        switch(kind){
            case TAG->{int n=rel.equals(PolaroidSite.TAG2)?2:rel.equals(PolaroidSite.TAG3)?3:rel.equals(PolaroidSite.TAG4)?4:1;if(n==1)return false;
                LiteraryVignettes.open(p,PLACE,own,"Tag"+n,PolaroidTexts.tag(n,p.getGameProfile().getName()),b.offset(rel));return true;}
            case PHOTO->{take(p,b,rel,own);return true;}
            case BALLOON->{sound(l,Vec3.atCenterOf(b.offset(rel)),PolaroidRegistry.BALLOON.get(),.6F,1.2F);cue(p,"A yellow balloon on a string, tied off on something under it.");return true;}
            case BOWL->{cue(p,"A ceramic cat bowl with a name painted round the rim. The water in it is fresh.");return true;}
            case CLOTHES->{cue(p,"Sheets off somebody's line, men's shirts, a child's coat. Flattened, slept on. One of the shirts is the same as the one you had on, on the porch.");return true;}
            case SEAT->{cue(p,s.getValue(PolaroidBlock.STAGE)==2?"Someone is sitting here. You do not look at their face.":"Velvet, worn shiny. The seat is warm.");return true;}
            case PROJECTOR->{return true;}
            case RAFT->{cue(p,"Pallets nailed together, half a raft. Somebody's dad's nails, bent over.");return true;}
            case LUNCHBOX->{cue(p,"A child's lunchbox, empty but for a roll of film with nothing on it yet.");return true;}
            case GRATE->{cue(p,s.getValue(PolaroidBlock.STAGE)==0?"The grate is latched from the other side.":"The grate hangs open.");return true;}
            case HATCH->{cue(p,s.getValue(PolaroidBlock.STAGE)==0?"A hatch in the clay, latched from underneath.":"The hatch is open. A ladder goes down.");return true;}
            default->{return false;}
        }
    }
    /** A photograph picked up from under its balloon: whoever picks it up keeps it, whoever it is of. */
    static void take(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag mine){
        var l=p.serverLevel();
        // The clicking reader's record is the one the dispatcher saves after this; everyone else's is saved here.
        for(var q:scene(p.server,b)){var own=q==p?mine:own(q);var list=shots(own);
            for(int i=0;i<list.size();i++){var t=list.getCompound(i);var s=shot(t.getString("Id"));
                if(s==null||!s.drop().equals(rel)||t.getInt("State")!=2)continue;
                t.putInt("State",3);put(own,t);
                if(s.visit()==3)own.putInt("Collected204",own.getInt("Collected204")+1);
                if(q!=p)save(q,own);clearDrop(l,b,s);
                // Two readers photographed on the same visit share the spot: the other one's photograph is still there under it.
                for(var r:scene(p.server,b)){var other=find(r==p?mine:own(r),s.id());if(r!=q&&other!=null&&other.getInt("State")==2){place(l,b,s);break;}}
                LiteraryVignettes.give(p,photo(t.getByteArray("Frame"),s.id(),q.getGameProfile().getName()));
                sound(l,Vec3.atCenterOf(b.offset(rel)),PolaroidRegistry.BALLOON.get(),.5F,.8F);
                l.sendParticles(MOTE,b.getX()+rel.getX()+.5,b.getY()+rel.getY()+2.5,b.getZ()+rel.getZ()+.5,6,.2,.6,.2,0);
                cue(p,q==p?"A photograph, still faintly warm. On the white border: "+PolaroidTexts.caption(s.id()):"A photograph of "+q.getGameProfile().getName()+". On the border: "+PolaroidTexts.caption(s.id()));
                return;
            }
        }
        cue(p,"A photograph, face down, gone soft with damp. It is not one of yours.");
    }
    public static ItemStack photo(byte[] frame,String id,String of){
        var s=new ItemStack(PolaroidRegistry.PHOTO.get());
        CustomData.update(DataComponents.CUSTOM_DATA,s,t->{t.putByteArray("Frame",frame);t.putString("Shot",id);t.putString("Of",of);});
        s.set(DataComponents.LORE,new ItemLore(List.of(Component.literal(PolaroidTexts.caption(id)).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC))));
        return s;
    }
    /** Looking at a photograph in the hand. */
    public static void view(ServerPlayer p,ItemStack s){
        var c=s.get(DataComponents.CUSTOM_DATA);if(c==null)return;var t=c.copyTag();
        HousePackets.send(p,new PolaroidViewPayload(t.getByteArray("Frame"),PolaroidTexts.caption(t.getString("Shot"))));
    }

    // ------------------------------------------------------------------------------------------------ Josh
    record Echo(net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> level,Vec3 at,Vec3 target,long until){}
    static final Map<UUID,Echo> ECHO=new HashMap<>();
    /** Josh on the walkie, once per line per reader; and, where given, his shape at a place with his flashlight on the next one. */
    static void josh(ServerPlayer p,CompoundTag own,String line,@Nullable Vec3 at,@Nullable Vec3 target){
        var said=own.getList("Josh204",Tag.TAG_STRING);for(int i=0;i<said.size();i++)if(said.getString(i).equals(line)&&at==null)return;
        if(at==null){said.add(StringTag.valueOf(line));own.put("Josh204",said);}
        p.displayClientMessage(Component.literal(PolaroidTexts.walkie(PolaroidTexts.josh(line))).withStyle(ChatFormatting.GOLD),true);
        p.serverLevel().playSound(null,p.blockPosition(),PolaroidRegistry.WALKIE_SOUND.get(),SoundSource.PLAYERS,.6F,1F);
        if(at!=null&&target!=null)ECHO.put(p.getUUID(),new Echo(p.level().dimension(),at,target,p.serverLevel().getGameTime()+70));
    }
    static void echoes(MinecraftServer s){
        for(var it=ECHO.entrySet().iterator();it.hasNext();){var e=it.next();var p=s.getPlayerList().getPlayer(e.getKey());var echo=e.getValue();
            if(p==null||!p.level().dimension().equals(echo.level())){it.remove();continue;}
            var l=p.serverLevel();long now=l.getGameTime();
            if(now>=echo.until()){for(int i=0;i<10;i++)l.sendParticles(p,FIREFLY,true,echo.at().x,echo.at().y+.8,echo.at().z,1,.5,.6,.5,.02);it.remove();continue;}
            for(double y=0;y<=1.3;y+=.22)l.sendParticles(p,SEPIA,true,echo.at().x,echo.at().y+y,echo.at().z,1,.12,.02,.12,0);
            var from=echo.at().add(0,1.1,0);var d=echo.target().subtract(from);double len=Math.min(10,d.length());d=d.normalize();
            for(double t=.6;t<len;t+=.6)l.sendParticles(p,BEAM,true,from.x+d.x*t,from.y+d.y*t,from.z+d.z*t,1,0,0,0,0);
        }
    }
    /** Josh's walkie-talkie, kept: his flashlight beam toward the way the reader came in, wherever in the House they are. */
    public static void walkie(ServerPlayer p,ItemStack s){
        var c=s.get(DataComponents.CUSTOM_DATA);if(c==null||!c.copyTag().hasUUID(LiteraryVignettes.OWNER)||!c.copyTag().getUUID(LiteraryVignettes.OWNER).equals(p.getUUID())){cue(p,"Static. Nobody on this channel for you.");return;}
        var origin=HouseSavedData.get(p.server).houseOrigin();
        var place=origin==null?null:LiteraryVignettes.current(p);if(place==null&&origin!=null&&p.level().dimension().equals(HouseDimensions.INTERIOR))place=LabyrinthPlaces.placeAt(origin,p.blockPosition());
        BlockPos entry=place==null?null:LiteraryRooms.isLiterary(place)?LiteraryVignettes.base(p,place):LabyrinthPlaces.base(origin,place);
        p.getCooldowns().addCooldown(s.getItem(),600);
        if(entry==null){p.displayClientMessage(Component.literal(PolaroidTexts.walkie("Josh: I can't see you from here. Over.")).withStyle(ChatFormatting.GOLD),true);return;}
        p.displayClientMessage(Component.literal(PolaroidTexts.walkie("Josh: The way you came in is that way. Over.")).withStyle(ChatFormatting.GOLD),true);
        p.serverLevel().playSound(null,p.blockPosition(),PolaroidRegistry.WALKIE_SOUND.get(),SoundSource.PLAYERS,.6F,1F);
        var look=p.getLookAngle();ECHO.put(p.getUUID(),new Echo(p.level().dimension(),p.position().add(look.x*1.5,0,look.z*1.5),Vec3.atCenterOf(entry),p.serverLevel().getGameTime()+60));
    }

    // ------------------------------------------------------------------------------------------------ the camera
    public static Vec3 sitter(BlockPos b,int row){return PolaroidSite.at(b,PolaroidSite.SITTER_X+.5,PolaroidSite.PLATEAU,PolaroidSite.rowZ(row)+.5);}
    static void cinema(ServerLevel l,BlockPos b,boolean on){
        stage(l,b.offset(PolaroidSite.PROJECTOR),on?1:0);
        for(int x=-9;x<=9;x++)for(int y=5;y<=11;y++)stage(l,b.offset(x,y,PolaroidSite.CINEMA_Z0),on?1:0);
        for(int i=0;i<PolaroidSite.ROWS.length();i++){stage(l,b.offset(PolaroidSite.SITTER_X,PolaroidSite.PLATEAU,PolaroidSite.rowZ(i)),0);
            stage(l,b.offset(-1,PolaroidSite.PLATEAU,PolaroidSite.rowZ(i)),0);stage(l,b.offset(1,PolaroidSite.PLATEAU,PolaroidSite.rowZ(i)),0);}
        stage(l,b.offset(PolaroidSite.EXIT_SIGN),0);
    }
    static void sit(ServerLevel l,BlockPos b,int from,int to){
        stage(l,b.offset(PolaroidSite.SITTER_X,PolaroidSite.PLATEAU,PolaroidSite.rowZ(from)),0);
        stage(l,b.offset(PolaroidSite.SITTER_X,PolaroidSite.PLATEAU,PolaroidSite.rowZ(to)),2);
    }
    /** The flash itself, at the end of its charge: caught, or a photograph of the reader from where he is. */
    static void flash(ServerPlayer p,BlockPos b,CompoundTag own,CompoundTag t,Shot s){
        var l=p.serverLevel();var node=at(b,s.node());
        for(var q:scene(p.server,b)){l.sendParticles(q,ParticleTypes.FLASH,true,node.x,node.y,node.z,1,0,0,0,0);
            var d=node.subtract(q.getEyePosition());double len=d.length();double dot=len<.1?1:q.getLookAngle().dot(d.scale(1/len));
            if(dot>.45&&len<40)HousePackets.send(q,new PolaroidFlashPayload(8,(float)Math.min(1,(dot-.45)/.5)));}
        sound(l,node,PolaroidRegistry.FLASH.get(),1.2F,1F);sound(l,node,PolaroidRegistry.CLICK.get(),.8F,1F);
        if(looking(p,node,LOOK)){caught(p,b,own,s.visit());return;}
        var figures=new ArrayList<HouseLens.Figure>();for(var q:scene(p.server,b))figures.add(new HouseLens.Figure(q.position(),q.getBbHeight()));
        if(s.visit()==3)figures.add(new HouseLens.Figure(sitter(b,own.getInt("Sil204")),1.3));
        byte[] frame=HouseLens.shoot(l,node,p.getEyePosition(),figures,FW,FH,s.id().equals("p4")?70:50);
        t.putInt("State",1);t.putLong("At",l.getGameTime()+DEVELOP);t.putByteArray("Frame",frame);
        t.putDouble("SX",p.getX()-b.getX());t.putDouble("SY",p.getY()-b.getY());t.putDouble("SZ",p.getZ()-b.getZ());
        own.putLong("LastFlash204",l.getGameTime());
    }
    /** Looked at the flash as it went off: the visit goes white and the reader is back on the porch. */
    static void caught(ServerPlayer p,BlockPos b,CompoundTag own,int visit){
        HousePackets.send(p,new PolaroidFlashPayload(60,1F));
        own.putInt("Caught204",own.getInt("Caught204")+1);own.putBoolean("Haunted204",true);
        clearVisit(p,b,own,visit);own.putInt("CrawlTicks204",0);
        var porch=PolaroidSite.at(b,.5,0,-2.5);p.teleportTo(p.serverLevel(),porch.x,porch.y,porch.z,180,0);p.setDeltaMovement(Vec3.ZERO);p.connection.resetPosition();
        cue(p,"The flash is full in your face. When you can see again you are standing on the porch.");
    }
    /** The photograph left under its balloon, half a minute after the flash. */
    static void drop(ServerPlayer p,BlockPos b,CompoundTag t,Shot s){
        var l=p.serverLevel();var at=b.offset(s.drop());place(l,b,s);
        sound(l,Vec3.atCenterOf(at),PolaroidRegistry.BALLOON.get(),.7F,1F);
        t.putInt("State",2);
        cue(p,s.visit()==2?"Something has been left by you, under the house, face up.":"A yellow balloon is tied off over there, bobbing, with something under it.");
    }
    static void place(ServerLevel l,BlockPos b,Shot s){
        var at=b.offset(s.drop());
        if(l.getBlockState(at).isAir()||l.getBlockState(at).canBeReplaced())l.setBlock(at,PolaroidBlock.of(PolaroidBlock.Kind.PHOTO,Direction.NORTH,1),F);
        var up=at.above(2);if(l.getBlockState(up).isAir()&&l.getBlockState(at.above()).isAir())l.setBlock(up,PolaroidBlock.of(PolaroidBlock.Kind.BALLOON,Direction.NORTH,0),F);
    }
    /** Someone stood where the photograph was taken and looked back at the camera. */
    static void aligned(ServerPlayer p,ServerPlayer who,BlockPos b,CompoundTag own,CompoundTag t,Shot s){
        var l=p.serverLevel();var node=at(b,s.node());var eye=who.getEyePosition();var d=node.subtract(eye);
        for(double k=0;k<=1;k+=.04)for(var q:scene(p.server,b))l.sendParticles(q,MOTE,true,eye.x+d.x*k,eye.y+d.y*k,eye.z+d.z*k,1,.03,.03,.03,0);
        sound(l,node,PolaroidRegistry.CLICK.get(),1F,.8F);
        t.putInt("State",4);put(own,t);
        var spot=who.position();
        switch(s.id()){
            case "creek"->{own.putInt("Gates204",own.getInt("Gates204")|GRATE);gates(l,b,openFor(p.server,b)|own.getInt("Gates204"));finishVisit(own,1);
                josh(p,own,"creek",spot,PolaroidSite.at(b,-13.5,1,-37.5));cue(who,"Somewhere inside the culvert a latch lifts.");}
            case "bowl"->{arm(p,own,"clothes",100);josh(p,own,"bowl",spot,PolaroidSite.at(b,-5,3.3,-56));}
            case "clothes"->{own.putInt("Gates204",own.getInt("Gates204")|HATCH);gates(l,b,openFor(p.server,b)|own.getInt("Gates204"));finishVisit(own,2);
                josh(p,own,"clothes",spot,PolaroidSite.at(b,-11.5,2.6,-56.5));cue(who,"Under the clay, a bolt draws back.");}
            default->{}
        }
    }
    static void finishVisit(CompoundTag own,int visit){if(own.getInt("Visit")==visit&&own.getInt("Beat")<visit)own.putInt("Beat",visit);}

    // ------------------------------------------------------------------------------------------------ every tick
    private static final Map<Long,Set<UUID>> PRESENT=new HashMap<>();
    private static final Map<UUID,long[]> HALL=new HashMap<>();
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();long tick=s.getTickCount();
        if(tick%2==0)echoes(s);
        if(tick%5!=0)return;
        var copies=new HashMap<Long,List<ServerPlayer>>();
        for(var p:s.getPlayerList().getPlayers()){
            if(!LiteraryVignettes.participant(p))continue;
            if(inside(p)){var b=base(p);if(b==null)continue;copies.computeIfAbsent(b.asLong(),k->new ArrayList<>()).add(p);var own=own(p);if(reader(p,b,own))save(p,own);}
            else if(tick%20==0&&p.level().dimension().equals(HouseDimensions.INTERIOR))away(p,tick);
        }
        for(var entry:copies.entrySet())if(tick%10==0)motes(s,BlockPos.of(entry.getKey()),entry.getValue());
        for(var it=PRESENT.entrySet().iterator();it.hasNext();){var t=it.next();if(!copies.containsKey(t.getKey())){reset(s,BlockPos.of(t.getKey()));it.remove();}}
        for(var entry:copies.entrySet()){var ids=new HashSet<UUID>();for(var p:entry.getValue())ids.add(p.getUUID());PRESENT.put(entry.getKey(),ids);}
    }
    /** One reader, every five ticks: their visit's triggers, their shots' clock, and every sightline to look back down. */
    static boolean reader(ServerPlayer p,BlockPos b,CompoundTag own){
        var l=p.serverLevel();long now=l.getGameTime();var r=rel(p,b);int visit=own.getInt("Visit");boolean open=own.getInt("Beat")<visit&&visit>=1&&visit<=4;
        boolean dirty=false;
        if(open){
            switch(visit){
                case 1->{if(find(own,"creek")==null&&r.z>=PolaroidSite.CREEK_Z0&&r.z<PolaroidSite.CREEK_Z1+1&&r.y<0){arm(p,own,"creek",10);dirty=true;}}
                case 2->{if(crawling(p,b)){own.putInt("CrawlTicks204",own.getInt("CrawlTicks204")+5);dirty=true;}
                    if(find(own,"bowl")==null&&own.getInt("CrawlTicks204")>=60){arm(p,own,"bowl",20);dirty=true;}}
                case 3->dirty|=cinemaVisit(p,b,own,r,now);
                case 4->dirty|=pinesVisit(p,b,own,r,now);
                default->{}
            }
        }
        // The camera's clock: the glint while it charges, the flash, the photograph half a minute later.
        var list=shots(own);
        for(int i=0;i<list.size();i++){var t=list.getCompound(i);var s=shot(t.getString("Id"));if(s==null)continue;
            switch(t.getInt("State")){
                case 0->{var node=at(b,s.node());
                    if(now>=t.getLong("At")-CHARGE&&now<t.getLong("At")){for(var q:scene(p.server,b))l.sendParticles(q,AMBER,true,node.x,node.y,node.z,2,.05,.05,.05,0);
                        if(!t.getBoolean("Charged")){t.putBoolean("Charged",true);sound(l,node,PolaroidRegistry.CHARGE.get(),.9F,1F);dirty=true;}}
                    else if(now>=t.getLong("At")){if(!t.getBoolean("Charged")){t.putLong("At",now+CHARGE);t.putBoolean("Charged",true);sound(l,node,PolaroidRegistry.CHARGE.get(),.9F,1F);}
                        // He waits until he can see the reader: a flash through a fence or a pier would leave a sightline nobody could look back down.
                        else if(!sees(l,node,p)){t.putLong("At",now+20);t.putBoolean("Charged",false);}
                        else{flash(p,b,own,t,s);if(!inside(p))return true;}dirty=true;
                        if(find(own,s.id())==null)return true;}
                }
                case 1->{if(now>=t.getLong("At")){drop(p,b,t,s);dirty=true;}}
                case 3->{if(s.id().equals("creek")||s.id().equals("bowl")||s.id().equals("clothes")){var spot=PolaroidSite.at(b,t.getDouble("SX"),t.getDouble("SY"),t.getDouble("SZ"));
                    for(var q:scene(p.server,b))if(Math.hypot(q.getX()-spot.x,q.getZ()-spot.z)<=SPOT&&Math.abs(q.getY()-spot.y)<=1.2&&looking(q,at(b,s.node()),LOOK)){aligned(p,q,b,own,t,s);dirty=true;break;}}}
                default->{}
            }
            if(i<shots(own).size())put(own,t);
        }
        return dirty;
    }
    static boolean cinemaVisit(ServerPlayer p,BlockPos b,CompoundTag own,Vec3 r,long now){
        var l=p.serverLevel();boolean dirty=false;
        boolean hall=r.y>=2.5&&r.z<=PolaroidSite.HALL_Z&&r.z>=PolaroidSite.CINEMA_Z0&&Math.abs(r.x)<=14;
        if(hall&&!own.getBoolean("Projector204")){own.putBoolean("Projector204",true);own.putInt("Sil204",0);cinema(l,b,true);sit(l,b,0,0);
            sound(l,PolaroidSite.at(b,.5,9.5,-70.5),PolaroidRegistry.PROJECTOR.get(),1F,1F);josh(p,own,"cinema",null,null);cue(p,"The projector clatters on behind you. Someone is sitting alone in the back row.");dirty=true;}
        if(!own.getBoolean("Projector204"))return dirty;
        if(now%40==0)sound(l,PolaroidSite.at(b,.5,9.5,-70.5),PolaroidRegistry.PROJECTOR.get(),.5F,1F);
        boolean front=hall&&r.z<=PolaroidSite.FRONT_Z;
        int fired=0;for(String id:List.of("c1","c2","c3","c4"))if(state(own,id)>=1)fired++;
        if(front&&find(own,"c1")==null){arm(p,own,"c1",40);dirty=true;}
        else if(fired>=1&&fired<4&&find(own,"c"+(fired+1))==null&&now-own.getLong("LastFlash204")>=200&&front){arm(p,own,"c"+(fired+1),20);dirty=true;}
        // The one behind moves only while the reader is not looking at them: a few rows nearer after each flash; behind them at the last.
        int sil=own.getInt("Sil204");int want=SITTER_ROWS[Math.min(4,fired)];
        boolean allTaken=true;for(String id:List.of("c1","c2","c3","c4"))if(state(own,id)<3)allTaken=false;
        if(fired==4&&!allTaken)want=SITTER_ROWS[3];
        if(want!=sil&&!looking(p,sitter(b,sil).add(0,1.1,0),.8)){sit(l,b,sil,want);own.putInt("Sil204",want);sil=want;dirty=true;}
        if(allTaken&&sil==SITTER_ROWS[4]&&looking(p,sitter(b,sil).add(0,1.1,0),LOOK)){
            // Turning round to face them: the power goes, and the fire exit opens.
            cinema(l,b,false);stage(l,b.offset(PolaroidSite.EXIT_SIGN),0);
            for(int i=0;i<PolaroidSite.ROWS.length();i++){stage(l,b.offset(-1,PolaroidSite.PLATEAU,PolaroidSite.rowZ(i)),1);stage(l,b.offset(1,PolaroidSite.PLATEAU,PolaroidSite.rowZ(i)),1);}
            stage(l,b.offset(PolaroidSite.SITTER_X,PolaroidSite.PLATEAU,PolaroidSite.rowZ(sil)),0);
            own.putBoolean("Projector204",false);own.putInt("Gates204",own.getInt("Gates204")|FIRE_EXIT);gates(l,b,openFor(p.server,b)|own.getInt("Gates204"));finishVisit(own,3);
            sound(l,p.position(),PolaroidRegistry.POWER.get(),1F,1F);
            josh(p,own,"power",p.position().add(0,0,1),PolaroidSite.at(b,15,4,-97));cue(p,"You turn round. The seat behind you is empty, and the power goes.");dirty=true;
        }
        return dirty;
    }
    static boolean pinesVisit(ServerPlayer p,BlockPos b,CompoundTag own,Vec3 r,long now){
        var l=p.serverLevel();boolean dirty=false;
        if(r.z>PolaroidSite.PINES_Z||r.y<0)return false;
        if(find(own,"p1")==null){arm(p,own,"p1",20);josh(p,own,"pines",null,null);dirty=true;}
        for(int n=2;n<=3;n++)if(find(own,"p"+n)==null&&state(own,"p"+(n-1))>=1&&now-own.getLong("LastFlash204")>=120){arm(p,own,"p"+n,10);dirty=true;break;}
        if(state(own,"p3")>=1&&find(own,"p4")==null){josh(p,own,"trench",null,null);if(trenchA(r)){arm(p,own,"p4",10);dirty=true;}}
        if(state(own,"p4")>=3&&trenchA(r)&&p.getXRot()<-80&&!own.getBoolean("Lit204")){
            // Looking straight up from the trench: the beam goes up into the branches, and fifty balloons light.
            own.putBoolean("Lit204",true);
            var eye=p.getEyePosition();for(double y=0;y<16;y+=.4)l.sendParticles(MOTE,eye.x,eye.y+y,eye.z,1,.04,.04,.04,0);
            for(var at:PolaroidSite.canopyBalloons()){var pos=b.offset(at);if(l.getBlockState(pos).isAir())l.setBlock(pos,PolaroidBlock.of(PolaroidBlock.Kind.BALLOON,Direction.NORTH,1),F);}
            sound(l,eye,PolaroidRegistry.CLICK.get(),1F,.6F);sound(l,eye,PolaroidRegistry.BALLOON.get(),1F,.7F);
            finishVisit(own,4);LiteraryVignettes.ready(p,PLACE,own,"looked_up_from_the_trench");
            LiteraryVignettes.reward(p,PLACE,own,"Walkie",new ItemStack(PolaroidRegistry.WALKIE.get()));
            josh(p,own,"end",p.position(),PolaroidSite.at(b,16.5,9,-111.5));cue(p,"Nobody is in the branches. Only balloons, all lit, as far as you can see.");dirty=true;
        }
        return dirty;
    }
    static boolean trenchA(Vec3 r){return r.x>=PolaroidSite.TRENCH_A_X0&&r.x<PolaroidSite.TRENCH_A_X1+1&&r.z>=PolaroidSite.TRENCH_Z0&&r.z<PolaroidSite.TRENCH_Z1+1&&r.y<2;}
    /** Down on hands and knees under the house next door, and in the last step of the scramble up to it. */
    public static boolean crawling(ServerPlayer p,BlockPos b){
        // From the top step of the scramble (two blocks high) the reader goes down onto hands and knees for the vent.
        var r=rel(p,b);return r.y>=1.9&&r.y<4&&r.z<=-39.3&&r.z>=PolaroidSite.CRAWL_Z0&&r.x>=PolaroidSite.CRAWL_X0&&r.x<PolaroidSite.CRAWL_X1+1;
    }
    /** Dust drifting in the projector's beam. */
    static void motes(MinecraftServer s,BlockPos b,List<ServerPlayer> readers){
        boolean on=false;for(var p:readers)on|=own(p).getBoolean("Projector204");if(!on)return;
        var l=readers.get(0).serverLevel();var from=PolaroidSite.at(b,.5,9.5,-71);var to=PolaroidSite.at(b,.5,8,-100.5);var rnd=l.getRandom();
        for(int i=0;i<6;i++){double k=rnd.nextDouble();var at=from.add(to.subtract(from).scale(k));for(var p:readers)l.sendParticles(p,MOTE,true,at.x,at.y,at.z,1,.4*k,.3*k,.2,0);}
    }
    /** An empty copy is set for its next reader: no photographs, no balloons, everything shut, the projector off. */
    static void reset(MinecraftServer s,BlockPos b){
        var l=s.getLevel(NovelRooms.dimension(PLACE));if(l==null||!l.isLoaded(b))return;
        for(var shot:SHOTS)clearDrop(l,b,shot);
        gates(l,b,0);cinema(l,b,false);
        for(var at:PolaroidSite.canopyBalloons()){var pos=b.offset(at);if(PolaroidBlock.is(l.getBlockState(pos),PolaroidBlock.Kind.BALLOON))l.setBlock(pos,Blocks.AIR.defaultBlockState(),F);}
    }
    /** Out in the House, until they finish: now and then a photograph of them from behind is in their pocket. */
    static void away(ServerPlayer p,long tick){
        var own=own(p);if(!own.getBoolean("Haunted204")||own.getBoolean("Completed"))return;
        var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null)return;
        var place=LabyrinthPlaces.placeAt(origin,p.blockPosition());if(place==null||!LabyrinthPacing.ordinary(place))return;
        var h=HALL.get(p.getUUID());long key=place.ordinal();
        // About one ordinary hall in four is one where it happens, and then only once, after the reader has been in it a while.
        if(h==null||h[0]!=key){HALL.put(p.getUUID(),new long[]{key,tick,p.getRandom().nextInt(4)==0?0:1});return;}
        if(h[2]!=0||tick-h[1]<200||p.getRandom().nextInt(6)!=0)return;
        h[2]=1;
        LiteraryVignettes.give(p,photo(HouseLens.fromBehind(p.serverLevel(),p,FW,FH),"hall",p.getGameProfile().getName()));
        HousePackets.send(p,new PolaroidFlashPayload(6,.6F));p.serverLevel().playSound(null,p.blockPosition(),PolaroidRegistry.CLICK.get(),SoundSource.AMBIENT,.5F,1F);
        cue(p,"Somewhere behind you, a click. There is a photograph in your pocket that you did not put there.");
    }
    public static void clearAll(){ECHO.clear();PRESENT.clear();HALL.clear();}
}
