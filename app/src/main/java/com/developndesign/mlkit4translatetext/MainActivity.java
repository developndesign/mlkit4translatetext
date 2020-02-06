package com.developndesign.mlkit4translatetext;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentification;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateRemoteModel;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.Set;

public class MainActivity extends AppCompatActivity {
    EditText sourceLanguageText; //source language
    TextView targetLanguageText;  //target language
    Button button;
    ProgressDialog progressDialog; //show progress bar during translation process

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sourceLanguageText = findViewById(R.id.source);
        targetLanguageText = findViewById(R.id.target);
        button = findViewById(R.id.button);
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setCanceledOnTouchOutside(false);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Please wait...");
                progressDialog.show();
                findLanguageOfText(sourceLanguageText.getText().toString());
            }
        });
    }

    private void findLanguageOfText(final String sourceText) {
        FirebaseLanguageIdentification languageIdentifier = FirebaseNaturalLanguage.getInstance()
                .getLanguageIdentification();
        languageIdentifier.identifyLanguage(sourceText)
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(@Nullable String languageCode) {
                        if (languageCode != null) {
                            if (!languageCode.equals("und")) {
                                //pass source Text and language code of source text
                                progressDialog.cancel();
                                translateTextOfLanguage(sourceText, languageCode);
                            } else {
                                targetLanguageText.setText("Can't identify language.");
                            }
                        }
                    }
                });
    }

    private void translateTextOfLanguage(final String sourceText, String languageCode) {
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(FirebaseTranslateLanguage.languageForLanguageCode(languageCode))
                .setTargetLanguage(FirebaseTranslateLanguage.EN)
                .build();
        final FirebaseTranslator englishTranslator = FirebaseNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().requireWifi()
                .build();
        //download model first
        englishTranslator.downloadModelIfNeeded().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void v) {
                englishTranslator.translate(sourceText).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(@NonNull String translated) {
                        // Translation successful.
                        progressDialog.cancel();
                        targetLanguageText.setText(translated);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "e 2 "+e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "e 1 "+e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void manageTranslationModel() {
        FirebaseModelManager modelManager = FirebaseModelManager.getInstance();
// Get translation models stored on the device.
        modelManager.getDownloadedModels(FirebaseTranslateRemoteModel.class)
                .addOnSuccessListener(new OnSuccessListener<Set<FirebaseTranslateRemoteModel>>() {
                    @Override
                    public void onSuccess(Set<FirebaseTranslateRemoteModel> models) {
                        // ...
                    }
                });

// Delete the German model if it's on the device.
        FirebaseTranslateRemoteModel deModel =
                new FirebaseTranslateRemoteModel.Builder(FirebaseTranslateLanguage.DE).build();
        modelManager.deleteDownloadedModel(deModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void v) {
                        // Model deleted.
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error.
                    }
                });

// Download the French model.
        FirebaseTranslateRemoteModel frModel =
                new FirebaseTranslateRemoteModel.Builder(FirebaseTranslateLanguage.FR).build();
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        modelManager.download(frModel, conditions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void v) {
                        // Model downloaded.
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error.
                    }
                });
    }
}
