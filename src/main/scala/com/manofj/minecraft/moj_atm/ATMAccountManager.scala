package com.manofj.minecraft.moj_atm

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID

import scala.collection.mutable.{ Map => MutableMap }
import scala.util.Failure
import scala.util.Success
import scala.util.Try

import com.google.common.primitives.UnsignedLong

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.CompressedStreamTools
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTUtil

import net.minecraftforge.common.util.Constants.NBT
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

import com.manofj.commons.scala.util.conversions.Any$

import com.manofj.minecraft.moj_atm.network.BalanceUpdate


@Mod.EventBusSubscriber
object ATMAccountManager {

  private[ this ] final val DATA_FILENAME = "moj_atm.dat"


  private[ this ] val accounts = MutableMap.empty[ UUID, Long ]

  private[ this ] var currentWorld = ""


  def sendUpdateMessage( accountId: UUID ): Unit = {
    val server = FMLCommonHandler.instance.getMinecraftServerInstance
    val owner = server.getPlayerList.getPlayerByUUID( accountId )
    val balance = getBalance( accountId )

    ATM.sendTo( new BalanceUpdate( balance ), owner )
  }

  def getBalance( accountId: UUID ): Long = accounts.getOrElseUpdate( accountId, 0 )
  def getBalance( player: EntityPlayer ): Long = getBalance( player.getUniqueID )

  def setBalance( accountId: UUID, balance: Long ): Unit = {
    accounts.update( accountId, balance )
    sendUpdateMessage( accountId )
  }
  def setBalance( player: EntityPlayer, balance: Long ): Unit = setBalance( player.getUniqueID, balance )


  def deserializeNBT( tag: NBTTagCompound ): Unit = {
    accounts.clear()

    if ( tag.hasKey( "Accounts", NBT.TAG_LIST ) ) {

      val list = tag.getTagList( "Accounts", NBT.TAG_COMPOUND )
      ( 0 until list.tagCount ) foreach { i =>
        val data    = list.getCompoundTagAt( i )
        val uuid    = NBTUtil.getUUIDFromTag( data )
        val balance = data.getLong( "Balance" )

        ATM.trace( s"Load account [$uuid : ${ UnsignedLong.valueOf( balance ) }]" )

        accounts.put( uuid, balance )
      }

      ATM.info( s"${ accounts.size } Account(s) loaded" )
    }
  }

  def serializeNBT: NBTTagCompound =
    new NBTTagCompound << { tag =>

      val list = new NBTTagList
      accounts.foreach { case ( uuid, balance ) =>
        list.appendTag( NBTUtil.createUUIDTag( uuid ) << { data =>
          data.setLong( "Balance", balance )
        } )
      }
      tag.setTag( "Accounts", list )

      ATM.info( s"${ list.tagCount } Account(s) saved" )
    }

  @SubscribeEvent
  def worldLoad( event: WorldEvent.Load ): Unit = {
    val world = event.getWorld
    if ( !world.isRemote && world.provider.isSurfaceWorld ) {
      val worldDir = world.getSaveHandler.getWorldDirectory
      val worldName = worldDir.getName

      ATM.trace( s"World load event [$worldName]" )

      if ( currentWorld != worldName ) {
        currentWorld = worldName

        val file = new File( worldDir, DATA_FILENAME )
        if ( file.isFile && file.canRead ) {
          ATM.trace( s"ATM accounts loading from $worldDir/$DATA_FILENAME" )

          Try( new FileInputStream( file ) ) match {
            case Success( input ) => deserializeNBT( CompressedStreamTools.readCompressed( input ) )
            case Failure( error ) => ATM.warn( "Failed to read ATM accounts data", error )
        } }
      }

    }
  }

  @SubscribeEvent
  def worldSave( event: WorldEvent.Save ): Unit = {
    val world = event.getWorld
    if ( !world.isRemote && world.provider.isSurfaceWorld ) {
      val worldDir = world.getSaveHandler.getWorldDirectory
      val worldName = worldDir.getName

      ATM.trace( s"World save event [$worldName]" )

      if ( currentWorld == worldName ) {
        val file = new File( worldDir, "moj_atm.dat" )

        ATM.trace( s"ATM accounts saving to $worldDir/$DATA_FILENAME" )

        Try( new FileOutputStream( file ) ) match {
          case Success( output ) => CompressedStreamTools.writeCompressed( serializeNBT, output )
          case Failure( error )  => ATM.warn( "Failed to write ATM accounts data", error )
      } }
    }
  }

}
