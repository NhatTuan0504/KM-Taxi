package com.example.tuann.clientuser.interfaces;

/**
 * Created by truonghn on 3/9/16.
 */
public interface IResultListener<T> {
    public void onSuccess(T result);
    public void onFail(String reason);
}
