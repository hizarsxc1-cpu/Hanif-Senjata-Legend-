package id.hanif.legendary;

import id.hanif.legendary.item.ModItems;
import id.hanif.legendary.network.SkillPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HLegendaryWeapons implements ModInitializer {
    public static final String MOD_ID = "hlegendary";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModItems.register();

        PayloadTypeRegistry.playC2S().register(SkillPayload.ID, SkillPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SkillPayload.ID, (payload, context) ->
            context.server().execute(() -> SkillHandler.activate(context.player(), payload.skill()))
        );

        // Heavy basic-attack impact and the occasional Hell Anchor passive.
        AttackEntityCallback.EVENT.register((player, world, hand, target, hitResult) -> {
            if (!world.isClient
                && hand == Hand.MAIN_HAND
                && player instanceof ServerPlayerEntity serverPlayer
                && target instanceof LivingEntity livingTarget
                && world instanceof ServerWorld serverWorld
                && serverPlayer.getMainHandStack().isOf(ModItems.GALAXY_SWORD)) {
                GalaxySwordSkillHandler.onBasicHit(serverPlayer, livingTarget, serverWorld);
            } else if (!world.isClient
                && hand == Hand.MAIN_HAND
                && player instanceof ServerPlayerEntity serverPlayer
                && target instanceof LivingEntity livingTarget
                && world instanceof ServerWorld serverWorld
                && serverPlayer.getMainHandStack().isOf(ModItems.DOOMBRINGER_AXE)) {
                DoombringerAxeSkillHandler.onBasicHit(serverPlayer, livingTarget, serverWorld);
            }
            return ActionResult.PASS;
        });

        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(GalaxySwordSkillHandler::tickAura);

        LOGGER.info("H Legendary Weapons initialized: thirteen legendary weapons registered");
    }
}
