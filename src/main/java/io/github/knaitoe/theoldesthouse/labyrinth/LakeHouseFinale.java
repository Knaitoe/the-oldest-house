package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.phys.Vec3;

/**
 * The fourth visit: saying hello.
 * <ol>
 * <li>Down in the basement the sauna's door shuts on its own (for that reader), and somebody walks upstairs.</li>
 * <li>Back on the ground floor, every lamp in the house comes on for that reader alone: private lit lamps, and a hidden
 * night-vision effect that is what actually lights the rooms for them. The still life in the lounge is a face. In the
 * dressing room a faceless wax figure stands in the black dress, seen by that reader alone.</li>
 * <li>What they do next is their ending: touch the figure (<b>said hello</b>), leave by the half door with the house lit
 * (<b>left the lights on</b>), or sit in a dining chair and stay seated while the lights go down one by one (<b>stayed a
 * little longer</b>).</li>
 * </ol>
 * Afterwards the house stays lit for a reader who left the lights on, stays dark with their place set for one who stayed,
 * and is gone for one who said hello: every later time they go through the culvert they come out on an empty third lake.
 */
public final class LakeHouseFinale {
    /** Stranger phases for the lake house's cast: Tess, the wax figure, and the five dresses. */
    public static final int TESS_PHASE=14,FIGURE_PHASE=15,DRESS_PHASE=16;
    public static final String[] DRESSES={"yellow","red","grey","mauve","black"};
    /** Ticks seated in a dining chair with the house lit before the reader stays. */
    public static final int STAY=200;
    /** Where the empty lake lies: an identical third lake, with no house in it, this far east. */
    public static final int EMPTY_DX=200;
    static final BlockPos FIGURE=new BlockPos(0,LakeHouse.UPPER+1,-163),PLACE_SET=new BlockPos(-7,LakeHouseRooms.G+1,-165);
    static final ResourceKey<PaintingVariant> FACE=ResourceKey.create(Registries.PAINTING_VARIANT,ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"lake_house_face"));
    private static @Nullable EntityDataAccessor<Holder<PaintingVariant>> variant;
    private LakeHouseFinale(){}

    /** Whether this visit is the reader's fourth, the one where they go back to say hello. */
    static boolean finale(CompoundTag own){return own.getInt("Visit")>=4&&own.getInt("Beat")>=3&&!own.getBoolean("Ready");}

    /** Every five ticks for every reader: the finale's stages, and what each ending leaves behind afterwards. */
    static void reader(ServerPlayer p,BlockPos b,CompoundTag own,Vec3 r){
        boolean inside=LakeHouseDive.inHouse(r);
        String outcome=own.getString("Outcome");
        if(own.getBoolean("Ready")){
            // What stays: the lit house for a reader who left it lit, their place at the table for one who stayed.
            if(outcome.equals(LakeHouse.LEFT_THE_LIGHTS_ON))lit(p,b,own,inside);
            if(outcome.equals(LakeHouse.STAYED)&&inside&&own.getInt("T")%100==5)placeSet(p,b);
            return;
        }
        if(!finale(own))return;
        int stage=own.getInt("Finale"),t=own.getInt("T");
        if(stage==0&&LakeHouseRules.basement(r)){
            // The sauna, the one room nobody looked into, shuts. It is hot in there and does nothing else.
            own.putInt("Finale",1);own.putInt("SaunaShut",t);own.putInt("StepsUntil",t+160);
            for(int y=0;y<=1;y++)p.connection.send(new ClientboundBlockUpdatePacket(b.offset(4,LakeHouseRooms.BASEMENT+y,-165),Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState()));
            LakeHouseRules.hear(p,SoundEvents.WOODEN_DOOR_CLOSE,b.offset(4,LakeHouseRooms.BASEMENT,-165),1F,.7F);
            return;
        }
        if(stage>=1&&own.contains("SaunaShut")&&t-own.getInt("SaunaShut")>=160){
            for(int y=0;y<=1;y++){var at=b.offset(4,LakeHouseRooms.BASEMENT+y,-165);p.connection.send(new ClientboundBlockUpdatePacket(p.serverLevel(),at));}
            own.remove("SaunaShut");
        }
        if(stage==1&&inside&&r.y<LakeHouse.UPPER){
            own.putInt("Finale",2);own.putInt("StaySeated",0);
            lights(p,b,LakeHouseRooms.LAMPS.size());nightVision(p,true);face(p,b,true);
            var figure=figure(p);if(figure!=null){figure.appearance(LiteraryActor.STRANGER,FIGURE_PHASE);figure.setNoGravity(true);figure.moveTo(Vec3.atBottomCenterOf(b.offset(FIGURE)));figure.setYRot(180);figure.setYHeadRot(180);}
            p.displayClientMessage(Component.literal("Every light in the house comes on at once.").withStyle(ChatFormatting.ITALIC),false);
            own.putBoolean("WasInside",true);
            return;
        }
        if(stage!=2)return;
        // Lit, the house keeps lit for them while they are in it.
        if(inside){nightVision(p,true);if(t%100==5)lights(p,b,litCount(own));}
        // Out through the half door with the lights on: they leave the house lit.
        if(own.getBoolean("WasInside")&&!inside&&r.z>LakeHouse.HZ_FRONT&&r.z<LakeHouse.HZ_FRONT+6&&Math.abs(r.x)<=3&&r.y<LakeHouse.UPPER){end(p,b,own,LakeHouse.LEFT_THE_LIGHTS_ON);return;}
        own.putBoolean("WasInside",inside);
        // Seated at the table while the lights go down one by one.
        if(LakeHouseRules.dryChair(p)){
            int seated=own.getInt("StaySeated")+5;own.putInt("StaySeated",seated);
            if(seated>=STAY/2)lights(p,b,litCount(own));
            if(seated>=STAY)end(p,b,own,LakeHouse.STAYED);
        }else if(own.getInt("StaySeated")>0){own.putInt("StaySeated",0);lights(p,b,LakeHouseRooms.LAMPS.size());}
    }
    /** How many lamps are still lit: all of them, until a seated reader has sat half the time, then one fewer every few ticks. */
    static int litCount(CompoundTag own){
        int seated=own.getInt("StaySeated"),n=LakeHouseRooms.LAMPS.size();
        if(seated<STAY/2)return n;return Math.max(0,n-(seated-STAY/2)*n/(STAY/2));
    }
    /** Touching the figure while the house is lit. */
    static boolean touch(ServerPlayer p,LiteraryActor actor,CompoundTag own){
        if(actor.phase()!=FIGURE_PHASE||!finale(own)||own.getInt("Finale")!=2)return false;
        var b=LakeHouse.base(p.server);if(b==null)return false;
        p.displayClientMessage(Component.literal("You put your hand on its shoulder and say your name.").withStyle(ChatFormatting.ITALIC),false);
        end(p,b,own,LakeHouse.SAID_HELLO);return true;
    }

    /** An ending: the account is ready, and the house is left as that ending leaves it. */
    static void end(ServerPlayer p,BlockPos b,CompoundTag own,String outcome){
        var figure=figure(p);if(figure!=null)figure.discard();forgetFigure(p);
        own.putInt("Finale",3);own.remove("StaySeated");
        switch(outcome){
            case LakeHouse.SAID_HELLO->{
                lights(p,b,0);nightVision(p,false);face(p,b,false);
                LakeHouseDive.blackout(p,Vec3.atBottomCenterOf(b.offset(EMPTY_DX,0,-170)).add(0,-.6,0),"You come up into an empty lake. There is nothing under you but water.");
                LiteraryVignettes.reward(p,LakeHouse.PLACE,own,"FoundYou",new ItemStack(LakeHouseRegistry.FOUND_FLASHLIGHT.get()));
            }
            case LakeHouse.STAYED->{
                lights(p,b,0);nightVision(p,false);face(p,b,false);p.stopRiding();
                LakeHouseDive.blackout(p,Vec3.atBottomCenterOf(b.offset(LakeHouse.RAFT.offset(1,1,0))),"You come to on the raft. It is only just getting light.");
                placeSet(p,b);
                LiteraryVignettes.reward(p,LakeHouse.PLACE,own,"GoldHelmet",new ItemStack(LakeHouseRegistry.GOLD_HELMET.get()));
            }
            default->{
                p.displayClientMessage(Component.literal("Behind you, every window is lit.").withStyle(ChatFormatting.ITALIC),false);
                LiteraryVignettes.reward(p,LakeHouse.PLACE,own,"HouseLight",new ItemStack(LakeHouseRegistry.HOUSE_LIGHT.get()));
            }
        }
        LiteraryVignettes.ready(p,LakeHouse.PLACE,own,outcome);
    }

    // ------------------------------------------------------------------------------------------------ the lights
    /** The first n lamps lit and the rest dark, for this reader alone. */
    static void lights(ServerPlayer p,BlockPos b,int n){
        var lamps=LakeHouseRooms.LAMPS;
        for(int i=0;i<lamps.size();i++)p.connection.send(new ClientboundBlockUpdatePacket(b.offset(lamps.get(i)),Blocks.REDSTONE_LAMP.defaultBlockState().setValue(RedstoneLampBlock.LIT,i<n)));
    }
    /** The light itself: a night vision with no particles and no icon, so the rooms are lit for them whatever their client relights. */
    static void nightVision(ServerPlayer p,boolean on){
        var e=p.getEffect(MobEffects.NIGHT_VISION);boolean ours=e!=null&&e.isAmbient()&&!e.showIcon();
        if(on){if(e==null||ours&&e.getDuration()<300)p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,400,0,true,false,false));}
        else if(ours)p.removeEffect(MobEffects.NIGHT_VISION);
    }
    /** After leaving the lights on: the house is lit for them every time they are in it, and dark the moment they leave it. */
    private static void lit(ServerPlayer p,BlockPos b,CompoundTag own,boolean inside){
        boolean was=own.getBoolean("LitInside");
        if(inside){nightVision(p,true);if(!was||own.getInt("T")%100==5)lights(p,b,LakeHouseRooms.LAMPS.size());}
        else if(was){nightVision(p,false);lights(p,b,0);}
        own.putBoolean("LitInside",inside);
    }
    /** The still life in the lounge, as this reader sees it: a still life, or a face. */
    static void face(ServerPlayer p,BlockPos b,boolean face){
        var l=p.serverLevel();
        var paintings=l.getEntitiesOfClass(Painting.class,LakeHouse.box(b),e->e.getTags().contains(LakeHouseDisplay.TAG));
        if(paintings.isEmpty())return;
        try{
            if(variant==null){var f=Painting.class.getDeclaredField("DATA_PAINTING_VARIANT_ID");f.setAccessible(true);
                @SuppressWarnings("unchecked") var accessor=(EntityDataAccessor<Holder<PaintingVariant>>)f.get(null);variant=accessor;}
            var registry=l.registryAccess().registryOrThrow(Registries.PAINTING_VARIANT);
            var holder=registry.getHolder(face?FACE:LakeHouseRooms.STILL_LIFE_VARIANT);if(holder.isEmpty())return;
            for(var painting:paintings)p.connection.send(new ClientboundSetEntityDataPacket(painting.getId(),List.of(SynchedEntityData.DataValue.create(variant,holder.get()))));
        }catch(ReflectiveOperationException|RuntimeException ignored){
            // Without the field the still life stays a still life; the lights and the figure carry the moment.
        }
    }

    // ------------------------------------------------------------------------------------------------ the figure and the place at the table
    private static @Nullable LiteraryActor figure(ServerPlayer p){return LiteraryVignettes.actor(p,LakeHouse.PLACE,"Figure",LiteraryActor.STRANGER,FIGURE,true);}
    private static void forgetFigure(ServerPlayer p){
        var d=LabyrinthData.get(p.server);var s=LiteraryVignettes.shared(d,LakeHouse.PLACE);s.remove("Figure_"+p.getUUID());LiteraryVignettes.shared(d,LakeHouse.PLACE,s);
    }
    /** A plate laid at their place at the table, for the reader who stayed: seen by them alone, on every visit after. */
    private static void placeSet(ServerPlayer p,BlockPos b){
        var l=p.serverLevel();var at=b.offset(PLACE_SET);
        if(!l.getEntitiesOfClass(LakeHouseDisplay.class,new net.minecraft.world.phys.AABB(at).inflate(.5),d->p.getUUID().equals(d.owner())).isEmpty())return;
        var plate=LakeHouseDisplay.show(l,at,Blocks.WHITE_CARPET.defaultBlockState(),p);
        if(plate!=null)plate.pose(new com.mojang.math.Transformation(new org.joml.Matrix4f().translate(.2F,0,.2F).scale(.6F,1,.6F)),0);
    }

    // ------------------------------------------------------------------------------------------------ the empty lake
    /**
     * For a reader who said hello: going north through the culvert, they come out on the empty lake instead, and coming back
     * south through its culvert they are on the clear lake again. The two culverts are the same, so nothing shows the change.
     */
    static void emptyLake(ServerPlayer p,BlockPos b,CompoundTag own,Vec3 r){
        if(!own.getBoolean("Ready")||!own.getString("Outcome").equals(LakeHouse.SAID_HELLO))return;
        boolean empty=r.x>EMPTY_DX/2.0;double lx=empty?r.x-EMPTY_DX:r.x;
        if(lx<-2||lx>3)return;
        if(!empty&&r.z<-112)cross(p,EMPTY_DX);else if(empty&&r.z>-112)cross(p,-EMPTY_DX);
    }
    private static void cross(ServerPlayer p,int dx){
        var vehicle=p.getVehicle();
        if(vehicle!=null){vehicle.teleportTo(vehicle.getX()+dx,vehicle.getY(),vehicle.getZ());p.connection.send(new net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket(vehicle));}
        else p.teleportTo(p.serverLevel(),p.getX()+dx,p.getY(),p.getZ(),p.getYRot(),p.getXRot());
    }
    /** Builds the empty lake: the same land, lakes, culvert and woods, and no house. */
    static void buildEmpty(ServerLevel l,BlockPos b){LakeHouse.landscape(new LakeHouse.Site(l,b.offset(EMPTY_DX,0,0)));}
}
