package id.hanif.legendary.network;

import id.hanif.legendary.HLegendaryWeapons;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record SkillPayload(int skill) implements CustomPayload {
    public static final Id<SkillPayload> ID =
        new Id<>(net.minecraft.util.Identifier.of(HLegendaryWeapons.MOD_ID, "use_skill"));
    public static final PacketCodec<PacketByteBuf, SkillPayload> CODEC =
        PacketCodec.tuple(PacketCodec.INTEGER, SkillPayload::skill, SkillPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
