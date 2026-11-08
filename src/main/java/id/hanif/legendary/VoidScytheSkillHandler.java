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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class VoidScytheSkillHandler {
    private static final String[] NAMES = {
        "Void Slash", "Black Hole", "Shadow Step", "Soul Drain",
        "Dark Nova", "Rift Collapse", "Eternal Void"
    };
    private static final int[] COOLDOWNS = {4, 14, 7, 12, 10, 24, 60};
    private static final Map<UUID, long[]> TIMERS = new HashMap<>();

    private VoidScytheSkillHandler() {}

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
        player.sendMessage(Text.literal("§5✦ " + NAMES[skill] + " §7activated"), true);
        switch (skill) {
            case 0 -> voidSlash(player, world);
            case 1 -> blackHole(player, world);
            case 2 -> shadowStep(player, world);
            case 3 -> soulDrain(player, world);
            case 4 -> darkNova(player, world);
            case 5 -> riftCollapse(player, world);
            case 6 -> eternalVoid(player, world);
            default -> { }
        }
    }

    private static void voidSlash(ServerPlayerEntity p, ServerWorld w) {
        Vec3d look = p.getRotationVec(1.0F).normalize();
        Vec3d center = p.getPos().add(look.multiply(3.5));
        damageForward(p, w, 8.0F, 5.8, 0.28, 55);
        w.spawnParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y + 1.0, center.z, 5, 1.1, 0.6, 1.1, 0.01);
        w.spawnParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 1.0, center.z, 32, 1.2, 0.8, 1.2, 0.06);
        w.spawnParticles(ParticleTypes.DRAGON_BREATH, center.x, center.y + 1.0, center.z, 14, 0.8, 0.4, 0.8, 0.02);
    }

    private static void blackHole(ServerPlayerEntity p, ServerWorld w) {
        Vec3d center = p.getPos().add(p.getRotationVec(1.0F).multiply(5.0));
        List<LivingEntity> targets = nearby(w, p, center, 6.0);
        for (LivingEntity target : targets) {
            pullToward(target, center, 0.72);
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 70, 2));
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 50, 0));
            target.damage(w.getDamageSources().playerAttack(p), 4.0F);
        }
        w.spawnParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 1.0, center.z, 90, 2.2, 1.4, 2.2, 0.12);
        w.spawnParticles(ParticleTypes.DRAGON_BREATH, center.x, center.y + 1.0, center.z, 30, 1.0, 0.8, 1.0, 0.025);
        w.spawnParticles(ParticleTypes.PORTAL, center.x, center.y + 1.0, center.z, 45, 1.4, 1.2, 1.4, 0.5);
    }

    private static void shadowStep(ServerPlayerEntity p, ServerWorld w) {
        Vec3d look = p.getRotationVec(1.0F);
        p.setVelocity(look.x * 2.0, Math.max(0.12, look.y * 0.45), look.z * 2.0);
        p.velocityModified = true;
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 24, 0, false, false, true));
        w.spawnParticles(ParticleTypes.PORTAL, p.getX(), p.getY() + 0.9, p.getZ(), 34, 0.6, 0.7, 0.6, 0.25);
        Vec3d end = p.getPos().add(look.multiply(2.5));
        w.spawnParticles(ParticleTypes.REVERSE_PORTAL, end.x, end.y + 0.8, end.z, 28, 0.7, 0.7, 0.7, 0.12);
    }

    private static void soulDrain(ServerPlayerEntity p, ServerWorld w) {
        Vec3d center = p.getPos().add(p.getRotationVec(1.0F).multiply(3.0));
        List<LivingEntity> targets = nearby(w, p, center, 4.5);
        int hits = 0;
        for (LivingEntity target : targets) {
            if (target.damage(w.getDamageSources().playerAttack(p), 6.0F)) {
                hits++;
                w.spawnParticles(ParticleTypes.DRAGON_BREATH, target.getX(), target.getY() + 1.0, target.getZ(), 14, 0.35, 0.5, 0.35, 0.025);
            }
        }
        if (hits > 0) p.heal(Math.min(8.0F, hits * 2.0F));
        w.spawnParticles(ParticleTypes.REVERSE_PORTAL, p.getX(), p.getY() + 1.0, p.getZ(), 34, 1.0, 0.8, 1.0, 0.08);
    }

    private static void darkNova(ServerPlayerEntity p, ServerWorld w) {
        Vec3d center = p.getPos();
        damageArea(p, w, center, 5.0, 10.0F, 0.85, 80);
        w.spawnParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 1.0, center.z, 105, 3.0, 1.3, 3.0, 0.11);
        w.spawnParticles(ParticleTypes.DRAGON_BREATH, center.x, center.y + 1.0, center.z, 42, 2.2, 0.9, 2.2, 0.03);
        w.spawnParticles(ParticleTypes.EXPLOSION, center.x, center.y + 0.5, center.z, 2, 1.0, 0.3, 1.0, 0.01);
    }

    private static void riftCollapse(ServerPlayerEntity p, ServerWorld w) {
        Vec3d center = p.getPos().add(p.getRotationVec(1.0F).multiply(7.0));
        List<LivingEntity> targets = nearby(w, p, center, 6.5);
        for (LivingEntity target : targets) {
            pullToward(target, center, 0.48);
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 55, 0));
            target.damage(w.getDamageSources().playerAttack(p), 12.0F);
        }
        w.spawnParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 1.0, center.z, 125, 3.2, 1.8, 3.2, 0.14);
        w.spawnParticles(ParticleTypes.DRAGON_BREATH, center.x, center.y + 1.0, center.z, 55, 2.5, 1.2, 2.5, 0.035);
        w.spawnParticles(ParticleTypes.FLASH, center.x, center.y + 0.7, center.z, 1, 0, 0, 0, 0);
    }

    private static void eternalVoid(ServerPlayerEntity p, ServerWorld w) {
        Vec3d center = p.getPos().add(p.getRotationVec(1.0F).multiply(4.0));
        List<LivingEntity> targets = nearby(w, p, center, 8.0);
        for (LivingEntity target : targets) {
            pullToward(target, center, 0.62);
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 90, 0));
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 90, 3));
            target.damage(w.getDamageSources().playerAttack(p), 16.0F);
        }
        w.spawnParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 1.0, center.z, 180, 4.0, 2.0, 4.0, 0.16);
        w.spawnParticles(ParticleTypes.DRAGON_BREATH, center.x, center.y + 1.0, center.z, 90, 3.0, 1.5, 3.0, 0.045);
        w.spawnParticles(ParticleTypes.PORTAL, center.x, center.y + 1.0, center.z, 90, 3.5, 1.5, 3.5, 0.7);
        w.spawnParticles(ParticleTypes.EXPLOSION, center.x, center.y + 0.5, center.z, 5, 2.5, 0.8, 2.5, 0.02);
    }

    private static List<LivingEntity> nearby(ServerWorld w, ServerPlayerEntity p, Vec3d center, double radius) {
        Box box = new Box(center.x - radius, center.y - 3.0, center.z - radius,
                          center.x + radius, center.y + 4.0, center.z + radius);
        double r2 = radius * radius;
        return w.getEntitiesByClass(LivingEntity.class, box, e -> e.isAlive() && e != p)
            .stream().filter(e -> e.squaredDistanceTo(center.x, center.y, center.z) <= r2)
            .sorted(Comparator.comparingDouble(e -> e.squaredDistanceTo(center.x, center.y, center.z)))
            .toList();
    }

    private static void pullToward(LivingEntity target, Vec3d center, double strength) {
        Vec3d delta = center.subtract(target.getPos()).normalize().multiply(strength);
        target.setVelocity(target.getVelocity().add(delta));
        target.velocityModified = true;
    }

    private static void damageForward(ServerPlayerEntity p, ServerWorld w, float damage, double range, double dotMin, int slowTicks) {
        Vec3d look = p.getRotationVec(1.0F).normalize();
        Box box = p.getBoundingBox().expand(range, 2.5, range);
        for (LivingEntity target : w.getEntitiesByClass(LivingEntity.class, box, e -> e.isAlive() && e != p)) {
            Vec3d toward = target.getPos().subtract(p.getPos()).normalize();
            if (look.dotProduct(toward) >= dotMin && p.squaredDistanceTo(target) <= range * range) {
                target.damage(w.getDamageSources().playerAttack(p), damage);
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, slowTicks, 0));
            }
        }
    }

    private static void damageArea(ServerPlayerEntity p, ServerWorld w, Vec3d center, double radius,
                                   float damage, double knockback, int slowTicks) {
        for (LivingEntity target : nearby(w, p, center, radius)) {
            target.damage(w.getDamageSources().playerAttack(p), damage);
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, slowTicks, 1));
            if (knockback > 0) {
                double dx = target.getX() - center.x;
                double dz = target.getZ() - center.z;
                target.takeKnockback(knockback, -dx, -dz);
            }
        }
    }
}
