package com.danijelsudimac.shipmentservice.service;

import com.danijelsudimac.shipmentservice.util.AvroUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class AvroSerializator implements PayloadSerializator {

    @Override
    public <T> byte[] serialize(T avroObject) throws IOException {
        return AvroUtils.serialize(avroObject);
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> targetClass) throws IOException {
        return AvroUtils.deserialize(data, targetClass);
    }
}
