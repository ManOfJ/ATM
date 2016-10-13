package com.manofj.minecraft.moj_atm

import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.relauncher.Side

import com.manofj.commons.minecraftforge.base.MinecraftForgeMod
import com.manofj.commons.minecraftforge.i18n.I18nSupportMod
import com.manofj.commons.minecraftforge.logging.LoggerLikeMod
import com.manofj.commons.minecraftforge.network.SimpleNetworkMod
import com.manofj.commons.minecraftforge.resource.ResourceLocationMakerMod

import com.manofj.minecraft.moj_atm.init.ATMInitializer
import com.manofj.minecraft.moj_atm.network.ATMActivate
import com.manofj.minecraft.moj_atm.network.BalanceUpdate
import com.manofj.minecraft.moj_atm.network.Deposit
import com.manofj.minecraft.moj_atm.network.Withdrawal


@Mod( modid       = ATM.modId,
      name        = ATM.modName,
      version     = ATM.modVersion,
      modLanguage = ATM.modLanguage )
object ATM
  extends MinecraftForgeMod
  with    I18nSupportMod
  with    LoggerLikeMod
  with    ResourceLocationMakerMod
  with    SimpleNetworkMod
{

  override final val modId      = "moj_atm"
  override final val modName    = "ATM"
  override final val modVersion = "@version@"


  @SidedProxy( modId      = ATM.modId,
               serverSide = "com.manofj.minecraft.moj_atm.init.ATMCommonInitializer",
               clientSide = "com.manofj.minecraft.moj_atm.init.ATMClientInitializer" )
  var initializer: ATMInitializer = null


  @Mod.EventHandler
  def preInit( event: FMLPreInitializationEvent ): Unit = {
    initializer.preInit( event )

    registerMessage( ATMActivate, Side.SERVER )
    registerMessage( BalanceUpdate, Side.CLIENT )
    registerMessage( Deposit, Side.values: _* )
    registerMessage( Withdrawal, Side.values: _* )
  }

  @Mod.EventHandler
  def init( event: FMLInitializationEvent ): Unit = initializer.init( event )

}
