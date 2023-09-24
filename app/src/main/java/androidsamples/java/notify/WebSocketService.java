package androidsamples.java.notify;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketService extends Service {
    private static final String TAG = "WebSocketService";
    private WebSocket webSocket;

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "My Notification Channel";
            String description = "Channel Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("channel_id", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        // Initialize and set up your WebSocket connection here
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("ws://129.159.21.84:3002").build();
        Log.d(TAG, "WebSocket creation started");

        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                Log.d(TAG, "WebSocket connection opened");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d(TAG, "Received message: " + text);
                // Handle incoming messages here and trigger notifications.
                showNotification(text); // Implement this method to show a notification.
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                // Handle binary messages if needed
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                // WebSocket is closing
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
                Log.v(TAG, "OnFailure: " + response + ", " + t);
                super.onFailure(webSocket, t, response);
            }
        };

        webSocket = client.newWebSocket(request, listener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start the WebSocket service
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void showNotification(String message) {
        // Implement this method to create and show a notification
        try {
            JSONObject obj = new JSONObject(message);
            String name = (String) obj.get("name");
            String text = (String) obj.get("message");

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_id")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("New Message from " + name)
                    .setContentText(text)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_ALL); // Set the notification priority

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

            // notificationId is a unique int for each notification that you must define
            int notificationId = 1;
            notificationManager.notify(notificationId, builder.build());

        }catch (JSONException err){
            Log.d("Error", err.toString());
        }
    }
}

