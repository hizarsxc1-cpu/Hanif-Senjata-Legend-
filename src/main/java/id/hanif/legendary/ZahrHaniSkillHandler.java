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

/** Seven skills for the special Zahr & Hani eclipse greatsword. */
public final class ZahrHaniSkillHandler {
    private static final String[] NAMES = {
        "Solar Judgment", "Radiant Wings", "Heaven's Wrath",
        "Shadow Devourer", "Abyssal Slash", "Void Phantom", "Eternal Eclipse"
    };
    private static final int[] COOLDOWNS = {5, 12, 18, 10, 8, 16, 90};
    private static final Map<UUID, long[]> TIMERS = new HashMap<>();

    private ZahrHaniSkillHandler() {}

    public static void activate(ServerPlayerEntity player, int skill) {
        if (skill < 0 || skill >= 7 || !(player.getWorld() instanceof ServerWorld world)) return;
        long now = System.currentTimeMillis();
        long[] timers = TIMERS.computeIfAbsent(player.getUuid(), ignored -> new long[7]);
        if (now < timers[skill]) {
            player.sendMessage(Text.literal(NAMES[skill] + " cooldown: " + ((timers[skill] - now + 999) / 1000) + "s"), true);
            return;
        }
        timers[skill] = now + COOLDOWNS[skill] * 1000L;
        String color = skill <= 2 ? "§e☀ " : skill <= 5 ? "§5☾ " : "§d☯ ";
        player.sendMessage(Text.literal(color + NAMES[skill] + " §7activated"), true);
        switch (skill) {
            case 0 -> solarJudgment(player, world);
            case 1 -> radiantWings(player, world);
            case 2 -> heavensWrath(player, world);
            case 3 -> shadowDevourer(player, world);
            case 4 -> abyssalSlash(player, world);
            case 5 -> voidPhantom(player, world);
            case 6 -> eternalEclipse(player, world);
            default -> { }
        }
    }

    private static List<LivingEntity> targets(ServerWorld world, ServerPlayerEntity player, Vec3d center, double radius) {
        Box box = new Box(center.x - radius, center.y - 3, center.z - radius,
                          center.x + radius, center.y + 4, center.z + radius);
        double r2 = radius * radius;
        return world.getEntitiesByClass(LivingEntity.class, box, e -> e.isAlive() && e != player)
            .stream().filter(e -> e.squaredDistanceTo(center.x, center.y, center.z) <= r2).toList();
    }

    private static void lightParticles(ServerWorld world, Vec3d center, int count, double spread) {
        world.spawnParticles(ParticleTypes.END_ROD, center.x, center.y + 1, center.z,
            count, spread, 1.0, spread, 0.025);
        world.spawnParticles(ParticleTypes.FIREWORK, center.x, center.y + 1, center.z,
            Math.max(4, count / 5), spread * 0.7, 0.7, spread * 0.7, 0.02);
    }

