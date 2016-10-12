package com.manofj.minecraft.moj_atm.network

import java.util.UUID

import com.google.common.primitives.UnsignedLong
import io.netty.buffer.ByteBuf

import net.minecraftforge.fml.client.FMLClientHandler
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import net.minecraftforge.fml.relauncher.Side

import com.manofj.commons.minecraftforge.network.MessageHandler

import com.manofj.minecraft.moj_atm.ATMAccountManager
import com.manofj.minecraft.moj_atm.util.XpUtils


object Deposit
  extends MessageHandler[ Deposit, Deposit ]
{

  override def onMessage( message: Deposit, ctx: MessageContext ): Deposit =
    ctx.side match {
      case Side.SERVER =>
        val server  = FMLCommonHandler.instance.getMinecraftServerInstance
        val players = server.getPlayerList
        val balance = ATMAccountManager.getBalance( message.accountId )
        val user    = players.getPlayerByUUID( message.playerId )
        val amount  = message.amount

        val xp = XpUtils.getPlayerXp( user ) min amount
        val newBalance = UnsignedLong.valueOf( balance ) plus UnsignedLong.valueOf( xp )

        XpUtils.removePlayerXp( user, xp )
        ATMAccountManager.setBalance( message.accountId, newBalance.longValue )

        new Deposit( xp, message.accountId, message.playerId )

      case Side.CLIENT =>
        val player = FMLClientHandler.instance.getClientPlayerEntity
        XpUtils.removePlayerXp( player, message.amount )

        null
    }

}

class Deposit
  extends IMessage
{
  var amount: Int = 0
  var accountId: UUID = null
  var playerId: UUID = null


  def this( amount: Int, accountId: UUID, playerId: UUID ) = {
    this()
    this.amount = amount
    this.accountId = accountId
    this.playerId = playerId
  }

  override def fromBytes( buf: ByteBuf ): Unit = {
    amount = buf.readInt()
    accountId = new UUID( buf.readLong(), buf.readLong() )
    playerId = new UUID( buf.readLong(), buf.readLong() )
  }

  override def toBytes( buf: ByteBuf ): Unit = {
    buf.writeInt( amount )
    buf.writeLong( accountId.getMostSignificantBits )
    buf.writeLong( accountId.getLeastSignificantBits )
    buf.writeLong( playerId.getMostSignificantBits )
    buf.writeLong( playerId.getLeastSignificantBits )
  }

}