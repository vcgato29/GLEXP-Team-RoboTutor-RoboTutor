package cmu.xprize.ak_component;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by jacky on 2016/7/1.
 */

public class CAkQuestionBoard extends View {
    public int leftNum, rightNum;
    protected float textSize;

    public String[] choices;
    public CAkPlayer.Lane answerLane;

    private Bitmap roadSign;

    public CAkQuestionBoard(Context context, CAkPlayer.Lane answerLane, String[] choices) {
        super(context);
        init(answerLane, choices);
    }

    public CAkQuestionBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(null, null);

    }

    public CAkQuestionBoard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(null, null);
    }

    protected void init(CAkPlayer.Lane answerLane, String[] choices) {
        this.choices = choices;
        this.answerLane = answerLane;
        roadSign = BitmapFactory.decodeResource(getResources(), R.drawable.roadsignb);
        textSize = 20;
    }

    @Override
    public void onDraw(Canvas canvas) {


        Paint vPaint = new Paint();
        vPaint.setAlpha( 220 );
        int width = getWidth();
        int height = getHeight();

        vPaint.setTextAlign(Paint.Align.CENTER);
        vPaint.setColor(Color.BLACK);
        vPaint.setTextSize(textSize);

        roadSign = Bitmap.createScaledBitmap(roadSign, width / 3, height, false);

        switch (choices.length) {
            case 1:


                canvas.drawBitmap(roadSign, width / 3, 0, vPaint);
                canvas.drawText(String.valueOf(choices[0]), width / 2, height * 5.0f / 12, vPaint);
                break;
            case 2:
                canvas.drawBitmap(roadSign, width / 12, 0, vPaint);
                canvas.drawText(String.valueOf(choices[0]), width * 6.0f / 24, height * 5.0f / 12, vPaint);

                Matrix matrix1 = new Matrix();
                matrix1.preScale(-1, 1);
                canvas.drawBitmap(Bitmap.createBitmap(roadSign, 0, 0,
                        roadSign.getWidth(),roadSign.getHeight(), matrix1, false),
                        width * 7.0f / 12, 0, vPaint);
                canvas.drawText(String.valueOf(choices[1]), width * 18.0f / 24, height * 5.0f / 12, vPaint);
                break;
            case 3:
                canvas.drawBitmap(roadSign, 0, 0, vPaint);
                canvas.drawText(String.valueOf(choices[0]), width / 6, height * 5.0f / 12, vPaint);

                canvas.drawBitmap(roadSign, width / 3, 0, vPaint);
                canvas.drawText(String.valueOf(choices[1]), width / 2, height * 5.0f / 12, vPaint);

                Matrix matrix2 = new Matrix();
                matrix2.preScale(-1, 1);
                canvas.drawBitmap(Bitmap.createBitmap(roadSign, 0, 0,
                        roadSign.getWidth(),roadSign.getHeight(), matrix2, false),
                        width * 2 / 3, 0, vPaint);
                canvas.drawText(String.valueOf(choices[2]), width * 5.0f / 6, height * 5.0f / 12, vPaint);
                break;
        }

    }

}
