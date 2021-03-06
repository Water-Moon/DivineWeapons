package com.deeplake.dweapon.item.weapon;

import com.deeplake.dweapon.init.ModItems;
import com.deeplake.dweapon.util.DWEntityUtil;
import com.deeplake.dweapon.util.NBTStrDef.DWNBTDef;
import com.deeplake.dweapon.util.NBTStrDef.DWNBTUtil;
import com.deeplake.dweapon.util.NBTStrDef.IDLGeneral;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import static com.deeplake.dweapon.util.NBTStrDef.IDLGeneral.isWaterRelated;

//Test Weapon.
public class DWaterSword extends DWeaponSwordBase {
	// /give @p dweapon:water_sword 1 0 {is_earth:false, is_sky:false, pearl_count:0}
	public DWaterSword(String name, ToolMaterial material) {
		super(name, material);
		
	}
	static final float base_damage = 4;
	public static final float baseDamageAttackMode = 5;//2 hearts
	public static final float pearlDamage = 1;//0.5 hearts
	public static final float earthDamageModifier = 1.5f;

	private static final float wet_multiplier = 2.0f;//damage rate when wet
	private static final float sky_base_damage = 75.0f;
	private static final int skyBuffTick = 10;

	private static int weatherSummonTick = 100;
	public static final int NORMAL_MODE = 0;
	public static final int CAN_RAIN_MODE = 1;
	public static final int RAINING_MODE = 2;

	public static final String BUCKET = "has_bucket";

	@Override
	public boolean getIsRepairable(ItemStack stack, ItemStack repairMaterial) {
		boolean isWaterRelated = repairMaterial.getItem() == Items.WATER_BUCKET ||
				repairMaterial.getUnlocalizedName().equals("idealland:gua_2");

		return isWaterRelated || super.getIsRepairable(stack, repairMaterial);
	}

//	@Override
//	public float getAttackDamage()
//	{
//		return base_damage;
//	}

	public float getActualDamage(ItemStack stack, float ratio, boolean isWet)
	{
		float damage = baseDamageAttackMode * ratio + pearlDamage * GetPearlCount(stack);
		if (IsSky(stack))
		{
			damage = sky_base_damage;
		}
		else if (IsEarth(stack))
		{
			damage *= earthDamageModifier;
		}

		if (isWet) {
			damage *= wet_multiplier;
		}
		return damage;
	}

	public boolean NoGravity(ItemStack stack, boolean isSneaking, boolean isSelected)
	{
		return (IsSky(stack) || (IsEarth(stack) && isSelected)) && isSneaking;
	}

	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
	{
		super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);

		if (entityIn instanceof EntityPlayerMP)
		{
			EntityPlayerMP playerMP = (EntityPlayerMP)(entityIn);
			//gives buff
			if ((IsSky(stack) || IsEarth(stack)) && isSelected)
			{
				if (playerMP.isWet()) {
					playerMP.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, skyBuffTick, 0));
					if (IsSky(stack)) {
						playerMP.addPotionEffect(new PotionEffect(MobEffects.SPEED, skyBuffTick, 2));
					} else if (IsEarth(stack)) {
						playerMP.addPotionEffect(new PotionEffect(MobEffects.SPEED, skyBuffTick, 0));
					}
				}

