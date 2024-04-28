package cn.xlibs.lib4j.core.utility;

import java.util.Objects;
import java.util.regex.Pattern;

import static cn.xlibs.lib4j.core.support.Constants.*;

/**
 * IPv4 Utils
 *
 * @author Fury
 * @since 2020-12-28
 *
 * All rights Reserved.
 */
public final class IPv4Utils {
    private IPv4Utils() {}
    public static final String INVALID_IP = "Invalid IPv4 Address";
    private static final String IP_RANGE = "([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])";
    private static final String C_PRIVATE_IP_SECOND_RANGE = "(1[6-9]|2[0-9]|3[01])";
    private static final String DOT = "\\.";
    private static final String A_PRIVATE_IP_REG = "^10" + DOT + IP_RANGE + DOT + IP_RANGE + DOT + IP_RANGE + "$";
    private static final String B_PRIVATE_IP_REG = "^172\\." + C_PRIVATE_IP_SECOND_RANGE + DOT + IP_RANGE + DOT + IP_RANGE + "$";
    private static final String C_PRIVATE_IP_REG = "^192\\.168\\." + IP_RANGE + DOT + IP_RANGE + "$";
    private static final String[] PRIVATE_IP_REG = {
            A_PRIVATE_IP_REG, B_PRIVATE_IP_REG, C_PRIVATE_IP_REG
    };
    public static final String LOOP_IP = "127.0.0.1";
    public static final String LOCALHOST = "localhost";

    public static boolean isIPv4Address(String ip) {
        return Objects.nonNull(ip) && InetAddressUtils.isIPv4Address(ip);
    }

