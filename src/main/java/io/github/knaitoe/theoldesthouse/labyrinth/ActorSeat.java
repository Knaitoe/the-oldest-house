package io.github.knaitoe.theoldesthouse.labyrinth;

import javax.annotation.Nullable;
import net.minecraft.core.Direction;

/**
 * The household, roadhouse, lawn-chair or other seat an actor has been placed in, if any: how high it holds the hips, and which way
 * it faces ({@code null} where the seat turns with the person). Until 0.4.560 only the client knew (it draws a person in a chair
 * seated, client.LiteraryRenderers); now the server does too, so that a seated person who talks to a reader turns their head to them
 * and leaves their body in the chair ({@link LiteraryActor}).
 */
public record ActorSeat(int seatHeight,@Nullable Direction facing){
    public static @Nullable ActorSeat of(LiteraryActor a){
        var state=a.level().getBlockState(a.blockPosition());
        if(state.is(io.github.knaitoe.theoldesthouse.house.HouseBlocks.HOUSEHOLD_FURNITURE.get())){var kind=state.getValue(io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock.KIND);return kind.seat?new ActorSeat(kind.seatHeight,state.getValue(io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock.FACING)):null;}
        if(state.is(LiteraryRegistry.ROADHOUSE.get())){var kind=state.getValue(RoadhouseBlock.KIND);return kind.seat()?new ActorSeat(kind.seatHeight,state.getValue(RoadhouseBlock.FACING)):null;}
        if(MoviePropBlock.is(state,MoviePropBlock.Kind.LAWN_CHAIR))return new ActorSeat(5,state.getValue(MoviePropBlock.FACING));
        // Brad's kit box in the God of the Pitt (0.4.220), eight pixels high.
        if(PittBlock.is(state,PittBlock.Kind.BRAD))return new ActorSeat(8,state.getValue(PittBlock.FACING));
        // Kelly in her chair, or on the floor by the sofa; anyone in an empty wheelchair (0.4.253).
        if(a.role()==LiteraryActor.STRANGER&&PeopleLights.inChair(a.phase()))return new ActorSeat(8,null);
        if(a.role()==LiteraryActor.STRANGER&&PeopleLights.onFloor(a.phase()))return new ActorSeat(1,null);
        if(PeopleLightsBlock.is(state,PeopleLightsBlock.Kind.CHAIR))return new ActorSeat(8,state.getValue(PeopleLightsBlock.FACING));
        // The last chair in Vacant Possession (0.4.228), its seat eight pixels up.
        if(VacantBlock.is(state,VacantBlock.Kind.CHAIR))return new ActorSeat(8,state.getValue(VacantBlock.FACING));
        // The familiar kitchen's chairs (0.4.331), where its visitors sit: the seat ten pixels up.
        if(TraceBlock.is(state,TraceBlock.Kind.CHAIR))return new ActorSeat(10,state.getValue(TraceBlock.FACING));
        // 0.4.527: the Pell house's people on the edge of Nessa's bed at bedtime, turned however they sit.
        if(a.role()==LiteraryActor.STRANGER&&PellPeople.cast(a.phase())&&a.gesture()!=PellPeople.ASLEEP&&PellThing.is(state,PellThing.Kind.BED))return new ActorSeat(9,null);
        // The Pell house's kitchen chairs (0.4.526), where Tilly and Callum sit at dinner; from 0.4.527 June's recliner too.
        // 0.4.540: June on the step stool beside the freezer, its seat nine up.
        if(a.role()==LiteraryActor.STRANGER&&PellPeople.cast(a.phase())&&PellThing.is(state,PellThing.Kind.STOOL))return new ActorSeat(9,state.getValue(PellThing.FACING));
        if(PellThing.seat(state))return new ActorSeat(PellThing.seatHeight(state),state.getValue(PellThing.FACING));
        // 0.4.602: the lodging house's hall bench, its seat eight up, where Dan sits; and a failed body sat on the cellar's floor, tied
        // to the dead post.
        if(LodgingBlock.is(state,LodgingBlock.Kind.BENCH))return new ActorSeat(8,state.getValue(LodgingBlock.FACING));
        if(LodgingPeople.onFloor(a))return new ActorSeat(1,null);
        // 0.4.550: the lodging house's chairs, armchairs and settee.
        if(LodgingThing.seat(state))return new ActorSeat(LodgingThing.seatHeight(state),state.getValue(LodgingThing.FACING));
        // 0.4.622: Rowan's mended chair at the bench, and the flat's seats.
        if(LongerBlock.seat(state))return new ActorSeat(state.getValue(LongerBlock.KIND).seatHeight(),state.getValue(LongerBlock.FACING));
        // 0.4.645: the Caterpillar on the mushroom's cap, sat on its top (where it stands, so the seat is at its feet).
        if(a.role()==LiteraryActor.STRANGER&&a.phase()==KingCaterpillar.PHASE&&KingBlock.is(state,KingBlock.Kind.CAP))return new ActorSeat(0,null);
        // 0.4.648: a guest at the King's feast, in a high-backed chair nine pixels up, turned across the table (the guest's own body yaw).
        if(KingBlock.is(state,KingBlock.Kind.FEAST_CHAIR))return new ActorSeat(9,null);
        // 0.4.667: the office chairs in Last Seen, their seats ten pixels up.
        if(LastSeenBlock.is(state,LastSeenBlock.Kind.CHAIR))return new ActorSeat(10,state.getValue(LastSeenBlock.FACING));
        // Roderick's chair (0.4.182), its seat nine pixels up.
        if(UsherBlock.is(state,UsherBlock.Kind.CHAIR))return new ActorSeat(9,state.getValue(UsherBlock.FACING));
        // 0.4.444: Earl, years later, on a flattened box in the alley beside the Tipple.
        if(a.role()==LiteraryActor.STRANGER&&a.phase()==BorrascaPeople.EARL&&state.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock)return new ActorSeat(1,null);
        return null;
    }
}
