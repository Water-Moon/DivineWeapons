package com.deeplake.dweapon.item.weapon;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.deeplake.dweapon.DWeapons;
import com.deeplake.dweapon.init.ModCreativeTab;
import com.deeplake.dweapon.init.ModItems;
import com.deeplake.dweapon.util.IHasModel;
import com.deeplake.dweapon.util.NBTStrDef.DWNBTDef;
import com.deeplake.dweapon.util.NBTStrDef.DWNBTUtil;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.text.translation.I18n;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

public class DWeaponSwordBase extends ItemSword implements IHasModel{

	//public DWNBT myNBT;
	
	public DWeaponSwordBase(String name, ToolMaterial material)
	{
		super(material);

		setUnlocalizedName(name);
		setRegistryName(name);
		setCreativeTab(ModCreativeTab.DW_MAIN);
		
		ModItems.ITEMS.add(this);
	}
	
	//GetAttr
	public float plainAtk(ItemStack stack)
	{
		return getAttackDamage() + 4.0F;
	}
	
	public static boolean IsSky(ItemStack stack)
	{
		return DWNBTUtil.GetBoolean(stack, DWNBTDef.IS_SKY);
	}
	
	public static void SetSky(ItemStack stack)
	{
		DWNBTUtil.SetBoolean(stack, DWNBTDef.IS_SKY, true);
	}
	
	public static boolean IsNameHidden(ItemStack stack)
	{
		return DWNBTUtil.GetBooleanDF(stack, DWNBTDef.IS_NAME_HIDDEN, true);
	}
	
	public static void SetNameHidden(ItemStack stack, boolean isHidden)
	{
		DWNBTUtil.SetBoolean(stack, DWNBTDef.IS_NAME_HIDDEN, isHidden);
	}
	
	public static boolean IsEarth(ItemStack stack)
	{
		return DWNBTUtil.GetBoolean(stack, DWNBTDef.IS_EARTH);
	}
	
	public static void SetEarth(ItemStack stack)
	{
		DWNBTUtil.SetBoolean(stack, DWNBTDef.IS_EARTH, true);
	}
	
	public static int GetPearlCount(ItemStack stack)
	{
		return DWNBTUtil.GetInt(stack, DWNBTDef.PEARL_COUNT);
	}
	
	public static void SetPearlCount(ItemStack stack, int count)
	{
		if (!(stack.getItem() instanceof DWeaponSwordBase)) {
			return;
		}
		
		if (count > 0 && count < GetPearlMax(stack)) {
			DWNBTUtil.SetInt(stack, DWNBTDef.PEARL_COUNT, count);
		}
	}
	
	public static int GetPearlMax(ItemStack stack)
	{
		if (!(stack.getItem() instanceof DWeaponSwordBase)) {
			return 0;
		}
		return 5;//Most Weapons can socket 5 pearls
	}
	
	public int GetPearlEmptySpace(ItemStack stack)
	{
		if (!(stack.getItem() instanceof DWeaponSwordBase)) {
			return 0;
		}
		if (IsSky(stack)) {
			return 0;//sky weapon needs no pearls to function.
		}
		
		return GetPearlMax(stack) - GetPearlCount(stack);
	}
	
	//---------------------------------------------------------
	
	
	//Delegates
	public boolean AttackDelegate(final ItemStack stack, final EntityPlayer player, final Entity target, float ratio)
	{
		return false;
	}
	
