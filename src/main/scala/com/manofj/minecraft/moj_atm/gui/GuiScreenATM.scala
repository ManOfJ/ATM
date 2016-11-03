package com.manofj.minecraft.moj_atm.gui

import java.text.NumberFormat

import scala.language.implicitConversions

import com.google.common.primitives.UnsignedLong
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type

import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.model.ModelHumanoidHead
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.resources.DefaultPlayerSkin
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation

import net.minecraftforge.fml.client.config.GuiButtonExt
import net.minecraftforge.fml.client.config.GuiUtils
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

import com.manofj.commons.scala.util.conversions.Boolean$

import com.manofj.minecraft.moj_atm.ATM
import com.manofj.minecraft.moj_atm.init.ATMItems
import com.manofj.minecraft.moj_atm.network.ATMActivate
import com.manofj.minecraft.moj_atm.network.Deposit
import com.manofj.minecraft.moj_atm.network.Withdrawal
import com.manofj.minecraft.moj_atm.tileentity.TileEntityATM
import com.manofj.minecraft.moj_atm.util.XpUtils


private object GuiScreenATM {

  final val ATM_GUI_TEXTURE = new ResourceLocation( "textures/gui/demo_background.png" )
  final val BUTTON_TEXTURE  = new ResourceLocation( "textures/gui/widgets.png" )

  final val ATM_GUI_ITEM = new ItemStack( ATMItems.ATM_GUI )

  final val TEXT_BACKSPACE     = ATM.languageKey( "backspace", "gui.atm.button" )
  final val TEXT_CLEAR         = ATM.languageKey( "clear", "gui.atm.button" )
  final val TEXT_SO_THAT_LEVEL = ATM.languageKey( "so_that_level", "gui.atm.button" )
  final val TEXT_SO_THAT_XP    = ATM.languageKey( "so_that_xp", "gui.atm.button" )
  final val TEXT_DEPOSIT       = ATM.languageKey( "deposit", "gui.atm.button" )
  final val TEXT_WITHDRAWAL    = ATM.languageKey( "withdrawal", "gui.atm.button" )

  final val MAX_LEVEL = XpUtils.xpToLv( Int.MaxValue )
  final val INT_MAX   = UnsignedLong.valueOf( Int.MaxValue )


  implicit def int2UnsignedLong( i: Int ): UnsignedLong = UnsignedLong.valueOf( i )

  implicit def long2UnsignedLong( l: Long ): UnsignedLong = UnsignedLong.valueOf( l )

  implicit def unsignedLong2Int( ul: UnsignedLong ): Int = ( ul.compareTo( INT_MAX ) > 0 ) ? Int.MaxValue ! ul.intValue

  implicit def unsignedLong2Long( ul: UnsignedLong ): Long = ul.longValue


  def checkOverflow( value: Long ): Long = value min Int.MaxValue

}