    private static void darkParticles(ServerWorld world, Vec3d center, int count, double spread) {
        world.spawnParticles(ParticleTypes.PORTAL, center.x, center.y + 1, center.z,
            count, spread, 1.0, spread, 0.04);
        world.spawnParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 1, center.z,
            Math.max(4, count / 4), spread * 0.7, 0.8, spread * 0.7, 0.02);
    }

    private static void solarJudgment(ServerPlayerEntity p, ServerWorld w) {
        Vec3d dir = p.getRotationVec(1).normalize();
        for (int i = 1; i <= 8; i++) {
            Vec3d point = p.getPos().add(dir.multiply(i));
            for (LivingEntity e : targets(w, p, point, 1.35)) {
                if (dir.dotProduct(e.getPos().subtract(p.getPos()).normalize()) > 0.15) {
                    e.damage(w.getDamageSources().playerAttack(p), 10.0F);
                    e.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 70, 0));
                    e.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 50, 0));
                }
            }
            lightParticles(w, point, 9, 0.25);
        }
        w.spawnParticles(ParticleTypes.FLASH, p.getX() + dir.x * 8, p.getY() + 1,
            p.getZ() + dir.z * 8, 1, 0, 0, 0, 0);
    }

    private static void radiantWings(ServerPlayerEntity p, ServerWorld w) {
        Vec3d old = p.getPos();
        Vec3d dir = p.getRotationVec(1).normalize();
        p.requestTeleport(p.getX() + dir.x * 5.5, p.getY() + Math.max(0, dir.y * 2),
            p.getZ() + dir.z * 5.5);
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 120, 1));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 50, 0));
        lightParticles(w, old, 42, 0.9);
        lightParticles(w, p.getPos(), 48, 1.0);
        for (LivingEntity e : targets(w, p, p.getPos(), 2.7)) {
            e.damage(w.getDamageSources().playerAttack(p), 8.0F);
            e.takeKnockback(0.65, -dir.x, -dir.z);
        }
    }

    private static void heavensWrath(ServerPlayerEntity p, ServerWorld w) {
        Vec3d center = p.getPos().add(p.getRotationVec(1).multiply(6));
        for (int i = 0; i < 9; i++) {
            double a = i * Math.PI * 2 / 9.0;
            double x = center.x + Math.cos(a) * 2.7;
            double z = center.z + Math.sin(a) * 2.7;
            w.spawnParticles(ParticleTypes.END_ROD, x, center.y + 3.5, z, 18, 0.12, 2.4, 0.12, 0.025);
            w.spawnParticles(ParticleTypes.FIREWORK, x, center.y + 0.7, z, 5, 0.25, 0.5, 0.25, 0.02);
        }
        for (LivingEntity e : targets(w, p, center, 5.0)) {
            e.damage(w.getDamageSources().playerAttack(p), 14.0F);
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 90, 0));
            e.takeKnockback(1.0, center.x - e.getX(), center.z - e.getZ());
        }
        lightParticles(w, center, 125, 3.5);
        w.spawnParticles(ParticleTypes.FLASH, center.x, center.y + 1, center.z, 2, 1, 0.5, 1, 0);
    }

    private static void shadowDevourer(ServerPlayerEntity p, ServerWorld w) {
        Vec3d center = p.getPos().add(p.getRotationVec(1).multiply(4));
        for (LivingEntity e : targets(w, p, center, 7.0)) {
            e.damage(w.getDamageSources().playerAttack(p), 7.0F);
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 2));
            Vec3d pull = center.subtract(e.getPos()).normalize().multiply(0.85);
            e.setVelocity(e.getVelocity().add(pull.x, 0.18, pull.z));
            e.velocityModified = true;
        }
        darkParticles(w, center, 110, 3.0);
    }

    private static void abyssalSlash(ServerPlayerEntity p, ServerWorld w) {
        Vec3d dir = p.getRotationVec(1).normalize();
        Vec3d center = p.getPos().add(dir.multiply(3.5));
        for (LivingEntity e : targets(w, p, center, 4.5)) {
            if (dir.dotProduct(e.getPos().subtract(p.getPos()).normalize()) > 0.0) {
                e.damage(w.getDamageSources().playerAttack(p), 13.0F);
                e.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 50, 0));
                e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 45, 1));
            }
        }
        darkParticles(w, center, 85, 2.2);
        w.spawnParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y + 1, center.z, 7, 1.5, 0.7, 1.5, 0.01);
    }

    private static void voidPhantom(ServerPlayerEntity p, ServerWorld w) {
        Vec3d old = p.getPos();
        Vec3d dir = p.getRotationVec(1).normalize();
        p.requestTeleport(p.getX() + dir.x * 6.0, p.getY() + Math.max(0, dir.y * 2),
            p.getZ() + dir.z * 6.0);
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 35, 0));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 80, 1));
        darkParticles(w, old, 38, 0.8);
        darkParticles(w, p.getPos(), 55, 1.0);
        for (LivingEntity e : targets(w, p, p.getPos(), 3.0)) {
            e.damage(w.getDamageSources().playerAttack(p), 11.0F);
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 80, 1));
        }
    }

    private static void eternalEclipse(ServerPlayerEntity p, ServerWorld w) {
        Vec3d center = p.getPos().add(p.getRotationVec(1).multiply(5));
        for (LivingEntity e : targets(w, p, center, 9.0)) {
            e.damage(w.getDamageSources().playerAttack(p), 22.0F);
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 110, 3));
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 110, 0));
            Vec3d pull = center.subtract(e.getPos()).normalize().multiply(1.1);
            e.setVelocity(e.getVelocity().add(pull.x, 0.28, pull.z));
            e.velocityModified = true;
        }
        for (int i = 0; i < 2; i++) {
            double y = center.y + 0.5 + i * 1.1;
            for (int j = 0; j < 24; j++) {
                double a = j * Math.PI / 12.0 + i * 0.13;
                double radius = 3.2 + i * 0.8;
                Vec3d point = new Vec3d(center.x + Math.cos(a) * radius, y, center.z + Math.sin(a) * radius);
                if ((j & 1) == 0) lightParticles(w, point, 5, 0.08);
                else darkParticles(w, point, 5, 0.08);
            }
        }
        lightParticles(w, center, 150, 4.5);
        darkParticles(w, center, 150, 4.5);
        w.spawnParticles(ParticleTypes.FLASH, center.x, center.y + 1, center.z, 3, 1.5, 0.8, 1.5, 0);
        w.spawnParticles(ParticleTypes.EXPLOSION, center.x, center.y + 1, center.z, 5, 2.0, 0.8, 2.0, 0.01);
        p.heal(8.0F);
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 160, 1));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 160, 1));
    }
}
