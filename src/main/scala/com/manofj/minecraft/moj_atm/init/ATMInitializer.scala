package com.manofj.minecraft.moj_atm.init

import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.network.NetworkRegistry

import com.manofj.minecraft.moj_atm.ATM
import com.manofj.minecraft.moj_atm.ATMGuiHandler


sealed trait ATMInitializer {
  def preInit( event: FMLPreInitializationEvent ): Unit
  def init( event: FMLInitializationEvent ): Unit
}

class ATMCommonInitializer
  extends ATMInitializer
{
  override def preInit( event: FMLPreInitializationEvent ): Unit = {
    ATMBlocks.preInit()
    ATMItems.preInit()
    NetworkRegistry.INSTANCE.registerGuiHandler( ATM, ATMGuiHandler )
  }

  override def init( event: FMLInitializationEvent ): Unit = {
    ATMBlocks.init()
  }
}

class ATMClientInitializer
  extends ATMCommonInitializer
{
  override def preInit( event: FMLPreInitializationEvent ): Unit = {
    super.preInit( event )
    ATMBlocks.preInitClient()
    ATMItems.preInitClient()
  }
}
