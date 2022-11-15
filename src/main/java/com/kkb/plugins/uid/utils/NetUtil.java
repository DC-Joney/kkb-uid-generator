/*
 * Copyright
 */
package com.kkb.plugins.uid.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * NetUtil
 *
 * @author ztkool
 * @since
 */
public abstract class NetUtil {

    public static InetAddress localAddress;

    static {
        try {
            localAddress = getLocalInetAddress();
        } catch (SocketException e) {
            throw new RuntimeException("fail to get local ip.");
        }
    }

    /**
     * 获取第一个已验证的 ip 地址（公共 ip 地址和 LAN ip 地址）
     *
     * @return
     * @throws SocketException
     */
    public static InetAddress getLocalInetAddress() throws SocketException {
        Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
        while (enumeration.hasMoreElements()) {
            NetworkInterface networkInterface = enumeration.nextElement();
            if (networkInterface.isLoopback()) {
                continue;
            }
            Enumeration<InetAddress> addressEnumeration = networkInterface.getInetAddresses();
            while (addressEnumeration.hasMoreElements()) {
                InetAddress address = addressEnumeration.nextElement();
                if (address.isLinkLocalAddress() || address.isLoopbackAddress() || address.isAnyLocalAddress()) {
                    continue;
                }
                return address;
            }
        }
        throw new RuntimeException("No validated local address!");
    }

    /**
     * 获取  host address
     *
     * @return
     */
    public static String getLocalAddress() {
        return localAddress.getHostAddress();
    }

}
