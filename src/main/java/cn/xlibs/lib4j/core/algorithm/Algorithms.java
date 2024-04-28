package cn.xlibs.lib4j.core.algorithm;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.UUID;
import java.util.zip.CRC32;

/**
 * Description here
 *
 * @author Fury
 * @since 2020/06/06
 * <p>
 * All rights Reserved.
 */
public final class Algorithms {
    private Algorithms(){}

    /**
     *<p>
     * Usage:
     * <code>
     *        UUID uuid = UUID.randomUUID().toString();
     *        boolean beat = Metronome.of(5, 4).beat(uuid);
     * </code>
     * or:
     * <code>
     *        String value = "your string value"
     *        boolean beat = Metronome.of(5, 4).beat(value);
     * </code>
     */
    public static final class Metronome {
        private Metronome(){}

        /**
         * setup Metronome
         * @param frequencyInMinutes job running in every <code>frequencyInMinutes</code> minutes
         * @param hitTimesEveryDay hit times when beat <code>value</code>
         * @return Setup of Metronome
         */
        public static Setup of(int frequencyInMinutes, int hitTimesEveryDay) {
            int tickTime = frequencyInMinutes * 60;
            long hitTimesFactor = (24 * 60 * 60) / hitTimesEveryDay / tickTime;
            return new Setup(tickTime, hitTimesFactor);
        }

        /**
         * Setup of Metronome
         */
        @AllArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class Setup {
            private int tickTime;
            private long hitTimesFactor;

            /**
             * beat <code>uuid</code> for hit checking
             * @param uuid - UUID
             * @return - true if hit
             */
            public boolean beat(UUID uuid) {
                return this.beat(uuid.toString());
            }

            /**
             * beat <code>value</code> for hit checking
             * @param value value for beat
             * @return true if hit
             */
            public boolean beat(String value) {
                return beat(value, System.currentTimeMillis());
            }

            /**
             * beat <code>value</code> for hit checking
             * @param value value for beat
             * @param currentTimeMillis current time in millis
             * @return true if hit
             */
            public boolean beat(String value, long currentTimeMillis) {
                CRC32 crc32 = new CRC32();
                byte[] bytes = value.getBytes();
                crc32.update(bytes, 0, bytes.length);

                long nowRemainder = (currentTimeMillis / 1000 / this.tickTime)
                        % this.hitTimesFactor;
                long objRemainder = crc32.getValue() % this.hitTimesFactor;

                return nowRemainder == objRemainder;
            }
        }
    }
}
