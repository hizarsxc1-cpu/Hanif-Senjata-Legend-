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

import java.util.Comparator;
import java.util.List;

public final class ThunderHammerSkillHandler {
    private static final String[] NAMES = {
        "Thunder Crash", "Lightning Rain", "Storm Field", "Chain Bolt",
        "Sky Breaker", "Thunder Dash", "Divine Judgment"
    };
    private static final int[] COOLDOWNS = {4, 12, 10, 8, 15, 6, 55};

    private ThunderHammerSkillHandler() {}

    public static void activate(ServerPlayerEntity player, int skill) {
        if (skill < 0 || skill >= 7 || !(player.getWorld() instanceof ServerWorld world)) return;
        long now = System.currentTimeMillis();
        long[] timers = player.getPersistentState().getOrCreate(
            id -> new ThunderCooldownState(), "hlegendary_thunder_cooldowns"
        ).timers;
        if (now < timers[skill]) {
            long left = (timers[skill] - now + 999) / 1000;
            player.sendMessage(Text.literal(NAMES[skill] + " cooldown: " + left + "s"), true);
            return;
        }
        timers[skill] = now + COOLDOWNS[skill] * 1000L;
        player.sendMessage(Text.literal("§b⚡ " + NAMES[skill] + " §7activated"), true);
        switch (skill) {
            case 0 -> thunderCrash(player, world);
            case 1 -> lightningRain(player, world);
            case 2 -> stormField(player, world);
            case 3 -> chainBolt(player, world);
            case 4 -> skyBreaker(player, world);
            case 5 -> thunderDash(player, world);
            case 6 -> divineJudgment(player, world);
            default -> { }
        }
    }

    private static void thunderCrash(ServerPlayerEntity p, ServerWorld w) {
        Vec3d c = p.getPos().add(p.getRotationVec(1.0F).multiply(3.0));
        damageArea(p, w, c, 4.0, 9.0F, 0.9, 50);
        sparks(w, c, 52, 2.0);
        w.spawnParticles(ParticleTypes.EXPLOSION, c.x, c.y + 0.4, c.z, 2, 0.6, 0.2, 0.6, 0.01);
    }

    private static void lightningRain(ServerPlayerEntity p, ServerWorld w) {
        Vec3d c = p.getPos().add(p.getRotationVec(1.0F).multiply(6.0));
        damageArea(p, w, c, 5.0, 8.0F, 0.25, 70);
        for (int i = 0; i < 7; i++) {
            double a = Math.PI * 2.0 * i / 7.0;
            double x = c.x + Math.cos(a) * 3.2;
            double z = c.z + Math.sin(a) * 3.2;
            w.spawnParticles(ParticleTypes.ELECTRIC_SPARK, x, c.y + 1.0, z, 7, 0.35, 1.5, 0.35, 0.08);
        }
        sparks(w, c, 38, 3.0);
    }

    private static void stormField(ServerPlayerEntity p, ServerWorld w) {
        Vec3d c = p.getPos();
        damageArea(p, w, c, 5.5, 7.0F, 0.15, 45);
        w.spawnParticles(ParticleTypes.ELECTRIC_SPARK, c.x, c.y + 1.0, c.z, 72, 3.2, 1.2, 3.2, 0.07);
        w.spawnParticles(ParticleTypes.CLOUD, c.x, c.y + 0.2, c.z, 22, 2.8, 0.3, 2.8, 0.02);
    }

    private static void chainBolt(ServerPlayerEntity p, ServerWorld w) {
        Box box = p.getBoundingBox().expand(10.0, 5.0, 10.0);
        List<LivingEntity> targets = w.getEntitiesByClass(LivingEntity.class, box,
            e -> e.isAlive() && e != p).stream()
            .sorted(Comparator.comparingDouble(p::squaredDistanceTo)).limit(4).toList();
        for (LivingEntity target : targets) {
            target.damage(w.getDamageSources().playerAttack(p), 7.0F);
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 45, 1));
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 45, 0));
            w.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                target.getX(), target.getY() + 1.0, target.getZ(), 24, 0.45, 0.7, 0.45, 0.09);
        }
        if (targets.isEmpty()) sparks(w, p.getPos().add(p.getRotationVec(1.0F).multiply(3.0)), 18, 1.0);
    }

    private static void skyBreaker(ServerPlayerEntity p, ServerWorld w) {
        Vec3d c = p.getPos().add(p.getRotationVec(1.0F).multiply(7.0));
        damageArea(p, w, c, 4.5, 12.0F, 1.0, 90);
        w.spawnParticles(ParticleTypes.ELECTRIC_SPARK, c.x, c.y + 2.0, c.z, 68, 2.0, 2.2, 2.0, 0.08);
        w.spawnParticles(ParticleTypes.FLASH, c.x, c.y + 1.0, c.z, 1, 0, 0, 0, 0);
    }

    private static void thunderDash(ServerPlayerEntity p, ServerWorld w) {
        Vec3d look = p.getRotationVec(1.0F);
        p.setVelocity(look.x * 1.8, Math.max(0.18, look.y * 0.55), look.z * 1.8);
        p.velocityModified = true;
        Vec3d c = p.getPos().add(look.multiply(2.0));
        damageArea(p, w, c, 2.5, 6.0F, 0.5, 35);
        sparks(w, p.getPos(), 30, 0.8);
    }

    private static void divineJudgment(ServerPlayerEntity p, ServerWorld w) {
        Vec3d c = p.getPos().add(p.getRotationVec(1.0F).multiply(4.0));
        damageArea(p, w, c, 8.0, 16.0F, 1.3, 120);
        w.spawnParticles(ParticleTypes.ELECTRIC_SPARK, c.x, c.y + 1.5, c.z, 95, 4.0, 2.0, 4.0, 0.1);
        w.spawnParticles(ParticleTypes.FLASH, c.x, c.y + 1.0, c.z, 2, 1.0, 0.3, 1.0, 0);
        w.spawnParticles(ParticleTypes.EXPLOSION, c.x, c.y + 0.5, c.z, 3, 2.0, 0.4, 2.0, 0.01);
    }

    private static void damageArea(ServerPlayerEntity p, ServerWorld w, Vec3d center,
                                   double radius, float damage, double knockback, int slowTicks) {
        Box box = new Box(center.x - radius, center.y - 2.5, center.z - radius,
                          center.x + radius, center.y + 3.0, center.z + radius);
        double radiusSquared = radius * radius;
        for (LivingEntity target : w.getEntitiesByClass(LivingEntity.class, box,
            e -> e.isAlive() && e != p)) {
            if (target.squaredDistanceTo(center.x, center.y, center.z) > radiusSquared) continue;
            target.damage(w.getDamageSources().playerAttack(p), damage);
            if (slowTicks > 0) {
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, slowTicks, 1));
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, slowTicks, 0));
            }
            if (knockback > 0) {
                double dx = target.getX() - center.x;
                double dz = target.getZ() - center.z;
                target.takeKnockback(knockback, -dx, -dz);
            }
        }
    }

    private static void sparks(ServerWorld w, Vec3d c, int count, double spread) {
        w.spawnParticles(ParticleTypes.ELECTRIC_SPARK, c.x, c.y + 1.0, c.z,
            count, spread, 0.9, spread, 0.08);
    }

    private static final class ThunderCooldownState {
        private final long[] timers = new long[7];
    }
}
