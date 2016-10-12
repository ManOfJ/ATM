package com.manofj.minecraft.moj_atm.init

import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack

import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

import com.manofj.commons.scala.util.conversions.Any$

import com.manofj.minecraft.moj_atm.block.BlockATM
import com.manofj.minecraft.moj_atm.tileentity.TileEntityATM
import com.manofj.minecraft.moj_atm.{ATM => Mod}


object ATMBlocks {

  final val ATM = new BlockATM << { _.setUnlocalizedName( Mod.languageKey( "atm" ) )
                                     .setCreativeTab( CreativeTabs.DECORATIONS ) }
  final val ATM_ACTIVE = new BlockATM


  private[ this ] final val ATM_ITEM = new ItemBlock( ATM )


  private[ init ] def preInit(): Unit = {
    GameRegistry.register( ATM, Mod.resourceLocation( "atm" ) )
    GameRegistry.register( ATM_ACTIVE, Mod.resourceLocation( "atm_active" ) )
    GameRegistry.register( ATM_ITEM, Mod.resourceLocation( "atm" ) )

    GameRegistry.registerTileEntity( classOf[ TileEntityATM ], "moj_atm:ATM" )
  }

  private[ init ] def init(): Unit = {
    GameRegistry.addRecipe( new ItemStack( ATM ),
      "%%%",
      "$$$",
      "###",
      '%': Character, Blocks.GLASS,
      '$': Character, Items.QUARTZ,
      '#': Character, Items.IRON_INGOT
    )
  }


  @SideOnly( Side.CLIENT )
  private[ init ] def preInitClient(): Unit = {
    ModelLoader.setCustomModelResourceLocation( ATM_ITEM, 0, new ModelResourceLocation( ATM.getRegistryName, "inventory" ) )
  }

}
