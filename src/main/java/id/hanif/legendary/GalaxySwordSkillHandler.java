package id.hanif.legendary;

import id.hanif.legendary.item.ModItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class GalaxySwordSkillHandler {
    private static final String[] NAMES = {
        "Galaxy Slash", "Starfall", "Gravity Well", "Cosmic Guard"
    };
    private static final int[] COOLDOWNS = {4, 12, 14, 22};
    private static final Map<UUID, long[]> TIMERS = new HashMap<>();
    private static final Map<UUID, Integer> AURA_TICKS = new HashMap<>();

    private GalaxySwordSkillHandler() {}

    public static void activate(ServerPlayerEntity p, int skill) {
        if (skill < 0 || skill >= 4 || !(p.getWorld() instanceof ServerWorld w)) return;
        long now = System.currentTimeMillis();
        long[] timers = TIMERS.computeIfAbsent(p.getUuid(), ignored -> new long[4]);
        if (now < timers[skill]) {
            p.sendMessage(Text.literal(NAMES[skill] + " cooldown: " + ((timers[skill] - now + 999) / 1000) + "s"), true);
            return;
        }
        timers[skill] = now + COOLDOWNS[skill] * 1000L;
        p.sendMessage(Text.literal("§d✦ " + NAMES[skill] + " §7activated"), true);
        switch (skill) {
            case 0 -> galaxySlash(p, w);
            case 1 -> starfall(p, w);
            case 2 -> gravityWell(p, w);
            case 3 -> cosmicGuard(p, w);
            default -> { }
        }
    }

    private static Vec3d aimed(ServerPlayerEntity p, double distance) {
        return p.getPos().add(p.getRotationVec(1.0F).normalize().multiply(distance));
    }

    private static void hitArea(ServerPlayerEntity p, ServerWorld w, Vec3d c, double radius, float damage, double knockback) {
        Box box = new Box(c.x-radius, c.y-3, c.z-radius, c.x+radius, c.y+4, c.z+radius);
        double r2 = radius * radius;
        for (LivingEntity e : w.getEntitiesByClass(LivingEntity.class, box, x -> x.isAlive() && x != p)) {
            if (e.squaredDistanceTo(c.x,c.y,c.z) <= r2) {
                e.damage(w.getDamageSources().playerAttack(p), damage);
                if (knockback > 0) e.takeKnockback(knockback, c.x-e.getX(), c.z-e.getZ());
            }
        }
    }

    private static void galaxySlash(ServerPlayerEntity p, ServerWorld w) {
        Vec3d d=p.getRotationVec(1).normalize(), c=aimed(p,3.5);
        for(int i=0;i<9;i++){
            double side=(i-4)*0.42;
            Vec3d q=c.add(-d.z*side,0,d.x*side);
            w.spawnParticles(ParticleTypes.END_ROD,q.x,q.y+0.9,q.z,2,.04,.12,.04,.01);
        }
        w.spawnParticles(ParticleTypes.SWEEP_ATTACK,c.x,c.y+1,c.z,5,1.1,.45,1.1,.01);
        hitArea(p,w,c,4.2,8.5F,.45);
    }

    private static void starfall(ServerPlayerEntity p, ServerWorld w) {
        Vec3d c=aimed(p,7);
        hitArea(p,w,c,3.8,10.0F,.9);
        for(int i=0;i<7;i++){
            double a=i*Math.PI*2/7, r=1.2+(i%2)*1.0;
            w.spawnParticles(ParticleTypes.END_ROD,c.x+Math.cos(a)*r,c.y+2.6,c.z+Math.sin(a)*r,8,.12,1.1,.12,.035);
        }
        w.spawnParticles(ParticleTypes.FIREWORK,c.x,c.y+1,c.z,20,1.4,.8,1.4,.025);
    }

    private static void gravityWell(ServerPlayerEntity p, ServerWorld w) {
        Vec3d c=aimed(p,5);
        Box b=new Box(c.x-5,c.y-3,c.z-5,c.x+5,c.y+4,c.z+5);
        for(LivingEntity e:w.getEntitiesByClass(LivingEntity.class,b,x->x.isAlive()&&x!=p)){
            if(e.squaredDistanceTo(c.x,c.y,c.z)<=25){
                e.damage(w.getDamageSources().playerAttack(p),5.0F);
                e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS,70,2));
                Vec3d pull=c.subtract(e.getPos()).normalize().multiply(.55);
                e.addVelocity(pull.x,pull.y*.25,pull.z); e.velocityModified=true;
            }
        }
        for(int i=0;i<24;i++){double a=i*Math.PI*2/24;w.spawnParticles(ParticleTypes.PORTAL,c.x+Math.cos(a)*2.4,c.y+.8+Math.sin(a*2)*.5,c.z+Math.sin(a)*2.4,1,0,0,0,0);}
    }

    private static void cosmicGuard(ServerPlayerEntity p, ServerWorld w) {
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE,180,1));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION,220,1));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION,100,0));
        w.spawnParticles(ParticleTypes.END_ROD,p.getX(),p.getY()+1,p.getZ(),38,.8,1,.8,.025);
        w.spawnParticles(ParticleTypes.ENCHANT,p.getX(),p.getY()+1,p.getZ(),48,1,.8,1,.5);
    }

    public static void onBasicHit(ServerPlayerEntity p, LivingEntity target, ServerWorld w) {
        double x=target.getX(), y=target.getY()+target.getHeight()*.55, z=target.getZ();
        // Restrained star trail on every successful hit.
        w.spawnParticles(ParticleTypes.END_ROD,x,y,z,7,.22,.28,.22,.025);
        w.spawnParticles(ParticleTypes.CRIT,x,y,z,5,.18,.2,.18,.04);
        // Cosmic Echo passive: occasional extra impact, not a screen-filling explosion.
        if(p.getRandom().nextFloat()<.16F){
            target.damage(w.getDamageSources().playerAttack(p),3.0F);
            w.spawnParticles(ParticleTypes.FIREWORK,x,y,z,8,.3,.35,.3,.02);
            w.spawnParticles(ParticleTypes.PORTAL,x,y,z,12,.35,.4,.35,.05);
            p.sendMessage(Text.literal("§d✦ Cosmic Echo"),true);
        }
    }

    public static void tickAura(MinecraftServer server) {
        for(ServerPlayerEntity p:server.getPlayerManager().getPlayerList()){
            UUID id=p.getUuid();
            if(p.getMainHandStack().isOf(ModItems.GALAXY_SWORD) && p.getWorld() instanceof ServerWorld w){
                int tick=AURA_TICKS.getOrDefault(id,0)+1;
                if(tick>=10){
                    AURA_TICKS.put(id,0);
                    double angle=(p.age*.12);
                    for(int i=0;i<3;i++){
                        double a=angle+i*Math.PI*2/3, r=.72;
                        w.spawnParticles(ParticleTypes.END_ROD,p.getX()+Math.cos(a)*r,p.getY()+.9+Math.sin(a*.7)*.28,p.getZ()+Math.sin(a)*r,1,0,0,0,0);
                    }
                }else AURA_TICKS.put(id,tick);
            }else AURA_TICKS.remove(id);
        }
    }
}
