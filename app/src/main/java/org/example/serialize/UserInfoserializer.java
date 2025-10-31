package org.example.serialize;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;
import org.example.model.UserInfoDto;

import java.util.Map;

public class UserInfoserializer implements Serializer<UserInfoDto> {

    //no need
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        Serializer.super.configure(configs, isKey);
    }

    @Override
    public byte[] serialize(String s, UserInfoDto userInfoDto) {
        byte[] retVal = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            retVal = objectMapper.writeValueAsString(userInfoDto).getBytes();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        return retVal;
    }


    @Override
    public byte[] serialize(String topic, Headers headers, UserInfoDto data) {
        return Serializer.super.serialize(topic, headers, data);
    }

    @Override
    public void close() {
        Serializer.super.close();
    }
}
