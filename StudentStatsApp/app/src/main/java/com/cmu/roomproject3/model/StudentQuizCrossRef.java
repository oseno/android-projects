package com.cmu.roomproject3.model;

import androidx.room.Entity;

import org.jetbrains.annotations.NotNull;

@Entity(primaryKeys = {"studentId", "quizId"})
public class StudentQuizCrossRef
{
    @NotNull
    public Long studentId;
    @NotNull
    public Long quizId;
    public double score;
}
