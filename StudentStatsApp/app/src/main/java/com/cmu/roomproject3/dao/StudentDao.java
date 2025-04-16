package com.cmu.roomproject3.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.cmu.roomproject3.model.CourseStudentCrossRef;
import com.cmu.roomproject3.model.Student;
import com.cmu.roomproject3.model.relations.StudentWithCourses;

import java.util.List;

@Dao
public interface StudentDao {
    @Insert
    Long insert(Student student);

    @Insert
    void insertCourseStudentCrossRef(CourseStudentCrossRef crossRef);

    @Transaction
    @Query("SELECT * FROM Student WHERE studentId IN " +
            "(SELECT studentId FROM CourseStudentCrossRef WHERE courseId = :courseId)")
    LiveData<List<Student>> getStudentsByCourse(Long courseId);
}