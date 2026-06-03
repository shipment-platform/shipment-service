package com.danijelsudimac.shipmentservice.service;

import java.io.IOException;

public interface PayloadSerializator {
    <T> byte[] serialize(T avroObject) throws IOException;
    <T> T deserialize(byte[] data, Class<T> targetClass) throws IOException;
}
