package me.rigamortis.seppuku.impl.module.render;

import me.rigamortis.seppuku.api.event.render.EventRender3D;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL32;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.lwjgl.opengl.GL11.*;

/**
 * A feature that projects the possible path of an entity that was fired.
 * Throwables in this game use randomness to come up with a path that
 * is <b>never</b> the same using {@link java.util.Random}.
 * <p>
 * This results in <b>perfect</b> predictions when disregarding
 * the randomness in the trajectory.
 * <p>
 * todo; remove un-needed flightPoint collection
 *
 * @author Ddong
 * @since Feb 18, 2017
 */
public final class ProjectilesModule extends Module {

    private final Queue<Vec3d> flightPoint = new ConcurrentLinkedQueue<>();

    public final Value<Float> width = new Value<Float>("Width", new String[]{"W", "Width"}, "Pixel width of the projectile path.", 1.0f, 0.0f, 5.0f, 0.1f);
    public final Value<Float> red = new Value<Float>("Red", new String[]{"R"}, "Red value for the projectile path.", 255.0f, 0.0f, 255.0f, 1.0f);
    public final Value<Float> green = new Value<Float>("Green", new String[]{"G"}, "Green value for the projectile path.", 255.0f, 0.0f, 255.0f, 1.0f);
    public final Value<Float> blue = new Value<Float>("Blue", new String[]{"B"}, "Blue value for the projectile path.", 255.0f, 0.0f, 255.0f, 1.0f);
    public final Value<Float> alpha = new Value<Float>("Alpha", new String[]{"A"}, "Alpha value for the projectile path.", 255.0f, 0.0f, 255.0f, 1.0f);

    public ProjectilesModule() {
        super("Projectiles", new String[]{"Proj"}, "Projects the possible path of an entity that was fired.", "NONE", -1, ModuleType.RENDER);
    }

    @Listener
    public void onRender(EventRender3D event) {
        final Minecraft mc = Minecraft.getMinecraft();

        ThrowableType throwingType = this.getTypeFromCurrentItem(mc.player);

        if (throwingType == ThrowableType.NONE) {
            return;
        }

        FlightPath flightPath = new FlightPath(mc.player, throwingType);

        while (!flightPath.isCollided()) {
            flightPath.onUpdate();

            flightPoint.offer(new Vec3d(flightPath.position.x - mc.getRenderManager().viewerPosX,
                    flightPath.position.y - mc.getRenderManager().viewerPosY,
                    flightPath.position.z - mc.getRenderManager().viewerPosZ));
        }

        final boolean bobbing = mc.gameSettings.viewBobbing;
        mc.gameSettings.viewBobbing = false;
        mc.entityRenderer.setupCameraTransform(event.getPartialTicks(), 0);
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(GL_SMOOTH);
        glLineWidth(width.getValue());
        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        GlStateManager.disableDepth();
        glEnable(GL32.GL_DEPTH_CLAMP);
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();

        while (!flightPoint.isEmpty()) {
            bufferbuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            Vec3d head = flightPoint.poll();
            bufferbuilder.pos(head.x, head.y, head.z).color(red.getValue() / 255.0f, green.getValue() / 255.0f, blue.getValue() / 255.0f, alpha.getValue() / 255.0f).endVertex();

            if (flightPoint.peek() != null) {
                Vec3d point = flightPoint.peek();
                bufferbuilder.pos(point.x, point.y, point.z).color(red.getValue() / 255.0f, green.getValue() / 255.0f, blue.getValue() / 255.0f, alpha.getValue() / 255.0f).endVertex();
            }

            tessellator.draw();
        }

        GlStateManager.shadeModel(GL_FLAT);
        glDisable(GL_LINE_SMOOTH);
        GlStateManager.enableDepth();
        glDisable(GL32.GL_DEPTH_CLAMP);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();

        mc.gameSettings.viewBobbing = bobbing;
        mc.entityRenderer.setupCameraTransform(event.getPartialTicks(), 0);

        if (flightPath.collided) {
            final RayTraceResult hit = flightPath.target;
            AxisAlignedBB bb = null;

            if (hit.typeOfHit == RayTraceResult.Type.BLOCK) {
                final BlockPos blockpos = hit.getBlockPos();
                final IBlockState iblockstate = mc.world.getBlockState(blockpos);

                if (iblockstate.getMaterial() != Material.AIR && mc.world.getWorldBorder().contains(blockpos)) {
                    final Vec3d interp = MathUtil.interpolateEntity(mc.player, mc.getRenderPartialTicks());
                    bb = iblockstate.getSelectedBoundingBox(mc.world, blockpos).grow(0.0020000000949949026D).offset(-interp.x, -interp.y, -interp.z);
                }
            } else if (hit.typeOfHit == RayTraceResult.Type.ENTITY && hit.entityHit != null) {
                final AxisAlignedBB entityBB = hit.entityHit.getEntityBoundingBox();
                if (entityBB != null) {
                    bb = new AxisAlignedBB(entityBB.minX - mc.getRenderManager().viewerPosX, entityBB.minY - mc.getRenderManager().viewerPosY, entityBB.minZ - mc.getRenderManager().viewerPosZ, entityBB.maxX - mc.getRenderManager().viewerPosX, entityBB.maxY - mc.getRenderManager().viewerPosY, entityBB.maxZ - mc.getRenderManager().viewerPosZ);
                }
            }

            if (bb != null) {
                RenderUtil.drawBoundingBox(bb, width.getValue(), red.getValue() / 255.0f, green.getValue() / 255.0f, blue.getValue() / 255.0f, alpha.getValue() / 255.0f);
            }
        }
    }

