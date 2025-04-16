package com.cmu.roomproject3.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.cmu.roomproject3.database.AppDatabase;
import com.cmu.roomproject3.model.Quiz;

import java.util.List;

public class QuizViewModel extends AndroidViewModel
{
    private final AppDatabase db;

    public QuizViewModel(Application application) {
        super(application);
        db = AppDatabase.getDatabase(application);
    }

    public LiveData<List<Quiz>> getQuizzesByCourse(Long courseId) {
        return db.quizDao().getQuizzesByCourse(courseId);
    }

    public void insert(Quiz quiz) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.quizDao().insert(quiz);
        });
    }
}
