// $Id$
/*
 * This file is a part of WorldEdit.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.operations;

import java.util.Iterator;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.regions.Region;

/**
 * Add snow to a given area. Simulates normal Minecraft precipitation.
 */
public class SimulateSnow extends AbstractOperation implements BlockChange {
    
    private final EditSession context;
    private final Region region;

    private int affected = 0;
    
    /**
     * Create a snowfall operation.
     * 
     * @param context to apply changes to
     * @param region area to apply changes to
     */
    public SimulateSnow(EditSession context, Region region) {
        this.context = context;
        this.region = region;
    }

    @Override
    protected Operation resume() throws MaxChangedBlocksException {
        BaseBlock ice = new BaseBlock(BlockID.ICE);
        BaseBlock snow = new BaseBlock(BlockID.SNOW);

        Iterator<BlockVector> points = region.columnIterator();
        int maxY = region.getMaximumPoint().getBlockY();
        int minY = region.getMinimumPoint().getBlockY();
        
        outer:
        while (points.hasNext()) {
            Vector columnPt = points.next();

            for (int y = maxY; y >= minY; --y) {
                Vector pt = columnPt.setY(y);
                int id = context.getBlockType(pt);

                switch (id) {
                case BlockID.STATIONARY_WATER:
                    if (context.setBlock(pt, ice)) {
                        ++affected;
                    }
                    break outer;

                case BlockID.AIR:
                    continue;

                default:
                    // can't set snow on top of nonsolid blocks
                    if (!BlockType.canPassThrough(id) &&
                            context.setBlock(pt.setY(y + 1), snow)) {
                        ++affected;
                    }
                    break outer;
                }
            }
            break;
        }

        return null;
    }

    @Override
    public void cancel() {
        // Nothing to clean up
    }

    @Override
    public int getBlocksChanged() {
        return affected;
    }

}