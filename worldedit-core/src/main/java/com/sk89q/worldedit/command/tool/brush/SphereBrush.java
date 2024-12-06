/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.command.tool.brush;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockTypes;

public class SphereBrush extends AbstractStructureBrush {

    @Override
    public int createStructure(EditSession editSession, BlockVector3 position, Pattern pattern, double size) throws MaxChangedBlocksException {
        if (pattern == null) {
            pattern = BlockTypes.COBBLESTONE.getDefaultState();
        }

        return createStructure(editSession, position, pattern, size, size, size, true);
    }

    /**
     * Makes a sphere or ellipsoid.
     *
     * @param pos     Center of the sphere or ellipsoid
     * @param block   The block pattern to use
     * @param radiusX The sphere/ellipsoid's largest north/south extent
     * @param radiusY The sphere/ellipsoid's largest up/down extent
     * @param radiusZ The sphere/ellipsoid's largest east/west extent
     * @param filled  If false, only a shell will be generated.
     * @return number of blocks changed
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int createStructure(EditSession editSession, BlockVector3 pos, Pattern block, double radiusX, double radiusY, double radiusZ, boolean filled) throws MaxChangedBlocksException {
        int affected = 0;

        radiusX += 0.5;
        radiusY += 0.5;
        radiusZ += 0.5;

        final double invRadiusX = 1 / radiusX;
        final double invRadiusY = 1 / radiusY;
        final double invRadiusZ = 1 / radiusZ;

        final int ceilRadiusX = (int) Math.ceil(radiusX);
        final int ceilRadiusY = (int) Math.ceil(radiusY);
        final int ceilRadiusZ = (int) Math.ceil(radiusZ);

        double nextXn = 0;
        forX:
        for (int x = 0; x <= ceilRadiusX; ++x) {
            final double xn = nextXn;
            nextXn = (x + 1) * invRadiusX;
            double nextYn = 0;
            forY:
            for (int y = 0; y <= ceilRadiusY; ++y) {
                final double yn = nextYn;
                nextYn = (y + 1) * invRadiusY;
                double nextZn = 0;
                forZ:
                for (int z = 0; z <= ceilRadiusZ; ++z) {
                    final double zn = nextZn;
                    nextZn = (z + 1) * invRadiusZ;

                    double distanceSq = lengthSq(xn, yn, zn);
                    if (distanceSq > 1) {
                        if (z == 0) {
                            if (y == 0) {
                                break forX;
                            }
                            break forY;
                        }
                        break forZ;
                    }

                    if (!filled) {
                        if (lengthSq(nextXn, yn, zn) <= 1 && lengthSq(xn, nextYn, zn) <= 1 && lengthSq(xn, yn, nextZn) <= 1) {
                            continue;
                        }
                    }

                    if (editSession.setBlock(pos.add(x, y, z), block)) {
                        ++affected;
                    }
                    if (editSession.setBlock(pos.add(-x, y, z), block)) {
                        ++affected;
                    }
                    if (editSession.setBlock(pos.add(x, -y, z), block)) {
                        ++affected;
                    }
                    if (editSession.setBlock(pos.add(x, y, -z), block)) {
                        ++affected;
                    }
                    if (editSession.setBlock(pos.add(-x, -y, z), block)) {
                        ++affected;
                    }
                    if (editSession.setBlock(pos.add(x, -y, -z), block)) {
                        ++affected;
                    }
                    if (editSession.setBlock(pos.add(-x, y, -z), block)) {
                        ++affected;
                    }
                    if (editSession.setBlock(pos.add(-x, -y, -z), block)) {
                        ++affected;
                    }
                }
            }
        }


        return affected;
    }
}
