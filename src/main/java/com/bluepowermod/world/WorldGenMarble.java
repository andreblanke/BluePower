/*
 * This file is part of Blue Power.
 *
 *     Blue Power is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Blue Power is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Blue Power.  If not, see <http://www.gnu.org/licenses/>
 */

package com.bluepowermod.world;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

import com.github.bsideup.jabel.Desugar;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenMinable;

public final class WorldGenMarble extends WorldGenMinable {

    private int numberOfBlocks;

    private final LinkedList<MarbleVein> marbleVeins = new LinkedList<>();
    private final Set<MarbleVeinCandidate> marbleVeinCandidates = new HashSet<>();
    private final Block block;

    public WorldGenMarble(Block block, int num) {
        super(block, num);

        this.block = block;
        this.numberOfBlocks = num;
    }

    private void addBlock(int x, int y, int z, int num) {
        final var marbleCandidate = new MarbleVeinCandidate(x, y, z);
        if (marbleVeinCandidates.contains(marbleCandidate))
            return;

        marbleVeins.add(new MarbleVein(x, y, z, num));
        marbleVeinCandidates.add(marbleCandidate);
    }

    @Desugar
    record MarbleVeinCandidate(int x, int y, int z) { }

    @Desugar
    record MarbleVein(int x, int y, int z, int num) { }

    private void searchBlock(World world, int x, int y, int z, int num) {
        if (world.isAirBlock(x - 1, y, z) || world.isAirBlock(x + 1, y, z) || world.isAirBlock(x, y - 1, z) || world.isAirBlock(x, y + 1, z)
                || world.isAirBlock(x, y, z - 1) || world.isAirBlock(x, y, z + 1)) {
            num = 6;
        }
        addBlock(x - 1, y, z, num);
        addBlock(x + 1, y, z, num);
        addBlock(x, y - 1, z, num);
        addBlock(x, y + 1, z, num);
        addBlock(x, y, z - 1, num);
        addBlock(x, y, z + 1, num);
    }

    @Override
    public boolean generate(World world, Random random, int x, int y, int z) {
        if (!world.blockExists(x, y, z))
            return false;

        int i = y;
        while (world.getBlock(x, i, z) != Blocks.stone) {
            if (i > 96) return false; // Don't generate marble over y96
            i++;
            addBlock(x, i, z, 6);
        }
        while (!marbleVeins.isEmpty() && (numberOfBlocks > 0)) {
            final var marbleVein = marbleVeins.removeFirst();
            if (world.getBlock(marbleVein.x(), marbleVein.y(), marbleVein.z()) == Blocks.stone) {
                world.setBlock(marbleVein.x(), marbleVein.y(), marbleVein.z(), block);
                if (marbleVein.num() > 0)
                    searchBlock(world, marbleVein.x(), marbleVein.y(), marbleVein.z(), marbleVein.num() - 1);
                --numberOfBlocks;
            }
        }
        return true;
    }
}
