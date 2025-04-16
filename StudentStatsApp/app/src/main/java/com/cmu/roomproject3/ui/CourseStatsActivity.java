package com.cmu.roomproject3.ui;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.cmu.roomproject3.R;

public class CourseStatsActivity extends AppCompatActivity {
    private static final String TAG = "CourseStatsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_stats);

        Long courseId = getIntent().getLongExtra("COURSE_ID", -1L);
        String courseTitle = getIntent().getStringExtra("COURSE_TITLE");
        Log.d(TAG, "Course ID: " + courseId + ", Course Title: " + courseTitle);
        setTitle(courseTitle);

        if (savedInstanceState == null) {
            CourseStatsFragment fragment = CourseStatsFragment.newInstance(courseId, courseTitle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}