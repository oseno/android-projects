package com.cmu.roomproject3.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.cmu.roomproject3.model.Quiz;
import com.cmu.roomproject3.model.StudentQuizCrossRef;
import com.cmu.roomproject3.model.relations.QuizWithStudents;

import java.util.List;

@Dao
public interface QuizDao {
    @Insert
    Long insert(Quiz quiz);

    @Update
    void update(Quiz quiz);

    @Query("SELECT * FROM Quiz WHERE courseId = :courseId")
    LiveData<List<Quiz>> getQuizzesByCourse(Long courseId);

    @Transaction
    @Query("SELECT * FROM Quiz WHERE quizId = :quizId")
    LiveData<QuizWithStudents> getQuizWithStudents(Long quizId);

    // Change to return LiveData for asynchronous access
    @Query("SELECT * FROM StudentQuizCrossRef WHERE quizId = :quizId")
    LiveData<List<StudentQuizCrossRef>> getScoresForQuiz(Long quizId);

    @Insert
    void insertStudentQuizCrossRef(StudentQuizCrossRef crossRef);
}