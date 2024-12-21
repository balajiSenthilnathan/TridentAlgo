package com.trident.trident_algo.api.helper;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Map;

@Component
public class CommonServiceHelper {

    @Getter
    private static String symbol;

    @Getter
    private static long timeOut;

    @Getter
    private static long cutoff;

    @Value("${binance.websocket.future.symbol}")
    public void setSymbol(String propertySymbol) {
        symbol = propertySymbol.toUpperCase();
    }

    @Value("${binance.scheduler.timeout:5}")
    public void setTimeOut(String propertyTimeOut) {
        timeOut = Integer.parseInt(propertyTimeOut);
    }


    @Value("${binance.scheduler.price.cutoff:5}")
    public void setCutoff(String propertyCutoff) {
        cutoff = Integer.parseInt(propertyCutoff);
    }


    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> deepCopy(Map<K, V> original) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(original);
            oos.flush();
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);
            return (Map<K, V>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Error during map deep copy", e);
        }
    }
}
