package com.manofj.minecraft.moj_atm.network

import io.netty.buffer.ByteBuf

import net.minecraftforge.fml.client.FMLClientHandler
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

import com.manofj.commons.minecraftforge.network.MessageHandler

import com.manofj.minecraft.moj_atm.gui.GuiScreenATM


object BalanceUpdate
  extends MessageHandler[ BalanceUpdate, IMessage ]
{

  override def onMessage( message: BalanceUpdate, ctx: MessageContext ): IMessage = {
    FMLClientHandler.instance.getClient.currentScreen match {
      case atm: GuiScreenATM => atm.receiveBalance( message.balance )
      case _ =>
    }
    null
  }

}

class BalanceUpdate
  extends IMessage
{
  var balance: Long = 0

  def this( balance: Long ) = {
    this()
    this.balance = balance
  }

  override def fromBytes( buf: ByteBuf ): Unit = balance = buf.readLong()
  override def toBytes( buf: ByteBuf ): Unit = buf.writeLong( balance )
}