@SideOnly( Side.CLIENT )
class GuiScreenATM( player: EntityPlayer, atm: TileEntityATM )
  extends GuiScreen
{
  import com.manofj.minecraft.moj_atm.gui.GuiScreenATM._


  private[ this ] var guiTop    = 0
  private[ this ] var guiLeft   = 0
  private[ this ] var guiWidth  = 248
  private[ this ] val guiHeight = 200

  private[ this ] val headModel  = new ModelHumanoidHead
  private[ this ] var playerSkin = DefaultPlayerSkin.getDefaultSkinLegacy

  private[ this ] val numberFormat = NumberFormat.getIntegerInstance
  private[ this ] var amount  = 0L
  private[ this ] var balance = UnsignedLong.ZERO


  private[ this ] def xpValues: Seq[ String ] =
    Seq( numberFormat.format( amount ),
         numberFormat.format( XpUtils.getPlayerXp( player ) ),
         numberFormat.format( balance.bigIntegerValue ) )


  def receiveBalance( balance: Long ): Unit = this.balance = balance


  override def doesGuiPauseGame(): Boolean = false


  override def initGui(): Unit = {
    import scala.collection.convert.WrapAsJava.asJavaCollection


    val functionButtons = Seq( ATM.message( TEXT_BACKSPACE ),
                               ATM.message( TEXT_CLEAR ),
                               ATM.message( TEXT_SO_THAT_LEVEL ),
                               ATM.message( TEXT_SO_THAT_XP ) )
    val functionButtonMaxWidth = functionButtons.map( fontRendererObj.getStringWidth ).max + 12

    val confirmButtons = Seq( ATM.message( TEXT_DEPOSIT ),
                              ATM.message( TEXT_WITHDRAWAL ) )
    val confirmButtonMaxWidth = confirmButtons.map( fontRendererObj.getStringWidth ).max + 12

    val balanceLimitText = UnsignedLong.MAX_VALUE.toString
    val valueTextAreaMaxWidth = fontRendererObj.getStringWidth( balanceLimitText ) + 12


    guiWidth = ( 11 + ( ( 22 + 2 ) * 3 ) + 4 + functionButtonMaxWidth + 11 ) max
               ( 11 + confirmButtonMaxWidth + 11 ) max
               ( 11 + valueTextAreaMaxWidth + 11 )

    guiTop = ( height - guiHeight ) / 2
    guiLeft = ( width - guiWidth ) / 2


    Option( player.getGameProfile ).foreach { profile =>
      val map = mc.getSkinManager.loadSkinFromCache( profile )

      if ( map.containsKey( Type.SKIN ) ) {
        playerSkin = mc.getSkinManager.loadSkin( map.get( Type.SKIN ), Type.SKIN )
      }
      else {
        playerSkin = DefaultPlayerSkin.getDefaultSkin( player.getUniqueID )
      }
    }


    buttonList.clear()

    buttonList.addAll( {
      Seq( "1", "2", "3",
           "4", "5", "6",
           "7", "8", "9",
           "-", "0", "00" )
        .zipWithIndex
        .map { case ( str, i ) =>
          val w = 22
          val h = 20
          val x = guiLeft + 11 + ( i % 3 ) * ( w + 2 )
          val y = guiTop + 11 + ( i / 3 ) * ( h + 2 )

          new GuiButtonExt( i, x, y, w, h, str )
        }
    } )

    buttonList.addAll( {
      functionButtons
        .zipWithIndex
        .map { case ( str, i ) =>
          val w = functionButtonMaxWidth
          val h = 20
          val x = guiLeft + 87
          val y = guiTop + 11 + i * ( h + 2 )

          new GuiButtonExt( 100 + i, x, y, w, h, str )
        }
    } )

    buttonList.addAll( {
      confirmButtons
        .zipWithIndex
        .map { case ( str, i ) =>
          val w = ( guiWidth - 22 - 2 ) / 2
          val h = 20
          val x = guiLeft + 11 + i * ( w + ( ( guiWidth % 2 == 0 ) ? 2 ! 3 ) )
          val y = guiTop + 169

          new GuiButtonExt( 200 + i, x, y, w, h, str )
        }
    } )

    ATM.sendToServer( new ATMActivate( true, atm.getPos ) )

  }

  override def updateScreen(): Unit = {
    if ( !player.isEntityAlive || !atm.isUseableByPlayer( player ) ) player.closeScreen()
  }

  override def onGuiClosed(): Unit = {
    ATM.sendToServer( new ATMActivate( false, atm.getPos ) )
  }

  override def keyTyped( typedChar: Char, keyCode: Int ): Unit =
    keyCode match {
      case 1 => player.closeScreen()
      case any =>
        if ( mc.gameSettings.keyBindInventory.isActiveAndMatches( keyCode ) )
          player.closeScreen()
    }

  override def actionPerformed( button: GuiButton ): Unit = {
    import scala.collection.convert.WrapAsScala.asScalaBuffer


    buttonList.withFilter( 200 to 201 contains _.id ).foreach( _.enabled = true )

    button.id match {
      // 数値ボタンの挙動
      case num if num < 9 => amount = checkOverflow( amount * 10 + num + 1 )
      case 10             => amount = checkOverflow( amount * 10 )
      case 11             => amount = checkOverflow( amount * 100 )

      // 関数ボタンの挙動
      case 100 => amount /= 10
      case 101 => amount = 0
      case 102 =>
        // プレイヤーのXPが指定されたレベルになるように調整する

        val xp = XpUtils.lvToXp( amount.toInt min MAX_LEVEL )
        val upToLevel: Long = xp - XpUtils.getPlayerXp( player )
        if ( upToLevel != 0 ) {
          if ( upToLevel > 0 ) {
            amount = upToLevel
            buttonList.find( _.id == 200 ).foreach( _.enabled = false )
          }
          else {
            amount = upToLevel.abs
            buttonList.find( _.id == 201 ).foreach( _.enabled = false )
          }
        }

      case 103 =>
        // プレイヤーのXPが指定された数値になるように調整する

        val upToXp = amount - XpUtils.getPlayerXp( player )
        if ( upToXp != 0 ) {
          if ( upToXp > 0 ) {
            amount = upToXp
            buttonList.find( _.id == 200 ).foreach( _.enabled = false )
          }
          else {
            amount = upToXp.abs
            buttonList.find( _.id == 201 ).foreach( _.enabled = false )
          }
        }

      // 決定ボタンの挙動
      case 200 =>
        val id = player.getUniqueID
        ATM.sendToServer( new Deposit( amount.toInt, id, id ) )
        amount = 0

      case 201 =>
        val id = player.getUniqueID
        ATM.sendToServer( new Withdrawal( amount.toInt, id, id ) )
        amount = 0

      case _ => // それ以外のボタンでは何もしない
    }
  }

  override def drawScreen( mouseX: Int, mouseY: Int, partialTicks: Float ): Unit = {
    import scala.collection.convert.WrapAsScala.asScalaBuffer
    import scala.collection.mutable.{ ArrayBuffer => List }


    drawDefaultBackground()

    GuiUtils.drawContinuousTexturedBox( ATM_GUI_TEXTURE, guiLeft, guiTop, 0, 0, guiWidth, guiHeight, 248, 166, 4, zLevel )

    mc.getTextureManager.bindTexture( playerSkin )
    GlStateManager.pushMatrix()
    GlStateManager.disableCull()
    GlStateManager.translate( guiLeft + 21, guiTop + 143, 50F )
    GlStateManager.enableRescaleNormal()
    GlStateManager.scale( -32F, 32F, 32F )
    GlStateManager.rotate( 180F, 0F, 1F, 0F )
    GlStateManager.enableAlpha()
    GlStateManager.enableBlendProfile( GlStateManager.Profile.PLAYER_SKIN )
    headModel.render( null, 0F, 0F, 0F, 0F, 0F, 0.0625F )
    GlStateManager.disableBlendProfile( GlStateManager.Profile.PLAYER_SKIN )
    GlStateManager.disableAlpha()
    GlStateManager.disableRescaleNormal()
    GlStateManager.disableCull()
    GlStateManager.popMatrix()

    RenderHelper.enableGUIStandardItemLighting()
    itemRender.renderItemIntoGUI( ATM_GUI_ITEM, guiLeft + 13, guiTop + 149 )
    RenderHelper.disableStandardItemLighting()

    xpValues
      .zipWithIndex
      .foreach { case ( str, i ) =>
        val w = guiWidth - 22
        val h = 20
        val x = guiLeft + 11
        val y = guiTop + 103 + i * ( h + 2 )

        GuiUtils.drawContinuousTexturedBox( BUTTON_TEXTURE, x, y, 0, 46, w, h, 200, 20, 2, 3, 2, 2, zLevel )

        val x2 = x + w - 6
        val y2 = y + ( h - 8 ) / 2

        def stringWidth = fontRendererObj.getStringWidth( _ )
        def drawChangeValueString( s: String, surplus: Boolean ) =
          drawString( fontRendererObj, s, x2 - stringWidth( str ) - 6 - stringWidth( s ), y2, surplus ? 0x00C800 ! 0xC80000 )

        buttonList.filter( 200 to 201 contains _.id ).map( _.enabled ) match {
          case List( true, true ) =>

          // 引き出しのみ選択可能時､残高が数値に満たなければ不足分を表示
          case List( false, true ) if i == 0 =>
            if ( balance.compareTo( amount ) < 0 ) {
              drawChangeValueString( s"(-${ amount - balance })", surplus = false )
            }

          // プレイヤー経験値の増減値を表示する
          case List( deposit, withdrawal ) if i == 1 =>
            val s = if ( deposit ) {
              s"(-${ amount min XpUtils.getPlayerXp( player ) })"
            }
            else {
              val b = balance: Int
              val x = Int.MaxValue - player.experienceTotal

              s"(+${ amount min b min x })"
            }
            drawChangeValueString( s, withdrawal )

          // 残高の増減値を表示する
          case List( deposit, withdrawal ) if i == 2 =>
            val s = if ( deposit ) {
              s"(+${ amount min XpUtils.getPlayerXp( player ) })"
            }
            else {
              val b = balance: Int
              val x = Int.MaxValue - player.experienceTotal

              s"(-${ amount min b min x })"
            }
            drawChangeValueString( s, deposit )

          case _ =>
        }

        drawString( fontRendererObj, str, x2 - stringWidth( str ), y2, 0xFFFFFF )
    }

    GlStateManager.scale( 1F, 1F, 1F )
    super.drawScreen( mouseX, mouseY, partialTicks )

  }
}
