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

public final class ChronosSkillHandler {
    private static final String[] N={"Time Slash","Temporal Slow","Time Dash","Rewind","Time Prison","Future Strike","Eternal Hourglass"};
    private static final int[] C={4,14,7,25,18,12,70};
    private static final Map<UUID,long[]> T=new HashMap<>();
    private ChronosSkillHandler(){}
    public static void activate(ServerPlayerEntity p,int s){
        if(s<0||s>=7||!(p.getWorld() instanceof ServerWorld w))return;
        long now=System.currentTimeMillis();long[] t=T.computeIfAbsent(p.getUuid(),k->new long[7]);
        if(now<t[s]){p.sendMessage(Text.literal(N[s]+" cooldown: "+((t[s]-now+999)/1000)+"s"),true);return;}
        t[s]=now+C[s]*1000L;p.sendMessage(Text.literal("§b⌛ "+N[s]+" §7activated"),true);
        switch(s){case 0->timeSlash(p,w);case 1->temporalSlow(p,w);case 2->timeDash(p,w);case 3->rewind(p,w);case 4->timePrison(p,w);case 5->futureStrike(p,w);case 6->eternalHourglass(p,w);default->{}}
    }
    private static List<LivingEntity> targets(ServerWorld w,ServerPlayerEntity p,Vec3d c,double r){
        Box b=new Box(c.x-r,c.y-3,c.z-r,c.x+r,c.y+4,c.z+r);double r2=r*r;
        return w.getEntitiesByClass(LivingEntity.class,b,e->e.isAlive()&&e!=p).stream().filter(e->e.squaredDistanceTo(c.x,c.y,c.z)<=r2).toList();
    }
    private static void hit(ServerPlayerEntity p,ServerWorld w,Vec3d c,double r,float damage,double kb){
        for(LivingEntity e:targets(w,p,c,r)){e.damage(w.getDamageSources().playerAttack(p),damage);e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS,65,1));if(kb>0)e.takeKnockback(kb,c.x-e.getX(),c.z-e.getZ());}
    }
    private static void clock(ServerWorld w,Vec3d c,int count,double spread){
        w.spawnParticles(ParticleTypes.PORTAL,c.x,c.y+1,c.z,count,spread,.8,spread,.035);
        w.spawnParticles(ParticleTypes.END_ROD,c.x,c.y+1,c.z,Math.max(4,count/4),spread*.7,.7,spread*.7,.025);
    }
    private static void timeSlash(ServerPlayerEntity p,ServerWorld w){
        Vec3d d=p.getRotationVec(1).normalize();Vec3d c=p.getPos().add(d.multiply(3.5));
        for(LivingEntity e:targets(w,p,c,4)){if(d.dotProduct(e.getPos().subtract(p.getPos()).normalize())>.05){e.damage(w.getDamageSources().playerAttack(p),10);e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS,50,2));}}
        clock(w,c,40,1.5);w.spawnParticles(ParticleTypes.SWEEP_ATTACK,c.x,c.y+1,c.z,4,1,.5,1,.01);
    }
    private static void temporalSlow(ServerPlayerEntity p,ServerWorld w){
        Vec3d c=p.getPos().add(p.getRotationVec(1).multiply(4));
        for(LivingEntity e:targets(w,p,c,6)){e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS,180,3));e.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE,120,1));e.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING,120,0));e.damage(w.getDamageSources().playerAttack(p),4);}
        clock(w,c,90,3);
    }
    private static void timeDash(ServerPlayerEntity p,ServerWorld w){
        Vec3d d=p.getRotationVec(1).normalize();Vec3d old=p.getPos();p.requestTeleport(p.getX()+d.x*6,p.getY()+Math.max(0,d.y*2),p.getZ()+d.z*6);
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED,100,1));clock(w,old,35,.7);clock(w,p.getPos(),35,.7);
        hit(p,w,p.getPos(),2.4,7,.35);
    }
    private static void rewind(ServerPlayerEntity p,ServerWorld w){
        p.heal(10);p.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION,120,1));p.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION,160,1));p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED,100,0));clock(w,p.getPos(),65,1.3);
    }
    private static void timePrison(ServerPlayerEntity p,ServerWorld w){
        Vec3d c=p.getPos().add(p.getRotationVec(1).multiply(4));
        for(LivingEntity e:targets(w,p,c,4.5)){e.damage(w.getDamageSources().playerAttack(p),6);e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS,130,8));e.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS,130,1));e.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING,130,0));}
        for(int y=0;y<5;y++)for(int i=0;i<12;i++){double a=i*Math.PI/6;w.spawnParticles(ParticleTypes.PORTAL,c.x+Math.cos(a)*2.5,c.y+y*.55,c.z+Math.sin(a)*2.5,1,0,0,0,0);}
        clock(w,c,50,1.8);
    }
    private static void futureStrike(ServerPlayerEntity p,ServerWorld w){
        Vec3d d=p.getRotationVec(1).normalize();Vec3d c=p.getPos().add(d.multiply(3.2));
        for(LivingEntity e:targets(w,p,c,4)){if(d.dotProduct(e.getPos().subtract(p.getPos()).normalize())>.0){for(int i=0;i<3;i++)e.damage(w.getDamageSources().playerAttack(p),4.5F);e.takeKnockback(.5,-d.x,-d.z);}}
        for(int i=0;i<3;i++){Vec3d echo=c.add(d.multiply(i*.65));clock(w,echo,24,1.0);}
    }
    private static void eternalHourglass(ServerPlayerEntity p,ServerWorld w){
        Vec3d c=p.getPos().add(p.getRotationVec(1).multiply(4.5));hit(p,w,c,8,18,1.5);
        for(int i=0;i<16;i++){double a=i*Math.PI/8;double x=c.x+Math.cos(a)*3.5,z=c.z+Math.sin(a)*3.5;w.spawnParticles(ParticleTypes.PORTAL,x,c.y+2,z,16,.15,2.2,.15,.04);}
        clock(w,c,180,4);w.spawnParticles(ParticleTypes.EXPLOSION,c.x,c.y+1,c.z,3,1.4,.5,1.4,.01);
        p.heal(6);p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE,160,0));p.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH,160,1));
    }
}