	@Override
	public boolean onLeftClickEntity(final ItemStack stack, final EntityPlayer player, final Entity target) {
		if (player.world.isRemote)
			return true;
		
		//if (player.getCooledAttackStrength(0.0f) > 0.8f) {
			return AttackDelegate(stack, player, target, player.getCooledAttackStrength(0.0f));
		//}

		//return true;//ignore attack
	}
	
	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase living, int count) {
		//Particle;
		super.onUsingTick(stack, living, count);
		DWeapons.LogWarning(String.format("base onUsingTick %s",count));
		
		if (living.world.isRemote)
		{
			clientUseTick(stack, living, count);
		}
		else
		{
			serverUseTick(stack, living, count);
		}
	}
	
	public void clientUseTick(ItemStack stack, EntityLivingBase living, int count)
	{
		
	}
	
	public void serverUseTick(ItemStack stack, EntityLivingBase living, int count)
	{
		
	}
	
	
	
	//----------------------------------------------------------------
	@Override
	public void registerModels() 
	{
		DWeapons.proxy.registerItemRenderer(this, 0, "inventory");
		
	}
	
	//-----------------Any Item
	@Nonnull
	@Override
	public Entity createEntity(World world, Entity location, ItemStack itemstack) {
	EntityItem entity = new EntityItem(world, location.posX, location.posY, location.posZ, itemstack);
		if(location instanceof EntityItem) {
		// workaround for private access on that field >_>
		  NBTTagCompound tag = new NBTTagCompound();
		  location.writeToNBT(tag);
		  entity.setPickupDelay(tag.getShort("PickupDelay"));
		}
		entity.motionX = location.motionX;
		entity.motionY = location.motionY;
		entity.motionZ = location.motionZ;
		return entity;
		//EnumRarity s = null;
	}
	
	@Nonnull
	@Override
	public EnumRarity getRarity(ItemStack stack) {
	    if (IsSky(stack)) 
	    {
	    	return EnumRarity.EPIC;
	    }
	    else if (IsEarth(stack))
	    {
	    	return EnumRarity.RARE;
	    }
	    else
	    {
	    	return EnumRarity.UNCOMMON;
	    }
	}
	
	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
	    return false;
	}
	  
	  /**
	     * Called when a player drops the item into the world,
	     * returning false from this will prevent the item from
	     * being removed from the players inventory and spawning
	     * in the world
	     *
	     * @param player The player that dropped the item
	     * @param item The item stack, before the item is removed.
	     */
	@Override
    public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player)
    {
        return false;
    }
  
    public int getItemEnchantability()
    {
        return 0;
    }
	
    //TestCode
    @Override
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving)
    {
        super.onBlockDestroyed(stack, worldIn, state, pos, entityLiving);
        //disable this after real play
        int pearlCount = GetPearlCount(stack);
		if (pearlCount == 5) 
		{
			SetPearlCount(stack, 0);
		}
		else
		{
			SetPearlCount(stack, pearlCount + 1);
		}
		
		return true;
    }
    
//    public String getUnlocalizedName(ItemStack stack)
//    {
//        //return "item." + this.unlocalizedName;
//    }
    
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
    }
    
    public String getItemStackDisplayName(ItemStack stack)
    {
    	String strMain ="";
    	if (IsNameHidden(stack))
    	{
    		strMain = I18n.format(getUnlocalizedName(stack) + DWNBTDef.TOOLTIP_HIDDEN);
    	}
    	else
    	{
    		strMain = I18n.format(getUnlocalizedName(stack) + ".name");
    	}
    		
    	String strLevel = "";
    	if (IsSky(stack))
    	{
    		strLevel = I18n.format("postfix.is_sky.name");
    	}else if (IsEarth(stack))
    	{
    		strLevel = I18n.format("postfix.is_earth.name");
    	}
    	String strPearl = "";
    	if (GetPearlCount(stack) > 0) {
    		strPearl = I18n.format("postfix.pearl_count.name", GetPearlCount(stack));
    	}
        return I18n.format("item.dweapon_name_format",strMain, strLevel, strPearl);
    }
	  //----------------------------------------------------
	  /* NBT loading */

	  
	  /**
     * Called when the player stops using an Item (stops holding the right mouse button).
     */
    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft)
    {
    	super.onPlayerStoppedUsing(stack, worldIn, entityLiving, timeLeft);
    }

    /**
     * allows items to add custom lines of information to the mouseover description
     */
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
    	if (IsNameHidden(stack)) 
    	{
    		String pearlDesc = I18n.format("postfix.hidden_desc.name", GetPearlCount(stack));
    		tooltip.add(pearlDesc);
    		return;
    	}
    	
    	//tooltip.clear();
    	if (GetPearlCount(stack) > 0)
    	{
    		String pearlDesc = I18n.format("item.shared.pearl_desc", GetPearlCount(stack));
    		tooltip.add(pearlDesc);
    	}
    	
    	if (IsSky(stack)) 
    	{
    		String skyDesc = I18n.format("item.shared.sky_desc");
    		tooltip.add(skyDesc);
    	}else if (IsEarth(stack))
    	{
    		String earthDesc = I18n.format("item.shared.earth_desc");
    		tooltip.add(earthDesc);
    	}
    }


	    /**
	     * Returns true if this item has an enchantment glint. By default, this returns
	     * <code>stack.isItemEnchanted()</code>, but other items can override it (for instance, written books always return
	     * true).
	     *  
	     * Note that if you override this method, you generally want to also call the super version (on {@link Item}) to get
	     * the glint for enchanted items. Of course, that is unnecessary if the overwritten version always returns true.
	     */
	    @SideOnly(Side.CLIENT)
	    public boolean hasEffect(ItemStack stack)
	    {
	        return IsSky(stack) || IsEarth(stack);
	    }
	    
	    @Override
		public boolean getIsRepairable(ItemStack stack, ItemStack repairMaterial) {
			return repairMaterial.getItem() != Items.ENCHANTED_BOOK && 
					OreDictionary.itemMatches(repairMaterial, new ItemStack(ModItems.DIVINE_INGOT), false) || super.getIsRepairable(stack, repairMaterial);
		}
}
