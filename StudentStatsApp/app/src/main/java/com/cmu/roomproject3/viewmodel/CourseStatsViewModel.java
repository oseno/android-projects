package com.cmu.roomproject3.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cmu.roomproject3.database.AppDatabase;
import com.cmu.roomproject3.model.*;
import com.cmu.roomproject3.model.StudentQuizCrossRef;
import com.cmu.roomproject3.model.relations.CourseWithQuizzes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseStatsViewModel extends AndroidViewModel {
    private static final String TAG = "CourseStatsViewModel";
    private final AppDatabase db;
    private final MutableLiveData<List<String>> courseStats;
    private String courseName;

    public CourseStatsViewModel(Application application) {
        super(application);
        db = AppDatabase.getDatabase(application);
        courseStats = new MutableLiveData<>();
    }

    public LiveData<List<String>> getCourseStats() {
        return courseStats;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void loadStatsForCourse(Long courseId, LifecycleOwner lifecycleOwner) {
        try {
            db.courseDao().getCourseWithQuizzes(courseId).observe(lifecycleOwner, courseWithQuizzes -> {
                try {
                    if (courseWithQuizzes == null) {
                        Log.e(TAG, "CourseWithQuizzes is null for courseId: " + courseId);
                        courseStats.setValue(new ArrayList<>());
                        return;
                    }

                    Log.d(TAG, "Course: " + courseWithQuizzes.course.getName() + ", Quiz count: " + courseWithQuizzes.quizzes.size());
                    for (Quiz quiz : courseWithQuizzes.quizzes) {
                        Log.d(TAG, "Quiz ID: " + quiz.getQuizId() + ", Name: " + quiz.getName() + ", Course ID: " + quiz.getCourseId());
                    }

                    final List<Student> studentList = new ArrayList<>();

                    db.studentDao().getStudentsByCourse(courseId).observe(lifecycleOwner, students -> {
                        try {
                            if (students == null) {
                                Log.e(TAG, "Students list is null for courseId: " + courseId);
                                studentList.clear();
                            } else {
                                studentList.clear();
                                studentList.addAll(students);
                            }

                            int maxQuizNumber = courseWithQuizzes.quizzes.size();
                            if (maxQuizNumber == 0) {
                                Log.w(TAG, "No quizzes found for courseId: " + courseId);
                                courseStats.setValue(new ArrayList<>());
                                return;
                            }

                            List<String> stats = new ArrayList<>();
                            stats.add("Course Title: " + courseName);
                            stats.add("");
                            stats.add("Student " + String.join(" ", getQuizHeaders(maxQuizNumber)));

                            Map<Integer, List<Double>> quizScoresMap = new HashMap<>();
                            for (int i = 1; i <= maxQuizNumber; i++) {
                                quizScoresMap.put(i, new ArrayList<>());
                            }

                            Map<Long, List<StudentQuizCrossRef>> allScores = new HashMap<>();
                            List<Long> quizIds = new ArrayList<>();
                            for (Quiz quiz : courseWithQuizzes.quizzes) {
                                quizIds.add(quiz.getQuizId());
                            }

                            final int[] quizzesProcessed = {0};
                            final int totalQuizzes = quizIds.size();

                            for (int i = 0; i < totalQuizzes; i++) {
                                Long quizId = quizIds.get(i);
                                final int quizIndex = i + 1;

                                db.quizDao().getScoresForQuiz(quizId).observe(lifecycleOwner, scores -> {
                                    try {
                                        allScores.put(quizId, scores != null ? scores : new ArrayList<>());
                                        Log.d(TAG, "Scores for quizId " + quizId + ": " + allScores.get(quizId).size() + " entries");
                                        for (StudentQuizCrossRef score : allScores.get(quizId)) {
                                            String studentIdStr = (score.studentId != null) ? score.studentId.toString() : "null";
                                            String quizIdStr = (score.quizId != null) ? score.quizId.toString() : "null";
                                            Log.d(TAG, "Score - Student ID: " + studentIdStr + ", Quiz ID: " + quizIdStr + ", Score: " + score.score);
                                        }

                                        quizzesProcessed[0]++;
                                        if (quizzesProcessed[0] == totalQuizzes) {
                                            try {
                                                for (Student student : studentList) {
                                                    if (student.getStudentId() == null) {
                                                        Log.e(TAG, "Student ID is null for student: " + student.getName());
                                                        continue;
                                                    }

                                                    List<String> row = new ArrayList<>();
                                                    row.add(String.valueOf(student.getStudentId()));
                                                    for (int j = 0; j < maxQuizNumber; j++) {
                                                        Long qId = quizIds.get(j);
                                                        List<StudentQuizCrossRef> scoresForQuiz = allScores.get(qId);
                                                        StudentQuizCrossRef score = scoresForQuiz.stream()
                                                                .filter(s -> s.studentId != null && student.getStudentId().equals(s.studentId))
                                                                .findFirst()
                                                                .orElse(null);
                                                        double scoreValue = (score != null) ? score.score : 0.0;
                                                        row.add(String.format("%.0f", scoreValue));
                                                        quizScoresMap.get(j + 1).add(scoreValue);
                                                    }
                                                    stats.add(String.join(" ", row));
                                                }

                                                stats.add("");
                                                stats.add("High " + getStatsRow(quizScoresMap, maxQuizNumber, "max"));
                                                stats.add("Low " + getStatsRow(quizScoresMap, maxQuizNumber, "min"));
                                                stats.add("Avg " + getStatsRow(quizScoresMap, maxQuizNumber, "avg"));
                                                courseStats.setValue(stats);
                                            } catch (Exception e) {
                                                Log.e(TAG, "Error building stats table: " + e.getMessage(), e);
                                                courseStats.setValue(new ArrayList<>());
                                            }
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error processing scores for quizId " + quizId + ": " + e.getMessage(), e);
                                        allScores.put(quizId, new ArrayList<>());
                                        quizzesProcessed[0]++;
                                        if (quizzesProcessed[0] == totalQuizzes) {
                                            courseStats.setValue(new ArrayList<>());
                                        }
                                    }
                                });
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing students for courseId " + courseId + ": " + e.getMessage(), e);
                            courseStats.setValue(new ArrayList<>());
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error loading course with quizzes for courseId " + courseId + ": " + e.getMessage(), e);
                    courseStats.setValue(new ArrayList<>());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error loading stats for courseId " + courseId + ": " + e.getMessage(), e);
            courseStats.setValue(new ArrayList<>());
        }
    }

    private String getQuizHeaders(int maxQuizNumber) {
        List<String> headers = new ArrayList<>();
        for (int i = 1; i <= maxQuizNumber; i++) {
            headers.add("Q" + i);
        }
        return String.join(" ", headers);
    }

    private String getStatsRow(Map<Integer, List<Double>> quizScoresMap, int maxQuizNumber, String type) {
        List<String> values = new ArrayList<>();
        for (int i = 1; i <= maxQuizNumber; i++) {
            List<Double> scores = quizScoresMap.get(i);
            if (scores.isEmpty()) {
                values.add("0");
                continue;
            }
            double value;
            switch (type) {
                case "max":
                    value = scores.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
                    values.add(String.format("%.0f", value));
                    break;
                case "min":
                    value = scores.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
                    values.add(String.format("%.0f", value));
                    break;
                case "avg":
                    value = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                    values.add(String.format("%.1f", value));
                    break;
                default:
                    values.add("0");
            }
        }
        return String.join(" ", values);
    }
}