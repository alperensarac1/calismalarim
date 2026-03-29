package com.example.pdfconverterjava.data.repository;


import com.example.pdfconverterjava.data.model.CreateJobResponse;
import com.example.pdfconverterjava.data.model.JobStatusResponse;
import com.example.pdfconverterjava.data.model.ListJobsResponse;
import com.example.pdfconverterjava.data.remote.PdfApiService;
import com.example.pdfconverterjava.data.remote.RetrofitClient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

public class PdfRepository {

    private final PdfApiService apiService;

    public PdfRepository() {
        apiService = RetrofitClient.getApiService();
    }

    /**
     * Tek dosyalı job oluşturur.
     * Örnek:
     * - jpg_to_pdf
     * - pdf_to_word
     * - word_to_pdf
     */
    public Call<CreateJobResponse> createSingleFileJob(int userId, String jobType, File file) {
        RequestBody jobTypeBody =
                RequestBody.create(jobType, MediaType.parse("text/plain"));

        RequestBody userIdBody =
                RequestBody.create(String.valueOf(userId), MediaType.parse("text/plain"));

        RequestBody fileRequestBody =
                RequestBody.create(file, MediaType.parse("application/octet-stream"));

        MultipartBody.Part filePart =
                MultipartBody.Part.createFormData("file", file.getName(), fileRequestBody);

        return apiService.createSingleFileJob(jobTypeBody, userIdBody, filePart);
    }

    /**
     * Çoklu dosyalı job oluşturur.
     * Örnek:
     * - pdf_merge
     */
    public Call<CreateJobResponse> createMultiFileJob(int userId, String jobType, List<File> files) {
        RequestBody jobTypeBody =
                RequestBody.create(jobType, MediaType.parse("text/plain"));

        RequestBody userIdBody =
                RequestBody.create(String.valueOf(userId), MediaType.parse("text/plain"));

        List<MultipartBody.Part> fileParts = new ArrayList<>();

        for (File file : files) {
            RequestBody requestBody =
                    RequestBody.create(file, MediaType.parse("application/pdf"));

            MultipartBody.Part part =
                    MultipartBody.Part.createFormData("files[]", file.getName(), requestBody);

            fileParts.add(part);
        }

        return apiService.createMultiFileJob(jobTypeBody, userIdBody, fileParts);
    }

    /**
     * Job durumunu getirir.
     */
    public Call<JobStatusResponse> getJobStatus(int jobId) {
        return apiService.getJobStatus(jobId);
    }

    /**
     * Kullanıcının geçmiş job listesini getirir.
     */
    public Call<ListJobsResponse> listJobs(int userId) {
        return apiService.listJobs(userId);
    }
}
