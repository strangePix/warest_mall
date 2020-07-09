package com.warest.mall.common;



import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;

/**
 * Created by geely
 */
//@JsonSerialize(include =  JsonSerialize.Inclusion.NON_NULL)
@JsonInclude(JsonInclude.Include.NON_NULL)
//保证序列化json的时候,如果是null的属性不会序列化，也就是不会返回给前端键值对
public class ResponseEntity<T> implements Serializable {

//    状态码   1表示失败  0表示成功  get_information有一个10
    private int status;
//    状态信息  一般为1时才有
    private String msg;
//    数据  一般为0时才有
    private T data;

    //方便调用，简明通用
    private ResponseEntity(int status){
        this.status = status;
    }
    //如果泛型为String类型，可能与下一个构造方法冲突，通过公共方法规避
    private ResponseEntity(int status, T data){
        this.status = status;
        this.data = data;
    }
    private ResponseEntity(int status, String msg){
        this.status = status;
        this.msg = msg;
    }
    private ResponseEntity(int status, String msg, T data){
        this.status = status;
        this.msg = msg;
        this.data = data;
    }


    @JsonIgnore
    //使之不在json序列化结果当中（不会返回给前端）  判断是否状态成功，用枚举类包含常量
    public boolean isSuccess(){
        return this.status == ResponseCode.SUCCESS.getCode();
    }

    public int getStatus(){
        return status;
    }
    public T getData(){
        return data;
    }
    public String getMsg(){
        return msg;
    }


    //    静态开放方法
    //一个成功的响应，不带数据
    public static <T> ResponseEntity<T> createBySuccess(){
        return new ResponseEntity<T>(ResponseCode.SUCCESS.getCode());
    }
    //一个成功响应，带消息
    public static <T> ResponseEntity<T> createBySuccessMessage(String msg){
        return new ResponseEntity<T>(ResponseCode.SUCCESS.getCode(),msg);
    }
    //一个成功的响应，带数据，传的是T，只会调用T的构造方法，这样就区分了
    public static <T> ResponseEntity<T> createBySuccess(T data){
        return new ResponseEntity<T>(ResponseCode.SUCCESS.getCode(),data);
    }
    //一个成功响应，带数据和消息
    public static <T> ResponseEntity<T> createBySuccess(String msg, T data){
        return new ResponseEntity<T>(ResponseCode.SUCCESS.getCode(),msg,data);
    }

    //失败响应，自带响应码消息，基本上就是error
    public static <T> ResponseEntity<T> createByError(){
        return new ResponseEntity<T>(ResponseCode.ERROR.getCode(),ResponseCode.ERROR.getDesc());
    }

    //带注明失败消息的失败响应
    public static <T> ResponseEntity<T> createByErrorMessage(String errorMessage){
        return new ResponseEntity<T>(ResponseCode.ERROR.getCode(),errorMessage);
    }
    //失败响应，响应码自定义
    public static <T> ResponseEntity<T> createByErrorCodeMessage(int errorCode, String errorMessage){
        return new ResponseEntity<T>(errorCode,errorMessage);
    }


}
