/**
 * Copyright (c) [2018] [ The Semux Developers ]
 * Copyright (c) [2016] [ <ether.camp> ]
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.vm.chainspec;

import static org.ethereum.vm.util.BigIntegerUtil.toBI;
import static org.ethereum.vm.util.ByteArrayUtil.EMPTY_BYTE_ARRAY;
import static org.ethereum.vm.util.ByteArrayUtil.parseWord;

import org.ethereum.vm.DataWord;
import org.ethereum.vm.crypto.zksnark.BN128;
import org.ethereum.vm.crypto.zksnark.BN128Fp;
import org.ethereum.vm.crypto.zksnark.BN128G1;
import org.ethereum.vm.crypto.zksnark.BN128G2;
import org.ethereum.vm.crypto.zksnark.Fp;
import org.ethereum.vm.crypto.zksnark.Fp2;
import org.ethereum.vm.crypto.zksnark.PairingCheck;
import org.ethereum.vm.util.Pair;

public class ByzantiumPrecompiledContracts extends BasePrecompiledContracts {

    private static final BN128Addition altBN128Add = new BN128Addition();
    private static final BN128Multiplication altBN128Mul = new BN128Multiplication();
    private static final BN128Pairing altBN128Pairing = new BN128Pairing();

    private static final DataWord altBN128AddAddr = DataWord.of(6);
    private static final DataWord altBN128MulAddr = DataWord.of(7);
    private static final DataWord altBN128PairingAddr = DataWord.of(8);

    @Override
    public PrecompiledContract getContractForAddress(DataWord address) {

        if (address.equals(altBN128AddAddr)) {
            return altBN128Add;
        } else if (address.equals(altBN128MulAddr)) {
            return altBN128Mul;
        } else if (address.equals(altBN128PairingAddr)) {
            return altBN128Pairing;
        }

        return super.getContractForAddress(address);
    }

    /**
     * Computes point addition on Barreto–Naehrig curve. See {@link BN128Fp} for
     * details<br/>
     * <br/>
     *
     * input data[]:<br/>
     * two points encoded as (x, y), where x and y are 32-byte left-padded
     * integers,<br/>
     * if input is shorter than expected, it's assumed to be right-padded with zero
     * bytes<br/>
     * <br/>
     *
     * output:<br/>
     * resulting point (x', y'), where x and y encoded as 32-byte left-padded
     * integers<br/>
     */
    public static class BN128Addition implements PrecompiledContract {

        @Override
        public long getGasForData(byte[] data) {
            return 500;
        }

        @Override
        public Pair<Boolean, byte[]> execute(PrecompiledContractContext context) {
            byte[] data = context.getInternalTransaction().getData();
            if (data == null) {
                data = EMPTY_BYTE_ARRAY;
            }

            byte[] x1 = parseWord(data, 0);
            byte[] y1 = parseWord(data, 1);

            byte[] x2 = parseWord(data, 2);
            byte[] y2 = parseWord(data, 3);

            BN128<Fp> p1 = BN128Fp.create(x1, y1);
            if (p1 == null)
                return Pair.of(false, EMPTY_BYTE_ARRAY);

            BN128<Fp> p2 = BN128Fp.create(x2, y2);
            if (p2 == null)
                return Pair.of(false, EMPTY_BYTE_ARRAY);

            BN128<Fp> res = p1.add(p2).toEthNotation();

            return Pair.of(true, encodeRes(res.x().bytes(), res.y().bytes()));
        }
    }

    /**
     * Computes multiplication of scalar value on a point belonging to
     * Barreto–Naehrig curve. See {@link BN128Fp} for details<br/>
     * <br/>
     *
     * input data[]:<br/>
     * point encoded as (x, y) is followed by scalar s, where x, y and s are 32-byte
     * left-padded integers,<br/>
     * if input is shorter than expected, it's assumed to be right-padded with zero
     * bytes<br/>
     * <br/>
     *
     * output:<br/>
     * resulting point (x', y'), where x and y encoded as 32-byte left-padded
     * integers<br/>
     */
    public static class BN128Multiplication implements PrecompiledContract {

        @Override
        public long getGasForData(byte[] data) {
            return 40000;
        }

        @Override
        public Pair<Boolean, byte[]> execute(PrecompiledContractContext context) {
            byte[] data = context.getInternalTransaction().getData();
            if (data == null) {
                data = EMPTY_BYTE_ARRAY;
            }

            byte[] x = parseWord(data, 0);
            byte[] y = parseWord(data, 1);

            byte[] s = parseWord(data, 2);

            BN128<Fp> p = BN128Fp.create(x, y);
            if (p == null)
                return Pair.of(false, EMPTY_BYTE_ARRAY);

            BN128<Fp> res = p.mul(toBI(s)).toEthNotation();

            return Pair.of(true, encodeRes(res.x().bytes(), res.y().bytes()));
        }
    }

    /**
     * Computes pairing check. <br/>
     * See {@link PairingCheck} for details.<br/>
     * <br/>
     *
     * Input data[]: <br/>
     * an array of points (a1, b1, ... , ak, bk), <br/>
     * where "ai" is a point of {@link BN128Fp} curve and encoded as two 32-byte
     * left-padded integers (x; y) <br/>
     * "bi" is a point of {@link BN128G2} curve and encoded as four 32-byte
     * left-padded integers {@code (ai + b; ci + d)}, each coordinate of the point
     * is a big-endian {@link Fp2} number, so {@code b} precedes {@code a} in the
     * encoding: {@code (b, a; d, c)} <br/>
     * thus each pair (ai, bi) has 192 bytes length, if 192 is not a multiple of
     * {@code data.length} then execution fails <br/>
     * the number of pairs is derived from input length by dividing it by 192 (the
     * length of a pair) <br/>
     * <br/>
     *
     * output: <br/>
     * pairing product which is either 0 or 1, encoded as 32-byte left-padded
     * integer <br/>
     */
    public static class BN128Pairing implements PrecompiledContract {

        private static final int PAIR_SIZE = 192;

        @Override
        public long getGasForData(byte[] data) {
            if (data == null) {
                return 100000;
            }

            return 80000 * (data.length / PAIR_SIZE) + 100000;
        }

        @Override
        public Pair<Boolean, byte[]> execute(PrecompiledContractContext context) {
            byte[] data = context.getInternalTransaction().getData();
            if (data == null) {
                data = EMPTY_BYTE_ARRAY;
            }

            // fail if input len is not a multiple of PAIR_SIZE
            if (data.length % PAIR_SIZE > 0)
                return Pair.of(false, EMPTY_BYTE_ARRAY);

            PairingCheck check = PairingCheck.create();

            // iterating over all pairs
            for (int offset = 0; offset < data.length; offset += PAIR_SIZE) {

                Pair<BN128G1, BN128G2> pair = decodePair(data, offset);

                // fail if decoding has failed
                if (pair == null)
                    return Pair.of(false, EMPTY_BYTE_ARRAY);

                check.addPair(pair.getLeft(), pair.getRight());
            }

            check.run();
            int result = check.result();

            return Pair.of(true, DataWord.of(result).getData());
        }

        private Pair<BN128G1, BN128G2> decodePair(byte[] in, int offset) {
            byte[] x = parseWord(in, offset, 0);
            byte[] y = parseWord(in, offset, 1);

            BN128G1 p1 = BN128G1.create(x, y);

            // fail if point is invalid
            if (p1 == null)
                return null;

            // (b, a)
            byte[] b = parseWord(in, offset, 2);
            byte[] a = parseWord(in, offset, 3);

            // (d, c)
            byte[] d = parseWord(in, offset, 4);
            byte[] c = parseWord(in, offset, 5);

            BN128G2 p2 = BN128G2.create(a, b, c, d);

            // fail if point is invalid
            if (p2 == null)
                return null;

            return Pair.of(p1, p2);
        }
    }
}
