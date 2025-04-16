package com.cmu.roomproject3.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Course
{
    @PrimaryKey(autoGenerate = true)
    private Long courseId;
    private String name;
    private String description;

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long id) {
        this.courseId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
