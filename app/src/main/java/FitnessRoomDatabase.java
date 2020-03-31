import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.fitnessapp.StepCount;
import com.example.fitnessapp.StepCountDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {StepCount.class}, version = 1, exportSchema = false)
public abstract class FitnessRoomDatabase extends RoomDatabase {

    public abstract StepCountDao stepCountDao();

    private static volatile FitnessRoomDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static FitnessRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (FitnessRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            FitnessRoomDatabase.class, "fitness_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