    public static boolean isLocalIp(String ip) {
        String[] localIps = {LOOP_IP, LOCALHOST};
        for (String localIp : localIps) {
            if (localIp.equals(ip)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPrivateIp(String ip) {
        if (isLocalIp(ip)) {
            return true;
        }

        if (Objects.nonNull(ip)) {
            for (String regex : PRIVATE_IP_REG) {
                if (ip.matches(regex)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 判断一个IP是否是一个合法IP
     * @param ipv4Str IP
     * @return 是或否
     */
    public static boolean isValid(String ipv4Str) {
        try {
            Ipv4.of(ipv4Str);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * 校验两个IP或者网段IP是否匹配
     * @param ipv4Str 五类IP或者CIDR IP
     * @param otherIpv4Str 五类IP或者CIDR IP
     * @return match or not
     */
    public static boolean match(String ipv4Str, String otherIpv4Str) {
        try {
            return Ipv4.of(ipv4Str).match(otherIpv4Str);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 校验两个IP或者网段IP是否匹配
     * @param cidrIpv4Str CIDR IP
     * @param ipv4Str 五类IP或者CIDR IP
     * @return match or not
     */
    public static boolean contains(String cidrIpv4Str, String ipv4Str) {
        try {
            return Ipv4.of(cidrIpv4Str).contains(ipv4Str);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Ipv4 实例 */
    public static class Ipv4 {
        /** 原始的ABCDE类IP或者CIDR IP */
        private final String original;

        /** 从原始串解析出来的.号分割的IP地址 */
        private String ipv4Str;

        /** 从原始串解析出来的网络号掩码长度 */
        private String netPart;

        /** 从IP字符串解析到的32位IP地址 */
        private int ipv4Int;

        /** 从原始串解析出来的网络号掩码, 仅在输入的是网段时才有 */
        private int netMask;

        /** 输入是否是一个CIDR表示 */
        private boolean isCidr;

        private Ipv4(String ipv4Str) {
            this.original = ipv4Str;
        }

        /**
         * 解析IP构建 Ipv4
         * @return Ipv4 实例
         */
        private Ipv4 parse() {
            validateParts(this.original, DOT, FOUR);

            this.isCidr = this.original.contains(SLASH);
            if (this.isCidr) {
                validateParts(this.original, SLASH, TWO);
                final String[] twoParts = this.original.split(SLASH);

                this.ipv4Str = twoParts[0];
                this.netPart = twoParts[1];
                this.netMask = 0xFFFFFFFF << (THIRTY_TWO - Integer.parseInt(this.netPart));
            } else {
                this.ipv4Str = this.original;
            }
            this.ipv4Int = parseIpv4(this.ipv4Str);

            return this;
        }

        /**
         * 是否匹配上另一个 IP
         * @param ipv4 Ipv4
         * @param containMode 是否包含关系模式
         * @return 匹配或不匹配
         */
        private boolean match(Ipv4 ipv4, boolean containMode) {
            if (this.isCidr) {
                if (ipv4.isCidr) {
                    return containMode ? this.netMask == ipv4.netMask
                                       : this.original.equalsIgnoreCase(ipv4.original);
                } else {
                    return containMode ? (this.ipv4Int & this.netMask) == (ipv4.ipv4Int & this.netMask)
                                       : this.ipv4Int == ipv4.ipv4Int;
                }
            }

            return this.ipv4Int == ipv4.ipv4Int;
        }

        /**
         * 是否匹配上一个IP
         * @param ip 五类IP或者CIDR IP
         * @return 匹配或不匹配
         */
        public boolean match(String ip) {
            return this.match(Ipv4.of(ip), false);
        }

        /**
         * 是否匹配上一个IP
         * @param ip IP
         * @return 匹配或不匹配
         */
        public boolean contains(String ip) {
            return this.match(Ipv4.of(ip), true);
        }

        @Override
        public String toString() {
            return "original: " + this.original + "\n"
                + "ipv4: " + ipv4Str + "\n"
                + "ipv4Int: " + Integer.toBinaryString(ipv4Int) + "\n"
                + "netPart: " + netPart + "\n"
                + "netMask: " + Integer.toBinaryString(netMask) + "\n"
                + "isCIDR: " + isCidr + "\n";
        }

        /**
         * 构造一个ProtocolV4
         * @param ipv4 ipv4
         * @return Ipv4
         */
        public static Ipv4 of(String ipv4) {
            return new Ipv4(Objects.requireNonNull(ipv4)).parse();
        }

        /**
         * 校验一个字符串分割后是否具有指定数量的部分
         * @param value 待分割字符串
         * @param delimiter 分割符
         * @param expectedParts 期望的分割数量
         */
        private static void validateParts(String value, String delimiter, int expectedParts) {
            boolean empty = Objects.isNull(value) || value.isEmpty()
                || Objects.isNull(delimiter) || delimiter.isEmpty();
            if (empty || splitParts(value, delimiter).length != expectedParts) {
                throw new IllegalStateException(INVALID_IP);
            }
        }

        /**
         * 分割字符串
         * @param value 待分割字符串
         * @param delimiter 分割符
         * @return 分割结果字符串数组
         */
        private static String[] splitParts(String value, String delimiter) {
            return value.split(delimiter);
        }

        /**
         * 把4段的IP字符串转换为32位数字IP
         * @param ip 4段IP字符串
         * @return 32位数字IP
         */
        private static int parseIpv4(String ip) {
            String[] fourParts;
            if (Objects.isNull(ip) || ip.isEmpty()
                || (fourParts = splitParts(ip, DOT)).length != FOUR) {
                throw new IllegalArgumentException(INVALID_IP + " " + ip);
            }

            int ipv4 = 0;
            for (String part : fourParts) {
                int val = Integer.parseInt(part);
                ipv4 = ((ipv4 << EIGHT) | val);
            }
            return ipv4;
        }
    }

    private static class InetAddressUtils {
        private static final String IPV4_BASIC_PATTERN_STRING = "(([1-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){1}(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){2}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])";
        private static final Pattern IPV4_PATTERN = Pattern.compile("^(([1-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){1}(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){2}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");
        private static final Pattern IPV4_MAPPED_IPV6_PATTERN = Pattern.compile("^::[fF]{4}:(([1-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){1}(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){2}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");
        private static final Pattern IPV6_STD_PATTERN = Pattern.compile("^[0-9a-fA-F]{1,4}(:[0-9a-fA-F]{1,4}){7}$");
        private static final Pattern IPV6_HEX_COMPRESSED_PATTERN = Pattern.compile("^(([0-9A-Fa-f]{1,4}(:[0-9A-Fa-f]{1,4}){0,5})?)::(([0-9A-Fa-f]{1,4}(:[0-9A-Fa-f]{1,4}){0,5})?)$");
        private static final char COLON_CHAR = ':';
        private static final int MAX_COLON_COUNT = 7;

        private InetAddressUtils() {
        }

        public static boolean isIPv4Address(String input) {
            return IPV4_PATTERN.matcher(input).matches();
        }

        public static boolean isIPv4MappedIPv64Address(String input) {
            return IPV4_MAPPED_IPV6_PATTERN.matcher(input).matches();
        }

        public static boolean isIPv6StdAddress(String input) {
            return IPV6_STD_PATTERN.matcher(input).matches();
        }

        public static boolean isIPv6HexCompressedAddress(String input) {
            int colonCount = 0;

            for(int i = 0; i < input.length(); ++i) {
                if (input.charAt(i) == ':') {
                    ++colonCount;
                }
            }

            return colonCount <= 7 && IPV6_HEX_COMPRESSED_PATTERN.matcher(input).matches();
        }

        public static boolean isIPv6Address(String input) {
            return isIPv6StdAddress(input) || isIPv6HexCompressedAddress(input);
        }
    }
}
