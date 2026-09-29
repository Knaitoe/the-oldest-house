package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthData;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthDealer;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlace;
import io.github.knaitoe.theoldesthouse.labyrinth.ModelHome;
import io.github.knaitoe.theoldesthouse.labyrinth.VignetteYields;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.StairBlock;
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

    @GameTest(template = "empty")
    public static void theHouseIsSetForEachVisit(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = home(helper);
        ModelHome.buildHome(level, base);
        ModelHome.stage(level, base, 1);

        for (BlockPos leg : ModelHome.TABLE) {
            helper.assertTrue(level.getBlockState(base.offset(leg)).is(Blocks.SPRUCE_FENCE), "the table stands");
        }
        for (ModelHome.Chair chair : ModelHome.stackedChairs(ModelHome.FINAL_VISIT)) {
            helper.assertTrue(level.getBlockState(base.offset(chair.pos())).getBlock() instanceof StairBlock,
                    "every chair is in its place at " + chair.pos());
        }
        helper.assertTrue(level.getBlockState(base.offset(ModelHome.WINDOW)).is(Blocks.GLASS_PANE)
                && level.getBlockState(base.offset(ModelHome.WINDOW).above()).is(Blocks.GLASS_PANE), "the kid's window is whole");
        helper.assertTrue(level.getBlockState(base.offset(-20, 0, -13)).is(Blocks.OAK_LOG), "on the first visit the tree is far off in the yard");
        helper.assertTrue(!level.getBlockState(base.offset(ModelHome.KIDS_DOOR)).getValue(DoorBlock.OPEN), "the kid's door is shut");

        ModelHome.stage(level, base, 3);
        helper.assertTrue(level.getBlockState(base.offset(-12, 0, -13)).is(Blocks.OAK_LOG)
                && level.getBlockState(base.offset(-20, 0, -13)).isAir(), "by the third it stands at the window, and not where it was");
        helper.assertTrue(level.getBlockState(base.offset(-10, 2, -13)).is(Blocks.OAK_LOG), "a branch reaches the glass");
        helper.assertTrue(level.getBlockState(base.offset(-18, -1, -17)).is(Blocks.COARSE_DIRT), "and the grass has sunk");

        ModelHome.stage(level, base, ModelHome.FINAL_VISIT);
        helper.assertTrue(level.getBlockState(base.offset(ModelHome.WINDOW)).isAir(), "on the last visit the window is broken");
        helper.assertTrue(level.getBlockState(base.offset(-6, 2, -13)).is(Blocks.OAK_LOG), "and the branch is in the kid's room");
        helper.assertTrue(level.getBlockState(base.offset(-7, 1, -13)).is(Blocks.OAK_LEAVES), "over the bed");
        helper.assertTrue(level.getBlockState(base.offset(ModelHome.KIDS_DOOR)).getValue(DoorBlock.OPEN), "the kid's door stands open");

        ModelHome.stage(level, base, 1);
        helper.assertTrue(level.getBlockState(base.offset(-6, 2, -13)).isAir()
                && level.getBlockState(base.offset(ModelHome.WINDOW)).is(Blocks.GLASS_PANE), "set back, the room has no tree in it");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theChairsStackSwayAndGoBack(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = home(helper).offset(0, 0, -60);
        ModelHome.buildHome(level, base);
        ModelHome.stage(level, base, 2);

        ModelHome.stack(level, base, 2);
        for (ModelHome.Chair chair : ModelHome.stackedChairs(2)) {
            helper.assertTrue(level.getBlockState(base.offset(chair.pos())).isAir(), "the chair at " + chair.pos() + " has gone to the stack");
        }
        helper.assertTrue(level.getBlockState(base.offset(ModelHome.KIDS_CHAIR.pos())).getBlock() instanceof StairBlock,
                "on the second visit the kid's chair is not in it yet");
        List<Display.BlockDisplay> stack = ModelHome.stackedDisplays(level, base);
        helper.assertTrue(stack.size() == ModelHome.stackedChairs(2).size(), "one chair in the stack for each taken: " + stack.size());
        for (Display.BlockDisplay chair : stack) {
            helper.assertTrue(chair.position().distanceTo(ModelHome.tableTop(base)) < 1.0E-3D, "the stack stands on the table top");
        }

        Tag before = stack.get(0).saveWithoutId(new CompoundTag()).get("transformation");
        ModelHome.teeter(level, base, ModelHome.amplitude(1.5D), 40L);
        Tag after = ModelHome.stackedDisplays(level, base).stream()
                .filter(display -> display.getUUID().equals(stack.get(0).getUUID()))
                .findFirst().orElseThrow().saveWithoutId(new CompoundTag()).get("transformation");
        helper.assertTrue(before != null && after != null && !before.equals(after), "close to it, the stack sways");
        helper.assertTrue(ModelHome.amplitude(1.5D) > ModelHome.amplitude(5.0D) && ModelHome.amplitude(5.0D) > ModelHome.amplitude(12.0D),
                "the nearer, the more it sways");

        ModelHome.unstack(level, base, 2);
        for (ModelHome.Chair chair : ModelHome.stackedChairs(2)) {
            helper.assertTrue(level.getBlockState(base.offset(chair.pos())).getBlock() instanceof StairBlock, "every chair is back");
        }
        helper.assertTrue(ModelHome.stackedDisplays(level, base).isEmpty(), "and the stack is gone");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aVisitMovesOnOnlyOnceItsBeatIsDone(GameTestHelper helper) {
        helper.assertTrue(ModelHome.nextVisit(0, false) == 1, "the first arrival is the first visit");
        helper.assertTrue(ModelHome.nextVisit(1, false) == 1, "leaving before the beat resumes the same visit");
        helper.assertTrue(ModelHome.nextVisit(1, true) == 2, "after the beat, the next arrival is the next visit");
        helper.assertTrue(ModelHome.nextVisit(ModelHome.FINAL_VISIT, true) == ModelHome.FINAL_VISIT, "and there is no visit after the last");
        helper.assertTrue(ModelHome.stackedChairs(1).size() == 6 && ModelHome.stackedChairs(2).size() == 8
                && ModelHome.stackedChairs(3).size() == 9, "the stack grows each visit");
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
