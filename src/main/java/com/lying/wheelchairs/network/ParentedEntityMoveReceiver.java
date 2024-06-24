package com.lying.wheelchairs.network;

import com.lying.wheelchairs.entity.IParentedEntity;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayChannelHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ParentedEntityMoveReceiver implements PlayChannelHandler
{
	public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender)
	{
		double x = buf.readDouble();
		double y = buf.readDouble();
		double z = buf.readDouble();
		float yaw = buf.readFloat();
		float pitch = buf.readFloat();
		server.execute(() -> IParentedEntity.getParentedEntitiesOf(player).forEach(child -> onParentMove(child, player, x, y, z, yaw, pitch)));
	}
	
	public static <T extends LivingEntity & IParentedEntity> void onParentMove(T child, LivingEntity parent, double x, double y, double z, float yaw, float pitch)
	{
		ServerWorld serverWorld = (ServerWorld)parent.getWorld();
		double currentX = child.getX();
		double currentY = child.getY();
		double currentZ = child.getZ();
		
		Vec3d parentOffset = child.getParentOffset(parent, yaw, pitch);
		x += parentOffset.x;
		y += parentOffset.y;
		z += parentOffset.z;
		
		double moveX = clampHorizontal(x);
		double moveY = clampVertical(y);
		double moveZ = clampHorizontal(z);
		
		float moveYaw = MathHelper.wrapDegrees(yaw);
		float movePitch = MathHelper.wrapDegrees(pitch);
		
		double offsetX = moveX - currentX;
		double offsetY = moveY - currentY;
		double offsetZ = moveZ - currentZ;
		boolean spaceEmpty = serverWorld.isSpaceEmpty(child, child.getBoundingBox().contract(0.0625));
		if(child.isClimbing())
			child.onLanding();
		Vec3d totalMove = new Vec3d(offsetX, offsetY, offsetZ);
		child.move(MovementType.PLAYER, totalMove);
		child.updatePositionAndAngles(moveX, moveY, moveZ, moveYaw, movePitch);
		boolean spaceEmpty2 = serverWorld.isSpaceEmpty(child, child.getBoundingBox().contract(0.0625));
		if(spaceEmpty && !spaceEmpty2)
		{
			child.updatePositionAndAngles(currentX, currentY, currentZ, moveYaw, movePitch);
			return;
		}
		else
			ParentedEntityPositionPacket.send(child.getUuid(), new Vec3d(moveX, moveY, moveZ), child);
	}
	
	private static double clampHorizontal(double d) { return MathHelper.clamp(d, -3.0E7, 3.0E7); }
	
	private static double clampVertical(double d) { return MathHelper.clamp(d, -2.0E7, 2.0E7); }
}
