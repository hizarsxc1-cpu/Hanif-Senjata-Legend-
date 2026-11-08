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

public final class SolariaSkillHandler {
    private static final String[] N={"Light Pierce","Radiant Burst","Solar Rain","Flash Step","Holy Prison","Sun Guardian","Divine Judgment"};
    private static final int[] C={4,12,10,7,18,30,65};
    private static final Map<UUID,long[]> T=new HashMap<>();
    private SolariaSkillHandler(){}
    public static void activate(ServerPlayerEntity p,int s){
        if(s<0||s>=7||!(p.getWorld() instanceof ServerWorld w))return;
        long now=System.currentTimeMillis();long[] t=T.computeIfAbsent(p.getUuid(),k->new long[7]);
        if(now<t[s]){p.sendMessage(Text.literal(N[s]+" cooldown: "+((t[s]-now+999)/1000)+"s"),true);return;}
        t[s]=now+C[s]*1000L;p.sendMessage(Text.literal("§e✦ "+N[s]+" §7activated"),true);
        switch(s){case 0->lightPierce(p,w);case 1->radiantBurst(p,w);case 2->solarRain(p,w);case 3->flashStep(p,w);case 4->holyPrison(p,w);case 5->sunGuardian(p,w);case 6->divineJudgment(p,w);default->{}}
    }
    private static List<LivingEntity> targets(ServerWorld w,ServerPlayerEntity p,Vec3d c,double r){
        Box b=new Box(c.x-r,c.y-3,c.z-r,c.x+r,c.y+4,c.z+r);double r2=r*r;
        return w.getEntitiesByClass(LivingEntity.class,b,e->e.isAlive()&&e!=p).stream().filter(e->e.squaredDistanceTo(c.x,c.y,c.z)<=r2).toList();
    }
    private static void hit(ServerPlayerEntity p,ServerWorld w,Vec3d c,double r,float damage,double kb){
        for(LivingEntity e:targets(w,p,c,r)){e.damage(w.getDamageSources().playerAttack(p),damage);e.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING,70,0));if(kb>0)e.takeKnockback(kb,c.x-e.getX(),c.z-e.getZ());}
    }
    private static void shine(ServerWorld w,Vec3d c,int count,double spread){
        w.spawnParticles(ParticleTypes.END_ROD,c.x,c.y+1,c.z,count,spread,.8,spread,.035);
        w.spawnParticles(ParticleTypes.FIREWORK,c.x,c.y+1,c.z,Math.max(3,count/5),spread*.7,.6,spread*.7,.02);
    }
    private static void lightPierce(ServerPlayerEntity p,ServerWorld w){
        Vec3d d=p.getRotationVec(1).normalize();
        for(int i=1;i<=8;i++){Vec3d c=p.getPos().add(d.multiply(i));for(LivingEntity e:targets(w,p,c,1.35)){e.damage(w.getDamageSources().playerAttack(p),9);e.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS,35,0));e.takeKnockback(.45,-d.x,-d.z);}w.spawnParticles(ParticleTypes.END_ROD,c.x,c.y+1,c.z,8,.22,.25,.22,.025);}
        w.spawnParticles(ParticleTypes.FLASH,p.getX()+d.x*8,p.getY()+1,p.getZ()+d.z*8,1,0,0,0,0);
    }
    private static void radiantBurst(ServerPlayerEntity p,ServerWorld w){
        Vec3d c=p.getPos();hit(p,w,c,4.8,8,.8);shine(w,c,75,2.5);w.spawnParticles(ParticleTypes.FLASH,c.x,c.y+1,c.z,1,0,0,0,0);
    }
    private static void solarRain(ServerPlayerEntity p,ServerWorld w){
        Vec3d c=p.getPos().add(p.getRotationVec(1).multiply(6));hit(p,w,c,4.2,12,1.0);
        for(int i=0;i<7;i++){double a=i*Math.PI*2/7;double x=c.x+Math.cos(a)*2.2,z=c.z+Math.sin(a)*2.2;w.spawnParticles(ParticleTypes.END_ROD,x,c.y+3,z,12,.12,1.0,.12,.02);}
        shine(w,c,65,2.2);w.spawnParticles(ParticleTypes.FLASH,c.x,c.y+1,c.z,1,0,0,0,0);
    }
    private static void flashStep(ServerPlayerEntity p,ServerWorld w){
        Vec3d d=p.getRotationVec(1).normalize();Vec3d old=p.getPos();p.requestTeleport(p.getX()+d.x*5,p.getY()+Math.max(0,d.y*2),p.getZ()+d.z*5);
        Vec3d c=p.getPos();p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED,100,1));hit(p,w,c,2.5,7,.4);shine(w,old,30,.6);shine(w,c,26,.6);
    }
    private static void holyPrison(ServerPlayerEntity p,ServerWorld w){
        Vec3d c=p.getPos().add(p.getRotationVec(1).multiply(4));
        for(LivingEntity e:targets(w,p,c,4.0)){e.damage(w.getDamageSources().playerAttack(p),5);e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS,100,8));e.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING,100,0));}
        for(int y=0;y<4;y++)for(int i=0;i<12;i++){double a=i*Math.PI/6;w.spawnParticles(ParticleTypes.END_ROD,c.x+Math.cos(a)*2.4,c.y+y*.65,c.z+Math.sin(a)*2.4,1,0,0,0,0);}
        shine(w,c,45,1.8);
    }
    private static void sunGuardian(ServerPlayerEntity p,ServerWorld w){
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE,240,1));p.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION,240,1));p.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION,120,1));p.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING,100,0));shine(w,p.getPos(),65,1.2);
    }
    private static void divineJudgment(ServerPlayerEntity p,ServerWorld w){
        Vec3d c=p.getPos().add(p.getRotationVec(1).multiply(5));hit(p,w,c,7.0,19,1.6);
        for(int i=0;i<9;i++){double a=i*Math.PI*2/9;w.spawnParticles(ParticleTypes.END_ROD,c.x+Math.cos(a)*3.0,c.y+2,c.z+Math.sin(a)*3.0,18,.15,2.5,.15,.025);}
        shine(w,c,160,3.5);w.spawnParticles(ParticleTypes.FLASH,c.x,c.y+1,c.z,2,.5,.2,.5,0);
        p.heal(5);p.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH,160,1));p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE,160,0));
    }
}
