package com.manofj.minecraft.moj_atm

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

import net.minecraftforge.fml.common.network.IGuiHandler

import com.manofj.minecraft.moj_atm.gui.GuiScreenATM
import com.manofj.minecraft.moj_atm.tileentity.TileEntityATM


object ATMGuiHandler
  extends IGuiHandler
{

  override def getClientGuiElement( ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int ): AnyRef = {
    ID match {
      case 0 =>
        Option( world.getTileEntity( new BlockPos( x, y, z ) ) ) match {
          case Some( atm: TileEntityATM ) => new GuiScreenATM( player, atm )
          case _ => ATM.warn( s"TileEntityATM is not set in the pos[x=$x,y=$y,z=$z]" ); null
        }
    }
  }

  override def getServerGuiElement( ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int ): AnyRef =
    ID match {
      case 0 => null
    }

}
