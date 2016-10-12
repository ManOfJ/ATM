package com.manofj.minecraft.moj_atm.tileentity

import java.util.UUID

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTUtil
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ITickable

import net.minecraftforge.common.util.Constants.NBT

import com.manofj.commons.scala.util.conversions.Any$

import com.manofj.minecraft.moj_atm.block.BlockATM
import com.manofj.minecraft.moj_atm.init.ATMBlocks


class TileEntityATM
  extends TileEntity
  with    ITickable
{

  private[ this ] var userOpt = Option.empty[ UUID ]
  private[ this ] var userCheckDelay = 0


  def isInUse: Boolean = userOpt.nonEmpty

  def using( user: EntityPlayer ): Unit = {
    userOpt = Option( user ).map( _.getUniqueID )
    markDirty()
  }

  def release(): Unit = {
    userOpt = None
    markDirty()
  }

  def isUser( player: EntityPlayer ): Boolean = Option( player ).map( _.getUniqueID ).exists( userOpt.contains )

  def isUseableByPlayer( player: EntityPlayer ): Boolean = {
    ( worldObj.getTileEntity( pos ) eq this ) && player.getDistanceSqToCenter( pos ) <= 32
  }


  override def update(): Unit =
    if ( !worldObj.isRemote ) {
      val blockType = getBlockType

      if ( !isInUse ) {
        userCheckDelay += 1

        if ( userCheckDelay >= 15 ) {
          userCheckDelay = 0

          val withinPlayer = worldObj.isAnyPlayerWithinRangeAt( pos.getX + 0.5, pos.getY + 0.5, pos.getZ + 0.5, 2.5 )
          ( withinPlayer, blockType ) match {
            case ( true,  ATMBlocks.ATM )        => BlockATM.setActive( active = true, worldObj, pos )
            case ( false, ATMBlocks.ATM_ACTIVE ) => BlockATM.setActive( active = false, worldObj, pos )
            case _ =>
          }
        }

      }
      else if ( blockType != ATMBlocks.ATM_ACTIVE ) {
        BlockATM.setActive( active = true, worldObj, pos )
      }

    }

  override def getUpdateTag: NBTTagCompound = writeToNBT( new NBTTagCompound )

  override def writeToNBT( compound: NBTTagCompound ): NBTTagCompound =
    super.writeToNBT( compound ) << { tag =>
      userOpt.foreach( uuid => tag.setTag( "UserId", NBTUtil.createUUIDTag( uuid ) ) )
    }

  override def readFromNBT( compound: NBTTagCompound ): Unit = {
    super.readFromNBT( compound )

    userOpt = None
    if ( compound.hasKey( "UserId", NBT.TAG_COMPOUND ) ) {
      val uuid = NBTUtil.getUUIDFromTag( compound.getCompoundTag( "UserId" ) )
      userOpt = Option( uuid )
    }
  }

}
