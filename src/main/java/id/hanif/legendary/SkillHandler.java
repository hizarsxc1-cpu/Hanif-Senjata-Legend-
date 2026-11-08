package id.hanif.legendary;

import id.hanif.legendary.item.ModItems;
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

public final class SkillHandler {
    private static final String[] NAMES = {
        "Flame Slash", "Fire Tornado", "Meteor Strike", "Inferno Burst",
        "Burning Dash", "Phoenix Awakening", "Hellstorm"
    };
    private static final int[] COOLDOWN_SECONDS = {3, 8, 12, 10, 5, 25, 45};
    private static final Map<UUID, long[]> COOLDOWNS = new HashMap<>();

    private SkillHandler() {}

    public static void activate(ServerPlayerEntity player, int skill) {
        if (skill < 0 || skill >= 7) return;
        if (player.getMainHandStack().isOf(ModItems.FROSTBITE)) {
            FrostbiteSkillHandler.activate(player, skill);
            return;
        }
        if (player.getMainHandStack().isOf(ModItems.THUNDER_HAMMER)) {
            ThunderHammerSkillHandler.activate(player, skill);
            return;
        }
        if (player.getMainHandStack().isOf(ModItems.VOID_SCYTHE)) {
            VoidScytheSkillHandler.activate(player, skill);
            return;
        }
        if (!player.getMainHandStack().isOf(ModItems.INFERNO_BLADE)) return;
        if (!(player.getWorld() instanceof ServerWorld world)) return;

        long now = System.currentTimeMillis();
        long[] timers = COOLDOWNS.computeIfAbsent(player.getUuid(), ignored -> new long[7]);
        if (now < timers[skill]) {
            long left = (timers[skill] - now + 999) / 1000;
            player.sendMessage(Text.literal(NAMES[skill] + " cooldown: " + left + "s"), true);
            return;
        }

        timers[skill] = now + COOLDOWN_SECONDS[skill] * 1000L;
        player.sendMessage(Text.literal("§6✦ " + NAMES[skill] + " §7activated"), true);
        switch (skill) {
            case 0 -> flameSlash(player, world);
            case 1 -> fireTornado(player, world);
            case 2 -> meteorStrike(player, world);
            case 3 -> infernoBurst(player, world);
            case 4 -> burningDash(player, world);
            case 5 -> phoenixAwakening(player, world);
            case 6 -> hellstorm(player, world);
            default -> { }
        }
    }

