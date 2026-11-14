package io.github.knaitoe.theoldesthouse.client;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.HazardBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.HazardMaps;
import io.github.knaitoe.theoldesthouse.labyrinth.HazardRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlace;
import io.github.knaitoe.theoldesthouse.network.HazardViewPayload;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.common.util.TriState;

/**
 * 0.4.592, the owner's playtest: "reflection isn't rendered properly in reflection room". The mirror hall's wall was panes of
 * framed glass with a second hall built behind it and a figure of the reader drawn in there. It read as a window into another
 * room. It is one true plane mirror now, the same glass as the quiet rooms' and the trial room's ({@link HouseReadReflection}):
 * the whole wall, wall to wall and floor to ceiling, showing the hall in front of it.
 *
 * <p>What the hall does is what it always did, in that glass. The reader in it is the reader as they were a beat ago, then two:
 * this client keeps what its own reader did, tick by tick, and the glass shows a body of theirs doing it late, drawn by the
 * player's own renderer, so it wears, holds and moves as they did. Past the hall's last stretch it stands at their door instead.
 * Nobody else is in the glass, as before: each reader sees only their own reflection. The server says only how late and whether
 * it is at the door ({@link HazardViewPayload}); nothing reflected is spawned, and no other client is told anything.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class MirrorHallGlass implements BlockEntityRenderer<HazardBlock.MirrorWall> {
    /** The glass stands this far proud of its blocks, so they never show through it at the far end of the hall. */
    static final float PROUD=.02F;
    /** Ticks of the reader kept: the hall asks for forty at most. */
    static final int KEPT=96;
    private static final EquipmentSlot[] SLOTS={EquipmentSlot.MAINHAND,EquipmentSlot.OFFHAND,EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
    /** What the reader was doing at the end of one tick. */
    private record Frame(double x,double y,double z,float yaw,float pitch,float body,float head,float walk,float attack,Pose pose,int age,ItemStack[] worn){}
    private static final Frame[] RING=new Frame[KEPT];
    private static int next,kept;
    @Nullable private static Late late;
    private static boolean placed,answering;
    /** The coat's own parts, for a wide and a narrow body. */
    @Nullable private static ModelPart standard,slim;
    private static float behind;

    private static final HouseReadReflection.Hook HOOK=new HouseReadReflection.Hook(){
        /** The reader's own reflection is the reader a beat ago, once the hall has them; nobody else has one. */
        @Override public @Nullable AbstractClientPlayer stand(AbstractClientPlayer person){
            if(person!=Minecraft.getInstance().player)return null;return late!=null&&placed?late:person;
        }
        /**
         * 0.4.159, kept: the hazard halls' mirrors show the House's version of the reader, the coat it read for them as well as
         * the glasses and the hidden face every glass shows. The coat is worn only in glass and on copies, never on the live body.
         */
        @Override public void face(PlayerModel<AbstractClientPlayer> model,AbstractClientPlayer player,PoseStack pose,MultiBufferSource out,int light){
            String coat=HouseReadLookClient.look().coat();if(coat==null||coat.isEmpty())return;
            if(standard==null){var models=Minecraft.getInstance().getEntityModels();standard=models.bakeLayer(HouseReadClothes.STANDARD);slim=models.bakeLayer(HouseReadClothes.SLIM);}
            HouseReadClothes.coat(model,player.getSkin().model()==PlayerSkin.Model.SLIM?slim:standard,coat,pose,out,light);
        }
    };

    public MirrorHallGlass(BlockEntityRendererProvider.Context c){}

    /** The wall's glass, from the block that carries it: its lowest block at the entrance end. */
    static HouseReadReflection.Glass glass(BlockPos anchor){
        var room=LabyrinthPlace.MIRROR_HALL.room();
        return new HouseReadReflection.Glass(anchor,Direction.EAST,0,-1-room.minZ(),0,room.maxY(),1+PROUD,HOOK,room.maxX()-HazardMaps.MIRROR_X);
    }
    @Override public void render(HazardBlock.MirrorWall be,float partial,PoseStack pose,MultiBufferSource out,int light,int overlay){
        if(!HazardBlock.mirrorAnchor(be.getBlockState()))return;
        var glass=glass(be.getBlockPos());HouseReadReflection.want(glass);
        if(HouseReadReflection.showing(glass.at()))HouseReadReflection.drawGlass(glass,pose,out);
    }
    /** The wall is forty blocks long and hangs from one end: it is drawn while any of it is in view, whether or not that end is. */
    @Override public boolean shouldRenderOffScreen(HazardBlock.MirrorWall be){return true;}
    @Override public net.minecraft.world.phys.AABB getRenderBoundingBox(HazardBlock.MirrorWall be){return HazardBlock.mirrorAnchor(be.getBlockState())?glass(be.getBlockPos()).box():new net.minecraft.world.phys.AABB(be.getBlockPos());}
    @Override public int getViewDistance(){return 96;}

    /**
     * Every client tick: what the reader did is kept, in the hall or out of it, so the glass has their last moments from the step
     * they come in on; and while the hall has them ({@code hall} is what it last sent), the reader in the glass is moved on.
     */
    static void tick(@Nullable HazardViewPayload hall){
        var mc=Minecraft.getInstance();var p=mc.player;
        if(p==null||mc.level==null||!mc.level.dimension().equals(HouseDimensions.INTERIOR)){forget();return;}
        if(mc.isPaused())return;
        var last=kept>0?RING[Math.floorMod(next-1,KEPT)]:null;var worn=new ItemStack[SLOTS.length];boolean same=last!=null;
        for(int i=0;i<SLOTS.length;i++){var now=p.getItemBySlot(SLOTS[i]);worn[i]=last!=null&&ItemStack.matches(last.worn()[i],now)?last.worn()[i]:now.copy();same&=last!=null&&worn[i]==last.worn()[i];}
        RING[next]=new Frame(p.getX(),p.getY(),p.getZ(),p.getYRot(),p.getXRot(),p.yBodyRot,p.yHeadRot,p.walkAnimation.speed(),p.attackAnim,p.getPose(),p.tickCount,same?last.worn():worn);
        next=(next+1)%KEPT;kept=Math.min(KEPT,kept+1);
        if(hall==null){leave();return;}
        if(late==null||late.level()!=mc.level){late=new Late(mc.level,p.getGameProfile());placed=false;behind=0;}
        late.wear(p);var view=hall.view();
        // Falling behind, the reader in the glass moves at half the reader's pace; it never skips ahead to catch up.
        int asked=view.contains("Delay")?view.getInt("Delay"):20;behind=behind<asked?Math.min(asked,behind+.5F):asked;
        boolean answer=view.getBoolean("Answer");
        // At the door: in front of the one that is really theirs, where the hall says it is.
        if(answer)late.stand(view.getDouble("X"),view.getDouble("Y"),view.getDouble("Z"),!placed||!answering);
        else late.take(RING[Math.floorMod(next-1-Math.min(kept-1,(int)behind),KEPT)],!placed||answering);
        placed=true;answering=answer;
    }
    /** How many ticks behind its reader the glass is showing them, or -1 while it shows them as they are. */
    static float behind(){return late!=null&&placed&&!answering?behind:-1;}
    /** Where the reader in the glass stands, or null while the glass shows the reader as they are. */
    static @Nullable net.minecraft.world.phys.Vec3 shown(){return late!=null&&placed?late.position():null;}
    /** Whether the glass shows its reader standing at their door. */
    static boolean atDoor(){return late!=null&&placed&&answering;}
    private static void leave(){late=null;placed=false;answering=false;behind=0;}
    private static void forget(){leave();if(kept>0){java.util.Arrays.fill(RING,null);kept=0;next=0;}}

    /** The reader in the glass is nobody to name. */
    @SubscribeEvent public static void nameTag(RenderNameTagEvent e){if(e.getEntity() instanceof Late)e.setCanRender(TriState.FALSE);}

    /** The reader as they were: a body of theirs that is in no world and on no server, drawn only in the hall's glass. */
    private static final class Late extends RemotePlayer {
        private ItemStack[] wearing;
        Late(ClientLevel level,GameProfile profile){super(level,profile);}
        /** What does not change from tick to tick: the parts of the skin shown, the hand used, the reader's size. */
        void wear(AbstractClientPlayer reader){
            int parts=0;for(var part:PlayerModelPart.values())if(reader.isModelPartShown(part))parts|=part.getMask();
            entityData.set(DATA_PLAYER_MODE_CUSTOMISATION,(byte)parts);setMainArm(reader.getMainArm());
            var scale=getAttribute(Attributes.SCALE);if(scale!=null)scale.setBaseValue(reader.getAttributeValue(Attributes.SCALE));
        }
        void take(Frame f,boolean cut){
            step();setPos(f.x(),f.y(),f.z());setYRot(f.yaw());setXRot(f.pitch());yBodyRot=f.body();yHeadRot=f.head();attackAnim=f.attack();tickCount=f.age();
            if(getPose()!=f.pose())setPose(f.pose());walkAnimation.update(f.walk(),1F);
            if(wearing!=f.worn()){wearing=f.worn();for(int i=0;i<SLOTS.length;i++)setItemSlot(SLOTS[i],f.worn()[i]);}
            if(cut)step();
        }
        /** Standing, its back to the door and its face down the hall. */
        void stand(double x,double y,double z,boolean cut){
            step();setPos(x,y,z);setYRot(0);setXRot(0);yBodyRot=0;yHeadRot=0;attackAnim=0;tickCount++;
            if(getPose()!=Pose.STANDING)setPose(Pose.STANDING);walkAnimation.update(0,1F);
            if(cut)step();
        }
        /** This tick's place and turn become the last tick's, as a ticking body's do. */
        private void step(){xo=xOld=getX();yo=yOld=getY();zo=zOld=getZ();yRotO=getYRot();xRotO=getXRot();yBodyRotO=yBodyRot;yHeadRotO=yHeadRot;oAttackAnim=attackAnim;}
    }

    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(HazardRegistry.MIRROR_WALL.get(),MirrorHallGlass::new);}
    }
}
