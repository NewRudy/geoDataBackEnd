package cn.geodata.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JudgeUtils {
    /**
     * @description:
     * @author: Tian
     * @date: 2022/1/17 21:19
     * @param name
     * @param value
     * @return: void
     */
    public static void isNullString(String name, String value) throws Exception{
        try{
            if(value == null || value == "") {
                throw new Error(name + " is null string.");
            }
        } catch (Exception err) {
            throw err;
        }
    }
}
