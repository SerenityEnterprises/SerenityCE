package host.serenity.serenity.util.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.item.*;

public enum ProjectileType {
    ARROW(0.05),
    EGG(0.03),
    ENDER_PEARL(0.03),
    POTION(0.05),
    SNOWBALL(0.03);

    private double gravity;

    ProjectileType(double gravity) {
        this.gravity = gravity;
    }

    public double getGravity() {
        return gravity;
    }

    public static ProjectileType getProjectileTypeFromShootableItem(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBow)
            return ARROW;

        if (item instanceof ItemEgg)
            return EGG;

        if (item instanceof ItemEnderPearl)
            return ENDER_PEARL;

        if (item instanceof ItemPotion && ItemPotion.isSplash(stack.getItemDamage()))
            return POTION;

        if (item instanceof ItemSnowball)
            return SNOWBALL;

        return null;
    }

    public static ProjectileType getProjectTypeFromProjectileEntity(Entity projectile) {
        if (projectile instanceof EntityArrow)
            return ARROW;

        if (projectile instanceof EntityEgg)
            return EGG;

        if (projectile instanceof EntityEnderPearl)
            return ENDER_PEARL;

        if (projectile instanceof EntityPotion)
            return POTION;

        if (projectile instanceof EntitySnowball)
            return SNOWBALL;

        return null;
    }
}
