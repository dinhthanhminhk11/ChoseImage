package com.example.choseimage;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.choseimage.adapter.image.impl.GlideAdapter;
import com.example.choseimage.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private ImageAdapter imageAdapter;
    private ActivityMainBinding binding;
    private ActivityResultLauncher<Intent> startForResultCallback = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    path = data.getParcelableArrayListExtra(FishBun.INTENT_PATH);
                    if (path == null) {
                        path = new ArrayList<>();
                    }
                    imageAdapter.changePath(path);
                    Toast.makeText(MainActivity.this, "ActivityResultLauncher size " + path.size(), Toast.LENGTH_SHORT).show();

                    for (Uri uri : path) {
                        MediaManager.get().upload(uri).callback(new UploadCallback() {
                            @Override
                            public void onStart(String requestId) {
                                Log.d(TAG, "onStart " + "started");
                            }

                            @Override
                            public void onProgress(String requestId, long bytes, long totalBytes) {
                                Log.d(TAG, "onStart " + uri);
                            }

                            // tra ve link img cloudy
                            @Override
                            public void onSuccess(String requestId, Map resultData) {
                                String linkImageAvt = resultData.get("url").toString();
                                Log.d("MInhLink", "linkImageAvt " + linkImageAvt);
                            }

                            @Override
                            public void onError(String requestId, ErrorInfo error) {
                                Log.d(TAG, "onStart " + error);
                            }

                            @Override
                            public void onReschedule(String requestId, ErrorInfo error) {
                                Log.d(TAG, "onStart " + error);
                            }
                        }).dispatch();
                    }

                }
            }
        }
    });
    private ArrayList<Uri> path = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initConfig();
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imageAdapter = new ImageAdapter(this, new ImageController(binding.imgMain), path);
        binding.recyclerview.setAdapter(imageAdapter);
        Toast.makeText(this, "size " + path.size(), Toast.LENGTH_SHORT).show();

        binding.clickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FishBun.with(MainActivity.this).setImageAdapter(new GlideAdapter()).setSelectedImages(path).startAlbumWithActivityResultCallback(startForResultCallback);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FishBun.FISHBUN_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                path = data.getParcelableArrayListExtra(FishBun.INTENT_PATH);
                if (path == null) {
                    path = new ArrayList<>();
                }
                imageAdapter.changePath(path);
                Toast.makeText(this, "onActivityResult size " + path.size(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initConfig() {
        try {
            Map config = new HashMap();
            config.put("cloud_name", "dl4lo9r1y");
            config.put("api_key", "477111637363519");
            config.put("api_secret", "q4gJ0kJOSRSDHKujijgYaSMhfJY");
            MediaManager.init(this, config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}