    private ThrowableType getTypeFromCurrentItem(EntityPlayerSP player) {
        // Check if we're holding an item first
        if (player.getHeldItemMainhand() == null) {
            return ThrowableType.NONE;
        }

        final ItemStack itemStack = player.getHeldItem(EnumHand.MAIN_HAND);
        // Check what type of item this is
        switch (Item.getIdFromItem(itemStack.getItem())) {
            case 261: // ItemBow
                if (player.isHandActive())
                    return ThrowableType.ARROW;
                break;
            case 346: // ItemFishingRod
                return ThrowableType.FISHING_ROD;
            case 438: //splash potion
            case 441: //splash potion linger
                return ThrowableType.POTION;
            case 384: // ItemExpBottle
                return ThrowableType.EXPERIENCE;
            case 332: // ItemSnowball
            case 344: // ItemEgg
            case 368: // ItemEnderPearl
                return ThrowableType.NORMAL;
            default:
                break;
        }

        return ThrowableType.NONE;
    }

    enum ThrowableType {
        /**
         * Represents a non-throwable object.
         */
        NONE(0.0f, 0.0f),

        /**
         * Arrows fired from a bow.
         */
        ARROW(1.5f, 0.05f),

        /**
         * Splash potion entities
         */
        POTION(0.5f, 0.05f),

        /**
         * Experience bottles.
         */
        EXPERIENCE(0.7F, 0.07f),

        /**
         * The fishhook entity with a fishing rod.
         */
        FISHING_ROD(1.5f, 0.04f),

        /**
         * Any throwable entity that doesn't have unique
         * world velocity/gravity constants.
         */
        NORMAL(1.5f, 0.03f);

        private final float velocity;
        private final float gravity;

        ThrowableType(float velocity, float gravity) {
            this.velocity = velocity;
            this.gravity = gravity;
        }

        /**
         * The initial velocity of the entity.
         *
         * @return entity velocity
         */

        public float getVelocity() {
            return velocity;
        }

        /**
         * The constant gravity applied to the entity.
         *
         * @return constant world gravity
         */
        public float getGravity() {
            return gravity;
        }
    }

    /**
     * A class used to mimic the flight of an entity.  Actual
     * implementation resides in multiple classes but the parent of all
     * of them is {@link net.minecraft.entity.projectile.EntityThrowable}
     */
    final class FlightPath {
        private EntityPlayerSP shooter;
        private Vec3d position;
        private Vec3d motion;
        private float yaw;
        private float pitch;
        private AxisAlignedBB boundingBox;
        private boolean collided;
        private RayTraceResult target;
        private ThrowableType throwableType;