				DWEntityUtil.TryRemoveDebuff(playerMP);
			}
			//playerMP.setNoGravity(playerMP.isWet() && NoGravity(stack, playerMP.isSneaking(), isSelected));

			//put out fire
			if (playerMP.isBurning() && isSelected)
			{
				playerMP.extinguish();
				stack.damageItem(1, playerMP);
			}

			if (CanRainHere(playerMP)) {
				boolean isRainingHere = IsRainingHere(playerMP);
				if (isRainingHere) {
					if (stack.isItemDamaged())
					{//auto fix
						int curDamage = stack.getItemDamage();
						int fixAmount = 1 + playerMP.getRNG().nextInt(1 + GetPearlCount(stack));

						stack.setItemDamage(Math.max(curDamage - fixAmount, 0));
					}
					SetWeaponMode(stack, RAINING_MODE);
				}
				else
				{
					if (IsSky(stack)) {
						SetWeaponMode(stack, CAN_RAIN_MODE);
					}
					else {
						SetWeaponMode(stack, NORMAL_MODE);
					}
				}
			}
			else
			{
				SetWeaponMode(stack, NORMAL_MODE);
			}
		}

		if (worldIn.isRemote && isSelected)
		{

		}
	}

	@Override
	public boolean AttackDelegate(final ItemStack stack, final EntityPlayer player, final Entity target, float ratio) {

		float damage = getActualDamage(stack, ratio, player.isWet());

		boolean success = false;
		success = target.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) player).setMagicDamage(), damage);

		if (success)
		{
			target.extinguish();
			stack.damageItem(1, player);
		}

		return success;
	}

	/**
	 * Called when a Block is right-clicked with this Item
	 */
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		boolean successFlag = false;
		ItemStack stack = player.getHeldItem(hand);
		int curDamage = stack.getItemDamage();
		if (curDamage == 0)
		{
			return EnumActionResult.PASS;
		}

		IBlockState targetBlock = worldIn.getBlockState(pos);
		RayTraceResult raytraceresult = this.rayTrace(worldIn, player, true);
		BlockPos blockpos = raytraceresult.getBlockPos();
		IBlockState iblockstate = worldIn.getBlockState(blockpos);
		Material material = iblockstate.getMaterial();

		if (material == Material.WATER && iblockstate.getValue(BlockLiquid.LEVEL) == 0) {
			successFlag = true;
		}

		if (!successFlag && isWaterRelated(targetBlock))
		{
			successFlag = true;
		}

		int repairAmount = 10;
		if (successFlag)
		{
			stack.setItemDamage(curDamage - repairAmount);
			player.getCooldownTracker().setCooldown(stack.getItem(), 4);
			player.playSound(SoundEvents.ENTITY_GENERIC_DRINK, 1f, 1f);
			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.PASS;
	}

	@Override
	public void clientUseTick(ItemStack stack, EntityLivingBase living, int count) {
		if (!IsSky(stack))//only sky can summon snow storm
		{
			return;
		}

		if (getMaxItemUseDuration(stack) - count >=  weatherSummonTick)
		{
			for (int i = 0; i < 10; i++)
			{
				CreateParticle(stack, living, -3d);
			}
		}
	}

	private void CreateParticle(ItemStack stack, EntityLivingBase living, double vm) {
		Random rand = new Random();
		double r = 1d;
		double x = living.posX + (rand.nextDouble() - 0.5d) * r;
		double y = living.posY + rand.nextDouble() * living.height;
		double z = living.posZ + (rand.nextDouble() - 0.5d) * r;

		double vx = 0d;
		double vy = -rand.nextDouble() * vm;
		double vz = 0d;

		living.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE,
				x,y,z,vx,vy,vz);
	}

	private void CreateParticleExplosion(Entity living, double vRandom, Vec3d baseDir, float baseSpeed) {
		Vec3d flyDir = baseDir.addVector(0,1f,0).normalize();

		Random rand = new Random();
		double r = 1d;
		double x = living.posX + (rand.nextDouble() - 0.5d) * r;
		double y = living.posY + rand.nextDouble() * living.height;
		double z = living.posZ + (rand.nextDouble() - 0.5d) * r;

		double vx = (rand.nextDouble() - 0.5d) * vRandom + flyDir.x * baseSpeed;
		double vy = (rand.nextDouble() - 0.5d) * vRandom + flyDir.y * baseSpeed;
		double vz = (rand.nextDouble() - 0.5d) * vRandom + flyDir.z * baseSpeed;

		//DWeapons.LogWarning(String.format("pos = (%.2f, %.2f, %.2f), v = (%.2f, %.2f, %.2f),",x,y,z,vx,vy,vz));
		living.world.spawnParticle(EnumParticleTypes.WATER_SPLASH,
				x,y,z,vx,vy,vz);
	}


	/**
	 * How long it takes to use or consume an item
	 */
	@Override
	public int getMaxItemUseDuration(ItemStack stack)
	{
		return 72000;
	}

	//Animation
	@Nonnull
	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		int mode = GetWeaponMode(stack);
		if (IsSky(stack) && mode != NORMAL_MODE)
		{
			return EnumAction.BOW;
		}
		else {
			return EnumAction.NONE;
		}
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
		player.setActiveHand(hand);
		ItemStack stack = player.getHeldItem(hand);

		return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
	}

	private static final int rainTime = 1200;

	/**
	 * Called when the player stops using an Item (stops holding the right mouse button).
	 */
	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase living, int time) {
		//DWeapons.LogWarning("onPlayerStoppedUsing");
		int mode = GetWeaponMode(stack);

		if (IsSky(stack) &&
				(getMaxItemUseDuration(stack) - time >= weatherSummonTick)) {

			if (!world.isRemote) {
				if (mode != NORMAL_MODE)
				{
					EntityPlayerMP playerMP = (EntityPlayerMP)(living);
					WorldInfo worldInfo = playerMP.mcServer.worlds[0].getWorldInfo();

					worldInfo.setRaining(true);

					worldInfo.setRainTime(rainTime);
				}
			}
			else
			{
				if (mode == NORMAL_MODE) {//cannot rain here
					living.playSound(SoundEvents.ENTITY_BLAZE_BURN, 0.6f, 1);
				} else
				{
					living.playSound(SoundEvents.ENTITY_LIGHTNING_THUNDER, 1.5f, 1);
				}
			}
		}

		return;
	}

	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
	{
		super.addInformation(stack, worldIn, tooltip, flagIn);
		if (IsNameHidden(stack))
		{
			return;
		}

		String shared = I18n.format(getUnlocalizedName()+DWNBTDef.TOOLTIP_SHARED);
		tooltip.add(shared);

		if (IsSky(stack))
		{
			String skyDesc = I18n.format(getUnlocalizedName()+DWNBTDef.TOOLTIP_SKY);
			tooltip.add(skyDesc);
		}else if (IsEarth(stack))
		{
			String earthDesc = I18n.format(getUnlocalizedName()+DWNBTDef.TOOLTIP_EARTH);
			tooltip.add(earthDesc);
		}else
		{
			String earthDesc = I18n.format(getUnlocalizedName()+DWNBTDef.TOOLTIP_NORMAL);
			tooltip.add(earthDesc);
		}
		addDamageInformation(stack, worldIn, tooltip, flagIn);
	}

	//@SideOnly(Side.CLIENT)
    public float GetReferenceDamage(ItemStack stack, @Nullable World worldIn)
	{
		return getActualDamage(stack, 1, false);
	}
}
