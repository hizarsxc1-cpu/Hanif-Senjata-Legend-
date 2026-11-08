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
import java.util.*;

public final class CelestialAxeSkillHandler {
    private static final String[] N = {"Tidal Cleave", "Aqua Ring", "Hydro Lance", "Tsunami Breaker", "Current Dash", "Ocean Guardian", "Abyssal Flood"};
    private static final int[] C = {4, 12, 10, 18, 6, 28, 60};
    private static final Map<UUID, long[]> T = new HashMap<>();
    private CelestialAxeSkillHandler() {}

    public static void activate(ServerPlayerEntity p, int s) {
        if (s < 0 || s >= 7 || !(p.getWorld() instanceof ServerWorld w)) return;
        long now = System.currentTimeMillis();
        long[] timers = T.computeIfAbsent(p.getUuid(), k -> new long[7]);
        if (now < timers[s]) {
            p.sendMessage(Text.literal(N[s] + " cooldown: " + ((timers[s] - now + 999) / 1000) + "s"), true);
            return;
        }
        timers[s] = now + C[s] * 1000L;
        p.sendMessage(Text.literal("§b✦ " + N[s] + " §7activated"), true);
        switch (s) {
            case 0 -> tidalCleave(p, w);
            case 1 -> aquaRing(p, w);
            case 2 -> hydroLance(p, w);
            case 3 -> tsunamiBreaker(p, w);
            case 4 -> currentDash(p, w);
            case 5 -> oceanGuardian(p, w);
            case 6 -> abyssalFlood(p, w);
            default -> {}
        }
    }

    private static List<LivingEntity> targets(ServerWorld w, ServerPlayerEntity p, Vec3d c, double r) {
        Box box = new Box(c.x-r, c.y-3, c.z-r, c.x+r, c.y+4, c.z+r);
        return w.getEntitiesByClass(LivingEntity.class, box, e -> e.isAlive() && e != p)
            .stream().filter(e -> e.squaredDistanceTo(c.x,c.y,c.z) <= r*r).toList();
    }
    private static void hit(ServerPlayerEntity p, ServerWorld w, Vec3d c, double r, float damage, double kb) {
        for (LivingEntity e : targets(w,p,c,r)) {
            e.damage(w.getDamageSources().playerAttack(p), damage);
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 50, 1));
            e.takeKnockback(kb, c.x-e.getX(), c.z-e.getZ());
        }
    }
    private static void splash(ServerWorld w, Vec3d c, int count, double spread) {
        w.spawnParticles(ParticleTypes.SPLASH,c.x,c.y+0.8,c.z,count,spread,0.7,spread,0.12);
        w.spawnParticles(ParticleTypes.BUBBLE,c.x,c.y+0.5,c.z,Math.max(4,count/3),spread*0.7,0.8,spread*0.7,0.04);
    }
    private static void tidalCleave(ServerPlayerEntity p, ServerWorld w) {
        Vec3d c=p.getPos().add(p.getRotationVec(1).multiply(3.2));
        hit(p,w,c,4.2,9,0.7); splash(w,c,36,1.8);
        w.spawnParticles(ParticleTypes.SWEEP_ATTACK,c.x,c.y+1,c.z,4,1.2,.5,1.2,.01);
    }
    private static void aquaRing(ServerPlayerEntity p, ServerWorld w) {
        Vec3d c=p.getPos();
        hit(p,w,c,5.0,6,1.0); splash(w,c,55,2.6);
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE,100,0));
    }
    private static void hydroLance(ServerPlayerEntity p, ServerWorld w) {
        Vec3d d=p.getRotationVec(1).normalize();
        for(int i=1;i<=7;i++){
            Vec3d c=p.getPos().add(d.multiply(i));
            for(LivingEntity e:targets(w,p,c,1.45)){
                e.damage(w.getDamageSources().playerAttack(p),8);
                e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS,80,2));
                e.takeKnockback(.45,-d.x,-d.z);
            }
            w.spawnParticles(ParticleTypes.BUBBLE,c.x,c.y+1,c.z,7,.25,.3,.25,.05);
            w.spawnParticles(ParticleTypes.SPLASH,c.x,c.y+1,c.z,5,.3,.3,.3,.08);
        }
    }
    private static void tsunamiBreaker(ServerPlayerEntity p, ServerWorld w) {
        Vec3d c=p.getPos().add(p.getRotationVec(1).multiply(5));
        hit(p,w,c,5.5,14,1.5); splash(w,c,90,3.0);
        w.spawnParticles(ParticleTypes.EXPLOSION,c.x,c.y+.5,c.z,2,.8,.3,.8,.01);
    }
    private static void currentDash(ServerPlayerEntity p, ServerWorld w) {
        Vec3d d=p.getRotationVec(1).normalize();
        p.setVelocity(d.x*1.75,Math.max(.22,d.y*.65),d.z*1.75); p.velocityModified=true;
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE,100,1));
        Vec3d c=p.getPos().add(d.multiply(2.2)); hit(p,w,c,2.4,7,.35); splash(w,p.getPos(),24,.7);
    }
    private static void oceanGuardian(ServerPlayerEntity p, ServerWorld w) {
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING,500,0));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE,240,1));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION,240,1));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION,120,1));
        splash(w,p.getPos(),42,1.3);
        w.spawnParticles(ParticleTypes.DOLPHIN,p.getX(),p.getY()+1,p.getZ(),16,.8,.8,.8,.08);
    }
    private static void abyssalFlood(ServerPlayerEntity p, ServerWorld w) {
        Vec3d c=p.getPos().add(p.getRotationVec(1).multiply(3.5));
        hit(p,w,c,8.0,18,1.8); splash(w,c,150,4.0);
        w.spawnParticles(ParticleTypes.BUBBLE,c.x,c.y+1,c.z,90,3.2,1.5,3.2,.16);
        p.heal(6);
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH,160,1));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING,400,0));
    }
}
