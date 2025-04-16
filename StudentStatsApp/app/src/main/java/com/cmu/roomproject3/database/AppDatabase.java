package com.cmu.roomproject3.database;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.cmu.roomproject3.dao.CourseDao;
import com.cmu.roomproject3.dao.QuizDao;
import com.cmu.roomproject3.dao.StudentDao;
import com.cmu.roomproject3.model.Course;
import com.cmu.roomproject3.model.CourseStudentCrossRef;
import com.cmu.roomproject3.model.Quiz;
import com.cmu.roomproject3.model.Student;
import com.cmu.roomproject3.model.StudentQuizCrossRef;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {
                Course.class,
                Quiz.class,
                Student.class,
                CourseStudentCrossRef.class,
                StudentQuizCrossRef.class
        },
        version = 1
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CourseDao courseDao();
    public abstract StudentDao studentDao();
    public abstract QuizDao quizDao();

    private static volatile AppDatabase INSTANCE;

    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "quiz_database")
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    databaseWriteExecutor.execute(() -> {
                                        AppDatabase database = getDatabase(context);
                                        seedDatabase(database);
                                    });
                                }
                            })
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static void seedDatabase(AppDatabase database) {
        try {
            CourseDao courseDao = database.courseDao();
            QuizDao quizDao = database.quizDao();
            StudentDao studentDao = database.studentDao();

            // Insert 3 courses
            Course course1 = new Course();
            course1.setName("Programming 101");
            course1.setDescription("Intro to Programming");
            Long course1Id = courseDao.insert(course1);

            Course course2 = new Course();
            course2.setName("Data Structures");
            course2.setDescription("Advanced Data Structures");
            Long course2Id = courseDao.insert(course2);

            Course course3 = new Course();
            course3.setName("DPSD");
            course3.setDescription("Design Patterns for SmartPhone Development");
            Long course3Id = courseDao.insert(course3);

            // Insert 5 quizzes for each course and store quiz IDs
            List<Long> quizIdsCourse1 = new ArrayList<>();
            List<Long> quizIdsCourse2 = new ArrayList<>();
            List<Long> quizIdsCourse3 = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                Quiz quiz1 = new Quiz();
                quiz1.setCourseId(course1Id);
                quiz1.setName("Quiz " + i);
                Long quiz1Id = quizDao.insert(quiz1);
                quizIdsCourse1.add(quiz1Id);

                Quiz quiz2 = new Quiz();
                quiz2.setCourseId(course2Id);
                quiz2.setName("Quiz " + i);
                Long quiz2Id = quizDao.insert(quiz2);
                quizIdsCourse2.add(quiz2Id);

                Quiz quiz3 = new Quiz();
                quiz3.setCourseId(course3Id);
                quiz3.setName("Quiz " + i);
                Long quiz3Id = quizDao.insert(quiz3);
                quizIdsCourse3.add(quiz3Id);
            }

            // Insert students with 4-digit IDs
            Student student1 = new Student();
            student1.setStudentId(2002L);
            student1.setName("Student 2002");
            Long student1Id = studentDao.insert(student1);

            Student student2 = new Student();
            student2.setStudentId(1999L);
            student2.setName("Student 1999");
            Long student2Id = studentDao.insert(student2);

            Student student3 = new Student();
            student3.setStudentId(2001L);
            student3.setName("Student 2001");
            Long student3Id = studentDao.insert(student3);

            // Enroll students in all courses
            // Programming 101
            studentDao.insertCourseStudentCrossRef(new CourseStudentCrossRef(course1Id, student1Id));
            studentDao.insertCourseStudentCrossRef(new CourseStudentCrossRef(course1Id, student2Id));
            studentDao.insertCourseStudentCrossRef(new CourseStudentCrossRef(course1Id, student3Id));

            // Data Structures
            studentDao.insertCourseStudentCrossRef(new CourseStudentCrossRef(course2Id, student1Id));
            studentDao.insertCourseStudentCrossRef(new CourseStudentCrossRef(course2Id, student2Id));
            studentDao.insertCourseStudentCrossRef(new CourseStudentCrossRef(course2Id, student3Id));

            // DPSD
            studentDao.insertCourseStudentCrossRef(new CourseStudentCrossRef(course3Id, student1Id));
            studentDao.insertCourseStudentCrossRef(new CourseStudentCrossRef(course3Id, student2Id));
            studentDao.insertCourseStudentCrossRef(new CourseStudentCrossRef(course3Id, student3Id));

            // Insert scores for Programming 101 using correct quiz IDs
            // Student 2002
            double[] scores1 = {78, 80, 84, 90, 86};
            for (int i = 0; i < 5; i++) {
                StudentQuizCrossRef score = new StudentQuizCrossRef();
                score.studentId = student1Id;
                score.quizId = quizIdsCourse1.get(i);
                score.score = scores1[i];
                quizDao.insertStudentQuizCrossRef(score);
            }
            // Student 1999
            double[] scores2 = {60, 70, 80, 90, 89};
            for (int i = 0; i < 5; i++) {
                StudentQuizCrossRef score = new StudentQuizCrossRef();
                score.studentId = student2Id;
                score.quizId = quizIdsCourse1.get(i);
                score.score = scores2[i];
                quizDao.insertStudentQuizCrossRef(score);
            }
            // Student 2001
            double[] scores3 = {70, 80, 90, 87, 71};
            for (int i = 0; i < 5; i++) {
                StudentQuizCrossRef score = new StudentQuizCrossRef();
                score.studentId = student3Id;
                score.quizId = quizIdsCourse1.get(i);
                score.score = scores3[i];
                quizDao.insertStudentQuizCrossRef(score);
            }

            // Insert random scores for Data Structures
            for (int i = 0; i < 5; i++) {
                // Student 2002
                StudentQuizCrossRef score1 = new StudentQuizCrossRef();
                score1.studentId = student1Id;
                score1.quizId = quizIdsCourse2.get(i);
                score1.score = 70 + (Math.random() * 30); // This will generate a random score
                quizDao.insertStudentQuizCrossRef(score1);

                // Student 1999
                StudentQuizCrossRef score2 = new StudentQuizCrossRef();
                score2.studentId = student2Id;
                score2.quizId = quizIdsCourse2.get(i);
                score2.score = 60 + (Math.random() * 35);
                quizDao.insertStudentQuizCrossRef(score2);

                // Student 2001
                StudentQuizCrossRef score3 = new StudentQuizCrossRef();
                score3.studentId = student3Id;
                score3.quizId = quizIdsCourse2.get(i);
                score3.score = 50 + (Math.random() * 40);
                quizDao.insertStudentQuizCrossRef(score3);
            }

            // Insert random scores for DPSD
            for (int i = 0; i < 5; i++) {
                // Student 2002
                StudentQuizCrossRef score1 = new StudentQuizCrossRef();
                score1.studentId = student1Id;
                score1.quizId = quizIdsCourse3.get(i);
                score1.score = 60 + (Math.random() * 40);
                quizDao.insertStudentQuizCrossRef(score1);

                // Student 1999
                StudentQuizCrossRef score2 = new StudentQuizCrossRef();
                score2.studentId = student2Id;
                score2.quizId = quizIdsCourse3.get(i);
                score2.score = 60 + (Math.random() * 40);
                quizDao.insertStudentQuizCrossRef(score2);

                // Student 2001
                StudentQuizCrossRef score3 = new StudentQuizCrossRef();
                score3.studentId = student3Id;
                score3.quizId = quizIdsCourse3.get(i);
                score3.score = 60 + (Math.random() * 40);
                quizDao.insertStudentQuizCrossRef(score3);
            }
        } catch (Exception e) {
            Log.e("AppDatabase", "Error seeding database: " + e.getMessage());
        }
    }
}