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

public final class WindKatanaSkillHandler {
    private static final String[] NAMES = {
        "Wind Cutter", "Cyclone", "Air Step", "Gale Rush",
        "Sky Dance", "Wind Prison", "Tempest God"
    };
    private static final int[] COOLDOWNS = {4, 10, 8, 7, 14, 18, 50};
    private static final Map<UUID, long[]> TIMERS = new HashMap<>();

    private WindKatanaSkillHandler() {}

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
        player.sendMessage(Text.literal("§b✦ " + NAMES[skill] + " §7activated"), true);
        switch (skill) {
            case 0 -> windCutter(player, world);
            case 1 -> cyclone(player, world);
            case 2 -> airStep(player, world);
            case 3 -> galeRush(player, world);
            case 4 -> skyDance(player, world);
            case 5 -> windPrison(player, world);
            case 6 -> tempestGod(player, world);
            default -> { }
        }
    }

    private static void windCutter(ServerPlayerEntity p, ServerWorld w) {
        Vec3d look = p.getRotationVec(1.0F).normalize();
        Vec3d center = p.getPos().add(look.multiply(3.8));
        for (LivingEntity target : targets(w, p, center, 4.0)) {
            Vec3d toward = target.getPos().subtract(p.getPos()).normalize();
            if (look.dotProduct(toward) > 0.15) {
                target.damage(w.getDamageSources().playerAttack(p), 8.0F);
                target.takeKnockback(0.35, -look.x, -look.z);
            }
        }
        w.spawnParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y + 1.0, center.z, 5, 1.3, 0.5, 1.3, 0.01);
        w.spawnParticles(ParticleTypes.CLOUD, center.x, center.y + 1.0, center.z, 24, 1.2, 0.5, 1.2, 0.04);
        w.spawnParticles(ParticleTypes.CRIT, center.x, center.y + 1.0, center.z, 18, 1.0, 0.5, 1.0, 0.08);
    }

    private static void cyclone(ServerPlayerEntity p, ServerWorld w) {
        Vec3d center = p.getPos().add(p.getRotationVec(1.0F).multiply(2.0));
        for (LivingEntity target : targets(w, p, center, 4.5)) {
            target.damage(w.getDamageSources().playerAttack(p), 6.0F);
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 50, 1));
            target.takeKnockback(0.55, p.getX() - target.getX(), p.getZ() - target.getZ());
        }
        for (int ring = 0; ring < 3; ring++) {
            double radius = 1.1 + ring * 0.8;
            for (int i = 0; i < 8; i++) {
                double a = (Math.PI * 2.0 * i / 8.0) + ring * 0.55;
                w.spawnParticles(ParticleTypes.CLOUD, center.x + Math.cos(a) * radius,
                    center.y + 0.3 + ring * 0.55, center.z + Math.sin(a) * radius, 1, 0, 0, 0, 0);
            }
        }
        w.spawnParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y + 1.0, center.z, 3, 1.5, 0.6, 1.5, 0.01);
    }

    private static void airStep(ServerPlayerEntity p, ServerWorld w) {
        Vec3d look = p.getRotationVec(1.0F);
        p.setVelocity(look.x * 0.75, 0.95, look.z * 0.75);
        p.velocityModified = true;
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 50, 0));
        w.spawnParticles(ParticleTypes.CLOUD, p.getX(), p.getY() + 0.2, p.getZ(), 18, 0.55, 0.2, 0.55, 0.03);
        w.spawnParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 0.4, p.getZ(), 8, 0.3, 0.2, 0.3, 0.015);
    }

    private static void galeRush(ServerPlayerEntity p, ServerWorld w) {
        Vec3d look = p.getRotationVec(1.0F).normalize();
        p.setVelocity(look.x * 1.85, Math.max(0.15, look.y * 0.45), look.z * 1.85);
        p.velocityModified = true;
        Vec3d center = p.getPos().add(look.multiply(2.0));
        for (LivingEntity target : targets(w, p, center, 2.8)) {
            target.damage(w.getDamageSources().playerAttack(p), 9.0F);
            target.takeKnockback(0.7, -look.x, -look.z);
        }
        w.spawnParticles(ParticleTypes.CLOUD, p.getX(), p.getY() + 0.7, p.getZ(), 28, 0.65, 0.45, 0.65, 0.05);
        w.spawnParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y + 1.0, center.z, 3, 0.6, 0.4, 0.6, 0.01);
    }

    private static void skyDance(ServerPlayerEntity p, ServerWorld w) {
        p.setVelocity(p.getVelocity().x, 0.85, p.getVelocity().z);
        p.velocityModified = true;
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 140, 1));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 100, 0));
        Vec3d c = p.getPos();
        w.spawnParticles(ParticleTypes.CLOUD, c.x, c.y + 0.8, c.z, 32, 1.0, 0.5, 1.0, 0.04);
        w.spawnParticles(ParticleTypes.END_ROD, c.x, c.y + 1.0, c.z, 14, 0.7, 0.7, 0.7, 0.02);
    }

    private static void windPrison(ServerPlayerEntity p, ServerWorld w) {
        Vec3d center = p.getPos().add(p.getRotationVec(1.0F).multiply(4.0));
        for (LivingEntity target : targets(w, p, center, 3.6)) {
            target.damage(w.getDamageSources().playerAttack(p), 5.0F);
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 3));
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 1));
        }
        for (int i = 0; i < 12; i++) {
            double a = Math.PI * 2.0 * i / 12.0;
            double x = center.x + Math.cos(a) * 2.0;
            double z = center.z + Math.sin(a) * 2.0;
            w.spawnParticles(ParticleTypes.CLOUD, x, center.y + 0.4, z, 2, 0.05, 0.6, 0.05, 0.025);
        }
        w.spawnParticles(ParticleTypes.END_ROD, center.x, center.y + 1.0, center.z, 16, 1.0, 0.8, 1.0, 0.025);
    }

    private static void tempestGod(ServerPlayerEntity p, ServerWorld w) {
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 240, 1));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 180, 1));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 240, 1));
        Vec3d center = p.getPos().add(p.getRotationVec(1.0F).multiply(2.5));
        for (LivingEntity target : targets(w, p, center, 7.0)) {
            target.damage(w.getDamageSources().playerAttack(p), 13.0F);
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1));
            target.takeKnockback(1.0, p.getX() - target.getX(), p.getZ() - target.getZ());
        }
        w.spawnParticles(ParticleTypes.CLOUD, center.x, center.y + 1.0, center.z, 75, 3.0, 1.1, 3.0, 0.08);
        w.spawnParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y + 1.0, center.z, 10, 2.5, 0.8, 2.5, 0.01);
        w.spawnParticles(ParticleTypes.END_ROD, center.x, center.y + 1.0, center.z, 30, 2.0, 1.0, 2.0, 0.04);
        w.spawnParticles(ParticleTypes.EXPLOSION, center.x, center.y + 0.5, center.z, 3, 1.5, 0.4, 1.5, 0.01);
    }

    private static List<LivingEntity> targets(ServerWorld w, ServerPlayerEntity p, Vec3d center, double radius) {
        Box box = new Box(center.x - radius, center.y - 3.0, center.z - radius,
                          center.x + radius, center.y + 4.0, center.z + radius);
        double r2 = radius * radius;
        return w.getEntitiesByClass(LivingEntity.class, box, e -> e.isAlive() && e != p)
            .stream().filter(e -> e.squaredDistanceTo(center.x, center.y, center.z) <= r2).toList();
    }
}
