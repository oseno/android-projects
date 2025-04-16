package com.cmu.roomproject3.model.relations;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.cmu.roomproject3.model.Course;
import com.cmu.roomproject3.model.CourseStudentCrossRef;
import com.cmu.roomproject3.model.Student;

import java.util.List;

public class CourseWithStudents
{
    @Embedded
    public Course course;

    @Relation(
            parentColumn = "courseId",
            entityColumn = "studentId",
            associateBy = @Junction(CourseStudentCrossRef.class)
    )
    public List<Student> students;
}
