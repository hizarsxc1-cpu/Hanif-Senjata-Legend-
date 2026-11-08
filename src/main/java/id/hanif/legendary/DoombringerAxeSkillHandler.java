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
 private static final String[] NAMES={"Flame Slash","Inferno Wheel","Blazing Leap","Hellfire Scythe","Wrath Eruption","Demon Guard","Doombringer's Wrath"};
 private static final int[] COOLDOWN={4,10,8,14,18,24,50};
 private static final Map<UUID,long[]> TIMERS=new HashMap<>();
 private DoombringerAxeSkillHandler(){}
 public static void activate(ServerPlayerEntity p,int skill){
  if(skill<0||skill>=7||!(p.getWorld() instanceof ServerWorld w))return;
  long now=System.currentTimeMillis(); long[] t=TIMERS.computeIfAbsent(p.getUuid(),id->new long[7]);
  if(now<t[skill]){p.sendMessage(Text.literal(NAMES[skill]+" cooldown: "+((t[skill]-now+999)/1000)+"s"),true);return;}
  t[skill]=now+COOLDOWN[skill]*1000L;p.sendMessage(Text.literal("§4☠ §c"+NAMES[skill]+" §4activated"),true);
  switch(skill){case 0->flameSlash(p,w);case 1->infernoWheel(p,w);case 2->blazingLeap(p,w);case 3->hellfireScythe(p,w);case 4->wrathEruption(p,w);case 5->demonGuard(p,w);case 6->doombringersWrath(p,w);default->{}}
 }
 private static void flameSlash(ServerPlayerEntity p,ServerWorld w){
  Vec3d d=p.getRotationVec(1).normalize(),c=p.getPos().add(d.multiply(3.5));hitForward(p,w,c,4.5,9,4);
  w.spawnParticles(ParticleTypes.SWEEP_ATTACK,c.x,c.y+1,c.z,8,1.7,.8,1.7,.01);w.spawnParticles(ParticleTypes.FLAME,c.x,c.y+1,c.z,45,1.7,.9,1.7,.04);w.spawnParticles(ParticleTypes.LAVA,c.x,c.y+1,c.z,8,1.2,.5,1.2,.01);
 }
 private static void infernoWheel(ServerPlayerEntity p,ServerWorld w){
  Vec3d c=p.getPos().add(p.getRotationVec(1).multiply(2.2));hitArea(p,w,c,4.2,7,4,.8);
  for(int y=0;y<6;y++)for(int i=0;i<14;i++){double a=Math.PI*2*i/14.0+y*.55,r=1+y*.22;w.spawnParticles(ParticleTypes.FLAME,c.x+Math.cos(a)*r,c.y+y*.42,c.z+Math.sin(a)*r,2,.04,.04,.04,.01);}
 }
 private static void blazingLeap(ServerPlayerEntity p,ServerWorld w){
  Vec3d d=p.getRotationVec(1).normalize();p.setVelocity(d.x*1.45,.95,d.z*1.45);p.velocityModified=true;
  w.spawnParticles(ParticleTypes.FLAME,p.getX(),p.getY()+.4,p.getZ(),35,.7,.3,.7,.04);w.spawnParticles(ParticleTypes.LAVA,p.getX(),p.getY(),p.getZ(),5,.4,.1,.4,.01);
 }
 private static void hellfireScythe(ServerPlayerEntity p,ServerWorld w){
  Vec3d d=p.getRotationVec(1).normalize(),c=p.getPos().add(d.multiply(5));hitForward(p,w,c,6,12,5);
  w.spawnParticles(ParticleTypes.SWEEP_ATTACK,c.x,c.y+1,c.z,14,2,1,2,.01);w.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME,c.x,c.y+1,c.z,32,1.8,.8,1.8,.03);
 }
 private static void wrathEruption(ServerPlayerEntity p,ServerWorld w){
  Vec3d c=p.getPos().add(p.getRotationVec(1).multiply(3));hitArea(p,w,c,5.5,13,6,1.2);
  w.spawnParticles(ParticleTypes.EXPLOSION,c.x,c.y+.7,c.z,4,1.5,.5,1.5,.01);w.spawnParticles(ParticleTypes.FLAME,c.x,c.y+.5,c.z,100,2.8,1.2,2.8,.07);w.spawnParticles(ParticleTypes.LAVA,c.x,c.y+.5,c.z,20,2,.5,2,.02);
 }
 private static void demonGuard(ServerPlayerEntity p,ServerWorld w){
  p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE,160,1));p.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE,160,0));p.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH,100,0));
  w.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME,p.getX(),p.getY()+1,p.getZ(),55,1,1,1,.04);w.spawnParticles(ParticleTypes.SMOKE,p.getX(),p.getY()+1,p.getZ(),30,.8,.8,.8,.02);
 }
 private static void doombringersWrath(ServerPlayerEntity p,ServerWorld w){
  Vec3d c=p.getPos().add(p.getRotationVec(1).multiply(4));hitArea(p,w,c,8,18,8,1.6);
  w.spawnParticles(ParticleTypes.EXPLOSION_EMITTER,c.x,c.y+1,c.z,1,0,0,0,0);w.spawnParticles(ParticleTypes.FLAME,c.x,c.y+1,c.z,180,4,1.5,4,.08);w.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME,c.x,c.y+1,c.z,90,3,1,3,.05);w.spawnParticles(ParticleTypes.LAVA,c.x,c.y+1,c.z,30,3,.8,3,.03);
 }
 private static void hitForward(ServerPlayerEntity p,ServerWorld w,Vec3d c,double range,float damage,int fire){
  Vec3d look=p.getRotationVec(1).normalize();Box b=p.getBoundingBox().expand(range,3,range);
  for(LivingEntity e:w.getEntitiesByClass(LivingEntity.class,b,x->x.isAlive()&&x!=p)){Vec3d to=e.getPos().subtract(p.getPos()).normalize();if(look.dotProduct(to)>.15&&e.squaredDistanceTo(c)<=range*range){e.damage(w.getDamageSources().playerAttack(p),damage);e.setOnFireFor(fire);}}
 }
 private static void hitArea(ServerPlayerEntity p,ServerWorld w,Vec3d c,double r,float damage,int fire,double kb){
  Box b=new Box(c.x-r,c.y-3,c.z-r,c.x+r,c.y+3,c.z+r);double r2=r*r;
  for(LivingEntity e:w.getEntitiesByClass(LivingEntity.class,b,x->x.isAlive()&&x!=p)){if(e.squaredDistanceTo(c.x,c.y,c.z)<=r2){e.damage(w.getDamageSources().playerAttack(p),damage);e.setOnFireFor(fire);e.takeKnockback(kb,-(e.getX()-c.x),-(e.getZ()-c.z));}}
 }
}
