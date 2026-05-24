package com.danijelsudimac.shipmentservice.util;

import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AvroUtils {
    public static <T> byte[] serialize(T avroObject) throws IOException {
        if (avroObject == null) {
            return null;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DatumWriter<T> writer = new SpecificDatumWriter<>((Class<T>) avroObject.getClass());
        Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);

        writer.write(avroObject, encoder);
        encoder.flush();
        out.close();

        return out.toByteArray();
    }

    public static <T> T deserialize(byte[] data, Class<T> targetClass) throws IOException {
        if (data == null || data.length == 0) {
            return null;
        }

        DatumReader<T> reader = new SpecificDatumReader<>(targetClass);
        Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
        return reader.read(null, decoder);
    }
}

