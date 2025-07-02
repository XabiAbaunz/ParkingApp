package com.lksnext.ParkingXAbaunz.domain;

public interface CallbackWithData<T> {
    void onSuccess(T data);
    void onFailure(String error);
}