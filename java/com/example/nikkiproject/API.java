package com.example.nikkiproject;

import android.media.Image;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

public interface API {

    @GET("fileUpload/")
    Call<List<Image>> getImages();                                                                      // GET request to get all images

    @GET("fileUpload/files/{filename}")                                                                 // GET request to get an image by its name
    @Streaming
    Call<ResponseBody> getImageByName(@Path("filename") String name);

//    @Multipart                                                                                          // POST request to upload an image from storage
//    @POST("detectFaces/")
//    Call<TestResult> uploadImage(@Part MultipartBody.Part partImage);
//
//    @POST("detectFaces/")
//    @FormUrlEncoded
//    Call<TestResult> uploadImage(@Field("title") String title, @Field("image") String image);
    //void upload(@Field("image") String base64, Callback<TestResult> callback);

    @POST("detectFaces/")
    Call<ReqModel> uploadImage(@Body ReqModel req);

    @POST("addPerson/")
    Call<ReqModel> addPerson(@Body ReqModel req);

    @POST("addFace/")
    Call<ReqModel> addFace(@Body ReqModel req);

    @POST("train/")
    Call<ReqModel> trainModel(@Body ReqModel req);

    @POST("delete/")
    Call<ReqModel> delete(@Body ReqModel req);

    @POST("reset/")
    Call<ReqModel> reset(@Body ReqModel req);
}
