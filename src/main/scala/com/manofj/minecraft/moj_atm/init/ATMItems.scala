package com.manofj.minecraft.moj_atm.init

import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.item.Item

import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

import com.manofj.minecraft.moj_atm.ATM


object ATMItems {

  final val ATM_GUI = new Item


  private[ init ] def preInit(): Unit = {
    GameRegistry.register( ATM_GUI, ATM.resourceLocation( "atm_gui" ) )
  }

  @SideOnly( Side.CLIENT )
  private[ init ] def preInitClient(): Unit = {
    ModelLoader.setCustomModelResourceLocation( ATM_GUI, 0, new ModelResourceLocation( ATM_GUI.getRegistryName, "inventory" ) )
  }

}
