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
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.util.List;

public class CheckSmile extends Fragment implements View.OnClickListener, OnSuccessListener<List<FirebaseVisionFace>>, OnFailureListener{

    private Button openCameraButton;
    private ImageView userPhotoImageView;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    private Bitmap bitmap;

    //the minimum probability for detecting smile & open eyes.
    private final float MINIMUM_PROBABILITY = 0.5f;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_check_smile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        openCameraButton = view.findViewById(R.id.open_camera_button);
        userPhotoImageView = view.findViewById(R.id.user_photo_image_view);
        openCameraButton.setOnClickListener(this);

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
                            userPhotoImageView.setImageBitmap(bitmap);
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
        //This is using Firebase ml kit api.

        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);

        //Initialize the FaceDetectorOptions.
        FirebaseVisionFaceDetectorOptions faceDetectorOptions = new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .build();

        //Initialize the face FaceDetector. so that we will be able to use the "detectInImage" function & onSuccess Interface to process the faces.
        FirebaseVisionFaceDetector faceDetector = FirebaseVision.getInstance()
                .getVisionFaceDetector(faceDetectorOptions);

        faceDetector.detectInImage(firebaseVisionImage).addOnSuccessListener(this).addOnFailureListener(this);
    }

    //It is used to iterate all the faces in the current image
    @Override
    public void onSuccess(List<FirebaseVisionFace> faces) {

        //Because I only need to detect one face.
        if(faces.size() == 1) {

            FirebaseVisionFace face = faces.get(0);
            float smileProbability = face.getSmilingProbability();
            if (smileProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY)
                if (smileProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY
                        && smileProbability > MINIMUM_PROBABILITY) {

                    //Stop alarm service
                    getActivity().stopService(new Intent(getContext(), AlarmService.class));

                    //Close the activity.
                    getActivity().finish();

                } else {
                    Toast.makeText(requireContext(), "לא זוהה חיוך בבקשה נסה שוב", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        //If we are not able to detect the image.

        //Might happen when the Image is too small or too large, the image quality is too low etc.
        Toast.makeText(requireContext(), "לא הצלחנו לזהות את התמונה שלך. בבקשה צלם שוב.", Toast.LENGTH_LONG).show();
    }
}