        FlightPath(EntityPlayerSP player, ThrowableType throwableType) {
            this.shooter = player;
            this.throwableType = throwableType;

            // Set the starting angles of the entity
            this.setLocationAndAngles(this.shooter.posX, this.shooter.posY + this.shooter.getEyeHeight(), this.shooter.posZ,
                    this.shooter.rotationYaw, this.shooter.rotationPitch);

            Vec3d startingOffset = new Vec3d(MathHelper.cos(this.yaw / 180.0F * (float) Math.PI) * 0.16F, 0.1d,
                    MathHelper.sin(this.yaw / 180.0F * (float) Math.PI) * 0.16F);

            this.position = this.position.subtract(startingOffset);
            // Update the entity's bounding box
            this.setPosition(this.position);

            // Set the entity's motion based on the shooter's rotations
            this.motion = new Vec3d(-MathHelper.sin(this.yaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.pitch / 180.0F * (float) Math.PI),
                    -MathHelper.sin(this.pitch / 180.0F * (float) Math.PI),
                    MathHelper.cos(this.yaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.pitch / 180.0F * (float) Math.PI));

            this.setThrowableHeading(this.motion, this.getInitialVelocity());
        }

        /**
         * Update the entity's data in the world.
         */
        public void onUpdate() {
            // Get the predicted positions in the world
            Vec3d prediction = this.position.add(this.motion);
            // Check if we've collided with a block in the same time
            RayTraceResult blockCollision = this.shooter.getEntityWorld().rayTraceBlocks(this.position, prediction,
                    this.throwableType == ThrowableType.FISHING_ROD, !this.collidesWithNoBoundingBox(), false);
            // Check if we got a block collision
            if (blockCollision != null) {
                prediction = blockCollision.hitVec;
            }

            // Check entity collision
            this.onCollideWithEntity(prediction, blockCollision);

            // Check if we had a collision
            if (this.target != null) {
                this.collided = true;
                // Update position
                this.setPosition(this.target.hitVec);
                return;
            }

            // Sanity check to see if we've gone below the world (if we have we will never collide)
            if (this.position.y <= 0.0d) {
                // Force this to true even though we haven't collided with anything
                this.collided = true;
                return;
            }

            // Update the entity's position based on velocity
            this.position = this.position.add(this.motion);
            float motionModifier = 0.99F;
            // Check if our path will collide with water
            if (this.shooter.getEntityWorld().isMaterialInBB(this.boundingBox, Material.WATER)) {
                // Arrows move slower in water than normal throwables
                motionModifier = this.throwableType == ThrowableType.ARROW ? 0.6F : 0.8F;
            }

            // Apply the fishing rod specific motion modifier
            if (this.throwableType == ThrowableType.FISHING_ROD) {
                motionModifier = 0.92f;
            }

            // Slowly decay the velocity of the path
            this.motion = MathUtil.mult(this.motion, motionModifier);
            // Drop the motionY by the constant gravity
            this.motion = this.motion.subtract(0.0d, this.getGravityVelocity(), 0.0d);
            // Update the position and bounding box
            this.setPosition(this.position);
        }

        /**
         * Checks if a specific item type will collide
         * with a block that has no collision bounding box.
         *
         * @return true if type collides
         */
        private boolean collidesWithNoBoundingBox() {
            switch (this.throwableType) {
                case FISHING_ROD:
                case NORMAL:
                    return true;
                default:
                    return false;
            }
        }

