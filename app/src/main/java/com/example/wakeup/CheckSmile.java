package com.example.wakeup;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import java.util.Calendar;
import java.util.List;

public class CheckSmile extends Fragment implements View.OnClickListener{

    private Button open_camera_button;
    private ImageView user_photo_imageView;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    private Bitmap bitmap;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_check_smile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        open_camera_button = view.findViewById(R.id.open_camera_button);
        user_photo_imageView = view.findViewById(R.id.user_photo_imageView);
        open_camera_button.setOnClickListener(this);

        activityForResult();
    }

    private void activityForResult(){

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        if(result.getResultCode() == RESULT_OK && result.getData() != null){
                            Bundle bundle = result.getData().getExtras();
                             bitmap = (Bitmap) bundle.get("data");
                            user_photo_imageView.setImageBitmap(bitmap);
                            detectSmile();
                        }
                    }
                });

    }

    @Override
    public void onClick(View v) {
        Intent cameraApp = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        activityResultLauncher.launch(cameraApp);
    }

    private void detectSmile(){
        //This is using firebase ml kit api.

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                .getVisionFaceDetector();

        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.NO_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .build();

        detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options);

        detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionFace> faces) {
                        for (FirebaseVisionFace face : faces) {
                            float smileProbability = face.getSmilingProbability();
                            if (smileProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                if (smileProbability > 0.5) {
                                    // Detected a smile
                                    Toast.makeText(requireContext(), "Smile detected!", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Did not detect a smile
                                    Toast.makeText(requireContext(), "No smile detected.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Smile probability was not computed
                                Toast.makeText(requireContext(), "Smile probability not computed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle error
                    }
                });

    }
}