package com.example.pdfconverterkotlin.ui.jobs

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pdfconverterkotlin.R
import com.example.pdfconverterkotlin.ui.MainViewModel
import com.example.pdfconverterkotlin.ui.adapter.JobHistoryAdapter

class JobsActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: JobHistoryAdapter

    private lateinit var recyclerJobs: RecyclerView
    private lateinit var progressBarJobs: ProgressBar
    private lateinit var tvEmpty: TextView

    // Şimdilik sabit user id
    private val userId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jobs)

        initViews()
        initRecycler()
        initViewModel()
        observeJobs()

        loadData()
    }

    private fun initViews() {
        recyclerJobs = findViewById(R.id.recyclerJobs)
        progressBarJobs = findViewById(R.id.progressBarJobs)
        tvEmpty = findViewById(R.id.tvEmpty)
    }

    private fun initRecycler() {
        adapter = JobHistoryAdapter()

        recyclerJobs.layoutManager = LinearLayoutManager(this)
        recyclerJobs.adapter = adapter
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
    }

    private fun observeJobs() {
        progressBarJobs.visibility = View.VISIBLE

        viewModel.jobList.observe(this) { jobs ->
            progressBarJobs.visibility = View.GONE

            adapter.submitList(jobs)

            tvEmpty.visibility = if (jobs.isEmpty()) View.VISIBLE else View.GONE
            recyclerJobs.visibility = if (jobs.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun loadData() {
        viewModel.loadJobs(userId)
    }
}