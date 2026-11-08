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

public final class DoombringerAxeSkillHandler {
    private static final String[] NAMES = {
        "Flame Slash", "Inferno Wheel", "Demon Guard", "Doombringer's Wrath"
    };
    private static final int[] COOLDOWN_SECONDS = {5, 12, 22, 45};
    private static final Map<UUID, long[]> COOLDOWNS = new HashMap<>();

    private DoombringerAxeSkillHandler() {}

    public static void activate(ServerPlayerEntity player, int skill) {
        if (skill < 0 || skill >= 4 || !(player.getWorld() instanceof ServerWorld world)) return;

        long now = System.currentTimeMillis();
        long[] timers = COOLDOWNS.computeIfAbsent(player.getUuid(), ignored -> new long[4]);
        if (now < timers[skill]) {
            long left = (timers[skill] - now + 999) / 1000;
            player.sendMessage(Text.literal(NAMES[skill] + " cooldown: " + left + "s"), true);
            return;
        }

        timers[skill] = now + COOLDOWN_SECONDS[skill] * 1000L;
        player.sendMessage(Text.literal("§4☠ §c" + NAMES[skill] + " §4activated"), true);

        switch (skill) {
            case 0 -> flameSlash(player, world);
            case 1 -> infernoWheel(player, world);
            case 2 -> demonGuard(player, world);
            case 3 -> doombringersWrath(player, world);
            default -> { }
        }
    }

    private static void flameSlash(ServerPlayerEntity player, ServerWorld world) {
        Vec3d direction = player.getRotationVec(1.0F).normalize();
        Vec3d center = player.getPos().add(direction.multiply(3.5));
        hitForward(player, world, center, 4.5, 9.0F, 4);
        world.spawnParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y + 1.0, center.z,
            12, 1.8, 0.9, 1.8, 0.01);
        world.spawnParticles(ParticleTypes.FLAME, center.x, center.y + 1.0, center.z,
            55, 1.8, 1.0, 1.8, 0.05);
        world.spawnParticles(ParticleTypes.LAVA, center.x, center.y + 1.0, center.z,
            10, 1.3, 0.6, 1.3, 0.02);
    }

    private static void infernoWheel(ServerPlayerEntity player, ServerWorld world) {
        Vec3d center = player.getPos().add(player.getRotationVec(1.0F).multiply(2.3));
        hitArea(player, world, center, 4.5, 8.0F, 5, 0.9);
        for (int y = 0; y < 7; y++) {
            double radius = 0.9 + y * 0.24;
            for (int i = 0; i < 16; i++) {
                double angle = Math.PI * 2.0 * i / 16.0 + y * 0.48;
                world.spawnParticles(ParticleTypes.FLAME,
                    center.x + Math.cos(angle) * radius, center.y + y * 0.42,
                    center.z + Math.sin(angle) * radius, 2, 0.03, 0.05, 0.03, 0.01);
            }
        }
        world.spawnParticles(ParticleTypes.SMOKE, center.x, center.y + 1.0, center.z,
            45, 1.8, 1.2, 1.8, 0.04);
    }

    private static void demonGuard(ServerPlayerEntity player, ServerWorld world) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 220, 1));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 220, 0));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 140, 0));
        world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1.0,
            player.getZ(), 75, 1.2, 1.1, 1.2, 0.05);
        world.spawnParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1.0,
            player.getZ(), 45, 0.9, 1.0, 0.9, 0.03);
        world.spawnParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.2,
            player.getZ(), 30, 1.0, 0.2, 1.0, 0.04);
    }

    private static void doombringersWrath(ServerPlayerEntity player, ServerWorld world) {
        Vec3d center = player.getPos().add(player.getRotationVec(1.0F).multiply(4.0));
        hitArea(player, world, center, 8.0, 18.0F, 8, 1.6);
        world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y + 1.0,
            center.z, 2, 0.3, 0.2, 0.3, 0.01);
        world.spawnParticles(ParticleTypes.FLAME, center.x, center.y + 1.0, center.z,
            190, 4.2, 1.5, 4.2, 0.09);
        world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x, center.y + 1.0, center.z,
            100, 3.2, 1.1, 3.2, 0.06);
        world.spawnParticles(ParticleTypes.LAVA, center.x, center.y + 1.0, center.z,
            35, 3.0, 0.9, 3.0, 0.03);
        world.spawnParticles(ParticleTypes.SMOKE, center.x, center.y + 0.5, center.z,
            90, 3.5, 1.0, 3.5, 0.04);
    }

    public static void onBasicHit(ServerPlayerEntity player, LivingEntity target, ServerWorld world) {
        double x = target.getX();
        double y = target.getY() + target.getHeight() * 0.55;
        double z = target.getZ();

        // Heavy impact feedback on every successful axe hit.
        world.spawnParticles(ParticleTypes.CRIT, x, y, z, 18, 0.45, 0.5, 0.45, 0.12);
        world.spawnParticles(ParticleTypes.SWEEP_ATTACK, x, y, z, 2, 0.35, 0.25, 0.35, 0.01);
        world.spawnParticles(ParticleTypes.SMOKE, x, y, z, 12, 0.35, 0.35, 0.35, 0.025);

        // Hell Anchor passive: a heavy eruption occasionally bursts from the target.
        if (player.getRandom().nextFloat() < 0.18F) {
            target.damage(world.getDamageSources().playerAttack(player), 4.0F);
            target.setOnFireFor(2);
            target.takeKnockback(0.85, player.getX() - target.getX(), player.getZ() - target.getZ());
            world.spawnParticles(ParticleTypes.EXPLOSION, x, y, z, 2, 0.25, 0.3, 0.25, 0.01);
            world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 35, 0.55, 0.65, 0.55, 0.06);
            world.spawnParticles(ParticleTypes.FLAME, x, y - 0.35, z, 45, 0.65, 0.18, 0.65, 0.07);
            world.spawnParticles(ParticleTypes.SMOKE, x, y, z, 24, 0.5, 0.5, 0.5, 0.035);
            player.sendMessage(Text.literal("§4☠ Hell Anchor!"), true);
        }
    }

    private static void hitForward(ServerPlayerEntity player, ServerWorld world, Vec3d center,
                                   double range, float damage, int fireSeconds) {
        Vec3d look = player.getRotationVec(1.0F).normalize();
        Box box = player.getBoundingBox().expand(range, 3.0, range);
        for (LivingEntity target : world.getEntitiesByClass(LivingEntity.class, box,
            entity -> entity.isAlive() && entity != player)) {
            Vec3d toward = target.getPos().subtract(player.getPos()).normalize();
            if (look.dotProduct(toward) > 0.15 && target.squaredDistanceTo(center) <= range * range) {
                target.damage(world.getDamageSources().playerAttack(player), damage);
                target.setOnFireFor(fireSeconds);
            }
        }
    }

    private static void hitArea(ServerPlayerEntity player, ServerWorld world, Vec3d center,
                                double radius, float damage, int fireSeconds, double knockback) {
        Box box = new Box(center.x - radius, center.y - 3.0, center.z - radius,
            center.x + radius, center.y + 3.0, center.z + radius);
        double radiusSquared = radius * radius;
        for (LivingEntity target : world.getEntitiesByClass(LivingEntity.class, box,
            entity -> entity.isAlive() && entity != player)) {
            if (target.squaredDistanceTo(center.x, center.y, center.z) <= radiusSquared) {
                target.damage(world.getDamageSources().playerAttack(player), damage);
                target.setOnFireFor(fireSeconds);
                target.takeKnockback(knockback, -(target.getX() - center.x), -(target.getZ() - center.z));
            }
        }
    }
}