        /**
         * Check if our path collides with an entity.
         *
         * @param prediction     the predicted position
         * @param blockCollision block collision if we had one
         */
        private void onCollideWithEntity(Vec3d prediction, RayTraceResult blockCollision) {
            Entity collidingEntity = null;
            RayTraceResult collidingPosition = null;

            double currentDistance = 0.0d;
            // Get all possible collision entities disregarding the local player
            List<Entity> collisionEntities = Minecraft.getMinecraft().world.getEntitiesWithinAABBExcludingEntity(this.shooter, this.boundingBox.expand(this.motion.x, this.motion.y, this.motion.z).grow(1.0D, 1.0D, 1.0D));

            // Loop through every loaded entity in the world
            for (Entity entity : collisionEntities) {
                // Check if we can collide with the entity or it's ourself
                if (!entity.canBeCollidedWith()) {
                    continue;
                }

                // Check if we collide with our bounding box
                float collisionSize = entity.getCollisionBorderSize();
                AxisAlignedBB expandedBox = entity.getEntityBoundingBox().expand(collisionSize, collisionSize, collisionSize);
                RayTraceResult objectPosition = expandedBox.calculateIntercept(this.position, prediction);

                // Check if we have a collision
                if (objectPosition != null) {
                    double distanceTo = this.position.distanceTo(objectPosition.hitVec);

                    // Check if we've gotten a closer entity
                    if (distanceTo < currentDistance || currentDistance == 0.0D) {
                        collidingEntity = entity;
                        collidingPosition = objectPosition;
                        currentDistance = distanceTo;
                    }
                }
            }

            // Check if we had an entity
            if (collidingEntity != null) {
                // Set our target to the result
                this.target = new RayTraceResult(collidingEntity, collidingPosition.hitVec);
            } else {
                // Fallback to the block collision
                this.target = blockCollision;
            }
        }

        /**
         * Return the initial velocity of the entity at it's exact starting
         * moment in flight.
         *
         * @return entity velocity in flight
         */
        private float getInitialVelocity() {
            switch (this.throwableType) {
                // Arrows use the current use duration as a velocity multplier
                case ARROW:
                    // Check how long we've been using the bow
                    int useDuration = this.shooter.getHeldItem(EnumHand.MAIN_HAND).getItem().getMaxItemUseDuration(this.shooter.getHeldItem(EnumHand.MAIN_HAND)) - this.shooter.getItemInUseCount();
                    float velocity = (float) useDuration / 20.0F;
                    velocity = (velocity * velocity + velocity * 2.0F) / 3.0F;
                    if (velocity > 1.0F) {
                        velocity = 1.0F;
                    }

                    // When the arrow is spawned inside of ItemBow, they multiply it by 2
                    return (velocity * 2.0f) * throwableType.getVelocity();
                default:
                    return throwableType.getVelocity();
            }
        }

        /**
         * Get the constant gravity of the item in use.
         *
         * @return gravity relating to item
         */
        private float getGravityVelocity() {
            return throwableType.getGravity();
        }

        /**
         * Set the position and rotation of the entity in the world.
         *
         * @param x     x position in world
         * @param y     y position in world
         * @param z     z position in world
         * @param yaw   yaw rotation axis
         * @param pitch pitch rotation axis
         */
        private void setLocationAndAngles(double x, double y, double z, float yaw, float pitch) {
            this.position = new Vec3d(x, y, z);
            this.yaw = yaw;
            this.pitch = pitch;
        }

        /**
         * Sets the x,y,z of the entity from the given parameters. Also seems to set
         * up a bounding box.
         *
         * @param position position in world
         */
        private void setPosition(Vec3d position) {
            this.position = new Vec3d(position.x, position.y, position.z);
            // Usually this is this.width / 2.0f but throwables change
            double entitySize = (this.throwableType == ThrowableType.ARROW ? 0.5d : 0.25d) / 2.0d;
            // Update the path's current bounding box
            this.boundingBox = new AxisAlignedBB(position.x - entitySize,
                    position.y - entitySize,
                    position.z - entitySize,
                    position.x + entitySize,
                    position.y + entitySize,
                    position.z + entitySize);
        }

        /**
         * Set the entity's velocity and position in the world.
         *
         * @param motion   velocity in world
         * @param velocity starting velocity
         */
        private void setThrowableHeading(Vec3d motion, float velocity) {
            // Divide the current motion by the length of the vector
            this.motion = MathUtil.div(motion, (float) motion.length());
            // Multiply by the velocity
            this.motion = MathUtil.mult(this.motion, velocity);
        }

        /**
         * Check if the path has collided with an object.
         *
         * @return path collides with ground
         */
        public boolean isCollided() {
            return collided;
        }

        /**
         * Get the target we've collided with if it exists.
         *
         * @return moving object target
         */
        public RayTraceResult getCollidingTarget() {
            return target;
        }
    }

}
