package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.house.TractHouseBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthData;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthDealer;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlace;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryVignettes;
import io.github.knaitoe.theoldesthouse.labyrinth.ModelHome;
import io.github.knaitoe.theoldesthouse.labyrinth.VignetteYields;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Poltergeist: the model home, the first multi-visit vignette. */
@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ModelHomeTests {
    private ModelHomeTests() {
    }

    private static BlockPos home(GameTestHelper helper) {
        return helper.absolutePos(BlockPos.ZERO).offset(-600, 8, 600);
    }

    private static boolean is(ServerLevel level, BlockPos base, BlockPos rel, TractHouseBlock.Kind kind) {
        return TractHouseBlock.is(level.getBlockState(base.offset(rel)), kind);
    }

    @GameTest(template = "empty")
    public static void theHouseIsSetForEachVisit(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = home(helper);
        ModelHome.buildHome(level, base);
        ModelHome.stage(level, base, 1, false, false);

        // A 1980s tract house: paneling and shag in the living room, linoleum and harvest-gold paper in the kitchen.
        helper.assertTrue(level.getBlockState(base.offset(-4, -1, -4)).is(HouseBlocks.TRACT_SHAG.get()), "the living room is carpeted in shag");
        helper.assertTrue(level.getBlockState(base.offset(4, -1, -13)).is(HouseBlocks.TRACT_LINOLEUM.get()), "the kitchen floor is linoleum");
        helper.assertTrue(level.getBlockState(base.offset(9, 1, -12)).is(HouseBlocks.TRACT_KITCHEN_PAPER.get()), "the kitchen is papered");
        helper.assertTrue(level.getBlockState(base.offset(-1, 1, -4)).is(HouseBlocks.TRACT_PANELING.get())
                || level.getBlockState(base.offset(0, 1, -8)).is(HouseBlocks.TRACT_PANELING.get()), "the living room is paneled");
        helper.assertTrue(is(level, base, new BlockPos(-8, 0, -4), TractHouseBlock.Kind.SOFA_MIDDLE)
                && is(level, base, new BlockPos(8, 0, -17), TractHouseBlock.Kind.STOVE)
                && is(level, base, new BlockPos(8, 1, -18), TractHouseBlock.Kind.FRIDGE_UPPER)
                && is(level, base, new BlockPos(-7, 0, -16), TractHouseBlock.Kind.CLOWN_DOLL), "sofa, range, fridge and the clown are in their rooms");
        helper.assertTrue(TractHouseBlock.light(TractHouseBlock.of(TractHouseBlock.Kind.CEILING_LIGHT, Direction.NORTH)) > 10
                && is(level, base, new BlockPos(-3, 4, -4), TractHouseBlock.Kind.CEILING_LIGHT), "a show home is lit");

        helper.assertTrue(is(level, base, ModelHome.TABLE[0], TractHouseBlock.Kind.TABLE_WEST)
                && is(level, base, ModelHome.TABLE[1], TractHouseBlock.Kind.TABLE_EAST), "the table stands");
        for (ModelHome.Chair chair : ModelHome.KITCHEN_CHAIRS) {
            helper.assertTrue(is(level, base, chair.pos(), TractHouseBlock.Kind.DINING_CHAIR), "every chair is in its place at " + chair.pos());
        }
        helper.assertTrue(is(level, base, ModelHome.KIDS_CHAIR.pos(), TractHouseBlock.Kind.KIDS_CHAIR), "the child's chair is in the child's room");
        helper.assertTrue(is(level, base, ModelHome.TV, TractHouseBlock.Kind.TV_OFF), "on the first visit the set is off");
        helper.assertTrue(level.getBlockState(base.offset(ModelHome.WINDOW)).is(Blocks.GLASS_PANE)
                && level.getBlockState(base.offset(ModelHome.WINDOW).above()).is(Blocks.GLASS_PANE), "the kid's window is whole");
        helper.assertTrue(level.getBlockState(base.offset(-20, 0, -13)).is(Blocks.OAK_LOG), "on the first visit the tree is far off in the yard");
        helper.assertTrue(!level.getBlockState(base.offset(ModelHome.KIDS_DOOR)).getValue(DoorBlock.OPEN), "the kid's door is shut");

        ModelHome.stage(level, base, 2, false, false);
        helper.assertTrue(is(level, base, ModelHome.TV, TractHouseBlock.Kind.TV_STATIC), "on the second the set shows static");
        helper.assertTrue(!level.getBlockState(base.offset(ModelHome.KIDS_DOOR)).getValue(DoorBlock.OPEN), "and the kid's door is still shut");
        ModelHome.stage(level, base, 2, false, true);
        helper.assertTrue(level.getBlockState(base.offset(ModelHome.KIDS_DOOR)).getValue(DoorBlock.OPEN), "once the static has opened it, it stays open that visit");

        ModelHome.stage(level, base, 3, false, false);
        helper.assertTrue(is(level, base, ModelHome.TV, TractHouseBlock.Kind.TV_OFF), "on the third the set is off again");
        helper.assertTrue(level.getBlockState(base.offset(-12, 0, -13)).is(Blocks.OAK_LOG)
                && level.getBlockState(base.offset(-20, 0, -13)).isAir(), "by the third it stands at the window, and not where it was");
        helper.assertTrue(level.getBlockState(base.offset(-10, 2, -13)).is(Blocks.OAK_LOG), "a branch reaches the glass");
        helper.assertTrue(level.getBlockState(base.offset(-18, -1, -17)).is(Blocks.COARSE_DIRT), "and the grass has sunk");
        helper.assertTrue(level.getBlockState(base.offset(ModelHome.WINDOW)).is(Blocks.GLASS_PANE), "the window holds until someone goes in");
        helper.assertTrue(level.getBlockState(base.offset(ModelHome.KIDS_DOOR)).getValue(DoorBlock.OPEN), "the kid's door stands open");

        ModelHome.stage(level, base, ModelHome.FINAL_VISIT, true, false);
        helper.assertTrue(level.getBlockState(base.offset(ModelHome.WINDOW)).isAir(), "once it has come in, the window stays broken");

        ModelHome.stage(level, base, 1, false, false);
        helper.assertTrue(level.getBlockState(base.offset(-12, 0, -13)).isAir()
                && level.getBlockState(base.offset(ModelHome.WINDOW)).is(Blocks.GLASS_PANE), "set back, the tree is far off and the glass whole");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theChairsStackSwayAndGoBack(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = home(helper).offset(0, 0, -60);
        ModelHome.buildHome(level, base);
        ModelHome.stage(level, base, 1, false, false);

        ModelHome.stack(level, base, 1);
        for (ModelHome.Chair chair : ModelHome.stackedChairs(1)) {
            helper.assertTrue(level.getBlockState(base.offset(chair.pos())).isAir(), "the chair at " + chair.pos() + " has gone to the stack");
        }
        helper.assertTrue(is(level, base, ModelHome.KIDS_CHAIR.pos(), TractHouseBlock.Kind.KIDS_CHAIR), "the child's chair, behind a locked door, is not in it");
        List<Display.BlockDisplay> stack = ModelHome.stackedDisplays(level, base);
        helper.assertTrue(stack.size() == ModelHome.stackedChairs(1).size(), "one chair in the stack for each taken: " + stack.size());
        for (Display.BlockDisplay chair : stack) {
            helper.assertTrue(chair.position().distanceTo(ModelHome.tableTop(base)) < 1.0E-3D, "the stack stands on the table top");
            helper.assertTrue(TractHouseBlock.is(NbtUtils.readBlockState(level.holderLookup(Registries.BLOCK), chair.saveWithoutId(new CompoundTag()).getCompound("block_state")),
                    TractHouseBlock.Kind.DINING_CHAIR), "the stack is the kitchen's own chairs");
        }

        Tag before = stack.get(0).saveWithoutId(new CompoundTag()).get("transformation");
        ModelHome.teeter(level, base, ModelHome.amplitude(1.5D), 40L);
        Tag after = ModelHome.stackedDisplays(level, base).stream()
                .filter(display -> display.getUUID().equals(stack.get(0).getUUID()))
                .findFirst().orElseThrow().saveWithoutId(new CompoundTag()).get("transformation");
        helper.assertTrue(before != null && after != null && !before.equals(after), "close to it, the stack sways");
        helper.assertTrue(ModelHome.amplitude(1.5D) > ModelHome.amplitude(5.0D) && ModelHome.amplitude(5.0D) > ModelHome.amplitude(12.0D),
                "the nearer, the more it sways");

        ModelHome.unstack(level, base, 1);
        for (ModelHome.Chair chair : ModelHome.stackedChairs(1)) {
            helper.assertTrue(is(level, base, chair.pos(), TractHouseBlock.Kind.DINING_CHAIR), "every chair is back");
        }
        helper.assertTrue(ModelHome.stackedDisplays(level, base).isEmpty(), "and the stack is gone");
        helper.succeed();
    }

    /** The last visit: the glass comes in and a limb reaches through toward the one who went into the room. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void theTreeReachesThroughTheWindowTowardTheReader(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = home(helper).offset(0, 0, -120);
        ModelHome.buildHome(level, base);
        ModelHome.stage(level, base, ModelHome.FINAL_VISIT, false, false);

        Vec3 root = ModelHome.limbRoot(base);
        Vec3 near = new Vec3(base.getX() - 6.5D, base.getY() + 1.1D, base.getZ() - 12.5D);
        Vec3 far = new Vec3(base.getX() - 2.5D, base.getY() + 1.1D, base.getZ() - 12.5D);
        Vec3[] full = ModelHome.limbSpine(root, near, 1.0D, 0.0D), half = ModelHome.limbSpine(root, near, 0.4D, 0.0D);
        helper.assertTrue(full.length == ModelHome.LIMB_SEGMENTS + 1 && full[0].distanceTo(root) < 1.0E-6D, "the limb grows from the branch at the glass");
        helper.assertTrue(full[ModelHome.LIMB_SEGMENTS].x > root.x + 3.0D && full[ModelHome.LIMB_SEGMENTS].distanceTo(near) < 0.2D, "at full stretch it reaches into the room, to whoever it is reaching for");
        helper.assertTrue(half[ModelHome.LIMB_SEGMENTS].distanceTo(root) < full[ModelHome.LIMB_SEGMENTS].distanceTo(root), "and it comes in a little at a time");
        Vec3 farTip = ModelHome.limbSpine(root, far, 1.0D, 0.0D)[ModelHome.LIMB_SEGMENTS];
        helper.assertTrue(farTip.distanceTo(root) <= ModelHome.LIMB_REACH + 1.0E-6D && farTip.distanceTo(far) > 1.0D, "it cannot reach across the whole room");

        var reader = NativeTestPlayers.survival(helper, "model_home_reader");
        try {
            reader.teleportTo(level, near.x, base.getY(), near.z, 90.0F, 0.0F);
            ModelHome.breakIn(level, base, reader);
            for (BlockPos glass : ModelHome.windowPanes()) {
                helper.assertTrue(level.getBlockState(base.offset(glass)).isAir(), "the glass came in at " + glass);
            }
            helper.assertTrue(ModelHome.limbDisplays(level, base).size() == ModelHome.LIMB_SEGMENTS + 2,
                    "a jointed limb with leaves at its end: " + ModelHome.limbDisplays(level, base).size());
        } finally {
            ModelHome.limbDisplays(level, base).forEach(Entity::discard);
            NativeTestPlayers.remove(reader);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aVisitMovesOnOnlyOnceItsBeatIsDone(GameTestHelper helper) {
        helper.assertTrue(ModelHome.nextVisit(0, false) == 1, "the first arrival is the first visit");
        helper.assertTrue(ModelHome.nextVisit(1, false) == 1, "leaving before the beat resumes the same visit");
        helper.assertTrue(ModelHome.nextVisit(1, true) == 2, "after the beat, the next arrival is the next visit");
        helper.assertTrue(ModelHome.nextVisit(ModelHome.FINAL_VISIT, true) == ModelHome.FINAL_VISIT, "and there is no visit after the last");
        helper.assertTrue(ModelHome.stackedChairs(1).size() == 6 && ModelHome.stackedChairs(2).isEmpty()
                && ModelHome.stackedChairs(3).isEmpty(), "the chairs stack on the first visit only");
        helper.succeed();
    }

    /** 0.4.105: a personal story is begun by the reader's own visits; another reader's progress weighs nothing for them. */
    @GameTest(template = "empty")
    public static void aPersonalStoryBegunByThisReaderIsDealtMoreOftenToThem(GameTestHelper helper) {
        LabyrinthData data = new LabyrinthData();
        UUID reader = UUID.randomUUID(), other = UUID.randomUUID();
        LabyrinthPlace room = LabyrinthPlace.LAKE_HOUSE;
        helper.assertTrue(room.isMultiVisit() && LabyrinthDealer.dealWeight(data, reader, room) == 1, "before the reader has been, it is as likely as any other");
        CompoundTag own = LiteraryVignettes.personal(data, reader, room);
        own.putInt("Visit", 2);
        LiteraryVignettes.save(data, reader, room, own);
        helper.assertTrue(LabyrinthDealer.dealWeight(data, reader, room) == LabyrinthDealer.UNFINISHED_WEIGHT && LabyrinthDealer.dealWeight(data, other, room) == 1,
                "begun by this reader, it comes up more for them only");
        own.putBoolean("Completed", true);
        LiteraryVignettes.save(data, reader, room, own);
        helper.assertTrue(LabyrinthDealer.dealWeight(data, reader, room) == 1, "finished, it is no longer weighted");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aBegunModelHomeIsDealtMoreOften(GameTestHelper helper) {
        LabyrinthData data = new LabyrinthData();
        LabyrinthPlace home = LabyrinthPlace.MODEL_HOME;
        helper.assertTrue(home.isMultiVisit() && home.isVignette() && home.isFinishable(), "the model home is a multi-visit vignette");
        helper.assertTrue(LabyrinthDealer.dealWeight(data, home) == 1, "before anyone has been, it is as likely as any other");
        CompoundTag state = new CompoundTag();
        state.putInt("Visit", 2);
        data.setState(ModelHome.ID, state);
        helper.assertTrue(LabyrinthDealer.dealWeight(data, home) == LabyrinthDealer.UNFINISHED_WEIGHT, "begun and unfinished, it comes up more");
        helper.assertTrue(LabyrinthDealer.vignettesAvailable(data).contains(home), "and it is still dealt");
        data.setCompleted(ModelHome.ID, true);
        helper.assertTrue(LabyrinthDealer.dealWeight(data, home) == 1 && !LabyrinthDealer.vignettesAvailable(data).contains(home),
                "finished, it is not dealt again");
        helper.assertTrue(ModelHome.ID.equals(VignetteYields.of(ModelHome.binder())), "the salesman's binder carries its vignette");
        helper.succeed();
    }
}
