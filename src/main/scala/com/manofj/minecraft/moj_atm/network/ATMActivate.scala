package com.manofj.minecraft.moj_atm.network

import io.netty.buffer.ByteBuf

import net.minecraft.util.math.BlockPos

import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

import com.manofj.commons.minecraftforge.network.MessageHandler

import com.manofj.minecraft.moj_atm.ATM
import com.manofj.minecraft.moj_atm.ATMAccountManager
import com.manofj.minecraft.moj_atm.tileentity.TileEntityATM


object ATMActivate
  extends MessageHandler[ ATMActivate, IMessage ]
{
  override def onMessage( message: ATMActivate, ctx: MessageContext ): IMessage = {
    val player = ctx.getServerHandler.playerEntity
    player.worldObj.getTileEntity( message.pos ) match {
      case atm: TileEntityATM =>
        if ( message.activate ) {
//          atm.using( player )

          val balance = ATMAccountManager.getBalance( player )
          ATM.sendTo( new BalanceUpdate( balance ), player )
        }
        else {
//          atm.release()
        }
    }
    null
  }
}

class ATMActivate
  extends IMessage
{
  var activate: Boolean = false
  var pos: BlockPos = BlockPos.ORIGIN

  def this( activate: Boolean, pos: BlockPos ) = {
    this()
    this.activate = activate
    this.pos = pos
  }

  override def fromBytes( buf: ByteBuf ): Unit = {
    activate = buf.readBoolean()
    pos = BlockPos.fromLong( buf.readLong() )
  }

  override def toBytes( buf: ByteBuf ): Unit = {
    buf.writeBoolean( activate )
    buf.writeLong( pos.toLong )
  }
}