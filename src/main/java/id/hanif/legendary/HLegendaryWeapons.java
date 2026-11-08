package id.hanif.legendary;

import id.hanif.legendary.item.ModItems;
import id.hanif.legendary.network.SkillPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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
            context.server().execute(() ->
                SkillHandler.activate(context.player(), payload.skill())
            )
        );
        LOGGER.info("H Legendary Weapons initialized: eleven legendary weapons registered");
    }
}