    private static void flameSlash(ServerPlayerEntity p, ServerWorld w) {
        Vec3d dir = p.getRotationVec(1.0F);
        Vec3d center = p.getPos().add(dir.multiply(3.2));
        damageForward(p, w, 8.0F, 5.0, 0.25, 3);
        w.spawnParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y + 1.0, center.z, 5, 1.4, 0.7, 1.4, 0.01);
        w.spawnParticles(ParticleTypes.FLAME, center.x, center.y + 1.0, center.z, 28, 1.5, 0.8, 1.5, 0.04);
    }

    private static void fireTornado(ServerPlayerEntity p, ServerWorld w) {
        Vec3d c = p.getPos().add(p.getRotationVec(1.0F).multiply(2.5));
        damageArea(p, w, c, 4.0, 6.0F, 3, 0.8);
        for (int y = 0; y < 5; y++) {
            double radius = 0.7 + y * 0.32;
            for (int i = 0; i < 8; i++) {
                double a = (Math.PI * 2 * i / 8.0) + y * 0.65;
                w.spawnParticles(ParticleTypes.FLAME, c.x + Math.cos(a) * radius, c.y + y * 0.55, c.z + Math.sin(a) * radius, 1, 0, 0, 0, 0);
            }
        }
    }

    private static void meteorStrike(ServerPlayerEntity p, ServerWorld w) {
        Vec3d c = p.getPos().add(p.getRotationVec(1.0F).multiply(7.0));
        c = new Vec3d(c.x, p.getY(), c.z);
        damageArea(p, w, c, 4.2, 10.0F, 5, 1.0);
        w.spawnParticles(ParticleTypes.LAVA, c.x, c.y + 1.0, c.z, 12, 1.8, 1.0, 1.8, 0.02);
        w.spawnParticles(ParticleTypes.EXPLOSION, c.x, c.y + 1.0, c.z, 2, 0.8, 0.3, 0.8, 0.01);
        w.spawnParticles(ParticleTypes.FLAME, c.x, c.y + 0.5, c.z, 55, 2.5, 0.8, 2.5, 0.06);
    }

    private static void infernoBurst(ServerPlayerEntity p, ServerWorld w) {
        damageArea(p, w, p.getPos(), 5.0, 11.0F, 4, 1.15);
        w.spawnParticles(ParticleTypes.FLAME, p.getX(), p.getY() + 1, p.getZ(), 85, 3.0, 1.2, 3.0, 0.07);
        w.spawnParticles(ParticleTypes.LAVA, p.getX(), p.getY() + 1, p.getZ(), 10, 2.0, 0.6, 2.0, 0.01);
    }

    private static void burningDash(ServerPlayerEntity p, ServerWorld w) {
        Vec3d dir = p.getRotationVec(1.0F);
        p.setVelocity(dir.x * 1.65, Math.max(0.18, dir.y * 0.55), dir.z * 1.65);
        p.velocityModified = true;
        damageArea(p, w, p.getPos().add(dir.multiply(2.0)), 2.2, 6.0F, 2, 0.35);
        w.spawnParticles(ParticleTypes.FLAME, p.getX(), p.getY() + 0.7, p.getZ(), 24, 0.65, 0.5, 0.65, 0.03);
    }

    private static void phoenixAwakening(ServerPlayerEntity p, ServerWorld w) {
        p.heal(10.0F);
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 200, 1));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 160, 0));
        w.spawnParticles(ParticleTypes.FLAME, p.getX(), p.getY() + 1.0, p.getZ(), 65, 1.2, 1.2, 1.2, 0.05);
        w.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, p.getX(), p.getY() + 1.0, p.getZ(), 18, 0.8, 1.0, 0.8, 0.04);
    }

    private static void hellstorm(ServerPlayerEntity p, ServerWorld w) {
        Vec3d c = p.getPos().add(p.getRotationVec(1.0F).multiply(3.0));
        damageArea(p, w, c, 8.0, 15.0F, 8, 1.4);
        w.spawnParticles(ParticleTypes.EXPLOSION, c.x, c.y + 1.0, c.z, 6, 3.0, 0.8, 3.0, 0.02);
        w.spawnParticles(ParticleTypes.FLAME, c.x, c.y + 1.0, c.z, 130, 4.0, 1.2, 4.0, 0.08);
        w.spawnParticles(ParticleTypes.LAVA, c.x, c.y + 1.0, c.z, 24, 3.0, 0.8, 3.0, 0.03);
    }

    private static void damageForward(ServerPlayerEntity p, ServerWorld w, float damage, double range, double dotMin, int fireSeconds) {
        Vec3d look = p.getRotationVec(1.0F).normalize();
        Box box = p.getBoundingBox().expand(range, 2.5, range);
        for (LivingEntity target : w.getEntitiesByClass(LivingEntity.class, box, e -> e.isAlive() && e != p)) {
            Vec3d toward = target.getPos().subtract(p.getPos()).normalize();
            if (look.dotProduct(toward) >= dotMin && p.squaredDistanceTo(target) <= range * range) {
                target.damage(w.getDamageSources().playerAttack(p), damage);
                target.setOnFireFor(fireSeconds);
            }
        }
    }

    private static void damageArea(ServerPlayerEntity p, ServerWorld w, Vec3d center, double radius, float damage, int fireSeconds, double knockback) {
        Box box = new Box(center.x - radius, center.y - 2.5, center.z - radius,
                          center.x + radius, center.y + 3.0, center.z + radius);
        double r2 = radius * radius;
        for (LivingEntity target : w.getEntitiesByClass(LivingEntity.class, box, e -> e.isAlive() && e != p)) {
            if (target.squaredDistanceTo(center.x, center.y, center.z) <= r2) {
                target.damage(w.getDamageSources().playerAttack(p), damage);
                target.setOnFireFor(fireSeconds);
                if (knockback > 0) {
                    double dx = target.getX() - center.x;
                    double dz = target.getZ() - center.z;
                    target.takeKnockback(knockback, -dx, -dz);
                }
            }
        }
    }
}
