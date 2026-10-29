package io.github.knaitoe.theoldesthouse.client;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
/** Tailored, posed human cast, antlerless cow elk and bounded native fan/miniature renderers. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class LiteraryRenderers {
    public static final ModelLayerLocation CAST=layer("literary_cast"),ELK=layer("literary_elk"),ELK_EYES=layer("literary_elk_eyes"),CRUTCH=layer("rabbit_crutch"),FAN=layer("literary_fan");
    private static ModelLayerLocation layer(String name){return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,name),"main");}
    private static ResourceLocation texture(String name){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/literary_"+name+".png");}
    public static ResourceLocation skin(int role,int phase){if(HouseReadShelter.role(role))return texture("read_"+(role==LiteraryActor.READ_MAN?"man":"woman")+(phase==1?"_smile":""));if(role==LiteraryActor.LAKE_TESS)return texture("lake_tess");if(role==LiteraryActor.LAKE_THEO)return texture("lake_theo");if(role==LiteraryActor.LAKE_WAX)return texture("lake_wax");String[] roles={"mother","dancer","red_figure","coffin_woman","lady","silhouette","brother","father","visitor","killer","family_father","family_mother","family_child","old_man","stranger","camera"};String name=roles[Math.max(0,Math.min(15,role))];if(role==LiteraryActor.SILHOUETTE&&phase==4)return texture("plain_boy");if(role==LiteraryActor.SILHOUETTE&&phase==5)return texture("quiet_walker");if(role==LiteraryActor.STRANGER&&phase>=4&&phase<4+BAR.length)return texture("bar_"+BAR[phase-4]);if(role==LiteraryActor.STRANGER&&phase>=9&&phase<9+COTTAGE.length)return texture("paw_"+COTTAGE[phase-9]);if(role==LiteraryActor.STRANGER&&phase>=14&&phase<14+MOVIE.length)return texture("movie_"+MOVIE[phase-14]);if(role==LiteraryActor.STRANGER&&phase>=21&&phase<21+COSTUME.length)return texture("costume_"+COSTUME[phase-21]);if(role==LiteraryActor.STRANGER&&phase>=34&&phase<34+WINTER.length)return texture("winter_"+WINTER[phase-34]);if(role==LiteraryActor.STRANGER)name+="_"+Math.min(3,phase);else if(role==LiteraryActor.SILHOUETTE&&phase>0)name+="_"+Math.min(3,phase);else if(role==LiteraryActor.FATHER&&phase>=2)name+=phase>=3?"_mangled":"_bandaged";else if(role>=LiteraryActor.FAMILY_FATHER&&role<=LiteraryActor.FAMILY_CHILD&&phase>0)name+="_changed";return texture(name);}
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(CAST,CastModel::layer);e.registerLayerDefinition(UncannyActorRenderer.BENT,UncannyActorRenderer::bentLayer);e.registerLayerDefinition(UncannyActorRenderer.WIDE,()->UncannyActorRenderer.layer(false));e.registerLayerDefinition(UncannyActorRenderer.SLIM,()->UncannyActorRenderer.layer(true));e.registerLayerDefinition(PlainFigureModel.LAYER,PlainFigureModel::layer);e.registerLayerDefinition(CRUTCH,CrutchLayer::layer);e.registerLayerDefinition(GhostsCastModel.LAYER,GhostsCastModel::layer);e.registerLayerDefinition(ELK,ElkModel::layer);e.registerLayerDefinition(ELK_EYES,ElkEyes::layer);e.registerLayerDefinition(FAN,ModelRenderer::fanLayer);}
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(net.minecraft.world.entity.EntityType.ITEM,ServedMealRenderer::new);e.registerEntityRenderer(LiteraryRegistry.ACTOR.get(),CastRenderer::new);e.registerEntityRenderer(LiteraryRegistry.ELK.get(),ElkRenderer::new);e.registerBlockEntityRenderer(LiteraryRegistry.MODEL.get(),ModelRenderer::new);}
    /** The Last Stop's own skins, on the existing stranger phases four through eight. */
    static final String[] BAR={"marla","russ","denny","wade","hollis"};
    /** Three Fingers (0.4.81): the cottage's people on stranger phases nine to thirteen, each in their own skin. */
    static final String[] COTTAGE={"walter","ruth","will","sergeant","messenger"};
    /** Movie Night (0.4.102): the projectionist, the ticket seller, the Pruitts and the two in the canoe, on stranger phases fourteen to twenty. */
    static final String[] MOVIE={"ray","dot","frank","june","jamie","bobby","lynn"};
    /** Costume Night (0.4.103): Hank, then the twelve entrants in number order, on stranger phases twenty-one to thirty-three. */
    static final String[] COSTUME={"hank","tina","danny","ron","kaylee","marcus","shelly","owen","brooke","wes","rosa","pip","lyle"};
    /** Winter Lake (0.4.104): Bud, Hank, Clay and Lonnie, the boys from the shallows, that winter, on stranger phases thirty-four to thirty-seven. */
    static final String[] WINTER={"bud","hank","clay","lonnie"};
    /** A seat an actor has been placed in: how high it holds the hips, and which way it faces. */
    record Seat(int seatHeight,net.minecraft.core.Direction facing){}
    /** The household, roadhouse or lawn-chair seat an actor has been placed in, if any. */
    static Seat seat(LiteraryActor a){
        var state=a.level().getBlockState(a.blockPosition());
        if(state.is(io.github.knaitoe.theoldesthouse.house.HouseBlocks.HOUSEHOLD_FURNITURE.get())){var kind=state.getValue(io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock.KIND);return kind.seat?new Seat(kind.seatHeight,state.getValue(io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock.FACING)):null;}
        if(state.is(io.github.knaitoe.theoldesthouse.labyrinth.LiteraryRegistry.ROADHOUSE.get())){var kind=state.getValue(io.github.knaitoe.theoldesthouse.labyrinth.RoadhouseBlock.KIND);return kind.seat()?new Seat(kind.seatHeight,state.getValue(io.github.knaitoe.theoldesthouse.labyrinth.RoadhouseBlock.FACING)):null;}
        if(io.github.knaitoe.theoldesthouse.labyrinth.MoviePropBlock.is(state,io.github.knaitoe.theoldesthouse.labyrinth.MoviePropBlock.Kind.LAWN_CHAIR))return new Seat(5,state.getValue(io.github.knaitoe.theoldesthouse.labyrinth.MoviePropBlock.FACING));
        return null;
    }
    public static final class CastModel extends HumanoidModel<LiteraryActor>{private final ModelPart coat,gown,mask,bandage,hood,lakeHair;
        private final LakeHouseHair lakeHairMotion;
        /** The elk killer's own tailoring (0.4.50) and the crew's hard hats; hidden on everyone else. */
        private final ModelPart killerHood,killerMask,killerCoat,killerBelt,sleeveRight,sleeveLeft,tailRight,tailLeft,bootRight,bootLeft,toeRight,toeLeft,axe,hardHat;
        private float walk,amount;
        public CastModel(ModelPart r){super(r);coat=body.getChild("coat");gown=body.getChild("gown");mask=head.getChild("mask");bandage=leftLeg.getChild("bandage");hood=head.getChild("hood");
            lakeHair=head.getChild("lake_hair");lakeHairMotion=new LakeHouseHair(lakeHair);killerHood=head.getChild("killer_hood");killerMask=head.getChild("killer_mask");hardHat=head.getChild("hard_hat");killerCoat=body.getChild("killer_coat");killerBelt=body.getChild("killer_belt");
            sleeveRight=rightArm.getChild("killer_sleeve");sleeveLeft=leftArm.getChild("killer_sleeve");axe=rightArm.getChild("killer_axe");
            tailRight=rightLeg.getChild("killer_tail");tailLeft=leftLeg.getChild("killer_tail");bootRight=rightLeg.getChild("killer_boot");bootLeft=leftLeg.getChild("killer_boot");toeRight=rightLeg.getChild("killer_toe");toeLeft=leftLeg.getChild("killer_toe");}
        public static LayerDefinition layer(){var m=HumanoidModel.createMesh(CubeDeformation.NONE,0);var r=m.getRoot();r.getChild("body").addOrReplaceChild("coat",CubeListBuilder.create().texOffs(64,28).addBox(-4.4F,1,-2.4F,8.8F,17,4.8F),PartPose.ZERO);r.getChild("body").addOrReplaceChild("gown",CubeListBuilder.create().texOffs(96,28).addBox(-5,9,-3,10,14,6,new CubeDeformation(.2F)),PartPose.ZERO);r.getChild("head").addOrReplaceChild("mask",CubeListBuilder.create().texOffs(64,0).addBox(-4.15F,-8.2F,-4.5F,8.3F,8.5F,1),PartPose.ZERO);r.getChild("head").addOrReplaceChild("hood",CubeListBuilder.create().texOffs(82,0).addBox(-4.4F,-8.4F,-4.4F,8.8F,8.8F,8.8F),PartPose.ZERO);r.getChild("left_leg").addOrReplaceChild("bandage",CubeListBuilder.create().texOffs(112,0).addBox(-2,5,-2,4,5,4,new CubeDeformation(.2F)),PartPose.ZERO);r.addOrReplaceChild("herd_eyes",CubeListBuilder.create().texOffs(0,0).addBox(-4,0,0,1,1,1).addBox(3,0,0,1,1,1),PartPose.ZERO);
            // Integer UV footprints with deformation, so every face of the killer's atlas is painted exactly.
            var head=r.getChild("head");var body=r.getChild("body");
            LakeHouseHair.layer(head);
            head.addOrReplaceChild("killer_hood",CubeListBuilder.create().texOffs(64,0).addBox(-4,-8,-4,8,8,8,new CubeDeformation(.62F)),PartPose.ZERO);
            head.addOrReplaceChild("killer_mask",CubeListBuilder.create().texOffs(96,0).addBox(-4,-8,-5,8,8,1,new CubeDeformation(.25F)),PartPose.ZERO);
            head.addOrReplaceChild("hard_hat",CubeListBuilder.create().texOffs(64,0).addBox(-4,-10,-4,8,3,8,new CubeDeformation(.35F)).texOffs(64,16).addBox(-5,-8,-5,10,1,10),PartPose.ZERO);
            body.addOrReplaceChild("killer_coat",CubeListBuilder.create().texOffs(64,16).addBox(-4,0,-2,8,12,4,new CubeDeformation(.42F)),PartPose.ZERO);
            body.addOrReplaceChild("killer_belt",CubeListBuilder.create().texOffs(0,48).addBox(-4,9,-2,8,2,4,new CubeDeformation(.6F)),PartPose.ZERO);
            r.getChild("right_arm").addOrReplaceChild("killer_sleeve",CubeListBuilder.create().texOffs(88,16).addBox(-3,-2,-2,4,12,4,new CubeDeformation(.32F)),PartPose.ZERO);
            r.getChild("left_arm").addOrReplaceChild("killer_sleeve",CubeListBuilder.create().texOffs(88,16).mirror().addBox(-1,-2,-2,4,12,4,new CubeDeformation(.32F)),PartPose.ZERO);
            r.getChild("right_arm").addOrReplaceChild("killer_axe",CubeListBuilder.create().texOffs(48,32).addBox(-1.5F,9.5F,-9,1,1,10).texOffs(72,32).addBox(-2,8,-10,2,5,3),PartPose.ZERO);
            for(var leg:new String[]{"right_leg","left_leg"}){var part=r.getChild(leg);boolean left=leg.startsWith("left");
                part.addOrReplaceChild("killer_tail",CubeListBuilder.create().texOffs(0,32).mirror(left).addBox(-2,0,-2,4,6,4,new CubeDeformation(.52F)),PartPose.ZERO);
                part.addOrReplaceChild("killer_boot",CubeListBuilder.create().texOffs(16,32).mirror(left).addBox(-2,7,-2,4,5,4,new CubeDeformation(.38F)),PartPose.ZERO);
                part.addOrReplaceChild("killer_toe",CubeListBuilder.create().texOffs(32,32).mirror(left).addBox(-2,10,-3,4,2,1,new CubeDeformation(.38F)),PartPose.ZERO);}
            return LayerDefinition.create(m,128,64);}
        private void dress(int role,int phase){boolean bar=role==LiteraryActor.STRANGER&&phase>=4&&phase<4+BAR.length;int cottage=role==LiteraryActor.STRANGER&&phase>=9&&phase<9+COTTAGE.length?phase-9:-1;int movie=role==LiteraryActor.STRANGER&&phase>=14&&phase<14+MOVIE.length?phase-14:-1;int costume=role==LiteraryActor.STRANGER&&phase>=21&&phase<21+COSTUME.length?phase-21:-1;int winter=role==LiteraryActor.STRANGER&&phase>=34&&phase<34+WINTER.length?phase-34:-1;coat.visible=role==LiteraryActor.FATHER||role==LiteraryActor.FAMILY_FATHER||role==LiteraryActor.OLD_MAN||role==LiteraryActor.VISITOR||role==LiteraryActor.STRANGER&&cottage<0&&movie<0&&costume<0&&winter<0&&(!bar||phase==4)||cottage==0||cottage==3||cottage==4;gown.visible=role==LiteraryActor.LADY||role==LiteraryActor.COFFIN_WOMAN||role==LiteraryActor.FAMILY_MOTHER||cottage==1||movie==3||costume==1||costume==6||costume==10||costume==12||role==LiteraryActor.LAKE_WAX;mask.visible=role==LiteraryActor.DANCER||role==LiteraryActor.RED_FIGURE;hood.visible=false;bandage.visible=role==LiteraryActor.FATHER&&phase>=2;
            boolean killer=role==LiteraryActor.KILLER;for(var part:new ModelPart[]{killerHood,killerMask,killerCoat,killerBelt,sleeveRight,sleeveLeft,tailRight,tailLeft,bootRight,bootLeft,toeRight,toeLeft,axe})part.visible=killer;hardHat.visible=false;lakeHair.visible=role==LiteraryActor.LAKE_TESS;hat.visible=!killer&&role!=LiteraryActor.LAKE_TESS;}
        public void pose(int role,int phase,float age){dress(role,phase);
            if(HouseReadShelter.role(role)){
                body.xRot=-.06F;body.y+=(float)Math.sin(age*.035F)*.07F;
                leftArm.xRot=.18F+(float)Math.sin(age*.027F)*.035F;rightArm.xRot=.18F-(float)Math.sin(age*.027F)*.035F;
                leftArm.zRot=-.07F;rightArm.zRot=.07F;
                if(role==LiteraryActor.READ_WOMAN){rightArm.xRot=-.45F;rightArm.yRot=-.25F;}
                if(phase==1)head.zRot=role==LiteraryActor.READ_MAN?.035F:-.035F;
            }
            if(role==LiteraryActor.DANCER&&phase==1){leftArm.xRot=rightArm.xRot=-.8F;leftArm.zRot=-.55F;rightArm.zRot=.55F;body.yRot=(float)Math.sin(age*.045)*.18F;leftLeg.xRot=(float)Math.sin(age*.08)*.22F;rightLeg.xRot=-leftLeg.xRot;}
            if(role==LiteraryActor.OLD_MAN){body.xRot=.19F;head.xRot=.14F;leftArm.xRot=rightArm.xRot=-.1F;}
            if(role==LiteraryActor.FATHER)father(phase,age);
            if(role==LiteraryActor.BROTHER&&phase>0){leftArm.xRot=rightArm.xRot=-.18F;head.xRot=.4F;}
            if(role==LiteraryActor.KILLER)killer(phase,age);
            if(LiteraryActor.lakePartner(role)){
                if(phase==2||phase==6){float stroke=age*.095F;body.xRot=0;head.xRot=-.8F;leftArm.xRot=rightArm.xRot=-2.65F+(float)Math.sin(stroke)*.55F;leftArm.zRot=-.18F-Math.max(0,(float)Math.cos(stroke))*.5F;rightArm.zRot=-leftArm.zRot;leftLeg.xRot=(float)Math.sin(age*.18)*.18F;rightLeg.xRot=-leftLeg.xRot;hat.copyFrom(head);}
                else if(phase==1){leftLeg.xRot=rightLeg.xRot=-1.45F;leftLeg.yRot=.12F;rightLeg.yRot=-.12F;leftArm.xRot=rightArm.xRot=-.35F;leftArm.zRot=-.12F;rightArm.zRot=.12F;}
                else if(phase==3){rightArm.xRot=-1.6F;head.xRot=-.25F;}
                else if(phase==4){body.xRot=.2F;leftArm.xRot=-.75F;rightArm.xRot=-1.2F;}
            }
            if(role==LiteraryActor.LAKE_WAX){head.xRot=.2F;leftArm.xRot=-.9F;rightArm.xRot=-1.7F+(float)Math.sin(age*.035)*.18F;}

        }
        /** A guarded left leg bears less weight each night; the final state cannot take a stride. */
        private void father(int phase,float age){
            float breath=(float)Math.sin(age*.065F),stride=(float)Math.cos(walk*.6662F);
            if(phase==4){
                float lift=(float)Math.sin(age*.21F);body.xRot=.3F;head.xRot=.12F;leftLeg.xRot=-.35F+lift*.08F;rightLeg.xRot=-.42F+lift*.08F;leftArm.xRot=rightArm.xRot=-.35F+lift*.18F;leftArm.zRot=-.18F;rightArm.zRot=.18F;body.y+=Math.max(0,lift)*.7F;
            }else if(phase>=3){
                body.xRot=.38F;body.zRot=-.12F;head.xRot+=.2F;
                leftLeg.xRot=-.8F;leftLeg.zRot=-.28F;rightLeg.xRot=-1.15F;
                leftArm.xRot=-.55F;rightArm.xRot=-.4F;rightArm.zRot=.23F;
                for(var part:new ModelPart[]{head,body,leftArm,rightArm,leftLeg,rightLeg})part.y+=5;
                body.y+=breath*.12F;
            }else{
                float pain=phase>=2?1F:.3F;
                leftLeg.xRot=.18F*pain-stride*amount*(.65F-.43F*pain);
                rightLeg.xRot=stride*amount*.85F;
                leftLeg.zRot=-.05F*pain;body.zRot=-.075F*pain;
                body.xRot+=.08F*pain;body.y+=(float)Math.sin(walk*1.33F)*amount*.45F*pain;
                rightArm.xRot=-.2F*pain;rightArm.zRot=.2F*pain;
                leftArm.xRot=-stride*amount*.28F;
            }
            hat.copyFrom(head);
        }
        public void animateFather(int phase,float stride,float strength,float age){
            for(var part:new ModelPart[]{head,body,leftArm,rightArm,leftLeg,rightLeg})part.getAllParts().forEach(ModelPart::resetPose);
            young=false;walk=stride;amount=strength;pose(LiteraryActor.FATHER,phase,age);
        }
        /**
         * Watching (0), searching (1), running (2), crouched at the pile (3); and (0.4.120) hacking at a carcass (4), hauling
         * one by a leg (5), rocked back by a shove (6), the axe up in both hands (7) and dragging a reader (8). He carries
         * himself low and heavy and makes no sound of his own; a blow is a two-handed overhead chop out of the raised axe.
         */
        private void killer(int phase,float age){
            float breath=(float)Math.sin(age*.07F);
            rightArm.zRot+=.1F;leftArm.zRot-=.1F;
            switch(phase){
                case 1->{float stride=walk*.42F,swing=(float)Math.cos(stride),plant=Math.abs((float)Math.sin(stride));
                    body.xRot+=.2F;head.xRot+=.18F;
                    leftLeg.xRot=swing*1.0F*amount;rightLeg.xRot=-swing*1.0F*amount;
                    leftArm.xRot=swing*.35F*amount;rightArm.xRot=-.22F-swing*.12F*amount;rightArm.zRot=.18F;
                    body.zRot=(float)Math.sin(stride)*.08F*amount;body.yRot=(float)Math.sin(stride)*.1F*amount;
                    lower(plant*.8F*amount);head.yRot+=(float)Math.sin(age*.045F)*.7F;}
                case 2->{float stride=walk*.6662F,swing=(float)Math.cos(stride);
                    body.xRot=.62F;head.xRot=-.42F;leftLeg.xRot=swing*1.4F*amount;rightLeg.xRot=-swing*1.4F*amount;
                    leftArm.xRot=-1.15F-swing*.35F*amount;leftArm.zRot=-.28F;rightArm.xRot=-2.7F+swing*.12F*amount;rightArm.zRot=.25F;
                    body.yRot=swing*.12F*amount;lower(Math.abs((float)Math.sin(stride))*.7F*amount);}
                case 3->{body.xRot=.62F;head.xRot=.5F;head.zRot=.34F;leftArm.xRot=-1.3F;leftArm.yRot=.25F;rightArm.xRot=-.55F;
                    leftLeg.xRot=-.5F;rightLeg.xRot=.28F;}
                case 4->{body.xRot=.72F;head.xRot=.6F;head.zRot=.1F;leftLeg.xRot=-.45F;rightLeg.xRot=.3F;leftLeg.zRot=-.08F;rightLeg.zRot=.08F;
                    leftArm.xRot=-1.15F;leftArm.zRot=-.22F;rightArm.xRot=-2.55F+breath*.05F;rightArm.zRot=.12F;lower(1.2F+breath*.3F);}
                case 5->{float stride=walk*.4F,swing=(float)Math.cos(stride);
                    body.xRot=.58F;head.xRot=-.15F;leftLeg.xRot=swing*.75F*amount;rightLeg.xRot=-swing*.75F*amount;
                    leftArm.xRot=.7F;leftArm.zRot=-.14F;leftArm.yRot=.12F;rightArm.xRot=-.3F-swing*.15F*amount;rightArm.zRot=.2F;
                    body.yRot=-.12F;lower(Math.abs((float)Math.sin(stride))*.6F*amount);}
                case 6->{body.xRot=-.2F;head.xRot=-.38F;head.zRot=.14F;leftArm.xRot=-.6F;leftArm.zRot=-.85F;rightArm.xRot=-.9F;rightArm.zRot=.6F;
                    rightLeg.xRot=.5F;leftLeg.xRot=-.18F;}
                case 7->{float tremble=(float)Math.sin(age*1.7F)*.03F;
                    body.xRot=-.14F;head.xRot=-.18F;rightArm.xRot=-3.05F+tremble;rightArm.zRot=-.08F;leftArm.xRot=-2.75F+tremble;leftArm.zRot=.38F;
                    leftLeg.xRot=-.32F;rightLeg.xRot=.32F;}
                case 8->{float stride=walk*.45F,swing=(float)Math.cos(stride);
                    body.xRot=.55F;head.xRot=.08F;leftLeg.xRot=swing*.85F*amount;rightLeg.xRot=-swing*.85F*amount;
                    leftArm.xRot=.9F;leftArm.zRot=-.06F;rightArm.xRot=-.22F-swing*.1F*amount;rightArm.zRot=.2F;
                    // Now and then he looks back over his shoulder at what he is pulling.
                    float glance=net.minecraft.util.Mth.clamp(((float)Math.sin(age*.035F)-.82F)*8,0,1);head.yRot+=glance*1.9F;head.xRot-=glance*.3F;
                    lower(Math.abs((float)Math.sin(stride))*.8F*amount);}
                default->{body.xRot+=.2F;head.xRot+=.12F;
                    float tic=net.minecraft.util.Mth.clamp(((float)Math.sin(age*.031F)-.96F)*25,0,1);
                    head.yRot+=(float)Math.sin(age*.025F)*.3F;head.zRot=(float)Math.sin(age*.013F)*.18F+tic*.4F;
                    rightArm.xRot=-.15F+breath*.03F;leftArm.xRot=.04F-breath*.03F;lower(breath*.35F);}
            }
            if(attackTime>0){float t=attackTime;rightArm.xRot=-3.05F+t*3.6F;rightArm.yRot=0;rightArm.zRot=.1F;leftArm.xRot=-2.75F+t*3.15F;leftArm.zRot=.38F*(1-t);
                body.xRot+=(float)Math.sin(t*Math.PI)*.45F;head.xRot+=t*.25F;}
            // Coat tails trail the stride; they hang a little behind whichever leg is forward.
            tailRight.xRot=Math.max(0,-rightLeg.xRot)*.35F;tailLeft.xRot=Math.max(0,-leftLeg.xRot)*.35F;
        }
        /** The killer's leaning phases take the native crouch's offsets, so the torso stays on the legs. */
        static boolean crouches(int phase){return switch(phase){case 2,3,4,5,8->true;default->false;};}
        /** Down from the hips: the head, torso and arms together, the feet where they are. */
        private void lower(float by){head.y+=by;body.y+=by;leftArm.y+=by;rightArm.y+=by;}
        @Override public void setupAnim(LiteraryActor a,float walk,float amount,float age,float yaw,float pitch){head.getAllParts().forEach(ModelPart::resetPose);body.getAllParts().forEach(ModelPart::resetPose);leftLeg.getAllParts().forEach(ModelPart::resetPose);rightLeg.getAllParts().forEach(ModelPart::resetPose);leftArm.getAllParts().forEach(ModelPart::resetPose);rightArm.getAllParts().forEach(ModelPart::resetPose);this.walk=walk;this.amount=amount;crouching=a.getPose()==net.minecraft.world.entity.Pose.CROUCHING||(a.role()==LiteraryActor.KILLER&&crouches(a.phase()));super.setupAnim(a,walk,amount,age,yaw,pitch);pose(a.role(),a.phase(),age);
            if(a.role()==LiteraryActor.FATHER){switch(a.gesture()){
                case 1->{rightArm.xRot=-1.45F;rightArm.yRot=-.45F;rightArm.zRot=.18F;head.yRot-=.25F;}
                case 2->{leftArm.xRot=rightArm.xRot=-1.2F;leftArm.yRot=.3F;rightArm.yRot=-.3F;body.xRot+=.12F;head.xRot+=.2F;}
                case 3->{rightArm.xRot=-.45F+(float)Math.sin(age*.12F)*.08F;rightArm.zRot=.18F;head.xRot+=.18F;}
                default->{}
            }hat.copyFrom(head);}
            if(a.role()==LiteraryActor.STRANGER&&a.gesture()==4)reaching();
            if(a.role()==LiteraryActor.LAKE_TESS)lakeHairMotion.pose(a,age);
            // An actor placed in a chair sits in it: hips on the seat, legs forward.
            var chair=seat(a);if(chair!=null){float lower=12-chair.seatHeight();for(var part:new ModelPart[]{head,body,leftArm,rightArm,leftLeg,rightLeg})part.y+=lower;leftLeg.xRot=rightLeg.xRot=-1.45F;leftLeg.yRot=.1F;rightLeg.yRot=-.1F;leftArm.xRot=rightArm.xRot=-.35F;hat.copyFrom(head);}
            if(a.role()==LiteraryActor.BROTHER||a.role()==LiteraryActor.FAMILY_CHILD||a.role()==LiteraryActor.SILHOUETTE&&a.phase()==4){
                var motion=HumanoidMotion.sample(a,age-a.tickCount);
                if(chair==null&&!(a.role()==LiteraryActor.BROTHER&&a.phase()>0))HumanoidMotion.gait(this,motion.phase(),motion.amount(),motion.running());
                body.yRot+=.02F*net.minecraft.util.Mth.sin(age*.037F+a.getId()*1.7F);HumanoidMotion.connect(this);
            }
            if(a.gesture()==io.github.knaitoe.theoldesthouse.labyrinth.CostumeNight.CRAWL)crawl(walk,amount);
            if(a.role()==LiteraryActor.SILHOUETTE&&a.scene().equals(LabyrinthPlace.CHILD_ROOM.id())&&a.hasCustomName()&&"Dinnerbone".equals(a.getCustomName().getString()))DinnerboneIdle.pose(this,a.level().getGameTime()+age-a.tickCount,a.getUUID().hashCode());RecentActorMotion.blend(a,age,this,coat,gown,mask,bandage,hood,lakeHair);}
        /** On all fours, the way she goes (Costume Night's crawl, 0.4.103): the torso level at hip height, the head up, arms and legs as four legs. */
        public void crawl(float walk,float amount){
            float step=net.minecraft.util.Mth.cos(walk*.9F)*.5F*Math.min(1,amount*1.6F);
            body.y=12;body.z=-6;body.xRot=(float)(Math.PI/2);body.yRot=0;body.zRot=0;gown.visible=false;
            head.y=12;head.z=-6;head.xRot=-.15F;
            for(var arm:new ModelPart[]{rightArm,leftArm}){arm.y=13;arm.z=-5;arm.yRot=0;arm.zRot=0;}
            for(var leg:new ModelPart[]{rightLeg,leftLeg}){leg.y=12;leg.z=6;leg.yRot=0;leg.zRot=0;}
            rightArm.xRot=step;leftArm.xRot=-step;rightLeg.xRot=-step;leftLeg.xRot=step;hat.copyFrom(head);
        }
        /** A preview of the crawl for native proofs, on the given skin's dressing. */
        public void previewCrawl(int role,int phase,float age){preview(role,phase,age);crawl(age*.4F,.6F);}
        public void previewLakeHair(boolean drifting,float age){lakeHairMotion.preview(drifting,age);}
        /** Script poses on the same baked joints, with native actor scale applied by the caller. */
        public void preview(int role,int phase,float age){
            for(var part:new ModelPart[]{head,body,leftArm,rightArm,leftLeg,rightLeg})part.getAllParts().forEach(ModelPart::resetPose);
            young=false;attackTime=0;crouching=false;riding=false;walk=age*.4F;amount=.6F;
            pose(role,phase,age);hat.copyFrom(head);
            if(role==LiteraryActor.BROTHER||role==LiteraryActor.FAMILY_CHILD||role==LiteraryActor.SILHOUETTE&&phase==4)HumanoidMotion.connect(this);
        }
        public void reaching(){leftArm.xRot=rightArm.xRot=-1.45F;leftArm.yRot=.28F;rightArm.yRot=-.28F;body.xRot+=.12F;}
        /** The killer's motion as the renderer drives it, for native proofs: phase, stride, swing and an attack in progress. */
        public void animateKiller(int phase,float walk,float amount,float age,float attack){for(var part:new ModelPart[]{head,body,leftArm,rightArm,leftLeg,rightLeg})part.getAllParts().forEach(ModelPart::resetPose);
            this.walk=walk;this.amount=amount;attackTime=attack;crouching=crouches(phase);young=false;riding=false;
            dress(LiteraryActor.KILLER,phase);
            float swing=(float)Math.cos(walk*.6662F)*amount;rightLeg.xRot=swing*1.4F;leftLeg.xRot=-swing*1.4F;rightArm.xRot=-swing;leftArm.xRot=swing;
            if(crouching){body.xRot=.5F;rightArm.xRot+=.4F;leftArm.xRot+=.4F;rightLeg.z=4.0F;leftLeg.z=4.0F;rightLeg.y=12.2F;leftLeg.y=12.2F;head.y=4.2F;body.y=3.2F;leftArm.y=5.2F;rightArm.y=5.2F;}
            killer(phase,age);}
        /** A body left where it fell: on its back (0), face down (1), slumped in a chair (2) or curled on its side (3). */
        /** Drawn by the block entity renderer, never by an entity renderer, so it resets the adult pose itself (a model is born young). */
        public void corpse(boolean crew,int variant,int stage){
            for(var part:new ModelPart[]{head,body,leftArm,rightArm,leftLeg,rightLeg})part.getAllParts().forEach(ModelPart::resetPose);
            attackTime=0;crouching=false;young=false;riding=false;dress(-1,0);hat.visible=false;gown.visible=!crew&&variant==0;hardHat.visible=crew&&variant!=2;
            switch(stage){
                case 0->{leftArm.zRot=-.42F;rightArm.zRot=.3F;rightArm.xRot=-.2F;leftLeg.zRot=-.08F;rightLeg.zRot=.1F;head.yRot=.55F;head.xRot=-.2F;}
                case 1->{rightArm.xRot=-2.9F;rightArm.zRot=.12F;leftArm.zRot=-.15F;head.yRot=-.7F;leftLeg.xRot=.12F;rightLeg.zRot=.08F;}
                case 2->{float lower=4;for(var part:new ModelPart[]{head,body,leftArm,rightArm,leftLeg,rightLeg})part.y+=lower;leftLeg.xRot=rightLeg.xRot=-1.45F;leftLeg.yRot=.12F;rightLeg.yRot=-.1F;body.xRot=.72F;head.xRot=.95F;head.zRot=.2F;head.z-=3.5F;leftArm.z-=3;rightArm.z-=3;leftArm.y+=1.5F;rightArm.y+=1.5F;leftArm.xRot=-1.05F;rightArm.xRot=-.95F;rightArm.zRot=.2F;}
                default->{leftLeg.xRot=rightLeg.xRot=-1.15F;leftLeg.zRot=.05F;body.xRot=.35F;head.xRot=.55F;leftArm.xRot=rightArm.xRot=-1.2F;leftArm.yRot=.3F;rightArm.yRot=-.3F;}
            }
        }
    }
    /** Native lying body fits the two-block casket, face up; no entity movement or respawn. */
    public static void bedPose(PoseStack p,float scale){p.translate(0,.5625/scale+.25,-.35);p.mulPose(Axis.XP.rotationDegrees(90));}
    public static void coffinPose(PoseStack p){p.translate(0,.4375,-.35);p.mulPose(Axis.XP.rotationDegrees(90));p.scale(.85F,.85F,.85F);}
    static void crutches(CastModel model,ModelPart pole,PoseStack p,MultiBufferSource out,int light){for(var arm:new ModelPart[]{model.leftArm,model.rightArm}){p.pushPose();arm.translateAndRotate(p);p.translate(0,.32,0);pole.render(p,out.getBuffer(RenderType.entityCutoutNoCull(ResourceLocation.withDefaultNamespace("textures/block/spruce_log.png"))),light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);p.popPose();}}
    private static final class CrutchLayer extends net.minecraft.client.renderer.entity.layers.RenderLayer<LiteraryActor,CastModel>{
        private final ModelPart pole;
        CrutchLayer(CastRenderer parent,EntityRendererProvider.Context c){super(parent);pole=c.bakeLayer(CRUTCH).getChild("pole");}
        static LayerDefinition layer(){var mesh=new MeshDefinition();mesh.getRoot().addOrReplaceChild("pole",CubeListBuilder.create().texOffs(0,0).addBox(-.7F,-1,-1,1.4F,22,1.4F).texOffs(0,0).addBox(-2,-1,-1,4,1.4F,1.4F),PartPose.ZERO);return LayerDefinition.create(mesh,16,16);}
        @Override public void render(PoseStack p,MultiBufferSource out,int light,LiteraryActor a,float walk,float amount,float partial,float age,float yaw,float pitch){if(a.role()!=LiteraryActor.FATHER||a.phase()!=4)return;crutches(getParentModel(),pole,p,out,light);}
    }
    /** The carcass he hauls off by a hind leg (0.4.120), drawn behind his left hand. */
    private static final class HaulLayer extends net.minecraft.client.renderer.entity.layers.RenderLayer<LiteraryActor,CastModel>{
        private final ElkModel elk;
        HaulLayer(CastRenderer parent,EntityRendererProvider.Context c){super(parent);elk=new ElkModel(c.bakeLayer(ELK));}
        @Override public void render(PoseStack p,MultiBufferSource out,int light,LiteraryActor a,float walk,float amount,float partial,float age,float yaw,float pitch){
            if(a.role()!=LiteraryActor.KILLER||a.phase()!=ElkHunt.HAUL)return;haul(elk,p,out,light,walk);}
    }
    /** From a cast layer's model space: a carcass on its side behind the left hand, head away, swaying as it drags. */
    public static void haul(ElkModel elk,PoseStack p,MultiBufferSource out,int light,float walk){
        p.pushPose();p.translate(0,1.501,0);p.scale(-1,-1,1);
        p.translate(-.3,0,1.55);p.mulPose(Axis.YP.rotationDegrees((float)Math.sin(walk*.4F)*5));p.scale(.85F,.85F,.85F);p.translate(-.5,0,-.5);
        ModelRenderer.carcassPose(p,Direction.NORTH,0,1);elk.dead(1,11L);
        elk.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(texture("elk_carcass"))),light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,0xffffffff);p.popPose();
    }
    private static final class CastRenderer extends MobRenderer<LiteraryActor,CastModel>{private final ModelPart eyes;private final PlainFigureModel.Renderer plain;private final GhostsCastModel.Renderer ghosts;private final GhostsPropRenderer ghostsProps;private final BorrascaNativeArt.CastRenderer borrasca;private final UncannyActorRenderer uncanny;private final HouseHumanRenderers.ActorRenderer harrigan;CastRenderer(EntityRendererProvider.Context c){super(c,new CastModel(c.bakeLayer(CAST)),.2F);eyes=c.bakeLayer(CAST).getChild("herd_eyes");plain=new PlainFigureModel.Renderer(c);ghosts=new GhostsCastModel.Renderer(c);ghostsProps=new GhostsPropRenderer(c);borrasca=new BorrascaNativeArt.CastRenderer(c);harrigan=new HouseHumanRenderers.ActorRenderer(c);uncanny=new UncannyActorRenderer(c);addLayer(new CrutchLayer(this,c));addLayer(new HaulLayer(this,c));}@Override public void render(LiteraryActor a,float yaw,float partial,PoseStack p,MultiBufferSource out,int light){if(UncannyStories.role(a.role())){uncanny.render(a,yaw,partial,p,out,light);return;}if(PlainFigureModel.matches(a.role(),a.phase())){plain.render(a,yaw,partial,p,out,light);return;}if(a.role()==LiteraryActor.HARRIGAN||a.role()==LiteraryActor.HARRIGAN_MOURNER){harrigan.render(a,yaw,partial,p,out,light);return;}if(a.role()>=LiteraryActor.BORRASCA_KYLE&&a.role()<=LiteraryActor.BORRASCA_PRESCOTT){borrasca.render(a,yaw,partial,p,out,light);return;}if(a.role()>=LiteraryActor.GHOST_PLAYHOUSE&&a.role()<=LiteraryActor.GHOST_DRAWER){ghostsProps.render(a,yaw,partial,p,out,light);return;}if(a.role()>=LiteraryActor.GHOST_MARJORIE&&a.role()<=LiteraryActor.GHOST_PRIEST){ghosts.render(a,yaw,partial,p,out,light);return;}if(a.role()==LiteraryActor.RED_FIGURE&&a.phase()==3){p.pushPose();p.translate(0,1.45,0);p.mulPose(Axis.YP.rotationDegrees(-yaw));eyes.render(p,out.getBuffer(RenderType.eyes(texture("herd_eyes"))),LightTexture.FULL_BRIGHT,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);p.popPose();return;}super.render(a,yaw,partial,p,out,light);}@Override public ResourceLocation getTextureLocation(LiteraryActor a){return skin(a.role(),a.phase());}@Override protected void setupRotations(LiteraryActor a,PoseStack p,float age,float yaw,float partial,float scale){var chair=seat(a);var facing=chair==null?null:chair.facing();boolean lying=a.role()==LiteraryActor.COFFIN_WOMAN&&a.phase()==1;super.setupRotations(a,p,age,(lying||a.role()==LiteraryActor.BROTHER&&a.phase()>0)?0:facing==null?yaw:facing.toYRot(),partial,scale);if(a.role()==LiteraryActor.LAKE_TESS&&(a.phase()==2||a.phase()==6)){p.mulPose(Axis.XP.rotationDegrees(-90-a.getViewXRot(partial)));p.translate(0,-1,.3);}if(lying)coffinPose(p);if(a.role()==LiteraryActor.BROTHER&&a.phase()>0)bedPose(p,.58F);}@Override public boolean shouldRender(LiteraryActor a,net.minecraft.client.renderer.culling.Frustum f,double x,double y,double z){var p=Minecraft.getInstance().player;return !(a.role()==LiteraryActor.COFFIN_WOMAN&&a.phase()==2)&&(a.scene().equals(LabyrinthPlace.FAMILY_COPY.id())||a.scene().equals(LabyrinthPlace.OLD_CABIN.id())||a.owner().isEmpty()||p!=null&&a.owner().get().equals(p.getUUID()))&&(UncannyStories.role(a.role())?uncanny.shouldRender(a,f,x,y,z):a.role()==LiteraryActor.HARRIGAN?harrigan.shouldRender(a,f,x,y,z):super.shouldRender(a,f,x,y,z));}}
    public static final class ElkModel extends EntityModel<LiteraryElk>{private final ModelPart root;public ElkModel(ModelPart r){root=r;}
        public static LayerDefinition layer(){var m=new MeshDefinition();var r=m.getRoot();r.addOrReplaceChild("body",CubeListBuilder.create().texOffs(0,0).addBox(-5,-6,-10,10,12,23),PartPose.offset(0,11,0));r.addOrReplaceChild("neck",CubeListBuilder.create().texOffs(0,36).addBox(-3.5F,-6,-4,7,15,8),PartPose.offsetAndRotation(0,7,-9,-.4F,0,0));var h=r.addOrReplaceChild("head",CubeListBuilder.create().texOffs(68,0).addBox(-4,-3.5F,-8,8,7,10).texOffs(68,20).addBox(-3,0,-12,6,4,5),PartPose.offset(0,3,-12));h.addOrReplaceChild("ear_left",CubeListBuilder.create().texOffs(100,20).addBox(0,-2,-1,6,3,2),PartPose.offsetAndRotation(3,-2,0,0,0,-.4F));h.addOrReplaceChild("ear_right",CubeListBuilder.create().texOffs(100,20).addBox(-6,-2,-1,6,3,2),PartPose.offsetAndRotation(-3,-2,0,0,0,.4F));for(int i=0;i<4;i++)r.addOrReplaceChild("leg"+i,CubeListBuilder.create().texOffs(32,38).addBox(-1.5F,0,-1.5F,3,13,3).texOffs(48,38).addBox(-2,11,-2,4,2,4),PartPose.offset(i%2==0?-3.5F:3.5F,11,i<2?-7:9));r.addOrReplaceChild("tail",CubeListBuilder.create().texOffs(68,38).addBox(-1,-1,0,2,2,5),PartPose.offsetAndRotation(0,8,12,.2F,0,0));return LayerDefinition.create(m,128,64);}
        @Override public void setupAnim(LiteraryElk a,float walk,float amount,float age,float yaw,float pitch){root.getAllParts().forEach(ModelPart::resetPose);root.getChild("head").yRot=yaw*.01745F;root.getChild("head").xRot=pitch*.01745F;for(int i=0;i<4;i++)root.getChild("leg"+i).xRot=(float)Math.cos(walk*.6662F+(i==0||i==3?0:Math.PI))*amount*.8F;}
        public void lying(){root.getAllParts().forEach(ModelPart::resetPose);for(int i=0;i<4;i++)root.getChild("leg"+i).xRot=-.6F;}
        /** A carcass where the crew left it: legs stiff (or folded under, 3), the head dropped and turned by how it fell. */
        public void dead(int stage,long seed){root.getAllParts().forEach(ModelPart::resetPose);var r=new java.util.Random(seed);
            for(int i=0;i<4;i++){var leg=root.getChild("leg"+i);leg.xRot=stage==3?(i<2?-1.25F:1.15F):(i<2?-.3F:.35F)+(r.nextFloat()-.5F)*.3F;leg.zRot=(r.nextFloat()-.5F)*.25F;}
            var head=root.getChild("head");head.xRot=.5F+(r.nextFloat()-.5F)*.3F;head.yRot=switch(stage){case 1->1.15F;case 2->-.45F;case 3->.6F;default->.25F;};head.zRot=(r.nextFloat()-.5F)*.3F;
            head.getChild("ear_left").zRot=.35F;head.getChild("ear_right").zRot=-.35F;root.getChild("tail").xRot=-.3F;}
        @Override public void renderToBuffer(PoseStack p,VertexConsumer b,int light,int overlay,int color){root.render(p,b,light,overlay,color);}}
    public static final class ElkEyes extends net.minecraft.client.renderer.entity.layers.RenderLayer<LiteraryElk,ElkModel>{
        private final ModelPart eyes;
        ElkEyes(ElkRenderer parent,EntityRendererProvider.Context c){super(parent);eyes=c.bakeLayer(ELK_EYES).getChild("eyes");}
        public static LayerDefinition layer(){var mesh=new MeshDefinition();mesh.getRoot().addOrReplaceChild("eyes",CubeListBuilder.create().texOffs(0,0).addBox(-3.5F,-1.7F,-8.06F,1.2F,.8F,.12F).addBox(2.3F,-1.7F,-8.06F,1.2F,.8F,.12F),PartPose.ZERO);return LayerDefinition.create(mesh,128,64);}
        public static void draw(ElkModel model,ModelPart eyes,PoseStack p,MultiBufferSource out){p.pushPose();model.root.getChild("head").translateAndRotate(p);eyes.render(p,out.getBuffer(RenderType.eyes(texture("herd_eyes"))),LightTexture.FULL_BRIGHT,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);p.popPose();}
        @Override public void render(PoseStack p,MultiBufferSource out,int light,LiteraryElk a,float walk,float amount,float partial,float age,float yaw,float pitch){if(a.owner().isPresent()&&!a.isInvisible())draw(getParentModel(),eyes,p,out);}
    }
    private static final class ElkRenderer extends MobRenderer<LiteraryElk,ElkModel>{ElkRenderer(EntityRendererProvider.Context c){super(c,new ElkModel(c.bakeLayer(ELK)),.5F);addLayer(new ElkEyes(this,c));}@Override public ResourceLocation getTextureLocation(LiteraryElk a){return texture(a.wounded()?"elk_wounded":"elk");}@Override public boolean shouldRender(LiteraryElk a,net.minecraft.client.renderer.culling.Frustum f,double x,double y,double z){var p=Minecraft.getInstance().player;return (a.owner().isEmpty()||p!=null&&a.owner().get().equals(p.getUUID()))&&super.shouldRender(a,f,x,y,z);}}
    public static final class ModelRenderer implements BlockEntityRenderer<LiteraryModelBlockEntity>{private final ModelPart fan;private final ElkModel elk;private final HumanoidModel<?> tiny,tinySlim;private final CastModel miniatureFigure,corpse;private final ElkModel carcass;
        public ModelRenderer(BlockEntityRendererProvider.Context c){fan=c.bakeLayer(FAN);elk=new ElkModel(c.bakeLayer(ELK));tiny=new PlayerModel<>(c.bakeLayer(ModelLayers.PLAYER),false);tinySlim=new PlayerModel<>(c.bakeLayer(ModelLayers.PLAYER_SLIM),true);miniatureFigure=new CastModel(c.bakeLayer(CAST));corpse=new CastModel(c.bakeLayer(CAST));carcass=new ElkModel(c.bakeLayer(ELK));}
        public static LayerDefinition fanLayer(){var m=new MeshDefinition();var r=m.getRoot();r.addOrReplaceChild("hub",CubeListBuilder.create().texOffs(0,0).addBox(-3,-2,-3,6,4,6),PartPose.ZERO);r.addOrReplaceChild("hanger",CubeListBuilder.create().texOffs(76,0).addBox(-1,2,-1,2,18.5F,2).texOffs(86,0).addBox(-3,18.5F,-3,6,2,6),PartPose.ZERO);for(int i=0;i<4;i++)r.addOrReplaceChild("blade"+i,CubeListBuilder.create().texOffs(24,0).addBox(2,-1,-2,22,1.5F,4),PartPose.rotation(0,(float)(i*Math.PI/2),.025F));return LayerDefinition.create(m,128,64);}
        /** The ceiling attachment stays still while the hub and blades turn. */
        public static void renderFan(ModelPart fan,PoseStack pose,MultiBufferSource out,int light,int overlay,float angle){var vertices=out.getBuffer(RenderType.entityCutoutNoCull(texture("fan")));fan.getChild("hanger").render(pose,vertices,light,overlay);pose.pushPose();pose.mulPose(Axis.YP.rotation(angle));fan.getChild("hub").render(pose,vertices,light,overlay);for(int i=0;i<4;i++)fan.getChild("blade"+i).render(pose,vertices,light,overlay,i%2==0?0xffffffff:0xffa0a0a0);pose.popPose();}
        @Override public void render(LiteraryModelBlockEntity e,float partial,PoseStack p,MultiBufferSource out,int light,int overlay){var state=e.getBlockState();if(state.is(LiteraryRegistry.FROZEN.get())){var original=LiteraryFrozenBlock.original(e.getLevel(),e.getBlockPos());if(!original.isAir())Minecraft.getInstance().getBlockRenderer().renderSingleBlock(original,p,out,light,overlay);return;}if(!state.is(LiteraryRegistry.PROP.get()))return;var kind=state.getValue(LiteraryPropBlock.KIND);var mc=Minecraft.getInstance();if(mc.player==null)return;
            if(kind==LiteraryPropBlock.Kind.JAR){FireflyJar.render(p,out,e.getLevel()==null?partial:e.getLevel().getGameTime()+partial,e.getBlockPos(),overlay);return;}
            if(kind==LiteraryPropBlock.Kind.LEDGER){if(EndingBookClient.visible(e.getBlockPos()))mc.getBlockRenderer().getModelRenderer().renderModel(p.last(),out.getBuffer(RenderType.cutout()),state,mc.getBlockRenderer().getBlockModel(state),1F,1F,1F,light,overlay);return;}
            if(kind==LiteraryPropBlock.Kind.FAN){float age=e.getLevel()==null?0:e.getLevel().getGameTime()+partial;float angle=age*.055F;p.pushPose();p.translate(.5,.72,.5);renderFan(fan,p,out,light,overlay,angle);p.popPose();ElkFanOutline.render(e.getBlockPos(),p,out,light,overlay);double height=mc.player.getEyeY()-e.getBlockPos().getY();if(LiteraryView.models().getInt("FanOutline")==0&&mc.options.getCameraType().isFirstPerson()&&height>-1.8&&height<1&&mc.player.distanceToSqr(e.getBlockPos().getCenter())<25){int floorLight=e.getLevel()==null?light:LevelRenderer.getLightColor(e.getLevel(),e.getBlockPos().below(LiteraryRooms.FAN.getY()));fanElk(elk,p,out,floorLight,overlay,LiteraryView.models().getBoolean("Scar"));}return;}
            if(kind==LiteraryPropBlock.Kind.CARCASS){carcass(carcass,state,net.minecraft.util.Mth.getSeed(e.getBlockPos()),p,out,light,overlay);return;}
            if(kind==LiteraryPropBlock.Kind.GUEST_BODY||kind==LiteraryPropBlock.Kind.CREW_BODY){corpse(corpse,state,net.minecraft.util.Mth.getSeed(e.getBlockPos()),kind==LiteraryPropBlock.Kind.CREW_BODY,p,out,light,overlay);return;}
            if(kind==LiteraryPropBlock.Kind.MINIATURE){var list=LiteraryView.models().getList("Models",Tag.TAG_COMPOUND);int index=Math.min(2,state.getValue(LiteraryPropBlock.STAGE));if(index>=list.size())return;var model=list.getCompound(index);miniature(model,mc.player.getSkin().texture(),mc.player.getSkin().model()==net.minecraft.client.resources.PlayerSkin.Model.SLIM?tinySlim:tiny,miniatureFigure,p,out,light,overlay);return;}
            if(kind==LiteraryPropBlock.Kind.TELEVISION){var view=LiteraryView.models();p.pushPose();p.translate(.5,0,.5);p.mulPose(Axis.YP.rotationDegrees(-state.getValue(LiteraryPropBlock.FACING).toYRot()));p.translate(-.5,0,-.5);screen(view.getIntArray("Screen"),view.getBoolean("ScreenDark"),view.getInt("ScreenStorm"),p,out);p.popPose();return;}
            String text=e.display().getString("Text");
            // The nursery wall: chalked large above the photograph, in letters the reader cannot miss.
            if(kind==LiteraryPropBlock.Kind.PHOTO&&state.getValue(LiteraryPropBlock.STAGE)==3){String[] lines=text.isEmpty()?new String[]{"HELP "+mc.player.getGameProfile().getName().toUpperCase(java.util.Locale.ROOT),"COME HOME"}:new String[]{text};
                p.pushPose();p.translate(.5,1.9,.5);p.mulPose(Axis.YP.rotationDegrees(-state.getValue(LiteraryPropBlock.FACING).toYRot()));p.translate(0,0,.502);p.scale(.026F,-.026F,.026F);
                for(int i=0;i<lines.length;i++)mc.font.drawInBatch(lines[i],-mc.font.width(lines[i])/2F,i*11,0xffe4dfd0,false,p.last().pose(),out,net.minecraft.client.gui.Font.DisplayMode.NORMAL,0,light);p.popPose();return;}
            if(kind==LiteraryPropBlock.Kind.TRUNK_MARK){if(text.isEmpty())text=mc.player.getGameProfile().getName()+", come home";p.pushPose();p.translate(.5,.6,.5);p.mulPose(Axis.YP.rotationDegrees(-state.getValue(LiteraryPropBlock.FACING).toYRot()));p.translate(0,0,.41);p.scale(.009F,-.009F,.009F);mc.font.drawInBatch(text,-mc.font.width(text)/2F,0,0xffd0bca0,false,p.last().pose(),out,net.minecraft.client.gui.Font.DisplayMode.NORMAL,0,light);p.popPose();}
        }
        /** The fan's image is a full elk on the actual room floor, using native model axes and the floor's light. */
        public static void fanElk(ElkModel elk,PoseStack p,MultiBufferSource out,int light,int overlay,boolean wounded){
            p.pushPose();fanElkPose(p);elk.dead(0,83);
            elk.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(texture(wounded?"elk_wounded":"elk"))),light,overlay,0xffffffff);p.popPose();
        }
        public static void fanElkPose(PoseStack p){p.translate(0,-LiteraryRooms.FAN.getY(),0);carcassPose(p,Direction.NORTH,0,0);}
        /** Elk carcasses are the jointed elk on its side (0-2, 2 skinned) or folded on its belly (3), never a block of hide. */
        public static void carcass(ElkModel elk,net.minecraft.world.level.block.state.BlockState s,long seed,PoseStack p,MultiBufferSource out,int light,int overlay){
            int stage=s.getValue(LiteraryPropBlock.STAGE);var r=new java.util.Random(seed);p.pushPose();
            carcassPose(p,s.getValue(LiteraryPropBlock.FACING),(r.nextFloat()-.5F)*40,stage);
            elk.dead(stage,seed);elk.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(texture(stage==2?"elk_carcass_skinned":"elk_carcass"))),light,overlay,0xffffffff);p.popPose();}
        /** From the block's corner to elk model space: on its side (0-2) with the body's flank on the floor, or on its belly (3). */
        public static void carcassPose(PoseStack p,Direction facing,float turn,int stage){p.translate(.5,0,.5);p.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()+turn));
            if(stage==3){p.scale(-1,-1,1);p.translate(0,-17/16F,0);}
            else{p.translate(0,5/16F,0);p.mulPose(Axis.ZP.rotationDegrees(stage==1?-90:90));p.scale(-1,-1,1);p.translate(0,-11/16F,0);}}
        /** From the block's corner to humanoid model space for a body: lying two blocks long, or seated over its block. */
        public static void corpsePose(PoseStack p,Direction facing,int stage){p.translate(.5,0,.5);p.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
            switch(stage){
                case 0->{p.translate(0,3/16F,-1);p.mulPose(Axis.XP.rotationDegrees(90));}
                case 1->{p.translate(0,3/16F,1);p.mulPose(Axis.XP.rotationDegrees(-90));}
                case 2->p.mulPose(Axis.YP.rotationDegrees(180));
                default->{p.translate(1,4/16F,0);p.mulPose(Axis.ZP.rotationDegrees(90));}
            }
            p.scale(-1,-1,1);p.translate(0,-1.501,0);}
        /** A passenger or one of the road crew: on the back (0), face down (1), slumped in a chair (2), curled (3). Two blocks long. */
        public static void corpse(CastModel m,net.minecraft.world.level.block.state.BlockState s,long seed,boolean crew,PoseStack p,MultiBufferSource out,int light,int overlay){
            int stage=s.getValue(LiteraryPropBlock.STAGE),variant=(int)Math.floorMod(seed>>>7,4);var facing=s.getValue(LiteraryPropBlock.FACING);
            if(stage==2)Minecraft.getInstance().getBlockRenderer().renderSingleBlock(io.github.knaitoe.theoldesthouse.house.HouseBlocks.HOUSEHOLD_FURNITURE.get().defaultBlockState().setValue(io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock.KIND,crew?io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock.Kind.KITCHEN_STOOL:io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock.Kind.CANE_CHAIR).setValue(io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock.FACING,facing),p,out,light,overlay);
            p.pushPose();corpsePose(p,facing,stage);m.corpse(crew,variant,stage);
            m.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(texture((crew?"crew_":"guest_")+variant))),light,overlay,0xffffffff);p.popPose();}
        /** Model-space feet are at y=1.5; place them on the sampled room's floor top. */
        public static void miniaturePersonPose(PoseStack p,int z){p.translate(4,1,z);p.scale(.35F,-.35F,-.35F);p.translate(0,-1.501,0);}
        public static void miniature(CompoundTag model,ResourceLocation playerSkin,HumanoidModel<?> tiny,CastModel figure,PoseStack p,MultiBufferSource out,int light,int overlay){var mc=Minecraft.getInstance();int[] blocks=model.getIntArray("Blocks");p.pushPose();p.translate(.12,.46,.12);p.scale(.09F,.09F,.09F);for(int i=0;i<Math.min(256,blocks.length);i++){var s=Block.stateById(blocks[i]);if(s.isAir())continue;p.pushPose();p.translate(i%8,i/64,(i/8)%8);mc.getBlockRenderer().renderSingleBlock(s,p,out,light,overlay);p.popPose();}tiny.young=false;tiny.riding=false;tiny.attackTime=0;for(var part:new ModelPart[]{tiny.head,tiny.body,tiny.leftArm,tiny.rightArm,tiny.leftLeg,tiny.rightLeg})part.getAllParts().forEach(ModelPart::resetPose);tiny.hat.copyFrom(tiny.head);if(tiny instanceof PlayerModel<?> player){player.jacket.copyFrom(player.body);player.leftSleeve.copyFrom(player.leftArm);player.rightSleeve.copyFrom(player.rightArm);player.leftPants.copyFrom(player.leftLeg);player.rightPants.copyFrom(player.rightLeg);}p.pushPose();miniaturePersonPose(p,4);tiny.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(playerSkin)),light,overlay,0xffffffff);p.popPose();if(model.getBoolean("Behind")){p.pushPose();miniaturePersonPose(p,6);figure.preview(LiteraryActor.SILHOUETTE,0,0);figure.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(texture("silhouette"))),light,overlay,0xff191719);p.popPose();}p.popPose();}
        public static void screen(int[] pixels,boolean dark,PoseStack pose,MultiBufferSource out){screen(pixels,dark,0,pose,out);}
        /** The picture under the reader's own storm: darker, bluer, with rain running down the glass as it grows. */
        public static int stormed(int rgb,int i,int storm,long time){if(storm<=0)return rgb;float k=Math.min(1,storm/100F)*.62F;int r=(int)((rgb>>16&255)*(1-k)+26*k),g=(int)((rgb>>8&255)*(1-k)+33*k),b=(int)((rgb&255)*(1-k)+44*k);
            int x=i%24,y=i/24;
            int waterline=16-Math.max(0,storm-28)*12/72;
            if(storm>28&&y>=waterline){int ripple=Math.floorMod(x+(int)(time/5)+y*3,7);r=23+ripple*2;g=56+ripple*3;b=79+ripple*4;}
            else if(storm>35&&Math.floorMod(x*7+y-(int)(time/2)*(1+x%3),11)==0){r=Math.min(255,r+40);g=Math.min(255,g+44);b=Math.min(255,b+52);}return r<<16|g<<8|b;}
        public static void screen(int[] pixels,boolean dark,int storm,PoseStack pose,MultiBufferSource out){if(pixels.length!=384)return;var vertices=out.getBuffer(RenderType.entityCutoutNoCull(ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png")));var matrix=pose.last().pose();long time=Minecraft.getInstance().level==null?0:Minecraft.getInstance().level.getGameTime();for(int i=0;i<384;i++){int rgb=dark?0x080a0b:stormed(pixels[i],i,storm,time);float x=.125F+(i%24)*(.625F/24),y=.125F+(15-i/24)*(.5F/16),right=x+.625F/24,top=y+.5F/16;for(var pt:new float[][]{{x,y},{right,y},{right,top},{x,top}})vertices.addVertex(matrix,pt[0],pt[1],.775F).setColor(rgb>>16&255,rgb>>8&255,rgb&255,255).setUv(.5F,.5F).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0,0,1);}}
        @Override public boolean shouldRenderOffScreen(LiteraryModelBlockEntity e){return e.getBlockState().is(LiteraryRegistry.PROP.get())&&switch(e.getBlockState().getValue(LiteraryPropBlock.KIND)){case FAN,CARCASS,GUEST_BODY,CREW_BODY->true;default->false;};}
        @Override public int getViewDistance(){return 48;}
        /** Carcasses and bodies reach past their own block; keep them drawn while any of them is in view. */
        @Override public net.minecraft.world.phys.AABB getRenderBoundingBox(LiteraryModelBlockEntity e){
            if(e.getBlockState().is(LiteraryRegistry.PROP.get())&&e.getBlockState().getValue(LiteraryPropBlock.KIND)==LiteraryPropBlock.Kind.FAN)
                return new net.minecraft.world.phys.AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(e.getBlockPos().offset(-3,-LiteraryRooms.FAN.getY(),-3)),net.minecraft.world.phys.Vec3.atLowerCornerOf(e.getBlockPos().offset(4,3,4)));
            return new net.minecraft.world.phys.AABB(e.getBlockPos()).inflate(2);
        }
    }
}
