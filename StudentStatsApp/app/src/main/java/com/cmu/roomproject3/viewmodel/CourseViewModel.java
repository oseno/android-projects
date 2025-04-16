package com.cmu.roomproject3.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.cmu.roomproject3.database.AppDatabase;
import com.cmu.roomproject3.model.Course;

import java.util.List;

public class CourseViewModel extends AndroidViewModel
{
    private final LiveData<List<Course>> allCourses;

    public CourseViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        allCourses = db.courseDao().getAllCourses();
    }

    public LiveData<List<Course>> getAllCourses() {
        return allCourses;
    }

    public void insert(Course course) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase.getDatabase(getApplication()).courseDao().insert(course);
        });
    }
}
