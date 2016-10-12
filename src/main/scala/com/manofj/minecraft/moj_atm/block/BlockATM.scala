package com.manofj.minecraft.moj_atm.block

import java.util.Random

import net.minecraft.block.BlockContainer
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyEnum
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumBlockRenderType
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.world.World

import com.manofj.commons.scala.util.conversions.Any$
import com.manofj.commons.scala.util.conversions.Boolean$

import com.manofj.minecraft.moj_atm.ATM
import com.manofj.minecraft.moj_atm.init.ATMBlocks
import com.manofj.minecraft.moj_atm.tileentity.TileEntityATM


object BlockATM {

  private[ this ] val facings = EnumFacing.Plane.HORIZONTAL.facings.toIndexedSeq


  final val TYPE   = PropertyEnum.create( "type", classOf[ EnumATMType ] )
  final val FACING = PropertyEnum.create( "facing", classOf[ EnumFacing ], facings: _* )


  private def meta2Type( meta: Int ): EnumATMType = ( meta < 8 ) ? EnumATMType.NORMAL ! EnumATMType.WALL

  private def meta2Facing( meta: Int ): EnumFacing = facings( meta & 7 )

  private def facing2Meta( facing: EnumFacing ): Int = facings.indexOf( facing )


  def setActive( active: Boolean, world: World, pos: BlockPos ): Unit = {

    val state      = world.getBlockState( pos )
    val tileEntity = world.getTileEntity( pos )
    val newState   = active ? ATMBlocks.ATM_ACTIVE.getDefaultState ! ATMBlocks.ATM.getDefaultState

    world.setBlockState(
      pos,
      newState
        .withProperty( BlockATM.TYPE, state.getValue( BlockATM.TYPE ) )
        .withProperty( BlockATM.FACING, state.getValue( BlockATM.FACING ) ),
      3 )

    if ( tileEntity.nonNull ) {
      tileEntity.validate()
      tileEntity.updateContainingBlockInfo()
      world.setTileEntity( pos, tileEntity )
    }

  }

}

class BlockATM
  extends BlockContainer( Material.IRON )
{

  {
    setHardness( 5F )
    setResistance( 10F )
    setSoundType( SoundType.METAL )
    setDefaultState( blockState.getBaseState
      .withProperty( BlockATM.TYPE, EnumATMType.NORMAL )
      .withProperty( BlockATM.FACING, EnumFacing.NORTH ) )
  }

  override def createBlockState(): BlockStateContainer = new BlockStateContainer( this, BlockATM.TYPE, BlockATM.FACING )


  override def getItemDropped( state: IBlockState, rand: Random, fortune: Int ): Item = Item.getItemFromBlock( ATMBlocks.ATM )

  override def getItem( worldIn: World, pos: BlockPos, state: IBlockState ): ItemStack = new ItemStack( ATMBlocks.ATM )


  override def getMetaFromState( state: IBlockState ): Int = {
    val meta = BlockATM.facing2Meta( state.getValue( BlockATM.FACING ) )
    ( state.getValue( BlockATM.TYPE ) == EnumATMType.NORMAL ) ? meta ! ( meta | 8 )
  }

  override def getStateFromMeta( meta: Int ): IBlockState =
    getDefaultState
      .withProperty( BlockATM.TYPE, BlockATM.meta2Type( meta ) )
      .withProperty( BlockATM.FACING, BlockATM.meta2Facing( meta ) )


  override def getRenderType( state: IBlockState ): EnumBlockRenderType = EnumBlockRenderType.MODEL

  override def onBlockPlaced( worldIn: World,
                              pos: BlockPos,
                              facing: EnumFacing,
                              hitX: Float,
                              hitY: Float,
                              hitZ: Float,
                              meta: Int,
                              placer: EntityLivingBase ): IBlockState =
    getStateFromMeta( meta )
      .withProperty( BlockATM.TYPE, facing.getAxis.isHorizontal ? EnumATMType.WALL ! EnumATMType.NORMAL )
      .withProperty( BlockATM.FACING, placer.getHorizontalFacing.getOpposite )

  override def createNewTileEntity( worldIn: World, meta: Int ): TileEntity = new TileEntityATM

  override def onBlockActivated( worldIn: World,
                                 pos: BlockPos,
                                 state: IBlockState,
                                 playerIn: EntityPlayer,
                                 hand: EnumHand,
                                 heldItem: ItemStack,
                                 side: EnumFacing,
                                 hitX: Float,
                                 hitY: Float,
                                 hitZ: Float ): Boolean =
    Option( worldIn.getTileEntity( pos ) ) match {
      case Some( atm: TileEntityATM ) =>
        if ( !atm.isInUse || atm.isUser( playerIn ) ) {
          playerIn.openGui( ATM, 0, worldIn, pos.getX, pos.getY, pos.getZ )
        }
        else {
          playerIn.addChatComponentMessage( new TextComponentTranslation( ATM.languageKey( "chat.atm.in_use" ) ) )
        }
        true

      case _ => false
    }

}
