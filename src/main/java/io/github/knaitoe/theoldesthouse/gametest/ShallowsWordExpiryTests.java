package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.Shallows;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ShallowsWordExpiryTests {
    private ShallowsWordExpiryTests() {}

    @GameTest(template="empty",batch="aaa_shallows_words04100")
    public static void oneCleanupExpiresEveryDueWordAndKeepsLiveWordsAndOriginalActors(GameTestHelper h){
        List<Entity> created=new ArrayList<>(),expired=new ArrayList<>(),future=new ArrayList<>(),unrelated=new ArrayList<>();
        var level=h.getLevel();
        long deadline=level.getGameTime()+120;
        try{
            // Enough interleaved native IDs to exercise removal across the lookup's map buckets.
            for(int i=0;i<128;i++){
                Entity due=word(h,level.getGameTime(),true);created.add(due);expired.add(due);
                if(i%8==0){
                    Entity live=word(h,deadline,true);created.add(live);future.add(live);
                    Entity other=word(h,0,false);created.add(other);unrelated.add(other);
                }
            }
            var body=EntityType.ARMOR_STAND.create(level);
            h.assertTrue(body!=null,"the original native actor is created");
            body.moveTo(Vec3.atCenterOf(h.absolutePos(new BlockPos(1,2,1))));
            body.setNoGravity(true);body.setHealth(13);body.setCustomName(Component.literal("The original actor"));
            h.assertTrue(level.addFreshEntity(body),"the original actor enters the native lookup");created.add(body);
            var originalId=body.getUUID();

            Shallows.expireWords(level);
            for(Entity entity:expired)h.assertTrue(entity.isRemoved()&&level.getEntity(entity.getUUID())==null,"one cleanup removes every due word from the actual native lookup");
            for(Entity entity:future)h.assertTrue(!entity.isRemoved()&&level.getEntity(entity.getUUID())==entity&&entity.getPersistentData().getLong("Until")==deadline,"future words retain their exact object and deadline");
            for(Entity entity:unrelated)h.assertTrue(!entity.isRemoved()&&level.getEntity(entity.getUUID())==entity,"an expired timestamp does not remove an unrelated text entity");
            Shallows.expireWords(level);
            h.assertTrue(level.getEntity(originalId)==body&&body.getHealth()==13&&body.getName().getString().equals("The original actor"),"repeated cleanup retains the original native actor, health and name");

            for(Entity entity:future)entity.getPersistentData().putLong("Until",level.getGameTime());
            Shallows.expireWords(level);
            for(Entity entity:future)h.assertTrue(entity.isRemoved(),"the same retained words expire when their own deadline becomes due");
            for(Entity entity:unrelated)h.assertTrue(!entity.isRemoved(),"later cleanup still preserves unrelated original text");
            h.succeed();
        }finally{
            for(Entity entity:created)entity.discard();
        }
    }

    private static Entity word(GameTestHelper h,long until,boolean tagged){
        Entity entity=EntityType.TEXT_DISPLAY.create(h.getLevel());
        h.assertTrue(entity!=null,"the native text display is created");
        entity.moveTo(Vec3.atCenterOf(h.absolutePos(new BlockPos(1,2,1))));
        entity.setNoGravity(true);entity.getPersistentData().putLong("Until",until);
        if(tagged)entity.addTag(Shallows.WORDS);
        h.assertTrue(h.getLevel().addFreshEntity(entity),"the native text display enters the actual entity lookup");
        return entity;
    }
}
