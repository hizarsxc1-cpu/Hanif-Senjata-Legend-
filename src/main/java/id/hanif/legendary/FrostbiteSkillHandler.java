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
import java.util.Map;
import java.util.UUID;

public final class FrostbiteSkillHandler {
    private static final String[] NAMES = {
        "Ice Fang", "Frost Nova", "Glacier Lance", "Crystal Prison",
        "Frozen Dash", "Absolute Zero", "Glacial Apocalypse"
    };
    private static final int[] COOLDOWNS = {3, 9, 7, 13, 5, 28, 50};
    private static final Map<UUID, long[]> TIMERS = new HashMap<>();

    private FrostbiteSkillHandler() {}

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
        player.sendMessage(Text.literal("§b❄ " + NAMES[skill] + " §7activated"), true);
        switch (skill) {
            case 0 -> iceFang(player, world);
            case 1 -> frostNova(player, world);
            case 2 -> glacierLance(player, world);
            case 3 -> crystalPrison(player, world);
            case 4 -> frozenDash(player, world);
            case 5 -> absoluteZero(player, world);
            case 6 -> glacialApocalypse(player, world);
            default -> { }
        }
    }

    private static void iceFang(ServerPlayerEntity p, ServerWorld w) {
        Vec3d look = p.getRotationVec(1.0F).normalize();
        Vec3d center = p.getPos().add(look.multiply(3.4));
        damageForward(p, w, 8.0F, 5.5, 0.28, 50, 1);
        w.spawnParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y + 1.0, center.z, 4, 1.0, 0.5, 1.0, 0.01);
        w.spawnParticles(ParticleTypes.SNOWFLAKE, center.x, center.y + 1.0, center.z, 28, 1.2, 0.7, 1.2, 0.025);
    }

    private static void frostNova(ServerPlayerEntity p, ServerWorld w) {
        Vec3d c = p.getPos();
        damageArea(p, w, c, 4.5, 7.0F, 70, 2);
        w.spawnParticles(ParticleTypes.SNOWFLAKE, c.x, c.y + 1.0, c.z, 65, 3.0, 1.0, 3.0, 0.04);
        w.spawnParticles(ParticleTypes.CLOUD, c.x, c.y + 0.5, c.z, 22, 2.5, 0.4, 2.5, 0.025);
    }

    private static void glacierLance(ServerPlayerEntity p, ServerWorld w) {
        Vec3d look = p.getRotationVec(1.0F).normalize();
        Vec3d center = p.getPos().add(look.multiply(6.0));
        damageForward(p, w, 10.0F, 8.0, 0.72, 90, 2);
        w.spawnParticles(ParticleTypes.END_ROD, center.x, center.y + 1.0, center.z, 22, 0.8, 0.5, 0.8, 0.035);
        w.spawnParticles(ParticleTypes.SNOWFLAKE, center.x, center.y + 1.0, center.z, 35, 1.0, 0.8, 1.0, 0.04);
    }

    private static void crystalPrison(ServerPlayerEntity p, ServerWorld w) {
        Vec3d c = p.getPos().add(p.getRotationVec(1.0F).multiply(4.0));
        damageArea(p, w, c, 3.5, 6.0F, 120, 0);
        w.spawnParticles(ParticleTypes.SNOWFLAKE, c.x, c.y + 1.0, c.z, 90, 2.0, 1.6, 2.0, 0.025);
        w.spawnParticles(ParticleTypes.END_ROD, c.x, c.y + 1.0, c.z, 18, 1.2, 1.4, 1.2, 0.02);
    }

    private static void frozenDash(ServerPlayerEntity p, ServerWorld w) {
        Vec3d look = p.getRotationVec(1.0F);
        p.setVelocity(look.x * 1.7, Math.max(0.16, look.y * 0.5), look.z * 1.7);
        p.velocityModified = true;
        Vec3d c = p.getPos().add(look.multiply(2.0));
        damageArea(p, w, c, 2.3, 6.0F, 55, 0.4);
        w.spawnParticles(ParticleTypes.SNOWFLAKE, p.getX(), p.getY() + 0.8, p.getZ(), 30, 0.7, 0.5, 0.7, 0.035);
    }

    private static void absoluteZero(ServerPlayerEntity p, ServerWorld w) {
        Vec3d c = p.getPos().add(p.getRotationVec(1.0F).multiply(5.0));
        damageArea(p, w, c, 5.0, 9.0F, 180, 0.2);
        w.spawnParticles(ParticleTypes.SNOWFLAKE, c.x, c.y + 1.0, c.z, 110, 3.0, 1.5, 3.0, 0.02);
        w.spawnParticles(ParticleTypes.END_ROD, c.x, c.y + 1.0, c.z, 35, 2.0, 1.0, 2.0, 0.03);
    }

    private static void glacialApocalypse(ServerPlayerEntity p, ServerWorld w) {
        Vec3d c = p.getPos().add(p.getRotationVec(1.0F).multiply(4.0));
        damageArea(p, w, c, 8.0, 15.0F, 220, 1.2);
        w.spawnParticles(ParticleTypes.SNOWFLAKE, c.x, c.y + 1.0, c.z, 150, 4.0, 2.0, 4.0, 0.04);
        w.spawnParticles(ParticleTypes.CLOUD, c.x, c.y + 0.5, c.z, 70, 4.0, 0.7, 4.0, 0.03);
        w.spawnParticles(ParticleTypes.END_ROD, c.x, c.y + 1.0, c.z, 30, 3.0, 1.5, 3.0, 0.025);
    }

    private static void damageForward(ServerPlayerEntity p, ServerWorld w, float damage, double range, double dotMin, int slowTicks, int amplifier) {
        Vec3d look = p.getRotationVec(1.0F).normalize();
        Box box = p.getBoundingBox().expand(range, 2.5, range);
        for (LivingEntity target : w.getEntitiesByClass(LivingEntity.class, box, e -> e.isAlive() && e != p)) {
            Vec3d toward = target.getPos().subtract(p.getPos()).normalize();
            if (look.dotProduct(toward) >= dotMin && p.squaredDistanceTo(target) <= range * range) {
                target.damage(w.getDamageSources().playerAttack(p), damage);
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, slowTicks, amplifier));
            }
        }
    }

    private static void damageArea(ServerPlayerEntity p, ServerWorld w, Vec3d center, double radius, float damage, int slowTicks, double knockback) {
        Box box = new Box(center.x - radius, center.y - 2.5, center.z - radius,
                          center.x + radius, center.y + 3.0, center.z + radius);
        double r2 = radius * radius;
        for (LivingEntity target : w.getEntitiesByClass(LivingEntity.class, box, e -> e.isAlive() && e != p)) {
            if (target.squaredDistanceTo(center.x, center.y, center.z) <= r2) {
                target.damage(w.getDamageSources().playerAttack(p), damage);
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, slowTicks, 2));
                if (knockback > 0) {
                    double dx = target.getX() - center.x;
                    double dz = target.getZ() - center.z;
                    target.takeKnockback(knockback, -dx, -dz);
                }
            }
        }
    }
}
