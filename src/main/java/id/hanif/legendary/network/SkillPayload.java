package id.hanif.legendary.network;

import id.hanif.legendary.HLegendaryWeapons;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SkillPayload(int skill) implements CustomPayload {
    public static final Id<SkillPayload> ID =
        new Id<>(Identifier.of(HLegendaryWeapons.MOD_ID, "use_skill"));
    public static final PacketCodec<RegistryByteBuf, SkillPayload> CODEC =
        PacketCodec.tuple(PacketCodecs.INTEGER, SkillPayload::skill, SkillPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
