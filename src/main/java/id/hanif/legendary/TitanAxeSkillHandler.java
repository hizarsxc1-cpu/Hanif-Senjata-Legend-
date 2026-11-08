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
public final class TitanAxeSkillHandler {
 private static final String[] N={"Earthquake","Boulder Smash","Titan Guard","Ground Breaker","Stone Fist","Mountain Fall","Titan Awakening"};
 private static final int[] C={7,12,18,10,9,24,60};
 private static final Map<UUID,long[]> T=new HashMap<>();
 private TitanAxeSkillHandler(){}
 public static void activate(ServerPlayerEntity p,int s){
  if(s<0||s>=7||!(p.getWorld() instanceof ServerWorld w))return;
  long n=System.currentTimeMillis();long[] t=T.computeIfAbsent(p.getUuid(),k->new long[7]);
  if(n<t[s]){p.sendMessage(Text.literal(N[s]+" cooldown: "+((t[s]-n+999)/1000)+"s"),true);return;}
  t[s]=n+C[s]*1000L;p.sendMessage(Text.literal("§6✦ "+N[s]+" §7activated"),true);
  switch(s){case 0->earthquake(p,w);case 1->boulder(p,w);case 2->guard(p,w);case 3->breaker(p,w);case 4->fist(p,w);case 5->mountain(p,w);case 6->awakening(p,w);default->{}}
 }
 private static List<LivingEntity> targets(ServerWorld w,ServerPlayerEntity p,Vec3d c,double r){
  Box b=new Box(c.x-r,c.y-3,c.z-r,c.x+r,c.y+4,c.z+r);double q=r*r;
  return w.getEntitiesByClass(LivingEntity.class,b,e->e.isAlive()&&e!=p).stream().filter(e->e.squaredDistanceTo(c.x,c.y,c.z)<=q).toList();
 }
 private static void hit(ServerPlayerEntity p,ServerWorld w,Vec3d c,double r,float d,double k){
  for(LivingEntity e:targets(w,p,c,r)){e.damage(w.getDamageSources().playerAttack(p),d);e.takeKnockback(k,c.x-e.getX(),c.z-e.getZ());}
 }
 private static void earthquake(ServerPlayerEntity p,ServerWorld w){Vec3d c=p.getPos();hit(p,w,c,5,7,0.9);w.spawnParticles(ParticleTypes.CLOUD,c.x,c.y+.2,c.z,48,2.8,.25,2.8,.15);w.spawnParticles(ParticleTypes.CLOUD,c.x,c.y+.3,c.z,22,2,.2,2,.04);}
 private static void boulder(ServerPlayerEntity p,ServerWorld w){Vec3d c=p.getPos().add(p.getRotationVec(1).multiply(4));hit(p,w,c,3.5,12,1.25);w.spawnParticles(ParticleTypes.CLOUD,c.x,c.y+.4,c.z,60,1.6,.8,1.6,.15);w.spawnParticles(ParticleTypes.EXPLOSION,c.x,c.y+.5,c.z,2,.5,.2,.5,.01);}
 private static void guard(ServerPlayerEntity p,ServerWorld w){p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE,240,1));p.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION,240,1));w.spawnParticles(ParticleTypes.CLOUD,p.getX(),p.getY()+.8,p.getZ(),36,1,1,1,.12);w.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING,p.getX(),p.getY()+1,p.getZ(),10,.6,.8,.6,.03);}
 private static void breaker(ServerPlayerEntity p,ServerWorld w){Vec3d c=p.getPos().add(p.getRotationVec(1).multiply(5));hit(p,w,c,4.2,9,1);w.spawnParticles(ParticleTypes.CLOUD,c.x,c.y+.2,c.z,65,2.3,.25,2.3,.16);w.spawnParticles(ParticleTypes.CLOUD,c.x,c.y+.3,c.z,24,1.7,.2,1.7,.05);}
 private static void fist(ServerPlayerEntity p,ServerWorld w){Vec3d l=p.getRotationVec(1).normalize(),c=p.getPos().add(l.multiply(2.5));for(LivingEntity e:targets(w,p,c,3)){if(l.dotProduct(e.getPos().subtract(p.getPos()).normalize())>.1){e.damage(w.getDamageSources().playerAttack(p),10);e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS,70,1));e.takeKnockback(.65,-l.x,-l.z);}}w.spawnParticles(ParticleTypes.CLOUD,c.x,c.y+.8,c.z,32,.8,.6,.8,.14);w.spawnParticles(ParticleTypes.CRIT,c.x,c.y+1,c.z,14,.7,.5,.7,.08);}
 private static void mountain(ServerPlayerEntity p,ServerWorld w){Vec3d l=p.getRotationVec(1);p.setVelocity(l.x*.8,Math.max(.9,l.y*.7+.8),l.z*.8);p.velocityModified=true;p.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING,50,0));p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE,80,0));w.spawnParticles(ParticleTypes.CLOUD,p.getX(),p.getY(),p.getZ(),30,.8,.4,.8,.12);}
 private static void awakening(ServerPlayerEntity p,ServerWorld w){p.heal(8);p.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH,300,1));p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE,300,1));p.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION,300,2));Vec3d c=p.getPos();hit(p,w,c,7,15,1.5);w.spawnParticles(ParticleTypes.CLOUD,c.x,c.y+.7,c.z,90,3,1,3,.16);w.spawnParticles(ParticleTypes.EXPLOSION,c.x,c.y+.5,c.z,4,1.7,.5,1.7,.01);w.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING,c.x,c.y+1,c.z,18,1.4,1,1.4,.04);}
}