package com.blank_learn.newversion.Quiz;

import android.os.Parcel;
import android.os.Parcelable;

public class QuizReviewModel implements Parcelable {
    private String question;
    private String userAnswer;
    private String correctAnswer;
    private String explanation;
    private boolean isCorrect;

    // Primary Constructor
    public QuizReviewModel(String question, String userAnswer, String correctAnswer, String explanation, boolean isCorrect) {
        this.question = question;
        this.userAnswer = userAnswer;
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
        this.isCorrect = isCorrect;
    }

    // --- Getters ---
    public String getQuestion() { return question; }
    public String getUserAnswer() { return userAnswer; }
    public String getCorrectAnswer() { return correctAnswer; }
    public String getExplanation() { return explanation; }
    public boolean isCorrect() { return isCorrect; }


    // =======================================================
    // Parcelable Implementation
    // =======================================================

    // Constructor used for reading from Parcel
    protected QuizReviewModel(Parcel in) {
        question = in.readString();
        userAnswer = in.readString();
        correctAnswer = in.readString();
        explanation = in.readString();
        // Boolean is read as a byte
        isCorrect = in.readByte() != 0;
    }

    // CREATOR field
    public static final Creator<QuizReviewModel> CREATOR = new Creator<QuizReviewModel>() {
        @Override
        public QuizReviewModel createFromParcel(Parcel in) {
            return new QuizReviewModel(in);
        }

        @Override
        public QuizReviewModel[] newArray(int size) {
            return new QuizReviewModel[size];
        }
    };

    // Write fields to Parcel
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(question);
        dest.writeString(userAnswer);
        dest.writeString(correctAnswer);
        dest.writeString(explanation);
        // Boolean is written as a byte (1 for true, 0 for false)
        dest.writeByte((byte) (isCorrect ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }
}