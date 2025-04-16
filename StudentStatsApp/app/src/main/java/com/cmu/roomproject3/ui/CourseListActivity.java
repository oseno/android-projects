package com.cmu.roomproject3.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cmu.roomproject3.R;
import com.cmu.roomproject3.viewmodel.CourseViewModel;

public class CourseListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);

        RecyclerView recyclerView = findViewById(R.id.course_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        CourseAdapter adapter = new CourseAdapter(course -> {
            Intent intent = new Intent(this, CourseStatsActivity.class);
            intent.putExtra("COURSE_ID", course.getCourseId());
            intent.putExtra("COURSE_TITLE", course.getName());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        CourseViewModel viewModel = new ViewModelProvider(this).get(CourseViewModel.class);
        viewModel.getAllCourses().observe(this, adapter::setCourses);
    }
}