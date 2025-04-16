package com.cmu.roomproject3.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Quiz {
    @PrimaryKey(autoGenerate = true)
    private Long quizId;
    private Long courseId;
    private String name;

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long cId) {
        this.courseId = cId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}