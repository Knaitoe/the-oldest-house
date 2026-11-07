package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.GhostsSetBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.GhostsLore;
import java.util.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

/** The same four physical plates show this reader's day; no shared block state or food item is replaced. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class GhostsMealModels {
    private GhostsMealModels(){}
    /** 0.4.325: the native proofs draw these anchors as on a given filming day; 0 in play. */
    public static int previewDay;
    @SubscribeEvent public static void baked(ModelEvent.ModifyBakingResult event){
        for(var facing:Direction.Plane.HORIZONTAL){
            var meals=new EnumMap<GhostsSetBlock.Kind,BakedModel>(GhostsSetBlock.Kind.class);
            for(var kind:GhostsSetBlock.Kind.values())if(kind.name().startsWith("MEAL_")){var model=event.getModels().get(BlockModelShaper.stateToModelLocation(GhostsSetBlock.of(kind,facing)));if(model!=null)meals.put(kind,model);}
            for(var kind:List.of(GhostsSetBlock.Kind.PLATE,GhostsSetBlock.Kind.PLAIN_PLATE)){
                var key=BlockModelShaper.stateToModelLocation(GhostsSetBlock.of(kind,facing));var original=event.getModels().get(key);if(original!=null)event.getModels().put(key,new Meal(original,meals,kind==GhostsSetBlock.Kind.PLAIN_PLATE));
            }
            var closed=BlockModelShaper.stateToModelLocation(GhostsSetBlock.of(GhostsSetBlock.Kind.LAPTOP_CLOSED,facing));
            var study=event.getModels().get(BlockModelShaper.stateToModelLocation(GhostsSetBlock.of(GhostsSetBlock.Kind.LAPTOP_STUDY,facing)));
            var original=event.getModels().get(closed);if(original!=null&&study!=null)event.getModels().put(closed,new Laptop(original,study));
            // 0.4.325: Merry's pad shows only on the last day; the canisters stand open, a spoon between them, only on the last day.
            var pad=BlockModelShaper.stateToModelLocation(GhostsSetBlock.of(GhostsSetBlock.Kind.PLAN_PAD,facing));var padModel=event.getModels().get(pad);
            if(padModel!=null)event.getModels().put(pad,new ByDay(padModel,null,GhostsLore::pad,GhostsSetBlock.Kind.PLAN_PAD));
            var tins=BlockModelShaper.stateToModelLocation(GhostsSetBlock.of(GhostsSetBlock.Kind.CANISTERS,facing));var tinsModel=event.getModels().get(tins);
            var open=event.getModels().get(BlockModelShaper.stateToModelLocation(GhostsSetBlock.of(GhostsSetBlock.Kind.CANISTERS_OPEN,facing)));
            if(tinsModel!=null&&open!=null)event.getModels().put(tins,new ByDay(tinsModel,open,GhostsLore::canisters,GhostsSetBlock.Kind.CANISTERS));
        }
    }
    private static final class Laptop extends BakedModelWrapper<BakedModel>{
        private final BakedModel study;
        Laptop(BakedModel original,BakedModel study){super(original);this.study=study;}
        private BakedModel shown(){return GhostsLore.laptop(GhostsTakeClient.day())==GhostsSetBlock.Kind.LAPTOP_STUDY?study:originalModel;}
        @Override public List<BakedQuad> getQuads(@Nullable BlockState s,@Nullable Direction side,RandomSource random){return GhostsLore.laptop(GhostsTakeClient.day())==null?List.of():shown().getQuads(s,side,random);}
        @Override public List<BakedQuad> getQuads(@Nullable BlockState s,@Nullable Direction side,RandomSource random,ModelData data,@Nullable RenderType render){return GhostsLore.laptop(GhostsTakeClient.day())==null?List.of():shown().getQuads(s,side,random,data,render);}
    }
    /** One anchor whose look follows this reader's filming day: its own model, the alternative, or nothing. */
    private static final class ByDay extends BakedModelWrapper<BakedModel>{
        private final BakedModel other;private final java.util.function.IntFunction<GhostsSetBlock.Kind> kind;private final GhostsSetBlock.Kind own;
        ByDay(BakedModel original,BakedModel other,java.util.function.IntFunction<GhostsSetBlock.Kind> kind,GhostsSetBlock.Kind own){super(original);this.other=other;this.kind=kind;this.own=own;}
        private BakedModel shown(){var k=kind.apply(previewDay>0?previewDay:GhostsTakeClient.day());return k==null?null:k==own||other==null?originalModel:other;}
        @Override public List<BakedQuad> getQuads(@Nullable BlockState s,@Nullable Direction side,RandomSource random){var m=shown();return m==null?List.of():m.getQuads(s,side,random);}
        @Override public List<BakedQuad> getQuads(@Nullable BlockState s,@Nullable Direction side,RandomSource random,ModelData data,@Nullable RenderType render){var m=shown();return m==null?List.of():m.getQuads(s,side,random,data,render);}
    }
    private static final class Meal extends BakedModelWrapper<BakedModel>{
        private final Map<GhostsSetBlock.Kind,BakedModel> meals;private final boolean merrys;
        Meal(BakedModel original,Map<GhostsSetBlock.Kind,BakedModel> meals,boolean merrys){super(original);this.meals=Map.copyOf(meals);this.merrys=merrys;}
        private BakedModel shown(){return meals.getOrDefault(GhostsSetBlock.meal(GhostsTakeClient.day(),merrys),originalModel);}
        @Override public List<BakedQuad> getQuads(@Nullable BlockState s,@Nullable Direction side,RandomSource random){return shown().getQuads(s,side,random);}
        @Override public List<BakedQuad> getQuads(@Nullable BlockState s,@Nullable Direction side,RandomSource random,ModelData data,@Nullable RenderType render){return shown().getQuads(s,side,random,data,render);}
    }
}
