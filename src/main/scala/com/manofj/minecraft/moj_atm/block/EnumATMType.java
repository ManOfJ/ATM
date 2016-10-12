package com.manofj.minecraft.moj_atm.block;

import net.minecraft.util.IStringSerializable;


public enum EnumATMType
    implements IStringSerializable
{
    NORMAL( "normal" ), WALL( "wall" );

    private final String name;

    EnumATMType(String name) {
        this.name = name;
    }

    @Override public String getName() { return name; }

    @Override public String toString() { return name; }
}
