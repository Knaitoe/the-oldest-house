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
    public static final ModelLayerLocation CAST=layer("literary_cast"),ELK=layer("literary_elk"),ELK_EYES=layer("literary_elk_eyes"),CRUTCH=layer("rabbit_crutch"),FAN=layer("literary_fan"),WITCH=layer("hollow_witch"),WOLF=layer("hollow_wolf");
    private static ModelLayerLocation layer(String name){return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,name),"main");}
    private static ResourceLocation texture(String name){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/literary_"+name+".png");}
    public static ResourceLocation skin(int role,int phase){if(role==LiteraryActor.STRANGER&&BikeNightSpecters.cast(phase))return texture(BikeNightSpecters.skin(phase));if(role==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.CalderPeople.cast(phase))return texture(io.github.knaitoe.theoldesthouse.labyrinth.CalderPeople.skin(phase));if(role==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.BikeNightGus.cast(phase))return texture(io.github.knaitoe.theoldesthouse.labyrinth.BikeNightGus.skin(phase));if(role==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.BikeNightBullies.cast(phase))return texture(io.github.knaitoe.theoldesthouse.labyrinth.BikeNightBullies.skin(phase));if(HouseReadShelter.role(role))return texture("read_"+(role==LiteraryActor.READ_MAN?"man":"woman")+switch(Math.min(2,phase>>1)){case 1->"_40";case 2->"_old";default->"";}+((phase&1)==1?"_smile":""));if(role==LiteraryActor.LAKE_TESS)return texture("lake_tess");if(role==LiteraryActor.LAKE_THEO)return texture("lake_theo");if(role==LiteraryActor.LAKE_WAX)return texture("lake_wax");String[] roles={"mother","dancer","red_figure","coffin_woman","lady","silhouette","brother","father","visitor","killer","family_father","family_mother","family_child","old_man","stranger","camera"};String name=roles[Math.max(0,Math.min(15,role))];if(role==LiteraryActor.SILHOUETTE&&phase==4)return texture("plain_boy");if(role==LiteraryActor.SILHOUETTE&&phase==5)return texture("quiet_walker");if(role==LiteraryActor.SILHOUETTE&&phase==6)return texture("devils_double");if(role==LiteraryActor.STRANGER&&phase>=4&&phase<4+BAR.length)return texture("bar_"+BAR[phase-4]);if(role==LiteraryActor.STRANGER&&phase>=9&&phase<9+COTTAGE.length)return texture("paw_"+COTTAGE[phase-9]);if(role==LiteraryActor.STRANGER&&phase>=14&&phase<14+MOVIE.length)return texture("movie_"+MOVIE[phase-14]);if(role==LiteraryActor.STRANGER&&phase>=21&&phase<21+COSTUME.length)return texture("costume_"+COSTUME[phase-21]);if(role==LiteraryActor.STRANGER&&phase>=34&&phase<34+WINTER.length)return texture("winter_"+WINTER[phase-34]);if(role==LiteraryActor.STRANGER&&phase>=38&&phase<38+USHER.length)return texture("usher_"+USHER[phase-38]);if(role==LiteraryActor.STRANGER&&phase>=39&&phase<39+CLOSING.length)return texture("closing_"+CLOSING[phase-39]);if(role==LiteraryActor.STRANGER&&phase>=41&&phase<41+POLYBIUS.length)return texture("polybius_"+POLYBIUS[phase-41]);if(role==LiteraryActor.STRANGER&&phase==43)return texture("replacement");if(role==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.PittGame.bradPhase(phase))return texture("pitt_brad");if(role==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.Polybius.regular(phase))return texture("polybius_kid_"+(char)('a'+phase-io.github.knaitoe.theoldesthouse.labyrinth.Polybius.REGULAR_PHASE));if(role==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.Polybius.teen(phase))return texture("polybius_teen");if(role==LiteraryActor.STRANGER&&phase==io.github.knaitoe.theoldesthouse.labyrinth.VacantPossession.TENANT)return texture("vacant_tenant");if(role==LiteraryActor.STRANGER&&phase==io.github.knaitoe.theoldesthouse.labyrinth.LastSwimmer.ATTENDANT)return texture("swimmer_attendant");if(role==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.LastSwimmer.swimmer(phase))return texture("swimmer_swimmer");if(role==LiteraryActor.STRANGER&&phase==io.github.knaitoe.theoldesthouse.labyrinth.WindowCleaner.CLEANER)return texture("window_cleaner");if(role==LiteraryActor.STRANGER&&phase==io.github.knaitoe.theoldesthouse.labyrinth.NightOperator.OPERATOR)return texture("night_operator");if(role==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.PeopleLights.kelly(phase))return texture("people_kelly");if(role==LiteraryActor.STRANGER&&phase==io.github.knaitoe.theoldesthouse.labyrinth.PeopleLights.MARK)return texture("people_mark");if(role==LiteraryActor.STRANGER&&phase==io.github.knaitoe.theoldesthouse.labyrinth.PeopleLights.STAN)return texture("people_stan");if(role==LiteraryActor.STRANGER&&phase==io.github.knaitoe.theoldesthouse.labyrinth.PlaceForYou.MARA)return texture("place_mara");if(role==LiteraryActor.STRANGER&&phase==io.github.knaitoe.theoldesthouse.labyrinth.PlaceForYou.KIT)return texture("place_kit");if(role==LiteraryActor.STRANGER&&(io.github.knaitoe.theoldesthouse.labyrinth.PlaceForYou.alex(phase)||io.github.knaitoe.theoldesthouse.labyrinth.PlaceForYou.ren(phase))){int age=io.github.knaitoe.theoldesthouse.labyrinth.PlaceForYou.alex(phase)?phase-io.github.knaitoe.theoldesthouse.labyrinth.PlaceForYou.ALEX:phase-io.github.knaitoe.theoldesthouse.labyrinth.PlaceForYou.REN;return texture("read_"+(io.github.knaitoe.theoldesthouse.labyrinth.PlaceForYou.alex(phase)?"man":"woman")+(age==1?"_40":age==2?"_old":""));}if(role==LiteraryActor.STRANGER&&phase==io.github.knaitoe.theoldesthouse.labyrinth.NightOperator.OPERATOR_BARE)return texture("night_operator_bare");if(role==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.Truant.phase(phase))return texture(phase==io.github.knaitoe.theoldesthouse.labyrinth.Truant.LEER?"truant_leer":"truant");if(role==LiteraryActor.STRANGER&&phase==io.github.knaitoe.theoldesthouse.labyrinth.SecondPerson.IRIS)return texture("flat_iris");if(role==LiteraryActor.STRANGER&&phase==io.github.knaitoe.theoldesthouse.labyrinth.SecondPerson.SAM)return texture("flat_sam");if(role==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.KitchenVisitors.visitor(phase))return texture(switch(phase){case io.github.knaitoe.theoldesthouse.labyrinth.KitchenVisitors.JOHNNY->"truant";case io.github.knaitoe.theoldesthouse.labyrinth.KitchenVisitors.HOLLOWAY->"kitchen_holloway";case io.github.knaitoe.theoldesthouse.labyrinth.KitchenVisitors.TENANT->"vacant_tenant";default->"stranger_1";});if(role==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.SpeakUp.diner(phase))return texture(io.github.knaitoe.theoldesthouse.labyrinth.SpeakUp.skin(phase));if(role==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.MarshSupper.cast(phase))return texture(io.github.knaitoe.theoldesthouse.labyrinth.MarshSupper.skin(phase));if(role==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.Drawing.cast(phase))return texture(io.github.knaitoe.theoldesthouse.labyrinth.Drawing.skin(phase));if(role==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.Corner.cast(phase))return texture(io.github.knaitoe.theoldesthouse.labyrinth.Corner.skin(phase));if(role==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.Kept.cast(phase))return texture(io.github.knaitoe.theoldesthouse.labyrinth.Kept.skin(phase));if(role==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.LastOrders.cast(phase))return texture(io.github.knaitoe.theoldesthouse.labyrinth.LastOrders.skin(phase));if(role==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.BorrascaPeople.cast(phase))return texture(io.github.knaitoe.theoldesthouse.labyrinth.BorrascaPeople.skin(phase));if(role==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.LastCrossing.cast(phase))return texture(io.github.knaitoe.theoldesthouse.labyrinth.LastCrossing.skin(phase));if(role==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.Pinehook.cast(phase))return texture(io.github.knaitoe.theoldesthouse.labyrinth.Pinehook.skin(phase));if(role==LiteraryActor.STRANGER&&phase==io.github.knaitoe.theoldesthouse.labyrinth.TallGrass.DRIVER)return texture("grass_driver");if(role==LiteraryActor.STRANGER)name+="_"+Math.min(3,phase);else if(role==LiteraryActor.SILHOUETTE&&phase>0)name+="_"+Math.min(3,phase);else if(role==LiteraryActor.FATHER&&phase>=2)name+=phase>=3?"_mangled":"_bandaged";else if(role>=LiteraryActor.FAMILY_FATHER&&role<=LiteraryActor.FAMILY_CHILD&&phase>0)name+="_changed";return texture(name);}
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(CAST,CastModel::layer);e.registerLayerDefinition(UncannyActorRenderer.BENT,UncannyActorRenderer::bentLayer);e.registerLayerDefinition(UncannyActorRenderer.WIDE,()->UncannyActorRenderer.layer(false));e.registerLayerDefinition(UncannyActorRenderer.SLIM,()->UncannyActorRenderer.layer(true));e.registerLayerDefinition(PlainFigureModel.LAYER,PlainFigureModel::layer);e.registerLayerDefinition(CRUTCH,CrutchLayer::layer);e.registerLayerDefinition(WITCH,WitchLayer::layer);e.registerLayerDefinition(WOLF,WolfLayer::layer);e.registerLayerDefinition(GhostsCastModel.LAYER,GhostsCastModel::layer);e.registerLayerDefinition(ELK,ElkModel::layer);e.registerLayerDefinition(ELK_EYES,ElkEyes::layer);e.registerLayerDefinition(FAN,ModelRenderer::fanLayer);e.registerLayerDefinition(LookoutRenderers.WHISTLER,LookoutRenderers::layer);}
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
    /** Usher (0.4.182): Roderick on stranger phase thirty-eight. */
    static final String[] USHER={"roderick"};
    static final String[] CLOSING={"chad","daisy"};
    /** Polybius (0.4.217): the two men in suits by the black cabinet, on stranger phases forty-one and forty-two. */
    static final String[] POLYBIUS={"a","b"};
    /** A seat an actor has been placed in: how high it holds the hips, and which way it faces. */
    record Seat(int seatHeight,net.minecraft.core.Direction facing){}
    /** The household, roadhouse or lawn-chair seat an actor has been placed in, if any. */
    static Seat seat(LiteraryActor a){
        var state=a.level().getBlockState(a.blockPosition());
        if(state.is(io.github.knaitoe.theoldesthouse.house.HouseBlocks.HOUSEHOLD_FURNITURE.get())){var kind=state.getValue(io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock.KIND);return kind.seat?new Seat(kind.seatHeight,state.getValue(io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock.FACING)):null;}
        if(state.is(io.github.knaitoe.theoldesthouse.labyrinth.LiteraryRegistry.ROADHOUSE.get())){var kind=state.getValue(io.github.knaitoe.theoldesthouse.labyrinth.RoadhouseBlock.KIND);return kind.seat()?new Seat(kind.seatHeight,state.getValue(io.github.knaitoe.theoldesthouse.labyrinth.RoadhouseBlock.FACING)):null;}
        if(io.github.knaitoe.theoldesthouse.labyrinth.MoviePropBlock.is(state,io.github.knaitoe.theoldesthouse.labyrinth.MoviePropBlock.Kind.LAWN_CHAIR))return new Seat(5,state.getValue(io.github.knaitoe.theoldesthouse.labyrinth.MoviePropBlock.FACING));
        // Brad's kit box in the God of the Pitt (0.4.220), eight pixels high.
        if(io.github.knaitoe.theoldesthouse.labyrinth.PittBlock.is(state,io.github.knaitoe.theoldesthouse.labyrinth.PittBlock.Kind.BRAD))return new Seat(8,state.getValue(io.github.knaitoe.theoldesthouse.labyrinth.PittBlock.FACING));
        // Kelly in her chair, or on the floor by the sofa; anyone in an empty wheelchair (0.4.253).
        if(a.role()==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.PeopleLights.inChair(a.phase()))return new Seat(8,null);
        if(a.role()==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.PeopleLights.onFloor(a.phase()))return new Seat(1,null);
        if(io.github.knaitoe.theoldesthouse.labyrinth.PeopleLightsBlock.is(state,io.github.knaitoe.theoldesthouse.labyrinth.PeopleLightsBlock.Kind.CHAIR))return new Seat(8,state.getValue(io.github.knaitoe.theoldesthouse.labyrinth.PeopleLightsBlock.FACING));
        // The last chair in Vacant Possession (0.4.228), its seat eight pixels up.
        if(io.github.knaitoe.theoldesthouse.labyrinth.VacantBlock.is(state,io.github.knaitoe.theoldesthouse.labyrinth.VacantBlock.Kind.CHAIR))return new Seat(8,state.getValue(io.github.knaitoe.theoldesthouse.labyrinth.VacantBlock.FACING));
        // The familiar kitchen's chairs (0.4.331), where its visitors sit: the seat ten pixels up.
        if(io.github.knaitoe.theoldesthouse.labyrinth.TraceBlock.is(state,io.github.knaitoe.theoldesthouse.labyrinth.TraceBlock.Kind.CHAIR))return new Seat(10,state.getValue(io.github.knaitoe.theoldesthouse.labyrinth.TraceBlock.FACING));
        // Roderick's chair (0.4.182), its seat nine pixels up.
        if(io.github.knaitoe.theoldesthouse.labyrinth.UsherBlock.is(state,io.github.knaitoe.theoldesthouse.labyrinth.UsherBlock.Kind.CHAIR))return new Seat(9,state.getValue(io.github.knaitoe.theoldesthouse.labyrinth.UsherBlock.FACING));
        // 0.4.444: Earl, years later, on a flattened box in the alley beside the Tipple.
        if(a.role()==LiteraryActor.STRANGER&&a.phase()==io.github.knaitoe.theoldesthouse.labyrinth.BorrascaPeople.EARL&&state.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock)return new Seat(1,null);
        return null;
    }
    public static final class CastModel extends HumanoidModel<LiteraryActor>{private final ModelPart coat,gown,mask,bandage,hood,lakeHair,eyelids;private int shelterGesture;
        /** 0.4.228: how solid the actor is drawn (1 solid); set by the renderer for a faded actor and put back after. */
        float alpha=1;
        @Override public void renderToBuffer(PoseStack p,VertexConsumer b,int light,int overlay,int color){super.renderToBuffer(p,b,light,overlay,alpha<1?net.minecraft.util.FastColor.ARGB32.color(Math.max(0,Math.min(255,(int)(alpha*255))),color):color);}
        private final LakeHouseHair lakeHairMotion;
        /** The elk killer's own tailoring (0.4.50) and the crew's hard hats; hidden on everyone else. */
        private final ModelPart killerHood,killerMask,killerCoat,killerBelt,sleeveRight,sleeveLeft,tailRight,tailLeft,bootRight,bootLeft,toeRight,toeLeft,axe,hardHat;
        private float walk,amount;
        public CastModel(ModelPart r){super(r);coat=body.getChild("coat");gown=body.getChild("gown");mask=head.getChild("mask");eyelids=head.getChild("eyelids");bandage=leftLeg.getChild("bandage");hood=head.getChild("hood");
            lakeHair=head.getChild("lake_hair");lakeHairMotion=new LakeHouseHair(lakeHair);killerHood=head.getChild("killer_hood");killerMask=head.getChild("killer_mask");hardHat=head.getChild("hard_hat");killerCoat=body.getChild("killer_coat");killerBelt=body.getChild("killer_belt");
            sleeveRight=rightArm.getChild("killer_sleeve");sleeveLeft=leftArm.getChild("killer_sleeve");axe=rightArm.getChild("killer_axe");
            tailRight=rightLeg.getChild("killer_tail");tailLeft=leftLeg.getChild("killer_tail");bootRight=rightLeg.getChild("killer_boot");bootLeft=leftLeg.getChild("killer_boot");toeRight=rightLeg.getChild("killer_toe");toeLeft=leftLeg.getChild("killer_toe");}
        public static LayerDefinition layer(){var m=HumanoidModel.createMesh(CubeDeformation.NONE,0);var r=m.getRoot();r.getChild("body").addOrReplaceChild("coat",CubeListBuilder.create().texOffs(64,28).addBox(-4.4F,1,-2.4F,8.8F,17,4.8F),PartPose.ZERO);r.getChild("body").addOrReplaceChild("gown",CubeListBuilder.create().texOffs(96,28).addBox(-5,9,-3,10,14,6,new CubeDeformation(.2F)),PartPose.ZERO);r.getChild("head").addOrReplaceChild("eyelids",CubeListBuilder.create().texOffs(8,13).addBox(-4,-4,-4.03F,8,1,0),PartPose.ZERO);r.getChild("head").addOrReplaceChild("mask",CubeListBuilder.create().texOffs(64,0).addBox(-4.15F,-8.2F,-4.5F,8.3F,8.5F,1),PartPose.ZERO);r.getChild("head").addOrReplaceChild("hood",CubeListBuilder.create().texOffs(82,0).addBox(-4.4F,-8.4F,-4.4F,8.8F,8.8F,8.8F),PartPose.ZERO);r.getChild("left_leg").addOrReplaceChild("bandage",CubeListBuilder.create().texOffs(112,0).addBox(-2,5,-2,4,5,4,new CubeDeformation(.2F)),PartPose.ZERO);r.addOrReplaceChild("herd_eyes",CubeListBuilder.create().texOffs(0,0).addBox(-4,0,0,1,1,1).addBox(3,0,0,1,1,1),PartPose.ZERO);
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
        private void dress(int role,int phase){leftArm.visible=!(role==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.Corner.oneArm(phase));boolean bar=role==LiteraryActor.STRANGER&&phase>=4&&phase<4+BAR.length;int cottage=role==LiteraryActor.STRANGER&&phase>=9&&phase<9+COTTAGE.length?phase-9:-1;int movie=role==LiteraryActor.STRANGER&&phase>=14&&phase<14+MOVIE.length?phase-14:-1;int costume=role==LiteraryActor.STRANGER&&phase>=21&&phase<21+COSTUME.length?phase-21:-1;int winter=role==LiteraryActor.STRANGER&&phase>=34&&phase<34+WINTER.length?phase-34:-1;boolean closing=role==LiteraryActor.STRANGER&&phase>=39&&phase<39+CLOSING.length;coat.visible=role==LiteraryActor.FATHER||role==LiteraryActor.FAMILY_FATHER||role==LiteraryActor.OLD_MAN||role==LiteraryActor.VISITOR||role==LiteraryActor.STRANGER&&cottage<0&&movie<0&&costume<0&&winter<0&&!closing&&(!bar||phase==4)||cottage==0||cottage==3||cottage==4;gown.visible=role==LiteraryActor.LADY||role==LiteraryActor.READ_WOMAN||role==LiteraryActor.COFFIN_WOMAN||role==LiteraryActor.FAMILY_MOTHER||cottage==1||movie==3||costume==1||costume==6||costume==10||costume==12||role==LiteraryActor.LAKE_WAX||role==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.CalderPeople.cast(phase)&&io.github.knaitoe.theoldesthouse.labyrinth.CalderPeople.gown(phase);mask.visible=role==LiteraryActor.DANCER||role==LiteraryActor.RED_FIGURE;hood.visible=false;bandage.visible=role==LiteraryActor.FATHER&&phase>=2;
            boolean killer=role==LiteraryActor.KILLER;for(var part:new ModelPart[]{killerHood,killerMask,killerCoat,killerBelt,sleeveRight,sleeveLeft,tailRight,tailLeft,bootRight,bootLeft,toeRight,toeLeft,axe})part.visible=killer;hardHat.visible=role==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.PittGame.bradPhase(phase);lakeHair.visible=role==LiteraryActor.LAKE_TESS;hat.visible=!killer&&role!=LiteraryActor.LAKE_TESS;}
        public void pose(int role,int phase,float age){eyelids.visible=false;body.zScale=1;dress(role,phase);
            if(HouseReadShelter.role(role)){
                boolean man=role==LiteraryActor.READ_MAN;float sway=(float)Math.sin(age*.06F);
                switch(shelterGesture){
                    // 0.4.172 (owner: "Fix the posture"): the rail lean only on the rail. Off it they stand and walk as anyone does,
                    // hold out a hand, dance, and step back after.
                    case HouseReadShelter.STAND->{body.yRot+=sway*.04F;}
                    case HouseReadShelter.HAND->{rightArm.xRot=-1.1F;rightArm.yRot=-.18F;rightArm.zRot=0;leftArm.xRot=.05F;leftArm.yRot=0;leftArm.zRot=-.06F;head.xRot=.06F;body.yRot=sway*.03F;}
                    case HouseReadShelter.DANCE->{
                        // A slow dance: both hands up to the partner's shoulders, swaying from foot to foot.
                        body.yRot=sway*.12F;body.zRot=sway*.03F;
                        rightArm.xRot=leftArm.xRot=-1.3F;rightArm.yRot=-.32F+body.yRot;leftArm.yRot=.32F+body.yRot;rightArm.zRot=leftArm.zRot=0;
                        rightLeg.xRot=sway*.12F;leftLeg.xRot=-sway*.12F;rightLeg.yRot=leftLeg.yRot=0;head.zRot=man?.07F:-.07F;}
                    case HouseReadShelter.AFTER->{rightArm.xRot=leftArm.xRot=-.42F;rightArm.yRot=leftArm.yRot=0;rightArm.zRot=-.28F;leftArm.zRot=.28F;head.xRot=.05F;}
                    default->{
                        // 0.4.159: leaning back on the rail with both elbows on the wood, the two mirror images, moving the same small amount.
                        float drift=(float)Math.sin(age*.027F)*.04F;
                        body.xRot=-.12F;body.y+=(float)Math.sin(age*.035F)*.06F;head.xRot=.08F;head.yRot=man?.32F:-.32F;
                        leftArm.xRot=.5F;rightArm.xRot=.5F;leftArm.zRot=-.2F;rightArm.zRot=.2F;
                        if(man){rightArm.xRot=.42F+drift;}else{leftArm.xRot=.42F+drift;}
                        if((phase&1)==1){head.zRot=man?.035F:-.035F;head.yRot=0;}
                    }
                }
            }
            if(role==LiteraryActor.DANCER&&phase==1){leftArm.xRot=rightArm.xRot=-.8F;leftArm.zRot=-.55F;rightArm.zRot=.55F;body.yRot=(float)Math.sin(age*.045)*.18F;leftLeg.xRot=(float)Math.sin(age*.08)*.22F;rightLeg.xRot=-leftLeg.xRot;}
            if(role==LiteraryActor.OLD_MAN){body.xRot=.19F;head.xRot=.14F;leftArm.xRot=rightArm.xRot=-.1F;}
            if(role==LiteraryActor.FATHER)father(phase,age);
            if(role==LiteraryActor.BROTHER&&phase>0)asleep(age);
            if(role==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.PeopleLights.inBed(phase))asleep(age);
            // 0.4.253: Kelly asleep on the floor, pulling herself along by her arms, one and then the other; her legs trail; her face slack.
            if(role==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.PeopleLights.dragging(phase)){float pull=age*.05F;body.xRot=0;head.xRot=-.3F;head.zRot=.12F;rightArm.xRot=-2.7F+(float)Math.sin(pull)*.65F;leftArm.xRot=-2.7F-(float)Math.sin(pull)*.65F;rightArm.zRot=.22F;leftArm.zRot=-.22F;leftLeg.xRot=rightLeg.xRot=0;leftLeg.yRot=rightLeg.yRot=0;leftLeg.zRot=-.04F;rightLeg.zRot=.04F;hat.copyFrom(head);}
            if(role==LiteraryActor.KILLER)killer(phase,age);
            // 0.4.227: the last swimmer swims the same crawl as Tess, slower.
            if(role==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.LastSwimmer.swimmer(phase)){float stroke=age*.07F;body.xRot=0;head.xRot=-.8F;leftArm.xRot=rightArm.xRot=-2.65F+(float)Math.sin(stroke)*.55F;leftArm.zRot=-.18F-Math.max(0,(float)Math.cos(stroke))*.5F;rightArm.zRot=-leftArm.zRot;leftLeg.xRot=(float)Math.sin(age*.14)*.18F;rightLeg.xRot=-leftLeg.xRot;hat.copyFrom(head);}
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
        @Override public void setupAnim(LiteraryActor a,float walk,float amount,float age,float yaw,float pitch){head.getAllParts().forEach(ModelPart::resetPose);body.getAllParts().forEach(ModelPart::resetPose);leftLeg.getAllParts().forEach(ModelPart::resetPose);rightLeg.getAllParts().forEach(ModelPart::resetPose);leftArm.getAllParts().forEach(ModelPart::resetPose);rightArm.getAllParts().forEach(ModelPart::resetPose);this.walk=walk;this.amount=amount;crouching=a.getPose()==net.minecraft.world.entity.Pose.CROUCHING||(a.role()==LiteraryActor.KILLER&&crouches(a.phase()));super.setupAnim(a,walk,amount,age,yaw,pitch);shelterGesture=HouseReadShelter.role(a.role())?a.gesture():HouseReadShelter.LEAN;pose(a.role(),a.phase(),age);
            if(a.role()==LiteraryActor.FATHER){switch(a.gesture()){
                case 1->{rightArm.xRot=-1.45F;rightArm.yRot=-.45F;rightArm.zRot=.18F;head.yRot-=.25F;}
                case 2->{leftArm.xRot=rightArm.xRot=-1.2F;leftArm.yRot=.3F;rightArm.yRot=-.3F;body.xRot+=.12F;head.xRot+=.2F;}
                case 3->{rightArm.xRot=-.45F+(float)Math.sin(age*.12F)*.08F;rightArm.zRot=.18F;head.xRot+=.18F;}
                default->{}
            }hat.copyFrom(head);}
            if(a.reaching())reaching();
            if(a.role()==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.Truant.phase(a.phase()))truant(a.gesture(),age);
            if(a.role()==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.Pinehook.cast(a.phase()))pinehook(a.gesture(),age);
            if(a.role()==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.Drawing.cast(a.phase()))drawing(a.gesture(),age);
            if(a.role()==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.Corner.cast(a.phase()))corner(a.gesture(),age);
            if(a.role()==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.BikeNightGus.cast(a.phase()))gus(a,a.gesture(),age);
            if(a.role()==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.BikeNightBullies.cast(a.phase()))bully(a,a.gesture(),age);
            if(a.role()==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.CalderPeople.cast(a.phase()))people(a,a.gesture(),age);
            if(a.role()==LiteraryActor.STRANGER&&BikeNightSpecters.cast(a.phase())&&a.gesture()==BikeNightSpecters.FLY)astride(age);
            if(a.role()==LiteraryActor.STRANGER&&BikeNightSpecters.cast(a.phase())&&a.gesture()==BikeNightSpecters.DRIFT)adrift(age+a.getId()*7);
            if(a.role()==LiteraryActor.STRANGER&&a.phase()==BikeNightSpecters.WOLF&&a.gesture()>=BikeNightSpecters.RUN)onFour(a.gesture(),age);
            if(a.role()==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.Kept.cast(a.phase()))kept(a.gesture(),age,a.getId());
            // 0.4.221: the arcade's regulars, hands on the stick and the buttons, working them.
            // 0.4.224: the pizza counter's teenager, slumped on the counter, chin on a fist, the other hand flat on a magazine, head tipped.
            if(a.role()==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.Polybius.teen(a.phase())){rightArm.xRot=-2.1F;rightArm.yRot=.35F;rightArm.zRot=.15F;leftArm.xRot=-1.15F;leftArm.yRot=-.1F;head.xRot=.32F+net.minecraft.util.Mth.sin(age*.05F)*.03F;head.zRot=-.22F;body.xRot=.22F;rightLeg.xRot=-.1F;}
            if(a.role()==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.Polybius.regular(a.phase())){rightArm.xRot=-1.05F+net.minecraft.util.Mth.sin(age*.9F)*.08F;leftArm.xRot=-1.05F+net.minecraft.util.Mth.cos(age*1.3F)*.06F;rightArm.yRot=-.18F;leftArm.yRot=.18F;head.xRot=.08F;}
            if(a.role()==LiteraryActor.LAKE_TESS)lakeHairMotion.pose(a,age);
            // An actor placed in a chair sits in it: hips on the seat, legs forward.
            var chair=seat(a);if(chair!=null){float lower=12-chair.seatHeight();for(var part:new ModelPart[]{head,body,leftArm,rightArm,leftLeg,rightLeg})part.y+=lower;leftLeg.xRot=rightLeg.xRot=-1.45F;leftLeg.yRot=.1F;rightLeg.yRot=-.1F;leftArm.xRot=rightArm.xRot=-.35F;hat.copyFrom(head);}
            if(a.role()==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.LastOrders.cast(a.phase()))orders(a.gesture(),age,a.getId());
            if(a.role()==LiteraryActor.BROTHER||a.role()==LiteraryActor.FAMILY_CHILD||a.role()==LiteraryActor.SILHOUETTE&&a.phase()==4){
                var motion=HumanoidMotion.sample(a,age-a.tickCount);
                if(chair==null&&!(a.role()==LiteraryActor.BROTHER&&a.phase()>0))HumanoidMotion.gait(this,motion.phase(),motion.amount(),motion.running());
                if(!(a.role()==LiteraryActor.BROTHER&&a.phase()>0))body.yRot+=.02F*net.minecraft.util.Mth.sin(age*.037F+a.getId()*1.7F);HumanoidMotion.connect(this);
            }
            if(a.crawling())crawl(walk,amount);
            if(a.role()==LiteraryActor.SILHOUETTE&&a.scene().equals(LabyrinthPlace.CHILD_ROOM.id())&&a.hasCustomName()&&"Dinnerbone".equals(a.getCustomName().getString()))DinnerboneIdle.pose(this,a.level().getGameTime()+age-a.tickCount,a.getUUID().hashCode());RecentActorMotion.blend(a,age,this,coat,gown,mask,bandage,hood,lakeHair);}
        /**
         * 0.4.410, the Kept: sat on the floor with the knees drawn up and the arms round them; walking hunched, head down, arms hanging; a
         * hand flat on a cell's door, the head bowed to it; both hands to the face; both hands out toward the crate. Thin and slow in all of it.
         */
        void kept(int gesture,float age,int id){
            float sway=net.minecraft.util.Mth.sin(age*.06F+id)*.03F;
            switch(gesture){
                case io.github.knaitoe.theoldesthouse.labyrinth.Kept.SIT->{for(var part:new ModelPart[]{head,body,leftArm,rightArm,leftLeg,rightLeg})part.y+=10;leftLeg.xRot=rightLeg.xRot=-2.3F;leftLeg.yRot=.25F;rightLeg.yRot=-.25F;leftArm.xRot=rightArm.xRot=-1.6F;leftArm.yRot=.45F;rightArm.yRot=-.45F;body.xRot=.35F;head.xRot=.45F+sway;}
                case io.github.knaitoe.theoldesthouse.labyrinth.Kept.HUNCH->{body.xRot+=.3F;head.xRot+=.45F;head.y+=1.2F;leftArm.xRot+=.15F;rightArm.xRot+=.15F;leftArm.zRot=.06F;rightArm.zRot=-.06F;}
                case io.github.knaitoe.theoldesthouse.labyrinth.Kept.HAND->{rightArm.xRot=-1.6F;rightArm.yRot=-.1F;rightArm.zRot=0;leftArm.xRot=.05F;head.xRot=.4F+sway;body.xRot=.12F;}
                case io.github.knaitoe.theoldesthouse.labyrinth.Kept.WEEP->{rightArm.xRot=leftArm.xRot=-2.0F;rightArm.yRot=-.55F;leftArm.yRot=.55F;head.xRot=.55F+net.minecraft.util.Mth.sin(age*.5F)*.04F;body.xRot=.3F;}
                case io.github.knaitoe.theoldesthouse.labyrinth.Kept.REACH->{rightArm.xRot=leftArm.xRot=-1.15F+sway;rightArm.yRot=-.15F;leftArm.yRot=.15F;body.xRot=.18F;head.xRot=.3F;}
                default->{body.xRot+=.15F;head.xRot+=.25F;}
            }
            hat.copyFrom(head);
        }
        /**
         * 0.4.416, Last orders: a dart drawn back by the ear; staring across the table, arms on it, the head not moving at all; bent low
         * over the cloth along a cue, drawing it back and forward; a hand at the mouth to whistle; the next booth's two leaning in over
         * their table; both hands laid flat on the table.
         */
        void orders(int gesture,float age,int id){
            float sway=net.minecraft.util.Mth.sin(age*.07F+id)*.03F;
            switch(gesture){
                case io.github.knaitoe.theoldesthouse.labyrinth.LastOrders.THROW->{rightArm.xRot=-2.5F+sway;rightArm.yRot=-.15F;rightArm.zRot=0;leftArm.xRot=-.5F;leftArm.yRot=.2F;body.yRot=-.12F;head.xRot=-.05F;}
                case io.github.knaitoe.theoldesthouse.labyrinth.LastOrders.STARE->{leftArm.xRot=rightArm.xRot=-1.15F;leftArm.yRot=.18F;rightArm.yRot=-.18F;head.xRot=.08F;head.yRot=0;head.zRot=0;}
                case io.github.knaitoe.theoldesthouse.labyrinth.LastOrders.CUE->{body.xRot=.5F;body.y=3.2F;head.y=4.2F;leftArm.y=rightArm.y=5.2F;leftLeg.z=rightLeg.z=4F;leftLeg.y=rightLeg.y=12.2F;head.xRot=-.3F;leftArm.xRot=-1.45F;leftArm.yRot=.35F;rightArm.xRot=-.65F+net.minecraft.util.Mth.sin(age*.25F)*.18F;rightArm.yRot=-.1F;}
                case io.github.knaitoe.theoldesthouse.labyrinth.LastOrders.WHISTLE->{rightArm.xRot=-2.1F;rightArm.yRot=-.5F;rightArm.zRot=0;head.xRot=-.18F;}
                case io.github.knaitoe.theoldesthouse.labyrinth.LastOrders.LEAN->{body.xRot+=.18F;head.xRot=.32F+sway;head.y+=.8F;leftArm.xRot=rightArm.xRot=-1.0F;leftArm.yRot=.12F;rightArm.yRot=-.12F;}
                case io.github.knaitoe.theoldesthouse.labyrinth.LastOrders.HANDS->{leftArm.xRot=rightArm.xRot=-1.4F;leftArm.yRot=.15F;rightArm.yRot=-.15F;head.xRot=.12F;}
                default->{}
            }
            hat.copyFrom(head);
        }
        /** 0.4.479, Bike night's three: on a bike, hands on the bars and legs going round with its cranks; pointing; coughing into both hands; kicking sand; arms folded; reaching. */
        void bully(LiteraryActor a,int gesture,float age){
            switch(gesture){
                case io.github.knaitoe.theoldesthouse.labyrinth.BikeNightBullies.RIDE->onBike(a);
                case io.github.knaitoe.theoldesthouse.labyrinth.BikeNightBullies.POINT->{rightArm.xRot=-1.5F;rightArm.yRot=-.1F;rightArm.zRot=0;}
                case io.github.knaitoe.theoldesthouse.labyrinth.BikeNightBullies.COUGH->{rightArm.xRot=leftArm.xRot=-2.25F;rightArm.yRot=-.55F;leftArm.yRot=.55F;rightArm.zRot=leftArm.zRot=0;head.xRot=.45F+net.minecraft.util.Mth.sin(age*1.4F)*.12F;body.xRot=.2F;}
                case io.github.knaitoe.theoldesthouse.labyrinth.BikeNightBullies.KICK->{rightLeg.xRot=-1.1F+net.minecraft.util.Mth.sin(age*.9F)*.3F;leftArm.xRot=-.4F;rightArm.xRot=.4F;}
                case io.github.knaitoe.theoldesthouse.labyrinth.BikeNightBullies.ARMS->{rightArm.xRot=leftArm.xRot=-.95F;rightArm.yRot=-.75F;leftArm.yRot=.75F;rightArm.zRot=leftArm.zRot=0;}
                case io.github.knaitoe.theoldesthouse.labyrinth.BikeNightBullies.REACH->{rightArm.xRot=-1.35F;rightArm.yRot=-.05F;rightArm.zRot=0;body.xRot=.1F;}
                default->{}
            }
            hat.copyFrom(head);
        }
        /**
         * 0.4.485, Bike night's people on the pavements: both arms up for a rider going by, or the one that has no pillowcase in it;
         * hands cupped to shout at one of the three; and the two on the hood, sitting back on their hands with their legs out along it.
         */
        void people(LiteraryActor a,int gesture,float age){
            boolean bag=io.github.knaitoe.theoldesthouse.labyrinth.CalderPeople.carries(a.phase());
            if(io.github.knaitoe.theoldesthouse.labyrinth.CalderPeople.hood(a.phase())){rightLeg.xRot=leftLeg.xRot=-1.5F;rightLeg.yRot=-.1F;leftLeg.yRot=.1F;rightLeg.zRot=leftLeg.zRot=0;rightArm.xRot=leftArm.xRot=.55F;rightArm.yRot=leftArm.yRot=0;rightArm.zRot=.12F;leftArm.zRot=-.12F;}
            switch(gesture){
                case io.github.knaitoe.theoldesthouse.labyrinth.CalderPeople.CHEER->{float up=-2.9F+net.minecraft.util.Mth.sin(age*.6F+a.getId())*.14F;leftArm.xRot=up;leftArm.yRot=0;leftArm.zRot=.3F;if(!bag){rightArm.xRot=up;rightArm.yRot=0;rightArm.zRot=-.3F;}}
                case io.github.knaitoe.theoldesthouse.labyrinth.CalderPeople.PUSH->{rightArm.xRot=leftArm.xRot=-1.2F;rightArm.yRot=-.1F;leftArm.yRot=.1F;rightArm.zRot=leftArm.zRot=0;body.xRot=.1F;}
                case io.github.knaitoe.theoldesthouse.labyrinth.CalderPeople.BOO->{leftArm.xRot=-2.05F;leftArm.yRot=.55F;leftArm.zRot=0;if(!bag){rightArm.xRot=-2.05F;rightArm.yRot=-.55F;rightArm.zRot=0;}}
                default->{}
            }
            hat.copyFrom(head);
        }
        /** Somebody on a bike: hands on the bars, leaning to them, and legs going round with its cranks. */
        /**
         * 0.4.499, the Hollow's witch on her broom: leaning along it, her hands on the stick ahead of her, her legs forward along it
         * and a little apart, her head up to see where she is going; she rides a little up and down on the air.
         */
        void astride(float age){
            float lean=.3F,bob=net.minecraft.util.Mth.sin(age*.3F)*.04F;
            body.xRot=lean;head.xRot=-.3F+bob;head.yRot*=.4F;hat.copyFrom(head);
            rightArm.xRot=leftArm.xRot=-.55F+bob;rightArm.yRot=-.3F;leftArm.yRot=.3F;rightArm.zRot=leftArm.zRot=0;
            rightLeg.xRot=leftLeg.xRot=-1.3F;rightLeg.yRot=.14F;leftLeg.yRot=-.14F;rightLeg.zRot=leftLeg.zRot=0;
            rightLeg.z=leftLeg.z=12*net.minecraft.util.Mth.sin(lean);rightLeg.y=leftLeg.y=12*net.minecraft.util.Mth.cos(lean);
        }
        /**
         * 0.4.500, the Hollow's ghosts: hanging in the air as they go, their feet a little behind them and together, their arms a
         * little out from their sides, their heads down; all of it moving slowly, each one out of step with the others.
         */
        void adrift(float age){
            float slow=net.minecraft.util.Mth.sin(age*.07F);
            body.xRot=.12F;head.xRot=.3F+slow*.05F;head.yRot=slow*.12F;hat.copyFrom(head);
            rightArm.xRot=leftArm.xRot=.18F+slow*.06F;rightArm.yRot=leftArm.yRot=0;rightArm.zRot=.2F+slow*.05F;leftArm.zRot=-.2F-slow*.05F;
            rightLeg.xRot=leftLeg.xRot=.42F+slow*.05F;rightLeg.yRot=leftLeg.yRot=0;rightLeg.zRot=.03F;leftLeg.zRot=-.03F;
            rightLeg.z=leftLeg.z=12*net.minecraft.util.Mth.sin(.12F);rightLeg.y=leftLeg.y=12*net.minecraft.util.Mth.cos(.12F);
        }
        /**
         * 0.4.501, the Hollow's werewolf, down on four legs: his back level, his arms and legs under him, his head up. Running,
         * his front pair and his back pair swing against each other; gathered to leap, he is lower with his arms braced ahead and
         * his legs folded; in the air he is stretched out, arms ahead and legs behind.
         */
        void onFour(int gesture,float age){
            boolean run=gesture==BikeNightSpecters.RUN,leap=gesture==BikeNightSpecters.LEAP;
            float lean=leap?1.5F:1.35F,drop=leap?8:run?10:13,swing=run?net.minecraft.util.Mth.sin(age*.9F)*.8F:0;
            body.xRot=lean;body.y=drop;head.y=drop;head.xRot=leap?-.1F:run?-.15F:.15F;head.yRot*=.5F;hat.copyFrom(head);
            rightArm.y=leftArm.y=2+drop;rightArm.yRot=leftArm.yRot=0;rightArm.zRot=.06F;leftArm.zRot=-.06F;
            rightArm.xRot=leftArm.xRot=leap?-1.9F:run?swing:-.5F;
            float hipY=drop+12*net.minecraft.util.Mth.cos(lean),hipZ=12*net.minecraft.util.Mth.sin(lean);
            rightLeg.y=leftLeg.y=hipY;rightLeg.z=leftLeg.z=hipZ;rightLeg.yRot=leftLeg.yRot=0;rightLeg.zRot=leftLeg.zRot=0;
            rightLeg.xRot=leftLeg.xRot=leap?1.2F:run?-swing:.55F;
        }
        void onBike(LiteraryActor a){
            float crank=a.getVehicle() instanceof io.github.knaitoe.theoldesthouse.labyrinth.RiddenBike bike?bike.crank(1F):0;
            rightArm.xRot=leftArm.xRot=-1.2F;rightArm.yRot=-.22F;leftArm.yRot=.22F;rightArm.zRot=leftArm.zRot=0;body.xRot=.16F;
            rightLeg.xRot=-.5F+net.minecraft.util.Mth.sin(crank)*.45F;leftLeg.xRot=-.5F-net.minecraft.util.Mth.sin(crank)*.45F;rightLeg.yRot=leftLeg.yRot=0;rightLeg.z=leftLeg.z=1.8F;
        }
        /** 0.4.476, Bike night's Gus: a wave; pointing; both arms up; both hands out to push; his hands on the seesaw's handle; a shrug; and (0.4.483) on his bike. */
        void gus(LiteraryActor a,int gesture,float age){
            switch(gesture){
                case io.github.knaitoe.theoldesthouse.labyrinth.BikeNightGus.RIDE->onBike(a);
                case io.github.knaitoe.theoldesthouse.labyrinth.BikeNightGus.WAVE->{rightArm.xRot=-2.7F;rightArm.yRot=0;rightArm.zRot=-.25F+net.minecraft.util.Mth.sin(age*.5F)*.35F;}
                case io.github.knaitoe.theoldesthouse.labyrinth.BikeNightGus.POINT->{rightArm.xRot=-1.45F;rightArm.yRot=-.1F;rightArm.zRot=0;}
                case io.github.knaitoe.theoldesthouse.labyrinth.BikeNightGus.CHEER->{float up=-2.9F+net.minecraft.util.Mth.sin(age*.6F)*.12F;rightArm.xRot=leftArm.xRot=up;rightArm.yRot=leftArm.yRot=0;rightArm.zRot=-.3F;leftArm.zRot=.3F;}
                case io.github.knaitoe.theoldesthouse.labyrinth.BikeNightGus.PUSH->{rightArm.xRot=leftArm.xRot=-1.5F;rightArm.yRot=-.12F;leftArm.yRot=.12F;rightArm.zRot=leftArm.zRot=0;body.xRot=.12F;}
                case io.github.knaitoe.theoldesthouse.labyrinth.BikeNightGus.SIT->{rightArm.xRot=leftArm.xRot=-.95F;rightArm.yRot=-.28F;leftArm.yRot=.28F;rightArm.zRot=leftArm.zRot=0;}
                case io.github.knaitoe.theoldesthouse.labyrinth.BikeNightGus.SHRUG->{rightArm.xRot=leftArm.xRot=-.35F;rightArm.yRot=leftArm.yRot=0;rightArm.zRot=-.55F;leftArm.zRot=.55F;head.zRot=.12F;}
                default->{}
            }
            hat.copyFrom(head);
        }
        /** 0.4.408, the house on the corner: her one hand held out to be taken; held in someone's, a little forward; laid flat on the last door. */
        void corner(int gesture,float age){
            switch(gesture){
                case io.github.knaitoe.theoldesthouse.labyrinth.Corner.HAND_OUT->{rightArm.xRot=-1.2F;rightArm.yRot=-.15F;rightArm.zRot=0;head.xRot=.05F+net.minecraft.util.Mth.sin(age*.1F)*.02F;}
                case io.github.knaitoe.theoldesthouse.labyrinth.Corner.HOLDING->{rightArm.xRot=-.55F;rightArm.yRot=-.35F;rightArm.zRot=0;}
                case io.github.knaitoe.theoldesthouse.labyrinth.Corner.TOUCH->{rightArm.xRot=-1.7F;rightArm.yRot=0;rightArm.zRot=0;head.xRot=-.1F;}
                default->{}
            }
            hat.copyFrom(head);
        }
        /** 0.4.407, the drawing: the list held up to read from; both hands on the box, holding it steady; a slip held out at arm's length; a stone held low at the side. */
        void drawing(int gesture,float age){
            switch(gesture){
                case io.github.knaitoe.theoldesthouse.labyrinth.Drawing.READ->{rightArm.xRot=-1.05F;rightArm.yRot=-.25F;leftArm.xRot=-.95F;leftArm.yRot=.35F;head.xRot=.35F;}
                case io.github.knaitoe.theoldesthouse.labyrinth.Drawing.STEADY->{rightArm.xRot=-.8F;leftArm.xRot=-.8F;rightArm.yRot=-.3F;leftArm.yRot=.3F;body.xRot+=.12F;head.xRot+=.2F;}
                case io.github.knaitoe.theoldesthouse.labyrinth.Drawing.HOLD_OUT->{rightArm.xRot=-1.55F;rightArm.yRot=0;rightArm.zRot=0;leftArm.xRot=-.1F;head.xRot=-.05F+net.minecraft.util.Mth.sin(age*.2F)*.02F;}
                case io.github.knaitoe.theoldesthouse.labyrinth.Drawing.HOLDING->{rightArm.xRot=-.25F;rightArm.zRot=.08F;leftArm.xRot=.05F;head.xRot=.08F;}
                default->{}
            }
            hat.copyFrom(head);
        }
        /** 0.4.364, Pinehook: knuckles to a window, rapping; a hand pressed to the side, bent over it; a hand held low and a little forward. */
        void pinehook(int gesture,float age){
            switch(gesture){
                case io.github.knaitoe.theoldesthouse.labyrinth.Pinehook.KNOCKING->{float rap=net.minecraft.util.Mth.abs(net.minecraft.util.Mth.sin(age*.55F));rightArm.xRot=-1.9F+rap*.35F;rightArm.yRot=-.15F;rightArm.zRot=0;head.xRot=.05F;}
                case io.github.knaitoe.theoldesthouse.labyrinth.Pinehook.HURT->{leftArm.xRot=-.85F;leftArm.yRot=.75F;leftArm.zRot=.2F;rightArm.xRot=-.35F;rightArm.zRot=.15F;body.xRot+=.28F;head.xRot+=.35F;}
                case io.github.knaitoe.theoldesthouse.labyrinth.Pinehook.KNIFE->{rightArm.xRot=-.55F;rightArm.yRot=-.1F;rightArm.zRot=.05F;leftArm.xRot=-.2F;head.xRot+=.15F;}
                // 0.4.367: lying still (the body laid down in setupRotations): arms at the sides, legs straight, the head turned a little.
                case io.github.knaitoe.theoldesthouse.labyrinth.Pinehook.DOWN->{for(var part:new ModelPart[]{rightArm,leftArm,rightLeg,leftLeg}){part.xRot=0;part.yRot=0;}rightArm.zRot=.08F;leftArm.zRot=-.08F;rightLeg.zRot=0;leftLeg.zRot=0;head.xRot=0;head.yRot=.35F;body.xRot=0;}
                // Held: both hands behind the back, the head down.
                case io.github.knaitoe.theoldesthouse.labyrinth.Pinehook.HELD->{rightArm.xRot=.45F;rightArm.yRot=.25F;rightArm.zRot=.3F;leftArm.xRot=.45F;leftArm.yRot=-.25F;leftArm.zRot=-.3F;head.xRot=.5F;body.xRot+=.12F;}
                default->{}
            }
        }
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
        /** 0.4.172: a shelter stranger's gesture (HouseReadShelter), for native proofs. */
        public void previewShelter(int role,int phase,int gesture,float age){shelterGesture=gesture;preview(role,phase,age);shelterGesture=HouseReadShelter.LEAN;}
        public void preview(int role,int phase,float age){
            for(var part:new ModelPart[]{head,body,leftArm,rightArm,leftLeg,rightLeg})part.getAllParts().forEach(ModelPart::resetPose);
            young=false;attackTime=0;crouching=false;riding=false;walk=age*.4F;amount=.6F;
            pose(role,phase,age);hat.copyFrom(head);
            if(role==LiteraryActor.BROTHER||role==LiteraryActor.FAMILY_CHILD||role==LiteraryActor.SILHOUETTE&&phase==4)HumanoidMotion.connect(this);
        }
        /** 0.4.202 (owner: "give npcs an actual sleeping animation"): asleep on the back, arms loose at the sides, the head
         *  turned a little on the pillow, eyes shut, and a slow breath in the chest. */
        public void asleep(float age){
            for(var part:new ModelPart[]{head,body,leftArm,rightArm,leftLeg,rightLeg}){part.xRot=0;part.yRot=0;part.zRot=0;}
            head.yRot=.22F+.04F*net.minecraft.util.Mth.sin(age*.011F);rightArm.zRot=.06F;leftArm.zRot=-.06F;rightArm.xRot=leftArm.xRot=.04F;
            leftLeg.zRot=-.03F;rightLeg.zRot=.03F;body.zScale=1+.05F*(.5F+.5F*net.minecraft.util.Mth.sin(age*.09F));eyelids.visible=true;hat.copyFrom(head);
        }
        /**
         * 0.4.264, Johnny Truant's gestures (labyrinth/Truant): a hand out to his right on a door he is holding; a hand forward at
         * shoulder height, between someone's shoulders; his lamp lifted; the lean round a corner, his head and shoulder out to his left
         * and that hand on the jamb; and both arms up, flinching. 0.4.282 adds the push, looking over an edge, the reach across his lamp,
         * a page held close, and a hand up on a door frame.
         */
        public void truant(int gesture,float age){
            switch(gesture){
                case io.github.knaitoe.theoldesthouse.labyrinth.Truant.HOLD->{rightArm.xRot=-1.35F;rightArm.yRot=.75F;body.yRot+=.18F;}
                case io.github.knaitoe.theoldesthouse.labyrinth.Truant.STEADY->{rightArm.xRot=-1.5F;rightArm.yRot=.12F;leftArm.xRot=-.25F;body.xRot+=.06F;}
                case io.github.knaitoe.theoldesthouse.labyrinth.Truant.LAMP->{rightArm.xRot=-1.85F+net.minecraft.util.Mth.sin(age*.08F)*.03F;rightArm.yRot=-.3F;head.xRot+=.08F;}
                case io.github.knaitoe.theoldesthouse.labyrinth.Truant.PEEK->{body.zRot=.2F;head.x+=2.6F;head.y+=.7F;head.zRot=.3F;leftArm.x+=1.4F;leftArm.zRot=.2F;leftArm.xRot=-1.55F;leftArm.yRot=-.3F;rightArm.zRot=.2F;}
                case io.github.knaitoe.theoldesthouse.labyrinth.Truant.FLINCH->{leftArm.xRot=rightArm.xRot=-2.1F;leftArm.yRot=.55F;rightArm.yRot=-.55F;body.xRot-=.1F;head.xRot+=.35F;}
                // 0.4.282: both hands forward, the push; bent over an edge, looking down; reaching across his lamp, a hand out for a wrist;
                // bent over a page held in both hands; a hand up on a door frame.
                case io.github.knaitoe.theoldesthouse.labyrinth.Truant.PUSH->{leftArm.xRot=rightArm.xRot=-1.6F;leftArm.yRot=.16F;rightArm.yRot=-.16F;body.xRot+=.3F;head.xRot+=.1F;}
                case io.github.knaitoe.theoldesthouse.labyrinth.Truant.OVER->{body.xRot+=.45F;head.xRot+=.8F;leftArm.xRot=rightArm.xRot=.25F;leftArm.zRot=-.14F;rightArm.zRot=.14F;}
                case io.github.knaitoe.theoldesthouse.labyrinth.Truant.REACH->{leftArm.xRot=-1.45F;leftArm.yRot=.65F;rightArm.xRot=-1.1F;rightArm.yRot=-.12F;body.xRot+=.1F;}
                case io.github.knaitoe.theoldesthouse.labyrinth.Truant.PAGE->{rightArm.xRot=leftArm.xRot=-.95F;rightArm.yRot=-.32F;leftArm.yRot=.32F;head.xRot+=.5F;}
                case io.github.knaitoe.theoldesthouse.labyrinth.Truant.FRAME->{rightArm.xRot=-2.65F;rightArm.yRot=.2F;rightArm.zRot=.28F;leftArm.xRot=-.15F;body.yRot-=.08F;}
                default->{}
            }
            hat.copyFrom(head);
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
    /** 0.4.202 (owner: "give npcs an actual sleeping animation"): a sleeper lies along the bed they are in, the way the game lays
     *  a sleeping player down: the head on the pillow at the head of the bed, the body back toward its foot, on the mattress. The
     *  pose stack is world-aligned at the actor and already scaled by its size. False when there is no bed under them. */
    public static boolean lieInBed(net.minecraft.world.entity.LivingEntity a,PoseStack p,float scale){
        var at=a.blockPosition();var s=a.level().getBlockState(at);
        if(!(s.getBlock() instanceof net.minecraft.world.level.block.BedBlock)){at=at.below();s=a.level().getBlockState(at);if(!(s.getBlock() instanceof net.minecraft.world.level.block.BedBlock))return false;}
        var head=s.getValue(net.minecraft.world.level.block.BedBlock.FACING);
        var pillow=s.getValue(net.minecraft.world.level.block.BedBlock.PART)==net.minecraft.world.level.block.state.properties.BedPart.HEAD?at:at.relative(head);
        float reach=a.getEyeHeight(net.minecraft.world.entity.Pose.STANDING)-.1F*scale;
        double x=pillow.getX()+.5-head.getStepX()*reach-a.getX(),y=pillow.getY()+.5625+.125*scale-a.getY(),z=pillow.getZ()+.5-head.getStepZ()*reach-a.getZ();
        p.translate(x/scale,y/scale,z/scale);
        p.mulPose(Axis.YP.rotationDegrees(switch(head){case SOUTH->90F;case WEST->0F;case NORTH->270F;default->180F;}));
        p.mulPose(Axis.ZP.rotationDegrees(90F));p.mulPose(Axis.YP.rotationDegrees(270F));
        return true;
    }
    public static void bedPose(PoseStack p,float scale){p.translate(0,.5625/scale+.25,-.35);p.mulPose(Axis.XP.rotationDegrees(90));}
    public static void coffinPose(PoseStack p){p.translate(0,.4375,-.35);p.mulPose(Axis.XP.rotationDegrees(90));p.scale(.85F,.85F,.85F);}
    static void crutches(CastModel model,ModelPart pole,PoseStack p,MultiBufferSource out,int light){for(var arm:new ModelPart[]{model.leftArm,model.rightArm}){p.pushPose();arm.translateAndRotate(p);p.translate(0,.32,0);pole.render(p,out.getBuffer(RenderType.entityCutoutNoCull(ResourceLocation.withDefaultNamespace("textures/block/spruce_log.png"))),light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);p.popPose();}}
    /**
     * 0.4.220: Brad's helmet lamp, lit. The model is drawn again through the lamp's glow mask (black everywhere but the glass),
     * full bright and added over him: white while the draft blows, amber when it is about to die.
     */
    private static final class BradLampLayer extends net.minecraft.client.renderer.entity.layers.RenderLayer<LiteraryActor,CastModel>{
        BradLampLayer(CastRenderer parent){super(parent);}
        @Override public void render(PoseStack p,MultiBufferSource out,int light,LiteraryActor a,float walk,float amount,float partial,float age,float yaw,float pitch){
            int lit=a.phase()-io.github.knaitoe.theoldesthouse.labyrinth.PittGame.BRAD_PHASE;if(a.role()!=LiteraryActor.STRANGER||lit<1||lit>2)return;
            getParentModel().renderToBuffer(p,out.getBuffer(RenderType.eyes(texture("pitt_brad_lamp"))),LightTexture.FULL_BRIGHT,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,lit==1?0xFFFFFFFF:0xFFFFB040);
        }
    }
    /**
     * 0.4.499, the Hollow's witch: her hat, on her head whatever she is doing, and her broom under her while she flies. Their boxes
     * and where each starts on their sheet are the numbers tools/draw_calder_specters_art.py draws them from.
     */
    private static final class WitchLayer extends net.minecraft.client.renderer.entity.layers.RenderLayer<LiteraryActor,CastModel>{
        static final ResourceLocation THINGS=texture("hollow_witch_things");
        private final ModelPart hat,broom;
        WitchLayer(CastRenderer parent,EntityRendererProvider.Context c){super(parent);var root=c.bakeLayer(WITCH);hat=root.getChild("hat");broom=root.getChild("broom");}
        static LayerDefinition layer(){
            var mesh=new MeshDefinition();
            // On the head: a brim, and a crown in three that leans back at its tip.
            mesh.getRoot().addOrReplaceChild("hat",CubeListBuilder.create().texOffs(0,0).addBox(-5,-9,-5,10,1,10).texOffs(0,11).addBox(-3,-12,-3,6,3,6).texOffs(24,11).addBox(-2,-15,-2,4,3,4).texOffs(40,11).addBox(-1,-18,0,2,3,2),PartPose.ZERO);
            // Under her hips, as she leans: the stick out ahead of her and behind, and the bristles at its back.
            mesh.getRoot().addOrReplaceChild("broom",CubeListBuilder.create().texOffs(0,20).addBox(-.5F,12.6F,-11,1,1,26).texOffs(0,47).addBox(-2.5F,10.6F,15,5,5,7),PartPose.ZERO);
            return LayerDefinition.create(mesh,64,64);
        }
        static void draw(CastModel model,ModelPart hat,ModelPart broom,boolean flying,PoseStack p,MultiBufferSource out,int light){
            var buffer=out.getBuffer(RenderType.entityCutoutNoCull(THINGS));
            p.pushPose();model.head.translateAndRotate(p);hat.render(p,buffer,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);p.popPose();
            if(flying)broom.render(p,buffer,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        }
        @Override public void render(PoseStack p,MultiBufferSource out,int light,LiteraryActor a,float walk,float amount,float partial,float age,float yaw,float pitch){
            if(a.role()!=LiteraryActor.STRANGER||a.phase()!=BikeNightSpecters.WITCH||a.isInvisible())return;
            draw(getParentModel(),hat,broom,a.gesture()==BikeNightSpecters.FLY,p,out,light);
        }
    }
    /**
     * The witch as the game draws her, for the proof's page: the cast's own model in her skin, astride or standing, with her hat and
     * her broom. The pose stack is the model's own space (y down, sixteenths of a block).
     */
    public static void drawWitch(PoseStack p,MultiBufferSource out,int light,boolean flying,float age){
        var models=Minecraft.getInstance().getEntityModels();var model=new CastModel(models.bakeLayer(CAST));var things=models.bakeLayer(WITCH);
        model.young=false;model.dress(LiteraryActor.STRANGER,BikeNightSpecters.WITCH);
        if(flying)model.astride(age);
        model.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(skin(LiteraryActor.STRANGER,BikeNightSpecters.WITCH))),light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,-1);
        WitchLayer.draw(model,things.getChild("hat"),things.getChild("broom"),flying,p,out,light);
    }
    /**
     * 0.4.501, the Hollow's werewolf: his muzzle with its nose and his two ears, on his head; and his tail, from the foot of his
     * back. Their boxes and where each starts on their sheet are the numbers tools/draw_calder_specters_art.py draws them from.
     */
    private static final class WolfLayer extends net.minecraft.client.renderer.entity.layers.RenderLayer<LiteraryActor,CastModel>{
        static final ResourceLocation THINGS=texture("hollow_wolf_things");
        private final ModelPart face,tail;
        WolfLayer(CastRenderer parent,EntityRendererProvider.Context c){super(parent);var root=c.bakeLayer(WOLF);face=root.getChild("face");tail=root.getChild("tail");}
        static LayerDefinition layer(){
            var mesh=new MeshDefinition();
            mesh.getRoot().addOrReplaceChild("face",CubeListBuilder.create().texOffs(0,0).addBox(-2,-4,-7,4,3,3).texOffs(14,0).addBox(-1,-4.4F,-7.6F,2,1,1).texOffs(22,0).addBox(-4,-11,-1,2,3,2).texOffs(22,0).mirror().addBox(2,-11,-1,2,3,2),PartPose.ZERO);
            // On along the line of his back from where his legs begin, lifted a little.
            mesh.getRoot().addOrReplaceChild("tail",CubeListBuilder.create().texOffs(0,8).addBox(-1.5F,0,-1.5F,3,9,3),PartPose.offsetAndRotation(0,11.5F,1.5F,.4F,0,0));
            return LayerDefinition.create(mesh,64,32);
        }
        static void draw(CastModel model,ModelPart face,ModelPart tail,PoseStack p,MultiBufferSource out,int light){
            var buffer=out.getBuffer(RenderType.entityCutoutNoCull(THINGS));
            p.pushPose();model.head.translateAndRotate(p);face.render(p,buffer,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);p.popPose();
            p.pushPose();model.body.translateAndRotate(p);tail.render(p,buffer,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);p.popPose();
        }
        @Override public void render(PoseStack p,MultiBufferSource out,int light,LiteraryActor a,float walk,float amount,float partial,float age,float yaw,float pitch){
            if(a.role()!=LiteraryActor.STRANGER||a.phase()!=BikeNightSpecters.WOLF||a.isInvisible())return;
            draw(getParentModel(),face,tail,p,out,light);
        }
    }
    /** The werewolf as the game draws him, for the proof's page: running, gathered, or in the air. */
    public static void drawWolf(PoseStack p,MultiBufferSource out,int light,int gesture,float age){
        var models=Minecraft.getInstance().getEntityModels();var model=new CastModel(models.bakeLayer(CAST));var things=models.bakeLayer(WOLF);
        model.young=false;model.dress(LiteraryActor.STRANGER,BikeNightSpecters.WOLF);model.onFour(gesture,age);
        model.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(skin(LiteraryActor.STRANGER,BikeNightSpecters.WOLF))),light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,-1);
        WolfLayer.draw(model,things.getChild("face"),things.getChild("tail"),p,out,light);
    }
    /** One of the Hollow's ghosts as the game draws it, for the proof's page: the cast's own model in its skin, adrift, half there. */
    public static void drawGhost(PoseStack p,MultiBufferSource out,int light,int which,float age){
        var model=new CastModel(Minecraft.getInstance().getEntityModels().bakeLayer(CAST));int phase=BikeNightSpecters.GHOST+which;
        model.young=false;model.dress(LiteraryActor.STRANGER,phase);model.adrift(age);
        int alpha=Math.round(255*(1-BikeNightSpecters.GHOST_FADE/100F));
        model.renderToBuffer(p,out.getBuffer(RenderType.entityTranslucent(skin(LiteraryActor.STRANGER,phase))),light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,alpha<<24|0xffffff);
    }
    private static final class CrutchLayer extends net.minecraft.client.renderer.entity.layers.RenderLayer<LiteraryActor,CastModel>{
        private final ModelPart pole;
        CrutchLayer(CastRenderer parent,EntityRendererProvider.Context c){super(parent);pole=c.bakeLayer(CRUTCH).getChild("pole");}
        static LayerDefinition layer(){var mesh=new MeshDefinition();mesh.getRoot().addOrReplaceChild("pole",CubeListBuilder.create().texOffs(0,0).addBox(-.7F,-1,-1,1.4F,22,1.4F).texOffs(0,0).addBox(-2,-1,-1,4,1.4F,1.4F),PartPose.ZERO);return LayerDefinition.create(mesh,16,16);}
        @Override public void render(PoseStack p,MultiBufferSource out,int light,LiteraryActor a,float walk,float amount,float partial,float age,float yaw,float pitch){if(a.role()!=LiteraryActor.FATHER||a.phase()!=4)return;crutches(getParentModel(),pole,p,out,light);}
    }
    /** The carcass he hauls off by a hind leg (0.4.120), drawn behind his left hand. */
    /** 0.4.251: the window cleaner's bucket and brush; nobody else in the cast carries what is in their hands. */
    private static final class CleanerKit extends net.minecraft.client.renderer.entity.layers.ItemInHandLayer<LiteraryActor,CastModel>{
        CleanerKit(CastRenderer parent,EntityRendererProvider.Context c){super(parent,c.getItemInHandRenderer());}
        @Override public void render(PoseStack p,MultiBufferSource out,int light,LiteraryActor a,float walk,float amount,float partial,float age,float yaw,float pitch){if(a.role()==LiteraryActor.STRANGER&&(a.phase()==io.github.knaitoe.theoldesthouse.labyrinth.WindowCleaner.CLEANER||io.github.knaitoe.theoldesthouse.labyrinth.Drawing.cast(a.phase())||io.github.knaitoe.theoldesthouse.labyrinth.CalderPeople.carries(a.phase())))super.render(p,out,light,a,walk,amount,partial,age,yaw,pitch);}
    }
    /** 0.4.253: Kelly's wheelchair, the same model as the empty chair block, drawn under her while she sits in it, turned with her. */
    private static final class WheelchairLayer extends net.minecraft.client.renderer.entity.layers.RenderLayer<LiteraryActor,CastModel>{
        WheelchairLayer(CastRenderer parent){super(parent);}
        @Override public void render(PoseStack p,MultiBufferSource out,int light,LiteraryActor a,float walk,float amount,float partial,float age,float yaw,float pitch){
            if(a.role()!=LiteraryActor.STRANGER||!io.github.knaitoe.theoldesthouse.labyrinth.PeopleLights.inChair(a.phase()))return;
            p.pushPose();p.translate(0,1.501,0);p.scale(-1,-1,1);p.translate(-.5,0,-.5);
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(io.github.knaitoe.theoldesthouse.labyrinth.PeopleLightsBlock.of(io.github.knaitoe.theoldesthouse.labyrinth.PeopleLightsBlock.Kind.CHAIR,Direction.NORTH),p,out,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
            p.popPose();
        }
    }
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
    private static final class CastRenderer extends MobRenderer<LiteraryActor,CastModel>{private final ModelPart eyes;private final PlainFigureModel.Renderer plain;private final GhostsCastModel.Renderer ghosts;private final GhostsPropRenderer ghostsProps;private final BorrascaNativeArt.CastRenderer borrasca;private final UncannyActorRenderer uncanny;private final HouseHumanRenderers.ActorRenderer harrigan;private final DareRenderers.Kid dareKid;private final LookoutRenderers.Whistler whistler;private final LookoutRenderers.Double lookoutDouble;private final SecondPersonRenderers.Resident resident;CastRenderer(EntityRendererProvider.Context c){super(c,new CastModel(c.bakeLayer(CAST)),.2F);dareKid=new DareRenderers.Kid(c);whistler=new LookoutRenderers.Whistler(c);lookoutDouble=new LookoutRenderers.Double(c);resident=new SecondPersonRenderers.Resident(c);eyes=c.bakeLayer(CAST).getChild("herd_eyes");plain=new PlainFigureModel.Renderer(c);ghosts=new GhostsCastModel.Renderer(c);ghostsProps=new GhostsPropRenderer(c);borrasca=new BorrascaNativeArt.CastRenderer(c);harrigan=new HouseHumanRenderers.ActorRenderer(c);uncanny=new UncannyActorRenderer(c);addLayer(new CrutchLayer(this,c));addLayer(new WitchLayer(this,c));addLayer(new WolfLayer(this,c));addLayer(new BradLampLayer(this));addLayer(new HaulLayer(this,c));addLayer(new CleanerKit(this,c));addLayer(new WheelchairLayer(this));}@Override public void render(LiteraryActor a,float yaw,float partial,PoseStack p,MultiBufferSource out,int light){if(a.role()==LiteraryActor.WHISTLER){whistler.render(a,yaw,partial,p,out,light);return;}if(a.role()==LiteraryActor.LOOKOUT_DOUBLE){lookoutDouble.render(a,yaw,partial,p,out,light);return;}if(a.role()==LiteraryActor.SP_RESIDENT){resident.render(a,yaw,partial,p,out,light);return;}if(LiteraryActor.dareKid(a.role())){dareKid.render(a,yaw,partial,p,out,light);return;}if((UncannyStories.role(a.role())||a.role()==LiteraryActor.ROAD_WOMAN)){uncanny.render(a,yaw,partial,p,out,light);return;}if(PlainFigureModel.matches(a.role(),a.phase())){plain.render(a,yaw,partial,p,out,light);return;}if(a.role()==LiteraryActor.HARRIGAN||a.role()==LiteraryActor.HARRIGAN_MOURNER){harrigan.render(a,yaw,partial,p,out,light);return;}if(a.role()>=LiteraryActor.BORRASCA_KYLE&&a.role()<=LiteraryActor.BORRASCA_PRESCOTT){borrasca.render(a,yaw,partial,p,out,light);return;}if(a.role()>=LiteraryActor.GHOST_PLAYHOUSE&&a.role()<=LiteraryActor.GHOST_DRAWER){ghostsProps.render(a,yaw,partial,p,out,light);return;}if(a.role()>=LiteraryActor.GHOST_MARJORIE&&a.role()<=LiteraryActor.GHOST_PRIEST){ghosts.render(a,yaw,partial,p,out,light);return;}if(a.role()==LiteraryActor.RED_FIGURE&&a.phase()==3){p.pushPose();p.translate(0,1.45,0);p.mulPose(Axis.YP.rotationDegrees(-yaw));eyes.render(p,out.getBuffer(RenderType.eyes(texture("herd_eyes"))),LightTexture.FULL_BRIGHT,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);p.popPose();return;}getModel().alpha=1-a.fade()/100F;try{super.render(a,yaw,partial,p,out,light);}finally{getModel().alpha=1;}}@Override protected net.minecraft.client.renderer.RenderType getRenderType(LiteraryActor a,boolean body,boolean translucent,boolean glowing){return a.fade()>0&&body?net.minecraft.client.renderer.RenderType.entityTranslucent(getTextureLocation(a)):super.getRenderType(a,body,translucent,glowing);}@Override public ResourceLocation getTextureLocation(LiteraryActor a){return skin(a.role(),a.phase());}@Override protected void setupRotations(LiteraryActor a,PoseStack p,float age,float yaw,float partial,float scale){if(a.role()==LiteraryActor.BROTHER&&a.phase()>0&&lieInBed(a,p,scale))return;if(a.role()==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.PeopleLights.inBed(a.phase())&&lieInBed(a,p,scale))return;var chair=seat(a);var facing=chair==null?null:chair.facing();boolean lying=a.role()==LiteraryActor.COFFIN_WOMAN&&a.phase()==1;super.setupRotations(a,p,age,(lying||a.role()==LiteraryActor.BROTHER&&a.phase()>0)?0:facing==null?yaw:facing.toYRot(),partial,scale);if(a.role()==LiteraryActor.LAKE_TESS&&(a.phase()==2||a.phase()==6)){p.mulPose(Axis.XP.rotationDegrees(-90-a.getViewXRot(partial)));p.translate(0,-1,.3);}if(a.role()==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.LastSwimmer.swimmer(a.phase())){p.mulPose(Axis.XP.rotationDegrees(-90));p.translate(0,-1,.3);}if(a.role()==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.PeopleLights.dragging(a.phase())){p.mulPose(Axis.XP.rotationDegrees(-90));p.translate(0,-1,.16);}if(a.role()==LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.Pinehook.cast(a.phase())&&a.gesture()==io.github.knaitoe.theoldesthouse.labyrinth.Pinehook.DOWN){p.mulPose(Axis.XP.rotationDegrees(-90));p.translate(0,-1,.16);}if(lying)coffinPose(p);if(a.role()==LiteraryActor.BROTHER&&a.phase()>0)bedPose(p,.58F);}@Override public boolean shouldRender(LiteraryActor a,net.minecraft.client.renderer.culling.Frustum f,double x,double y,double z){var p=Minecraft.getInstance().player;return !(a.role()==LiteraryActor.COFFIN_WOMAN&&a.phase()==2)&&(a.scene().equals(LabyrinthPlace.FAMILY_COPY.id())||a.scene().equals(LabyrinthPlace.OLD_CABIN.id())||a.owner().isEmpty()||p!=null&&a.owner().get().equals(io.github.knaitoe.theoldesthouse.labyrinth.GhostsCrew.castRole(a.role())?GhostsTakeClient.cast(p.getUUID()):p.getUUID())||a.role()==LiteraryActor.LAKE_WAX&&LakePartnerLook.shared(a.owner().get()))&&(a.role()==LiteraryActor.WHISTLER?whistler.shouldRender(a,f,x,y,z):a.role()==LiteraryActor.LOOKOUT_DOUBLE?lookoutDouble.shouldRender(a,f,x,y,z):(UncannyStories.role(a.role())||a.role()==LiteraryActor.ROAD_WOMAN)?uncanny.shouldRender(a,f,x,y,z):a.role()==LiteraryActor.HARRIGAN?harrigan.shouldRender(a,f,x,y,z):super.shouldRender(a,f,x,y,z));}}
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
