package cn.geodata.utils.resultUtils;

import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;

public interface StatusCode extends Serializable {
    @JsonValue
        //序列化时只显示code，message以BaseResponse中的为准
    int code();

    String message();
}
