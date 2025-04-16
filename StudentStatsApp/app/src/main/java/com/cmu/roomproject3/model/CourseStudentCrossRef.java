package com.cmu.roomproject3.model;

import androidx.room.Entity;

import org.jetbrains.annotations.NotNull;

@Entity(primaryKeys = {"courseId", "studentId"})
public class CourseStudentCrossRef
{
    @NotNull
    public Long courseId;
    @NotNull
    public Long studentId;

    public CourseStudentCrossRef() {}

    public CourseStudentCrossRef(Long cId, Long sId)
    {
        courseId = cId;
        studentId = sId;
    }
}
