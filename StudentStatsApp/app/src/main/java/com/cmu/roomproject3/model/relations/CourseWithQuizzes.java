package com.cmu.roomproject3.model.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.cmu.roomproject3.model.Course;
import com.cmu.roomproject3.model.Quiz;

import java.util.List;

public class CourseWithQuizzes
{
    @Embedded
    public Course course;

    @Relation(
            parentColumn = "courseId",
            entityColumn = "courseId"
    )
    public List<Quiz> quizzes;
}
