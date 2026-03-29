package com.example.pdfconverterjava.data.remote;

import com.example.pdfconverterjava.data.model.CreateJobResponse;
import com.example.pdfconverterjava.data.model.JobStatusResponse;
import com.example.pdfconverterjava.data.model.ListJobsResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface PdfApiService {

    // Tek dosya ile job oluşturma
    @Multipart
    @POST("create_job.php")
    Call<CreateJobResponse> createSingleFileJob(
            @Part("job_type") RequestBody jobType,
            @Part("user_id") RequestBody userId,
            @Part MultipartBody.Part file
    );

    // Çoklu dosya ile job oluşturma
    @Multipart
    @POST("create_job.php")
    Call<CreateJobResponse> createMultiFileJob(
            @Part("job_type") RequestBody jobType,
            @Part("user_id") RequestBody userId,
            @Part List<MultipartBody.Part> files
    );

    // Job durumunu getir
    @GET("job_status.php")
    Call<JobStatusResponse> getJobStatus(
            @Query("job_id") int jobId
    );

    // Kullanıcının geçmiş job listesini getir
    @GET("list_jobs.php")
    Call<ListJobsResponse> listJobs(
            @Query("user_id") int userId
    );
}
