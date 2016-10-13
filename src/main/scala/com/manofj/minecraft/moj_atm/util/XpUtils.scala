package com.manofj.minecraft.moj_atm.util

import scala.annotation.tailrec

import net.minecraft.entity.player.EntityPlayer


object XpUtils {

  def xpBarCap( level: Int ): Int =
    level match {
      case lv if lv >= 30 => 112 + ( lv - 30 ) * 9
      case lv if lv >= 15 =>  37 + ( lv - 15 ) * 5
      case lv             =>   7 + lv * 2
    }

  @tailrec
  def xpToLv( xp: Int, lv: Int = 0 ): Int = {
    val cap = xpBarCap( lv )
    if ( xp >= cap ) xpToLv( xp - cap, lv + 1 ) else lv
  }

  def lvToXp( lv: Int, xpBar: Float = 0F ): Int =
    Range( 0, lv ).map( xpBarCap ).sum + math.floor( xpBarCap( lv ) * xpBar ).toInt


  def getPlayerXp( player: EntityPlayer ): Int = lvToXp( player.experienceLevel, player.experience )

  def addPlayerXp( player: EntityPlayer, amount: Int ): Unit = {

    player.addScore( amount )

    val limit = Int.MaxValue - player.experienceTotal
    val _amount = if ( amount > limit ) limit else amount
    val xp = getPlayerXp( player ) + _amount
    val lv = xpToLv( xp )
    val rest = xp - lvToXp( lv )

    player.experienceTotal += _amount
    player.experienceLevel = lv
    player.experience = rest.toFloat / xpBarCap( lv )

  }

  def removePlayerXp( player: EntityPlayer, amount: Int ): Unit = {
    val xp = getPlayerXp( player )

    player.addScore( -xp )
    player.experienceTotal -= xp
    player.experienceLevel = 0
    player.experience = 0

    if ( xp > amount ) {
      addPlayerXp( player, xp - amount )
    }
  }

}
