/**
 *  Copyright (C) 2002-2011  The FreeCol Team
 *
 *  This file is part of FreeCol.
 *
 *  FreeCol is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  FreeCol is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FreeCol.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.freecol.server.ai;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.EquipmentType;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.GoodsType;
import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.Map.Direction;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.server.ServerTestHelper;
import net.sf.freecol.server.model.ServerUnit;
import net.sf.freecol.util.test.FreeColTestCase;
import net.sf.freecol.util.test.FreeColTestUtils;


public class StandardAIPlayerTest extends FreeColTestCase {

    private static final EquipmentType horsesEqType
        = spec().getEquipmentType("model.equipment.horses");
    private static final EquipmentType musketsEqType
        = spec().getEquipmentType("model.equipment.muskets");
    private static final EquipmentType toolsEqType
        = spec().getEquipmentType("model.equipment.tools");

    private static final GoodsType horsesType
        = spec().getGoodsType("model.goods.horses");
    private static final GoodsType musketsType
        = spec().getGoodsType("model.goods.muskets");

    private static final UnitType artilleryType
        = spec().getUnitType("model.unit.artillery");
    private static final UnitType colonistType
        = spec().getUnitType("model.unit.freeColonist");
    private static final UnitType expertSoldierType
        = spec().getUnitType("model.unit.veteranSoldier");
    private static final UnitType indenturedServantType
        = spec().getUnitType("model.unit.indenturedServant");


    @Override
    public void tearDown() throws Exception {
        ServerTestHelper.stopServerGame();
        super.tearDown();
    }

    public void testEquipBraves() {
        Game game = ServerTestHelper.startServerGame(getTestMap());
        AIMain aiMain = ServerTestHelper.getServer().getAIMain();

        FreeColTestCase.IndianSettlementBuilder builder
            = new FreeColTestCase.IndianSettlementBuilder(game);
        IndianSettlement camp = builder.initialBravesInCamp(3).build();
        NativeAIPlayer player = (NativeAIPlayer) aiMain.getAIPlayer(camp.getOwner());
        game.setCurrentPlayer(camp.getOwner());

        int bravesToEquip = camp.getUnitCount();
        int horsesReqPerUnit = spec().getEquipmentType("model.equipment.indian.horses").getAmountRequiredOf(horsesType);
        int musketsReqPerUnit = spec().getEquipmentType("model.equipment.indian.muskets").getAmountRequiredOf(musketsType);
        int totalHorsesReq = bravesToEquip * horsesReqPerUnit;
        int totalMusketsReq = bravesToEquip * musketsReqPerUnit;
        int totalHorsesAvail = totalHorsesReq*2;
        int totalMusketsAvail = totalMusketsReq*2;

        // Verify initial conditions
        assertEquals("No horses should exist in camp",0,camp.getGoodsCount(horsesType));
        assertEquals("No muskets should exist in camp",0,camp.getGoodsCount(musketsType));

        for(Unit unit : camp.getUnitList()){
            if(unit.isMounted()){
                fail("Indian should not have mounted braves");
            }
            if(unit.isArmed()){
                fail("Indian should not have armed braves");
            }
        }

        // Setup
        camp.addGoods(horsesType,totalHorsesAvail);
        camp.addGoods(musketsType,totalMusketsAvail);

        assertEquals("Wrong initial number of horses in Indian camp",totalHorsesAvail,camp.getGoodsCount(horsesType));
        assertEquals("Wrong initial number of muskets in Indian camp",totalMusketsAvail,camp.getGoodsCount(musketsType));

        // Exercise SUT
        player.equipBraves(camp);

        // Verify results
        assertEquals("Wrong final number of horses in Indian camp",totalHorsesReq,camp.getGoodsCount(horsesType));
        assertEquals("Wrong final number of muskets in Indian camp",totalMusketsReq,camp.getGoodsCount(musketsType));

        int mounted = 0;
        int armed = 0;
        for(Unit unit : camp.getUnitList()){
            if(unit.isMounted()){
                mounted++;
            }
            if(unit.isArmed()){
                armed++;
            }
        }
        assertEquals("Wrong number of units armed",camp.getUnitCount(),armed);
        assertEquals("Wrong number of units mounted",camp.getUnitCount(),mounted);
    }

    public void testEquipBravesNotEnoughReqGoods(){
        Game game = ServerTestHelper.startServerGame(getTestMap());
        AIMain aiMain = ServerTestHelper.getServer().getAIMain();

        FreeColTestCase.IndianSettlementBuilder builder
            = new FreeColTestCase.IndianSettlementBuilder(game);
        IndianSettlement camp = builder.initialBravesInCamp(3).build();
        NativeAIPlayer player = (NativeAIPlayer) aiMain.getAIPlayer(camp.getOwner());
        game.setCurrentPlayer(camp.getOwner());

        int bravesToEquip = camp.getUnitCount() - 1;
        int horsesReqPerUnit = spec().getEquipmentType("model.equipment.indian.horses").getAmountRequiredOf(horsesType);
        int musketsReqPerUnit = spec().getEquipmentType("model.equipment.indian.muskets").getAmountRequiredOf(musketsType);
        int totalHorsesAvail = bravesToEquip * horsesReqPerUnit;
        int totalMusketsAvail = bravesToEquip * musketsReqPerUnit;

        // Verify initial conditions
        assertEquals("No horses should exist in camp",0,camp.getGoodsCount(horsesType));
        assertEquals("No muskets should exist in camp",0,camp.getGoodsCount(musketsType));

        for(Unit unit : camp.getUnitList()){
            if(unit.isMounted()){
                fail("Indian should not have mounted braves");
            }
            if(unit.isArmed()){
                fail("Indian should not have armed braves");
            }
        }

        // Setup
        camp.addGoods(horsesType,totalHorsesAvail);
        camp.addGoods(musketsType,totalMusketsAvail);

        assertEquals("Wrong initial number of horses in Indian camp",totalHorsesAvail,camp.getGoodsCount(horsesType));
        assertEquals("Wrong initial number of muskets in Indian camp",totalMusketsAvail,camp.getGoodsCount(musketsType));

        // Exercise SUT
        player.equipBraves(camp);

        // Verify results
        assertEquals("Wrong final number of horses in Indian camp",0,camp.getGoodsCount(horsesType));
        assertEquals("Wrong final number of muskets in Indian camp",0,camp.getGoodsCount(musketsType));

        int mounted = 0;
        int armed = 0;
        for(Unit unit : camp.getUnitList()){
            if(unit.isMounted()){
                mounted++;
            }
            if(unit.isArmed()){
                armed++;
            }
        }
        assertEquals("Wrong number of units armed",bravesToEquip,armed);
        assertEquals("Wrong number of units mounted",bravesToEquip,mounted);
    }

}
