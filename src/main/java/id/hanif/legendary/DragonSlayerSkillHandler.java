package id.hanif.legendary;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class DragonSlayerSkillHandler {
    private static final String[] NAMES = {
        "Dragon Breath", "Wing Crash", "Dragon Roar", "Scale Armor",
        "Flame Dive", "Dragon Claw", "Ancient Dragon Form"
    };
    private static final int[] COOLDOWNS = {5, 12, 16, 22, 9, 7, 65};
    private static final Map<UUID, long[]> TIMERS = new HashMap<>();

    private DragonSlayerSkillHandler() {}

    public static void activate(ServerPlayerEntity player, int skill) {
        if (skill < 0 || skill >= 7 || !(player.getWorld() instanceof ServerWorld world)) return;
        long now = System.currentTimeMillis();
        long[] timers = TIMERS.computeIfAbsent(player.getUuid(), ignored -> new long[7]);
        if (now < timers[skill]) {
            long left = (timers[skill] - now + 999) / 1000;
            player.sendMessage(Text.literal(NAMES[skill] + " cooldown: " + left + "s"), true);
            return;
        }
        timers[skill] = now + COOLDOWNS[skill] * 1000L;
        player.sendMessage(Text.literal("§6✦ " + NAMES[skill] + " §7activated"), true);
        switch (skill) {
            case 0 -> dragonBreath(player, world);
            case 1 -> wingCrash(player, world);
            case 2 -> dragonRoar(player, world);
            case 3 -> scaleArmor(player, world);
            case 4 -> flameDive(player, world);
            case 5 -> dragonClaw(player, world);
            case 6 -> ancientDragonForm(player, world);
            default -> { }
        }
    }

    private static void dragonBreath(ServerPlayerEntity p, ServerWorld w) {
        Vec3d look = p.getRotationVec(1.0F).normalize();
        Vec3d center = p.getPos().add(look.multiply(4.0));
        for (LivingEntity target : targets(w, p, center, 4.5)) {
            Vec3d toward = target.getPos().subtract(p.getPos()).normalize();
            if (look.dotProduct(toward) > 0.35) {
                target.damage(w.getDamageSources().playerAttack(p), 8.0F);
                target.setOnFireFor(5);
            }
        }
        w.spawnParticles(ParticleTypes.FLAME, center.x, center.y + 1.0, center.z, 55, 1.7, 0.8, 1.7, 0.07);
        w.spawnParticles(ParticleTypes.LAVA, center.x, center.y + 1.0, center.z, 8, 1.0, 0.5, 1.0, 0.02);
        w.spawnParticles(ParticleTypes.DRAGON_BREATH, center.x, center.y + 1.0, center.z, 22, 1.3, 0.6, 1.3, 0.025);
    }

    private static void wingCrash(ServerPlayerEntity p, ServerWorld w) {
        Vec3d center = p.getPos();
        for (LivingEntity target : targets(w, p, center, 5.0)) {
            target.damage(w.getDamageSources().playerAttack(p), 9.0F);
            target.takeKnockback(1.15, p.getX() - target.getX(), p.getZ() - target.getZ());
        }
        w.spawnParticles(ParticleTypes.CLOUD, center.x, center.y + 0.4, center.z, 38, 2.8, 0.4, 2.8, 0.08);
        w.spawnParticles(ParticleTypes.FLAME, center.x, center.y + 0.6, center.z, 28, 2.2, 0.6, 2.2, 0.04);
        w.spawnParticles(ParticleTypes.EXPLOSION, center.x, center.y + 0.4, center.z, 2, 1.0, 0.2, 1.0, 0.01);
    }

    private static void dragonRoar(ServerPlayerEntity p, ServerWorld w) {
        Vec3d center = p.getPos().add(p.getRotationVec(1.0F).multiply(3.0));
        for (LivingEntity target : targets(w, p, center, 7.0)) {
            target.damage(w.getDamageSources().playerAttack(p), 5.0F);
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 120, 1));
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 70, 1));
        }
        w.spawnParticles(ParticleTypes.DRAGON_BREATH, center.x, center.y + 1.0, center.z, 70, 3.0, 1.2, 3.0, 0.035);
        w.spawnParticles(ParticleTypes.FLAME, center.x, center.y + 1.0, center.z, 32, 2.5, 0.8, 2.5, 0.04);
    }

    private static void scaleArmor(ServerPlayerEntity p, ServerWorld w) {
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 240, 1));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 240, 0));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 160, 0));
        w.spawnParticles(ParticleTypes.FLAME, p.getX(), p.getY() + 1.0, p.getZ(), 32, 0.9, 1.0, 0.9, 0.025);
        w.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, p.getX(), p.getY() + 1.0, p.getZ(), 14, 0.7, 0.9, 0.7, 0.04);
    }

    private static void flameDive(ServerPlayerEntity p, ServerWorld w) {
        Vec3d look = p.getRotationVec(1.0F);
        p.setVelocity(look.x * 1.55, Math.max(0.75, look.y * 1.2 + 0.55), look.z * 1.55);
        p.velocityModified = true;
        Vec3d center = p.getPos().add(look.multiply(2.0));
        for (LivingEntity target : targets(w, p, center, 3.0)) {
            target.damage(w.getDamageSources().playerAttack(p), 7.0F);
            target.setOnFireFor(4);
        }
        w.spawnParticles(ParticleTypes.FLAME, p.getX(), p.getY() + 0.5, p.getZ(), 40, 0.8, 0.5, 0.8, 0.06);
        w.spawnParticles(ParticleTypes.CLOUD, p.getX(), p.getY(), p.getZ(), 14, 0.6, 0.3, 0.6, 0.03);
    }

    private static void dragonClaw(ServerPlayerEntity p, ServerWorld w) {
        Vec3d look = p.getRotationVec(1.0F).normalize();
        Vec3d center = p.getPos().add(look.multiply(3.0));
        for (LivingEntity target : targets(w, p, center, 3.6)) {
            Vec3d toward = target.getPos().subtract(p.getPos()).normalize();
            if (look.dotProduct(toward) > 0.2) {
                target.damage(w.getDamageSources().playerAttack(p), 11.0F);
                target.takeKnockback(0.55, -look.x, -look.z);
            }
        }
        w.spawnParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y + 1.0, center.z, 4, 1.0, 0.5, 1.0, 0.01);
        w.spawnParticles(ParticleTypes.FLAME, center.x, center.y + 1.0, center.z, 24, 1.0, 0.5, 1.0, 0.035);
    }

    private static void ancientDragonForm(ServerPlayerEntity p, ServerWorld w) {
        p.heal(6.0F);
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 300, 1));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 240, 1));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 300, 0));
        Vec3d center = p.getPos();
        for (LivingEntity target : targets(w, p, center, 6.0)) {
            target.damage(w.getDamageSources().playerAttack(p), 13.0F);
            target.setOnFireFor(6);
            target.takeKnockback(1.0, p.getX() - target.getX(), p.getZ() - target.getZ());
        }
        w.spawnParticles(ParticleTypes.DRAGON_BREATH, center.x, center.y + 1.0, center.z, 95, 2.8, 1.3, 2.8, 0.04);
        w.spawnParticles(ParticleTypes.FLAME, center.x, center.y + 1.0, center.z, 85, 2.4, 1.2, 2.4, 0.06);
        w.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, center.x, center.y + 1.0, center.z, 22, 1.5, 1.2, 1.5, 0.045);
        w.spawnParticles(ParticleTypes.EXPLOSION, center.x, center.y + 0.5, center.z, 3, 1.4, 0.4, 1.4, 0.01);
    }

    private static List<LivingEntity> targets(ServerWorld w, ServerPlayerEntity p, Vec3d center, double radius) {
        Box box = new Box(center.x - radius, center.y - 3.0, center.z - radius,
                          center.x + radius, center.y + 4.0, center.z + radius);
        double r2 = radius * radius;
        return w.getEntitiesByClass(LivingEntity.class, box, e -> e.isAlive() && e != p)
            .stream().filter(e -> e.squaredDistanceTo(center.x, center.y, center.z) <= r2).toList();
    }
}
