package com.deeplake.dweapon.util.NBTStrDef;

import com.deeplake.dweapon.DWeapons;
import com.deeplake.dweapon.init.ModPotions;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class IDLGeneral {
    //server side dont have this constructor.
    public static AxisAlignedBB ServerAABB(Vec3d from, Vec3d to)
    {
        return new AxisAlignedBB(from.x, from.y, from.z, to.x, to.y, to.z);
    }

    public static boolean EntityHasBuff(EntityLivingBase livingBase, Potion buff)
    {
        return livingBase.getActivePotionEffect(buff) != null;
    }

    public static int EntityBuffCounter(EntityLivingBase livingBase, Potion buff)
    {
        PotionEffect effect = livingBase.getActivePotionEffect(buff);
        return effect == null ? -1 : effect.getDuration();
    }
}